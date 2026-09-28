# Findings: `CAP-063` (Group AY — hardware re-test of the `ai-sessions/0048` and `ai-sessions/0052` builds; the maintainer's twelve observations answered)

Standardized, evidence-based extraction from `CAP-063-btsnoop_hci.log`, `CAP-063-recording.mp4`, `CAP-063-debug-export.log`,
`CAP-063-OpenControl-for-Pixel-Buds-log-a5f9783708f6.txt` (app logcat), `CAP-063-System-log-6cf0a8a3bd50.txt` and, as context only,
`CAP-063-btsnoop_hci.log.last`, per `ai-sessions/0053`. The timeline these findings refer to is `CAP-063-EVENT-NOTES.md`.

- 🟢 **FACT** — directly observed in this capture (frame number, log line or film time given).
- 🟡 **HYPOTHESIS** — plausible reading, not yet independently confirmed; the settling experiment is named.
- ⚪ **ASSUMPTION** — not tested here.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-063` · **Date:** 2026-09-27, 15:57:33–16:25:03 film overlay (phone 15:57:34.4–16:25:04.4) · **Firmware:** 🟢 `release_5.203`
(every DLCI 0x02 announcement, e.g. frame 1261) · **Phone:** Pixel 9a, GrapheneOS, Android 17 `CP2A.260805.005`, Google Play services and the Google
app present · **App under test:** OpenControl, the `ai-sessions/0052` build (`94e4fb1` or later; identified by log wording and wire behaviour, §0) ·
**HCI log:** 11,615 packets, raw, 0 `cap_len≠len` · **Buds:** `04:00:6e:cf:6e:07`, classic handle `0x000b` (HCI Connection Complete frame 810, reused by
every reconnect), LE handle `0x0042` (LE Connection Complete frame 692). LE handle `0x0040` is another device (`c8:cc:a8:e7:48:93`, frame 259) and is
excluded. `bluetooth.addr` is empty with this `H4 with linux header` encapsulation, so every command pre-filters by handle.

Commands used throughout (rule 4a):
`tshark -r CAP-063-btsnoop_hci.log -Y "bthci_acl.chandle==0x000b && btrfcomm && btrfcomm.len>0" -T fields -e frame.number -e frame.time -e
frame.p2p_dir -e btrfcomm.dlci -e data.data` (DLCI 0x04/0x08/0x0a payloads); the same with `-Y "bthci_acl.chandle==0x000b && btrfcomm &&
(btrfcomm.frame_type==0x2f || btrfcomm.frame_type==0x43 || btrfcomm.frame_type==0x63 || btrfcomm.frame_type==0x0f)"` for every `SABM`/`DISC`/`UA`/`DM`;
`python3 scripts/pwrpc_decode.py CAP-063-btsnoop_hci.log` (DLCI 0x02 — here always 0x02: every MAESTRO open was phone-initiated); HCI events with
`-Y "bthci_evt.code==0x03 || bthci_evt.code==0x04 || bthci_evt.code==0x05 || bthci_cmd.opcode==0x0405 || bthci_cmd.opcode==0x0406"`; HFP/AVRCP with
`-Y "bthci_acl.chandle==0x000b && (bthfp || btavrcp)"`. `p2p_dir` 0 = phone→Buds, 1 = Buds→phone. **Clocks:** phone = film overlay + 1.4 s (±0.2 s,
the status-bar minute flips 0.6 s into overlay second :58 at both 16:05→16:06, t = 504.8 s, and 16:23→16:24, t = 1584.8 s — no drift); HCI and the
debug export are phone local time; logcat and system log (UTC) = local − 2 h 00 min 00.0 s (logcat 14:24:21.283 "Session ended by the user's
Disconnect tap" ↔ HCI phone `DISC` DLCI 0x02 frame 10981 at 16:24:21.290).

---

## 0. Capture integrity and build identity (🟢 FACT)

- `capinfos`: 11,615 packets, "Packet size limit: (not set)", 15:57:40.614170–16:26:58.325151; `cap_len≠len` 0; one out-of-order pair (irrelevant).
  `.last`: 961 packets, 15:56:10.981–15:57:16.332 — an HCI Reset (frame 1) and only the other LE device (handle `0x0040`, disconnected 0x16 at
  15:57:15.867, frame 927): **no Buds traffic**, it ends 18 s before the film's first frame. Context only.
- Film: 1650.15 s, 49,253 video packets; stream 0 "Audio: none, 0 channels" — `ffmpeg -i CAP-063-recording.mp4 -map 0:a -f null -` fails "Decoding
  requested, but no decoder found for: none". **No sound claim can be checked on the film.**
- Debug export: exactly 65,536 bytes, 746 lines, 15:57:55.706–16:18:29.012, last line cut mid-frame; the 1,000-line buffer did **not** wrap (the first
  line is the link observer at app start). The share-sheet truncation is already fixed in the app (`800555d`). The logcat carries the app's
  `OpenControlBuds` lines only from 14:23:50.493 UTC (= 16:23:50.49) on, so **16:18:29–16:23:50 is covered by the HCI log alone**.
- Build: log wording that exists only from `0052` on — "Settings read (channel 21)" (export 15:58:15.392), "Runtime info re-requested on Refresh
  (channel 19)" (16:03:47.029), "Setting 17 written (channel 21)" (16:07:16.130), "Message Stream still claimed from an earlier action: released for a
  fresh claim" (16:14:50.721) — and wire behaviour: six `ReadSetting`s after every `ReadSetting 4:16` (15 of 15 Connects), `WriteSetting` of fields
  17/19/22/4/7. Nothing contradicts it (no `0048` loss wording, the tab is titled "Sound" on film). The exact commit hash was not recorded (P1).

## 1. Re-test verdicts (`ai-sessions/0048` §9, `0051` §19, `0052` §9)

| Item | Verdict | Evidence |
|---|---|---|
| AY-0 connect by itself | 🟢 **confirmed** (done differently: right after pairing, buds in the open case) | export 11–14: link CONNECTED 15:58:14.536 → "Automatic re-open (ADR-044, trigger: LINK_BACK)" 15:58:14.539 → app `SABM` DLCI 0x02 frame 1226 (15:58:14.727); announcement 1261 → `ReadSetting 4:16` 1267 → six reads 1293…1336 → `SubscribeRuntimeInfo` 1340 → snapshot claim `08 11` 1379 → `08 13 … 00 20` 1389 |
| AY-1 ring a docked bud | 🟢 **ACKed** — first capture of a `04 01` with both buds docked (lid open); audibility = the maintainer's observation (no audio) | 8518 `04 01 00 01 02` (16:18:48.220) → ACK 8520 `ff 01 00 03 04 01 00` → echo 8521; Stop 8598 → ACK 8600; runtime stream 8426 says both docked (6.2.2 = 6.3.2 = 2) |
| AY-2 EQ write docked | 🟢 **accepted** | 8633 `WriteSetting 4:{16:[-3.5,0.5,1,-1,2.5]}` (16:19:25.855, Balanced) → empty `RESPONSE` 8636; also 2374 → 2380 (15:59:25, Light bass, docked) |
| AY-2b mono write docked | 🟢 **accepted** | 8662 `4:{19:1}` → 8665; 8668 `4:{19:0}` → 8670 |
| AY-3a–c one bud worn (Settable test) | ⚪ **not run as designed** (skipped); two incidental one-worn periods exist but the worn bud is off film — §3 | — |
| AY-4 ANC while not worn | 🟢 **confirmed** | Notify 4774 `08 13 00 04 01 e8 00 20` (16:06:11.245, buds on the table on film); no DLCI 0x04 `SABM` from 16:06:12.765 (4787 `DISC`) until 16:12:33.477 (5894); export 16:06:30.286 "ANC tile tapped while the Buds allow no ANC change — nothing sent" |
| AY-5 one bud out → re-open | 🟢 **confirmed** (done differently, 3×) | Buds `DISC` 3927 → app `SABM` 3949 (+2.08 s); 4684 → 4707 (+2.09 s); 10644 → 10776 (+2.04 s); each "Automatic re-open (…AFTER_LOSS)" 1.5 s after the loss line |
| AY-6 both into the case | 🟢 **confirmed** — ACL drop, no re-open while the link is down; the re-open followed Android's own reconnect | ACL `0x13` 6378 (16:15:21.995) and 7566 (16:16:35.891); no app `SABM` until the user's Connect (6573) resp. Android's reconnect 7611 → re-open 7810 |
| AY-7 buds into / out of the case | 🟢 **confirmed** — every dock change pushed a stream packet and the charging lines followed | §4 table |
| AY-8 / AY-8b Android-panel disconnect / reconnect | 🟢 **confirmed** | phone `DISC` HFP DLCI 0x09 10001 (16:23:14.294) → ACL `0x13` 10030; Create Connection 10075 (16:23:36.540) → app `SABM` 10247 (+1.08 s after Connection Complete 10077) |
| AY-9 no re-open after Disconnect | 🟢 **confirmed** (twice) | no app `SABM` DLCI 0x02 between the Disconnect taps 2837 / 3443 and the Connect taps 2980 / 3610, despite a bud re-seated and a Home/return (export 166–169) |
| AY-10 background loss → re-open on resume | 🟢 **confirmed, by accident** | Buds `DISC` 7175 (16:16:05.556) while the app was stopped (export 641 "observer stopped" 16:16:04.440); re-open only at resume (export 650, 16:16:06.213), one `SABM` 7350 |
| AY-11 ring notice across Disconnect | 🟢 **confirmed** (the `CAP-062` I4 defect is fixed) | Ring 10951 → ACK 10959; Disconnect 10981; Connect 11043; Stop 11167 → ACK 11178; film 16:24:32–41 shows both notice texts (§EVENT-NOTES) |
| AY-12 remaining `APP_TESTPLAN.md` steps | partly: C5 (one tap), F5 (not identifiable), F6 ✓, F7 ✗ not run, H5 not shown, J4 ✓ (≈ 1 min), K1–K5 ✗, L3 ✗, A5/B4/(E) ✗ | EVENT-NOTES step mapping |
| AY-13a Refresh on a fresh claim | 🟢 **confirmed** (8 Refreshes) | each: app `SABM` DLCI 0x04 → `03 03 00 03 …` burst (3905, 4107, 4180, 6094, 6143, 6232, 6281, 8455) and exactly one `SubscribeRuntimeInfo` (3863, 4066, 4143, 6057, 6107, 6195, 6239, 8417) |
| AY-13b two Refreshes within 1 s | 🟢 **confirmed** (16:14:49.924 and 16:14:50.717, 0.79 s apart) | export 557–579: "Message Stream still claimed from an earlier action: released for a fresh claim"; `SABM` 6216 → burst 6232 → `DISC` 6240 (16:14:50.724) → `SABM` 6263 → burst 6281; two re-subscriptions 6195/6239, each answered (6202, 6249) |
| AY-14 re-subscription answered? | 🟢 **answered — 8 of 8** re-sent `SubscribeRuntimeInfo`s on an open channel got a `SERVER_STREAM` within 19–355 ms | 3863→3870, 4066→4078, 4143→4152, 6057→6064, 6107→6114, 6195→6202, 6239→6249, 8417→8426 (docked, idle 93 s: 16:18:40.329 → 16:18:40.523, Case 34 %) |
| `0052` §9 settings reads | 🟢 **confirmed** — 15 of 15 Connects: `ReadSetting 4:2, 4:4, 4:7, 4:17, 4:19, 4:22`, all answered OK, sequential | §5 |
| `0052` §9 writes byte-compared | 🟢 **byte-identical** to `SettingsFixtures.kt` for the same channel (reads ×9, `CONV_OFF/ON`, `TOUCH_OFF/ON`, `MONO_ON/OFF` on their channels); identical modulo address/`channel_id`/CRC for the rest (hold L/R ×10, mono ch 21 ×3, balance) — 174 of 174 phone frames CRC-valid | §5 |
| `0052` §9 read-back after reconnect | 🟢 **confirmed** (D13, 16:12:33) | 5881…5929: `17:7` (Right 4 = last write 5368), `19:0` (5375), `22:1` (5386), `4:1` (5490), `7:{1:5 2:5}` (5796/5803) |
| Safe Mode not triggered | 🟢 **confirmed** | "Firmware: release_5.203" on film; 46 writes sent and answered; no "Safe Mode" line |
| "Refuted if" list (skeleton A.7, applied literally) | 🟢 **none of the eight refutation conditions occurred** | no app `SABM` 0x02 while not visible or after a Disconnect tap before Connect; one per event; no `08 12` after a Settable `00` Notify (the three `Set`s 4233/4249/4497 follow `e8` Notifies 4184/4244/4253); every Case value on film carries a time; no "another app" loss text; every settings change on screen follows its `RESPONSE`; no field 12; no app frame on DLCI 0x08; one `SubscribeRuntimeInfo` per Refresh |

## 2. Observations 1–3, 10, 11 — what the wire shows for the writes and the ring

- 🟢 **Find My Buds (obs. 1):** two rings, both ACKed and echoed — docked (8518 → 8520/8521, Stop 8598 → 8600/8601) and on the table (10951 →
  10959/10962, Stop after a reconnect 11167 → 11178/11179). The film has no audio: "the buds ring" stays the maintainer's observation.
- 🟢 **EQ (obs. 2):** 5 of 5 `WriteSetting 4:{16:…}` answered OK (2374, 6009, 6017, 6026, 8633), one per preset tap or slider release, and persisted
  (the next Connect reads the last write: 6606 at 16:15:37.232 → `[-5.71, 3, 0, 0, 5.23]`). Audibility = observation.
- 🟢 **Balance and mono (obs. 3):** 20 balance writes (17:200 = Left 100, 17:199 = Right 100, 17:104 = Left 52, then small values), 5 mono writes —
  every one answered OK within 40–450 ms. The "Centre" label was never shown: the finger landed on ±1…±11 in 12 releases between 16:07:43 and 16:08:00
  (e.g. 5215 `17:1`, 5219 `17:1`, 5245 `17:1` = Left 1/…); the slider has 201 positions on a narrow track (`EqScreen.kt:220-227`, `SettingsUi.kt:54-58`).
  Audibility = observation.
- 🟢 **Use touch controls (obs. 10):** `4:{4:0}` 5471 (16:09:41.032) → OK; `4:{4:1}` 5490 (16:09:58.988) → OK. **While it was off, the Buds sent no
  AVRCP pass-through at all** (none between 16:09:30.43 and 16:10:11.23); with it on, a tap sequence followed: PAUSE 5496 (16:10:11.231), PLAY 5532
  (16:10:15.133). 🟡 the maintainer tapped the bud during the off period (off film); the wire is consistent with "taps ignored".
- 🟢 **Press-and-hold (obs. 11):** 10 `WriteSetting 4:{7:{1|2:{4:{1:5|6}}}}` (5654 … 5803), all OK. "Digital assistant" → see §7.

## 3. ANC and the Settable byte (obs. 9, lead 2)

Every `Set` of the app and every `Notify` of the Buds (DLCI 0x04; all on the app's own claims — Google Play services held DLCI 0x04 only
15:58:15.02–15:58:15.12 and 15:58:20.32–16:00:16.81, frames 1275/1708, and never re-claimed after the app's collision at 16:00:16.81):

| Notify frame (time) | Settable | Mode | Physical state (film) |
|---|---|---|---|
| 1389 (15:58:15.838), 2761 (16:00:17.319), 6635 (16:15:37.279), 8032 (16:17:06.249), 8461 (16:18:40.838) | `00` | `20` | both in the case |
| 4774 (16:06:11.245), 10844 (16:23:53.720), 11116 (16:24:40.503) | `00` | `20` | both on the table |
| 9094 (16:20:27.639) | `00` | `20` | both just taken out of the case, in the hand |
| 3050 (16:01:03.940), 4184, 5923, 6097, 6148, 6236, 6285 | `e8` | `08` | both off film (the ANC-tab and settings steps "worn") |
| 3376 (16:02:09.252), 3678 (16:03:05.803) | `e8` | `40` | one bud lying in the lower slot (**not** charging, §4), the other off film |
| 3909 (16:03:48.083) | `e8` | `40` | one bud on the table beside the case (film), the other off film |
| 4019, 4111 (16:04:03–17), 7420 (16:16:08.603), 10379 (16:23:37.779) | `e8` | `40` | both off film |
| 4244/4245, 4252/4253, 4509/4512 | `e8` | `40`/`20`/`08` | after the app's `Set`s (below), worn |

- 🟢 FACT: the three app `Set`s were answered ACK: 4233 `08 12 … 40` → 4241 `ff 01 00 06 08 12 01 e8 e8 40`; 4249 `… 20` → 4251; 4497 `… 08` → 4508
  (F6: ADAPTIVE then OFF 1.37 s apart, both applied in order). **Correction of the draft timeline:** the 16:05:27.97 `Set` ACTIVE is an **app tap on
  ACTIVE** (film t = 470–471 s: the finger on the ACTIVE button at overlay 16:05:24–25), not an ANC Refresh; there is no `08 11` in that claim.
- 🟢 FACT: the ANC mode changed **without any app `Set`** three times — `08` (3050, 16:01:03) → `40` (3376, 16:02:09); `40` (3909, 16:03:48) → `08`
  (4184, 16:04:32); `08` (6285, 16:14:51) → `40` (7420, 16:16:08). 🟡 HYPOTHESIS: the maintainer's press-and-hold on a bud (Noise control) — the gesture
  is off film, the wire carries no command for it (the Buds apply it themselves and only report it in the next `Notify`). This is the evidence behind
  observation 9's "long press works"; F5 as written (hold, then Refresh) is **not identifiable**.
- 🟢 FACT: every `00` reading on film had no bud worn (in the case, on the table, in the hand); every `e8` reading came with at least one bud off film.
  The film never shows the ears, so "one bud worn" is 🟡 for the one-bud rows. **ADR-024 Update's 🟡 "Settable `0x00` = no bud worn" is not
  contradicted (0 counter-examples in 22 `Notify`s) and gains the in-the-hand case (9094), but it is not settled:** AY-3 (one bud visibly worn on
  film, the other visibly on the table) was skipped.
- 🟢 FACT: while not worn the Buds report mode `20` (OFF) although the last app `Set` was ACTIVE (4497 at 16:05:27 → 4774 at 16:06:11 reads `20`, and
  5923 at 16:12:33 reads `08` again once worn). The app shows "ANC mode: OFF" then (film 16:06:12). 🟡 HYPOTHESIS: `20` is the Buds' "nothing active
  while not worn" report, not a mode change.
- 🟢 **Defect (app): ANC stays disabled after the buds are back in the ears until the next claim.** The `00` Notify 4774 (16:06:11) set
  `AncAvailability.NOT_ALLOWED`; the buds were picked up at ≈ 16:06:48 (film) and worn for the settings steps, but no claim followed until the D13
  reconnect (5894, 16:12:33), so the ANC buttons stayed disabled ~6 min (film 16:06:57–16:07:12 "ANC can only be changed while you wear the Buds").
  Putting the buds in the ears did **not** close DLCI 0x02 this time (no Buds `DISC` between 4684 and the user's 5829), so ADR-044's re-open — which
  re-reads the Settable byte in its snapshot claim — did not run. Cause in code: `BudsRepositoryImpl.setAncMode` returns `AncNotAllowed` before any
  claim while `_ancAvailability == NOT_ALLOWED` (`BudsRepositoryImpl.kt:651-657`); only Refresh or a reconnect re-reads it.

## 4. Battery and dock state (obs. 5, 6, 7; lead 8)

Runtime-info stream (`SubscribeRuntimeInfo`, `3:0 6:{1:{1:case 2:1} 2:{1:L 2:1|2} 3:{1:R 2:1|2}} 7:{1:R 2:L 3:0}`) against the film (upper slot =
**Right**: at 16:15:51.551, frame 7059, 6.3.2 turns 1 when the upper bud leaves; 16:16:10.762, frame 7439, 6.3.2 = 2 when it is re-seated):

| Stream frame (time) | 6.2.2 L | 6.3.2 R | 6.1 Case | Film |
|---|---|---|---|---|
| 1342 … 2723 (15:58:15–16:00:17) | 2 | 2 | 37 → 36 | both seated |
| 3059 (16:01:04.208) … 3277 | 1 | 1 | — | both out |
| 3386 (16:02:09.528), 3687, 3809 (16:03:34.358) | 1 | 1 | — | **a bud lies in the lower slot 16:02:06–16:02:16 and 16:02:29–16:03:17 (film), but the Buds report it not charging** |
| 6338 (16:15:14.895) | **2** | 1 | 36 | Left seated first |
| 6658 (16:15:37.620) | 2 | 2 | 36 | both seated |
| 7059 (16:15:51.551) | 2 | 1 | 36 | Right (upper) out |
| 7112 (16:15:54.748) | 1 | 1 | — | Left out |
| 7439 (16:16:10.762) | 1 | **2** | 36 | Right re-seated |
| 7555 (16:16:30.478) | 1 | 2 | 34 | Left in the hand, seated ≈ 16:16:32–33 (film) — the ACL dropped (7566, 16:16:35.891) before a packet reported it |
| 8082 (16:17:06.593) … 8641 | 2 | 2 | 34 | both seated (after Android's reconnect) |
| 9116 (16:20:28.023) | 2 | 1 | 33 | Right out first |
| 9247 (16:20:28.964) | 1 | 1 | — | both out |

- 🟢 FACT: every value the app showed matched the wire at the time: Left/Right 100 % (`03 03 00 03 e4 e4 ff` / `64 64 ff`, 29 of 29 app claims carried
  the burst), Case 37 → 36 → 34 → 33 % (1342, 2462, 7555, 9116); Android's Bluetooth panel showed "Batterijniveau 100%" (film 16:17:06). Each
  percentage carried its own receive time; a Case without 6.1 was shown as "last seen HH:MM:SS" (film 16:01:05, 16:15:56, 16:20:29).
- 🟢 **Correction of the draft timeline:** at 16:16:30 the screen says Left "not charging (out of the case) (16:16:30)" — the draft's "Left charging in
  the case (16:16:30)" is wrong (film frame t = 1136.4, overlay 16:16:29; the Left bud is still in the hand).
- 🟢 FACT: the **16:02–16:03 "bud in the case, not charging"** is the Buds' own report (stream 3386/3687/3809 and DLCI 0x04 `64 64 ff` 3369/3674), not an
  app defect. 🟡 HYPOTHESIS: the bud was not seated deep enough to charge (the film shows it resting on the slot). 🔴 whether the charge contacts or a
  magnet sensor decide "in the case".
- 🟢 FACT (new vs `CAP-062`): **the stream is not silent while docked and idle** — 8165, 8200, 8306, 8380 (16:17:18–16:18:28) repeat the same values,
  each within 2–40 ms of an `AT+BIEV=2,100` (8169, 8201, 8307, 8381). The Case time on screen therefore moves without a user action (film 16:17:18,
  16:17:28, 16:18:18, 16:18:29).
- 🟢 FACT: **the Buds answer a second `SubscribeRuntimeInfo` on an open channel** (8 of 8, §1 AY-14). ADR-043's 2026-09-26 Update's 🔴 is answered.
- 🟢 Cosmetic (app): after a Connect the previous session's per-bud lines stay ≈ 2 s ("charging in the case (16:00:17)" on film 16:01:02–03) until the
  new claim/stream arrives (3046 at 16:01:03.872, 3059 at 16:01:04.208) — dated, so not a false claim, but visibly stale.

## 5. Settings reads and writes (DLCI 0x02, ADR-036/045)

- 🟢 15 Connects, each: announcement → `ReadSetting 4:16` → `4:2, 4:4, 4:7, 4:17, 4:19, 4:22` → `SubscribeRuntimeInfo`, every answer OK
  (`python3 scripts/pwrpc_decode.py …`); 90 settings reads, 0 errors. Values: in-ear detection `2:1`, touch `4:1`, hold `7:{1:5 2:5}` (Noise control),
  balance `17:7` (= Right 4), mono `19:1` → `19:0` after 16:08:06, conversation detection `22:1`.
- 🟢 46 writes (5 EQ, 20 balance, 5 mono, 4 conversation detection, 2 touch, 10 hold — `grep WriteSetting`), **46 of 46 answered by an empty
  `RESPONSE`, status OK**, 37–450 ms later; the screen changed only after it ("changed HH:MM:SS"; export "Setting N written" 13–30 ms after each
  `RESPONSE`, e.g. 5092 at 16:07:16.110 → export 16:07:16.130).
- 🟢 Byte comparison (script over every phone→Buds DLCI 0x02 frame, HDLC-unescaped, CRC-32 checked, 174/174 valid): the requests equal
  `SettingsFixtures.kt` byte for byte where the channel is the fixture's (`READ_2/4/7/17/19/22_REQ` ×9 on ch 21, `CONV_OFF_1720`/`CONV_ON_1808` ×2,
  `TOUCH_OFF_1995`/`TOUCH_ON_1741`, `MONO_ON_1621`/`MONO_OFF_1823` on ch 19), and equal them apart from the pw_hdlc address, `channel_id` and CRC on
  the other channel (hold ×10, mono ch 21 ×3, reads ×6 on ch 19, balance ±100). No request names field 12; no `SubscribeToSettingsChanges`.
- Raw example (frame 5471, 16:09:41.032, touch controls off, ch 21): `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 04 22 02 20 00 53 90 8d 4b 7e`
  = `TOUCH_OFF_1995` exactly; answer 5473 `RESPONSE` status OK.

## 6. Session ends and re-opens (obs. 5, 8; lead 9)

| # | Session end (phone) | Ended by | ACL | Film | App cause shown | Re-open |
|---|---|---|---|---|---|---|
| 1 | 16:00:10.18 | Disconnect tap (2628) | up | buds docked | — | user Connect 16:00:16.59 (2670) |
| 2 | 16:00:29.95 | Disconnect tap (2837) | up | docked | — | none during AY-9; user Connect 16:01:02.42 (2980) |
| 3 | 16:02:06.15 | Buds `DISC` 3285 | up | a bud going from the hand/ear onto the lower slot | "was closed" for < 1 s, then "The Buds closed the app's channel …" (export 128) | automatic 1.5 s later (export 129, `SABM` 3309) |
| 4 | 16:02:27.16 | Disconnect tap (3443) | up | — | — | user Connect 16:03:04.18 (3610) |
| 5 | 16:04:00.28 | Buds `DISC` 3927 | up | the table bud picked up (to an ear) | Buds closed | automatic (3949) |
| 6 | 16:06:08.05 | Buds `DISC` 4684 | up | both out of the ears onto the table | Buds closed | automatic (4707) |
| 7 | 16:12:30.18 | Disconnect tap (5829, D13) | up | worn | — | user Connect (5855) |
| 8 | 16:15:22.00 | ACL `0x13` 6378 (16:15:21.995), both seated (ADR-016) | down | screen dimmed/off | "The Maestro channel (equalizer) was closed. Tap Connect to reconnect." (undetermined — no link reading within the window) | none (link down; Android did **not** reconnect by itself); user Connect 16:15:33.85 created the ACL (6490) |
| 9 | 16:16:05.56 | Buds `DISC` 7175, app not visible | up | buds off film | Buds closed (export 649) | on resume (export 650, `SABM` 7350) |
| 10 | 16:16:35.90 | ACL `0x13` 7566, second bud seated | down | Left seated ≈ 16:16:32–33 | "Android no longer shows the Buds connected …" (after a < 10 ms wrong intermediate "Buds closed" log line, export 697–699) | when Android reconnected (user tap in Quick Settings, 7611) → 7810 |
| 11 | 16:19:48.16 | ACL `0x13` 8687, lid closed (C8) | down | lid closed 16:19:45–47 | "Android no longer shows …" (film 16:20:05) | Buds paged the phone on lid-open + removal (Connect Request 8754, 16:20:25.84) → app `SABM` 9035 |
| 12 | 16:23:15.72 | Android-panel disconnect: phone `DISC` HFP 10001 → ACL `0x13` 10030 | down | — (dialog covers the app) | not on film | Android reconnect 10075 → 10247 |
| 13 | 16:23:50.48 | Buds `DISC` 10644 | up | both laid on the table | Buds closed | automatic (10776) |
| 14 | 16:24:21.29 | Disconnect tap (10981, AY-11) | up | on the table | — | user Connect (11043) |

- 🟢 ADR-044 held in every case: one attempt per event, only while visible, none after a Disconnect tap, no re-open while the link was down, no
  second loss within 10 s of an automatic re-open. 🟢 GrapheneOS's Bluetooth auto-off played no part (`BluetoothAutoOff … shouldScheduleAlarm: false`,
  e.g. 14:23:37.264 UTC). 🟢 The foreground service started/stopped at every session change (29 `am_foreground_service_start/stop` for the package).
- 🟢 **Difference from `CAP-062`:** after an ADR-016 ACL drop with the lid open, Android did **not** re-create the ACL by itself (#8: nothing for 11 s
  until the user's Connect; #10: nothing for 27 s until the Quick Settings tap). `CAP-062` 7226 re-created it 12 ms after the drop. 🔴 why.
- 🟢 Putting buds **into** the ears closed DLCI 0x02 in `CAP-062` (1/1) but **not** here (16:06:48 → no Buds `DISC`), and the one Buds `DISC` that
  coincided with a bud reaching the case slot (#3) came 0–3 s **before** the film shows it seated — 🟡 the trigger is the ear removal, not the
  seating (consistent with `CAP-062` "seating the first bud did not close DLCI 0x02, 2/2").
- 🟢 **Loss-cause wording defect (#8):** the app's loss happened while it was not visible (observer stopped 16:15:21.408, export 586), the first link
  reading came 3.05 s after the loss (export 593), outside the −2…+2 s window of `classifySessionLoss` — so the screen kept the "undetermined"
  sentence "The Maestro channel (equalizer) was closed", although Android's link was down (the next reading) and the ACL had dropped.

## 7. Digital assistant, conversation detection, touch taps — AVRCP/HFP/GSND (obs. 4, 11; leads 6, 7)

- 🟢 **Conversation detection (obs. 4):** during 16:09:21–16:09:36 (Sound tab idle, music playing, conversation detection on since 5386 at
  16:09:20.818) the Buds sent **AVRCP pass-through PAUSE** (5389, 16:09:22.952, pushed/released 5392) and **PLAY** (5433, 16:09:30.364), each answered
  "Accepted", followed by the phone's PlaybackStatusChanged Paused/Playing (5396, 5439). No DLCI 0x04 frame (no claim held) and no DLCI 0x02 frame in
  between. Film t = 704–715 s: no hand near the phone at 16:09:21.6 / 16:09:29.0 overlay; the maintainer's ears are not on film. 🟡 HYPOTHESIS:
  conversation detection pauses the media with an ordinary AVRCP PAUSE and resumes it with PLAY — a tap on a bud produces the identical frames
  (5496/5532 at 16:10:11/15 after touch controls were switched back on), so the wire alone cannot tell them apart. This differs from `CAP-029` §2
  ("no wire effect"), where no media session was playing. Settling experiment: conversation detection on, hands visible on film, speak; then off, speak.
- 🟢 **Digital assistant (obs. 11):** the hold actions were set to "Digital assistant" from 16:10:36 (Left) / 16:11:24 (Right) until 16:12:03/09. In
  that window the Buds sent **no** HFP `AT+BVRA` (none in the whole capture, on either HFP DLCI: dissected 0x0c and raw 0x09 searched for `42 56 52 41`), **no** AVRCP pass-through, only AVRCP VolumeChanged 26 % (5725,
  16:11:36.17, system log `volume_changed … caller=com.android.bluetooth` 14:11:36.251 UTC) and PAUSE 5733 (16:11:44). The GSND channels
  (DLCI 0x08/0x0a, "GSND CONTROL"/"GSND AUDIO", `CAP-033`) were **closed** the whole time: the phone closed them 373 ms after Android stopped the
  Google app's `…bisto.interactor.BistoRealService` ("Stopping service due to app idle", 13:59:59.985 UTC → `DISC` 2572/2573 at 16:00:00.358/.370) and
  nothing re-opened them until 16:15:39 (6919/6947). The same stop→close pattern repeats 3 more times (14:18:26.652 → 8363 +69 ms; 14:21:48.847 → 9617
  +156 ms; 14:24:58.323 → 11223 +31 ms) — 4 of 4 here, 9 of 9 with `CAP-061`/`CAP-062`. The system log also reports `VoiceInteractionManager: no
  auto selectable voice recognition services found for user 0` (13:57:42.120 UTC).
  🟡 HYPOTHESIS (strong): the Buds' "Digital assistant" hold is served by the Google app's Assistant-on-headphones service over GSND (DLCI 0x08/0x0a),
  not by HFP voice recognition — so on this phone nothing is triggered unless that service holds the channel; the "different sound" the maintainer
  heard is the Buds' own earcon. `GSOUND` (`PROTOCOL.md` §6, the cross-vendor lead) fits Google's "GSound" headphone-assistant naming. Android's
  documented route is HFP voice recognition — developer.android.com `BluetoothHeadset.startVoiceRecognition` (fetched 2026-09-28): *"This methods
  sends the voice recognition AT command to the headset and establishes the audio connection"* — i.e. `AT+BVRA`, which neither side sent here. Settling experiment: the Google app disabled vs enabled, hold = Digital assistant, capture DLCI 0x08/0x0a and `AT+BVRA`.
- 🟢 In-ear auto-pause: PAUSE 4329 (16:05:01), 6301 (16:15:08, buds going into the case) — the Buds' own media control, no app involvement.

## 8. Wishes 7 and 12 — lid and in-ear state; wish 11 — the ANC-mode list

- 🟢 **Lid (wish 7):** closing the lid with both buds docked dropped the ACL (8687, 16:19:48.16, 1–3 s after the film shows it closed) — the same event
  as "both buds seated" (ADR-016, #8/#10 above). Opening it produced nothing until a bud was taken out (Buds' Connect Request 8754, 16:20:25.84; film:
  lid open 16:20:13–14, first bud out 16:20:23). No RFCOMM frame, no stream packet, no Android broadcast in the system log marks the lid itself.
  The only lid-correlated signal is on LE: the Buds' Fast Pair / Find Hub adverts (`0xFE2C`/`0x1853` and `0xFEAA` service data, reported under
  `04:00:6e:cf:6e:07` — 2,292 reports, `tshark … -Y "bthci_evt.code==0x3e && bthci_evt.bd_addr==04:00:6e:cf:6e:07"`) stop between 16:19:47 and
  16:20:10 (lid closed) — seeing that needs BLE scanning, which this app does not do (no `BLUETOOTH_SCAN`; ADR-006's bounded exception covers the
  battery advertisement only). **Answer: not possible within the current decisions** (no signal on any channel the app uses).
- 🟢 **In-ear (wish 12):** no per-bud wear signal exists on any channel the app reads: stream field 3 = 0 and 7.3 = 0 in all 72 stream packets; the
  settings read (field 2 = 1) is the *setting*, not the state; the only wear-related value is the Settable byte (🟡 "any bud worn", aggregate, §3),
  and the Buds' `DISC` of DLCI 0x02 on some wear changes (§6, not on all). 🔴 whether any other `maestro_pw` stream carries it (the official app's
  unnamed services `0x73d5d805`, `0xaf3a7737`, … are not decoded).
- 🟢 **Field 12 (wish 11):** no frame in `CAP-063` names field 12 (the app cannot encode it; the Buds pushed nothing — the app does not subscribe to
  settings changes). The bit order stays 🔴 (`PROTOCOL.md` §4.5.3 2026-09-26 Update); Group AR (`CAP-056`, Pixel 7a, official app) settles it.

## 9. What works (evidence as above)

- 🟢 Pairing from the app (CDM → bond → ACL in 5 s, 15:58:09–15:58:14), permissions, automatic open right after pairing (AY-0), 15 Connects and 14
  session ends handled, ADR-044 in all its branches (§6), the foreground service, the tile's refusal while not worn.
- 🟢 Firmware line, no Safe Mode; ANC `Set` ACKed 3/3; Settable gate (no `Set` while `00`); Find ring/stop ACKed 4/4 incl. docked and across a
  reconnect with the right notices (AY-11).
- 🟢 Settings: 90 reads, 46 writes, all OK, byte-identical to the fixtures, read back after a reconnect; the screen only changes after the Buds' OK.
- 🟢 Battery: L/R with charging on 29/29 claims; Case from the stream with its time and "last seen"; per-bud charging lines follow every dock change;
  Refresh on a fresh claim (8/8) with one answered re-subscription each; two Refreshes 0.79 s apart handled.
- 🟢 No crash or ANR of the app (system log: the only `am_crash` is `org.thoughtcrime.securesms`, `TransactionTooLargeException` "data parcel size
  898632 bytes", 14:25:50 UTC — after the film, 🟡 the share-sheet export of this session into another app; the app itself was unaffected).

## 10. Protocol correlation (per channel)

| Channel / protocol | What the app does | What the Buds answer | Goes well | Goes wrong / to improve |
|---|---|---|---|---|
| **DLCI 0x02 pw_rpc** (`maestro_pw.Maestro`, ch 19/21) | per Connect: wait for `GetSoftwareInfo`, `ReadSetting 4:16`, six settings reads, `SubscribeRuntimeInfo`; writes 16/17/19/22/4/7; one re-subscription per Refresh | announcement, `RESPONSE` status OK 166 of 166 (15 announcements, 105 reads, 46 writes), stream on dock changes and with `AT+BIEV` | 15/15 announcements, 0 errors, re-subscription answered 8/8 | the Buds `DISC` it on some wear changes (6 of 14 ends), not on putting buds in the ears |
| **DLCI 0x04 Message Stream** | 29 on-demand claims (+2 collisions with Play services at the first two Connects) | Device Info (`03 0a` nonce, `03 01 da 2d b1`, `03 02` BLE address, `03 09` "Revision 6", `03 0b` FHN), `07 34`, battery ×3, `Notify`, ACK, ring echo | every claim got its burst; ANC/Ring ACKed | Settable `00` disables ANC until the next claim even after the buds are worn again (§3) |
| **DLCI 0x08/0x0a** (GSND) | nothing | Google app (Bisto) opens/closes them | no contention | — (the assistant path, §7) |
| **HFP** (DLCI 0x0c when the phone set it up; DLCI 0x09 on the Buds-initiated reconnects 16:15:38 and 16:20:28 — undissected, raw `41 54 2b …`) | nothing (ADR-040) | `AT+BIEV=2,100` ×62 (44 on 0x0c, 18 on 0x09), `+CIEV` signal; **no `AT+BVRA`** anywhere | — | — |
| **AVRCP** | nothing | PLAY/PAUSE pass-through on taps, in-ear and (🟡) conversation detection; absolute volume | shows touch-controls off works | — |
| **LE / GATT** (handle `0x0042`) | nothing | 58 Fast Pair Key-based Pairing writes by Play services, each "Error 0x81"; LE closed 15:58:39.84 (0x16) | — | out of scope (ADR-008) |
| **Android state** | mirrors it; ADR-044 re-open | — | link-back and resume re-opens | undetermined loss text when the loss happens while not visible (§6 #8) |

## 11. Improvements

The prioritised list, with rules, risks, tests and effort, is in `ai-sessions/0053_CAPTURE_RESULT_2026_09_27.md` §E. In short: re-check ANC
availability on a disabled tap (§3); re-classify a loss that happened while not visible on return (§6 #8); a snap-to-centre / "Centre" control for
balance (§2); mark the previous session's per-bud lines stale at Connect (§4); explain "Digital assistant" and hide or annotate it when no assistant
service is present (§7); the wishes 7/11/12 answers (§8).

## 12. Open questions

- 🔴 Why Android did not re-create the ACL after an ADR-016 drop with the lid open (§6), unlike `CAP-062`.
- 🔴 What makes a bud lying in a slot "not charging" (§4); what field 3 and 7.3 of the stream mean (constant 0 here, 72/72).
- 🟡 Settable `00` = no bud worn (§3, AY-3 not run); conversation detection = AVRCP PAUSE/PLAY (§7); the Digital-assistant hold goes over GSND to the
  Google app (§7); the ANC mode changes without an app `Set` are press-and-hold gestures (§3).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-063-2026-09-27_15-57-33_16-25-03-Group_AY/CAP-063-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-063-2026-09-27_15-57-33_16-25-03-Group_AY/CAP-063-FINDINGS
