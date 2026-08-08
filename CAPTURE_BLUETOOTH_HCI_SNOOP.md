# CAPTURE_BLUETOOTH_HCI_SNOOP.md — Bluetooth HCI Snoop Capture Guide

**Purpose:** step-by-step procedure to capture, extract, and analyze Bluetooth HCI traffic
between a phone and the Pixel Buds Pro 2, in order to fill in the confidence-rated
placeholders in `PROTOCOL-NOTES.md` (magic bytes, checksum, opcode table, battery
approach). This document is the *how* — `PROTOCOL-NOTES.md` is the *what we found*.

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
5. **Restart Bluetooth** — toggle Bluetooth off, then on again (or reboot the phone).
   This matters: logging is not always guaranteed to start cleanly against an
   already-active Bluetooth stack; a fresh Bluetooth session after enabling the toggle
   ensures the logger is actually running.
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
`/data/misc/bluetooth/logs/btsnoop_hci.log` — on a production (non-rooted) build,
including stock Pixel firmware and GrapheneOS, this will fail with a permission error,
since `adbd` cannot run as root on production builds. This is expected and not specific
to GrapheneOS.

The supported, working method on both phones:

1. Run on your computer:
   ```
   adb bugreport buds_capture
   ```
   This produces `buds_capture.zip` (or a similarly named file/folder depending on
   platform-tools version) in your current directory. No root required — this works on a
   stock, non-rooted GrapheneOS production build as confirmed by GrapheneOS's own
   community support.
2. Unzip the result.
3. Locate the snoop log inside. The exact internal path varies by Android version —
   check both of these first:
   - `FS/data/log/bt/btsnoop_hci.log`
   - `FS/data/misc/bluetooth/logs/btsnoop_hci.log`

   If neither exists, search the extracted contents by filename:
   ```
   find . -iname "*btsnoop*"
   ```
4. Copy/rename the file somewhere memorable, e.g.
   `captures/2026-08-02_pixel7a_anc-toggle.log`, so repeated sessions don't overwrite
   each other.
5. Once you've confirmed extraction worked, you can disable the HCI snoop toggle again
   (§2 step 3) to avoid unnecessary background logging and disk usage between sessions.

---

## 4. Capture Procedure — Isolate Every Action

The single most important rule: **one action per capture window, with a pause before and
after.** A capture full of overlapping actions is very hard to attribute correctly in
Wireshark; a capture of ten cleanly isolated actions is straightforward.

Recommended rhythm per action: **wait ~5s → note the exact time → perform the action →
wait ~5–10s → move to the next action.**

### 4.1 Pixel 7a (official app) — primary session

**Source:** the action list below is the validated inventory from `TESTPLAN_EN.md`
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

**⚠️ Priority tip:** PROTOCOL-NOTES.md §6 flags the "Play sound on Left earbud" action
(group K below) as a specifically valuable, low-risk target — its frame can be directly
compared against the Fast Pair Message Stream spec's own worked example
(`0x04 0x01 ...` for a ring action) to confirm or refute the framing hypothesis in
`PROTOCOL-NOTES.md` §2.0. If you only have time for a short session, prioritize group K.

#### Group A — Connection / bonding baseline
1. **Pairing / bonding baseline** — capture this as its own isolated session,
   ideally before the command groups below (B–P). If the Buds are already
   paired, **"forget" the device on the phone side first** (Bluetooth
   settings → the paired device → Forget), then re-pair through Bluetooth
   settings — this captures a real bonding handshake instead of skipping it
   because "it's already paired." This is a **lightweight, safely repeatable**
   action: it does not touch the Buds' own memory or the Find My Device link,
   unlike the full factory reset in Group P #16 below — do not confuse the
   two. Wait for the connection to settle before moving on to Group B.

#### Group B — Active Noise Control
2. **ANC → Off**. Wait. Note time.
3. **ANC → Noise Cancellation** (active). Wait. Note time.
4. **ANC → Adaptive** (if your firmware exposes it — confirmed present in `release_5.203`
   per `PROTOCOL-NOTES.md` §4.1). Wait. Note time.
5. **ANC → Transparency**. Wait. Note time.

#### Group C — Conversation Detection & Multipoint
6. **Toggle 'Conversation Detection' on/off**. Wait. Note time.
7. **Toggle 'Multipoint' on/off**. Wait. Note time.

#### Group D — Equalizer: presets
8. **Select EQ preset: Standard**. Wait. Note time.
9. **Select EQ preset: Bass Boost**. Wait. Note time.
10. **Select EQ preset: Bass Reduction**. Wait. Note time.
11. **Select EQ preset: Balanced**. Wait. Note time.
12. **Select EQ preset: Vocal Boost**. Wait. Note time.
13. **Select EQ preset: Clarity**. Wait. Note time.
14. **Select EQ preset: Last saved**. Wait. Note time.
15. **Save current EQ as a new preset** ('Save') — a distinct write action from preset
    selection. Wait. Note time.

