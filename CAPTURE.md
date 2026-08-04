# CAPTURE.md — Bluetooth HCI Snoop Capture Guide

**Purpose:** step-by-step procedure to capture, extract, and analyze Bluetooth HCI traffic
between a phone and the Pixel Buds Pro 2, in order to fill in the confidence-rated
placeholders in `protocol-notes.md` (magic bytes, checksum, opcode table, battery
approach). This document is the *how* — `protocol-notes.md` is the *what we found*.

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

Do this as one continuous `adb bugreport` session covering all of the following, each
logged with its own timestamp:

1. **Pairing** (if not already paired, or do a deliberate re-pair as its own isolated
   capture): open Bluetooth settings, pair the Buds, wait for the connection to settle.
2. **ANC → Off**. Wait. Note time.
3. **ANC → Active** (noise cancelling on). Wait. Note time.
4. **ANC → Aware / Transparency**. Wait. Note time.
5. **ANC → Adaptive** (if your firmware exposes it). Wait. Note time.
6. **EQ — change one band** by a clearly visible amount (e.g. drag a single slider to a
   distinctly different position). Wait. Note time. Repeat per band, one at a time, not
   all bands in one gesture.
7. **EQ — select a preset**, if the app exposes presets. Wait. Note time.
8. **Open the battery view / trigger a refresh** in the app. Wait. Note time.
9. **(Optional) Find My Buds** — trigger a "ring earbud" action if you want to explore
   this low-priority feature. Wait. Note time.

After finishing, pull the bugreport once (§3) — you don't need a separate bugreport per
action, just clean timestamps to slice the single log into segments afterward.

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
   - Compare the structure against the envelope hypothesis in `protocol-notes.md` §2
     (magic byte, length field, channel/msg ID, payload, checksum).
   - Record the confirmed values back into `protocol-notes.md` §4.1's opcode table, mark
     the entry `[VERIFIED-LOCAL]` with today's date, and raise its confidence to 🟢.
5. If a frame doesn't match the expected envelope shape at all, don't force-fit it —
   note it as an open question (`protocol-notes.md` §7) rather than recording a guess as
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
  (`protocol-notes.md` §0/§5) alongside any capture you take.
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
question in `protocol-notes.md` §7 rather than assuming the envelope hypothesis in §2 is
wrong — this is a separate problem from frame structure.

**Q: Should I capture on the GrapheneOS phone first, since that's my actual target
platform?**
No — do the Pixel 7a (official app) capture first. It's the only source of frames you
can positively attribute to a specific command, since you're the one triggering them
through the known, official UI. The GrapheneOS capture is for validating
connection/pairing/passive behavior on the target OS, not for discovering new commands.

**Q: How do I know when I've captured "enough" and can stop?**
When every row in `protocol-notes.md` §4.1's opcode table and every open question in §7
that's answerable via traffic analysis (as opposed to, say, firmware version lookup) has
moved from 🔴/🟡 to 🟢 with a `[VERIFIED-LOCAL]` tag — or when you've made a deliberate,
documented decision to leave a specific low-priority item (e.g. Find My Buds) unverified
for now.

---

## 8. After Capturing: What to Update

Every capture session should end with at least one of these:

- [ ] `protocol-notes.md` §2 — envelope structure fields confirmed/corrected.
- [ ] `protocol-notes.md` §4.1 — opcode table rows filled in, confidence raised to 🟢.
- [ ] `protocol-notes.md` §4.2 — battery approach confirmed or ruled out.
- [ ] `protocol-notes.md` §5 — firmware version and any version-specific differences
      logged.
- [ ] `protocol-notes.md` §7 — open questions resolved, or new ones added if the capture
      revealed something unexpected.

Treat an capture session that doesn't result in at least one of the above as incomplete —
either the action wasn't actually isolated/identifiable, or something in the setup (§2,
§6) needs revisiting before the next attempt.
