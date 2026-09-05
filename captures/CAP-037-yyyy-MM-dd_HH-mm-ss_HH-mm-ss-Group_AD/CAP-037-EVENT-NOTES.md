# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AD, "Get ANC state" reconnect-reliability + dock-state transition, purpose-built repeat (`CAP-037`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `___` / `[ ]` below as the
session happens, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). **Do not pre-fill any log-derived value**
(frame numbers, exact timestamps, packet counts) before the capture exists. Once reviewed, rename
this folder from the placeholder `CAP-037-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AD` to the actual
session date/start-time/end-time.

**Status legend used throughout this file** (`PROJECT_RULES.md` §1, `PROTOCOL.md` §0): 🟢 **FACT**
(directly observed, evidence referenced) · 🟡 **HYPOTHESIS** (unverified, with a stated test) ·
⚪ **ASSUMPTION** (treated as true without verification, with a stated reason) · 🔴 **OPEN
QUESTION** (identified gap, no hypothesis yet). Never write a conclusion in this file without one
of these four labels.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AD, `OBS-004`):** `CAP-036` found DLCI 0x04's
"Get ANC state" (`0x11`) query firing once, on a single reconnect. `DECISIONS.md` ADR-022 has
since promoted the *trigger-reliability* claim to 🟢 FACT — but that promotion rested on
**retrospective** analysis of 10 captures that were never designed for this question (settings-
toggle tests, a pairing repeat, an ANC repeat) — this project has never yet run a **purpose-built**
session whose entire point is this query. This session is that purpose-built repeat, and combines
it with a second, still-open question: `DECISIONS.md` ADR-024 promoted the "Notify ANC state"
`Settable-toggles` byte as a **dock-state indicator** (`0x00` = both earbuds seated in the case,
`0xe8` otherwise) from 7 video-confirmed samples **across different sessions** — but no single
session has yet captured the **within-session transition** itself (buds go from docked to
undocked, or back, while being reconnected each time). This session is designed to close both
gaps in one go: repeated, isolated reconnects, deliberately alternating dock state between
repeats.

**Method:** Pixel 7a, stock Android, official Pixel Buds Companion App installed and **open on the
Device details screen** throughout (so the query, if it fires, has a UI to be attributed to —
matching `CAP-036`'s own baseline), Google Play Services **enabled** (the normal baseline, same as
Group AC). Buds already bonded — this is a **reconnect** repeat, not a fresh pair (do not
"Forget").

**⚠️ The rule this whole capture depends on: touch nothing except the dock state and the
Bluetooth toggle/case lid.** No ANC tap, no EQ change, no settings screen navigation beyond what's
needed to keep Device details on screen. Every DLCI 0x02/0x04/0x08 frame in this log must be
connection-initiated, never a settings-write — if a setting is touched by accident, say so
explicitly in the Event Timeline and treat that window as contaminated.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-037`                      |
|      Group(s)    | AD (`OBS-004` — reconnect-reliability + dock-state-transition repeat; incidental `PAIR-003`) |
|       Date       |                     `___`                           |
| Firmware version | ⚪ ASSUMPTION `release_5.203` (carry over unless checked on-screen this session) |
|   Test device    | Pixel 7a, Android `___`. **Official Pixel Buds Companion App, Google Play Services enabled** |
| Video file       | `CAP-037-recording.mp4` — `___` |
| Log file         | `CAP-037-btsnoop_hci.log` — `___`s, `___` packets, `___`–`___` local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` |

**Capture-integrity pre-flight (do this immediately after extraction, before any analysis — per
`CAP-014-FINDINGS.md` §0's method, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §6's snaplen gotcha, and
`CAP-036-EVENT-NOTES.md`'s note that `bluetooth.addr` did not resolve for RFCOMM frames in that
session — check `bthci_evt.bd_addr`/connection-handle instead if the same happens here):**

```
$ capinfos CAP-037-btsnoop_hci.log
# check: "Packet size limit" should read "(not set)" / "inferred: 262144" or similar — NOT a small value

$ tshark -r CAP-037-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
# expect: mismatches: 0
```

If this fails (truncated), stop before spending analysis time — re-extract via
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3 **step 3's raw path first**, per the same reasoning as
`CAP-036-EVENT-NOTES.md`'s pre-flight note (a truncated log can silently clip the exact short
frames this session is looking for). Record which extraction path was actually used.

