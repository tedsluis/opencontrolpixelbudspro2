# Findings: `CAP-079` (Group BO — round 2: release candidate 1.4.0 in the secondary user without Google Play and in the Owner user with it, and the experiment build 1.4.0-exp.1 with the Dosimeter requests)

**Status:** analyzed 2026-10-18 by `ai-sessions/0099` (event timeline: CAP-079-EVENT-NOTES.md). Labels: 🟢 FACT · 🟡 HYPOTHESIS · ⚪ ASSUMPTION · 🔴 OPEN
QUESTION (`PROJECT_RULES.md` §1). Every proposal in §17 is a proposal — nothing here is promoted (`AGENTS.md` §6).

**Log letters** (as in the EVENT-NOTES): **H1** CAP-079-btsnoop_hci1.log (part 1, user 10, release), **L1** hci1.log.last (from frame 9,435), **L2**
hci2.log.last, **H2** hci3.log.last (part 2, user 0, release), **H3** hci4.log.last (part 3, user 10, experiment), **H4** hci4.log (part 4, user 0,
experiment); exports **E1 … E4**; app logcats **A1 … A4** (UTC); full logcats **L-1/2/3**; system logs **Y2/Y4** (UTC). Frame numbers are per log.

**Tools** (rule 4a; committed: `scripts/pwhdlc_crc_table.py`, `scripts/message_stream_claims.py`, `scripts/rpc_request_answers.py`,
`scripts/ring_tone_windows.py`, `scripts/pwrpc_decode.py`):
`python3 scripts/pwhdlc_crc_table.py <log> 0x000b > t.tsv` (every pw_hdlc frame, CRC-32, decoded RpcPacket and raw hex);
`python3 scripts/message_stream_claims.py <log> 0x000b` (the DLCI 0x04/0x05 claims and their owner);
`python3 scripts/rpc_request_answers.py t.tsv ReadSetting WriteSetting` (each request, its answer, the pushes within 1 s);
`tshark -r <log> -Y "bthci_acl.chandle==0x000b && (btrfcomm.dlci==4||btrfcomm.dlci==5)" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e
btrfcomm.frame_type -e data.data` split as `[Group][Code][Len:2 BE][Value]` (the Message Stream messages of §5).

## 0. Builds, users, parts, logs, privacy

- 🟢 **Builds.** The APKs on the phone (`pm path --user 0/10`, one path per package; pulled and hashed 2026-10-18, CAP-079-adb-checks-0099.log) are the two
  APKs built at `e2b1c06`: release 1.4.0 `2f75ad86…511172`, experiment 1.4.0-exp.1 `5280e7b3…071ab11e`. Info tab on film: "1.4.0, build e2b1c06
  (2026-10-17)" (s072815 41, s074935 51) and "1.4.0-exp.1, build e2b1c06" (s082214 78) — not "-dirty". Installed for users 0 and 10 at 06:41:56 / 06:42:10
  (release) and 06:42:21 / 06:42:28 (experiment): L-1 `installation completed` and `ShortcutService: replacing package … userId=0` / `userId=10` after each.
- 🟢 **Users and parts.** `am_switch_user` (L): 10 at 07:01:07.813, 0 at 07:36:34.212, 10 at 08:00:17.895, 0 at 08:30:44.705. Part 1 = user 10 release (E1,
  A1 `userType: full.secondary`, package `…opencontrolpixelbuds:10400`), part 2 = user 0 release (E2, A2), part 3 = user 10 experiment (E3, A3), part 4 = user 0
  experiment (E4, A4). Play's Message Stream marker `03 08 00 02 01 25` on handle 0x000b: H1 0, H2 7, H3 0, H4 3 (`tshark … -Y "bthci_acl.chandle==0x000b && frame
  contains 03:08:00:02:01:25"`, exit 0; positive control `CAP-076-btsnoop_hci1.log` 18) — Play holds DLCI 0x04 only in user 0.
- 🟢 **The P0 check** in the shell log used `grep -i -E "gms|vending"`; in user 10 it matched only GrapheneOS's three `app.grapheneos.gmscompat*` packages (exit 0) —
  not a sign of Play; in user 0 it also listed `com.android.vending` and `com.google.android.gms`.
- 🟢 **Logs and nesting** — the EVENT-NOTES' pre-flight (`cmp -n`): analysing L1 (from 9,435), H1, L2, H2, H3, H4 covers all eight HCI files. Not in any HCI
  file: 07:30:22 – 07:36:38 (H1 was pulled while user 10 ran). L1's tail after `CAP-078` is that run's user-0 ACL to 18:15:06 (0x13), two ACLs of < 2 s at 21:12, and
  another device's LE link overnight (handle 0x0041, a vendor GATT service — counted, not analysed).
