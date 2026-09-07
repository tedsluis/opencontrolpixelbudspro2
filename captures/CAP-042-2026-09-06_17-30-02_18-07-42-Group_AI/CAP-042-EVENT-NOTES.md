# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AI, long pure-idle bracket for the periodic DLCI 0x02/0x04/0x08/HFP push cadence (`CAP-042`)

**Status:** ✅ **Captured and decoded — see `CAP-042-FINDINGS.md`.** Central result: after the
initial connect-settling burst, the DLCI 0x02/0x04/0x08 push recurs only twice in the whole
~37m39s session (~16m and ~19m apart), far sparser than `CAP-036`'s short-session sample, and
HFP's `AT+BIEV` drops out of the cross-channel sync entirely. Folder renamed to
`CAP-042-2026-09-06_17-30-02_18-07-42-Group_AI`.

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
|       Date       |                     2026-09-06                      |
| Firmware version | ⚪ ASSUMPTION `release_5.203` |
|   Test device    | Pixel 7a, Android ⚪ ASSUMPTION (carried from `CAP-036`, not visible this session — screen off throughout). **Official Pixel Buds Companion App (backgrounded), Google Play Services enabled (⚪ ASSUMPTION, not re-verified)** |
| Video file       | `CAP-042-recording.mp4` — 2119.65s (~35m20s), 17:29:53–~18:05:13 local (wall-clock overlay); two spot-check frames (start ~17:29:53, mid ~18:04:48) confirm an undisturbed scene: buds resting loose beside the open, empty case, phone screen off |
| Log file         | `CAP-042-btsnoop_hci.log` — 2258.87s (~37m39s), 8,560 packets, 2026-09-06 17:30:02.871–18:07:41.746 local time (see §0 of `CAP-042-FINDINGS.md` — this is the MAIN log only; `.log.last` is leftover `CAP-041`-session content, not part of this session — do not merge) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` |

**Capture-integrity pre-flight** — identical method to `CAP-036`–`CAP-041`. Result: main log
untruncated (0/8,560 `frame.cap_len`≠`frame.len` mismatches, no snaplen cap). **Extra
caution for this specific session paid off:** the on-device log did rotate, producing a
`CAP-042-btsnoop_hci.log.last` file — but cross-referencing its Connection Complete events against
`CAP-041-btsnoop_hci.log` shows `.log.last` is actually leftover `CAP-041`-session content (identical
frame numbers/timestamps/chandles), not an earlier half of *this* session. `CAP-042`'s own session
is fully and cleanly contained in the main log alone (starts fresh at frame 1, 17:30:02.871). See
`CAP-042-FINDINGS.md` §0 for full command+evidence.

## Preparation checklist (before recording)

- [x] Buds already bonded and connect normally. Did not "Forget" or re-pair (confirmed on wire: no SSP/pairing traffic, only a Connection Complete).
- [x] Official Pixel Buds Companion App installed; version not visible on screen this session (backgrounded throughout).
- [x] Google Play Services **enabled** — carried `CAP-036`'s verification forward as ⚪ ASSUMPTION.
- [x] No third-party BLE/GATT tool used.
- [x] Bluetooth HCI snoop logging enabled and the phone rebooted.
- [x] **Fixed dock state: undocked/worn** (both earbuds resting loose beside the open, empty case — video-confirmed at start and mid-session; wire-confirmed via `Settable-toggles=0xe8` per `DECISIONS.md` ADR-024, see `CAP-042-FINDINGS.md` §1). Held constant for the entire session.
- [x] Video recording with a visible wall-clock overlay ready — a static framing with periodic implicit confirmation (long single take, no edits) rather than per-minute stills.
- [x] Session ran ~37m39s of idle time after the initial connect settled — well over the 15-minute minimum.

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

| Time (local) | Channel(s) firing | Δ from previous occurrence (same channel) | Test-ID | Evidence in `CAP-042-btsnoop_hci.log` |
|---|---|---|---|---|
| 17:30:04.78 | Connection established (chandle 0x0002) | — | `OBS-002` | frame 277 |
| 17:30:05.35–09.40 | Connect-settling burst — DLCI 0x02/0x04/0x08 full content, HFP SLC setup + 2× `AT+BIEV` | — | `OBS-002` | frames 571–969 |
| 17:46:16.95 (t≈974.08s) | Push #1 — DLCI 0x02/0x04/0x08 fire within ~30ms of each other; **HFP silent** | ~967.5s since burst end | `OBS-002` | frames 4518–4609 |
| 18:05:03.04 (t≈2101.04s) | Push #2 — DLCI 0x02/0x04/0x08 fire together again; **HFP silent** | 1123.7s since Push #1 | `OBS-002` | frames 7569–7671 |
| 18:07:41.75 | Log ends (last frame) | 154.6s since Push #2 | — | frame 8560 |

Only 2 post-settling occurrences found in the entire ~37m39s session (see `CAP-042-FINDINGS.md` §2
for the full per-frame table) — a far sparser cadence than `CAP-036`'s own ~7-minute session
showed, and HFP's `AT+BIEV` does not recur at all after the initial burst. Full analysis in
`CAP-042-FINDINGS.md` §2–§6.

**Contamination log:** None from this session's own procedure — the Buds' own chandle (0x0002)
shows no user-driven traffic anywhere in the log, consistent with "touch nothing" being followed.
An unrelated third-party device (`68:44:30:d6:fb:72`) connects/disconnects repeatedly on other
chandles throughout the session — background noise unrelated to the Buds, excluded from all
analysis (see `CAP-042-FINDINGS.md` §0).

## Decode / Analysis

```
# DLCI 0x02/0x04/0x08 candidate pushes (adapt DLCI number per channel):
tshark -r CAP-042-btsnoop_hci.log -Y "bthci_acl.chandle==<Buds chandle> and btrfcomm.dlci in {2,4,8} and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e data.data