## Preparation checklist (before recording)

- [ ] Buds are **already bonded** to this Pixel 7a and connect normally. Do **not** "Forget" or
      re-pair.
- [ ] Official Pixel Buds Companion App installed and working; record its version if visible on
      screen: `___`
- [ ] Google Play Services **enabled** (the normal baseline) — this session does not need a fresh
      `dumpsys` re-check; carry `CAP-036`'s verification forward as ⚪ ASSUMPTION unless something
      looks different on screen.
- [ ] No third-party BLE/GATT tool (nRF Connect or similar) used at any point.
- [ ] Bluetooth HCI snoop logging enabled and the phone rebooted
      (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §2 step 5).
- [ ] Video recording with a visible wall-clock overlay ready.
- [ ] **Note the starting dock state** before recording: are both buds seated in the case, or
      already out? Record here: `___`
- [ ] Decide and write down the exact repeat sequence before starting (so it isn't improvised
      mid-recording) — see Procedure below for the recommended default sequence.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AD)

Leave a clean few-second buffer between repeats so one repeat's settling traffic doesn't bleed
into the next. Log explicit **window start** and **window end** boundaries for every repeat.

**Recommended default sequence — 5 repeats, alternating dock state, so both the reliability
question and the dock-state-transition question get isolated same-session evidence:**

1. Start video recording (wall-clock overlay visible), confirm HCI snoop logging active. Buds
   start **docked** (both seated in the case, case open, case visible on camera).
2. **Repeat 1 — reconnect from docked.** Toggle Bluetooth ON (or open the case lid if that alone
   triggers reconnect — note which). Note the exact time the connection completes on screen, then
   idle ~5s. Note window end. Do **not** remove the buds from the case this repeat.
3. Toggle Bluetooth OFF (clean disconnect), buffer ~3s.
4. **Repeat 2 — reconnect with buds undocked.** Remove both buds from the case (hold them in hand
   or set them on the table, still on camera, not worn) *before* toggling Bluetooth back ON. Note
   connection-complete time, idle ~5s, note window end.
5. Toggle Bluetooth OFF, buffer ~3s.
6. **Repeat 3 — reconnect from docked again.** Place both buds back in the case, close the lid
   briefly then reopen (or just reseat them, case open), toggle Bluetooth ON. Note times as above.
