# Findings: `CAP-010` (Group W — GATT cache-busting attempt)

Standardized, evidence-based extraction from `CAP-010-btsnoop_hci.log` + `CAP-010-recording.mp4`,
staged here for later promotion directly into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Modeled on
`captures/CAP-003-2026-08-10_20-59-16_21-00-37-Group_R/CAP-003-FINDINGS.md` and
`captures/CAP-004-2026-08-11_06-22-36_06-25-12-Group_S/CAP-004-FINDINGS.md`. Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-010` · **Date:** 2026-08-16 · **Firmware:** `release_5.203` (confirmed
on-the-wire this session too — §5) · **Phone:** Pixel 7a, Android 17 — same physical phone as
`CAP-001`–`CAP-007` (BD_ADDR `E8:D5:2B:7E:CA:81` / `Google_7e:ca:81`, visible on-screen
11:42:58–11:43:03), system Bluetooth Settings + official Pixel Buds Companion App only (no BLE
tool, no WebBluetooth, no `pm clear`). **Log file:** `CAP-010-btsnoop_hci.log` (350.7s, 2,903
packets, 2026-08-16 11:41:45.67–11:47:36.40 local/+0200). **Video:** `CAP-010-recording.mp4`
(149.9s, 11:42:31–~11:45:01 local, on-screen wall-clock overlay). **Devices:** phone
`Google_7e:ca:81` (Pixel 7a), peer `Google_cf:6e:07` (`04:00:6E:CF:6E:07`, the same physical
Buds/case used in every prior capture in this project — confirmed via classic EIR name match,
frame 652).

**Stated goal of this session, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1 Group W /
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `GATT-001` row:** trigger a genuine, *live* GATT
primary-service/characteristic discovery against the Buds, using a stronger cache-busting method
than Group R's "just remove the bond" — which `CAP-003-FINDINGS.md` §1 and `CAP-004-FINDINGS.md`
§6 already showed fails, 3-for-3 — via either (a) `adb shell pm clear com.android.bluetooth` on
this phone, or (b) discovery from a phone that has never connected to this device before (the
project's Pixel 9a). **§1 shows neither option was actually exercised in this capture, and — as a
direct, evidenced consequence, not a coincidence — the same negative result reproduces a fourth
time (§2).**

---

## 1. Procedure check: this capture does not execute Group W's defined method (🟢 FACT)

This matters enough to state before anything else, because the task that produced this capture's
metadata draft assumed a "WebBluetooth/third-party GATT discovery tool" session had taken place —
**it had not.** Checked directly, three independent ways:

- **On-screen evidence:** the entire video shows only stock Android UI — the system "Pair new
  device" screen, the standard SSP "Pair with Pixel Buds Pro 2 van Ted?" dialog, the official
  Pixel Buds Companion App's setup flow, ending on its "Device details" page (sound/ANC/EQ,
  Hearing wellness, More settings). No BLE scanner app (nRF Connect, LightBlue, etc.), no browser,
  no WebBluetooth `chooser` prompt appears anywhere.
- **The Bluetooth quick-settings panel at 11:42:38 still lists three unrelated, previously-bonded
  devices as `Saved`** (`Charge 6`, `Light-HD`, `OpenMeet by Shokz`). `pm clear
  com.android.bluetooth` wipes **all** bonded devices on the phone, not just the Buds' — their
  survival here is direct, positive proof that command was not run this session.
- **The classic pairing state machine is byte-for-byte the same "remove bond, re-pair on the same
  phone" pattern already documented for `CAP-002`/`CAP-003`** (`Delete Stored Link Key` frame 1236
  → `Create Connection` frame 1238 → IO Capability/SSP → `Simple Pairing Complete` frame 1305,
  `PROTOCOL.md` §5.1) — mechanically identical to Group R's already-tried, already-failed method,
  not Group W's untried candidates, despite the folder label.

**Consequence:** the negative discovery result in §2 below is the *expected* outcome of running
an already-known-insufficient method a fourth time — it is not new evidence that Group W's own
candidate methods (`pm clear`, or the Pixel 9a) would also fail. Neither has actually been tried
yet.

## 2. Primary goal not achieved (again): zero live GATT discovery traffic — 4th consecutive negative result (🔴 OPEN QUESTION, sharper framing)

Checked exhaustively across the **entire** log, not just the video-covered window:

```
tshark -r CAP-010-btsnoop_hci.log -Y "btatt.opcode in {0x04,0x05,0x10,0x11}"
```

→ **zero matches.** No `Find Information` (`0x04`/`0x05` — characteristic/descriptor discovery)
and no `Read By Group Type` (`0x10`/`0x11` — primary service discovery) request or response
exists anywhere in this 2,903-packet, 350.7s capture.

Every one of the 35 ATT packets that *does* exist —

```
tshark -r CAP-010-btsnoop_hci.log -Y "btatt" -T fields -e frame.number -e frame.time -e btatt.opcode -e _ws.col.Info
```

— is one of three kinds, none of which is discovery:

1. **Three `Read By Type Request/Response` pairs for standard, well-known 16-bit GAP/GATT UUIDs**
   (`Device Name` `0x2a00` — frames 1988/1994; `Database Hash` `0x2b2a` — frames 1995/1997;
   `Appearance` `0x2a01` — frames 1998/2002), each issued directly against the full
   `0x0001..0xffff` handle range. This is the standard **"Read Using Characteristic UUID"** ATT
   sub-procedure (Bluetooth Core Spec v5.x, Vol 3 Part G §4.3.3) — it lets a client fetch a
   *specific, already-known* UUID's value without walking the attribute table at all. It cannot
   enumerate anything the client doesn't already know the UUID for, and it is not evidence of
   discovery.
2. **The `Database Hash` read is specifically the standard GATT Caching mechanism** (Core Spec
   5.1+, already characterized in `CAP-003-FINDINGS.md` §1) — a client uses it to check whether
   its cached database is still valid, skipping full discovery if the hash matches.
3. **Ten `Read`/`Write`/`Handle Value Notification` exchanges against raw, literal 16-bit
   handles** (`0x0f28`, `0x0f2a`, `0x0c04`, `0x0c05`, `0x0c0a`, `0x0c0c`, `0x0c0d`, `0x0c13`,
   `0x0c14`) — see §3. Accessing a handle directly by number is only possible if the client
   already knows that number; no ATT sub-procedure resolves an unknown handle from a UUID without
   either full discovery or (for the three standard UUIDs above) the Read-By-Type shortcut.

**This is the exact same handle set already documented in `CAP-002`/`CAP-003`** — direct,
independent confirmation of `CAP-003-FINDINGS.md` §4's "GATT handle numbers are stable across
sessions for this device" finding, now a fourth data point (`CAP-002`, `CAP-003`, `CAP-004`'s
partial confirmation, `CAP-010`).

**Sharper open question than before.** `CAP-003`/`CAP-004` already explained *why discovery
doesn't happen* (the Database Hash caching mechanism lets a client skip it). What this capture
adds: this session's **classic bond genuinely was fresh** (`Delete Stored Link Key` did run,
frame 1236) — yet the client still went straight to raw, literal handle numbers for
non-standard, Fast-Pair-specific characteristics, with no discovery and no per-UUID lookup for
those. Two explanations remain open, neither established by this capture:

- **(a) Android's separate GATT attribute-table cache** (distinct from the classic bond, per
  `CAP-003-FINDINGS.md` §1) **is keyed to the peer's BD_ADDR and survived from this project's own
  prior sessions with this exact physical device** — all of `CAP-001`–`CAP-007` ran on this same
  Pixel 7a. This session's classic bond was fresh, but the GATT cache Group W specifically targets
  was never actually cleared (§1) — so its survival here is unsurprising, not a new mystery.
- **(b) Google's Fast Pair client implementation ships a hardcoded/spec-mandated handle
  expectation** for its own GATT service, independent of any per-device discovery or cache —
  plausible given Fast Pair devices register metadata with Google's cloud Nearby/Fast Pair
  registry, but nothing in this project's captures establishes this either way.

Distinguishing (a) from (b) is exactly what Group W's untried methods would resolve, and neither
was exercised here — **this remains the concrete, actionable next step, unchanged in substance
from `CAP-003-FINDINGS.md` §7 and `CAP-004-FINDINGS.md` §8, now with one more failed attempt on
record.**

## 3. GATT handle cluster: new byte-level detail, still no UUIDs (🟢 FACT for form/bytes, 🔴 for identity)

Reproduction:
```
tshark -r CAP-010-btsnoop_hci.log -Y "btatt.opcode == 0x12 or btatt.opcode == 0x1b or btatt.opcode == 0x0b or btatt.opcode == 0x0a" \
  -T fields -e frame.number -e frame.time -e btatt.opcode -e btatt.handle -e btatt.value