#### Group E — Equalizer: individual sliders
Change one band at a time by a clearly visible amount — not all bands in one gesture.
16. **Adjust EQ slider: High treble**. Wait. Note time.
17. **Adjust EQ slider: Treble**. Wait. Note time.
18. **Adjust EQ slider: Mid**. Wait. Note time.
19. **Adjust EQ slider: Bass**. Wait. Note time.
20. **Adjust EQ slider: Low bass**. Wait. Note time.

#### Group F — Touch & head gesture toggles
21. **Toggle 'Touch controls' fully on/off**. Wait. Note time.
22. **Toggle 'Head gestures' fully on/off**. Wait. Note time.

#### Group G — Press-and-hold configuration
23. **Set 'Press and hold' Left → Toggle ANC**. Wait. Note time.
24. **Set 'Press and hold' Left → Digital assistant**. Wait. Note time.
25. **Set 'Press and hold' Right → Toggle ANC**. Wait. Note time.
26. **Set 'Press and hold' Right → Digital assistant**. Wait. Note time.
27. **Check/uncheck one ANC mode in the press-and-hold rotation list** (e.g. remove
    'Off' from the cycle). Wait. Note time.

#### Group H — Audio & volume settings
28. **Toggle 'Mono audio' on/off**. Wait. Note time.
29. **Toggle 'Volume EQ' on/off**. Wait. Note time.
30. **Shift the 'Volume balance' slider**. Wait. Note time. (Per `TESTPLAN_EN.md` §1 this
    is stored locally on the earbuds themselves — a good candidate for a confirmable
    persistent write.)

#### Group I — Firmware & device info
31. **Tap the 'Firmware up to date' check** (manual). Wait. Note time.
32. **Open 'More settings'** to view firmware version per component — may trigger a
    status query. Wait. Note time.
33. **View serial numbers per component** (same screen). Wait. Note time.
34. **View connection status** ("Earbud status: Connected"). Wait. Note time.

#### Group J — In-ear detection & case sounds
35. **Toggle 'In-ear detection' on/off**. Wait. Note time.
36. **Toggle case sound 'Earbuds replaced' on/off**. Wait. Note time.
37. **Toggle case sound 'Other notifications' on/off**. Wait. Note time.

#### Group K — Find My Buds (high-priority, see tip above)
38. **Play sound on Left earbud**. Wait. Note time.
39. **Play sound on Right earbud**. Wait. Note time.
40. **Play sound on Case**. Wait. Note time.
41. **Play sound on both earbuds simultaneously**. Wait. Note time.

#### Group L — Passive/automatic observation windows
These aren't taps — they're deliberate waiting periods to catch background/automatic app
traffic per `TESTPLAN_EN.md` §3.
42. **Idle wait with the app open**, ~60s right after connecting, without touching
    anything — intended to catch the "battery status notification on every reconnect"
    behavior. Note the start time.
43. **Force-close and reopen the app** — intended to catch any status query the app sends
    on launch. Note the exact time of reopening.

After finishing a group, pull the bugreport once (§3) — you don't need a separate
bugreport per action, just clean timestamps to slice the single log into segments
afterward.

### 4.2 Pixel 9a (GrapheneOS) — secondary/validation session

No app-driven commands are possible here, so this session focuses on connection-level
and passive behavior:

1. **Pairing**, via system Bluetooth settings (Settings → Connected devices → Pair new
   device). Note the start time precisely — this is the most information-dense part of
   this session (bonding handshake, initial service discovery).
2. **Idle observation** — once connected, wait ~30–60 seconds without touching anything.
   This can reveal spontaneous status frames the Buds send unprompted.
3. **Open the Bluetooth device detail screen** for the Buds in system settings (this can
   trigger GATT service/characteristic discovery on some Android versions).
4. **Disconnect and reconnect** once, as its own isolated pair of actions, to observe
   both a clean teardown and a reconnection to an already-bonded device (useful for
   validating `ARCHITECTURE.md` §6/§7 resilience assumptions).

Pull the bugreport (§3) the same way.

### 4.3 Hardware Actions (either phone)

**Source:** `TESTPLAN_EN.md` sections 2 (User Actions via the Case & Buds) and 4
(Automatic Actions Initiated by the Hardware). These actions are grouped separately from
4.1/4.2 because they aren't tied to a specific phone — a tap on the bud, or the case
button, behaves the same regardless of which device is connected. Run this group on
**whichever phone you already have connected and logging** at the time; if you want to
compare hardware behavior across both OSes for a specific action, repeat that one action
on the other phone as its own short session rather than redoing the whole group.

