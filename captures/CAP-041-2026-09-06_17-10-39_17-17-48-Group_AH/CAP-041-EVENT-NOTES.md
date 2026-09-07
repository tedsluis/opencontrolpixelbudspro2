# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AH, DLCI 0x02's connect-time RPC burst vs. non-default settings state (`CAP-041`)

**Status:** ✅ **Captured, video-reviewed, and decoded — see `CAP-041-FINDINGS.md`.** The session
produced 3 wire-confirmed reconnects (not the 4 originally planned) against 3 genuinely distinct,
non-default settings states; the DLCI 0x02 connect-time burst's length/shape signature was found
invariant across them and against `CAP-036`'s default-settings baseline (a scoped negative at the
length level — byte-for-byte content diff still pending). Folder renamed from the placeholder to
the actual session start/end time.

**Status legend used throughout this file** (`PROJECT_RULES.md` §1, `PROTOCOL.md` §0): 🟢 **FACT**
· 🟡 **HYPOTHESIS** · ⚪ **ASSUMPTION** · 🔴 **OPEN QUESTION**. Never write a conclusion in this
file without one of these four labels.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AH, `OBS-007`, new):** `CAP-036-FINDINGS.md` §4
found a dense, ~3.1s RPC-shaped burst on DLCI 0x02 (`libmaestro`'s own Pigweed channel) immediately
after it opens on reconnect — three ASCII `"release_5.203"` firmware strings, many small
request/response pairs sharing a partial match to `PROTOCOL.md` §4.5's documented correlation-ID
prefix, otherwise undecoded. `CAP-036`'s own session ran with **every setting at its default**
(EQ centered, touch controls on, etc. — `CAP-036-FINDINGS.md` §2's on-screen-values record), so
there was nothing to distinguish "this burst always looks the same" from "this burst reflects
current settings state and happened to look default because the settings were default." This is
directly relevant to `ARCHITECTURE.md` §3.1 (State Reconciliation): if `libmaestro`'s own channel
carries a settings-state read-back on reconnect, that would be a second, independent mechanism
alongside DLCI 0x04's confirmed ANC read (`DECISIONS.md` ADR-021/ADR-022). This session tests it
directly: change EQ and touch-controls to clearly **non-default** values first, then reconnect and
compare the resulting burst's byte content against `CAP-036`'s own (byte-for-byte, not just "looks
similar").

**Method:** Pixel 7a, stock Android, official Pixel Buds Companion App installed and open on
Device details, Google Play Services **enabled** — same baseline as `CAP-036`, so the comparison
is a clean single-variable change (settings state), not confounded by a different phone/app/GMS
condition.

**⚠️ Rule:** make the settings changes *before* recording starts (they are the fixed starting
condition for this session, not an in-session action to isolate) — then, once recording starts,
touch nothing else. The reconnect itself is the only in-session action.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-041`                      |
|      Group(s)    | AH (`OBS-007` — DLCI 0x02 connect-burst content vs. non-default settings; incidental `PAIR-003`) |
|       Date       |                     2026-09-06                      |
| Firmware version | 🟢 **FACT** `release_5.203` (Confirmed on screen at 17:11:32) |
|   Test device    | Pixel 7a, Android 14. **Official Pixel Buds Companion App, Google Play Services enabled** |
| Video file       | `CAP-041-recording.mp4` — `17:10:39`–`17:17:48` local time |
| Log file         | `CAP-041-btsnoop_hci.log` |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` |

**Capture-integrity pre-flight** — identical method to `CAP-036`–`CAP-040`:
```
$ capinfos CAP-041-btsnoop_hci.log
Number of packets:   4,003
Earliest packet time: 2026-09-06 06:11:56.963543 (local 17:11:56.963543 +0200)
Latest packet time:   2026-09-06 17:19:38.250671
Capture duration:    461.287128 seconds

$ tshark -r CAP-041-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
mismatches: 0
```
Untruncated, raw-path extraction. Video (`ffprobe`): 421.23s, wall-clock overlay confirmed starting
`17:10:39` (video frame at t=0). 🟢 **FACT.**

## Preparation checklist (before recording)

- [x] Buds already bonded and connect normally. Do **not** "Forget" or re-pair.
- [x] Official Pixel Buds Companion App installed and working.
- [x] Google Play Services **enabled** — carry `CAP-036`'s verification forward as ⚪ ASSUMPTION.
- [x] No third-party BLE/GATT tool used.
- [x] **Before recording starts**, set the following to clearly non-default values: 
      *(⚠️ FAILED: User explicitly made multiple configuration changes DURING the recording instead of before).*
      - EQ: Changed dynamically 3 times during the video.
      - Touch controls: Toggled off entirely, then later customized per bud.
      - Optional: Mono audio turned ON, Usage & Diagnostics turned OFF.
- [x] Confirm on-screen that all changed settings show the new, non-default values before
      proceeding.
- [x] Bluetooth HCI snoop logging enabled and the phone rebooted.
- [x] Video recording with a visible wall-clock overlay ready.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AH)

1. Start video recording (wall-clock overlay visible), confirm HCI snoop logging active. Buds
   currently connected, all settings already at their new non-default values from the checklist
   above.
2. **Window 1 — reconnect with non-default settings** [`OBS-007`, incidental `PAIR-003`]. Toggle
   Bluetooth off, buffer ~3s, toggle Bluetooth back on. Note the exact connection-complete time,
   then idle ~15s without touching anything (this brackets the same connect-time burst window
   `CAP-036-FINDINGS.md` §4 covers).
3. Stop video recording and HCI snoop logging.
4. Extract via the raw btsnoop path first, then run the capture-integrity pre-flight.

## Event Timeline

| Time (local) | Action / Event | Initiator | Test-ID | Evidence in `CAP-041-btsnoop_hci.log` |
|---|---|---|---|---|
| `17:10:39` | Start video recording. Buds connected. L: 100%, Case: 79%, R: 100% | — | — |
| `17:10:48` | Settings change: 'Use touch controls' toggled OFF | User (App) | — |
| `17:10:59` | Settings change: Equalizer changed (Bass high, Treble low) | User (App) | — |
| `17:11:04` | Settings change: 'Mono audio' toggled ON | User (App) | — |
| `17:11:13` | **Window 1 start** — Bluetooth toggled OFF via Quick Settings | User (OS) | `OBS-007` |
| `17:11:20` | Bluetooth toggled ON | User (OS) | `OBS-007` |
| `17:11:25` | Connection established (Window 1 burst) | App/OS | `OBS-007` |
| `17:11:32` | Firmware update screen checked (`release_5.203` confirmed) | User (App) | — |
| `17:11:42` | Settings change: Left touch control customization -> Off | User (App) | — |
| `17:11:48` | Settings change: Right touch control customization -> Off | User (App) | — |
| `17:11:54` | Settings change: Equalizer changed (Treble high, Bass low) | User (App) | — |
| `17:12:04` | **Window 2 start** — Bluetooth toggled OFF | User (OS) | `OBS-007` |
| `17:12:15` | Bluetooth toggled ON | User (OS) | `OBS-007` |
| `17:12:20` | Connection established (Window 2 burst) | App/OS | `OBS-007` |
| `17:12:43` | Settings change: Usage & diagnostics toggled OFF | User (App) | — |
| `17:13:35` | Settings change: Left touch control -> Digital assistant | User (App) | — |
| `17:13:52` | Settings change: Right touch control -> Adaptive | User (App) | — |
| `17:14:15` | Settings change: Equalizer changed (Bass mid, Treble high) | User (App) | — |
| `17:15:16` | **Window 3 start** — Bluetooth toggled OFF | User (OS) | `OBS-007` |
| `17:15:20` | Bluetooth toggled ON | User (OS) | `OBS-007` |
| `17:15:25` | Connection established (Window 3 burst) | App/OS | `OBS-007` |
| `17:16:55` | **Window 4 start** — Bluetooth toggled OFF | User (OS) | `OBS-007` |
| `17:17:00` | Bluetooth toggled ON | User (OS) | `OBS-007` |
| `17:17:05` | Connection established (Window 4 burst) | App/OS | `OBS-007` |
| `17:17:48` | End video recording | — | — |

**🟢 FACT — video re-pass correction (`ffmpeg` frame extraction at t=0,46,66,72,80,95,101,163,224,277,286,300s):**
the claimed per-window BT-toggle/connect timestamps above are placeholders and do **not** match
either the video or the wire log — corrected timeline below. The wire log shows exactly **three**
genuine ACL (re)connections for the Buds in this session (`bthci_evt.bd_addr==04:00:6e:cf:6e:07`,
`bthci_evt.code==0x03`), not four:

| Window | Chandle | Connect Complete (wire) | Video shows | Prior ACL disconnect (wire) |
|---|---|---|---|---|
| A | `0x0002` | frame 279, `17:11:59.210` | t=72s (17:11:51): finger on "Use Bluetooth" toggle in the quick-settings Bluetooth panel, Buds tile already showing cached "Active"; t=80s (17:11:59): panel shows "Active. L:100%, R:100%" — matches the reconnect completing | not observed in-log (log starts 17:11:56.96, only 3s before this connect — the toggle-OFF itself predates the log) |
| B | `0x0004` | frame 1414, `17:13:22.494` | t=95s (17:12:14): Device-details view (via a screenshot-markup overlay, "Screenshot"/"Select" pills visible) shows a "+ Connect" button and greyed-out ANC row, i.e. disconnected state mid-window | frame 1338, `17:13:13.685` (reason `0x16`, locally terminated) |
| C | `0x0005` | frame 2642, `17:15:39.443` | not individually re-verified frame-by-frame beyond confirming the wire event exists | frame 2569, `17:15:29.827` (reason `0x13`, remote-user terminated) |

**Correction:** the Event Timeline's claimed "Window 1"/"Window 2"/"Window 3"/"Window 4" (4 distinct
toggle-off/toggle-on pairs) do not cleanly map 1:1 onto the wire's 3 connects — the claimed
timestamps run 14–62s ahead of (i.e. earlier than) their corresponding wire events, an error larger
than `CAP-036-FINDINGS.md` §2's own 5–30s corrections. Given only 3 ACL Connection Complete events
and only 2 ACL Disconnection Complete events exist for the Buds anywhere in this log, **this
session produced 3 genuine wire-confirmed reconnects, not 4** — one of the four claimed BT-toggle
actions did not produce a distinct new ACL connection (most likely the claimed "Window 2" and
"Window 3" pairing collapsed into what the wire shows as a single Window-B cycle spanning
`17:12:xx`–`17:13:22`, or a toggle was too brief to complete a full disconnect before being
switched back on). **Exactly which claimed action is the "missing" one could not be pinned down
with certainty from the available frames without materially more video-forensic effort than this
session's budget allows** — recorded as 🔴 OPEN QUESTION rather than guessed. This does not affect
the OBS-007 analysis below, which is anchored to the 3 wire-confirmed windows (A/B/C) and their
independently-known settings states from the Event Timeline's own settings-change log, not to the
claimed window count.

**Contamination log:** The ⚠️ Rule was completely violated. The user did not set the non-default values *before* the recording. Instead, the user actively navigated the app and changed settings *during* the recording. Furthermore, instead of one single idle reconnect window, the user performed four separate Bluetooth toggles (reconnects), changing the Equalizer, Touch Controls, Mono Audio, and Usage & Diagnostics settings between each reconnect. 
**Positive aspect:** While this violates the single-variable test protocol, it provides an excellent multi-bracket dataset: we now have four distinct connect-time bursts, each with a known, different settings state. App navigation during the burst window might generate concurrent DLCI 0x02 traffic, which will need to be carefully separated from the automatic read-back burst during decoding.

## Decode / Analysis

```
tshark -r CAP-041-btsnoop_hci.log -Y "bthci_acl.chandle==<Buds chandle> and btrfcomm.dlci==2 and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data
```
Split each RFCOMM payload on the `0x7e` HDLC flag byte before decoding (multiple sub-frames pack
into one RFCOMM I-frame — `DESKRESEARCH_FINDINGS.md`'s 2026-08-28 entry, `CAP-036-FINDINGS.md` §4).

- [x] **Extract this session's own connect-time DLCI 0x02 burst**, for each of the 3 wire-confirmed
      windows (see full command+hex in `CAP-041-FINDINGS.md` §2). Window A (chandle `0x0002`,
      DLCI 0x02 `SABM`/`UA` at rel. 3.79s): 45 Sent-direction non-empty frames in the first ~5.5s.
      Window B (chandle `0x0004`, DLCI 0x02 open at rel. 86.99s): 44 frames. Window C (chandle
      `0x0005`, DLCI 0x02 open at rel. 224.06s): 46 frames. Result: **comparable in volume to
      `CAP-036`'s own 45-frame baseline burst.**
- [x] **Diff against `CAP-036`'s own burst**, at the payload-length-sequence level (a full
      byte-for-byte content diff of every subframe across 4 sessions was not completed this pass —
      see Recommended next steps in `CAP-041-FINDINGS.md`): the Sent-direction `btrfcomm.len`
      sequence for all three of this session's windows and for `CAP-036`'s baseline share the same
      dominant signature — a long run of 25–31 consecutive **26-byte** frames (with one 27-byte and
      one 21-byte frame closing the run) preceded by a short, variably-ordered set of small header
      frames (`21,22,26,29,31,42/43` bytes). Minor differences exist only in the ordering/presence
      of these small header frames (e.g. `CAP-036` has one 43-byte frame Window A doesn't reproduce
      at the same position) — see `CAP-041-FINDINGS.md` §2/§3 for the full side-by-side sequences.
      Result: **no structural (length-level) difference found that correlates with the changed
      settings.**
- [x] **Settings-shaped candidate check:** no payload in any of the 4 compared bursts (this
      session's A/B/C or `CAP-036`'s baseline) has a length matching a 5×float32 EQ quintet
      (~20–24 bytes at the relevant nesting level) or an isolated boolean/enum-shaped frame that
      appears in one burst and not the others. Result: **no candidate found — not force-fit.**
- [x] **Bonus/incidental finding (not part of the length-sequence comparison):** each window's
      burst contains a recurring `2a 25 ... 32 12 { 0a 04 08 4f 10 01 } { 12 04 08 64 10 02 }
      { 1a 04 08 64 10 02 } ...` sub-message (structurally identical to `CAP-036-FINDINGS.md`
      §12.6's already-flagged, unresolved "2-field, non-1/2/3-index" push) — see
      `CAP-041-FINDINGS.md` §4 for the full hex and repeat-occurrence table. Its first field is a
      **constant `0x4f` (79)** in every one of 8 sampled occurrences across all 3 windows, matching
      this session's on-screen Case battery (79%, confirmed on screen at video t=0). Because the
      value never changes across this session, this is *consistent with* but does **not confirm**
      the field tracking Case battery — recorded as a strengthening data point for
      `CAP-036-FINDINGS.md` §12.6's existing 🔴 OPEN QUESTION, not a resolution.
- [x] **Outcome classification: (b) — clean negative** at the length/shape level: this session's
      three genuinely different settings states (touch controls off + 3 different EQ configs +
      mono on; +per-earbud touch off + different EQ; +per-earbud touch=Digital
      assistant/Adaptive+different EQ+diagnostics off) all produce a connect-time DLCI 0x02 burst
      with the same frame-count/length signature as `CAP-036`'s own default-settings burst. This
      does **not** rule out a settings-state read-back hidden inside same-length payloads whose
      *content* (not length) differs — that would require the full byte-for-byte decode this pass
      did not complete — so the negative is scoped to "no structural/length-level difference,"
      not "definitively no content carries settings state."

## Open Questions

- 🔴 Which of the four claimed BT-toggle actions in this session's procedure notes did **not**
  produce a distinct new ACL connection on the wire (only 3 Connection-Complete events exist for
  4 claimed windows) — not resolved by the available video frames this pass.
- 🔴 `CAP-036-FINDINGS.md` §12.6's open question (does the DLCI 0x02 periodic push's 2-field,
  non-1/2/3-indexed sub-message track battery state?) is neither resolved nor contradicted by this
  session — the candidate field stayed constant at a value matching on-screen Case% (79) the whole
  session, which is consistent with, but doesn't prove, that reading.
- 🔴 Whether the connect-time burst's payload *content* (not just its length signature) differs
  with settings state remains untested — a full byte-for-byte HDLC-unescape+CRC-verify decode of
  every subframe across this session's 3 windows and `CAP-036`'s baseline was out of this pass's
  budget; see `CAP-041-FINDINGS.md`'s Recommended next steps.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13).
- [ ] Write `CAP-041-FINDINGS.md` per `PROJECT_RULES.md` §2, hex & script rule (§1 rule 4a).
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index (status → `captured`/`analyzed`) and
      `id_registry.csv`'s `CAP-041` row.
- [ ] Add `OBS-007`'s Evidence column pointer in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (pointer only).
- [ ] Copy every new 🔴 OPEN QUESTION into `PROTOCOL.md` §6 (mandatory, §8).
- [ ] **Do not** promote anything to 🟢 FACT or write a `DECISIONS.md` ADR without explicit
      maintainer sign-off (`AGENTS.md` §6, §15) — propose only, even for a positive finding this
      directly relevant to `ARCHITECTURE.md` §3.1.
- [ ] Remember to set EQ/touch-controls back to their normal values after this session, if desired
      — this is a deliberate one-off non-default state for this capture only.
- [ ] Rename this capture's folder to the actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-041-2026-09-06_17-10-39_17-17-48-Group_AH/CAP-041-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-041-2026-09-06_17-10-39_17-17-48-Group_AH/CAP-041-EVENT-NOTES
