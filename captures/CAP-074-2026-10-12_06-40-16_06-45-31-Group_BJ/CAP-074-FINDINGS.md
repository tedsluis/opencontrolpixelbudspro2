# Findings: `CAP-074` (Group BJ — the released 1.2.0, build `5b4d5db`, in the GrapheneOS Owner user **with** Google Play services: why only the first press-and-hold after a Connect changes the ANC mode on screen)

Standardized, evidence-based extraction from the screen recording (with its audio track), the two HCI snoop logs, the debug export, the app logcat and the
system log, per `ai-sessions/0086`. The timeline these findings refer to is `CAP-074-EVENT-NOTES.md` (prefixes B, A, E, L, X, S as defined there; all times phone
time (+02:00) unless marked UTC; S cited as film seconds `t`, phone = 06:40:15.8 + t ± 0.2 s). The maintainer's run had no skeleton; what it was is in the
EVENT-NOTES ("The maintainer's steps, as run").

- 🟢 **FACT** — directly observed in this capture (frame number, log line, film time or `file:line` given).
- 🟡 **HYPOTHESIS** — plausible reading, not yet independently confirmed; the settling experiment is named.
- ⚪ **ASSUMPTION** — not tested here.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-074` · **Date:** 2026-10-12, phone 06:40:15.8–06:45:31.0 · **Firmware:** 🟢 `release_5.203` (10 of 10 announcements; Info tab S t=6) · **Phone:**
Pixel 9a, GrapheneOS, Android 17 `CP3A.261005.005` (= `CAP-072`/`CAP-073`), **the Owner user (user 0) with Google Play services** · **App under test:** OpenControl
**1.2.0, build `5b4d5db`** (release-signed, versionCode 10200; the published 1.2.0 of `ai-sessions/0085`; the installed APK's hash not pulled) · **HCI logs:** B
`CAP-074-btsnoop_hci.log` (18,097 packets, 2026-10-11 19:50:00.312 – 2026-10-12 06:52:09.044 — the run), A `CAP-074-btsnoop_hci.log.last` (64,787 packets, 2026-10-11
09:20:50.730 – 19:50:00.311 — the day before; nothing of the run) · **Buds:** `…:07`, classic handle `0x000b` in both ACLs of 2026-10-12.

Commands used throughout (rule 4a; every filter on handle `0x000b`, shown matching the Connection Complete events B 9121 and 10528 — `tshark -r B -Y
"bthci_evt.code==0x03 || bthci_evt.code==0x05" -T fields -e frame.number -e frame.time -e bthci_evt.code -e bthci_evt.bd_addr -e bthci_evt.connection_handle -e
bthci_evt.reason`, exit 0: 3575 (handle `0x0041`, 2026-10-11), 9121, 10473, 10528): the Message Stream inventory `tshark -r B -Y "bthci_acl.chandle==0x000b && btrfcomm
&& (btrfcomm.dlci==4 || btrfcomm.dlci==5)" -T fields -e frame.number -e frame.time_epoch -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type -e btrfcomm.len -e
data.data` fed to a scratch parser (`ai-sessions/0086` RESULT, `ms_inventory.py`: every `SABM`/`UA`/`DISC`/`DM` with its direction; a claim is the phone's `SABM`;
its owner is its first phone message — `08 11 00 00` = this app, `03 08 00 02 01 25` = Google Play services, the rule of `CAP-065`/`CAP-066`; every `08 13 00 04 01`
is listed with the claim it arrived on); the pw_hdlc frames of DLCI 2 split on `0x7e`, unescaped and `zlib.crc32`-checked against the trailing 4 bytes
(`hdlc_crc.py`): **CRC-32 OK on 426 of 426 frames in B and 62 of 62 in A, 0 bad, 0 short**; positive control `CAP-073-btsnoop_hci.log` → 381 of 381 (its own
FINDINGS' count). The pw_rpc content by `scripts/pwrpc_decode.py B` (426 packets, every request and answer of the run named).

## 0. Build, install, user, logs and privacy (🟢 FACT unless marked)

- **The build on film is the published 1.2.0:** "App: 1.2.0, build 5b4d5db (2026-10-10)", no "-dirty" (S t=6); `package: io.github.tedsluis.opencontrolpixelbuds:10200,
  targetSdk 34` (L line 4). 🟡 **The installed APK equals `dist/1.2.0`** (`ai-sessions/0085`: SHA-256 `ac04415e…a67220`): no `pm path`/`adb pull` was made for this run
  and the system log holds no install record for 10-12 (`grep -c dex2oat` → 2, both of 2026-10-11 and not this package — exit 0); settles: `adb shell pm path --user 0
  io.github.tedsluis.opencontrolpixelbuds`, `adb pull`, `sha256sum` against `~/opencontrol-1.2.0-tested` (Checkpoint 0 of `ai-sessions/0086`).
- **The user is the Owner, with Google Play services:** both app processes started as `User=0` (X `am_proc_start: [User=0,PID=12456,UID=10354,…]` 04:30:19.455 UTC and
  `[User=0,PID=13677,UID=10354,…]` 04:38:09.372); 64 of 68 `am_proc_start` lines of 2026-10-12 are `User=0`, 4 are `User=10` (other packages); no user switch
  (`grep -c -E 'am_switch_user|uc_switch_user'` → 0 on 10-12). `com.google.android.gms` (uid 10205) runs (`am_proc_start … UID=10205 … com.google.android.gms` X
  10-11 19:53:29; 1,016 lines name it); `app.grapheneos.gmscompat` posts notifications (X 04:39:21; S t=314 "GmsCompat"). **On the wire: 22 Play-services claims of
  DLCI 0x04** (§1) — the user without Play (`CAP-073`) had 0.
- **Processes:** 12456 (04:30:19–04:38:05 UTC; killed by Android, `am_kill … Reason=remove task` — the app swiped away, 🟡 from the reason text) and **13677 (04:38:09 →
  past the end of every log) — no crash, no ANR, no force-stop:** `grep -c` over X → `am_crash` 0, `am_anr` 0, `FATAL` 0 (exit 1 each); positive control `grep -c
  NearbyDiscovery` → 232. `StrictMode`: 65 lines, all pid 10067 (`android.process.acore`), none from the app. `am_wtf … Background started FGS: Allowed [callingPackage:
  io.github.tedsluis…]` ×13 — one per Connect/re-open (04:30:20, 04:31:11, 04:31:43, 04:38:10, 04:40:50, 04:41:53, 04:42:29, 04:42:55, 04:43:23, 04:43:50, 04:44:21, 04:44:52,
  04:45:12 UTC), as in `CAP-072`/`CAP-073`. The **SIGQUITs** at 04:45:57–04:46:15 (37 processes; the app's at 04:46:12.407 = L's last line) follow `bugreportz -v`/`-p`
  at 04:45:41 (X 68174–68177, `adbd`) — the bugreport's trace dump (`CAP-071-FINDINGS.md` §0), not an app fault. `W System: A resource failed to call close.` ×3 for
  the app (L 243–246, 344 — after cut claims; the `CAP-066` framework pattern, `ARCHITECTURE.md` §12).
- **What the system log reaches:** `main` only from **04:45:21.795 UTC** (X line 63393 — the last 75 s), `events` and `system` further back. The Bluetooth process's RFCOMM
  port lines of a collision (`bt_port_api: RFCOMM_CreateConnectionWithSecurity: already at opened state`, `cleanup_rfc_slot`, `NearbyDiscovery: RfcommEventStreamMedium …
  Failed to read from socket`) are `main`-buffer lines and are **absent for this run: 0 each** (`grep -c`, exit 1; positive controls: `NearbyDiscovery` 232, and the
  same strings present in `CAP-065-System-log-a52da8511ec6.txt` lines 50516/50523). So this capture proves the collision on the **wire and in the export** (§4), and
  the stack-level mechanism rests on `ai-sessions/0039` §2.4, `CAP-065` §3 and `CAP-066` §2 (system logs of the same phone) plus the AOSP source read today (§8).
  What the `main` buffer does hold is Play services' own handling of the last claim (§4, 06:45:21–06:45:27).
- **The logs' clocks:** E local (+02:00), L and X UTC; E's lines from 06:44:13.366 equal L's `OpenControlBuds` lines (1-ms rounding on some); the film's clock from the
  status bar (EVENT-NOTES). **The Buds' end state** (session 10's reads, E 618–662): EQ `[−3.5, 0.5, 1.0, −1.0, 2.5]` (the 06:38:27 write), balance Centre (`17:0`), mono off,
  conversation detection on, Volume EQ on, touch on, both holds Noise control, **the mode list all four** (`12:{1:1 2:1 3:1 4:1}`, the 06:38:41 write), in-ear on,
  head gestures on, Multipoint on, both case sounds on; the last mode on the wire Transparency (B 17977). The `TODO.md` §1 restore state (Balanced, Centre, NC +
  Transparency) is **not** what the Buds hold now — EQ and the mode list differ.
- **Privacy:** on film no Wi-Fi name, phone number or notification content beyond app names; the Info tab shows the three serials (t=6; ADR-010 scope, as `CAP-072`/
  `CAP-073`; written here redacted only: Case `5707…51`, Right bud `5708…09`, Left bud `5707…47`); the audio track is the maintainer's cue words. X carries the SSID (165
  lines), the Buds' address (47) and 106 address-shaped strings, no e-mail (173 `@` matches are all `android@<package>`-style account types), no phone number, no
  serial. E carries the `GetHardwareInfo` answer in 10 Debug-mode hex lines (ADR-058 item 5), L in 3, plus Android's own `XX:XX:XX:XX:6E:07` (18). **Treatment (Checkpoint 0 of `ai-sessions/0086`, the maintainer's choices in chat 2026-10-12 — done):** the full system log kept in `~/opencontrol-capture-originals/CAP-074/`, an extract committed as CAP-074-logcat-extract.txt with `awk '$1=="10-12" &&
  $2>="04:29:00.000" && $2<="04:47:00.000" && ($4=="12456"||$4=="13677"||/opencontrolpixelbuds/||/ 1002 /||/NearbyDiscovery|NearbyFastPair|FastPair/||/dumpstate/||/bugreportz/
  ||/am_wtf/||/volume_changed/)' X | grep -v -i -E '<SSID patterns>|([0-9a-f]{2}:){5}[0-9a-f]{2}'` (2,370 lines; the SSID values → 0, address-shaped strings → 0 in the extract, positive control on the full log 30 / 106, exit 0 / 1); the recording committed **as is, with its audio
  track**; `chmod 644` on every file; CAP-074-sha256sum.txt lists the six committed files; the folder renamed to the film's times.

