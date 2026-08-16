# Findings: `CAP-010` (Group W retry, 18:30 — GATT discovery via nRF Connect)

Standardized, evidence-based extraction from `CAP-010-btsnoop_hci.log` + `CAP-010-recording.mp4`,
staged here for later promotion into `PROTOCOL.md` per `PROJECT_RULES.md` §2. This is a **second,
independent `CAP-010` capture**, distinct from
`captures/CAP-010-2026-08-16_11-42-31_11-45-01-Group_W/CAP-010-FINDINGS.md` (the 11:42 attempt).
Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number or video timestamp.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-010` (18:30 session) · **Date:** 2026-08-16 · **Firmware:** `release_5.203`
(not re-confirmed on-the-wire this session — see §4) · **Phone:** Pixel 7a, Android 17 — same
physical phone as `CAP-001`–`CAP-007`/the 11:42 `CAP-010`. **GATT client: nRF Connect for Mobile
(Nordic Semiconductor)**, not the official Pixel Buds Companion App. **Peer device:** `04:00:6E:CF:6E:07`
("Pixel Buds Pro 2 van Ted", classic EIR name match). **Log file:** `CAP-010-btsnoop_hci.log`
(559.2s, 1,747 packets, 18:31:32.72–18:40:51.93 local/+0200). **Video:** `CAP-010-recording.mp4`
(419.6s, 18:30:12–18:37:12 local, on-screen wall-clock overlay).

**Stated goal of this session** (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1 Group W /
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `GATT-001` row): trigger a genuine, live GATT
primary-service/characteristic discovery against the Buds. **Result: achieved — for the first time
in this project — but not via either of Group W's originally-defined methods (`pm clear
com.android.bluetooth` or a first-time connection from the Pixel 9a), and not fully recoverable
from the wire capture itself.** See §1–§3.

---

## 1. Methodology: what actually triggered discovery this time (🟢 FACT)

This matters to state precisely, because it does not match either candidate method Group W was
defined around (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` `GATT-001` row; `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
§9 `CAP-014` row):

- The video shows the phone's Bluetooth toggled off then on (18:30:16), then **nRF Connect for
  Mobile** (Nordic Semiconductor's generic BLE scanner/GATT-browser app) launched (18:30:28) — not
  the official Pixel Buds Companion App, not a browser/WebBluetooth flow.
- No `pm clear com.android.bluetooth` was run (no evidence of it on screen, and other bonded
  devices are not the subject of this test either way — this check is less conclusive here than in
  the 11:42 capture since the quick-settings panel isn't shown long enough to confirm other saved
  devices survived).
- The Pixel 9a was **not** used — this is the same Pixel 7a as every prior capture.
- What actually differs from every prior `CAP-010`/`CAP-002`/`CAP-003`/`CAP-004` attempt: **a
  brand-new GATT client app (nRF Connect) that had never before connected to this peripheral**
  called `connectGatt()`/`discoverServices()` fresh. Android's GATT attribute-table cache is keyed
  per `(local app UID, peer BD_ADDR)` — a new client UID has no cache to hit, forcing a real
  `Read By Group Type` walk regardless of whether the *device's* classic bond or the *official
  app's* own cache is fresh. This is consistent with, but a third, previously-untried variant of,
  the caching mechanism already characterized in `CAP-003-FINDINGS.md` §1.
- **Consequence for the project's test plan:** `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `GATT-001` row
  and `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s planned `CAP-014` currently frame `pm clear`/Pixel-9a as
  the only two untried paths to real discovery. This capture demonstrates a third, now-proven path
  (any never-before-used GATT client app) — worth reflecting in those documents (see §5).

Reproduction of the connection event:
```
tshark -r CAP-010-btsnoop_hci.log -Y "bthci_evt.le_meta_subevent==0x0a" \
  -T fields -e frame.number -e frame.time -e bthci_evt.bd_addr -e bthci_evt.status -e bthci_evt.connection_handle