- 🟡 **The unreadable camera film** c070731: no `moov` box. Its video was recovered from a copy (scratch script: the AVC NAL units of `mdat` with c070945's SPS/PPS;
  the positive control on c070945's first 96,461,054 bytes recovered 3,458 of the 3,459 frames starting there, no decoder error); 3,468 frames, ≈ 07:07:33 – 07:09:31
  by the camera's burned-in clock. Audio not recoverable (raw AAC frames carry no lengths). The original is unmodified (SHA-256 `c8169e41…` before and after).
  c080913's audio track has no sample description (`ffmpeg -map 0:a` → "no decoder found for: none", exit 234); s072815 has no audio stream.
- **Privacy.** Films: Wi-Fi name, notifications (one with an account address), a web search, media titles, serial numbers on the Info tab — not transcribed.
  Serials in text: first 4 + last 2. Extracts: MACs masked (`XX:XX:XX:XX:…`), 0 e-mail, 0 SSID (`grep -c`). The audio holds no speech of the maintainer
  (their statement; the only speech-like segment, 08:16:53 – 08:17:11, is on the phone's own microphone and is not attributed).
- 🟢 **Health.** Neither package: FATAL 0, ANR 0 (A1 … A4; L over 10-18 06–08). FATAL/`am_crash` in L-3 only for another app's web view (08:16:31). `am_wtf`
  "Background started FGS: Allowed … opencontrolpixelbuds … uidState: TOP" ×35 — one per session start, informational. CloseGuard: A1 ×1, A2 ×5 "A resource failed to
  call close." next to `BluetoothSocket: close() … Already closed` (release); experiment StrictMode ×4 = `BluetoothLeAudio.<init>` via `OsConnectionObserver` ×3 and a
  socket's `ParcelFileDescriptor` ×1 — the two framework sources of `ARCHITECTURE.md` §12. `killBluetoothProcess` 07:41:36.775 = the Bluetooth off on film
  (s074935 9), not a deadlock.

## 1. ACLs and the Message Stream claims — ADR-068's "only Find"

🟢 **Every OpenControl claim of DLCI 0x04 in the run was a Find action** (`scripts/message_stream_claims.py <log> 0x000b`):

| Part | App claims | Owner (first message) | Lives | Other claims |
|---|---|---|---|---|
| 1 (H1, user 10) | 4: 3837 07:21:19.190, 3942 07:22:04.651, 4285 07:22:56.465, 4523 07:23:51.986 | `04 01 00 01` (Ring) ×4 | 21.7, 13.0 (Buds `DISC`), 22.5, 21.7 s | none |
| 2 (H2, user 0) | 4: 4566 07:47:27.625, 4860 07:48:06.660, 5367 07:48:41.369, 5625 07:49:07.688 | Ring ×3, Stop ×1 | 4.77, 4.81, 4.75 (cut from the phone side = Play's connect), 1.65 s | Play ×7 |
| 3 (H3, user 10) | 0 | — | — | none |
| 4 (H4, user 0) | 0 | — | — | Play ×3 |

No claim at any Connect or re-open (part 1: 15 MAESTRO opens, part 2: 5), at any Refresh battery (4), mode tap (26 release writes) or tile tap (11). The
refuted-if of BO-1/BO-2/BO-3/BO-18 ("DLCI 0x04 opened") did not occur.

## 2. The Connect sequence

🟢 Every release session sent, on the announced channel: `ReadSetting 4:16`, the twelve settings reads (2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29),
`SubscribeRuntimeInfo`, `SubscribeToSettingsChanges`, `ReadSetting 4:13`, `GetHardwareInfo` — 1.3.1's sequence minus the snapshot claim (H1 14 sessions, e.g.
261 … 323; H2 4 complete + one closed by the Buds after 1.3 s). Every request frame equals the reference table of the skeleton where it has one (EVENT-NOTES
appendix): `ReadSetting 4:13` ch 19 `…24 c2 39 47 7e` (×20) / ch 21 `…9e 82 ab ac 7e` (×8), `SubscribeRuntimeInfo` ch 19 `…60 2d 65 a9 7e` (`pwhdlc_crc_table.py`,
the raw column). The channel followed the worn bud (`PROTOCOL.md` §2.2a L-1): only the Right worn → 21 (H1 2123 07:12:16.852), none → 19 (2435), only the Right
in → 21 (3286), both → 19 (3374).

## 3. The mode through setting 13 (ADR-067) — `ANC-001` … `ANC-004`, `ANC-007`

### 3.1 Every release write
🟢 **26 `WriteSetting 4:{13:n}` by the release app** (H1 24, H2 2; `rpc_request_answers.py t.tsv WriteSetting`): **22 RESPONSE OK, 4 FAILED_PRECONDITION**.

| Condition | Writes | Answer | Evidence |
|---|---|---|---|
| Both worn, tab (BO-3) | 13:1, 2, 3, 4 ch 19 | OK ×4 | H1 1837/1844, 1880/1887, 1934/1941, 1976/1983 |
| Right worn only (BO-4) | 13:2, 4, 1, 3 ch 21 | OK ×4 | H1 2183 … 2362 |
| None worn (BO-5) | 13:2 | **9** | H1 2495 → 2501 |
| None worn, **in-ear detection off** (BO-6) | 13:2, 1, 3, 4 | OK ×4 | H1 2850 … 2977 |
| None worn, tile (BO-7) | 13:1 ×3 | **9** ×3 | H1 3219/3222, 3228/3230, 3243/3245 |
| Both worn, tile | 13:1, 2, 3, 4 | OK ×4 | H1 3431 … 3461 |
| Off unticked, tile (BO-8) | 13:2, 3, 4, 2 | OK ×4 | H1 3653 … 3683 |
| User 0, tab (BO-18) | 13:4, 3 ch 19 | OK ×2 | H2 3756/3766, 3844/3849 |

🟢 Every OK was pushed back as `4:{13:n}` on the write's channel (in the same RFCOMM frame as the RESPONSE, after it) and on the other channel 2–87 ms after the RESPONSE (22 of 22); no
push after a status 9. 🟢 E1/E2: "Noise-control mode written (channel N): <mode>" ×22 and "… not written: … (FAILED_PRECONDITION — no bud worn?)" ×4, each with its
frame; the pushes of the own write logged "(set_by_app)" (60 + 6). 🟢 The four first-ever channel-19 forms (13:2, 3, 4 on ch 19) were answered OK; their bytes
(`…42 d8 d5 f5`, `…d4 e8 d2 82`, `…77 7d 5d b6 1c`) pass the CRC.
🟢 **In-ear detection off makes the mode settable with no bud worn:** the write `4:{2:0}` was followed by the push 13 = 4 (H1 2736 → 2742) and `{2:1}` by 13 = 0
(3118 → 3128, 3143 → 3178) — with both buds on the table (camera 07:14:13 – 07:15:17).
🟢 **The hold list (BO-8):** with Off unticked (`4:{12:{1:1 2:0 3:1 4:1}}` H1 3528 OK) the tile stepped NC → Transparency → Adaptive → NC, never `13:1`; the tab showed Off
disabled with "Only the modes ticked under Controls → …" (s072815 614).
🟢 **No "unavailable" frame between two tile states** at 1 fps and in the 10-fps check of s072815 535 – 551 (P2); the tile showed "Tap to switch" once after a
session re-open (s072815 520), not between two modes.

### 3.2 The holds and the cause (BO-9, `TOUCH-007`)
🟢 23 press-and-holds (part 1 13: H1 3700 … 3789; part 2 10: H2 3986 … 4162) — each a push on both channels, each shown "Changed by the Buds" (E1/E2
"(changed_by_buds)" + "(unchanged)" for the second channel), the right bud's holds with the head on the left of the frame (camera 07:19:27 – 07:20:11, 07:45:52 –
07:46:11). With Off unticked the cycle never contained 1 (part 1); with all ticked it ran 2, 1, 4, 3 (part 2).

### 3.3 Defects seen on screen (proposals in §17)
- 🟢 **A stale error banner.** After a status 9 (or a Refresh with 13 = 0) the red "The Buds don't allow changing noise control right now …" stays while the Buds
  push a settable mode: s072815 382 (07:14:17, Adaptive pushed at 07:14:14.174) until a Refresh 07:14:22; s074935 173 (07:44:20, NC shown). Code: `_ancError` is
  cleared only by an OK write or a successful Refresh (`BudsRepositoryImpl.kt:1011, 1050`), not by `applyAncState`.
- 🟢 **Text of 1.3.x.** The ANC card says "… Tapping a mode checks again first." (`AncScreen.kt:216`; s072815 329) — 1.4.0 writes without a check; the battery (i)
  says "Left/Right %: read from the Buds when the app last reached their Message Stream (at Connect, Refresh and each ANC / Find action)." (`ConnectionScreen.kt:341`;
  s072815 73) — since ADR-068 they come from runtime info; Find's (i) "Each action briefly claims …" (`ConnectionBanner.kt:85`) — a ring now holds up to 20 s.

## 4. The battery (ADR-068) — `BATT-004`

🟢 Left/Right on the card equal runtime-info 6.2/6.3 field 1 in every packet compared (e.g. H1 1137 `6:{2:{1:100 2:1} 3:{1:100 2:1}}` → "100 %" ×2, s072815 73;
H2 2699 `6:{1:{1:77 2:1} 2:{1:100 2:2} 3:{1:100 2:2}}` → Case 77 %, both charging, s074935 107). 🟢 **Refresh battery ×4** (H1 1134, 1336, 1437; H2 2674): one
`SubscribeRuntimeInfo` each, a packet 49–86 ms later, no claim, no "Battery refresh:" failure line in any export. 🟢 An absent entry is shown "last seen" with its
time (Case, s072815 73: "77 % last seen 07:08:20").

## 5. The ring (ADR-069/070) — `FIND-001`, `FIND-002`, `FIND-004`, `FIND-005`

### 5.1 What the app sent and acknowledged
🟢 Every Ring was ACKed `ff 01 00 03 04 01 00` and followed by the Buds' status equal to the command (`…02`, `…01`, `…03`); **every status that arrived while
DLCI 0x04 was open was ACKed `ff 01 00 02 04 01` by the app** — 9 of 9 (H1 3862, 3967, 4310, 4374, 4546; H2 4597, 4885, 5392, 5649), 3–7 ms after it; E1/E2
"Ring status acknowledged" ×5/×4. 🟢 **Ring both** (`04 01 00 01 03`): ACK + status 03 in both users (H1 4296 → 4306/4308; H2 5378 → 5388/5390); 🟡 both buds
sounding — one tone track, the buds not separately audible.

### 5.2 "Stopped on the bud" never appeared — the hold is too short (user 10)
| Ring | Sent | Touch (camera) | Tone (3.14 kHz) | Hold ended (E1) | Stop status on the wire |
|---|---|---|---|---|---|
| Left | H1 3848 07:21:19.360 | 07:21:38 – 07:21:48 | 07:21:27.0 – 07:21:43.3 | "no stop report within 20 s" 07:21:39.403, released 07:21:40.915 | none while open |
| Right | H1 3953 07:22:04.780 | 07:22:12 – 07:22:26 | 07:22:09.4 – 07:22:25.4 | — (session lost 07:22:17.693: Notify `e8 e8 80`, Buds `DISC` of 0x04 and MAESTRO) | none |
| Both | H1 4296 07:22:56.602 | 07:23:11 (brief) | 07:23:01.2 – 07:23:14.5 | "no stop report within 20 s" 07:23:16.622 | `…00` 4372 07:23:17.448 = the answer to the app's Stop 4363 07:23:16.971 |
| Left | H1 4535 07:23:52.116 | none | **07:23:56.7 – 07:24:55.0** | "no stop report within 20 s" 07:24:12.142 | none while open |

🟢 With nothing touched the Buds rang ≈ 63 s (the last pulse ends 62.9 s after the Ring; 21 pulses ≈ 3 s apart) — **the 20-s hold (`RING_HOLD_MAX_MS`) ended
first, so the Buds' stop status had no recipient**. 🟢 In BO-10 the touch began 18.6 s after the Ring and the tone ended at 07:21:43.3 — after the hold had ended.
🟡 The Buds' own ring time-out without a timeout byte is ≈ 60 s (one sample). The Device Action page (fetched 2026-10-18, "Last updated 2025-08-12 UTC"): "The second
byte in additional data, if present, represents the timeout in seconds. … `0x013C` would be passed" and "Ringing should continue until an additional action is
received directing a stop, or a timeout value has passed" — no default is given. 🟡 So `CAP-078`'s stops at 14.8–17.1 s (which ADR-069 item 3 cites) were most
likely the maintainer's touches, not a time-out (`CAP-078-FINDINGS.md` §4.1 left the cause open). The screen kept "Ringing: Left earbud — tap Stop to end it." after
the tone ended until the Disconnect (s072815 1032, 07:25:06) — refuted-if of BO-10 ("Stopped on the bud missing after the status 00") not literally met (no status reached the app),
the expected screen not met. 🟢 After the app's own Stop: status 00 ACKed and no "Stopped on the bud" (BO-11's refuted-if not met).
🟡 The tone became detectable 2.6–7.6 s after the Ring (camera clock ± 1 s; the specification: "ramped from a low volume to max volume over time") — `CAP-078` 5.9–6.3 s.

