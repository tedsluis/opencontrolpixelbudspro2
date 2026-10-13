# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the Owner user **with** Google Play services and the secondary user without) — Group BL, 1.2.2: the constant re-claim step, "Take it back again", the cause line and the renamed card (`CAP-076`)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0089`, 2026-10-13). The hardware run of **1.2.2** (versionCode 10202) — the follow-up
of `CAP-075` (`ai-sessions/0088`, the maintainer's choice "P1 + P2 + P4"; `DECISIONS.md` ADR-062 Update 2 and ADR-063): in the option **"This phone has Google
Play services"** (stored as `RECLAIM`, 1.2.1's "Take it back") the app re-claims the noise-control channel **3 s after every cut, up to 20 times** (no 6/12-s
back-off), then shows the hint with "this app took it back 20 times and stopped"; the hint has a **"Take it back again"** button (a claim with its `Get`, no
`Set`); a mode the app's own `Get` finds changed since it last saw it gets the (i) line **"Changed since the app last saw it (read at HH:MM:SS): …"**; the
Settings card is renamed **"Noise control with Google Play services"**, its options say whether the phone has Google Play services, and it shows whether Android
finds Play services on this phone (ADR-063). Nothing new on the wire: every claim is the existing one (`SABM` DLCI 0x04, `08 11 00 00`, the phone's `DISC`).
**Two parts, ≤ 20 minutes on film with sound** (the maintainer's choice in chat 2026-10-13, "CAP-076" → "both users, as CAP-075"): **part 1** the Owner user
(user 0) with Play services; **part 2** the secondary user (user 10) without Play — the `CAP-073` §4 regression in both options and the renamed card. The
maintainer films; a later CAPTURE session analyses it and gives the release verdict (`RELEASING.md` C3/C4). After the run: rename this folder to the screen
recordings' first/last phone times and the recordings to CAP-076-recording1.mp4 / CAP-076-recording2.mp4.

**Expected numbers (from `CAP-075`, one Play version — `CAP-075-FINDINGS.md` §1, §4; `ai-sessions/0089` RESULT §A.1):** a cycle cut → 3.0 s → attempt 1 refused
(0.26–1.05 s; Play's retry, 2.1–2.8 s after the cut, is on the channel) → attempt 2 → the app's `SABM` ≈ 4.5–4.9 s after the cut → held 4.55–4.92 s → the next
cut ⇒ **≈ 9.2–9.5 s per cycle, ≈ 50 % held, for ≈ 3.1 minutes** (20 cycles), then the hint. A press-and-hold every ≈ 4 s lands in the app's window about half
of the time. If Play's timers differ in this run, say the measured ones.

**Not in this run** (say so if asked): the hold's other untested branches of `TODO.md` §2 (the tile on the tab, Home/return, a loss with the tab shown) —
optional at the end of part 2 (BL-22 … BL-24); the case-sound experiment; the wear sequences.

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-076` |
| Group(s) | BL (new) |
| Date | TBD |
| Phone | Pixel 9a, GrapheneOS (Android 17) — **part 1: the Owner user (user 0) with Google Play services** (`CAP-074`/`CAP-075` set 2's user); **part 2: the secondary user (user 10) without Google Play** (`CAP-073`/`CAP-075` set 1's user) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.2.2** (versionCode 10202), the release APK of `scripts/release.sh 1.2.2` (B1), kept as `~/opencontrol-1.2.2-tested` (B3) — **Info tab on film in each user**: "App: 1.2.2, build <B1 hash> (<date>)", no "-dirty"; APK SHA-256 and certificate SHA-256 from the script's output; **the installed APK pulled** (P1 — not done in `CAP-074`/`CAP-075`) |
| Previous version in each user | 1.2.1 build `31ee183` (installed 2026-10-12/13); **the stored choice in both users is "Take it back" (`RECLAIM`)** (`TODO.md` §1, `CAP-075-FINDINGS.md` §4) — after the update it must read as **"This phone has Google Play services"** (ADR-062 Update 2: the stored names keep their meaning) |
| Google Play services (part 1) | present and active (`com.google.android.gms`, uid 10205 in `CAP-074`/`CAP-075`); **its *Nearby devices* permission state and its version on film** (P0 — `TODO.md` §3's standing item, not done in `CAP-075`) |
| Official Pixel Buds app | not used |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` expected (any other ⇒ Safe Mode: say so, stop) — both buds **worn** for every hold step (Settable `e8`; `00` means no bud worn, ADR-049) |
| Video files | TBD — the screen recording **with sound** per part (say each action and each hold aloud, as in `CAP-075`); no camera |
| Log files | TBD — the HCI snoop logs, the app's debug export per part (Debug mode on), the app logcat, **the system log with the GrapheneOS log viewer right after BL-9's chain** (P6 — its main buffer covered every cut in `CAP-075`), the P0/P1 outputs |

## Preparation

| # | Check | Done |
|---|---|---|
| P0 | **In each user**: `adb shell am get-current-user`; `adb shell pm list packages --user <id> \| grep -i -E "gms\|vending"; echo "exit=$?"` (part 1: Play listed, exit 0; part 2: nothing, exit 1); positive control `grep opencontrol` (exit 0). **Part 1 on film:** Settings → Apps → Google Play services → Permissions → *Nearby devices* (allowed / not allowed) and the app's version line. Save the outputs **with the exit statuses** | ☐ |
| P1 | In the installed 1.2.1 (`31ee183`): Debug mode **on** (on film). `adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds \| grep -E "firstInstallTime\|lastUpdateTime\|versionCode"` → save. `adb install -r ~/opencontrol-1.2.2-tested/opencontrol-pixelbudspro2-1.2.2.apk` (one APK serves both users) — **no uninstall**; the same `dumpsys` → save. After the run: `adb shell pm path --user <id> io.github.tedsluis.opencontrolpixelbuds` → `adb pull` → `sha256sum` against `~/opencontrol-1.2.2-tested` | ☐ |
| P2 | HCI snoop log on (set in the Owner; it stays on across the user switch) | ☐ |
| P3 | No camera; the buds and the phone within reach | ☐ |
| P4 | Android's **screen recorder with sound** on for each part | ☐ |
| P5 | The status bar across a minute change at the start and the end of each recording | ☐ |
| P6 | **The system log of part 1 right after BL-9's chain has stopped** (the GrapheneOS log viewer, as `CAP-075`'s L) — the Bluetooth process's `already at opened state` / `cleanup_rfc_slot` and Play's `RfcommEventStreamMedium … Retry times remaining N` lines of the 20 cycles; **watch "Retry times remaining"**: `CAP-075` saw 5 → 4 once; what Play does at 0 is ❓ (`TODO.md` §4). A second system log at the end of the run. `adb bugreportz` sends signal 3 to every Java process — not an app fault (`CAP-071-FINDINGS.md` §0) | ☐ |
| P7 | **Do Not Disturb on** | ☐ |
| P8 | Buds charged, **both worn** before BL-4; Android shows them connected before the app is opened | ☐ |
| P9 | **No app use before the film** — not opened between P1's install and BL-1 | ☐ |

**Rhythm:** one action, then 5–10 s; say what you do ("left hold", "right hold", "tap", "switch", "take it back again"). Export the debug log at the end of each
part. Serials: never unredacted in any note (first 4 + last 2 characters).

## Steps

DLCIs are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05). Every MAESTRO request starts `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25` on
**channel 19** or `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25` on **channel 21** (Info → "Control channel"). **Reference frames** — "H2" = `CAP-075-btsnoop_hci2.log`
(the Owner user with Play, 1.2.1 `31ee183`, handle `0x000b`), "H1" = `CAP-075-btsnoop_hci1.log` (user 10 without Play, the same build), re-derived by
`ai-sessions/0089` with `tshark -r <log> -Y "frame.number==N" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type
-e data.data` (exit 0):

| Request / event | Reference |
|---|---|
| The Connect: `SABM` DLCI 0x02, then `ReadSetting 4:16`, 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29, `SubscribeRuntimeInfo`, `GetHardwareInfo` | channel 19: H2 1193 … 1302 — `7e003b0310131dea71de7d5e2551aed0ae2a022010fdae3f247e` (4:16) … `7e003b0310131dea71de7d5e25e3a5ec28ff1ebbb77e` (`GetHardwareInfo`); channel 21: H1 762 … 841 (`7e004b0310151dea71de7d5e2551aed0ae2a02201047eeadcf7e` … `7e004b0310151dea71de7d5e25e3a5ec28f96761b57e`) — `CAP-075-FINDINGS.md` §2: byte-identical to `CAP-073`/`CAP-074` |
| **The cut** (Play's recovery connect refused; the stack closes the app's port) | the phone's `DISC` of the app's DLCI 0x04 without a "released" line — **H2 2369** 06:30:19.281 (the app's `SABM` 2298 06:30:14.565: 4.716 s); export "RFCOMM on-demand channel 0x04 closed (IOException: bt socket closed, read return: -1)" + "Message Stream hold ended: the channel was closed, not by this app"; system log 0.04–0.22 s before: `RFCOMM_CreateConnectionWithSecurity: already at opened state` + `cleanup_rfc_slot … app_uid: 10205` + `… app_uid: <the app's uid>` (`CAP-075-FINDINGS.md` §5) |
| Play's retry after the cut | **H2 2401** 06:30:21.799 (`SABM`, first message `03 08 00 02 01 25` at 2409) — 2.518 s after the cut (range 2.06–2.82 s) |
| **The re-claim** (3.0 s after the close line; attempt 1 refused; attempt 2) | export "Message Stream re-claim 1/**20** after a foreign close, 3000 ms later (ADR-062)" (1.2.1: "1/3") 3.0 s after the "closed" line; "connect failed after N ms (attempt 1/3)" and Play's `DISC` within 3 ms (**H2 2476** 06:30:22.867 — Play's socket killed by the refused connect); the app's **`SABM` H2 2502** 06:30:23.760 (4.479 s after the cut) + `08110000` (2512); held until the next cut **H2 2570** (4.749 s) |
| **The shown hold** (an unprovoked `Notify` on the app's re-claimed hold) | **H2 3006** 06:30:58.178 `0813000401e8e808` (ACTIVE) on re-claim 3/3 (`SABM` 2960) — on screen within a second (film 2 s114); the (i) "Changed by the Buds at …" |
| **A re-claim's changed read** (the app's own `Get` finds another mode) | **H2 2968** `08110000` → **2980** 06:30:54.437 `0813000401e8e880` (TRANSPARENT; the app's last report 2735 `…e8e840` ADAPTIVE) — in 1.2.1 no cause line (film 2 s265–269 for 5361); **1.2.2: the (i) line "Changed since the app last saw it (read at HH:MM:SS): a press-and-hold on a bud or the Buds' own change, made while this app did not have the channel."** |
| A hold that reaches Play (not the app) | an unprovoked `Notify` on Play's claim — H2 2878, 2891, 2907 (Play #9, `SABM` 2805); nothing on screen |
| The hold in the user without Play (part 2) | H1 1192 `SABM` 17:26:06.611 … 1268 (14.40 s, four holds), 1289 … 1389 (33.62 s, nine holds) — every hold on the app's claim, shown within a second; **no `DISC` while the tab is shown**; the phone's `DISC` 1.50–1.51 s after leaving the tab |

Anything else from the app on the wire (a request not in this table or in `CAP-075` §2/§3, a second `GetHardwareInfo` in a session, anything on DLCI 0x08/0x0a,
a `Set` from "Take it back again", a claim while the tab is **not** shown that is not a tap/pull/Connect) is a finding.

### Part 1 — the Owner user (user 0) **with** Google Play services (screen recording with sound; `APP_TESTPLAN.md` Y1–Y10; `PAIR-003`, `BATT-004`, `FW-003`, `ANC-001`…`ANC-004`, `TOUCH-007`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BL-1 | P0–P9 done, user 0, both buds worn | open the app | the session opens by itself (ADR-044); "Probably worn"; Left/Right %, the Case | DLCI 0x02 `SABM`, the thirteen requests in order, `GetHardwareInfo` last, answered; the snapshot claim (attempt 1 refused if Play is on the channel, attempt 2 connects); `03 03` burst; `08 11` → `Notify` | `PAIR-003`, `BATT-004` | a crash; a request missing or out of order; Safe Mode |
| BL-2 | ready | gear → **Info**: 3 s on the build line, the firmware, the serials; **Settings**: the card **"Noise control with Google Play services"** — 3 s on it, open its **(i)**, read the five lines aloud or hold each 3 s, close | "App: 1.2.2, build <B1 hash>", no "-dirty"; `release_5.203` ×3; the card between Dark mode and Case sounds; the intro "The Buds report a press-and-hold on a bud to one app at a time. Choose what fits this phone:"; **"Google Play services: found on this phone"** (ADR-063); **"This phone has Google Play services" selected** (the 1.2.1 choice "Take it back" kept); each option with its second line | nothing | `FW-003` | "-dirty"; another hash; the card under its old name; "This phone has no Google Play services" selected (the stored choice lost); "not found" or no presence line in this user |
| BL-3 | back on Connection | **Disconnect**, wait 5 s, **Connect** | ready | the phone's `DISC` of DLCI 0x02; the thirteen requests again; the snapshot claim | `PAIR-003` | — |
| BL-4 | ready, the **Connection** tab | open the **ANC** tab | the mode | the hold (snapshot claim kept or one new `SABM` + `08 11`); "Message Stream hold started: the noise-control tab is on screen (ADR-061)" | `ANC-001`…`004` | no hold line |
| **BL-5** | the hold open, option **"This phone has Google Play services"**, both buds worn | **press and hold a bud every ≈ 4 s for 3½ minutes** (≈ 50 holds; alternate left/right); say each; **touch nothing on the phone**; count aloud the holds you see change on screen | during the chain: **about half** of the holds change the mode on screen within a second; the ANC card shows **no hint** for ≈ 3 minutes; **after the 20th re-claim's cut** (≈ 3.1 min after the first cut) the hint **"Another app took the noise-control channel at HH:MM:SS — usually Google Play services' Fast Pair; this app took it back 20 times and stopped. A press-and-hold on a bud is not shown until this app has the channel again: tap Take it back again, a mode, Refresh, or pull down."** and the **"Take it back again"** button under it; no further change on screen after that | per cycle: the cut ≈ 4.5–4.9 s after the app's `SABM`; Play's retry `SABM` 2.1–2.8 s after the cut; the export's **"Message Stream re-claim N/20 after a foreign close, 3000 ms later (ADR-062)"** 3.0 s after each "closed" line (N = 1 … 20, **every step 3000 ms**); attempt 1 refused + Play's `DISC` within 3 ms; the app's `SABM` + `08 11`; after the 20th re-claim's cut **"Message Stream re-claim stopped after 20 re-claims — Take it back again, a tap, a pull or re-entering the noise-control tab claims again (ADR-062)"** and no app `SABM` | `TOUCH-007`, `ANC-001`…`004` | a step other than 3000 ms in the export (a 6000/12000 = the old back-off); a 21st re-claim; the stop before 20; the hint missing, without "20 times", or without the button; a `Set` on the wire |
| BL-6 | right after BL-5, **P6 at once** | the GrapheneOS log viewer: save the system log | — | — (the stack lines of the 20 cycles; Play's "Retry times remaining" values) | — | the log not covering the chain |
| BL-7 | the hint with the button | open the ANC **(i)** at a moment the shown mode came from a re-claim's own read (if BL-5 showed one: the mode changed within ≈ 0.5 s after a re-claim, not after a hold you said), read it, close | the (i): the mode with its time, **"Changed since the app last saw it (read at HH:MM:SS): a press-and-hold on a bud or the Buds' own change, made while this app did not have the channel."**, the hint line, the new last sentence "… what this app does then is set on the Settings tab under Noise control with Google Play services." | the re-claim's `08 11` answered by a `Notify` with another mode than the app's last report (as H2 2968 → 2980) | `TOUCH-007` | a changed read without the line; the line after an unprovoked hold (that is "Changed by the Buds") |
| **BL-8** | the hint with the button (the chain stopped) | **tap "Take it back again"**; then hold a bud twice ≈ 4 s apart; wait 15 s | the hint and the button gone at once; the holds inside the app's window shown; ≈ 5 s later the claim is cut and **re-claimed 3 s later** (no hint) | **one** claim: attempt 1 refused (Play on the channel), attempt 2 `SABM` + **`08 11 00 00` only — no `08 12`**, no `03 03`-wait battery claim, no `SubscribeRuntimeInfo` on DLCI 0x02; export **"Message Stream claim by the hint card's action (ADR-062 Update 2)"**; after its cut **"re-claim 1/20 …"** (the counter reset) | `ANC-001`…`004`, `TOUCH-007` | a `Set` (`08 12`) after the tap; a DLCI 0x02 request; no re-claim after the cut (the counter not reset); "re-claim 21/20" |
| BL-9 | the chain of BL-8 running | **tap the Sound tab within 2 s of a cut** (while the 3-s delay runs); wait 10 s; back to **ANC** | Sound; on return a new claim | no `SABM` 3 s after that cut ("Message Stream re-claim cancelled (ADR-062)"); on return a new `SABM` + `08 11` | — | a re-claim while Sound is shown |
| BL-10 | the ANC tab, the chain running | **Home** within 2 s of a cut; wait 10 s; return | the home screen; on return the ANC tab with a new claim | no `SABM` while hidden ("re-claim cancelled"); on return a new claim | — | a `SABM` while the app is hidden |
| BL-11 | the chain running | gear → Settings → **"This phone has no Google Play services"**; back to ANC; hold a bud every ≈ 4 s for 20 s | the row selected; on return: the first hold inside the claim shown, then the hint **without** "took it back …" and with the button; no re-claim | "re-claim cancelled" at the switch (if one was pending); the return's claim; its cut; **"Noise-control channel taken by another client: the hint is shown, no re-claim (ADR-062, hint only)"**; no app `SABM` after the cut | `TOUCH-007` | a re-claim in this option |
| BL-12 | the hint (no Play option) | **tap "Take it back again"**; wait 10 s | the hint gone; ≈ 5 s later the hint back (the claim cut, no re-claim) | one claim with `08 11` only; its cut; the hint-only line again; no re-claim | `ANC-001`…`004` | a `Set`; a re-claim |
| BL-13 | the ANC tab | gear → Settings → **"This phone has Google Play services"** (back to this user's right option); back to ANC | the row selected; a new claim on return | the `DISC` ≈ 1.5 s after the gear (if a claim was open), a new `SABM` + `08 11` on return; after its cut "re-claim 1/20" | — | the choice not kept |
| BL-14 | the ANC tab | **Disconnect**; wait 5 s; **Connect**; stay on the ANC tab | the hint gone at Connect | the thirteen requests; the snapshot claim; the chain from 1/20 | `PAIR-003` | the hint surviving the Connect |
| BL-end1 | — | Debug → **Export**; a second system log; the status bar across a minute change; stop the recording; **switch to user 10** | "Debug log saved (N lines)." | — | — | an empty file |

### Part 2 — the secondary user (user 10) **without** Google Play (screen recording with sound; `APP_TESTPLAN.md` Y11–Y14; the `CAP-073` §4 regression)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BL-15 | user 10, P0 (no Play), both buds worn, the recording on | open the app; **Info** 3 s; **Settings**: the card, 3 s | the session opens by itself; "App: 1.2.2, build <B1 hash>"; the card **"Noise control with Google Play services"**, **"Google Play services: not found on this phone"**, and — the stored 1.2.1 choice "Take it back" — **"This phone has Google Play services" selected** (information only: nothing switches by itself) | the Connect sequence (`CAP-075` H1's bytes); the snapshot claim released ≈ 1.5 s after its answer | `PAIR-003`, `FW-003`, `BATT-004` | Safe Mode; "-dirty"; "found" in this user; the option changed by itself |
| **BL-16** | ready, "This phone has Google Play services" (as stored) | open the **ANC** tab; **press and hold a bud every ≈ 5 s for 30 s**; touch nothing | **every** hold changes the mode on screen; **no hint, no button** | one `SABM` + `08 11`; **no `DISC` for the 30 s**; the unprovoked `Notify` of each hold on the app's claim; no "re-claim" line; 0 Play claims (`frame contains 03:08:00:02:01:25` → 0 in this part) | `TOUCH-007`, `ANC-001`…`004` | a `DISC` while the tab is shown; a hold not shown; a re-claim line; the hint |
| BL-17 | the hold open | gear → Settings → **"This phone has no Google Play services"** (this user's right option); back to **ANC**; hold every ≈ 5 s for 30 s | every hold shown; no hint | the `DISC` ≈ 1.5 s after the gear; one new `SABM` + `08 11` on return; no further `SABM`, no `DISC` for the 30 s | `TOUCH-007` | any extra `SABM`; a `DISC` while the tab is shown |
| BL-18 | the hold open | open the ANC **(i)** after a hold, read it, close | "Changed by the Buds at HH:MM:SS (a press-and-hold on a bud, or the Buds' own change)."; the last sentence with "Noise control with Google Play services"; **no** "Changed since the app last saw it" line (the app had the channel for every hold) | nothing new | `TOUCH-007` | the changed-since line here |
| BL-19 | the hold open | go to the **Connection** tab, wait 5 s (the claim released), **hold a bud once** there, then back to **ANC** | on return the mode read; the (i) shows **"Changed since the app last saw it (read at …)"** (the change was made while the app had no claim) | the return's `08 11` answered with another mode than the last report | `TOUCH-007` | no line after a changed read |
| BL-20 | the hold open | tap the **Controls** tab; wait 5 s | Controls | the phone's `DISC` ≈ 1.5 s after the tab change; nothing else | — | no `DISC`, or one before 1.3 s |
| BL-21 | — | gear → Settings: leave **"This phone has no Google Play services"** (this user's right option) | the row selected | — | — | — |
| BL-22 (optional, `TODO.md` §2) | back on ANC, the hold open | pull **Quick Settings** down; tap the **ANC tile** once; close | the tile steps one mode; the app follows | `08 11` → `Notify` → `Set` → ACK on the held channel, no `SABM`, no `DISC` | `ANC-001`…`004` | a second `SABM` |
| BL-23 (optional) | the hold open | **Home**; wait 5 s; return | the home screen; ANC again | the `DISC` ≈ 1.5 s after Home; a new `SABM` + `08 11` on return | — | no `DISC` after Home |
| BL-24 (optional) | the ANC tab shown, both worn | both buds **into the case**; wait 10 s; **out into the ears** | the session ends with its cause; ready again by itself | the loss; the ADR-044 re-open, its snapshot claim kept as the hold | `PAIR-003` | the snapshot released while the tab is shown |
| BL-end2 | — | Debug → **Export**; the status bar across a minute change; stop the recording; P1's APK pull | "Debug log saved (N lines)." | — | — | — |

## Don'ts

- Do not uninstall the installed 1.2.1 and do not clear the app's data (P1 — the run is an update; the stored choice of each user must survive it).
- Do not open the app before the film (P9).
- In BL-5, BL-11, BL-16 and BL-17 touch nothing on the phone while holding the buds; say each hold.
- Do not tap two things within 5 s of each other, except where a step says "within 2 s of a cut" (BL-9, BL-10).
- Do not write a serial number unredacted anywhere in this folder (first 4 + last 2 characters); no Wi-Fi name, phone number, account name or e-mail in any
  committed text (the system log is committed as an extract, as `CAP-075`).

## After the run

Into this folder: the screen recordings, the system logs of part 1 (as extracts — the full logs and the originals to `~/opencontrol-capture-originals/CAP-076/`),
every debug export, the app logcats if saved, the HCI snoop logs, the P0/P1 outputs with their exit statuses, the pulled APK's `sha256sum`, `dist/1.2.2/`'s
`.sha256` and the script's certificate line; then `sha256sum *` into CAP-076-sha256sum.txt. Commit on the release branch after the build commit (`RELEASING.md`
C3) — never on `main`.

## Analysis checklist

- [ ] Every filter scoped to the Buds' ACL handle (the Connection Complete for the Buds' address with that handle shown); DLCIs by content; a negative with its
      command, its exit status and a positive control (`AGENTS.md` §13 step 8).
- [ ] P0 (part 1: Play listed, the *Nearby devices* permission state and Play's version on film; part 2: no Play, exit 1 with the positive control), P1 (the
      update: `firstInstallTime` unchanged, `lastUpdateTime` later, `versionCode=10202`); the Info frame's hash = B1's; the pulled APK's SHA-256 = B1's.
- [ ] **The stored choice after the update** (BL-2, BL-15): "This phone has Google Play services" selected in both users (1.2.1's `RECLAIM`); ADR-063's line
      "found" in user 0, "not found" in user 10.
- [ ] **The claim inventory of DLCI 0x04/0x05 with both clients** (`CAP-075-FINDINGS.md` §1's method: the owner by the first phone message — `08 11` the app,
      `03 08 00 02 01 25` Play).
- [ ] **BL-5:** per cycle the cut, Play's retry, the export's re-claim line **3 000 ms** after the "closed" line (N/20), attempt 1's refusal, the app's `SABM`,
      the held time and the cycle length; **the coverage of the whole chain** (expected ≈ 50 % for ≈ 3.1 min, against `CAP-075`'s 43 % for 44 s); the stop after
      the 20th and the hint with "20 times"; the holds shown vs lost; Play's timers of this version; the "Retry times remaining" values in the system log (P6).
- [ ] **BL-7 / BL-19:** every `Get` answer that differs from the app's last report of the session → the (i) line "Changed since the app last saw it (read at …)";
      unprovoked `Notify` → "Changed by the Buds"; a session's first read → no line.
- [ ] **BL-8 / BL-12:** "Take it back again" → one claim, `08 11 00 00` only (`data.data[0:2]==08:12` → 0 between the tap and the next user action, with a
      positive control), no DLCI 0x02 request; in the Play option the next cut re-claimed as 1/20; in the no-Play option no re-claim.
- [ ] **BL-9 … BL-11:** no `SABM` after a tab change, Home or the switch to "no Google Play services" (the "re-claim cancelled" lines) — `TODO.md` §2's
      BK-11 … BK-13 items.
- [ ] **Part 2 (BL-16 … BL-20):** 0 Play claims (positive control: part 1's count); one claim per tab entry, **no `DISC` while the tab is shown**, every hold
      shown, no re-claim and no hint in either option; the `DISC` ≈ 1.5 s after leaving the tab.
- [ ] The optional BL-22 … BL-24 if run; the system log: crashes, ANRs, `StrictMode`; the `bugreportz` SIGQUITs discounted.
- [ ] One row per step — done / not done / done differently; `APP_TESTPLAN.md` section Y and the Summary; traceability (`AGENTS.md` §13 step 7): `PAIR-003`
      (BL-1, BL-3, BL-14, BL-15, BL-24), `BATT-004` (BL-1, BL-15), `FW-003` (BL-2, BL-15), `ANC-001`…`ANC-004` (BL-4, BL-5, BL-8, BL-12, BL-16, BL-22),
      `TOUCH-007` (BL-5, BL-7, BL-8, BL-11, BL-16, BL-17, BL-18, BL-19) — each referenced in the timeline or flagged "expected but not observed".
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `id_registry.csv`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the Group column: BL), `APP_TESTPLAN.md`, `TODO.md` §2/§3;
      write the findings file (CAP-076-FINDINGS); an ADR-062 Update with the numbers; the release verdict is the analysing session's, with the maintainer
      (`RELEASING.md` C4).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-076-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BL/CAP-076-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-076-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BL/CAP-076-EVENT-NOTES
