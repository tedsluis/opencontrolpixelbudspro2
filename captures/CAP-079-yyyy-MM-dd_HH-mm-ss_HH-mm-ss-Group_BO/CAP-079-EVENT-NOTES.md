# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the secondary user without Google Play and the Owner user with it) — Group BO, round 2: release candidate 1.4.0 and the Dosimeter experiment (`CAP-079`)

**Status:** 🔲 **planned** — skeleton written 2026-10-17 by `ai-sessions/0098` (the build: `ai-sessions/0098_FEATURE_RESULT_2026_10_17.md`). Nothing below is a
result; every "expected" is a prediction from `CAP-078` and the 1.4.0 code, to be confirmed or refuted by this run.

**What this run checks.** 1.4.0 switches the noise-control mode with `WriteSetting 4:{13:n}` on the app's own channel (DLCI 0x02, `DECISIONS.md` ADR-067), shows
Left/Right battery from runtime info with **no Message Stream (DLCI 0x04) claim at Connect** (ADR-068), reads and ACKs the Buds' ringing status while a Ring
holds the claim (ADR-069) and adds Ring both (ADR-070). Since 1.4.0 the app claims DLCI 0x04 **only for Find**. The run also covers the two 1.3.1 steps no run
has exercised (TalkBack, A93-APP-07; a press-and-hold within ≈ 1 s of a Connect, A93-APP-04) and, in the experiment build only, the Dosimeter requests approved
in chat 2026-10-17 (F1 … F4, `ai-sessions/0098` Checkpoint 1) with a sound-level meter on film (`WELL-002`, `WELL-003`).

Storage per `DECISIONS.md` ADR-066: films, full logcats and system logs stay local; commit the HCI logs, the app's debug exports, the extracts.

## Preparation

| # | Check | Done |
|---|---|---|
| P0 | `adb shell am get-current-user`; `adb shell pm list packages --user 10 \| grep -E "com.google.android.gms$\|com.android.vending$"; echo "exit=$?"` (nothing, exit 1) and the same for `--user 0` (both, exit 0) | ☐ |
| P1 | **1.4.0** (the release APK of this branch, versionCode 10400) installed over 1.3.1 in **both users**; the **experiment APK 1.4.0-exp.1** (package `….experiment`) in both users; `sha256sum` of both against the RESULT; `dumpsys package <package> \| grep -E "versionName\|lastUpdateTime"` on film or in the shell log | ☐ |
| P2 | HCI snoop log on; a fresh log per part (four parts) | ☐ |
| P3 | Screen recording **with sound** + the camera on the phone and the buds; the status bar across a minute change at the start and the end of each part | ☐ |
| P4 | Do Not Disturb on; TalkBack off at the start (BO-14 turns it on and off) | ☐ |
| P5 | Buds charged; in-ear detection **on** at the start; all four press-and-hold modes ticked (Controls) | ☐ |
| P6 | **One app at a time:** force-stop the other OpenControl package before opening one (`adb shell am force-stop --user N <package>`) | ☐ |
| P7 | Part 3–4 only: a **sound-level meter** (a stand-alone meter preferred; a phone app on a second phone otherwise) in frame next to the buds, its display readable on the camera; a speaker for speech and music at fixed volumes | ☐ |

**Rhythm:** one action, then 5–10 s; say what you do and what you hear. **Never ring with a bud in an ear.**

## Reference frames (built by the app's codecs; every one is pinned by a test with the capture frame it equals)

