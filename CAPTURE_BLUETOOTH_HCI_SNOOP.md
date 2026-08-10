# CAPTURE_BLUETOOTH_HCI_SNOOP.md — Bluetooth HCI Snoop Capture Guide

**Purpose:** step-by-step procedure to capture, extract, and analyze Bluetooth HCI traffic
between a phone and the Pixel Buds Pro 2, in order to fill in the confidence-rated
placeholders in `PROTOCOL_NOTES.md` (magic bytes, checksum, opcode table, battery
approach). This document is the *how* — `PROTOCOL_NOTES.md` is the *what we found*.

Two devices are used, for two different purposes:

| Device | Role | Why |
|---|---|---|
| Pixel 7a, Android 16 (with Google Play Services), official Pixel Buds app | **Primary capture** | Only source of actual `libmaestro` command frames (ANC toggle, EQ write) triggered on demand |
| Pixel 9a, GrapheneOS, no Pixel Buds app | **Secondary / validation capture** | Confirms pairing/bonding behavior, GATT service discovery, and any spontaneous/OS-level traffic on the actual target OS, without app-driven commands |

Do the Pixel 7a capture first — it's the one that produces frames you can actually match
against `pbpctrl`'s documented commands. The Pixel 9a capture is a validation/baseline
step, not a substitute.

---

## 1. Prerequisites

### 1.1 On your computer
- [ ] **Wireshark** installed (has a built-in Bluetooth HCI dissector — no plugins needed).
- [ ] **Android platform-tools** (`adb`) installed and on your `PATH`.
- [ ] A few hundred MB of free disk space — `adb bugreport` output can be large.
- [ ] A USB cable for each phone (data-capable, not charge-only).

### 1.2 On each phone
- [ ] Developer options enabled.
- [ ] USB debugging enabled.
- [ ] Bluetooth HCI snoop logging enabled (see §2).
- [ ] The Pixel Buds Pro 2 already known to pair reliably with that phone (test this once
      *before* your real capture session, so pairing issues don't eat into your capture
      time or contaminate your log with retries).

### 1.3 A way to log timestamps of your own actions
A notes app, a voice memo, or simply writing down wall-clock times as you go. This is
what lets you match a Wireshark frame to "the moment I tapped ANC → Off" later. Don't
skip this — without it, correlating frames to actions is much harder after the fact.

---

## 2. Enabling HCI Snoop Logging

Applies to **both** phones; steps are identical regardless of GrapheneOS vs. stock
Android, since HCI snoop logging is an AOSP-level feature.

1. **Enable Developer options** (skip if already enabled):
   Settings → About phone → Software information → tap **Build number** 7 times.
2. Go to **Settings → System → Developer options**.
3. Enable **"Enable Bluetooth HCI snoop log"**.
4. Enable **"USB debugging"**.
5. **Restart Bluetooth — reboot the phone (recommended default).** A toggle
   off/on (Settings → Bluetooth) is the officially documented way to make the
   logger pick up the setting, and normally works. A reboot is recommended
   here anyway, for a cost-based reason rather than a confirmed reliability
   problem: on the rare occasion the toggle doesn't take, the failure is
   silent — you only discover it after doing a full round of actions and
   pulling an empty bugreport (see the FAQ in §7). A phone reboot costs about
   a minute; redoing a capture session doesn't. If you've already verified on
   your specific phone that a plain toggle reliably starts logging, that's a
   fine, faster alternative — just confirm it once (§7 FAQ has the recovery
   steps if a session comes back empty either way).
