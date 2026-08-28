# Findings: `CAP-003` (Group R forced-GATT-rediscovery capture)

Standardized, evidence-based extraction from `CAP-003-btsnoop_hci.log` + `CAP-003-recording.mp4`, staged here
for later promotion directly into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Modeled
on `captures/CAP-001-2026-08-09_08-51-00_08-52-20-Group_Z/CAP-001-FINDINGS.md` (`CAP-001`). Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-003` · **Date:** 2026-08-10 · **Firmware:** `release_5.203` ·
**Phone:** Pixel 7a, Android 17 — **nRF Connect** (generic
BLE/GATT tool), with the official Pixel Buds app (v1.0.955078536) taking over partway through. **Log file:**
`CAP-003-btsnoop_hci.log` (302.2s, 2,863 packets, 20:58:57.10–21:03:59.33 local/+0200 — a short,
freshly-restarted log, not a shared multi-hour one). **Video:** `CAP-003-recording.mp4` (81.1s,
20:59:16–21:00:37 local, on-screen wall-clock overlay). **Devices:** phone `Google_7e:ca:81`
(Pixel 7a, same phone as `CAP-001`/`CAP-002`), peer `Google_cf:6e:07`
(`04:00:6E:CF:6E:07`, the Buds/case — confirmed the same physical device as `CAP-001`/`CAP-002`
via the classic-link BD_ADDR, frame 1689).

**Stated goal of this session (per the maintainer):** map the GATT service/characteristic
structure of the Buds/case by forcing a fresh discovery — pairing removed via system settings
beforehand, connected via **nRF Connect** instead of the official app — specifically to resolve
two UUID gaps left open by `CAP-002`'s `CAP-002-FINDINGS.md`: handle `0x0f2a` (already known to return
the string `"Revision 6"` — the UUID around it, not the value, was the target) and the `0x0c0X`
handle cluster (`CAP-002`'s Key-based-Pairing-shaped write/notify bursts). **§1 reports whether
that goal was met — it was not, for a specific, evidenced reason.** A full classic pairing
exchange is also documented (§2) as a bonus data point, per the session's design.

---

## 1. Primary goal not achieved: no GATT discovery traffic on the wire (🔴 OPEN QUESTION, with a concrete explanation)

Despite the test design (remove pairing, connect fresh via a generic tool), **no GATT primary
service discovery or characteristic discovery occurred on the wire in this capture.** This was
checked exhaustively, not assumed:

- Every ATT packet in the capture was filtered specifically for the two opcode pairs named in
  the task: `0x10`/`0x11` (`Read By Group Type` — primary service discovery) and `0x08`/`0x09`
  (`Read By Type` — characteristic discovery). **Zero** `Read By Group Type` packets exist
  anywhere in the log. The only `Read By Type` traffic is a single **Database Hash** read/response
  pair (frames 1649, 1657, 20:59:38.38–38.42) — not a characteristic-discovery query.
- This was checked against the **entire log**, not just the video-covered window, in case
  discovery happened outside the recorded portion — same result: zero discovery-opcode traffic
  anywhere in the full 302s capture.
- **Why:** the "Database Hash" mechanism (a standard BLE GATT Caching feature, Bluetooth Core
  Spec 5.1+) lets a GATT client skip re-discovery entirely if it already holds a cached copy of
  the server's database matching the advertised hash. Android's Bluetooth stack evidently still
  held a cached GATT database for this specific device (keyed by its identity, independent of the
  classic/BLE **bond**), even though the maintainer removed the pairing via system settings
  beforehand. **Removing a device's pairing/bond does not necessarily clear Android's separate
  GATT database cache** — this capture is direct evidence of that distinction, not previously
  documented anywhere in this project's files.
- nRF Connect's own UI *did* show a resolved service list (Generic Attribute, Generic Access,
  Broadcast Audio Scan Service — `CAP-003-EVENT-NOTES.md`, 20:59:42) — but this comes from Android's
  `BluetoothGatt` API serving nRF Connect its **cached** database, not from a live over-the-air
  query. The UI evidence and the wire evidence are consistent with each other, not contradictory —
  both point to the same cache being used instead of fresh discovery.

**Consequence for `CAP-002`'s open questions:** neither handle `0x0f2a` nor the `0x0c0X` cluster
could be resolved to real UUIDs by this capture. The two open questions from `CAP-002` §4/§7
remain open. See §5 for what *could* still be extracted despite this, and §6 for a concrete
recommendation on how to actually force rediscovery next time.

## 2. Bonus data point: classic BR/EDR pairing lifecycle (🟢 FACT)

As expected from clearing the pairing beforehand, a full fresh pairing sequence occurs, closely
matching `CAP-002`'s pattern (also a fresh pair) but succeeding without any Page-Timeout retry:

| Step | Time | Frame(s) | Detail |
|---|---|---|---|
| BLE (LE) connection established first | 20:59:38.320 | 1621 (`LE Enhanced Connection Complete`) | Precedes the classic connection by ~0.4s — consistent with Fast Pair's own design (BLE-first, classic pairing triggered from the BLE side) |
| Delete stored link key | 20:59:38.730 | 1687–1688 | Confirms a deliberate fresh-pairing flow, same as `CAP-002` |
| Create Connection | 20:59:38.731 | 1689 | |
| Connect Complete | 20:59:39.090 (status `0x00`) | 1692 | Succeeds immediately, no Page Timeout (unlike `CAP-001`) |
| Link Key Request → **Negative Reply** | 20:59:39.098 | 1708–1709 | No prior bonding material, as expected |
| IO Capability Request/Reply/Response | 20:59:39.100–39.129 | 1711–1721 | Secure Simple Pairing (SSP) negotiation |
| Simple Pairing Complete | 20:59:39.825 | 1750 | ~0.7s after IO Capability exchange — much faster than `CAP-002`'s ~6.4s gap, since no on-screen confirmation dialog with a permission toggle was in the way this time (nRF Connect doesn't show one) |
| Link Key Notification (new key stored) → Authentication Complete → Set Connection Encryption → Encryption Change | 20:59:39.834–39.876 | 1751–1756 | |

This is the **third** independent capture (`CAP-001` reconnect, `CAP-002` fresh pair, `CAP-003`
fresh pair) showing the same overall pairing state-machine shape, reinforcing confidence in
`PROTOCOL.md` §5's connection-lifecycle sketch for the classic-link portion specifically (the BLE
and RFCOMM/profile portions still vary more, see §3).

## 3. RFCOMM: further evidence that channel numbers are session-local, not profile-fixed (🟢 FACT)

RFCOMM channels opened this session: **0** (multiplexer control), **4** (phone-init, frame 1943;
also buds-init reopen, frame 2157), **5** (frame 1954), **2** (frame 2035, closed frame 2071,
reopened frame 2265), **1** (frame 2348). HFP AT-command traffic (`AT+BRSF`, `AT+BAC`, `AT+CIND`,
`AT+CMER`, `AT+BIND`, `AT+BIEV`, ...) appears on **channel 4** this time (frames 2178+) — matching
`CAP-001`'s channel-4 HFP placement, *not* `CAP-002`'s channel-6 placement. This is a third data
point (alongside `CAP-001`'s and `CAP-002`'s CAP-002-FINDINGS.md corrections) confirming the reusable
methodological note already recorded in `CAP-001`'s `CAP-001-FINDINGS.md` §2: **RFCOMM server channel
numbers are negotiated per-connection and must never be treated as a stable per-profile label**
— only content/structure (and, within one session, the DLCI) reliably identifies a channel's
role. This capture's own SDP records (Audio Sink, AVRCP, HFP AG/HS, PnP Information — frames
1765–2164) match the same profile set seen in both earlier captures.

## 4. GATT handle activity: reproducible across three sessions, still unidentified (🟡 HYPOTHESIS)

Even without discovery traffic, the **same GATT handle numbers** used in `CAP-002` reappear here,
carrying similarly-shaped data — itself informative:

- **Handle `0x0f2a`** is read once (frame 1819 request → 1835 response, 20:59:40.33–40.39) and
  returns the identical literal ASCII string **`"Revision 6"`** (`5265766973696f6e2036`) seen in
  both `CAP-001` (via RFCOMM Message Stream) and `CAP-002` (via this same GATT handle). Three
  independent sessions, two different transports, one consistent value — this is now about as
  strong as evidence gets without an actual UUID. Still 🟡 for the *field's real-world meaning*
  (per `CAP-002` §3's open question on whether "Revision 6" is firmware or a protocol/schema
  revision), but 🟢 FACT for "this specific handle/value pairing is stable across sessions."
- **A new handle not seen before: `0x0f28`, read repeatedly (🟢 FACT).** Read twice back-to-back
  every ~60 seconds throughout the whole capture (frames 2506/2510 at 20:59:51, 2693/2697 at
  21:00:51, 2791/2794 at 21:01:52, 2801/2804 at 21:02:52 — i.e. this polling **continues for
  several minutes after the video ends**, well past the app-setup flow). Every read returns the
  same single byte, `0x31` (frames 2508, 2512, 2695, 2699, 2793, 2796, 2803, 2806). Two candidate
  readings, neither confirmed: (a) the raw value `49` (decimal), which doesn't obviously map to
  anything already known about this device; (b) the byte as ASCII `'1'`, which — given handle
  `0x0f28` sits two handles before `0x0f2a` (`"Revision 6"`), and the standard Device Information
  Service lays out Hardware/Firmware/Software Revision String characteristics consecutively (per
  `CAP-002` §3's spec research) — would make a plausible **Hardware Revision String** value of
  `"1"`. Kept at 🟡 HYPOTHESIS: plausible by position and by matching the standard service's
  layout pattern, but no UUID confirms it.
- **The `0x0c0X` write/notify cluster reappears with the same handle numbers as `CAP-002`**
  (`0x0c04`, `0x0c05`, `0x0c0a`, `0x0c0c`, `0x0c0d`, `0x0c13`, `0x0c14`), plus **two handles not
  seen in `CAP-002`: `0x0c07`/`0x0c08`** (frames 1741–1747), following the identical
  CCCD-enable-then-encrypted-write-then-notify shape already described in `CAP-002` §4. This
  extends, but does not resolve, that open question — see §6.
- **Handle numbers are stable across sessions in a way channel numbers are not (🟢 FACT,
  methodological note):** contrast this with §3 — RFCOMM channel *numbers* differ session to
  session for the same content, while GATT *handle* numbers for the same characteristics stayed
  identical across `CAP-002` and `CAP-003` (`0x0f2a`, `0x0c04`, `0x0c05`, `0x0c0a`, `0x0c0c`,
  `0x0c0d`, `0x0c13`, `0x0c14` all reappear unchanged). This is expected/standard BLE behavior
  (a GATT server's attribute table is normally static across connections, unlike RFCOMM's
  per-connection channel negotiation) but is worth stating explicitly as a rule for future capture
  analysis: **GATT handles may be compared directly across sessions for the same device; RFCOMM
  channel numbers may not.**

> **Task 12 (2026-08-12): are `0x0f2a`/`0x0f28` part of a densely-read, contiguous Device
> Information block? Checked, and the answer is a clean negative — neighboring handles are never
> read at all, in any of the three captures that touch this range.** Filtered every GATT Read
> Request/Response (`btatt.opcode in {0x0a,0x0b}`) with `0x0f20 <= handle <= 0x0f30` across
> `CAP-002`, `CAP-003`, and `CAP-004`'s full logs:
> ```
> tshark -r CAP-003-btsnoop_hci.log -Y "btatt.opcode in {0x0a,0x0b} and btatt.handle >= 0x0f20 and btatt.handle <= 0x0f30" \
>   -T fields -e frame.number -e frame.time -e btatt.opcode -e btatt.handle -e btatt.value
> ```
> `CAP-002`: only `0x0f2a` (frames 49423→49425, value `"Revision 6"`). `CAP-003` (this file): only
> `0x0f2a` (1819→1835) and `0x0f28` (eight reads total, four request/response pairs, each pair
> itself two-back-to-back as already noted above — 2506/2508, 2510/2512, 2693/2695, 2697/2699,
> 2791/2793, 2794/2796, 2801/2803, 2804/2806). `CAP-004`: **zero** reads in this range at all
> (expected — no Pixel Buds app ever ran there, so "More settings"/Device details, the screen that
> triggers these reads, was never opened). **`0x0f26`, `0x0f27`, and `0x0f29` are never read in any
> of the three captures.** This does not disprove the Device Information Service hypothesis
> (`0x0f28`'s hypothesis (b) above) — an app is free to read only the specific characteristics it
> needs rather than every one a service exposes — but it means the "contiguous DIS block" reading
> cannot be positively confirmed from read-pattern evidence either; it remains exactly as
> hypothesis-strength as before, now on a checked rather than assumed basis. Resolving it still
> requires the live-discovery capture recommended in §7 item 1 below.
>
> **Task 5 (2026-08-12): the `0x0c0X` cluster's byte lengths and flow precisely match the official
> Fast Pair Key-based Pairing / Passkey characteristic shapes — upgraded from "structurally
> resembles" to a much stronger, cross-capture-confirmed match on FORM (identity/UUID still
> unconfirmed).** Extracted every write/notify (`btatt.opcode in {0x12,0x13,0x1b,0x52}`) on handles
> `0x0c04`/`0x0c05` (this capture and `CAP-002`) and `0x0c07`/`0x0c08` (this capture only) with
> exact byte lengths:
> ```
> CAP-002  0x0c05 write  "0100"                       2 bytes   -- CCCD enable (standard: 0x0001 LE)
> CAP-002  0x0c04 write  (first, frame 49418)         80 bytes  -- = 16 x 5
> CAP-002  0x0c04 notify (frame 49420)                16 bytes
> CAP-002  0x0c04 write  (frame 49483, 49614, 49665)  16 bytes  each
> CAP-002  0x0c04 notify (frame 49667)                16 bytes
> CAP-003  0x0c05 write  "0100"                       2 bytes   -- CCCD enable
> CAP-003  0x0c04 write  (first, frame 1670)          80 bytes  -- = 16 x 5, same size as CAP-002's first write
> CAP-003  0x0c04 notify (frame 1684)                 16 bytes
> CAP-003  0x0c08 write  "0100"                       2 bytes   -- CCCD enable
> CAP-003  0x0c07 write  (frame 1744)                 16 bytes
> CAP-003  0x0c07 notify (frame 1746)                 16 bytes
> ```
> **Hex-dump completeness check (2026-08-14), deskresearch task — raw bytes added for this table's
> byte-length-only claims, per `PROJECT_RULES.md` §1's traceability rule.** Re-extracted directly
> via `tshark -r CAP-003-btsnoop_hci.log -Y "frame.number==<N>" -T fields -e btatt.value`:
> ```
> CAP-003 frame 1670 (0x0c04 write, 80B):  5d5d38fdf3274918966c031d7f310982c0795d4942576c26726ce22
>                                           784288e5f7c628d6cf264f16cc45b7e64c1b1c97ad556da51904984
>                                           1745891a948a2ac0c0def2bde46300b9be931614739064eb89
> CAP-003 frame 1684 (0x0c04 notify, 16B): 84d83dad08b766db4565cbc61ac65bfb
> CAP-003 frame 1744 (0x0c07 write, 16B):  a06dddde67d139fa4485a9149845664d
> CAP-003 frame 1746 (0x0c07 notify, 16B): 699a3d82a737a20b9668fd4931455f00
> CAP-002 frame 49418 (0x0c04 write, 80B, full-log numbering): f741fcec7fb76ddcf7e59d8ad932ca7b
>                                           1895f4f4bb122a325be0917b89912d05a243f03ade548b09fa58606
>                                           bcda5a750fc23bcbdd4443aac128a232e07fe89a42f61d71a9a8d23
>                                           13605185ab3e230d36
> CAP-002 frame 49420 (0x0c04 notify, 16B): db561ec89f958a2f4e1e1ff34eaef953
> ```
> All values are high-entropy/opaque, consistent with the already-stated AES-128-block-ciphertext
> reading — no new structure visible, this addendum only supplies the underlying bytes so the
> byte-length claims above are directly traceable rather than described only by length.
>
> Every write/notify **after** the first one on `0x0c04` is exactly **16 bytes** — an AES-128 block
> size — and `0x0c07`'s write/notify are also exactly 16 bytes each: byte-for-byte the shape the
> official Fast Pair Key-based Pairing Request/Response and Passkey characteristics are documented
> to use. The CCCD writes are exactly 2 bytes, value `0x0001` little-endian — the standard
> "notifications enabled" value, on both characteristic pairs. The flow (CCCD-enable write → 16-byte
> encrypted write → 16-byte encrypted notify) is identical on both `0x0c04`/`0x0c05` and
> `0x0c07`/`0x0c08`, and identical between `CAP-002` and `CAP-003` (two independent sessions) for
> `0x0c04`/`0x0c05` specifically, including the **same unusual 80-byte (`16×5`) first write on
> `0x0c04`** in both sessions — too specific a match to be coincidental. Per this project's
> promotion rule (cross-capture structural match, or spec byte-match, ⇒ 🟢 FACT), **the FORM of this
> exchange — byte lengths and CCCD-gated write/notify flow — is promoted to 🟢 FACT as matching the
> official Fast Pair Key-based Pairing/Passkey characteristic shape.** What remains 🟡/🔴, unchanged
> by this pass: the actual UUIDs (no live discovery ever resolved them — see the 2026-08-12 update
> in `CAP-004` `CAP-004-FINDINGS.md` §6, confirming this gap persists in all three captures with this goal),
> and therefore whether `0x0c04`/`0x0c05` specifically is Key-based Pairing and `0x0c07`/`0x0c08`
> specifically is Passkey (plausible, and consistent with §5 below's timing correlation — `0x0c07`'s
> burst falls inside the classic SSP window, exactly where Passkey's silent numeric-comparison
> cross-check would be expected — but not UUID-confirmed). The meaning of the first 80-byte write is
> also still open — 80 = 5×16 is consistent with a bulk transfer of several concatenated 16-byte
> blocks (e.g. an Account Key sync) rather than a single Key-based Pairing Action Request, but this
> is not confirmed against the spec's exact procedure names.

## 5. Timing correlation: GATT bursts map to distinct phases of the Fast Pair procedure (🟡 HYPOTHESIS)

The `0x0c0X` bursts and the `0x0f2a`/`0x0f28` reads line up with distinct moments in the
connection sequence, suggesting a specific interleaving with Fast Pair's own protocol phases:

| Burst | Time | Concurrent log/video context |
|---|---|---|
| `0x0c0d`→`0x0c05`→`0x0c04` write, `0x0c04`/`0x0c0c` notify | 20:59:38.45–38.67 | Right after the BLE connection completes (20:59:38.32), **before** the classic `Create Connection` is even sent (20:59:38.73) — this exchange happens purely over BLE, ahead of any classic activity |
| `0x0c08`→`0x0c07` write, `0x0c07` notify | 20:59:39.50–39.61 | **During** the classic SSP window — IO Capability Response was received at 39.129, `Simple Pairing Complete` fires at 39.825, so this burst sits squarely inside that gap |
| `0x0f2a` read, `0x0c0a` write | 20:59:40.33–40.45 | Right as SDP/RFCOMM channel setup is underway (SDP `Connection Request` at 39.845 through RFCOMM channels opening through 40.55) |
| `0x0c13`/`0x0c14` read/write/notify | 20:59:40.46–40.69 | Continues immediately after, overlapping the tail of RFCOMM channel setup |

The timing of the second burst — falling *inside* the classic SSP window, between IO Capability
exchange and Simple Pairing Complete — is a new observation not present in `CAP-002`'s writeup
(where SSP took ~6.4s due to an on-screen dialog, giving less precise burst-to-phase alignment).
It is **consistent with, but does not prove,** the hypothesis that one of these BLE GATT
characteristics is Fast Pair's documented **Passkey characteristic**, which the spec describes as
used to let the Seeker and Provider silently cross-check the classic pairing's numeric-comparison
value over BLE — which would also explain why no passkey digits are ever shown on screen in any
of the three captures to date. Not claimed as FACT; requires UUID confirmation.

## 6. Other observations

- **Fast Pair "ownership transfer" flow observed for the first time (🟢 FACT, video evidence,
  20:59:43–54):** the system dialog *"Pixel Buds Pro 2 is connected to someone else's account...
  Ask owner to share device / Remove previous owner / Start using the device"* is new to this
  project's documentation. It appeared because the device still carried an account association
  from earlier test sessions (`CAP-001`/`CAP-002`) despite the local pairing having been removed —
  i.e. **Fast Pair's account-key/ownership state is stored server-side (Google account), separate
  from both the local classic bond and the local GATT cache discussed in §1.** Three independent
  state layers are now evidenced across this project's captures: local classic bond, local GATT
  database cache, and cloud-side Fast Pair account ownership — each cleared independently of the
  others.

  > **Scope note, added 2026-08-28 (`DECISIONS.md` ADR-008):** this bullet predates ADR-008
  > (2026-08-15) by five days and was never retroactively scope-checked against it. Fast Pair
  > **Ownership Transfer** is explicitly out of scope for this project (`PROJECT.md` non-goals,
  > `ADR-008`) — the observation above is retained as historical record of what was seen on screen
  > (the user tapped "Start using the device," not "Remove previous owner"; no wire bytes for this
  > exchange were decoded), but the "three independent state layers" architectural reading should
  > not be treated as an invitation to further investigate the Ownership Transfer mechanism itself.
  > No further capture time should be spent decoding this flow's wire behavior, per ADR-008's own
  > decision.
- **The Pixel Buds app only takes over at the "Set up" tap (21:00:04) (🟢 FACT, video evidence):**
  everything before that — discovery, connection, pairing, the ownership-transfer dialog, the
  "Set up device" card — is Android system UI and/or nRF Connect, not the official app. This
  matters for any future correlation: log activity before this point should not be attributed to
  Pixel-Buds-app-specific logic.

## 7. Recommended next steps

1. **To actually resolve the `0x0f2a`/`0x0c0X` UUIDs, a stronger cache-busting method is needed
   than removing the pairing.** Candidates to try: clearing the Bluetooth system app's storage/
   cache directly (Android Settings → Apps → Bluetooth → Storage → Clear cache/storage, which
   typically wipes the GATT database cache, not just the bond list); or performing the discovery
   from a genuinely different phone that has never connected to this device before (e.g. the
   project's GrapheneOS Pixel 9a, which per `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s two-device setup
   has not run the official app and may not hold a cached database for this device at all).