### 5.3 User 0: Play cuts the claim (BO-19)
🟢 3 of 3 rings: attempt 1 refused (E2 "connect failed after 287/177/342 ms (attempt 1/3)"; Y2 "already at opened state" 05:48:40.701, 05:48:46.122 UTC), attempt 2
0.63–0.73 s later; the claim cut 4.75–4.81 s after its `SABM` from the phone side; E2 "Ring: hold ended — the claim was closed by another client" ×3; **no re-claim**
(ADR-069 item 4 ✓); Play re-claimed 2.5 s later and **received and ACKed the stop status** after a touch (H2 4785 → 4788 07:48:00.492; 5059 → 5061 07:48:24.599). The
screen kept "Ringing: Left earbud" (s074935 361 – 400) — the CHANGELOG's Known issue, confirmed. Stop (07:49:04 on film) → a new claim (attempt 2 kills Play's socket,
Y2 05:49:07.094), status 00 ACKed by the app, release after 1.65 s.

## 6. P1, P2, the mode line (P6)
🟢 P2: §3.1. 🟢 The Connection tab's "Noise control: <mode>" followed every change on film (s072815 14, 29, 60; s074935 187). P1 ("EQ written") not exercised —
no EQ write in the run (0 `WriteSetting 4:{16:…}`).