6. Connect the phone to your computer via USB and **accept the "Allow USB debugging?"
   dialog** on the phone (approve your computer's RSA key fingerprint).
7. Verify the connection: `adb devices` should list the phone as `device` (not
   `unauthorized` or `offline`).

**Note on log rotation:** the on-device snoop log has a size cap and will eventually
overwrite old data during long sessions. Keep individual capture sessions short and
focused (a handful of isolated actions, see §4) rather than one long open-ended session,
so nothing rotates out before you extract it.

---

## 3. Extracting the Log (identical method for stock Android *and* GrapheneOS)

Do **not** attempt `adb root` or a direct `adb pull` of
`/data/misc/bluetooth/logs/btsnoop_hci.log` — on a normal production/non-rooted build,
including stock Pixel firmware and GrapheneOS, this will fail with a permission error,
since `adbd` cannot run as root on such a build. This is expected and not specific to
GrapheneOS; it has nothing to do with whether the device is capable of being rooted,
only with the fact that neither phone is rooted here.

The supported, working method on both phones:

1. Run on your computer:
   ```
   adb bugreport buds_capture
   ```
   This produces `buds_capture.zip` (or a similarly named file/folder depending on
   platform-tools version) in your current directory. No root required. A GrapheneOS
   community forum report documents `adb bugreport` producing a report containing the
   BTSnoop log on a stock, non-rooted GrapheneOS build on Android 16 — this is a
   community observation, not an official GrapheneOS platform guarantee, but it's
   consistent with `adb bugreport` being the standard, documented AOSP mechanism (below)
   rather than anything GrapheneOS-specific.
2. Unzip the result. Depending on your unzip tool, this may spill its contents directly
   into your current directory (an `FS/` folder, various `.txt`/`.zip` files, etc.)
   rather than into a named subfolder — that's normal, not a sign anything went wrong.
3. **Check the raw log path first — simplest, no tooling needed if it's there:** look
   for either of these inside the unzipped `FS/` tree:
   - `FS/data/log/bt/btsnoop_hci.log`
   - `FS/data/misc/bluetooth/logs/btsnoop_hci.log`

   If one of these exists, **you're done** — it's already a raw BTSnoop file, open it
   directly in Wireshark (§5). No need to run `btsnooz.py` at all. The exact path varies
   by Android version, which is why step 4 below exists as a version-resilient
   alternative — but check here first, since it's usually simpler when present.
4. **If neither raw path exists — extract via `btsnooz.py`, per current AOSP
   documentation:**
   ```
   # Get btsnooz.py from the AOSP source tree if you don't already have it:
   # https://cs.android.com/android/platform/superproject/+/android-latest-release:packages/modules/Bluetooth/system/tools/scripts/btsnooz.py

   # Find the actual bugreport text file first — do NOT assume it's named after
   # whatever you passed to `adb bugreport`. That name only applies to the .zip;
   # the .txt inside always keeps Android's own generated name:
   ls bugreport-*.txt
   # e.g. bugreport-lynx-CP2A.260705.006-2026-08-09-08-52-28.txt

   btsnooz.py bugreport-<device>-<build>-<timestamp>.txt > buds_capture_btsnoop.log
   ```
   This doesn't depend on guessing an internal zip path that has already moved between
   Android releases (see step 3 above) — `btsnooz.py` extracts the BTSnoop data directly
   from the bugreport's text dump, which is a more stable interface across versions, at
   the cost of an extra tool to set up.
   If neither the raw path (step 3) nor a `bugreport-*.txt` file is found at all, search
   the extracted contents by filename:
   ```
   find . -iname "*btsnoop*"
   ```
5. Copy/rename the resulting log somewhere memorable, e.g.
   `captures/2026-08-02_pixel7a_anc-toggle.log`, so repeated sessions don't overwrite
   each other.
6. Once you've confirmed extraction worked, you can disable the HCI snoop toggle again
   (§2 step 3) to avoid unnecessary background logging and disk usage between sessions.

---

## 4. Capture Procedure — Isolate Every Action

**Groups A–Q (and Z) below are capture scenarios — efficient bundles of related actions
to run in one session — not the project's official test record.** Each numbered item is
annotated with its permanent Test-ID (e.g. `ANC-001`) from
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, which is where the actual catalog of investigated
behaviors, their existence-confidence, and their evidence status live. A Group can bundle
Test-IDs from more than one feature area for capture efficiency (e.g. Group C bundles
`CONV-001` and `MULTI-001`, which are unrelated features) — the Group is about *how to
run the session*, the Test-ID is *what's actually being investigated*. See
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §0.1 for the full reasoning.

The single most important rule: **one user-level action per capture window, with a pause
before and after.** A capture full of overlapping actions is very hard to attribute
correctly in Wireshark; a capture of ten cleanly isolated actions is straightforward.
Note that "one action" means one thing you deliberately triggered — not one frame. Some
actions (pairing/bonding above all, see Group A below) legitimately produce a *burst* of
related frames as their own multi-step protocol exchange runs to completion; that whole
burst still counts as the result of a single isolated action, not as several actions to
capture separately.

Recommended rhythm per action: **wait ~5s → note the exact time → perform the action →
wait ~5–10s → move to the next action.**

**This timing is a practical heuristic, not a guarantee that every response arrives
within it.** Since this guide deliberately avoids live capture (§7 FAQ — the live-capture
port usually isn't available on modern Android, so you only see the traffic after
extraction), there's no way to watch responses arrive in real time and wait until they
settle; a fixed interval is the only thing that's actually practical to follow. The known
failure mode: an occasional slow/delayed response can land after you've already moved on,
and get misattributed to whatever the next action was. Mitigation is in the analysis
phase, not the capture phase — see §5 step 3.

### 4.1 Pixel 7a (official app) — primary session

**Source:** the action list below is the validated inventory from `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
sections 1 (User Actions from the Pixel Buds App) and 3 (Automatic Actions Initiated by
the App) — checked against your own app screenshots and official Google support
documentation. It supersedes the older, shorter action list this section used to contain.

This is a **comprehensive** list (~40 actions). You do not need to do it all in one
sitting — per the FAQ in §7 ("Do I need to do a full capture session every time?"), you
can pull a fresh bugreport for any subset at any time. Splitting this into 3–4 shorter
sessions by group (below) is often more manageable than one long one, and keeps each
`adb bugreport` capture focused (see the log rotation note in §6).

Run one continuous **Bluetooth HCI snoop logging session** per group — the
on-device logger you enabled in §2 keeps recording throughout, there's nothing
you need to re-invoke to "keep it going" — and collect a single `adb bugreport`
(§3) once, at the end of the group. Log each action with its own timestamp,
following the usual rhythm: **wait ~5s → note the exact time → perform the
action → wait ~5–10s → move to the next action.**

**⚠️ Priority tip:** PROTOCOL_NOTES.md §6 flags the "Play sound on Left earbud" action
(group K below) as a specifically valuable, low-risk target — its frame can be directly
compared against the Fast Pair Message Stream spec's own worked example
(`0x04 0x01 ...` for a ring action) to confirm or refute the framing hypothesis in
`PROTOCOL_NOTES.md` §2.0. If you only have time for a short session, prioritize group K.
**Caution when you get there:** a superficial resemblance to the worked example (e.g. a
plausible-looking group/code byte pair) is not by itself confirmation — don't promote the
framing hypothesis to FACT off one frame that merely looks compatible. See the
cross-command check in Group K below.

#### Group Z — Pipeline validation (do this first, before anything else)
Before spending your pairing baseline (Group A) or any real reverse-engineering capture,
confirm the whole tooling chain actually works end to end, using the cheapest, most
trivial, already-connected action you have — this catches a broken pipeline (logging
didn't start, bugreport came back empty, extraction script misconfigured, wrong
Wireshark filters) on a throwaway capture instead of on your pairing baseline or Find My
Buds, both of which are more valuable and, in the pairing case, mildly disruptive to redo.
1. With the Buds already connected (any existing pairing is fine — this step doesn't
   need a fresh bond), work through §2 (enable HCI snoop, restart Bluetooth) and confirm
   `adb devices` lists the phone.
2. Perform **one trivial, already-familiar action** — e.g. a single ANC mode toggle.
   Note the timestamp.
3. Pull a bugreport (§3), extract via `btsnooz.py`, and open the result in Wireshark.
4. Confirm you can actually see, before doing any protocol interpretation: general HCI
   traffic, an RFCOMM stream, frames addressed to/from the Buds' Bluetooth address, and
   that your noted timestamp lines up with visible activity in the log.
5. If any of the above is missing, fix the tooling (§2, §3, §6 Notes & Gotchas) and
   repeat this step — don't move on to Group A until this passes. Log this as your first
   entry in the Capture Index (§9); mark it `discarded` afterward if you'd rather not
   keep a throwaway capture around, but it still counts as a real, worthwhile session.

#### Group A — Connection / bonding baseline
1. **Pairing / bonding baseline** [`PAIR-001`] — capture this as its own isolated session,
   ideally before the command groups below (B–P). While connecting, this session also
   incidentally covers [`BATT-004`] (battery via RFCOMM on connect) — no separate action
   needed for that, just note it happened here if relevant. If the Buds are already
   paired, **"forget" the device on the phone side first** (Bluetooth
   settings → the paired device → Forget), then re-pair through Bluetooth
   settings — this captures a real bonding handshake instead of skipping it
   because "it's already paired." This is a **lightweight, safely repeatable**
   action: it does not touch the Buds' own memory or the Find My Device link,
   unlike the full factory reset in Group P #16 below — do not confuse the
   two. **Expect a burst, not a single frame:** pairing is one user action
   (tapping the device in the picker) that triggers an automatic multi-step
   exchange (inquiry, authentication, link-key exchange, SDP, profile
   connect) — this entire burst is the result of that one action, not
   several actions to isolate individually; there is nothing to click
   separately for each sub-step. Wait for the connection to settle before
   moving on to Group B.

#### Group B — Active Noise Control
2. **ANC → Off** [`ANC-001`]. Wait. Note time.
3. **ANC → Noise Cancellation** (active) [`ANC-002`]. Wait. Note time.
4. **ANC → Adaptive** [`ANC-003`] (if your firmware exposes it — confirmed present in `release_5.203`
   per `PROTOCOL_NOTES.md` §4.1). Wait. Note time.
5. **ANC → Transparency** [`ANC-004`]. Wait. Note time.

#### Group C — Conversation Detection & Multipoint
6. **Toggle 'Conversation Detection' on/off** [`CONV-001`]. Wait. Note time.
7. **Toggle 'Multipoint' on/off** [`MULTI-001`]. Wait. Note time.

#### Group D — Equalizer: presets
8. **Select EQ preset: Standard** [`EQP-001`]. Wait. Note time.
9. **Select EQ preset: Bass Boost** [`EQP-002`]. Wait. Note time.
10. **Select EQ preset: Bass Reduction** [`EQP-003`]. Wait. Note time.
11. **Select EQ preset: Balanced** [`EQP-004`]. Wait. Note time.
12. **Select EQ preset: Vocal Boost** [`EQP-005`]. Wait. Note time.
13. **Select EQ preset: Clarity** [`EQP-006`]. Wait. Note time.
14. **Select EQ preset: Last saved** [`EQP-007`]. Wait. Note time.
15. **Save current EQ as a new preset** ('Save') [`EQP-008`] — a distinct write action from preset
    selection. Wait. Note time.

#### Group E — Equalizer: individual sliders
Change one band at a time by a clearly visible amount — not all bands in one gesture.
16. **Adjust EQ slider: High treble** [`EQS-001`]. Wait. Note time.
17. **Adjust EQ slider: Treble** [`EQS-002`]. Wait. Note time.
18. **Adjust EQ slider: Mid** [`EQS-003`]. Wait. Note time.
19. **Adjust EQ slider: Bass** [`EQS-004`]. Wait. Note time.
20. **Adjust EQ slider: Low bass** [`EQS-005`]. Wait. Note time.

#### Group F — Touch & head gesture toggles
21. **Toggle 'Touch controls' fully on/off** [`TOUCH-001`]. Wait. Note time.
22. **Toggle 'Head gestures' fully on/off** [`HEAD-001`]. Wait. Note time.

#### Group G — Press-and-hold configuration
23. **Set 'Press and hold' Left → Toggle ANC** [`HOLD-001`]. Wait. Note time.
24. **Set 'Press and hold' Left → Digital assistant** [`HOLD-002`]. Wait. Note time.
25. **Set 'Press and hold' Right → Toggle ANC** [`HOLD-003`]. Wait. Note time.
26. **Set 'Press and hold' Right → Digital assistant** [`HOLD-004`]. Wait. Note time.
27. **Check/uncheck one ANC mode in the press-and-hold rotation list** [`HOLD-005`] (e.g. remove
    'Off' from the cycle). Wait. Note time.

#### Group H — Audio & volume settings
28. **Toggle 'Mono audio' on/off** [`AUDIO-001`]. Wait. Note time.
29. **Toggle 'Volume EQ' on/off** [`AUDIO-002`]. Wait. Note time.
30. **Shift the 'Volume balance' slider** [`AUDIO-003`]. Wait. Note time. (Per `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §1 this
    is stored locally on the earbuds themselves — a good candidate for a confirmable
    persistent write.)

