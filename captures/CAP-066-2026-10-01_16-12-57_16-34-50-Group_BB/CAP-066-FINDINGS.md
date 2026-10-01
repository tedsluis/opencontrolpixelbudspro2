# Findings: `CAP-066` (Group BB — the first hardware run of the `ai-sessions/0062` build: ANC `Get` before every `Set`, cut-off handling, the settings menu; the Settable byte straight from the case; lead L-1, the hosting bud)

Standardized, evidence-based extraction from `CAP-066-btsnoop_hci.log.last` and `CAP-066-btsnoop_hci.log` (before / after the Bluetooth off/on at 16:32),
`CAP-066-recording.mp4` (video and audio), the two debug exports, the two app logcats and the two system logs (before / after the force-stop at 16:26:54), per
`ai-sessions/0063`. The timeline these findings refer to is `CAP-066-EVENT-NOTES.md`.

- 🟢 **FACT** — directly observed in this capture (frame number, log line or film time given).
- 🟡 **HYPOTHESIS** — plausible reading, not yet independently confirmed; the settling experiment is named.
- ⚪ **ASSUMPTION** — not tested here.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-066` · **Date:** 2026-10-01, film overlay 16:12:57–16:34:50 (phone 16:12:57.5–16:34:50.5) · **Firmware:** 🟢 `release_5.203` (10 of 10
announcements, Info tab on film) · **Phone:** Pixel 9a, GrapheneOS, Android 17 `CP3A.260905.009`, Google Play services present · **App under test:** OpenControl
build `043a09b` (= `ad0c4ba`'s app code, §0) · **HCI logs:** "A" = `.log.last` (8,086 packets, 16:13:05.082–16:32:17.845), "B" = `.log` (2,587 packets,
16:32:23.726–16:36:28.686), 0 `cap_len≠len` · **Buds:** `04:00:6e:cf:6e:07`, classic handle `0x000b` for every ACL; LE handle `0x0041` = the Fitbit Charge 6,
excluded. `bluetooth.addr` is empty with this `H4 with linux header` encapsulation, so every command pre-filters by handle.

Commands used throughout (rule 4a), each run on both files:
`tshark -r <log> -Y "bthci_acl.chandle==0x000b && btrfcomm && (btrfcomm.frame_type==0x2f || btrfcomm.frame_type==0x43 || btrfcomm.frame_type==0x63 ||
btrfcomm.frame_type==0x0f)" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type` (every `SABM`/`DISC`/`UA`/`DM`:
242 + 53 frames); HCI connection events with `-Y "bthci_evt.code==0x03 || bthci_evt.code==0x04 || bthci_evt.code==0x05 || bthci_cmd.opcode==0x0405 ||
bthci_cmd.opcode==0x0406 || bthci_evt.le_meta_subevent==0x0a || bthci_cmd.opcode==0x0c03"`; MAESTRO and the Message Stream with the scratch decoder of
`CAP-065-FINDINGS.md` (copied unchanged: `tshark … -Y "bthci_acl.chandle==0x000b && btrfcomm" -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci
-e btrfcomm.frame_type -e data.data`, reusing `scripts/pwrpc_decode.py`'s `parse`/`describe`/`unescape`, pw_hdlc on **DLCI 2 or 3** per direction with a CRC-32
check of every frame, Message Stream on DLCI 4/5 at message level `[Group][Code][Length BE]`, every buffer reset at a control frame of its DLCI); a claim is
Play services' when its first phone message is `03 08 00 02 01 25`, the app's when it is `08 11` or `08 12`. AVRCP with `-Y "btavctp or btavrcp"` (exit 0; 20 +
12 frames). `p2p_dir` 0 = phone→Buds. **Clocks:** phone = film overlay + 0.5 s (±0.2 s; the minute flips at both ends, no drift); HCI and the exports are phone
local time; logcats and system logs (UTC) = local − 2 h 00 min 00.000 s.

---

## 0. Capture integrity, build identity, audio, logs (🟢 FACT unless marked)

- **Two HCI logs.** A = 16:13:05.082–16:32:17.845 (starts with the HCI Reset of the P2 Bluetooth-on, frame 1; ends with the phone's `Disconnect` commands
  8040/8042 and `Disconnection Complete` reason `0x16` for `0x0041` and `0x000b`, 8071/8072); B = 16:32:23.726–16:36:28.686 (frame 1 = HCI Reset). The 5.9-s gap
  is the K1/K2 Bluetooth-off period (system log `STATE_OFF` 14:32:17.651, `STATE_TURNING_ON` 14:32:24.378 UTC) — no Buds traffic falls into it. A: 0
  out-of-order pairs, B: 2.
- **Build — "the latest build, see Settings → Info" (the maintainer's statement): 🟢 confirmed.** The Info tab on film (16:13:32–36; full-resolution frame t =
  35.5 s) reads **"App: 0.1.0-dev, build 043a09b (2026-10-01)"** — no "-dirty", so the checkout had no tracked change when it was built
  (`app/build.gradle.kts:24–26`: `git rev-parse --short HEAD` plus "-dirty" if `git status --porcelain --untracked-files=no` is non-empty). `043a09b` is the
  `0062` RESULT commit (2026-10-01 15:38:08 +0200); `git diff ad0c4ba 043a09b -- android` is empty (exit 0, no output), so the app code is `ad0c4ba`, the last
  commit under `android/` (`git log -1 -- android` → `ad0c4ba`). The hash shown is `HEAD`, not the last `android/` commit — by design of the build script.
  Firmware lines "Case / Left bud / Right bud: release_5.203" and "Control channel: 21" (16:13) / "19" (16:28) equal the export's "Maestro channel announced by
  the Buds" lines (E1 294, E2 11) and the announcements A820 / A7274. Logs that exist only in this build are present: "the claim asks the Buds first and steps from
  their answer" (E1 199 ff., `AncTileService.kt:132`), "ANC Set not sent: this claim's Notify reports no switchable mode" (E1 346, `BudsRepositoryImpl.kt:729`),
  "(provisional: …)" (17 + 3 lines, `SessionDiagnostics.kt:54`), StrictMode stacks (§8).