2. If a future capture does get real discovery traffic, prioritize resolving `0x0f28`,
   `0x0f2a`, and the `0x0c0X` cluster specifically — the exact handles are now known and stable
   (§4), only the UUIDs are missing.
3. Attempt to decrypt or otherwise identify the `0x0c0X` write/notify bursts against Fast Pair's
   Key-based Pairing / Passkey / Account Key GATT procedure spec, using the phase-alignment
   evidence in §5 as a starting hypothesis for which burst maps to which named procedure step.
4. ~~This capture's classic-pairing bonus data (§2) is solid enough to fold into
   `PROTOCOL.md` §5's connection-lifecycle section alongside `CAP-001`/`CAP-002`'s — three
   consistent observations now support the classic-link portion of that sequence.~~ **Done
   2026-08-15** — see `PROTOCOL.md` §5.1, promoted to 🟢 FACT citing all three captures'
   frame numbers.

## 8. Promotion readiness — what's ready for `PROTOCOL.md`

**Ready to promote now (🟢 FACT, cross-capture-verified):**
- ~~Classic BR/EDR fresh-pairing state machine (delete-key → create-connection →
  negative-link-key-reply → IO-capability/SSP → simple-pairing-complete → new-link-key) — now
  confirmed in two independent fresh-pairing sessions (`CAP-002`, `CAP-003`) — §2.~~ **Promoted
  2026-08-15**, `PROTOCOL.md` §5.1 — includes `CAP-001`'s reconnect (stored-key) path alongside
  these two fresh-pairing sessions, so both branches of the state machine are now documented.