Same rhythm as before: **wait ~5s → note the exact time → perform the action → wait
~5–10s → move to the next action.**

#### Group M — Case & wear state
1. **Open the charging case lid**. Wait. Note time.
2. **Remove Left earbud from the case**. Wait. Note time.
3. **Remove Right earbud from the case**. Wait. Note time.
4. **Insert Left earbud into the ear**. Wait. Note time.
5. **Insert Right earbud into the ear**. Wait. Note time.
6. **Place buds back in the case and close the lid**. Wait. Note time. (Expected to
   terminate the active Bluetooth Classic connection — good for validating
   `ARCHITECTURE.md` §6/§7 disconnect handling.)

#### Group N — Touch gestures
7. **Tap once** on a bud. Wait. Note time.
8. **Double-tap** on a bud. Wait. Note time.
9. **Triple-tap** on a bud. Wait. Note time.
10. **Swipe forward** on a bud (volume up). Wait. Note time.
11. **Swipe backward** on a bud (volume down). Wait. Note time.
12. **Press and hold** on a bud. Wait. Note time. (Behavior depends on the per-earbud
    configuration set in §4.1 Group G — note which mode was active when you test this.)

#### Group O — Head gestures
Requires 'Head gestures' enabled (§4.1 Group F).
13. **Nod** (simulating answering a call, or a text reply if 'Spoken notifications' is
    on). Wait. Note time.
14. **Shake** (simulating rejecting a call/dismissing a text reply). Wait. Note time.

#### Group P — Voice & case button
15. **Start speaking** with Conversation Detection on (§4.1 Group C), to trigger the
    detection event. Wait. Note time.