- **Installation.** `PackageManager: installation completed for package:io.github.tedsluis.opencontrolpixelbuds` 13:42:56.914 UTC (before-force-stop system log
  28166), process start PID 6546 13:43:00.230 (28493). The `installer_clear_app_data_caller … flags=39` line (28123) is **not** a data clear: `flags = 0x27 =
  FLAG_CLEAR_CODE_CACHE_ONLY (0x20) | FLAG_STORAGE_EXTERNAL (0x4) | FLAG_STORAGE_CE (0x2) | FLAG_STORAGE_DE (0x1)` (AOSP
  `frameworks/native/cmds/installd/binder/android/os/IInstalld.aidl`, `refs/heads/main`, lines 150–156), and its logged call stack runs through
  `InstallPackageHelper.prepPerformDexoptIfNeeded` (28127). Debug mode was on from the first session of the new process (hex lines from E1 9). So P1 ("no data
  clear") held. 🟢 **The same holds for `CAP-064`:** its line 8044 reads `installer_clear_app_data_caller … flags=39` with the call stack through
  `prepPerformDexoptIfNeeded` (line 8048, `CAP-064-System-log-11c30e3704a6.txt`), so `CAP-064-FINDINGS.md` §0's "the data were cleared at installation" is wrong
  (correction in §11 item 7).
- **Audio.** AAC stereo, decodes (`ffmpeg -i CAP-066-recording.mp4 -vn -ac 1 -ar 16000 …`). Per-second RMS: median −74.8 dBFS, 90th percentile −61.2, maximum
  −37.3 dBFS (t = 1198 s, 16:32:55); 165 s more than 12 dB above the median; no speech pattern in the full spectrogram. Two runs of regular ≈ 0.5-s broadband
  transients at 16:32:51–16:33:11 and 16:33:48–16:34:02 (footsteps) bracket K3. 🟢 The maintainer's statement "I did not speak" is consistent with the track.
- **Debug exports (two, both with Debug mode on).** E1 `…-162556.txt` 769 lines 15:43:01.669–16:25:46.906 (the whole first process); E2 `…-163412.txt` 255
  lines 16:27:05.492–16:33:56.192 (the second process; logcat "Debug log exported (255 lines)" 14:34:16.973 UTC). Uncovered by any export: 16:25:47–16:27:05
  (covered by the first logcat to 16:26:23.6, the second system log's app lines to 16:26:30.8, and HCI) and 16:33:56–16:34:50 (the second logcat, HCI). No
  `MalformedFrame`, Safe Mode, quarantine, undecodable-answer, late-answer, timeout or NAK line in either (`grep -c` → 0 for each; positive control "EQ read ok"
  in both).
- **App logcats.** Before-force-stop 787 lines (PID 6546, to 14:26:23.637 UTC); after 697 lines — its 261 lines of PID 6546 are all in the first (`comm -12` →
  261), plus 305 of PID 12310 (to 14:35:00.410). No crash, ANR or tombstone. Activity lifecycle (second process): created 14:27:05.551; relaunched 14:31:47.326
  (rotation to landscape) and 14:32:12.037 (back to portrait); stopped/restarted 14:34:14.851/16.893 (the export's save-as dialog); stopped 14:35:00.389.
- **System logs.** Before 65,360 lines (to 14:26:33.395), after 62,154 (to 14:35:12.805); 29,801 identical lines. The **main** buffer (with the Bluetooth
  process) covers only 14:21:18–14:26:33 and 14:32:27–14:35:12 UTC. Force-stop: `ActivityManager: Force stopping io.github.tedsluis.opencontrolpixelbuds …
  from pid 741 (com.android.settings)` 14:26:54.523, `am_kill … PID=6546 … Reason=stop …` 14:26:54.529 (after-force-stop log 40206–40213), new process
  `am_proc_start … PID=12310` 14:27:02.770 (40561). **A bug report ran:** `dumpstate` takes the bug-report lock at 14:34:48.929 UTC (58014); the Vanadium WebView
  sandbox SIGABRT in the crash buffer (14:35:11) is inside its `DumpForSigQuit` — not the app.
- **GrapheneOS Bluetooth auto-off — the `CAP-065` 🔴 `delayMillis: 0` is answered: 🟢 the auto-off was disabled.** `BluetoothAutoOff … delayMillis: 0` is
  logged 4 times (14:13:05.346, 14:13:05.700, and the two after K2), "scheduled alarm" 0 times and "alarm triggered" / "adapter.disable(true)" 0 times
  (`grep -c` over both system logs; positive control: 181 `BluetoothAutoOff` lines). GrapheneOS source (`platform_frameworks_base`, branch `16-qpr2`,
  `services/core/java/com/android/server/ext/DelayedConditionalAction.java`, fetched 2026-10-01): `long delayMillis = getDelayDurationMillis(); Slog.d(TAG,
  "delayMillis: " + delayMillis); if (delayMillis == 0) { return; }` — the alarm is only scheduled for a non-zero setting, and the alarm listener itself
  returns with "alarm has been disabled, returning" when the delay is 0. Every Bluetooth state change of the run was the maintainer's (16:13:00.8 off /
  16:13:05.3 on, 16:32:16.8 off / 16:32:24.4 on). (Branch `16-qpr2` vs the phone's Android 17 build: the log strings match line for line.)

## 1. Automatic connect, Android's link and the ACLs (lead 9)

The lid stayed **open** for the whole film, so no lid-open sample was added. Every ACL of the run:

| # | Film (phone) | Visible | Physical action | HCI | Android link (export) | App `SABM` MAESTRO | Re-open | Connect tap |
|---|---|---|---|---|---|---|---|---|
| A | 16:13:05.3 | yes | **Bluetooth on** (P2), both buds on the table | **phone** `Create Connection` A173 (16:13:05.698) → A430 | `CONNECTED` 16:13:11.724 | A765 | LINK_BACK (E1 287) | no ✓ |
| B | 16:17:24 | yes | Right-slot bud, then Left, out onto the table | **Buds' Connection Request** A2804 (16:17:24.961) | `CONNECTED` 16:17:25.441 | A3248 (DLCI 0x03) | LINK_BACK (E1 446) | no ✓ |
| C | 16:22:19 | yes | Left-slot bud out (Right still docked) | **Buds'** A5530 (16:22:20.792) | `CONNECTED` 16:22:21.217 | A5882 | LINK_BACK (E1 621) | no ✓ |
| D | 16:32:24.4 | yes | **Bluetooth on** (K2), both worn | **phone** B173 (16:32:25.043) → B287 | `CONNECTED` 16:32:29.440 | B700 | LINK_BACK (E2 172) | no ✓ |
| E | 16:33:48 | yes | back in range (K3) | **Buds'** B1445 (16:33:50.763) | `CONNECTED` 16:33:51.531 | B1865 (DLCI 0x03) | LINK_BACK (E2 218) | no ✓ |
| F | 16:35:37 (after the film) | ? | buds being docked | ACL `0x13` B2437 (16:35:36.945) → **phone** `Create Connection` B2438 (16:35:37.044, **0.1 s later**) → B2453 → `0x13` B2520 | — (no export) | — | — | — |

- 🟢 The app re-opened by itself every time the link came up while it was visible (5 of 5) — **no Connect tap and no Disconnect tap in the whole run** (`grep -c
  "user's Disconnect tap\|Connect tapped"` over E1 from 16:13 and E2 → 0; positive control: E1 138 "Session ended by the user's Disconnect tap" at 15:45:06, before
  the film).
- 🟢 The phone paged the Buds right after each Bluetooth-on (A, D) and once 0.1 s after an ACL drop with the buds going into the case (F) — `CAP-064` §1a's 🔴
  ("why Android re-paged the docked Buds 3.4 ms after a drop") gets a second sample (F, after the film; no Bluetooth-process log at 14:35:37 UTC — the system
  log ends 14:35:12). Otherwise every ACL began with the Buds' Connection Request 1–2 s after a bud left the case (B, C) or on return into range (E), as in
  `CAP-065` §1.

## 2. ANC claims — section I and every other tap (`ai-sessions/0062` F-1/F-2/F-3; the skeleton's "Refuted if" applied literally)

49 Message Stream claims: **23 the app's** (21 in A, 2 in B = the exports' 20 + 3 "RFCOMM channel 0x04 connected" lines in the film window; 17 of them after a
failed first attempt, "connect failed after … (attempt 1/3)" ×14 + ×3) and 26 Play services'. Every app claim:

| Claim (`SABM`) | Time | Trigger | Phone → Buds | Buds' answer | Result |
|---|---|---|---|---|---|
| A1019 | 16:13:16.07 | snapshot (LINK_BACK) | `08 11 00 00` | `08 13 00 04 01 e8 00 20` | `00` |
| A1630 | 16:15:17.50 | **BB-1** Refresh | `08 11` | `… e8 00 20` | `00`, note stays |
| A1778 | 16:15:25.22 | **BB-2** Adaptive | `08 11` | `… e8 00 20` | **no `08 12`** ("Set not sent", E1 346) |
| A1951 | 16:15:40.13 | **BB-3** tile | `08 11` | `… e8 00 20` | **no `08 12`** (E1 357), toast |
| A2177 | 16:16:18.26 | **BB-4** Adaptive | `08 11` → `08 12 00 14 01 e8 e8 40` + 16 × `00` (A2202) | `… e8 e8 80` (A2201) → ACK `ff 01 00 06 08 12 01 e8 e8 40` (A2204) | Adaptive ✓ |
| A2344 | 16:16:29.06 | **BB-4t** tile | `08 11` → `08 12 … 20` (A2370) | `… e8 e8 40` (A2369) → ACK (A2372) | Off ✓ (Adaptive → Off) |
| A2566 | 16:16:52.46 | snapshot (AFTER_LOSS) | `08 11` | `… e8 e8 80` | — |
| A3301 | 16:17:27.94 | snapshot (LINK_BACK) | `08 11` | `… e8 00 20` (A3345) | `00`; the phone closes the claim 41 ms later (A3349) |
| A3554, A3722, A3916, A4127, A4364, A4570 | 16:17:49.9 – 16:20:15.7 | **BB-5** Refresh ×6 | `08 11` | `… e8 00 20` each | `00` ×6 |
| A4855 | 16:21:13.33 | **BB-8** Transparency | `08 11` → `08 12 … 80` (A4879) | `… e8 e8 20` → ACK (A4881) | Transparency ✓ |
| A5007 | 16:21:22.57 | **BB-8** Off | `08 11` → `08 12 … 20` (A5030) | `… e8 e8 80` → ACK (A5033) | Off ✓ |
| A5295, A5917, A6460, A6868, A7334, B900, B2044 | 16:21:39.7 – 16:33:54.2 | snapshots (re-opens) | `08 11` | `00 20` once (A5944), else `e8 80` / `e8 20` | — |

Command: the scratch decoder's per-claim listing; raw e.g. A1789 `08110000`, A1800 `0813000401e80020`, A2201 `0813000401e8e880`, A2202
`0812001401e8e84000000000000000000000000000000000`, A2204 `ff010006081201e8e840`, A2369 `0813000401e8e840`, A2370 `0812001401e8e820` + 16 × `00`.

- 🟢 **F-1 holds on hardware, "Refuted if" — no literal hit:** every one of the 23 app claims sends `08 11` first; the 4 `08 12` (BB-4, BB-4t, BB-8 ×2) each
  follow that claim's own `Notify` with Settable `e8`; none follows a `00`; no tap claims twice (each tap = one `SABM` … `DISC`). 4 ACKs, **0 NAK** (`ff 02`: 0
  in both logs). The `Get` → `Notify` round trip took 15–368 ms (median ≈ 260 ms) — the cost of F-1.
- 🟢 **The real I-1 path finally ran** (purpose I): the app's last check said `00` (stale note shown with both buds worn, film 16:14:30 and 16:21:00–10), and a tap
  then read `e8` and switched (BB-4, BB-8) — the disabled-tap re-check of `ai-sessions/0054` works as designed.
- 🟢 **BB-4t / the tile's step from the fresh `Notify`:** A2344's `Notify` read Adaptive (`40`); the `Set` asked for Off (`20`) — the tile cycle's next mode after
  **that** reading (`AncMode.nextForTile`). The tile showed "ANC / Adaptive" before and "ANC / Off" after (film 16:16:26 / 16:16:30).
- 🟢 **F-2 wording on film:** ANC note "The Buds don't allow changing noise control right now (usually because no bud is in an ear). Tapping a mode checks again
  first." (16:14:30, 16:15:14–34, 16:17:46–16:21:10), tile subtitle "Not allowed now" (16:13:18, 16:15:36), toast "The Buds don't allow changing noise control right
  now (usually because…" (16:15:42).
- 🟢 **BB-15 (F-3, watch only): no cut-off occurred** — for each app claim the Buds' last answer came before the claim's close: 0 of 23 have an answer after the
  `DISC` (gaps answer → `DISC` 41 ms (A3301) and 1.44–1.56 s (the 1.5-s linger) for the other 22). No "answer cut off" line in either export or logcat (`grep -c
  "cut off"` → 0, exit 1; positive control: the string is logged by `BudsRepositoryImpl.kt:772` and "provisional" counts 17 / 3 in the same files). F-3 is
  therefore **not hardware-verified** by this run.
- 🟢 Play services' collision path was logged again by the Bluetooth process: the app's first attempt at 14:21:21.665 UTC (`RFCOMM_CreateConnectionWithSecurity:
  already at opened state 2 … scn=2`, `on_cl_rfc_init: INIT unsuccessful`), then `NearbyDiscovery: RfcommEventStreamMedium … Failed to read from socket` 0.14 s
  later (and again at 14:32:31.244 / 14:32:31.517 and 14:33:53.221 / 14:33:53.333) — the app's own failed attempt closes Play services' socket, and its second
  attempt succeeds (`ARCHITECTURE.md` §6.0b).

## 3. The Settable byte (ADR-049; BB-5; lead 3)

All 76 `Notify ANC state` frames of the run (31 app, 45 Play services), against the film's wear state (head-side mapping):

| Settable | Count | Frames (examples) | Film |
|---|---|---|---|
| `00` | **28** | A749, A1049, A1161 (both on the table, 16:13); A1523 (both just laid down, 16:14:49); A1652/1800/1972 (BB-1…3); A2738 (Left being docked, Right docked, 16:17:00.6); **A3345, 3454, 3574, 3635, 3742, 3798, 3937, 3991, 4148, 4206, 4384, 4444, 4595, 4651** (BB-5: buds straight from the case to the table, 2.6 s … 175 s after leaving the case); A5431 (Left out of the ear, Right docked); A5944, A6164 (Left on the table, Right being undocked) | **no bud in an ear** |
| `e8` | **48** | A1396 (Right just into the ear), A1489 (Left out, Right worn), A2201 (both worn), A2500 / A5221 / A6800 (Right just out, Left worn), A6274 (Right into the ear, Left on the table), A6672 (Left out, Right worn), B843 … B2155 (worn, off film) | **at least one bud in an ear** |

Command: the scratch decoder's message list filtered to `08 13`, joined to its claim's owner (notify.txt (scratchpad)); raw `0813000401e80020` / `0813000401e8e880`.

- 🟢 FACT (this capture): **0 counter-examples in either direction** — 28 `00`, each with no bud worn; 48 `e8`, each with at least one bud worn.
- 🟢 **BB-5 / the `TODO.md` 🔴 "why `CAP-064` read `e8` for 28 s":** buds taken straight from the case to the table read **`00` from 2.6 s to 175 s after leaving the
  case**, 14 samples (6 app Refreshes, 1 app snapshot, 7 Play-services claims), in-ear detection on (field 2 read `1`, E1 301). `CAP-064`'s ≈ 28 s of `e8`
  did not recur here either (`CAP-065`: 5 more samples). 🔴 still unexplained — two runs against one; nothing in this run's logs differs in a way that explains it.
  ADR-049 item 3 (🟡 "`00` ⇒ no bud worn", the converse refuted by `CAP-064`) gains 28 supporting samples and no counter-example; status unchanged.
- 🟢 Transitions are prompt: `00` → `e8` 0.8–2.6 s after a bud went into an ear (A1396, A2090, A4741), `e8` → `00` when the last worn bud came out (A1523, A2738,
  A5431).
- 🟡 Mode reports without an app `Set`: `80` (Transparent) after a bud went into or out of an ear with the other bud not worn or just changed (A1396, A2500, A4741,
  A5221, A6274, A6672, A6800), back to the user's mode (`20`) when both were worn again (A4760, A6598, A6779, A7024) — but A1427 (both worn, 16:14:23) still read
  `80`. A wear-driven mode rule of the Buds; not interpreted further (`CAP-064` §3, `CAP-065` §3 saw `40` in the same places).

## 4. Lead L-1 — the hosting bud (`PROTOCOL.md` §2.2a 2026-10-01 🟡; BB-12 / BB-12m)

Every `GetSoftwareInfo` `RESPONSE` with `call_id` 4294967295 of the run (raw A2547 `7e 80 a3 03 2a 64 22 57 … 08 01 10 13 1d ea 71 de 7d 5e 25 44 fa 99 71 38 ff ff
ff ff 0f b5 f9 b0 f5 7e` = channel `10 13` = 19, CRC OK; A820 `7e 00 a5 03 … 10 15 …` = 21):

| # | Announcement | Ch | Same ACL? | What happened just before (film) | Per-bud charging (stream) | Fits "names the bud hosting the link" |
|---|---|---|---|---|---|---|
| 1 | A820 16:13:13.86 | **21** | new (phone page after Bluetooth-on) | both buds on the table | none charging (A940) | — (no bud out alone) |
| 2 | A2547 16:16:52.21 | **19** | yes — Buds `DISC` 0x02 A2502 16:16:49.881 | **Right out of the ear**, Left worn, session was on **21** | none (A2615) | ✓ **21 → 19** (BB-12m) |
| 3 | A3267 16:17:27.49 | **21** | new (Buds' ConnReq A2804) | Right-slot bud out first (16:17:24), Left 2 s later | none (A3365) | ✓ (first out) |
| 4 | A5268 16:21:39.33 | **19** | yes — Buds `DISC` 0x03 A5223 16:21:37.023 | **Right out of the ear into the case**, Left worn, session was on **19** | Right charging (A5344) | ✓ (19 kept by the worn Left) |
| 5 | A5893 16:22:22.30 | **19** | new (Buds' ConnReq A5530) | only the **Left** out (Right still docked) | Right charging (A5966) | ✓ (as `CAP-065`) |
| 6 | A6441 16:22:56.50 | **21** | yes — Buds `DISC` 0x02 A6278 16:22:53.869 | **Right into the ear**, Left lying on the table, session was on **19** | none (A6507) | ✓ if the worn bud takes over — **19 → 21 on insertion** (new) |
| 7 | A6850 16:24:15.26 | **19** | yes — Buds `DISC` 0x02 A6803 16:24:12.944 | **Right out of the ear**, Left worn, session was on **21** | none (A6914) | ✓ **21 → 19** (BB-12m rep.) |
| 8 | A7274 16:27:07.16 | **19** | yes (re-open after the force-stop) | both worn, unchanged | none | ✓ (unchanged) |
| 9 | B742 16:32:30.45 | **19** | new (phone page after K2) | both worn (off film) | none (B893) | — |
| 10 | B1947 16:33:52.94 | **19** | new (Buds' ConnReq after K3) | both worn (off film) | none (B2046) | — |

**No switch** (no Buds `DISC`) when the bud that is *not* named changed: Left into the ear 16:14:21 and 16:23:00 (on 21), **Left out of the ear 16:23:22** (on 21,
A6672 then nothing until A6800), Left in 16:24:00 (on 21), Right into the ear 16:24:48 (on 19). Command: the control-frame inventory (`DISC` on DLCI 0x02/0x03 by
`p2p_dir` 1: A2502, A5223, A6278, A6803, B2422 — 5 in the run; B2422 after the film) plus the decoder's announcement list.

- 🟢 FACT (this capture): **with both buds worn and the session on 21, taking the Right out made the Buds close MAESTRO and announce 19 — 2 of 2** (#2, #7), and the
  Right going from the ear into the case did the same on 19 (#4). The `0061` prediction "21 → 19 when the Right is taken out" is **confirmed (2/2)**.
- 🟢 FACT: the Buds closed MAESTRO with the ACL up **only** when the Right bud's state changed (out of an ear ×3, into an ear with the Left not worn ×1) — never
  for the Left (4 changes) in this run.
- 🟡 HYPOTHESIS (strengthened, not proven): the announced channel names the bud that hosts the phone's link; the Buds move the link to a **worn** bud when the
  host leaves the ear (#2, #4, #7) and, with no bud worn, to the first bud put in (#6). **Not tested: BB-12** (the Left out with both worn on **19** — the "19 →
  21" half of the prediction): no Left removal happened while on 19 with the Right worn. Settling experiment: both worn on 19, take the Left out → predicted
  Buds `DISC` + 21.
- 🟢 ADR-034 item 3 is unaffected: only 19 and 21 occurred, with the tabulated addresses (`00 3b`/`80 a3`, `00 4b`/`00 a5`).

## 5. Settings (MAESTRO, ADR-036/045/046/047; BB-10)

- 🟢 All pw_rpc packets CRC-32 valid, 0 unparsed: A — 8 announcements, 64 `ReadSetting` + 64 `RESPONSE` OK, 8 `SubscribeRuntimeInfo` + 35 `SERVER_STREAM`, **32
  `WriteSetting` + 32 `RESPONSE` OK**; B — 2 announcements, 16 + 16 reads, 2 subscriptions + 2 stream packets. 0 `CLIENT_ERROR`/`SERVER_ERROR`, no other method.
  Values read every time: EQ `[-1, 0, 4, 2, 0]`, 2:1, 4:1, 7 = Noise control both, 12 = all four, **17:0**, 19:0, 22:1 (E1 295–311).
- 🟢 **BB-10 (restore Right 4 = `17:7`) — not reached.** The 32 writes (A7723 … A7873, 16:30:40–16:31:27; raw A7723 `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 1d 9a
  8c 9e 2a 05 22 03 88 01 0b c2 f4 d8 2c 7e` = `WriteSetting 4:{17:11}` = zigzag −6 = Right 6) carried, decoded with zigzag: Right 6 (×3), Centre 0 (×6), Left 4,
  7 (×4), 9 (×5), 10 (×2), 11, 12, 15, 17 (×2), 25, 26 (×2), 31, 34, 35. **`17:7` (Right 4) was never sent**; the last write is `4:{17:0}` = Centre (A7873, raw
  `… 2a 05 22 03 88 01 00 4a 2d 0a bb 7e`), each answered `RESPONSE` OK and "Setting 17 written" (E2 52–147). The balance is left at Centre, as after `CAP-064`.
  🟡 Cause: a drag on a 201-step slider (`EqScreen.kt:244–250`, `valueRange = -100f..100f`, one write per release) lands on the exact small values only by luck;
  the Right side was reached only as "Right 6" (three tries). Not a protocol issue — §11 item 5.
- 🟢 Runtime-info stream (35 + 2 packets): the per-bud charging field changed with every dock change it reported (§EVENT-NOTES wear mapping, 0 contradictions);
  entry 6.1 (Case) present exactly while a bud was charging (A2633–2724, A5344, A5966; Case 85 → 84 → 83 %); absent otherwise — the app showed "Case: Battery
  unavailable — Not reported yet …" after the force-stop (film 16:27:10) and "last seen" before it (film 16:22:28).

## 6. The `0062` build's UI (F-2, F-4 … F-7; BB-13, BB-14, BB-16, K4d, K4r)

- 🟢 **BB-16 / F-4:** gear → "Settings" with the tabs Settings / Debug / Info (16:13:26–36, 16:27:46–16:28:38, 16:28:42–54, 16:29:30–50); the Debug tab is the
  Debug screen (switch, Export debug log, Unidentified frames). **Back returned to the tab it came from 3 of 3** (Sound 16:28:38, Sound 16:28:54.5, Find 16:29:50.5 —
  4-fps frames; the next tab change each time follows a tap on the bottom bar). Nothing from the app on the wire while the menu was open (no app claim and no
  MAESTRO request 16:27:46–16:28:38, 16:28:42–16:29:50).
- 🟢 **F-5 Info** as designed: build, "Firmware (from the Buds' announcement, HH:MM:SS)", Case / Left bud / Right bud, "Control channel: N" — 21 at 16:13 and 19
  after the restart, matching the announcements.
- 🟢 **K4d / F-6 — done in part:** "On" applied at once (16:28:44 → dark at 16:28:46, no activity relaunch in the logcat), "System" at once (16:32:44 → light at
  16:32:46, Android in light mode); all five tabs readable in dark (16:28:56–16:29:30). **"Off" was not chosen and Android's own dark switch was not toggled** — the
  System-follows-Android behaviour is not tested.
- 🟢 **F-7:** in dark mode the Connection card's "Disconnect" is drawn in the card's own dark text colour (film 16:28:58, full resolution t = 960.5 s). WCAG
  contrast from the camera image (`ffmpeg -ss 960.5 -i CAP-066-recording.mp4 -frames:v 1`; PIL, relative luminance of the darkest 5 % of the label box vs the box
  median): "Disconnect" ≈ **3.11:1** ((132,58,59) on (214,144,144)), "App control: ready" ≈ 2.78:1, "Connected to this phone (Android)" ≈ 3.09:1 — the action is
  now as legible as the card's other text (`CAP-065`: 1.23:1 vs 4.24:1). A camera image (glare, exposure) — approximate; the absolute values are not the screen's.
- 🟢 **BB-13:** the tile is large (subtitle visible) before the film and re-arranged on film (16:25:06–30). **BB-14 (A58-APP-01, F-1/F-2):** a real force-stop
  (§0); the tile read ≈ 9 s after the new process was ready: "ANC / Off" (16:27:16–20). Every "Open the app" subtitle of the run fell in a window when the session
  was not ready (16:16:50 lost 16:16:49.9 → ready 16:16:52.1; 16:17:04–16:17:28 link down; 16:32:18–24 Bluetooth off). "Never 'Open the app' while ready" — **no
  literal hit**; the ≤ 3-s window of the skeleton was not met (⚪ not tested within 3 s).
- 🟢 **K4r — a defect:** rotating to landscape relaunched the activity (logcat `wm_on_stop … handleRelaunchActivity` / `wm_on_create` 14:31:47.259–.326 UTC) and
  the screen switched from **Sound** (frame 16:31:46.5, landscape) to **Connection** (16:31:47.5); rotating back (14:32:12.037) kept Connection. No crash, the
  session stayed `Ready` (no loss line). 🟡 Cause (code reading, `OpenControlNavHost.kt:228–258`): on the relaunch the first composition sees no back-stack entry
  yet (`currentIndex` falls back to 0 = Connection, `:243–244`) and the sync effect scrolls the restored pager to page 0 (`:246–248`); the settled-page collector
  then navigates to Connection (`:251–257`). Settling test: a Robolectric `StateRestorationTester` test — select Sound, `emulateSavedInstanceStateRestore()`, assert
  Sound (§11 item 3).
- 🟢 **BB-9 variant (I-4):** the battery (i) opened ≈ 6 s after the automatic re-open at 16:22:22 showed the new values with their times (16:22:23) and "Case: 83 % —
  last seen …"; the "last connection" marks were not on film (the new report came within 0.1 s of ready, `03 03` at 16:22:22.394).

## 7. Session ends and re-opens (lead 10; ADR-044, T-1)

| # | End (phone) | Ended by | ACL | Film | Cause logged (last line) | Re-open |
|---|---|---|---|---|---|---|
| 1 | 16:13:01.386 | **Bluetooth off** (P2) | off | — | "undetermined (no reading … ) **(provisional …)**" (E1 282) | LINK_BACK 16:13:11.736 after Bluetooth-on |
| 2 | 16:16:49.901 | **Buds `DISC` A2502** | up | Right out of the ear | "the Buds closed the channel **(provisional …)**" (E1 392) | AFTER_LOSS 16:16:51.420 (1.52 s) → ch 19 |
| 3 | 16:17:03.173 | ACL `0x13` A2745 | down | both docked, lid open | "Android's link … went down" (E1 444) | LINK_BACK 16:17:25.444 |
| 4 | 16:21:37.041 | **Buds `DISC` A5223** | up | Right from the ear into the case | "the Buds closed the channel (provisional …)" (E1 574) | AFTER_LOSS 16:21:38.559 → ch 19 |
| 5 | 16:21:48.607 | ACL `0x13` A5444 | down | Left docked | "re-open skipped: … 9484 ms after an automatic re-open" (E1 615), then "went down" (E1 619) | LINK_BACK 16:22:21.221 |
| 6 | 16:22:53.884 | **Buds `DISC` A6278** | up | Right into the ear (Left on the table) | "the Buds closed the channel (provisional …)" (E1 679) | AFTER_LOSS 16:22:55.394 → ch 21 |
| 7 | 16:24:12.960 | **Buds `DISC` A6803** | up | Right out of the ear | "the Buds closed the channel (provisional …)" (E1 725) | AFTER_LOSS 16:24:14.467 → ch 19 |
| 8 | 16:26:54.6 | **force-stop** (phone `DISC` A7219 0.12 s after `am_kill`) | up | App-info "Gedwongen stoppen" | — (process killed) | new process: LINK_BACK at start 16:27:06.016 |
| 9 | 16:32:17.632 | **Bluetooth off** (K1) | off | Quick Settings | "undetermined … (provisional …)" (E2 167) | LINK_BACK 16:32:29.444 after Bluetooth-on |
| 10 | 16:33:28.737 | ACL **`0x08`** B1414 (K3) | down | out of range | "went down" (E2 216) | LINK_BACK 16:33:51.534 on return |

- 🟢 ADR-044 held in every case: one attempt per event, only while visible, the 10-s guard fired once (#5 — the ACL went down anyway), no re-open while the
  link was down; every re-open succeeded.
- 🟢 **T-1 on hardware:** each early verdict carries "(provisional: …)" (17 + 3 lines); where the link went down the final line is unmarked (#3, #5, #10). As
  `ai-sessions/0062` predicted (known limit), a **provisional line stays the last line** when no later reading arrives: the four Buds-side closes (#2, #4, #6, #7,
  correct verdicts) and both Bluetooth-offs (#1, #9: "undetermined", Android's link read `UNKNOWN`, not `NOT_CONNECTED`). 🟡 A Bluetooth-off is never named as
  such in the loss log — §11 item 6.
- 🟢 K3: the out-of-range loss is reason **`0x08`** (connection timeout), not `0x13` — as the skeleton expected.

## 8. StrictMode — the leaked objects (F-8)

`grep -n "StrictMode policy violation"` over both logcats → **5** `LeakedClosableViolation`s (positive control: the stacks are in the same files; release builds
set no policy):

| Time (UTC) | Object finalized without `close()` | Where it was created |
|---|---|---|
| 14:21:54.436, 14:22:57.424, 14:24:15.487 (PID 6546); 14:32:36.646 (PID 12310) | **`android.os.ParcelFileDescriptor`** | `IBluetoothSocketManager$Stub$Proxy.connectSocket` ← `BluetoothSocket.connect(BluetoothSocket.java:655)` ← `BluetoothRfcommSocket.connect(RfcommSocket.kt:58)` ← `RfcommBudsTransport.openSocketWithRetry(RfcommBudsTransport.kt:197)` |
| 14:32:36.652 (PID 12310) | **`android.bluetooth.BluetoothLeAudio`** (a profile proxy) | `BluetoothLeAudio.<init>(BluetoothLeAudio.java:555)` ← `BluetoothAdapter.getProfileProxy` ← `OsConnectionObserver$observe$1.invokeSuspend(OsConnectionObserver.kt:137)` |

- 🟢 FACT (log): every `BluetoothSocket` the app opened is closed by the app (`RfcommBudsTransport.kt` `closeQuietly` on every failure path, the transport's
  teardown on loss) — the logcat shows `D BluetoothSocket: close() … channel=N …` for sockets the app closed while open, and **`V BluetoothSocket: close() …:
  Already closed`** for sockets whose reader had hit end-of-stream (e.g. 14:21:37.028 right after the Buds' `DISC` at 16:21:37.023; the finalizer thread 6607 logs
  the same before each violation).
- 🟡 HYPOTHESIS (strong; source + log strings): the leak is in the framework, not the app. In the Bluetooth module's `BluetoothSocket.java`
  (`platform/packages/modules/Bluetooth`, branch `android16-qpr2-release`, fetched 2026-10-01 — the only branch whose `close()` logs exactly the device's "close() …:
  Already closed" (`Log.v`) and "close() … channel=… mSocketIS=… mSocketOS=… mSocket=… mSocketState=…" (`Log.d`) lines), `read()` does `if (ret < 0) {
  mSocketState = SocketState.CLOSED; throw new IOException("bt socket closed, read return: " + ret); }` (lines 970–972) — the exact text of the app's loss lines —
  and `close()` begins `if (mSocketState == SocketState.CLOSED) { Log.v(TAG, "close() " + this + ": Already closed"); return; }` (1016–1018), so `mSocket` and
  `mPfd` are never closed after a peer-side close; the `ParcelFileDescriptor` is then finalized and reported. The app cannot reach `mPfd` (no public API). Not a
  proof for the phone's own build: its line numbers (`BluetoothSocket.java:655`) match none of the fetched branches.
- 🟡 HYPOTHESIS (source): the `BluetoothLeAudio` report is a framework artifact too — in the same branch `BluetoothLeAudio` opens its `CloseGuard` in the
  constructor (`mCloseGuard.open("close")`, line 754), its `close()` only calls `mAdapter.closeProfileProxy(this)` (759–761) and never `mCloseGuard.close()`, and
  `finalize()` calls `warnIfOpen()` (792–795) — so every finalized LE Audio proxy warns, closed or not (`BluetoothA2dp`/`BluetoothHeadset` have no `CloseGuard`:
  `grep -c CloseGuard` → 0). The app does close its proxies (`OsConnectionObserver.kt:141–144`); 🟡 it does **not** close a proxy that was unbound before the flow
  ended (`onServiceDisconnected` removes it from the map, `:125–126`) — e.g. at a Bluetooth-off — which would leave a `ProfileConnection` registered in the
  adapter. §11 item 4.
- 🟢 The three `CAP-065` "A resource failed to call close." lines are therefore explained (same thread pattern, next to "Already closed"). No app-owned leak was
  found.

## 9. HFP, AVRCP, GSND, LE, other hosts

- 🟢 HFP: 3 SLCs (one per phone-or-Buds-created ACL with HFP; `AT+BRSF`, `AT+CIND=?`, `AT+CMER`, `AT+BIND` …), 29 × `AT+BIEV`, 0 × `AT+BVRA` (`grep -c BVRA` → 0;
  positive control: 29 `AT+BIEV`). Not an app source (ADR-040).
- 🟡 **New, a Buds-side oddity:** three times the Buds sent `AT+NREC=0` followed by stray bytes on the HFP channel (A3192 DLCI 0x08 `41542b4e5245433d30 e4 79 66 43
  34 80 24 ff 96 c9 33 0e 15 03 f0 35 05 0a 0d`; A6112 DLCI 0x09 `41542b4e5245433d30 ea71de7d5e2590821ee6602d65a97e 86 19 01 0d`; B1975), each right after a
  clean `AT+NREC=0\r` (A3182, A6087). A6112's stray bytes `ea 71 de 7d 5e 25 90 82 1e e6 60 2d 65 a9 7e` are byte for byte the tail of the app's
  `SubscribeRuntimeInfo` request on channel 19 (`7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 90 82 1e e6 60 2d 65 a9 7e`, sent 0.8 s earlier, E1 656) — a buffer reused
  inside the Buds' firmware. The phone answered `+CME ERROR` (A6114). No effect on the app; out of scope beyond this note.
- 🟢 AVRCP (20 + 12 frames): per ACL `GetCapabilities`, `RegisterNotification` PlaybackStatusChanged **"Paused"**, VolumeChanged; no AVDTP `Start` — **no music
  played** (P5 not used; `AUDIO-001` not exercisable).
- 🟢 The app opened only RFCOMM server channels 1 and 2 (logcat `connect(), socket connected. mPort=1` ×6, `mPort=2` ×7; no other value) — nothing on DLCI
  0x08/0x0a from the app. DLCI 0x04/0x05 outside the app's 23 claims is Play services' (26 claims; `03 08 00 02 01 25`, SASS `07 11`, `07 10`, `08 11` — Play
  services sends a `Get` too).
- 🟢 LE: only `c8:cc:a8:e7:48:93` (handle `0x0041`) = the Fitbit Charge 6 (CDM association 37, `com.fitbit.FitbitMobile`). No LE link to the Buds; no pairing,
  bonding or CDM event for the Buds or the app (`grep -c` of revoke / bond / association lines → 0; positive control: 26 `CDM_DevicePresenceProcessor` lines).

## 10. Protocol correlation (per channel)

| Channel / protocol | What the app does | What the Buds answer | Goes well | Goes wrong / to improve |
|---|---|---|---|---|
| **MAESTRO pw_rpc** (DLCI 0x02/0x03, ch 19/21) | 10 session opens (all automatic), 80 reads, 10 + 0 subscriptions, 32 balance writes | 80 + 32 `RESPONSE`s OK, 37 stream packets | 0 errors; channel follows the hosting bud | balance target not reachable by drag (§5) |
| **Message Stream** (DLCI 0x04/0x05) | 23 claims, each `08 11` first; 4 `Set`s | Device Info, battery, `Notify`, 4 ACK, 0 NAK | F-1 exactly as designed; 17 first attempts collide (as before) | F-3 not exercised (no cut-off happened) |
| **SASS / Play services** | nothing | — | — | — |
| **HFP / AVRCP** | nothing | `AT+BIEV`; Paused | — | the Buds' stray `AT+NREC` bytes (§9) |
| **Android link** | mirrors it; ADR-044 re-open | — | 5/5 automatic re-opens on link-up; K3 `0x08` | a Bluetooth-off loss stays "undetermined (provisional)" |
| **UI** | menu, Info, dark mode, tile | — | F-2, F-4, F-5, F-7 on film | **rotation resets the tab** (§6) |

## 11. Improvements (proposals only — nothing under `android/` was changed)

1. **No change needed for F-1/F-2** — they behave as designed on hardware; keep. (The `Get` costs ≈ 260 ms per tap.)
2. **F-3 still unverified** — keep BB-15 as a watch item in the next run; the code path exists (`BudsRepositoryImpl.kt:772`) but the collision never fell between a
   request and its answer here.
3. **Keep the tab across a configuration change (K4r, §6, a defect).** Cause (🟡): `OpenControlNavHost.kt:243–248` scrolls the pager to `currentIndex` while the
   restored back stack is not yet known (index 0). Proposed: do not sync pager → back stack until `backStackEntry` is non-null (skip the effect while
   `currentDestination == null`), or derive the pager's initial page from the saved nav state. Guardrails: UI only, no wire change. Test: Robolectric
   `StateRestorationTester` (`:ui`, already on the classpath for `SettingsMenuTest`) — select Sound, `emulateSavedInstanceStateRestore()`, assert the Sound content
   and the selected Sound item. Hardware re-test: Sound tab → rotate → still Sound; HCI bracket: none (UI only).
4. **StrictMode noise (F-8).** Both objects are framework-owned (§8); no app fix is possible for the `ParcelFileDescriptor`. Proposed: keep the policy, record in
   `ARCHITECTURE.md` §12 that the expected violations are these two framework objects, and (🟡, optional) close a profile proxy also when it is unbound
   (`OsConnectionObserver.observe`: keep the proxy objects in a separate list and `closeProfileProxy` all of them in `awaitClose`). Test: the observer test with a
   fake adapter that unbinds a proxy before the flow ends → assert `closeProfileProxy` called for it.
5. **Balance precision (BB-10, UX).** A ±100 slider over the screen width cannot hit Right 4 reliably. Proposed: `steps` on the balance slider (e.g. 40 steps of
   5) or small −/+ buttons next to it (one write per tap). Needs a maintainer choice; wire unchanged (`WriteSetting 4:{17:…}`, ADR-045). Test: `EqScreenTest` —
   a tap on "+" from Centre writes −1 (Right 1) … per the chosen step.
6. **Name a Bluetooth-off loss (T-1, cosmetic).** When Android's link reads `UNKNOWN` because the adapter is off, log "Bluetooth was switched off" instead of a
   lasting "undetermined (provisional)". Guardrails: `BluetoothAdapter.ACTION_STATE_CHANGED` is already observed (`AGENTS.md` §2); no new permission. Test: a
   `SessionLoss` unit test with an adapter-off reading.
7. **Documentation:** `CAP-064-FINDINGS.md` §0 says the data were cleared at installation; its own line 8044/8048 is the same code-cache-only clear (`flags=39`,
   `prepPerformDexoptIfNeeded`) — rewrite that sentence in place (rule 9a; a FINDINGS correction, no FACT/ADR involved).

## 12. Open questions

- 🔴 Why `CAP-064` read `e8` for 28 s with both buds on the table straight from the case (two later runs: always `00`, 19 samples).
- 🔴 BB-12: does the channel switch 19 → 21 when the **Left** is taken out with both worn on 19?
- 🔴 F-3 on hardware (no cut-off occurred).
- 🔴 Why the phone pages the Buds 0.1 s after an ACL drop at docking (16:35:37, after the film; `CAP-064` §1a) — no Bluetooth-process log at that time.
- 🔴 K4d's "Off" and Android's own dark switch with "System"; L3 (an export with Debug mode off); K5, A5, (E), B4, Z1 — not done.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-066-2026-10-01_16-12-57_16-34-50-Group_BB/CAP-066-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-066-2026-10-01_16-12-57_16-34-50-Group_BB/CAP-066-FINDINGS
