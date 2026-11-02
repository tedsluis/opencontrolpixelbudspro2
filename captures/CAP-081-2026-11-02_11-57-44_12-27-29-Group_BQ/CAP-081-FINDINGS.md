# Findings: `CAP-081` (Group BQ — the original Pixel Buds Pro (Gen 1): the official app's writes, then OpenControl's)

**Analysed 2026-11-02 by `ai-sessions/0102`, a third-party contribution** (`CONTRIBUTING.md`: redacted per "the PII exception"). Companion:
`CAP-081-EVENT-NOTES.md` (the timeline and the Test-IDs). Labels per `PROJECT_RULES.md` §1: 🟢 FACT (a frame or a log line), 🟡 HYPOTHESIS, ⚪ ASSUMPTION,
❓ OPEN QUESTION. Nothing here is promoted in `PROTOCOL.md` or decided in `DECISIONS.md`: the pull request proposes that, the maintainer decides
(`AGENTS.md` §6).

Names: **A** = `CAP-081-btsnoop_hci1.log` (part 1, Pixel 4 XL, Android 13, the official Pixel Buds app 1.0.996096625; 11:57:44.417 – 12:11:39.238, 6,520
records), **B** = `CAP-081-btsnoop_hci2.log` (part 2, Pixel 9 Pro, GrapheneOS `CP3A.261005.005` (Android 17), OpenControl debug build; 12:21:28.304 –
12:27:29.201, 2,903 records), **E** = `CAP-081-opencontrol-debug-20261102-122613.txt` (part 2's export, Debug mode on). The Buds: Pixel Buds Pro (Gen 1),
`release_5.11_signed` on case, left and right (A 915, B 1458). Every `tshark` filter below is scoped to the Buds' ACL handle (A `0x0003`, B `0x000b`; the
redaction left only the Buds' ACL traffic in both logs). Decoder: `scripts/pwrpc_decode.py <log>` (exit 0 on both).

**In short (🟢 unless marked).**
- The Gen 1 speaks the same `maestro_pw.Maestro` service as the Pro 2 (`0x7ede71ea`, hello `GetSoftwareInfo` with `call_id 0xffffffff`). It reports
  **`GetHardwareInfo` field 1 = 4** (A 931, B 1591; the Pro 2: 6) and Fast Pair **Model ID `6edaf7`** (A 568: `03 01 00 03 6e da f7`).
- **The official app's Gen 1 writes on Maestro** (all channel 21, all `RESPONSE OK`): in-ear detection 2, touch controls 4, press-and-hold 7 (one bud per
  write), the hold list 12 (three booleans, never a fourth), Volume EQ 15, the live EQ 16, the saved EQ 18, balance 17, mono 19, conversation detection 22.
  **Byte for byte the frames this app's encoders build** (`Gen1OfficialAppWritesTest`, 22 frames of A, whole frame incl. the CRC).
- **Find** goes over the Message Stream (DLCI 0x04) exactly as on the Pro 2: `04 01 00 01 02` left, `01` right, `00` stop (A 1416, 1491, 1477), each ACKed.
  The official app offers **Ring left and Ring right only** (no "Ring both" on the screen; no `…03` in A).
- **The official app does not use Maestro for the noise-control mode or multipoint on a Gen 1.** It sends `08 12 …` (mode) and `07 12 …` (multipoint) on
  the Message Stream with 16 trailing bytes (A 1243/1252/1262, A 4543). 🟡 They are the Fast Pair MAC extension's authentication data (an 8-byte message
  nonce and an 8-byte MAC keyed with the account key) — inferred from the length and the session nonce `03 0a` the Buds send at every open (PROTOCOL.md §0.1);
  not checked against the specification page in this session. This app has no account key and does not send these.
- **This app's `WriteSetting 4:{13:n}` and `4:{11:b}` work on the Gen 1** (B 1812 … 1874, 2575; 2040/2046/2720): every one `RESPONSE OK`, the mode pushed back
  as `13 = n` on the settings stream. After part 2 left the Buds at noise control Off and multipoint off, the official app on the Pixel 4 XL showed **noise
  control Off and Multipoint off** (the maintainer of this contribution, checked by eye, not filmed). So the writes change the Buds' own state, not only what
  this app reads back.
- **Part 2 (this app): 43 `WriteSetting`, 43 `RESPONSE OK`, on fields 2, 4, 7, 11, 12, 13, 15, 16, 17, 19, 22 only.** 20 of the 43 frames are identical to an
  official-app frame of A; the other 23 are the mode and multipoint (above) or values the official app never wrote (other EQ gains and balance values, one
  hold list). No write of 27, 28 or 29, no Adaptive, no Ring both (§4).