```
→ `652	2026-08-16T18:31:39.512655+0200	04:00:6e:cf:6e:07	0x00	0x0002` — matches the video's
on-screen `CONNECT` tap at 18:31:39 to the second.

## 2. The wire capture cannot answer its own question: severe ACL truncation (🟢 FACT)

Unlike every prior `CAP-010`/`CAP-002`/`CAP-003`/`CAP-004` attempt, this log **does** contain live
discovery traffic:

```
tshark -r CAP-010-btsnoop_hci.log -Y "btatt.opcode in {0x04,0x05,0x08,0x09,0x10,0x11}" \
  -T fields -e frame.number -e btatt.opcode | wc -l
```
→ **137 matches** (vs. zero in the 11:42 `CAP-010`, `CAP-002`, `CAP-003`). This alone is a real,
positive result: a live `Read By Group Type`/`Read By Type`/`Find Information` walk of the full
`0x0001–0xffff` handle range did happen on the wire, twice (once right after the BLE connection at
18:31:39, once again right after classic pairing completes at 18:32:58).

**But the log cannot be used to read the actual discovered UUIDs.** `CAP-010-btsnoop_hci.log` was
recorded with a very short per-packet snapshot length — most ACL frames carrying ATT discovery
responses are truncated to their first ~15 captured bytes regardless of true length:

```
tshark -r CAP-010-btsnoop_hci.log -Y "frame.number==680" -V
```
Relevant excerpt:
```
Frame Length: 71 bytes (568 bits)
Capture Length: 15 bytes (120 bits)
...
Bluetooth L2CAP Protocol
    Length: 62
        [Expert Info (Warning/Malformed): Length too short]
Bluetooth Attribute Protocol
    Opcode: Read By Group Type Response (0x11)
    Length: 6
```
Raw bytes actually captured for this frame (`Read By Group Type Response` to the very first
service-discovery request):
```
0000  02 02 20 42 00 3e 00 04 00 11 06 01 00 09 00
```
— 15 bytes total (HCI ACL header 4B + L2CAP header 4B + only 6 of the true 62 L2CAP-payload bytes:
opcode `0x11`, one declared "attribute data length" byte `06`, then only 2 of what should be many
more bytes of handle-range/UUID data). Every one of this session's `Read By Group Type Response`
(4/4), `Read By Type Response` (20/22), and `Read By Type Request` (52/52) frames is truncated the
same way:

```
tshark -r CAP-010-btsnoop_hci.log -Y "btatt.opcode==0x11 or btatt.opcode==0x09" \
  -T fields -e frame.number -e frame.cap_len -e frame.len | awk -F'\t' '$2!=$3' | wc -l