7. Toggle Bluetooth OFF, buffer ~3s.
8. **Repeat 4 — reconnect with buds undocked**, mirroring repeat 2.
9. Toggle Bluetooth OFF, buffer ~3s.
10. **Repeat 5 — reconnect from docked**, mirroring repeats 1/3, as a final reliability check.
11. Stop video recording and HCI snoop logging. Keep the session short enough to avoid on-device
    log rotation (§2's note).
12. Extract via the raw btsnoop path first (§3 step 3), then run the capture-integrity pre-flight
    above before any analysis.

## Event Timeline

*(Fill in after reviewing the video frame-by-frame against its wall-clock overlay, cross-checked
against `CAP-037-btsnoop_hci.log` via `tshark` — per `AGENTS.md` §13. One row per repeat boundary.
Leave `___` where a value isn't known yet rather than estimating it.)*

| Time (local) | Action / Event | Dock state | Test-ID | Evidence in `CAP-037-btsnoop_hci.log` |
|---|---|---|---|---|
| `___` | Start video recording | docked | — | — |
| `___` | **Repeat 1 start** — Bluetooth toggled ON | docked | `OBS-004`, `PAIR-003` | frame `___` |
| `___` | Connection established (on-screen) | docked | `OBS-004` | frame `___` |
| `___` | **Repeat 1 end** — Bluetooth toggled OFF | docked | `OBS-004` | frame `___` |
| `___` | **Repeat 2 start** — buds removed from case, Bluetooth toggled ON | undocked | `OBS-004`, `PAIR-003` | frame `___` |
| `___` | Connection established (on-screen) | undocked | `OBS-004` | frame `___` |
| `___` | **Repeat 2 end** — Bluetooth toggled OFF | undocked | `OBS-004` | frame `___` |
| `___` | **Repeat 3 start** — buds re-docked, Bluetooth toggled ON | docked | `OBS-004`, `PAIR-003` | frame `___` |
| `___` | Connection established (on-screen) | docked | `OBS-004` | frame `___` |
| `___` | **Repeat 3 end** — Bluetooth toggled OFF | docked | `OBS-004` | frame `___` |
| `___` | **Repeat 4 start** — buds removed again, Bluetooth toggled ON | undocked | `OBS-004`, `PAIR-003` | frame `___` |
| `___` | Connection established (on-screen) | undocked | `OBS-004` | frame `___` |
| `___` | **Repeat 4 end** — Bluetooth toggled OFF | undocked | `OBS-004` | frame `___` |
| `___` | **Repeat 5 start** — buds re-docked, Bluetooth toggled ON | docked | `OBS-004`, `PAIR-003` | frame `___` |
| `___` | Connection established (on-screen) | docked | `OBS-004` | frame `___` |
| `___` | **Repeat 5 end** — Bluetooth toggled OFF | docked | `OBS-004` | frame `___` |
| `___` | End video recording | — | — | — |

**Contamination log:** `___` (state explicitly whether any repeat accidentally touched a setting).

## Decode / Analysis

*(Full decode with the exact command **and** the raw hex bytes per finding belongs in
`CAP-037-FINDINGS.md`, per `PROJECT_RULES.md` §1 rule 4a.)*

```
tshark -r CAP-037-btsnoop_hci.log -Y "bthci_acl.chandle==<Buds chandle> and btrfcomm.dlci==4 and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data
```
(Use `bthci_evt.bd_addr` to find the Buds' chandle first if `bluetooth.addr` doesn't resolve, per
`CAP-036-EVENT-NOTES.md`'s note.)

- [ ] **Does `08 11 00 00` (Get) + `08 13` (Notify) appear on every one of the 5 repeats?**
      Per-repeat: frame numbers, timing from channel-open to Get, timing from Get to Notify. Any
      miss is itself a finding (breaks `DECISIONS.md` ADR-022's "zero misses" record) — report it
      plainly, don't explain it away. Result: `___`
- [ ] **Does the `Settable-toggles` byte match dock state on every repeat**, per `DECISIONS.md`
      ADR-024 (`0x00` for docked repeats, `0xe8` for undocked)? This is the first **within-session**
      test of that correlation — report each repeat's byte value against its known dock state.
      Result: `___`
- [ ] **Timing consistency:** does the ~34ms channel-open-to-Get and ~10ms Get-to-Notify timing
      from `CAP-036` hold across all 5 repeats, or does it vary? Result: `___`
- [ ] **Three-way outcome (per this Group's own stated-in-advance outcomes):** (a) 5/5 fire, dock
      state matches on every repeat → strong same-session reproduction of both ADR-022 and
      ADR-024, propose as additional evidence (not a new promotion — both are already FACT);
      (b) fires every time but dock-state correlation breaks on one or more repeats → a genuine
      counter-example to ADR-024, flag prominently, do not quietly reconcile it;
      (c) misses on one or more repeats → a genuine counter-example to ADR-022, flag prominently.
      Classification: `___`

## Open Questions

*(Add every new 🔴 OPEN QUESTION found here — and copy each into `PROTOCOL.md` §6's matching
subsection per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §8's mandatory rule.)*

- 🔴 `___`

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13) —
      confirm `OBS-004`/`PAIR-003` are clearly referenced above.
- [ ] Write `CAP-037-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status from
      `planned` to `captured`/`analyzed`, and fill in the Android/firmware/app-version columns and
      the log path. Update `id_registry.csv`'s `CAP-037` row to match.
- [ ] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `OBS-004` row with a **pointer only**, never a
      restated finding.
- [ ] Copy every new 🔴 OPEN QUESTION into `PROTOCOL.md` §6 (mandatory, §8).
- [ ] **Do not** promote anything in `PROTOCOL.md` to 🟢 FACT, and **do not** write or amend a
      `DECISIONS.md` ADR, without explicit maintainer sign-off — propose it, clearly labelled as a
      proposal (`AGENTS.md` §6, §15). Even a clean 5/5 reproduction of ADR-022/ADR-024 is
      *additional evidence for an already-FACT claim*, not itself grounds for the agent to write a
      new ADR unprompted.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-037-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AD/CAP-037-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-037-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AD/CAP-037-EVENT-NOTES
