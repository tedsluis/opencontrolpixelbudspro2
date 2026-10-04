# CAP-025: Find My Buds / Ring Action (Group K)

Standardized, evidence-based extraction from `CAP-025-btsnoop_hci.log` + `CAP-025-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. **Correction to this session's original task framing:** a
video recording does exist for this capture (verified directly, `ffprobe`, 273.7s) — the analysis
below is video-confirmed, not log-derived-only as this batch's task description assumed. Status
legend per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-025` |
| Purpose | Group K — the last remaining fully unattributed app command; test `PROTOCOL.md` §4.4's Ring hypothesis (Message Stream Action group `0x04`, code `0x01`) |
| Date | 2026-08-21 |
| Firmware | not queried — ⚪ ASSUMPTION `release_5.203` |
| Test device | Pixel 7a, Android 17 (⚪ ASSUMPTION), official Pixel Buds Companion App (version not visible on screen) |
| Log file | [`CAP-025-btsnoop_hci.log`](./CAP-025-btsnoop_hci.log) — 384.4s, 2026-08-21 08:40:49.737–08:47:14.185 (+0200) |
| Notes file | [`CAP-025-EVENT-NOTES.md`](./CAP-025-EVENT-NOTES.md) |
| Video file | [`CAP-025-recording.mp4`](./CAP-025-recording.mp4) — 273.7s, 08:40:52–08:45:26 local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` (confirmed via `bthci_evt.bd_addr` on the wire; DLCI-based filtering was used for the analysis below regardless) |

## 2. Methodology & a key structural finding

Video reviewed via tiled contact sheets (full pass) and targeted single-frame extraction; DLCI
0x04 searched for `PROTOCOL.md` §4.4's exact predicted shape (`Group 0x04`, `Code 0x01`):

```
$ tshark -r CAP-025-btsnoop_hci.log -Y "btrfcomm.dlci==0x04 and btrfcomm.len>0" \
    -T fields -e frame.number -e frame.time -e data.data
```

**The app splits "Find My Buds" across two different screens/mechanisms**, discovered from the
video, not assumed from the Group's Test-ID list:

1. **Device details → Find device**: two buttons only, **"Ring Left"** / **"Ring Right"** — no
   Case, no combined option. Maps to `FIND-001`/`FIND-002`.
2. **Find device → "Most recent location" → Find Hub map view**: three per-target icons (combined
   earbuds / case / individual) with its own "Play sound" flow, showing "Connecting…" and the copy
   *"If you have another device linked with your Google Account, it may try to play sound on Pixel
   Buds Pro 2"* — network/account-mediated, not the same local command. Maps to `FIND-003`(Case)/
   `FIND-004`(both).

## 3. Analysis: `FIND-002` (Ring Right)

Video: toggle screen at t=38s (08:41:30) shows a tap on "Ring Right"; status text changes to
"Right earbud volume increasing…", button becomes "Mute Right". A second tap ("Mute Right") at
t=49s (08:41:41) returns the screen to idle.

```
$ tshark -r CAP-025-btsnoop_hci.log -Y 'btrfcomm.len>0 && btrfcomm.dlci==4 && frame.number>=2040 && frame.number<=2204 &&
    (data.data[0:2]==04:01 || data.data[0:2]==ff:01)' -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data      (p2p_dir 0 = phone → Buds)

2040  08:41:30.234  phone → Buds   04 01 00 01 01        Ring Right
2044  08:41:30.380  Buds → phone   ff 01 00 03 04 01 00  the Buds' ACK (echoed group/code + one state byte)
2045  08:41:30.420  Buds → phone   04 01 00 01 01        the Buds' own message, same format
2048  08:41:30.427  phone → Buds   ff 01 00 02 04 01     the phone's ACK of 2045

