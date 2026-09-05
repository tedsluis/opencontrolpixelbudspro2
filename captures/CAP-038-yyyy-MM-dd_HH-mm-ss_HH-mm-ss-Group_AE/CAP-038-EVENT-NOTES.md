# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AE, realistic physical reconnect trigger vs. a system-Bluetooth-toggle reconnect (`CAP-038`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `___` / `[ ]` below as the
session happens, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). **Do not pre-fill any log-derived value**
before the capture exists. Once reviewed, rename this folder from the placeholder
`CAP-038-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AE` to the actual session date/start-time/end-time.

**Status legend used throughout this file** (`PROJECT_RULES.md` §1, `PROTOCOL.md` §0): 🟢 **FACT**
(directly observed, evidence referenced) · 🟡 **HYPOTHESIS** (unverified, with a stated test) ·
⚪ **ASSUMPTION** (treated as true without verification, with a stated reason) · 🔴 **OPEN
QUESTION** (identified gap, no hypothesis yet). Never write a conclusion in this file without one
of these four labels.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AE, `OBS-005`, new):** `CAP-036`'s reconnect was
triggered by toggling Bluetooth in system settings **while the Buds sat visibly in the open case
the entire session, never removed, never worn** (confirmed via that session's own video re-pass,
`CAP-036-FINDINGS.md` §2) — not the normal user flow of taking the Buds out of the case and putting
them in your ears. That normal flow could plausibly generate wire traffic a pure OS-level toggle
never exercises: an in-ear-detection event, A2DP audio-profile establishment, or a different
sequencing of the DLCI 0x02/0x04/0x08 connection burst than `CAP-036`/`CAP-037` show. This session
tests that directly, with two windows: a genuinely realistic reconnect (Buds removed from the case
and worn), and — time permitting — the case lid being opened **without** touching the Bluetooth
toggle at all, to see whether that alone behaves differently from an OS-level toggle.

**Method:** Pixel 7a, stock Android, official Pixel Buds Companion App installed and in normal use
(not necessarily pinned to one screen this time — let the app behave as it would for a real user),
Google Play Services **enabled** (the normal baseline). Buds already bonded — this is a reconnect,
not a fresh pair.

**⚠️ The rule this capture depends on: no *settings* are touched.** Wearing the Buds and letting
audio/A2DP/in-ear-detection do whatever they naturally do is the point of this session and is not
itself a contamination — but do not tap into EQ, touch-controls, ANC, or any other settings screen.
If a setting is touched by accident, say so explicitly and treat that window as contaminated.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-038`                      |
|      Group(s)    | AE (`OBS-005` — realistic reconnect trigger vs. OS-toggle reconnect; incidental `PAIR-003`, `INEAR`-family) |
|       Date       |                     `___`                           |
| Firmware version | ⚪ ASSUMPTION `release_5.203` |
|   Test device    | Pixel 7a, Android `___`. **Official Pixel Buds Companion App, Google Play Services enabled** |
| Video file       | `CAP-038-recording.mp4` — `___` |
| Log file         | `CAP-038-btsnoop_hci.log` — `___`s, `___` packets, `___`–`___` local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` |

**Capture-integrity pre-flight** — identical method to `CAP-036`/`CAP-037` (see those files):
`capinfos` snaplen check + `frame.cap_len==frame.len` mismatch count, raw extraction path
preferred. Record results here: `___`

## Preparation checklist (before recording)

- [ ] Buds are **already bonded** to this Pixel 7a. Do **not** "Forget" or re-pair.
- [ ] Official Pixel Buds Companion App installed and working; record its version if visible:
      `___`
- [ ] Google Play Services **enabled** — carry `CAP-036`'s verification forward as ⚪ ASSUMPTION.
- [ ] No third-party BLE/GATT tool used at any point.
- [ ] Bluetooth HCI snoop logging enabled and the phone rebooted.
- [ ] Video recording with a visible wall-clock overlay ready, framed so the case, the act of
      removing/inserting the Buds, and the phone screen are all visible.
- [ ] Note whether Bluetooth is on or off before the session starts, and whether the Buds are
      currently connected to anything: `___`

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AE)

1. Start video recording (wall-clock overlay visible), confirm HCI snoop logging active.
2. **Window 1 — realistic reconnect** [`OBS-005`, incidental `PAIR-003`, `INEAR`-family]. With
   Bluetooth already on and the Buds disconnected (either already out of range/off, or manually
   disconnected from system settings first — note which), **physically remove both earbuds from
   the case and insert them into your ears**, the way a normal user would. Note the exact moment
   each earbud is removed from the case, the exact moment each is inserted into an ear, and the
   exact moment the phone shows the connection as active. Then idle ~15–20s without navigating
   anywhere or touching any control on the buds (no taps, no press-and-hold).
