# Findings: `CAP-059` (Group AU — first hardware run of the `ai-sessions/0041` build, OpenControl's own app, correlated end to end)

Standardized, evidence-based extraction from `CAP-059-btsnoop_hci.log`, `CAP-059-recording1.mp4`/
`CAP-059-recording2.mp4`, `CAP-059-debug-export.log`, `CAP-059-OpenControl-for-Pixel-Buds-log-
6a07477ae6ec.txt` (app logcat), and `CAP-059-System-log-ce75839464e2.txt` (Android system log),
staged here for later promotion into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Migrated per
`ai-sessions/0043` from `DESKRESEARCH_FINDINGS.md`'s 2026-09-20 (second entry) — that document's own
stated scope is cross-capture/pattern analysis, not a single-capture record, so per `PROJECT_RULES.md`
rule 9a this file is the "current truth" home for this capture's findings going forward. Rewritten in
place, not an accumulating changelog.

- 🟢 **FACT** — directly observed in this capture, with a frame number/timestamp.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-059` · **Date:** 2026-09-20, 17:16:59–17:22:29 local · **Firmware:** 🟢 FACT
`release_5.203` · **Phone:** Pixel 9a, GrapheneOS, Android 17 `CP2A.260805.005` · **App under test:**
OpenControl for Pixel Buds (this project's own app), built from the `ai-sessions/0041` commit (`9fe4b70`)
— 🟡 timing-consistent, not independently re-verified against every log line this migration inherits.
**Log file:** `CAP-059-btsnoop_hci.log` — `capinfos` "Packet size limit: (not set)", 6,156 packets, 0
`cap_len`≠`len` mismatches (raw path, untruncated). **Video:** two camera films (a second device
filming the phone/case/buds, burned-in wall-clock overlay + microphone audio) — `CAP-059-recording1.mp4`
(40.14s) and `CAP-059-recording2.mp4` (286.49s); both now have their address-overlay lines
cropped/blacked-out before commit (`ai-sessions/0043` Phase 0 — the timestamp line is untouched;
`PROTOCOL.md`/wire evidence below is unaffected since it never depended on the overlay's address
text). **Buds MAC (partial, `DECISIONS.md` ADR-010):** `04:00:6e:cf:6e:07`.

**Clock correlation (🟢 FACT):** every timestamp below is converted to UTC first — btsnoop and logcat
are natively UTC, the app's debug export and the video overlay are phone-local (UTC+2). HCI and the
debug export agree to ≤70ms after a shift of exactly 2h (app `ConnectionState: Ready -> Disconnected`
17:19:45.720 vs. `Sent DISC Channel=1` frame 3068 at 15:19:45.719Z). The two films are camera
recordings of a second device, so their own clock had to be independently related to the phone's:
the phone's status-bar clock flips 17:18→17:19 when the overlay reads 17:18:58.6–58.8 (`ffmpeg -ss 72
-t 5 … fps=5,crop …` around the flip, confirmed again at 17:17→18 and 17:19→20) ⇒ **phone ≈ overlay +
1.3s (±0.5s)**; for recording2, `overlay(t) = 17:17:44.5 + t`. Frames extracted with `ffmpeg -i <film>
-vf fps=1/2,crop=680:1280:40:0,scale=520:-1` and every frame read (143 + 20 frames), plus
full-resolution crops where text was small.

**Method (queries used throughout):** pre-filter by connection handle, not address (`CAP-036`
lesson) — `tshark -Y "bthci_evt.bd_addr == 04:00:6e:cf:6e:07" …` → classic ACL handle `0x000b`
(Connect Complete frames 641, 3424), LE handle `0x0041` (frames 538, 5237). RFCOMM control frames:
`tshark -Y "btrfcomm.frame_type==0x2f || ==0x63 || ==0x43 || ==0x0f" -T fields -e frame.number
-e frame.time_epoch -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type` (`p2p_dir` 0 =
phone→Buds). DLCI 0x02 via `python3 scripts/pwrpc_decode.py <log>`. DLCI 0x04/0x08 via a
reassembling `[Group][Code][Length 2B][Value]` parser over `tshark -Y "btrfcomm.dlci==0x04 &&
btrfcomm.frame_type==0xef" -T fields -e frame.number -e frame.time_epoch -e frame.p2p_dir
-e data.data`.

**Purpose of this capture (per the maintainer's own events-file template, recovered — see
`ai-sessions/0043_FEATURE_RESULT_2026_09_22.md` for the recovery note):** examine whether the app is
working as intended, and specifically explain why — with Google Play services' *Nearby devices*
permission denied — a prior session's connection had ended after ~89s when the Buds themselves closed
both RFCOMM channels (DLCI 0x02 and 0x04, within 1ms) while the audio link stayed up
(`ai-sessions/0040` §2.1). Three explicit questions: (1) what HCI-level event, if any, immediately
precedes the Buds' RFCOMM `DISC`; (2) does it happen idle, with periodic commands, or with a second
host (multipoint); (3) does the Buds' periodic `07 34` (SASS) message on DLCI 4 expect an answer.

---

## 1. Three session losses, three different causes (🟢 FACT for each, from HCI + film + system log)

| App line | Phone time | HCI evidence | What caused it |
|---|---|---|---|
| `Ready -> Disconnected` | 17:19:45.720 | frame 3068 `Sent DISC Channel=1` (phone→Buds, DLCI 0x02) 15:19:45.719Z; ACL untouched; film: finger on **[Disconnect]** at overlay 17:19:43 | **The user's own Disconnect tap.** |
| `RFCOMM channel 0x02 lost (bt socket closed, read return: -1)` | 17:20:15.897 | frame 3358 `Sent DISC` on the HFP DLCI 0x0c 15:20:14.517Z, **Disconnect Complete reason 0x13** (remote user terminated) frame 3390 15:20:15.877Z; system log: `input_interaction … SystemUIDialog` 15:20:14.392Z, Settings unfrozen .535, Telecom HFP state 3(.547)→0(.689); film: Android's Bluetooth panel "Actief"→"Opgeslagen" at overlay 17:20:13→15 | **The user tapped the Buds' row in Android's own Bluetooth panel** (⚪ the tap itself is off-camera); Android tore down HFP then the ACL — a full disconnect. |
| `RFCOMM channel 0x02 lost` | 17:21:16.028 | frame 4595 `Rcvd DISC Channel=1` (Buds→phone, DLCI 0x02) 15:21:16.023Z, frame 4597 `Rcvd DISC Channel=2` (DLCI 0x04) **+5ms**, then phone `DISC`+`DM` on DLCI 4 (4598/4599); **no** `Disconnect Complete` — ACL stays up (Spotify keeps playing, Android panel still "connected" at 17:21:29) | **The Buds closed both RFCOMM channels while the ACL/audio stayed up** — the phenomenon this capture was made to investigate. |

Frames 4586–4595 (`tshark -Y "frame.number>=4562 && frame.number<=4600"`): app claim `SABM` DLCI 4
frame 4552 (17:21:14.460), Buds' `07 34` at 4567 (+118ms, `01 2f 2e 65 82 90 4f a2 1d 6d d6 c8`), the
app's `Set ANC` frame 4581 (17:21:15.565, `08 12 … 01 e8 e8 20`), Buds' ACK + Notify 4586–4589
(`08 13 … 01 e8 e8 20`, 15.644–15.665), a Case-battery push on DLCI 8 (4593, 15.683), **0.34s of
silence**, then `DISC` at 4595. No HCI event (mode change, role switch, other handle, L2CAP request)
lies between 4593 and 4595.

**Answers to the capture's own three questions:**
1. 🟢 FACT (this instance): nothing at HCI/L2CAP/ACL level precedes the Buds' `DISC` — it is the first
   thing observed; the Buds sent it on DLCI 2 first, DLCI 4 5ms later (`ai-sessions/0040` §2.1 found
   within 1ms for its own instance). The phone answered `UA` (DLCI 2) and `DISC`/`DM` (DLCI 4).
2. 🔴 OPEN — a second host cannot be seen from the phone's own HCI log (one ACL handle to the Buds;
   other advertising addresses in the log are unrelated devices); idle/periodic-command runs as
   originally planned were not performed this session (one continuous tour instead).
3. See §5 below.

**Same phenomenon as `LOG-001`'s originally-reported "89s deny-mode drop"?** 🟡 HYPOTHESIS yes (both
RFCOMM channels closed by the Buds within ms, audio link up); 🔴 OPEN why — here it came 1.56s after
an app claim that had itself been re-opened only 0.56s after the previous release (`Sent DISC` 4528 at
17:21:13.900, `SABM` 4552 at 17:21:14.460); no other claim in this capture was re-opened within <1.26s
(17:19:27.835→29.098) and none of those dropped. **Verifying experiment (not yet run):** repeat with a
deliberate <0.6s re-claim series, and separately with a genuinely idle window (no claim at all).

## 2. App's Android-state mirror received no system broadcast this run (🟢 FACT for this run; cause 🟡 HYPOTHESIS, strong)

`OsConnectionObserver` (registered `RECEIVER_NOT_EXPORTED`) logged `started` 17:17:31.179 and three
evaluations (`UNKNOWN`, no bonded address yet), then **nothing** until `stopped` 17:20:10.753 (149-line
debug export, 46 observer lines) — although the Buds were bonded at 17:17:49 (HCI Authentication
Complete frame 691, 17:17:49.121), HFP state 1→2 at 17:17:49.6/50.76, and A2DP came up (Telecom
`changed state to 1/2`, `updateAudioRoutes … bt_a2dp`). `HfpBatteryReader`'s **unflagged** receiver
logged the very same `BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED` at 17:17:49.609 and
17:17:50.760 (and four more at 17:20:15.026/.031, 17:20:25.735, 17:20:26.178) — same process, same
action, only the unflagged receiver saw it. The pairing bond receiver (also `NOT_EXPORTED`) never
logged a `bond state` line either.

**UI consequence (film):** the card read "Android doesn't show the Buds as connected (yet)" from
17:17:53 to 17:19:43, then "Paired — not connected to this phone" 17:19:45–17:19:57, while Android's
own panel simultaneously said "**Actief. Batterijniveau 100%**" (17:19:51–55). Once the observer was
re-created on resume (17:20:36.237) the initial read was correct ("Connected to this phone (Android)"
at 17:20:35, `CONNECTED via profiles [2]`→`[1, 2]`).

**Mechanism (🟡 HYPOTHESIS):** the relevant broadcasts come from the Bluetooth stack's own process,
not the system UID, and a dynamically registered receiver flagged `RECEIVER_NOT_EXPORTED` does not
receive them cross-process — the discriminating evidence is the export-flag difference alone (same
process, same intent action, one receiver logged it, the sibling receiver in the same app did not).

## 3. Bond reported a timeout although it succeeded (🟢 FACT)

`createBond()` at 17:17:48.024; HCI: LE SMP pairing (frames 560–605), classic `Create Connection`
frame 613 (CTKD, `DECISIONS.md` ADR-030), `Authentication Requested` 671, **`Authentication Complete`
691 at 17:17:49.121** — Android listed the Buds as bonded and HFP connected at 17:17:49.6. Yet the
app's debug export logs `Pairing: bond timed out` at 17:18:33.046 (45s after `createBond()`), with no
`bond state` line at all. A **double association request** is also real and independently confirmed:
`Pairing: association requested` at 17:17:41.507 and .589, system log
`CDM_AssociationRequestsProcessor: processNewAssociationRequest()` at 15:17:41.521 and .590 (the
second answered "More than one AssociationRequests are processing."), and the film shows the app
printing that exact error under the button while the first picker was still on screen (overlay
17:17:45). The second `requestAssociation` at 17:17:50.445 took the "reusing existing association /
already bonded" path (correct).

## 4. Fast Pair Message Stream (DLCI 0x04) claim contention: 4/4 first attempts collided with Play services, then 0/11 (🟢 FACT; the reason 🔴 OPEN)

Phone-side `SABM` on DLCI 4 not matched by an app `connected` line belongs to Play services: 17:17:50.299,
17:18:00.109, 17:18:18.502, 17:18:40.321. The app's own claims at 17:17:54.7, 17:18:13.4, 17:18:35.2 and
17:18:49.8 each failed once (`read ret: -1` after 192/290/280/77ms = the stack closing the incumbent's
port — frames 1454, 2039, 2265, 2445 `Sent DISC`) and succeeded on attempt 2; Play services re-opened
3.12s, 1.68s and 2.95s after the app's own releases (`DECISIONS.md` ADR-032 had previously seen
2.7–5.0s), and **not at all** after the release at 17:18:53.44 for the remaining ~9 minutes of the
session (11 later app claims, all succeeding on attempt 1). Nothing on the films or in the logs shows a
Play-services permission change mid-session — the *Nearby devices* permission state was not recorded
at capture time (🔴 **capture-procedure gap**, not a wire finding). DLCI 8 and 10 were opened
phone-side at 17:17:50.7 (frames 1135, 1088; the app never opens either) and closed at 17:19:35.06,
17:21:16.27 and 17:21:46.28, re-opened at 17:20:26.75 and 17:21:17.

## 5. `07 34` (SASS) — does anyone answer it? (🟢 FACT for this capture)

20 `07 34` frames from the Buds: one on every one of the 19 DLCI 4 opens (25–406ms after the phone's
`SABM`; ms-after-SABM sequence 341, 74, 40, 29, 29, 25, 37, 33, 31, 332, 377, 406, 393, 128, 27, 333,
189, 119, 120; first 17:17:50.640, last 17:21:38.403), plus one extra 1.32s after its own open
(17:20:01.815) — not a fixed-cadence stream in this capture. **Zero** Message Stream `ACK` (`Group
0xFF`) from the phone anywhere in the log (all 16 `Group 0xFF` frames are the Buds acknowledging the
phone's own commands, e.g. frame 2779 `ff 01 00 06 08 12 01 e8 e8 08`). Claims that never received an
answer to `07 34` lasted 1.4–4.6s (e.g. frame 4470, 17:21:09.393, released by the app 17:21:13.899)
without a drop — an unanswered `07 34` is **not**, by itself, a timer that kills the channel within
~4.6s (refutes the simple form of that hypothesis; the §1 drop followed one by 1.45s, but the same
delay elapsed unharmed in earlier claims this same session). Play services answers with `07 10`/`06 01`
(frames 1053, 1054, 7ms later) on its own claims; the app sends none.

## 6. ANC, battery, EQ, Find — wire vs. film (🟢 FACT unless stated)

- **ANC byte mapping matches the UI in 4/4 taps:** `Set` `08 12 … 01 e8 e8 <m>` (frames 2768, 2843,
  2908, 2982) and `Notify` `08 13 … 01 e8 e8 <m>` (2782, 2856, 2922, 3001) with `<m>` = `08`→UI
  "ACTIVE", `20`→"OFF", `40`→"ADAPTIVE", `80`→"TRANSPARENT" (film 17:19:21→33); Notify follows Set by
  0.26s. Audibility in the ears is not observable from a camera.
- **Battery (`Group 0x03 Code 0x03`, `DECISIONS.md` ADR-033):**
  `tshark … data.data[0:4]==03:03:00:03` → `e4 e4 ff` in every claim up to 17:19:03.2 (frames
  1056…2595: both buds seated in the case, film "Left/Right 100% (charging)"), `64 64 ff` from the
  claim at 17:19:22 on (frames 2776…4882: both buds out since ≈17:19:11, film "Left 100%, Right
  100%"). `0xe4 = 0x80|0x64`. The Case byte is `ff` in **all 60** frames — 🟢 FACT: this message
  cannot be the Case's battery source.
- **Case on DLCI 0x08:** `Group 0x0e Code 0x01` pushed 19 times (first frame 1211, 17:17:50.895, ≈165ms
  after the phone opened DLCI 8): `0a 21 0a 03 61 6c 6c 12 1a 0a 06 08 64 10 01 18 01 0a 06 08 64 10 01
  18 02 0a 06 08 61 10 01 18 03 20 01` = Left 100 (`08 64 … 18 01`), Right 100 (`… 18 02`), **Case
  0x61 = 97% (`08 61 10 01 18 03`)** (`DECISIONS.md` ADR-014 index mapping); identical value in all 19
  pushes, no independent ground truth on film. 10 of 19 pushes follow an ANC Notify by 9–278ms, five
  cluster at the bud removals (17:19:05.95–13.9) — 🟡 HYPOTHESIS for what actually triggers the push
  (every push this capture saw was still phone-initiated by opening DLCI 8; see `DECISIONS.md`
  ADR-035 — whether the Buds ever push *without* a phone-side request is not answered by this
  capture).
- **EQ (`pw_rpc`, `python3 scripts/pwrpc_decode.py`):** 36 packets, all `status=OK`, zero
  `CLIENT_ERROR`/`SERVER_ERROR`/`NOT_FOUND`. The unsolicited `GetSoftwareInfo` (`call_id 0xFFFFFFFF`)
  is the first DLCI 0x02 packet in 4/4 connections (22ms, 102ms, 30ms, 58ms after `UA` — frames
  1420→1431, 3177→3190, 3503→3512, 4827→4843); channel 21 on connections 1–3 (`00 4b`/`00 a5`) and 19
  on connection 4 (`00 3b`/`80 a3`) — no new channel pair seen. The app sent nothing before its first
  `ReadSetting 4:16` (frames 1444, 3197, 3522, 4848) and got an answer 4/4 — "a fresh client needs no
  opening message" is now 🟢 FACT on 4 connections (was 🟡 HYPOTHESIS). First read
  `4:{16:{1:f32(-5.00) 2:f32(-1.50) 3:0 4:0 5:0}}` (frame 1455) matches the film's first EQ screen (Low
  bass −5, Bass −1.5). 12/12 writes on the mirrored channel answered `RESPONSE status OK` (frames
  2171…2216 ch21, 4927…4998 ch19); the next connection's read returns the last write (`[-2,0,2,3,5]` =
  Clarity preset, frames 3202, 3558, 4855) — writes persisted in the Buds; the film's slider positions
  track the reads.
- **Find My Buds:** Buds ACK `ff 01 00 03 04 01 00` (frames 2310, 2491, 2506, 2600) for the four
  commands `04 01 02` (17:18:35.842), `04 01 00` (:50.48), `04 01 01` (:51.90), `04 01 00`
  (17:19:03.219). **Audible on the camera's microphone:** `ffmpeg -af
  asetnsamples=n=1600,astats=metadata=1:reset=1,ametadata=print` shows a noise floor of ≈−75dBFS and
  tone bursts of 1.3–1.4s every **3.0s** at −39…−44dBFS at 17:18:38.9/41.9/44.9/47.9 (Ring Left) and
  17:18:55.0/58.0/61.0 (Ring Right), none after the Stop at 17:19:03.2 (phone-mapped, ±0.5s) — ringing
  starts ≈3.1s after the command and continues after the Message Stream socket itself was released
  (17:18:37.37), stopping only on the Stop command. Not reflected in the app's own UI (no status
  text) — a UI gap, see §9.

## 7. HFP battery: present on the wire, absent from the app (🟢 FACT)

`tshark -Y bthfp` shows `AT+BIEV=2,100` sent by the Buds (frames 1216, 2624, 2631, 2685, 2694, 3077,
3097 = 17:17:50, 17:19:05, :06, :10 ×2, :47, :50 — at the bud removals and after re-attachment); the
value is the Right earbud's per `DECISIONS.md` ADR-015, never the Case. The app's debug export
contains **only** `HFP broadcast received: CONNECTION_STATE_CHANGED` (six times) and **no**
vendor-specific event, despite the receiver being registered from 17:17:25.364. The wire-level fact
underlying ADR-015/ADR-023 stands; the app-side HFP route delivered nothing this session (HFP battery
was subsequently removed from the app — `ai-sessions/0042`).

## 8. Fast Pair BLE battery advertisement carries no readable Case value here (🟢 FACT for this capture)

741 `0xFE2C` service-data advertisements
(`tshark -Y "btcommon.eir_ad.entry.uuid_16 == 0xfe2c" …`), 315 from the Buds' own public (bonded)
address, so a `ScanFilter` on it is possible. Parsed as flags + `LLLLTTTT` fields, the 54 distinct
payloads are `10 | 50 <5B account-key filter> | 21 <2B salt> | 46 <4B>` (e.g. `10 50 90 96 06 01 11 21
73 ec 46 e2 dd f1 fb`) — a field of type `0x46`, where the official Fast Pair Battery Notification
would carry `0x33`/`0x34` then three `0bSVVVVVVV` bytes; no payload here contains that marker at the
expected field position. `PROTOCOL.md` §4.3 Option A's `CAP-011` result (inconclusive) stands and this
is a second, cleaner non-match. GATT (Option D): LE discovery on handle `0x0041`
(`tshark -Y "btatt.opcode==0x11" -T fields -e frame.number -e btatt.uuid16"`, frames 704, 768, 5275)
lists services `0x1800 0x1801 0x1849 0x184c 0x1855 0x1858 0x185b 0xfcf1 0xfef3`, and on the second
connection additionally `0x180a` — **no Battery Service `0x180f`**, zero `0x2a19` matches anywhere in
the log: 🟢 FACT for this firmware/capture.