## 7. TalkBack (BO-14) — not exercised
🟢 TalkBack was never on: L "Not exempting com.android.talkback - not an active AccessibilityService" in both users (07:01:14, 07:36:37) and no focus frame or
TalkBack caption on any film. A93-APP-07 stays unit-tested only.

## 8. A hold during a Connect (BO-15) — A93-APP-04
🟢 **Exercised once:** H1 4802 `ReadSetting 4:13` 07:26:21.603 → frame 4806 07:26:21.662 holds, in this order, the push `4:{13:3}` (ch 19), a runtime-info packet and the
RESPONSE `4:{13:3}`; E1 07:26:21.665 "from the settings stream (changed_by_buds)", 07:26:21.666 "from the mode read (read)", 07:26:21.667 "Noise-control mode read
(channel 19)" — the read was not ended by the push and did not fail (1.3.1's fix holds). The hold itself is not on film (the hand out of frame); the four
values 3, 2, 4, 3 at 07:26:21 – 38 follow the hold order.

## 9. A Disconnect during a Connect (BO-16) — not hit
🟡 The Disconnect taps (07:26:23, 07:26:27, 07:26:33, 07:27:19, 07:27:20) all came after Ready (E1 "Session ended by the user's Disconnect tap (was Ready)"); the
shortest session was 0.59 s (H1 SABM 5573 07:27:19.885 → phone `DISC` 5608 07:27:20.474). 0 "Connect finished after a Disconnect tap" lines (`grep -c`, positive
control: none possible). A68-APP-13 (a) remains unit-tested only. 🔴 At 07:26:49.765 a Disconnect's `DISC` got no `UA` and the ACL ended 16 ms later with reason
**0x08** (`HCI_ERR_CONNECTION_TOUT`, Android `hci_error_code.h`, fetched 2026-10-18) — the only connection timeout of the run; cause not visible.