```

| Frame | Time (11:43:…) | Handle | Operation | Bytes | Value (hex) |
|---|---|---|---|---|---|
| 2003→2005 | 40.245→40.277 | `0x0f28` | Read Req/Resp | 1 | `31` |
| 2008 | 40.329 | `0x0c0d` | Write Req (CCCD enable) | 2 | `0100` |
| 2013 | 40.366 | `0x0c05` | Write Req (CCCD enable) | 2 | `0100` |
| 2017 | 40.428 | `0x0c04` | Write Req | **80** | `569576a4…a32a5dfe13` (redacted mid-value — high-entropy, see note) |
| 2019 | 40.635 | `0x0c04` | Notification | 16 | `42b4fc876942804effb7d85b6537db2f` |
| 2020 | 40.664 | `0x0c0c` | Notification | 40 | `24b2bde8d05348fb…c47b7db73` (redacted mid-value) |
| 2022→2024 | 40.812→40.874 | `0x0f2a` | Read Req/Resp | 10 | `5265766973696f6e2036` = ASCII `"Revision 6"` |
| 2025 | 40.882 | `0x0c0a` | Write Req | 16 | `e43d87881263f590b74b4a2de3f5e936` |
| 2029→2031 | 40.963→41.023 | `0x0c13` | Read Req/Resp | 9 | `0185c4ee3ab683959b` |
| 2032 | 41.038 | `0x0c14` | Write Req (CCCD enable) | 2 | `0100` |
| 2035 | 41.106 | `0x0c13` | Write Req | 10 | `0108dd5e0e6261af6069` |
| 2037 | 41.157 | `0x0c13` | Notification | 32 | `011e88d7356971c8ad…7b7b5ae798ce700` (redacted mid-value) |
| 2039 | 41.196 | `0x0c14` | Write Req (CCCD disable) | 2 | `0000` |

Note: full raw hex values were extracted and verified locally via the `tshark` command above;
long high-entropy payloads are elided here per this task's privacy instruction (they are
opaque/encrypted-looking bytes, not personally identifying, but kept out of the committed file as
a precaution — full values remain reproducible from the raw log with the command above).

**Consistent with `CAP-002`/`CAP-003`'s cluster** (`CAP-003-FINDINGS.md` §4): the CCCD-enable →
encrypted-write → encrypted-notify shape reproduces exactly, including the same distinctive
**80-byte (`16×5`) first write on `0x0c04`** (frame 2017) — now confirmed in a **third**
independent session (`CAP-002`, `CAP-003`, `CAP-010`), further strengthening
`CAP-003-FINDINGS.md` §4 Task 5's already-🟢-promoted "matches the official Fast Pair
Key-based-Pairing/Passkey characteristic FORM" finding. `0x0f28` (1 byte, `0x31`) and `0x0f2a`
(10 bytes, ASCII `"Revision 6"`) both reproduce exactly as in `CAP-002`/`CAP-003` — now four
independent sessions confirming the identical value for `0x0f2a`.

**New in this capture, not previously byte-characterized:**
- **`0x0c0c` notification (frame 2020, 40 bytes)** — this handle was already known to be part of
  the cluster (`CAP-003-FINDINGS.md` §4 lists it), but no prior finding gave its payload length.
- **`0x0c13`/`0x0c14`'s exact byte lengths** (9/10/32 bytes on `0x0c13`; 2 bytes each for
  `0x0c14`'s CCCD enable/disable) — `0x0c13`/`0x0c14` appears in `CAP-003`'s handle list without
  byte-level detail. These lengths (9, 10, 32) do **not** cleanly fit the 16-byte AES-block
  pattern seen on `0x0c04`/`0x0c05`/`0x0c0a` — 🟡 HYPOTHESIS, not confirmed against any spec:
  `0x0c13`/`0x0c14` may be a structurally different characteristic from the Key-based-Pairing pair
  (a leading `0x01` byte precedes the payload on all three `0x0c13` values, possibly a
  version/type tag — not decoded further here).

## 4. Classic BR/EDR pairing lifecycle — matches the established fresh-pairing state machine (🟢 FACT)

| Step | Time | Frame(s) | Detail |
|---|---|---|---|
| `Delete Stored Link Key` | 11:43:28.378 | 1236–1237 | Confirms a deliberate fresh-pairing flow |
| `Create Connection` → `Connect Complete` | 11:43:28.380 → 11:43:28.649 (status `0x00`) | 1238 → 1242 | Succeeds immediately, no Page Timeout |
| `Link Key Request` → **Negative Reply** | 11:43:28.679 | 1266–1268 | No prior classic bonding material, as expected |
| `IO Capability Request/Reply/Response` | 11:43:28.682–28.715 | 1270–1276 | Secure Simple Pairing negotiation |
| `User Confirmation Request` → (on-screen dialog, ~5.5s user delay) → **Reply** | 11:43:28.962 → 11:43:34.925 | 1290 → 1303 | The multi-second gap is the visible "Pair with Pixel Buds Pro 2 van Ted?" dialog waiting on the tap (video: 11:43:29–34) |
| `Simple Pairing Complete` → `Link Key Notification` → `Authentication Complete` → `Encryption Change` | 11:43:35.141–35.251 | 1305–1312 | New link key stored; completes in ~0.2s once confirmed |

This is a **fourth** independent capture (`CAP-001` reconnect, `CAP-002`/`CAP-003` fresh pair,
`CAP-010` fresh pair) matching the classic-link state machine already promoted to `PROTOCOL.md`
§5.1 — adds one more confirming instance, no new mechanism.

## 5. RFCOMM / Message Stream — consistent with prior captures, one new observation (🟢 FACT)

- **Channels opened:** 0 (multiplexer control, frame 1483), **6** (labeled "Hands-Free" by
  Wireshark, frame 1490 — HFP AT-command handshake confirmed, frames 1515–1680), 4 (frame 1553),
  5 (frame 1598), 1 (frame 1717), 2 (frame 1851). HFP landing on channel 6 here (not channel 4 as
  in `CAP-001`/`CAP-003`, and matching `CAP-004`'s channel 6) is a further confirming data point
  for `CAP-001-FINDINGS.md` §2's "RFCOMM channel numbers are session-local, not profile-fixed"
  rule.
- **DLCI 0x08's private one-time handshake reappears byte-for-byte** — `google-pixel-buds-pro-v1`
  + capability blob (frame 1636), `Europe/Amsterdam` (frame 1656), and the `release_5.203`
  firmware string inside the Group `0x03`/Code `0x02` protobuf blob first identified in
  `CAP-004-FINDINGS.md` §5a Task 2 (frames 1661/1688/1691) — a **fifth** independent confirmation
  of this content (`CAP-001`, `CAP-002`, `CAP-003`, `CAP-004`, now `CAP-010`), reproducible via:
  ```
  tshark -r CAP-010-btsnoop_hci.log -Y 'frame contains "release_5.203"' -T fields -e frame.number -e frame.time -e btrfcomm.dlci
  ```
- **New observation: a full RFCOMM channel teardown/reopen cycle occurs at ~11:46:58–11:47:04,
  well after the video ends (11:45:01).** Channels 1/2/4/5 all close (`UA`/disconnect activity,
  frames 2604–2632) and reopen (frames 2654–2837), with the HFP `AT+BIEV=2,100` battery report
  repeating on reopen (frames 2607–2668). No on-screen action could be correlated (recording had
  already stopped) — plausibly an app-level background reconnect or a link-supervision timeout
  cycle. Flagged as a new open observation, not investigated further in this pass.

## 6. Conclusions

- **Group W's own goal — resolve the `0x0f2a`/`0x0c0X` handle→UUID mapping via genuine live
  discovery — is not achieved, for the fourth consecutive capture attempting it** (`CAP-002`,
  `CAP-003`, `CAP-004`, `CAP-010`).
- **Unlike the first three attempts, this one is explained by a procedure gap, not new evidence
  against the untried methods.** Group W's two candidate methods (`pm clear
  com.android.bluetooth`, or the Pixel 9a) exist specifically because bond-removal-only (Group
  R's method) was already known to fail — and this capture, despite its folder label, used
  bond-removal-only again (§1).
- **Does this resolve the GATT caching issues seen in `CAP-003`/`CAP-004`? No.** If anything it
  sharpens the open question (§2): even a genuinely fresh classic bond did not trigger discovery
  or per-UUID resolution for the proprietary characteristics, meaning the relevant cache (or
  hardcoded-knowledge mechanism) operates below and independently of the classic bond layer.
- On the positive side, this capture independently reproduces — with no contradictions — five
  separate findings already established across `CAP-001`–`CAP-004`: the classic fresh-pairing
  state machine (§4), the GATT handle cluster's stable numbering and FORM (§3), the
  `0x0f2a`="Revision 6" value (§3), the DLCI 0x08 private handshake incl. `release_5.203` (§5),
  and RFCOMM channel-number instability (§5) — plus one genuinely new data point, the post-video
  reconnect cycle (§5).

## 7. Recommended next steps

1. **Actually run Group W's defined procedure** — `adb shell pm clear com.android.bluetooth`
   (confirm acceptable first: this clears *all* bonded devices on the phone, not just the Buds')
   or a first-time connection from the project's Pixel 9a — and confirm via the log itself (not
   the app's/tool's UI) that a live `Read By Group Type` response actually appears this time.
2. If/when live discovery succeeds, prioritize resolving the now-4-session-stable handle set
   (`0x0f28`, `0x0f2a`, `0x0c04`, `0x0c05`, `0x0c0a`, `0x0c0c`, `0x0c0d`, `0x0c13`, `0x0c14`)
   against real UUIDs — `CAP-004-FINDINGS.md` §6's named candidate services (`Google Fast Pair
   Service 0xFE2C`, `Device Information 0x180A`) remain the leading hypotheses to check the
   resolved handles against. (`Accessory Non-Owner Service` is explicitly **not** a candidate to
   check against — investigating it is out of scope per `DECISIONS.md` ADR-008.)
3. Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Capture Index row for `CAP-010` (currently
   `*planned*`/`TBD`) and `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `GATT-001` row to record this
   attempt and its actual outcome, so a future session doesn't re-attempt the same
   already-insufficient method under the Group W label again.
