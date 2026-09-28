# Findings: `CAP-056` (Group AR — the press-and-hold ANC-mode checklist per bud, the `qht` bit order, and in-ear detection off; `HOLD-005`, `INEAR-001`…`INEAR-004`)

Evidence-based extraction from `CAP-056-btsnoop_hci.log` + `CAP-056-recording.mp4` (with its audio track), staged here for promotion into
`PROTOCOL.md` per `PROJECT_RULES.md` §2. Every claim carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture (frame number / film time), or code/spec text quoted with its location.
- 🟡 **HYPOTHESIS** — a reading of the evidence, not yet independently confirmed; the settling experiment is named.
- ⚪ **ASSUMPTION** — carried over, not tested here.
- 🔴 **OPEN QUESTION** — not resolved by this capture.

**Capture ID:** `CAP-056` · **Date:** 2026-09-28 · **Firmware:** `release_5.203` (🟢, `GetSoftwareInfo` 1447 and every later announcement) ·
**Phone:** Pixel 7a (⚪ — not shown on screen), official Pixel Buds app (version not on film), Android version not on film · **Log:** 5,376 packets,
17:30:42.812–17:39:02.441, raw path, untruncated · **Video:** 306.04 s, overlay 17:30:53–17:35:58, AAC audio · **Buds:** `04:00:6e:cf:6e:07`, classic
handle `0x0005` (the only link to the Buds; the LE handle `0x0003` is an unrelated Heart-Rate device) · **Timeline:** `CAP-056-EVENT-NOTES.md`.

Commands used throughout (rule 4a):

```
python3 scripts/pwrpc_decode.py CAP-056-btsnoop_hci.log                                   # every DLCI 0x02 pw_rpc packet
tshark -r CAP-056-btsnoop_hci.log -Y "frame.number in {…}" -T fields -e frame.number -e frame.p2p_dir -e data.data   # raw bytes
tshark -r CAP-056-btsnoop_hci.log -Y "btrfcomm.dlci==0x04 and btrfcomm.len>0" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data
tshark -r CAP-056-btsnoop_hci.log -Y "btrfcomm.dlci==8 and btrfcomm.len>0" -T fields -E separator='|' -e frame.number -e frame.time_epoch \
       -e bthci_acl.chandle -e btrfcomm.dlci -e frame.p2p_dir -e data.data > d08.tsv && python3 scripts/decode_dlci08_tlv.py d08.tsv
tshark -r CAP-056-btsnoop_hci.log -Y "btrfcomm and bthci_acl.chandle==0x0005 and btrfcomm.frame_type!=0xef and btrfcomm.frame_type!=0xff" \
       -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type                  # SABM/UA/DISC/DM
tshark -r CAP-056-btsnoop_hci.log -Y "btavctp or btavdtp.signal_id" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e _ws.col.Info
```

`frame.p2p_dir` 0 = sent by the phone, 1 = received from the Buds.

---

## 0. Capture integrity and clock (🟢 FACT)

- `capinfos`: 5,376 packets, 499.628954 s, "Packet size limit: (not set)"; `cap_len ≠ len`: 0 of 5,376. SHA-256 log `cfbdc45b…647d1e`, mp4 `135bc165…31db9a`.
- **Coverage:** the film covers log 17:30:53–17:35:58. The log's other 3 min (17:35:58–17:39:02) carry only the Buds' `0x73d5d805`/`0x4d93b6e2`
  stream (e.g. 4486, 5070, 5316) and phone `+CIEV: 4,3/4,4` on HFP (4925, 4946) — no setting, wear or channel event.
- **Clock:** the overlay's seconds tick measured by a pixel-difference on the overlay digits (frame-exact decode): video t = 37.706 s → 17:31:31 and
  t = 280.612 s → 17:35:34 (same phase within 2 frames). The first checkbox (write 1689 at 17:31:30.889) starts changing at overlay 17:31:31.10–.13;
  the last anchor, the "In-ear detection" switch (write 4048 at 17:35:33.419), starts moving at overlay 17:35:33.64. Both lags are 0.21–0.24 s ⇒ the
  overlay clock equals the phone clock within ≈ 0.3 s with no drift over the film, and the official app redraws a control ≈ 0.2 s after it sends the
  write. (A lag this size fits the UI updating after the click handler; it is not claimed as an app property.)

## 1. `HOLD-005` / `0051` F-6 — the `qht` bit order (🟢 FACT for this capture)

All 16 `WriteSetting 4:{12:{…}}` of the session, each one tap on film, each answered by an empty `RESPONSE` status OK and mirrored on
`SubscribeToSettingsChanges`:

