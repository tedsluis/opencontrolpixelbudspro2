# Findings: `CAP-069` (Group BE — the official app 1.0.990706425 on the Pixel 7a: head gestures, Multipoint, assistant hold, a tap on the current ANC mode, EQ Default, wear states, Find; `HEAD-001`, `MULTI-001`, `HOLD-003`/`HOLD-004`, `ANC-001`…`ANC-004`, `ANC-006`, `EQP-001`, `INEAR-002`…`INEAR-004`, `CASE-004`/`CASE-005`, `FIND-001`/`FIND-005`)

Evidence-based extraction from `CAP-069-btsnoop_hci.log` + `CAP-069-recording.mp4` (no audio samples), staged here for promotion into `PROTOCOL.md` per
`PROJECT_RULES.md` §2. Every claim carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture (frame number / film time), or spec text quoted with its URL.
- 🟡 **HYPOTHESIS** — a reading of the evidence, not yet independently confirmed; the settling experiment is named.
- ⚪ **ASSUMPTION** — carried over, not tested here.
- 🔴 **OPEN QUESTION** — not resolved by this capture.

**Capture ID:** `CAP-069` · **Date:** 2026-10-04 · **Firmware:** `release_5.203` (🟢 film 16:07:44–48 and announcements 907, 6336, 7089, 8139) · **Phone:**
Pixel 7a, Android 17 (🟢 film 16:05:30, security update 5 Sep 2026; build number not shown) · **Official app:** 1.0.990706425, updated 30 Sep 2026 (🟢 film
16:26:54–16:27:06) · **Log:** 10,582 packets, 16:05:56.025–16:30:06.677 · **Film:** 1,297.98 s, overlay 16:05:29–16:27:07, **phone = overlay + 0.43 s** ·
**Buds:** classic handles `0x0002` (ACL 1, 16:05:58–16:19:43), `0x0001` (ACL 2, 16:20:02–16:21:50), `0x0002` (ACL 3, 16:22:14–end); the LE links are other
devices (`CAP-069-EVENT-NOTES.md`, Log Metadata) · **Timeline:** `CAP-069-EVENT-NOTES.md`.

Commands used throughout (rule 4a; run from the capture folder unless noted):

```
python3 scripts/pwrpc_decode.py <log>                                  # (repo root) every MAESTRO pw_rpc packet, DLCI 2 and 3, per handle/direction
python3 scripts/message_stream_tally.py -v                             # (repo root) ANC Get/Set/ACK/NAK per log
tshark -r CAP-069-btsnoop_hci.log -Y "frame.number in {…}" -T fields -e frame.number -e frame.time -e data.data            # raw bytes and times
tshark -r CAP-069-btsnoop_hci.log -Y "btrfcomm && btrfcomm.frame_type!=0xef && btrfcomm.frame_type!=0xff" -T fields \
       -e frame.number -e frame.time -e bthci_acl.chandle -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.channel -e btrfcomm.frame_type   # control frames
tshark -r CAP-069-btsnoop_hci.log -Y "bthci_evt.code==0x03 || bthci_evt.code==0x05" -T fields -e frame.number -e frame.time \
       -e bthci_evt.connection_handle -e bthci_evt.reason                                                                    # ACL lifecycle
```

The message-level parse of the Message Stream and GSND CONTROL (`[Group:1][Code:1][Len:2 BE][Value]`, reassembled per handle, DLCI and direction, 0 leftover
bytes) is a scratch script over `tshark -Y "btrfcomm.len>0 && (…)" -T fields -e frame.number -e frame.time -e bthci_acl.chandle -e btrfcomm.dlci
-e frame.p2p_dir -e data.data`; its ANC counts equal `message_stream_tally.py`'s line for this log: `{'get': 4, 'notify_00': 5, 'notify_e8': 14, 'set': 4, 'ack': 4}`.
GSND CONTROL is server channel 4: DLCI 8 on ACL 1 (frames < 7000) and ACL 2, DLCI 9 on ACL 3 (frames > 8000) — on ACL 3 DLCI 8 is HFP (§10).

## 0. Capture integrity and clock (🟢 FACT)

- The log starts with `Sent Reset` (frame 1, 16:05:56.025) — Bluetooth switched on on film at overlay 16:05:56 (finger on the switch; phone 16:05:56.4). The
  film's first 27 s precede it; nothing of the Buds is missing. No `.log.last` was pulled (the pre-toggle buffer is not in the folder).
- Clock: phone 16:06:00.00 = overlay 16:05:59.55 and phone 16:27:00.00 = overlay 16:26:59.58 (status-bar minute flip between two 30-fps frames vs the overlay
  second flip) ⇒ phone = overlay + 0.43 s at both ends. Every write below lands 0.1–1.2 s after its filmed tap.