#### Group I — Firmware & device info
31. **Tap the 'Firmware up to date' check** (manual) [`FW-001`]. Wait. Note time.
32. **Open 'More settings'** to view firmware version per component [`FW-002`] — may trigger a
    status query. Wait. Note time.
33. **View serial numbers per component** (same screen) [`FW-003`]. Wait. Note time.
34. **View connection status** ("Earbud status: Connected") [`FW-004`]. Wait. Note time.

#### Group J — In-ear detection & case sounds
35. **Toggle 'In-ear detection' on/off** [`INEAR-001`]. Wait. Note time.
36. **Toggle case sound 'Earbuds replaced' on/off** [`CASE-001`]. Wait. Note time.
37. **Toggle case sound 'Other notifications' on/off** [`CASE-002`]. Wait. Note time.

#### Group K — Find My Buds (high-priority, see tip above)
38. **Play sound on Left earbud** [`FIND-001`]. Wait. Note time.
39. **Play sound on Right earbud** [`FIND-002`]. Wait. Note time.
40. **Play sound on Case** [`FIND-003`]. Wait. Note time.
41. **Play sound on both earbuds simultaneously** [`FIND-004`]. Wait. Note time.

**Before treating the framing hypothesis as confirmed from #38 alone: cross-check
against 2–3 semantically different commands** — e.g. an ANC mode change (Group B) and one
EQ preset or slider write (Group D/E). One matching frame is a promising HYPOTHESIS, not
a FACT (`PROJECT_RULES.md` §1: FACT requires repeated confirmation, not a single
observation). For each of these frames, check specifically whether the same structural
elements line up across all of them: magic/group byte(s), length-field semantics,
channel/message-ID position, where the protobuf payload boundary falls, whether a
checksum is present, and whether a Message Group/Code reading (§2 Hypothesis A) explains
the leading bytes as consistently as a magic-byte reading (§2 Hypothesis B) would. If all
sampled commands share the same structure, that's what actually raises confidence toward
FACT — not the Left-earbud frame resembling the spec's worked example on its own.

