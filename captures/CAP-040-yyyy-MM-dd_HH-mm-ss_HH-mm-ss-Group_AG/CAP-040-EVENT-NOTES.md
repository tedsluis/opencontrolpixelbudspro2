# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AG, DLCI 0x08's unmapped Get-shaped codes vs. a known-changing value (`CAP-040`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `___` / `[ ]` below as the
session happens, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update).
**Do not pre-fill any log-derived value** before the capture exists. Once reviewed, rename this
folder from the placeholder `CAP-040-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AG` to the actual session
date/start-time/end-time.

**Status legend used throughout this file** (`PROJECT_RULES.md` §1, `PROTOCOL.md` §0): 🟢 **FACT**
· 🟡 **HYPOTHESIS** · ⚪ **ASSUMPTION** · 🔴 **OPEN QUESTION**. Never write a conclusion in this
file without one of these four labels.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AG, `PRIV-001`, new):** `CAP-036-FINDINGS.md` §5
found DLCI 0x08's connect-time burst contains several zero-length, `[Group][Code][00 00]`-shaped
`Sent` frames — `05 0c`, `04 02`, `04 04`, `04 11`, `04 13`, `04 15`, `0e 04` — structurally
identical in shape to DLCI 0x04's confirmed "Get" pattern (`PROTOCOL.md` §4.1), but none of these
Group/Code pairs is mapped to any known setting, and this project has never checked what (if
anything) the corresponding `Rcvd` responses contain. `AGENTS.md` §13.6's zero-creativity rule
means these can only be decoded via **correlation against a known, independently-verifiable
value** — not by guessing. This session brackets a value that is guaranteed to change in a way
that can be read directly off the phone screen (battery percentage, via a partial discharge, or
the Case's docked/undocked count) across several reconnects, so any `Rcvd` response that tracks
that known change can be attributed with actual evidence.

**Method:** Pixel 7a, stock Android, official Pixel Buds Companion App installed and open on
Device details (so on-screen battery/case values are visible for cross-check), Google Play
Services **enabled**. Buds already bonded.

**⚠️ Rule:** no settings screen is opened, no toggle is touched. The only deliberate action this
session is letting the battery level change naturally (or, more practically in a single sitting,
bracketing a case open/close/dock cycle, which is the fastest reliably-observable state change)
across multiple reconnects.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-040`                      |
|      Group(s)    | AG (`PRIV-001` — DLCI 0x08 unmapped Get-code correlation; incidental `PAIR-003`, `BATT`-family) |
|       Date       |                     `___`                           |
| Firmware version | ⚪ ASSUMPTION `release_5.203` |
|   Test device    | Pixel 7a, Android `___`. **Official Pixel Buds Companion App, Google Play Services enabled** |
| Video file       | `CAP-040-recording.mp4` — `___` |
| Log file         | `CAP-040-btsnoop_hci.log` — `___`s, `___` packets, `___`–`___` local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` |

**Capture-integrity pre-flight** — identical method to `CAP-036`–`CAP-039`. Record results here:
`___`

## Preparation checklist (before recording)

- [ ] Buds already bonded and connect normally. Do **not** "Forget" or re-pair.
- [ ] Official Pixel Buds Companion App installed and working; version if visible: `___`
- [ ] Google Play Services **enabled** — carry `CAP-036`'s verification forward as ⚪ ASSUMPTION.
- [ ] No third-party BLE/GATT tool used.
- [ ] Bluetooth HCI snoop logging enabled and the phone rebooted.
- [ ] Video recording with a visible wall-clock overlay ready, framed so the on-screen
      Left/Right/Case battery percentages are legible at every reconnect.
- [ ] **Record the starting on-screen battery values** (Left/Right/Case) before the session: `___`
- [ ] Decide which known-changing value to bracket (pick one, don't try both in one session):
      - **Option A (battery discharge)** — needs a genuinely non-trivial existing charge
        difference between Left/Right/Case, or enough session time for one to visibly tick down;
        opportunistic, cannot be forced on demand.
      - **Option B (case dock count/state)** — reconnect repeatedly while deliberately varying
        which of Left/Right/Case is docked at each reconnect (e.g. only Left docked, only Right
        docked, both docked, both out) — forceable on demand, recommended default if no
        convenient battery gap exists.
      Chosen option: `___`

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AG)

1. Start video recording (wall-clock overlay visible), confirm HCI snoop logging active. Record
   on-screen Left/Right/Case battery before anything else.
2. **If Option A (battery discharge):** let the session run long enough, or start soon enough
   after unplugging the case from charge, that at least one component's on-screen percentage
   changes by the time of a later reconnect. Reconnect (toggle Bluetooth or case-cycle) at the
   start and again after the change is visible on screen, noting the before/after values and
   times.
3. **If Option B (dock-state variation), default 4-repeat sequence** — mirrors `CAP-037`'s
   structure but varies *which* component is docked rather than just docked-vs-not:
   - Repeat 1: both Left and Right docked, case open. Reconnect, idle ~5s, note on-screen values.
   - Repeat 2: only Left docked (Right removed, held/on table). Reconnect, idle ~5s, note values.
   - Repeat 3: only Right docked (Left removed). Reconnect, idle ~5s, note values.
   - Repeat 4: both removed (case empty). Reconnect, idle ~5s, note values.
   Toggle Bluetooth off between repeats, buffer ~3s each time.
4. Stop video recording and HCI snoop logging.
5. Extract via the raw btsnoop path first, then run the capture-integrity pre-flight.

## Event Timeline

| Time (local) | Action / Event | On-screen L/R/Case | Test-ID | Evidence in `CAP-040-btsnoop_hci.log` |
|---|---|---|---|---|
| `___` | Start video recording | `___`/`___`/`___` | — | — |
| `___` | **Repeat/bracket 1 start** | `___`/`___`/`___` | `PRIV-001`, `PAIR-003` | frame `___` |
| `___` | Connection established | `___`/`___`/`___` | `PRIV-001` | frame `___` |
| `___` | **Repeat/bracket 1 end** | `___`/`___`/`___` | `PRIV-001` | frame `___` |
| `___` | **Repeat/bracket 2 start** | `___`/`___`/`___` | `PRIV-001`, `PAIR-003` | frame `___` |
| `___` | Connection established | `___`/`___`/`___` | `PRIV-001` | frame `___` |
| `___` | **Repeat/bracket 2 end** | `___`/`___`/`___` | `PRIV-001` | frame `___` |
| `___` | *(repeat rows as needed for 3/4)* | | | |
| `___` | End video recording | `___`/`___`/`___` | — | — |

**Contamination log:** `___`

## Decode / Analysis

```
tshark -r CAP-040-btsnoop_hci.log -Y "bthci_acl.chandle==<Buds chandle> and btrfcomm.dlci==8" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data
```

- [ ] **For each of the 6 unmapped codes (`05 0c`, `04 02`, `04 04`, `04 11`, `04 13`, `04 15`,
      `0e 04`), find the `Sent` Get frame and the immediately-following `Rcvd` frame(s) on the same
      Group** (matching Group number, not necessarily the same Code — the response Code may
      differ, as it does for DLCI 0x04's own Get/Notify pair using Codes `0x11`/`0x13`). Record the
      raw hex of each. Result: `___`
- [ ] **Per repeat/bracket, decode each response's numeric fields** (protobuf-shaped tag/varint
      parsing, per `PROTOCOL.md` §2.2a's method) and check whether any field's value **changes**
      in step with the known bracketed value (on-screen battery, or which component is docked).
      A field that stays constant across all repeats is not this value; a field that tracks the
      bracket exactly, repeat after repeat, is a real candidate — state which. Per
      `AGENTS.md` §13.6, do not guess a meaning for a field that doesn't visibly track anything.
      Result: `___`
- [ ] **Outcome classification:** (a) one or more codes' response content correlates cleanly with
      the bracketed value across all repeats → propose a semantic reading, label 🟡 HYPOTHESIS
      pending replication, with the full correlation table as evidence; (b) no response content
      correlates with anything bracketed → a clean negative for this specific bracketing method,
      record it (a different known-changing value may still reveal something these codes track);
      (c) inconclusive (bracket didn't actually produce a real change, contamination, truncated
      log) → 🔴 unconfirmed, re-run with a clearer bracket. Classification: `___`

## Open Questions

- 🔴 `___`

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13).
- [ ] Write `CAP-040-FINDINGS.md` per `PROJECT_RULES.md` §2, hex & script rule (§1 rule 4a).
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index (status → `captured`/`analyzed`) and
      `id_registry.csv`'s `CAP-040` row.
- [ ] Add `PRIV-001`'s Evidence column pointer in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (pointer only).
- [ ] Copy every new 🔴 OPEN QUESTION into `PROTOCOL.md` §6 (mandatory, §8).
- [ ] **Do not** promote anything to 🟢 FACT or write a `DECISIONS.md` ADR without explicit
      maintainer sign-off (`AGENTS.md` §6, §15) — propose only.
- [ ] Rename this capture's folder to the actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-040-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AG/CAP-040-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-040-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AG/CAP-040-EVENT-NOTES