## 1. ACLs, the Message Stream claims of both clients, and the Play-services marker

- 🟢 **Two ACLs on 2026-10-12:** B 9121 06:30:18.228 (the Buds' `Connection Request` B 9117) → B 10473 06:31:07.373 (reason `0x16`, "terminated by local host" — the
  phone, not the Buds; what triggered it is not on film, 🔴); B 10528 06:31:12.317 (the phone's `Create Connection` B 10526) → beyond the log's end 06:52:09. The run is
  on the second.
- 🟢 **The Play-services marker:** `tshark -r B -Y 'btrfcomm && frame contains 03:08:00:02:01:25' | wc -l` → **22** (exit 0; A → 7, the day before); positive control
  `CAP-066-btsnoop_hci.log.last` → 22 (its own FINDINGS' count); `CAP-073` B → 0 (the user without Play). The 22 are exactly the first phone messages of the 22
  Play-services claims below (one per claim). `03 08` is the Device Information "Platform type" message (`developers.google.com/nearby/fast-pair/specifications/extensions/deviceinformation`,
  fetched 2026-10-12: *"The message will contain a first byte identifying platform type in the additional data"* — `01` Android, the second byte `25` = SDK 37).
- 🟢 **47 claims of DLCI 0x04 in B — 25 the app's, 22 Play services'** (ms_inventory_current.txt; every claim ends with a **phone-side `DISC`** answered by the Buds'
  `UA` except the last, open at the log's end). Each Play claim opens with `03 08 00 02 01 25`, then `08 11 00 00` (Play sends a `Get` too — 22 of 22), `07 10 00 00`
  and SASS `07 11 …` writes answered `ff 01 00 06 07 11 01 01 80 00`, then `07 41 "in-use"`, `07 40`, `07 42` with their ACKs (as `CAP-065` §7). Each app claim opens
  with `08 11 00 00` and matches an E "RFCOMM channel 0x04 connected" line one to one (16 in the run, 3 at 06:38, 6 before — the 06:30 process has no export).
- 🟢 **The claim inventory of the run** (open = the phone's `SABM`; close = the phone's `DISC`; "cut" = closed with E "closed … not by this app"; "released" = E
  "released"; "killed" = Play's claim closed at the moment the app's attempt 1 fails):

| Owner | `SABM` | Open | `DISC` | Close | Life | How it ended | `Notify` on it (frame mode) |
|---|---|---|---|---|---|---|---|
| Play | 13450 | 06:38:42.230 | 14072 | 06:40:38.237 | 116.0 s | killed (E 84, 275 ms) | 13473 `40` |
| app | 14093 | 06:40:38.838 | 14126 | 06:40:41.897 | 3.06 s | released (hold ended "tab left" 06:40:40.392) | 14112 `40` |
| Play | 14152 | 06:40:43.534 | 14263 | 06:40:44.652 | 1.12 s | killed (E 98, 202 ms) | 14173 `40` |
| app | 14285 | 06:40:45.315 | 14314 | 06:40:46.973 | 1.66 s | released (Refresh battery) | 14304 `40` |
| Play | 14346 | 06:40:49.933 | 14450 | 06:40:50.698 | 0.77 s | killed (E 119, 117 ms) | 14367 `40` |
| **app** | 14500 | 06:40:51.246 | 14577 | 06:40:55.922 | **4.68 s** | **cut** (E 161) | 14525 `40`, 14552 `40` (Get), 14556/14557 `80` (Set) |
| Play | 14624 | 06:40:58.217 | 14710 | 06:40:58.844 | 0.63 s | killed (E 163, 363 ms) | 14674 `80` |
| **app** | 14732 | 06:40:59.463 | 14810 | 06:41:04.385 | **4.92 s** | **cut** (E 176) | 14752 `80` (Get), 14758/14760 `20` (Set) |
| **app** | 14845 | 06:41:06.187 | 14890 | 06:41:06.569 | **0.38 s** | **cut** (E 189) — attempt 1 (channel free) | 14865 `20` (Get), 14877/14881 `08` (Set) |
| Play | 14947 | 06:41:11.005 | 15022 | 06:41:11.489 | 0.48 s | killed (E 191, 208 ms) | 14974 `08` |
| **app** | 15051 | 06:41:12.164 | 15129 | 06:41:16.983 | **4.82 s** | **cut** (E 204) | 15073 `08` (Get), 15077/15078 `40` (Set) |
| **Play** | 15152 | 06:41:19.406 | 15334 | 06:41:53.486 | **34.1 s** | killed (E 218, 143 ms) | 15176 `40` (Get), **15204 `80`, 15231 `08`, 15235 `80`, 15242 `08`, 15247 `20`, 15256 `40`** |
| app | 15386 | 06:41:54.140 | 15423 | 06:41:55.777 | 1.64 s | released (snapshot) | 15412 `40` |
| **app** | 15500 | 06:41:57.701 | 15539 | 06:41:58.736 | **1.03 s** | **cut** (E 266) — attempt 1 | 15518 `40` |
| **Play** | 15592 | 06:42:00.981 | 15774 | 06:42:30.128 | **29.1 s** | killed (E 280, 160 ms) | 15618 `80` (Get), **15647 `08`, 15662 `80`, 15673 `08`, 15677 `20`, 15688 `40`** |
| **app** | 15822 | 06:42:30.708 | 15909 | 06:42:35.842 | **5.13 s** | **cut** (E 321) | 15853 `40`, **15860 `80` (shown)** |
| **Play** | 15931 | 06:42:38.219 | 16078 | 06:42:56.307 | **18.1 s** | killed (E 335, 95 ms) | 15957 `08` (Get), **15986 `80`, 15991 `08`, 15994 `20`, 16004 `40`** |
| **app** | 16124 | 06:42:56.884 | 16196 | 06:43:01.545 | **4.66 s** | **cut** (E 380) | 16147 `40`, **16176 `80` (shown)** |
| **Play** | 16245 | 06:43:04.092 | 16437 | 06:43:23.587 | **19.5 s** | killed (E 396, 117 ms) | 16270 `08` (Get), **16299 `80`, 16305 `08`, 16313 `20`, 16316 `40`** |
| app | 16484 | 06:43:24.142 | 16524 | 06:43:25.780 | 1.64 s | released (snapshot) | 16509 `40` |
| **app** | 16578 | 06:43:27.271 | 16618 | 06:43:28.878 | **1.61 s** | **cut** (E 440) — attempt 1 | 16595 `40` |
| **Play** | 16665 | 06:43:31.263 | 16818 | 06:43:50.845 | **19.6 s** | killed (E 456, 151 ms) | 16689 `40` (Get), **16735 `80`** |
| **app** | 16867 | 06:43:51.398 | 16952 | 06:43:56.246 | **4.85 s** | **cut** (E 497) | 16892 `80`, **16907 `08`, 16937 `80` (both shown)** |
| **Play** | 16977 | 06:43:58.717 | 17142 | 06:44:22.312 | **23.6 s** | killed (E 513, 101 ms) | 16999/17003 `08` (Get), **17031 `20`, 17040 `40`, 17048 `80`, 17063 `08`, 17067 `80`** |
| **app** | 17188 | 06:44:22.880 | 17254 | 06:44:27.543 | **4.66 s** | **cut** (E 555) | 17211 `80`, **17231 `08`, 17235 `20` (both shown)** |
| **Play** | 17303 | 06:44:30.082 | 17465 | 06:44:53.023 | **22.9 s** | killed (E 571, 176 ms) | 17330 `20` (Get), **17355 `40`, 17365 `80`, 17372 `08`, 17377 `80`, 17381 `08`, 17384 `20`** |
| **app** | 17513 | 06:44:53.625 | 17594 | 06:44:58.664 | **5.04 s** | **cut** (E 610) | 17540 `20`, **17548 `40` (shown)** |
| **Play** | 17618 | 06:45:01.189 | 17759 | 06:45:13.412 | **12.2 s** | killed (E 626, 153 ms) | 17644 `40` (Get), **17675 `80`, 17683 `08`, 17687 `80`** |
| **app** | 17807 | 06:45:14.014 | 17891 | 06:45:18.893 | **4.88 s** | **cut** (E 667) | 17835 `80`, **17843 `08`, 17848 `20` (both shown)** |
| Play | 17915 | 06:45:21.308 | — | (log end) | > 408 s | — | 17940 `40` (Get), **17977 `80`** |

The 06:30–06:31 window (the earlier process) and the 06:38 start show the same alternation: Play 378.6 s (B 11768 → 12932, killed by the new process's first attempt,
E 15 "56 ms"), the three app claims of 06:38 released after 1.6–2.3 s, Play claims of 7.9 s / 7.7 s in between (EVENT-NOTES). **Before the run: 6 app claims (3 cut
after 4.1 / 4.9 / 5.0 s, 3 released after 0.7–3.8 s; wire only, no export), 5 Play claims.**

- 🟢 **No other app on the Buds' RFCOMM from this app's side:** the phone's `SABM`s in B are on DLCI 0 (×2, the multiplexer), 2 (×13, MAESTRO — every one of the app's
  sessions), 4 (×47), 8 (×2) and 10 (×2) (`tshark -r B -Y "bthci_acl.chandle==0x000b && btrfcomm.frame_type==0x2f" -T fields -e frame.p2p_dir -e btrfcomm.dlci`, exit 0);
  the Buds' `SABM`s are on 9 (×2, HFP). DLCI 0x08 ("GSND CONTROL") and 0x0a were opened by the phone at 06:30:24 (B 9763, 9733) with the `05 0c`/`04 02`/`04 04`/
  `04 11`/`04 13` burst, the `0e 04` and the Buds' `0e 01` Case pushes — **not this app** (it opens 0x02 and 0x04 only; `ARCHITECTURE.md` §6.0b); 🟡 the same other
  client as `CAP-059`–`CAP-061` (Play services or the Google app), identity 🔴 as before.
- 🟢 **Battery and Model ID on every claim:** 151 "Battery updated" frames (`frame contains 03:03:00:03` → 151, exit 0; 100 % / 100 % / `ff`), the Model ID `03 01 00 03 da
  2d b1`, firmware `03 02`, "Revision 6" `03 09`, the session nonce `03 0a` and `03 0b` on each open (E's hex lines; the Debug tab's 18 "Unidentified frames" at t=10 are
  these Group 03/07 codes). **0 NAKs** in B (`frame contains ff:02:00` → 0, exit 0); **5 `Set`s → 5 ACKs** (B 10105 → 10108 before the run; 14553 → 14555, 14755 → 14757,
  14867 → 14875, 15074 → 15076 in it).

## 2. The Connect reads — the thirteen requests, byte for byte (`PAIR-003`, `BATT-004`, `FW-003`)

🟢 **10 sessions in the export** (06:38:10 LINK_BACK; Connect taps 06:40:50, 06:41:53, 06:42:29, 06:42:55, 06:43:23, 06:43:50, 06:44:21, 06:44:52, 06:45:12), **all on
channel 19**, each: the Buds' `GetSoftwareInfo` → `ReadSetting 16, 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29` → `SubscribeRuntimeInfo` → `GetHardwareInfo`, every
request answered `status=OK` (E "Settings read" ×10, "Hardware info read (channel 19): 3 serial numbers" ×10, 130 `ReadSetting` answers). 🟢 **Every Connect-read
request is byte-identical to `CAP-073`'s own:** the phone→Buds DLCI 2 frames of 06:38–06:46 are 17 distinct byte strings (`tshark -r B -Y "bthci_acl.chandle==0x000b &&
btrfcomm.dlci==2 && frame.p2p_dir==0 && btrfcomm.len>0 && frame.time >= \"2026-10-12 06:38:00\"" -T fields -e data.data | sort | uniq -c`, exit 0); the 15 Connect-read
strings (×10 or ×11 each) are each found by `tshark -r CAP-073-btsnoop_hci.log -Y "frame contains <bytes>"` — at B 3304–3376 of `CAP-073` (e.g. `GetHardwareInfo`
`7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 e3 a5 ec 28 ff 1e bb b7 7e` = `CAP-073` B 3376 = `CAP-072` A 8616 = `CAP-024` 801; `SubscribeRuntimeInfo` `… 90 82 1e e6 60 2d 65
a9 7e` = `CAP-073` B 3375); the two strings not in `CAP-073` are this run's own writes (§3). The Buds' answers carry the same three serials in every `GetHardwareInfo`
answer (E 49, 152, 254, …), the Info tab labels them Case / Right bud / Left bud (S t=6).

- 🟢 **The snapshot claim of every Connect collided with Play services:** in **10 of 10** sessions the DLCI 0x04 attempt 1 failed after 56–363 ms with `IOException:
  read failed, socket might closed or timeout, read ret: -1` (E 15, 84, 98, 119, 163, 191, 218, 280, 335, 396, 456, 513, 571, 626 — 13 such lines in the run, E 84 … 626, incl. the
  tap and tab claims; E 15 is 06:38:10, before the film) and attempt 2 connected 0.6–0.7 s later — the `ai-sessions/0039` collision shape: the app's failed attempt closes Play's port (the phone's `DISC`
  of Play's claim within 2 ms of E's "connect failed" line, e.g. B 14450 06:40:50.698 ↔ E 119 06:40:50.698), the retry finds the port free. **`CAP-073` (no Play): 0 of 12
  first attempts failed.**

## 3. Every write — byte for byte

🟢 **Two `WriteSetting` requests in the run's process, both answered by the empty `RESPONSE` status OK** (`pwrpc_decode` lines 138–142): the EQ `4:{16:[−3.5, 0.5,
1.0, −1.0, 2.5]}` at 06:38:27.7 (B 13222 → 13224; not in `CAP-073`, a custom curve — the Sound tab, before the film) and the mode list `4:{12:{1:1 2:1 3:1 4:1}}` at
06:38:41.2 (B 13427 → 13429, "Setting 12 written"; = `CAP-056` 1725's value, the ADR-046 request shape; not in `CAP-073`, which never wrote all four). The earlier process
wrote `4:{12:{1:1 2:0 3:1 4:1}}` (B 10196 → 10198), `4:{17:1}` (10268 → 10271) and `4:{17:0}` (10292 → 10294) at 06:30 — before the film, no export. **4 ANC `Set`s in the
run** (TRN 14553, Off 14755, NC 14867, Adaptive 15074: `08 12 00 14 01 e8 e8 xx` + 16 × `00` = `CAP-073` B 3557/3577/3587/3597) **→ 4 ACKs** (`ff 01 00 06 08 12 01 e8 e8 xx`),
each after a `Get` whose `Notify` read Settable `e8`, each followed by the Buds' `Notify` ×2 of the new mode. **No write failed, nothing was retried.** The screen
followed each ACK (S t=39, 44, 51, 57).

## 4. The holds, the cuts and every `Notify` — which client received it and what the screen showed (`TOUCH-007`, ADR-061)

**The maintainer's statement:** after a Disconnect/Connect, several press-and-holds; the ANC mode changed audibly every time; the app's mode changed only after the
first hold.

**What the evidence shows (🟢 unless marked):**

1. **Every entry of the noise-control tab started a hold, and every hold ended by a close the app did not make.** E logs "Message Stream hold started: the
   noise-control tab is on screen (ADR-061)" 13 times in the run (06:40:38.936, 06:40:51.921, 06:40:59.801, 06:41:06.430, 06:41:12.441, 06:41:57.867, 06:42:31.044,
   06:42:58.043, 06:43:27.434, 06:43:51.743, 06:44:24.476, 06:44:53.944, 06:45:14.634 — fourteen with the pre-film 06:38:24.617) *(count corrected 2026-10-15, `ai-sessions/0094`)* and "hold ended" 13 times: **once "the
   noise-control tab was left"** (06:40:40.392 — the tab visit of t=22–25) and **twelve times "the channel was closed, not by this app"**, each 2–3 ms after "RFCOMM
   on-demand channel 0x04 closed (IOException: bt socket closed, read return: -1) — session channels untouched" (E 161/162, 176/177, 189/190, 204/205, 266/267, 321/322,
   380/381, 440/441, 497/498, 555/556, 610/611, 667/668). In `CAP-073` the one hold of the run ended by the tab (31 s), never by a close.
2. **Each of those twelve closes is a phone-side `DISC` on the wire with no release by the app:** B 14577, 14810, 14890, 15129, 15539, 15909, 16196, 16618, 16952, 17254,
   17594, 17891 (`btrfcomm.frame_type == 0x43`, direction phone→Buds, the Buds' `UA` 13 ms later, e.g. 14577 06:40:55.922 → 14579 06:40:55.935); E has no "released" line
   near any of them (the app's own release logs "RFCOMM on-demand channel 0x04 released", E 52, 63, 76, 94, 106, 257, 432 — only after its own `DISC`s B 13024, 13215,
   13421, 14126, 14314, 15423, 16524). The app's reader got end-of-stream (`read return: -1`), i.e. **the stack closed the app's socket.**
3. **The timing of the twelve cuts is Play services' recovery timer.** Measured from the moment Play lost its socket (the `DISC` of its claim, killed by the app's
   failed attempt 1) to the cut of the app's claim: **5.22, 5.54, 5.49, 5.25, 5.71, 5.24, 5.29, 5.40, 5.23, 5.64, 5.48 s** (11 cuts; the twelfth, B 14890 at 06:41:06.569,
   came 2.18 s after the previous cut — Play's *retry*, see 5). Measured from the app's own `SABM`: 4.68, 4.92, 4.82, 5.13, 4.66, 4.85, 4.66, 5.04, 4.88 s for the nine claims
   that followed a collision directly, and **1.03 s (15500), 1.61 s (16578), 0.38 s (14845)** for the three claims opened on attempt 1 into a free channel — their life
   is whatever was left of Play's 5.2–5.7 s. The same ≈ 5.3 s shows where the app had already released by itself: Play's re-open came 5.30, 5.32, 5.33, 5.30, 5.28 s
   after it lost its socket (06:38:10.631 → 13047 06:38:15.937; 13168 → 13251; 13373 → 13450; 14072 → 14152; 14263 → 14346). **16 of 16 measured recoveries of the run fall in
   5.2–5.7 s after the loss** *(count corrected 2026-10-15, `ai-sessions/0094`)*; `ai-sessions/0040` measured 2.7–5.0 s on 2026-09-19 and `CAP-059` 1.7–3.1 s (a different Play version or state — ⚪ not resolved here).
4. **What a recovery does to the app's claim:** no Play `SABM` is on the wire at the moment of a cut — the stack refuses Play's connect before anything reaches the
   Buds and, on that failure path, removes the **existing** port, which is the app's (§8: AOSP `port_api.cc` returns `PORT_ALREADY_OPENED` with the incumbent's handle,
   `bta_jv_act.cc` then calls `RFCOMM_RemoveConnection(handle)`). The wire shows only the resulting `DISC` of the app's DLCI; the stack's own log lines are not retained
   in X (§0) — 🟡 for *this* log, 🟢 as a mechanism in `ai-sessions/0039` §2.4 (`cleanup_rfc_slot … app_uid: 10338` 0.04 s after Play's `already at opened state`),
   `CAP-065` §3 (09:27:10.450 UTC) and `CAP-066` §2.
5. **Play's retry after a failed recovery comes 2.2–2.6 s later and succeeds:** Play's `SABM` 2.29, 2.42, 2.25, 2.38, 2.55, 2.38, 2.47, 2.54, 2.52, 2.42 s after the cut
   (B 14624, 15152, 15592, 15931, 16245, 16665, 16977, 17303, 17618, 17915), each with `03 08 00 02 01 25` as its first message (e.g. B 14633, 0.2 s after `SABM` 14624).
   Twice the retry itself collided with a new app claim (06:41:06.569 cut the app's 0.38-s claim 14845; the next retry 4.44 s later succeeded, B 14947) — so a claim
   the app opens into a free channel between Play's recovery and its retry is cut within 2.2 s.
6. **The `Notify` frames of the run — 82 on DLCI 0x04, 34 on the app's claims, 48 on Play's** (`ms_inventory`/`correlate.py`; by the app's own rule `causeOfNotify`:
   the first `Notify` after the app's `Get` answers it, a `Notify` of the `Set`'s mode within its wait is the app's own change, any other is "changed by the Buds"):
   - on the app's claims: 16 answer its `Get`, 9 answer its `Set` (two per ACK, incl. duplicates), **9 are unprovoked** — **15860 `80` 06:42:33.204, 16176 `80` 06:43:00.045,
     16907 `08` 06:43:53.664, 16937 `80` 06:43:55.921, 17231 `08` 06:44:23.761, 17235 `20` 06:44:27.082, 17548 `40` 06:44:57.004, 17843 `08` 06:45:14.501, 17848 `20`
     06:45:17.550** — and **each of the nine is on screen** (S t=138 Transparency, 164 Transparency, 218 NC, 220 Transparency, 249 NC, 251 Off, 281 Adaptive, 299 NC, 302
     Off) with no touch indicator. The (i) line "Changed by the Buds …" was not opened (not on film).
   - on Play's claims: 13 answer Play's own `08 11` (one per claim; three of them already carry a changed mode — 15618 `80`, 15957 `08`, 16270 `08`, each 0.4–0.5 s
     after Play's `SABM` with a cue word 0.2–0.4 s later: a hold at the moment of Play's open; by the same rule — a mode other than the channel's previous
     `Notify` — two more do: 16999 `08` after the app's 16937 `80`, 17940 `40` after the app's 17848 `20`, cue words not checked; count added 2026-10-15,
     `ai-sessions/0094`), **35 are unprovoked** (the bold frames of §1's table) — **none reached the
     app, none is on screen:** the screen keeps the last mode the app saw (Adaptive t=57–94 and t=102–129, Transparency t=138–156 and t=164–182 and t=220–242, Adaptive
     t=191–210, Off t=251–274, Adaptive t=281–295) while the Buds cycled through 5–7 modes each time.
   - **The order of the Buds' changes follows the press-and-hold list** written at 06:38:41 (all four: `40` → `80` → `08` → `20` → `40` …, e.g. 15204–15256; `CAP-073`'s
     cycle had Off unticked).
7. 🟡 **The 47 unrequested changes are the maintainer's press-and-holds:** no camera; consistent with the statement, with the mode list's cycle, with 33 of 33 voiced
   segments of the audio track lying 0.1–1.5 s **after** a `Notify` (the maintainer announcing the hold just made; EVENT-NOTES "cue words"), and with Google's rule that
   a gesture change is notified *"to all connected Seekers"* (§8). Which bud: not determinable (the words are not transcribed).
8. **Why "only the first hold":** a hold reaches the app only while the app holds DLCI 0x04. After a Connect the snapshot claim (or the tab's own claim) lives **4.7–5.1 s**
   (3) before Play's recovery cuts it — a hold inside that window is shown (06:42:33.2 at 2.5 s after the `SABM`; 06:43:00.0 at 3.2 s; 06:43:53.7 and :55.9 at 2.3 and
   4.5 s; 06:44:27.1 at 4.2 s; 06:44:57.0 at 3.4 s; 06:45:17.6 at 3.5 s; the two inside the snapshot claim itself, 06:44:23.8 and 06:45:14.5) — the second and later holds
   land on Play's claim. The app never re-claims by itself after a foreign close (ADR-061, the maintainer's choice "no re-claim after a close", `ai-sessions/0084` §B 2;
   `BudsRepositoryImpl.kt:638–646` — the `channelClosed` collector calls `endHold(CHANNEL_CLOSED)` only); the next Disconnect/Connect, tap, pull or tab re-entry claims
   again, which is why every Connect in this run gave exactly one visible hold (two at 06:43:54/56, two with a hold during the Connect at 06:44:24/27 and 06:45:14/18).
9. **The taps of 06:40:54–06:41:12 worked every time** (§3): each tap's claim found Play on the channel, failed its attempt 1, killed Play's port and succeeded on
   attempt 2 — 0.6–0.7 s to the `Set`'s ACK; the user sees the mode after the ACK (S t=39, 44, 51, 57). The cut 4.7–4.9 s later (or 0.38 s at 06:41:06) came after the ACK
   in all four — no `AnswerCutOff` in E.
10. **On Play's side** the `main` buffer covers the last claim: `NearbyDiscovery … onEventStreamResponse` lines from 04:45:21.827 UTC (0.5 s after Play's `SABM` 17915 at
    06:45:21.308), and for the unprovoked `Notify` 17977 (06:45:26.634): `NearbyFastPair: HearableControlManager: onReceiveEventStreamResponse, %s, eventCode=%d,
    additionalData=%s` at 04:45:26.639 and `NearbyDiscovery: [feature-id=Ancl] ActiveNoiseCancellationModule: update dataStore for %s, AncVersionCode=%d,
    SupportedAncMode=%s, EnabledAncMode=%s, AncIndex=%d` at 04:45:26.668 UTC — **Play services received that hold and stored it** (the values are redacted by Android's
    log privacy, the event is not). Nothing in X names a collision (the `main` buffer starts after the last cut).
11. **"During playback":** 🟢 no media on the wire during the film — AVRCP play status Stopped/Paused from 06:30 to 06:45:35 (B 9384 … 11002; `tshark -Y btavrcp -V`,
    "Play Status"), **Playing only B 18024 06:45:35.123 → Stopped B 18072 06:45:53.618** (after the recording; X `SendMediaUpdate … play_state=true` 04:45:35.102); the audio
    track has no continuous energy (§0 of the EVENT-NOTES). AVDTP Start/Suspend pairs around the app's actions (B 13112 … 15489) as in `CAP-073`. 🟡 The maintainer
    listened to the modes themselves, or "playback" refers to the 18 s after the film — the claims and cuts above do not depend on it (none of the twelve cuts coincides
    with an AVDTP signal). **Side observation:** Android's media volume stepped down three times (X `volume_changed` 04:41:31.919 17→15, 04:42:00.118 15→13, 04:43:42.370
    13→12; the volume panel on film t=76, 105, 207), each within 0.5 s of a `Notify` on Play's claim (B 15231, 15618, 16735) — 🟡 a swipe on the bud together with the
    hold; out of scope.

**Answer:** 🟢 the app's behaviour is exactly ADR-061 as built and chosen: the hold is kept until the channel is closed by someone else, and then nothing re-claims.
🟢 In the user with Google Play services the channel **is** closed by someone else, every time, ≈ 5.2–5.7 s after Play services lost it to the app's own connect: Play's
Fast Pair recovers its event stream on that timer, the stack refuses the second connection to the open DLCI and removes the app's port on the way. 🟢 The Buds send the
`Notify` of every hold to whoever holds DLCI 0x04 — 35 of them to Play services, which stores them. **The difference with `CAP-073` is contention, not the Buds and not
the build** (§7).

## 5. Playback

See §4 item 11: none during the film (🟢 AVRCP Stopped/Paused, no music in the audio track); 18 s of Playing at 06:45:35–06:45:53, after the recording. Playback has
no effect on the claims: the cuts' timing (§4 item 3) is the same before, during and after the one AVDTP stream of the window (B 15428 Start 06:41:56.639 … 15489
Suspend 06:41:57.678 lies inside Play's loss → recovery interval of 06:41:53.486 → 06:41:58.736 without changing it). `AUDIO-002` (Volume EQ): expected but not
observed.

## 6. Session ends and re-opens — the Disconnect/Connect series

🟢 **10 session ends: 9 by the user's Disconnect tap** ("Session ended by the user's Disconnect tap (was Ready)", E 107, 206, 268, 323, 382, 442, 501, 557, 612 — the phone's
`DISC` of DLCI 2 each time, the ACL stays up) **and the process's end** beyond the logs (the 06:38 session ended with process 12456's kill at 06:38:05 — the last
`DISC` of DLCI 2 before 06:38:10, B 12933 region). **No session loss, no Buds-side `DISC`, no ACL drop in the run**; the one automatic re-open is the LINK_BACK of
06:38:10 (E 4); the 9 Connects are taps (E "Disconnected -> Connecting" 1.3–3.5 s after each Disconnect, the Connect button on film t=34, 134, 157, 211, 246, 275, 297).
Every Connect reached `Ready` in 0.27–0.35 s (DLCI 2 attempt 1 each time — MAESTRO is never contended, as every capture); every session's DLCI 0x04 snapshot collided
(§2). The "Probably worn (checked …)" line moved with every snapshot `Notify` (S t=30, 94, 99, 130, 156, 182, 189, 210, 243, 247, 274, 295, 298 — the Worn rule as in
`CAP-072`/`CAP-073`; both buds worn throughout, Settable `e8` in 82 of 82 `Notify` frames). After the film the session stayed open in the background (runtime-info
packets 06:53:29–06:57:24, E 673–680; the ACL open at the log's end 06:52:09) until the app's resume at 07:03:01, whose ANC-tab claim (attempt 1 failed 231 ms — Play
held the channel again — attempt 2 OK, hold 0.7 s, released by the tab) is the last thing in E.

## 7. `CAP-074` versus `CAP-073` — the same build in the two users

| Aspect | `CAP-073` (2026-10-10, user 10 **without** Play services) | `CAP-074` (2026-10-12, user 0 **with** Play services) |
|---|---|---|
| Build, firmware, phone | 1.2.0 `5b4d5db`, `release_5.203`, Pixel 9a `CP3A.261005.005` | the same (Info tab t=6; 10 announcements; L/X headers) |
| Connect reads | the thirteen + `GetHardwareInfo`, byte-identical to `CAP-072` (11 of 11) | **byte-identical to `CAP-073`** (15 of 15 strings, 10 of 10 sessions; §2) |
| Play-services marker | 0 in the run (`.last`: 14, the Owner user) | **22** (A: 7) |
| DLCI 0x04 claim owner over time | the app only; between its claims nobody | **alternating app / Play**: Play holds it whenever the app does not (116 s, 34 s, 29 s, 18–24 s, 12 s, > 408 s); the app's claims last 0.4–5.1 s before the cut |
| The app's first connect attempt on 0x04 | 0 of 12 failed | **13 of 16 failed** (95–363 ms), 3 connected into a free channel and were cut within 0.4–1.6 s *(corrected 2026-10-15, `ai-sessions/0094`)* |
| The hold's lifetime and end | 31 s, ended by leaving the tab (`DISC` 1.509 s later) | **0.4–5.1 s, ended twelve of twelve times by a foreign close** ("not by this app"), once by leaving the tab |
| Play's recovery after losing the channel | n/a | **5.2–5.7 s, 16 of 16**; retry 2.2–2.6 s after a failed recovery |
| Unprovoked `Notify` received by the app | 4 of 4 of the run (B 3531, 3543, 3546, 3547 — on the 31-s hold) | **9 of 44** (the 35 on Play's claims + 3 at Play's opens unseen) |
| Unprovoked `Notify` received by Play services | 0 (no Play) | 35 + 3 (one logged by `HearableControlManager`, 06:45:26.6) |
| Unprovoked `Notify` received by nobody | 0 (the Buds send only to a connected Seeker: no claim ⇒ no frame — `CAP-072` §5) | 0 — Play is on the channel whenever the app is not; every hold is on the wire |
| Modes shown on screen | every hold (Adaptive → TRN → NC → Adaptive, t=270–280) | the first (sometimes the second) hold after each Connect; then frozen on the last mode the app saw |
| The "first-hold-only" pattern | absent | **present in 9 of 9 Connects** |
| The Buds | the same `Notify` per change, `e8` Settable | the same — the Buds behave identically; the recipient differs |
| ANC taps | 4 of 4 ACKed on the held channel, no second `SABM` | 4 of 4 ACKed, each after a collision + retry (a new `SABM` each, the hold having been cut) |

**Conclusion the table supports (🟢):** the only difference between the two runs that bears on the symptom is Google Play services' presence on DLCI 0x04. The Buds
behave the same, the app's bytes are the same, ADR-061's code path is the same; in the Play user the hold is cut by the stack on Play's recovery timer and the app,
by design, does not take the channel back.

## 8. The root cause — with its label — and the external checks

**🟢 FACT (this capture, with `ai-sessions/0039`/`CAP-065`/`CAP-066` for the stack lines):** in a user with Google Play services, the ADR-061 hold of DLCI 0x04 ends
≈ 5.2–5.7 s after Play's Fast Pair lost the channel to the app (≈ 4.7–5.1 s after the app's `SABM` when the app's own connect caused that loss): Play's recovery connect
collides with the app's open DLCI, and the Android stack closes the app's port (`IOException … read return: -1`, the phone's `DISC` on the wire, no app release). The
app then holds nothing until the next tap, pull, tab re-entry or Connect (ADR-061's "no re-claim after a close"). The Buds notify every press-and-hold to the client
on the channel — Play services for everything after that first window — so only the first hold (or two) after a Connect is shown. In the user without Play services
nothing contends, the hold lives until the tab is left, and every hold is shown (`CAP-073`).

What stays **🟡**: (a) that each of the twelve `DISC`s was produced by the stack's `PORT_ALREADY_OPENED` → `RFCOMM_RemoveConnection` path *in this run* — the Bluetooth
process's lines are not in this system log (settles: repeat one Connect + tab + 6 s wait and pull the system log within a minute, as `CAP-065`/`CAP-066` did);
(b) the 5.2–5.7 s is Play's recovery timer of **this** Play-services version/state (`ai-sessions/0040` saw 2.7–5.0 s, `CAP-059` 1.7–3.1 s; the `CAP-059`/`061`/`062`
runs where Play *stopped* re-claiming after one collision did not recur here — 🔴 why remains open, `TODO.md` §4).

**External checks (fetched 2026-10-12):**

- Fast Pair **Hearable Controls** (`developers.google.com/nearby/fast-pair/specifications/extensions/hearablecontrols`): *"If the user changes the setting via headset
  gesture or companion application, the Provider should also send notification to all connected Seekers."* and *"When a Provider receives a 'Set ANC state' message, it
  should Acknowledge, and Notify ANC state to all connected Seekers."* — "connected Seekers" are the clients with a Message Stream connection; with one RFCOMM
  connection per (device, channel) on the phone (`ARCHITECTURE.md` §6.0b, the RFCOMM spec) there is one such client at a time. The table rows: `0x11` Get (Seeker →
  Provider, MAC N, ACK N), `0x12` Set (Y, Y), `0x13` Notify (Provider → Seeker, N, N).
- Fast Pair **Message Stream** (`…/extensions/messagestream`): *"When the Provider connects to a supported Seeker, the Seeker will attempt to connect the message
  stream."*, the fixed UUID `df21fe2c-2515-4fdb-8886-f12c4d67927c` (the one this app and Play services both connect to, `ai-sessions/0039` §2.4) — the page says nothing
  about reconnection timing or connection limits.
- Fast Pair **Device Information** (`…/extensions/deviceinformation`): code `0x08` "Platform type" — *"The message will contain a first byte identifying platform type
  in the additional data"* (Android `01`; the second byte the SDK version) — the marker `03 08 00 02 01 25` of Play's claims; code `0x03` "Battery updated": *"When Message
  Stream connects or the battery value changes, an update should be sent."* (the `03 03` burst on every open, §1).
- AOSP **`system/stack/rfcomm/port_api.cc`** (`android.googlesource.com/platform/packages/modules/Bluetooth`, `main`, read 2026-10-12), `RFCOMM_CreateConnectionWithSecurity`:
  on the client path, `p_port = port_find_port(dlci, bd_addr)`; if that port exists and `!p_port->is_server`, it logs `"already at opened state {}, RFC_state={},
  MCB_state={}, …"`, sets `*p_handle = p_port->handle;` and `return PORT_ALREADY_OPENED;` — the **existing** (incumbent) port's handle is handed back.
- AOSP **`system/bta/jv/bta_jv_act.cc`**, `bta_jv_rfcomm_connect`: `port_status = RFCOMM_CreateConnectionWithSecurity(…, &handle, …); if (port_status != PORT_SUCCESS) {
  log::error("RFCOMM_CreateConnection failed"); … status = FAILURE; } … if (status == FAILURE) { if (handle) { if (RFCOMM_RemoveConnection(handle) != PORT_SUCCESS) …` —
  with `handle` = the incumbent's, **the failing connect removes the incumbent's connection**: the `DISC` on the wire and the incumbent's `read return: -1`. (🟢 the code
  as read; that `handle` carries the incumbent's value follows from the two quotes together.) This is the "already at opened state → `cleanup_rfc_slot` of the other
  uid" pair `ai-sessions/0039` §2.4 logged on this phone.
- Android **`BluetoothSocket`** reference: not retrievable through this session's fetch tool (the page's text came back as its navigation sidebar in three segments);
  the one-connection-per-(device, channel) statement rests on the RFCOMM specification quote in `ARCHITECTURE.md` §6.0b (2026-09-24) and the AOSP code above.

## 9. Fix options for the maintainer (proposals, not decisions — `AGENTS.md` §6)

The number a fix is designed around: **Play services re-connects ≈ 5.2–5.7 s after it lost the channel and retries ≈ 2.3 s after a failed attempt**; each of its
attempts against an open app claim costs the app its socket (the stack, not Play, closes it); each of the app's attempts against an open Play claim costs Play its
socket. Nothing the app sends can change that — only *when* it holds the channel.

| # | Option | What changes | Pros | Cons / wire cost | ADR-032 / ADR-061 |
|---|---|---|---|---|---|
| A | **Re-claim after a foreign close while the tab is shown**, with a back-off (e.g. 0.5 s, then 1, 2, 4 s; stop after N) | `channelClosed` collector: `reconcileHold(null)` instead of only `endHold` (the M5 mutant of `ai-sessions/0084` — a one-line change plus the back-off and its tests) | restores `CAP-073`'s behaviour in the Play user: the hold comes back 0.5–2 s after each cut, so most holds are shown | a ping-pong with Play: every re-claim kills Play's socket (its connect fails, it retries 2.3 s later, kills ours 5.3 s later …) for as long as the tab is open — Play's SASS/battery/ANC state on the phone's own UI goes stale; ≈ 2 `SABM`/`DISC` pairs per 6 s; the holds that land in the 0.5–2 s gaps are still lost; a back-off that stops makes the symptom return later in the same tab view | supersedes ADR-061's "no re-claim after a close" (the maintainer's 2026-10-10 choice) and widens ADR-032 (i) |
| B | **Re-claim only on a trigger the user can see** — a "Channel taken by another app (Google Play services' Fast Pair) — tap Refresh to see the current mode" line on the ANC card, no automatic re-claim | UI + `messageStreamError`/`holdEnd` state: show the cause of the cut; Refresh already re-claims | honest, no ping-pong, no new wire traffic; the user learns why the mode froze | the symptom stays: a hold after the cut is not shown until the user taps; "only the first hold" remains true | ADR-061 Update (the (i) text already says Play "cannot use it meanwhile" — it must say the reverse too: Play takes it back ≈ 5 s later) |
| C | **Accept and document** as a known limit of a user with Play services (`CHANGELOG.md` Known issues, `README.md`, the release notes) — the app is built for the user without Play (`PROJECT.md` Definition of done) | text only | nothing to build or re-test; `CAP-073` stays the verified behaviour | the maintainer's own Play user keeps the symptom; the ANC (i) text claims a lockout that lasts 5 s only | ADR-061 Update (a dated note on the observed contention) |
| D | **One-shot re-claim per close** (re-claim once, immediately or after Play's 2.3-s retry window, then hold until the next close; never a second time) | as A with N = 1 | half the ping-pong of A; each Connect gives two windows instead of one | still a collision every ~8 s while the tab is open; still frozen after the second cut; hard to explain in the (i) | as A |
| E | **Claim only at a user-visible moment and keep it short** — revert to ADR-032's claim-per-action (no hold) in the Play user, i.e. detect contention (a cut with `read return: -1` within 10 s of a claim) and switch the hold off for this session | `releaseOrHold`: a per-session "contended" flag | no ping-pong; the Play user gets `CAP-072`'s behaviour (holds never shown, taps always work); the non-Play user keeps the hold | "Changed by the Buds" never appears in the Play user; two behaviours to document and test | ADR-061 Update ("the hold is not kept on a phone where another client takes the channel") |
| F | **Re-claim on Play's retry gap** — after a cut, wait ≈ 2.6 s (Play's retry lands at 2.2–2.6 s and succeeds into the free channel), then claim: the app's connect kills Play's fresh socket, Play recovers 5.3 s later and cuts the app again — a steady 8-s cycle with the app holding ≈ 5 s of every 8 | as A with a fixed delay | predictable; ≈ 60 % of the holds shown | still a ping-pong; rests on Play's timers (5.3 s / 2.3 s) that differed in earlier captures (2.7–5.0 s; 1.7–3.1 s) — fragile | as A |

