# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the Owner user **with** Google Play services and the secondary user without) — Group BK, 1.2.1: the "Noise-control channel" switch (`CAP-075`)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0087`, 2026-10-12). The hardware run of **1.2.1** (versionCode 10201) — the fix
of `CAP-074`'s finding (`ai-sessions/0086`): in a user with Google Play services, Play's Fast Pair takes the noise-control channel back ≈ 5 s after this app
opened it on the noise-control tab, so only the first press-and-hold after a Connect was shown. 1.2.1 adds the Settings tab's **"Noise-control channel"**
choice (`DECISIONS.md` ADR-062): **"Show a hint, do not take it back"** (the default — the card says the channel was taken and when) and **"Take it back while
the noise-control tab is open (may fight that app)"** (a re-claim 3, 6 and 12 s after the first, second and third foreign close of one visit of the tab, then the
hint). Nothing new on the wire: the re-claim is the existing claim (`SABM` DLCI 0x04, `08 11 00 00`, the phone's `DISC`). **Two parts, ≤ 20 minutes on film**
(the maintainer's choice in chat 2026-10-12, "CAP-075 scope" → "Both users"): **part 1** the Owner user (user 0) with Play services — both modes, the system log
pulled within a minute of the first cut, the APK pull, the Nearby-devices permission state on film; **part 2** the secondary user (user 10) without Play — the
regression of `CAP-073` §4 in both switch positions (nothing contends: the hold lives until the tab is left, no re-claim ever). The maintainer films; a later
CAPTURE session analyses it and gives the release verdict (`RELEASING.md` C3/C4). After the run: rename this folder to the screen recording's first/last phone
times and the recording to CAP-075-recording.mp4.

**Not in this run** (say so if asked): the hold's other untested branches of `TODO.md` §2 (the tile on the tab, Home/return, a loss with the tab shown) — welcome
if time allows (BK-19 … BK-21, optional), not required for the verdict; the case-sound experiment; the wear sequences.

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-075` |
| Group(s) | BK (new) |
| Date | TBD |
| Phone | Pixel 9a, GrapheneOS (Android 17, `CP3A.261005.005` in `CAP-074`) — **part 1: the Owner user (user 0) with Google Play services** (`CAP-074`'s user); **part 2: the secondary user (user 10) without Google Play** (`CAP-073`'s user) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.2.1** (versionCode 10201), the release APK of `scripts/release.sh 1.2.1` (B1), kept as `~/opencontrol-1.2.1-tested` (B3) — **Info tab on film in each user**: "App: 1.2.1, build <B1 hash> (<date>)", no "-dirty"; APK SHA-256 and certificate SHA-256 (= `README.md` / `SECURITY.md`) from the script's output; **the installed APK pulled** (P1) |
| Previous version in each user | 1.2.0 build `5b4d5db` (installed 2026-10-10; one APK serves both users, `CAP-073-FINDINGS.md` §0) — the update is from it (`adb install -r`, no uninstall) |
| Google Play services (part 1) | present and active (`com.google.android.gms`, uid 10205 in `CAP-074`); **its *Nearby devices* permission state on film** (P0 — `TODO.md` §3's standing item, never recorded so far) and its version (Settings → Apps → Google Play services, on film) |
| Official Pixel Buds app | not used (not installed in either user in `CAP-073`/`CAP-074`) |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` expected (any other ⇒ Safe Mode: say so, stop) — both buds **worn** for every hold step (the Settable byte `e8`; a `00` means no bud worn and the taps would be refused, ADR-049) |
| Video files | TBD — the screen recording **with sound** for the whole run (the recorder's audio on: the maintainer says what they do, as in `CAP-074`); no camera needed (the holds are the maintainer's statement + the wire, as `CAP-073`/`CAP-074`) |
| Log files | TBD — the HCI snoop logs, the app's debug exports (one per part, Debug mode on), the app logcat, **the system log pulled within one minute of the first cut of part 1** (P6 — `CAP-074-FINDINGS.md` §10's first open question: the Bluetooth process's `RFCOMM_CreateConnectionWithSecurity: already at opened state` / `cleanup_rfc_slot` / `RfcommEventStreamMedium` lines are kept only ≈ 1–3 minutes), the P0/P1 outputs |

## Preparation

| # | Check | Done |
|---|---|---|
| P0 | **In each user**: `adb shell am get-current-user`; `adb shell pm list packages --user <id> \| grep -i -E "gms\|vending"; echo "exit=$?"` (part 1: Play listed, exit 0; part 2: nothing, exit 1); positive control `grep opencontrol` (exit 0). **Part 1 on film:** Settings → Apps → Google Play services → Permissions → *Nearby devices* (allowed / not allowed) and the app's version line. Save the outputs **with the exit statuses** | ☐ |
| P1 | In the installed 1.2.0 (`5b4d5db`): Dark mode and Debug mode as you like them, **Debug mode on** (on film). `adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds \| grep -E "firstInstallTime\|lastUpdateTime\|versionCode"` → save. `adb install -r ~/opencontrol-1.2.1-tested/opencontrol-pixelbudspro2-1.2.1.apk` (one APK serves both users) — **no uninstall**; the same `dumpsys` → save. After the run: `adb shell pm path --user <id> io.github.tedsluis.opencontrolpixelbuds` → `adb pull` → `sha256sum` against `~/opencontrol-1.2.1-tested` (`CAP-074` §0's ⚪: the installed APK's hash) | ☐ |
| P2 | HCI snoop log on (set in the Owner; it stays on across the user switch) | ☐ |
| P3 | No camera (both parts are screen recording + wire); the buds and the phone within reach | ☐ |
| P4 | Android's **screen recorder with sound** on for each part (one recording per user, or one across the switch) | ☐ |
| P5 | The status bar across a minute change at the start and at the end of each recording | ☐ |
| P6 | **The system log of part 1 within one minute of the first cut** (BK-5): `adb bugreportz` or the GrapheneOS log viewer right after BK-5's 30 s — the stack lines of the cut are retained only briefly (`CAP-074`: the `main` buffer reached back 75 s). A second system log at the end of the run. Each `adb bugreportz` sends signal 3 to every Java process — not an app fault (`CAP-071-FINDINGS.md` §0) | ☐ |
| P7 | **Do Not Disturb on** | ☐ |
| P8 | Buds charged, **both worn** before BK-4; Android shows them connected before the app is opened | ☐ |
| P9 | **No app use before the film** — not opened between P1's install and BK-1 | ☐ |

**Rhythm:** one action, then 5–10 s; say what you do ("left hold", "right hold", "tap", "switch"). Export the debug log at the end of each part. Serials: never
unredacted in any note (first 4 + last 2 characters).

## Steps

DLCIs are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05). Every MAESTRO request starts `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25` on
**channel 19** or `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25` on **channel 21** (Info → "Control channel"). **Reference frames** — "B74" = `CAP-074-btsnoop_hci.log`
(the Owner user with Play, 1.2.0 `5b4d5db`, handle `0x000b`), "B73" = `CAP-073-btsnoop_hci.log` (user 10 without Play, the same build), re-derived by
`ai-sessions/0087` with `tshark -r <log> -Y "frame.number==N" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type
-e data.data` (exit 0):

| Request / event | Reference (channel 19 unless said) |
|---|---|
| The Connect: `SABM` DLCI 0x02, then `ReadSetting 4:16`, 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29, `SubscribeRuntimeInfo`, `GetHardwareInfo` | B74 15741 (`SABM`, 06:42:29.897), 15758 `7e003b0310131dea71de7d5e2551aed0ae2a022010fdae3f247e` (4:16), 15770 … 15832 (the twelve settings, ascending), 15844 `…90821ee6602d65a97e` (runtime info), 15846 `7e003b0310131dea71de7d5e25e3a5ec28ff1ebbb77e` (`GetHardwareInfo`) — byte-identical to `CAP-073` B73 3304–3376 (`CAP-074-FINDINGS.md` §2); channel 21 as `CAP-073-EVENT-NOTES.md`'s table |
| The snapshot claim after a Connect (user 0): attempt 1 fails, attempt 2 connects | B74 14450 (Play's claim killed, `DISC`), 14500 `SABM` 06:40:51.247, 14525 `Notify` `0813000401e8e840` (ADAPTIVE, `e8`) |
| A tap on the held channel (no second `SABM`) | B74 14552 `0813000401e8e840` (the `Get` answered) → 14553 `0812001401e8e880` + 16 × `00` (`Set` TRANSPARENT) → 14555 `ff010006081201e8e880` (ACK) → 14556 `0813000401e8e880` |
| **The cut** (Play's recovery; the stack closes the app's port) | the phone's `DISC` of the app's DLCI 0x04 **without** a "released" line in the export — B74 14577 06:40:55.922, **4.68 s** after `SABM` 14500 (5.22 s after Play's loss 14450); export: "RFCOMM on-demand channel 0x04 closed (IOException: bt socket closed, read return: -1)" + "Message Stream hold ended: the channel was closed, not by this app" |
| Play's retry after a cut | Play's `SABM` with `03 08 00 02 01 25` as its first message, 2.2–2.6 s after the cut — B74 14624 06:40:58.217 (2.29 s after 14577) |
| The shown hold (the unprovoked `Notify` inside the app's claim) | B74 15853 `0813000401e8e840` (the snapshot's answer) → **15860 `0813000401e8e880`** (06:42:33.204, no `08 11`/`08 12` before it) → shown TRANSPARENT (film t=138) → the cut 15909 (06:42:35.842) |
| A hold that reaches Play (not the app) | an unprovoked `Notify` on Play's claim — B74 15986 … 16004 (between Play's `SABM` 15931 and its `DISC` 16078); nothing on screen |
| **The re-claim (mode "Take it back", 1.2.1)** | a new app `SABM` + `08 11 00 00` ≈ **3.0 s** after the cut (then ≈ 6 s, ≈ 12 s after the next cuts), attempt 1 failing (Play on the channel) and attempt 2 connecting ≈ 0.4 s later; export: "Message Stream re-claim N/3 after a foreign close, D ms later (ADR-062)" then "hold started"; after the third: "Message Stream re-claim stopped after 3 re-claims — …" and the hint on the card. **No reference capture exists — this run makes it** |
| The hold in the user without Play (part 2) | B73 3484 `SABM` 08:27:59.763 → 3492 `08110000` → 3503 `0813000401e8e808`; the unprovoked 3531 `…40`, 3543 `…80`, 3546 `…08`, 3547 `…40` (four holds, each shown); **no `DISC` for 30.97 s**; the phone's `DISC` 3654 08:28:30.890, 1.509 s after leaving the tab |

Anything else from the app on the wire (a request not in this table or in `CAP-074` §2/§3, a second `GetHardwareInfo` in a session, anything on DLCI
0x08/0x0a, a claim while the tab is **not** shown that is not a tap/pull/Connect) is a finding.

### Part 1 — the Owner user (user 0) **with** Google Play services (screen recording with sound; `APP_TESTPLAN.md` X1–X9; `PAIR-003`, `BATT-004`, `FW-003`, `ANC-001`…`ANC-004`, `TOUCH-007`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BK-1 | P0–P9 done, user 0, both buds worn | open the app | the session opens by itself (ADR-044); "Probably worn"; Left/Right %, the Case | DLCI 0x02 `SABM`, the announcement, the thirteen requests in order, `GetHardwareInfo` last, answered; the snapshot claim: attempt 1 fails ≈ 100–360 ms (Play on the channel), attempt 2 connects; `03 03` burst; `08 11` → `Notify`; the claim released ≈ 1.5 s after its answer **or** cut by Play ≈ 5 s after the `SABM` (the Connection tab is shown — either is fine here) | `PAIR-003`, `BATT-004` | a crash; a request missing or out of order; Safe Mode |
| BK-2 | ready | gear → **Info**: hold 3 s on the build line, the firmware, the serials; **Settings**: the "Noise-control channel" card — hold 3 s on it, open its (i), read it, close | "App: 1.2.1, build <B1 hash>", no "-dirty"; `release_5.203` ×3; Case / Right bud / Left bud; the card between Dark mode and Case sounds, "Show a hint, do not take it back" selected; the (i)'s four lines | nothing | `FW-003` | "-dirty"; another hash; the card missing or the other row selected on a fresh update |
| BK-3 | back on Connection | **Disconnect**, wait 5 s, **Connect** | ready | the phone's `DISC` of DLCI 0x02; the thirteen requests again; the snapshot claim (attempt 1 fails, attempt 2 connects) | `PAIR-003` | — |
| BK-4 | ready, the **Connection** tab | open the **ANC** tab at once (within 1.5 s of "ready", so the snapshot claim becomes the hold) — or, if later, a new claim | the mode; (i) not needed yet | the hold on the snapshot claim (no new `SABM`) or one new `SABM` + `08 11`; export "Message Stream hold started: the noise-control tab is on screen (ADR-061)" | `ANC-001`…`004` | no hold line |
| **BK-5** | the hold open, mode **hint only** | **press and hold a bud every ≈ 5 s for 30 s** (6 holds); say each; touch nothing on the phone; **then P6 at once** (the system log within a minute of the first cut) | the **first** hold (if inside the ≈ 5 s) changes the mode on screen; **then the hint line appears below the mode buttons:** "The noise-control channel was taken by another app at HH:MM:SS (for example Google Play services' Fast Pair). A change made on a bud is not shown until you tap a mode, Refresh, or pull down." — the later holds change the mode in the ears, **not** on screen; **no new claim** by the app for the rest of the 30 s | the cut ≈ 4.7–5.1 s after the app's `SABM` (the phone's `DISC`, no "released" line; export "closed … read return: -1" + "hold ended: the channel was closed, not by this app" + **"Noise-control channel taken by another client: the hint is shown, no re-claim (ADR-062, hint only)"**); Play's `SABM` 2.2–2.6 s later; every later `Notify` on Play's claim; **no app `SABM` until BK-6** | `TOUCH-007` | an app `SABM` after the cut without a tap (a re-claim in hint-only mode — the M1 defect); no hint line; the hint without a time |
| BK-6 | the hint shown | open the ANC **(i)**, read it, close; then **tap a mode** | the (i) has the hint line and ends with the corrected sentence ("… loses it to this app and may take it back about 5 seconds later; what the app does then is set on the Settings tab under Noise-control channel."); after the tap: the mode after its ACK, **the hint line gone**; ≈ 5 s later the hint is back (the new claim is cut again) | the tap's claim: attempt 1 fails (Play on the channel), attempt 2 connects, `08 11` → `Notify` → `Set` → ACK; held; the cut ≈ 5 s later; the hint-only line in the export again | `ANC-001`…`004` | the hint not cleared by the tap; the old (i) sentence "cannot use it meanwhile" |
| BK-7 | the hint shown | **pull down** on the ANC tab (Refresh) | the hint gone, the mode read; ≈ 5 s later the hint is back | a new claim with `08 11` → `Notify`; the cut | — | the pull not clearing the hint |
| BK-8 | the ANC tab | gear → Settings → **"Take it back while the noise-control tab is open (may fight that app)"**; back to the **ANC** tab | the row selected; back on the tab a new claim (the menu covered the tab: the hold ended, a re-entry) | the `DISC` ≈ 1.5 s after the gear opened (the tab left), a new `SABM` + `08 11` on return | — | the choice not kept (a re-open of the menu shows "hint only") |
| **BK-9** | the hold open, mode **take it back**, both buds worn | **press and hold a bud every ≈ 4 s for 60 s** (≈ 15 holds); say each; touch nothing; count aloud which ones change the mode on screen | the mode changes on screen for the holds that fall in the app's windows (≈ 5 s of every ≈ 8.5 s during the chain: expect ≈ 6 of 10 shown, the others not); **after the third re-claim's cut** (≈ 37 s after the first cut) **the hint line with "; the app took it back 3 times and stopped"**, and no further change on screen for the rest of the 60 s | the cut ≈ 5 s after the hold's `SABM`; **the re-claim `SABM` + `08 11` ≈ 3.0 s after the cut** (attempt 1 fails: Play's retry had taken the channel; attempt 2 connects ≈ 0.4 s later); the cut again ≈ 5.2–5.7 s after Play lost its socket; the re-claim ≈ 6 s after that cut; the cut; the re-claim ≈ 12 s after; the cut; **then no app `SABM`**; export: "Message Stream re-claim 1/3 after a foreign close, 3000 ms later (ADR-062)", "… 2/3 … 6000 ms", "… 3/3 … 12000 ms", "Message Stream re-claim stopped after 3 re-claims — …"; Play's `SABM` 2.2–2.6 s after each cut (its retry) | `TOUCH-007`, `ANC-001`…`004` | a re-claim sooner than 2.8 s or later than 3.3 s after the cut (the first step); a fourth re-claim; the hint missing or without "3 times"; a re-claim while the tab is not shown |
| BK-10 | the hint shown (after the cap) | **tap a mode** → hold a bud twice ≈ 4 s apart → wait 10 s | the hint gone; the holds inside the app's windows shown; the chain restarted (a re-claim ≈ 3 s after the next cut) | the tap's claim; the cut; **the re-claim at ≈ 3 s again** (a new chain after the tap) | `ANC-001`…`004`, `TOUCH-007` | no re-claim after the tap's claim was cut (the counter not reset) |
| BK-11 | the hold or a re-claim pending | **tap the Sound tab within 2 s of a cut** (while the 3-s delay runs); wait 10 s; back to **ANC** | Sound; on return a new claim | after the tab change: **no `SABM` 3 s after the cut** ("Message Stream re-claim cancelled (ADR-062)" in the export), the phone's `DISC` if a claim was open; on return a new `SABM` + `08 11` | — | a re-claim while Sound is shown |
| BK-12 | the ANC tab, the hold or the chain running | **Home** within 2 s of a cut; wait 10 s; return to the app | the home screen; on return the ANC tab with a new claim | no `SABM` while hidden ("re-claim cancelled"); the `DISC` ≈ 1.5 s after Home if a claim was open; on return a new claim | — | a `SABM` while the app is hidden |
| BK-13 | the chain running (within 2 s of a cut) | gear → Settings → **"Show a hint, do not take it back"**; back to ANC | the row selected; on return the hint behaviour of BK-5 (the first hold shown, then the hint, no re-claim) | "re-claim cancelled" at the switch; no `SABM` 3 s after that cut; the return's claim; its cut; no re-claim | — | a re-claim after the switch to hint only |
| BK-14 | the ANC tab, hint only | **Disconnect**; wait 5 s; **Connect**; stay on the ANC tab | not connected, then ready; the hint gone at Connect | the thirteen requests; the snapshot claim becomes the hold; its cut ≈ 5 s later; the hint | `PAIR-003` | the hint surviving the Connect |
| BK-end1 | — | Debug → **Export**; a second system log; the status bar across a minute change; stop the recording; **switch to user 10** | "Debug log saved (N lines)." | — | — | an empty file |

