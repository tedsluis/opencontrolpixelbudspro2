# Findings: `CAP-061` (Group AW — first hardware run of the `ai-sessions/0045` build; six reported symptoms root-caused)

Standardized, evidence-based extraction from `CAP-061-btsnoop_hci.log`, `CAP-061-recording.mp4`, `CAP-061-debug-export.log`,
`CAP-061-OpenControl-for-Pixel-Buds-log-7a465b5a9d5e.txt` (app logcat) and `CAP-061-System-log-2c0390537392.txt`, per
`ai-sessions/0046`. The timeline these findings refer to is `CAP-061-EVENT-NOTES.md`.

- 🟢 **FACT** — directly observed in this capture (frame number, log line or film time given).
- 🟡 **HYPOTHESIS** — plausible reading, not yet independently confirmed; the settling experiment is named.
- ⚪ **ASSUMPTION** — not tested here.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-061` · **Date:** 2026-09-24, 17:24:59–17:31:14 local (film) · **Firmware:** 🟢 `release_5.203` (DLCI 0x02 frame 1508,
DLCI 0x08 frame 1311) · **Phone:** Pixel 9a, GrapheneOS, Android 17 `CP2A.260805.005`, Google Play services and the Google app present ·
**App under test:** OpenControl, `ai-sessions/0045` code (`android/` of `5ade05e` = `964fa91`) · **HCI log:** 6,369 packets, raw, 0
`cap_len≠len` · **Buds (partial):** `04:00:6e:cf:6e:07`, classic handle `0x000b`, LE handle `0x0042` (pre-filter by handle; `bluetooth.addr`
is empty with this encapsulation — `CAP-061-EVENT-NOTES.md` Log Metadata).

Commands used throughout (rule 4a): `tshark -r CAP-061-btsnoop_hci.log -Y "bthci_acl.chandle==0x000b && btrfcomm" -T fields -e frame.number
-e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type -e btrfcomm.len -e data.data`, then a per-DLCI, per-direction
`[Group:1][Code:1][Len:2 BE][Value]` reassembly that resets on every `SABM`/`DISC` of that DLCI (DLCI 0x04/0x08), and
`python3 scripts/pwrpc_decode.py CAP-061-btsnoop_hci.log` (DLCI 0x02). `p2p_dir` 0 = phone→Buds, 1 = Buds→phone.

---

## 0. Capture integrity (🟢 FACT)

`capinfos`: 6,369 packets, "Packet size limit: (not set)", 17:25:03.610669–17:34:33.853806; `cap_len≠len` 0 of 6,369. One 1 µs
out-of-order pair (frame 2334) — irrelevant. Film: 375.99 s, 29.83 fps, overlay timestamp only (all 11,209 frames scanned); film overlay
= 17:24:59.15 + t, phone clock 0.0–0.2 s behind it, measured at both ends. The logcat/system log are UTC (local − 2 h 00 min 00.000 s).

## 1. Safe Mode refused every write: the firmware announcement was never parsed (ANC, EQ and Find symptoms)

**🟢 FACT — the Buds announced `release_5.203` on every connection.** DLCI 0x02 frames 1508, 4271, 4931 (channel 21) and 5524, 5697
(channel 19): `GetSoftwareInfo` RESPONSE, `call_id 0xFFFFFFFF`. Debug-mode hex of the same packet (export line 154, 17:28:25.581):

```
7e 00 a5 03 | 2a 64 | 22 57 { 0a 1b {0a 0a <serial, 10 bytes> 12 0d "release_5.203"} 12 1b {…} 1a 1b {…} }
            |       | 29 34 29 3f c2 f6 cb d8 1a | 30 00 | 08 01 10 15 1d ea 71 de 7d 5e 25 44 fa 99 71 38 ff ff ff ff 0f | e8 a9 58 66 | 7e
