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
|       Date       |                     2026-09-06                      |
| Firmware version | ⚪ ASSUMPTION `release_5.203` |
|   Test device    | Pixel 7a, Android 14. **Official Pixel Buds Companion App, Google Play Services enabled** |
| Video file       | `CAP-040-recording.mp4` — 24:10s, `07:28:00`–`07:52:10` local time |
| Log file         | `CAP-040-btsnoop_hci.log` — `___`s, `___` packets, `___`–`___` local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` |

**Capture-integrity pre-flight** — identical method to `CAP-036`–`CAP-039`. Record results here:
`___`

## Preparation checklist (before recording)

- [x] Buds already bonded and connect normally. Do **not** "Forget" or re-pair.
- [x] Official Pixel Buds Companion App installed and working.
- [x] Google Play Services **enabled** — carry `CAP-036`'s verification forward as ⚪ ASSUMPTION.
- [x] No third-party BLE/GATT tool used.
- [x] Bluetooth HCI snoop logging enabled and the phone rebooted.
- [x] Video recording with a visible wall-clock overlay ready, framed so the on-screen
      Left/Right/Case battery percentages are legible at every reconnect.
- [x] **Record the starting on-screen battery values** (Left/Right/Case) before the session: Left: 95%, Case: 85%, Right: 100%. *(Note: These values remain exactly the same throughout the entire 24-minute video).*
- [x] Decide which known-changing value to bracket:
      Chosen option: **Option B (case dock count/state)**. *Note: User execution deviated from the protocol's 4-state sequence, opting for a 2-state (Both Docked vs Both Undocked) high-repetition loop instead.*

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
*Note: Because battery percentages remained static (95/85/100) for the entire duration, the only bracketed variable is the physical Dock State. The user repeated a specific cycle approximately 15 times. The cycle consists of: Auto-Connect via Case Open (Docked) -> App Connect (Undocked) -> App Connect (Docked) -> Case Close.*

| Time (local) | Action / Event | Dock State | Test-ID | Evidence in `CAP-040-btsnoop_hci.log` |
|---|---|---|---|---|
| `07:28:00` | Start video recording | Unknown | — | — |
| `07:28:16` | User navigates to Device Details | — | — | — |
| `07:28:28` | Initial connection established | Undocked | `PRIV-001` | frame `___` |
| `07:28:36` | Buds placed in case | Docked | — | — |
| `07:28:40` | Tapped 'Disconnect' in App | Docked | — | frame `___` |
| `07:28:44` | Tapped 'Connect' in App | Docked | `PRIV-001` | frame `___` |
| `07:29:10` | Case closed (auto disconnect) | Docked | — | frame `___` |
| `07:29:32` | Case opened | Docked | `PRIV-001` | frame `___` |
| `07:29:46` | Tapped 'Disconnect' in App | Docked | — | frame `___` |
| `07:29:50` | Tapped 'Connect' in App | Docked | `PRIV-001` | frame `___` |
| `07:30:26` | Buds removed from case | Undocked | — | — |
| `07:30:33` | Tapped 'Disconnect' in App | Undocked | — | frame `___` |
| `07:30:35` | Tapped 'Connect' in App | Undocked | `PRIV-001` | frame `___` |
| `07:31:13` | Buds placed in case | Docked | — | — |
| `07:31:21` | Case closed (auto disconnect) | Docked | — | frame `___` |
| `07:31:34` | Case opened (auto connect) | Docked | `PRIV-001` | frame `___` |
| `07:31:49` | Buds removed from case | Undocked | — | — |
| `07:31:54` | Tapped 'Disconnect' in App | Undocked | — | frame `___` |
| `07:31:56` | Tapped 'Connect' in App | Undocked | `PRIV-001` | frame `___` |
| `07:32:27` | Buds placed in case | Docked | — | — |
| `07:32:32` | Tapped 'Disconnect' in App | Docked | — | frame `___` |
| `07:32:34` | Tapped 'Connect' in App | Docked | `PRIV-001` | frame `___` |
| `07:33:04` | Case closed (auto disconnect) | Docked | — | frame `___` |
| `07:33:17` | Case opened (auto connect) | Docked | `PRIV-001` | frame `___` |
| `07:33:37` | Buds removed from case | Undocked | — | — |
| `07:33:43` | Tapped 'Disconnect' in App | Undocked | — | frame `___` |
| `07:33:45` | Tapped 'Connect' in App | Undocked | `PRIV-001` | frame `___` |
| `07:34:15` | Buds placed in case | Docked | — | — |
| `07:34:24` | Tapped 'Disconnect' in App | Docked | — | frame `___` |
| `07:34:26` | Tapped 'Connect' in App | Docked | `PRIV-001` | frame `___` |
| `07:34:52` | Case closed (auto disconnect) | Docked | — | frame `___` |
| `07:35:04` | Case opened (auto connect) | Docked | `PRIV-001` | frame `___` |
| `07:35:25` | Buds removed from case | Undocked | — | — |
| `07:35:32` | Tapped 'Disconnect' in App | Undocked | — | frame `___` |
| `07:35:34` | Tapped 'Connect' in App | Undocked | `PRIV-001` | frame `___` |
| `07:36:03` | Buds placed in case | Docked | — | — |
| `07:36:10` | Tapped 'Disconnect' in App | Docked | — | frame `___` |
| `07:36:12` | Tapped 'Connect' in App | Docked | `PRIV-001` | frame `___` |
| `07:36:38` | Case closed (auto disconnect) | Docked | — | frame `___` |
| `07:36:51` | Case opened (auto connect) | Docked | `PRIV-001` | frame `___` |
| `07:37:11` | Buds removed from case | Undocked | — | — |
| `07:37:18` | Tapped 'Disconnect' in App | Undocked | — | frame `___` |
| `07:37:20` | Tapped 'Connect' in App | Undocked | `PRIV-001` | frame `___` |
| `07:37:48` | Buds placed in case | Docked | — | — |
| `07:37:54` | Tapped 'Disconnect' in App | Docked | — | frame `___` |
| `07:37:55` | Tapped 'Connect' in App | Docked | `PRIV-001` | frame `___` |
| `07:38:22` | Case closed (auto disconnect) | Docked | — | frame `___` |
| `07:38:34` | Case opened (auto connect) | Docked | `PRIV-001` | frame `___` |
| `07:38:52` | Buds removed from case | Undocked | — | — |
| `07:38:58` | Tapped 'Disconnect' in App | Undocked | — | frame `___` |
| `07:39:00` | Tapped 'Connect' in App | Undocked | `PRIV-001` | frame `___` |
| `07:39:28` | Buds placed in case | Docked | — | — |
| `07:39:35` | Tapped 'Disconnect' in App | Docked | — | frame `___` |
| `07:39:36` | Tapped 'Connect' in App | Docked | `PRIV-001` | frame `___` |
| `07:40:02` | Case closed (auto disconnect) | Docked | — | frame `___` |
| `07:40:14` | Case opened (auto connect) | Docked | `PRIV-001` | frame `___` |
| `07:40:35` | Buds removed from case | Undocked | — | — |
| `07:40:42` | Tapped 'Disconnect' in App | Undocked | — | frame `___` |
| `07:40:43` | Tapped 'Connect' in App | Undocked | `PRIV-001` | frame `___` |
| `07:41:12` | Buds placed in case | Docked | — | — |
| `07:41:19` | Tapped 'Disconnect' in App | Docked | — | frame `___` |
| `07:41:20` | Tapped 'Connect' in App | Docked | `PRIV-001` | frame `___` |
| `07:41:46` | Case closed (auto disconnect) | Docked | — | frame `___` |
| `07:41:58` | Case opened (auto connect) | Docked | `PRIV-001` | frame `___` |
| `07:42:20` | Buds removed from case | Undocked | — | — |
| `07:42:26` | Tapped 'Disconnect' in App | Undocked | — | frame `___` |
| `07:42:28` | Tapped 'Connect' in App | Undocked | `PRIV-001` | frame `___` |
| `07:42:56` | Buds placed in case | Docked | — | — |
| `07:43:03` | Tapped 'Disconnect' in App | Docked | — | frame `___` |
| `07:43:05` | Tapped 'Connect' in App | Docked | `PRIV-001` | frame `___` |
| `07:43:30` | Case closed (auto disconnect) | Docked | — | frame `___` |
| `07:43:43` | Case opened (auto connect) | Docked | `PRIV-001` | frame `___` |
| `07:44:04` | Buds removed from case | Undocked | — | — |
| `07:44:11` | Tapped 'Disconnect' in App | Undocked | — | frame `___` |
| `07:44:13` | Tapped 'Connect' in App | Undocked | `PRIV-001` | frame `___` |
| `07:44:41` | Buds placed in case | Docked | — | — |
| `07:44:48` | Tapped 'Disconnect' in App | Docked | — | frame `___` |
| `07:44:50` | Tapped 'Connect' in App | Docked | `PRIV-001` | frame `___` |
| `07:45:15` | Case closed (auto disconnect) | Docked | — | frame `___` |
| `07:45:27` | Case opened (auto connect) | Docked | `PRIV-001` | frame `___` |
| `07:45:48` | Buds removed from case | Undocked | — | — |
| `07:45:55` | Tapped 'Disconnect' in App | Undocked | — | frame `___` |
| `07:45:57` | Tapped 'Connect' in App | Undocked | `PRIV-001` | frame `___` |
| `07:46:25` | Buds placed in case | Docked | — | — |
| `07:46:32` | Tapped 'Disconnect' in App | Docked | — | frame `___` |
| `07:46:34` | Tapped 'Connect' in App | Docked | `PRIV-001` | frame `___` |
| `07:46:59` | Case closed (auto disconnect) | Docked | — | frame `___` |
| `07:47:11` | Case opened (auto connect) | Docked | `PRIV-001` | frame `___` |
| `07:47:32` | Buds removed from case | Undocked | — | — |
| `07:47:38` | Tapped 'Disconnect' in App | Undocked | — | frame `___` |
| `07:47:40` | Tapped 'Connect' in App | Undocked | `PRIV-001` | frame `___` |
| `07:48:09` | Buds placed in case | Docked | — | — |
| `07:48:15` | Tapped 'Disconnect' in App | Docked | — | frame `___` |
| `07:48:17` | Tapped 'Connect' in App | Docked | `PRIV-001` | frame `___` |
| `07:48:42` | Case closed (auto disconnect) | Docked | — | frame `___` |
| `07:48:54` | Case opened (auto connect) | Docked | `PRIV-001` | frame `___` |
| `07:49:15` | Buds removed from case | Undocked | — | — |
| `07:49:22` | Tapped 'Disconnect' in App | Undocked | — | frame `___` |
| `07:49:23` | Tapped 'Connect' in App | Undocked | `PRIV-001` | frame `___` |
| `07:49:52` | Buds placed in case | Docked | — | — |
| `07:49:59` | Tapped 'Disconnect' in App | Docked | — | frame `___` |
| `07:50:01` | Tapped 'Connect' in App | Docked | `PRIV-001` | frame `___` |
| `07:50:26` | Case closed (auto disconnect) | Docked | — | frame `___` |
| `07:50:38` | Case opened (auto connect) | Docked | `PRIV-001` | frame `___` |
| `07:50:58` | Buds removed from case | Undocked | — | — |
| `07:51:04` | Tapped 'Disconnect' in App | Undocked | — | frame `___` |
| `07:51:06` | Tapped 'Connect' in App | Undocked | `PRIV-001` | frame `___` |
| `07:51:34` | Buds placed in case | Docked | — | — |
| `07:51:41` | Tapped 'Disconnect' in App | Docked | — | frame `___` |
| `07:51:43` | Tapped 'Connect' in App | Docked | `PRIV-001` | frame `___` |
| `07:52:09` | Case closed (auto disconnect) | Docked | — | frame `___` |
| `07:52:10` | End video recording | — | — | — |

**Contamination log:** The execution deviated from the suggested "Option B" protocol in two ways. First, the user did not test intermediate states (e.g., "only Left docked" or "only Right docked"); the test alternated strictly between "both docked" and "both out". Second, the user utilized the Pixel Buds app's "Connect/Disconnect" UI buttons for many of the reconnects, rather than the OS-level Bluetooth toggle. While this provides a huge volume of "Both Docked" vs. "Both Undocked" samples, the reliance on the App UI for reconnection is a slight methodological deviation from earlier baselines like `CAP-036`.

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