- The audio track of the film is empty (no sample table), so audible events (the assistant's voice, a ring) cannot be timed from the film.

## 1. `HEAD-001` — "Use head gestures" = `qhr` field 29, 1 = off, 2 = on (🟢 FACT for this capture; promotion proposed in §13)

Six filmed toggles, six writes, six OK, six settings-stream mirrors, on channel 21 (address `00 4b`):

| Filmed tap (phone) | Screen after | Write (frame, time) | Raw payload | OK | Stream | GC `04 16` after |
|---|---|---|---|---|---|---|
| 16:09:17.9 | OFF | 2492, 16:09:18.409 | `2a 05 22 03 e8 01 01` = `4:{29:1}` | 2506 | 2502 `29:1` | 2503 `04 16 00 02 08 02` (+0.28 s) |
| 16:09:28.9 | ON + "Optimize head gestures" dialog | 2564, 16:09:29.409 | `2a 05 22 03 e8 01 02` = `4:{29:2}` | 2568 | 2566 `29:2` | 2567 `… 08 01` (+0.08 s) |
| 16:10:17.2 | OFF | 2737, 16:10:17.740 | `… e8 01 01` | 2744 | 2744 | 2741 `… 08 02` |
| 16:10:41.9 | ON + dialog | 2831, 16:10:42.847 | `… e8 01 02` | 2837 | 2835 | 2836 `… 08 01` |
| 16:11:01.2 | OFF | 2923, 16:11:01.921 | `… e8 01 01` | 2934 | 2932 | 2933 `… 08 02` |
| 16:11:17.9 | ON + dialog | 3025, 16:11:18.982 | `… e8 01 02` | 3033 | 3031 | 3032 `… 08 01` |

Raw request 2492: `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 05 22 03 e8 01 01 bc 10 6b a2 7e`; 2564: `… 2a 05 22 03 e8 01 02 06 41 62 3b 7e`.
The connect-time read is `4:{29:2}` (1226, 7209, 8470) with the toggle ON on film (16:06:20, 16:06:54). Together with `CAP-020` 1935/2038 (both directions,
the OFF tap on film) and the smali write site (`PROTOCOL.md` §4.5.4, 2026-10-03 Update), field 29 = Head gestures with **1 = off, 2 = on** holds in 3 + 3
filmed samples on the current firmware and app, zero counter-examples. 🟢 FACT for this capture.

**GSND CONTROL Code `0x16` (🟡, refines lead L68-4):** its value is **not** a copy of field 29 — it is `08 01` after every ON and `08 02` after every OFF
(6/6), i.e. **inverted** against the skeleton's prediction (`BE-2`'s "Expected": `08 01` after ON — that matched; `BE-3`'s `08 02` after OFF — matched; the
skeleton therefore predicted it right, field 29 and Code `0x16` simply use opposite numbers). It also changes without a write: `08 02` with the setting ON when no
bud is worn (6329 16:19:40 both docked; 6880 and 8359 at connect; 7521 both buds on the table) and `08 01` when a bud goes into an ear (2262, 7320). 🟡
HYPOTHESIS: Code `0x16` = "head gestures active" (1 = active: setting on and worn; 2 = inactive). Test: a head-gesture toggle with no bud worn (predict no
`04 16` change, or `02` both ways).

## 2. `MULTI-001` — Multipoint = field 11 (0 = off, 1 = on) and SASS bit 2 (🟢 FACT for this capture; proposal §13)

| Filmed tap (phone) | Write | Raw | OK | Stream | SASS `07 11` after (MS, Buds → phone) |
|---|---|---|---|---|---|
| 16:12:05.9 OFF | 3161, 16:12:06.338 | `2a 04 22 02 58 00` = `4:{11:0}` | 3170 | 3165, 3170 | 3166 `07 11 00 04 01 02 98 00` (+0.34 s) |
| 16:12:14.4 ON | 3212, 16:12:14.919 | `2a 04 22 02 58 01` = `4:{11:1}` | 3219 | 3214, 3219 | 3215 `… b8 00` (+0.02 s) |
| 16:12:20.9 OFF | 3245, 16:12:21.524 | `… 58 00` | 3254 | 3247, 3254 | 3248 `… 98 00` |
| 16:12:27.0 ON | 3275, 16:12:27.685 | `… 58 01` | 3285 | 3277, 3285 | 3278 `… b8 00` |