| Request | Channel 19 (request address `00 3b`) | Channel 21 (`00 4b`) | Real bytes |
|---|---|---|---|
| `WriteSetting 4:{13:1}` Off | `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 04 22 02 68 01 f8 89 dc 6c 7e` | `7e 00 4b 03 10 15 … 22 02 68 01 c8 65 2a 04 7e` | `CAP-078` H 5921 / H 7277 |
| `4:{13:2}` Noise cancellation | — | `… 22 02 68 02 72 34 23 9d 7e` | `CAP-078` H 6491 (`SettingsCodecTest`, `Cap078Fixtures.kt`) |
| `4:{13:3}` Transparency | — | `… 22 02 68 03 e4 04 24 ea 7e` | `CAP-078` H 6540 |
| `4:{13:4}` Adaptive | — | `… 22 02 68 04 47 91 40 74 7e` | `CAP-078` H 7298 |
| The Buds' answers | — | `RESPONSE` OK `7e 00 a5 03 08 01 10 15 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 03 6d 4e d8 7e`; `FAILED_PRECONDITION` (status 9) with no bud worn | `CAP-078` H 6495 / H 6777 |
| Refresh: `ReadSetting 4:13` | `7e 00 3b … 2a 02 20 0d 24 c2 39 47 7e` | `7e 00 4b … 2a 02 20 0d 9e 82 ab ac 7e` | `CAP-056` 1450 / `CAP-051` 1178 |
| Refresh battery: `SubscribeRuntimeInfo` | `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 90 82 1e e6 60 2d 65 a9 7e` | (the channel-21 form) | `CAP-062` 2777 |
| Ring Left / Right / both (DLCI 0x04) | `04 01 00 01 02` / `04 01 00 01 01` / `04 01 00 01 03` | — | `CAP-078` H 7772 / `CAP-025` 2040 / `CAP-078` H 8203 |
| The Buds' ring status, the phone's ACK of it | Buds `04 01 00 01 xx` (`00` = stopped); phone `ff 01 00 02 04 01` | — | `CAP-078` H 7775, 7816 / H 7970 (`RingCodecTest`) |
| Experiment F1 `SetWallclock` | RPC `10 13 1d ea 71 de 7e 25 4e ed 3b 67 2a 07 08 <ms varint>` | `10 15 …` | `CAP-041` 783 (`ExperimentFramesTest`) |
| Experiment F2 `SubscribeToLiveDb` | `7e 00 3b 03 10 13 1d 05 d8 d5 73 25 e2 b6 93 4d e4 61 0c 6a 7e` | `7e 00 4b 03 10 15 1d 05 d8 d5 73 25 e2 b6 93 4d e2 18 d6 68 7e` | `CAP-027` 1212 / 1214 |
| Experiment F3 `FetchDailySummaries` | `7e 00 3b 03 10 13 1d 05 d8 d5 73 25 ce 72 b7 73 f3 b3 c7 26 7e` | `7e 00 4b 03 10 15 1d 05 d8 d5 73 25 ce 72 b7 73 f5 ca 1d 24 7e` | `CAP-058` 5736 |
| Experiment F4 `WriteSetting 4:{21:b}` | `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 05 22 03 a8 01 0b …` — **never on a wire** | `… 2a 05 22 03 a8 01 00 ea ad f6 a5 7e` (off) / `… a8 01 01 7c 9d f1 d2 7e` (on) | `CAP-058` 5747 / 5753 (channel 21 only) |

