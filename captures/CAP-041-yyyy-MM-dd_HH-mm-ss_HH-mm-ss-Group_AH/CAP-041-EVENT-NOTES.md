# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AH, DLCI 0x02's connect-time RPC burst vs. non-default settings state (`CAP-041`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `___` / `[ ]` below as the
session happens, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update).
**Do not pre-fill any log-derived value** before the capture exists. Once reviewed, rename this
folder from the placeholder `CAP-041-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AH` to the actual session
date/start-time/end-time.

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

**Capture-integrity pre-flight** — identical method to `CAP-036`–`CAP-040`. Record results here:
`___`

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

**Contamination log:** The ⚠️ Rule was completely violated. The user did not set the non-default values *before* the recording. Instead, the user actively navigated the app and changed settings *during* the recording. Furthermore, instead of one single idle reconnect window, the user performed four separate Bluetooth toggles (reconnects), changing the Equalizer, Touch Controls, Mono Audio, and Usage & Diagnostics settings between each reconnect. 
**Positive aspect:** While this violates the single-variable test protocol, it provides an excellent multi-bracket dataset: we now have four distinct connect-time bursts, each with a known, different settings state. App navigation during the burst window might generate concurrent DLCI 0x02 traffic, which will need to be carefully separated from the automatic read-back burst during decoding.

## Decode / Analysis

```
tshark -r CAP-041-btsnoop_hci.log -Y "bthci_acl.chandle==<Buds chandle> and btrfcomm.dlci==2 and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data
```
Split each RFCOMM payload on the `0x7e` HDLC flag byte before decoding (multiple sub-frames pack
into one RFCOMM I-frame — `DESKRESEARCH_FINDINGS.md`'s 2026-08-28 entry, `CAP-036-FINDINGS.md` §4).

- [ ] **Extract this session's own connect-time DLCI 0x02 burst** (same method as
      `CAP-036-FINDINGS.md` §4: frames from channel-open through the burst's natural end, a few
      seconds later). Record frame range and total byte count. Result: `___`
- [ ] **Byte-for-byte diff against `CAP-036`'s own burst** (`CAP-036-btsnoop_hci.log`, DLCI 0x02,
      same extraction method) — same frame count? Same payload lengths in the same order? Any
      payload that differs, byte for byte? List every difference found, with both sessions' raw
      hex side by side — per `AGENTS.md` §13.6, do not speculate about what a differing byte
      *means* beyond noting that it differs and at what offset. Result: `___`
- [ ] **If differences are found:** do any of them look plausibly related to the specific settings
      changed (EQ band values, touch-controls state) — e.g. a payload of the right rough size/shape
      to carry a 5-band float quintet (`PROTOCOL.md` §4.2's known EQ shape) or a boolean flag? Only
      report this as a candidate if the byte pattern actually resembles the known shape — do not
      force a settings-content reading onto an arbitrary differing byte. Result: `___`
- [ ] **Outcome classification:** (a) burst content differs in a way that plausibly reflects the
      changed settings → strong new evidence for a `libmaestro`-side state read-back, directly
      relevant to `ARCHITECTURE.md` §3.1, propose as 🟡 HYPOTHESIS with the full byte comparison;
      (b) burst is byte-for-byte identical (modulo the known session-specific correlation-ID/nonce
      bytes already documented) despite genuinely different settings → a clean negative, this
      burst does not carry settings state; (c) inconclusive (settings weren't actually different
      by the time of reconnect, contamination, truncated log) → 🔴 unconfirmed, re-run.
      Classification: `___`

## Open Questions

- 🔴 `___`

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
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-041-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AH/CAP-041-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-041-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AH/CAP-041-EVENT-NOTES
