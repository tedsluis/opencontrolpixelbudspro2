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
|       Date       |                     `___`                           |
| Firmware version | ⚪ ASSUMPTION `release_5.203` |
|   Test device    | Pixel 7a, Android `___`. **Official Pixel Buds Companion App, Google Play Services enabled** |
| Video file       | `CAP-041-recording.mp4` — `___` |
| Log file         | `CAP-041-btsnoop_hci.log` — `___`s, `___` packets, `___`–`___` local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` |

**Capture-integrity pre-flight** — identical method to `CAP-036`–`CAP-040`. Record results here:
`___`

## Preparation checklist (before recording)

- [ ] Buds already bonded and connect normally. Do **not** "Forget" or re-pair.
- [ ] Official Pixel Buds Companion App installed and working; version if visible: `___`
- [ ] Google Play Services **enabled** — carry `CAP-036`'s verification forward as ⚪ ASSUMPTION.
- [ ] No third-party BLE/GATT tool used.
- [ ] **Before recording starts**, set the following to clearly non-default values (and leave them
      set for the whole session):
      - EQ: move at least the Bass and Treble sliders well off-center (e.g. +4/-4), **do not**
        pick a value near `CAP-036`'s own recorded default (all bands centered at 0) by accident.
        Record the exact values set: `___`
      - Touch controls: turn **off** (`CAP-036`'s session had this **on**). Record: `___`
      - Optional, if time permits: also change Multipoint or In-ear detection off their `CAP-036`
        defaults (both were **on** in `CAP-036`) for a broader contrast. Record: `___`
- [ ] Confirm on-screen that all changed settings show the new, non-default values before
      proceeding.
- [ ] Bluetooth HCI snoop logging enabled and the phone rebooted **after** the settings changes
      above are already in place (so the reconnect burst reflects the new state from the start).
- [ ] Video recording with a visible wall-clock overlay ready.

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
| `___` | Start video recording (non-default EQ/touch-controls already set) | — | — | — |
| `___` | **Window 1 start** — Bluetooth toggled OFF | User (Hardware) | `OBS-007`, `PAIR-003` | frame `___` |
| `___` | Bluetooth toggled ON | User (Hardware) | `OBS-007` | frame `___` |
| `___` | Connection established (on-screen) | App/OS (Auto) | `OBS-007` | frame `___` |
| `___` - `___` | Idle, nothing touched | — | `OBS-007` | frames `___`–`___` |
| `___` | **Window 1 end** | — | `OBS-007` | frame `___` |
| `___` | End video recording | — | — | — |

**Contamination log:** `___`

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