| Tap (film) | Screen | Checkbox | Write (frame, time) | Wire value `{1 2 3 4}` | Push / OK |
|---|---|---|---|---|---|
| 17:31:30 | Customize left | untick Noise cancellation | 1689, 17:31:30.889 | `0 1 1 1` | 1696 / 1697 |
| 17:31:36 | left | re-tick NC | 1725 | `1 1 1 1` | 1727 / 1729 |
| 17:31:42 | left | untick Off | 1786 | `1 0 1 1` | 1788 / 1789 |
| 17:31:47 | left | re-tick Off | 1802 | `1 1 1 1` | 1804 / 1805 |
| **17:31:53** | left | **untick Adaptive** | **1815**, 17:31:54.306 | **`1 1 1 0`** | 1817 / 1818 |
| 17:31:59 | left | re-tick Adaptive | 1830 | `1 1 1 1` | 1832 / 1833 |
| **17:32:05** | left | **untick Transparency** | **1843**, 17:32:05.982 | **`1 1 0 1`** | 1845 / 1846 |
| 17:32:11 | left | re-tick Transparency | 1863 | `1 1 1 1` | 1865 / 1866 |
| 17:32:21 | Customize right | untick NC | 1891 | `0 1 1 1` | 1895 / 1896 |
| 17:32:28 | right | re-tick NC | 1908 | `1 1 1 1` | 1910 / 1913 |
| 17:32:34 | right | untick Off | 1928 | `1 0 1 1` | 1930 / 1931 |
| 17:32:40 | right | re-tick Off | 1944 | `1 1 1 1` | 1946 / 1947 |
| **17:32:45** | right | **untick Adaptive** | **1959** | **`1 1 1 0`** | 1963 / 1964 |
| 17:32:51 | right | re-tick Adaptive | 1975 | `1 1 1 1` | 1977 / 1978 |
| **17:32:57** | right | **untick Transparency** | **1991** | **`1 1 0 1`** | 1994 / 1995 |
| 17:33:03 | right | re-tick Transparency | 2006 | `1 1 1 1` | 2008 / 2009 |

Raw bytes (phone → Buds, channel 19, request address `00 3b`):

```
1689  7e003b0310131dea71de7d5e251d9a8c9e2a0c220a62080800100118012001f81ce2ad7e   4:{12:{1:0 2:1 3:1 4:1}}
1815  7e003b0310131dea71de7d5e251d9a8c9e2a0c220a62080801100118012000da27927c7e   4:{12:{1:1 2:1 3:1 4:0}}   Adaptive unticked
1843  7e003b0310131dea71de7d5e251d9a8c9e2a0c220a620808011001180020017b7d5d570a7e 4:{12:{1:1 2:1 3:0 4:1}}   Transparency unticked
1697  7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e                          RESPONSE (type 1), ch 19, WriteSetting, empty payload
```

(`2a 0c` = RpcPacket payload, `22 0a` = `WriteSetting` field 4, `62 08` = `qhr` field 12 length 8, then `08 v1 10 v2 18 v3 20 v4` = `qht` fields 1–4.)