4. Investigate the ~11:46:58–11:47:04 post-video RFCOMM reconnect cycle (§5) with a session that
   keeps recording (video and/or a screen-off wall-clock note) well past app setup, to determine
   whether it's a fixed-interval background behavior or triggered by something observable.

## 8. Promotion readiness — what's ready for `PROTOCOL.md`

**Ready to promote now (🟢 FACT, cross-capture-verified, no new promotion needed beyond adding this
capture as a confirming citation):**
- Classic BR/EDR fresh-pairing state machine — `PROTOCOL.md` §5.1 already covers this; add
  `CAP-010` frames 1236–1312 as a fourth confirming instance.
- GATT handle numbers (`0x0f28`, `0x0f2a`, `0x0c04`, `0x0c05`, `0x0c0a`, `0x0c0c`, `0x0c0d`,
  `0x0c13`, `0x0c14`) stable across sessions for this device — now four confirming captures.
- The `0x0c04`/`0x0c05` write/notify FORM (80-byte first write, 16-byte subsequent
  writes/notifies) matching the official Fast Pair Key-based-Pairing/Passkey characteristic shape
  — now three confirming captures (`CAP-002`, `CAP-003`, `CAP-010`).
- DLCI 0x08's private one-time handshake content, including the `release_5.203` firmware string —
  now five confirming captures.