Raw 3161: `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 04 22 02 58 00 ad 63 6b ac 7e` — byte-identical to `CAP-019` 2482 (`PROTOCOL.md` §4.5.2).
Every connect-time `07 11` reads `b8` with Multipoint on (611, 6438, 6899, 8267); the read is `4:{11:1}` (1267, 7272, 8326). `0xb8` = `1011 1000`, `0x98` =
`1001 1000`: only bit 5 (LSB numbering) changes = **bit 2** in the SASS page's MSB-first numbering. The Fast Pair SASS page
(https://developers.google.com/nearby/fast-pair/specifications/extensions/sass, raw text fetched 2026-10-04) says: *"Bit 2: multipoint current state 1, if
multipoint is on 0, otherwise"*. 🟢 FACT for this capture: the "Use multipoint" switch writes field 11 = 0/1, acknowledged OK, and the Buds' SASS capability flags
follow it within 0.35 s, 4 of 4 (plus `CAP-019` 2296/2487 — six of six). The phone answers each ON with `07 21 00 00` (3222, 3281; also at connect 779, 6441,
6900, 8272) — 🟡 meaning not read here.

## 3. `HOLD-004` / `HOLD-003` — the assistant press-and-hold (lead L68-1)

- **The setting (🟢):** "Customize right" → Digital assistant writes `4:{7:{2:{4:{1:6}}}}` (3370, 16:13:16.605, OK 3374) and back to Active noise control
  `4:{7:{2:{4:{1:5}}}}` (5482, OK 5486); the RpcPacket payload `2a 0a 22 08 3a 06 12 04 22 02 08 06|05` is byte-identical to `CAP-021` 3619/4976 (those on
  channel 19, address `00 3b`; these on 21, `00 4b`). GC `04 14 00 02 08 03` (3373) and `… 08 01` (5485) follow each write — 🟡 Code `0x14` = the Right bud's
  press-and-hold action as GSND sees it (3 = assistant, 1 = ANC); two samples.
- **The holds (🟢 on the wire; ⚪ the hold itself is not on film):** the bud and the head are out of view. Three assistant sessions, each opened by a Buds
  message `01 09 00 03 0a 01 03` on GC and the first GSND AUDIO payload frame **1.7–2.3 ms** later:

| Start (GC frame, time) | First AUDIO frame | Further GC messages | Phone `08 11` | AUDIO frames | Phone `08 06 00 04 08 00 10 01` | Film |
|---|---|---|---|---|---|---|
| 3403, 16:13:32.972 | 3404 (+2.3 ms) | `… 05` 3417, `… 03` 3423, `… 01` 3456, `… 21` 3463 | 3470 | 258 (3404–3995) | 3992, 16:13:42.20 | mic pill 16:13:34.4, overlay 16:13:34.9 |
| 4084, 16:13:59.371 | 4085 (+1.7 ms) | `… 01` 4119, `… 21` 4120 | 4122 | 221 (4085–4579) | 4576, 16:14:07.68 | mic active 16:14:00.2 |
| 4806, 16:14:51.212 | 4807 (+2.0 ms) | `… 05` 4822, `… 03` 4827, `… 01` 4882, `… 21` 4883 | 4888 | 7 + 256 (4807–4823, 4829–5406) | 5401, 16:15:02.15 | mic active 16:14:54.4 |

  All AUDIO frames are Buds → phone on DLCI 0x0a of ACL 1; nothing of their content is decoded (skeleton "Don'ts"). With each session the phone sets AVRCP
  absolute volume 48 → 32 % and back to 48 % at its end (16:13:34.62 / 16:13:42.31; 16:14:00.29 / 16:14:07.76). 🟢 FACT (3 of 3): an assistant session
  on DLCI 0x0a starts with GC `01 09 00 03 0a 01 03` and ends with the phone's `08 06 00 04 08 00 10 01` ≈ 8–11 s later. 🟡 HYPOTHESIS: `01 09 … 0a 01 xx` reports
  the hold gesture (03 = press, 05 = released early, 01/21 = hold confirmed); the third session's `03 → 05 → 03` (a short press, 7 audio frames, then a second
  press 2.4 s later) fits a re-press. Test: a hold with the bud on film at 4 fps. The `CAP-021` waves (lead L68-1) have the same opening message — this capture
  ties that message to an assistant activation on screen, not yet to a filmed finger.
- **BE-12, the negative (🟢):** between the end of session 2 (4579, 16:14:07.70) and the start of session 3 (4806, 16:14:51.21) — 43.5 s with nothing touched on
  film — `tshark -r CAP-069-btsnoop_hci.log -Y 'bthci_acl.chandle==0x0002 && btrfcomm.dlci==0x0a && btrfcomm.len>0 && frame.number>4579 && frame.number<4806'`
  → 0 frames (exit 0), and `… btrfcomm.dlci==0x08 && frame.number>4579 && frame.number<4806 && frame contains 01:09:00` → 0 (exit 0). Positive control: the same
  filter over 3400–3470 returns 3403 3417 3423 3456 3463. No wave without a session.
- At wear changes a different message appears: `01 09 00 04 0a 02 0b 06` (6155, 6323, 7455, 7514) with no AUDIO payload — 🔴 meaning.

## 4. `ANC-006` — a tap on the ANC mode that is already selected: the official app sends **no** `Set` (🟢 FACT for this capture)

Four taps on another mode (positive controls) and three taps on the highlighted mode, all with both buds worn (Settable `e8`):

| Filmed tap (phone) | From → to | `08 12` | Answer |
|---|---|---|---|
| 16:15:26.9 | Adaptive → Noise cancellation (`BE-14`) | 5519 `08 12 00 14 01 e8 e8 08` + 16 bytes, 16:15:27.85 | ACK `ff 01 00 06 08 12 01 e8 e8 08` 5523; Notify `08 13 00 04 01 e8 e8 08` 5524, 5527 |
| 16:16:10.9 | NC → Adaptive | 5600 `… e8 e8 40`, 16:16:12.01 | ACK 5606; Notify 5607, 5610 |
| **16:16:16.4** | **tap on Adaptive (selected)** (`BE-15`) | **none** (5601–5643: 0) | — |
| 16:16:33 | Adaptive → Transparency | 5644 `… e8 e8 80`, 16:16:34.48 | ACK 5651; Notify 5652, 5655 |
| **16:16:42.4** | **tap on Transparency (selected)** (`BE-16`) | **none** (5645–5691: 0) | — |
| 16:16:50.4 | Transparency → Off | 5692 `… e8 e8 20`, 16:16:51.88 | ACK 5696; Notify 5697, 5698 |
| **16:16:58.4** | **tap on Off (selected)** (repeated) | **none** (5693–end: 0) | — |

Command: `tshark -r CAP-069-btsnoop_hci.log -Y "btrfcomm.len>0 && frame.number>=A && frame.number<=B && frame contains 08:12:00:14"` → 0 lines (exit 0) for each window;
the same filter over the whole log returns exactly 5519 5600 5644 5692 (positive control). Each finger is on the highlighted button for 2–4 frames at 4 fps; the
button stays highlighted, the row is not greyed (it is greyed only during a real change, 16:15:27, 16:16:50). The four real changes are ACKed (0 NAK); each
`Set` left 0.4–0.9 s after the tap. Compare `CAP-068` (`PROTOCOL.md` §4.1, 2026-10-04 Update): OpenControl sends the `Set` for the current mode and the Buds
ACK it. The decision is the maintainer's (§12 item 3, checkpoint).