### Part 2 — the secondary user (user 10) **without** Google Play (screen recording; `APP_TESTPLAN.md` X10–X12; the `CAP-073` §4 regression)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BK-15 | user 10, P0 (no Play), both buds worn, the recording on | open the app; **Info** 3 s; Settings: the "Noise-control channel" card (hint only, the default) | the session opens by itself; "App: 1.2.1, build <B1 hash>"; the card as in BK-2 | the Connect sequence (`CAP-073`'s bytes); the snapshot claim released ≈ 1.5 s after its answer (the Connection tab shown) | `PAIR-003`, `FW-003`, `BATT-004` | Safe Mode; "-dirty" |
| **BK-16** | ready, hint only | open the **ANC** tab; **press and hold a bud every ≈ 5 s for 30 s**; touch nothing | **every** hold changes the mode on screen (`CAP-073` §4: four of four); **no hint line** | one `SABM` + `08 11`; **no `DISC` for the 30 s**; the unprovoked `Notify` of each hold on the app's claim; nothing sent by the app; 0 Play-services claims (`frame contains 03:08:00:02:01:25` → 0 in this part) | `TOUCH-007`, `ANC-001`…`004` | a `DISC` while the tab is shown; a hold not shown; the hint line |
| BK-17 | the hold open | gear → Settings → **"Take it back …"**; back to **ANC**; **press and hold a bud every ≈ 5 s for 30 s** | every hold shown; **no hint, no re-claim** (nothing took the channel) | the `DISC` ≈ 1.5 s after the gear; one new `SABM` + `08 11` on return; **no further `SABM`, no `DISC`** for the 30 s; no "re-claim" line in the export | `TOUCH-007` | any re-claim line or extra `SABM`; a `DISC` while the tab is shown |
| BK-18 | the hold open, take it back | tap the **Controls** tab; wait 5 s | Controls | the phone's `DISC` ≈ 1.5 s after the tab change ("hold ended: the noise-control tab was left"); nothing else | — | no `DISC`, or one before 1.3 s |
| BK-19 (optional, `TODO.md` §2) | back on ANC, the hold open | pull **Quick Settings** down; tap the **ANC tile** once; close | the tile steps one mode; the app follows | `08 11` → `Notify` → `Set` → ACK on the held channel, no `SABM`, no `DISC` | `ANC-001`…`004` | a second `SABM` |
| BK-20 (optional) | the hold open | **Home**; wait 5 s; return | the home screen; ANC again | the `DISC` ≈ 1.5 s after Home ("… the app is not visible"); a new `SABM` + `08 11` on return | — | no `DISC` after Home |
| BK-21 (optional) | the ANC tab shown, both worn | both buds **into the case**; wait 10 s; **out into the ears** | the session ends with its cause; ready again by itself | the loss; the ADR-044 re-open, its snapshot claim **kept** as the hold (no `DISC` while the tab is shown) | `PAIR-003` | the snapshot released while the tab is shown |
| BK-end2 | — | gear → Settings → back to **"Show a hint …"** (the default, as installed); Debug → **Export**; the status bar across a minute change; stop the recording; P1's APK pull | "Debug log saved (N lines)." | — | — | — |