```
→ 24 of 26 truncated. **Zero-Creativity rule applied: no UUID is reconstructed from these
truncated frames.** Handles that survive intact — because `Read`/`Write`/`Find Information`
requests against a single known handle are short enough to fit inside the snapshot limit — are
reported in §4 below with full byte values.

**This is a capture-tooling limitation, not a protocol fact about the Buds.** Whatever forced the
short snaplen (btsnoop buffer settings on this specific export, or the export tool used) needs to
be fixed before a future capture can extract 128-bit UUIDs and handle-range data directly from the
wire. Recommended fix in §5.

## 3. The GATT profile — recovered from the video, not the log (🟢 FACT)

Because nRF Connect's own UI reflects exactly what its GATT client resolved over the air
(independent of what our truncated capture recorded), the full service list was read directly from
video frames at 18:31:41–53 (top of list, unscrolled) and 18:33:22–29 (scrolled to the bottom).
Frames were cropped/upscaled for legibility; each UUID below was cross-checked against at least two
separate extracted frames.

**Full primary/secondary service list, in on-screen (= discovery/handle) order:**

| # | Service name (nRF Connect's label) | UUID | Type |
|---|---|---|---|
| 1 | Generic Attribute | `0x1801` | PRIMARY |
| 2 | Generic Access | `0x1800` | PRIMARY |
| 3 | Broadcast Audio Scan Service | `0x184F` | PRIMARY |
| 4 | Audio Stream Control Service | `0x184E` | PRIMARY |
| 5 | Published Audio Capabilities Service | `0x1850` | PRIMARY |
| 6 | Volume Control | `0x1844` | PRIMARY |
| 7 | Microphone Control | `0x184D` | PRIMARY |
| 8 | Audio Input Control | `0x1843` | **SECONDARY** |
| 9 | Common Audio Service | `0x1853` | PRIMARY |
| 10 | Telephony and Media Audio Service | `0x1855` | PRIMARY |
| 11 | **Google Fast Pair Service** | `0xFE2C` | PRIMARY |
| 12 | **Accessory Non-Owner Service** | `15190001-12f4-c226-88ed-2ac5579f2a85` | PRIMARY |
| 13 | Device Information | `0x180A` | PRIMARY |
| 14 | Battery Service | `0x180F` | PRIMARY |
| 15 | **Unknown Service** (nRF has no name for this one) | `109b862f-50e3-45cc-8ea1-ac62de4846d1` | PRIMARY |

Extraction method (reproducible against the raw video file):
```
ffmpeg -ss <t> -i CAP-010-recording.mp4 -frames:v 1 -vf "crop=540:640:170:590,scale=1080:1280" -q:v 1 out.jpg
```
at `t=189.0`, `189.6`, `190.0`, `190.5` (18:33:21–18:33:23), which agree byte-for-byte on both
128-bit UUIDs; entries 1–8 (unscrolled) confirmed independently at `t=87..115` (18:31:39–18:32:11)
across five separate frames.

**Cross-referenced against this project's own records:** `grep -rn "Accessory Non-Owner\|15190001\|109b862f\|FE2C" DESKRESEARCH_FINDINGS.md REVERSE_ENGINEERING.md PROTOCOL.md` returns **no
prior hits** — both 128-bit UUIDs are new to this project as of this capture. `0xFE2C` (Google
Fast Pair Service) was already an ⚪ assumption/named hypothesis in `CAP-004-FINDINGS.md` §6; this
is the first direct confirmation it's actually present on this device.

**What "Accessory Non-Owner Service" vs. "Unknown Service" tells us:** nRF Connect ships a bundled
UUID name database. It successfully names `15190001-12f4-c226-88ed-2ac5579f2a85` — meaning that
UUID is registered somewhere nRF's database draws from (consistent with it being a documented,
if narrow-audience, Google Fast Pair "non-owner"/anti-stalking feature service). It has **no** name
for `109b862f-50e3-45cc-8ea1-ac62de4846d1` — this one is either undocumented publicly or too new
for nRF's bundled database, and is the more likely candidate for whatever proprietary,
device-specific control channel underlies the `0x0c0X`/`0x0f2X` handle cluster this project has
been chasing since `CAP-002` (🟡 HYPOTHESIS — see §4).

**Handle numbers themselves were not captured** — nRF Connect's summary list shows service name +
UUID + type only, not the handle range. Tapping into a service to view its characteristics (which
does show handles) did not happen on screen this session. This is the natural next step (§5).

## 4. Byte-level detail that *did* survive truncation (🟢 FACT)

A handful of ATT operations are short enough (single known handle, ≤~10-byte payload) to fit
inside the ~15-byte snapshot limit and were captured intact:

```
tshark -r CAP-010-btsnoop_hci.log -Y "btatt.opcode in {0x0a,0x0b,0x12,0x13} and frame.cap_len==frame.len" \
  -T fields -e frame.number -e frame.time -e btatt.opcode -e btatt.handle -e btatt.value