## 5. `EQP-001` — the official "Default" preset writes 0.0 × 5 = OpenControl's "Flat" (🟢 FACT for this capture)

Five Default taps, five writes `4:{16:{0.0, 0.0, 0.0, 0.0, 0.0}}`, raw RpcPacket payload
`2a 1e 22 1c 82 01 19 0d 00 00 00 00 15 00 00 00 00 1d 00 00 00 00 25 00 00 00 00 2d 00 00 00 00` in 1990 (16:07:18.544, from Balanced), 5803 (16:17:36.77, from
Heavy bass), 5965 (16:18:44.02, from Heavy bass), 6066 (16:19:07.80, from Last saved) — every one OK — byte-identical in payload to OpenControl 1.0.1's "Flat"
(`CAP-015` frame 2111, `PROTOCOL.md` §4.2 2026-10-03 Update). (Four writes for five filmed selections: the menu opened at 16:19:10 and 16:19:14 with Default
already ticked and closed without a write.) Heavy bass wrote `{5.0, 3.0, 0, 0, 0}` (5783, 5942) = the §4.2 preset table. 🟢 FACT for this capture:
"Default" = all five bands 0.0.

**Field 18 and the Save button (🟡, lead for `CAP-053`):** five slider drags wrote field 16 once per band (6006, 6018, 6026, 6033, 6039; last at 16:18:58.57);
the filmed **Save** tap (16:18:59.4–59.9) was followed by `4:{18:{-4.9, 4.3, -4.4, 3.5, -4.2}}` (6050, 16:19:00.58, OK 6053) and the label "Last saved"; no
navigation in between. One sample in which the Save tap is isolated from slider release (≈ 2 s) and from leaving the page — 🟡 HYPOTHESIS strengthened: a
field-18 write follows the Save button. "Last saved" then wrote the same curve to field 16 (6120). The next connections read 16 = 18 = that curve (7185, 7228,
8312, 8437).

**Not restored (🟢):** the run ends with the EQ on the custom curve (not Balanced, P7) and with field 12 = all four modes (Off ticked at 16:06:40, not
unticked) — the reads at the last connect show it (8312, 8400).

## 6. Field 12 — one list for both buds (🟢 for this capture; strengthens `PROTOCOL.md` §4.5.3's 🟡)

Ticking **Off** in "Customize right" (finger 16:06:40) wrote `4:{12:{1:1 2:1 3:1 4:1}}` (1813, OK 1816) — no side field, as in `CAP-056` — and 3.5 s later
"Customize left" showed Off ticked without any write (film 16:06:43–44; no `WriteSetting` between 1816 and 1990). The film test `PROTOCOL.md` §4.5.3 asked for
("open Customize right while a mode is unticked on the left", done here the other way round) shows one list in the app. ⚪ Whether the app reads the list back
from the Buds when the second screen opens, or shows its own cached copy, is not visible (no `ReadSetting 4:12` between 1816 and 1990).

## 7. `FIND-001` / `FIND-005` — Find in the official app 1.0.990706425

- **🟢 FACT (film):** the app has no "Find device" entry: Device details, Controls and gestures, Sound, Hearing wellness, Audio switch, More settings (the whole list,
  16:22:30–48 and 16:23:34–48) and Case sounds were opened; none offers Find. The maintainer opened **Find Hub** from the launcher (16:23:58). Find Hub listed two
  "Pixel Buds Pro 2" entries ("Nearby"); one "Device not seen for 7 days", the other with Case/Left/Right "Last seen Sep 20 around 13:56/13:57".
- **Play sound — Left** (16:24:30.4) and **Right** (16:25:06.4): each showed "Connecting…" and *"If you have another device linked with your Google Account, it
  may try to play sound on Pixel Buds Pro 2."*, then **"Can't play sound"** (16:24:54, 16:25:30). Nothing rang (film; no audio track).
- **🟢 FACT (wire), the checked negative:** `tshark -r CAP-069-btsnoop_hci.log -Y "btrfcomm.len>0 && frame contains 04:01:00:01"` → 0 frames (exit 0); positive
  control: the same filter on `CAP-068-btsnoop_hci2.log` returns 2142, 2153, 2416, … No `04 01` and no `ff 01 … 04 01` in either direction in the whole log
  (message-level parse: 0 Group-`0x04` messages). In the Find Hub window (16:24:25–16:25:40) the Buds' handle carried only MAESTRO traffic and one Buds battery
  burst `03 03 00 03 64 64 ff` ×3 (9840–9847); the phone ran LE scans (6 × `LE Set Extended Scan Enable`, 561 advertising reports) and advertising (4 ×
  `LE Set Extended Advertising Enable`) — 🟡 Find Hub's own nearby search; not analysed further (ADR-008/027).
- **`FIND-005` (lead L68-2):** not observable — nothing rang, so nothing could be stopped on the bud. `PROTOCOL.md` §4.4's 🟡 (the Buds' `04 01 00 01 00` on a
  stop on the bud) stays open; with the official app's Find gone, the only remaining way to test it is OpenControl's own Ring with a client holding the Message
  Stream when the bud is touched (§12 item 6).
