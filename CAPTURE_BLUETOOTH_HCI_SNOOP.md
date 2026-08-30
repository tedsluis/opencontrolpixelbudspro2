# CAPTURE_BLUETOOTH_HCI_SNOOP.md — Bluetooth HCI Snoop Capture Guide

**Purpose:** step-by-step procedure to capture, extract, and analyze Bluetooth HCI traffic
between a phone and the Pixel Buds Pro 2, in order to fill in the confidence-rated
placeholders in `PROTOCOL.md` (magic bytes, checksum, opcode table, battery
approach). This document is the *how* — the relevant `CAP-NNN-FINDINGS.md` and
`PROTOCOL.md` are the *what we found*.

Two devices are used, for two different purposes:

| Device | Role | Why |
|---|---|---|
| Pixel 7a, Android 17 (with Google Play Services), official Pixel Buds app | **Primary capture** | Only source of actual `libmaestro` command frames (ANC toggle, EQ write) triggered on demand |
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

> **PROPOSAL — pending maintainer approval:** step 3 (raw file) and step 4 (`btsnooz.py` fallback)
> are not interchangeable in practice — **always check step 3 first and prefer it whenever the raw
> file is present**, and **always record which path a given capture actually used** in that
> capture's own `CAP-NNN-FINDINGS.md` (per `PROJECT_RULES.md` rule 11's reproducibility
> requirement) — a capture's usability for anything beyond short control-frame sequencing may
> depend on this choice, not just on what happened during the session itself. See
> `DESKRESEARCH_FINDINGS.md`'s 2026-08-28 entry for the full 5-session comparison and evidence
> (still 🟡 HYPOTHESIS, not a controlled test).

---

## 4. Capture Procedure — Isolate Every Action

**Groups A–Q below are the main run-through capture scenarios — efficient bundles of related
actions to run in one session — not the project's official test record; Z, R, S, T, U, V, W, X,
and Y are additional special-purpose groups added later (pipeline validation, occasional/targeted
follow-ups), each documented in its own subsection below.** Each numbered item is
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

**⚠️ Priority tip (superseded 2026-08-14 — original text kept below, not deleted, per
`PROJECT_RULES.md` §3):** the original advice below was to prioritize Group K to confirm/refute
the RFCOMM framing hypothesis. That hypothesis question is now resolved for ANC specifically
(`PROTOCOL.md` §4.1 — the official Fast Pair Message Stream, Group `0x08`, byte- and
timing-verified against `CAP-001`), which is what Group K's own frame would have helped confirm.
**The new top priority is Group T (EQ command isolation, below)** — EQ's command channel is the
one major control feature this project still needs to attribute, now that the earlier assumption
"EQ probably shares ANC's channel" no longer holds (`PROTOCOL.md` §2.3's 2026-08-14 addendum). The
same caution applies to Group T's result as applied here to Group K: a superficial byte-pattern
match on a single capture is a HYPOTHESIS, not a FACT — see Group T's own cross-command check.

**Original tip (2026-08-08 or earlier, kept for the record):** this document's own verification
methodology flags the "Play sound on Left earbud" action (group K below) as a specifically
valuable, low-risk target — its frame can be directly compared against the Fast Pair Message
Stream spec's own worked example (`0x04 0x01 ...` for a ring action) to confirm or refute the
framing hypothesis in `PROTOCOL.md` §2.1. If you only have time for a short session, prioritize
group K.
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
   per `PROTOCOL.md` §4.1). Wait. Note time.
5. **ANC → Transparency** [`ANC-004`]. Wait. Note time.

> **Repeat recommendation (2026-08-14), lower priority than Group T:** despite ANC's opcode
> reaching 🟢 FACT, one sub-question stays open — `CAP-001`'s *first two* taps (Transparency,
> Off) have no corresponding `0x12` "Set ANC state" frame anywhere in that log
> (`PROTOCOL.md` §4.1, `CAP-001-FINDINGS.md` §5), possibly because the ANC row was still visibly
> greyed out (UI-state realization, not a genuine command) rather than a missed capture. A clean
> repeat of this Group with the same isolation requirements as before — but starting only once the
> ANC row is already fully active/enabled on screen, and doing a genuine **single** tap per
> window rather than the six-in-a-row `CAP-001` did — would confirm whether every real tap
> produces a `0x12` frame, and re-confirm the bit-mapping (`PROTOCOL.md` §4.1's table) on a clean,
> single-tap capture before it's relied on for implementation.

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

> **Correction, 2026-08-14 (does not invalidate running this Group, but changes what to expect):**
> removing the bond does **not** reliably clear Android's cached GATT service/characteristic
> database. `CAP-003-FINDINGS.md` §1 found the cache survived bond removal (zero live
> `Read By Group Type` discovery traffic), and `CAP-004-FINDINGS.md` §6 independently reconfirmed
> the same negative result — two for two attempts using this method. **Group W below is the only
> method that has an untried, stronger chance of working** (`pm clear com.android.bluetooth`, or
> discovery from a phone that has never connected to this device before) — see also
> `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `GATT-001` row. Group R is kept in this guide because it is
> still useful for its **bonus data** (a fresh classic pairing exchange, step 4 below) even though
> it is no longer expected to force live GATT discovery on its own; if your goal is specifically
> the GATT handle→UUID mapping, run Group W instead, not this Group.

Android caches the GATT service/characteristic database per bonded device and does **not**
rediscover it on a normal reconnect — every other group in this guide runs against an
already-discovered device, so none of them exercise this. `GATT-001`
in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` needs Group W, not this Group, for its Pixel-7a-specific
resolution (see the correction above).

**Why not just reconnect normally:** there is no non-hacky way to force a GATT refresh from
outside an app — the only programmatic option (`BluetoothGatt.refresh()`) is a hidden `@hide`
API requiring reflection, which `AGENTS.md` §3 bans for this project's own code. Removing the
bond was the first thing tried to force it, but per the correction above, it is **not** reliable
for that purpose — it remains useful only as described above.

1. **Remove the bond via system Bluetooth settings** — Settings → Connected devices → Pixel
   Buds Pro 2 → Forget. Use the **system settings**, not the Pixel Buds app's own "Forget"
   button — `CAP-001`'s `CAP-001-FINDINGS.md` §6 found the app-level Forget did not fully
   clear a BLE-level association, so it isn't reliable for this purpose. Confirm the device no
   longer appears in the paired-devices list.
2. Work through §2 (enable HCI snoop, restart Bluetooth/reboot) as usual.
3. **Reconnect using a generic BLE tool (e.g. nRF Connect), not the official Pixel Buds app** —
   install it on the Pixel 7a if not already present. This is a deliberate exception to
   this guide's usual preference for the official app: here the goal is a human-readable
   view of the discovered GATT structure on screen, not attributing a specific proprietary
   command, so a generic scanner is more useful, not less rigorous. The HCI snoop log
   captures everything at the system level regardless of which app initiates the connection.
   **Do not expect this to actually trigger live discovery** (see the correction above) — treat
   any live discovery traffic that does appear as a bonus, not the expected outcome.
4. **Isolate the whole connect-and-discover sequence as one action window**: note the exact
   time you tap Connect in the BLE tool, and the time the service/characteristic list
   finishes populating on screen (nRF Connect's UI may still show a service list here even
   without live discovery traffic — it can be serving its own cached view; do not assume an
   on-screen list means fresh wire traffic occurred, check the log). Expect a full classic SSP
   pairing exchange to also appear in this capture (removing the bond clears both classic and
   BLE state for a dual-mode device) — this **is** this Group's actual, reliable bonus data
   point, per the correction above: a `PAIR-001` capture (bond removal via system settings only
   — no factory reset happens in this Group, so this can never be genuine `PAIR-002` data), not a
   `GATT-001` one.
5. If the tool supports it, manually **read or subscribe to specific characteristics of
   interest** once they're identified on screen (e.g. anything near handle `0x0f2a` or the
   `0x0c0X` cluster flagged in `CAP-002`'s `CAP-002-FINDINGS.md` §4/§7) — each such action
   is its own isolated event, noted with its own timestamp, the same way a UI tap is treated
   elsewhere in this guide.
6. Extract and analyze as usual (§3, §5). In Wireshark, filter specifically for the ATT
   opcodes that perform GATT discovery: `btatt.opcode == 0x10` / `0x11` (Read By Group Type
   Request/Response — primary service discovery) and `btatt.opcode == 0x08` / `0x09` (Read By
   Type Request/Response — characteristic discovery). Their responses contain the
   handle-to-UUID mapping this Group exists to capture.

#### Group S — Google Play Services disabled, no Pixel Buds app (occasional, not part of the normal run-through)
**Purpose:** isolate whether the Fast Pair Message Stream traffic identified in `CAP-002`
(`CAP-002-FINDINGS.md` §3 — the `[Group][Code][Length][Value]`-framed channel carrying, among other
things, the `"Revision 6"` string) is **Buds-initiated** (would still appear identically) or
**driven by Google Play Services' phone-side Fast Pair/Nearby logic** (would disappear or
change). This is directly relevant to this project's Zero-GMS goal (`AGENTS.md` §1,
`PROJECT.md`): if the Buds only send this data because GMS asks for it in a specific way,
this project's own app will need to replicate that GMS-side behavior, not just listen
passively.