## 10. Experiment A with a mode unticked (BO-21, ADR-067's Note)
🟢 **The Buds accept and apply a mode that is not ticked in field 12.** Field 12 `4:{12:{1:1 2:1 3:1 4:0}}` (Adaptive unticked) H3 2365 08:12:26.465 → OK; then
`WriteSetting 4:{13:4}` H3 2426 08:12:38.952 → RESPONSE OK 2433, push 13 = 4 on ch 19 and 21 (2430, 2434) — the mode became Adaptive; again 2488 → OK (no change).
So the app's refusal (`AncModeNotInHoldList`) is the app's policy, not a firmware rule. With all ticked: 13 = 4, 2, 3, 1 OK (H3 2213 … 2356). E3 "Experiment A:
RESPONSE OK" ×6.

## 11. The Dosimeter (F1 – F4, BO-22 … BO-28; draft ADR-060) — `WELL-001` … `WELL-003`

Raw values only — no unit, no dose, no health reading.
- 🟢 **F1 `SetWallclock`** H3 2559 08:13:11.642 `1:1792303991632` (= the phone's clock in E3, 10 ms before the frame) → RESPONSE OK 2563 08:13:11.720 (raw
  `7e80a303080110131dea71de7d5e254eed3b6712a9053e7e`); H4 4591 → 4593 likewise. **The app reported "Experiment F1 failed: Timeout" both times** (E3 08:13:13.153,
  E4 08:36:19.057) and listed the answer as an unidentified frame: `routeMaestro` returns an `RpcResult` only for ReadSetting/WriteSetting/GetHardwareInfo
  (`CodecRouter.kt:328`), so the runner's wait never matched. An experiment-build defect; the release has no `SetWallclock`.