2120  08:41:41.243  phone → Buds   04 01 00 01 00        Stop
2123  08:41:41.283  Buds → phone   ff 01 00 03 04 01 00
2124  08:41:41.284  Buds → phone   04 01 00 01 00
2127  08:41:41.294  phone → Buds   ff 01 00 02 04 01
```

Each command is therefore followed by **two messages from the Buds** — their ACK and then a message of the command's own format carrying the new state — and by
**one ACK from the phone**. 🟢 FACT for the directions; 🟡 HYPOTHESIS that the Buds' message is the specification's "Syncing ringing status back to Seekers"
(`PROTOCOL.md` §4.4, 2026-10-03 Correction). (Rewritten in place 2026-10-03, `ai-sessions/0069`, A68-CAP-07/A68-PROT-04: this section used to read 2045/2124
as the phone's retransmissions and both `ff 01` frames as two ACK variants from the Buds.)

Decoded per `PROTOCOL.md` §2.1's Message Stream envelope (`[Group:1][Code:1][Len:2BE][Value]`):
`Group=0x04` (Action), `Code=0x01` (Ring) — exact match to the spec-quoted worked example's
Group/Code. `Value=0x01` = start-ring-Right; `Value=0x00` = stop.

**Status:** 🟢 **FACT, promoted 2026-08-23** (`DECISIONS.md` ADR-011) — video-confirmed tap, exact
timing match, two independent samples (start and stop) both matching the predicted envelope;
maintainer sign-off obtained per `AGENTS.md` §6.

## 4. Analysis: `FIND-001` (Ring Left)

Video: tap "Ring Left" at t=54s (08:41:46); status → "Left earbud volume increasing…", button →
"Mute Left". Tap "Mute Left" at t=67s (08:41:58) returns to idle. A further Stop (2202,
08:42:02.900) follows 4 s later with no corresponding video-visible tap; the Buds answer it with their ACK only (2204) — no status message, consistent with
nothing having changed (the bud was not ringing).

```
2131  08:41:46.703  phone → Buds   04 01 00 01 02        Ring Left
2133  08:41:46.732  Buds → phone   ff 01 00 03 04 01 00
2134  08:41:46.736  Buds → phone   04 01 00 01 02
2137  08:41:46.743  phone → Buds   ff 01 00 02 04 01
2180  08:41:58.882  phone → Buds   04 01 00 01 00        Stop
2182  08:41:58.886  Buds → phone   ff 01 00 03 04 01 00
2183  08:41:58.919  Buds → phone   04 01 00 01 00
2186  08:41:58.932  phone → Buds   ff 01 00 02 04 01
2202  08:42:02.900  phone → Buds   04 01 00 01 00        Stop (not ringing)
2204  08:42:02.905  Buds → phone   ff 01 00 03 04 01 00
```

Same envelope as `FIND-002`. `Value=0x02` = start-ring-**Left** (distinct from Right's `0x01`);
`Value=0x00` = stop (same code as Right's stop — a single shared "stop" value, not per-earbud).

**Status:** 🟢 **FACT, promoted 2026-08-23** (`DECISIONS.md` ADR-011), same basis as §3.

## 5. Value byte — resolved table (this capture's central contribution)

| Value | Meaning | Evidence |
|---|---|---|
| `0x00` | Stop / mute ringing (shared, not per-earbud) | 3 occurrences, all following a video-confirmed "Mute Left/Right" tap or immediately after |
| `0x01` | Start ringing **Right** | 1 start (2040; the Buds' own message 2045 carries the same value), video-confirmed |
| `0x02` | Start ringing **Left** | 1 start (2131; the Buds' own message 2134), video-confirmed |

This resolves what would otherwise have been an open question (this batch's other captures had
to leave several field-to-meaning mappings as unconfirmed HYPOTHESES without video — this one has
a direct, unambiguous video match for every value observed).

## 6. Cross-command structural validation (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1 Group K discipline)

| Element | ANC (`Set ANC state`, 🟢 FACT, `PROTOCOL.md` §4.1) | Ring (this capture) |
|---|---|---|
| DLCI | 0x04 | 0x04 (same) |
| Envelope | `[Group:1][Code:1][Len:2BE][Value]` | Same shape |
| Group | `0x08` | `0x04` (different group, as the spec predicts) |
| ACK | `0xFF 0x01 0x00 0x06 <echo>` | the Buds' ACK is `0xFF 0x01 0x00 0x03 0x04 0x01 0x00` (echo + one state byte; the spec's example carries two); `0xFF 0x01 0x00 0x02 0x04 0x01` is the **phone's** ACK of the Buds' own message (§3) |

Ring's frames decode cleanly under the identical Group/Code/Length/Value rule already established
for ANC, with 4 video-confirmed action/response pairs (2 starts, 2 stops) — satisfying, and
exceeding, the guide's "2–3 semantically different commands" cross-check bar.

## 7. `FIND-003`/`FIND-004` — no local Ring command observed; different mechanism

The Find Hub "Play sound" flow is video-confirmed active continuously from ≈08:42:27 (t=95s)
through at least 08:45:02 (t=250s, "Pixel Buds Pro 2 - Left" detail sheet with a "Play sound"
option still visible). **Zero** `Group 0x04 Code 0x01` frames occur anywhere in this ~2.5-minute
window. Instead, three full classic-connection reopen bursts occur:

```
08:43:42.430–.706  full capability-handshake burst (same shape as every session-open burst
                    documented since CAP-004-FINDINGS.md §5a)