# HFP AT+BIEV:
tshark -r CAP-042-btsnoop_hci.log -Y "btrfcomm.len>0" -T fields -e frame.number -e frame.time -e _ws.col.Info \
  | grep -i "BIEV=2"
```

- [x] **Full occurrence list, all channels, whole session** — done. Result: connect-settling burst
      (0–6.5s) then only 2 further clusters (Push #1 ~974s, Push #2 ~2101s), each with DLCI
      0x02/0x04/0x08 firing together but **no** `AT+BIEV` (only 2 `AT+BIEV` occurrences total,
      both inside the initial burst). Full per-frame table: `CAP-042-FINDINGS.md` §2.
- [x] **Cross-channel timing:** DLCI 0x02/0x04/0x08 still fire within ~30ms–a few hundred ms of
      each other at both Push #1 and Push #2 — the 3-channel sync holds at this timescale. HFP does
      **not** join either push (unlike `CAP-036`'s 4-channel sync). Result: `CAP-042-FINDINGS.md` §3.
- [x] **Gap distribution:** post-burst gaps measured are 967.5s (16m7.5s) and 1123.7s (18m43.7s) —
      both **longer** than `CAP-009`'s previously-documented ~14.6-minute maximum (n=2, 🟡
      HYPOTHESIS not a new ceiling). Result: `CAP-042-FINDINGS.md` §4.
- [x] **Cross-check against `CAP-027-FINDINGS.md`'s cross-sync-caveat:** the sync does **not** hold
      perfectly at idle either — HFP drops out here despite no streaming, narrowing `CAP-027`'s
      "streaming-specific" framing to "HFP is the piece of the sync that decouples most easily,
      under multiple different conditions." Result: `CAP-042-FINDINGS.md` §6.
- [x] **Outcome classification: (b) and (c) both apply** — the cadence/gap distribution differs
      meaningfully from `CAP-009`'s model (far sparser, longer gaps), proposed as 🟡 HYPOTHESIS
      refinement; and the sync does desync even at idle (HFP specifically), narrowing `CAP-027`'s
      streaming-specific reading. See `CAP-042-FINDINGS.md` §8 for the full confirmed-vs-proposed
      breakdown.

## Open Questions

- 🔴 Does the periodic DLCI 0x02/0x04/0x08 push cadence depend on the companion app being
      foregrounded vs. backgrounded? (copied to `PROTOCOL.md` §6)
- 🔴 What triggers the `Group 0x04 Code 0x12` alternating-value ping specifically, with no other
      channel churn nearby? (copied to `PROTOCOL.md` §6)
- 🔴 Why does HFP's `AT+BIEV` fail to recur even once in 37m39s while DLCI 0x02/0x04/0x08 fire
      twice? (copied to `PROTOCOL.md` §6)

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
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-042-2026-09-06_17-30-02_18-07-42-Group_AI/CAP-042-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-042-2026-09-06_17-30-02_18-07-42-Group_AI/CAP-042-EVENT-NOTES