## Don'ts

- Do not uninstall the installed 1.2.0 and do not clear the app's data (P1 — the run is an update; the choice of BK-8 must survive BK-end1's user switch only
  as a stored setting of each user — each user has its own DataStore file).
- Do not open the app before the film (P9).
- In BK-5, BK-9, BK-16 and BK-17 touch nothing on the phone while holding the buds; say each hold.
- Do not tap two things within 5 s of each other, except where a step says "within 2 s of a cut" (BK-11, BK-12, BK-13).
- Do not write a serial number unredacted anywhere in this folder (first 4 + last 2 characters); no Wi-Fi name, phone number, account name or e-mail in any
  committed text (the system log is committed as an extract, as `CAP-074`).

## After the run

Into this folder: the screen recording(s), the two system logs of part 1 and the one of part 2 (as extracts — the full logs and the originals to
`~/opencontrol-capture-originals/CAP-075/`), every debug export, the app logcat if saved, the HCI snoop logs, the P0/P1 outputs with their exit statuses, the
pulled APK's `sha256sum`, `dist/1.2.1/`'s `.sha256` and the script's certificate line; then `sha256sum *` into CAP-075-sha256sum.txt. Before committing: blur
Wi-Fi names, addresses and other device names on film. Commit on the release branch after the build commit (`RELEASING.md` C3) — never on `main`.