**Setup (validated manually before capturing, per the maintainer's own testing):** with the
Pixel Buds app uninstalled and Google Play Services disabled (Settings → Apps → see all
apps → Google Play Services → Disable; `adb shell pm disable-user --user 0 com.google.android.gms`
is the scriptable equivalent), pairing the Buds via system Bluetooth settings still succeeds,
but the Fast Pair "Connect" half-sheet (the purple dialog with the Buds product image,
normally shown when GMS is enabled) does **not** appear — confirming the half-sheet itself is
GMS-driven UI, not app- or Buds-driven. Whether the underlying RFCOMM Message Stream traffic
is also GMS-driven, or independent of it, is what this capture is for — it is **not**
assumed by the setup validation above.

1. Confirm the Pixel Buds app is uninstalled and Google Play Services is disabled, per the
   setup note above.
2. Work through §2 (enable HCI snoop, restart Bluetooth/reboot) as usual.
3. **Pair via system Bluetooth settings** [`GFPS-001`] — there is no app and no Fast Pair
   half-sheet to use here, so this is the only pairing path available. Note whether the
   device was already unpaired (e.g. left over from a prior Group R session) or whether this
   capture also includes a fresh bonding handshake — record this explicitly in this session's
   `CAP-004-EVENT-NOTES.md`, since it changes what else can be read from the same capture (see Group
   R step 4 for the equivalent bonus-`PAIR-001` note — Group S is also bond-removal-only, never a
   factory reset, so this is `PAIR-001` data too, not `PAIR-002`).
4. **Isolate the whole pair-and-settle sequence as one action window**: note the exact
   connect-tap time and when the connection visibly settles.
5. Extract and analyze as usual (§3, §5). Specifically check whether a channel/DLCI carrying
   the same `[Group][Code][Length][Value]` framing as `CAP-002` §3 appears at all, and if so,
   whether the same fields (e.g. Code `0x09`'s value) match. **Do not assume an outcome before
   analyzing** — either result (present or absent) is a real, useful finding for the open
   question above, not a "pass" or "fail" of this Group.

#### Group T — EQ command isolation (occasional, not part of the normal run-through; **current top priority**, added 2026-08-14)
**Why this replaces the earlier ANC-first priority tip:** ANC's command channel is now confirmed
(`PROTOCOL.md` §4.1 — the official Fast Pair Message Stream, Group `0x08`, DLCI 0x04), which also
retires the earlier assumption that EQ "probably shares ANC's channel." EQ's command channel is
therefore still completely open, and is now the single highest-priority capture target for this
project's original implementation goal (see the corrected note at the end of §4.1's intro, below
Group S).
1. **Change EQ preset: Bass Boost** [`EQP-002`], as a single isolated action — same rhythm as
   Group D (≥10s silence before, ≥10s after) — but run **alone** this time, not bundled with the
   other seven preset taps back-to-back the way Group D does it, so the capture has only one
   candidate command frame to attribute.
2. After the window settles, run a second, independent, isolated window with a **structurally
   different** EQ action — **Adjust EQ slider: Bass** [`EQS-004`] — before drawing any conclusion,
   matching Group K's own "don't promote off one matching frame" discipline.

**Analysis instructions for this Group specifically:**
1. First check whether any official Fast Pair extension page not yet checked (beyond Hearable
   Controls, Device Action, SASS, Device Information — see `PROTOCOL.md` §2.3/§4.1) documents an
   EQ-shaped message group — the same kind of check that resolved ANC.
2. If no official page covers it, inspect **every** open channel's traffic in a tight window
   around the tap: DLCI 0x04 (Message Stream, watch for any Group not already accounted for),
   DLCI 0x02 (`libmaestro`'s confirmed Pigweed-HDLC framing, `PROTOCOL.md` §2.2a — decode the
   Address/Control fields of every "Sent" sub-frame near the tap and check whether one appears
   specifically at that moment, distinct from its otherwise-undecoded baseline traffic), and
   DLCI 0x08 (the private `[Group][Code][Length][Value]` envelope, `CAP-004-FINDINGS.md` §5a).
3. Apply Group K's cross-command check: a single matching frame from step 1's two actions is a
   HYPOTHESIS, not a FACT — the two actions exist specifically so there's a second, structurally
   different sample to check against.

#### Group U — DLCI 0x08 Group `0x04` Code `0x12` liveness/event bracket (occasional, added 2026-08-14)
**Purpose:** `CAP-004-FINDINGS.md` §5a's Task 5 found Code `0x12` alternates its value on an
irregular interval — 🟡 HYPOTHESIS of a free-running liveness/sequence-parity bit, not yet tested
against a real physical event. This Group also closes `INEAR-004`'s existing gap (no capture
scenario exists yet for "bud removed from ear, not placed back in case" — flagged in
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §9).
1. **Remove one worn earbud and hold it in hand (not placing it back in the case)** [`INEAR-004`],
   with the connection otherwise idle. Note the exact time. Keep logging Code `0x12` occurrences
   on DLCI 0x08 for at least 60s before and 60s after this moment.
2. **Close the case lid while the connection is still active** (buds elsewhere, e.g. still worn or
   in hand — not the normal "buds back in case" `CASE-006` sequence) [`OBS-003`]. Note the exact
   time.
3. **A deliberate, multi-minute idle wait** (≥3 minutes, connection active, nothing touched)
   [`OBS-003`]. Note the start and end time.

**Analysis:** for each bracketed moment, check whether the Code `0x12` `field1` 2↔3 alternation
breaks, skips, or pauses at that moment (supports event-driven) or continues unperturbed on its
own irregular cadence (supports a free-running counter) — per `CAP-004-FINDINGS.md` §5a's own
framing of this open question.

#### Group V — In-call HFP/SCO audio behavior (occasional, added 2026-08-14)
**Purpose:** `CAP-002-FINDINGS.md` §5 found zero `AT+` traffic anywhere outside `CAP-001`'s own
pairing-time handshake across a full 8+ hour log, and `CAP-001-FINDINGS.md` §6 Task 6 ruled out
any SCO/eSCO HCI event in all four captures to date — both findings converge on the same missing
scenario: **none of the four captures so far ever contains an actual phone call**, the one trigger
that would exercise HFP's Service Level Connection setup and channel 5/DLCI 0x0a's audio path.
1. **Place or receive an actual phone call** while connected to the Buds [`CALL-001`]. Note the
   exact start and end time of the call.
2. Optionally, during the call, trigger a deliberate audio-routing action (e.g. switch the audio
   output device) as a bonus data point — note its time separately.

**Analysis:** check whether a full HFP AT-command SLC handshake reappears (matching `CAP-001`'s
shape) and whether channel 5/DLCI 0x0a carries any payload this time, or whether an HCI-level
`Setup Synchronous Connection` (`0x0428`) / `Enhanced Setup Synchronous Connection` (`0x043D`) /
`Synchronous Connection Complete` (`0x2C`) event appears at all (none has, in any capture to date
— `CAP-001-FINDINGS.md` §6 Task 6).

#### Group W — Stronger GATT cache-busting for live service discovery (occasional, added 2026-08-14)
**Purpose:** `CAP-002`, `CAP-003`, and `CAP-004` all failed to trigger a live `Read By Group Type`
GATT discovery against the Buds — Android's cached GATT database survived bond removal in every
attempt so far (`CAP-003-FINDINGS.md` §1, re-confirmed in `CAP-004-FINDINGS.md` §6). Group R's
"remove the bond" method is **not** a reliable trigger, contrary to what this document and
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `GATT-001` row previously stated (corrected here and there —
see the 2026-08-14 note on `GATT-001` below and in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §5).
1. **Option (a) — clear the Bluetooth system app's cache directly:**
   `adb shell pm clear com.android.bluetooth` [`GATT-001`]. **Risk, note before running:** this
   clears **all** of the phone's Bluetooth pairings and state, not just the Buds' — expect to
   have to re-pair every other Bluetooth device on that phone afterward. Confirm this is
   acceptable before running it. **Also clear nRF Connect's own app cache/data**
   (`adb shell pm clear no.nordicsemi.android.mcp` — check the actual package name installed;
   Settings → Apps → nRF Connect → Storage → Clear storage/cache works too) **if using it to
   reconnect** — `CAP-004-FINDINGS.md` §6 found nRF Connect's on-screen service list may be
   served from the app's own cached data rather than fresh wire discovery, which would make the
   UI *look* like discovery succeeded even when the Android Bluetooth stack itself never emitted
   a live `Read By Group Type` exchange. Clearing only `com.android.bluetooth` without also
   clearing nRF Connect's own cache risks a false-positive "it worked" read from the UI that the
   HCI snoop log won't actually back up — always confirm against the log (§5), never the tool's
   UI alone.
2. **Option (b) — discover from a phone that has never connected to this device before:** run the
   discovery from the Pixel 9a (GrapheneOS), which has not previously run any app or tool against
   this specific Buds unit [`GATT-001`]. If using nRF Connect (or any BLE scanner) on this phone
   too, use a fresh install or clear its cache first, same reasoning as option (a) — "never
   connected before" should mean the *tool's* cache is clean, not just the *device's* pairing
   state.
3. Isolate the connect-and-discover sequence as its own window, same as Group R step 4.

#### Group X — Battery-level discrepancy bracket (occasional, added 2026-08-14)
**Purpose:** `CAP-001-FINDINGS.md` §3 found `AT+CIND?`'s `battchg=3` (≈60%) and `AT+BIEV=2,100`
(100%) disagreeing at the same moment — unresolved whether either indicator actually tracks a
real battery-level change over time.
1. Start logging well before an expected, natural battery-level decline (e.g. at the start of a
   normal day of use), keeping the connection active/idle rather than disconnecting between
   checks.
2. Periodically (e.g. every 15–30 minutes) note the wall-clock time — no action needed, both
   indicators are expected to update on their own per their respective triggers.
3. End the session after a natural, visible battery-percentage drop has occurred on screen.
   **Can be combined with Group V's session** if a phone call happens to occur naturally within
   the same window — no need to force this, note it if it happens.

**Analysis:** extract every `AT+CIND?` (`battchg`) and `AT+BIEV=2,...` value across the session
with its timestamp, and compare both trends against the on-screen battery percentage over the
same window.

> **Note on Group S's repeatability (added 2026-08-14):** `CAP-004-FINDINGS.md` §8 item 4 flags
> that Group S's Cross-Transport-Key-Derivation bonding result (§2 there) might be an artifact of
> nRF Connect's early BLE connection rather than of GMS being disabled — `CAP-004` used nRF
> Connect first, deviating from this Group's own system-settings-only procedure (see the
> "⚠️ Procedure deviation" note in `CAP-004-EVENT-NOTES.md`). A repeat of Group S **exactly as
> described above** (system Bluetooth settings only, no BLE tool at any point) would isolate this
> confound. The core `GFPS-001` result (§4a/§4b there) is not expected to change, only the §2
> CTKD-vs-classic-SSP bonding-mechanism finding.
>
> **Resolved 2026-08-26 (`CAP-012`):** the repeat was captured, exactly as described above (no
> BLE tool at any point, independently verified both on screen and on the wire —
> `CAP-012-FINDINGS.md` §2). **Confirmed: classic SSP, not CTKD** — this isolates the confound
> cleanly, as predicted. As also predicted, the `GFPS-001` channel-topology result (DLCI 0x04
> never opens) reproduces `CAP-004`'s; **however, `CAP-012`'s own log turned out to be severely
> ACL-truncated (`btsnooz`-fallback extraction), so the payload-content half of `GFPS-001` — which
> `CAP-004`'s untruncated log *could* answer — is inconclusive here, not a second confirming data
> point** (`CAP-012-FINDINGS.md` §1/§4). A further repeat with a working, non-truncated snoop log
> would still be worthwhile for that reason alone.

