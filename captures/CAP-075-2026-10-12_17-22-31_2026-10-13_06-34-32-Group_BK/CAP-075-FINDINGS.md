# Findings: `CAP-075` (Group BK — 1.2.1, build `31ee183`, the "Noise-control channel" switch of ADR-062 in both GrapheneOS users: what each switch position did without Google Play (set 1) and with it (set 2), measured against `CAP-073` and `CAP-074`)

**Analysed by `ai-sessions/0088` (2026-10-13).** Companion: `CAP-075-EVENT-NOTES.md` (the timeline, the steps as run, the Test-IDs). Labels per
`PROJECT_RULES.md` §1: 🟢 FACT (a frame, a log line or a film second), 🟡 HYPOTHESIS, ⚪ ASSUMPTION, ❓ OPEN QUESTION. The ADR-062 Update (§9), the
`PROTOCOL.md` note (§5) and the verdict (§11) were **approved by the maintainer in chat 2026-10-13** (`ai-sessions/0088`, Phase D) and written; the
proposals (§9) are proposals (`AGENTS.md` §6). Frame numbers are per log:
"H1L" = CAP-075-btsnoop_hci1.log.last, "H1" = CAP-075-btsnoop_hci1.log, "H2L" = CAP-075-btsnoop_hci2.log.last, "H2" = CAP-075-btsnoop_hci2.log; "E1"/"E2" the
two exports; "L" = CAP-075-logcat-all.txt (local time), "X" = CAP-075-System-log-571517c9248e.txt (UTC). Every `tshark` filter was run on the Buds' ACL
handle (`bthci_acl.chandle==0x000b`; the Connection Complete for the Buds' address with that handle: H1L 501, H1 151, H2L 18119, H2 469); every negative
below carries its command, its exit status and a positive control (`AGENTS.md` §13 step 8).

**In short (🟢 unless marked).** Without Play the switch changes nothing and the hold works as in `CAP-073`: 41 of 41 press-and-holds on screen within a second,
in both positions, no hint, no re-claim, no `DISC` while the tab was shown. With Play, "Show a hint" shows the hint with the cut's time and never re-claims; the
app's own claim lives only until Play's next connect, which is **5.33–5.82 s after Play lost the channel**, not a fixed time after the app's claim (0.42 s and
1.2 s for two of them). "Take it back" ran three chains of exactly three re-claims (3 000 / 6 000 / 12 000 ms after the cut, as scheduled); **every re-claim
collided**: Play's retry came 2.2–2.8 s after each cut, so attempt 1 was refused by the Bluetooth stack ("already at opened state") and attempt 2, 0.9–1.5 s
later, connected by killing Play's socket; every re-claimed hold was cut again 5.3–5.8 s after that. The app held the channel **≈ 43 % of each 44-s chain**,
then nothing until a tap. Of the 31 holds made in set 2's film, 8 were on screen within a second, 16 never. The back-off buys nothing: Play's timers did not
change over 15 cycles. The two best next steps for the Play user are a constant 3-s step with a longer chain (§9 P1) and a way to restart the chain from the
hint (§9 P2). No crash, no write, no Safe Mode, no session lost by the app (§11).

## 0. Build, install, users, logs and privacy (🟢 FACT unless marked)

- **Build.** "App: 1.2.1, build 31ee183 (2026-10-12)", no "-dirty", on film in both users (film 1 s187–189, film 2 s24–36) = B1's build commit
  (`CAP-075-EVENT-NOTES.md` Log Metadata). The installed APK was **not pulled** (⚪ as `CAP-074` §0: the hash of what ran is the Info line's hash only).
- **Install.** The update over 1.2.0 `5b4d5db` is on film 1 (s0–4 the 1.2.0 Info; s166–168 Android's "Updaten…" 17:25:17.9–19.9; s187 the 1.2.1 Info); the
  app's data survived (Debug mode and Dark mode unchanged; the Case sounds; the 1.2.1 process 14361 read the thirteen settings again at 17:25:20). P1's
  `dumpsys` was not saved. One APK serves both users: the Owner's 1.2.1 process 17838 at 06:18:58 the next morning (L `am_proc_start`).
- **Users.** Set 1 = the secondary user: the app logcat's header `userType: full.secondary` (CAP-075-OpenControl-for-Pixel-Buds-Pro-2-log-97b3b88fc408.txt line
  3) — "Secondary user: any user added to the device other than the system user" (source.android.com, *Multiple users*, last updated 2026-09-22 UTC, fetched
  2026-10-13). Set 2 = the Owner (user 0): `am_proc_start: [0,17838,10354,io.github.tedsluis.opencontrolpixelbuds,…]` (L line for 06:18:58.248; X the same at
  04:18:58.248 UTC); 0 `am_switch_user` lines in X (`grep -c am_switch_user` → 0, exit 1; positive control `grep -c am_proc_start` → 123, exit 0). The
  maintainer can still confirm with `adb shell am get-current-user` / `pm list users` (Checkpoint 0).
- **Play.** Set 2: present and active — uid 10205 / process 7277 (`NearbyDiscovery`, `HearableControl` lines in L), 19 claims of its own on DLCI 0x04 in H2 and
  3 + 2 in H2L, 33 `cleanup_rfc_slot … scn: 2, app_uid: 10205` lines; `frame contains 03:08:00:02:01:25` → **6 (H2L) + 18 (H2)**, exit 0. Set 1: **0** in H1L
  and 0 in H1 (the same filter, exit 0; positive control `CAP-074-btsnoop_hci.log` → 22). Play's *Nearby devices* permission and version: not on film (P0's
  item not done — `TODO.md` §3's standing item stays open).
- **Other clients on the Buds' ACL in set 2.** The Google app (uid 10302, `com.google.android.googlequicksearchbox` per L's `am_pss` lines) opened RFCOMM
  server channels 4 and 5 (DLCI 0x08 and 0x0a) 06:29:24.078/.216 (L `on_cli_rfc_connect … scn: 5/4, app_uid: 10302`; H2 `SABM` 884/966) and closed them
  06:30:43.47/.49 — 59 + 4 frames in the `[Group][Code][Length][Value]` envelope (`PROTOCOL.md` §2.3's third channel; e.g. H2 1020 phone → Buds
  `05 0c 00 00 04 02 00 00 04 04 …`, 1041 Buds → phone with the firmware string `release_5.203`). The same two channels were opened in H2L at 18:13:21 and
  06:19:04. **Not this app and not Play** (the uid); content not analysed (§12). HFP: DLCI 0x09 by the Buds and 0x0c by the phone (`AT+BIEV` as `CAP-009`).