- RFCOMM channel numbers are session-local, not profile-fixed — a third confirming data point
  (HFP on channel 4 here, channel 6 in `CAP-002`, channel 4 in `CAP-001`) — §3.
- GATT handle numbers, unlike RFCOMM channel numbers, are stable across sessions for the same
  device — §4.
- Handle `0x0f2a` reliably returns `"Revision 6"` across three sessions and two transports — §4
  (the *meaning* of that string remains open, carried over from `CAP-002`).
- **The `0x0c04`/`0x0c05` and `0x0c07`/`0x0c08` write/notify FORM (byte lengths, CCCD-gated flow)
  matches the official Fast Pair Key-based Pairing/Passkey characteristic shape exactly, confirmed
  across `CAP-002` and `CAP-003` independently, including the specific 80-byte (`16×5`) first write
  on `0x0c04` in both** (2026-08-12, §4 Task-5 addendum). UUID identity is the only piece still
  missing before this can be a full protocol-level identification.

**Not ready yet:**
- Any UUID for handle `0x0f2a`, `0x0f28`, or the `0x0c0X` cluster — **this session's stated goal,
  still unresolved.** Needs the stronger cache-busting approach in §7 item 1. **Re-confirmed
  2026-08-12: `CAP-004` (a fourth, later session) also failed to trigger live discovery for the
  Buds — see `CAP-004` `CAP-004-FINDINGS.md` §6's 2026-08-12 update — so this is now a four-for-four
  negative result, not specific to this session's method.**