- **OpenControl's own Find is not affected (🟢 by reference):** OpenControl sends its own `04 01 00 01 xx` on its own claim and the Buds ACK it (`CAP-068-FINDINGS.md`
  §6); nothing in this capture changes the Buds' side.
- **New Device-Information message (🟢 wire + spec):** on ACL 2 and 3 the Buds send `03 0b 00 19 …` right after the claim (6853, 8241 — 8241's value is zeros
  but for `02 5f e7 f0` and a final `fe`). The Device Information page
  (https://developers.google.com/nearby/fast-pair/specifications/extensions/deviceinformation, raw text fetched 2026-10-04) defines *"0x0B: Current FHN ephemeral
  identifier message … Additional data, length 24 or 36 bytes"*; here the length is 25 (`0x0019`) — 🔴 the extra byte. It belongs to Find Hub Network (out of
  scope, ADR-008/027): recorded, not decoded.

## 8. Wear and placement states — DLCI GC Code `0x05` and the other signals (lead L68-5, `INEAR-002`…`004`, `CASE-004`/`005`)

Bud identity from the case slot and the charging bits; the ears are **not** on film (⚪ "in an ear" is inferred from a hand covering the camera followed by the
Buds' own messages).

| Phone time | Movement (film) | GC `04 05` | Notify (Settable, mode) | MS battery (L, R) | Runtime 6.2/6.3 (2 = charging) | Other |
|---|---|---|---|---|---|---|
| connect 16:05:59 | both in the case | `03` (694) | `00`, Off (617) | `e4 e4` (588) | 2/2, Case 68 (933) | `04 16 02` |
| 16:08:32.6 | Right out of the case | `05` (2255, 16:08:34.91) | `e8`, Transparency (2256) | `e4 64` (2217) | 2/1 (2236) | `04 16 01` 2262 |
| 16:08:42–46 | Left out; both out of view | `05` (2351), then `06` (2363, 16:08:48.48) | `e8`, Adaptive (2366) | `64 64` (2306) | –/– (2315) | |
| ≈16:19:35 | (not on film) a bud out of an ear | `04` (6153) | `e8`, Transparency (6156) | — | — | Buds `DISC` MAESTRO + MS 6157/6158 |
| 16:19:37.5 | Right into the case | `04` (6257, after re-open) → `03` (6326, 16:19:40.35) | — | — | 1/2, Case 67 (6352) | `04 16 02` 6329; announcement ch 19 |
| ≈16:19:42.4 | Left into the case | — | `00`, Off (6434) | `64 e4` (6425) | — | ACL drop `0x13` 6468 |
| 16:19:58 | Left out, in the hand | `03` (6875) | `00`, Off (6894) | — | — | Buds reconnect 6580–6584; ch 19 |
| 16:20:20–23 | Left into an ear | `04` (7309, 16:20:23.19) | `e8`, Transparency (7317) | — | — | `04 16 01` 7320 |
| 16:20:34.4 | Right out of the case, in the hand | `04` (7385, 16:20:37.81) | — | unchanged | **still 2** for Right (7358, 16:20:35.99) | |
| 16:20:43–46 | Right into an ear | `06` (7415, 16:20:46.32) | `e8`, Off (7416) | `64 64` (7404, 16:20:46.06) | 1/1 (7412) | |
| 16:21:00–02 | Right out of the ear → table | `04` (7453) | `e8`, Transparency (7458) | — | — | `01 09 00 04 0a 02 0b 06` 7455 |
| 16:21:20–23 | Left out of the ear → table | `03` (7517, 16:21:22.93) | **`00`**, Off (7520) | — | — | `04 16 02` 7521 |
| 16:21:36–38 | Right into the case | `03` (7615) | — | `64 e4` (7590) | 1/2, Case 67 (7594) | |
| 16:21:48–50 | Left into the case | — | — | — | — | ACL drop `0x13` 7658 |
| 16:22:12–16 | both out to the table | `03` (8352) | `00`, Off (8260) | `64 64` (8248) | 1/1 (8159) | Buds reconnect 7759–7763; ch 21 |

Readings (each a 🟡 HYPOTHESIS unless marked):
- **Code `0x05`:** `03` with no bud worn (both in the case, one in the hand, both on the table — 6 samples: 694, 6326, 6875, 7517, 7615, 8352), `06` with both buds worn (2), `04` with exactly one bud
  worn (5), `05` twice during the unfilmed insertion at 16:08 (🔴 — "one worn, other out of the case" fits 2351 but not 7385, where the same state read `04`).
  This narrows L68-5: 3 = no bud worn, 4 = one worn, 6 = both worn; **5 stays 🔴**; value 1 was not seen. Test: insert one bud with the other in the hand, ears
  on film.
- **Settable `00` only with no bud worn** (617, 6434, 6894, 7520, 8260; `e8` in every other Notify) — supports ADR-049 item 3 (🟡 "`00` ⇒ no bud worn"), 0
  counter-examples.
- **The Buds change the ANC mode by themselves on wear changes** (🟢, 7 Notifies with no `Set` before them: 2256, 2366, 6156, 7317, 7416, 7458, 7520): out of an ear →
  Transparency (`80`), into an ear → the mode set before (Adaptive 2366, Off 7416) or Transparency (7317). The film shows the app's highlight move without a tap
  (16:20:24, 16:20:46). 🟡: this is the Buds' own "Transparency when one bud is out" behaviour.
- **The charging bit lags removal (🟢 for this sample):** the Right bud was off the case at 16:20:34.4 on film, yet runtime info (7358, 16:20:35.99) still said
  charging and the MS battery flipped only at 16:20:46.06 (7404), 0.26 s before `04 05 06` — when the bud went into the ear, 11.7 s later. Elsewhere the bit
  changed within 0.5 s of the movement (2217, 2306). 🔴 why; relevant to OpenControl's "charging in the case" line (§12 item 7).
- **Field 13 on the settings stream (🟡, `PROTOCOL.md` §6 item of 2026-09-28):** `13:2` NC, `13:4` Adaptive, `13:3` Transparency, `13:1` Off — 4/4 with the four `Set`s
  (5528, 5611, 5656, 5701) and 6/6 with the unsolicited Notifies (2270, 2367, 7327, 7419, 7459), `13:0` with Settable `00` (7530). Strengthened; field 13 is
  readable (`ReadSetting 4:13` → `13:0`, 933).
- **The MAESTRO channel follows the hosting bud (🟡, `PROTOCOL.md` §2.2a):** announcement ch 21 with both buds in the case (907), ch 19 when only the Left was
  out (6336, 7089), ch 21 with both taken out together (8139) — consistent with "the channel names the bud that hosts the link".

## 9. What is new in the official app 1.0.990706425 (wire only; nothing decompiled — ADR-017 boundary)

Compared with `CAP-056` (version 1.0.955078536, same phone; `python3 scripts/pwrpc_decode.py` on both logs):

| | `CAP-056` (955078536) | `CAP-069` (990706425) |
|---|---|---|
| Channels used by the phone | 19 and 21 | 19 and 21 (unchanged — requests go out on both) |
| Services | Maestro, Dosimeter, Multipoint, DynamicServerConfigService, BundledUpdate, UpdateHelperService | the same **plus `maestro_pw.JitterBuffer`** (first time on any wire: method `0x8d99df93`, request `1:1`, 965/6339/…, 7 requests, each answered by an empty RESPONSE) **and an unnamed service `0xbf6c9399`** (method `0x92476025`) |
| `0xbf6c9399` | — | subscribed on ch 19 and 21 when Device details/Sound opened (1894/1896, 16:06:58.6; 8848/8853, 16:23:15.1), 146 `SERVER_STREAM` packets of ≈ 50 bytes (`1:2 2:<bytes>`), cancelled with `CLIENT_ERROR CANCELLED` when the Equalizer opened (1943/1944, 16:07:09.5); the second stream ran to the end of the log (last 10575) |
| Dosimeter `SubscribeToLiveDb` | 12 | 4 (cancelled 9021/9022) |
| Find device | in the app | **gone** — Find Hub only (§7) |
| Device Information `03 0b` | not checked | sent by the Buds (§7) |

The name of `0xbf6c9399` is not among the nine names in `scripts/pwrpc_decode.py` (65599 hashes of the APK 955078536 literals) — 🔴 OPEN QUESTION; the way to
name it is the string table of the new APK (pull and decompile 1.0.990706425 — a follow-up, checkpoint question). No error status from the Buds: every `UNKNOWN`
answer is a `ReadSetting` of a field the firmware does not hold (1, 25, 34, 35, 36 and three empty — as in earlier captures), `NOT_FOUND` only for
`GetStagedVersion`.

## 10. Connection lifecycle and the other protocols (🟢 FACT for this capture)

- **ACLs:** ACL 1 phone-initiated after Bluetooth on (`Create Connection` 178 → 291, 16:05:58.215); dropped by the Buds with reason `0x13` when the second bud
  was seated (6468, 16:19:43.815 — ADR-016); ACL 2 Buds-initiated when the Left left the case (`Connect Request` 6580 → 6584, 16:20:02.334 — ADR-016's
  Buds-initiated variant); dropped `0x13` at both docked (7658); ACL 3 Buds-initiated when both left the case (7759 → 7763, 16:22:14.177) and up to the end.
- **RFCOMM:** the Buds closed MAESTRO and the Message Stream together (`DISC` 6157/6158, 16:19:36.358) at a bud-out-of-ear moment with the ACL up; the phone then
  closed GSND CONTROL/AUDIO (6174/6175) and re-opened all four within 1.1–6.0 s (6204, 6232, 6314, 6407) — the official app (MAESTRO) and Play services (MS claim
  6416). On ACL 3 the phone closed and re-opened GSND AUDIO (8157 → 8271) — 🔴 why.
- **Multiplexer side:** on ACL 3 both sides sent `SABM` DLCI 0 within 10 ms (7967/7982); the phone accepted the Buds', so its own DLCIs are odd (MAESTRO 3, MS 5,
  GSND 9 / 0x0b) and **DLCI 0x08 is HFP** there (the Buds opened the phone's server channel 4: `AT+BRSF=921` 6973 on ACL 2's DLCI 0x09, AT text on ACL 3's 0x08).
  Counting by DLCI number alone would mix GSND CONTROL with HFP — confirms `PROTOCOL.md` §2.3's "count by server channel".
- **HFP:** 52 frames containing `AT+BIEV` (`tshark -Y 'frame contains "AT+BIEV"'`, exit 0), all `AT+BIEV=2,100`, on every ACL.
- **AVRCP/A2DP:** 48 `btavctp or btavrcp` frames (exit 0): registration at connect, the volume ducking around each assistant session (§3), playback-status
  changes Playing/Stopped at the end of sessions 1 and 2 (16:13:42.6–43.7, 16:14:07.7). AVDTP signalling on all three ACLs (26, 28 and 28 frames on ACL 1, 2 and 3; A2DP set-up). No audio
  routing content is analysed (`PROJECT.md` non-goal).
- **Firmware:** `release_5.203` in all three entries of all four announcements (and on film).
- **Runtime-info field 2 (🟡, desk entry of 2026-10-03):** equals the phone's clock to 0.1–0.8 s (e.g. 7358: `1791123635619` = 16:20:35.619, frame time
  16:20:35.989) — supports "wall clock, ms".

