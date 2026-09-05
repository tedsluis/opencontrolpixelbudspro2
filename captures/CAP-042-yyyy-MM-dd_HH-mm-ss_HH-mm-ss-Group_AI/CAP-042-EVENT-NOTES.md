# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AI, long pure-idle bracket for the periodic DLCI 0x02/0x04/0x08/HFP push cadence (`CAP-042`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `___` / `[ ]` below as the
session happens, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update).
**Do not pre-fill any log-derived value** before the capture exists. Once reviewed, rename this
folder from the placeholder `CAP-042-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AI` to the actual session
date/start-time/end-time.

**Status legend used throughout this file** (`PROJECT_RULES.md` §1, `PROTOCOL.md` §0): 🟢 **FACT**
· 🟡 **HYPOTHESIS** · ⚪ **ASSUMPTION** · 🔴 **OPEN QUESTION**. Never write a conclusion in this
file without one of these four labels.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AI, `OBS-002`):** `CAP-036-FINDINGS.md` §12.5
found a periodic push recurring on DLCI 0x02, 0x04, 0x08, **and** HFP's `AT+BIEV`, all firing
within 7–18ms of each other, starting ~3.5 minutes after connection and continuing at an irregular
cadence (mostly ~10s, with occasional 50–70s gaps) for as long as the (short, ~7-minute) session
ran. `CAP-009-FINDINGS.md` §2 already characterizes HFP's own push cadence over a much longer
(101-minute) idle session as "settling burst, then irregular — median ~20s, up to ~14.6 minutes"
(`PROTOCOL.md` §4.3 Option C) — but that session never specifically checked whether DLCI 0x02/0x04
join HFP's push in lockstep the way `CAP-036` found. This session is a long (15+ minute), fully
idle bracket, specifically to characterize this cross-channel synchronized cadence over a longer
window than `CAP-036`'s own ~7 minutes could show — is it truly periodic, does the "near-lockstep"
timing hold up over dozens of occurrences, and does the gap distribution match `CAP-009`'s existing
HFP-only characterization once DLCI 0x02/0x04/0x08 are checked too. This is exactly the kind of
"ambient/long-duration behavior" `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `OBS-002` row has been waiting
for a dedicated capture scenario for.

**Method:** Pixel 7a, stock Android, official Pixel Buds Companion App installed (connected,
backgrounded — do not keep the app open on screen; this tests background/idle behavior, not an
open-app baseline), Google Play Services **enabled**, buds worn (a stable, unchanging dock state
for the whole session, out of the case) or docked (case open) — **pick one and hold it constant**,
since `DECISIONS.md` ADR-024 shows dock state affects at least one of these frames' content
(the DLCI 0x08 battery triple, and DLCI 0x04's `Settable-toggles` byte, should either of those
opcodes recur mid-session) and a changing dock state would confound the cadence measurement with a
dock-state-transition event.

**⚠️ Rule: touch absolutely nothing for the entire idle window.** No app screen open, no settings
change, no ANC tap, ideally screen off. The value of this capture is entirely in it being long and
genuinely idle — a single accidental tap invalidates the whole point.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-042`                      |
|      Group(s)    | AI (`OBS-002` — long idle bracket for background/battery-polling behavior) |
|       Date       |                     `___`                           |
| Firmware version | ⚪ ASSUMPTION `release_5.203` |
|   Test device    | Pixel 7a, Android `___`. **Official Pixel Buds Companion App (backgrounded), Google Play Services enabled** |
| Video file       | `CAP-042-recording.mp4` — `___` (a wall-clock overlay on an otherwise-idle scene is enough; the video doesn't need to show the phone screen continuously for this capture — a periodic check-in shot per minute confirming nothing was touched is sufficient) |
| Log file         | `CAP-042-btsnoop_hci.log` — `___`s, `___` packets, `___`–`___` local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` |

**Capture-integrity pre-flight** — identical method to `CAP-036`–`CAP-041`. Record results here:
`___`. **Extra caution for this specific session:** `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §2's
on-device log-rotation note is more relevant here than for any other capture in this batch, given
the deliberately long duration — confirm the snoop log's own buffer size is large enough for
15+ minutes before starting, or check the log's own coverage against the intended window
immediately after extraction.

## Preparation checklist (before recording)

- [ ] Buds already bonded and connect normally. Do **not** "Forget" or re-pair.
- [ ] Official Pixel Buds Companion App installed; version if visible: `___`
- [ ] Google Play Services **enabled** — carry `CAP-036`'s verification forward as ⚪ ASSUMPTION.
- [ ] No third-party BLE/GATT tool used.
- [ ] Bluetooth HCI snoop logging enabled and the phone rebooted.
- [ ] **Decide and record the fixed dock state for this session** (worn/out-of-case vs. docked in
      an open case) — must not change for the entire session: `___`
- [ ] Video recording with a visible wall-clock overlay ready (see the note above on periodic
      check-in shots rather than continuous phone-screen framing).
- [ ] Plan for at least 15 minutes of genuinely idle time after the initial connect settles —
      block out the time before starting so the session isn't cut short.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AI)

1. Start video recording (wall-clock overlay visible), confirm HCI snoop logging active. Connect
   the Buds normally (reconnect or already connected) and confirm the fixed dock state on screen
   once, then put the phone screen to sleep / background the app.
2. **Idle window** [`OBS-002`]. Do not touch the phone, the buds, or the case for **at least 15
   minutes**. If recording video continuously isn't practical for the full window, take a
   wall-clock-overlay check-in shot roughly once a minute confirming nothing has moved — the
   value of this capture is in the *log*, not a continuous video.
3. Stop video recording and HCI snoop logging.
4. Extract via the raw btsnoop path first, then run the capture-integrity pre-flight — check
   specifically that the log's own duration matches the intended idle window and wasn't rotated
   short.

## Event Timeline

*(For this capture, list every occurrence of the periodic push across all of DLCI 0x02/0x04/0x08
and HFP `AT+BIEV`, not individual user actions — there are none. One row per occurrence, or a
condensed table once the full list is extracted — see Decode/Analysis below for the extraction
command.)*

| Time (local) | Channel(s) firing | Δ from previous occurrence | Test-ID | Evidence in `CAP-042-btsnoop_hci.log` |
|---|---|---|---|---|
| `___` | Connection established | — | `OBS-002` | frame `___` |
| `___` | First periodic-push occurrence | `___` | `OBS-002` | frame `___` |
| `___` | *(repeat for every occurrence found)* | | | |

**Contamination log:** `___`

## Decode / Analysis

```
# DLCI 0x02/0x04/0x08 candidate pushes (adapt DLCI number per channel):
tshark -r CAP-042-btsnoop_hci.log -Y "bthci_acl.chandle==<Buds chandle> and btrfcomm.dlci in {2,4,8} and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e data.data

# HFP AT+BIEV:
tshark -r CAP-042-btsnoop_hci.log -Y "btrfcomm.len>0" -T fields -e frame.number -e frame.time -e _ws.col.Info \
  | grep -i "BIEV=2"
```

- [ ] **Full occurrence list, all channels, whole session** — extract every DLCI 0x02/0x04/0x08
      push matching `CAP-036-FINDINGS.md` §4/§5's known content shapes, plus every `AT+BIEV=2`
      push, with exact timestamps. Result: `___`
- [ ] **Cross-channel timing:** for each occurrence, do DLCI 0x02/0x04/0x08 and HFP still fire
      within a similarly tight window (`CAP-036`'s samples were 7–18ms apart) across this much
      longer session, or does the synchronization drift/break down over time? Result: `___`
- [ ] **Gap distribution:** compute the delta between consecutive occurrences across the whole
      session. Does the pattern match `CAP-009-FINDINGS.md` §2's HFP-only characterization
      (settling burst, then irregular, median ~20s, up to ~14.6 minutes) once DLCI 0x02/0x04/0x08
      are included, or does adding those channels change the picture (e.g. a different cadence, or
      the sync breaking down at longer gaps)? Result: `___`
- [ ] **Cross-check against `CAP-027-FINDINGS.md`'s cross-sync-caveat** (`DESKRESEARCH_FINDINGS.md`
      2026-09-04 round 2, `PROTOCOL.md` §4.3): that session found DLCI 0x08 firing 3 times with no
      HFP counterpart during active A2DP streaming. This session is idle (no streaming) — does the
      sync hold up perfectly here, supporting "streaming specifically breaks it," or does it also
      occasionally desync even at idle? Result: `___`
- [ ] **Outcome classification:** (a) sync holds and cadence matches `CAP-009`'s existing model
      closely → strengthens (not new) the existing characterization, no promotion needed;
      (b) sync holds but the cadence/gap distribution differs meaningfully from `CAP-009`'s model
      → a genuine refinement candidate for `PROTOCOL.md` §4.3 Option C, propose as 🟡 HYPOTHESIS;
      (c) sync breaks down even at idle → narrows `CAP-027`'s "active-streaming-specific" reading,
      propose revisiting that caveat; (d) inconclusive (dock state changed mid-session, log
      rotated short, contamination) → 🔴 unconfirmed, re-run. Classification: `___`

## Open Questions

- 🔴 `___`

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13).
- [ ] Write `CAP-042-FINDINGS.md` per `PROJECT_RULES.md` §2, hex & script rule (§1 rule 4a).
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index (status → `captured`/`analyzed`) and
      `id_registry.csv`'s `CAP-042` row.
- [ ] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `OBS-002` row — this is its first dedicated
      capture scenario; update the "Capture scenario(s)" column accordingly (pointer only, no
      restated finding).
- [ ] Copy every new 🔴 OPEN QUESTION into `PROTOCOL.md` §6 (mandatory, §8).
- [ ] **Do not** promote anything to 🟢 FACT or write a `DECISIONS.md` ADR without explicit
      maintainer sign-off (`AGENTS.md` §6, §15) — propose only.
- [ ] Rename this capture's folder to the actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-042-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AI/CAP-042-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-042-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AI/CAP-042-EVENT-NOTES