- 🟢 **Unticking "Adaptive" clears boolean 4; unticking "Transparency" clears boolean 3** — on both screens (1815/1959, 1843/1991). With
  "Noise cancellation" → boolean 1 (1689/1891) and "Off" → boolean 2 (1786/1928) the mapping is **1 = Noise cancellation, 2 = Off, 3 = Transparency,
  4 = Adaptive**. The labels are read from full-resolution frames (e.g. t = 40 s, 17:31:33: rows "Noise cancellation / Off / Adaptive /
  Transparency", title "Customize left", tab "Active noise control").
- 🟢 This is the app code's mapping (re-checked this session in `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/`):
  `qht.java:31` `RawMessageInfo` `"\u0001\u0004…\u0001ဇ\u0000\u0002ဇ\u0001\u0003ဇ\u0002\u0004ဇ\u0003"` with objects `{"b","c","d","e","f"}` (b = has-bits,
  fields 1–4 → `c,d,e,f`); `hgj.java:216–227` sets `c` for `aJ()` (`keyOn`, `hgj.java:84–91`, from `anc_preference_key_on`, `:115–117`),
  `:245–256` `d` for `aI()` (`keyOff`, `anc_preference_key_off`, `:121–123`), `:274–285` `e` for `aK()` (`keyTransparency`, `anc_preference_key_txp`,
  `:124–126`), `:303–314` `f` for `aH()` (`keyAdaptive`, `anc_preference_key_adaptive`, `:118–120`).
- 🟢 **The on-screen-order reading of `PROTOCOL.md` §4.5.3 (3 = Adaptive, 4 = Transparency) is refuted** by 1815/1843/1959/1991.
- 🟢 The session's writes are byte-identical to `CAP-021`'s for the same state: `CAP-056` 1689 = `CAP-021` 5237, 1725 = 5247, 1786 = 5255 (all three on
  channel 19, address `00 3b`) — `tshark -r CAP-021-btsnoop_hci.log -Y "frame.number in {5237,5247,5255}" -T fields -e frame.number -e data.data`.
- 🟢 Connect-time read 1531: `4:{12:{1:1 2:1 3:1 4:1}}` — all four selected, matching the film at 17:31:27.
- 🟢 (code) the official app refuses to go below two selected modes: `hgj.java:165–168` logs `"ANC gesture loop requires at least 2 selections.
  Rechecking toggle"` and re-checks the box. Not exercised on film (never fewer than three were selected).
- 🟢 **Name reconciliation (ADR-019 dimension (b)).** The screen that writes field 12 is the per-bud "Active noise control" checklist whose footnote
  reads "Press and hold to cycle between the selected active noise control modes" (film, t = 40 s); the code's read site logs `"Log ANC gesture loop to
  Clearcut"` (`fxb.java` case 12) and the write site logs `"Clicked preference to include %s in ANC gesture loop: %s"` (`hgj.java:164`). Film + wire +
  code now tie "ANC gesture loop" to this checklist. (Promotion = maintainer decision, §9.)

## 2. One list or two? (lead 3)

- 🟢 **The write carries no Left/Right field**: the "Customize right" writes are byte-identical to the "Customize left" writes for the same
  checkbox state (1891 = 1689, 1959 = 1815, 1991 = 1843 — same bytes including the CRC). Left and Right are distinguishable **only by the film**.
- 🟢 The read (1531) returns **one** `qht` message.
- 🟢 (code) one preference screen, one `qht` builder; the fragment's `argument_is_left_bud` flag (`hgj.java:214`, set by `hgr.java:49`) is passed only
  to `hgi`, which uses it to pick the analytics page id (`hgi.java:50`: `true != this.d ? 23 : 22`) for the Clearcut log `"Log settings update event…
  CATEGORY_GESTURE_CUSTOMIZATION"` (`hgi.java:53`) — not to the Buds.
- 🔴 **Whether the Buds keep one list or the app shows one list under two titles is not settled on film:** step A2 (open "Customize right" while
  Adaptive is unticked on the left) was skipped; every time a screen was opened all four boxes were ticked on both sides. 🟡 HYPOTHESIS (strong: wire +
  code): one shared list. Settling experiment: untick one mode on "Customize left", open "Customize right" on film; optionally long-press each bud and
  watch the `Notify` modes (§5).

## 3. `INEAR-001` — the "In-ear detection" switch = `qhr` field 2 (🟢 FACT for this capture)

| Tap (film) | Switch on film | Write (frame, time, channel) | Value | Push / OK | Buds SASS `07 11` flags (frame) | DLCI 0x08 `04 05` |
|---|---|---|---|---|---|---|
| (before the film) | OFF at 17:31:13 and 17:33:32 | read `4:{2:0}` 1502 (17:31:16.524) | 0 | — | `b0 00` 1053 (17:31:09.768) | `08 01` 1133 |
| 17:33:34 | → ON 17:33:35 | 2173, 17:33:35.629, ch 19 | 1 | 2175 / 2179 | `b8 00` 2176 (+18 ms) | `08 06` 2177 |
| 17:34:24 | → OFF 17:34:26 | 2849, 17:34:25.565, ch 21 | 0 | 2851 / 2855 | `b0 00` 2852 (+42 ms) | `08 01` 2854 |
| 17:35:17 | → ON 17:35:19 | 3627, 17:35:18.806, ch 21 | 1 | 3632 / 3632 | `b8 00` 3630 (+57 ms) | `08 06` 3631 |
| 17:35:32 | → OFF 17:35:33.6 | 4048, 17:35:33.419, ch 19 | 0 | 4051 / 4056 | `b0 00` 4052 (+71 ms) | `08 01` 4054 |
| 17:35:49 | → ON ≈ 17:35:52 | 4344 (17:35:52.190) + 4361, ch 21 | 1 | 4362, 4367 / 4362, 4369 | `b8 00` 4425 (after the re-open's `Get`) | `08 06` 4357 |

Raw bytes:

```
2173  7e003b0310131dea71de7d5e251d9a8c9e2a0422021001067a3a8b7e     4:{2:1}  ch 19  = CAP-024 1912 byte for byte
4048  7e003b0310131dea71de7d5e251d9a8c9e2a0422021000904a3dfc7e     4:{2:0}  ch 19  = CAP-024 1850 byte for byte
2849  7e004b0310151dea71de7d5e251d9a8c9e2a0422021000a0a6cb947e     4:{2:0}  ch 21  (address 00 4b, channel 0x15, own CRC)
3627  7e004b0310151dea71de7d5e251d9a8c9e2a04220210013696cce37e     4:{2:1}  ch 21
2176  071100040102b800   (DLCI 0x04, Buds)  SASS "Notify capability", version 0x0102, flags 0xb800
2852  071100040102b000   (DLCI 0x04, Buds)  flags 0xb000
```

- 🟢 Five filmed taps on the switch labelled **"In-ear detection — Earbuds automatically play audio when in and pause audio when out"** (label read
  at t = 283 s) produced five `WriteSetting 4:{2:…}` with the value of the new switch position, each acknowledged OK. Together with `CAP-024`
  1850/1912 (byte-identical) the UI switch writes field 2 in both directions.
- 🟢 **Independent confirmation from the Buds themselves:** after every field-2 write the Buds send the Fast Pair Audio-switch (SASS)
  "Notify capability" `07 11` with flags `0xb8`/`0xb0`, unprompted (no phone `07 10` before 2176, 2852, 3630, 4052). The official SASS page
  (developers.google.com/nearby/fast-pair/specifications/extensions/sass, fetched 2026-09-28) defines the flags MSB-first: *"Bit 3: 1, if this
  device supports on-head detection (even if on-head detection is turned off now)"* and *"Bit 4: 1, if on-head detection is turned on; 0, otherwise
  (does not support on-head detection or on-head detection is disabled)"*. `0xb8` = `1011 1000` → bit 4 = 1; `0xb0` = `1011 0000` → bit 4 = 0 — the
  bit follows field 2 in 5 of 5 writes and at connect (1053 `b0` with read 1502 `2:0`). (The page says the Provider sends it "upon receiving get
  capability"; here it is also sent on change — observed, not spec.)
- 🟢 Google's help page "Manage in-ear detection" (support.google.com/googlepixelbuds/answer/9642984, fetched 2026-09-28): *"If your Pixel Buds detect
  that you've taken them out, your current audio selection will automatically pause. When you put them back in, the audio will automatically
  resume."* and, for Pro 2, "More settings" → toggle "In-ear detection".
- 🟢 The last write (4344) went out 3 s after the tap because the Buds had closed DLCI 0x02 at 17:35:48.467 (4168) and the app re-opened it at
  17:35:51.800 (4304). The phone then sent `CLIENT_ERROR` status `CANCELLED` (4360, `7e004b03080410151dea71de7d5e251d9a8c9e30012100bc927e`), the same
  write again (4361), received two OK responses (4362, 4369) and sent `CLIENT_ERROR` `FAILED_PRECONDITION` (4370, `…3009…`). The setting was applied
  (push 4362, SASS `b8` 4425). The client errors are the official app's own; not interpreted further.

## 4. The maintainer's observation — in-ear detection on vs off (§1.3 of the prompt)

**Observation:** "switching in-ear detection on or off worked; with it on, the music stopped when a bud was taken out and resumed when it was put
back (implied: with it off it did not stop)." **Verdict: confirmed, with one correction of mechanism.**

| # | Film (phone time) | OHD | Wear change | DLCI 0x08 `04 05` / `04 16` | `Notify` (DLCI 0x04) | field-13 push | Buds `DISC` DLCI 0x02+0x04 | Phone PlaybackStatusChanged |
|---|---|---|---|---|---|---|---|---|
| 1 | 17:33:44–45 (head frame-left) | on | one bud out | `04` 2253 (17:33:46.689) | `e8 e8 40` 2257 | `13:4` 2259 | no | **Paused** 2268 (17:33:46.818), Suspend 2294 |
| 2 | 17:33:50–53 | on | back in | `06` 2318 | `e8 e8 08` 2322 | `13:2` 2324 | no | **Playing** 2326 (17:33:54.248) |
| 3 | 17:34:07–08 (head frame-right) | on | the other bud out | `05` 2453 (17:34:09.187) | `e8 e8 40` 2458 | — (channel closing) | **yes** 2459/2462 (17:34:09.212) | **Paused** 2482 (17:34:09.353) |
| 4 | ≈ 17:34:16–17 | on | back in | `06` 2760 | `e8 e8 08` 2764 | `13:2` 2765 | no | **Playing** 2772 (17:34:17.800) |
| 5 | 17:34:30–31 (frame-left, bud in the fingers) | **off** | one bud out | `01` 2920 (unchanged) | none | none | **yes** 2923/2924 (17:34:31.965) | none |
| 6 | 17:34:37–39 | off | back in | `01` 3187 | none | none | no | none |
| 7 | ≈ 17:34:49 (frame-right) | off | the other bud out | `01` 3259 | none | none | **yes** 3260/3263 (17:34:50.902) | none |
| 8 | 17:34:56–57 | off | back in | `01` 3518 | none | none | no | none |
| 9 | 17:35:21–22 (both in the hands 17:35:22–28) | on | both out | `04` 3667 → `03` 3687; `04 16` `02` 3689 | `e8 e8 40` 3670; after re-open `01 e8 00 20` 3940 | read `13:0` 3870 | **yes** 3672/3673 (17:35:22.002) | **Paused** 3705 (17:35:22.166) |
| 10 | 17:35:29–31 (off film) | on | both back in | `04` 3974 → `06` 3995; `04 16` `01` 3985 | `e8 e8 40` 3983 → `e8 e8 08` 4001 | `13:4`, `13:2` 4025/4028 | no | **Playing** 3991 (17:35:31.465) |
| 11 | 17:35:37–38 (both in the hands 17:35:38–44) | off | both out | `01` 4099/4102; `04 16` `02` 4103 | none | none | no | none |
| 12 | ≈ 17:35:45–48 (off film) | off | both back in | `01` 4166; `04 16` `01` 4167 | none | none | **yes** 4168/4170 (17:35:48.467) | none |

Raw examples: 2253 `040500020804`, 2318 `040500020806`, 2453 `040500020805`, 3687 `040500020803`, 2854 `040500020801`, 3689 `041600020802`,
4167 `041600020801` (DLCI 0x08, Buds); 2257 `0813000401e8e840`, 2322 `0813000401e8e808`, 3940 `0813000401e80020` (DLCI 0x04, Buds);
2259 `7e80a3032a0422026804080710131dea71de7d5e25f5ad21289ac572667e` (`SubscribeToSettingsChanges 4:{13:4}`).

- 🟢 **With in-ear detection on, the phone paused at every removal and resumed at every re-insertion (3 of 3 each); with it off, the phone's
  playback status did not change at any of the 6 wear changes (3 clusters: rows 5–6, 7–8, 11–12).** Music was playing throughout (Playing 2029 at 17:33:13.881, the
  podcast started on film). What the ears heard is the maintainer's observation; the wire shows the phone's own player state.
- 🟢 **Correction of mechanism: no AVRCP pass-through from the Buds in this capture** (zero `PASS THROUGH` frames in either direction; the only AVCTP
  traffic is `RegisterNotification`, 1222–4027). The pause/resume is the **phone's** `PlaybackStatusChanged` (sent by the phone as AVRCP target, e.g. 2268
  "Changed — Paused") followed by AVDTP Suspend/Start. `CAP-063` (Pixel 9a/GrapheneOS, GSND closed) instead had the Buds send AVRCP PAUSE pass-through
  (4329, 6301) for the same kind of event. 🟡 HYPOTHESIS: which route the Buds take depends on whether a GSND client (the Google app's assistant service,
  `PROTOCOL.md` §6) holds DLCI 0x08 — here the phone held DLCI 0x08/0x0a throughout (re-opened 1 s after every close), and each pause follows a DLCI 0x08
  `04 05` push by 100–170 ms. Settling experiment: same removal with the Google app disabled on the Pixel 7a (DLCI 0x08 closed) — AVRCP PAUSE expected
  instead.
- 🟢 **The Buds still close DLCI 0x02 (and 0x04) on wear changes with in-ear detection off**: rows 5, 7, 12 (all 3 off clusters). With it on:
  rows 3 and 9 (2 of 3 clusters). No row shows a clean rule (row 1 = one bud out, no `DISC`; row 5 = the same side, OHD off, `DISC`) — 🔴 what triggers the
  `DISC`. The official app re-opened DLCI 0x02 3.3–3.5 s after each `DISC` (`SABM` 2629, 3045, 3385, 3846, 4304).
- 🟢 **With in-ear detection off the Buds report no ANC change and no field-13 change on wear changes** (rows 5–8, 11–12: no `Notify`, no push,
  although DLCI 0x04 was open, e.g. 17:35:27.5–17:35:48.5). With it on, removing a bud makes the Buds report mode `0x40` (Adaptive) and field 13 = 4, and
  re-insertion restores `0x08`/`13:2` (rows 1–4, 9–10). 🟡 HYPOTHESIS: with in-ear detection on, a single worn bud switches its noise control (the
  reported `0x40`), and with none worn the Buds report `13:0` / `Notify … 00 20`.
- 🟢 **Settable `00` with no bud worn, in-ear detection on:** 3940 (17:35:28.123) `01 e8 00 20` while both buds were in the hands (film 17:35:22–28) —
  one more sample for ADR-024's 🟡 "`0x00` = no bud worn", no counter-example. Every other `Notify` of the session reads `e8` with at least one bud
  worn. 🔴 **Settable with in-ear detection off and no bud worn — no sample** (no `Get` was sent in rows 11/12's window; the re-open `Get`s 3137
  (17:34:37.601) and 3476 (17:34:56.721) fall during a re-insertion and read `e8 08`).
- 🟡 **DLCI 0x08 `04 05` (the `CAP-050` §4 "fluctuates near dock changes" code) tracks wear while in-ear detection is on:** `06` both worn, `04` / `05`
  one bud out (a different value for each side of the head — rows 1 vs 3, 9 first step), `03` both out, and it sits at `01` whenever in-ear detection is
  off (2854, 2920, 3187, 3259, 3518, 4054, 4099, 4166) and at connect with it off (1133). `01 09` `0a 03 0b 06 13|12` accompanies the changes (2254, 2320,
  2455, 2761, 3668, 3981). `CAP-050` saw `03` with the buds **docked** and `04` with both on the table (its §4), which does not fit a simple
  "worn count" reading in both captures — so this stays 🟡, not promoted. DLCI 0x08 is GSND, not an app channel (ADR-043 withdrew the app's claim).
- 🟡 `04 16` flips `01` → `02` when both buds leave the ears and back on re-insertion, **independently of in-ear detection** (3689/3985 on, 4103/4167
  off); `CAP-050` §4 had `02` docked and `01` with both on the table — 🔴 meaning.
- 🔴 Which physical ear (Left/Right) each removal was: the overhead camera shows an ear from frame-left (rows 1, 5) and from frame-right (rows 3, 7),
  consistent with `04 05` = `04` vs `05`, but left/right cannot be decided from the images. **AY-3 (one bud worn, one on the table) is therefore not
  settled by this capture** beyond row 1/3 (one bud out, one worn, Settable not sampled: no `Get` in those windows).
- 🟢 Runtime-info stream (`SubscribeRuntimeInfo`, 10 packets: 1460, 2402, 2653, 2822, 3069, 3204, 3407, 3873, 4028, 4340): always
  `3:0 6:{2:{1:100 2:1} 3:{1:100 2:1}} 7:{1:0 2:0 3:0}` — fields 3 and 7.3 stay 0 through every wear change (as in `CAP-063`); no Case entry (no bud
  charging).

## 5. Touch-and-hold (lead 6)

- 🟢 No long press is identifiable on film (the buds are in the ears off film except in rows 9/11, where they are held, not pressed). Every `Notify`
  mode change coincides with a wear change (§4), none with a hold. The bit order is therefore not cross-checked by the hold cycle. 🔴 Settling
  experiment: untick Adaptive, long-press a bud three times on film → the `Notify` modes should cycle through NC/Off/Transparency only.

## 6. What the official app sends besides (lead 7) — listed, not interpreted

- 🟢 Connect burst on DLCI 0x02 (1444–1619): `GetSoftwareInfo` push (1447), `SubscribeRuntimeInfo` (1448), `GetHardwareInfo` (1449 → 1462),
  `SetWallclock` (1464), `SubscribeToSettingsChanges` (1451), `ReadSetting` for fields 1–5, 7, 11–13, 15–19, 21–32, 34–36 plus two answered with an empty `4:` (status `UNKNOWN` for
  1, 25, 34–36 and the two empty ones, 1485–1619), and requests to unnamed services `0x73d5d805` (method `0x73b772ce`, `0x4d93b6e2` stream), `0xaf3a7737`,
  `0x755ffe65` (`0x67b5452c` → `NOT_FOUND` every session, e.g. 1493), `0x1c256c5d`, `0x4e4abee7` (`0xd978edbe`, cancelled by the phone 2–3 s after each
  re-open, e.g. 2750). Unnamed-service requests go out on **both** channels 19 (`00 3b`) and 21 (`00 4b`) in one RFCOMM payload (e.g. 1444).
- 🟢 The announced channel alternates per re-open (19, 21, 19, 21, 19, 21: 1447, 2640, 3055, 3395, 3858, 4317) and every `Maestro` request follows it
  (ADR-034's rule held 6/6).
- 🟢 Each re-open repeats `GetSoftwareInfo` → `SubscribeRuntimeInfo` → `ReadSetting 4:13` only (e.g. 2640–2653) — not the full sweep.
- 🟢 DLCI 0x04 was opened by the phone (`SABM` 991, 2693, 3115, 3453, 3910, 4396 — ⚪ Play services, ADR-025/032) with `Get ANC` `08 11 00 00` on each
  of the 6 payload-carrying opens (1029, 2710, 3133, 3472, 3926, 4419 — ADR-022 reproduced), the Device Information burst (`03 01` Model ID `da 2d b1`,
  `03 09` "Revision 6", session nonce `03 0a`, e.g. 1023), battery `03 03 00 03 64 64 ff` (100/100, e.g. 1034), SASS `07 10/11/21/40/41/42/34`
  (`07 41` payload starts with ASCII "in-use", e.g. 1058). `07 34` pushes are encrypted (not decoded; ADR-008 scope).
- 🟢 HFP (DLCI 0x09, opened by the Buds 1259): SLC 1287–1404, `AT+BIEV=2,100` ×5 (1369, 2404, 2823, 3206, 4013), phone `+CIEV: 4,…` (signal) ×11. No
  `AT+BVRA`.

## 7. Anything new (lead 8)

- 🟢 No ACL or LE disconnection (no `Disconnection Complete` in the whole log); no role change; no NAK (`ff 02`) on DLCI 0x04; pw_rpc statuses other
  than OK: the `ReadSetting` `UNKNOWN`s and `NOT_FOUND` above and the phone's `CLIENT_ERROR`s (4360, 4370; `0x4e4abee7` cancels).
- 🟢 First ACL attempt paged out (`Connection Complete` status `0x04` Page Timeout, 793, 17:31:02.989); the retry succeeded (835).
- 🔴 A second host (Pixel 9a) cannot be excluded from the 7a's log (§ EVENT-NOTES Log Metadata).

## 8. What the app would need (proposals only — nothing here is decided or built)

**Field 12 (wish 11, `ai-sessions/0053` W-11 — choose which ANC modes the press-and-hold cycles through).** The capture now supplies: the bit order
(§1), the name reconciliation (§1), real fixtures (1689/1891, 1725, 1786, 1815, 1843, 1531 read; channel-21 form not captured for field 12 — the app would
use the announced channel's address, ADR-034), the official app's "at least 2" rule (code). Still missing: (a) the maintainer's approval of the
`PROTOCOL.md` promotions (§9 P-1/P-2); (b) the one-list question (§2, 🟡) — an app with one list for both buds would match wire and code; (c) a new ADR
(draft D-A below); (d) UI: four checkboxes under "Press and hold", greyed when a bud's action is "Digital assistant" (⚪ — the official app's rule is not
checked in code), refusing to go below two; (e) hardware re-test: untick Adaptive in OpenControl → `WriteSetting 4:{12:{1:1 2:1 3:1 4:0}}` → empty
`RESPONSE` OK, then a long press cycles NC → Off → Transparency only (`Notify` `08`, `20`, `80`).

**Field 2 (W-12b — make in-ear detection writable).** Supplied: the label on film 5/5 (§3), the Buds' own SASS bit (spec-quoted), Google's help page,
real fixtures on both channels (2173/4048 ch 19 = `CAP-024` 1912/1850; 2849/3627 ch 21), the read (1502). Behaviour with it off (§4): **the phone no
longer pauses; the Buds still close DLCI 0x02 on some wear changes** (so ADR-044's automatic re-open still has work to do — 3 `DISC`s with it off); **the
Buds stop reporting ANC changes on wear changes** (no `Notify`); Settable with it off and not worn is 🔴 — so OpenControl's "ANC only while worn" rule
(`ai-sessions/0054` I-1, which relies on Settable `00`) is **untested with in-ear detection off**. Still missing: the promotion (§9 P-4), an ADR (draft
D-B), the UI (a switch on "Controls", with a one-line note that with it off the phone does not pause and the ANC "while worn" check may not apply), and a
re-test: OpenControl writes `4:{2:0}` → OK + SASS `b0`; one bud out → no pause; then `4:{2:1}` → OK + SASS `b8`.

**W-12a ("worn").** No new app-readable signal: the wear-tracking values seen here are on DLCI 0x08 (GSND, not the app's channel) and in the Settable
byte (only with in-ear detection on, per this capture). Stays **C** (evidence first: AY-3 with the ears identifiable, plus the same with in-ear detection off).

Draft **D-A** (ADR template; **no number**; not registered):

```
## ADR-XXX — DLCI 0x02: ReadSetting and WriteSetting unblocked for qhr field 12 (the press-and-hold ANC-mode list)
- Date: (on approval)
- Status: Proposed
- Context: ADR-036/045 kept field 12 read- and write-gated because its bit order was disputed (PROTOCOL.md §4.5.3 2026-09-26 Update). CAP-056 settles
  it on film and wire (1 = Noise cancellation, 2 = Off, 3 = Transparency, 4 = Adaptive; frames 1815/1843/1959/1991), matching the app code
  (qht.java:31, hgj.java:216–331); 16/16 official writes acknowledged OK.
- Options considered: (a) keep gated; (b) read only; (c) read + write, one shared list — chosen; (d) two lists — rejected: the wire has no side field.
- Decision: ReadSetting 4:12 and WriteSetting 4:{12:{1:b 2:b 3:b 4:b}} (four booleans, all four always sent, byte-identical to CAP-056 1689/1815/1843
  for channel 19; the announced channel and its ADR-034 address otherwise), through the Safe-Mode gate (ADR-042), one write per tap, applied only on
  the empty RESPONSE status OK; never fewer than two booleans set (the official app's rule, hgj.java:165–168). Shown once for both buds.
- Consequences: settings codec gains field 12; READABLE_FIELDS gains 12; a "Modes for press and hold" list on Controls; hardware re-test as in
  CAP-056-FINDINGS.md §8. Whether the Buds keep one list is 🟡 (A2 not run) — if a re-test shows per-bud lists, this ADR is superseded.
```

Draft **D-B** (updates `ai-sessions/0053` §6's draft; **no number**; not registered):

```
## ADR-XXX — DLCI 0x02: WriteSetting unblocked for qhr field 2 (In-ear detection)
- Date: (on approval)
- Status: Proposed
- Context: field 2 is read at Connect (ADR-036) but not writable. CAP-056: the switch "In-ear detection" writes 4:{2:0|1} (5/5 on film, 2173/2849/
  3627/4048/4344), acknowledged OK, and the Buds' SASS capability bit 4 ("on-head detection is turned on", Fast Pair SASS spec) follows it 5/5.
  With it off the phone does not pause on removal, the Buds still close DLCI 0x02 on some wear changes, and report no ANC change on wear changes.
- Options considered: (a) keep read-only; (b) writable — chosen.
- Decision: WriteSetting 4:{2:v}, v ∈ {0,1}, byte-identical to CAP-056 2173/4048 (ch 19) or 3627/2849 (ch 21) for the announced channel (ADR-034),
  Safe-Mode gate (ADR-042), one write per tap, applied only on the empty RESPONSE OK; the current value read at Connect and shown with its time.
- Consequences: a switch on Controls with the note "With in-ear detection off, audio does not pause when you take a bud out, and the 'only while worn'
  ANC check may not apply"; ADR-044 re-open unchanged (the Buds still DISC with it off, CAP-056 2923/3260/4168); the I-1 "while worn" rule is untested
  with it off — hardware re-test listed in CAP-056-FINDINGS.md §8.
```

## 9. Proposed `PROTOCOL.md` changes (awaiting maintainer sign-off, `AGENTS.md` §6 — **not applied by this file**)

- **P-1 (§4.5.3, promotion):** "`qht` bit order 🟢 FACT: 1 = Noise cancellation, 2 = Off, 3 = Transparency, 4 = Adaptive — `CAP-056` 1815/1959
  (Adaptive → 4) and 1843/1991 (Transparency → 3) on film, matching `qht.java:31`/`hgj.java:216–331`; the on-screen-order reading is refuted. Resolves the
  2026-09-26 🔴."
- **P-2 (§4.5.3, promotion):** "the 'ANC-mode rotation checklist' ("Press and hold to cycle between the selected active noise control modes") is the
  code's 'ANC gesture loop' (`qhr` field 12) — 🟢 FACT (film + wire + `hgj.java:164`/`fxb.java` case 12)."
- **P-3 (§4.5.3):** "the write carries no Left/Right field: 🟢 FACT (`CAP-056` 1891 = 1689, 1959 = 1815, 1991 = 1843); one shared list 🟡 (A2 not run)."
- **P-4 (§4.5.5, promotion):** "'In-ear detection' = `qhr` field 2 — 🟢 FACT: 5 filmed taps (`CAP-056` 2173/2849/3627/4048/4344), `CAP-024` 1850/1912
  byte-identical, and the Buds' SASS capability bit 4 ('on-head detection is turned on') following it 5/5."
- **P-5 (§4.5.5, new):** behaviour with it off (🟢 for `CAP-056`): no phone pause (0 of 6 wear changes vs 6 of 6 with it on), Buds `DISC` of DLCI 0x02 still occurs
  (2923/3260/4168), no `Notify`/field-13 change on wear; Settable with it off 🔴.
- **P-6 (§6 Behavior, new):** the pause route: phone-side `PlaybackStatusChanged` without AVRCP pass-through while GSND is held (🟡); DLCI 0x08 `04 05`
  tracks wear with OHD on (🟡, conflicts with `CAP-050` §4); `04 16` (🔴); which trigger closes DLCI 0x02 (🔴); field-13 pushes mirror the `Notify` mode
  (`13:4` ↔ `0x40`, `13:2` ↔ `0x08`, `13:0` with none worn — 🟡 with `qhs.java`'s names `ANC_STATE_ACTIVE(2)`/`ANC_STATE_ADAPTIVE(4)`/`ANC_STATE_UNKNOWN(0)`).
- **P-7 (§4.1, supporting evidence):** Settable `00` with both buds in the hands (3940), no counter-example; stays 🟡 (ADR-024 Update).

No existing 🟢 FACT or ADR is contradicted except the 🟡 on-screen-order reading of §4.5.3 (a HYPOTHESIS, refuted by P-1).

## 10. Test-ID traceability (`AGENTS.md` §13)

- `HOLD-005` — fully exercised on both screens (16 writes, §1–§2); Left/Right answer: not on the wire, one list 🟡.
- `INEAR-001` — 5 filmed toggles (§3). `INEAR-004` (removed) and `INEAR-002`/`INEAR-003` (inserted) — rows 1–12 of §4; which of Left/Right is not
  identifiable on film.
- `PAIR-003` — reconnect of the bonded Buds after Bluetooth-on (835).
- `ANC-*` (read only), `BATT-004`-style battery reads (DLCI 0x04 `03 03`, `AT+BIEV`) — incidental.

## 11. Open questions

- 🔴 One list or two (A2 not run) — §2.
- 🔴 What triggers the Buds' `DISC` of DLCI 0x02 on wear changes (it happens with in-ear detection on and off, not at every change) — §4.
- 🔴 Settable with in-ear detection off and no bud worn — §4.
- 🔴 Which ear was removed in each row; the meaning of DLCI 0x08 `04 05` / `04 16` / `01 09` — §4.
- 🔴 The pause route with GSND closed vs held (the Google-app-disabled test) — §4.
- 🔴 The official app's version and the Pixel 9a's Bluetooth state were not recorded on film.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-056-2026-09-28_17-30-53_17-35-58-Group_AR/CAP-056-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-056-2026-09-28_17-30-53_17-35-58-Group_AR/CAP-056-FINDINGS