```

The RpcPacket payload (field 5, `2a`, 100 bytes) holds field 4 (`22`, the three `{serial, firmware}` entries), **field 5 with tag `0x29`
= wire type 1** (8-byte fixed64, value `34 29 3f c2 f6 cb d8 1a`) and field 6 (`30 00`). Wire type 1 is "I64 — fixed64, sfixed64, double",
and "the wire type tells the parser how big the payload after it is. This allows old parsers to skip over new fields they don't understand"
(protobuf.dev/programming-guides/encoding, fetched 2026-09-24). The same shape is in **140 of 140** unsolicited announcements across the
44 captures that contain one (`pwrpc_decode.py` prints `… 5:raw 6:0` for each; `CAP-001` … `CAP-061`), and the field-5 value is identical
in `CAP-001`, `CAP-036`, `CAP-050` and `CAP-061` (2026-08-09 … 2026-09-24). Its meaning is 🔴 open.

**🟢 FACT — the app discarded it.** `Proto.fields` (`android/data/…/codec/CaseBatteryFrame.kt:104-131`) reads wire types 0 and 2 only and
returns `null` for the whole message on any other type (`else -> return null`, line 127). `SoftwareInfo.firmwareStrings`
(`codec/SoftwareInfo.kt:35-37`) calls it on the RpcPacket payload, gets `null` at the `29` tag and returns an empty list; `handleRoutedFrame`
(`BudsRepositoryImpl.kt:381-388`) then logs "Maestro channel announced by the Buds: 21" (export lines 22, 156, 253; 332, 355 for ch 19) but
never sets `_deviceInfo`; `writeGate` (`:652-668`) waits 3 s (`MAESTRO_ANNOUNCE_WAIT_MS`) for it and `SafeModeGate.evaluate` refuses with
"The Buds did not announce their firmware version on this connection." — **30** such lines in the export (first 17:27:17.974). The unit
tests fed `SoftwareInfo` a synthetic payload without fields 5/6 (`CaseBatteryCodecTest.kt:134-167`), so the defect never showed.

**🟢 FACT — nothing was sent for any write.** The whole log has **zero** `08 12` (ANC Set) and **zero** `04 01` (Ring) on DLCI 0x04 and
**zero** `WriteSetting` on DLCI 0x02. Film: "Safe Mode — nothing was sent — the Buds' firmware or model isn't one this app was verified
against (read-only)" on the ANC (17:27:18), EQ (17:27:48) and Find (17:27:57) screens and the Safe Mode card (17:27:21, "Detected: Fast
Pair model da2db1"). The Model ID input of the gate worked: `03 01 00 03 da 2d b1` arrived on every app claim (e.g. export line 54).

**Answers:** ANC "does not work", EQ "does not work", Find "does not work" and the Safe Mode message are **one defect** in the gate's
input, not in the gate. The gate behaved as designed for the input it had. A secondary effect: each refused write held its claim for the
3 s wait plus the 1.5 s linger, so DLCI 0x04 claims lasted 4.6–23.2 s (§3) and EQ taps queued 3 s apart (export lines 223–236).

## 2. Case battery: the Buds never answered the app on DLCI 0x08

**Per-open table** (all 27 `SABM`s on DLCI 0x08; opener = content: the non-app opener sends `05 0c 04 02 04 04 04 11 04 13 04 15 0e 04
09 03 03 01 …`, the app sends at most `0e 04 00 00`; app opens also match the export's "RFCOMM channel 0x08 connected" lines):

| `SABM` (time) | Opener | Phone `0e 04` | First Buds `0e 01` | End |
|---|---|---|---|---|
| 1226 (17:25:41.561) | Google app / Play services | 1283 | 1319 (Case `0x44` = 68 %, flag `10 01`) | 1621 phone `DISC` +4.69 s (the app's failed attempt 1) |
| 1658, 1693 (17:25:46.83/.93) | **app** | — | — | **Buds `DISC`** 1662, 1699 (+0.012 s, +0.144 s after the `SABM`; 1.2 ms / 4 ms after the `UA`) |
| 1790 (17:25:48.569) | Google app / PS | 1809 | 1848 (68 %) | 2116 phone +4.47 s |
| 2146, 2177 | **app** | — | — | **Buds `DISC`** 2152, 2182 (+0.010, +0.012 s) |
| 2298 (17:25:55.530) | Google app / PS | 2348 | 2365 (68 %) | 2528 phone +2.93 s |
| 2562, 2592 | **app** | — | — | **Buds `DISC`** 2565, 2595 (+0.014, +0.013 s) |
| 2643 (17:26:00.752) | Google app / PS | 2688 | 2709 (68 %) | 2916 phone +30.57 s |
| 2938, 2965 | **app** | — | — | **Buds `DISC`** 2943, 2969 (+0.014, +0.011 s) |
| 3002 (17:26:33.460) | Google app / PS | 3046 | 3062 (68 %) | 3368 phone +29.71 s |
| 3392, 3419 | **app** | — | — | **Buds `DISC`** 3395, 3425 (+0.062, +0.198 s) |
| 3456 (17:27:05.904) | Google app / PS | 3492 | 3518 (68 %, **no** flag: `08 44 18 03`) | 3626 phone +4.60 s |
| 3653, 3681 | **app** | — | — | **Buds `DISC`** 3656, 3687 (+0.064, +0.179 s) |
| 3719 (17:27:13.181) | Google app / PS | 3756 | 3782 (68 %, no flag) | 3908 phone +11.28 s |
| 4335, 4429, 4605, 4993, 5214, 5419, 5585, 5766 | **app** | 4343, 4438, 4614, 5001, 5222, 5427, 5593, 5776 | — (**zero** Buds frames) | phone release +2.06–5.15 s; 5585 ends with the ACL (5597) |

- 🟢 FACT: **7 of 7** non-app opens received the Case push after their request burst (first push 19–390 ms after `0e 04`: 168, 390, 29, 24, 19, 230, 182 ms).
- 🟢 FACT: **20 of 20** app opens received **no** Buds frame. **12** were closed by a Buds-side `DISC` 10–198 ms after the `SABM` — each
  right after the app's attempt 1 had failed and the stack had closed the other owner's 0x08 (phone `DISC` 1621, 2116, 2528, 2916, 3368,
  3626: ADR-032's mechanism) — so the request was never sent (export: "RFCOMM on-demand channel 0x08 closed (IOException …)" then
  "Case battery not read: ChannelLost", lines 30–33 … 90–93). **8** were held 2.1–5.2 s after the other owner had gone (from 17:27:24),
  sent **exactly** `0e 04 00 00` (ADR-039) and received nothing (export "Case battery not read: Timeout", lines 169, 180, 195, 266, 294,
  321, 369).
- **Therefore ADR-039's 🟡 HYPOTHESIS "sending `0e 04 00 00` is sufficient" is refuted by this capture**: 8/8 app claims that sent it
  got no answer on an open no other client held. 🟡 HYPOTHESIS for why: the Buds answer on this channel only after (or as part of) the
  other opener's burst, whose first message is `05 0c` (answered by `05 0a`); which of the burst's messages is needed is 🔴 open, and
  the burst contains frames of unknown meaning, including `03 01` (a time/zone write: `… "Europe/Amsterdam"`) and `02 0b … "500p"`
  (`PRIV-001`).
- 🟡 HYPOTHESIS (new, one correlation): **the non-app owner of DLCI 0x08/0x0a in this capture is the Google app's Assistant-on-headphones
  service, not Play services.** System log 15:27:24.371 `am_stop_idle_service … com.google.android.googlequicksearchbox/…assistant.surfaces.
  bisto.interactor.BistoRealService` ("Stopping service due to app idle"); **91 ms** later (17:27:24.462/.464) the phone sends `DISC` on
  DLCI 0x08 **and** 0x0a (3908, 3909) and neither is reopened by anyone but the app for the rest of the capture. Consistent with "GSND" =
  the two SDP service names "GSND CONTROL"/"GSND AUDIO" (`PROTOCOL.md` §2.3) and with `CAP-004`'s "DLCI 0x08 not GMS-dependent". Test: a
  capture with the Google app disabled (or its Assistant-headphones setting off) — DLCI 0x08/0x0a should then never open.
- 🟢 FACT: each app attempt 1 closed the other owner's DLCI 0x08 (6 times, above) — i.e. the app's Case claim **disrupts that service's
  channel**, which re-opened it 1.3–2.3 s later each time (1790, 2298, 2643, 3002, 3456, 3719).
- **Case value on the wire:** 68 % (`0x44`) in all 7 pushes, flag present until 17:26:57 (buds in the case), absent from 17:27:06 (case
  empty) — consistent with ADR-014's "stale without flag" caveat (🟡).

### 2a. A candidate Case source on the app's own session channel (DLCI 0x02 `SubscribeRuntimeInfo`)

🟡 **HYPOTHESIS (strong), not used by the app, proposed for maintainer review:** the official app's connect burst subscribes to
`maestro_pw.Maestro/SubscribeRuntimeInfo` (method id `0xe61e8290`, `h65599("SubscribeRuntimeInfo")`, `PROTOCOL.md` §6) with an
**empty** request payload — `CAP-036` frame 1410: `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 90 82 1e e6 66 54 bf ab 7e` — and the Buds
answer with `SERVER_STREAM` packets (e.g. `CAP-036` 1421, 2009, 2048) whose payload is `2:<epoch ms> 3:0 6:{1:{1:<n> 2:1} 2:{1:<n> 2:1|2}
3:{1:<n> 2:1|2}} 7:{1:0|1 2:0|1 3:0}`. **Entry 6.1's value equals the DLCI 0x08 Case percentage (ADR-014, 🟢) of the same capture in 13 of
13 captures that contain both:** `CAP-001` 62/62, `CAP-002` 62/62, `CAP-003` 38/38, `CAP-010` 42/42, `CAP-014` 49/49, `CAP-016` 100/100,
`CAP-032` 57/57, `CAP-036` 100/100, `CAP-037` 87/87, `CAP-038` 85/85, `CAP-041` 79/79, `CAP-044` 84/84 (later 83), `CAP-048` 95/95.
Command: `python3 scripts/pwrpc_decode.py <log> | grep SubscribeRuntimeInfo` against the first `0e 01` index-3 entry of `tshark -r <log>
-Y "btrfcomm.dlci==8 && btrfcomm.len>0"`. Entry 6.1 is **absent** in 29 other captures (e.g. `CAP-005`, `CAP-009`, `CAP-050`: only 6.2/6.3
present), so it is only reported in some states (🔴 which — plausibly when a bud is in the case). The app never sends this request
(`CAP-061`: no `SubscribeRuntimeInfo` packet); DLCI 0x02 is the app's own session channel (ADR-032), not shared with any other client in any
capture. What entries 6.2/6.3, field 2 of each entry and field 7 mean is 🔴 open.

## 3. DLCI 0x04: attribution and timing

31 `SABM`s: 5 by Play services (1050, 1933, 2216, 2760, 3111 — they send `03 08`, `07 10`, `06 01`), 26 by the app. 🟢 FACT: each of the
app's five attempt-1 failures while Play services held the channel (export lines 25, 34, 43, 52, 65) coincides with a phone `DISC` of Play
services' open (1536, 2013, 2447, 2851, 3303, 0–10 ms apart); after 3303 (17:27:01.467) Play services never re-opened DLCI 0x04 and every
later app claim opened on attempt 1. 🔴 Why Play services stopped re-claiming (as in `CAP-059`; its *Nearby devices* permission state was
not recorded). Every claim got Device Information (`03 0a` session nonce, `03 01 da 2d b1`, `03 02`, `03 09 "Revision 6"`, from 17:27:02 also
`03 0b`), `07 34`, three battery frames; the app's `08 11` got `08 13` 8–451 ms later in 18 of 18. Write-action claims were held 4.6–23.2 s
(the 3 s gate wait of §1, queued taps, then the 1.5 s linger).

## 4. The dock line: "Both earbuds seem to be in the case" while one bud was out

All 20 `Notify ANC state` frames (`08 13 00 04 01 e8 <settable> <mode>`) against the film and the battery flags (`03 03 00 03 <L> <R> ff`,
`0bSVVVVVVV`, bit 7 = charging, ADR-033):

| Frames | Time | Settable / mode | Battery (L, R) | Film |
|---|---|---|---|---|
| 1594, 2085, 2502, 2896 | 17:25:46–17:26:31 | `00` / `20` (OFF) | `e4 e4` (both charging) | both seated ✓ |
| 3197 (Play-services channel) | 17:26:45.720 | `e8` / `40` | `e4 64` (L charging, R not) | Right out since 17:26:36–37 ✓ |
| 3272, 3346, 3604, 3996, 4309 … 5392 (12) | 17:26:57–17:30:39 | `e8` / `08` | `64 64` | both out ✓ |
| **5560** | **17:30:58.806** | **`00` / `20`** | **`64 e4`** (5557, 2 ms earlier: **Left not charging**, Right charging) | **Right seated (17:30:52.4–53.0), Left in the hand beside the case** (30 fps: seated only at t ≈ 360.45 s = 17:30:59.4–59.6 phone time) ✗ |
| 5741 | 17:31:06.765 | `00` / `20` | `e4 e4` | both seated ✓ |

- 🟢 FACT: **one counter-example to the derived reading "`0x00` = both earbuds in the case"**: frame 5560 reads `0x00` 0.6–0.8 s before the
  second bud is seated, while the same claim's battery frame says that bud is not charging. The app showed "Both earbuds seem to be in the
  case — … (updated 17:30:58) — provisional" next to "Left: 100% (updated 17:30:58)" and "Right: 100% (charging)" (film 17:30:59) — the
  contradiction the maintainer reported. The app displayed what the Buds sent; the Buds' byte, read as "both docked", was wrong here.
  This is a **6th documented counter-example** (after `CAP-048` ×2, `CAP-047` ×3; `CAP-038` a 7th in the other direction), and a different
  kind: a **premature** `0x00` (one bud still out), not a stale one after a reopen. The opposite asymmetry exists in the same session: frame
  3197 reads `e8` with the Left bud seated and the Right out. 🟡 HYPOTHESIS: which bud is docked matters (Right docked alone → `00`; Left
  docked alone → `e8`) — two samples, one each; the spec meaning ("which ANC modes are currently switchable", ADR-024 Update 2026-09-24)
  allows either.
- 🟢 FACT: **the per-bud charging bit tracked every dock transition on film in this session** (5 of 5: `e4 e4` seated, `e4 64` at the
  Right removal 17:26:37.50, `64 64` at the Left removal 17:26:52.26, `64 e4` with only the Right seated, `e4 e4` both seated). 🟡
  HYPOTHESIS that "charging" implies "in the case" in general — not when the case battery is empty, and not verified at other levels than
  100 %.
- 🟢 FACT: from 17:26:38 to 17:27:00 the screen kept "Both earbuds seem to be in the case (updated 17:26:31)" while buds were out: no claim
  happened in between, so the last reading stayed, with its time. This is the ADR-032 "last known" design (no polling), not a decode error.

## 5. The ANC tile request

🟢 FACT: export line 205 "Add ANC tile request result: 1" (17:29:01.640). `StatusBarManager` reference (developer.android.com, fetched
2026-09-24): `TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED` — "Response indicating that the tile was already added and the user was not
prompted. Constant Value: 1". So the tile **is** in this phone's Quick Settings; the app only logs the result and shows nothing, which reads
as "nothing happened". The tile was not used on film (the Quick Settings rows visible at 17:25:09 and 17:28:34 do not show it — it is on a
page not shown). No `AncTileService` line in the logs.

## 6. Session and link events

- 🟢 FACT: 17:30:48.339 **Buds `DISC` on DLCI 0x02** (5456) with the ACL up, while the buds were being handled (film); app "Session lost …
  not a user disconnect" (export 323–325). Same class as `CAP-059`/`CAP-060` §1 drops 1/2.
- 🟢 FACT: ACL `Disconnection Complete` reason `0x13` at 17:30:59.968 (5597), 0.4–0.6 s after the **second** bud was seated — ADR-016
  once more; and at 17:31:09.588 (6068) when the lid was closed with both buds seated after the app's reconnect (5605–5608).
- 🟢 FACT: pairing via the app's CDM association ran **LE first**: LE connection 584, SMP Pairing Request with `Linkkey` key distribution
  (606), DHKey check (639/640), then classic Create Connection (652) — the CTKD path of ADR-030, here triggered by Android's own bonding for
  the app (no nRF Connect).
- 🟢 FACT (out of scope, ADR-008): 56 writes to the Fast Pair Key-based Pairing characteristic `0x0c04` on the LE link, each answered
  "Error 0x81" (17:25:37.93–17:25:59.93); a CCCD write to `0x0f33` (Battery Service, `CAP-034` handle map) at 17:25:38.4 with no battery
  notification afterwards.
- 🟢 FACT: `AT+BIEV=2,100` 21 times on HFP (20 on DLCI 0x0c, then 1 on DLCI 0x09 after the reconnect, frame 5996) — not consumed by the app (ADR-040).

## 7. What works (evidence as above)

- 🟢 Pairing through the app (CDM → bond → classic link), permissions, the Android-link mirror ("Connected to this phone (Android)", export
  line 16), Connect/Disconnect (5 sessions), the foreground service start/stop (system log `am_foreground_service_start/stop` at every
  Connect/Disconnect/loss).
- 🟢 Channel announcement and **EQ read** on channels 21 and 19 (the address table of ADR-034), `[-5, -1.5, 0, 0, 0]` = the film's EQ tab.
- 🟢 Left/Right battery with the charging flag on every claim, matching the film; ANC mode read (`OFF` docked, `ACTIVE` out) matching the
  film; the Model ID gate input; the "provisional" marking right after an open (film 17:25:46, 17:27:02, 17:30:58); session-loss messages.
- 🟢 The dock line when the byte and the buds agree (19 of 20 Notifies).

## 8. `ai-sessions/0045` §9 re-test verdicts

| Item | Verdict | Evidence |
|---|---|---|
| (A) Case with `0e 04` | 🔴 **refuted** — 8/8 app claims sent exactly `0e 04 00 00`, 0 answers; 12 other opens closed by the Buds before sending | §2 |
| (B) ANC answers | ⚪ **not exercised** — no `08 12` was ever sent (Safe Mode) | §1 |
| (C) Safe Mode on `release_5.203` | 🔴 **refuted** — the card appeared on the verified firmware; root cause: the announcement parser | §1 |
| (D) Dock line | 🟡 **partly** — "provisional" appears right after an open and the line changes after Refresh (17:27:02); one wrong "both in the case" from the Buds' own byte (§4) | §4 |
| (E) Not paired | ⚪ expected but not observed (the Forget-then-Connect path was not run) | EVENT-NOTES |
| (F) Taps survive the screen | ⚪ expected but not observed | EVENT-NOTES |
| (G) Notification | 🟢 service start/stop at every Connect/Disconnect/loss (system log); the text is not legible on film | §7 |
| (H) Tile / EQ audibility / Find ringing / drop hunt | tile: already added (§5); EQ audibility and Find ringing ⚪ not exercised (Safe Mode); one Buds-side `DISC` on 0x02 (§6) | §5, §6 |

## 9. Test-ID traceability (`AGENTS.md` §13.7)

`PAIR-001` 🟢, `PAIR-003` 🟢 (×5), `CASE-003` 🟢, `CASE-004` 🟢, `CASE-005` 🟢, `CASE-006` 🟢, `BATT-004` 🟢 (Left/Right; Case never
answered), `ANC-001`–`ANC-004`, `FIND-001`/`FIND-002`, `EQS-001`, `EQP-*` — **attempted in the UI, nothing on the wire** (Safe Mode), so no
protocol evidence for them from this capture.

## 10. Decisions taken on these findings (maintainer, chat 2026-09-24, `ai-sessions/0046` RESULT §5)

1. **Case battery:** the app no longer opens DLCI 0x08; it sends one `SubscribeRuntimeInfo` request per Connect and reads entry 6.1 —
   `DECISIONS.md` ADR-043 (supersedes ADR-035 items 1–2, ADR-038, ADR-039 item 1); ADR-039 Update records the refutation of §2.
2. **Promoted to 🟢 FACT:** `SubscribeRuntimeInfo` entry 6.1 field 1 = Case battery % (§2a, 13/13) — `PROTOCOL.md` §4.3 Option F.
3. **Dock line removed from the app** — `DECISIONS.md` ADR-024 Update (frame 5560, §4).
4. **Promoted to 🟢 FACT:** the announcement's structure (fields 4 / 5 fixed64 / 6, 140/140) — `PROTOCOL.md` §2.2a; the app's reader fixed (§1).
5. Recorded without a decision (🟡, `PROTOCOL.md` §4.3 Option E / §6): the Google app's Assistant-headphones service as the likely other DLCI
   0x08/0x0a owner (§2).

## 11. Open questions

- 🔴 Which part of the other opener's DLCI 0x08 burst makes the Buds answer; why the Buds `DISC` an app open right after the stack closed
  the other owner's port (12/12 here, 3/3 in `CAP-060`).
- 🔴 Why Play services stopped re-claiming DLCI 0x04 after 17:27:01 (its *Nearby devices* permission state was not recorded).
- 🔴 The announcement's field 5 (`34 29 3f c2 f6 cb d8 1a`, constant for six weeks) and field 6.
- 🔴 When `SubscribeRuntimeInfo` carries entry 6.1, and what 6.x.2 and field 7 mean.
- 🔴 The GmsCompat "Google Play-services has crashed" notification visible at 17:28:34 (no matching line in the system log window).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-061-2026-09-24_17-24-59_17-31-14-Group_AW/CAP-061-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-061-2026-09-24_17-24-59_17-31-14-Group_AW/CAP-061-FINDINGS
