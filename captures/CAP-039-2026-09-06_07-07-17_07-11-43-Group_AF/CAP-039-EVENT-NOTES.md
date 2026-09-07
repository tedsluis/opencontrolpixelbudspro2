# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AF, `Settable-toggles` byte: Set-tap vs. reconnect-Get, same session/dock-state (`CAP-039`)

**Status:** ✅ **Captured, video-reviewed, and decoded — see `CAP-039-FINDINGS.md`.** The session
ran 6 reconnects and 4 ANC taps, not the planned 1 Get + 1 Set. Folder renamed to
`CAP-039-2026-09-06_07-07-17_07-11-43-Group_AF`.

**Status legend used throughout this file** (`PROJECT_RULES.md` §1, `PROTOCOL.md` §0): 🟢 **FACT**
· 🟡 **HYPOTHESIS** · ⚪ **ASSUMPTION** · 🔴 **OPEN QUESTION**. Never write a conclusion in this
file without one of these four labels.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AF, `OBS-006`, new):** the original open question
(`CAP-036-FINDINGS.md` §3) was framed as "does the `Settable-toggles` byte differ between a
`Set`-triggered Notify and a `Get`-triggered Notify?" `DECISIONS.md` ADR-024 has since reframed
this: across 7 video-confirmed samples, the byte tracks **dock state** (`0x00` docked, `0xe8`
otherwise), not trigger type — but every one of those samples came from a **different session**,
so the reframing itself has never been tested by directly comparing a Set and a Get **within one
session, at a fixed, known dock state**. This session does exactly that: one ANC tap (a `Set`)
and one forced reconnect (a `Get`), both performed with the Buds in the **same** dock state
(undocked/worn), to see whether the byte is identical in both cases (supporting ADR-024's
dock-state reading) or still differs by trigger type (meaning ADR-024's reframing was incomplete).

**Method:** Pixel 7a, stock Android, official Pixel Buds Companion App installed and open on
Device details, Google Play Services **enabled**. Buds already bonded, worn in the ears for the
entire session (constant dock state = undocked throughout, so dock state cannot be the explanation
for any difference observed).

**⚠️ Rule:** exactly one ANC tap this session, nothing else touched. Keep the Buds in your ears
(undocked) for the *entire* session, including during the forced reconnect — do not put them back
in the case at any point, or the dock-state variable stops being held constant.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-039`                      |
|      Group(s)    | AF (`OBS-006` — Settable-toggles Set-vs-Get, fixed dock state; incidental `ANC`-family, `PAIR-003`) |
|       Date       |                     2026-09-06                      |
| Firmware version | ⚪ ASSUMPTION `release_5.203` |
|   Test device    | Pixel 7a, Android 14. **Official Pixel Buds Companion App, Google Play Services enabled** |
| Video file       | `CAP-039-recording.mp4` — 05:03s, `07:07:17`–`07:12:20` local time |
| Log file         | `CAP-039-btsnoop_hci.log` — 372.04s, 6,184 packets, 2026-09-06 07:07:28.040–07:13:40.082 local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` |

**Capture-integrity pre-flight** — identical method to `CAP-036`/`CAP-037`/`CAP-038`. Result:
`capinfos` shows no snaplen cap; `tshark -r CAP-039-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len | awk '$2!=$3{c++} END{print c+0}'` → `0` mismatches. Untruncated, raw
extraction path. Note: the log's own first frame is `Sent Reset` (a full HCI controller
init/reboot sequence), confirming Bluetooth was freshly re-enabled right at the start of this
session (consistent with the prep checklist's "phone rebooted" step) — the log does not
pre-date this reboot, so anything before it (including, it turns out, the video's very first
seconds) is not captured. See §2's video re-pass below — this explains why the documented
`07:07:23` tap has no corresponding wire frame.

## Preparation checklist (before recording)

- [x] Buds already bonded and connect normally. Do **not** "Forget" or re-pair.
- [x] Official Pixel Buds Companion App installed and working.
- [x] Google Play Services **enabled** — carried `CAP-036`'s verification forward as ⚪ ASSUMPTION.
- [x] No third-party BLE/GATT tool used.
- [x] Bluetooth HCI snoop logging enabled and the phone rebooted.
- [x] Video recording with a visible wall-clock overlay ready.
- [x] **Note the current ANC mode before recording:** 'Off' mode.
- [x] Decide in advance which ANC mode you'll tap to: Tapped from 'Off' to 'Noise cancellation'.
      **Correction (video/wire re-pass, see Event Timeline below): this pre-session plan was not
      what actually happened on camera/wire.** The session opened with mode already at Noise
      cancellation (not Off), and 4 taps alternated Noise cancellation ↔ Adaptive — "Off" is never
      selected anywhere in this session's wire or video evidence.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AF)

1. Start video recording (wall-clock overlay visible), confirm HCI snoop logging active. Buds
   already in ears, already connected, Device details screen open.
2. **Window 1 — the Set** [`ANC`-family, incidental]. Tap a different ANC mode once. Note the
   exact tap time and the resulting on-screen mode. Idle ~5s.
3. Clean buffer (~5s), nothing touched.
4. **Window 2 — the Get** [`OBS-006`, incidental `PAIR-003`]. Force a reconnect **without removing
   the Buds from your ears** — toggle Bluetooth off then on again from system settings (the Buds
   stay in your ears throughout; this only affects the phone side of the link). Note the exact
   toggle-off and toggle-on times, and the on-screen reconnection-complete time. Idle ~10s without
   touching anything.
5. Stop video recording and HCI snoop logging.
6. Extract via the raw btsnoop path first, then run the capture-integrity pre-flight.

## Event Timeline

**🟢 CORRECTED via full video re-pass (`ffmpeg` frame extraction against the wall-clock overlay,
cross-checked against the wire log) — per `CAP-036-FINDINGS.md` §2's precedent method. The
originally-documented timeline above (`07:07:23` single Set, `07:08:16` single Get) does NOT match
the video or the wire: the video's own overlay shows the phone in the system Bluetooth quick-panel
at `07:07:22`, not the Device-details screen, and the actual video/log evidence shows SIX
reconnects and FOUR ANC Set taps across the session — the user was toggling Bluetooth/reconnecting
repeatedly throughout (reason `0x16`, "Terminated by Local Host", on 4 of the 5 disconnects — the
same locally-initiated pattern `CAP-040`'s own procedure deliberately used), not the single
clean Set-then-Get this Group's procedure called for. This is corrected here as real evidence,
not silently reconciled.**

| Time (local, video-confirmed unless noted) | Action / Event | Initiator | Test-ID | Evidence in `CAP-039-btsnoop_hci.log` |
|---|---|---|---|---|
| `07:07:17` | Start video recording | — | — | — |
| `07:07:22` | Phone shown in system Bluetooth quick-panel, Buds already "Active" (L:98%/R:100%); user's finger near the Buds row (video-confirmed, `t6.png`) — the log has not started yet (first log frame is `07:07:28.040`, a full `Sent Reset` HCI re-init sequence) | User (OS) | — | — (predates log) |
| `07:07:28.040` | Log starts — HCI controller Reset/init sequence (consistent with the "phone rebooted" prep step) | — | — | frame 1 |
| `07:07:33`–`34` | "Pixel Buds Pro 2 van Ted — Connecting…" shown on screen (video-confirmed, `t17.png`); wire Connection Complete chandle `0x0002` (BLE) @`07:07:34.091`, chandle `0x0005` (classic) @`07:07:35.111` | App/OS (Auto) | `PAIR-003` | frames 653, 683 |
| `07:07:39.230`/`.412` | DLCI 0x04 Get (`08 11 00 00`) / Notify (`08 13 00 04 01 e8 08 08`) immediately after reconnect — Current state `0x08` = Noise cancellation (pre-existing mode, matches on-screen "Noise cancellation" selected at `t40.png`) | App/OS (Auto) | `OBS-006` (Get #1) | frames 890/909 |
| `07:07:56`–`57` | **Real "Window 1" tap** — Device details screen, user taps **"Adaptive"** tile (video-confirmed, `t40.png` — finger on 3rd tile; mode was Noise cancellation, NOT "Off" as originally documented) | User (App) | `ANC`-family | Set frame 1411 @`07:07:57.588` |
| `07:07:57.896` | Notify: Current `0x40` = Adaptive | App (Auto) | — | frame 1416 |
| `~07:07:43`–`46` | Rapid ~300-frame DLCI 0x02 write burst (consistent with an EQ slider drag — see Decode/Analysis §, bonus) | User (App) | — | frames 1203–1362 (representative) |
| `07:08:11.861` | Disconnection Complete, chandle `0x0005`, reason `0x16` (Terminated by Local Host) | User (OS)/App | `PAIR-003` | frame 1628 |
| `07:08:29.642`/`.979` | Reconnect #2 — chandle `0x0006`; DLCI 0x04 Get/Notify, Current `0x40` (still Adaptive) | App/OS (Auto) | `OBS-006` (Get #2) | frames 1939/1967 |
| `07:08:51.455` | Disconnection Complete, chandle `0x0006`, reason `0x16` | User (OS)/App | `PAIR-003` | frame 2461 |
| `07:09:04.052`/`.459` | Reconnect #3 — chandle `0x0007`; Get/Notify, Current `0x40` (still Adaptive) | App/OS (Auto) | `OBS-006` (Get #3) | frames 2766/2821 |
| `07:09:16.651`/`.795` | **Tap 2** — Set → Notify Current `0x08` (Noise cancellation) | User (App) | `ANC`-family | frames 3194/3197 |
| `07:09:30.824` | Disconnection Complete, chandle `0x0007`, reason `0x16` | User (OS)/App | `PAIR-003` | frame 3298 |
| `07:09:42.452`/`.873` | Reconnect #4 — chandle `0x0008`; Get/Notify, Current `0x08` (still NC) | App/OS (Auto) | `OBS-006` (Get #4) | frames 3586/3624 |
| `07:09:57.168`/`.399` | **Tap 3** — Set → Notify Current `0x40` (Adaptive) | User (App) | `ANC`-family | frames 4153/4161 |
| `07:10:11.215` | Disconnection Complete, chandle `0x0008`, reason `0x16` | User (OS)/App | `PAIR-003` | frame 4312 |
| `07:10:25.025`/`.079` | Reconnect #5 — chandle `0x0009`; Get/Notify, Current `0x40` (still Adaptive) | App/OS (Auto) | `OBS-006` (Get #5) | frames 4648/4692 |
| `07:10:43.569`/`.913` | **Tap 4** — Set → Notify Current `0x08` (Noise cancellation) | User (App) | `ANC`-family | frames 5089/5095 |
| `07:10:53.223` | Disconnection Complete, chandle `0x0009`, reason `0x13` (Remote User Terminated — the one disconnect NOT locally-initiated) | Buds (Auto)? | `PAIR-003` | frame 5184 |
| `07:11:04.637`/`.662` | Reconnect #6 — chandle `0x000a`; Get/Notify, Current `0x08` (still NC) | App/OS (Auto) | `OBS-006` (Get #6) | frames 5462/5496 |
| `07:11:36` | Device details screen, "Noise cancellation" selected, L:98%/Case:85%/R:100% still shown (video-confirmed, `t260.png`) | — | — | — |
| `~07:11:43` | Video ends (ffprobe duration 265.62s; 07:07:17+265.62s) — log continues to `07:13:40.082` with idle BLE advertising only, nothing further of interest | — | — | frame 6184 (log end) |

**Contamination log:** The originally-documented "one Set, one Get" plan was not followed. The
user instead performed **6 reconnects** (5 of them locally-initiated disconnect/reconnect cycles,
reason `0x16`, matching the same button-mashing pattern `CAP-040`'s own procedure used
deliberately) and **4 ANC taps** alternating Noise cancellation ↔ Adaptive (never "Off"), all
interleaved with a mid-session EQ-slider-drag-shaped DLCI 0x02 burst and other settings navigation
(In-Ear Detection, Touch Controls, Multipoint — not individually decoded, see Decode/Analysis).
This is treated as additional evidence per this task's brief, not noise: it turns OBS-006's
single Set-vs-Get comparison into a **6-Get/4-Set, same-session, constant-dock-state dataset** —
a much stronger test of `DECISIONS.md` ADR-024 than originally planned. The Buds remained
undocked (never redocked) for the entire session, confirmed both by procedure and by the
`Settable-toggles` byte staying `0xe8` on every single Get/Notify (see Decode/Analysis).

## Decode / Analysis

```
tshark -r CAP-039-btsnoop_hci.log -Y "bthci_acl.chandle==<Buds chandle> and btrfcomm.dlci==4 and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data
```

- [x] **All 4 Set frames' resulting Notify `Settable-toggles` byte:** `0xe8` in every case (frames
      1416, 3197, 4161, 5095 — see full table in `CAP-039-FINDINGS.md` §3).
- [x] **All 6 Get/Notify pairs' `Settable-toggles` byte:** `0xe8` in every case (frames 909, 1967,
      2821, 3624, 4692, 5496).
- [x] **Direct comparison:** all 10 occurrences (4 Set-triggered + 6 Get-triggered) read
      **identically `0xe8`**, with the Buds undocked/worn throughout and never redocked. This
      **directly supports `DECISIONS.md` ADR-024's dock-state reading** — trigger type (Set vs.
      Get) does not matter, only dock state, now confirmed with 10 same-session samples instead of
      1+1. No counter-example found.