```

| Frame(s) | Time | Handle | Operation | Value (hex) | Note |
|---|---|---|---|---|---|
| 911→913 | 18:31:41.958→41.990 | `0x0f28` | Read Req/Resp | `31` | Same value as `CAP-002`, `CAP-003`, the 11:42 `CAP-010` — 5th confirming session |
| 1570→1572 | 18:33:05.660→05.710 | `0x0f28` | Read Req/Resp | `31` | Re-read after classic pairing; identical value |
| 1583→1604 | 18:33:07.650→07.689 | `0x0f32` | Read Req/Resp | `64` | **New handle, not seen in any prior capture of this device** |
| 1605→1615 | 18:33:07.694→07.749 | `0x0f33` | Write Req/Resp | `0100` | CCCD-enable (Notify) immediately following the `0x0f32` read — standard "read once, then subscribe" pattern, consistent with `0x0f33` being `0x0f32`'s Client Characteristic Configuration Descriptor at handle+1 |
| 1043→1072 | 18:32:58.478→58.538 | `0x0005` | Write Req/Resp | `07` | Fires immediately after classic `Connect Complete` (frame 994) |
| 1088→1090 | 18:32:58.660→58.719 | — / `0x0003` | Find Info Req/Resp | — | Resolves handle `0x0003` to UUID `0x2A05` (**Service Changed**) — confirms the `0x0005` write above is the Service-Changed-indication CCCD being (re-)enabled after a fresh classic bond, standard GATT-caching housekeeping (`CAP-003-FINDINGS.md` §1) |

Raw bytes for the two writes, reproducible via `tshark -r CAP-010-btsnoop_hci.log -Y "frame.number==1605" -x`:
```
0000  02 02 00 09 00 05 00 04 00 12 33 0f 01 00      (Write Req: opcode 12, handle 0x0f33 LE, value 0001)
```

**`0x0f32`/`0x0f33` is a genuinely new observation.** Every prior capture (`CAP-002`, `CAP-003`,
11:42 `CAP-010`) only ever showed `0x0f28` and `0x0f2a` in this immediate neighborhood; `0x0f2a`
was **not** the target of any successful Read Request/Response this session — nRF Connect never
triggered the "Revision 6" read that the official app always does. The only frame mentioning
`0x0f2a` at all is an incidental `Error Response - Attribute Not Found` (frame 884, 18:31:41.705,
part of a `Read By Type Request` probe during discovery, not a deliberate value read). 🟡
HYPOTHESIS: `0x0f32` sits inside the same
high-handle cluster as `0x0f28`/`0x0f2a`, plausibly still under the **Device Information** service
(`0x180A`, confirmed present in §3) given the proximity — but this is ordering intuition, not a
captured handle-range boundary, and is **not** confirmed by this capture.

Handle `0x0f2a`'s absence and the classic-pairing-triggered `0x0003`/`0x0005` Service-Changed
exchange (which never appeared in any prior capture either, because prior sessions used the
official app, which apparently doesn't re-touch it) are both first-time observations specific to
nRF Connect's client behavior, not new facts about the Buds' own GATT server layout.

**No application-level read/write/notify traffic on the `0x0c0X` cluster this session** — expected,
since that activity is driven by the official Companion App's Fast Pair Key-based-Pairing flow
(`CAP-002`/`CAP-003`/11:42-`CAP-010`), which never ran here (no DLCI 0x08 Message-Stream handshake
either — confirmed: `tshark -r CAP-010-btsnoop_hci.log -Y 'frame contains "release_5.203"'` → 0
matches, vs. 5 confirming sessions previously).

**However, the discovery walk itself did pass directly through this cluster's handle range, and
this is the first time any capture has independently confirmed part of its structure via real GATT
discovery rather than write-pattern inference (🟢 FACT, upgraded from 🟡):**

```
tshark -r CAP-010-btsnoop_hci.log -Y "btatt.handle >= 0x0c00 and btatt.handle <= 0x0c20 and btatt.opcode==0x05" \
  -T fields -e frame.number -e frame.time -e btatt.handle -e btatt.uuid16 -e frame.cap_len -e frame.len
