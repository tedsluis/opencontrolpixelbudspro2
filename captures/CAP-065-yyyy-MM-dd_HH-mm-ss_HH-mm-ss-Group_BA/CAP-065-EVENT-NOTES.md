# Event Notes: OpenControl for Pixel Buds on Pixel 9a (GrapheneOS) — Group BA, robustness, UI and the `ai-sessions/0059` fixes (`CAP-065`)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0059`, 2026-09-30). After the run: fill in every blank, rename this folder from the
placeholder `CAP-065-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BA` to the film's first/last overlay times, and analyse it as `CAP-063` was (`ai-sessions/0053`).

**Purpose:** what `ai-sessions/0058` (the audit) and `ai-sessions/0059` (its processing) left for hardware — and the two `CAP-064` sections the maintainer moved
here (chat 2026-09-30, "Split as proposed"), so that each run stays short:

- **I — the 0059 app fixes:** the Quick Settings ANC tile right after Connect (A58-APP-01: it used to stay on "Open the app" until the first ANC report of the
  process), the EQ sliders disabled until the EQ is read (A58-APP-02, the maintainer's choice "Disable until read"), and two rare answers that can only be
  watched for, not provoked (an unreadable `ReadSetting` answer, A58-APP-03; a late `WriteSetting` answer after a timeout, A58-APP-04).
- **II — the Settable byte, ears visible (`DECISIONS.md` ADR-049, 🟡 "`0x00` ⇔ no bud worn"):** the three `0xe8`-while-not-worn samples of the audit (A58-CAP-09)
  are `CAP-042` frame 602 (both buds "resting loose beside the open, empty case"), `CAP-045` frame 612 (Left in the case, Right loose) and `CAP-048` frame 11939
  (both buds loose). Their films do not show the ears; this section repeats the two situations with the ears and the buds in view.
- **III — lead L-1 (`PROTOCOL.md` §2.2a 2026-09-30, 🟡):** the request address is derivable from the announced pw_rpc channel (19/21 = Left/Right Bluetooth core
  on `MAESTRO_A`, 24/26 the same on `MAESTRO_B`). Which channel do the Buds announce when only the Left bud is out, and when only the Right?
- **IV — moved from `CAP-064` VI:** the `APP_TESTPLAN.md` steps `CAP-063` skipped (F7, K4, K1–K3, L2/L3, K5; destructive last: A5, (E), B4).
- **V — moved from `CAP-064` VIII:** the `ai-sessions/0057` pull-to-refresh, Debug and (i) checks (old AZ-9 … AZ-14, now BA-12 … BA-17).

**Not in this session:** lead L-2 (the HID descriptor) was settled from `CAP-033`'s existing bytes (🟢, `PROTOCOL.md` §6 2026-09-30) and needs no capture; lead
L-4 (the Dosimeter stream) is not in scope (the maintainer, chat 2026-09-30, "Not now — note only"). The official Pixel Buds app is not used.

---

## A.0. Which phone, which app, which build

| Item | Value |
|---|---|
| Phone | **Pixel 9a, GrapheneOS** (Settings → About phone → build number: `__________`) |
| App under test | **OpenControl for Pixel Buds**, debug APK of the commit that completes `ai-sessions/0059`. Commit hash: `__________` (`git log -1 --format=%H`) — **also say it aloud on film** (P1) |
| Official Pixel Buds app | **Not used.** Pixel 7a: **Bluetooth off** for the whole session |
| Other Bluetooth devices | Watch, car, speaker: off or out of range; write down anything that connects anyway |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` (anything else is Safe Mode — stop and report) |

## A.1. Log Metadata (fill in)

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-065`                     |
|      Group(s)    |                        BA                          |
|       Date       |                                                     |
| Firmware version | (from the Connection tab "Firmware: …")             |
|   Test device    | Pixel 9a, GrapheneOS, build `…`; OpenControl commit `…`; Google Play services present: yes/no; Play services *Nearby devices*: allowed/denied (P4) |
| Video file       | `CAP-065-recording.mp4` — first/last overlay time: … / … |
| Log file         | `CAP-065-btsnoop_hci.log` — raw path used (`FS/data/log/bt/…` or `FS/data/misc/bluetooth/logs/…`) or `btsnooz.py`: … |
| App debug export | `CAP-065-debug-export.log` (Android's "save as" dialog — the whole log; Debug mode on from …) |
| App logcat       | `CAP-065-OpenControl-for-Pixel-Buds-log-<id>.txt` |
| System log       | `CAP-065-System-log-<id>.txt` |
| Events file      | `CAP-065-events.txt` (your own time + action notes, P6) |
| Clock offsets    | Film ↔ phone: minute change filmed at start (`__:__`) **and** end (`__:__`) — measured in the analysis |

## A.2. Preparation — before filming

The lessons of `CAP-063`/`CAP-064` are marked **(CAP-063)**.

| # | Check | Done |
|---|---|---|
| P1 | Build the debug APK of the commit in A.0 (`cd android && ./gradlew assembleDebug`), install it (`adb install -r app/build/outputs/apk/debug/app-debug.apk`). Write the hash in A.0 **and say it aloud on film at the start** **(CAP-063: not recorded)** | ☐ |
| P2 | Do **not** clear the app's data before the run (A5/(E)/B4 at the end revoke and re-pair on purpose) | ☐ |
| P3 | Developer options: **Bluetooth HCI snoop log = Enabled**, **USB debugging = on**; switch Bluetooth **off and on on film** (the log starts after a toggle) | ☐ |
| P4 | Google Play services → Permissions → *Nearby devices*: write **allowed / denied**, **say it aloud on film**, leave it **(CAP-063: not recorded)** | ☐ |
| P5 | Buds and case charged (> 50 %); buds in the case, lid closed, at the start | ☐ |
| P6 | **Events file** ready (notes app on another device, or paper): phone time (HH:MM:SS) + step ID for every action **(CAP-063: none)** | ☐ |
| P7 | **Camera** on a tripod films the phone screen **and** the case/buds **and your head** in one frame; for II, the ears and the table must be visible — turn your head to the camera when a bud goes in or out **(CAP-063: "worn" never on film)**. No location/address overlay | ☐ |
| P8 | Camera microphone on; **say aloud** what you hear. The phone's own audio is not on the film **(CAP-063)** | ☐ |
| P9 | **Do Not Disturb on**; open the notification shade only when a step asks **(CAP-063: names and an e-mail address were readable in the shade)** | ☐ |
| P10 | Music you know, playable offline | ☐ |
| P11 | GrapheneOS Settings → Security → *Bluetooth auto-off timeout*: write the current value `______` (K5) | ☐ |
| P12 | Laptop with `adb` next to you for A.6 (bug report within a minute of the last action) | ☐ |
| P13 | Read this whole file once before starting | ☐ |

**Rhythm for every step:** wait ~5 s → note the time → **one** action → wait 5–10 s → next step. Something unexpected: **stop, say it aloud, write the time**.

## A.3. Start of the film

| Step | Action | Expected on screen | Time (phone) | Result |
|---|---|---|---|---|
| S0 | Film the status bar across a **minute change** (≥ 10 s before and after). Say aloud: date, "CAP-065, Group BA", the commit hash, Play services *Nearby devices* state | — | | |
| S1 | Force-stop OpenControl (Settings → Apps → Force stop). Quick Settings: Bluetooth **off**, then **on** (starts the HCI log). Do **not** open the app yet | — | | |
| S2 | Open OpenControl; top bar **bug icon → Debug**: **Debug mode on**; back | "Unidentified frames (n)" appears | | |

## A.4. Steps

"Expected on the wire" is for the analysis. DLCI 0x02 = the app's session (MAESTRO), 0x04 = the Message Stream (claimed on demand).

### I. The `ai-sessions/0059` app fixes

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| BA-1 | buds in the case, lid closed, app on the Sound tab | Open the lid, take both buds out, put them **in your ears** (on film). Watch the Sound tab from the moment the session opens until the EQ shows "read HH:MM:SS" | the five EQ sliders are **disabled** (greyed) until the EQ is read, then enabled at the Buds' values; the presets are enabled throughout (A58-APP-02) | app `SABM` 0x02 → the Buds' `GetSoftwareInfo` → `ReadSetting 4:16` → `RESPONSE 4:{16:…}` | | |
| BA-2 | ready, buds worn | Pull down Quick Settings **within 3 s of "ready"**; read the ANC tile aloud; tap it once | subtitle shows a mode or "Tap to switch" — **never "Open the app"** while the app shows ready (A58-APP-01); the tap switches the mode | one DLCI 0x04 claim: `08 11` → `08 13 … e8 …` → `08 12 …` → ACK | | |
| BA-3 | ready | Force-stop the app; reopen; Connect if it does not connect by itself; **immediately** pull down Quick Settings | as BA-2: the tile follows the session at once, even before the snapshot's ANC report | — | | |
| BA-4 | ready | *(watch only, nothing to provoke)* After the run, search the debug export for "answered with a value this app cannot read" and "late WriteSetting answer" | — | if either line exists: the `ReadSetting`/`WriteSetting` frame and its answer in the HCI log | | |

### II. The Settable byte with the ears visible (ADR-049)

Pre-state for the section: ready, ANC tab. For each step say aloud where each bud is. **Ears and table in view.**

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| BA-5 | both worn | Take both buds out and lay them on the table **in view**, case open and empty; wait 10 s (the session may re-open by itself — wait for "ready"); ANC tab → **Refresh** | say which: the "ANC can only be changed while you wear the Buds…" line appears or not | `08 11` → `08 13 00 04 01 e8 <Settable> <mode>` — 🟡 predicts `00`; `CAP-048` 11939 read `e8` in this situation | | |
| BA-6 | both on the table | Put the **Left** bud into its slot in the open case (Right stays on the table, not worn); wait 10 s; **Refresh** | as BA-5 — say which | as BA-5 — 🟡 predicts `00`; `CAP-045` 612 read `e8` in this situation | | |
| BA-7 | Left docked, Right on the table | Close the lid **only if** the Right bud is outside it (it is); wait 10 s; open the lid; **Refresh** | as BA-5 | as BA-5 | | |
| BA-8 | — | Put both buds in your ears (on film); **Refresh** | no "can only be changed…" line | Settable `e8` | | |

### III. Lead L-1 — which channel do the Buds announce?

Read the channel from the debug export line "Maestro channel announced by the Buds: N" (and the HCI log, `scripts/pwrpc_decode.py`: the first `GetSoftwareInfo`
`RESPONSE` with `call_id 0xFFFFFFFF`).

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| BA-9 | ready | **Disconnect**. Put **both** buds in the case, close the lid, wait 15 s (the link drops) | "Paired — not connected …" | ACL `Disconnection Complete` | | |
| BA-10 | both docked, lid closed | Open the lid, take **only the Left** bud out (Right stays docked); **Connect** | ready | the announcement's `channel_id`: 🟡 predicts **19** (or 24) — Left Bluetooth core | | |
| BA-11 | Left out, Right docked | **Disconnect**; Left back in, lid closed, 15 s; open, take **only the Right** out; **Connect** | ready | 🟡 predicts **21** (or 26) — Right Bluetooth core. A different pairing refutes "the channel names the hosting bud" | | |

### IV. Robustness steps moved from `CAP-064` VI (`APP_TESTPLAN.md`) — destructive steps last

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| F7 | buds worn, ANC tab | Tap **ACTIVE**, press Home **immediately**; come back after 5 s | ACTIVE applied (audible), no stuck state | `08 12 … 08` → ACK; later `DISC` 0x04 | | |
| K4 | ready | Rotate the phone; dark mode on and off | nothing lost, no crash | — | | |
| K1 | ready | Quick Settings: **Bluetooth off** | "Bluetooth is disabled." + Enable Bluetooth; no crash | — | | |
| K2 | after K1 | Bluetooth on; Connect if it does not connect by itself | ready | app `SABM` 0x02 | | |
| K3 | ready | Walk out of range (another room) for 30 s, come back | a clear loss text, then ready again | ACL loss; re-open | | |
| L2 | — | Bug icon → Debug: **Export debug log** → save as `CAP-065-debug-export.log` | "Debug log saved (N lines)." | — | | |
| L3 | — | **Debug mode off**, export again (a second file) | no hex lines | — | | |
| K5 | — | *(optional, last before K-destructive)* auto-off timeout to the shortest (P11), lock, wait, unlock, open the app; restore the timeout | "Bluetooth is disabled." (normal), no crash | — | | |
| A5 | — | *(destructive, end)* Settings → Apps → OpenControl → Permissions → *Nearby devices*: **Don't allow**; open the app | "Bluetooth permission needed" / "You denied the permission." + Allow; allow again afterwards | — | | |
| (E) | — | *(destructive, end)* Forget the Buds in Android's Bluetooth settings; open the app | "No Pixel Buds Pro 2 paired yet." + Pair a device | — | | |
| B4 | after (E) | Open the lid; tap **Pair a device twice quickly** | no second picker, no crash; then pair normally | — | | |

### V. Material 3 overhaul, pull to refresh and the settings re-read (moved from `CAP-064` VIII, `ai-sessions/0057`)

Run section V **before** the destructive steps A5, (E), B4 of section IV (do IV in order up to K5, then V, then A5, (E), B4).

Pre-state: ready, both buds worn, music playing. Wait ≥ 5 s between pulls and **say each pull aloud** with the tab name. The detailed screen checks are
`APP_TESTPLAN.md` section O; this section is what the **HCI log** must show.

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| BA-12 | Controls tab | **Pull down** once | spinner, then the (i) "read HH:MM:SS" times move | DLCI 0x02 exactly **seven** `ReadSetting` requests `4:2, 4:4, 4:7, 4:12, 4:17, 4:19, 4:22` in that order, each answered before the next (≤ 2 s), **nothing else** from the app (no write, no `SubscribeRuntimeInfo`, no DLCI 0x04 claim) | | |
| BA-13 | Sound tab | **Pull down** once | spinner; EQ and settings times in the (i) move | `ReadSetting 4:16` first, then the same seven reads, in order, nothing else | | |
| BA-14 | Connection tab, then Find tab | **Pull down** on each | new battery times or "No new battery reading…" | per pull: one DLCI 0x04 claim (`SABM` → `03 03 …` → `08 11`/`08 13`) released ≈ 1.5 s later, and one `SubscribeRuntimeInfo` on DLCI 0x02 — as a *Refresh battery* tap (E8) | | |
| BA-15 | ANC tab | **Pull down** | the (i) "updated" time moves | one DLCI 0x04 claim with `08 11` → `08 13`, as the Refresh button | | |
| BA-16 | — | **Disconnect**, then pull on Controls | the app connects | `SABM` DLCI 0x02, then the normal Connect sequence (EQ read, the seven reads, subscription, the snapshot claim) — once | | |
| BA-17 | ready | Bug icon → Debug → system back; tap a few (i)s | Debug full screen, back to the tab; dialogs open/close | **nothing** on any channel | | |

**Refuted if (section VIII):** a pull sends anything other than its tab's action; a read is repeated in the same pass (a retry) or out of order; a pull
sends anything while the app is still connecting; opening Debug or an (i) puts anything on the wire.

### Z. Restore

| Step | Action | Time | Done |
|---|---|---|---|
| Z1 | After B4: pair again, Connect; allow *Nearby devices* again (A5); ANC as you like it | | ☐ |
| Z2 | Film the status bar across a **minute change** again, then stop the film | | ☐ |

## A.5. What you must not do

- Do not use the official Pixel Buds app, or the Pixel 7a's Bluetooth, during this session.
- Do not tap Connect where a step does not ask for it (BA-1, BA-5): those test the automatic behaviour.
- Do not do two actions in one step; do not skip the pauses.
- Do not open the notification shade unless a step asks (DND is on) — the Quick Settings steps (BA-2, BA-3, K1, K5) are the only ones.
- Do not run A5, (E) or B4 before section V is done.

## A.6. After the run — within 1 minute of the last action

| # | Collect | Done |
|---|---|---|
| X1 | Bug icon → Debug → **Export debug log** (Debug mode on) → save as `CAP-065-debug-export.log` (L2 made one earlier; export again now) | ☐ |
| X2 | `adb bugreport cap065`; the raw snoop log from `FS/data/log/bt/btsnoop_hci.log` or `FS/data/misc/bluetooth/logs/btsnoop_hci.log` (else `btsnooz.py`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3) → `CAP-065-btsnoop_hci.log`; write the path in A.1 | ☐ |
| X3 | App logcat and system log (GrapheneOS log viewer) | ☐ |
| X4 | Film → `CAP-065-recording.mp4`; its first/last overlay time in A.1 | ☐ |
| X5 | Everything, with this file and the events file, into this folder; rename the folder; `sha256sum *` | ☐ |

## A.7. Analysis checklist

- [ ] Integrity pre-flight (`capinfos`, `cap_len≠len`, out-of-order, `sha256sum`); audio track present or not.
- [ ] Pre-filter by the Buds' classic ACL handle (map it to the Buds' address via HCI Connection Complete) — `bluetooth.addr` is empty with this
      encapsulation (`AGENTS.md` §13.1).
- [ ] Film ↔ phone offset from the two filmed minute changes; logcat/system log UTC offset.
- [ ] A negative needs a positive control (`AGENTS.md` §13 step 8): every "0 frames" with its command, exit status and a filter that matches a known frame.
- [ ] I: BA-1 film frames of the Sound tab against the `ReadSetting 4:16` answer time; BA-2/BA-3 tile subtitle against `connectionState` in the export.
- [ ] II: the Settable byte of each `Notify` against the film (which bud is where, ears visible) — a counter-example either way is the result (ADR-049 🟡).
- [ ] III: `python3 scripts/pwrpc_decode.py CAP-065-btsnoop_hci.log | grep GetSoftwareInfo` — the `ch=` of each announcement against which bud was out.
- [ ] IV/V: as `CAP-064`'s old sections — `APP_TESTPLAN.md` sections K, L, A, B, E and O.
- [ ] Traceability (`AGENTS.md` §13 step 7): every step has a timeline row or an explicit "skipped". Test-IDs: `ANC-001`–`ANC-004` (BA-2/3, BA-5–8, F7),
      `PAIR-001` (B4, Z1), `PAIR-003` (BA-9–11, K2), `CASE-004`–`CASE-006` (BA-6, BA-9–11), `BATT-004` (BA-14), `EQS-001`/`EQP-002`
      (BA-1, BA-13), `AUDIO-001`/`AUDIO-003`, `HOLD-001`, `INEAR-001` (BA-12 reads).

**Refuted if:**
- the ANC tile shows "Open the app" while the app shows the session ready (BA-2/BA-3);
- an EQ slider is enabled before the EQ is read, or a slider write leaves while the EQ is unread (BA-1);
- a `Notify` reads Settable `0x00` while a bud is visibly in an ear, or `0xe8` while no bud is in an ear (BA-5 … BA-8) — the latter refutes ADR-049's 🟡;
- the announced channel does not follow the bud that is out (BA-10/BA-11) — refutes L-1's "names the hosting bud" (the address derivation itself stays as tabulated);
- any app frame on DLCI 0x08; any app `SABM` 0x02 while the app is not visible; a pull sends anything other than its tab's action (V);
- a settings value changes on screen without the Buds' `RESPONSE`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-065-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BA/CAP-065-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-065-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BA/CAP-065-EVENT-NOTES