> **Note on Group A's repeatability, optional (added 2026-08-14).** `CAP-001-FINDINGS.md` §6 found
> a BLE link and a still-valid link key both existing *before* the on-screen "Forget" tap and
> before the case was reopened — unresolved whether "Forget" fully clears prior association state.
> A repeat of Group A that starts HCI snoop logging **before** any association with the device
> exists at all (e.g. immediately after a phone restart, before ever opening the case or any Buds
> app) would isolate this.
>
> **Update (2026-08-26): `CAP-013` attempted this repeat — did not achieve it, primary question
> still open.** Logging did not actually start before the clearing action (which itself was a
> phone-wide "Reset Bluetooth & Wi-Fi," not a single-device "Forget") — the log's first frame lands
> 2m21s after that action, and after the case-open/pair-button/device-list-tap sequence too
> (`CAP-013-FINDINGS.md` §0). The primary question above remains 🔴 OPEN. `CAP-013` did resolve the
> secondary `PAIR-004` question (fresh SSP, no key reuse, for whatever bonding state was active when
> its own logging window began) — see `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `PAIR-004` row. **PROPOSAL
> — pending maintainer approval:** a further repeat is still needed, this time with logging
> verifiably started before the clearing action itself (e.g. enable HCI snoop logging immediately
> after a phone restart, before touching Bluetooth settings at all) — proposed as `CAP-031` (next
> free ID per `id_registry.csv`, not yet assigned/registered).
>
> **Update (2026-08-27), `CAP-031`, PROPOSAL — pending maintainer approval: second consecutive
> attempt, still did not achieve it — primary question still open.** This session used a genuine
> narrow, per-device "Forget" (screenshot-confirmed, unlike `CAP-013`'s broader reset) and added a
> live snoop-log file-size-polling check during recording specifically to catch `CAP-013`'s
> failure mode — but the log's first frame (06:06:37.16) still lands 66s *after* the on-screen
> Forget tap (06:05:31), and after the case-open/pair-button/first-scan-attempt sequence too
> (`CAP-031-FINDINGS.md` §0). The primary question remains 🔴 OPEN, untested a third time.
> `CAP-031` did reconfirm the secondary `PAIR-004` question (fresh SSP, no key reuse — a sixth
> confirming instance) and, as bonus negative results, found that neither of `CAP-013`'s two other
> anomalies (DLCI 0x02's ~61s-delayed open, the unattributed second BLE link) reproduce this
> session (`CAP-031-FINDINGS.md` §5/§6) — both look like single-session artifacts, not recurring
> behavior. **A fourth attempt is still needed** for the primary question, this time verifying the
> snoop log's own *content* freshness (last-frame timestamp against a live wall clock) immediately
> before the Forget tap, not just the log file's size (`CAP-031-FINDINGS.md` §8's proposed root
> cause and method).
>
> **Update (2026-08-27), `CAP-032`, PROPOSAL — pending maintainer approval: fourth attempt
> succeeded.** This session was extracted via the raw BTSnoop file path (§3 step 3) instead of the
> `btsnooz.py` fallback used for the three prior attempts — the resulting log is genuinely
> untruncated (`frame.cap_len == frame.len` for all 2,455 frames) and its first frame
> (18:29:45.72) lands **~58s *before*** the on-screen Forget tap (18:30:42), and ~30s before the
> video itself starts. **The primary question is now answered for this session: 🟢 no BLE link or
> valid classic link key existed for the Buds anywhere in the covered pre-Forget window**
> (`CAP-032-FINDINGS.md` §0.3) — zero classic connection events, exactly one LE connection (to an
> unrelated random-address device, not the Buds), and a `Delete Stored Link Key` issued at the
> Forget tap's own moment reporting `Num_Keys_Deleted = 0`. **This does not reproduce `CAP-001`'s
> original finding** — it shows the opposite, a clean counter-example rather than a confirmation
> or a refutation of that session's own result; `CAP-001`'s session-specific puzzle (why *that*
> session had residual state) remains independently open. `PAIR-004`'s secondary question is
> reconfirmed a seventh time (fresh SSP, no key reuse). This also supports (one data point, not yet
> independently isolated) the hypothesis that the `btsnooz`-fallback extraction path itself — not
> the individual session — was responsible for `CAP-012`/`CAP-013`/`CAP-031`'s truncation; see the
> proposed §3 addendum below.

#### Group Y — BLE-only connection isolation for the `0x0044` notification burst (occasional, added 2026-08-20)
**Purpose:** `CAP-016-FINDINGS.md` §11 found a 73-frame `Handle Value Notification` burst on BLE
ATT handle `0x0044` (connection handle `0x0002`, 23 of the 73 frames containing a recurring
`0xfea9` byte-pair marker), confined to a ~29s window right after the BLE link forms and before
the classic link exists. Not yet isolated from a physical trigger: every capture that shows this
burst to date also has a bud removal/insertion happening nearby in the same session, so it's
unconfirmed whether the burst is caused by the BLE link forming at all, or by the bud/case action
that happened to coincide with it in those captures.
1. **Enable Bluetooth and let the phone's BLE link to the already-paired Buds form on its own**
   [`GATT-002`] — do **not** touch the buds or the case at any point before, during, or for at
   least 60s after the link forms. The buds/case stay exactly wherever they already are (worn, in
   hand, or already sitting open/closed) — this is the opposite of Group M's procedure, which
   deliberately triggers bud/case events; here the goal is a clean BLE-connect with *zero*
   physical events nearby to correlate against. Note the exact time Bluetooth was (re-)enabled or
   the BLE link began forming.
2. Keep the observation window open and logging for at least 60s past that point, per the usual
   observation-window discipline (§4.1 Group L's boundary-logging convention: note observation
   start, any event of interest, and observation end explicitly).

**Analysis:** filter the resulting log for `btatt.opcode==0x1b and btatt.handle==0x0044`. If the
burst still appears despite no bud/case action anywhere in or near the window, that's a clean
positive result narrowing `0x0044`'s trigger to "BLE link establishment alone" (`PROTOCOL.md` §6).
If it does not appear, that's an equally useful negative result, pointing back toward a bud/case
physical action as the real trigger after all — either outcome closes this open question, per the
three-way outcome guidance already used for Group Q's items 19–20.

#### Group AA — SDP UUID branch isolation for `gbm.a()`'s "default internal rfcomm socket" path (occasional, added 2026-08-30)

**Purpose:** `REVERSE_ENGINEERING.md`'s `gbm`/`fzd` entries and `DECISIONS.md` ADR-018 found the
companion app's own decompiled code (`gbm.java:35-43`) picks between two internal RFCOMM sockets
depending on which of two SDP UUIDs is present in the discovered set: "pigweed" (`25e97ff7-...`,
confirmed = DLCI 0x02 in every capture so far) or "default" (`3a046f6d-...`, never observed on the
wire anywhere — a raw-byte scan of all 26 capture files this project has, in every format
(`*btsnoop_hci.log`, `*btsnooz_hci.log`, both nRF Connect logs), found zero occurrences in either
byte order; see `REVERSE_ENGINEERING.md`'s `gbm` entry Open questions). This Group covers the two
hypotheses that are safely testable with a single Pixel Buds Pro 2 unit on its current firmware
(`release_5.203`):

- **`SDP-001`:** `gbm.a()`'s discovered-UUID set may come from an SDP browse whose content depends
  on *who* triggers it — the OS's own default pairing flow, vs. the companion app's own
  `fetchUuidsWithSdp()` call (`fxm.java:110`, which only fires when the HID UUID `0x1124` is
  absent). Every existing capture has the app already open, so this hasn't been isolated.
- **`SDP-002`** (opportunistic): a firmware update might be what changes which UUID gets advertised
  at all — the "pigweed"/"default" pair reads plausibly as a pre-/post-migration artifact. Only
  testable the next time an actual OTA update becomes available, same caveat as `FWUPD-001`/
  `FWUPD-002`.

**Explicitly out of scope for this Group** — not safely or practically testable, recorded here so
they aren't silently retried: deliberately downgrading firmware (unsupported by Google, real
bricking risk — see `WORKSTATION_PREPARATIONS.md`'s Disaster Recovery section; never attempt this),
and testing against a different physical unit or hardware generation (the maintainer owns one
Pixel Buds Pro 2 — `PROJECT_RULES.md` §8's own-hardware scope). If `SDP-001`/`SDP-002` both come
back negative, the leading remaining explanation is that the "default" branch is unreachable on any
currently-shipping `release_5.203`+ unit — a static-analysis question (checking for a
firmware-version gate elsewhere in the APK, `APK_REVERSE_ENGINEERING_PROCEDURE.md` §4), not a
capture question, and out of this Group's scope.

1. **[`SDP-001`]** Force-stop the Pixel Buds companion app first (`Settings → Apps → Pixel Buds →
   Force stop`), so it cannot react to the pairing at all. Start Bluetooth HCI snoop logging (§2).
   "Forget" the Buds via system Bluetooth settings only — the same safe, repeatable action already
   used for `PAIR-001` (Group A #1), **not** `CASE-007`'s factory reset. Re-pair via system
   Bluetooth settings' "Pair new device" flow only. Note the exact time pairing completes.
2. Keep observing for at least 60s after bonding completes, **without** opening the companion app
   or touching the buds/case — this is the window where the pre-app-fetch UUID set (if one exists
   and differs from the baseline) would show up.
3. **Still `SDP-001`, second half of the same session:** now open the companion app normally and
   let it connect as usual — this reproduces every prior capture's baseline in the same log, for a
   direct in-session before/after comparison rather than relying on a separate session.
4. Pull the bugreport (§3) once, at the end.
5. **[`SDP-002`], opportunistic, separate session whenever a firmware update becomes available:**
   repeat the SDP-observation half of steps 1–2 (a fresh SDP browse doesn't require a fresh bond —
   simply reconnecting is enough if already bonded) immediately before installing the update, and
   again immediately after it completes and the Buds reconnect.

**Analysis:** pre-filter by address first, per §13's CLI-hygiene rule, then to `btsdp`:
`tshark -r CAP-NNN-btsnoop_hci.log -Y "bluetooth.addr == 04:00:6e:cf:6e:07 and btsdp" -T fields -e frame.number -e frame.time_relative -e btsdp.data_element.value.uuid_128 -e btsdp.protocol.channel`
(exact command already validated against `CAP-001`/`CAP-002`/`CAP-032` in `REVERSE_ENGINEERING.md`'s
`gbm` entry). Three-way outcome, matching Group Y's own guidance above:
- **Positive:** the pre-app-open window (step 2) shows the "default" UUID (`3a046f6d-...`, either
  byte order) instead of or alongside "pigweed" — closes the open question; write it up per the
  usual FACT/HYPOTHESIS discipline (`PROJECT_RULES.md` §1) before touching `PROTOCOL.md`.
- **Negative:** still only "pigweed" (or no `btsdp` traffic at all, e.g. if the OS doesn't run a
  full SDP browse without the app prompting it) — consistent with every capture to date; narrows
  the explanation toward `SDP-002` or a static-analysis-only dead-code question rather than a
  UI-timing artifact.
- **`SDP-002` positive:** a before/after firmware comparison shows the advertised UUID changing —
  directly explains the two-UUID code as a migration artifact.

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
17. **(Open question, see `PROTOCOL.md` §6)** Try a shorter/different press [`CASE-008`] on the
    case button to see if it triggers pairing mode without a full reset. No officially
    confirmed duration exists for this — treat your own finding here as
    `[VERIFIED-LOCAL]` material for the relevant `CAP-NNN-FINDINGS.md` once confirmed.

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
  evidence *for* a purely on-device implementation. Record it in the relevant
  `CAP-NNN-FINDINGS.md` as such (e.g. 🟢 FACT: "no wire-visible signal observed on
  trigger, N attempts"), don't just leave the row blank.
- **Inconclusive** — you're not sure the local trigger actually fired (e.g. unclear
  whether the clap was loud enough, or the environment change was large enough), or the
  observation window was contaminated by other traffic. This does **not** support either
  conclusion above; note it as 🔴 unconfirmed and, if practical, retry with a clearer
  trigger before drawing any conclusion.

18. **Passive BLE scan while the case is closed and idle** [`BATT-002`/`BATT-003`] — intended to catch the Fast
    Pair Battery Notification advertisement (`PROTOCOL.md` §4.3 Option A) without
    any active RFCOMM connection. This doesn't require the buds to be connected to the
    capturing phone at all — any nearby scan should do, per the spec. **This is a
    one-off, manual reverse-engineering capture, not a template for the app.** The
    production app's own BLE scanning stays governed separately, and more narrowly, by
    the bounded exception in `AGENTS.md` §7 / `DECISIONS.md` ADR-006 — this experiment
    does not authorize a broader scanning implementation than that.
19. **Trigger a loud, sudden sound near the buds while worn** [`LOUD-001`] (e.g. clap sharply nearby)
    to attempt to observe Loud Noise Protection engaging (`PROTOCOL.md` §4.5/§6) —
    confirm you actually noticed the local effect (e.g. audible volume dip) before
    concluding anything about the Bluetooth traffic (or lack of it); see the three-way
    outcome guidance above.
20. **Move between distinctly different acoustic environments while worn** [`ADAPT-002`] (e.g. quiet
    room → street) to attempt to observe Adaptive Audio adjusting
    (`PROTOCOL.md` §4.5/§6) — same guidance as #19: confirm the local effect first,
    then classify the Bluetooth-traffic outcome using the three categories above.

---

## 5. Analyzing in Wireshark

1. Open the extracted `CAP-*-btsnoop_hci.log` file directly in Wireshark
   (File → Open, or drag-and-drop — no special import steps needed, Wireshark recognizes
   the BTSnoop file format natively). This gets you HCI/L2CAP/RFCOMM/ATT-level framing
   for free — it does **not** mean Wireshark understands the `libmaestro` payload itself.
   There is no dissector for a proprietary, undocumented protocol, so the actual command
   bytes inside an RFCOMM frame will show up as opaque raw data; decoding what they mean
   is manual work you do against the hypotheses in `PROTOCOL.md` §2 (see step 4
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
   deciding, and note the ambiguity in this session's `CAP-NNN-FINDINGS.md` if it can't
   be resolved from the log alone.
4. For each identified command frame:
   - Note the raw bytes (right-click → Copy → ...as Hex Stream is fastest).
   - **If it's an RFCOMM frame** (`btrfcomm` — an app-triggered command, or the Find My
     Buds/Ring action): compare the structure against the envelope hypothesis in
     `PROTOCOL.md` §2 (magic byte, length field, channel/msg ID, payload, checksum).
   - **If it's a BLE advertisement** (`btle` — the Battery Notification, Group Q #18):
     compare it against the Fast Pair Battery Notification structure in
     `PROTOCOL.md` §4.3 Option A (flags, account key data, battery-level-length/type
     byte, then the 3 battery bytes) instead. This is a **different, unrelated** structure
     — it is not RFCOMM traffic and was never expected to match the §2 envelope
     hypothesis; don't force-fit it there or record a false "doesn't match" finding.
   - Record the confirmed values in this session's `CAP-NNN-FINDINGS.md` first, then
     promote them into `PROTOCOL.md` §4.1's opcode table (RFCOMM frames) or §4.3
     (battery), mark the entry `[VERIFIED-LOCAL]` with today's
     date, and raise its confidence to 🟢. Include the Test-ID from
     `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (e.g. `ANC-001`) and this session's `CAP-NNN` ID in
     that note — this is what closes the evidence chain in
     `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §0.1. Also add the Test-ID to this session's row in
     the Capture Index (§9) Test(s) column if it isn't there yet.
5. If a frame doesn't match the expected envelope shape at all, don't force-fit it —
   note it as an open question (`PROTOCOL.md` §6) rather than recording a guess as
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
  (`PROTOCOL.md` §0/§0.1) alongside any capture you take.
- **Two Buds, one identity:** the case, left bud, and right bud may all appear as
  distinct addresses/roles in some traffic (notably GATT). Don't assume all frames come
  from a single logical peer — check `bluetooth.addr` per frame when in doubt.
- **`btsnooz` ACL truncation isn't limited to BLE/GATT — added 2026-08-26, `CAP-012`.**
  `CAP-017-FINDINGS.md` §2 already documented severe (~15-byte) ACL truncation on a BLE/GATT
  session's log; `CAP-012-FINDINGS.md` §1 shows the identical phenomenon on a **classic BR/EDR**
  session (RFCOMM/SDP/HFP data, not GATT) — confirmed the same way, `frame.cap_len` capped at a
  small value regardless of `frame.len` for every `HCI_ACL` packet. This is the signature of §3
  step 4's `btsnooz.py`-from-bugreport fallback path being used instead of step 3's raw,
  untruncated `btsnoop_hci.log` — check `capinfos` (`Packet size limit: inferred: ...`) right
  after extraction, before spending analysis time on a session, and prefer the step 3 raw path
  whenever it's present in the bugreport archive.

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
- If this happens consistently, note it as an open question in `PROTOCOL.md` §6 —
  including which of the above you've ruled out — rather than assuming the envelope
  hypothesis in §2 is simply wrong.

**Q: Should I capture on the GrapheneOS phone first, since that's my actual target
platform?**
No — do the Pixel 7a (official app) capture first. It's the only source of frames you
can positively attribute to a specific command, since you're the one triggering them
through the known, official UI. The GrapheneOS capture is for validating
connection/pairing/passive behavior on the target OS, not for discovering new commands.

**Q: How do I know when I've captured "enough" and can stop?**
When every row in `PROTOCOL.md` §4.1's opcode table and every open question in §6
that's answerable via traffic analysis (as opposed to, say, firmware version lookup) has
moved from 🔴/🟡 to 🟢 with a `[VERIFIED-LOCAL]` tag — or when you've made a deliberate,
documented decision to leave a specific low-priority item (e.g. Find My Buds) unverified
for now.

---

## 8. After Capturing: What to Update

Every capture session should end with at least one of these, recorded first in that
session's `CAP-NNN-FINDINGS.md` and then promoted directly into `PROTOCOL.md`
(`PROTOCOL_NOTES.md` has been retired — there is no intermediate buffer):

- [ ] `PROTOCOL.md` §2 — envelope structure fields confirmed/corrected.
- [ ] `PROTOCOL.md` §4.1 — opcode table rows filled in, confidence raised to 🟢.
- [ ] `PROTOCOL.md` §4.3 — battery approach confirmed or ruled out (now split into
      Option A: BLE advertisement, and Option B: RFCOMM Message Stream — see the current
      version of that section).
- [ ] `PROTOCOL.md` §0.1 — firmware version and any version-specific differences
      logged.
- [ ] `PROTOCOL.md` §6 — open questions resolved where this capture resolves them.

**Mandatory, not optional:** every new 🔴 OPEN QUESTION recorded in a
`CAP-NNN-FINDINGS.md` (or `DESKRESEARCH_FINDINGS.md`) MUST also be copied into
`PROTOCOL.md` §6, in the matching Framing/Commands & schemas/Behavior
subsection, even if it doesn't get resolved this session. An open question
that stays only in the findings doc is effectively invisible to future
sessions, since §6 is the consolidated list agents are expected to check
(`AGENTS.md` §0.1) — it must not depend on someone remembering to go re-read
every individual findings file. (Example of this rule being applied: the
UI-baseline-vs-wire-baseline firmware distinction, first noted in §0.1's
2026-08-14 addendum but not copied into §6 until 2026-08-15 — don't repeat
that gap for new open questions going forward.)

Treat an capture session that doesn't result in at least one of the above as incomplete —
either the action wasn't actually isolated/identifiable, or something in the setup (§2,
§6) needs revisiting before the next attempt.

---

## 9. Capture Index

Every capture session gets a row here, added at or immediately after extraction
(§3) — this is the authoritative index that `CAP-NNN-FINDINGS.md` and
`PROTOCOL.md` evidence entries reference back to, per `PROJECT_RULES.md`
rule 3 (traceability) and rule 14 (capture metadata). A capture that never
gets a row here is, for evidence purposes, effectively lost — don't skip this
step, even for a quick one-action session.

**ID format:** `CAP-NNN`, zero-padded, strictly incrementing, never reused —
if a capture turns out to be unusable, mark it `discarded` in Status rather
than deleting the row or reassigning its number to a later capture. Register
every new `CAP-NNN` in `id_registry.csv` (repo root) when adding its row here
— `scripts/lint_docs.py` checks every reference against that registry, which
is how the 2026-08-18 `CAP-005`/`CAP-007`/`CAP-010` ID-reuse incident (see
`CHANGELOG.md`) would have been caught mechanically instead of found by hand.

| ID | Date | Phone | Android | Buds FW | App version | Group(s) | Test(s) | Purpose | Bugreport file | Extracted log | Status |
|---|---|---|---|---|---|---|---|---|---|---|---|
| `CAP-001` | 2026-08-09 | Pixel 7a | 17 | release_5.203 | 1.0.955078536 | Z, A, B, M | `PAIR-001`, `CASE-003`, `CASE-004`, `CASE-005`, `ANC-001`, `ANC-002`, `ANC-003`, `ANC-004`, `CASE-006` | Pipeline validation; scope grew beyond Z into full pairing baseline + all 4 ANC modes + case/bud handling | `captures/CAP-001-2026-08-09_08-51-00_08-52-20-Group_Z/CAP-001-btsnoop_hci.log` | same file (already `btsnooz`-extracted) | analyzed — see `CAP-001-FINDINGS.md` in that folder; ANC-opcode attribution inconclusive due to lack of action isolation |
| `CAP-002` | 2026-08-09 | Pixel 7a | 17 | release_5.203 | 1.0.955078536 | A | `PAIR-001`, `CASE-001` | Fresh pairing/bonding baseline (deleted stored link key first) through the Pixel Buds app's first-run setup flow (Fast Pair save-to-account, CDM permission, Device details load) | `captures/CAP-002-2026-08-09_17-04-53_17-06-46-Group_A/CAP-002-btsnoop_hci.log` (sliced from a shared, non-restarted ~8h20m snoop log — see that folder's `CAP-002-EVENT-NOTES.md` process note) | same file | analyzed — see `CAP-002-FINDINGS.md` in that folder; Fast Pair Message Stream Device Information group tentatively identified (channel 2/DLCI 0x04); no RFCOMM traffic found during app setup/Device-details load; HFP channel opened but no AT-command traffic observed (contrast with `CAP-001`) |
| `CAP-003` | 2026-08-10 | Pixel 7a | 17 | release_5.203 | nRF Connect (generic BLE tool), then official app v1.0.955078536 (took over partway) | R | `PAIR-001` | Forced GATT rediscovery attempt (pairing removed via system settings, connected via nRF Connect instead of the official app) to resolve `CAP-002`'s open `0x0f2a`/`0x0c0X` handle UUIDs; classic pairing captured as a bonus data point | `captures/CAP-003-2026-08-10_20-59-16_21-00-37-Group_R/CAP-003-btsnoop_hci.log` (short, freshly-restarted log, no slicing needed) | same file | analyzed — see `CAP-003-FINDINGS.md` in that folder; **primary goal not achieved** — Android's GATT database cache survived the pairing removal, so zero `Read By Group Type`/characteristic-discovery traffic occurred; `0x0f2a`/`0x0c0X` UUIDs still unresolved; new handle `0x0f28` found (polled every ~60s, value `0x31`); reinforces that RFCOMM channel numbers are session-local while GATT handles are stable across sessions |
| `CAP-004` | 2026-08-11 | Pixel 7a | 17 | release_5.203 | nRF Connect (BLE phase), then system Bluetooth settings (no Pixel Buds app at any point — uninstalled) | S | `PAIR-001`, `CASE-003` | `GFPS-001` — Google Play Services disabled + Pixel Buds app uninstalled, to isolate whether `CAP-002`'s Fast Pair Message Stream traffic is Buds-initiated or GMS-driven; bonus classic pairing captured (procedure deviated from Group S's system-settings-only description — nRF Connect was used first) | `captures/CAP-004-2026-08-11_06-22-36_06-25-12-Group_S/CAP-004-btsnoop_hci.log` (contains unrelated background Fitbit Charge 6 traffic, excluded — see `CAP-004-FINDINGS.md` §1) | same file | analyzed — see `CAP-004-FINDINGS.md` in that folder; **mixed `GFPS-001` result**: `CAP-002` §3's channel-2/DLCI-0x04 TLV content (Model ID, "Revision 6", etc.) is absent (channel 2 never opens) — GMS-and/or-app-dependent, unresolved which (GMS was disabled **and** the official app was uninstalled together in this session, a confound not yet isolated — see `CAP-004-FINDINGS.md` §4a); but `CAP-001`'s channel-4/DLCI-0x08 content (`google-pixel-buds-pro-v1`, `Europe/Amsterdam`) reappears unchanged — not GMS-dependent (this one isn't confounded, since it's a presence-despite-both-removed result, not an absence); classic bonding used Cross-Transport Key Derivation (LE Secure Connections → classic key), not classic SSP as in `CAP-002`/`CAP-003`; new, unidentified Message Stream Groups `0x04`/`0x05`/`0x09` found; nRF Connect's cached GATT service list gives named candidate services (Google Fast Pair Service `0xFE2C`, Accessory Non-Owner Service, Device Information) for the still-open `0x0f2a`/`0x0c0X` handle questions |
| `CAP-005` | 2026-08-15 | Pixel 7a | 17 | release_5.203 | 1.0.955078536 | T | `EQP-002`, `EQS-004` | **Highest priority.** EQ command isolation — single isolated EQ preset change ("Heavy bass"), then a second isolated EQ Bass-slider drag + Save, to find EQ's command channel now that it's known not to share ANC's (`PROTOCOL.md` §4.1/§2.3) | `captures/CAP-005-2026-08-15_15-02-31_15-03-45-Group_T/CAP-005-btsnoop_hci.log` | same file | analyzed — see `CAP-005-FINDINGS.md` in that folder; **goal achieved (🟡 HYPOTHESIS level):** both actions isolate cleanly to DLCI 0x02 (`libmaestro`'s Pigweed `pw_hdlc` channel, `PROTOCOL.md` §2.2a) only — DLCI 0x04/0x08 silent in both windows; a nested, protobuf-tag-consistent envelope decodes down to a 5×`float32` band-gain quintet, with only the Bass field changing (+3.0 on preset, then −4.1 on the drag); the slider drag additionally revealed a second, later "Save"-tap wire burst (outer field 18) distinct from the live-drag burst (outer field 16), not anticipated by this capture's own event notes — first EQ-attributable content decoded on any channel, still 🟡 not 🟢 (single capture, field-to-band mapping inferred from one changed field) |
| `CAP-006` | 2026-08-15 | Pixel 7a | 17 | release_5.203 | 1.0.955078536 | B (repeat) | `ANC-001`, `ANC-002`, `ANC-003`, `ANC-004` | Clean, single-tap-per-window repeat of Group B to resolve `CAP-001`'s first-two-taps-have-no-`0x12`-frame open sub-question and re-confirm the ANC bit-mapping before implementation | `captures/CAP-006-2026-08-15_17-23-49_17-25-06-Group_B/CAP-006-btsnoop_hci.log` | same file | analyzed — see `CAP-006-FINDINGS.md` in that folder; **goal achieved, ADR-009 blocker resolved:** all 4 isolated single taps produced a matching `0x12` "Set ANC state" frame (exactly 4 `0x12` frames in the whole 233s log, one per tap, zero extras/misses), each within ~1.3s of its video-observed tap; bit-mapping re-confirmed byte-for-byte against `PROTOCOL.md` §4.1 (`0x08`=ANC, `0x20`=Off, `0x40`=Adaptive, `0x80`=Transparency); `CAP-001`'s 2/6 miss rate does not reproduce under isolated single-tap conditions — `FrameEncoder` implementation for this command proposed as unblocked, pending maintainer sign-off/`DECISIONS.md` update |
| `CAP-007` | 2026-08-16 | Pixel 7a | 17 (⚪ assumed, not visible on screen this capture) | release_5.203 | n/a (Android system Bluetooth "Device details" page, not the companion app) | U | `INEAR-004`(partial), `CASE-004`, `OBS-003` | DLCI 0x08 Group `0x04` Code `0x12` liveness/event bracket — bud-removed-from-case, case-closed-while-active, and multi-minute idle brackets, to test whether the alternating value is event-driven or a free-running counter (`CAP-004-FINDINGS.md` §5a Task 5) | `captures/CAP-007-2026-08-16_09-14-10_09-17-57-Group_U/CAP-007-btsnoop_hci.log` | same file | analyzed — see `CAP-007-FINDINGS.md` in that folder; **Group U's own question answered (🟢 FACT): Code `0x12` is Buds-initiated (Rcvd-only), fires on DLCI 0x08 channel-(re)open and also autonomously after a ~184s idle gap with no channel churn — neither purely reactive nor purely free-running**; value cycles `0x02`/`0x03`/`0x04` (corrects `CAP-004`'s "2↔3" characterization); **original in-ear-mapping goal not achieved** — no earbud was ever inserted into an ear in this session (only removed from the open case), and that one physical event produced no value change on any DLCI (clean negative result); case-lid-close bracket also produced zero wire traffic (clean negative result) |
| `CAP-008` | 2026-08-26 | Pixel 7a | 17 (⚪ assumed) | release_5.203 | 1.0.955078536 (⚪ assumed, not shown on screen this session) | V | `CALL-001` | First capture of an actual phone call — resolves whether HFP AT-command SLC setup reoccurs (`CAP-002-FINDINGS.md` §5) and whether channel 5/DLCI 0x0a carries any payload or SCO/eSCO HCI event (`CAP-001-FINDINGS.md` §6 Task 6) | `captures/CAP-008-2026-08-26_09-38-44_09-41-36-Group_V/CAP-008-btsnoop_hci.log` | same file | analyzed — see `CAP-008-FINDINGS.md` in that folder; **both open questions resolved**: the full HFP SLC handshake reoccurs on a fresh classic-link connection (frames 776–1132, on RFCOMM channel 6/DLCI 0x0c this session — a third distinct channel-number assignment for the same profile, reinforcing `CAP-001`'s "channel numbers are session-local" note); two clean `Enhanced Setup Synchronous Connection`/`Synchronous Connection Complete` (mSBC, eSCO) pairs appear, one per call — the first SCO/eSCO HCI events in this project, closing `CAP-001-FINDINGS.md` §6 Task 6. `CALL-001` itself is now sub-second wire/video-correlated for both call start and end, across two independent calls. DLCI 0x0a stays silent through both calls (14th consecutive negative result) but is now specifically ruled out as the call's audio path. **Bonus findings:** AVDTP Suspend/Start (A2DP) brackets each call almost exactly; `AT+BIEV` HF Indicator #2 tracks the **Left** earbud this session (vs. Right in `CAP-009`) — refines the open "fixed-Right vs. HFP-primary" question; one isolated unsolicited `+CIEV:6,...` (`battchg`) push, a new 🔴 open question |
| `CAP-009` | 2026-08-23 | Pixel 7a (⚪ assumed) | 17 (⚪ assumed) | release_5.203 (⚪ assumed) | n/a (system Bluetooth only, no companion app screen this session) | X | `BATT-006` | Battery-level discrepancy bracket — cross-check `AT+CIND`/`battchg` against `AT+BIEV` over a natural battery decline (`CAP-001-FINDINGS.md` §3); no phone call occurred, so not combined with `CAP-008` | `captures/CAP-009-2026-08-23_18-33-52_20-12-55-Group_X/CAP-009-btsnoop_hci.log` | same file | analyzed — see `CAP-009-FINDINGS.md` in that folder; **`BATT-006` resolved**: `AT+CIND?`'s `battchg` is a single non-repeating snapshot at SLC setup (never a live reading); `AT+BIEV=2` pushes at an irregular (not fixed ~6–7s) cadence and tracks the Right earbud's percentage specifically, not Left/Case/an aggregate, across 86 minutes; HFP/RFCOMM never reopens after the 20:02:27 case+USB reconnect (A2DP-only from then on). **Bonus:** 75-occurrence, 101-minute confirmation of the already-FACT `PROTOCOL.md` §4.3 Option E (DLCI 0x08 `Group 0x0e Code 0x01`, `DECISIONS.md` ADR-014) — first session to capture a live charge cycle (Left 93%→100%) and the case's empty→populated transition. **Re-analyzed 2026-08-2x** (independent video timeline, full wire re-run): all 7 maintainer-noted transitions confirmed (2 refined by minutes, periodic-manual-check lag not an error); corrected an assumed ~50s video-coverage gap to the actual ~2s; found a 🟡 HYPOTHESIS candidate for `PROTOCOL.md` §4.3 Option B's open battery code (DLCI 0x04 `Group 0x03 Code 0x03`) and a 🟡 HYPOTHESIS BLE Fast Pair scan explanation for the post-reconnect on-screen updates. All 5 proposals from this pass maintainer-approved (`DECISIONS.md` ADR-015) |
| `CAP-010` | 2026-08-16 | Pixel 7a | 17 | release_5.203 | official Pixel Buds Companion App (version not visible on screen) | W (attempted) | `PAIR-001` | Intended as Group W's stronger GATT cache-busting attempt; **actual on-screen procedure was a standard system-Settings forget-and-re-pair on the same Pixel 7a used throughout this project — neither `pm clear com.android.bluetooth` nor the Pixel 9a was used, so Group W's own method was not actually exercised (`CAP-010-FINDINGS.md` §1)** | `captures/CAP-010-2026-08-16_11-42-31_11-45-01-Group_W/CAP-010-btsnoop_hci.log` | same file | analyzed — see `CAP-010-FINDINGS.md` in that folder; **`GATT-001` still unresolved, 4th consecutive negative result** (`CAP-002`, `CAP-003`, `CAP-004`, `CAP-010`): zero `Read By Group Type`/`Find Information` traffic despite a genuinely fresh classic bond this time — explained by the procedure gap above, not new evidence against Group W's untried methods; independently reproduces the `0x0f2a`/`0x0c0X` handle cluster's stable numbering and FORM (now a 3rd–4th confirming session), the DLCI 0x08 private handshake incl. `release_5.203` (5th confirming session), and the classic fresh-pairing state machine (4th confirming session); new byte-level detail for `0x0c0c` (40B notify) and `0x0c13`/`0x0c14` (9/10/32B, doesn't fit the `0x0c04`/`0x0c05` AES-block pattern — 🟡 possibly a structurally distinct characteristic) |
| `CAP-011` | 2026-08-21 | Pixel 7a | 17 (⚪ assumed) | release_5.203 (⚪ assumed) | official Pixel Buds Companion App (open for most of session) | Q (#18) | `BATT-002`, `BATT-003` | Passive BLE scan intended for case-closed/idle, to capture the Fast Pair Battery Notification advertisement independently of RFCOMM (`PROTOCOL.md` §4.3 Option A) | `captures/CAP-011-2026-08-21_09-45-17_09-55-16-Group_Q/CAP-011-btsnoop_hci.log` | same file | analyzed — see `CAP-011-FINDINGS.md` in that folder; **procedure deviation**: an active classic RFCOMM+GATT connection was present throughout (app left open on "Device details"), not the intended connection-free scan; `0xFE2C` Fast Pair Service BLE advertisements confirmed present (🟢 FACT, 634 frames, 5 rotating addresses) but the sampled payloads do **not** structurally match `PROTOCOL.md` §4.3 Option A's documented Battery Notification byte layout — recorded as inconclusive, not force-fit; a clean connection-free repeat is still needed. **Re-analyzed 2026-08-23** (maintainer spotted a 1% battery drop in the recording): pinpointed the exact change to 09:52:25.8 and found a DLCI 0x08 message (`Group 0x0e Code 0x01`, cross-confirmed by `Group 0x04 Code 0x03`) tracking the Left/Right values across 4 occurrences in the log — proposed as `PROTOCOL.md` §4.3 Option E, 🟡 HYPOTHESIS pending sign-off; see `CAP-011-FINDINGS.md` §7 |
| `CAP-012` | 2026-08-26 | Pixel 7a | 17 (⚪ assumed, not visible on screen this capture) | release_5.203 (⚪ assumed — not re-confirmed on the wire this session, log severely ACL-truncated) | uninstalled (GMS disabled) | S (repeat) | `GFPS-001`, `PAIR-001`, `CASE-003`, bonus `PAIR-003` (first Pixel 7a occurrence) | Repeat of Group S following its original system-settings-only procedure exactly (no nRF Connect at any point, independently confirmed both on screen and on the wire — zero BLE connection to the Buds anywhere in the log), to isolate whether `CAP-004`'s Cross-Transport-Key-Derivation bonding result was an artifact of nRF Connect's early BLE connection (`CAP-004-FINDINGS.md` §8 item 4) | `captures/CAP-012-2026-08-26_15-30-19_15-32-28-Group_S/CAP-012-btsnooz_hci.log` (**severely ACL-truncated, ~15-byte captured length per data packet — `btsnooz`-fallback extraction rather than a raw untruncated log; see `CAP-012-FINDINGS.md` §1**) | same file | analyzed — see `CAP-012-FINDINGS.md` in that folder; **hypothesis confirmed (🟢 FACT): this session uses classic Secure Simple Pairing, not CTKD** — `CAP-004`'s CTKD result is isolated to nRF Connect's early BLE connection, not to the GMS-disabled/no-app condition itself (§2/§10 there); `GFPS-001`'s channel-topology result reproduces `CAP-004`'s (DLCI 0x04 never opens) but the payload-content sub-question is **inconclusive**, not resolved, due to this log's truncation (§1/§4); bonus finding: a manual disconnect/reconnect (`PAIR-003`) retriggers the full HFP AT-command handshake (§6), narrowing a `PROTOCOL.md` §6 open question |
| `CAP-013` | 2026-08-26 | Pixel 7a | 17 (⚪ assumed, not screen-confirmed) | release_5.203 | official Pixel Buds Companion App (version not visible on screen) | A (repeat) | `PAIR-001`, `PAIR-004`, incidental `BATT-004`, `APP-001`, `APP-002` | Intended: HCI snoop logging started before any prior association with the device exists — resolves whether a clearing action fully clears prior bonding/BLE-association state (`CAP-001-FINDINGS.md` §6). **PROPOSAL — pending maintainer approval:** status/row text below proposed, not yet maintainer-approved. | `captures/CAP-013-2026-08-26_17-09-01_17-14-04-Group_A/CAP-013-btsnooz_hci.log` (**severely ACL-truncated, ~15-byte captured length per data packet — `btsnooz`-fallback extraction, same issue as `CAP-012`; see `CAP-013-FINDINGS.md` §1**) | same file | analyzed — **partial: pre-clearing-action window not captured** — see `CAP-013-FINDINGS.md`/`CAP-013-EVENT-NOTES.md` in that folder. Verified (not assumed) that the intended method wasn't actually followed: the on-screen clearing action was "Reset Bluetooth & Wi-Fi" (not a single-device "Forget"), and the log's first frame (17:11:45.8) starts **2m21s after** that action — also after Bluetooth re-enable, case-open, pair-button-press, and device-list-tap, none of which are logged. **Primary question (`CAP-001-FINDINGS.md` §6) remains 🔴 OPEN, not answered.** **Secondary question (`PAIR-004`) is answered: 🟢 CONFIRMED fresh classic SSP handshake** (full IO-Capability/User-Confirmation/new-Link-Key-Notification sequence, frames 117–270) for the bonding state active when this capture's window began — no key-reuse path observed. Bonus: DLCI 0x02 (`libmaestro` candidate) opens ~61s after the other 4 channels this session, coinciding with an app-permission "Allow" tap — 🟡 single-sample, not promoted; a second BLE link to an unrelated random address appears mid-session, unattributed (🔴 OPEN). A genuine repeat (logging before the clearing action itself) is still needed — see proposed follow-up capture ID below. |
| `CAP-014` | 2026-08-27 | Pixel 7a | 17 (⚪ assumed, not re-confirmed on screen this session) | release_5.203 (confirmed on-screen 20:59:07) | nRF Connect for Mobile (Nordic Semiconductor); official Pixel Buds app also surfaced twice, 20:54:39 and 20:58:52–53, unintentionally | W (repeat — **PROPOSAL, pending maintainer approval:** neither of Group W's own candidate methods was actually used this session either, see deviation note below) | `GATT-001`, incidental `PAIR-001` | Intended: repeat the `CAP-017` nRF-Connect procedure with (1) a fixed/longer HCI snoop snaplen and (2) an on-screen drill-down into "Accessory Non-Owner Service"/"Unknown Service" — **(2)'s Accessory-Non-Owner half was correctly not executed, that instruction conflicted with `DECISIONS.md` ADR-008** (`CAP-014-FINDINGS.md` §2) | `captures/CAP-014-2026-08-27_20-53-37_20-59-17-Group_W/CAP-014-btsnoop_hci.log` | same file | analyzed — see `CAP-014-FINDINGS.md` in that folder; **PROPOSAL, pending maintainer approval:** (1) succeeded — snaplen confirmed fixed, 0 truncated frames out of 4,663 (§0); **but `GATT-001`'s handle↔UUID mapping is still 🔴 OPEN** — root cause now identified precisely (§4): this session reused the same, already-bonded Pixel 7a with a cached GATT client, so Android served the `0x0c0X`/`0x0f2X` cluster from its cached database instead of re-declaring it on the wire (only the GATT service itself, handles `0x0001`–`0x0009`, was genuinely re-discovered live). Neither Group W Option (a) `pm clear com.android.bluetooth` nor Option (b) (Pixel 9a) was actually tried — both remain the clearly-identified next step, now combined with a confirmed-working snaplen for the first time. Reproduces `0x0f2a`="Revision 6" (3rd session, byte-identical to `CAP-002`) and `0x0f32`=`0x64`/`0x0f33` CCCD (2nd session, byte-identical to `CAP-017`, narrowing that open question); bonding used Cross-Transport Key Derivation (2nd confirmed instance after `CAP-004`, consistent with `CAP-012`'s "BLE-tool-first" explanation) |
| `CAP-015` | 2026-08-18 | Pixel 7a | 17 | release_5.203 | 1.0.955078536 | T | `EQP-002`, `EQS-004` | Completes/supersedes `CAP-005`'s Group T goal: five distinct EQ presets tapped in sequence, then all five EQ sliders each dragged to both extremes and back near-zero (3 passes), to resolve `CAP-005`'s open field-to-band mapping question | `captures/CAP-015-2026-08-18_06-11-06_06-17-40-Group_T/CAP-015-btsnoop_hci.log` | same file | analyzed — see `CAP-015-FINDINGS.md` in that folder; **field-to-band mapping promoted to 🟢 FACT** (field 1↔Low bass, 2↔Bass, 3↔Mid, 4↔Treble, 5↔Upper treble, wire order reversed from on-screen order) — matches `CAP-005`'s single-band inference exactly; also established the ±6.0 band-gain clamp (🟢 FACT, units unconfirmed) and a confirmed preset-quintet reference table |
| `CAP-016` | 2026-08-18 | Pixel 7a | 17 (build `CP2A.260705.006`, confirmed on-screen) | release_5.203 | n/a (Android system Bluetooth "Device details" page, not the companion app) | U | `CASE-004`, `CASE-005`, `CASE-006`, `OBS-003` | Re-run of Group U (distinct session from `CAP-007`): Bluetooth-on, both buds removed from case one at a time, empty-case lid close/reopen, both buds re-docked, disconnect, lid close — general case/bud-removal correlation rather than Group U's own narrower liveness-bracket procedure (see `CAP-016-FINDINGS.md` scope note) | `captures/CAP-016-2026-08-18_06-31-31_06-33-58-Group_U/CAP-016-btsnoop_hci.log` | same file | analyzed — see `CAP-016-FINDINGS.md` in that folder; **clean single-attempt, Buds-initiated reconnect on first bud removal (🟢 FACT, contrast `CAP-001`'s 3-attempt phone-initiated connect)**; **ACL disconnects the instant both buds are re-docked, Buds-initiated (reason `0x13`), 🟢 FACT**; **case-lid open/close while buds are elsewhere produces zero wire signal, 🟢 FACT — 2nd independent capture confirming this (`CAP-007` §3.4)**; a full RFCOMM channel bounce (all 4 DLCIs) recurs with **no camera-visible trigger this time**, ruling out "bud removal is the sole cause" for that class of event (`CAP-007` §3.3's hypothesis); ANC-state Notify's "settable-toggles" byte (`0x00` vs `0xe8`) newly observed to change value, tracking the app's own "no ANC mode selectable" UI state — proposed refinement to `PROTOCOL.md`'s ANC Notify field table, not yet promoted; **`INEAR-002`/`INEAR-003`/`INEAR-004` still not exercised** — no earbud is shown inserted into or removed from an ear on camera in this session either |
| `CAP-017` | 2026-08-16 | Pixel 7a | 17 | release_5.203 (⚪ assumed — not re-confirmed on-the-wire this session, DLCI 0x08 never opened) | nRF Connect for Mobile (Nordic Semiconductor), not the official app | W | `GATT-001` | Second same-day Group W attempt (distinct session from `CAP-010`'s 11:42 attempt), this time driving a brand-new third-party GATT client (nRF Connect) instead of the official app, to force a real cache-miss discovery walk | `captures/CAP-017-2026-08-16_18-30-12_18-37-12-Group_W/CAP-017-btsnoop_hci.log` | same file | analyzed — see `CAP-017-FINDINGS.md` in that folder; **`GATT-001`'s discovery goal achieved for the first time in this project (🟢 FACT)** via a third path not previously identified (fresh GATT client UID has no cache to hit) — 137 live `Read By Type`/`Read By Group Type`/`Find Information` frames on the wire (vs. zero in every prior attempt); full 15-service GATT profile recovered from video (incl. Google Fast Pair Service `0xFE2C` and two previously-undocumented 128-bit UUIDs); **but the wire log itself is severely ACL-truncated (~15B snaplen), so UUID bytes aren't recoverable from the log, and no characteristic-level drill-down happened on screen — confirmed by a dedicated full-video verification pass (§4b) that the handle↔UUID mapping cannot be closed from this capture's artifacts**; recapture needed with (1) fixed snaplen and (2) on-screen characteristic drill-down into "Unknown Service" (`109b862f-…`) — **"Accessory Non-Owner Service" is out of scope for any drill-down/read, per `DECISIONS.md` ADR-008; do not include it in a future session's plan** (correction added post-`CAP-014`, which caught this exact instruction still standing here and had to decline it — see `CAP-014-FINDINGS.md` §2). `CAP-014`'s repeat (below) fixed the snaplen but did not achieve a genuine cache-miss discovery, so the mapping is still open — see that row |
| `CAP-018` | *planned* | either phone | TBD | TBD | TBD | Y | `GATT-002` | BLE-only connection isolation for the `0x0044` notification burst (`CAP-016-FINDINGS.md` §11) — enable Bluetooth and let the BLE link to the already-paired Buds form without touching the buds/case at all, to isolate whether the burst is triggered by BLE link establishment alone, independent of any bud/case action | — | — | planned |
| `CAP-019` | 2026-08-21 | Pixel 7a | 17 (⚪ assumed) | release_5.203 (⚪ assumed) | 1.0.955078536 (assumed same as prior sessions) | C | `CONV-001`, `MULTI-001` | Conversation Detection and Multipoint on/off toggles | `captures/CAP-019-2026-08-21_07-35-50_07-39-30-Group_C/CAP-019-btsnoop_hci.log` | same file | analyzed — see `CAP-019-FINDINGS.md` in that folder; both toggles isolate to a single DLCI 0x02 `Sent` frame each (🟡 HYPOTHESIS, `field5{field4{field22=1}}`/`field5{field4{field11=1}}`); Multipoint additionally triggers a DLCI 0x04 Group `0x07` (SASS) burst including an ASCII `"in-use"` string — first content-level correlation of SASS traffic to a specific action |
| `CAP-020` | 2026-08-21 | Pixel 7a | 17 (⚪ assumed) | release_5.203 (⚪ assumed) | 1.0.955078536 (assumed) | F | `TOUCH-001`, `HEAD-001` | Touch controls and Head gestures top-level on/off toggles; leaves Head gestures ON for `CAP-028` (Group O) | `captures/CAP-020-2026-08-21_07-46-14_07-47-49-Group_F/CAP-020-btsnoop_hci.log` | same file | analyzed — see `CAP-020-FINDINGS.md` in that folder; both toggles isolate to a single DLCI 0x02 `Sent` frame each (🟡 HYPOTHESIS, `field5{field4{field4=1}}`/`field5{field4{field29=2}}`) — first identification of the general-purpose `field5{field4{...}}` settings-write envelope, reused by every other capture in this batch |
| `CAP-021` | 2026-08-21 | Pixel 7a | 17 (⚪ assumed) | release_5.203 (⚪ assumed) | 1.0.955078536 (assumed) | G | `HOLD-001`–`HOLD-005` | Per-earbud press-and-hold configuration and ANC-mode rotation list | `captures/CAP-021-2026-08-21_07-59-36_08-07-04-Group_G/CAP-021-btsnoop_hci.log` | same file | analyzed — see `CAP-021-FINDINGS.md` in that folder; all 4 Left/Right × ANC/Assistant combinations (`HOLD-001`–`HOLD-004`) isolate cleanly (🟡 HYPOTHESIS, `field5{field4{field7{field1\|2{field4=5\|6}}}}`, field1=Left/field2=Right, 5=ANC/6=Digital assistant); `HOLD-005`'s 16-frame checklist burst confirms field order matches on-screen order but cannot be split between Left's/Right's lists from wire content alone |
| `CAP-022` | 2026-08-21 | Pixel 7a | 17 (⚪ assumed) | release_5.203 (⚪ assumed) | 1.0.955078536 (assumed) | H | `AUDIO-001`–`AUDIO-003` | Mono audio, Volume EQ, Volume balance | `captures/CAP-022-2026-08-21_08-15-24_08-17-27-Group_H/CAP-022-btsnoop_hci.log` | same file | analyzed — see `CAP-022-FINDINGS.md` in that folder; all 3 isolate to DLCI 0x02 writes (🟡 HYPOTHESIS: `field19`=Mono audio, `field15`=Volume EQ, `field17`=Volume balance, 7 samples from one continuous drag); Volume balance's persistent-write claim not tested (no disconnect/reconnect this session) |
| `CAP-023` | 2026-08-21 | Pixel 7a | 17 (⚪ assumed) | **release_5.203** | 1.0.955078536 (assumed) | I | `FW-001`, `FW-002` (`FW-003`/`FW-004` not exercised — gap) | Firmware/device-info screen; resolves `PROTOCOL.md` §0.1's open wire-baseline-vs-UI-baseline firmware-version question | `captures/CAP-023-2026-08-21_08-23-40_08-25-21-Group_I/CAP-023-btsnoop_hci.log` | same file | analyzed — see `CAP-023-FINDINGS.md` in that folder; **primary goal achieved**: on-screen firmware version (`release_5.203`, all 3 components) matches DLCI 0x08's private-envelope string byte-for-byte, same session — resolves which on-the-wire string the app calls "the firmware version"; `FW-001`'s manual check produces zero wire traffic (🟢 FACT, cached not live-queried); `FW-003`/`FW-004` not visited this session |
| `CAP-024` | 2026-08-21 | Pixel 7a | 17 (⚪ assumed) | release_5.203 (confirmed same day, `CAP-023`) | 1.0.955078536 (assumed) | J | `INEAR-001`, `CASE-001`, `CASE-002` | In-ear detection toggle and case-sound settings | `captures/CAP-024-2026-08-21_08-31-27_08-33-24-Group_J/CAP-024-btsnoop_hci.log` | same file | analyzed — see `CAP-024-FINDINGS.md` in that folder; all 3 isolate to DLCI 0x02 writes (🟡 HYPOTHESIS: `field2`=In-ear detection, `field28`="Bud return"/`CASE-001`, `field27`="Other alerts"/`CASE-002`); no case-specific vs. bud-specific channel distinction found — same shared envelope |
| `CAP-025` | 2026-08-21 | Pixel 7a | 17 (⚪ assumed) | release_5.203 (⚪ assumed) | 1.0.955078536 (assumed) | K | `FIND-001`, `FIND-002` (`FIND-003`/`FIND-004` attempted, resolve to a different mechanism) | Find My Buds/Ring, the last remaining fully unattributed app command; `PROTOCOL.md` §4.4's hypothesis tested | `captures/CAP-025-2026-08-21_08-40-52_08-45-26-Group_K/CAP-025-btsnoop_hci.log` | same file | analyzed — see `CAP-025-FINDINGS.md` in that folder; **`FIND-001`/`FIND-002` strongly confirmed, video-correlated**: DLCI 0x04 `Group 0x04 Code 0x01`, `Value`=`0x01`(start Right)/`0x02`(start Left)/`0x00`(stop) — matches the spec's worked ACK example exactly; **major structural finding**: `FIND-003`(Case)/`FIND-004`(both) route through a separate Find Hub/Find-My-Device-Network UI flow that produces **no** local Ring command on the wire — a different, likely GMS/account-mediated mechanism, not a capture gap |
| `CAP-026` | *planned* | Pixel 7a | TBD | TBD | TBD | L | `BATT-001`, `OBS-001` | Main run-through group, never yet captured — passive observation windows (idle-wait-for-reconnect-notification, force-close/reopen) | — | — | planned |
| `CAP-027` | 2026-08-30 | Pixel 7a (⚪ assumed, not screen-confirmed) | 17 (⚪ assumed) | release_5.203 (🟢 confirmed on-wire) | official Pixel Buds Companion App (not screen-confirmed; Spotify used as the music source) | N | `TOUCH-002`–`TOUCH-007` | Physical touch gestures on the bud hardware; `TOUCH-007`'s behavior depends on the per-earbud mode set in `CAP-021` (Group G) | `captures/CAP-027-2026-08-30_15-45-14_15-49-07-Group_N/CAP-027-btsnoop_hci.log` | same file | analyzed — see `CAP-027-FINDINGS.md` in that folder; **structural finding: `TOUCH-002`–`TOUCH-006` ride AVRCP (Pass Through/VolumeChanged), not any RFCOMM DLCI at all** — all 13 instances 1:1-correlated to a wire event; `TOUCH-007` (press-and-hold) is the exception, confirmed on DLCI 0x04 (official Fast Pair Message Stream, already-FACT "Notify ANC state" `0x13` shape from `PROTOCOL.md` §4.1), not DLCI 0x02 (`libmaestro`). Both `TOUCH-007` timestamps (previously `TBD`) recovered this way. Left-bud `TOUCH-007` shows two rotation-step Notify frames 5.68s apart, unresolved whether that's one long hold or two presses (🔴 open) |
| `CAP-028` | *planned* | either phone | TBD | TBD | TBD | O | `HEAD-002`, `HEAD-003` | Main run-through group, never yet captured — physical head gestures; requires Head gestures enabled first via `CAP-020` (Group F) | — | — | planned |
| `CAP-029` | *planned* | either phone | TBD | TBD | TBD | P | `CONV-002`, `CASE-007`(optional/destructive), `CASE-008`, `PAIR-002`(if `CASE-007` run) | Main run-through group, never yet captured — Conversation Detection voice trigger, optional factory reset (`CASE-007`, resets Find My Device link — see `WORKSTATION_PREPARATIONS.md` Disaster Recovery), and the still-open shorter-press pairing-mode question (`CASE-008`) | — | — | planned |
| `CAP-030` | *planned* | either phone | TBD | TBD | TBD | Q (items #19–20) | `LOUD-001`, `ADAPT-002` | Group Q's two remaining items (item #18 already planned separately as `CAP-011`) — attempt to observe Loud Noise Protection and Adaptive Audio engaging; requires firmware ≥4.467 | — | — | planned |
| `CAP-031` | 2026-08-27 | Pixel 7a | 17 (⚪ assumed, not screen-confirmed) | release_5.203 | official Pixel Buds Companion App (version not visible on screen) | A (repeat, 3rd attempt) | `PAIR-001`, `PAIR-004`, incidental `BATT-004` | Third attempt at `CAP-001-FINDINGS.md` §6's original goal — HCI snoop logging started before any prior association with the device exists, this time with a live in-recording file-size-polling check added specifically to avoid `CAP-013`'s failure mode. **PROPOSAL — pending maintainer approval:** status/row text below proposed, not yet maintainer-approved. | `captures/CAP-031-2026-08-27_06-04-48_06-08-10-Group_A/CAP-031-btsnooz_hci.log` (**`btsnooz`-format, inferred 15–126-byte captured length per packet, same truncation issue as `CAP-012`/`CAP-013`; see `CAP-031-FINDINGS.md` §1**) | same file | analyzed — **partial: pre-clearing-action window not captured, a second consecutive failure of this method** — see `CAP-031-FINDINGS.md`/`CAP-031-EVENT-NOTES.md` in that folder. This session used a genuine narrow, per-device "Forget" (screenshot-confirmed, unlike `CAP-013`'s broader reset) and a live snoop-log file-size-polling check during recording, but the log's first frame (06:06:37.16) still starts **66s after** the on-screen Forget tap (06:05:31) — also after case-open, pair-button-press, and the entire first "Pair new device" scan attempt, none of which are logged. **Primary question (`CAP-001-FINDINGS.md` §6) remains 🔴 OPEN, not answered, untested a third time.** **Secondary question (`PAIR-004`) is reconfirmed: 🟢 CONFIRMED fresh classic SSP handshake** (frames 598–689, a sixth confirming instance) — no key-reuse path observed. Bonus, both negative results: `CAP-013`'s DLCI 0x02 ~61s-delay does **not** reproduce (opens 1.64s after DLCI 0x00, within the initial burst) and `CAP-013`'s unattributed second BLE link does **not** reproduce (exactly one LE link this session, to the Buds' own public address) — both now look like single-session artifacts. A fourth attempt is still needed, this time verifying snoop-log *content* freshness (not just file size) before the Forget tap. |
| `CAP-032` | 2026-08-27 | Pixel 7a | 17 (⚪ assumed, not screen-confirmed) | release_5.203 | official Pixel Buds Companion App (version not visible on screen) | A (repeat, 4th attempt) | `PAIR-001`, `PAIR-004`, incidental `BATT-004` | Fourth attempt at `CAP-001-FINDINGS.md` §6's original goal — this time extracted via the raw BTSnoop file path (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3 step 3) rather than the `btsnooz.py` fallback used for `CAP-012`/`CAP-013`/`CAP-031`. **PROPOSAL — pending maintainer approval:** status/row text below proposed, not yet maintainer-approved. | `captures/CAP-032-2026-08-27_18-30-15_18-32-33-Group_A/CAP-032-btsnoop_hci.log` (**genuine raw, untruncated BTSnoop — `frame.cap_len == frame.len` for all 2,455 frames, no `capinfos`-inferred size cap; see `CAP-032-FINDINGS.md` §0.1**) | same file | analyzed — **success: pre-clearing-action window captured for the first time in four attempts** — see `CAP-032-FINDINGS.md`/`CAP-032-EVENT-NOTES.md` in that folder. The log's first frame (18:29:45.72) starts **~58s before** the on-screen Forget tap (18:30:42) and ~30s before the video itself begins. **Primary question (`CAP-001-FINDINGS.md` §6) is now answered for this session: 🟢 no prior BLE link or valid classic link key existed for the Buds anywhere in the covered pre-Forget window** (`CAP-032-FINDINGS.md` §0.3) — this does not reproduce `CAP-001`'s original finding (a clean counter-example, not a contradiction; `CAP-001`'s own session-specific puzzle remains independently open). **Secondary question (`PAIR-004`) reconfirmed: 🟢 CONFIRMED fresh classic SSP handshake** (frames 1090–1153, a seventh confirming instance). Bonus: the untruncated log fully decodes DLCI 0x08's battery push (`[100,1,1]`/`[100,1,2]`/`[57,1,3]` = Left/Right/Case, matching the on-screen reading exactly) and firmware/capability-identifier fields; a previously-undocumented vendor-specific HCI command (`0xFD57`/`0x0157`, frame 91) embeds the Buds' address 105ms into the log, structurally consistent with bulk bonded-device provisioning at BT-enable time, not a connection — recorded 🔴 OPEN QUESTION, not bearing on the primary question. The `btsnooz`-vs-raw extraction-path hypothesis (`CAP-013-FINDINGS.md` §1, `CAP-031-FINDINGS.md` §1) is supported by this one data point (`CAP-032-FINDINGS.md` §0.1/§7 Test C), not yet independently isolated. |
| `CAP-033` | 2026-08-30 | Pixel 7a | TBD | release_5.203 (🟢 confirmed on-wire) | n/a — app force-stopped throughout the entire recorded session | AA | `SDP-001`, `SDP-002`(not attempted — no update pending) | SDP UUID branch isolation for `gbm.a()`'s "default internal rfcomm socket" path — system-settings-only pairing (app force-stopped) to check whether the pre-app-fetch SDP UUID set ever differs from every existing capture's "pigweed"-only result (`REVERSE_ENGINEERING.md`'s `gbm`/`fzd` entries, `DECISIONS.md` ADR-018) | `captures/CAP-033-2026-08-30_15-17-03_15-19-52-Group_AA/CAP-033-btsnoop_hci.log` | same file | analyzed, **isolation not fully clean — see `CAP-033-FINDINGS.md` §1** ("Forget" preceded Force-stop by ~10s, reverse of procedure, though scoped away from the actual SDP-browse window; Step 3's app-open baseline was never executed at all, on- or off-camera). `SDP-001` result capped at 🟡 HYPOTHESIS: "default" UUID still zero occurrences (raw byte scan, both byte orders), "pigweed" UUID confirmed present and named **"MAESTRO APP"** on-the-wire (SDP service-name string, frame 1279) — first wire-level (not just APK-code) corroboration of `DECISIONS.md` ADR-018's channel identity. **Bonus structural finding:** the same SDP response names DLCI 0x08 as **"GSND CONTROL"** and DLCI 0x0a as **"GSND AUDIO"** — new, previously-undocumented leads for `PROTOCOL.md` §2.3's open DLCI-0x08 identity question and `CAP-021-FINDINGS.md` §4a's unattributed DLCI-0x0a burst, proposed pending maintainer review, not promoted |

**Column notes:**

- **Phone** — `Pixel 7a` (primary, official app) or `Pixel 9a` (secondary,
  GrapheneOS), per the two-device setup described at the top of this
  document.
- **Group(s)** — the letter(s) from §4 (Z, A–Q, R, S — Z, R, and S are
  special-purpose groups that intentionally sort outside the A–Q run-through:
  Z is pipeline-validation, always done first; R is the occasional
  forced-GATT-discovery procedure; S is the occasional GMS-disabled/no-app
  procedure — both R and S are done only when needed) covered in this
  session; one bugreport pull can cover several groups if captured as one
  continuous logging session (§4.1).
- **Test(s)** — the `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` Test-ID(s) actually
  exercised in this session (e.g. `ANC-001, ANC-002`) — this is what a
  `CAP-NNN-FINDINGS.md` finding should ultimately trace back through: finding →
  this row's capture ID + frame number → the Test-ID here → the catalog entry
  in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`. Usually a subset of everything the
  Group(s) column's scenario covers, since not every attempt succeeds cleanly.
- **Status** — one of: `planned` (added 2026-08-14 — a specified but not yet
  run session, per its own capture-scenario paragraph in §4; every other
  column stays `TBD`/`—` until the session actually happens), `captured`
  (extracted, not yet reviewed), `analyzed` (reviewed in Wireshark per §5,
  findings recorded per §8), `promoted` (a finding from this capture has been
  written into `PROTOCOL.md` with a `[VERIFIED-LOCAL]`
  tag), `discarded` (unusable — note why, e.g. in an extra remarks column if
  needed).
- Reference a capture from `PROTOCOL.md` by its ID (e.g.
  "confirmed in `CAP-001`, frame 214") rather than by date or description, so
  the reference survives even if this row's description is later edited.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/CAPTURE_BLUETOOTH_HCI_SNOOP.md - https://tedsluis.github.io/opencontrolpixelbudspro2/CAPTURE_BLUETOOTH_HCI_SNOOP