```

| Frame | Time | Handle | Resolved UUID | Captured intact? |
|---|---|---|---|---|
| 854 | 18:31:41.406 | `0x0c05` | `0x2902` (Client Characteristic Configuration) | Yes (15/15 bytes) |
| 857 | 18:31:41.435 | `0x0c08` | `0x2902` | Yes |
| 860 | 18:31:41.465 | `0x0c0d` | `0x2902` | Yes |
| 863 | 18:31:41.495 | `0x0c14` | `0x2902` | Yes |
| 875 | 18:31:41.615 | `0x0c18` | `0x2902` | Yes |

These `Find Information Response` frames are short enough (single 2-byte 16-bit UUID) to survive
the snaplen truncation intact. `0x0c05` and `0x0c14` were already suspected to be CCCDs purely from
their write-pattern in `CAP-002`/`CAP-003`/11:42-`CAP-010` (a 2-byte `0100`/`0000` write immediately
preceding/following the cluster's encrypted writes) — **this is now directly confirmed**, not
inferred. `0x0c08` and `0x0c18` are new: `0x0c08` was previously only seen paired with `0x0c07` in
`CAP-003-FINDINGS.md` §4 without confirmation it was a CCCD; `0x0c18` has not appeared in any prior
capture's handle list at all — a sixth handle to add to this project's known cluster. A handful of
`Error Response - Attribute Not Found` frames in the same range (`0x0c00`, `0x0c13`, `0x0c15`,
`0x0c17`) are artifacts of the discovery walk's blind probing, not evidence about those specific
handles. This session traded the cluster's *application-level* activity for this *structural*
confirmation instead — a different, complementary kind of evidence.

## 4b. Verification pass: can the handle↔UUID gap be closed from this capture? (🟢 FACT — no)

A dedicated re-review was done specifically to check whether §3/§4's open handle↔UUID mapping could
be resolved from material already in hand (video frames not previously extracted, or an in-app log
view independent of the truncated btsnoop capture), before concluding a recapture is required.

**Method:** the full video was sampled at 5s resolution (84 frames, 18:30:12–18:37:12) via
```
ffmpeg -i CAP-010-recording.mp4 -vf "select='lt(t\,210)*not(mod(floor(t/5)\,1))',fps=1/5,scale=160:284,tile=6x7" -frames:v 1 sheetA.jpg
ffmpeg -ss 210 -i CAP-010-recording.mp4 -vf "fps=1/5,scale=160:284,tile=6x7" -frames:v 1 sheetB.jpg
```
and reviewed as two contact sheets, with full-resolution re-extracts of every visually distinct
screen for close reading.

**Findings:**
- The device screen in this nRF Connect build exposes only **`CLIENT` / `SERVER`** tabs once
  connected (confirmed at 18:31:43 and 18:31:55, full-res) — there is no `LOG` tab or raw-ATT-hex
  view in this UI that could substitute for the truncated btsnoop capture.
- The `CLIENT` tab's service list (§3's 15 entries) stays a **flat, unexpanded list** —
  service name / UUID / PRIMARY-or-SECONDARY only — for the entirety of both windows it's on
  screen (18:31:41–53, 18:33:22–29). No tap-to-expand into any service's characteristics is visible
  anywhere in the 420s recording.
- The screen visible 18:34:30–18:36:52 (previously identified as the Bonded/Advertiser entry) is
  confirmed, on full-resolution re-extraction (18:34:30 and 18:34:40), to be the **`HISTORY`** tab
  of the advertiser detail view — an RSSI-vs-time scatter plot plus a scrollable log of individual
  advertising packets (color-coded per packet, which is what made it look list-like at thumbnail
  resolution) — not a GATT characteristic tree. It does reconfirm `Service Data: UUID: 0x1853` and
  `0x184E`/`0x184F` from the advertising payload itself, consistent with §3, but carries no handle
  information.
- No other frame in the 84-frame sample shows a `Handle:`/`Properties:` style characteristic
  detail view.

**Conclusion:** the handle↔UUID mapping is not recoverable from this session's existing artifacts —
not the log (truncated, §2) and not the video (no drill-down ever happened on screen, confirmed
above). This is not a gap in this analysis pass; it's a gap in what was captured. Closing it
requires a new capture (see §5's recommended next step, which already specified this before this
verification pass and remains unchanged).

## 5. Conclusions & next steps

- **`GATT-001`'s core goal — trigger genuine live discovery — is achieved for the first time in
  this project (🟢 FACT)**, via a method (fresh third-party GATT client app) not previously
  identified as a candidate in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`/`CAPTURE_BLUETOOTH_HCI_SNOOP.md`.
  Recommend updating both documents' `GATT-001`/`CAP-014` entries to record this as a third proven
  path, alongside the still-untried `pm clear`/Pixel-9a options.