**Log lines to look for** (the app's debug export): "Noise-control mode written (channel N): <mode>", "Noise-control mode not written: the Buds allow no
change (FAILED_PRECONDITION — no bud worn?)", "Noise-control request failed: …", "Ring status from the Buds: ringing left/right/both | stopped", "Ring status
acknowledged (ff 01 00 02 04 01)", "Ring: claim held until the Buds report the stop (at most 20 s)", "Ring: hold ended — the Buds reported the stop | the claim
was closed by another client | no stop report within 20 s", "Connect finished after a Disconnect tap: its channels are closed again", "EQ written (channel N)";
experiment build: "Experiment A/F1/F2/F3/F4: sending …", "Experiment F: <type> ch=N <method> <status> <fields>", "Experiment F: N live-stream packets in the
last S s".

## Steps

| Step | Action (say it aloud) | Expected on screen | Expected on the wire | Test-ID | "Refuted if" |
|---|---|---|---|---|---|
| **Part 1 — user 10 (no Google Play), OpenControl 1.4.0** | | | | | |
| BO-1 | Connect (both buds worn) | Connected; Left, Right and Case % on the battery card; "Noise control: <mode>" on the Connection tab | the MAESTRO session (DLCI 0x02) with `SubscribeRuntimeInfo` (6.2/6.3/6.1); **no SABM on DLCI 0x04** | `PAIR-003`, `BATT-004` | refuted if DLCI 0x04 is opened at Connect, or L/R stay empty with runtime info carrying 6.2/6.3 |
| BO-2 | Battery card → Refresh battery | the values (and their time) renewed | one `SubscribeRuntimeInfo` and its stream packet; nothing on DLCI 0x04 | `BATT-004` | refuted if DLCI 0x04 is opened |
| BO-3 | ANC tab, both worn: Off, Noise cancellation, Transparency, Adaptive, 8 s apart; listen | the mode follows; the Connection tab's line follows | per tap `WriteSetting 4:{13:n}` (the table above), the push `4:{13:n}` and `RESPONSE` OK; no DLCI 0x04 | `ANC-001` … `ANC-004`, `ANC-007` | refuted if a tap opens DLCI 0x04 or the mode does not change with status OK |
| BO-4 | One bud worn (Left out, kept out of the case): two modes | as BO-3 | as BO-3 | `ANC-007`, `INEAR-004` | — |
| BO-5 | No bud worn (both on the table): tap Noise cancellation | "The Buds don't allow changing noise control right now …"; the mode stays | the write, then `RESPONSE` status 9 (`FAILED_PRECONDITION`) | `ANC-007` | refuted if the app shows the new mode |
| BO-6 | In-ear detection off (Controls), both worn: two modes; in-ear detection on again | as BO-3 | as BO-3 | `ANC-007`, `INEAR-001` | — |
| BO-7 | Quick Settings tile: three taps, 5 s apart | the tile steps through the ticked modes; no "unavailable" flash after a change (P2) | one `WriteSetting 4:{13:n}` per tap | `ANC-007` | refuted if the tile flashes "unavailable" between two states |
| BO-8 | Controls: untick Off for press and hold; ANC tab; the tile twice; tick Off again | Off disabled with "Only the modes ticked under Controls → …"; the tile skips Off | the field-12 write; no write of `13:1` | `ANC-007` (ADR-067 Note) | refuted if `4:{13:1}` is sent while Off is unticked |
| BO-9 | Hold a bud (press and hold) twice | the mode follows ("Changed by the Buds") | the push `4:{13:n}` on both channels | `TOUCH-007` | — |
| BO-10 | **Buds out of the ears**, near the camera: Find → Ring Left; when it rings, **touch the Left bud** | "Ringing: Left earbud …", then "Stopped on the bud at HH:MM:SS" | Ring `04 01 00 01 02`, the Buds' ACK, status `…02` → ACK `ff 01 00 02 04 01`; status `…00` → ACK; the claim released after "hold ended" + the linger | `FIND-001`, `FIND-005` | refuted if a status is not ACKed while DLCI 0x04 is open, or "Stopped on the bud" is missing after the status `00` |
| BO-11 | Ring Right; tap **Stop** in the app after ≈ 5 s | the notice ends; **no** "Stopped on the bud" line | Stop `04 01 00 01 00`, the Buds' status `00` (ACKed) | `FIND-002` | refuted if "Stopped on the bud" appears after the app's own Stop |
| BO-12 | Ring both; listen to both; Stop | "Ringing: Both earbuds …" | `04 01 00 01 03`, ACK, status `03` | `FIND-004` | refuted if only one bud rings or a NAK comes |
| BO-13 | Ring Left and **let it run** (touch nothing) | after the Buds' own stop (14.8–17.1 s in `CAP-078`) "Stopped on the bud at …" | the status `00` without a Stop from the phone; the hold ends at the latest at 20 s | `FIND-005` | — |
| BO-14 | TalkBack on: Settings → Case sounds (each switch row), Controls → a switch row, Sound → the Bass slider, the bottom tabs, Find's buttons; TalkBack off | each row read with its name and state; "Bass, … dB"; each tab once | — | — (A93-APP-07) | refuted if a switch is read without its label |
| BO-15 | Disconnect; tap Connect and **press and hold a bud at once** (within ≈ 1 s); repeat until the mode changes during the Connect | the held mode shown after the Connect | a `4:{13:n}` push between `ReadSetting 4:13` and its answer (if timed right); the read ends with its `RESPONSE` | `TOUCH-007` (A93-APP-04) | refuted if the read fails while its `RESPONSE` came |
| BO-16 | Disconnect; tap Connect and **Disconnect at once** (A68-APP-13) | "Disconnected"; not connected a moment later | the channels the Connect opened are closed again (`DISC`) | `PAIR-003` | refuted if the app shows Connected after the Disconnect tap |
| BO-17 | Export the debug log; force-stop the app | "Debug log saved (N lines)." | — | — | — |
| **Part 2 — the Owner user 0 (with Google Play), OpenControl 1.4.0** | | | | | |
| BO-18 | Connect; the battery card; ANC tab: two modes; the tile once; two press-and-holds | as BO-1/BO-3/BO-9; holds shown with Play holding DLCI 0x04 | no app claim on DLCI 0x04; Play's own claim untouched | `ANC-007`, `TOUCH-007`, `BATT-004` | refuted if the app opens DLCI 0x04 for a mode change |
| BO-19 | Buds out of the ears: Ring Left; touch nothing for 10 s; Stop | "Ringing: Left earbud …"; no "Stopped on the bud" the app did not hear | Ring, the hold, Play's cut (≈ 5.3 s after its loss, `CAP-075`), "hold ended — the claim was closed by another client"; Stop on a new claim | `FIND-001`, `FIND-005` | refuted if the app re-claims by itself after the cut |
| BO-20 | Export; force-stop | — | — | — | — |
| **Part 3 — user 10, the experiment app 1.4.0-exp.1** (force-stop the release app first; gear → Debug) | | | | | |
| BO-21 | Connect; the card "Experiments (round 2, not a release)"; Controls: untick Adaptive; card **A → Adaptive** (both worn); tick Adaptive again | "Experiment A: RESPONSE …" | `WriteSetting 4:{13:4}` while field 12 has Adaptive unticked; the Buds' answer | `ANC-007` (ADR-067 Note) | — (the answer is the result) |
| BO-22 | **F1 Clock** once; the phone's clock and the meter in frame | "Experiment F1: RESPONSE OK" (or an error); the log line "the phone's clock N ms" | `SetWallclock` with N, its `RESPONSE` | `WELL-003` | — |
| BO-23 | **F3 Daily 19**, **F3 Daily 21** (before) | "Experiment F3: sent …" | the `FetchDailySummaries` answers; the log's "Experiment F: RESPONSE … FetchDailySummaries …" | `WELL-001`, `WELL-003` | — |
| BO-24 | **F2 Live 19**, **F2 Live 21**; buds worn; one minute each, read the meter aloud every ≈ 10 s: **silence**, **speech** (normal voice at ≈ 1 m), **music at a low volume**, **music at a high volume** | "Experiment F2: sent …" | `SERVER_STREAM` packets of `SubscribeToLiveDb` (field 2); "N live-stream packets in the last S s" once a minute | `WELL-002` | refuted (for the live value) if the packets do not change with the level |
| BO-25 | **F3 Daily 19**, **F3 Daily 21** (after) | as BO-23 | the answers — a change in the current day entry? | `WELL-001`, `WELL-003` | — |
| BO-26 | **F4 21 on**, wait 10 s, **21 off** (channel 21); then **19 on**, **19 off** (channel 19, never captured) | "Experiment F4: RESPONSE …" | the writes and their answers; any setting push of field 21 | `WELL-002` | — |
| BO-27 | Export; force-stop | — | — | — | — |
| **Part 4 — the Owner user 0, the experiment app** | | | | | |
| BO-28 | Connect; F2 Live 21 for one minute (music at one level, the meter in frame); F3 Daily 21 once | as BO-24/BO-23 — with Google Play present (🟡 iv of draft ADR-060) | as BO-24 | `WELL-002` | — |
| BO-end | Export; the app logcats; `adb logcat -b all`; the four HCI logs; stop the films | — | — | — | — |

## Run card (one page for the maintainer)

1. P0–P7 (two users, two apps, HCI on, film + camera with sound; the meter for parts 3–4).
2. **User 10, 1.4.0:** Connect → Refresh battery → ANC four modes (both worn) → one bud → no bud → in-ear detection off → tile ×3 → untick Off, tab + tile, tick
   again → two holds → (buds out of ears) Ring Left + touch → Ring Right + Stop → Ring both + Stop → Ring Left, let it run → TalkBack → Connect + hold at once
   (repeat) → Connect + Disconnect at once → export.
3. **User 0, 1.4.0:** Connect, battery, two modes, tile, two holds → Ring Left, wait 10 s, Stop → export.
4. **User 10, experiment app:** untick Adaptive, A Adaptive, tick again → F1 → F3 19/21 → F2 19 and 21 with four levels of one minute (read the meter aloud) → F3
   19/21 → F4 21 on/off, 19 on/off → export.
5. **User 0, experiment app:** F2 21 one minute + F3 21 → export.
6. Pull everything; keep the films and the full logcats local (ADR-066).

---

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-079-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BO/CAP-079-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-079-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BO/CAP-079-EVENT-NOTES