- 🟢 **The Buds' clock appears after F1:** runtime-info and announcement field 2 (epoch ms) is absent in every packet before the first `SetWallclock` (H1 81, H2 36,
  L1 64, H3 28) and present in every one after (H3 11, H4 27); e.g. H3 2596 08:13:22.765 `2:1792304002562` = 08:13:22.562 local.
- 🟢 **F3 `FetchDailySummaries`** answered on both channels (8 of 8): field 1 = 475 (`CAP-058`, 2026-10-05: 462 — +13 days for 13 calendar days; 🟡 day 0 =
  2025-06-30). Day 475 (field 6), ch 19 / ch 21: 724,528.31 / 49,913,470,976 (08:13:28 / 43) → 2,233,065,216 / 52,201,500,672 (08:20:09 / 14) → 2,263,590,144 /
  52,250,238,976 (08:38 / 08:39) → 3,794,701,824 / 53,847,781,376 (08:41:34 / 40); days 468 – 474 unchanged; field 5 rose with day 475. 🟡 Channel 19's day 474
  (62,259,949,568) against channel 21's 38,178,200, and ch 19's tiny day 475 at 08:13:28, fit **one bud starting a new day at F1** (the left core's clock set at
  08:13:11) — not settled, no summary was taken before F1 (`WELL-003` partly).