- **The full 15-service primary/secondary GATT profile is now known (🟢 FACT, §3)** — five
  standard LE Audio services (BASS/ASCS/PACS/VCS/MICS/AICS/CAS/TMAS-family), the standard
  Device Information and Battery services, the Google Fast Pair Service (`0xFE2C`), and two
  previously-undocumented 128-bit proprietary services (`15190001-…` "Accessory Non-Owner
  Service", `109b862f-…` "Unknown Service").
- **The `0x0c0X` cluster's CCCD structure is now independently confirmed via real discovery, not
  just write-pattern inference (🟢 FACT, §4)** — `0x0c05`/`0x0c0d`/`0x0c14` resolve to `0x2902`
  (Client Characteristic Configuration) directly, plus two new handles in the same cluster
  (`0x0c08`, `0x0c18`) not previously documented by any capture.
- **The handle→UUID mapping this project has wanted since `CAP-002` remains 🔴 OPEN** — this
  capture proves *which UUIDs exist* but not *which handle range each occupies*, because (a) the
  wire log is truncated below the level needed to read `Read By Group Type` handle-range data, and
  (b) nRF Connect's summary list doesn't display handles, and no characteristic-level drill-down
  happened on screen this session.
- **Recommended immediate next step (highest value, lowest effort):** re-run this exact
  nRF-Connect-against-the-Buds procedure once more, this time (1) fixing whatever produced the
  ~15-byte ACL snaplen so the wire capture can actually carry full discovery-response payloads —
  check the export path/tool used to produce `CAP-010-btsnoop_hci.log` against the one used for
  earlier captures (`CAP-001`–`CAP-004`, which were **not** truncated this way), and (2) tapping
  into "Accessory Non-Owner Service" and especially "Unknown Service" in nRF Connect's CLIENT tab
  to read their characteristics and handles on screen — this alone would very likely resolve the
  `0x0c0X`/`0x0f2X` cluster's UUID identity that four prior captures have failed to obtain.
- Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Capture Index with this second `CAP-010` session
  (folder `captures/CAP-010-2026-08-16_18-30-12_18-37-12-Group_W/`) — the existing `CAP-010` row
  currently only describes the 11:42 attempt.

## 6. Open Questions

- 🔴 Handle ranges for all 15 services in §3 — genuinely unresolved by this capture; confirmed
  unrecoverable from either the log or the video by a dedicated verification pass (§4b), not just
  unresolved by omission. A recapture per §5 is required.
- 🔴 Whether `109b862f-50e3-45cc-8ea1-ac62de4846d1` ("Unknown Service") is the container for the
  `0x0c0X` cluster already characterized by byte-shape in `CAP-002`/`CAP-003`/11:42-`CAP-010` — 🟡
  plausible given it's the one 128-bit UUID nRF's own database can't name, but not tested here.
- 🔴 What `0x0f32` (value `0x64`) represents, and why it and its CCCD `0x0f33` appear only in this
  session, never in any capture driven by the official app.
- 🔴 Whether the short-ACL-snaplen issue in `CAP-010-btsnoop_hci.log` is specific to this one
  export or affects the btsnoop-capture procedure going forward — needs checking before the next
  capture is trusted to carry full discovery payloads.
- 🔴 Whether `0x0f2a` ("Revision 6") is genuinely absent from nRF Connect's own read pattern, or
  simply wasn't triggered because no characteristic-level read happened on screen this session
  (§3's last paragraph) — the service list alone doesn't tell us whether nRF Connect read every
  characteristic's value or just enumerated the declarations.
