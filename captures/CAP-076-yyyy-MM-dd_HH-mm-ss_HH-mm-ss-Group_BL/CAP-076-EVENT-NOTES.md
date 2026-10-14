# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the Owner user **with** Google Play services and the secondary user without) — Group BL, 1.3.0: the settings stream on the app's own channel (`CAP-076`)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0089` for 1.2.2 on 2026-10-13; **rewritten by `ai-sessions/0091` for 1.3.0 on
2026-10-14** — 1.2.2 was released without this run, `TODO.md` §1). The hardware run of **1.3.0** (versionCode 10300; `DECISIONS.md` ADR-064 and ADR-065): at every
MAESTRO session (Connect and every ADR-044 re-open) the app sends one `SubscribeToSettingsChanges` and one `ReadSetting 4:13` after `SubscribeRuntimeInfo`, so
the Buds push every change of the noise-control mode — a press-and-hold on a bud, their own switch, the app's own `Set` — as a `4:{13:n}` packet on the app's
own channel (DLCI 0x02), **whatever tab is shown and without a claim of the shared channel DLCI 0x04**; every other setting the app reads follows the same
stream. The hold of 1.2.0 (ADR-061), the re-claim with the setting "Noise control with Google Play services" (ADR-062) and the Play-services line with the
`<queries>` entry (ADR-063) are gone. **Two new requests on the wire, nothing else new**: the official app's bytes (`CAP-051` 1177/1178, `CAP-056` 1451/1450).
**Two parts, both GrapheneOS users, the extended tier** (`RELEASING.md` §11a): **part 1** the Owner user (user 0) with Play services — where 1.2.x showed a hold
about half of the time at best; **part 2** the secondary user (user 10) without Play. The maintainer films; a later CAPTURE session analyses it and gives the
release verdict (`RELEASING.md` C3/C4). After the run: rename this folder to the screen recordings' first/last phone times and the recordings to
CAP-076-recording1.mp4 / CAP-076-recording2.mp4; the camera films to CAP-076-camera1.mp4 / CAP-076-camera2.mp4.

**Expected (from the official app's captures — `CAP-051-FINDINGS.md` §2–§3, `CAP-056-FINDINGS.md` §4, `CAP-038-FINDINGS.md` §3; `ai-sessions/0091` RESULT
§A.1):** the stream packet for a hold follows the Buds' `Notify` by 1–144 ms (`CAP-051` 1707 → 1717, 1754 → 1755, 1801 → 1802), also while no client holds the
Message Stream (`CAP-038` 2170, 2205); so **every hold shows on screen within a second, on any tab, in both users**, with "Changed by the Buds at HH:MM:SS" in
the noise-control (i). The app's own tap: `Set` → ACK → `Notify` → the stream packet 269 ms after the Set (`CAP-051` 1527 → 1534) — shown as the app's change.
A wear change the Buds answer with a `DISC` of DLCI 0x02 (12 of 77 in the captures, `DESKRESEARCH_FINDINGS.md` 2026-10-14): the ADR-044 re-open ≈ 1.5 s later
subscribes again and reads 13 — the mode on screen may be a few seconds old in between.