## 11. What works and what does not in the official app (this run)

- **Works (🟢):** every setting write acknowledged OK (26 `WriteSetting`, 26 OK, 0 error); ANC 4/4 ACKed; head gestures, Multipoint, press-and-hold, EQ presets,
  slider writes and Save; reconnection after each dock change without a tap; the settings screens show the Buds' values at each connect.
- **Does not / changed:** Find device is gone from the app (Find Hub only); Find Hub's Play sound failed for Left and Right with the Buds connected to the same phone
  ("Can't play sound", no local command); the app greys the ANC row and shows "Connect" when both buds are docked (by design, the link drops); the app's ANC
  highlight follows the Buds' own mode changes on wear changes without telling the user why (§8).
- **Run defects (the maintainer's statement "not every step was done correctly" — confirmed):** BE-17 done before section I and repeated; one extra OFF/ON pair
  in I; an extra third assistant hold; one extra current-mode tap; extra EQ drags + Save; BE-18 restored "Last saved" instead of Balanced; the Off tick in field
  12 (16:06:40) was not undone; the head was never filmed; BE-28/29 could not be run (app change).

## 12. Improvements for OpenControl (proposals only — nothing under `android/`, `dist/` or `scripts/` is changed)

Common guardrails for each: no new permission, service, dependency or background work; no polling timer; one attempt per event (ADR-044); every write through the
Safe-Mode gate (ADR-042), applied only on the empty `RESPONSE` status OK, on the announced channel with its ADR-034 address; fixtures are real bytes of this
capture (`AGENTS.md` §11).

1. **Head gestures switch (field 29).** Needs: §13 P-1 promoted + a new ADR (draft D-1). Change: `SettingsCodec` encodes/decodes `4:{29:1|2}`;
   `Maestro.READABLE_FIELDS` + 29; a "Head gestures" switch on Controls (`SettingsMenu.kt`), read at Connect with the other fields, `BudsRepositoryImpl.writeSetting`.
   Tests: encode 2492/2564's bytes on channel 21 and their channel-19 counterparts (`CAP-020` 1935/2038), decode reads 1226/7209; unknown value (0, 3) → "—".
   Re-test: tap OFF → `4:{29:1}` → OK + stream `29:1` + GC `04 16 … 02`; ON → `29:2` → OK.
2. **Multipoint switch (field 11).** Needs: §13 P-2 + ADR (draft D-2). Change: as item 1 for `4:{11:0|1}` (readable already per ADR-036). Optionally show the SASS
   bit as a cross-check — not needed. Tests: 3161/3212 bytes. Re-test: OFF → `4:{11:0}` → OK + `07 11 … 98 00`. Note: OpenControl does not hold the Message
   Stream at that moment, so the SASS answer may go to Play services — the write's OK is the acknowledgement.
3. **`ANC-006`** — decision, options at the checkpoint: (a) keep sending the `Set` (status quo; ACKed in `CAP-068`, harmless); (b) skip it when the same claim's `Get`
   just reported the requested mode (matches the official app: 3/3 no `Set`; one less command on a shared channel; needs a code change and a test with
   `CAP-069` 5600/5607 as fixtures). Recommended: (b), only as a small change after the maintainer's choice.
4. **EQ "Default".** OpenControl's "Flat" writes the same bytes as the official "Default" (§5). Options: rename "Flat" → "Default" (matches the official app's
   label, no wire change), or keep "Flat" and note the equivalence. No ADR needed (a UI label); `PROTOCOL.md` §4.2 Update for the fact.
