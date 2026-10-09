# Event Notes: OpenControl for Pixel Buds on Pixel 9a (GrapheneOS) — Group AZ, hardware re-test of the `ai-sessions/0054`, `0056` and `0059` builds (`CAP-064`)

**Status:** 🟢 **Video pass + full log analysis complete** (`ai-sessions/0060`). See `CAP-064-FINDINGS.md` for the wire-level analysis, the commands, the
raw bytes and the answer to the maintainer's observation "the app does not connect by itself when the case is opened". The procedure the maintainer
followed (the skeleton written before the capture, sections VI and VIII moved to `CAP-065`) is kept unchanged as **Appendix A**; the timeline below records
what was **actually** done — the maintainer filmed the ears, changed the order of some steps, skipped some and repeated others.

**Purpose:** the hardware re-test of `ai-sessions/0054` (I-1 … I-5), the AY-3 one-bud-worn test of "Settable `0x00` = no bud worn" (`DECISIONS.md`
ADR-049, 🟡), and section VII of `ai-sessions/0056` (the press-and-hold ANC-mode list, field 12, ADR-046; the in-ear detection switch, field 2, ADR-047).
The maintainer reported afterwards (chat 2026-10-01) that the app did not connect by itself when the case was opened — answered in `CAP-064-FINDINGS.md` §1.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-064`                     |
|      Group(s)    |                        AZ                          |
|       Date       |                    2026-10-01                      |
| Firmware version | 🟢 `release_5.203` — in all 14 MAESTRO announcements (9 on DLCI 0x02, 5 on DLCI 0x03 — the same server channel, corrected 2026-10-03) (e.g. HCI frame 1215, debug export line 228) and on screen (battery (i) dialog "Firmware: release_5.203", film 10:17:13) |
|   Test device    | Pixel 9a, GrapheneOS, Android 17 `CP3A.260905.009` (logcat header `google/tegu/tegu:17/CP3A.260905.009/2026092501`; Quick Settings build line on film 10:08:12). App: OpenControl, package `io.github.tedsluis.opencontrolpixelbuds:1`, targetSdk 34 — **at least the `ai-sessions/0057` build** (Debug in the top bar, five tabs, (i) dialogs on film) **with the `0056` field-12/field-2 writes** (frames 9684, 9922); the system log shows the installer clearing the app's code cache only (`flags=39`, not its data — `CAP-064-FINDINGS.md` §0; corrected 2026-10-08, `ai-sessions/0081`) at 06:53:21 UTC (= 08:53:21, line 8044), 16 min after the last app commit `b65085a` (08:37:32, `ai-sessions/0059`); no log line or wire behaviour that exists only in `b65085a` occurred in this run, so "the latest build" is the maintainer's statement, consistent with but not proven by the evidence (FINDINGS §0). The hash was not said aloud (the maintainer does not speak on films, chat 2026-10-01). Google Play services and the Google app present (Play services' Fast Pair holds DLCI 0x04 between the app's claims; the Google app opens DLCI 0x08/0x0a). Play services *Nearby devices*: **allowed** — the maintainer's statement in chat 2026-10-01 (used for casting to a Chromecast; not on film). Pixel 7a Bluetooth: not recorded; the HCI log shows one classic link only (the Buds) |
| Other devices    | An LE device `c8:cc:a8:e7:48:93` (handle `0x0041`, LE Connection Complete frame 284, 10:04:27.202, 339 frames) — the Fitbit "Charge 6" (🟢 in `CAP-065-FINDINGS.md`; a tracker, not a speaker — corrected 2026-10-03) Android's Bluetooth panel shows "Verbonden" on film at 10:04:28; excluded from the analysis |
| Video file       | `CAP-064-recording.mp4`: 1514.80 s, H.264 1280×720 rotation −90 (portrait), 45,206 video frames (≈ 29.84 fps), **AAC stereo 44.1 kHz audio, 1514.31 s** (decodes). Burned-in overlay `Oct 1, 2026 HH:MM:SS`: **first frame 10:04:14, last frame 10:29:28** |
| Audio            | Very quiet (median −69 dBFS per second, max −39.8 dBFS); broadband handling/tap transients only, no speech (the maintainer did not speak); nothing heard inside the buds is on it (FINDINGS §0) |
| Log file         | `CAP-064-btsnoop_hci.log` — `capinfos`: 11,390 packets, "Packet size limit: (not set)" (raw path), 10:04:23.294471–10:31:21.841209; 0 `cap_len≠len`; 1 out-of-order pair. Film covers 10:04:14.6–10:29:28.6 phone time; the HCI log starts 9 s after the film and runs 1 min 53 s past it |
| App debug export | `CAP-064-opencontrol-debug-20261001-103023.txt` (the app's own file name; the skeleton planned the name CAP-064-debug-export.log): 1,131 lines (the last has no newline), 08:53:29.939–10:30:19.222; the 20,000-line ring buffer did not wrap (first line = the process start). Debug mode was already on at 10:04:42 (film): every frame of the film window is hex-logged |
| App logcat       | `CAP-064-OpenControl-for-Pixel-Buds-log-49ab12ce3f7f.txt` (401 lines; events from 06:53:29 UTC; `OpenControlBuds` lines only 08:24:15.644–08:29:43.255 UTC); last line "Wrote stack traces to tombstoned" = the bug report's `dumpstate` (FINDINGS §0) |
| System log       | `CAP-064-System-log-11c30e3704a6.txt` (61,308 lines; 09-30 05:32 … 10-01 08:31 UTC; no lines of the Bluetooth process itself) |
| Events file      | none (P6 not done); no `CAP-064-btsnoop_hci.log.last` |
| Clock offsets    | **Film ↔ phone, measured at both ends** (5 fps crops of the status bar against the overlay): the phone's minute flips 10:04→10:05 at t ≈ 45.4 s and 10:28→10:29 at t ≈ 1485.5 s, both **0.6 s into overlay second :59** ⇒ **phone = overlay + 0.4 s (±0.2 s), no drift**; overlay ≈ 10:04:14.2 + t. **Logcat / system log (UTC) = phone − 2 h 00 min 00.000 s** (logcat 08:27:44.886 "Session ended by the user's Disconnect tap" = export 10:27:44.886; HCI phone `DISC` DLCI 0x03 frame 10905 at 10:27:44.949). HCI and the export are phone local time |
| Buds (partial, `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` (ADR-010). Classic handle `0x000b` for every ACL of the run (Connection Complete frames 907, 1872, 5073, 5769, 7204, 8414). **Pre-filter:** `bluetooth.addr` is empty with this encapsulation — every command filters `bthci_acl.chandle==0x000b`. **DLCIs are session-local:** while the phone opened the RFCOMM multiplexer (until 10:17:31) MAESTRO = 0x02, Message Stream = 0x04; after the Buds opened it (SABM DLCI 0x00 frame 8617, 10:17:32.040) MAESTRO = 0x03, Message Stream = 0x05 — identified by content (pw_hdlc / `[Group][Code][Length]`) |
| Wear mapping     | The maintainer's rule (chat 2026-10-01): **head on the right of the frame = Left bud, head on the left = Right bud.** Slots: the bud the head-left hand takes from / puts into the **right-hand (upper) slot of the frame** is the Right bud. Cross-checked against the runtime-info stream's per-bud charging field at 5 dock changes (frames 4929, 6228, 6517, 7054, 9437): **0 contradictions** (FINDINGS §4) |

## Capture-integrity pre-flight

```
$ capinfos CAP-064-btsnoop_hci.log
File encapsulation:  Bluetooth H4 with linux header
Packet size limit:   file hdr: (not set)
Number of packets:   11 k   (Interface #0: Number of packets = 11390)
Earliest packet time: 2026-10-01 10:04:23.294471
Latest packet time:   2026-10-01 10:31:21.841209

$ tshark -r CAP-064-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len -e frame.time_epoch \
  | awk '$2!=$3{c++} {if(NR>1 && $4<p) o++; p=$4} END{print "packets:",NR," mismatches:",c+0," out-of-order:",o+0}'
packets: 11390  mismatches: 0  out-of-order: 1

$ ffprobe … CAP-064-recording.mp4   → audio aac 44100 Hz 2 ch 1514.309 s (65,215 frames); video h264 1280x720, r_frame_rate 90000/3013,
                                       45,206 frames, 1514.804 s, side data rotation −90; creation_time 2026-10-01T08:29:28Z

$ sha256sum *   (identical before and after the move into this folder, `sha256sum -c`, 2026-10-01)
e3f6b44d…c6effc  CAP-064-btsnoop_hci.log                         7e94e0b8…156f40  CAP-064-opencontrol-debug-20261001-103023.txt
9702c0dc…1d62f6  CAP-064-OpenControl-for-Pixel-Buds-log-…txt     27afe1b5…1951d8a  CAP-064-recording.mp4
32deb6c1…b1cec4  CAP-064-System-log-11c30e3704a6.txt
```

File modes: the four files the phone produced were `-rwxr-----`; set to `644` on the maintainer's approval (chat 2026-10-01). `git check-attr filter` → `lfs` for all five capture files.

## Video and audio review method

- **Every second:** the whole film was extracted at 1 fps (1,515 frames, `ffmpeg -vf fps=1,scale=405:720`) and viewed at 2 s spacing on 95 contact
  sheets (4 × 2); every transition a claim depends on was narrowed with 1, 2, 4 or 5 fps crops at full resolution (the status-bar minute flips, the
  lid opening, the Connect taps 10:04:50 / 10:16:11 / 10:16:42, the I-4a battery bars, the ANC tap 10:07:27, the tile taps, AZ-2/AZ-4 boxes, the battery
  (i) dialog, the I-5 note, the in-ear note).
- **Audio:** decoded to 16 kHz mono; per-second RMS level over the full 1514 s and a full-length spectrogram (`ffmpeg … showspectrumpic`, zoomed
  400–480 s). 83 seconds rise more than 12 dB above the median — short broadband transients (handling, taps, the case); no sustained harmonic speech
  pattern; one faint harmonic pattern at t ≈ 474–480 s (≈ 10:12:08–14) is unidentified. The audio is used for nothing below.
- **Privacy (ADR-037):** no camera address overlay. The notification shade is legible twice — 10:23:34–36 (WhatsApp group and contact names with
  message snippets, LinkedIn with a third person's name, a bank's "€ 9,00 afgeschreven" notice, Play Integrity, USB debugging, Volkskrant) and
  10:24:56–10:25:02 (the same plus the Spotify card "Back To Black – Amy Winehouse" playing on "Pixel Buds Pro…"). The maintainer's own head, hair,
  glasses and ears are on film by design. **The maintainer chose to keep the film unblurred** (chat 2026-10-01, `ai-sessions/0060`).
- **Wear:** the ears are on film as planned (P7). Each wear row below names the side of the frame the head was on and the bud that follows from the
  maintainer's mapping.

## Event Timeline

Times are **phone local time** (HCI / debug export); film-only events are overlay + 0.4 s, to ±1 s. Frame numbers are HCI frames of
`CAP-064-btsnoop_hci.log` (handle `0x000b`); "export N" is line N of the debug export. "Step" = the skeleton step (Appendix A); "var." = done
differently, "rep." = repeated. Registry Test-IDs in brackets.

| Time (phone) | Action / event | Actor | Step / test | Evidence |
|---|---|---|---|---|
| 10:04:14–10:04:22 | Film starts in Quick Settings: dialog "Bluetooth staat uit"; Bluetooth switched **on** (10:04:22) | User (Android) | S1 | film; HCI log starts 10:04:23.294 |
| 10:04:24–28 | Bluetooth list: "Niro", "Pixel Buds Pro 2 …", "Charge 6" all "Opgeslagen"; "Charge 6 Verbonden" at 10:04:28 | OS | — | film; LE Connection Complete handle `0x0041` frame 284 (10:04:27.202) |
| 10:04:34.9 | OpenControl opened (Controls tab: "Not connected to the Buds. Controls are disabled …"); link observer started, `NOT_CONNECTED` | User / App | S1 | export 216–217; logcat `wm_on_resume_called` |
| 10:04:40 | Connection tab: "Paired — not connected …", "App control: not open yet", the earlier loss text ("… while the app was in the background …", from 09:15:58) | App | — | film; export 207–213 |
| 10:04:42–46 | Top bar bug → Debug: **Debug mode already on**, "Unidentified frames (38)" | User | S2 (already on) | film |
| 10:04:50.9 | **Connect tap, lid closed, both buds docked** → phone Create Connection (673) → Page Timeout `0x04` after 5.14 s (699) → "Couldn't open the Maestro channel … read failed … " + Retry | User / App | — (not in the skeleton) [`PAIR-003`] | export 218–220; film 10:04:56 |
| 10:05:10–16 | Lid **opened** (both buds seated, LED); the screen stays "Couldn't open …"; **nothing on the HCI log** (no Connection Request, no Create Connection) | User | I-0 var. [`CASE-003`] | film; HCI (no event 699 → 903) |
| 10:05:28–31 | Both buds taken out (upper/right slot first), laid left and right of the case | User | I-0 [`CASE-005`, `CASE-004`] | film |
| 10:05:30.33 | **Buds' Connection Request** (903) → ACL (907); HFP/A2DP/AVRCP up | Buds / OS | — | HCI |
| 10:05:31.0–10:05:32.4 | **Opened by itself:** link `CONNECTED` → "Automatic re-open (ADR-044, trigger: LINK_BACK)" → `SABM` 0x02 (1198) → announcement ch 21 (1215) → reads 16, 2, 4, 7, **12**, 17, 19, 22 (1231…1279) → `SubscribeRuntimeInfo` (1282, Case 93) ; snapshot claim `08 11` (1245) → `08 13 … 01 e8 00 20` (1257) | App | **I-0 ✓** (positive control of ADR-044 1(b)) [`PAIR-003`] | export 221–258; film "ready" 10:05:32 |
| 10:05:52 | ANC tab: "ANC can only be changed while you wear the Buds. Tapping a mode checks again first.", Off, buttons **enabled** (no "(checked …)" time on the main surface) | App | I-0 ✓ (wording) | film |
| 10:06:00–06 | Both buds put back into the case (lid open) → ACL `0x13` (1808, 10:06:04.559); "Not connected to the Buds …" | User / Buds | (not planned) [`CASE-006`] | export 273–279; film |
| 10:06:12–16 | Both buds taken out again, laid on the table → Buds' Connection Request (1868) → **re-open by itself** (LINK_BACK 10:06:13.829, `SABM` 2239); snapshot `Notify 01 e8 e8 20` (2299, buds in the hand/on the table) → no wear note | User / App | I-0 rep. | export 280–330; film 10:06:16 |
| 10:06:36.3 | **ADAPTIVE tapped, both buds on the table** → claim (attempt 1 fails, `SABM` 2672) → `Set 40` (2687, no `Get`) → **ACK** (2698) → `Notify 01 e8 e8 40` (2699) → "Adaptive" | User / App | I-1a var. (precondition "disabled" not met) [`ANC-003`] | export 331–341; film 10:06:38 |
| 10:06:44–52 | Bud from the right of the case into an ear, **head on the left of the frame ⇒ Right** | User | I-1b [`INEAR-003`] | film |
| 10:06:58–10:07:06 | Bud from the left of the case into an ear, **head on the right ⇒ Left**; both worn | User | I-1b [`INEAR-002`] | film |
| 10:07:27.4 | **ADAPTIVE tapped, both worn** (finger on film 10:07:26.6–27.4 overlay) → claim → `Set 40` (2977, no `Get`) → ACK (2988) | User / App | I-1b var. (`Set` without `Get`: the app's last `Notify` read `e8`) [`ANC-003`] | export 348–357 |
| 10:07:42–46 | Head left ⇒ **Right** bud out, laid right of the case → Buds `DISC` 0x02 (3103, 10:07:44.330) → re-open 1.5 s later (AFTER_LOSS, `SABM` 3130, ch 19) | User / Buds / App | I-1c [`INEAR-004`] | export 358–399 |
| 10:07:50–56 | Head right ⇒ **Left** bud out, laid left of the case; Play services' claim reads `Notify 01 e8 00 20` (3299, 10:07:54.689) — the app does not see it | User / Buds | I-1c | film; HCI |
| 10:08:08–12.8 | Quick Settings pulled down, page 2; **ANC tile tapped** (buds on the table) → claim → `Set 20` (3433, no `Get`) → **NAK `ff 02 00 03 02 08 12`** (3440, reason 0x02) → `Notify 01 e8 00 20` (3443); no toast | User / App | I-1c var. (a `Set` was sent and NAKed — FINDINGS §2) | export 404–417; film 10:08:12–18 |
| 10:08:30–34 | Head left ⇒ **Right** bud in → Buds `DISC` 0x02 (3568, 10:08:35.026) → AFTER_LOSS re-open (3597), snapshot `e8 e8 40` | User / App | I-1d [`INEAR-003`] | export 420–461 |
| 10:08:42–46 | Head right ⇒ **Left** bud in; both worn | User | I-1d [`INEAR-002`] | film |
| 10:08:54.6 | **ANC tile tapped** (both worn, app not opened) → `Set 20` (3835) → ACK (3843): ADAPTIVE → OFF, no toast | User / App | I-1d ✓ (without `Get`) [`ANC-001`] | export 462–473; film 10:08:54–56 |
| 10:09:20–24 | Shade closed, back to the app: ANC tab "OFF" | User | — | export 474–479 |
| 10:09:28–32 | Head right ⇒ **Left** bud out, laid **left of the case**; Right stays worn | User | AY-3a [`INEAR-004`] | film |
| 10:09:47 | **Refresh** → claim → `Get` (4091) → **`Notify 01 e8 e8 20`** (4103, 10:09:49.393): one bud worn ⇒ `e8`; no wear note | User / App | **AY-3a ✓** | export 482–490; film 10:09:46 |
| 10:10:06–12 | Head right ⇒ **Left** bud back in (both worn) | User | AY-3b | film |
| 10:10:16–24 | Head left ⇒ **Right** bud out, laid right of the case → Buds `DISC` 0x02 (4261, 10:10:20.842) → AFTER_LOSS re-open (4288), snapshot `e8 e8 40` | User / App | AY-3b [`INEAR-004`] | export 493–534 |
| 10:10:40 | **Refresh** → `Get` (4521) → **`Notify 01 e8 e8 40`** (4533, 10:10:41.488): Left worn, Right on the table ⇒ `e8` | User / App | **AY-3b ✓** | export 535–543; film 10:10:38 |
| 10:10:48–52 | Head left ⇒ **Right** bud back in (both worn) | User | AY-3c | film |
| 10:10:58 | **Refresh** → `Get` (4708) → `Notify 01 e8 e8 20` (4720) | User / App | AY-3c ✓ | export 546–554; film 10:10:56–11:00 |
| 10:11:16 | Connection tab: "Connected to this phone (Android) ✓ App control: ready", L 100 % / Case 92 % (dimmed) / R 100 % | App | — | film |
| 10:11:30.9 | **Home** (app in the background) | User | I-2a | export 559; logcat `wm_on_stop_called` |
| 10:11:42–58 | Head left ⇒ **Right** bud out and seated (right/upper slot, 10:11:46–50; stream 4929 6.3.2 = 2), head right ⇒ **Left** seated (10:11:56–58) → ACL `0x13` (4978, 10:11:59.873); no app `SABM` while away | User / Buds | I-2a [`CASE-006`] | HCI; export 567–570 |
| 10:12:13.7 | Back (Recents): "Paired — not connected …", **"Android no longer showed the Buds connected when you returned to the app — the connection ended while the app was in the background (for example both buds in the case, or out of range). Tap Connect to reconnect."** | App | **I-2a ✓** | export 571–573; film 10:12:14–16 |
| 10:12:35.5 | **Home** again | User | I-2b | export 574 |
| 10:12:36–52 | The right-slot (**Right**) bud out onto the table and back into the slot; meanwhile the Buds' Connection Request (5069, 10:12:40.837) → ACL; Play services / the Google app open DLCI 0x04/0x08/0x0a (5330–5556); `Notify 01 e8 00 20` on Play services' claim (5591, one bud on the table, one docked); ACL `0x13` (5675, 10:12:53.107) as the bud is re-seated; **no app `SABM` 0x02** | User / Buds / OS | I-2b var. (the ACL came and went while away; no session to lose) | HCI |
| 10:12:57.4 | Back (Recents): still "Paired — not connected …" (link `NOT_CONNECTED`), the I-2a text | App | I-2b | export 576–577; film 10:12:58 |
| 10:13:30–33 | **With the app visible, the right-slot bud taken out** → Buds' Connection Request (5765, 10:13:32.204) → **re-open by itself** (LINK_BACK 10:13:32.840, `SABM` 6129) → "ready" with no tap (film 10:13:33) | User / Buds / App | (not planned) — **positive control** of ADR-044 1(b) | export 578–622; film 10:13:30–33 |
| 10:13:33–44 | Head left ⇒ **Right** bud into the ear; head right ⇒ **Left** out of the slot into the ear; stream 6228 (10:13:34.075: Left charging, Right not), 6517 (10:13:42.85: both out) | User / Buds | [`CASE-005`, `CASE-004`, `INEAR-002/003`] | film; HCI |
| 10:13:54–10:14:04 | Sound tab: EQ −1/0/4/2/0, Balance "Right 4", Mono off, Conversation detection on | App | I-3 pre-state | film |
| 10:14:06–10:15:24 | **Balance drags** (both worn): 26 releases, each one `WriteSetting 4:{17:n}` → `RESPONSE` OK: Left 43, Right 9, 9, 7, 7, **Right 4** (10:14:28.24, not snapped), Left 7, **Centre** (10:14:43.30), Right 13, 9, 7, **Centre** (:53.33), Right 10, **Centre** (:57.17), Right 6, **Centre** (10:15:00.39), Right 11, 9, 8, Left 12, Right 5, Left 10, 9, 11, 5, **Centre** (10:15:23.88) | User / App | **I-3a–d** var. + rep. [`AUDIO-003`] | 6571…6856; export 627–708; film "Left 43" 10:14:08, "Right 4" 10:14:28–38, "Centre" 10:14:44, 10:14:58–15:00, 10:15:24+ |
| 10:15:42–46 | Head left ⇒ **Right** bud out and seated (right slot) → Buds `DISC` 0x02 (6938, 10:15:45.551) → re-open (AFTER_LOSS, 6971); read `4:{17:0}` (7044) — balance persisted | User / App | (not planned) [`CASE-006`] | export 711–751 |
| 10:15:50–56 | Head right ⇒ **Left** bud out and seated → ACL `0x13` (7147, 10:15:55.888) ends the session (export 752, 10:15:55.900; no Buds `DISC` first); **"Automatic re-open skipped: the session was lost 8237 ms after an automatic re-open"** | User / Buds / App | (not planned) [`CASE-006`] | export 752–758 |
| 10:16:04 | Connection tab: "Paired — not connected … Android no longer shows the Buds connected to this phone — after a disconnect in Android's own Bluetooth settings, with both buds in the case, or out of range. Tap Connect to reconnect." + Connect; **both buds docked, lid open, no ACL** | App | — | film 10:16:04–10 |
| 10:16:11.4 | **Connect tap** (both docked, lid open) → **the phone pages the Buds** (Create Connection 7201) → ACL 10:16:12.380 (7204) → ready 10:16:12.59; link `CONNECTED` only afterwards (10:16:14.384) | User / App | (the "had to tap Connect" moment — FINDINGS §1) [`PAIR-003`] | export 759–796; film 10:16:10–14 |
| 10:16:26.6 | **Disconnect** (both docked) | User | I-4a | export 797; 7836 |
| 10:16:32–40 | Both buds taken out (right slot first) and laid left/right of the case; the app **stays** "not open yet" (no app `SABM` 0x02) | User | I-4a; ADR-044 item 2 ✓ [`CASE-005`, `CASE-004`] | film; HCI |
| 10:16:41.5–10:16:45 | **Connect tap** → ready 10:16:43.139; **for ≈ 1.5 s the battery card shows the previous connection's "100 %⚡" (charging) for both buds** (blue bars), replaced at ≈ 10:16:45 by "100 %" (not charging) — claim burst `64 64` (export 832, 10:16:44.784), stream 8168 without 6.1 (10:16:44.662) | User / App | **I-4a** (main surface — see FINDINGS §4) | export 799–835; film t = 747–751 s |
| 10:17:13–16 | Battery (i) dialog: "Left: 100% (updated 10:16:44) — not charging (out of the case) (10:16:44) … Case: 92% — last seen 10:16:14 (no bud charging in the case) … Firmware: release_5.203" | User / App | I-4a ✓ ((i) wording) | film (full-res crop t = 781 s) |
| 10:17:18–26 | Right-of-case bud seated (right slot) → Buds `DISC` (8294, 10:17:22.561) → re-open (AFTER_LOSS 10:17:24.09) → left-of-case bud seated → Buds `DISC` 0.77 s after the re-open (8378) → **"re-open skipped: lost 774 ms after an automatic re-open"** → ACL `0x13` (8398, 10:17:27.084) → **the phone pages back 3.4 ms later** (8399) → ACL (8414) → link `CONNECTED` → **re-open by itself** (LINK_BACK 10:17:32.498) | User / Buds / OS / App | I-4b var. (no Disconnect/Connect) [`CASE-006`] | export 836–891; film 10:17:22–32 |
| 10:17:33 | Both "100 %⚡" (charging), Case 92 % | App | I-4b ✓ | stream 8882 (both 2); film |
| 10:18:02–10:19:26 | Controls tab: touch on, Left/Right Noise control, "Modes for press and hold (both buds)" all four ticked, **I-5 note** "Digital assistant needs an assistant app on this phone that supports headphones (for example the Google app). Without one, holding the bud may only play a tone."; In-ear detection on, with its note (buds docked) | User / App | **I-5 ✓**, **AZ-1 ✓** | film (full-res t = 838, 852 s) |
| 10:19:28–44 | Head left ⇒ **Right** out of the slot into the ear → Buds `DISC` (9317, 10:19:30.185) → re-open (AFTER_LOSS, 9356); head right ⇒ **Left** out into the ear | User / App | AZ pre-state [`CASE-005/004`, `INEAR-003/002`] | export 895–936; stream 9437 |
| 10:20:24–29.8 | **Untick Adaptive** → `WriteSetting 4:{12:{1:1 2:1 3:1 4:0}}` (9684, = `CAP-041` 2192 byte for byte) → `RESPONSE` OK (9688, 10:20:29.571) → box unticked on screen from ≈ 10:20:29.6–29.8 (5 fps) | User / App | **AZ-2 ✓** [`HOLD-005`] | export 945–947 |
| 10:20:48–10:21:24 | **Long presses** on the bud with the head on the left (⇒ **Right**), Controls tab open (ANC tab not opened, no Refresh); the Buds report on Play services' claim: `Notify … 80` (9737, 10:20:50.996), `08` (9750), `20` (9762), `80` (9776), `08` (9785), `20` (9793, 10:21:22.671); AVRCP VolumeChanged 40 % (9746, 10:20:55.20) and 33 % (9789, 10:21:22.05), volume panel on film | User / Buds | **AZ-3** var. (6 presses, no Refresh): **never `40`** [`HOLD-003`] | HCI; film |
| 10:21:42–44 | **Untick Noise cancellation** (not Off) → `4:{12:{1:0 2:1 3:1 4:0}}` (9845) → OK (9849); Off and Transparency greyed, "At least two modes must stay selected" | User / App | **AZ-4** var. ✓ [`HOLD-005`] | export 954–956; film |
| 10:21:58–10:22:00 | Taps on the greyed boxes: **nothing on the wire** (no request between 9849 and 9899) | User | AZ-4 ✓ | HCI |
| 10:22:18–22 | Tick Noise cancellation (9899 `{1:1 2:1 3:1 4:0}` → OK 9903), tick Adaptive (9907 all four → OK 9910) | User / App | **AZ-5 ✓** | export 959–964 |
| 10:22:30–32 | **In-ear detection → off** → `4:{2:0}` (9922, = `CAP-056` 2849) → OK (9926); Buds' SASS `07 11 00 04 01 02 b0 00` (9927, 0.8 ms later, on Play services' claim) | User / App / Buds | **AZ-6a ✓** [`INEAR-001`] | export 965–967 |
| 10:22:44–10:23:00 | Head right ⇒ **Left** bud out, held beside the case ≈ 14 s, back in; music **not playing** (AVRCP status Stopped/Paused since 10:16:14) | User | AZ-6b (not testable: no playback) [`INEAR-004`] | film; AVRCP 7783 |
| 10:23:31–38 | Buds' AVRCP PLAY (10026) → Playing 10:23:38.86 (10044) | Buds / OS | — | HCI |
| 10:23:34–40 | Notification shade (privacy), Quick Settings, home, Recents, back | User | — | film; export 970–973 |
| 10:23:50–10:24:02 | Head right ⇒ **Left** bud out (held), back in, **music playing, in-ear detection off**: **no PlaybackStatusChanged / PAUSE** | User | **AZ-6b ✓** (rep.) [`INEAR-004`] | AVRCP: nothing between 10047 and 10494 |
| 10:24:15.6 | Buds `DISC` 0x03 (10102) ≈ 13 s after the re-insertion → re-open (AFTER_LOSS, 10129); read `4:{2:0}` (10166) | Buds / App | AZ-6b (the Buds still close the session) | export 974–1015; film 10:24:16 |
| 10:24:40–50 | Head left ⇒ **Right** out onto the table, head right ⇒ **Left** out onto the table → Buds `DISC` (10309, 10:24:50.181) → re-open; snapshot **`Notify 01 e8 e8 20`** (10394) | User / Buds / App | AZ-6c [`INEAR-004`] | export 1016–1057 |
| 10:24:56–10:25:02 | Shade pulled down (privacy), Spotify card **paused by the user** → AVRCP Paused (10494, 10:25:02.43) | User | — | film; HCI |
| 10:25:13 | **ANC Refresh**, both buds on the table, in-ear detection off → `Get` (10586) → **`Notify 01 e8 e8 20`** (10600, 10:25:16.549): no wear note | User / App | **AZ-6c ✓** (settles the 🔴) | export 1058–1066 |
| 10:26:30–44 | Head left ⇒ **Right** in, head right ⇒ **Left** in (no Buds `DISC`) | User | AZ-6d | film |
| 10:26:57.4 | **In-ear detection → on** → `4:{2:1}` (10817, = `CAP-056` 3627) → OK (10821); SASS `… b8 00` (10822) | User / App | **AZ-6d ✓** [`INEAR-001`] | export 1071–1073 |
| 10:27:06–40 | ANC tab (Off), Connection tab, Controls tab | User | — | film |
| 10:27:43–47 | **Disconnect** (10:27:44.886) → **Connect** (10:27:47.036) → reads `4:{2:1}`, `4:{12:{1:1 2:1 3:1 4:1}}` → Controls shows them (10:27:54–58) | User / App | **AZ-7 ✓** [`PAIR-003`] | export 1076–1113 |
| 10:28:18–24 | Left → Digital assistant (11126) → OK; Right → Digital assistant (11135) → OK; **the list disappears** | User / App | **AZ-8 ✓** [`HOLD-002`, `HOLD-004`] | export 1114–1119; film 10:28:20–24 |
| 10:28:32–36 | Left → Noise control (11164) → OK, **the list reappears**; Right → Noise control (11168) → OK | User / App | AZ-8 ✓, Z1 [`HOLD-001`, `HOLD-003`] | export 1122–1127; film 10:28:34–36 |
| 10:28:58–10:29:28 | In-ear on, Connection, ANC "Off"; status bar 10:28→10:29 on film; film ends 10:29:28.6 | User | Z1 (partly), Z2 | film |
| 10:29:28 → 10:30:23 | Bug report started (`dumpstate` 08:29:28 UTC); app stopped (10:29:43); export saved 10:30:19–23 (off film); the session stayed open (last stream packet 11324, 10:30:40.15) | User | X1–X3 | logcat; system log 51721, 57512–57521 |

## Step mapping (skeleton Appendix A → what happened)

| Step | Result | Evidence |
|---|---|---|
| P1 hash aloud / written | **not applicable / not done**: the maintainer does not speak on films (chat 2026-10-01); A.0 blank — the build is derived from the logs (FINDINGS §0) | — |
| P2 do not clear app data | **held**: the installer's call at 08:53:21 (system log 8044) cleared the code cache only (`flags=39`), not the app's data — `CAP-064-FINDINGS.md` §0 (corrected 2026-10-03) | system log 8044, 8048 |
| P4 *Nearby devices* state | not on film; **allowed** per the maintainer (chat 2026-10-01) | — |
| P6 events file | **not provided** | — |
| S0 minute change + spoken items | **done differently**: the status bar is on film across 10:04→10:05 and 10:28→10:29 (used for the offsets); nothing spoken | film |
| S1 Bluetooth off → on, open app | done | 10:04:14–34 |
| S2 Debug mode on | done (already on) | film 10:04:42 |
| (extra) Connect tap with the lid closed | page timeout, "Couldn't open …" | 673/699 |
| I-0 | **done differently**: lid opened 10:05:12 with the buds docked — nothing until a bud was taken out at 10:05:28–31; then ready by itself (LINK_BACK) and the ANC note shown; **repeated** 10:06:00–16 | export 222, 281 |
| I-1a | **done, precondition not met**: the Buds' `Notify` read `e8` (2299) with both buds on the table, so the mode buttons were enabled; the ADAPTIVE tap was **ACKed** (2698) | FINDINGS §2 |
| I-1b | **done differently**: `Set` without `Get` (the app's last `Notify` read `e8`) → ACK | 2977/2988 |
| I-1c | **done differently**: the tile sent a `Set` (no `Get`) → **NAK** reason 0x02, then `Notify … 00`; no toast | 3433/3440/3443 |
| I-1d | **done** (no `Get`, as I-1b) → ACK | 3835/3843 |
| AY-3a / AY-3b / AY-3c | **done** — `e8` / `e8` / `e8` | 4103, 4533, 4720 |
| I-2a | **done** ✓ | export 573 |
| I-2b | **done differently**: one bud out and back in while away: the ACL came up and went down (5069 → 5675); no app `SABM` | HCI |
| I-3a–d | **done differently, repeated** (26 releases; Centre ×5, Right 4 not snapped) | 6571…6856 |
| I-4a | **done** (main surface: previous charging bolts ≈ 1.5 s; the "last seen / last connection" words only in (i)) | FINDINGS §4 |
| I-4b | **done differently** (buds seated without Disconnect/Connect; two DISCs, chain guard, Android's own reconnect) | export 836–891 |
| I-5 | **done** ✓ | film 10:18:04–52 |
| AZ-1 | done ✓ (read at every Connect, 14/14) | FINDINGS §5 |
| AZ-2 | done ✓ | 9684/9688 |
| AZ-3 | **done differently** (6 presses, no ANC tab, no Refresh): wire modes `80 08 20 80 08 20`, never `40` ✓ | 9737…9793 |
| AZ-4 | **done differently** (Noise cancellation unticked instead of Off) ✓ | 9845/9849 |
| AZ-5 | done ✓ | 9899–9910 |
| AZ-6a | done ✓ (SASS `b0 00`) | 9922/9926/9927 |
| AZ-6b | **done twice**: 1st without playback (not testable), 2nd with playback ✓ no pause | AVRCP |
| AZ-6c | done ✓ (`e8`) | 10394, 10600 |
| AZ-6d | done ✓ (SASS `b8 00`) | 10817/10821/10822 |
| AZ-7 | done ✓ | export 1076–1113 |
| AZ-8 | done ✓ | 11126…11168 |
| Z1 restore | **partly**: in-ear detection on ✓, ANC-mode list all four ✓, press-and-hold Noise control ✓; **balance not restored** (start Right 4 = `17:7`, end Centre `17:0`); ANC Off | reads 10:27:47 |
| Z2 minute change | done (status bar 10:28→10:29 on film) | film |
| `CAP-065` steps done anyway | **one, by accident**: "one bud docked, the other loose on the table" with in-ear detection on — `Notify 01 e8 00 20` on Play services' claim (5591, 10:12:44.95) | FINDINGS §3 |

**Traceability (`AGENTS.md` §13.7):** every skeleton step (S0–S2, I-0, I-1a–d, AY-3a–c, I-2a/b, I-3a–d, I-4a/b, I-5, AZ-1 … AZ-8, Z1/Z2) appears above
with a result. Registry Test-IDs the skeleton names: exercised — `ANC-001` (tile 10:08:54), `ANC-003` (10:06:36, 10:07:27), `INEAR-001` (AZ-6a/d),
`INEAR-002`/`INEAR-003` (every insertion, ears on film), `INEAR-004` (every removal), `HOLD-001`–`HOLD-004` (AZ-8), `HOLD-005` (AZ-2/4/5),
`CASE-004`–`CASE-006`, `BATT-004` (every claim's `03 03` burst), `AUDIO-003` (I-3), `PAIR-003` (AZ-7, I-4a, 10:16:11). **Not exercised in the app:**
`ANC-002` (Noise Cancellation) and `ANC-004` (Transparency) were never tapped in OpenControl — both modes occur only through the AZ-3 long presses
(9750, 9737).

## Analysis checklist

- [x] Integrity pre-flight, hashes, audio check (present, no speech).
- [x] Pre-filter by handle `0x000b`; the LE device `0x0041` excluded; session-local DLCIs identified by content.
- [x] Clock offsets measured at two minute flips (phone = overlay + 0.4 s); logcat/system log − 2 h.
- [x] DLCI 0x02/0x03 decoded in full (384 pw_rpc packets, all CRC-valid); DLCI 0x04/0x05 message-level parse (every claim, app and Play services).
- [x] AY-3, AZ-6c and every other `Notify` against the film's wear state (FINDINGS §3).
- [x] "Refuted if" lists applied literally (FINDINGS §2).
- [x] Session-end and lid-open/auto-connect tables (FINDINGS §1, §6).
- [x] Registry Test-IDs referenced above; the Capture Index row, Group AZ and `id_registry.csv` are updated by `ai-sessions/0060`.

---

## Appendix A — the procedure as planned (the skeleton, written 2026-09-28 by `ai-sessions/0054`/`0056`, split by `0059`; unchanged except that the two planned file names that were never produced are no longer code-formatted)

### A.0. Which phone, which app, which build

| Item | Value |
|---|---|
| Phone | **Pixel 9a, GrapheneOS** (Settings → About phone → build number: `__________`) |
| App under test | **OpenControl for Pixel Buds**, debug APK of the commit that completes `ai-sessions/0059` (it contains the `0054`, `0056`, `0057` and `0059` builds). Commit hash: `__________` (`git log -1 --format=%H`) — **also say it aloud on film** (P1) |
| Official Pixel Buds app | **Not used.** Pixel 7a: **Bluetooth off** for the whole session (multipoint would add a second phone's traffic) |
| Other Bluetooth devices | Watch, car, speaker: off or out of range; write down anything that connects anyway (`CAP-063`: a "Charge 6" speaker was connected) |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` (the app shows it; anything else is Safe Mode — stop and report) |

### A.1. Log Metadata (fill in)

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-064`                     |
|      Group(s)    |                        AZ                          |
|       Date       |                                                     |
| Firmware version | (from the Connection tab "Firmware: …")             |
|   Test device    | Pixel 9a, GrapheneOS, build `…`; OpenControl commit `…`; Google Play services present: yes/no; Play services *Nearby devices*: allowed/denied (P4) |
| Video file       | `CAP-064-recording.mp4` — first/last overlay time: … / … |
| Log file         | `CAP-064-btsnoop_hci.log` — raw path used (`FS/data/log/bt/…` or `FS/data/misc/bluetooth/logs/…`) or `btsnooz.py`: … |
| App debug export | CAP-064-debug-export.log (planned name, not produced) (saved with Android's "save as" dialog — the whole log, not cut at 64 KiB; Debug mode on from …) |
| App logcat       | `CAP-064-OpenControl-for-Pixel-Buds-log-<id>.txt` |
| System log       | `CAP-064-System-log-<id>.txt` |
| Events file      | CAP-064-events.txt (planned, not produced) (your own time + action notes, P6) |
| Clock offsets    | Film ↔ phone: minute change filmed at start (`__:__`) **and** end (`__:__`) — measured in the analysis |

### A.2. Preparation — before filming

Tick each box. The lessons of `CAP-063` are marked **(CAP-063)**.

| # | Check | Done |
|---|---|---|
| P1 | Build the debug APK of the commit in A.0 (`cd android && ./gradlew assembleDebug`), install it (`adb install -r app/build/outputs/apk/debug/app-debug.apk`). Write the hash in A.0 **and say it aloud on film at the start** **(CAP-063: the hash was not recorded)** | ☐ |
| P2 | Do **not** clear the app's data | ☐ |
| P3 | Developer options: **Bluetooth HCI snoop log = Enabled**, **USB debugging = on**; then switch Bluetooth **off and on on film** (the log starts after a toggle) | ☐ |
| P4 | Settings → Apps → Google Play services → Permissions → *Nearby devices*: write **allowed / denied**, **say it aloud on film**, leave it as it is **(CAP-063: not recorded)** | ☐ |
| P5 | Buds and case charged (> 50 %); buds in the case, lid closed, at the start | ☐ |
| P6 | **Events file** ready (notes app on another device, or paper): the phone time (HH:MM:SS) and the step ID for every action, e.g. "I-1b 14:02:11 tap ADAPTIVE" **(CAP-063: none was provided)** | ☐ |
| P7 | **Camera** on a tripod films the phone screen **and** the case/buds **and your head** in one frame, sharp enough to read the screen. For the wear steps (I-1, AY-3) **your ears must be visible on film** — turn your head to the camera when you put a bud in or take it out **(CAP-063: "worn" could never be confirmed; every wear state was "off film")**. No location/address overlay on the camera | ☐ |
| P8 | Camera microphone on; **say aloud** what you hear (ANC switching, balance). The phone's own audio is not on the film **(CAP-063: the audio track was empty)** | ☐ |
| P9 | **Do Not Disturb on**; open the notification shade only when a step asks **(CAP-063: WhatsApp/LinkedIn names and your e-mail address were readable in the shade)** | ☐ |
| P10 | A stereo test file on the phone (left/right channel test) and music you know, playable offline | ☐ |
| P11 | GrapheneOS Settings → Security → *Bluetooth auto-off timeout*: write the current value `______` (K5) | ☐ |
| P12 | Know the gestures: tap = play/pause, press-and-hold = Noise control (the setting of `CAP-063`'s end) | ☐ |
| P13 | Laptop with `adb` next to you for A.6 (start the bug report within a minute of the last action) | ☐ |
| P14 | Read this whole file once before starting | ☐ |

**Rhythm for every step:** wait ~5 s → note the time → **one** action → wait 5–10 s (longer where a step says so) → next step. Something unexpected:
**stop, say it aloud, write the time**, then continue. A repeated step is written "repeat" in the events file.

### A.3. Start of the film

| Step | Action | Expected on screen | Time (phone) | Result |
|---|---|---|---|---|
| S0 | Film the phone's status bar across a **minute change** (≥ 10 s before and after). Say aloud: date, "CAP-064, Group AZ", the commit hash, Play services *Nearby devices* state | — | | |
| S1 | Quick Settings: Bluetooth **off**, then **on** (starts the HCI log). Open OpenControl | the app connects by itself once Android shows the Buds connected (after I-0) | | |
| S2 | Top bar **bug icon → Debug**: **Debug mode on** (there is no Debug tab since `ai-sessions/0057`) | "Unidentified frames (n)" appears | | |

### A.4. Steps

"Expected on the wire" is for the analysis. DLCI 0x02 = the app's session (MAESTRO), 0x04 = the Message Stream (claimed on demand).

### I. ANC re-check on a disabled tap (I-1) and one bud worn (AY-3)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| I-0 | buds in the case, lid closed | Open the lid, take both buds out and **lay them on the table** (visible). Wait for "ready" | ready by itself (ADR-044); ANC tab: "ANC can only be changed while you wear the Buds (checked HH:MM:SS). Tapping a mode checks again first." — the mode buttons are **enabled** | app `SABM` 0x02 …; snapshot claim `08 11` → `08 13 00 04 01 e8 00 20` (Settable `00`) | | |
| I-1a | buds on the table, ANC tab | Tap **ADAPTIVE** (buds still on the table) | no mode change; the "(checked HH:MM:SS)" time moves to now; no error line | **one** app `SABM` 0x04 → `08 11 00 00` → `08 13 … e8 00 …` → **no** `08 12`; `DISC` 0x04 ≈ 1.5 s later | | |
| I-1b | same | Put **both** buds in your ears **on film** (turn to the camera); wait 10 s (the Buds may close the session and it re-opens by itself — wait for "ready"). ANC tab: tap **ADAPTIVE** | "ANC mode: ADAPTIVE (updated HH:MM:SS)"; the buttons stay enabled; you hear Adaptive | **one** claim: `SABM` 0x04 → `08 11` → `08 13 … e8 e8 …` → `08 12 00 14 01 e8 e8 40 …` → ACK `ff 01 00 06 08 12 01 e8 e8 40` — `Get` and `Set` **in the same claim** (unless the re-open's snapshot already read `e8`: then the `Set` comes without a `Get`, as before — say which) | | |
| I-1c | buds worn | Take both buds out, lay them on the table **on film**; wait 10 s; pull down Quick Settings, tap the **ANC tile** once | tile subtitle "Only while worn"; a toast "ANC can only be changed while you wear the Buds." | one claim: `08 11` → `08 13 … 00 …` → no `08 12` | | |
| I-1d | buds on the table | Put the buds in your ears **on film**, then tap the **ANC tile** once (do not open the app) | the mode switches (the one after the mode shown); no toast | one claim: `08 11` → `08 13 … e8 …` → `08 12 …` → ACK | | |
| AY-3a | both worn | Take the **Left** bud out **on film** and lay it on the table **in view**; the Right stays **visibly** in your ear. Wait 10 s. ANC tab → **Refresh** | say which: the "can only be changed…" line appears, or not | `08 11` → `08 13 00 04 01 e8 <Settable> <mode>` — **the test of "Settable `0x00` = no bud worn"** (ADR-049, 🟡): `e8` expected if one worn bud is enough | | |
| AY-3b | Left on the table, Right worn | Swap **on film**: Left **in**, Right **out** onto the table. Wait 10 s. **Refresh** | as AY-3a — say which | as AY-3a | | |
| AY-3c | one worn | Right back **in** (both worn, on film). **Refresh** | no "can only be changed…" line | Settable `e8` | | |

### II. Loss wording after returning to the app (I-2)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| I-2a | ready, app on the Connection tab, lid **open**, buds out | Press **Home** (app in the background). Put **both** buds into the case (lid stays open) so the Buds drop the link. Wait **10 s**. Return to the app | "Paired — not connected …" and **"Android no longer showed the Buds connected when you returned to the app — the connection ended while the app was in the background (for example both buds in the case, or out of range). Tap Connect to reconnect."** | ACL `Disconnection Complete` `0x13` while the app is not visible; no app `SABM` 0x02 while it is away; the debug export: "Session loss cause: the loss happened while the app was not visible; on return Android's link was down" | | |
| I-2b | (optional) ready | Home; take **one** bud out of the case and put it back (the Buds close the app's channel, the link stays); wait 10 s; return | the session re-opens by itself right away; if the text shows for a moment: "The app's channel was closed while the app was in the background; Android showed the Buds connected when you returned. …" — say what you saw | Buds `DISC` 0x02 while away; one app `SABM` 0x02 after the return | | |

### III. Balance "Centre" (I-3)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| I-3a | ready, buds worn, stereo test file playing, Sound tab | Drag Balance to about **Left 20**, release | "Left NN · changed …" | `WriteSetting 4:{17:<2·NN>}` → empty `RESPONSE` | | |
| I-3b | | Drag the knob back **near the middle** (within a few steps of it), release | the knob jumps to the middle; **"Centre · changed HH:MM:SS"**; both ears equally loud | `WriteSetting 4:{17:0}` (= `CAP-046` 1873's payload on this channel) → empty `RESPONSE` | | |
| I-3c | | Drag to about **Right 4** (just outside the snap), release | "Right 4" (not snapped) | `4:{17:7}` | | |
| I-3d | | Back near the middle, release | "Centre" | `4:{17:0}` | | |

### IV. Stale per-bud lines at Connect (I-4)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| I-4a | ready, both buds **in the case** (lid open), Connection tab shows both "charging in the case (HH:MM:SS)" | Tap **Disconnect**. Take **both** buds out onto the table (on film). Tap **Connect**. **Film the battery lines for the first 3 s** | first: "Left: 100% — last seen HH:MM:SS — charging in the case (HH:MM:SS, last connection)" (the times of the previous connection); then, within ≈ 2 s: "Left: NN% (updated …) — not charging (out of the case) (…)" | app `SABM` 0x02; stream packet without 6.1; claim with `03 03 00 03 …` | | |
| I-4b | ready, both out | **Disconnect**; put both buds **in the case**; **Connect**; film the lines | the old "not charging" lines marked "last connection" until the stream says "charging in the case"; Case "last seen" until 6.1 arrives | stream packet with 6.1 | | |

### V. Digital-assistant note (I-5)

| Step | Pre-state | Action | Expected on screen | Time | Result |
|---|---|---|---|---|---|
| I-5 | ready | Controls tab: read aloud the line under "Press and hold" | "Digital assistant needs an assistant app on this phone that supports headphones (for example the Google app). Without one, holding the bud may only play a tone." | | |

### VI. *(moved to `CAP-065`)*

The `APP_TESTPLAN.md` steps `CAP-063` skipped (F7, K4, K1–K3, L2/L3, K5, A5, (E), B4) moved to `CAP-065` (Group BA), section IV, on the maintainer's choice of
2026-09-30 (`ai-sessions/0059`): `CAP-064` stays a wear-and-settings run; the destructive steps belong at the end of the shorter robustness run.

### VII. Press-and-hold ANC-mode list and in-ear detection in OpenControl (`ai-sessions/0056`, ADR-046/047)

Pre-state for the section: ready, both buds worn **on film**, music playing, Controls tab; Left **and** Right press and hold = **Noise control** (set it with the
chips first if needed — otherwise the list is hidden, by design). Long presses: hold a bud ~2 s **facing the camera**, then wait 5 s before the next one.

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| AZ-1 | right after Connect | Open Controls; read the list aloud | "Modes for press and hold (both buds)" with Noise cancellation / Off / Adaptive / Transparency, the Buds' ticks, "read HH:MM:SS" | in the Connect burst `ReadSetting 4:12` (between `4:7` and `4:17`) → `RESPONSE 4:{12:{…}}` | | |
| AZ-2 | all four ticked | Untick **Adaptive** | Adaptive unticked, "changed HH:MM:SS" (only after the Buds' OK) | **one** `WriteSetting 4:{12:{1:1 2:1 3:1 4:0}}` (on channel 19 byte-identical to `CAP-056` 1815) → empty `RESPONSE` OK | | |
| AZ-3 | Adaptive unticked, ANC tab open | **Three long presses** on one bud, on film; after **each** press say what you hear and tap **Refresh** on the ANC tab | the ANC mode line follows each press | one claim per Refresh: `08 11` → `08 13 … <mode>`; **expected modes only `08`, `20`, `80`** (NC, Off, Transparency), **never `40`** (Adaptive) | | |
| AZ-4 | Adaptive unticked | Untick **Off**, then try to untick **Noise cancellation** | after Off: two ticked, both greyed, "At least two modes must stay selected."; NC cannot be unticked | one write `4:{12:{1:1 2:0 3:1 4:0}}` → OK; **nothing** for the NC attempt | | |
| AZ-5 | — | Tick **Off** and **Adaptive** again (restore all four) | all four ticked, "changed …" | two writes → OK each; the last `4:{12:{1:1 2:1 3:1 4:1}}` (ch 19 = `CAP-056` 1725) | | |
| AZ-6a | in-ear detection **on** (the switch reads on) | Tap **In-ear detection** → off | switch off after the OK, "changed HH:MM:SS"; the note "With it off, audio does not pause …" is always visible | `WriteSetting 4:{2:0}` → OK; the Buds' SASS `07 11 00 04 01 02 b0 00` on DLCI 0x04 **if** that channel is open (a claim) | | |
| AZ-6b | off, music playing | Take the **Left** bud out **on film** (ear visible), wait 10 s, put it back | music keeps playing (say so) | **no** AVRCP `PlaybackStatusChanged` / PAUSE; possibly a Buds `DISC` of DLCI 0x02 and the app's re-open (ADR-044) | | |
| AZ-6c | off | Take **both** buds out, lay them on the table **on film**; wait 10 s; ANC tab → **Refresh** | say which: the "ANC can only be changed while you wear the Buds…" line appears or not | `08 11` → `08 13 01 e8 <Settable> …` — **settles 🔴 "Settable with in-ear detection off and no bud worn"** (`CAP-056-FINDINGS.md` §4) | | |
| AZ-6d | off, buds on the table | Put both buds back in **on film**; Controls → tap **In-ear detection** → on | switch on after the OK | `WriteSetting 4:{2:1}` → OK; SASS `… b8 00` if DLCI 0x04 is open | | |
| AZ-7 | — | Disconnect, Connect, open Controls | the list and the switch read back as last written ("read …") | `ReadSetting 4:12` / `4:2` answers = the last writes | | |
| AZ-8 | ready | Set **both** buds' press and hold to **Digital assistant**; then Left back to **Noise control** | the list disappears while neither bud is Noise control, and reappears | the two/one `4:{7:…}` writes → OK; no field-12 write | | |

**Refuted if (section VII):** a list or switch value changes on screen before the Buds' `RESPONSE` OK; more than one `WriteSetting` per tap, or any `CLIENT_ERROR` from
the app; a field-12 write with fewer than two `1`s, or one that is not all four booleans; the NC attempt of AZ-4 puts anything on the wire; a long press in AZ-3
reaches Adaptive (then the Buds do not follow the list — ADR-046 would need a new look); in AZ-6b the phone pauses with the switch off.

**Not `CAP-064` — a later Pixel 7a capture with the official app (outside this Pixel 9a/GrapheneOS scope):** open "Customize left", untick one mode (e.g.
Adaptive), go back, open **"Customize right"** on film — does it show Adaptive unticked (one shared list, the 🟡 of `PROTOCOL.md` §4.5.3) or ticked (two lists — then
ADR-046 is superseded)? Record it as its own capture ID when it is run.

### VIII. *(moved to `CAP-065`)*

The `ai-sessions/0057` pull-to-refresh / Debug / (i) checks (the old AZ-9 … AZ-14) moved to `CAP-065` (Group BA), section V, as BA-12 … BA-17 (same steps,
same expected wire), on the maintainer's choice of 2026-09-30 (`ai-sessions/0059`).

### Z. Restore

| Step | Action | Time | Done |
|---|---|---|---|
| Z1 | Balance back to the start value; in-ear detection and the ANC-mode list back to their start values (VII); ANC as you like it | | ☐ |
| Z2 | Film the status bar across a **minute change** again, then stop the film | | ☐ |

### A.5. What you must not do

- Do not use the official Pixel Buds app, or the Pixel 7a's Bluetooth, during this session.
- Do not tap Connect where a step does not ask for it (I-0, I-1b, I-2a/b): those test the automatic behaviour.
- Do not do two actions in one step; do not skip the pauses.
- Do not open the notification shade unless a step asks (DND is on).
- Change in-ear detection and the ANC-mode list only in section VII, and leave both as they were at the start (Z1).

### A.6. After the run — within 1 minute of the last action

| # | Collect | Done |
|---|---|---|
| X1 | Bug icon → Debug → **Export debug log** (Debug mode on) → save as CAP-064-debug-export.log (planned name, not produced) | ☐ |
| X2 | `adb bugreport cap064`; the raw snoop log from `FS/data/log/bt/btsnoop_hci.log` or `FS/data/misc/bluetooth/logs/btsnoop_hci.log` (else `btsnooz.py`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3) → `CAP-064-btsnoop_hci.log`; write the path in A.1 | ☐ |
| X3 | App logcat and system log (GrapheneOS log viewer) | ☐ |
| X4 | Film → `CAP-064-recording.mp4`; its first/last overlay time in A.1 | ☐ |
| X5 | Everything, with this file and the events file, into this folder; rename the folder; `sha256sum *` | ☐ |

### A.7. Analysis checklist

- [ ] Integrity pre-flight (`capinfos`, `cap_len≠len`, out-of-order, `sha256sum`); audio track present or not.
- [ ] Pre-filter by the Buds' classic ACL handle (map it to `04:00:6e:cf:6e:07` via HCI Connection Complete) — `bluetooth.addr` is empty with this
      encapsulation (`AGENTS.md` §13.1).
- [ ] Film ↔ phone offset from the two filmed minute changes; logcat/system log UTC offset.
- [ ] DLCI 0x04 per claim: for I-1a/c exactly `08 11` → `08 13 … 00 …` and **no** `08 12`; for I-1b/d `08 11` → `08 13 … e8 …` → `08 12` → ACK in **one** claim.
- [ ] AY-3: the Settable byte of each `Notify` against the film (which bud is visibly in an ear) — a counter-example either way is the result.
- [ ] I-2: the export's loss and cause lines against the film (app in the background) and the HCI `Disconnection Complete`.
- [ ] I-3: `python3 scripts/pwrpc_decode.py CAP-064-btsnoop_hci.log | grep WriteSetting` — `4:{17:0}` after I-3b/d, answered OK.
- [ ] I-4: film frames of the first 3 s after each Connect against the stream/battery frame times.
- [ ] VII: `pwrpc_decode.py … | grep -E '4:\{?(12|2)[:\}]'` — each write one per tap, answered OK; AZ-3 `Notify` modes; AZ-6c Settable byte; AZ-6b no pause.
- [ ] Traceability (`AGENTS.md` §13.7): every step above has a timeline row or an explicit "skipped"; registry Test-IDs to reference: `ANC-001`–`ANC-004`,
      `INEAR-001`–`INEAR-004`, `HOLD-005`, `HOLD-001`–`HOLD-004` (AZ-8), `CASE-004`–`CASE-006`, `BATT-004`, `AUDIO-003`, `PAIR-003`.

**Refuted if** (the `ai-sessions/0054` build, plus the standing `0048`/`0052` criteria):
- an `08 12` in a claim whose `Notify` read Settable `00` (I-1), or a `Set` on a disabled tap **before** that claim's `08 13`;
- more than one DLCI 0x04 claim for one disabled tap, or a claim without a user tap (no automatic ANC re-check exists);
- after I-2a the screen says "The Maestro channel (equalizer) was closed" (undetermined) although the first link reading on return was "not connected", or
  it claims "The Buds closed the app's channel" for a loss read on return;
- a release within ±3 of the centre written as anything but `4:{17:0}`, or "Centre" shown for a value ≠ 0;
- right after Connect a previous connection's per-bud value shown without "last seen" / "last connection", or a new report still marked so;
- any app `SABM` 0x02 while the app is not visible or after a Disconnect tap before a Connect tap; more than one per event;
- any app frame on DLCI 0x08; a field-12 or field-2 request outside section VII's taps and the Connect reads; a settings value changed on screen without the Buds'
  `RESPONSE` (plus section VII's own criteria).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-064-2026-10-01_10-04-14_10-29-28-Group_AZ/CAP-064-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-064-2026-10-01_10-04-14_10-29-28-Group_AZ/CAP-064-EVENT-NOTES