## 9. App defect: Case/battery values shown with no freshness indicator (🟢 FACT)

While the session was open the battery card showed "Left/Right 100%" from the last claim only, with
nothing indicating its age — the film shows values from 17:17:55 still displayed at 17:19:37, after the
buds had already left the case at 17:19:06 (the value shown happened to still be correct, from the
17:19:22 claim, but the card gave the viewer no way to know that at the time). Acceptable per
`DECISIONS.md` ADR-032 ("only as fresh as the last claim"), but the card did not say *when* that claim
happened — see `ai-sessions/0043` Phase H task 28 (replacing "(last known)" with an actual timestamp),
which addresses this directly.

---

**Result:** nothing in this file is promoted to `PROTOCOL.md` as FACT by this migration — that needs
the maintainer's own chat approval (`AGENTS.md` §6). The action items these findings drove are recorded
in `ai-sessions/0042_FEATURE_RESULT_2026_09_20.md`.

**Promoted to `PROTOCOL.md`:** nothing yet from this capture specifically.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-059-2026-09-20_17-16-59_17-22-29-Group_AU/CAP-059-FINDINGS.md

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-059-2026-09-20_17-16-59_17-22-29-Group_AU/CAP-059-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-059-2026-09-20_17-16-59_17-22-29-Group_AU/CAP-059-FINDINGS