#### Group L — Passive/automatic observation windows
These aren't taps — they're deliberate waiting periods to catch background/automatic app
traffic per `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §3. **Log explicit boundaries for each window, not just a
single timestamp** — otherwise settling traffic from whatever you did right before the
window starts is hard to distinguish from genuinely spontaneous traffic during it. For
each item below, note: **observation start** (when you stopped touching anything),
**any event of interest during the window**, **observation end**, the **Bluetooth
connection state** (connected/reconnecting/idle), and the **app's foreground/background
state**.
42. **Idle wait with the app open** [`BATT-001`], ~60s right after connecting, without touching
    anything — intended to catch the "battery status notification on every reconnect"
    behavior. Note the start time, and leave a clean ~10s gap after the preceding
    connect action before this window starts, so reconnect-settling traffic doesn't
    bleed into what you're trying to observe as spontaneous.
43. **Force-close and reopen the app** [`OBS-001`] — intended to catch any status query the app sends
    on launch. Note the exact time of reopening as the window start, and note when you
    consider the window over (e.g. ~30–60s after reopening, or once traffic visibly
    settles).

After finishing a group, pull the bugreport once (§3) — you don't need a separate
bugreport per action, just clean timestamps to slice the single log into segments
afterward.

#### Group R — Forced GATT re-discovery (occasional, not part of the normal run-through)
Android caches the GATT service/characteristic database per bonded device and does **not**
rediscover it on a normal reconnect — every other group in this guide runs against an
already-discovered device, so none of them exercise this. Run Group R only when you
specifically need fresh GATT evidence: the first time, and again after any firmware update
that might change the GATT layout (`PROTOCOL.md` §0.1). This is what resolves `GATT-001`
in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` on the Pixel 7a specifically — that Test-ID previously
had no dedicated Pixel 7a scenario.

**Why not just reconnect normally:** there is no non-hacky way to force a GATT refresh from
outside an app — the only programmatic option (`BluetoothGatt.refresh()`) is a hidden `@hide`
API requiring reflection, which `AGENTS.md` §3 bans for this project's own code. Removing the
bond is the reliable, non-hacky way to force it.

1. **Remove the bond via system Bluetooth settings** — Settings → Connected devices → Pixel
   Buds Pro 2 → Forget. Use the **system settings**, not the Pixel Buds app's own "Forget"
   button — `CAP-001`'s `FINDINGS.md` §6 found the app-level Forget did not fully clear a BLE-level
   association, so it isn't reliable for this purpose. Confirm the device no longer appears
   in the paired-devices list.
2. Work through §2 (enable HCI snoop, restart Bluetooth/reboot) as usual.
3. **Reconnect using a generic BLE tool (e.g. nRF Connect), not the official Pixel Buds app**
   [`GATT-001`] — install it on the Pixel 7a if not already present. This is a deliberate exception to
   this guide's usual preference for the official app: here the goal is a human-readable
   view of the discovered GATT structure on screen, not attributing a specific proprietary
   command, so a generic scanner is more useful, not less rigorous. The HCI snoop log
   captures everything at the system level regardless of which app initiates the connection.
4. **Isolate the whole connect-and-discover sequence as one action window**: note the exact
   time you tap Connect in the BLE tool, and the time the service/characteristic list
   finishes populating on screen. Expect a full classic SSP pairing exchange to also appear
   in this capture (removing the bond clears both classic and BLE state for a dual-mode
   device) — this is a welcome bonus `PAIR-001`/`PAIR-002` data point, not a sign anything
   went wrong; it just isn't the primary target of this Group.
5. If the tool supports it, manually **read or subscribe to specific characteristics of
   interest** once they're identified on screen (e.g. anything near handle `0x0f2a` or the
   `0x0c0X` cluster flagged in `CAP-002`'s `FINDINGS.md` §4/§7) — each such action is its own
   isolated event, noted with its own timestamp, the same way a UI tap is treated elsewhere
   in this guide.
6. Extract and analyze as usual (§3, §5). In Wireshark, filter specifically for the ATT
   opcodes that perform GATT discovery: `btatt.opcode == 0x10` / `0x11` (Read By Group Type
   Request/Response — primary service discovery) and `btatt.opcode == 0x08` / `0x09` (Read By
   Type Request/Response — characteristic discovery). Their responses contain the
   handle-to-UUID mapping this Group exists to capture.

