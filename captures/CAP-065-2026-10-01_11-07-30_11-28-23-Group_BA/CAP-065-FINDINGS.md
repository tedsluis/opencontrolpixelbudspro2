# Findings: `CAP-065` (Group BA — robustness, UI and the `ai-sessions/0059` fixes; the Settable byte with the ears on film; lead L-1, the announced channel per bud)

Standardized, evidence-based extraction from `CAP-065-btsnoop_hci.log`, `CAP-065-recording.mp4` (video and audio),
`CAP-065-opencontrol-debug-20261001-112907.txt` (the app's debug export), `CAP-065-OpenControl-for-Pixel-Buds-log-14015f5bc461.txt` (app logcat) and
`CAP-065-System-log-a52da8511ec6.txt`, per `ai-sessions/0061`. The timeline these findings refer to is `CAP-065-EVENT-NOTES.md`.

- 🟢 **FACT** — directly observed in this capture (frame number, log line or film time given).
- 🟡 **HYPOTHESIS** — plausible reading, not yet independently confirmed; the settling experiment is named.
- ⚪ **ASSUMPTION** — not tested here.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-065` · **Date:** 2026-10-01, film overlay 11:07:30–11:28:23 (phone 11:07:30.5–11:28:23.5) · **Firmware:** 🟢 `release_5.203`
(10 of 10 DLCI 0x02/0x03 announcements) · **Phone:** Pixel 9a, GrapheneOS, Android 17 `CP3A.260905.009`, Google Play services and the Google app present ·
**App under test:** OpenControl, the same installation as `CAP-064` (§0) · **HCI log:** 12,771 packets, raw, 0 `cap_len≠len` · **Buds:** `04:00:6e:cf:6e:07`,
classic handle `0x000b` for all seven ACLs; LE handle `0x0041` = the Fitbit Charge 6 (§7), excluded. `bluetooth.addr` is empty with this `H4 with linux
header` encapsulation, so every command pre-filters by handle.

Commands used throughout (rule 4a):
`tshark -r CAP-065-btsnoop_hci.log -Y "bthci_acl.chandle==0x000b && btrfcomm && (btrfcomm.frame_type==0x2f || btrfcomm.frame_type==0x43 ||
btrfcomm.frame_type==0x63 || btrfcomm.frame_type==0x0f)" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e
btrfcomm.frame_type` (every `SABM`/`DISC`/`UA`/`DM`, 304 frames); HCI connection events with `-Y "bthci_evt.code==0x03 || bthci_evt.code==0x04 ||
bthci_evt.code==0x05 || bthci_cmd.opcode==0x0405 || bthci_evt.le_meta_subevent==0x0a"`; MAESTRO and the Message Stream with a scratch decoder over
`-Y "bthci_acl.chandle==0x000b && btrfcomm" -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type -e data.data` that
reuses `scripts/pwrpc_decode.py`'s `parse`/`describe`/`unescape`, reads **DLCI 2 or 3** per direction with a CRC-32 check of every pw_hdlc frame
(`scripts/pwrpc_decode.py` alone reads DLCI 2 only and would miss the four ACLs in which the Buds opened the multiplexer), and parses DLCI 4/5 at message
level (`[Group][Code][Length BE]`) per direction, with every buffer reset at a `SABM`/`DISC`/`UA`/`DM` of its DLCI; a Message Stream claim is Play services'
when its first phone message is `03 08 00 02 01 25`, the app's when it is `08 11` or `08 12` (the app's claims match the export's "RFCOMM channel 0x04
connected" lines one to one). AVRCP with `-Y "bthci_acl.chandle==0x000b && (btavctp or btavrcp)"` (exit 0, 42 frames — the positive control for every
AVRCP negative; a first command with an invalid field list printed nothing with exit 0 and was discarded); HFP from the raw RFCOMM payloads.
`p2p_dir` 0 = phone→Buds. **Clocks:** phone = film overlay + 0.5 s (±0.2 s; the minute flips at both ends, no drift); HCI and the export are phone local
time; logcat and system log (UTC) = local − 2 h 00 min 00.000 s.

---

## 0. Capture integrity, build identity, audio, logs (🟢 FACT unless marked)

- `capinfos`: 12,771 packets, "Packet size limit: (not set)", 11:07:36.017203–11:30:07.973343; `cap_len≠len` 0; 4 out-of-order pairs. The film
  (1252.98 s) covers 11:07:30.5–11:28:23.5; the HCI log starts 5.5 s after the film's first frame and ends 1 min 44 s after its last.