08:44:25.204–.327  same shape, ~43s later
08:45:04.556–.736  same shape, ~39s later
```

**Status:** 🟢 **FACT** that no local Ring command accompanies this UI flow (checked explicitly
across the full window, not assumed). 🟡 **HYPOTHESIS**: `FIND-003`/`FIND-004` route through
Google's Find My Device Network (account/cloud-mediated), consistent with the on-screen copy
referencing "another device linked with your Google Account" — distinct from, and not yet
resolved to the same level as, `FIND-001`/`FIND-002`'s confirmed local mechanism.

## 8. Conclusions & Status

- **`FIND-001`/`FIND-002` (Ring Left/Right) are confirmed**, video-correlated, including the
  previously-unresolvable start/stop value mapping. Promoted in `PROTOCOL.md` §4.4 to 🟢 FACT for
  the Left/Right Ring mechanism specifically, 2026-08-23 (`DECISIONS.md` ADR-011, maintainer
  sign-off obtained per `AGENTS.md` §6).
- **`FIND-003`/`FIND-004` are structurally different** — not a gap in this capture's coverage, but
  a genuine architectural finding: Case/combined ringing goes through Find Hub's network-mediated
  path, not the direct local Message Stream command. This should be recorded as its own open
  question, not conflated with the Left/Right mechanism.
- **Recommended next step:** if this project ever wants local, offline "ring the case" support (a
  Zero-GMS goal, `AGENTS.md` §1), the Find Hub/cloud path is out of scope by that same rule
  (`AGENTS.md` §1/`PROJECT_RULES.md` §21) — worth flagging to the maintainer as a possible hard
  limit on offline Case-ring support, not just an open research question.

## 9. Open Questions

- 🟡 What the Buds' ACK's state byte (`ff 01 00 03 04 01 00` — `00` in all five) and their own `04 01 00 01 xx` message mean beyond "the new ring state": a
  Ring stopped on the bud itself is the test (`CAP-068`). The "two ACK variants" question is answered (§3; `PROTOCOL.md` §6 ticked 2026-10-03).
- 🔴 Does `FIND-003`(Case)/`FIND-004`(both) genuinely require Find My Device Network/GMS, with no
  local-only fallback? Directly relevant to this project's Zero-GMS goal. → copied to
  `PROTOCOL.md` §6.
- 🔴 What triggers the repeated classic-connection reopen bursts during the Find Hub flow (three
  times, ~40s apart)? → copied to `PROTOCOL.md` §6.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-025-2026-08-21_08-40-52_08-45-26-Group_K/CAP-025-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-025-2026-08-21_08-40-52_08-45-26-Group_K/CAP-025-FINDINGS
