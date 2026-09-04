# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AC, settings-state read-back on (re)connect and on settings-screen open (`CAP-036`)

**Status:** ✅ **Captured and analyzed 2026-09-04.** Full decode/analysis in
`CAP-036-FINDINGS.md` — this file carries the corrected Event Timeline (verified via a
frame-by-frame video re-pass) and a summary pointer to that decode, per
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update). Folder already renamed
from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the actual session
date/start-time/end-time.

**Status legend used throughout this file** (`PROJECT_RULES.md` §1, `PROTOCOL.md` §0): 🟢 **FACT**
(directly observed, evidence referenced) · 🟡 **HYPOTHESIS** (unverified, with a stated test) ·
⚪ **ASSUMPTION** (treated as true without verification, with a stated reason) · 🔴 **OPEN
QUESTION** (identified gap, no hypothesis yet). Never write a conclusion in this file without one
of these four labels.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AC, `OBS-004`):** isolate whether the **official**
Pixel Buds companion app ever issues a state **query** — a read/"give me the current value" frame —
as opposed to the state **writes** that are the only thing this project has ever observed for
settings. Today, every entry in `PROTOCOL.md` §4.2 (EQ) and §4.5 (Conversation Detection,
Multipoint, touch & press-and-hold, head gestures, in-ear detection, mono audio, Volume EQ, volume
balance, case sounds) rests exclusively on write-direction evidence: a UI tap producing a `Sent`
frame on DLCI 0x02, whose only observed response is an `Rcvd`-direction echo of the same
field/prefix shape ("no distinct ACK opcode observed" — §4.5.1's own wording, repeated across
§4.5). On DLCI 0x04, a read direction *is* documented for ANC (Group `0x08` Code `0x11`, "Get ANC
state", `PROTOCOL.md` §4.1) but has **never** been seen on the wire in any capture — only `0x12`
(Set) and `0x13` (Notify).

This matters beyond curiosity: `ARCHITECTURE.md` §3.1 (State Reconciliation) requires *this*
project's app to query the hardware's actual current state on **every** (re)connection before
trusting any cached value. This session tests whether the official app does the same thing, and on
which trigger.

**A clean negative result is valuable, citable evidence for this experiment — not a failed
session.** If no query traffic is found in any window, that is a positive finding in its own right
about how the official app behaves (and a real constraint to reason about for `ARCHITECTURE.md`
§3.1), provided the windows were clean. This project already has precedent for treating a clean
negative as real evidence rather than an empty result — see `DESKRESEARCH_FINDINGS.md`'s 2026-08-28
cross-capture pass, which records "the best available negative-result test for this question … and
it comes back clean" as a labelled finding with the same discipline as any positive one. Record the
absence explicitly, naming the exact filters run and the windows they covered
(`PROJECT_RULES.md` §1 rule 4a — the hex & script rule applies to a negative result too: the
command that found nothing is exactly what makes the nothing verifiable).

**Method for this session — deliberately the *normal* baseline, the opposite of Groups S/AB:**
Pixel 7a, official Pixel Buds companion app installed and in use, Google Play Services **enabled**.
Groups S and AB strip GMS and the app away to isolate what the Buds do on their own; here the
official app's own behavior *is* the object of study, so it must be present, connected, and
behaving normally.

**⚠️ The single rule this whole capture depends on: NO SETTING IS TOUCHED AT ANY POINT.** Not a
slider, not a toggle, not an ANC mode, not an EQ preset, not a press-and-hold option. Every frame in
this log must be app- or connection-initiated, never user-initiated. If a setting is touched by
accident, say so explicitly in the Event Timeline and treat that window as contaminated — do not
quietly keep it.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-036`                      |
|      Group(s)    | AC (`OBS-004` — settings-state read-back on (re)connect and on settings-screen open; incidental `PAIR-003`) |
|       Date       |                     2026-09-04                      |
| Firmware version | ⚪ ASSUMPTION `release_5.203` (carried over from previous sessions, not explicitly checked on-screen during this capture) |
|   Test device    | Pixel 7a, Android 14. **Official Pixel Buds Companion App, Google Play Services enabled** (Normal baseline) |
| Video file       | `CAP-036-recording.mp4` — 05:18s, `06:35:58`–`06:41:16` local time |
| Log file         | `CAP-036-btsnoop_hci.log` — 454.462092s, 2,492 packets, 2026-09-04 06:35:47.353558–06:43:21.815650 local/+0200 |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` |

**Capture-integrity pre-flight (do this immediately after extraction, before any analysis — per
`CAP-014-FINDINGS.md` §0's method and `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §6's snaplen gotcha):**

```
$ capinfos CAP-036-btsnoop_hci.log
# check: "Packet size limit" should read "(not set)" / "inferred: 262144" or similar — NOT a small value

$ tshark -r CAP-036-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
# expect: mismatches: 0
```

If this fails (truncated), stop before spending analysis time — re-extract via
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3 **step 3's raw path first**
(`FS/data/log/bt/btsnoop_hci.log` or `FS/data/misc/bluetooth/logs/btsnoop_hci.log`) rather than
step 4's `btsnooz.py` fallback, which is what produced the truncation in `CAP-012`/`CAP-013`/
`CAP-017`/`CAP-031`. **Truncation is disproportionately dangerous for this specific session:** a
negative result ("no query frame found") is one of the two outcomes this capture is designed to
produce, and a truncated log can manufacture that negative artificially by clipping exactly the
short frames being looked for. Record which extraction path was actually used:
`./FS/data/misc/bluetooth/logs/btsnoop_hci.log` (raw step-3 path / `btsnooz.py` step-4 fallback).

**Result (2026-09-04, confirmed):** raw step-3 path used (file name suffix `-btsnoop_hci.log`,
not `-btsnooz_hci.log`). `capinfos`: `Packet size limit: file hdr: (not set)`,
`Interface #0 info: Capture length = 262144` — no snaplen cap.
`tshark ... | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'` → `mismatches: 0` (2,492/2,492
frames). Not truncated. See `CAP-036-FINDINGS.md` §0 for the full command/output.

**CLI-hygiene correction found this session:** the `bluetooth.addr == 04:00:6e:cf:6e:07` filter
this file's own §8 command (below) and `AGENTS.md` §13 both specify **returns zero matches** in
this log for RFCOMM/`btrfcomm` frames — Wireshark's `bluetooth.addr` aggregate field is not
populated for ACL/RFCOMM traffic in this particular btsnoop capture, only for the HCI event layer
(`bthci_evt.bd_addr`). Verified workaround: `bthci_evt.bd_addr == 04:00:6e:cf:6e:07` **does**
match (3 frames, including the Connection Complete event, frame 906), which identifies
`bthci_acl.chandle == 0x0005` as the Buds' connection handle for the rest of this session — used
as the pre-filter instead of `bluetooth.addr` throughout `CAP-036-FINDINGS.md`. A second,
unrelated BLE/GATT connection (`bthci_acl.chandle == 0x0003`, plain `ATT` service-discovery
traffic, 06:36:03–06:36:21, before the Buds' own reconnect completes) is present in the log —
per the `CAP-004-FINDINGS.md` §1 Fitbit-contamination precedent, this is background traffic from
another paired device, not the Buds, and structurally never touches DLCI 0x02/0x04/0x08 — see
`CAP-036-FINDINGS.md` §1.

## Preparation checklist (before recording)

- [x] Buds are **already bonded** to this Pixel 7a and connect normally. Do **not** "Forget" or
      re-pair — window 1 is a *reconnect* (`PAIR-003`), not a fresh pairing handshake, whose
      traffic burst would swamp the isolation this session needs.
- [x] Official Pixel Buds Companion App installed and working; record its version if visible on
      screen: `1.0.955078535`
- [x] Google Play Services **enabled** (the normal baseline — verify, don't assume):
      ```
      $ adb shell dumpsys package com.google.android.gms | grep -i enabled
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
      com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE:
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "android.app.action.DEVICE_ADMIN_ENABLED"
      android.app.action.DEVICE_ADMIN_ENABLED:
          Action: "android.app.action.DEVICE_ADMIN_ENABLED"
          Action: "android.app.action.DEVICE_ADMIN_ENABLED"
          Action: "android.app.action.DEVICE_ADMIN_ENABLED"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
      com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE:
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
          Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
          Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
      android.permission.CHANGE_COMPONENT_ENABLED_STATE
      android.permission.PROVIDE_DEFAULT_ENABLED_CREDENTIAL_SERVICE
      android.permission.CHANGE_COMPONENT_ENABLED_STATE: granted=true
      android.permission.PROVIDE_DEFAULT_ENABLED_CREDENTIAL_SERVICE: granted=true
    User 0: ceDataInode=4032 deDataInode=1866 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=1 instant=false virtual=false quarantined=false
      enabledComponents:
    User 10: ceDataInode=419837 deDataInode=266544 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      enabledComponents:
      android.permission.CHANGE_COMPONENT_ENABLED_STATE
      android.permission.PROVIDE_DEFAULT_ENABLED_CREDENTIAL_SERVICE
      android.permission.CHANGE_COMPONENT_ENABLED_STATE: granted=true
      android.permission.PROVIDE_DEFAULT_ENABLED_CREDENTIAL_SERVICE: granted=true
    User 0: ceDataInode=0 deDataInode=0 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
    User 10: ceDataInode=0 deDataInode=0 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      android.permission.CHANGE_COMPONENT_ENABLED_STATE: granted=true
      android.permission.PROVIDE_DEFAULT_ENABLED_CREDENTIAL_SERVICE: granted=true
      ```
      Record the exact output verbatim, even if unexpected: `___`
      (`enabled=0`/`COMPONENT_ENABLED_STATE_DEFAULT` = enabled; `enabled=3` = disabled-by-user,
      which would mean this session is *not* the intended baseline — say so rather than proceeding
      as if it were.)
- [x] No third-party BLE/GATT tool (nRF Connect or similar) is used at any point this session —
      the official app and system Bluetooth settings only.
- [x] Bluetooth HCI snoop logging enabled and the phone rebooted
      (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §2 step 5).
- [x] Video recording with a visible wall-clock overlay ready, so each window's start/end can be
      pinned to a real timestamp for the log cross-check.
- [x] Note the **on-screen current values** of the settings about to be observed *before* the
      recording starts (EQ preset/slider positions, touch-control toggle states) — if a query frame
      *is* found, its payload can then be checked against what the values actually were, which is
      the difference between "a frame appeared" and "a frame carrying the current state appeared".
      Record them here (recovered via the video re-pass, since screens showing these values were
      opened during, not strictly before, the session — see `CAP-036-FINDINGS.md` §2 for frame
      references): **ANC mode = Off** (confirmed both from the wire, §3 below, and on-screen at
      06:37:04 — the "Off" tile alone shows the selected/tinted background); **EQ = "Last saved"
      preset, all five bands (Upper treble/Treble/Mid/Bass/Low bass) centered at 0, Volume EQ =
      ON** (on-screen at 06:37:28); **Use touch controls = ON, Left/Right press-and-hold = Active
      noise control, Use head gestures = ON** (on-screen at 06:38:05/06:38:28); **In-ear detection
      = ON, Multipoint = ON** (on-screen at 06:39:16, on the "More settings" list, before either
      sub-screen was individually opened).
- [x] Re-read the "no setting is touched" rule above one more time before pressing record.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AC)

Leave a clean few-second buffer before each window, per §4's usual rhythm, so the previous window's
settling traffic doesn't bleed into the next one. Log explicit **observation start** and
**observation end** boundaries for every window, not just a single timestamp (§4.1 Group L's
boundary-logging convention).

1. Start video recording (wall-clock overlay visible) and confirm HCI snoop logging is active.
2. **Window 1 — reconnect (not a fresh pair)** [`OBS-004`, incidental `PAIR-003`]. Trigger a
   reconnect the way a user normally would — take the buds out of the case, or toggle the
   connection from system Bluetooth settings. Note the exact time the connection is **established**
   on screen, then **idle ~10–15s without navigating anywhere at all**, app included. Note the
   window end.
3. Clean buffer (~5s, nothing touched).
4. **Window 2 — open the EQ screen** [`OBS-004`]. Note the exact time the EQ screen finishes
   rendering, then **idle ~15–20s without touching any slider or preset**. The sliders are the whole
   risk on this screen — do not let a scroll turn into a drag. Note the window end.
5. Clean buffer (~5s, nothing touched). Navigate away, back out to the device details screen.
6. **Window 3 — open the touch-controls screen** [`OBS-004`]. Note the exact open time, then
   **idle ~15–20s without touching any toggle**. Note the window end.
7. **Optional bonus, time permitting** — same idle-only pattern, still purely observational,
   still nothing touched: repeat window 3's shape for the **in-ear detection** settings screen, and
   again for the **multipoint** settings screen, each with its own clean buffer and its own
   start/end timestamps. Skip freely if the session runs long; either way, record below whether
   these were run at all.
8. Stop video recording and HCI snoop logging. Keep the session short — don't let it run long
   enough to risk on-device log rotation (§2's note).
9. Extract via `adb bugreport` (§3) — **check for the raw `btsnoop_hci.log` path first**, before
   falling back to `btsnooz.py` — then run the capture-integrity pre-flight above before any
   analysis.

## Event Timeline

**Corrected via a full frame-by-frame video re-pass (1-second-resolution `ffmpeg` extraction,
this project's standard validation method) cross-checked against `CAP-036-btsnoop_hci.log` via
`tshark`, per `AGENTS.md` §13 — several of this table's original placeholder times were off by
5–40 seconds against what the video actually shows (see `CAP-036-FINDINGS.md` §2 for the
frame-by-frame evidence and a list of every correction made). The times below are the corrected,
verified ones.**

| Time (local) | Action / Event | Initiator | Test-ID | Evidence in `CAP-036-btsnoop_hci.log` |
|---|---|---|---|---|
| `06:35:47.35` | btsnoop log starts | — | — | frame 1 |
| `06:35:58` | Start video recording (Bluetooth off, Buds already out of case) | — | — | — |
| `06:36:05` | **Window 1 start** — "Use Bluetooth" toggled ON | User (App) | `OBS-004`, `PAIR-003` | — (no wire event yet) |
| `06:36:28.61` | Phone sends HCI `Create Connection` to the Buds (reconnect actually begins — ~23s after the toggle, not immediately) | OS (Auto) | `PAIR-003` | frame 898 |
| `06:36:30.96` | HCI `Connection Complete`, BD_ADDR `04:00:6e:cf:6e:07`, `chandle 0x0005` | OS (Auto) | `PAIR-003` | frame 906 |
| `06:36:31.30`–`31.33` | DLCI 0x04 (Fast Pair Message Stream) opens: `SABM`→`UA` | OS/App (Auto) | `OBS-004` | frames 1109, 1125 |
| `06:36:31.359` | **`08 11 00 00` — "Get ANC state" query, Sent, DLCI 0x04** (documented-but-never-before-observed opcode, `PROTOCOL.md` §4.1) | App (Auto) | `OBS-004` | frame 1169 |
| `06:36:31.369` | **`08 13 00 04 01 e8 00 20` — "Notify ANC state" response, Rcvd, DLCI 0x04** (current_state=`0x20`=Off, ~10.7ms after the query) | Buds (Auto) | `OBS-004` | frame 1182 |
| `06:36:31.37`–`31.81` | DLCI 0x08 opens and exchanges its connect-time capability/firmware/battery burst (Option E, `PROTOCOL.md` §4.3) | OS/App/Buds (Auto) | — | frames 1186–1372 |
| `06:36:32.57`–`35.66` | DLCI 0x02 (`libmaestro`) opens and exchanges a dense RPC-shaped burst (not decoded further this session — connection-settling traffic, see `CAP-036-FINDINGS.md` §4) | OS/App/Buds (Auto) | — | frames 1392–1591 |
| `~06:36:40`–`06:36:41` | Connection established (on-screen confirmation: "Active", L:100% C:100% R:100%) — note the ~9s gap between wire-level completion (above) and this UI confirmation | App/OS (Auto) | `OBS-004`, `PAIR-003` | — (UI-only; wire side already complete) |
| `06:36:40`–`~06:37:03` | Idle, nothing touched, nothing navigated — **zero DLCI 2/4/8 frames in this span** | — | `OBS-004` | none (checked) |
| `~06:37:04` | **Window 1 end** — user taps gear icon → Device details screen (ANC row shows "Off" selected — matches the wire decode above exactly) | User (App) | `OBS-004` | — |
| `~06:37:27`–`06:37:28` | **Window 2 start** — EQ screen opened, finished rendering ("Last saved", all 5 bands centered, Volume EQ ON) | User (App) | `OBS-004` | — |
| `06:37:28`–`~06:37:53` | Idle, no slider or preset touched — **zero DLCI 2/4/8 frames in this span** | — | `OBS-004` | none (checked) |
| `~06:37:53` | **Window 2 end** — user taps back arrow | User (App) | `OBS-004` | — |
| `~06:37:58` | Back on Device details screen | User (App) | `OBS-004` | — |
| `~06:38:01`–`06:38:05` | Navigated to "Controls and gestures" screen (Use touch controls=ON, Left/Right press-and-hold=Active noise control, Use head gestures=ON) | User (App) | `OBS-004` | — |
| `06:38:05`–`~06:38:23` | Idle on "Controls and gestures", nothing touched — **zero DLCI 2/4/8 frames in this span** | — | `OBS-004` | none (checked) |
| `~06:38:23`–`06:38:28` | **Window 3 start** — tapped into the "Touch controls" leaf screen (Use touch controls=ON) | User (App) | `OBS-004` | — |
| `06:38:28`–`~06:38:44` | Idle on "Touch controls", no toggle touched — **zero DLCI 2/4/8 frames in this span** | — | `OBS-004` | none (checked) |
| `~06:38:46` | **Window 3 end** — navigated back to "Controls and gestures", then (~06:38:53–58) back to Device details | User (App) | `OBS-004` | — |
| `~06:39:08`–`06:39:16` | Navigated to "More settings" list (In-ear detection=ON, Multipoint=ON already visible here, before either sub-screen opened) | User (App) | `OBS-004` | — |
| `06:39:16`–`~06:39:43` | Idle on "More settings", nothing touched — **zero DLCI 2/4/8 frames in this span** | — | `OBS-004` | none (checked) |
| `06:39:51` | First periodic DLCI 0x02/0x04/0x08 push burst begins (~10s-ish cadence thereafter; same known content as the connect-time pushes, all Rcvd-direction) — **begins ~3s before the Multipoint screen even renders, i.e. time-triggered, not screen-open-triggered** | Buds (Auto) | — | frames 1999–2007 (DLCI 0x08), 2009 (DLCI 0x02) |
| `~06:39:53`–`06:39:54` | **Window 5 start** — Multipoint screen opened (Use multipoint=ON) | User (App) | `OBS-004` | — |
| `06:39:54`–`06:41:16` | Idle on Multipoint screen, nothing touched — **only the already-known periodic push (above) recurs (frames through 2361, continuing to 06:42:31, well past this video); no new Sent/query frame anywhere in this span** | — | `OBS-004` | frames 1999–2361 (periodic push only) |
| `06:41:16` | **Window 5 end** — video ends here, screen still untouched | User (App) | `OBS-004` | — |
| `06:41:18` | End video recording | — | — | — |
| `not run` | *(optional)* Window 4 — in-ear detection screen — skipped this session (time) | — | `OBS-004` | — |

**Contamination log:** None on the Buds' own connection. Windows were clean and no sliders or
toggles were accidentally manipulated. **Unrelated background traffic present but structurally
isolated** (per `CAP-004-FINDINGS.md` §1's precedent): a second BLE/GATT connection
(`bthci_acl.chandle == 0x0003`, plain ATT service-discovery traffic, 06:36:03–06:36:21) from
another paired device, ending before the Buds' own reconnect even completes — never touches DLCI
0x02/0x04/0x08, so it does not affect any window's evidence. See `CAP-036-FINDINGS.md` §1.

## Decode / Analysis

*(Full decode with the exact command **and** the raw hex bytes per finding belongs in
`CAP-036-FINDINGS.md`, per `PROJECT_RULES.md` §1 rule 4a. This section carries only the summary and
a pointer. The hex & script rule applies to a negative result too — the command that found nothing
is what makes the nothing verifiable.)*

**CLI hygiene (`AGENTS.md` §13): pre-filter on the Buds' address first**, before layering on any
protocol-specific filter — a shared, non-restarted snoop log can contain unrelated devices' traffic
(see `CAP-004`'s incidental Fitbit frames).

```
tshark -r CAP-036-btsnoop_hci.log -Y "bluetooth.addr == <buds MAC> and btrfcomm.dlci in {2,4,8}" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.len -e data.data
```
(DLCI values decimal: 0x02=2, 0x04=4, 0x08=8 — `tshark` compares this field numerically.)

- [x] **DLCI 0x04 — the one named read opcode:** does a `08 11 …` frame ("Get ANC state",
      `PROTOCOL.md` §4.1) appear anywhere — in any window, or at connection setup? Never observed in
      any capture to date. **Result: YES — found once, frame 1169 (`08 11 00 00`, Sent), 06:36:31.359,
      immediately after DLCI 0x04's channel opens during the Window 1 reconnect, ~9s before the UI's
      own "Active" confirmation renders. Answered ~10.7ms later by frame 1182 (`08 13 00 04 01 e8 00
      20`, Rcvd — "Notify ANC state", current_state=`0x20`=Off), which matches the on-screen ANC
      state ("Off" tile selected) confirmed later at 06:37:04. Never recurs in Windows 2/3/5 (EQ,
      Controls/Touch, Multipoint) — the trigger is reconnection, not settings-screen-open. See
      `CAP-036-FINDINGS.md` §3 for the full decode and raw hex.**
- [x] **DLCI 0x02 — any `Sent`-direction frame in a window where nothing was touched?** Split each
      RFCOMM payload on the `0x7e` flag byte *before* decoding — multiple complete HDLC sub-frames
      routinely pack into one RFCOMM I-frame, a trap `DESKRESEARCH_FINDINGS.md`'s 2026-08-28 pass
      documents explicitly. For anything found, say whether it matches §4.5's known
      `field5{field4{…}}` **write** envelope or is a differently-shaped frame — those are different
      findings and must not be merged. **Result: NO. Every `Sent`-direction DLCI 0x02 frame in this
      session (34 frames) falls inside the pre-Window-1 connection-settling burst (06:36:32.57–
      35.66, frames 1404–1591) — none inside Window 1/2/3/5's stated idle spans. That burst itself
      is not decoded further this session (RPC-shaped, not matching the write envelope's specific
      inner-field shape on inspection) — see `CAP-036-FINDINGS.md` §4. A separate, periodic
      Rcvd-only push (frames 2009/2048/2164/2204/2237/2334/2360, ~10s-ish cadence from 06:39:51
      onward) recurs during Window 5 but is never `Sent`-direction and is not write-shaped.**
- [x] **DLCI 0x08 — do not mistake a known push for a query.** This channel already pushes
      unsolicited content on connect (battery, the `google-pixel-buds-pro-v1` capability blob,
      firmware string). Expect traffic here in window 1 and attribute it against `PROTOCOL.md`
      §4.3 Option E / §2.3 before reading anything new into it. **Result: confirmed — the connect-time
      burst (frames 1273–1372) matches Option E's known capability/firmware/battery content
      exactly. It also contains several zero-length `[Group][Code][00 00]`-shaped `Sent` frames
      (e.g. `04 02 00 00`, `04 04 00 00`, `04 11 00 00`, `04 13 00 00`, `04 15 00 00`, `0e 04 00
      00`) — structurally identical in shape to DLCI 0x04's confirmed "Get" pattern, but these
      Group/Code pairs are not mapped to any known setting (`PROTOCOL.md` §6 already lists DLCI
      0x08's Groups as largely unresolved) — flagged as a new 🔴 open question, not claimed as a
      settings query. The same connect-time content (Option E) recurs verbatim during Window 5's
      idle period (~10s-ish cadence from 06:39:51), always `Rcvd`, never a new `Sent` frame.**
- [x] **Per-window attribution:** for each frame found, state which window it falls in and how far
      it sits from that window's start — a frame arriving 12s into an idle window means something
      different from one arriving 200ms after the screen opened. **Result: the only query/response
      pair (DLCI 0x04, `08 11`/`08 13`) falls inside the reconnect's connection-establishment burst,
      ~34ms after DLCI 0x04's own channel opens and ~9s before the UI shows "Active" — i.e. it is
      attributable to the reconnect trigger, not to any settings-screen-open trigger. No frame of
      any kind (query-shaped or otherwise, on DLCI 2/4/8) falls inside Windows 1(idle)/2/3's stated
      idle spans. Window 5's only traffic is the periodic Option-E-shaped push, which starts 3s
      *before* the Multipoint screen finishes rendering — time-triggered, not screen-open-triggered.
      See `CAP-036-FINDINGS.md` §6 for the full table.**
- [x] **Three-way outcome classification (required — do not record a bare "nothing found"; per
      Group AC's own stated-in-advance outcomes):** (a) query frame found → identify channel +
      trigger, label 🟡 HYPOTHESIS until it replicates; (b) no query traffic, windows clean → a
      positive negative-result finding, recorded with the filters and windows that back it;
      (c) inconclusive (setting touched, window contaminated, log truncated) → 🔴 unconfirmed, re-run.
      **Classification: a mix of (a) and (b), split cleanly by trigger — not a single outcome for
      the whole session:**
      - **On reconnection (trigger 1): outcome (a).** DLCI 0x04's "Get ANC state" (`0x11`) query
        fires, Seeker→Provider, immediately after channel establishment, answered by "Notify ANC
        state" (`0x13`) ~10.7ms later — 🟡 HYPOTHESIS pending replication (single sample, one
        session), not self-promoted to 🟢 FACT per `AGENTS.md` §6/§15.
      - **On settings-screen-open (trigger 2, Windows 2/3/5 — EQ, Controls-and-gestures, Touch
        controls, More settings, Multipoint, all clean and uncontaminated): outcome (b).** No query
        traffic of any kind on DLCI 0x02/0x04/0x08 in any of these windows. A genuine, citable
        negative result per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AC's own stated discipline.
      See `CAP-036-FINDINGS.md` §7 for the full outcome table and §10 for proposed downstream
      updates (awaiting maintainer sign-off).

## Open Questions

*(Add every new 🔴 OPEN QUESTION found here — and remember `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §8's
**mandatory** rule: each one must also be copied into `PROTOCOL.md` §6's matching subsection, even
if unresolved, or it becomes invisible to future sessions.)*

- 🔴 DLCI 0x08's connect-time zero-length `[Group][Code][00 00]`-shaped `Sent` frames (`04 02`,
  `04 04`, `04 11`, `04 13`, `04 15`, `0e 04`) are structurally identical to DLCI 0x04's confirmed
  "Get" pattern, but none of these Group/Code pairs is mapped to any known setting — genuinely
  open, not a settings query claim (`CAP-036-FINDINGS.md` §5).
- 🔴 The dense RPC-shaped burst on DLCI 0x02 immediately after channel establishment (frames
  1404–1591, 06:36:32.57–35.66) is not decoded this session beyond noting it shares a partial
  constant prefix with §4.5's documented correlation-ID pattern (`03 10 XX 1d ea 71 de 7e 25`) —
  whether it carries any settings-state read-back via `libmaestro`'s own channel (as opposed to the
  DLCI 0x04 mechanism this session did confirm) is unresolved (`CAP-036-FINDINGS.md` §4).
- 🔴 The "Notify ANC state" frame found this session (`08 13 00 04 01 e8 00 20`) decodes its
  "Settable toggles" byte as `0x00`, whereas every previously-documented "Set ANC state" frame
  (`PROTOCOL.md` §4.1, `CAP-001`) shows `settable=0xe8` in the same byte position — not reconciled;
  either this byte means something different in a `Notify` triggered by an explicit `Get` versus one
  triggered by a `Set`, or one of the two readings needs revisiting (`CAP-036-FINDINGS.md` §3).

**Carried in for this session, do not conflate with the above:** `PROTOCOL.md` §6 (Behavior) already
carries an item from `CAP-024-FINDINGS.md` §4 — does opening the "Case sounds" screen itself trigger
a state-sync **write** on DLCI 0x02, or does a write only register on an explicit tap? Windows 2 and
3 above are the same experimental shape (a settings screen opened with nothing touched) applied to
different screens, so whatever they show is directly relevant to that question too. But **a write on
screen-open and a read on screen-open are different findings** — this session's own question is the
read. Keep the two labelled separately in `CAP-036-FINDINGS.md`.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check, against `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` and the Group(s) column of this
      session's Capture Index row) — `OBS-004` is exercised throughout (every timeline row above);
      incidental `PAIR-003` (reconnect to an already-bonded device) is exercised at frames 898/906.
      No expected-but-unobserved Test-ID for this Group.
- [x] Write `CAP-036-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a) — including for a negative
      result. Done.
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status from
      `planned` to `analyzed`, and fill in the Android/firmware/app-version columns and the log
      path. Update `id_registry.csv`'s `CAP-036` row to match. Done.
- [x] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `OBS-004` Evidence column with a **pointer only**
      (`CAP-036-FINDINGS.md`), never a restated finding. Done.
- [x] Copy every new 🔴 OPEN QUESTION into `PROTOCOL.md` §6 (mandatory, §8). Done — 3 new items.
- [x] **Do not** promote anything in `PROTOCOL.md` to 🟢 FACT, and **do not** write or amend a
      `DECISIONS.md` ADR, without explicit maintainer sign-off — propose it, clearly labelled as a
      proposal (`AGENTS.md` §6, §15). **Update 2026-09-04, same-day sign-off obtained:** the
      maintainer explicitly approved promoting the "Get ANC state" (`0x11`) opcode's *identity*
      (Group/Code, direction, structure) to 🟢 FACT — applied to `PROTOCOL.md` §4.1 and recorded in
      `DECISIONS.md` ADR-021 — while explicitly declining to promote the broader trigger-reliability
      claim or the settings-screen-open negative result, which remain 🟡 HYPOTHESIS.
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the actual
      session date/start-time/end-time. Already done prior to this session
      (`CAP-036-2026-09-04_06-35-58_06-41-18-Group_AC`).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-036-2026-09-04_06-35-58_06-41-18-Group_AC/CAP-036-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-036-2026-09-04_06-35-58_06-41-18-Group_AC/CAP-036-EVENT-NOTES
