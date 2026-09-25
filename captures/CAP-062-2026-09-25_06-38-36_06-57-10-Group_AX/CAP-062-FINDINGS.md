# Findings: `CAP-062` (Group AX — `APP_TESTPLAN.md` run of the `ai-sessions/0046` build; the maintainer's five observations answered)

Standardized, evidence-based extraction from `CAP-062-btsnoop_hci.log`, `CAP-062-recording.mp4`, `CAP-062-debug-export.log`,
`CAP-062-OpenControl-for-Pixel-Buds-log-091e23cb54d0.txt` (app logcat) and `CAP-062-System-log-8bfd96877cca.txt`, per `ai-sessions/0047`. The
timeline these findings refer to is `CAP-062-EVENT-NOTES.md`.

- 🟢 **FACT** — directly observed in this capture (frame number, log line or film time given).
- 🟡 **HYPOTHESIS** — plausible reading, not yet independently confirmed; the settling experiment is named.
- ⚪ **ASSUMPTION** — not tested here.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-062` · **Date:** 2026-09-25, 06:38:36–06:57:10 local (film) · **Firmware:** 🟢 `release_5.203` (DLCI 0x02 frame 2768) ·
**Phone:** Pixel 9a, GrapheneOS, Android 17 `CP2A.260805.005`, Google Play services and the Google app present · **App under test:** OpenControl,
`ai-sessions/0046` code (`7498cbc`; identified by log wording, §1) · **HCI log:** 11,456 packets, raw, 0 `cap_len≠len` · **Buds (partial):**
`04:00:6e:cf:6e:07`, classic handle `0x000b`, LE handle `0x0042` (pre-filter by handle; `bluetooth.addr` is empty with this encapsulation).

Commands used throughout (rule 4a):
`tshark -r CAP-062-btsnoop_hci.log -Y "bthci_acl.chandle==0x000b && btrfcomm" -T fields -e frame.number -e frame.time_epoch -e frame.p2p_dir
-e btrfcomm.dlci -e btrfcomm.frame_type -e btrfcomm.len -e data.data`, then a per-DLCI, per-direction `[Group:1][Code:1][Len:2 BE][Value]`
reassembly that resets on every `SABM`/`DISC` of that DLCI (DLCI 0x04/0x05 and 0x08/0x09); `python3 scripts/pwrpc_decode.py CAP-062-btsnoop_hci.log`
(DLCI 0x02/0x03); HCI events with `-Y "bthci_evt.code==0x03 || bthci_evt.code==0x04 || bthci_evt.code==0x05 || bthci_cmd.opcode==0x0405 ||
bthci_cmd.opcode==0x0406"`. `p2p_dir` 0 = phone→Buds, 1 = Buds→phone. Phone time = film overlay + 0.7 s (EVENT-NOTES, Clock offsets).

---

## 0. Capture integrity (🟢 FACT)

`capinfos`: 11,456 packets, "Packet size limit: (not set)", 06:38:41.863250–07:00:15.793623; `cap_len≠len` 0 of 11,456; one 0.28 ms
out-of-order pair (frame 4907) — irrelevant. Film 1115.19 s, 33,311 frames, overlay timestamp only; phone clock = overlay + 0.7 s at both ends.
The build is the `0046` one: "Runtime info requested (channel 19)" (export line 30) and a `SubscribeRuntimeInfo` REQUEST from the app (frame
2777) exist only from `7498cbc` on; the app never opens DLCI 0x08 (§6) and no "Safe Mode" line appears.

## 1. `ai-sessions/0046` §9 re-test verdicts