**Not ready yet (at the time):**
- Any UUID for handle `0x0f2a`, `0x0f28`, or the `0x0c0X`/`0x0c1X` cluster — **still unresolved,
  now 4-for-4 negative across every capture that has attempted this.** Needs Group W's actual
  defined method (§7 item 1), genuinely untried so far.
- The `0x0c13`/`0x0c14` "structurally distinct from `0x0c04`/`0x0c05`" hypothesis (§3) — new this
  session, not cross-checked against any spec.

**Resolved 2026-09-01 (`CAP-034`, maintainer sign-off obtained per `AGENTS.md` §6):** the handle↔UUID
mapping above is now known. `0x0c04` = Key-based Pairing (`FE2C1234…`), `0x0c0a` = Account Key
(`FE2C1236…`), `0x0c0c` = Additional Data (`FE2C1237…`), `0x0f28` = Serial Number String, `0x0f2a` =
Firmware Revision String (both under Device Information, `0x180A`) — confirming this section's own
80-byte-write/Key-based-Pairing-FORM hypothesis as an exact name match, not just a shape match. The
`0x0c13`/`0x0c14` "structurally distinct" hypothesis above is also confirmed: `0x0c13` is
`FE2C1238…`, a genuinely separate characteristic from the Key-based-Pairing quartet — still no
official name found for it. See `captures/CAP-034-2026-09-01_06-46-31_06-52-45-Group_W/CAP-034-FINDINGS.md`
§4 for the full command+hex evidence and `PROTOCOL.md` §6 for the promoted table.
- The 11:46:58–11:47:04 post-video reconnect cycle (§5) — single occurrence, no correlated
  on-screen event, cause unknown.
- The `libmaestro`/ANC-EQ control channel identity — still completely unaddressed by any capture
  to date, including this one (no ANC/EQ action was performed in this session).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-010-2026-08-16_11-42-31_11-45-01-Group_W/CAP-010-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-010-2026-08-16_11-42-31_11-45-01-Group_W/CAP-010-FINDINGS