16. **Hold the case button for 30 seconds** (case open, buds inside, plugged into power)
    — ⚠️ this is a confirmed **full factory reset**, not just pairing mode (per
    `TESTPLAN_EN.md` §2). Do this deliberately, last, and only once you're ready to
    re-pair from scratch — it will also reset the Find My Device link on the Pro 2.
    If you do trigger it, capture the subsequent re-pair as its own isolated session
    right afterward (same rhythm as Group A #1) — this gives a second,
    from-true-factory-state bonding capture to compare against Group A's lightweight
    forget-and-re-pair baseline. It is optional and not a prerequisite for anything
    else in this guide — Group A's lightweight baseline is sufficient on its own for
    every other group.
17. **(Open question, see `PROTOCOL-NOTES.md` §7)** Try a shorter/different press on the
    case button to see if it triggers pairing mode without a full reset. No officially
    confirmed duration exists for this — treat your own finding here as
    `[VERIFIED-LOCAL]` material for `PROTOCOL-NOTES.md` once confirmed.

#### Group Q — Automatic hardware behavior (observation, not action)
These are waiting periods to catch spontaneous hardware-initiated traffic per
`TESTPLAN_EN.md` §4 — nothing to tap, just capture while the condition holds.
18. **Passive BLE scan while the case is closed and idle** — intended to catch the Fast
    Pair Battery Notification advertisement (`PROTOCOL-NOTES.md` §4.3 Option A) without
    any active RFCOMM connection. This doesn't require the buds to be connected to the
    capturing phone at all — any nearby scan should do, per the spec.
19. **Trigger a loud, sudden sound near the buds while worn** (e.g. clap sharply nearby)
    to attempt to observe Loud Noise Protection engaging (`PROTOCOL-NOTES.md` §4.2/§7) —
    note whether anything appears on the wire at all, since this may be purely on-device.
20. **Move between distinctly different acoustic environments while worn** (e.g. quiet
    room → street) to attempt to observe Adaptive Audio adjusting
    (`PROTOCOL-NOTES.md` §4.2/§7) — same caveat as above.

---

## 5. Analyzing in Wireshark

1. Open the extracted `btsnoop_hci.log` file directly in Wireshark
   (File → Open, or drag-and-drop — no special import steps needed, Wireshark recognizes
   the BTSnoop format natively).
2. Useful filters to narrow the view:
   - `bthci_acl` — general ACL-level Bluetooth traffic.
   - `btrfcomm` — RFCOMM traffic specifically (this is where `libmaestro` frames live).
   - `btatt` — GATT/ATT traffic (relevant for the BLE battery-service investigation).
   - `bluetooth.addr == <buds MAC>` — restrict to the Buds' Bluetooth address once you've
     identified it (visible in the pairing frames or in Wireshark's Bluetooth device
     list under View → Bluetooth Devices).
3. Use the **timestamp column** together with your own action log (§1.3, §4) to identify
   which frame(s) correspond to which action. Wireshark's relative or UTC time display
   (View → Time Display Format) should be set to whatever you used when noting action
   times, to avoid an offset mismatch.
4. For each identified command frame:
   - Note the raw bytes (right-click → Copy → ...as Hex Stream is fastest).
   - Compare the structure against the envelope hypothesis in `PROTOCOL-NOTES.md` §2
     (magic byte, length field, channel/msg ID, payload, checksum).
   - Record the confirmed values back into `PROTOCOL-NOTES.md` §4.1's opcode table, mark
     the entry `[VERIFIED-LOCAL]` with today's date, and raise its confidence to 🟢.
5. If a frame doesn't match the expected envelope shape at all, don't force-fit it —
   note it as an open question (`PROTOCOL-NOTES.md` §7) rather than recording a guess as
   fact.

---

## 6. Notes & Gotchas

- **Log rotation:** the in-device snoop buffer is finite; very long or idle-heavy
  sessions can push earlier frames out before you extract them. Keep sessions focused
  (§2, §4).
- **Bluetooth restart is not optional.** Skipping the toggle-off/toggle-on step in §2 is
  the single most common reason a capture ends up empty despite the setting being "on."
- **`adb root` will not work** on either phone for this purpose — both stock Pixel
  firmware and GrapheneOS run production (non-rootable) builds. Don't waste time trying
  to root a device just for this; `adb bugreport` is the supported path and is
  sufficient.
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
  (`PROTOCOL-NOTES.md` §0/§5) alongside any capture you take.
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
session (see the "Bluetooth restart is not optional" note in §6) — re-check §2 step 5,
redo the toggle-off/on, reproduce the action, and pull a fresh bugreport. If it still
doesn't appear, search the whole archive by filename (`find . -iname "*btsnoop*"`) rather
than assuming a fixed path.

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
Possibly — depending on the profile and pairing security level, some traffic may be
link-layer encrypted at capture time in ways Wireshark can't automatically decrypt
without additional key material. If this happens consistently, note it as an open
question in `PROTOCOL-NOTES.md` §7 rather than assuming the envelope hypothesis in §2 is
wrong — this is a separate problem from frame structure.

**Q: Should I capture on the GrapheneOS phone first, since that's my actual target
platform?**
No — do the Pixel 7a (official app) capture first. It's the only source of frames you
can positively attribute to a specific command, since you're the one triggering them
through the known, official UI. The GrapheneOS capture is for validating
connection/pairing/passive behavior on the target OS, not for discovering new commands.

**Q: How do I know when I've captured "enough" and can stop?**
When every row in `PROTOCOL-NOTES.md` §4.1's opcode table and every open question in §7
that's answerable via traffic analysis (as opposed to, say, firmware version lookup) has
moved from 🔴/🟡 to 🟢 with a `[VERIFIED-LOCAL]` tag — or when you've made a deliberate,
documented decision to leave a specific low-priority item (e.g. Find My Buds) unverified
for now.

---

## 8. After Capturing: What to Update

Every capture session should end with at least one of these:

- [ ] `PROTOCOL-NOTES.md` §2 — envelope structure fields confirmed/corrected.
- [ ] `PROTOCOL-NOTES.md` §4.1 — opcode table rows filled in, confidence raised to 🟢.
- [ ] `PROTOCOL-NOTES.md` §4.3 — battery approach confirmed or ruled out (now split into
      Option A: BLE advertisement, and Option B: RFCOMM Message Stream — see the current
      version of that section).
- [ ] `PROTOCOL-NOTES.md` §5 — firmware version and any version-specific differences
      logged.
- [ ] `PROTOCOL-NOTES.md` §7 — open questions resolved, or new ones added if the capture
      revealed something unexpected.

Treat a capture session that doesn't result in at least one of the above as incomplete —
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

| ID | Date | Phone | Android | Buds FW | App version | Group(s) | Purpose | Bugreport file | Extracted log | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| `CAP-001` | | Pixel 7a | | | | A | Pairing/bonding baseline | | | captured |

**Column notes:**

- **Phone** — `Pixel 7a` (primary, official app) or `Pixel 9a` (secondary,
  GrapheneOS), per the two-device setup described at the top of this
  document.
- **Group(s)** — the letter(s) from §4 (A–Q) covered in this session; one
  bugreport pull can cover several groups if captured as one continuous
  logging session (§4.1).
- **Status** — one of: `captured` (extracted, not yet reviewed), `analyzed`
  (reviewed in Wireshark per §5, findings recorded per §8), `promoted` (a
  finding from this capture has been written into `PROTOCOL_NOTES.md` /
  `PROTOCOL.md` with a `[VERIFIED-LOCAL]` tag), `discarded` (unusable —
  note why, e.g. in an extra remarks column if needed).
- Reference a capture from `PROTOCOL_NOTES.md` / `PROTOCOL.md` by its ID (e.g.
  "confirmed in `CAP-001`, frame 214") rather than by date or description, so
  the reference survives even if this row's description is later edited.