- The Passkey-characteristic hypothesis in §5 — **strengthened 2026-08-12 (§4 Task-5 addendum) to
  a precise byte-length/flow match against the spec**, but still not UUID-confirmed.
- Neighboring handles `0x0f26`/`0x0f27`/`0x0f29` around the `0x0f28`/`0x0f2a` pair — **checked
  2026-08-12 (§4 Task-12 addendum): never read in any of the three captures with reads in this
  range, so the "contiguous Device Information block" hypothesis is neither confirmed nor
  disproved by read-pattern evidence** — same live-discovery capture needed.
- The Fast Pair ownership-transfer flow (§6) — solid video evidence of the *behavior*, but not
  yet correlated to any specific Bluetooth log traffic (a `PROTOCOL.md`-relevant gap: is this
  purely a cloud/GMS-side check, or does it involve a local protocol exchange? Not established
  here).
- The `libmaestro`/ANC-EQ control channel identity — **still completely unaddressed by any of
  `CAP-001`, `CAP-002`, or `CAP-003`**; all three captures so far cover pairing/setup/discovery,
  not actual ANC/EQ commands under clean isolation.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-003-2026-08-10_20-59-16_21-00-37-Group_R/CAP-003-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-003-2026-08-10_20-59-16_21-00-37-Group_R/CAP-003-FINDINGS