- **The Gen 1 has no settings 27, 28, 29**: the official app reads them and gets status 2 `UNKNOWN` with an empty value (A 1014, 1017, 1020). It also writes
  `4:{38:0}` after each touch-controls change and gets status 2 (A 1773/1775, 5396/5398). This app's Gen 1 read sweep leaves out 27, 28 and 29 (B 1464 … 1581).

## 0. Setup, logs, privacy

- **Phones.** Part 1: Pixel 4 XL (`coral`), Android 13, the official Pixel Buds app `1.0.996096625` (`dumpsys package
  com.google.android.apps.wearables.maestro.companion`), Google Play services present. Part 2: Pixel 9 Pro (`caiman`), GrapheneOS `CP3A.261005.005`
  (Android 17), no official app; OpenControl debug build of branch `gen1-support` (base `9193b64` plus this pull request's code changes; APK SHA-256 in
  `CAP-081-sha256sums.txt`). This app needs Android 14, so it could not run on the Pixel 4 XL.
- **Logs.** A is the snoop log of the second bugreport of part 1 (the first bugreport's log is a byte prefix of it: `cmp -n 452230`, exit 0). B is the
  snoop log of part 2's bugreport. Both bugreports, the three screen recordings and the two unredacted logs stay local; their SHA-256 are in
  `CAP-081-sha256sums.txt` (`DECISIONS.md` ADR-066).
- **Redaction** (`scripts/redact_btsnoop.py`, `CONTRIBUTING.md` "the PII exception"). Same record count and timestamps, so **every frame number here is the
  original's**. ACL data of other devices and the LE advertising reports are replaced by an empty vendor event (`04 ff 00`; A 1,927, B 716 records); the Buds'
  address is `00:00:00:00:ee:02`, the phones' `…:ee:01`; the link keys are zeroed (A 2, B 1); the three serial numbers, the owner's first name (in the Buds'
  name) and the time zone (Assistant channel) are replaced by `X` of the same length. A replaced serial leaves its `GetHardwareInfo` frame's CRC unmatched
  (A 931, B 1591): a decoder reports it as a bad frame. Checked afterwards: no original address in either byte order, no serial, no name
  (`strings`, a byte search, exit 0); the decoded `WriteSetting` lists of the redacted and the original logs are identical (`diff`, exit 0).
- **E** holds state lines and, from the moment Debug mode was switched on, inbound frame hex; no serial (the hardware info was read before Debug mode) and no
  address (`grep -i -c -E "([0-9a-f]{2}:){5}[0-9a-f]{2}"` → 0; positive control: the same pattern on `tshark -V` of the unredacted part-2 log's Connection
  Complete → 1).

## 1. Identification (🟢)

| Signal | Gen 1 | Evidence |
|---|---|---|
| Firmware (hello `GetSoftwareInfo` field 4) | `release_5.11_signed` × 3 (case, left, right; version `575`) | A 915, B 1458 |
| `GetHardwareInfo` field 1 (device type) | **4** | A 931, B 1591 (`1:4 2:1 5:7 …`) |
| Fast Pair Model ID (Message Stream group 3 code 1) | **`6edaf7`** | A 568 `03 01 00 03 6e da f7` |
| Settings 27, 28, 29 | status 2 `UNKNOWN`, no value | A 1014, 1017, 1020 |

## 2. The official app's writes on Maestro (part 1)

Command: `scripts/pwrpc_decode.py CAP-081-btsnoop_hci1.log | grep WriteSetting`; raw frames: `tshark -r CAP-081-btsnoop_hci1.log -Y "frame.number in {…}"
-T fields -e frame.number -e data.data`. All `REQUEST` on channel 21 (address `00 4b`), answered `RESPONSE` status OK on `00 a5`.

| Setting | Request (answer) | Payload | Raw frame (pw_hdlc) |
|---|---|---|---|
| Touch controls off / on | 1765 (1769) / 1786 (1788) | `4:{4:0}` / `4:{4:1}` | `7e004b0310151dea71de7d5e251d9a8c9e2a042202200053908d4b7e` / `…2a0422022001c5a08a3c7e` |
| Press-and-hold left → noise control / Assistant | 1848 (1851) / 1885 (1887) | `4:{7:{1:{4:{1:5}}}}` / `…{1:6}` | `…2a0a22083a060a04220208059601c6257e` / `…2a0a22083a060a04220208062c50cfbc7e` |
| Press-and-hold right → Assistant / noise control | 1924 (1926) / 1939 (1941) | `4:{7:{2:{4:{1:6}}}}` / `…{1:5}` | `…2a0a22083a06120422020806dad04a537e` / `…2a0a22083a06120422020805608143ca7e` |
| Conversation detection on / off | 2012 (2014) / 2035 (2037) | `4:{22:1}` / `4:{22:0}` | `…2a052203b00101b46fc4c07e` / `…2a052203b00100225fc3b77e` |
| EQ live (109 writes, one per slider step) | e.g. 2129 | `4:{16:{1..5: f32}}` | `…2a1e221c8201190d0000000015000000001d0000000025000000002d6866a63ff114ff0b7e` |
| Volume EQ off / on | 2681 (2683) / 2692 (2694) | `4:{15:0}` / `4:{15:1}` | `…2a04220278000f47ef397e` / `…2a04220278019977e84e7e` |
| EQ saved ("Save") | 2825 (2827) | `4:{18:{…}}` | `…2a1e221c9201190dcccccc3f150000c03f1d9899d93f250000c03f2d9899993f9294c3c07e` |
| Balance −20 / +20 / 0 | 3114 / 3131 / 3159 | `4:{17:39}` / `40` / `0` (zig-zag) | `…2a052203880127615eb1387e` / `…2a052203880128f0430ea87e` / `…2a0522038801000aebbb9d7e` |
| Mono on / off | 3171 / 3189 | `4:{19:1}` / `4:{19:0}` | `…2a052203980101ec789af67e` / `…2a0522039801007a489d817e` |
| In-ear detection off / on | 5014 (5018) / 5070 (5074) | `4:{2:0}` / `4:{2:1}` | `…2a0422021000a0a6cb947e` / `…2a04220210013696cce37e` |
| Hold list NC+Off+Tr / NC+Tr / NC+Off | 5200 / 5205 / 5280 | `4:{12:{1:1 2:1 3:1}}` / `{1:1 2:0 3:1}` / `{1:1 2:1 3:0}` | `…2a0a22086206080110011801a59fe6337e` / `…2a0a2208620608011000180192f524327e` / `…2a0a2208620608011001180033afe1447e` |

(`…` = the common prefix `7e004b0310151dea71de7d5e251d9a8c9e`.) The official app also writes `4:{19:0}` after each balance change (3123, 3135, 3163) and
`4:{38:0}` after each touch-controls change (1773, 5396; status 2). 🟢 **Each frame equals this app's encoder output for the same value** — the unit test
`android/data/.../Gen1OfficialAppWritesTest.kt` holds 22 of them as fixtures and passes.

## 3. The official app on the Message Stream (part 1)

Command: `tshark -r CAP-081-btsnoop_hci1.log -Y "btrfcomm.dlci == 4 && btrfcomm.len > 0" -T fields -e frame.number -e hci_h4.direction -e data.data`
(174 frames, exit 0).

- **Find:** `04 01 00 01 02` (1416, sent; the Buds echo it 1419 as their ringing status), `…00` stop (1477), `…01` (1491), `…00` (1620, 1726); each ACKed
  `ff 01 00 02 04 01`. The film shows "Ring left" before "Ring right" (recording 1: "Left earbud volume increasing" at 0:33, "Right earbud volume increasing" at 0:42), so `02` = left, `01` = right — this app's `RingTarget`.
  No `04 01 00 01 03` in A (`data.data contains 04:01:00:01:03` → 0 frames; the same filter without `:03` → 56 frames, positive control).
- **Noise-control mode:** `08 12 00 14 01 a8 a8 20 <16 bytes>` (1243), `… a8 a8 08 …` (1252), `… a8 a8 80 …` (1262), each ACKed `ff 01 00 06 08 12 01 a8 a8 xx`
  and followed by the Buds' `08 13 00 04 01 a8 a8 xx`. Recording 1 shows Off → Noise cancellation → Transparency at 0:00–0:09 (= 11:59:19–28). 🟡 The
  byte after `a8 a8` is the mode bit (`20`, `08`, `80`); the 16 bytes as in "In short" (OPEN QUESTION: their exact input — not pursued: it needs the account key).
- **Multipoint:** `07 12 00 11 01 <16 bytes>` (4543, recording 2 at 0:04 = 12:08:15) ACKed `ff 01 00 03 07 12 01`. No Maestro write of field 11 in A
  (`scripts/pwrpc_decode.py CAP-081-btsnoop_hci1.log | grep "Sent REQUEST.*WriteSetting" | grep -c "4:{11:"` → 0; positive control: the reads `| 4:11` → 3, e.g. 995).
- "Audio switch" was toggled too (recording 2, 0:15–0:21): `07 11 …`/`07 40`…`07 42` exchanges at 12:08:31–33 (e.g. 4769, 4846). Not used by this app;
  recorded for `SWITCH-001` only.

## 4. This app's writes (part 2)

Command: `scripts/pwrpc_decode.py CAP-081-btsnoop_hci2.log | grep WriteSetting` (43 requests, 43 answers, all OK) and the Message Stream filter of §3 on B.

| Setting | Requests (frames) | Values |
|---|---|---|
| Noise-control mode `4:{13:n}` | 1812, 1833, 1843, 1853, 1863, 1874, 2575 | 3, 2, 3, 1, 2, 3, **1 (Off, last)** |
| Touch controls `4:{4:b}` | 1931, 1941 | 0, 1 — = A 1765, 1786 |
| Press-and-hold `4:{7:{1\|2:…}}` | 1952, 1958, 1964, 1970 | L→5, L→6, R→6, R→5 — = A 1848, 1885, 1924, 1939 |
| Hold list `4:{12:{1 2 3}}` | 1976 … 2006 (6) | three booleans each; = A 5200/5205/5280 where the values match |
| In-ear `4:{2:b}` | 2012, 2021 | 0, 1 — = A 5014, 5070 |
| Conversation detection `4:{22:b}` | 2028, 2034 | 1, 0 — = A 2012, 2035 |
| Multipoint `4:{11:b}` | 2040, 2046, 2720 | 0, 1, **0 (last)** |
| EQ live `4:{16:…}` | 2103 … 2250 (10) | other gains than A; layout as A 2129 |
| Volume EQ `4:{15:b}` | 2176, 2195 | 0, 1 — = A 2681, 2692 |
| Balance `4:{17:z}` | 2256, 2262, 2268 | 29 (−15), 74 (+37), 0 — 0 = A 3159 |
| Mono `4:{19:b}` | 2274, 2293 | 1, 0 — = A 3171, 3189 |

- 🟢 Every answer `RESPONSE OK`; every mode write followed by the Buds' push `13 = n` (E: "Noise-control mode written (channel 21): …", 7 lines).
- 🟢 Find: `04 01 00 01 02` (2396, 2429), `…01` (2453), `…00` (2489), each ACKed; the Buds reported "ringing left", "ringing right", "stopped" and the app
  ACKed each status (E 12:25:31–12:25:41). **No `…03`** in B (filter as in §3 → 0; without `:03` → 7 frames).
- 🟢 Negatives: no `WriteSetting` of 1, 3, 5, 6, 8–10, 14, 18, 21, 27–29 or 38 in B (the list above is complete: 43 of 43); the read sweep (B 1464 … 1581) asks
  for 16, 2, 4, 7, 11, 12, 15, 17, 19, 22, 13 — not 27, 28, 29.
- 🟢 The film of part 2 shows the Find tab with Ring Left, Ring Right and Stop only (3:04–3:20) and the noise-control tab with Noise cancellation, Off and
  Transparency only (1:08–1:24, 3:24–3:32).
- **Cross-check on the official app** (after part 2, Buds moved to the Pixel 4 XL): Multipoint **off**, noise control **Off**, the EQ as this app last wrote it.
  ⚪ Reported by the contributor, not filmed.

## 5. What this supports (proposals for the maintainer, nothing decided here)

- 🟡 → proposed 🟢: the Gen 1 identification rule (§1) and the Gen 1 Maestro write shapes of §2 (official-app frames = this app's frames).
- 🟡 → proposed 🟢: `WriteSetting 4:{13:n}` and `4:{11:b}` change the Gen 1's mode and multipoint (B, plus the official app reading them back) — with the
  caveat that the official app itself uses signed Message Stream commands for both.
- 🟢 Find left/right on the Gen 1 = the Pro 2's Device Action; Ring both is not offered by the official app on the Gen 1.

## 6. Open questions

- ❓ What the 16 trailing bytes of `08 12` / `07 12` are computed over (not needed by this app).
- ❓ Field 38 (written 0 by the official app after touch controls, status 2 on the Gen 1).
- ❓ `GetHardwareInfo` field 6 is `-1` when the official app asks (A 931) and 7 when this app asks (B 1591); same request bytes. Not pursued.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-081-2026-11-02_11-57-44_12-27-29-Group_BQ/CAP-081-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-081-2026-11-02_11-57-44_12-27-29-Group_BQ/CAP-081-FINDINGS