- [x] **Outcome classification: (a)** — all 10 values `0xe8` (undocked), matching ADR-024,
      trigger-independent. This is a same-session confirmation (10 occurrences, not the originally
      planned 2), not a new promotion — see `CAP-039-FINDINGS.md` for the proposal write-up.

## Open Questions

- 🔴 What causes the app/OS to disconnect-and-immediately-reconnect 5 times in ~3.5 minutes with no
  visible on-screen trigger for most of them (reason `0x16`, locally-initiated)? Not resolved by
  this capture — flagged for a future session with continuous full-screen video coverage of the
  Bluetooth quick-panel/app, since this session's camera was mostly on the Device details screen
  and did not show whichever UI action triggered each disconnect.
- 🔴 DLCI 0x04 `Group 0x03 Code 0x03` (`03 03 00 03 <L> <R> ff`) recurs constantly through this
  session with value `62 64 ff` (Left=98,Right=100 — matching on-screen exactly) and changes to
  `61 64 ff` (Left=97) once, at `07:13:26`, after the video's own coverage ends — reproduces
  `CAP-009-FINDINGS.md` §7's existing 🟡 HYPOTHESIS candidate for `PROTOCOL.md` §4.3 Option B's open
  battery code with a live 98→97 Left transition, but the transition itself is not video-confirmed
  (past video end) — see `CAP-039-FINDINGS.md` §4.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13).
- [ ] Write `CAP-039-FINDINGS.md` per `PROJECT_RULES.md` §2, hex & script rule (§1 rule 4a).
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index (status → `captured`/`analyzed`) and
      `id_registry.csv`'s `CAP-039` row.
- [ ] Add `OBS-006`'s Evidence column pointer in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (pointer only).
- [ ] Copy every new 🔴 OPEN QUESTION into `PROTOCOL.md` §6 (mandatory, §8).
- [ ] **Do not** promote anything to 🟢 FACT or write/amend a `DECISIONS.md` ADR without explicit
      maintainer sign-off (`AGENTS.md` §6, §15) — propose only, even if this session's result
      would revise ADR-024.
- [ ] Rename this capture's folder to the actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-039-2026-09-06_07-07-17_07-11-43-Group_AF/CAP-039-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-039-2026-09-06_07-07-17_07-11-43-Group_AF/CAP-039-EVENT-NOTES
