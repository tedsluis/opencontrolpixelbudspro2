# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AC, settings-state read-back on (re)connect and on settings-screen open (`CAP-036`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `___` / `[ ]` below as the
session happens, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). **Do not pre-fill any log-derived value**
(frame numbers, exact timestamps, packet counts) before the capture exists. Once reviewed, rename
this folder from the placeholder `CAP-036-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AC` to the actual
session date/start-time/end-time, e.g. `CAP-036-2026-09-05_07-00-00_07-08-00-Group_AC`.

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
| Log file         | `CAP-036-btsnoop_hci.log` — `___`s, `___` packets, `___`–`___` local time |
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
      Record them here: `___`
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

*(Fill in after reviewing the video frame-by-frame against its wall-clock overlay, cross-checked
against `CAP-036-btsnoop_hci.log` via `tshark` — per `AGENTS.md` §13. One row per distinct
event/boundary. Leave `___` where a value isn't known yet rather than estimating it.)*

| Time (local) | Action / Event | Initiator | Test-ID | Evidence in `CAP-036-btsnoop_hci.log` |
|---|---|---|---|---|
| `06:35:58` | Start video recording | — | — | — |
| `06:36:05` | **Window 1 start** — reconnect triggered (Bluetooth toggled ON, tap on device to connect) | User (App) | `OBS-004`, `PAIR-003` | frame `___` |
| `06:36:40` | Connection established (on-screen confirmation: "Active", 100% battery) | App/OS (Auto) | `OBS-004`, `PAIR-003` | frame `___` |
| `06:36:40` - `06:37:03` | Idle, nothing touched, nothing navigated | — | `OBS-004` | frames `___`–`___` |
| `06:37:03` | **Window 1 end** (User taps gear icon to enter settings) | User (App) | `OBS-004` | frame `___` |
| `06:37:27` | **Window 2 start** — EQ screen opened, finished rendering | User (App) | `OBS-004` | frame `___` |
| `06:37:27` - `06:38:00` | Idle, no slider or preset touched | — | `OBS-004` | frames `___`–`___` |
| `06:38:00` | **Window 2 end** (User navigates back) | User (App) | `OBS-004` | frame `___` |
| `06:38:04` | Navigated away from EQ screen (back to device details, opening Controls) | User (App) | `OBS-004` | frame `___` |
| `06:38:46` | **Window 3 start** — touch-controls screen opened | User (App) | `OBS-004` | frame `___` |
| `06:38:46` - `06:39:15` | Idle, no toggle touched | — | `OBS-004` | frames `___`–`___` |
| `06:39:15` | **Window 3 end** (User navigates back) | User (App) | `OBS-004` | frame `___` |
| `not run` | *(optional)* **Window 4 start/end** — in-ear detection screen opened | — | `OBS-004` | — |
| `06:39:53` | *(optional)* **Window 5 start** — multipoint screen opened | User (App) | `OBS-004` | frame `___` |
| `06:39:53` - `06:41:16` | Idle, nothing touched on multipoint screen | — | `OBS-004` | frames `___`–`___` |
| `06:41:16` | **Window 5 end** | User (App) | `OBS-004` | frame `___` |
| `06:41:18` | End video recording | — | — | — |

**Contamination log:** None. Windows were clean and no sliders or toggles were accidentally manipulated.

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

- [ ] **DLCI 0x04 — the one named read opcode:** does a `08 11 …` frame ("Get ANC state",
      `PROTOCOL.md` §4.1) appear anywhere — in any window, or at connection setup? Never observed in
      any capture to date. Result: `___`
- [ ] **DLCI 0x02 — any `Sent`-direction frame in a window where nothing was touched?** Split each
      RFCOMM payload on the `0x7e` flag byte *before* decoding — multiple complete HDLC sub-frames
      routinely pack into one RFCOMM I-frame, a trap `DESKRESEARCH_FINDINGS.md`'s 2026-08-28 pass
      documents explicitly. For anything found, say whether it matches §4.5's known
      `field5{field4{…}}` **write** envelope or is a differently-shaped frame — those are different
      findings and must not be merged. Result: `___`
- [ ] **DLCI 0x08 — do not mistake a known push for a query.** This channel already pushes
      unsolicited content on connect (battery, the `google-pixel-buds-pro-v1` capability blob,
      firmware string). Expect traffic here in window 1 and attribute it against `PROTOCOL.md`
      §4.3 Option E / §2.3 before reading anything new into it. Result: `___`
- [ ] **Per-window attribution:** for each frame found, state which window it falls in and how far
      it sits from that window's start — a frame arriving 12s into an idle window means something
      different from one arriving 200ms after the screen opened.
- [ ] **Three-way outcome classification (required — do not record a bare "nothing found"; per
      Group AC's own stated-in-advance outcomes):** (a) query frame found → identify channel +
      trigger, label 🟡 HYPOTHESIS until it replicates; (b) no query traffic, windows clean → a
      positive negative-result finding, recorded with the filters and windows that back it;
      (c) inconclusive (setting touched, window contaminated, log truncated) → 🔴 unconfirmed, re-run.
      Classification: `___`

## Open Questions

*(Add every new 🔴 OPEN QUESTION found here — and remember `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §8's
**mandatory** rule: each one must also be copied into `PROTOCOL.md` §6's matching subsection, even
if unresolved, or it becomes invisible to future sessions.)*

- 🔴 `___`

**Carried in for this session, do not conflate with the above:** `PROTOCOL.md` §6 (Behavior) already
carries an item from `CAP-024-FINDINGS.md` §4 — does opening the "Case sounds" screen itself trigger
a state-sync **write** on DLCI 0x02, or does a write only register on an explicit tap? Windows 2 and
3 above are the same experimental shape (a settings screen opened with nothing touched) applied to
different screens, so whatever they show is directly relevant to that question too. But **a write on
screen-open and a read on screen-open are different findings** — this session's own question is the
read. Keep the two labelled separately in `CAP-036-FINDINGS.md`.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check, against `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` and the Group(s) column of this
      session's Capture Index row) — confirm `OBS-004` is clearly referenced above, and flag any
      Test-ID that was expected but not actually observed rather than leaving it silently missing.
- [ ] Write `CAP-036-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a) — including for a negative
      result.
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status from
      `planned` to `captured`/`analyzed`, and fill in the Android/firmware/app-version columns and
      the log path. Update `id_registry.csv`'s `CAP-036` row to match.
- [ ] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `OBS-004` Evidence column with a **pointer only**
      (e.g. `CAP-036-FINDINGS.md §N`), never a restated finding.
- [ ] Copy every new 🔴 OPEN QUESTION into `PROTOCOL.md` §6 (mandatory, §8).
- [ ] **Do not** promote anything in `PROTOCOL.md` to 🟢 FACT, and **do not** write or amend a
      `DECISIONS.md` ADR, without explicit maintainer sign-off — propose it, clearly labelled as a
      proposal (`AGENTS.md` §6, §15).
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the actual
      session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-036-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AC/CAP-036-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-036-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AC/CAP-036-EVENT-NOTES