**The analysing session's view (the decision is the maintainer's):** A/D/F fight Play services on every tab view and rest on Play's timers; E keeps the app honest
on both phones at the price of the feature in the Play user; B + C cost nothing on the wire and tell the user what happened. Whichever is chosen, the ANC (i) text's
sentence "Another app … cannot use it meanwhile" needs the observed reverse added, and 1.2.0's Known issues a line for the Play user. A fix is its own FEATURE prompt
(`ai-sessions/0087`), as the prompt says.

**The maintainer's choice (chat 2026-10-12, `AskUserQuestion` "Fix option", answered in their own words):** *a switch on the Settings tab that switches between option 1
(B + C: a visible hint and a known-issue line, no re-claim) and option 2 (A: re-claim after a foreign close while the tab is shown, with a back-off), so that the user
can choose what works best.* Recorded in `TODO.md` §5 for the FEATURE prompt; the known-issue line in `CHANGELOG.md`/`README.md` was **not** chosen (the same chat,
"Docs") — 1.2.0's limit in the Play user is documented here and in ADR-061's Update of 2026-10-12.

## 10. Open questions

- 🔴 The stack's lines for the twelve cuts of this run (the system log's `main` buffer did not reach back) — settles: a repeat with the system log pulled within a
  minute of a cut.
- 🔴 Why Play services' recovery timer reads 5.2–5.7 s here, 2.7–5.0 s in `ai-sessions/0040` and 1.7–3.1 s in `CAP-059`, and why it stopped re-claiming altogether in
  `CAP-059`/`CAP-061`/`CAP-062` (never here) — the Nearby-devices permission state and the Play version were not recorded in any of them (`TODO.md` §3's standing
  item).
- 🔴 What ended the first ACL at 06:31:07 ("terminated by local host", before the film).
- 🟡 The three volume steps at holds (§4 item 11); 🟡 the 06:30:24 opener of DLCI 0x08/0x0a (as `CAP-059`–`CAP-061`).
- 🔴 The "Changed by the Buds" (i) line's text after a hold — still not opened on film (as `CAP-073`).
- ⚪ The installed APK's hash (no pull); the Nearby-devices permission state of Play services (not recorded, the standing `TODO.md` item).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-074-2026-10-12_06-40-16_06-45-31-Group_BJ/CAP-074-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-074-2026-10-12_06-40-16_06-45-31-Group_BJ/CAP-074-FINDINGS