5. **"Save as preset" (field 18) — later.** §5's Save sample is one 🟡; `CAP-053` should settle it before OpenControl ever writes field 18.
6. **Ring status (`FIND-005`).** Nothing new on the wire; the official app no longer rings at all. Proposal: keep 1.0.1's behaviour; test the Buds' stop message
   with OpenControl's own Ring while its claim is still open (a test-only longer linger, or Play services holding the channel) — a capture question, not an app change.
7. **Wear signal.** Code `0x05` (3/4/6) and Settable `00` are candidates for a "worn" indicator, but the app does not open GSND CONTROL (ADR-043) and Code `0x05` is
   🟡. Proposal: nothing now; if wanted, field 13/Settable from the existing claims only. The lagging charging bit (§8) is a reason to word "charging in the case"
   carefully (it can stay set ≈ 12 s after removal).
8. **Explain the Buds' own ANC changes.** When a `Notify` changes the mode without a user tap, OpenControl could say "changed by the Buds" in the (i) details.
   UI-only, no wire change.

## 13. `PROTOCOL.md` / `DECISIONS.md` changes — all signed off by the maintainer (chat 2026-10-04) and applied

P-1/P-2 with D-1/D-2 were approved at the checkpoint (`AskUserQuestion` "Field 29" / "Field 11") and became `PROTOCOL.md` §4.5.4/§4.5.2 Updates and ADR-052/ADR-053;
the maintainer then signed off on every proposal of this section in chat (2026-10-04, *"Ik wil een sign-off geven op alle voorstellen … in paragraaf 13"*). P-3 → §4.1,
P-4 → §4.2, P-5 → §4.5.3 (🟢 for the app's UI), P-6 → §4.4, P-7 → §2.3 and §6 (`01 09 00 03 0a 01 03` 🟢 for this capture; the other readings 🟡/🔴 as listed).

- **P-1 (§4.5.4):** 🟡 → 🟢 FACT: "`qhr` field 29 = 'Use head gestures', 1 = off, 2 = on" — `CAP-069` 2492/2564/2737/2831/2923/3025 (each a filmed tap, OK, mirrored)
  + `CAP-020` 1935/2038 + smali `cmi` write site. GC Code `0x16` recorded as 🟡 "head gestures active (1 = active, 2 = inactive)".
- **P-2 (§4.5.2):** the 🟡 SASS reading → 🟢 FACT: "SASS capability flags bit 2 (MSB-first; `0x20`) = multipoint on, follows the field-11 write" — `CAP-069` 3166/3215/3248/3278
  + `CAP-019` 2296/2487 + the SASS page sentence. (Field 11's identity is already 🟢.)
- **P-3 (§4.1):** dated Update: "the official app sends no `Set` for the selected mode" — 🟢 for `CAP-069` (3 taps, 0 `Set`, positive controls 5519/5600/5644/5692).
- **P-4 (§4.2):** dated Update: "official 'Default' = 0.0 × 5" — 🟢 (1990, 5803, 5965, 6066); field-18-after-Save 🟡 sample 6050.
- **P-5 (§4.5.3):** dated Update: one shared list — film 16:06:40–44 with 1813 (🟢 app UI); the Buds side stays as before.
- **P-6 (§4.4):** dated Update: the official app 1.0.990706425 has no Find device; Find Hub's Play sound produced no local command (0 `04 01`) and failed; `FIND-005`
  untestable with the official app.
- **P-7 (§2.3 / §6):** GC Code `0x05` 3 = no bud worn, 4 = one, 6 = both (🟡, 15 samples; 5 🔴); `01 09 … 0a 01 03` opens an assistant session (🟢 3/3); new service
  `0xbf6c9399` and JitterBuffer on the wire (🟢 observed, 🔴 meaning); Device Information `0x0B` = FHN EID (spec).

**Draft ADR D-1 (no number) — DLCI 0x02: `ReadSetting` and `WriteSetting` unblocked for `qhr` field 29 (head gestures).**
Context: field 29 = head gestures, 1 = off / 2 = on, promoted per P-1. Decision: `ReadSetting 4:29` and `WriteSetting 4:{29:v}`, v ∈ {1, 2}, byte-identical to
`CAP-069` 2492/2564 (channel 21) or `CAP-020` 2038/1935 (channel 21 — same address) and their channel-19 forms; announced channel with its ADR-034 address; Safe-Mode
gate (ADR-042); once per tap; applied only on the empty `RESPONSE` status OK; any other read value shown as "—". Consequences: settings codec + switch on Controls;
`Maestro.READABLE_FIELDS` + 29; re-test as §12 item 1. The "Optimize head gestures" dialog of the official app is not reproduced.

**Draft ADR D-2 (no number) — DLCI 0x02: `WriteSetting` unblocked for `qhr` field 11 (Multipoint).**
Context: field 11 is readable (ADR-036); its write, both directions, is acknowledged and the Buds' SASS flag follows (P-2). Decision: `WriteSetting 4:{11:v}`,
v ∈ {0, 1}, byte-identical to `CAP-069` 3161/3212 (channel 21) or the channel-19 form; same rules as ADR-045. Consequences: a Multipoint switch (with the note
"lets the Buds connect to two phones"); re-test as §12 item 2.

## 14. Test-ID traceability (`AGENTS.md` §13)

`HEAD-001` §1 · `MULTI-001` §2 · `HOLD-004`, `HOLD-003` §3 · `ANC-001` (Off) 5692, `ANC-002` (NC) 5519, `ANC-003` (Adaptive) 5600, `ANC-004` (Transparency) 5644 ·
`ANC-006` §4 · `EQP-001` §5 · `HOLD-005` §6 · `INEAR-002`/`003`/`004`, `CASE-004`/`005` §8 (ears not on film) · `FIND-001` §7 (via Find Hub; failed) · `FIND-005` not
observable · `PAIR-003` §10 (Bluetooth off/on and three reconnects) · `BATT-004` §8 (MS battery at every claim; runtime info). `INEAR-001` (the in-ear detection
setting) was not exercised.

## 15. Open questions

- 🔴 GC Code `0x05` value 5; value 1 never seen; Code `0x16` with the setting off and a bud worn.
- 🔴 `01 09 00 04 0a 02 0b 06` at wear changes; the hold codes (03/05/01/21) against a filmed finger.
- 🔴 Service `0xbf6c9399` (name, purpose) and `JitterBuffer` method `0x8d99df93` — needs the new APK's strings.
- 🔴 Device Information `0x0B` length 25 vs the spec's 24/36.
- 🔴 Why the Right bud's charging bit stayed set ≈ 12 s after it left the case (7358 → 7404).
- 🔴 Why the phone closed and re-opened GSND AUDIO on ACL 3 (8157 → 8271).
- 🟡 `FIND-005` (the Buds' stop message) — still untested.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-069-2026-10-04_16-05-29_16-27-07-Group_BE/CAP-069-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-069-2026-10-04_16-05-29_16-27-07-Group_BE/CAP-069-FINDINGS