| Item | Verdict | Evidence |
|---|---|---|
| R1 Firmware / Safe Mode | 🟢 **confirmed** — "Firmware: release_5.203", no Safe Mode card, writes sent | film t=150 s; export 24–26 (announcement ch 19 decoded); `08 12` sent 16× (§2) |
| R2 ANC `Set` answered | 🟢 **confirmed** — 6 ACKs `ff 01 00 06 08 12 01 e8 e8 <mode>` + `Notify`, 10 NAKs `ff 02 00 03 02 08 12`, both shown correctly | §2 |
| R3 EQ `WriteSetting` | 🟢 **confirmed** — 7/7 answered by an empty `RESPONSE`, status OK; persisted (next `ReadSetting` returns the last write). Audibility ⚪ not established | frames 9061→9064 … 9216→9218; 9314 |
| R4 Find ring and Stop | 🟢 **confirmed** — `04 01 00 01 02/01/00` each ACKed; the ring keeps sounding after the 1.5 s release **and after Disconnect**, until Stop (audio track) | §5 |
| R5 Case from `SubscribeRuntimeInfo`, no app DLCI 0x08 | 🟢 **confirmed** — Case 60 % shown whenever a bud was docked; equal to the DLCI 0x08 Case value (`0x3c`) of the same capture; 0 app `SABM`s on DLCI 0x08 | §4, §6 |
| R6 No dock sentence | 🟢 **confirmed** — none on any frame; "(charging)" followed each bud (`e4 64`, `64 64`, `e4 e4`) | §4 |
| R7 Tile message | 🟢 **confirmed** — "The ANC tile is already in Quick Settings …" (result 1) | export 366; film t=587 |
| R8 0045 (E)/(F) | ⚪ **expected but not observed** — not run | — |

## 2. Observation 3 — "The Buds refused the command (not allowed in the current state)": the Buds refuse ANC whenever they are not worn

**Every ANC `Set` in the capture** (DLCI 0x04/0x05, app `08 12 00 14 01 e8 e8 <mode> 00×16`) with the Buds' answer and the `Notify` that follows:

| Frames (Set → answer → Notify) | Time | Mode | Answer | Settable (`Notify` byte 6) | Buds on film |
|---|---|---|---|---|---|
| 5989 → 5998 → 6000 | 06:45:20.355 | `80` | **`ff 02 00 03 02 08 12`** | `00` | on the table beside the open, empty case (full-res t=404 s) |
| 6042 → 6050 → 6053 | 06:45:27.341 | `40` | NAK `02` | `00` | on the table |
| 6099 → 6107 → 6110 | 06:45:37.149 | `08` | NAK `02` | `00` | on the table |
| 6146 → 6155 → 6158 | 06:45:41.577 | `80` | NAK `02` | `00` | on the table |
| 6241 → 6243 → 6244 | 06:45:55.680 | `08` | NAK `02` | `00` | on the table |
| 8342, 8446, 8454, 8503 → NAK each | 06:48:33.680–06:48:50.625 | `08` (tile) | NAK `02` | `00` | **in the case**, lid open (t=596–612 s) |
| 8694 → 8705 → 8706 | 06:49:17.640 | `40` (tile) | **`ff 01 00 06 08 12 01 e8 e8 40`** | `e8` | not on the table or in the case (worn — the test plan's F/G step) |
| 8747, 8799, 8853, 8918 → ACK each | 06:49:22.439–06:49:36.341 | `20`, `08`, `80`, `40` (tile) | ACK | `e8` | worn |
| 10119 → 10127 → 10131 | 06:54:37.414 | `40` | NAK `02` | `00` | on the table (t=959–961 s) |
| 10337 → 10348 → 10349 | 06:55:30.484 | `08` | ACK | `e8` | worn (picked up at ≈ 06:55:02–04) |

- 🟢 FACT: `ff 02 00 03 02 08 12` is a Fast Pair **NAK**, reason `0x02`, for Group `0x08` Code `0x12`. The acknowledgement page
  (`developers.google.com/nearby/fast-pair/specifications/extensions/acknowledgement`, fetched 2026-09-25): "For a NAK, the reason should also be
  included as the first byte of additional data." — reason `0x02` = "Not allowed due to current state". The app's text is a faithful rendering.
- 🟢 FACT: in this capture **10 of 10 NAKs** come with `Settable = 0x00` and **6 of 6 ACKs** with `Settable = 0xe8`, in the same exchange. The
  Hearable Controls page (fetched 2026-09-25): "Settable toggles: Any or all of the UI toggle bits above may also be set here, to indicate which
  are currently enabled." — the Buds say "no mode is switchable now", and then refuse a switch.
- 🟢 FACT: the Settable `0x00` state is **not limited to the case**: five of the NAKs were sent with both buds lying on the table outside the
  open, empty case (film t=402–438 s, 959 s). Across this session all 45 `Notify` frames agree: the 29 that read `00` came while the buds were on the table or in the case,
  the 16 that read `e8` only after they were taken out of view into the ears (06:49:11–06:49:36, 06:51:45, 06:55:10–06:56:43).
- 🟡 **HYPOTHESIS (strong): `Settable = 0x00` means "not worn" (no bud in an ear), not "docked".** It explains every `CAP-062` reading and the
  documented counter-examples to ADR-024's "both in the case" reading — `CAP-061` frame 5560 (`00` with the Left bud in the hand, not worn),
  `CAP-048`/`CAP-047` (`00` with the case empty) — since those buds were out of the case but not in the ears. Not FACT: the film never shows the
  ears; "worn" is inferred from the buds leaving the table/case during the test plan's in-ear steps. **Settling experiment:** one bud in an ear,
  the other on the table, filmed; then swap; read `08 11` → `08 13` each time (no `Set` needed).
- 🟢 FACT (all captures, same command over every `*btsnoop_hci.log`): every official-app and earlier app `Set` (35 in `CAP-001`, `-002`, `-006`,
  `-039`, `-051`, `-059`, `-060`) was ACKed and each was answered by a `Notify` with Settable `e8`; `CAP-062` holds the only NAKs. `CAP-036` (buds in
  the case for the whole session) contains **no** `Set` at all — only a `Get`/`Notify 01 e8 00 20` pair (frame 1182). 🟡 HYPOTHESIS: the official
  app does not offer ANC while the Buds report no settable mode (`CAP-001-EVENT-NOTES.md` records the ANC row "greyed out" until shortly before the
  first real `Set`).
- **EQ:** 🟢 FACT: no EQ refusal occurs in `CAP-062` — all 7 `WriteSetting`s (06:50:38–06:51:09, buds worn) were answered `OK`, and no EQ write was
  attempted with the buds in the case (the EQ tab showed "Not connected …" whenever the session had been closed by a dock change). Across all 51
  captures `scripts/pwrpc_decode.py` finds **no** `WriteSetting` `RESPONSE` with a status other than OK. The EQ part of observation 3 is **not
  reproduced**; whether the Buds accept an EQ write while docked is 🔴 untested (experiment: dock one bud, lid open, reconnect, move one slider).

**Answer:** the refusal is the Buds' firmware rule (NAK `0x02` whenever they report no switchable ANC mode, which in this session means "not
worn"). The app cannot override it and must not fake it (`AGENTS.md` §5). What the app can do is read the Settable byte it already receives
and show "ANC can only be changed while you wear the Buds" with the controls disabled — see the improvement list (RESULT §11, I-3).

## 3. Observations 2 and 5 — who ends the app's session while Android stays connected

**Every session end** (app `ConnectionState: Ready -> Disconnected`), with the first event that closed it:

| # | Session (phone time) | Ended by | ACL | Physical state on film | Evidence |
|---|---|---|---|---|---|
| 1 | 06:41:06.8–06:41:18.8 | the user's **Disconnect** tap | up | buds on the table | export 42; HCI 2851 |
| 2 | 06:41:27.2–06:41:52.9 | **Android** disconnect (Bluetooth dialog tap): phone `DISC` HFP 3036 → ACL `0x13` 3065 | down | — | film t=192–194 |
| 3 | 06:42:05.0–06:42:35.1 | ACL `Disconnection Complete` `0x13` (3814) ≈ 1–3 s after the **second bud was seated** (lid open) — ADR-016 | down | both docked | film t=229–237 |
| 4 | 06:43:03.0–06:43:28.2 | ACL `0x13` (4667) ≈ 0.5–1.5 s after the second bud was seated — ADR-016 | down | both docked | film t=284–291 |
| 5 | 06:43:31.9–06:43:40.7 | **Buds `DISC` DLCI 0x02** (5313) after one bud was **taken out** | up | one out | film t=302–307 |
| 6 | 06:43:47.7–06:46:04.7 | Buds `DISC` 0x02 (6258) when both buds went **into the case**, then ACL `0x13` (6277, +2.8 s) | down | both docked | film t=446–451 |
| 7 | 06:46:12.0–06:46:22.8 | Buds `DISC` 0x02 (6848) after the **Right** bud was taken out | up | Right out | film t=465–466 |
| 8 | 06:46:25.3–06:46:46.3 | Buds `DISC` 0x02 (7192) when the buds went back in, then ACL `0x13` (7225, +3.9 s) | down | both docked | film t=488–495 |
| 9 | 06:46:54.6–06:48:52.6 | Buds `DISC` 0x02 (8514) after both buds were **taken out** | up | both out | film t=614–619 |
| 10 | 06:49:10.6–06:51:41.1 | the user's Disconnect tap (H6) | up | worn | export 527 |
| 11 | 06:51:44.2–06:52:05.2 | Buds `DISC` 0x02 (9362) when the buds were taken **out of the ears** | up | on the table | film t=808–811 |
| 12 | 06:52:21.3–06:53:20.4 | the user's Disconnect tap (I4) | up | on the table | export 622 |
| 13 | 06:53:45.2–06:55:04.3 | Buds `DISC` 0x02 (10163) when the buds were put **in the ears** | up | worn | film t=984–991 |
| 14 | 06:55:09.1–06:56:26.4 | Android disconnect (Bluetooth dialog): phone `DISC` HFP 0x09 10503 → ACL `0x13` 10529 | down | worn | film t=1064–1067 |

- 🟢 FACT: **7 of 14** session ends were a Buds-side `DISC` on DLCI 0x02 with the ACL up (and Android still "Connected") — each within seconds of
  a **change of wear/dock state**: a bud taken out of the case (3/3), both buds into the case (2/2, followed by the ACL drop), out of the ears
  (1/1), into the ears (1/1). Seating the **first** bud did **not** close DLCI 0x02 in the two cases on film (06:42:27, 06:43:24.5 — the
  runtime-info stream even reported the change, 3760, 4652). 3 ends were the user's own tap, 2 were Android disconnects, 2 were the ADR-016 ACL
  drop with both buds seated.
- 🟢 FACT: the other RFCOMM clients recover by themselves: after each Buds-side `DISC` on 0x02 the phone also closes and **re-opens** the Google
  app's DLCI 0x08/0x0a within ≈ 1 s (5334/5335 → 5360/5387; 6869/6870 → 6897/6923), and after an ACL drop Android itself re-creates the ACL when
  the case is open (Create Connection 7226, 12 ms after the drop, no app activity). The app does not re-open its channel — by decision
  (`ARCHITECTURE.md` §6.0b: "no automatic session opening — … Connect stays a tap", maintainer 2026-09-20).
- 🟢 FACT: GrapheneOS's Bluetooth auto-off played no part — `BluetoothAutoOff … shouldScheduleAlarm: false` throughout the connected periods
  (system log 04:39:47.534 … 04:56:33.006).
- 🟡 HYPOTHESIS: the Buds close the MAESTRO channel on purpose on a wear/dock change (a session reset the official app is built to survive by
  re-opening), not a fault. Test: repeat one removal with the official app and look for its own re-`SABM` on the MAESTRO DLCI within seconds.

**Answer to observation 2:** the app is not disconnected by Android or by Bluetooth — the Buds close the app's DLCI 0x02 channel on every
wear/dock change, and the app, by the current decision, waits for a Connect tap. "Always connected while the phone is" needs a **new maintainer
decision (an ADR superseding the 2026-09-20 "no automatic session opening")** — options with costs in RESULT §11 (I-1, I-2).
**Answer to observation 5:** when both buds go in the case the Buds first close DLCI 0x02 and then drop the classic link (ADR-016); with the lid
open Android re-creates the link by itself (06:46:50.2), so an app that re-opens its session would get the docked state again: in this capture
every manual Connect with both buds docked (06:43:30.8, 06:46:09.6, 06:46:52.9) produced "Left (charging), Right (charging), Case 60 %".

## 4. Observations 4 and 5 — which bud is in the case, and the Case battery: the runtime-info stream already carries it

The `SubscribeRuntimeInfo` stream the app has subscribed to since ADR-043 (payload `3:0 6:{1:{1:<case %> 2:1} 2:{1:<%> 2:1|2} 3:{1:<%> 2:1|2}}
7:{1:0|1 2:0|1 3:0}`) against the per-bud charging bit (bit 7 of `03 03 00 03 <L> <R> ff`, ADR-033) of the nearest DLCI 0x04 battery frame:

| Stream frame (time) | `6.2.2` | `6.3.2` | `7.1` | `7.2` | 6.1 (Case) | Battery frame (L, R) | Film |
|---|---|---|---|---|---|---|---|
| 2782, 2927, 3236, 3488, 5508, 7118, 8612 … 11447 | 1 | 1 | 0 | 0 | absent | `64 64` (neither charging) | both out (table or ears) |
| 3760 (06:42:28.851) | 1 | **2** | **1** | 0 | 60 | `64 e4` (4520) | Right seated, Left out |
| 4845, 6429, 7447, 7649 | **2** | **2** | **1** | **1** | 60 | `e4 e4` | both seated |
| 7033 (06:46:25.732) | **2** | 1 | 0 | **1** | 60 | `e4 64` (7058) | Right taken out, Left seated |

Raw example (frame 7033, debug export line 298): `7e 00 a5 03 2a 1e 18 00 32 12 0a 04 08 3c 10 01 12 04 08 64 10 02 1a 04 08 64 10 01 3a 06 08 00
10 01 18 00 …` → RpcPacket payload `18 00` (3:0), `32 12 { 0a 04 {08 3c 10 01} 12 04 {08 64 10 02} 1a 04 {08 64 10 01} }`, `3a 06 {08 00 10 01 18 00}`.

- 🟢 FACT (this capture): entry **6.2 is the Left bud and 6.3 the Right bud** (their field 1 is the percentage, 100/100 like the DLCI 0x04
  frames); their **field 2 is `2` exactly when that bud's charging bit is set**, `1` otherwise; **`7.2` = Left and `7.1` = Right** in the case/
  charging; **entry 6.1 (Case) is present exactly when at least one bud is in the case** (lid open) — 22 of 22 stream packets agree.
- 🟢 FACT (all captures): the same comparison over every capture that carries both messages (`xval` script in the RESULT, 45 captures, 403 stream
  packets with a battery frame within ±3 s): field 2 of 6.2/6.3 agrees with the charging bit in **401/403**; `7.x` and the presence of 6.1 in
  **397/403**. The two field-2 mismatches (`CAP-006` 2959/2968) have their battery frame 1.9 s later, across a transition; the other four (`CAP-016`
  2199, `CAP-038` 3579/3605) have 6.3.2 = 2 right but `7.x`/6.1 not yet updated.
- 🟢 FACT: the Buds **push** the stream when a bud is seated or removed (3760 at 06:42:28.85 with the Right bud seated at ≈ 06:42:26–28; 7033 at
  06:46:25.7 and 7118 at 06:46:35.6 with the removals) — and nothing between changes (E6: no packet 06:46:57–06:48:52).
- 🟡 HYPOTHESIS: field 2 `2` / `7.x` `1` mean "this bud is in the case" (they coincide with charging; a bud in a case with an empty battery is
  the untested distinction). The app today reads only 6.1 (`RuntimeInfo.kt`), and "(charging)" only from DLCI 0x04 claims.
- 🟢 FACT: the Case value is also correct: 60 % on DLCI 0x02 equals the Google app's DLCI 0x08 push `… 0a 06 08 3c 10 01 18 03 …` (1766, 3607, …) —
  a 14th capture with Option F = Option E. When no bud is docked, DLCI 0x02 omits 6.1 (the app shows "unavailable") while DLCI 0x08 keeps pushing
  the last value **without** its flag (`0a 04 08 3c 18 03`, e.g. 2667) — the stale form of ADR-014.

**Answer to observation 4:** the Buds already tell the app which bud is in the case, on the app's own channel, without a new request — the app
throws it away. Showing it needs a maintainer decision to promote the field meanings (checkpoint) and a small decoder change (I-4). It still
depends on the session being open (observation 2).
**Answer to observation 5:** the Case battery is only reported while a bud is in the case (6.1 absent otherwise, 🟢 in this capture); the app
shows it whenever its session is open at that moment (06:43:32, 06:46:12, 06:46:55). What is missing is the session (§3) and, when no bud is
docked, the "last known" value — the app deliberately shows "unavailable" then; keeping the last value with its time would need the maintainer's
consent (I-5).

## 5. Observation 1 — Find My Buds with the buds in the case

- 🟢 FACT: no `04 01` was sent in `CAP-062` while a bud was in the case — every Ring/Stop (9541, 9607/9617, 9660, 9723, 9772, 9942) was sent with
  the buds on the table; each was ACKed `ff 01 00 03 04 01 00` and **echoed** by the Buds as `04 01 00 01 <value>` (9553, 9619, 9672, 9735, 9784,
  9954). The ring was **heard**: 3-s bursts on the audio track start ≈ 12 s after a Ring (it ramps up) and stop with each Stop (t ≈ 852, 874,
  913 s); after the Disconnect at 06:53:20.4 the Left ring kept sounding until the Stop at 06:53:50.1.
- 🟢 FACT: whenever both buds were in the case the app had no session: the Buds closed DLCI 0x02 and dropped the ACL (§3 rows 3, 4, 6, 8), and
  the Find buttons are disabled without a session ("Not connected to the Buds. Controls are disabled", film t=813). This is why Find "does not
  work" in the case in practice.
- 🔴 OPEN QUESTION: whether the Buds ring a bud that sits in the case. The Device Action spec defines no such exclusion and the official app's
  Find screen was never captured with docked buds. **Experiment** (no new send type — `04 01 00 01 02` is ADR-011's): case open, both buds
  docked, Android reconnected (it does so by itself), app Connect, Ring Left; look for ACK vs NAK `ff 02 00 03 <reason> 04 01` and listen.
  Case ringing itself stays out of scope (ADR-027).

## 6. DLCI 0x08/0x0a and DLCI 0x04: who else uses the channels

- 🟢 FACT: **the app never opened DLCI 0x08** (0 app lines; every 0x08/0x09 `SABM` is followed by the non-app burst `05 0c 00 00 … 0e 04 00 00 …`,
  e.g. 1717/1734).
- 🟡 → stronger HYPOTHESIS: the other owner of DLCI 0x08/0x0a is the Google app's Assistant-headphones service. **4 of 4** stops of
  `…googlequicksearchbox/…bisto.interactor.BistoRealService` ("Stopping service due to app idle") are followed by the phone's `DISC` of DLCI 0x08
  **and** 0x0a 31–298 ms later: 04:41:32.881 UTC → 06:41:32.912/.926 (2955/2956); 04:44:54.383 → 06:44:54.455/.470 (5842/5843); 04:48:15.872 →
  06:48:16.170/.174 (8224/8225); 04:57:55.173 → 06:57:55.252/.258 (11308/11309); and two service starts ("Waited long enough for …
  BistoRealService", 04:42:06.749, 04:46:23.019) precede a 0x08 `SABM` by 291 and 689 ms (3542, 6897). With `CAP-061` this is 5 of 5. Test still:
  the Google app disabled → DLCI 0x08/0x0a should never open.
- 🟢 FACT: Google Play services held DLCI 0x04 twice (1543, 2608; `03 08`, `07 10`, `06 01`) and lost it for good when the app's first claim
  failed (phone `DISC` 2784 at 06:41:07.219, the ADR-032 collision); every later 0x04/0x05 open (40) was the app's, on attempt 1. 🔴 Why Play
  services does not re-claim (third capture in a row); its *Nearby devices* permission state was again not recorded.
- 🟢 FACT (out of scope, ADR-008): 59 Fast Pair Key-based Pairing writes to `0x0c04` on the LE link, each answered "Error 0x81"; the phone closed
  the LE link at 06:40:12.8 (2355).

## 7. Defects in the app (beyond the observations)

1. 🟢 **I4 fails — the "ring still running" notice is cleared by Disconnect.** Film t=884.5/892 s: after Disconnect during a Left ring the Find tab
   shows only "Not connected …", although the ring kept sounding (audio, §5). Cause: `BudsRepositoryImpl.disconnect()` sets
   `_ringingTarget.value = null` (`BudsRepositoryImpl.kt:469`), so `RingingNotice` (`FindMyBudsScreen.kt:79-85`) never reaches its "reconnect and
   tap Stop" branch; `connect()` clears it again (`:424`).
2. 🟢 **Stale session-loss wording.** After the ACL drop at 06:42:35.113 the screen said "The Maestro channel (equalizer) was closed while Android
   still shows the Buds connected — likely another app …" (film t=238.5 s) although Android had lost the link; the Android link turned
   `NOT_CONNECTED` 116 ms after the RFCOMM loss line and 109 ms after the "Session lost" line (export 100–103) and the text changed ≈ 1.5 s after the loss (film t = 240.5 s, overlay 06:42:36, already
   "Android no longer shows the Buds connected …"; frames at t = 239/240.5/242/245 s, `ai-sessions/0048` §4). 🟢 FACT (film + export): the wording was
   chosen from the link state shown on screen, which passes a "not connected" only after the 1.5 s debounce of `OsConnectionObserver`, so the text
   named a link state older than the loss for ≈ 1.5 s. The "likely another app" guess is also wrong for the 7 Buds-side `DISC`s of §3.
3. 🟢 **ANC controls stay enabled while the Buds report no switchable mode** (Settable `00` in the claim's own `Notify`), so the user gets a
   refusal after 1–2 s instead of an explanation up front (§2).
4. 🟢 **The Case line says "The Buds haven't reported the Case level on this connection"** even right after the Buds did report it on this
   connection and then withdrew it because no bud is docked (7118 → screen "unavailable") — true to ADR-043 item 2, but misleading.
5. 🟡 **Refresh battery claims DLCI 0x04** (8 claims) although the stream already reports each bud's in-case/charging state on DLCI 0x02.

## 8. What works (evidence as above)

- 🟢 Pairing through the app (second attempt; the first, with the lid closed, found nothing and was cancelled cleanly), permissions, the
  Android-link mirror, 14 Connect/Disconnect cycles, the foreground service start/stop at every Connect/Disconnect/loss (system log
  `am_foreground_service_start/stop`), the notification text following the ANC mode (film t=940, 1009, 1022).
- 🟢 The firmware line and no Safe Mode (R1); ANC write with ACK (6/6 worn) and honest NAK reporting (10/10); the Quick Settings tile (G1–G4);
  EQ read on channels 19 and 21, 7/7 writes OK, one write per release, persistence across a reconnect (H6).
- 🟢 Left/Right battery with the charging flag on every claim; the Case from `SubscribeRuntimeInfo` = 60 % = DLCI 0x08; no dock sentence.
- 🟢 Find: Ring Left/Right/Stop sent, ACKed, heard, stopped.
- 🟢 No crash or ANR of the app (the `Signal Catcher … signal 3` at 04:58:30 UTC is the bug report's stack dump, `BUGREPORT_STARTED` 04:57:59).

## 9. Protocol correlation (per channel)

| Channel / protocol | What the app does | What the Buds answer | Goes well | Goes wrong / to improve |
|---|---|---|---|---|
| **DLCI 0x02/0x03 pw_rpc** (`maestro_pw.Maestro`) | per Connect: waits for `GetSoftwareInfo`, `ReadSetting 4:16`, `SubscribeRuntimeInfo`; EQ `WriteSetting` | announcement 22–250 ms after `UA`; RESPONSE OK; stream on dock changes | 14/14 announcements, 14/14 reads, 7/7 writes, stream (§4) | the Buds `DISC` it on every wear/dock change (§3); only 6.1 is used — 6.2/6.3/7 are dropped (§4) |
| **DLCI 0x04/0x05 Message Stream** | on-demand claims: snapshot (`08 11`), ANC `Set`, Ring/Stop, Refresh | Device Info (`03 0a`, `03 01 da 2d b1`, `03 02`, `03 09`, `03 0b`), `07 34`, battery ×3, `Notify`, ACK/NAK, Ring echo | 40 claims, all on attempt 1 after the first collision; every answer decoded | ANC `Set` sent while the claim's own `Notify` says no mode is settable (§2) |
| **DLCI 0x08/0x0a** (GSND) | nothing (ADR-043) | Case push to the Google app | no contention with the app | — (owner lead §6) |
| **HFP** (DLCI 0x0c/0x09) | nothing (ADR-040) | `AT+BIEV` ×14 | — | — |
| **LE / GATT** | nothing | Fast Pair KBP errors to Play services | — | out of scope (ADR-008) |
| **Android state** (profiles, ACL) | mirrors it (read-only) | — | "Connected to this phone (Android)" follows the ACL; Android re-creates the ACL on lid-open | stale loss wording (§7.2); no reaction to a Buds-side `DISC` with the link still up (§3) |

## 10. `APP_TESTPLAN.md` results

| Section | ✅ | ❌ | ⚠️ (done differently / partly) | not run |
|---|---|---|---|---|
| A | A3, A4 | — | A1, A2 (Bluetooth switched on in Quick Settings first) | A5, A6 |
| B | B1, B3 | — | B2 (first attempt with the lid closed: nothing found, cancelled) | B4 |
| C | C1, C2, C3, C4, C6, C7, C9, C10 | — | C8 (session ended by the ACL drop at the second bud, lid open; stale wording §7.2) | C5 |
| D | D1, D2 | — | — | — |
| E | E1, E2, E3, E4, E5, E6, E7 | — | — | — |
| F | F4 (worn), F8 | — | F1–F3 (sent with the buds not worn → NAK, shown correctly) | F5 (as written), F6, F7 |
| G | G1, G2, G4 | — | G3 (4 NAKs docked, 5 ACKs worn) | — |
| H | H1, H3, H4, H6 | — | H2 (Clarity only) | H5 |
| I | I1, I2, I3 | I4 (§7.1) | — | — |
| J | J1, J2, J3 | — | — | J4 |
| K | — | — | — | K1–K5 |
| L | L1, L2 | — | — | L3 |

## 11. Improvements

The prioritised list, with the governing rules, risks, tests and effort, is in `ai-sessions/0047_CAPTURE_RESULT_2026_09_25.md` §11. In short:
I-1 re-open the session after a Buds-side `DISC` while the ACL is up (needs a new ADR); I-2 background session via CDM device presence (new ADR);
I-3 disable ANC with an explanation while Settable = `0x00`; I-4 show which bud is in the case from the stream (needs the field promotion); I-5
keep the last Case value with its time; I-6 fix the I4 notice; I-7 fix the loss wording; I-8 Refresh battery from the stream; I-9 the Find-in-case
experiment.

## 12. Open questions

- 🔴 Whether a docked bud rings (§5); whether the Buds accept an EQ write while docked (§2).
- 🔴 Why Play services stops re-claiming DLCI 0x04 after one collision (3 captures).
- 🔴 What field 3 (`3:0`) and `7.3` of the runtime-info stream mean; whether field 2 means "in the case" or "charging" (§4).
- 🟡 Settable `0x00` = not worn (§2); the Buds' `DISC` on wear/dock changes as a deliberate session reset (§3); the DLCI 0x08/0x0a owner (§6).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-062-2026-09-25_06-38-36_06-57-10-Group_AX/CAP-062-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-062-2026-09-25_06-38-36_06-57-10-Group_AX/CAP-062-FINDINGS