**Not in this run** (say so if asked): `ReadSetting 4:8/9/10` (the maintainer's choice in chat 2026-10-14, "Not part of this run"); `WriteSetting 4:{13:n}`
(`TODO.md` §4 E-01, needs its own approval); the case-sound experiment.

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-076` |
| Group(s) | BL (new) |
| Date | TBD |
| Phone | Pixel 9a, GrapheneOS (Android 17) — **part 1: the Owner user (user 0) with Google Play services** (`CAP-074`/`CAP-075` set 2's user); **part 2: the secondary user (user 10) without Google Play** (`CAP-073`/`CAP-075` set 1's user) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.3.0** (versionCode 10300), the release APK of `scripts/release.sh 1.3.0` (B1), kept as `~/opencontrol-1.3.0-tested` (B3) — **Info tab on film in each user**: "App: 1.3.0, build <B1 hash> (<date>)", no "-dirty"; APK SHA-256 and certificate SHA-256 from the script's output; **the installed APK pulled** (P1) |
| Previous version in each user | 1.2.2 build `bfd7891` (installed 2026-10-14 with Obtainium in the Owner user; the secondary user's version on film) — **the stored "Noise control with Google Play services" choice of 1.2.1/1.2.2 is ignored by 1.3.0** (ADR-065 item 5: never read, never migrated); the card is gone |
| Google Play services (part 1) | present and active (`com.google.android.gms`, uid 10205 in `CAP-074`/`CAP-075`); **its *Nearby devices* permission state and its version on film** (P0 — `TODO.md` §3's standing item, not done in `CAP-075`) |
| Official Pixel Buds app | not used on this phone; **optionally on a second phone** for BL-13 / BL-25 (the EQ or a switch changed there — the stream packet reaches OpenControl) — say if no second phone was available |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` expected (any other ⇒ Safe Mode: say so, stop) — both buds **worn** for every hold step (Settable `e8`; `00` means no bud worn, ADR-049) |
| Video files | TBD — the screen recording **with sound** per part (say each action and each hold aloud, as in `CAP-075`) **and a camera on the ears and the phone** for the hold steps (the hold on the bud and the screen in one frame; no address overlay — `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3) |
| Log files | TBD — the HCI snoop logs, the app's debug export per part (Debug mode on), the app logcat, the system log with the GrapheneOS log viewer at the end of each part, the P0/P1 outputs |

## Preparation

| # | Check | Done |
|---|---|---|
| P0 | **In each user**: `adb shell am get-current-user`; `adb shell pm list packages --user <id> \| grep -i -E "gms\|vending"; echo "exit=$?"` (part 1: Play listed, exit 0; part 2: nothing, exit 1); positive control `grep opencontrol` (exit 0). **Part 1 on film:** Settings → Apps → Google Play services → Permissions → *Nearby devices* (allowed / not allowed) and the app's version line. Save the outputs **with the exit statuses** | ☐ |
| P1 | In the installed 1.2.2 (`bfd7891`): Debug mode **on** (on film); gear → Settings: the card "Noise control with Google Play services" and its selected option on film (what 1.3.0 will ignore). `adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds \| grep -E "firstInstallTime\|lastUpdateTime\|versionCode"` → save. `adb install -r ~/opencontrol-1.3.0-tested/opencontrol-pixelbudspro2-1.3.0.apk` (one APK serves both users) — **no uninstall**; the same `dumpsys` → save. After the run: `adb shell pm path --user <id> io.github.tedsluis.opencontrolpixelbuds` → `adb pull` → `sha256sum` against `~/opencontrol-1.3.0-tested` | ☐ |
| P2 | HCI snoop log on (set in the Owner; it stays on across the user switch) — **Bluetooth off and on on film** after enabling it | ☐ |
| P3 | The camera on a stand: the ears and the phone screen in one frame for BL-5 … BL-8 and BL-18 … BL-21; the phone's clock with seconds visible, or the status bar at a minute change at the start and the end | ☐ |
| P4 | Android's **screen recorder with sound** on for each part | ☐ |
| P5 | The status bar across a minute change at the start and the end of each recording | ☐ |
| P6 | **The system log at the end of each part** (the GrapheneOS log viewer, as `CAP-075`'s L) — crashes, ANRs, `StrictMode`; in part 1 Play's `RfcommEventStreamMedium` lines around the taps of BL-10/BL-11. `adb bugreportz` sends signal 3 to every Java process — not an app fault (`CAP-071-FINDINGS.md` §0) | ☐ |
| P7 | **Do Not Disturb on** | ☐ |
| P8 | Buds charged, **both worn** before BL-4; Android shows them connected before the app is opened | ☐ |
| P9 | **No app use before the film** — not opened between P1's install and BL-1 | ☐ |
| P10 | Optional: a second phone with the official Pixel Buds app paired to the same Buds (multipoint) for BL-13 / BL-25 — say on film if there is none | ☐ |

**Rhythm:** one action, then 5–10 s; say what you do ("left hold", "right hold", "tap", "tile", "bud out", "Bluetooth off"). Export the debug log at the end
of each part. Serials: never unredacted in any note (first 4 + last 2 characters).

## Steps

DLCIs are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05). Every MAESTRO request starts `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25` on
**channel 19** or `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25` on **channel 21** (Info → "Control channel"). **Reference frames** — the official app's bytes,
re-derived by `ai-sessions/0091` with `tshark -r <log> -Y 'frame.number==N' -T fields -E separator='|' -e frame.number -e frame.time -e frame.p2p_dir
-e bthci_acl.chandle -e btrfcomm.dlci -e data.data` (exit 0) after scoping to the Buds' ACL handle, decoded with `python3 scripts/pwrpc_decode.py <log>`
(a payload holding several HDLC frames split on `7e`, each CRC-32 verified):

| Request / event | Reference (whole pw_hdlc frame incl. CRC) |
|---|---|
| The Connect: `SABM` DLCI 0x02, then `ReadSetting 4:16`, 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29, `SubscribeRuntimeInfo`, **`SubscribeToSettingsChanges`, `ReadSetting 4:13`**, `GetHardwareInfo` — **fifteen MAESTRO requests** | `CAP-075` H2 1193 … 1302 (channel 19) / H1 762 … 841 (channel 21) for the thirteen of 1.2.x (`CAP-075-FINDINGS.md` §2), then the two new ones below, then `GetHardwareInfo` (`7e003b0310131dea71de7d5e25e3a5ec28ff1ebbb77e` on 19, `7e004b0310151dea71de7d5e25e3a5ec28f96761b57e` on 21) |
| **`SubscribeToSettingsChanges` REQUEST** (method `0x2821adf5`, empty payload) | channel 21: `CAP-051` 1177 — `7e004b0310151dea71de7d5e25f5ad2128019754b07e`; channel 19: `CAP-056` 1451 / `CAP-051` 2229 — `7e003b0310131dea71de7d5e25f5ad212807ee8eb27e`. Find them: `tshark -r CAP-076-btsnoop_hci1.log -Y 'btrfcomm.dlci==2 \|\| btrfcomm.dlci==3' -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data \| grep -i "25f5ad2128019754b0\\\|25f5ad212807ee8eb2"` — **one per MAESTRO session, direction 0 (sent)** |
| **`ReadSetting 4:13` REQUEST** | channel 21: `CAP-051` 1178 / `CAP-036` 1517 — `7e004b0310151dea71de7d5e2551aed0ae2a02200d9e82abac7e`; channel 19: `CAP-056` 1450 / `CAP-021` 1069 — `7e003b0310131dea71de7d5e2551aed0ae2a02200d24c239477e`. Find: `… \| grep -i "2a02200d"` — one per MAESTRO session, right after the subscription |
| **Its RESPONSE `4:{13:n}`** (type 1; the first read's value after every Connect/re-open) | channel 19: `7e80a3032a04220268 0n 080110131dea71de7d5e2551aed0ae` + CRC — n = 0 `…c6b9d8857e` (`CAP-051` 2247, after the Buds' own `DISC`), 1 `…2e62233c7e` (`CAP-021` 1089), 2 `…57085e2d7e` (`CAP-056` 1468), 3 `…bfd3a5947e` (`CAP-027` 923); channel 21: n = 0 `7e00a5032a0422026800080110151dea71de7d5e2551aed0ae651b08147e` (`CAP-045` 2033), 4 `…067d5e749e7e` (`CAP-051` 1225). 1 = Off, 2 = Noise cancellation, 3 = Transparency, 4 = Adaptive, **0 = no mode settable** (no bud worn) |
| **The stream packet `4:{13:n}`** (type 7 `SERVER_STREAM`, direction 1, the Buds → the phone) — **one per hold, per own tap and per wear-driven switch** | channel 19: `7e80a3032a04220268 0n 080710131dea71de7d5e25f5ad2128` + CRC — n = 1 `…117bf5557e` (`CAP-021` 1842), 2 `…681188447e` (`CAP-038` 2205, `CAP-056` 2324/4028), 3 `…80ca73fd7e` (`CAP-038` 2170), 4 `…9ac572667e` (`CAP-056` 2259/4025); channel 21: 0 `7e00a5032a0422026800080710151dea71de7d5e25f5ad21285a02de7d5d7e` (`CAP-045` 2277), 2 `…cbb358d57e` (`CAP-051` 1717), 3 `…2368a36c7e` (1802/1534), 4 `…3967a2f77e` (1755). Find: `… \| grep -i "0807101[35]1dea71de7d5e25f5ad2128"`; count them against the holds made |
| A stream packet for another setting (BL-13 / BL-25, the official app on a second phone) | field 16 (the EQ): `CAP-015` 2116 `7e80a3032a1e221c8201190d…080710131dea71de7d5e25f5ad2128…`; field 2: `CAP-056` 2175 `7e80a3032a0422021001080710131dea71de7d5e25f5ad2128475780f97e`; field 12: `CAP-056` 1696; field 29: `CAP-069` 2502 — the value on screen with the packet's time ("read HH:MM:SS" in the card's (i)) |
| The app's own tap (unchanged, ADR-032) | `SABM` DLCI 0x04 (or 0x05), `08 11 00 00`, `Notify`, `08 12 00 14 01 e8 e8 <mode> …`, ACK `ff 01 00 06 08 12 01 e8 e8 <mode>`, then the Buds' `Notify` and **the stream packet of that mode on DLCI 0x02** (`CAP-051` 1527 → 1529 → 1530/1533 → 1534: 269 ms); the phone's `DISC` of DLCI 0x04 ≈ 1.5 s after the answer (`MESSAGE_STREAM_LINGER_MS`) |
| Play's Fast Pair claim (part 1) | `SABM` DLCI 0x04, first message `03 08 00 02 01 25` (`CAP-075-FINDINGS.md` §1) — its presence is expected and changes nothing for the stream |
| The Buds' own `DISC` on a wear change (BL-9 / BL-22) | `DISC` DLCI 0x02 from the Buds (`CAP-051` 1996, 5 ms after the Notify 1993) → the app's loss line → ADR-044's re-open ≈ 1.5 s later: `SABM` DLCI 0x02, the fifteen requests, the subscription and the read again (`CAP-051` 2229/2231 → 2247) |

Anything else from the app on the wire (a request not in this table or in `CAP-075` §2/§3, a second `SubscribeToSettingsChanges` or `ReadSetting 4:13` within one
session, a `WriteSetting 4:{13:n}`, anything on DLCI 0x08/0x0a, a `SABM` on DLCI 0x04 that is not a tap, the tile, Refresh, a pull or the Connect's snapshot) is
a finding.

### Part 1 — the Owner user (user 0) **with** Google Play services (screen recording with sound + the camera; `APP_TESTPLAN.md` Y1–Y13; `PAIR-003`, `BATT-004`, `FW-003`, `ANC-001`…`ANC-004`, `TOUCH-007`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BL-1 | P0–P10 done, user 0, both buds worn | open the app | the session opens by itself (ADR-044); "Probably worn"; Left/Right %, the Case; the mode | DLCI 0x02 `SABM`; the **fifteen** requests in order — the subscription and `ReadSetting 4:13` after `SubscribeRuntimeInfo`, `GetHardwareInfo` last — answered; the snapshot claim (one `SABM` DLCI 0x04, `08 11` → `Notify`, `03 03` burst, released ≈ 1.5 s later) | `PAIR-003`, `BATT-004` | a crash; a request missing, doubled or out of order; Safe Mode |
| BL-2 | ready | gear → **Info**: 3 s on the build line, the firmware, the serials; **Settings**: 3 s on the whole tab | "App: 1.3.0, build <B1 hash>", no "-dirty"; `release_5.203` ×3; Settings shows **Dark mode, Case sounds, Use different Buds — no "Noise control with Google Play services" card, no "Google Play services: …" line** | nothing | `FW-003` | "-dirty"; another hash; the card or the line still there |
| BL-3 | back on Connection | **Disconnect**, wait 5 s, **Connect** | ready | the phone's `DISC` of DLCI 0x02; the fifteen requests again (the subscription and the read once more — one per session); the snapshot claim | `PAIR-003` | the subscription missing after the Connect |
| BL-4 | ready, stay on the **Connection** tab, both buds worn, the camera on the ears | **press and hold a bud 10 times, ≈ 6 s apart** (alternate left/right); say each; **touch nothing on the phone**; count aloud the mode changes you see on the Connection tab's noise-control line (if any) — then open the **ANC** tab once and read the mode | the mode in the app follows **every** hold within a second (the tab need not be open: the notification's mode line and, on opening ANC, the current mode) | per hold: **one stream packet `4:{13:n}` on DLCI 0x02** (direction 1), no `Notify` (no claim), **no `SABM` on DLCI 0x04 from the app**; Play's own claims may come and go | `TOUCH-007`, `ANC-001`…`004` | a hold without a stream packet; an app `SABM` on DLCI 0x04 during the holds; a mode shown that differs from the last packet's |
| **BL-5** | the **ANC** tab open, both buds worn, the camera on the ears | **press and hold a bud 10 times, ≈ 6 s apart**; say each; count aloud the holds you see change on screen | **10 of 10** holds change the mode on screen within a second; the (i) after one of them: **"Changed by the Buds at HH:MM:SS: the Buds reported a new mode — a press-and-hold on a bud, or a change they made themselves."**; the (i)'s last sentence "The Buds report every change of the mode on the app's own channel. …" | one stream packet per hold; **no app `SABM` on DLCI 0x04** (the ANC tab claims nothing by itself since 1.3.0) | `TOUCH-007`, `ANC-001`…`004` | a hold not shown; an app `SABM`; a hint or "Take it back again" anywhere |
| BL-6 | the ANC tab | open the ANC **(i)** right after a hold, read it, close | the mode with its time; the "Changed by the Buds at …" line; "Connection: Ready"; the last sentence with "that part is a hypothesis" | nothing | `TOUCH-007` | the old hold sentence; the channel-taken line |
| BL-7 | the ANC tab | **tap a mode** (one tap); wait 5 s; open the (i) | the tapped mode at once; the (i) without a "Changed by the Buds" line (the app's own change) | one `SABM` DLCI 0x04 + `08 11` + `08 12` + ACK; the Buds' `Notify`; **the stream packet of the tapped mode on DLCI 0x02 within ≈ 0.3 s of the `Set`**; the phone's `DISC` ≈ 1.5 s after the ACK; if Play is on the channel at that moment: attempt 1 refused, attempt 2 connects (`CAP-075` §5) — or the tap fails with its message: tap again, say so | `ANC-001`…`004` | the stream packet of the own Set shown as "Changed by the Buds"; a `DISC` later than 2 s after the ACK |
| BL-8 | the ANC tab | pull **Quick Settings** down; tap the **ANC tile** once; close; look at the app | the tile steps one mode; the app follows at once | as BL-7: one claim for the tile's `Set`; the stream packet; the `DISC` ≈ 1.5 s later | `ANC-001`…`004` | a second `SABM`; the app not following |
| **BL-9** | the ANC tab, **both buds worn**, the camera on the ears | **take one bud out** (say which); wait 10 s; **put it back**; wait 10 s; read the mode after each | if the Buds close the session (the loss line "… the Buds closed …", ready again ≈ 1.5 s later by itself): after the re-open the mode read (`ReadSetting 4:13`'s answer) — the (i) says **"Changed since the app last saw it (read at HH:MM:SS): …"** if it differs from the mode before, nothing if it is the same; if the Buds keep the session: the stream packet of their own switch, "Changed by the Buds" (the stored mode per wear state, 🟡 `PROTOCOL.md` §6) | either the Buds' `DISC` of DLCI 0x02 → the re-open's `SABM`, the fifteen requests, **the subscription and the read again** (the first read's value after the re-open), or a stream packet `4:{13:n}` without a `DISC` — say which happened | `PAIR-003`, `TOUCH-007` | a re-open without the subscription; a changed read without the line; the app sending anything on reaction to the packet |
| BL-10 | ready | **Bluetooth off** (Quick Settings); wait 5 s; **Bluetooth on**; wait 15 s | "Bluetooth was switched off" as the loss cause; ready again by itself once Android reconnects the Buds | the loss; the re-open's `SABM` DLCI 0x02, the fifteen requests — **the subscription again** (M1 on hardware) | `PAIR-003` | no subscription after the re-open |
| BL-11 | ready, the ANC tab | **tap a mode** while Play's claim is likely open (any moment — Play holds the channel most of the time in this user); if the tap fails ("… could not claim …"), wait 5 s and tap again | the mode after at most two taps; nothing else; **no hint, no "Take it back again"** | one claim per tap; a refused attempt 1 with Play's `DISC` is the `CAP-075` §5 collision — write down how many taps failed; **the app never re-claims by itself** | `ANC-001`…`004` | an app `SABM` not caused by a tap/tile/Refresh/pull/Connect |
| BL-12 | the ANC tab | hold a bud **5 more times** ≈ 6 s apart | 5 of 5 shown | 5 stream packets; no app `SABM` | `TOUCH-007` | a hold not shown |
| BL-13 (optional, P10) | the Sound tab | on the **second phone**, in the official Pixel Buds app: move one EQ slider, then switch **In-ear detection** off and on (Controls); watch OpenControl | the equalizer follows within a second ("read HH:MM:SS" in its (i), not "changed by this app"); the switch follows | the stream packets for field 16 and field 2 on OpenControl's DLCI 0x02 (the official app writes on its own session) | — | a value not following; a write from OpenControl in reaction |
| BL-end1 | — | Debug → **Export**; the system log; the status bar across a minute change; stop the recordings; **switch to user 10** | "Debug log saved (N lines)." | — | — | an empty file |

### Part 2 — the secondary user (user 10) **without** Google Play (screen recording with sound + the camera; `APP_TESTPLAN.md` Y14–Y22)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BL-14 | user 10, P0 (no Play), both buds worn, the recordings on | open the app; **Info** 3 s; **Settings** 3 s | the session opens by itself; "App: 1.3.0, build <B1 hash>"; no Play card, no Play line | the Connect sequence with the **fifteen** requests (the subscription and the read); the snapshot claim released ≈ 1.5 s after its answer | `PAIR-003`, `FW-003`, `BATT-004` | Safe Mode; "-dirty"; the card |
| BL-15 | ready, the **Connection** tab | **hold a bud 10 times** ≈ 6 s apart; say each; then open ANC | every hold followed (the mode line / the notification; ANC shows the last) | 10 stream packets; **no app `SABM`** on DLCI 0x04; 0 Play claims (positive control: part 1's count) | `TOUCH-007`, `ANC-001`…`004` | a hold without a packet; an app `SABM` |
| **BL-16** | the **ANC** tab, both worn | **hold a bud 10 times** ≈ 6 s apart | 10 of 10 shown within a second; "Changed by the Buds at …" in the (i) | 10 stream packets; no app `SABM`; **no `DISC` of DLCI 0x04 while the tab is shown — because there is no claim to cut** | `TOUCH-007`, `ANC-001`…`004` | a hold not shown; an app `SABM` |
| BL-17 | the ANC tab | **tap a mode**; wait 5 s; the (i) | the mode; no "Changed by the Buds" line | one claim: `08 11`, `08 12`, ACK, the `Notify`, the stream packet ≈ 0.3 s after the Set, the `DISC` ≈ 1.5 s after the ACK | `ANC-001`…`004` | the own Set shown as the Buds' change |
| BL-18 | the ANC tab | the **ANC tile** once; close; the app | the tile steps one mode; the app follows | as BL-17 | `ANC-001`…`004` | — |
| BL-19 | the ANC tab, both worn | **one bud out**; wait 10 s; **back in**; wait 10 s | as BL-9 (the re-open's read with "Changed since the app last saw it" if it differs, or the Buds' own switch as "Changed by the Buds") | as BL-9 | `PAIR-003`, `TOUCH-007` | as BL-9 |
| BL-20 | ready | **Bluetooth off**; 5 s; **on**; wait 15 s | the loss named; ready again by itself | the re-open's fifteen requests — the subscription again | `PAIR-003` | no subscription after the re-open |
| BL-21 | the Controls tab | hold a bud 5 times ≈ 6 s apart; then ANC | the last mode on ANC; (i) "Changed by the Buds" | 5 packets, no app `SABM` | `TOUCH-007` | — |
| BL-22 | ready | **Disconnect**; wait 5 s; hold a bud twice (no session); **Connect**; the ANC tab, the (i) | after the Connect the mode read; the (i) **"Changed since the app last saw it (read at HH:MM:SS): a press-and-hold on a bud or the Buds' own change, made while this app was not connected."** | the phone's `DISC`; nothing while disconnected; the Connect's fifteen requests; the read's answer with the new mode | `PAIR-003`, `TOUCH-007` | the line missing after a differing read; the line after a read of the same mode |
| BL-23 | the ANC tab | both buds **into the case**; wait 15 s; **out into the ears**; wait 15 s | the session ends with its cause (or the Buds report "no mode settable": the mode kept, "The Buds don't allow changing noise control right now …"); ready again by itself; the mode read | the loss (or a stream packet `4:{13:0}` — `CAP-045` 2277 — and no loss); the re-open's requests; the read's answer (0 while in the case, 1–4 once worn) | `PAIR-003` | a value 0 shown as a mode |
| BL-24 (optional) | the ANC tab | **Home**; wait 10 s; hold a bud twice; return | on return the holds' result shown (the stream arrived while hidden — the session stays open in the foreground service) | 2 packets while hidden; no `SABM` | `TOUCH-007` | — |
| BL-25 (optional, P10) | the Sound tab | the second phone's official app: an EQ slider; a switch | OpenControl follows | the stream packets for 16 / the switch's field | — | — |
| BL-end2 | — | Debug → **Export**; the system log; the status bar across a minute change; stop the recordings; P1's APK pull | "Debug log saved (N lines)." | — | — | — |

## Don'ts

- Do not uninstall the installed 1.2.2 and do not clear the app's data (P1 — the run is an update; the ignored 1.2.x choice must still be in the file).
- Do not open the app before the film (P9).
- In BL-4, BL-5, BL-12, BL-15, BL-16 and BL-21 touch nothing on the phone while holding the buds; say each hold; keep the ears on camera.
- Do not tap two things within 5 s of each other.
- Do not write a serial number unredacted anywhere in this folder (first 4 + last 2 characters); no Wi-Fi name, phone number, account name or e-mail in any
  committed text (the system log is committed as an extract, as `CAP-075`); the camera films carry no address overlay.

## After the run

Into this folder: the screen recordings and the camera films, the system logs (as extracts — the full logs and the originals to
`~/opencontrol-capture-originals/CAP-076/`), every debug export, the app logcats if saved, the HCI snoop logs, the P0/P1 outputs with their exit statuses, the
pulled APK's `sha256sum`, `dist/1.3.0/`'s `.sha256` and the script's certificate line; then `sha256sum *` into CAP-076-sha256sum.txt. Commit on the release
branch after the build commit (`RELEASING.md` C3) — never on `main`.

## Analysis checklist

- [ ] Every filter scoped to the Buds' ACL handle (the Connection Complete for the Buds' address with that handle shown); DLCIs by content; a negative with its
      command, its exit status and a positive control (`AGENTS.md` §13 step 8).
- [ ] P0 (part 1: Play listed, the *Nearby devices* permission state and Play's version on film; part 2: no Play, exit 1 with the positive control), P1 (the
      update: `firstInstallTime` unchanged, `lastUpdateTime` later, `versionCode=10300`); the Info frame's hash = B1's; the pulled APK's SHA-256 = B1's.
- [ ] **The two new requests, byte-identical** (the table above), **once per MAESTRO session**: count the `SubscribeToSettingsChanges` REQUESTs and the
      `ReadSetting 4:13` REQUESTs against the `SABM`s of DLCI 0x02 (Connects + re-opens) in each log — equal; their position after `SubscribeRuntimeInfo` and
      before `GetHardwareInfo`.
- [ ] **The counts for the verdict:** (1) OpenControl's `SABM`s on DLCI 0x04/0x05 (owner by the first phone message, `08 11` = the app, `03 08 00 02 01 25` =
      Play — `CAP-075-FINDINGS.md` §1's method) **vs the taps, tile taps, pulls and Connects/re-opens made** — equal (no claim the user did not cause);
      (2) **holds shown vs holds made** per step (BL-4, 5, 12, 15, 16, 21 — expected all); (3) **stream packets `4:{13:n}` vs `Notify`s** — one packet per
      hold, per own tap and per wear-driven switch, with the `Notify`s only inside the app's or Play's claims; (4) **the first read's value after every
      Connect/re-open** (the RESPONSE after each `ReadSetting 4:13`), its (i) line when it differs from the mode shown before.
- [ ] **BL-7 / BL-17:** the own tap's stream packet classed as the app's change (no "Changed by the Buds" line in the film); its delay after the `Set`.
- [ ] **BL-9 / BL-19 / BL-23:** which of the two happened (the Buds' `DISC` → re-open → read, or a stream packet without a `DISC`); the mode before and after;
      the (i) line; a value 0 never shown as a mode (the "not allowed" line instead).
- [ ] **BL-10 / BL-20:** the subscription after the Bluetooth-off re-open (M1 on hardware).
- [ ] **BL-11:** the taps that collided with Play (`CAP-075` §5's refusal), how many, and that nothing re-claimed afterwards — the known issue of 1.3.0.
- [ ] **BL-13 / BL-25 if run:** the official app's writes on its own session, the stream packets on OpenControl's, the values on screen with the packet's time.
- [ ] The system logs: crashes, ANRs, `StrictMode`; the `bugreportz` SIGQUITs discounted.
- [ ] One row per step — done / not done / done differently; `APP_TESTPLAN.md` section Y and the Summary; traceability (`AGENTS.md` §13 step 7): `PAIR-003`
      (BL-1, BL-3, BL-9, BL-10, BL-14, BL-19, BL-20, BL-22, BL-23), `BATT-004` (BL-1, BL-14), `FW-003` (BL-2, BL-14), `ANC-001`…`ANC-004` (BL-4, BL-5, BL-7,
      BL-8, BL-11, BL-15 … BL-18), `TOUCH-007` (BL-4 … BL-6, BL-9, BL-12, BL-15, BL-16, BL-19, BL-21, BL-22, BL-24) — each referenced in the timeline or flagged
      "expected but not observed".
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `id_registry.csv`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the Group column: BL), `APP_TESTPLAN.md`, `TODO.md`
      §1/§2/§3; write the findings file (CAP-076-FINDINGS); an ADR-065 Update with the numbers; `PROTOCOL.md` §6's wear-change item if BL-9/BL-19/BL-23 settle
      it (a promotion needs the maintainer's approval, `AGENTS.md` §6); the release verdict is the analysing session's, with the maintainer (`RELEASING.md` C4).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-076-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BL/CAP-076-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-076-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BL/CAP-076-EVENT-NOTES