- **Build — "the latest build, the same as `CAP-064`" (the maintainer's statement).** 🟢 *Same installation:* the system log shows the app process
  PID 21818 — the process `CAP-064-FINDINGS.md` §0 names as OpenControl during `CAP-064` — alive from the start of the events buffer (07:44 UTC) until
  `am_kill … PID=21818 … Reason=remove task` at 09:00:00.760 UTC (line 27374, `am_proc_died` 27393), then `am_proc_start … PID=8072 … next-top-activity`
  at 09:00:16.110 (28757); there is no installer, data-clear or package line for the app in the log (`grep -c installer_clear_app_data` → 0; positive
  control: the same grep found the line in `CAP-064`'s log, line 8044). So no reinstall and no data clear happened between the runs — nothing contradicts
  "same build as `CAP-064`". 🟡 *Which build:* as in `CAP-064` §0 — at least the `0057` UI with the `0056` field-12/field-2 code; no log line, frame or
  screen carries a commit hash or build number (`versionCode` 1), and no behaviour that exists only from `b65085a` on was triggered here except one: the
  EQ note of `EqScreen.kt:217` ("Reading the Buds' current EQ… The sliders are off until it arrives…") is on film (§2 BA-1) — that note was added by
  `ai-sessions/0059` (A58-APP-02), i.e. by `b65085a` (`git log -S "The sliders are off until" -- android` → `b65085a`). 🟢 **The installed build contains
  `b65085a`'s EQ change.**
- **Audio.** AAC stereo, decodes (`ffmpeg -i CAP-065-recording.mp4 -vn -ac 1 -ar 16000 …`). Per-second RMS: median −71.7 dBFS, 90th percentile −59.1,
  maximum −37.9 dBFS (t ≈ 705 s, 11:19:15, the lid opening at BA-10); 134 seconds rise > 12 dB above the median, broadband transients (handling, the case,
  taps). No speech pattern in the full spectrogram; a faint, stable-pitch tonal pattern at t ≈ 1030–1120 s (11:24:40–11:26:10, while the phone lies
  untouched) is unidentified. The maintainer's statement "I did not speak" is consistent with the track; nothing heard inside the buds is on it.
- **Debug export.** 739 lines (738 newlines), 11:00:19.035–11:29:04.055; line 1 is the new process's start, so the 20,000-line ring buffer did not wrap.
  Debug mode was on (film 11:11:50, 11:27:48): every inbound frame is hex-logged; outbound frames are not (the requests come from the HCI log). No
  `MalformedFrame`, Safe Mode, quarantine, undecodable-answer or late-answer line (§2 BA-4).
- **One export, not two.** The skeleton's L2/L3 planned an export with Debug mode on and one with it off; only one file exists, saved 11:29:07 (its name),
  after the film; the export is not on film and Debug mode was still on at 11:27:48. L3 was not done.
- **App logcat.** 606 lines, buffers events/system/main interleaved, 08:04:34.908–09:28:30.331 UTC; no crash, ANR or tombstone line. Activity lifecycle:
  created 09:00:19 (new process), destroyed/created 09:10:42/48 (task swiped away in Recents — the process stayed), paused/stopped 09:22:18/19 and restarted
  09:22:29 (F7 Home), stopped 09:23:06 → destroyed/created 09:23:45 (dark theme on) → stopped 09:24:19 → destroyed/created 09:24:25 (dark theme off),
  stopped 09:28:30. Three `W System: A resource failed to call close.` (09:24:04.606, 09:26:39.149, 09:27:21.562) on thread 8085, each near
  `V BluetoothSocket: close() …: Already closed` on the same thread — 🟡 the Java `CloseGuard` report of an object finalized without `close()` (the
  thread is the finalizer's); which object is not logged (no stack: StrictMode's `detectLeakedClosableObjects` is not enabled). Settling experiment: a debug
  build with `StrictMode.VmPolicy.Builder().detectLeakedClosableObjects().penaltyLog()` and one Refresh with a contended claim.
- **System log.** 64,665 lines, 09-30 05:32 – 10-01 09:28:54.997 UTC. **It carries lines of the Bluetooth process** (PID 8499) — but only for its start
  (09:07) and 09:23–09:28 UTC (772 lines), so the RFCOMM collisions of 11:10:06 and 11:21:35 are not in it (§3). GrapheneOS Bluetooth auto-off played no
  part: Bluetooth went off at 09:07:27.8 UTC with `BluetoothAutoOff … alarm already scheduled: false` (31674–31705, a user action, S1); after it came back
  `shouldScheduleAlarm: true` / `delayMillis: 0` is logged twice (31942–31943, 32020–32021) and Bluetooth never went off again. 🟢 `delayMillis: 0`
  means the auto-off is **disabled**: GrapheneOS's `DelayedConditionalAction.java` returns before scheduling an alarm when the delay is 0 (branch `16-qpr2`;
  `ai-sessions/0063`, `CAP-066-FINDINGS.md` §0) — no "scheduled alarm" line follows. Each foreground-service start is again accompanied by an `am_wtf … Background started
  FGS: Allowed [callingPackage: io.github.tedsluis.opencontrolpixelbuds …]` (e.g. 09:08:27.888 UTC) — as in `CAP-064` §0. **A bug report ran:** `dumpstate`
  takes the bug-report lock at 09:28:21.136 UTC (2 s before the film ends); the logcat ends at 09:28:30, before it would dump the app.

## 1. Automatic connect, lid openings and Android's link (lead 7)

**Verdict: `CAP-064` §1 confirmed, 4 more lid openings.** With both buds in the case, opening the lid started no Bluetooth connection; every ACL of the run
was created by the **Buds'** Connection Request the moment a bud left the case, and the phone paged nobody (0 `Create Connection` commands in the log).
Whenever that link came up while the app was visible and ADR-044 was on, the app re-opened by itself (6 of 6: A, B, C, D, F, G); once (E) it rightly did not, after a Disconnect tap.

| # | Film (phone) | Tab | Visible | Physical action | HCI | Android link (export) | App `SABM` MAESTRO | Re-open | Connect tap |
|---|---|---|---|---|---|---|---|---|---|
| A | 11:08:20.5 → 11:08:22–26 | (Quick Settings over the app) | yes (resumed since 11:00:31) | **lid opened**, both docked; then the Left bud out | nothing 290 → 596 (6.6 s), then **Buds' Connection Request** 596 (11:08:27.10) | `CONNECTED` 11:08:27.816 | 863 | LINK_BACK 11:08:27.839 (export 12) | no ✓ |
| B | 11:08:58 | Sound | yes | Left out of the slot again | Connection Request 1528 | `CONNECTED` 11:08:59.354 | 1837 (DLCI 0x03) | LINK_BACK (66) | no ✓ |
| C | 11:12:52 | Connection | yes | Right out (lid open since 11:08) | Connection Request 3689 | `CONNECTED` 11:12:54.351 | 4036 | LINK_BACK (205) | no ✓ |
| D | 11:14:20 | ANC | yes | Right out (lid open) | Connection Request 4698 | `CONNECTED` 11:14:21.410 | 4929 | LINK_BACK (268) | no ✓ |
| E | 11:19:16.5 → 11:19:20 | Connection | yes | **lid opened**, both docked; Left out | nothing 7094 → 7346 (36.5 s, lid closed 27 s of it), then Connection Request 7346 (11:19:23.02) | `CONNECTED` 11:19:23.890 | none — ADR-044 off after the Disconnect tap (11:18:31.9) ✓ | — | **yes** 11:19:52.254 (BA-10) |
| F | 11:20:50.5 → 11:20:54 | Connection | yes | **lid opened**, both docked; Right out | nothing 8613 → 8809, then Connection Request 8809 (11:20:57.89) | `CONNECTED` 11:20:58.658 | 9108… (DLCI 0x02, 9134 announcement) | LINK_BACK (503) | no ✓ (BA-11) |
| G | 11:21:28.5 → 11:21:32 | Connection | yes | **lid opened**, both docked; Left out | nothing 9683 → 9848, then Connection Request 9848 (11:21:33.91) | `CONNECTED` 11:21:34.468 | DLCI 0x03 | LINK_BACK (553) | no ✓ |

Command: as in the header (HCI connection events) → exactly the seven Connection Request / Connection Complete pairs and six `0x13` (1498, 3646, 4653, 7094, 8613, 9683) / one LE `0x08` (12282) — `tshark -r CAP-065-btsnoop_hci.log -Y 'bthci_evt.code==0x05' -T fields -e frame.number -e bthci_evt.reason`; "five" corrected 2026-10-03, `ai-sessions/0069`
Disconnection Complete events; `-Y "bthci_cmd.opcode==0x0405"` → 0 frames (positive control: the same filter matches `CAP-064` frames 673, 7201 and 8399).

- 🟢 Opening the lid with both buds docked produced **no HCI event** in A, E (after the lid had been closed), F and G; the link came 3.5–7 s after the lid
  opened, each time 1–4 s after a bud left its slot. In `CAP-064` the same held (B, F there). No case in this run of Android re-paging the Buds after an ACL
  drop (all six drops were the Buds' `0x13` on docking; `CAP-064`'s 🔴 G-vs-F question gets no new sample).
- 🟢 The ACL drops when the **last bud is docked**, also with the lid open (11:08:53.56 lid open, 11:12:45.97 lid open, 11:14:14.83 lid open), and does **not**
  drop when the lid is closed with one bud outside (BA-7, 11:15:30–11:16:14: ACL `0x000b` stayed up). 🟢 With the lid closed and the Left bud inside, the
  Buds report the Left bud as unknown (`03 03 00 03 ff 64 ff`, 6015) and the runtime-info stream carries neither the Left entry nor the Case (5874 `…32 06 1a 04
  08 64 10 01 3a 04 08 00 18 00…` = `6:{3:{1:100 2:1}} 7:{1:0 3:0}`).

## 2. Re-test verdicts (skeleton steps; "Refuted if" applied literally)

| Step | Verdict | Evidence |
|---|---|---|
| BA-1 EQ sliders disabled until read | 🟢 **confirmed as far as visible**: at the 11:08:59 re-open the EQ card shows, between "Still connecting…" and the enabled sliders, the note "Reading the Buds' current EQ… The sliders are off until it arrives; a preset can be chosen now." for ≈ 5 frames (≈ 0.15 s) — the wire gap between ready (11:08:59.549) and the `ReadSetting 4:16` answer (1929, 11:08:59.609) is 60 ms; ⚪ the slider tracks themselves are mostly hidden by the hand in those frames; the presets' state in that window is not identifiable. "Refuted if" — an enabled slider before the read: **not observed**; a slider write while unread: **none** (0 `WriteSetting` in the run) | film t = 88.4–90.2 s (every frame); `EqScreen.kt:217` |
| BA-2 tile within 3 s of ready | ⚪ **not identifiable as designed**: the tile was viewed 57 s after ready and is in its **compact** form (icon only) — no subtitle on any frame, so "never 'Open the app'" cannot be read. What the film shows: the tile's on/off colour follows the session's ANC mode (`AncTile.kt:44`: active = ready and mode ≠ Off): red at 11:09:57 (Adaptive), grey ≈ 1.8 s after the ACKed OFF (11:10:01.95), red ≈ 1.8 s after the ACKed ACTIVE (11:10:11.23), grey ≈ 0.2 s after the ACKed OFF (11:11:26.28) | tile colour per 0.5 s (red − green of the tile area: red 45–61, grey 19–30); export 114–188 |
| BA-2 the taps | 🟢 five tile taps, each one claim with a `Set` and **no `Get`** (the app's availability was not `NOT_ALLOWED`: the tap log lines carry no "checks again first", `AncTileService.kt:117`): OFF (2567→ACK 2579), ACTIVE (2640→ACK 2651 **after the channel was closed**, see §3), ACTIVE again (2806→2814), TRANSPARENT (2869→2879), ADAPTIVE (3045→3052); later OFF (3337→3346). All ACKed by the Buds; no toast | export 114–188 |
| BA-3 force-stop, then the tile at once | ⚪ **done differently**: the task was swiped away (activity destroyed 09:10:42.050 UTC; no `am_kill`, the process and the `Ready` session stayed), not a force-stop — so A58-APP-01 (a fresh process before the first ANC report) was **not** exercised. The tile worked 36 s after the app was reopened (3337→3346) | logcat; system log (no `am_kill` between 09:00:16 and the end) |
| BA-4 watch-only lines | 🟢 neither occurred: `grep -c "answered with a value this app cannot read"` → 0 (exit 1), `grep -ciE "Late WriteSetting\|Maestro request held\|quarantin"` → 0; positive control `grep -c "EQ read ok"` → 11; the strings exist in the build's code (`BudsRepositoryImpl.kt:543`, `:863`, `:882`). With 0 writes in the run, the late-answer path could not occur | export |
| BA-5 both on the table | 🟢 **`00`** — `Get` 5453 `08 11 00 00` → `Notify` 5465 `08 13 00 04 01 e8 00 20` (11:14:33.437); the note shown | film 11:14:20–34 |
| BA-6 Left docked, Right on the table | 🟢 **`00`** — 5707 `… 01 e8 00 20` (11:15:00.485) | film 11:14:42–11:15:04 |
| BA-7 lid closed, Right outside | 🟢 **`00`** — 6019 `08 13 00 04 01 e8 00 20` (11:15:53.518); battery 6015 `03 03 00 03 ff 64 ff` | film 11:15:30–11:16:02 |
| BA-8 both worn | 🟢 **`e8`** — 6334 `08 13 00 04 01 e8 e8 20` (11:16:41.787); the note disappears; again 6532 (11:17:02.573) | film 11:16:22–50 |
| BA-9 … BA-11 | 🟢 see §4 (L-1): ch 19 with only the Left out (3 sessions), ch 21 with only the Right out (2 sessions) | |
| F7 ANC tap then Home | 🟢 Noise cancellation: `Set 01 e8 e8 08` (10790, no `Get`: the availability was unknown after the failed snapshot claim of 11:21:35, `BudsRepositoryImpl.kt:628`/`:693`) → ACK (10799) → `Notify … 08`; Home ≈ 1 s later; the claim was released normally (603); on return the mode is shown, the earlier error line gone | export 584–606; logcat |
| K4 rotation / dark mode | rotation ⚪ **skipped**; dark mode 🟢 two recreations, no crash, every tab readable; 🟡 the Connection card's "Disconnect" label has low contrast on the pink card in dark mode (film 11:23:46–11:24:00) | logcat; film |
| K1, K2, K3, K5, L3, A5, (E), B4, Z1 | ⚪ **not done** (EVENT-NOTES step mapping) | |
| BA-12 pull on Controls | 🟢 **exactly seven** `ReadSetting` 2, 4, 7, 12, 17, 19, 22 in that order, each answered before the next (11476 → 11499, 1.19 s), **nothing else** from the app on any channel | MAESTRO list; no app claim 11:26:28–11:26:49 |
| BA-13 pull on Sound | 🟢 `ReadSetting 4:16` first (11536), then the seven (11541 → 11563), nothing else | |
| BA-14 pull on Connection / Find | 🟢 per pull one `SubscribeRuntimeInfo` (11591, 11674; answered 11601, 11684) and one claim with `03 03` + `Get`/`Notify` — the *Refresh battery* sequence; the Find claim was closed 0.5 s after its `Notify` by Play services' colliding connect (system log 09:27:10.450 UTC) | export 659–683 |
| BA-15 pull on ANC | 🟢 one claim, `Get` (11882) → `Notify … 08` (11894); no MAESTRO request | |
| BA-16 Disconnect, pull while disconnected | 🟢 the pull connected once: `SABM` DLCI 0x03, announcement ch 19, the eight reads, the subscription, one snapshot claim | export 697–724 |
| BA-17 Debug, (i)s | 🟢 nothing from the app on any channel while Debug was open (11:27:48–56) or any (i) dialog was open (8 dialogs, 11:09:26 … 11:17:06) | HCI |
| Refuted-if list (A.7) | **no literal hit**: no "Open the app" seen (not identifiable, BA-2); no enabled slider before the read seen, no write; no `00` with a bud in an ear and no `e8` with none (§3); the channel followed the bud that was out (§4); no app frame on DLCI 0x08/0x0a (the export has no channel 0x08 line; the app's sockets are RFCOMM server channels 1 and 2 only, logcat `BluetoothSocket … channel=1/2`); no app `SABM` MAESTRO while not visible (hidden 11:10:42–48, 11:22:19–29, 11:23:06–45, 11:24:19–25: none); each pull sent only its tab's action; no settings value changed (no write) | |

## 3. ANC and the Settable byte (ADR-049; BA-5 … BA-8)

Every `Notify ANC state` of the run (app's and Play services' claims), against the film's wear state (head-orientation mapping):

| Settable | Frames (time) | Film |
|---|---|---|
| `00` | 908, 990 (11:08:28.1/.6), 1315 (11:08:31.02), 1485 (11:08:51.28), 1881 (11:08:59.57), 2134 (11:09:01.54), 3641 (11:12:43.04), 4120 (11:12:55.08), 4357, 5112, 5280, 5372, **5465 (BA-5)**, 5527, **5707 (BA-6)**, 5767, **6019 (BA-7)**, 6081, 7091 (11:18:44.52), 7683, 8269, 8360, 8594, 9163, 9344, 9597, 10356, 10464 (11:21:38.27) | **no bud in an ear**: the Left bud in the hand on its way to the ear (908–1315), being docked (1485, 3641, 7091), on the table / in the case (all others) |
| `e8` | 1352 (11:08:31.45), 2144 (11:09:02.70), 2278, 2343, 2580 … 3411 (tile period), 3596 (11:12:35.88), 6224 (11:16:23.62), 6257, **6334 (BA-8)**, 6384, 6532, 6594, 6815, 6881, 7011 (11:18:37.66), 10620 (11:21:52.05), 10676, 10802 … 12201 | **at least one bud in an ear** |

Command: the scratch decoder's message list filtered to `08 13`; raw example 5465 `0813000401e80020`, 6334 `0813000401e8e820`.

- 🟢 FACT (this capture): **0 counter-examples in either direction** — 28 `00` all with no bud worn, every `e8` with a bud worn — including the two
  situations ADR-049 item 3 asked about: one bud docked and one loose (5707, BA-6, film 11:14:42–11:15:04) and both loose (5465, BA-5): **`00`**, unlike the
  notes of `CAP-045` 612 and `CAP-048` 11939 (whose films did not show the ears).
- 🟢 **Transitions are prompt:** `00` → `e8` 0.4–2.7 s after a bud goes into an ear (1315 → 1352; 2134 → 2144; 6081 → 6224 1.6 s after the Right went in at
  ≈ 11:16:22); `e8` → `00` when the last worn bud comes out (3596 `e8 40` at 11:12:35.9 = first bud out, 3641 `00` at 11:12:43.0 = second out; 7011 → 7091).
- 🟡 `CAP-064`'s exception (`e8` for ≈ 28 s with both buds on the table straight from the case, in-ear detection on) did **not** recur: here every bud taken
  straight from the case to the table read `00` (4120, 5280, 8269, 9344, 10356). The difference is not explained. `CAP-064` §3's hypothesis ("`00` follows a
  removal from the ear or a dock") fits this run (each table period followed a dock or a removal from the ear) but is not tested by it. ADR-049 item 3 —
  🟡 "`00` ⇒ no bud worn" — gains 28 supporting samples and no counter-example; the converse remains refuted by `CAP-064`.
- 🟢 ADR-049 items 1/2 hold: all 7 `Set`s of the run were sent with a non-`00` or unknown availability and all were ACKed (2579, 2651, 2814, 2879, 3052,
  3346, 10799); 0 NAK.
- 🟢 **A `Set` the Buds applied but the app never saw (new):** tile tap 11:10:05.526 → claim `SABM` 2613 → `Set … 08` 2640 (11:10:06.345) → **the phone side
  closes the claim** (`DISC` 2649 at 11:10:06.494, then three `DM`) → the Buds' ACK 2651 (11:10:06.530) and `Notify … 08` (2654, 2655) arrive after the close;
  the export logs only "RFCOMM on-demand channel 0x04 closed (IOException …)" (132) and keeps OFF — the tile stays grey while the Buds are in Noise
  cancellation (Play services' claim reads `Notify … 08`, 2723 at 11:10:09.41). The user tapped again (11:10:09.092) and the second `Set … 08` was ACKed. 🟡 the
  close was the stack's collision path (another client's connect to the same channel closes the incumbent, `ARCHITECTURE.md` §6.0b, ADR-032) — the system log
  has no Bluetooth-process lines at that time; the same mechanism **is** logged at 11:27:10.450 UTC (`RFCOMM_CreateConnectionWithSecurity: already at opened
  state …`, `NearbyDiscovery: RfcommEventStreamMedium … Failed to read from socket`, the app's port closed 0.1 s later).
- 🟢 **A snapshot `Get` answered after the close (new):** 11:21:35 — `Get` 10321, battery 10341, phone `DISC` 10344 (11:21:35.235), the Buds' `Notify … 00 20`
  10356 (11:21:35.408) after the close; the app shows "The Buds didn't respond in time." on the Connection and ANC tabs (film 11:21:36–11:22:10) — the
  Buds did answer; the channel was gone. The availability therefore stayed `UNKNOWN` (`BudsRepositoryImpl.kt:628`), which is why F7's `Set` went without
  `Get` (`:693`).
- 🟢 Mode changes without an app `Set` on wear changes: `40` after a bud went into an ear (1352, 2144, 6224, 10620) or came out (3596, 7011), `20` again
  after the second bud (2343, 6257, 10676) — as `CAP-064` §3 (🟡 the Buds' own wear-driven mode report).

## 4. Lead L-1 — the announced pw_rpc channel per session (`PROTOCOL.md` §2.2a 2026-09-30 🟡)

`GetSoftwareInfo` `RESPONSE`, `call_id` 4294967295, of every session (scratch decoder; raw 1883 `7e 80 a3 03 2a 64 … 10 13 1d ea 71 de 7d 5e 25 44 fa 99
71 38 ff ff ff ff 0f …` = channel `10 13` = 19; 4074 `7e 00 a5 03 … 10 15 …` = 21):

| # | Announcement (frame, time) | Channel | Bud(s) out of the case when the ACL came up (film, slot / head side) | Per-bud charging (stream) | Fits "names the bud that hosts the link" |
|---|---|---|---|---|---|
| 1 | 884, 11:08:28.08 | **19** | only the **Left** (left slot, head right) | 1089: Right charging | ✓ |
| 2 | 1883, 11:08:59.57 | **19** | only the **Left** | 2112: Right charging | ✓ |
| 3 | 4074, 11:12:54.96 | **21** | only the **Right** (right slot; Left docked) | 4162: Left charging | ✓ |
| 4 | 4962, 11:14:21.62 | **21** | **Right** first (11:14:20), Left 1–2 s later | 5268: none charging | ✓ (first out) |
| 5 | 6762, 11:17:48.50 | **19** | both worn — same ACL as #4; the Buds `DISC`ed DLCI 0x03 and 0x05 at 11:17:46.13 (6720/6721) with no visible action | 6827: none charging | — (a switch 21 → 19 inside one ACL) |
| 6 | 8190, 11:19:53.09 (BA-10) | **19** | only the **Left** | 8256: Right charging | ✓ |
| 7 | 8509, 11:20:15.96 (BA-10 rep.) | **19** | only the **Left** | 8571: Right charging | ✓ |
| 8 | 9134, 11:20:58.85 (BA-11) | **21** | only the **Right** | 9350: Left charging | ✓ |
| 9 | 10123, 11:21:34.62 | **19** | only the **Left** | 10405: Right charging | ✓ |
| 10 | 12062, 11:27:35.99 (BA-16) | **19** | both worn (same ACL as #9) | 12124: none charging | ✓ (unchanged) |

- 🟢 FACT (this capture): with only one bud out of the case the Buds announced **19 when it was the Left and 21 when it was the Right — 7 of 7** (incl. #4's
  first-out); the request addresses were the tabulated pairs (19 ↔ `00 3b`/`80 a3`, 21 ↔ `00 4b`/`00 a5`), matching `fux.java:90–103` (19 = `LEFT_BT_CORE`,
  21 = `RIGHT_BT_CORE` on `MAESTRO_A`). 24/26 (`MAESTRO_B`) never occurred.
- 🟡 HYPOTHESIS (strong): the announced channel names the bud that **currently hosts the phone's link** (the "primary"), not merely the first bud out —
  #5 shows the channel changing 21 → 19 within one ACL after a Buds-side close with both buds worn. The Message Stream's Device Information `03 02` ("BLE
  address updated") changed at the same moment (`… 4b 57 ea ef 23 a3` before, `… 70 16 cc 60 6a b9` in the 11:17:49 claim, export 398), as it changed between
  ACLs. Settling experiment: both buds worn, take the **Left** out while the session runs on 19 (and the Right while on 21) and watch for a Buds-side `DISC`
  followed by an announcement on the other channel.
- 🟢 `CAP-064`'s 8 × 21 / 6 × 19 are consistent: there each session's channel was not tabulated against the bud out.

## 5. Settings (MAESTRO, ADR-036/045/046/047)

- 🟢 266 pw_rpc packets, **all CRC-32 valid, 0 unparsed**: 10 `GetSoftwareInfo` announcements (ch 19 ×7, ch 21 ×3), 95 `ReadSetting` requests + 95 answers OK
  (10 Connects × 8 fields + BA-12's 7 + BA-13's 8), 12 `SubscribeRuntimeInfo` (10 Connects + 2 *Refresh battery*) + 54 `SERVER_STREAM`. **0 `WriteSetting`**,
  0 `SubscribeToSettingsChanges`, 0 `CLIENT_ERROR`/`SERVER_ERROR`, no other method.
- 🟢 Values read every time: EQ `[-1, 0, 4, 2, 0]` (field 16; the "Vocal boost" quintet of `PROTOCOL.md` §4.2), 2:1, 4:1, 7 = Noise control both, 12 = all
  four, **17:0** (Centre — the balance `CAP-064` left, not restored to Right 4: `CAP-066` IV), 19:0, 22:1.
- 🟢 Byte comparison of the 107 phone requests against the hex fixtures in `android/data/src/test/kotlin/…/codec/*Fixtures.kt`: `READ_2/4/7/17/19/22_REQ`
  (ch 21), `READ_12_REQ_CH21_1514`, `READ_12_REQ_CH19`, `SUBSCRIBE_RUNTIME_INFO_2777` (ch 19) byte-identical; the ch 19 reads of the other fields, the ch 21
  `ReadSetting 4:16` and the ch 21 subscription have no fixture for that channel and decode to the intended payload with a valid CRC (the ch 21 subscription
  `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 90 82 1e e6 66 54 bf ab 7e` equals the official app's `CAP-036` frame 1410). Raw example: BA-12's first read 11476
  `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 51 ae d0 ae 2a 02 20 02 b5 df 86 d7 7e` = `ReadSetting 4:2` on ch 19.

## 6. Session ends and re-opens (lead 8)

| # | End (phone) | Ended by | ACL | Film | Cause logged (final) | Re-open |
|---|---|---|---|---|---|---|
| 1 | 11:08:53.595 | ACL `0x13` 1498 | down | Left docked (both in, lid open) | "Android's link … went down" (export 64; 3 lines in 222 ms) | LINK_BACK 11:08:59.360 when the Left came out |
| 2 | 11:12:46.007 | ACL `0x13` 3646 | down | both docked, lid open | "link went down" (203; 3 lines) | LINK_BACK 11:12:54.355 (attempt 1 failed in 84 ms, 2nd OK) |
| 3 | 11:14:14.841 | ACL `0x13` 4653 | down | both docked, lid open | "link went down" (266; 3 lines) | LINK_BACK 11:14:21.414 |
| 4 | 11:17:46.136 | **Buds `DISC` 6720 (DLCI 0x03) + 6721 (0x05)** | up | both worn, no action | "the Buds closed the channel" (377) | **AFTER_LOSS** 11:17:47.650 (1.51 s) → ch 19 |
| 5 | 11:18:31.908 | **Disconnect tap** (BA-9) | up → down 11:18:46.50 | both docked, lid closed | — | none: LINK_BACK at 11:19:23.890 ignored ✓ (ADR-044 item 2); Connect tap 11:19:52.254 |
| 6 | 11:20:00.934 | **Disconnect tap** | up | Left out | — | Connect tap 11:20:14.712 (none in between ✓) |
| 7 | 11:20:20.912 | ACL `0x13` 8613 | down | Left docked | "link went down" (501; 3 lines) | LINK_BACK 11:20:58.666 (Right out) |
| 8 | 11:21:13.204 | ACL `0x13` 9683 | down | Right docked | "link went down" (551; 2 lines) | LINK_BACK 11:21:34.471 (Left out) |
| 9 | 11:27:28.979 | **Disconnect tap** (BA-16) | up | both worn | — | the pull on Controls 11:27:35.168 (BA-16) |

- 🟢 ADR-044 held in every case: one attempt per event, only while visible (no app `SABM` MAESTRO in the four hidden windows), none after a Disconnect tap
  until the next Connect, none while the link was down; the 10 s chain guard was not needed (no loss within 10 s of an automatic re-open).
- 🟢 The session stayed open after the film (stream packets until the log ends; the app stopped at 11:28:30 and resumed at 11:29:04).
- 🟢 Cosmetic, as `CAP-064` §6: the cause is logged up to three times within ≈ 0.2 s (#1, #2, #3, #7) — "undetermined" → "the Buds closed the channel" →
  "link went down"; the last one is right.
- 🟢 No Buds-side `DISC` on a wear change in this run except #4 (in-ear detection on: 2:1 read every time); `CAP-064` saw 9.

## 7. HFP, AVRCP, GSND, LE

- 🟢 HFP: 59 × `AT+BIEV` (scratch decoder `AT` lines on DLCI 0x08/0x09), the SLC sequence (`AT+BRSF`, `AT+CIND=?`, `AT+CMER`, `AT+BIND`, …) 7 times — one per
  ACL; **0 `AT+BVRA`** (`grep -c BVRA` → 0; positive control: 252 AT/indicator lines). Not an app source (ADR-040).
- 🟢 AVRCP (42 frames): per ACL `GetCapabilities` and `RegisterNotification` PlaybackStatusChanged **"Stopped"** and VolumeChanged 32 % — **no music played in
  this run** (`AUDIO-001` not exercisable).
- 🟢 DLCI 0x08/0x0a (GSND) were opened by the phone side or the Buds in every ACL (e.g. `SABM` 1012 DLCI 0x0a, 1042 DLCI 0x08 at 11:08:28.7; phone payload
  `05 0c 00 00`, `04 02 00 00` …) — not by the app (its sockets are channels 1 and 2 only; no channel-0x08 line in the export). DLCI 0x04/0x05 outside the app's
  25 claims is Play services' (26 claims: `03 08 00 02 01 25`, SASS `07 11` writes, `07 21`, `07 41 "in-use"`, `07 40`, `07 42` with their ACKs).
- 🟢 LE: only `c8:cc:a8:e7:48:93` (handle `0x0041`) = the **Fitbit Charge 6**: the system log's CDM presence events for association 37 of
  `com.fitbit.FitbitMobile` (connected 09:07:37.987, disconnected 09:28:05.235, connected 09:28:10.081 UTC) match its LE events (290, 12282 reason `0x08`,
  12323). No LE link to the Buds.

## 8. Protocol correlation (per channel)

| Channel / protocol | What the app does | What the Buds answer | Goes well | Goes wrong / to improve |
|---|---|---|---|---|
| **MAESTRO pw_rpc** (DLCI 0x02/0x03, ch 19/21) | 10 session opens (7 automatic — 6 LINK_BACK, 1 AFTER_LOSS — 2 Connect taps, 1 pull); 95 reads; 12 subscriptions (2 by *Refresh battery*) | 95 `RESPONSE`s OK, 54 stream packets | 0 errors; BA-12/13 exactly as specified | — |
| **Message Stream** (DLCI 0x04/0x05) | 25 claims (snapshots, Refresh, tile, F7, pulls; 19 first connect attempts failed in 0.08–0.9 s, the 2nd succeeded — export "connect failed after … (attempt 1/3)" ×19) | Device Info, battery, `Notify`, 7 ACK, 0 NAK | Settable correct every time | two claims closed by the stack after the request left: an applied `Set` shown as not applied (11:10:06), an answered `Get` reported as "didn't respond in time" (11:21:35) |
| **SASS** (on Play services' claims) | nothing | `07 11 … b8 00` (in-ear detection on) | — | — |
| **HFP / AVRCP** | nothing | `AT+BIEV`; playback Stopped | — | — |
| **Android link** | mirrors it; ADR-044 re-open | — | 6 LINK_BACK + 1 AFTER_LOSS re-opens, 0 wrong | **no event when the lid opens with docked buds** (§1) |

## 9. Improvements (proposals only — nothing under `android/` was changed)

1. **A request whose claim is closed under it (new, §3).** Cause: the stack closes the app's DLCI 0x04/0x05 socket when Play services connects to the same
   channel (system log 09:27:10.450 UTC); an ACK or `Notify` that arrives after that is lost to the app (2651, 10356). Today the app keeps the old mode
   silently (11:10:06) or says "The Buds didn't respond in time." (11:21:35). Proposed (`BudsRepositoryImpl` `withMessageStream`/`ancSetOnClaim`/
   `sendAncGetAndAwait`): when the claim's channel closes after a `Set` or `Get` was written and before its answer, report a distinct result — e.g.
   `BudsError.ChannelLost(0x04)` with the wording "The answer was cut off — another app took the Buds' channel. Tap Refresh to see the current mode." — and
   mark the shown mode "not confirmed" (dimmed, (i) dot) until the next `Notify`. Guardrails: no automatic re-claim or retry beyond ADR-032's bounded
   attempts within the same tap, foreground only, no polling, no new permission. Unit test: `BudsRepositoryImplTest` with `FakeBudsTransport` — real bytes
   `CAP-065` 2640 (`Set … 08`), then `channelClosed(0x04)`, then 2651 (ACK) on a closed channel → result `ChannelLost`, mode unchanged and marked
   unconfirmed; and 10321 → close → 10356 → not "Timeout". Hardware step: tile taps while Play services re-opens its claim (as 11:10:05) → no silent keep.
   *Fits the next FEATURE session* (it touches the same ANC claim code as `0060` §H item 2).
2. **ANC `Get` before every `Set` (`0060` §H item 2).** Still wanted; this run shows its cost is one round trip (`Get` → `Notify` 24–432 ms in the 18 app claims with a `Get`) and that the unknown-availability path (F7) would also have benefited. No change to the proposal.
3. **The tile's subtitle (BA-2).** Not an app defect: the tile is in Android's compact form, which shows no subtitle. The test needs the tile enlarged in the
   Quick Settings edit mode — added to `CAP-066`.
4. **Dark mode (K4, 🟡 cosmetic).** The Connection card's "Disconnect" label had low contrast on the pink card (film 11:23:46–11:24:00). For the next FEATURE
   session's Settings → dark mode (On/Off/System): check the card's `onPrimaryContainer`/button colours in the dark scheme with a Robolectric screenshot test.
5. **Loss-cause log (cosmetic, `CAP-064` §9 item 5)** — seen four more times; unchanged proposal.
6. **CloseGuard (🟡, §0)** — enable `detectLeakedClosableObjects` in debug builds to find the unclosed resource behind the three warnings.

## 10. Open questions

- 🔴 Whether the announced channel switches when the hosting bud is taken out with the other still worn (§4 hypothesis and its experiment).
- 🔴 Why `CAP-064` read `e8` for 28 s with both buds on the table straight from the case, while every such case here read `00` (§3).
- 🔴 What closed the app's claims at 11:10:06.49 and 11:21:35.24 (no Bluetooth-process lines then); 🟡 Play services' collision, as logged at 11:27:10.450.
- 🔴 Why the Buds closed the session at 11:17:46 with both buds worn and nothing touched (§4 #5, §6 #4).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-065-2026-10-01_11-07-30_11-28-23-Group_BA/CAP-065-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-065-2026-10-01_11-07-30_11-28-23-Group_BA/CAP-065-FINDINGS