## Analysis checklist

- [ ] Every filter scoped to the Buds' ACL handle (the Connection Complete for the Buds' address with that handle shown); DLCIs by content; a negative with its
      command, its exit status and a positive control (`AGENTS.md` §13 step 8).
- [ ] P0 (part 1: Play listed, the *Nearby devices* permission state and Play's version on film; part 2: no Play, exit 1 with the positive control), P1 (the
      update: `firstInstallTime` unchanged, `lastUpdateTime` later, `versionCode=10201`); the Info frame's hash = B1's; the pulled APK's SHA-256 = B1's.
- [ ] **The claim inventory of DLCI 0x04/0x05 with both clients** (`CAP-074-FINDINGS.md` §1's method: the owner by the first phone message — `08 11` the app,
      `03 08 00 02 01 25` Play; every `SABM`/`DISC` with its time; every `Notify` with the claim it arrived on and what the screen showed).
- [ ] **Part 1, hint only (BK-5 … BK-7, BK-13, BK-14):** every cut ≈ 4.7–5.1 s after the app's `SABM` (a comparison with `CAP-074`); **no app `SABM` after a cut
      without a tap, pull, re-entry or Connect**; the hint line on film with the cut's time; the hint cleared by the tap and the pull; the (i)'s corrected
      sentence; the export's "hint is shown, no re-claim" lines.