- 🟢 **F2 `SubscribeToLiveDb`** (29 requests): packets carry field 2 only (f32). **Rate:** ≈ 60 per minute per channel while audio played (part 3 08:18 – 08:21,
  part 4 08:36 – 08:41), 3 – 10 per minute in silence (H3 08:14 – 08:16). So part 4's "120 per minute" against part 3's 6 – 13 is music, not a double
  subscription. **Values:** silence 0.00 (some 2.5 – 6.3); music at four media-volume settings (panels on film 08:18:05 – 10, 08:18:27 – 33, 08:19:17 – 22 up,
  08:19:45 – 51 down): medians 1.4 – 1.6 × 10⁶ → 1.9 – 2.0 × 10⁴ → 3.2 – 4.4 × 10⁸ → 3.3 – 4.3 × 10⁹ → 1.4 – 2.1 × 10⁶ on both channels, while the program level
  recorded by the screen recorder stayed −14 … −17 dBFS. As `round(10·log10(v))` (the APK's display rule, `hiw.java:32–37`) the steps are ≈ 62 → 43 → 86 → 96 → 62.
  🟡 The value follows the level the buds play, not the room: a speech-like room sound (08:16:53 – 08:17:11, screen-film microphone) left it at 0 – 6. **No
  sound-level meter was in frame (P7 not done): `WELL-002` as specified is not observed;** draft ADR-060's 🟡 (i) is not settled.
- 🟢 **The live stream outlives its session.** In part 4 the Buds sent live packets on ch 21 and 19 from 08:31:19.235, 0.48 s after the first MAESTRO `SABM` of a new
  ACL in the other user, with **no** `SubscribeToLiveDb` in that ACL (the first one is H4 4695 08:36:28.063); the subscriptions were made in part 3 (user 10) before
  the session close (08:30:18), the ACL loss (08:30:20) and the user switch. 🟢 The settings stream did the same (§14).
- 🟢 **F4 `WriteSetting 4:{21:b}`** (`22 03 a8 01 b`): on **channel 19** (the session's channel) on/off → RESPONSE OK and a push `4:{21:b}` on ch 19 (H3 4278/4280/4281,
  4368/4371; H4 likewise) — the never-captured channel-19 form works; on **channel 21** → **FAILED_PRECONDITION** ×4 (H3 4415 → 4418, 4497 → 4499; E4 08:42:04, 08:42:10).
  🟡 A write on the bud core that does not host the session is refused, while reads and streams work on both (F2/F3 on 21: OK).
- **Cost.** H3 686 + H4 1,021 live packets (21 B each on the wire); at 60/min/channel ≈ 2 packets/s while audio plays.

## 12. Sessions, losses, re-opens
🟢 Part 1: 7 Buds-side closes of MAESTRO (H1 971, 2093, 2402, 3253, 3343, 4009, 4104), each re-opened 1.5 s later except 4104 (E1 "Automatic re-open skipped: the session
was lost 9173 ms after an automatic re-open"); the cause lines stayed "(provisional)" in every case (E1 ×14 — no final line is logged after 1 s). Each close
fits a bud event on film (07:12:14 Left out, 07:13:19 Right out, 07:16:32 Right in, 07:16:45 Left in, 07:22:17 the rung bud touched, 07:22:28 laid back) except
07:08:58.9 (buds worn, no event visible). Parts 3/4: LINK_BACK re-open at app start (E3 08:04:15.344, E4 08:31:18.548).

## 13. Playback and audio
🟢 A2DP (AVDTP) and AVRCP on handle 0x000b in every part (AVRCP H1 42, H2 15, H3 118, H4 45; `-Y "bthci_acl.chandle==0x000b && (btavctp||btavrcp)"`, positive control
`CAP-029` 26); HFP opened by the Buds on DLCI 0x09 and by the phone on DLCI 0x0c (server channel 6, `+CIND` at H4 379) — `AT+` frames 85/43/72/57. Ring tone: §5.

## 14. Unexpected findings
1. 🟢 **Server-streaming subscriptions survive the session.** Settings pushes arrived in a MAESTRO session before its own `SubscribeToSettingsChanges` (H1 4877/4878
   07:26:26.172 — the previous session closed by the phone at 07:26:23.368; H2 5110 07:48:27.732 in a session the Buds closed before any request) and live-dB
   packets in a new ACL in the other user (§11). 🟡 The Buds keep the pw_rpc stream calls per channel id across RFCOMM closes and ACL losses.
2. 🟢 **The 20-s ring hold is shorter than the ring** (§5.2).
3. 🟢 **F1 reported as a time-out** although answered (§11).
4. 🟢 **Field 21 is refused on the non-hosting channel** (§11).
5. 🟢 **A mode outside the hold list is accepted** (§10).
6. 🟢 **The stale `ancError` banner and three 1.3.x texts** (§3.3).
7. 🟢 **Every Dosimeter packet and the `SetWallclock` answer appear in Debug → "Unidentified frames"** (capped at 200; s082214 255 … 567) — the experiment's
   traffic is not classified by the release codec (expected for a non-release service, noisy).
8. 🟢 The release app's tile process runs beside the experiment app (L `am_proc_start` "bound-service" u10 08:09:03, 08:22:26; u0 08:31:14) — no second RFCOMM
   connection on the wire (P6 effectively held).
9. 🟢 Settings 3 and 6 never written; no request outside the inventory (`pwhdlc_crc_table.py`, the REQUEST rows of all four logs): GetHardwareInfo, ReadSetting
   (2, 4, 7, 11, 12, 13, 15, 16, 17, 19, 22, 27, 28, 29), SubscribeRuntimeInfo, SubscribeToSettingsChanges, WriteSetting (2, 12, 13, 21), SetWallclock, Dosimeter
   FetchDailySummaries / SubscribeToLiveDb. CRC failures 0. No LE link to the Buds. DLCI 0x08/0x0a opened only by the phone in user 0 (Google's GSND clients).
10. 🔴 One ACL connection timeout (0x08) at a Disconnect tap (§9).

## 15. The skeleton's expectations, applied literally (steps run)
| Step | Refuted if … | Result |
|---|---|---|
| BO-1 | DLCI 0x04 opened at Connect / L/R empty with 6.2/6.3 present | not refuted |
| BO-2 | DLCI 0x04 opened | not refuted |
| BO-3 | a tap opens DLCI 0x04 or the mode does not change with OK | not refuted |
| BO-5 | the app shows the new mode | not refuted (banner, mode kept) |
| BO-7 | "unavailable" between two states | not refuted |
| BO-8 | `13:1` sent while Off unticked | not refuted |
| BO-10 | a status not ACKed while open / "Stopped on the bud" missing after status 00 | not refuted literally; **expected screen not reached** (no status 00 within the hold) |
| BO-11 | "Stopped on the bud" after the app's own Stop | not refuted |
| BO-12 | one bud only or a NAK | not refuted (🟡 "both" by status only) |
| BO-14 | a switch read without its label | not run |
| BO-15 | the read fails while its RESPONSE came | not refuted |
| BO-16 | Connected shown after the Disconnect tap | not refuted; the race not hit |
| BO-18 | the app opens DLCI 0x04 for a mode change | not refuted |
| BO-19 | the app re-claims after the cut | not refuted |
| BO-24 | the packets do not change with the level | not refuted for the playback level; the meter part not run |

## 16. Counts
Release writes 13: 26 (22 OK, 4 status 9); field 12: 4 (release) + 2 (experiment); field 2: 4; experiment A: 6 OK; holds: 23; release Connect/re-open sessions: 20;
Refresh battery: 4; app claims of DLCI 0x04: 8 (all Find); ring statuses ACKed by the app: 9 of 9; stop statuses to Play: 2; SetWallclock: 2 (OK, app "Timeout");
F3: 8; F2: 29; live packets: 1,707; F4: 8 (4 OK on 19, 4 status 9 on 21); pw_hdlc frames: 3,046 (+314 L1), CRC bad 0; films: 9 (1 recovered); changed seconds
viewed: 646 screen + 1,741 camera.

## 17. Proposals (not accepted — for the maintainer, `AGENTS.md` §6)

**Verdict proposal for 1.4.0.** ADR-067 (26 writes, all as designed; status 9 handled; the hold list respected), ADR-068 (no claim except Find; L/R and
Refresh from runtime info) and ADR-070 (status 03 in both users) behave as decided. ADR-069 works on the wire (every status ACKed; no re-claim after Play's cut)
but its user-visible half — "Stopped on the bud" — **never appeared** in user 10, because the 20-s hold ends before a touch made after ≈ 20 s or the Buds'
≈ 60-s time-out. Three texts are 1.3.x and one banner goes stale. Options: **(A) fix first** (recommended) — hold until the stop status or ≈ 65 s, clear
`ancError` when a push makes the mode settable, correct the three texts; a short re-run of BO-10/BO-13 (and BO-14/BO-16, not run); **(B) release with Known
issues** — "Stopped on the bud" only for a stop within 20 s; the texts; the banner; **(C)** release as built — not recommended (a feature that never showed). **The maintainer's choice (chat 2026-10-18, `AskUserQuestion` "1.4.0"): (B) release
with Known issues**, and `ai-sessions/0100` for the fixes; the `PROTOCOL.md` notes and the ADR-060 Update were approved in the same chat and are written.

**PROTOCOL.md (draft texts in the RESULT's checkpoint):** §4.1 🟢 notes — the 1.4.0 write path on hardware (22 OK / 4 status 9, both users, pushed on both cores);
in-ear detection off makes setting 13 settable with no bud worn; a mode outside field 12 is accepted. §4.4 🟡 note — the Buds ring ≈ 60 s without a timeout byte
(one untouched sample, 63 s); `CAP-078`'s 15-s stops not a time-out. §4.3 Option F 🟢 note — field 2 (epoch ms) only after `SetWallclock`. §2.2a/§6 🟢 note —
stream subscriptions outlive the MAESTRO session, the ACL and a user switch. §4.5.8a 🟢 note — field 21 on channel 19 OK, on the non-hosting channel status 9.

**ADR-060 (draft) Update proposal:** (iv) settled 🟢 (the Buds answer OpenControl's Dosimeter requests); (v) settled 🟢 (the channel-19 form, on the hosting
channel); (vi) the cost is ≈ 1 packet/s per channel while audio plays, 3 – 10/min in silence, and the subscription outlives the session; (vii) `SetWallclock` is
answered and puts the clock into runtime info; (i) still 🟡 (no meter; the value follows the playback level); (iii) day 0 = 2025-06-30 🟡.

**ADR-069 Update proposal** (with fix A): `RING_HOLD_MAX_MS` from 20 s to ≈ 65 s (or a timeout byte in the Ring), the reason being §5.2.

**Next session proposal:** `ai-sessions/0100` FEATURE "1.4.0 fixes before release" (the hold, the banner, the three texts, the experiment's `SetWallclock` routing;
tests with this capture's bytes) and a short hardware run, the next capture (BO-10, BO-13, BO-14, BO-16, WELL-002 with a meter).

## 18. Open questions
- 🔴 Why the ACL timed out (0x08) at the Disconnect tap of 07:26:49.
- 🟡 The Buds' default ring time-out (one sample ≈ 60 s); whether a timeout byte is honoured.
- 🟡 Whether a touch stops the ring at once (BO-10's tone ended ≈ 4 – 5 s after the touch began).
- 🟡 What the live-dB value is (a level of the buds' output — no meter); why one bud's day index advanced at F1.
- 🟡 Why field 21 is refused on the non-hosting core while F2/F3 work there.
- 🔴 How long a stream subscription survives without a client (it survived ≈ 30 s and a user switch).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-079-2026-10-18_07-07-31_08-42-33-Group_BO/CAP-079-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-079-2026-10-18_07-07-31_08-42-33-Group_BO/CAP-079-FINDINGS