### 4.2 Pixel 9a (GrapheneOS) — secondary/validation session

No app-driven commands are possible here, so this session focuses on connection-level
and passive behavior:

1. **Pairing** [`PAIR-001`], via system Bluetooth settings (Settings → Connected devices → Pair new
   device). Note the start time precisely — this is the most information-dense part of
   this session (bonding handshake, initial service discovery), and like Group A #1 it's
   one user action producing a multi-frame burst, not something to isolate frame by frame.
2. **Idle observation** [`BATT-003`] — once connected, wait ~30–60 seconds without touching anything.
   This can reveal spontaneous status frames the Buds send unprompted. Note the
   **observation start** (once you stop touching anything, leaving a clean gap after the
   pairing burst settles), the **observation end**, and confirm the **connection state**
   stayed `connected` throughout — so this window isn't later confused with settling
   traffic from item 1.
3. **Open the Bluetooth device detail screen** for the Buds in system settings [`GATT-001`] (this can
   trigger GATT service/characteristic discovery on some Android versions).
4. **Disconnect and reconnect** once, as its own isolated pair of actions [`PAIR-003`], to observe
   both a clean teardown and a reconnection to an already-bonded device (useful for
   validating `ARCHITECTURE.md` §6/§7 resilience assumptions).

Pull the bugreport (§3) the same way.

### 4.3 Hardware Actions (either phone)

**Source:** `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` sections 2 (User Actions via the Case & Buds) and 4
(Automatic Actions Initiated by the Hardware). These actions are grouped separately from
4.1/4.2 because they aren't tied to a specific phone — a tap on the bud, or the case
button, behaves the same regardless of which device is connected. Run this group on
**whichever phone you already have connected and logging** at the time; if you want to
compare hardware behavior across both OSes for a specific action, repeat that one action
on the other phone as its own short session rather than redoing the whole group.

Same rhythm as before: **wait ~5s → note the exact time → perform the action → wait
~5–10s → move to the next action.**

#### Group M — Case & wear state
1. **Open the charging case lid** [`CASE-003`]. Wait. Note time.
2. **Remove Left earbud from the case** [`CASE-004`]. Wait. Note time.
3. **Remove Right earbud from the case** [`CASE-005`]. Wait. Note time.
4. **Insert Left earbud into the ear** [`INEAR-002`]. Wait. Note time.
5. **Insert Right earbud into the ear** [`INEAR-003`]. Wait. Note time.
6. **Place buds back in the case and close the lid** [`CASE-006`]. Wait. Note time. (Expected to
   terminate the active Bluetooth Classic connection — good for validating
   `ARCHITECTURE.md` §6/§7 disconnect handling.)

#### Group N — Touch gestures
7. **Tap once** on a bud [`TOUCH-002`]. Wait. Note time.
8. **Double-tap** on a bud [`TOUCH-003`]. Wait. Note time.
9. **Triple-tap** on a bud [`TOUCH-004`]. Wait. Note time.
10. **Swipe forward** on a bud (volume up) [`TOUCH-005`]. Wait. Note time.
11. **Swipe backward** on a bud (volume down) [`TOUCH-006`]. Wait. Note time.
12. **Press and hold** on a bud [`TOUCH-007`]. Wait. Note time. (Behavior depends on the per-earbud
    configuration set in §4.1 Group G — note which mode was active when you test this.)

#### Group O — Head gestures
Requires 'Head gestures' enabled (§4.1 Group F).
13. **Nod** [`HEAD-002`] (simulating answering a call, or a text reply if 'Spoken notifications' is
    on). Wait. Note time.
14. **Shake** [`HEAD-003`] (simulating rejecting a call/dismissing a text reply). Wait. Note time.

#### Group P — Voice & case button
15. **Start speaking** with Conversation Detection on [`CONV-002`] (§4.1 Group C), to trigger the
    detection event. Wait. Note time.