- **Logs.** Sizes, modes and SHA-256 of the 13 delivered files as in the prompt's §2 (re-derived; `CAP-075-EVENT-NOTES.md` pre-flight). The four HCI logs cover
  the two sets completely (each set's Bluetooth off/on rotated the log: H1L → H1 at 17:23:04/08, H2L → H2 at 06:29:13/17); H2L also holds the Owner's unfilmed
  session of 2026-10-12 18:13–18:14 (`CAP-075-EVENT-NOTES.md` "Between the sets"). The exports: E1 228 lines (process 14361, 17:25:20–17:31:53), E2 512 lines
  (17838, 06:18:58–06:35:31). The system log of set 2 exists twice: X (`bugreportz`, main buffer from 04:34:02 UTC = 06:34:02 — after the last cut) and L
  (the GrapheneOS log viewer, main buffer from 06:26:54.556, line 71 989 — **covers all 15 cuts of the film**); both are local only, the committed file is
  the redacted extract CAP-075-logcat-extract.txt (20 711 lines of the app, the Bluetooth process and Play, 06:18–06:36; Checkpoint 0). No system log for set 1.
- **Health.** `am_crash` 0, `am_anr` 0 for the app in X and L (`grep -c`, exit 1; positive control: `am_proc_start` exit 0); one `FATAL EXCEPTION` in L at
  2026-10-12 18:54:36 (process 26475, not this app — `grep -n 'FATAL' L` → 1 line, exit 0); `StrictMode` 0 for the app; the SIGQUIT 06:35:41 in the app logcat
  is `bugreportz`. pw_hdlc CRC-32 0 bad in 318 frames; 0 `DM`; every `pw_rpc` status `OK` (scripts/pwrpc_decode.py).
- **Privacy (Checkpoint 0).** On film 1: the Wi-Fi name in the shade/Quick Settings (s23–27, s40, s369–370), the three serials on Info (s16–22, s62–149,
  s187–189); on film 2: the serials (s24–36), the Spotify card and a notification with the first characters of the account's e-mail address (s326–327); both
  audio tracks carry the maintainer's voice (the cue words). In X / L: the Wi-Fi name 14 / 265 lines, MAC-shaped strings 89 / 534, the Buds' address prefix
  46 / 88, the Gmail account name 7 / 8 (`grep -c`, exit 0 each); 0 `@` in the exports and the app logcats (exit 1; positive control L, exit 0). Neither
  export nor app logcat carries the Buds' address (the app logs it redacted) — the serials appear in E1 line 48 and E2 lines 49, 142, 259, 325 and 397 (the `GetHardwareInfo`
  answers as hex) and in the app's own "Serial numbers" lines: **the exports are not serial-free** (committed as is, as `CAP-073`/`CAP-074` — Checkpoint 0). The films: committed as is (the
  maintainer's choice, Checkpoint 0).

## 1. ACLs, the Message Stream claims of both clients, and Play's reaction times

**Method** (as `CAP-074-FINDINGS.md` §1): a claim = one `SABM` on DLCI 0x04 (server channel 2, direction bit 0 — the phone initiates: "server applications on
the non-initiating device are reachable on DLCIs 2, 4, 6, …" (RFCOMM §5.2, quoted via docs.fuchsia.dev's `bt_rfcomm` DLCI page, fetched 2026-10-13); DLCI
0x05 never appears) to its `DISC`; the owner is the first phone message after the `UA` (`08 11 00 00` = this app's `Get`; `03 08 00 02 01 25` = Play's
"Platform type" first). Generated with claims.py over `tshark -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type
-e data.data` (exit 0); the inventories are the four claims_*.txt files of the session's scratch directory (not committed).

| Log | ACL (handle `0x000b`) | Claims | App | Play | Closer of the app's claims |
|---|---|---|---|---|---|
| H1L | 501 17:22:45.301 → 1097 17:23:04.162 `0x16` | 1 | 1 (#1 `SABM` 617 → `DISC` 841, 1.55 s) | 0 | the phone (the app's release) |
| H1 | 151 17:23:09.631 → 1814 17:28:47.531 `0x13` | 10 | 10 (#1 … #10, below) | 0 | the phone, 1.50–1.51 s after the hold's end or 1.54–1.64 s after a snapshot/Refresh answer |
| H2L | 18:13:17 → 18:14:11 `0x13`; 18:14:11 → 18:14:12 `0x13`; 18119 06:19:00.784 → 21818 06:29:13.490 `0x16` | 12 | 7 (#1, #2, #4, #6, #8 on 2026-10-12; #10 06:19:01) | 5 (#3, #5, #7, #9; #11 06:19:05–06:28:49, 583 s, closed **by the Buds** 21491; #12 06:28:54–06:29:13) | the phone; #10 cut by Play (18576 06:19:03.666) |
| H2 | 469 06:29:21.136 → open at the end | 38 | 19 | 19 (#38 open at the end) | the phone: 15 cut by Play's connect, 4 released by the app (#2, #16, #19, #37) or its Disconnect (#12) |

**Set 1's ten claims (H1, all the app's, every `Notify` on them):** #1 460 17:23:10.055 → 546 (1.54 s, snapshot, `…00 20`), #2 615 17:23:23.003 → 640 (4.07 s,
the 1.2.0 ANC tab), #3 782 17:25:20.575 → 845 (1.55 s, the 1.2.1 snapshot), #4 868 17:25:24.563 → 954 (5.33 s, H1 hold, Settable `00`), #5 1111 17:26:03.977 →
1159 (1.64 s, Refresh battery, `…e8 e8 80`), #6 1192 17:26:06.611 → 1268 (14.40 s, H2: 4 holds), #7 1289 17:26:24.270 → 1389 (33.62 s, H3: 9), #8 1442
17:27:04.775 → 1551 (35.72 s, H4: 12), #9 1572 17:27:42.237 → 1670 (35.62 s, H5: 12), #10 1724 17:28:22.537 → 1802 (22.62 s, H6: 4). **No `DISC` while the tab
was shown**; the `DISC` is the phone's 1.50–1.51 s after E1's "hold ended" line in all six holds.

**Set 2's claims in H2 (the film 06:29:04–06:34:32 and after):** the app's — #2 1283 (snapshot after the re-open, 2.07 s), #4 2298 (the tab, **4.716 s**), #6 2502
(re-claim 1/3, 4.749), #8 2716 (2/3, 4.818), #10 2960 (3/3, 4.716), #12 3297 (tab re-entry, 0.76 s — the Disconnect), #13 3392 (snapshot, 1.000 — cut), #14 3514
(tab, 1.016 — cut), #16 3924 (snapshot, 2.01 — released), #17 4025 (tab, 1.620 — cut), #19 4408 (tab re-entry, 2.09 — released), #21 4639 (snapshot kept as the
hold, 4.749), #23 4865 (1/3, 4.638), #25 5080 (2/3, 4.923), #27 5339 (3/3, 4.781), #29 5571 (Refresh, 4.671), #31 5762 (1/3, 4.554), #33 5957 (2/3, 4.548), #35
6235 (3/3, 4.598), #37 6772 (06:35:29, 2.07 — released); Play's — #1 858, #3 1366, #5 2401, #7 2606, #9 2805, #11 3071, #15 3618, #18 4122, #20 4489, #22 4768,
#24 4957, #26 5188, #28 5433, #30 5666, #32 5856, #34 6056, #36 6341, #38 6833 (plus #11/#12 of H2L). Each Play claim starts `03 08 00 02 01 25` (Platform
type), `08 11 00 00` (its own `Get`), then the Buds' ACKs `ff 01 00 06 07 11 …`, `07 41`, `07 40`, `07 42`, `07 11` — the same sequence as `CAP-074` §1.

