# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AG, DLCI 0x08's unmapped Get-shaped codes vs. a known-changing value (`CAP-040`)

**Status:** ✅ **Analyzed.** See `CAP-040-FINDINGS.md` for the full write-up. Central `PRIV-001`
result: the 7 flagged unmapped DLCI 0x08 codes fire exactly **once**, in the connect-time burst at
session start — the documented "~15 repeats" (App Connect/Disconnect taps) turned out to produce
**zero** wire-visible RFCOMM/HCI traffic at all, so no per-repeat correlation was possible for
those specific codes (see the Contamination-log correction below). A genuinely valuable, unplanned
bonus finding did emerge from the previously-unreviewed video tail (past the originally-documented
`07:52:10` end) — see `CAP-040-FINDINGS.md` §4.

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
| Video file       | `CAP-040-recrding.mp4` (filename has a typo in the original recording, "recrding" not "recording" — left as-is) — actual ffprobe duration **1745.23s (~29m5s)**, `07:28:00`–`07:57:05` local time. The originally-noted "24:10s, 07:28:00–07:52:10" window covers only the documented ~15-repeat procedure; the video actually runs ~5 more minutes past that (see §Contamination log and Decode/Analysis — this tail turned out to contain the session's only *genuine* wire-level disconnect). |
| Log file         | Two files exist for this session — see the critical `.log.last` finding below. **Authoritative source: `CAP-040-btsnoop_hci.log` (main log)** — 8,539 packets, 0/8,539 `frame.cap_len`≠`frame.len` mismatches (untruncated), 1831.99s, 2026-09-06 07:28:07.139–07:58:39.126 local. |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` |

**Capture-integrity pre-flight — done, with a major correction to this task's own Guardrail 5 assumption:**

```
$ capinfos CAP-040-btsnoop_hci.log        # main log
Number of packets:   8,539       Capture duration: 1831.987236 s
Earliest packet time: 2026-09-06 07:28:07.138550   Latest: 2026-09-06 07:58:39.125786
$ tshark -r CAP-040-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3{c++} END{print "mismatches:", c+0}'
mismatches: 0

$ capinfos CAP-040-btsnoop_hci.log.last   # the "rotated" file
Number of packets:   11,177      Capture duration: 1236.643995 s
Earliest packet time: 2026-09-06 07:07:28.040028   Latest: 2026-09-06 07:28:04.684023
```

**`CAP-040-btsnoop_hci.log.last` is NOT the earlier half of CAP-040's own session.** Its earliest
packet timestamp (`07:07:28.040028`) is *byte-for-byte identical* to `CAP-039-btsnoop_hci.log`'s
own earliest packet timestamp, and its Buds (`04:00:6e:cf:6e:07`) Connection-Complete events occur
at the exact same chandles/timestamps as CAP-039's own log (`0x0002@07:07:34.091`,
`0x0005@07:07:35.111`, `0x0006@07:08:25.794`, `0x0007@07:09:01.366`, `0x0008@07:09:38.914`,
`0x0009@07:10:24.477`, `0x000a@07:11:04.170`). This is leftover on-device rolling-buffer content
that also happens to contain **CAP-039's own session** plus the ~14-minute idle gap between
CAP-039 and CAP-040 — not part of Group AG's own procedure. CAP-040's own main log starts cleanly
and independently at `07:28:07.139` (frame 1), with the Buds' own fresh Connection Complete for
*this* session at frame 646, `07:28:12.077` — matching the video's own start. **This capture's
findings below use only the main, unmerged log** (`CAP-040-btsnoop_hci-merged.log` was created per
this task's Guardrail 5 but is not used as evidence — merging it would misattribute CAP-039's own
traffic to CAP-040).

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
| `07:28:28` | Initial connection established | Undocked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:28:36` | Buds placed in case | Docked | — | — |
| `07:28:40` | Tapped 'Disconnect' in App | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:28:44` | Tapped 'Connect' in App | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:29:10` | Case closed (auto disconnect) | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:29:32` | Case opened | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:29:46` | Tapped 'Disconnect' in App | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:29:50` | Tapped 'Connect' in App | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:30:26` | Buds removed from case | Undocked | — | — |
| `07:30:33` | Tapped 'Disconnect' in App | Undocked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:30:35` | Tapped 'Connect' in App | Undocked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:31:13` | Buds placed in case | Docked | — | — |
| `07:31:21` | Case closed (auto disconnect) | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:31:34` | Case opened (auto connect) | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:31:49` | Buds removed from case | Undocked | — | — |
| `07:31:54` | Tapped 'Disconnect' in App | Undocked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:31:56` | Tapped 'Connect' in App | Undocked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:32:27` | Buds placed in case | Docked | — | — |
| `07:32:32` | Tapped 'Disconnect' in App | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:32:34` | Tapped 'Connect' in App | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:33:04` | Case closed (auto disconnect) | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:33:17` | Case opened (auto connect) | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:33:37` | Buds removed from case | Undocked | — | — |
| `07:33:43` | Tapped 'Disconnect' in App | Undocked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:33:45` | Tapped 'Connect' in App | Undocked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:34:15` | Buds placed in case | Docked | — | — |
| `07:34:24` | Tapped 'Disconnect' in App | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:34:26` | Tapped 'Connect' in App | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:34:52` | Case closed (auto disconnect) | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:35:04` | Case opened (auto connect) | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:35:25` | Buds removed from case | Undocked | — | — |
| `07:35:32` | Tapped 'Disconnect' in App | Undocked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:35:34` | Tapped 'Connect' in App | Undocked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:36:03` | Buds placed in case | Docked | — | — |
| `07:36:10` | Tapped 'Disconnect' in App | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:36:12` | Tapped 'Connect' in App | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:36:38` | Case closed (auto disconnect) | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:36:51` | Case opened (auto connect) | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:37:11` | Buds removed from case | Undocked | — | — |
| `07:37:18` | Tapped 'Disconnect' in App | Undocked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:37:20` | Tapped 'Connect' in App | Undocked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:37:48` | Buds placed in case | Docked | — | — |
| `07:37:54` | Tapped 'Disconnect' in App | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:37:55` | Tapped 'Connect' in App | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:38:22` | Case closed (auto disconnect) | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:38:34` | Case opened (auto connect) | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:38:52` | Buds removed from case | Undocked | — | — |
| `07:38:58` | Tapped 'Disconnect' in App | Undocked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:39:00` | Tapped 'Connect' in App | Undocked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:39:28` | Buds placed in case | Docked | — | — |
| `07:39:35` | Tapped 'Disconnect' in App | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:39:36` | Tapped 'Connect' in App | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:40:02` | Case closed (auto disconnect) | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:40:14` | Case opened (auto connect) | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:40:35` | Buds removed from case | Undocked | — | — |
| `07:40:42` | Tapped 'Disconnect' in App | Undocked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:40:43` | Tapped 'Connect' in App | Undocked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:41:12` | Buds placed in case | Docked | — | — |
| `07:41:19` | Tapped 'Disconnect' in App | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:41:20` | Tapped 'Connect' in App | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:41:46` | Case closed (auto disconnect) | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:41:58` | Case opened (auto connect) | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:42:20` | Buds removed from case | Undocked | — | — |
| `07:42:26` | Tapped 'Disconnect' in App | Undocked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:42:28` | Tapped 'Connect' in App | Undocked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:42:56` | Buds placed in case | Docked | — | — |
| `07:43:03` | Tapped 'Disconnect' in App | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:43:05` | Tapped 'Connect' in App | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:43:30` | Case closed (auto disconnect) | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:43:43` | Case opened (auto connect) | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:44:04` | Buds removed from case | Undocked | — | — |
| `07:44:11` | Tapped 'Disconnect' in App | Undocked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:44:13` | Tapped 'Connect' in App | Undocked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:44:41` | Buds placed in case | Docked | — | — |
| `07:44:48` | Tapped 'Disconnect' in App | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:44:50` | Tapped 'Connect' in App | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:45:15` | Case closed (auto disconnect) | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:45:27` | Case opened (auto connect) | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:45:48` | Buds removed from case | Undocked | — | — |
| `07:45:55` | Tapped 'Disconnect' in App | Undocked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:45:57` | Tapped 'Connect' in App | Undocked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:46:25` | Buds placed in case | Docked | — | — |
| `07:46:32` | Tapped 'Disconnect' in App | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:46:34` | Tapped 'Connect' in App | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:46:59` | Case closed (auto disconnect) | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:47:11` | Case opened (auto connect) | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:47:32` | Buds removed from case | Undocked | — | — |
| `07:47:38` | Tapped 'Disconnect' in App | Undocked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:47:40` | Tapped 'Connect' in App | Undocked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:48:09` | Buds placed in case | Docked | — | — |
| `07:48:15` | Tapped 'Disconnect' in App | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:48:17` | Tapped 'Connect' in App | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:48:42` | Case closed (auto disconnect) | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:48:54` | Case opened (auto connect) | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:49:15` | Buds removed from case | Undocked | — | — |
| `07:49:22` | Tapped 'Disconnect' in App | Undocked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:49:23` | Tapped 'Connect' in App | Undocked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:49:52` | Buds placed in case | Docked | — | — |
| `07:49:59` | Tapped 'Disconnect' in App | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:50:01` | Tapped 'Connect' in App | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:50:26` | Case closed (auto disconnect) | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:50:38` | Case opened (auto connect) | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:50:58` | Buds removed from case | Undocked | — | — |
| `07:51:04` | Tapped 'Disconnect' in App | Undocked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:51:06` | Tapped 'Connect' in App | Undocked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:51:34` | Buds placed in case | Docked | — | — |
| `07:51:41` | Tapped 'Disconnect' in App | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:51:43` | Tapped 'Connect' in App | Docked | `PRIV-001` | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:52:09` | Case closed (auto disconnect) | Docked | — | no discrete frame (channel stayed open, see Contamination log correction) |
| `07:52:10` | End video recording | — | — | — |

**Contamination log:** The execution deviated from the suggested "Option B" protocol in two ways. First, the user did not test intermediate states (e.g., "only Left docked" or "only Right docked"); the test alternated strictly between "both docked" and "both out". Second, the user utilized the Pixel Buds app's "Connect/Disconnect" UI buttons for many of the reconnects, rather than the OS-level Bluetooth toggle. While this provides a huge volume of "Both Docked" vs. "Both Undocked" samples, the reliance on the App UI for reconnection is a slight methodological deviation from earlier baselines like `CAP-036`.

**Wire-evidence correction to the above (found during Phase 1/3 analysis, see `CAP-040-FINDINGS.md` §1 for full evidence):** the deviation turned out to matter more than "slight." The app's own "Connect"/"Disconnect" button taps produce **zero** HCI/RFCOMM-visible traffic of any kind — checked directly (e.g. the `07:29:46`/`07:29:50` Disconnect/Connect pair: no frame at all on `chandle 0x0002` in that window). The classic ACL connection (`chandle 0x0002`) and DLCI 0x08 both stay open continuously from `07:28:12` onward; only DLCI 0x02/0x04 show occasional independent channel bounces, and only **one** genuine HCI `Disconnection Complete` occurs in the entire session — at `07:55:41.465` (reason `0x13`), inside a burst of real DLCI 0x04 `SABM`/`DISC` cycling from `07:54:33`–`07:55:41` that falls **after** this table's documented end (`07:52:10`) and inside the video's own previously-unreviewed tail (video runs to ~`07:57:05`, not `07:52:10` — see Log Metadata). Video frames at `07:52:28`/`07:56:20`/`07:57:00` confirm: still connected and undocked-then-redocked through most of the documented table, then genuinely disconnected (app shows "Connect", not "Disconnect") from `07:56:20` onward, matching the real `07:55:41` teardown. **Practical consequence: almost none of this table's ~30 individual "Tapped Disconnect/Connect"/"Case closed/opened" rows correspond to a distinguishable wire event** — the per-row frame-number blanks below are intentionally left as "no discrete frame (channel stayed open)" rather than filled with a misleading frame number; the one section of this session that *does* carry real, wire-confirmed reconnect activity is the `07:54:28`–`07:55:41` cluster near the end, addressed in full in `CAP-040-FINDINGS.md`.

## Decode / Analysis

```
tshark -r CAP-040-btsnoop_hci.log -Y "bthci_acl.chandle==<Buds chandle> and btrfcomm.dlci==8" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data
```

- [ ] **For each of the 6 unmapped codes (`05 0c`, `04 02`, `04 04`, `04 11`, `04 13`, `04 15`,
      `0e 04`), find the `Sent` Get frame and the immediately-following `Rcvd` frame(s) on the same
      Group** (matching Group number, not necessarily the same Code — the response Code may
      differ, as it does for DLCI 0x04's own Get/Notify pair using Codes `0x11`/`0x13`). Record the
      raw hex of each. **Result: found exactly one occurrence of all 7 codes together, bundled in
      a single Sent frame (#980, 07:28:12.857, right after DLCI 0x08's one-and-only `SABM`/`UA` for
      this session at frames 958/962) — `05 0c 00 00 / 04 02 00 00 / 04 04 00 00 / 04 11 00 00 /
      04 13 00 00 / 04 15 00 00 / 0e 04 00 00`, followed in the same frame by the already-documented
      `Group 0x03 Code 0x02` firmware string and capability blob. This is the only time DLCI 0x08
      opens/closes in the entire 30-minute session (confirmed via `rfcomm.frametype`/`_ws.col.Info`
      filtered to `Channel=4`: one `SABM`→`UA` at session start, no `DISC` until the log ends) — see
      `CAP-040-FINDINGS.md` §1/§3 for the full command+hex.**
- [ ] **Per repeat/bracket, decode each response's numeric fields** (protobuf-shaped tag/varint
      parsing, per `PROTOCOL.md` §2.2a's method) and check whether any field's value **changes**
      in step with the known bracketed value (on-screen battery, or which component is docked).
      A field that stays constant across all repeats is not this value; a field that tracks the
      bracket exactly, repeat after repeat, is a real candidate — state which. Per
      `AGENTS.md` §13.6, do not guess a meaning for a field that doesn't visibly track anything.
      Result: **not applicable as designed — with only 1 occurrence, there is nothing to compare
      across "repeats" for these 7 specific codes. As a bonus/adjacent check, the already-mapped
      `Group 0x0e Code 0x01` battery-triple push (not one of the 7 flagged codes, but same DLCI)
      recurs 22 times across the session and its Left-earbud (`index=1`) field shows a real,
      video-confirmed dock-linked event: value drifts 96→95 (normal discharge) for most of the
      session, then briefly reads the `0xff`(255) "unknown" sentinel at `07:54:28`, then settles to
      `100` from `07:54:29` onward — exactly coinciding with the video-confirmed real docking event
      in the previously-unreviewed tail. See `CAP-040-FINDINGS.md` §4 for the full table/hex — this
      is a new 🟡 HYPOTHESIS, not a FACT promotion.**
- [ ] **Outcome classification: (c) inconclusive for the originally-designed bracketing method** —
      the procedure's own deviation (App Connect/Disconnect buttons instead of a real disconnect)
      meant the "~15 repeats" never actually re-triggered DLCI 0x08's channel at all, so the 7
      flagged codes could not be correlated against the dock-state bracket as intended. This is a
      clean, well-evidenced negative for *this specific capture's execution*, not a negative for
      the underlying question — a re-run using the OS Bluetooth toggle or physical case actions
      (which the `CAP-037` procedure shows genuinely do reopen every channel) is recommended before
      concluding these codes are unrelated to dock state. Classification: **(c), with a strong
      unplanned bonus lead per the Result above.**

## Open Questions

- 🔴 The 7 flagged DLCI 0x08 unmapped `[Group][Code][00 00]` Sent codes (`05 0c`, `04 02`, `04 04`,
      `04 11`, `04 13`, `04 15`, `0e 04`) remain entirely unattributed — this session could not test
      them against dock state at all, since DLCI 0x08 never reopens after its one connect-time
      burst. Needs a re-run with a real reconnect trigger (OS toggle or physical case cycling).
- 🔴 Does the app's own in-app "Connect"/"Disconnect" button ever produce *any* wire-visible signal
      under any condition, or is it purely a local/optimistic UI state with no protocol effect at
      all? This session found zero signal across ~15 taps, but that could be specific to this app
      version/session rather than a general rule.
- 🔴 What does `Group 0x0e Code 0x01`'s Left-earbud field reporting `0xff` (255, the documented
      "unknown" sentinel) immediately before settling to `100` at the moment of physical docking
      actually represent — a genuine sensor read gap during case contact, or an artifact of the
      charging-detection logic? Single-session, single-occurrence observation.

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
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-040-2026-09-06_07-28-00_07-57-05-Group_AG/CAP-040-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-040-2026-09-06_07-28-00_07-57-05-Group_AG/CAP-040-EVENT-NOTES