3. Clean buffer (~5s).
4. **Window 2, optional/time-permitting — case-lid-only trigger** [`OBS-005`]. With the Buds now
   back in the case and Bluetooth still on, close the case lid fully (confirm on-screen
   disconnect), wait ~5s, then **open the case lid only** — do not touch the Bluetooth toggle or
   any app screen. Note whether/when a reconnect occurs on its own, and how (if at all) it differs
   from Window 1's or `CAP-036`'s toggle-triggered reconnect. Skip freely if the session is
   running long; note whether this window was run at all.
5. Stop video recording and HCI snoop logging.
6. Extract via the raw btsnoop path first, then run the capture-integrity pre-flight.

## Event Timeline

| Time (local) | Action / Event | Initiator | Test-ID | Evidence in `CAP-038-btsnoop_hci.log` |
|---|---|---|---|---|
| `___` | Start video recording | — | — | — |
| `___` | **Window 1 start** — Left earbud removed from case | User (Hardware) | `OBS-005`, `INEAR`-family | frame `___` |
| `___` | Right earbud removed from case | User (Hardware) | `OBS-005` | frame `___` |
| `___` | Both earbuds inserted into ears | User (Hardware) | `OBS-005`, `INEAR`-family | frame `___` |
| `___` | Connection established (on-screen) | App/OS (Auto) | `OBS-005`, `PAIR-003` | frame `___` |
| `___` - `___` | Idle, nothing touched | — | `OBS-005` | frames `___`–`___` |
| `___` | **Window 1 end** | — | `OBS-005` | frame `___` |
| `not run` / `___` | *(optional)* **Window 2 start** — case lid opened, no toggle touched | User (Hardware) | `OBS-005` | frame `___` |
| `___` | *(optional)* Reconnect observed (or not) | App/OS (Auto) | `OBS-005` | frame `___` |
| `___` | *(optional)* **Window 2 end** | — | `OBS-005` | frame `___` |
| `___` | End video recording | — | — | — |

**Contamination log:** `___`

## Decode / Analysis

```
tshark -r CAP-038-btsnoop_hci.log -Y "bthci_acl.chandle==<Buds chandle>" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.len -e data.data
```

- [ ] **Does the DLCI 0x04 `08 11`/`08 13` pair still fire on this realistic reconnect** (per
      `DECISIONS.md` ADR-022's precisely-scoped trigger — channel (re)establishes and carries real
      payload)? Expected: yes, since the trigger condition doesn't distinguish physical cause.
      Result: `___`
- [ ] **Does the `Settable-toggles` byte match "undocked" (`0xe8`) per `DECISIONS.md` ADR-024**,
      now that the Buds are genuinely worn, not merely "not in the case"? Result: `___`
- [ ] **New traffic not seen in `CAP-036`/`CAP-037`:** anything on any DLCI, or any BLE/GATT/A2DP/
      HFP event, that only makes sense given the Buds are physically worn (an in-ear-detection
      push, A2DP `AVDTP` stream setup, an HFP SLC establishment tied to audio routing)? List every
      candidate with frame numbers — do not assume a plausible-looking frame is this without
      checking its content against `PROTOCOL.md`'s existing decode tables. Result: `___`
- [ ] **Window 2 (if run): does opening the case lid alone (no BT toggle) trigger a reconnect, and
      if so, does it produce the same wire sequence as Windows 1/`CAP-036`'s toggle**, or something
      structurally different? Result: `___`
- [ ] **Outcome classification:** (a) new traffic found attributable specifically to physical
      wearing → record with frame numbers, label 🟡 HYPOTHESIS pending replication; (b) no new
      traffic beyond what `CAP-036`/`CAP-037` already show → a clean negative, meaning the app's
      wire behavior on reconnect doesn't depend on *how* the reconnect was triggered; (c)
      inconclusive (contaminated window, truncated log) → 🔴 unconfirmed, re-run. Classification:
      `___`

## Open Questions

- 🔴 `___`

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13).
- [ ] Write `CAP-038-FINDINGS.md` per `PROJECT_RULES.md` §2, hex & script rule (§1 rule 4a).
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index (status → `captured`/`analyzed`) and
      `id_registry.csv`'s `CAP-038` row.
- [ ] Add `OBS-005`'s Evidence column pointer in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (pointer only).
- [ ] Copy every new 🔴 OPEN QUESTION into `PROTOCOL.md` §6 (mandatory, §8).
- [ ] **Do not** promote anything to 🟢 FACT or write a `DECISIONS.md` ADR without explicit
      maintainer sign-off (`AGENTS.md` §6, §15) — propose only.
- [ ] Rename this capture's folder to the actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-038-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AE/CAP-038-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-038-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AE/CAP-038-EVENT-NOTES