- [ ] **Part 1, take it back (BK-9 … BK-12):** per chain the three re-claims — their `SABM` **3.0 / 6.0 / 12.0 s after the cut** (window ± 0.3 s from the export's
      "closed" line; the attempt-1 failure and attempt 2 ≈ 0.4 s later), each held until Play's recovery (5.2–5.7 s after Play's loss — say the numbers of this
      Play version), the fourth cut without a re-claim, the "stopped" line and the hint with "3 times"; the holds shown vs lost per window; the new chain after
      the tap (BK-10); **no `SABM` after a tab change, Home or the switch to hint only** (BK-11 … BK-13, "re-claim cancelled"); Play's retry delay after each cut.
- [ ] **The system log of part 1** (P6): the Bluetooth process's lines of the first cut — `RFCOMM_CreateConnectionWithSecurity: already at opened state`
      (Play's connect), `cleanup_rfc_slot … app_uid: <this app's uid>` (the stack closing the app's port), `NearbyDiscovery: RfcommEventStreamMedium …` — the
      🟡 of `CAP-074-FINDINGS.md` §8 (a) settled or not; and Play's own lines at the re-claims (what it logs when the app kills its socket).
- [ ] **Part 2 (BK-16 … BK-18):** 0 Play-services claims (positive control: part 1's count); one claim per tab entry, **no `DISC` while the tab is shown**, every
      hold shown, no re-claim and no hint in either switch position; the `DISC` ≈ 1.5 s after leaving the tab.
- [ ] The optional BK-19 … BK-21 if run (`TODO.md` §2's branches); the system log: crashes, ANRs, `StrictMode`; the `bugreportz` SIGQUITs discounted.
- [ ] One row per step — done / not done / done differently; `APP_TESTPLAN.md` section X and the Summary; traceability (`AGENTS.md` §13 step 7): `PAIR-003`
      (BK-1, BK-3, BK-14, BK-15, BK-21), `BATT-004` (BK-1, BK-15), `FW-003` (BK-2, BK-15), `ANC-001`…`ANC-004` (BK-4, BK-6, BK-9, BK-10, BK-16, BK-19),
      `TOUCH-007` (BK-5, BK-9, BK-10, BK-16, BK-17) — each referenced in the timeline or flagged "expected but not observed".
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `id_registry.csv`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the Group column: BK), `APP_TESTPLAN.md`, `TODO.md` §2/§3;
      write the findings file (CAP-075-FINDINGS); an ADR-062 Update with the numbers; the release verdict is the analysing session's, with the maintainer
      (`RELEASING.md` C4).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-075-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BK/CAP-075-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-075-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BK/CAP-075-EVENT-NOTES