**Play's two timers (🟢 FACT, this Play version, 2026-10-13):**

| Measure | Values (s) | n | Range |
|---|---|---|---|
| **Recovery**: Play's socket lost → Play's next connect (= the app's cut when the app held the channel, or Play's `SABM` when the channel was free) | 5.547 (#3→#4), 5.642 (#5→#6), 5.539 (#7→#8), 5.561 (#9→#10), 5.560 (#11→#13), 5.420 (#15→#17), 5.572 (#18→#20 `SABM`), 5.526 (#20→#21), 5.604 (#22→#23), 5.821 (#24→#25), 5.564 (#26→#27), 5.677 (#28→#29), 5.518 (#30→#31), 5.462 (#32→#33), 5.327 (#34→#35), 5.662 (#1→#3 `SABM`), 5.672 (#36→#38 `SABM`), 5.623 (H2L #11→#12 `SABM`, after the Buds' close) | 18 | **5.33–5.82** |
| **Retry**: the app's cut (the collision that also killed Play's socket) → Play's `SABM` | 2.518, 2.569, 2.520, 2.588, 2.513 (→ the cut of #14, Play's `SABM` not on the wire), 2.452, 2.534, 2.817, 2.660, 2.560, 2.581, 2.672, 2.463, 2.226; H2L 06:19:03.666 → 05.727 = 2.061 | 15 | **2.06–2.82** |
| the one exception | 4.559 (06:31:33.021 → #15 06:31:37.580) after L 06:31:33.023 "Failed to read from socket. **Retry times remaining 4**" — Play's second loss within 2.5 s; every other such line says "remaining 5" (32 lines) | 1 | — |

`CAP-074` measured 5.2–5.7 and 2.2–2.6 s (one version of Play, 2026-10-12): **the same timers the next morning**, slightly wider. The app's held claims lived
**4.55–4.92 s** (the `SABM` → `DISC` of #4, #6, #8, #10, #21, #23, #25, #27, #29, #31, #33, #35), because each had killed Play's socket 0.6–1.0 s before its
`SABM` (attempt 1's refusal is the collision). A claim that did **not** kill Play's socket lived until Play's running timer expired: #13 1.000 s, #14 1.016 s
(cut by the retry), #17 1.620 s.

## 2. The Connect reads — byte for byte (`PAIR-003`, `BATT-004`, `FW-003`)

Three sessions in set 1 (17:22:45 ch 21, 17:23:10 ch 21, 17:25:20 ch 21) and five in set 2 (06:19:00 ch 21; 06:29:27, 06:31:28, 06:31:55, 06:32:38 — all ch 19).
The 1.2.1 sequences were compared request by request (`tshark … -Y 'btrfcomm.dlci==2 && frame.p2p_dir==0 && data.data'`, exit 0):

- **Channel 21** (set 1 17:25:20, H1 762–841; set 2 06:19:01, H2L 18253–18326): fifteen frames each, **byte-identical pairwise** — `ReadSetting` 4:16
  `7e004b0310151dea71de7d5e2551aed0ae2a02201047eeadcf7e`, then 4:2 `…0220020f9f143c7e`, 4:4, 4:7, 4:11, 4:12, 4:15, 4:17, 4:19, 4:22, 4:27, 4:28, 4:29,
  `SubscribeRuntimeInfo` `7e004b0310151dea71de7d5e2590821ee66654bfab7e`, `GetHardwareInfo` `7e004b0310151dea71de7d5e25e3a5ec28f96761b57e` — the first and the last
  equal `CAP-073-EVENT-NOTES.md`'s channel-21 reference bytes (the two that document quotes).
- **Channel 19** (set 2 06:29:28, H2 1193–1302): `7e003b0310131dea71de7d5e2551aed0ae2a022010fdae3f247e` (4:16) … `7e003b0310131dea71de7d5e2590821ee6602d65a97e`,
  `7e003b0310131dea71de7d5e25e3a5ec28ff1ebbb77e` — **equal to `CAP-074-FINDINGS.md` §2's B74 15758 … 15846** (the twelve settings ascending between them, as there).
- The answers: the thirteen `ReadSetting` responses (e.g. H1 806 … 839: 4:{2:1}, 4:{4:1}, 4:{7:{1:{4:{1:5}} 2:{4:{1:5}}}}, 4:{11:1}, 4:{12:{1:1 2:1 3:1 4:1}},
  4:{15:1}, 4:{17:0}, 4:{19:0}, 4:{22:1}, 4:{27:1}, 4:{28:1}, 4:{29:2}; 4:16 = the EQ −3.5/0.5/1.0/−1.0/2.5), the runtime-info stream's first packet (H1 844:
  Case 74 % charging, L/R 100 %; H2 1301's answer: Case 69 %), `GetHardwareInfo` answered once per session (H1 844, H2L 18326's answer, H2 1302's answer
  1319–1322 — the three serials). **No second `GetHardwareInfo` in any session** (`pwrpc_decode`: 1/2/2/4 in the four logs = one per session). 🟢 FACT: the 1.2.1
  Connect is byte-identical to 1.2.0's in both users.
- The snapshot claim after each Connect: set 1 at attempt 1 (nothing contends); set 2 at attempt 1 twice (06:19:01.189 — Play had not claimed yet; 06:31:29.594 —
  Play's socket had just died and its timer was running) and at attempt 2 three times (06:29:30, 06:31:57, 06:32:40 — Play on the channel; attempt 1 refused after
  433 / 408 / 501 ms). `BATT-004`: "Battery updated" ×3 on every claim's open (E2 lines 55–56 etc.).

## 3. Every write — byte for byte

The app wrote, on DLCI 0x02, only the sequences of §2 (3 + 5 sessions × 15 requests; nothing else: `pwrpc_decode` lists no other method — GetSoftwareInfo 1/2/2/4
are the Buds' announcements); on DLCI 0x04 only `SABM`, `08 11 00 00` (one per claim: 1 / 10 / 7 / 19 — the `data.data[0:2]==08:11` count per log 1 / 10 / 12 / 38
includes Play's own `Get`s) and the phone's `DISC`/`UA`. **0 `Set` frames** (`bthci_acl.chandle==0x000b && btrfcomm.dlci==4 && frame.p2p_dir==0 &&
data.data[0:2]==08:12` → 0 / 0 / 0 / 0, `tshark` exit 0 each; positive control `CAP-074-btsnoop_hci.log` → 5). Nothing from the app on DLCI 0x08/0x0a (the
Google app's channels, §0). 🟢 FACT: 1.2.1 wrote nothing new to the Buds; `ANC-001`…`ANC-004`'s `Set` path was not exercised in this run (no mode tap).

## 4. The holds — per set, per switch position; each hold's `Notify`: on screen, Play's, or nobody's (`TOUCH-007`, ADR-061/ADR-062)

**Set 1 (no Play) — both positions identical (🟢 FACT):** 41 mode changes the app did not request, in H2 … H6 (4 + 9 + 12 + 12 + 4; H1 fell with the buds in the
case), **41 on the app's own claim, 41 on screen within a second** (the 1-fps frames in `CAP-075-EVENT-NOTES.md`); **0 "closed, not by this app"**, 0 "re-claim",
0 "hint" lines in E1 (`grep -c`, exit 1 each; positive control E2, exit 0); 0 Play claims. H3 and H5 (hint only) and H4 and H6 (take it back) differ in nothing
on the wire or on screen. The ADR-062 paths that run only after a foreign close (`onHeldClaimTaken`: the hint, the re-claim, the cancel) never ran — by design;
the switch's only visible effect in this user is the Settings row itself. The ANC (i) during H4 showed the "Changed by the Buds at 17:27:31 (a press-and-hold on
a bud, or the Buds' own change)." line and updated live (s302–305).

**Set 2 (Play) — the 31 holds of the film, one row each** (the time is the hold's first `Notify`, or the gap in which a `Get` later found the mode changed):

| # | Hold (mode → time) | Carried by | On screen? |
|---|---|---|---|
| 1 | → `80` in the gap 06:30:40.5–43.1 | Play #9's `Get` (2832) | by the app's re-claim 3/3 `Get` at 06:30:54.437 — **≈ 11 s late** |
| 2–4 | `08` 45.007, `80` 47.619, `08` 50.006 | Play #9 (2878, 2891, 2907) | **never** (Adaptive s80–109) |
| 5 | → `80` in the gap 53.2–54.0 | the app #10's `Get` (2980) | ✓ ≤ 1 s (s110–111) |
| 6 | `08` 06:30:58.178 | the app #10 (3006) | ✓ ≤ 1 s (s114) — **the only unprovoked `Notify` on the app's claim in chain 1** |
| 7 | → `80` in the gap 58.7–01.3 | Play #11's `Get` (3103) | never |
| 8–9 | `08` 06:31:05.986, `20` 06.239 | Play #11 (3155, 3158) | #8 never; #9 **after the user's tab re-entry/Connect** (s147, 06:31:31 — 25 s late) |
| 10 | → `40` in the gap 33.0–37.6 | Play #15's `Get` (3646) | never |
| 11–14 | `80` 39.801, `08` 43.527, `80` 45.847, `08` 51.482 | Play #15 (3688, 3711, 3731, 3801) | #11–13 never; #14 **after the user's Connect** (#16's `Get` 06:31:58.136; s176, 8.5 s late) |
| 15–18 | `20` 06:32:05.495, `40` 08.816, `80` 19.143, `08` 22.074 | Play #18 (4181, 4203, 4265, 4289) | **never** (NC s179–199 — the mode happened to return to NC) |
| 19 | → `20` in the gap 45.2–47.8 | Play #22's `Get` (4792) | drawn by #23's `Get` 50.775 and overdrawn after 39 ms by #20 — not visible |
| 20 | `40` 06:32:50.814 | the app #23 (4893) | ✓ ≤ 1 s (s227) |
| 21 | `80` 06:32:58.192 | Play #24 (4997) | never |
| 22 | → `08` in the gap 06:33:01.3–02.2 | the app #25's `Get` (5102) | ✓ ≤ 1 s (s239) |
| 23 | → `20` in the gap 07.2–09.8 | Play #26's `Get` (5218) | never |
| 24 | `40` 06:33:14.693 | Play #26 (5266) | never |
| 25 | → `80` in the gap 19.8–20.6 | the app #27's `Get` (5361) | ✓ ≤ 1 s (s257) |
| 26 | `08` 06:33:42.323 | the app #29 (5606) | ✓ ≤ 1 s (s278) |
| 27 | → `20` in the gap 43.3–45.9 | Play #30's `Get` (5692) | by the app's re-claim 1/3 `Get` 48.550 — **2.7–5.3 s late** (s284) |
| 28 | → `40` in the gap 52.7–55.3 | Play #32's `Get` (5880) | drawn by #33's `Get` 06:34:00.797, overdrawn after 65 ms by #29 |
| 29 | `80` 06:34:00.862 | the app #33 (5983) | ✓ ≤ 1 s (s297) |
| 30 | `08` 06:34:12.656 | Play #34 (6166) | by #35's `Get` 17.958 (5.3 s late), overdrawn after 0.3 s by #31 |
| 31 | `20` 06:34:18.265 | the app #35 (6260) | ✓ ≤ 1 s (s314) |

**Totals (🟢 FACT):** 31 holds; **8 on screen within a second** (#5, #6, #20, #22, #25, #26, #29, #31: five unprovoked `Notify` frames on the app's claim, three
changes in a gap read by the next re-claim's own `Get` within a second), **2 shown late by a re-claim** (#1 ≈ 11 s, #27 2.7–5.3 s), **3 drawn for < 0.3 s or
overdrawn** (#19, #28, #30), **2 only after a user action** (#9, #14), **16 never** (16 `Notify` frames on Play's claims and 7 gap changes that only Play's `Get`
saw, minus the ones later read). By switch position: **"Show a hint" (06:31:20.5–06:32:26.5): 9 holds (#10–#18), 0 within a second, 1 after a user action, 8
never**; **"Take it back" — the chains (06:30:14–59, 06:32:42–06:33:25, 06:33:38–06:34:22): 19 holds, 8 within a second, 2 late, 3 overdrawn, 6 never**; the
stopped period after chain 1 (06:30:59–06:31:20): 3 holds, 0 within a second. The app held the channel **19.0 / 19.1 / 18.4 s of the 44.2 / 44.9 / 43.7 s of
the three chains = 43 / 43 / 42 %**, then 0 % until the next user action (`SABM`→`DISC` of the four claims of each chain, §1). Cycle lengths cut → cut: 9.23 /
12.03 / 18.21 s (chain 1), 9.49 / 12.43 / 18.19 (chain 2), 9.40 / 12.01 / 17.65 (chain 3) — the held 4.55–4.92 s is the same in every cycle, so the coverage per
cycle is ≈ 51 %, 39 %, 26 %.

**Why a hold on the app's claim is rare even under "Take it back" (🟢, from §1's timers):** the app's claim is cut 5.3–5.8 s after Play's loss, and Play's loss
is the app's own attempt 2 — so a re-claimed hold lives ≈ 4.7 s regardless of the step; a press-and-hold every ≈ 4–5 s lands inside that window about half the
time during a cycle of 9.2 s, a third during 12 s, a quarter during 18 s. The ANC (i) after a re-claim's read (s265–269) shows "ANC mode: TRANSPARENT (updated
06:33:21)" **without** a cause line: the mode had changed on Play's claim or in a gap, and the app learned it from its own `Get` — correct per ADR-061's labels
(read, not notified) but it hides that a hold happened while the channel was Play's (§9 P6).

**The cue words (⚪ for the holds themselves, as `CAP-073`/`CAP-074`):** film 1's bursts 17:26:12.28, 12.99, 16.43, 26.55, 31.04, 43.30, 17:27:08.69/09.49 are
0.17–1.19 s after a `Notify` (or 0.5 s before, for the double at 17:27:09); film 2's 35 segments fall 0.1–2.3 s after a `Notify` or a Play `Get` answer in the
tabulated cases (e.g. 06:30:59.21 after #6, 06:31:33.72 after #14's `20`), with long segments 06:34:12.2–25.1 during the Spotify playback. The words are not
intelligible to the analyst; the holds are the maintainer's statement plus the wire.

**ADR-062's paths, as exercised (🟢):** "Noise-control channel taken by another client: the hint is shown, no re-claim (ADR-062, hint only)" ×2 (E2 06:31:33.097,
06:32:02.276) with the hint on film 1 s later, each carrying the cut's time; "re-claim 1/3 … 3000 ms", "2/3 … 6000 ms", "3/3 … 12000 ms" ×3 chains — the lines
3 009–3 011 / 6 003–6 006 / 12 003–12 006 ms after the "closed" line; "re-claim stopped after 3 re-claims — a tap, a pull or re-entering the noise-control tab
claims again (ADR-062)" ×3 with the hint "…; the app took it back 3 times and stopped. …" on film within a second; "re-claim cancelled (ADR-062)" ×1
(06:19:04.522, pre-film); the hint cleared by a Connect (06:31:55) and by Refresh (06:33:37); a new chain after Refresh (chain 3); no claim while the app was
hidden (the 06:35:29 claim follows "observer started" 06:35:28.168); the Settings choice kept across the app's restarts and the user switch (each user its own
value: set 1 ends on "Take it back" since 17:28:19, set 2 had it before 06:19 and ends on it since 06:32:26).

## 5. Play's mechanism, frame by frame (the stack path of `CAP-074-FINDINGS.md` §8 (a), now observed) and Play's reaction time per claim

**The cut, 15 of 15 in L's reach (🟢 FACT — proposal: closes `CAP-074-FINDINGS.md` §10's first open question):** for every app claim cut during the film
(06:30:19.32, 28.57, 40.71, 58.96, 06:31:30.58, 33.10, 06:32:02.28, 45.32, 54.86, 06:33:07.26, 25.42, 43.45, 52.74, 06:34:04.73, 22.38 — E2's "closed" lines),
L carries, 0.04–0.22 s before the export line: `bt_port_api: … RFCOMM_CreateConnectionWithSecurity: already at opened state 2, RFC_state=4, MCB_state=5, …
scn=2, is_server=false` → `bt_btif_sock_rfcomm: cleanup_rfc_slot: disconnected from RFCOMM socket connections for device: …, scn: 2, app_uid: 10205` →
`NearbyDiscovery: RfcommEventStreamMedium: … Failed to read from socket. Retry times remaining 5` → `cleanup_rfc_slot … scn: 2, app_uid: 10354` 1–14 ms before
the app's own "closed … read return: -1" (e.g. L 110190 / 110194 / 110197 / 110204 for 06:30:19.280–.319). Reading: Play's recovery `connect()` on server
channel 2 is refused by the stack because a connection to that channel is open (the app's), the stack cleans up **both** slots (Play's new one and the app's
open one), Play's reader fails and schedules its retry (2.2–2.8 s), and the app's blocking read returns −1 — `BluetoothSocket`'s `throw new IOException("bt socket
closed, read return: " + ret)` (AOSP `BluetoothSocket.java`, main, fetched 2026-10-13; the class javadoc: "RFCOMM is a connection-oriented, streaming transport
over Bluetooth", `connect()` "will block until a connection is made or the connection fails"). **The mirror image** is the app's attempt-1 refusal: the same
`already at opened state` line at 06:29:28.826, 06:30:13.733, 06:30:22.866, … (17 in E2: 433, 450, 537, 416, 461, 777, 408, 981, 501, 792, 473, 519, 1 053,
699, 468, 261, 777 ms after the attempt began), each with Play's `DISC` by the phone within 2 ms (Play's socket cleaned up by the app's refused connect) and
Play's "Esdc … connected=false" + "update dataStore … disable ANC when event stream is temporarily disconnected" lines; the app's attempt 2 then connects
0.9–1.5 s after the attempt-1 start. 🟢 FACT for the lines and their order; 🟡 HYPOTHESIS for the stack's internal reason (the `port_api.cc` text is the
stack's own, the cleanup of both slots is inferred from the two `cleanup_rfc_slot` lines).

**Play's side (🟢 for the lines):** at each of its re-opens "Esco …", "receive session nonce …", `onReceiveEventStreamResponse` (its `Get`'s answer); an
extra `onReceiveEventStreamResponse` at every `Notify` it received during its claims (06:30:45.012, 47.625, 50.012, 06:31:05.992, 06.244, 39.805, 43.533,
45.853, 51.501, 06:32:05.499, 08.821, 19.148, 22.078, 58.194, 06:33:14.698, 06:34:12.659 = the 16 holds of §4 marked "never" + #9/#14) — Android's own Buds
state follows them; "update dataStore … disable ANC when event stream is temporarily disconnected" at each loss. Play's DLCI is 0x04 (server channel 2) in
every one of its 24 claims and all 33 stack lines; never 0x05.

**Play's reaction time per claim:** §1's table (recovery 5.33–5.82 s ×18, retry 2.06–2.82 s ×15, 4.56 s once after "Retry times remaining 4"). **Proposal for
`PROTOCOL.md` §4.1 (awaiting approval; wording for the maintainer):** "Google Play services' Fast Pair client (the Owner user, 2026-10-12/13, one Play version)
re-opens the Message Stream 5.3–5.8 s after it lost it and retries 2.1–2.8 s after a connect of its own that collided with an open claim (4.6 s once after two
losses within 2.5 s); on Android 17/GrapheneOS a second `connect()` to an open server channel is refused with `already at opened state` and the stack closes
both sockets — `CAP-074` (B 14577) and `CAP-075` (H2 2369; L 110190–110204)." Approved as 🟢 FACT by the maintainer in chat 2026-10-13 (`AskUserQuestion` "PROTOCOL" → "Add as 🟢 FACT in §4.1") and written to `PROTOCOL.md` §4.1 with a §8 row.

## 6. Playback

Set 1: AVRCP `Stopped` once (17:23:11.6), 29 AVDTP start/suspend pairs (the system sounds of the taps); no music. Set 2: AVRCP `Paused` 06:29:23.8, **`Playing`
06:34:11.67 → `Paused` 06:34:46.2** (AVDTP start 06:34:11.84 / suspend 06:34:49.19): Spotify, a podcast, on the Buds (the shade s326); the holds #30 (06:34:12.66,
on Play's claim) and #31 (06:34:18.27, on the app's) fell during it and behaved like the others (🟢: playback changed nothing in the claim pattern — 2 holds only,
⚪ for a general statement). The volume panel overlays (film 1 s219; film 2 s151, s179) have no AVRCP volume frame — a phone-side key press (🟡).

## 7. Session ends and re-opens

| When | What | Evidence | Label |
|---|---|---|---|
| 17:23:04.162 | Bluetooth off by the maintainer; the ACL `0x16` | H1L 1097; film 1 s32–36 | 🟢 |
| 17:23:10 | the ADR-044 re-open after Bluetooth on (ch 21) | H1 151, 460; film 1 s41–42 | 🟢 |
| 17:25:17–20 | the update restarts the app; the LINK_BACK re-open (ch 21) | E1 17:25:20.283; app logcat 1 | 🟢 |
| 17:28:47.096 | **the Buds close DLCI 0x02**, 4.9 s after the app went to the background, then end the ACL `0x13` (Remote User Terminated Connection) 0.4 s later | H1 1805 (Buds → phone `DISC`), 1807 `AT+BIEV=2,100`, 1814; E1 17:28:47.101 | 🟢 for the direction; ❓ the trigger (the buds into the case? not on film) |
| 06:19:03.666 | the snapshot-turned-hold cut by Play 2.6 s after the app's `SABM` (Play had not claimed on this ACL yet: its first claim is #11 at 06:19:05.727 — the cut is Play's **first** connect on this ACL, refused as "already at opened" — consistent with §5) | H2L 18576; E2 06:19:03.674 | 🟢 |
| 06:28:49.008 | **the Buds close DLCI 0x02 and 0x04** (both clients' sockets), the ACL stays up; the app's session lost while hidden; Play back 5.62 s later with the buds now worn (`e8`) | H2L 21490/21491; E2 06:28:49.051; L cleanup scn 1/2 | 🟢 for the direction; ❓ the trigger (the buds out of the case / worn between 06:19 and 06:28:55 — off screen) |
| 06:29:13.490 | Bluetooth off; `0x16` | H2L 21818; film 2 s9–14 | 🟢 |
| 06:29:27.645 | the LINK_BACK re-open (ch 19), the snapshot at attempt 2 | E2; H2 1193–1330 | 🟢 |
| 06:31:26.6 / 28.2, 06:31:51.8 / 55.5, 06:32:35.4 / 38.7 | three Disconnect / Connect pairs (ch 19 each), the snapshot at attempt 1 / 2 / 2; the second cleared the hint; the third's snapshot became the hold (the ANC tab within 1.5 s) | E2; film 2 s143–147, s168–176, s211–218 | 🟢 |

Both Buds-initiated closes happened with the app in the background and **after** the Buds had sent `AT+BIEV` battery indicators; neither is a `0x08`
connection timeout; the `0x13`/`0x16` names are the Bluetooth Core error codes (AOSP `hci_error_code.h`: `HCI_ERR_PEER_USER = 0x13`,
`HCI_ERR_CONN_CAUSE_LOCAL_HOST = 0x16`, fetched 2026-10-13; Espressif's reason table: "Remote User Terminated Connection" / "Connection Terminated By Local
Host"). The app's handling: "channel 0x02 lost (read return -1)" → the session ended → the LINK_BACK re-open on return (set 2) or "on return Android's link was
down" (set 1) — ADR-044 as designed; **no crash, no stuck state**.

## 8. `CAP-073` vs `CAP-074` vs `CAP-075` set 1 vs set 2 — and the answer per user

| | `CAP-073` (1.2.0, no Play) | `CAP-074` (1.2.0, Play) | `CAP-075` set 1 (1.2.1, no Play) | `CAP-075` set 2, "Show a hint" (1.2.1, Play) | `CAP-075` set 2, "Take it back" (1.2.1, Play) |
|---|---|---|---|---|---|
| Holds while the tab was shown | 4 | 47 (9 on the app's claims) | 41 | 9 | 19 (+ 3 in the stopped period) |
| On screen within a second | 4 of 4 | 9 of 47 (the first after each connect/tap) | **41 of 41**, both positions | **0 of 9** (1 after the user's Connect) | **8 of 19** (+ 2 late, 3 overdrawn, 6 never) |
| The app's claim cut by another client | never (31 s hold) | every ≈ 4.7–5.1 s after the `SABM` | **never** (14–36 s holds) | at Play's next connect: 0.42 s, 1.2 s | at Play's recovery 5.3–5.8 s after its loss: 4.55–4.92 s after the `SABM` |
| Re-claims | — | — (none by design) | 0 | 0 (by design; the hint ×2) | 3 per chain at 3 / 6 / 12 s (scheduled), on the wire 4.0–4.8 s after the cut; then the hint |
| Channel held while the tab was shown | 100 % | ≈ 5 s per claim | 100 % | 0.4–1.6 s per claim | ≈ 43 % of a 44-s chain, then 0 % |
| Hint line | — | — | never | ✓ with the cut's time | ✓ with "took it back 3 times and stopped" |
| Play's timers | — | 5.2–5.7 / 2.2–2.6 s | — | 5.33–5.82 / 2.06–2.82 s | the same |

**The answer per user (on evidence, 🟢):**
- **The user without Play:** the two positions are indistinguishable (0 foreign closes in 6 holds / 41 press-and-holds; the regression of `CAP-073` §4 holds:
  every hold shown, no `DISC` while the tab is shown). The default "Show a hint" is right for this user — it never shows a hint, and "Take it back" has nothing
  to take back.
- **The user with Play:** "Take it back" shows more (8 of 19 holds within a second against 0 of 9, and the chain re-reads a changed mode three times) but is far
  from the `CAP-073` experience (43 % coverage for 44 s, then nothing until a tap) and fights Play (Play's socket killed 4 times per chain, Android's own Buds
  state stale meanwhile). "Show a hint" leaves Android's Buds state consistent and the screen honest (the hint) but shows essentially no hold. **Neither is a
  good user experience for a press-and-hold session in this user**; which the maintainer prefers as the *default* is a preference — the evidence says only that
  the default does no harm in either user and that "Take it back" is the one that shows holds at all.

## 9. Improvement proposals for the Play user, ranked (proposals — no code in this session; `AGENTS.md` §6)

| Rank | Proposal | Evidence | Design (sketch) | Cost | Test |
|---|---|---|---|---|---|
| **P1 (Recommended)** | **A constant step and a longer chain**: re-claim 3 s after every cut (no 6/12-s back-off) while the tab is shown, capped by count (e.g. 20) or time (e.g. 3 min), then the hint | Play's timers did not change over 15 cycles (§1) — the back-off buys no peace, it only lengthens the dead time: the held window is 4.55–4.92 s in every cycle, so coverage falls from ≈ 51 % (9.2-s cycle) to 39 % (12 s) to 26 % (18 s); a constant 3-s step keeps ≈ 50 % for the whole visit instead of 43 % for 44 s and 0 % after | `HOLD_RECLAIM_DELAYS_MS` → one delay + a cap; the hint text "took it back N times"; the "stopped" line with the cap's reason; an ADR-062 Update | small (constants, the stop condition, tests, docs) | BK-9 for 3 minutes: ≈ 5 s held of every ≈ 9.3 s throughout; no fourth hint before the cap. **Risk:** a sustained fight while the tab is shown (ADR-062's "may fight"); ❓ what Play does when "Retry times remaining" reaches 0 (seen 5 → 4 once) |
| **P2** | **A "Take it back again" action on the hint card** (or the hint tappable): restarts the chain without a mode write or a battery read | the maintainer restarted chain 3 with Refresh (06:33:37); a mode tap would write a `Set` the user did not mean; the hint already names the condition | the card's hint row gets a text button; `onHeldClaimTaken`'s counter reset = the existing "a tap, a pull or re-entering" path | small (UI + one repository call + a test) | BK-10 via the button: "re-claim 1/3" after the next cut |
| **P3** | **Shorter attempt-2 latency when attempt 1 was refused**: retry after ≈ 0.2 s instead of the observed 0.9–1.5 s between the scheduled re-claim and the `SABM` | the re-claim's `SABM` reaches the wire 1.0–1.8 s after the "re-claim" line (attempt 1 refused 261–1 053 ms, attempt 2 ≈ 0.9 s later) — ≈ 1.3 s of each 9.2-s cycle | the transport's retry delay for the on-demand channel only (the session channels keep theirs) | small, but touches `RfcommBudsTransport`'s retry for all on-demand claims | the re-claim's `SABM` ≤ 3.6 s after the cut; coverage ≈ 55 % per cycle |
| **P4** | **Label a change learned by a re-claim's own read**: the (i) line "Changed while another app had the channel (read at HH:MM:SS)" when the `Get`'s mode differs from the last known | s265–269: "ANC mode: TRANSPARENT (updated 06:33:21)" with no cause although hold #25 had happened in a gap; 10 of 31 holds were learned this way (§4) | a third cause value in `AncState` next to notified/read; one string | small | the (i) after a re-claim that found a changed mode |
| **P5** | **Stop contending: read the mode on the app's own channel** — check whether DLCI 0x02 (MAESTRO) exposes the current noise-control mode (a `ReadSetting` id outside the thirteen, or a runtime-info field) | in this run the 0x02 stream packets (06:30:59.197, 06:31:30.892, 06:31:49.4, 06:31:58.4, 06:32:41.4, 06:32:59.4, 06:33:09.5, 06:34:39.4, 06:34:59.99) **never coincide with a hold** (31 holds; the nearest 1.0 s after #6) — nothing on 0x02 reflected a mode change; the thirteen settings are static | a reverse-engineering task (`REVERSE_ENGINEERING.md`: the `WriteSetting`/`ReadSetting` ids of the Maestro schema), then a `PROTOCOL.md` entry at 🟢 before any code (`AGENTS.md` §6) | high, uncertain | a `ReadSetting` of the candidate id during a hold; negative result possible |
| **P6** | **Re-claim before Play's retry** (≈ 2.0 s after the cut, into the free window) | **rejected on evidence**: a claim made while Play's timer runs lives only until Play's next connect (#13 1.0 s, #14 1.0 s, #17 1.6 s); the retry at 2.2–2.8 s would cut it within ≈ 0.5 s — worse than the collision path (4.7 s) | — | — | — |
| **P7** | **The CHANGELOG's Known-issues line with the measured numbers** (and ADR-062's "about 5 seconds of every 9" qualified: the first cycle only) | §4's cycle lengths and coverage | text only | none | — |

A tight fight that drives Play's retry counter to 0 (five consecutive collisions) is **not** proposed: its outcome is unknown (❓) and it is the "fight" ADR-062
warns about, not a feature.

**ADR-062 Update (approved by the maintainer in chat 2026-10-13, `AskUserQuestion` "ADR-062 Upd" → "Approve the draft"; written to `DECISIONS.md`):** "**Update 2026-10-13 (`ai-sessions/0088`, `CAP-075`): hardware-verified.** Without
Play (the secondary user) both modes are identical and regression-free against `CAP-073` (41 of 41 holds shown; no foreign close). With Play (the Owner user, one
Play version, 2026-10-13): mode 1 showed the hint with the cut's time and never re-claimed (2 of 2); mode 2 ran three chains of exactly three re-claims scheduled
3 000 / 6 000 / 12 000 ms after the cut (the export), each reaching the wire 4.0–4.8 s after the cut because attempt 1 is refused — Play's retry (2.1–2.8 s after
the cut) is on the channel — and attempt 2 connects 0.9–1.5 s later by killing Play's socket; each re-claimed hold is cut 5.3–5.8 s after Play's loss (4.55–4.92 s
after its `SABM`). Coverage ≈ 43 % of a 44-s chain, then 0 % until a tap; 8 of 31 holds shown within a second. The 'about 5 seconds of every 9' estimate holds for
the first cycle only (9.2–9.5 s); the second and third cycles are 12.0–12.4 and 17.7–18.2 s. The cancel path ran once (pre-film, 06:19:04); BK-11 … BK-13 (a tab
change, Home, the switch within 2 s of a cut) are untested. Play's timers equal `CAP-074`'s. Follow-up: §9 P1/P2 of `CAP-075-FINDINGS.md`."

## 10. The skeleton's "Refuted if" conditions, per step

| Step | Refuted if … | Outcome |
|---|---|---|
| BK-1 | a crash; a request missing or out of order; Safe Mode | **not refuted**: 0 crashes; the thirteen requests in order in all 8 sessions; `release_5.203` |
| BK-2 | "-dirty"; another hash; the card missing or the other row selected on a fresh update | **not refuted** for set 1 (the fresh update: "Show a hint" selected, s181); set 2's card showed "Take it back" — not a fresh update but the Owner's stored choice from before the film (E2 06:19:04 "re-claim cancelled" proves the mode was set by then) |
| BK-5 | an app `SABM` after the cut without a tap (hint only); no hint line; the hint without a time | **not refuted**: 0 app `SABM` between the cuts 06:31:33.02 / 06:32:02.20 and the next user action (claims #16 = the Connect 06:31:55, #19 = the tab re-entry 06:32:33); the hint with "06:31:33" / "06:32:02" |
| BK-6 | the hint not cleared by the tap; the old (i) sentence | partly tested: the hint cleared by Refresh and by a Connect (no mode tap in the run); the corrected (i) sentence on film ×2 |
| BK-7 | the pull not clearing the hint | tested by the Refresh button (cleared); the pull-down gesture itself not used |
| BK-8 | the choice not kept | **not refuted**: kept across tab changes, the app's restarts (set 1 17:25, set 2's process from 06:18) and per user |
| BK-9 | a re-claim sooner than 2.8 s or later than 3.3 s after the cut (the first step); a fourth re-claim; the hint missing or without "3 times"; a re-claim while the tab is not shown | **the scheduled re-claim: 3.009–3.011 s after the "closed" line (×3) — not refuted; the wire's `SABM`: 4.48 / 4.85 / 4.85 s after the cut (×3) — outside the window because attempt 1 is refused; the skeleton's window was written for the scheduled moment** (a finding, not a refutation: §4, §9 P3); no fourth re-claim (×3); the hint with "3 times" (×3); no claim while hidden |
| BK-10 | no re-claim after the tap's claim was cut | **not refuted** (chain 3 after Refresh) |
| BK-11 … BK-13 | a re-claim while Sound / hidden / after the switch | **not tested** (the one "cancelled" is pre-film) |
| BK-14 | the hint surviving the Connect | **not refuted** (gone at 06:31:55; s171–176) |
| BK-15 | Safe Mode; "-dirty" | not refuted |
| BK-16 | a `DISC` while the tab is shown; a hold not shown; the hint line | **not refuted** (H2, H3, H5: 25 of 25 shown, no `DISC`, no hint) |
| BK-17 | any re-claim line or extra `SABM`; a `DISC` while the tab is shown | **not refuted** (H4, H6: 16 of 16 shown; 0 re-claim lines; one `SABM` per tab entry) |
| BK-18 | no `DISC`, or one before 1.3 s | **not refuted** (1.50–1.51 s ×6) |
| BK-19 … BK-21 | — | not run |

## 11. Verdict table (`RELEASING.md` C4 — proposal; the verdict is the maintainer's)

| C4 category | Found? | Evidence |
|---|---|---|
| Crash | no | `am_crash` 0 / `am_anr` 0 (X, L); both app logcats without an exception; 8 sessions, 49 app claims |
| Wrong write | no | 0 `Set` frames; the Connect reads byte-identical to `CAP-073`/`CAP-074`; nothing on other DLCIs |
| Safe Mode on the verified firmware | no | `release_5.203` ×3 in both users; no Safe Mode screen |
| Lost session | **not by the app**: two Buds-initiated closes with the app in the background (17:28:47, 06:28:49) and the ADR-044 re-open on return; no loss while the app was in use | §7 |
| The definition of done (the user without Play unchanged) | **met** | §4, §8: 41 of 41, no foreign close, both positions identical |
| The Play user's behaviour as ADR-062 describes | **as described, with the numbers of §4** — the CHANGELOG's "about 5 seconds of every 9" is the first cycle only; "roughly 4 of 10 holds" was ≈ 8 of 19 within a second plus 2 late | §4, §9 P7 |

**The maintainer's verdict (chat 2026-10-13, `AskUserQuestion` "Verdict C4" → "No heavy issue: release 1.2.1 as built"):** C4's light path — no rebuild;
the CHANGELOG's Known-issues line corrected to the measured numbers (done, `ai-sessions/0088`); §9 P1 + P2 + P4 go to the next FEATURE prompt ("FEATURE" →
"P1 + P2 + P4"), toward 1.2.2; `TODO.md` §5 holds it, §M continues with D1–D4 / E1–E4.

## 12. Open questions

1. ❓ What triggers the Buds' own closes of DLCI 0x02 (17:28:47) and of 0x02 + 0x04 (06:28:49), both while the app was in the background and after an `AT+BIEV`
   battery indicator — the buds moving into/out of the case (off screen both times)? A run with the case moves on camera (`CASE-004`/`CASE-005`) would settle it.
2. ❓ What does Play do when "Retry times remaining" reaches 0 — give up, or a longer back-off? Only one decrement (5 → 4) was seen; the counter was back at 5 on the
   next loss.
3. ❓ The Google app's DLCI 0x08/0x0a traffic (uid 10302; 63 frames; the firmware string `release_5.203` in its answers) — what it reads and whether it ever
   contends with anything; no evidence it touched DLCI 0x04.
4. ❓ Does any `ReadSetting` id outside the thirteen, or a runtime-info field, expose the current noise-control mode on DLCI 0x02 (§9 P5)?
5. ❓ The volume panels on film (17:26:19, 06:31:35, 06:32:03) — a phone key or a bud gesture? No AVRCP volume frame either way.
6. ⚪ The "Unidentified frames (13)" / "(11)" counters on the Debug tab (film 1 s185, film 2 s37–39) — not opened on film; which frames they are is not checked
   here (the `03 03` battery messages are identified, so these are others).
7. BK-11 … BK-13 and BK-19 … BK-21 remain untested (`TODO.md` §2).
8. Resolved (Checkpoint 0): the exports' serial lines (E1 48; E2 49, 142, 259, 325, 397) stay as they are, as in `CAP-073`/`CAP-074`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-075-2026-10-12_17-22-31_2026-10-13_06-34-32-Group_BK/CAP-075-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-075-2026-10-12_17-22-31_2026-10-13_06-34-32-Group_BK/CAP-075-FINDINGS