16. **Hold the case button for 30 seconds** [`CASE-007`] (case open, buds inside, plugged into power)
    — ⚠️ this is a confirmed **full factory reset**, not just pairing mode (per
    `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §2). Do this deliberately, last, and only once you're ready to
    re-pair from scratch — it will also reset the Find My Device link on the Pro 2.
    If you do trigger it, capture the subsequent re-pair as its own isolated session
    right afterward [`PAIR-002`] (same rhythm as Group A #1) — this gives a second,
    from-true-factory-state bonding capture to compare against Group A's lightweight
    forget-and-re-pair baseline. It is optional and not a prerequisite for anything
    else in this guide — Group A's lightweight baseline is sufficient on its own for
    every other group.
17. **(Open question, see `PROTOCOL_NOTES.md` §7)** Try a shorter/different press [`CASE-008`] on the
    case button to see if it triggers pairing mode without a full reset. No officially
    confirmed duration exists for this — treat your own finding here as
    `[VERIFIED-LOCAL]` material for `PROTOCOL_NOTES.md` once confirmed.

#### Group Q — Automatic hardware behavior (observation, not action)
These are waiting periods to catch spontaneous hardware-initiated traffic per
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §4 — nothing to tap, just capture while the condition holds. As with
Group L, log explicit **observation start** and **observation end** boundaries, not just
a single timestamp, so this traffic isn't confused with settling traffic from whatever
preceded the window.

For items 19–20 below, "nothing found" is not a single outcome — record which of these
three actually happened, since they mean different things for protocol reconstruction:
- **Local behavior confirmed + Bluetooth traffic observed** → record the frame(s) as
  `[VERIFIED-LOCAL]` per the usual process (§5 step 4).
- **Local behavior confirmed, but no Bluetooth traffic in the log despite a clean
  observation window** → this is itself a positive finding, not an absence of one — it's
  evidence *for* a purely on-device implementation. Record it in `PROTOCOL_NOTES.md` as
  such (e.g. 🟢 FACT: "no wire-visible signal observed on trigger, N attempts"), don't
  just leave the row blank.
- **Inconclusive** — you're not sure the local trigger actually fired (e.g. unclear
  whether the clap was loud enough, or the environment change was large enough), or the
  observation window was contaminated by other traffic. This does **not** support either
  conclusion above; note it as 🔴 unconfirmed and, if practical, retry with a clearer
  trigger before drawing any conclusion.

18. **Passive BLE scan while the case is closed and idle** [`BATT-002`/`BATT-003`] — intended to catch the Fast
    Pair Battery Notification advertisement (`PROTOCOL_NOTES.md` §4.3 Option A) without
    any active RFCOMM connection. This doesn't require the buds to be connected to the
    capturing phone at all — any nearby scan should do, per the spec. **This is a
    one-off, manual reverse-engineering capture, not a template for the app.** The
    production app's own BLE scanning stays governed separately, and more narrowly, by
    the bounded exception in `AGENTS.md` §7 / `DECISIONS.md` ADR-006 — this experiment
    does not authorize a broader scanning implementation than that.
19. **Trigger a loud, sudden sound near the buds while worn** [`LOUD-001`] (e.g. clap sharply nearby)
    to attempt to observe Loud Noise Protection engaging (`PROTOCOL_NOTES.md` §4.2/§7) —
    confirm you actually noticed the local effect (e.g. audible volume dip) before
    concluding anything about the Bluetooth traffic (or lack of it); see the three-way
    outcome guidance above.
20. **Move between distinctly different acoustic environments while worn** [`ADAPT-002`] (e.g. quiet
    room → street) to attempt to observe Adaptive Audio adjusting
    (`PROTOCOL_NOTES.md` §4.2/§7) — same guidance as #19: confirm the local effect first,
    then classify the Bluetooth-traffic outcome using the three categories above.

---

## 5. Analyzing in Wireshark

1. Open the extracted `btsnoop_hci.log` file directly in Wireshark
   (File → Open, or drag-and-drop — no special import steps needed, Wireshark recognizes
   the BTSnoop file format natively). This gets you HCI/L2CAP/RFCOMM/ATT-level framing
   for free — it does **not** mean Wireshark understands the `libmaestro` payload itself.
   There is no dissector for a proprietary, undocumented protocol, so the actual command
   bytes inside an RFCOMM frame will show up as opaque raw data; decoding what they mean
   is manual work you do against the hypotheses in `PROTOCOL_NOTES.md` §2 (see step 4
   below) — that manual decoding is the actual point of this whole procedure.
2. Useful filters to narrow the view:
   - `bthci_acl` — general ACL-level Bluetooth traffic.
   - `btrfcomm` — RFCOMM traffic specifically (this is where `libmaestro` frames live).
   - `btatt` — GATT/ATT traffic (relevant for the BLE battery-service investigation).
   - `btle` — BLE Link Layer traffic (advertising/scan reports) — this is where the Fast
     Pair Battery Notification capture (Group Q #18) shows up, **not** `btrfcomm`.
   - `bluetooth.addr == <buds MAC>` — restrict to the Buds' Bluetooth address once you've
     identified it (visible in the pairing frames or in Wireshark's Bluetooth device
     list under View → Bluetooth Devices).
3. Use the **timestamp column** together with your own action log (§1.3, §4) to identify
   which frame(s) correspond to which action. Wireshark's relative or UTC time display
   (View → Time Display Format) should be set to whatever you used when noting action
   times, to avoid an offset mismatch. **Since the capture-side wait time (§4) is a
   heuristic, not a guarantee:** if a frame appears just after your noted "wait ~5–10s"
   window for one action but before the next action's timestamp, treat it as a probable
   late response to the earlier action rather than automatically attributing it to
   whatever came next — check payload plausibility against both candidates before
   deciding, and note the ambiguity in `PROTOCOL_NOTES.md` if it can't be resolved from
   the log alone.
4. For each identified command frame:
   - Note the raw bytes (right-click → Copy → ...as Hex Stream is fastest).
   - **If it's an RFCOMM frame** (`btrfcomm` — an app-triggered command, or the Find My
     Buds/Ring action): compare the structure against the envelope hypothesis in
     `PROTOCOL_NOTES.md` §2 (magic byte, length field, channel/msg ID, payload, checksum).
   - **If it's a BLE advertisement** (`btle` — the Battery Notification, Group Q #18):
     compare it against the Fast Pair Battery Notification structure in
     `PROTOCOL_NOTES.md` §4.3 Option A (flags, account key data, battery-level-length/type
     byte, then the 3 battery bytes) instead. This is a **different, unrelated** structure
     — it is not RFCOMM traffic and was never expected to match the §2 envelope
     hypothesis; don't force-fit it there or record a false "doesn't match" finding.
   - Record the confirmed values back into `PROTOCOL_NOTES.md` §4.1's opcode table
     (RFCOMM frames) or §4.3 (battery), mark the entry `[VERIFIED-LOCAL]` with today's
     date, and raise its confidence to 🟢. Include the Test-ID from
     `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (e.g. `ANC-001`) and this session's `CAP-NNN` ID in
     that note — this is what closes the evidence chain in
     `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §0.1. Also add the Test-ID to this session's row in
     the Capture Index (§9) Test(s) column if it isn't there yet.
5. If a frame doesn't match the expected envelope shape at all, don't force-fit it —
   note it as an open question (`PROTOCOL_NOTES.md` §7) rather than recording a guess as
   fact.

---

## 6. Notes & Gotchas

- **Log rotation:** the in-device snoop buffer is finite; very long or idle-heavy
  sessions can push earlier frames out before you extract them. Keep sessions focused
  (§2, §4).
- **A Bluetooth restart is required** for the HCI snoop logging setting to take effect —
  per Android's own source documentation, not just this guide's experience (this project
  has no capture statistics of its own yet to say how common empty-log failures are).
  Reboot by default (§2 step 5); a plain toggle only if you've confirmed it works
  reliably on your specific phone.
- **`adb root` will not work** on either phone for this purpose — both stock Pixel
  firmware and GrapheneOS are, as set up for this project, normal production/non-rooted
  builds, on which `adbd` cannot run as root. This is about the current, as-configured
  state of these two phones, not a claim that Pixel hardware can't be rooted by other
  means — don't waste time trying to root a device just for this; `adb bugreport` is the
  supported path and is sufficient.
- **Path differences across Android versions:** the internal bugreport path for the
  snoop log has moved between `FS/data/log/bt/` and `FS/data/misc/bluetooth/logs/` across
  Android releases — if your expected path is empty, search the archive by filename
  instead of assuming the path is wrong.
- **GrapheneOS specifics:** no additional developer-mode dance is required beyond
  standard USB debugging — `adb bugreport` functions the same as on stock Android for
  this purpose on a non-rooted production build.
- **Firmware version drift:** if automatic Buds firmware updates are still enabled (see
  the earlier discussion on checking/disabling them in the official app), a capture done
  today may not match one done next month. Always note the confirmed firmware version
  (`PROTOCOL_NOTES.md` §0/§5) alongside any capture you take.
- **Two Buds, one identity:** the case, left bud, and right bud may all appear as
  distinct addresses/roles in some traffic (notably GATT). Don't assume all frames come
  from a single logical peer — check `bluetooth.addr` per frame when in doubt.

---

## 7. FAQ

**Q: Do I need to do a full capture session every time I want to check one new command?**
No — you can pull a fresh bugreport (§3) any time after enabling logging (§2), covering
just that one new isolated action. You don't need to redo pairing or previously-confirmed
commands each time.

**Q: The zip from `adb bugreport` doesn't contain a Bluetooth folder at all — what now?**
This has been reported when the HCI snoop toggle wasn't actually active during the
session (see the "A Bluetooth restart is required" note in §6) — re-check §2 step 5,
reboot the phone this time even if you used a plain toggle before, reproduce the action,
and pull a fresh bugreport. If it still doesn't appear, search the whole archive by
filename (`find . -iname "*btsnoop*"`) rather than assuming a fixed path.

**Q: Can I use Wireshark's live capture (the "Android Bluetooth Btsnoop" USB interface)
instead of pulling a bugreport each time?**
In principle yes on some Android versions, but on modern Android the live-capture port
is frequently not exposed by default, requiring workarounds (e.g. manually forwarding a
local port via `nc` over `adb shell`) that add complexity without much benefit for this
project's needs. The `adb bugreport` method in §3 is simpler and reliable across both
phones — stick with it unless you have a specific reason to need live capture.

**Q: Is it safe/expected that `adb root` fails on GrapheneOS?**
Yes — this is expected, deliberate behavior on any non-rooted production Android build,
not a GrapheneOS-specific restriction, and not something to work around by rooting the
device. Rooting significantly weakens the security model this whole project is built
around (see `AGENTS.md` §2), so `adb bugreport` remains the correct approach even though
it's slightly more roundabout than a direct file pull.

**Q: My Pixel 7a capture shows encrypted-looking or unreadable payload bytes even inside
the RFCOMM stream — is that expected?**
Most likely, **not** actual encryption — a few distinct things get conflated under
"looks encrypted," worth separating:
- **HCI-boundary visibility:** an HCI snoop log is captured at the boundary between the
  host (Android's Bluetooth stack) and the controller (the Bluetooth chip). On typical
  implementations, Bluetooth link-layer encryption/decryption happens *in the
  controller*, below this boundary — so ACL data crossing HCI toward the host is usually
  already plaintext at the L2CAP/RFCOMM level, not still link-encrypted.
- **What this means in practice:** raw, unfamiliar-looking bytes inside an RFCOMM frame
  are, for this project, far more likely to just be serialized protobuf data you don't
  recognize yet (protobuf's binary encoding often looks fairly random to the eye without
  the schema) than genuine Bluetooth link-layer encryption. Don't assume "unreadable"
  means "encrypted, therefore unusable" — it's probably exactly the payload you're
  trying to reverse engineer.
- **When it might genuinely be encryption:** if bytes are unreadable *consistently*,
  across many otherwise-isolated captures, and never resolve into anything matching the
  envelope hypothesis no matter how you slice the offsets, that's a better (though still
  not certain) signal of something below Wireshark's visibility — e.g. a
  higher-layer/application-level encryption scheme, rather than standard Bluetooth link
  encryption, which per the point above shouldn't normally still be present at this
  layer.
- If this happens consistently, note it as an open question in `PROTOCOL_NOTES.md` §7 —
  including which of the above you've ruled out — rather than assuming the envelope
  hypothesis in §2 is simply wrong.

**Q: Should I capture on the GrapheneOS phone first, since that's my actual target
platform?**
No — do the Pixel 7a (official app) capture first. It's the only source of frames you
can positively attribute to a specific command, since you're the one triggering them
through the known, official UI. The GrapheneOS capture is for validating
connection/pairing/passive behavior on the target OS, not for discovering new commands.

**Q: How do I know when I've captured "enough" and can stop?**
When every row in `PROTOCOL_NOTES.md` §4.1's opcode table and every open question in §7
that's answerable via traffic analysis (as opposed to, say, firmware version lookup) has
moved from 🔴/🟡 to 🟢 with a `[VERIFIED-LOCAL]` tag — or when you've made a deliberate,
documented decision to leave a specific low-priority item (e.g. Find My Buds) unverified
for now.

---

## 8. After Capturing: What to Update

Every capture session should end with at least one of these:

- [ ] `PROTOCOL_NOTES.md` §2 — envelope structure fields confirmed/corrected.
- [ ] `PROTOCOL_NOTES.md` §4.1 — opcode table rows filled in, confidence raised to 🟢.
- [ ] `PROTOCOL_NOTES.md` §4.3 — battery approach confirmed or ruled out (now split into
      Option A: BLE advertisement, and Option B: RFCOMM Message Stream — see the current
      version of that section).
- [ ] `PROTOCOL_NOTES.md` §5 — firmware version and any version-specific differences
      logged.
- [ ] `PROTOCOL_NOTES.md` §7 — open questions resolved, or new ones added if the capture
      revealed something unexpected.

Treat an capture session that doesn't result in at least one of the above as incomplete —
either the action wasn't actually isolated/identifiable, or something in the setup (§2,
§6) needs revisiting before the next attempt.

---

## 9. Capture Index

Every capture session gets a row here, added at or immediately after extraction
(§3) — this is the authoritative index that `PROTOCOL_NOTES.md` and
`PROTOCOL.md` evidence entries reference back to, per `PROJECT_RULES.md`
rule 3 (traceability) and rule 14 (capture metadata). A capture that never
gets a row here is, for evidence purposes, effectively lost — don't skip this
step, even for a quick one-action session.

**ID format:** `CAP-NNN`, zero-padded, strictly incrementing, never reused —
if a capture turns out to be unusable, mark it `discarded` in Status rather
than deleting the row or reassigning its number to a later capture.

| ID | Date | Phone | Android | Buds FW | App version | Group(s) | Test(s) | Purpose | Bugreport file | Extracted log | Status |
|---|---|---|---|---|---|---|---|---|---|---|---|
| `CAP-001` | 2026-08-09 | Pixel 7a | TBD | TBD | TBD | Z, A, B, M | `PAIR-001`, `CASE-003`, `CASE-004`, `CASE-005`, `ANC-001`, `ANC-002`, `ANC-003`, `ANC-004`, `CASE-006` | Pipeline validation; scope grew beyond Z into full pairing baseline + all 4 ANC modes + case/bud handling | `captures/2026-08-09_08-51-00_08-52-20-Group_Z/btsnoop_hci.log` | same file (already `btsnooz`-extracted) | analyzed — see `FINDINGS.md` in that folder; ANC-opcode attribution inconclusive due to lack of action isolation |
| `CAP-002` | 2026-08-09 | Pixel 7a | TBD | TBD | TBD | A | `PAIR-001`, `PAIR-002`, `CASE-001` | Fresh pairing/bonding baseline (deleted stored link key first) through the Pixel Buds app's first-run setup flow (Fast Pair save-to-account, CDM permission, Device details load) | `captures/2026-08-09_17-04-53_17-06-46-Group_A/btsnoop_hci.log` (sliced from a shared, non-restarted ~8h20m snoop log — see that folder's `EVENT-NOTES.md` process note) | same file | analyzed — see `FINDINGS.md` in that folder; Fast Pair Message Stream Device Information group tentatively identified (channel 2/DLCI 0x04); no RFCOMM traffic found during app setup/Device-details load; HFP channel opened but no AT-command traffic observed (contrast with `CAP-001`) |
| `CAP-003` | 2026-08-10 | Pixel 7a | TBD | TBD | nRF Connect (generic BLE tool), official app took over partway | R | `PAIR-001`, `PAIR-002` | Forced GATT rediscovery attempt (pairing removed via system settings, connected via nRF Connect instead of the official app) to resolve `CAP-002`'s open `0x0f2a`/`0x0c0X` handle UUIDs; classic pairing captured as a bonus data point | `captures/2026-08-10_20-59-16_21-00-37-Group_R/btsnoop_hci.log` (short, freshly-restarted log, no slicing needed) | same file | analyzed — see `FINDINGS.md` in that folder; **primary goal not achieved** — Android's GATT database cache survived the pairing removal, so zero `Read By Group Type`/characteristic-discovery traffic occurred; `0x0f2a`/`0x0c0X` UUIDs still unresolved; new handle `0x0f28` found (polled every ~60s, value `0x31`); reinforces that RFCOMM channel numbers are session-local while GATT handles are stable across sessions |

**Column notes:**

- **Phone** — `Pixel 7a` (primary, official app) or `Pixel 9a` (secondary,
  GrapheneOS), per the two-device setup described at the top of this
  document.
- **Group(s)** — the letter(s) from §4 (Z, A–Q, R — Z and R are special-purpose
  groups that intentionally sort outside the A–Q run-through: Z is
  pipeline-validation, always done first; R is the occasional forced-GATT-discovery
  procedure, done only when needed) covered in this session; one
  bugreport pull can cover several groups if captured as one continuous
  logging session (§4.1).
- **Test(s)** — the `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` Test-ID(s) actually
  exercised in this session (e.g. `ANC-001, ANC-002`) — this is what a
  `PROTOCOL_NOTES.md` finding should ultimately trace back through: finding →
  this row's capture ID + frame number → the Test-ID here → the catalog entry
  in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`. Usually a subset of everything the
  Group(s) column's scenario covers, since not every attempt succeeds cleanly.
- **Status** — one of: `captured` (extracted, not yet reviewed), `analyzed`
  (reviewed in Wireshark per §5, findings recorded per §8), `promoted` (a
  finding from this capture has been written into `PROTOCOL_NOTES.md` /
  `PROTOCOL.md` with a `[VERIFIED-LOCAL]` tag), `discarded` (unusable —
  note why, e.g. in an extra remarks column if needed).
- Reference a capture from `PROTOCOL_NOTES.md` / `PROTOCOL.md` by its ID (e.g.
  "confirmed in `CAP-001`, frame 214") rather than by date or description, so
  the reference survives even if this row's description is later edited.