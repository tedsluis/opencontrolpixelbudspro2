# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the secondary user without Google Play and the Owner user with it) — Group BN, round 1: the experiment build 1.3.1-exp.1 (five untested requests) and the 1.3.1 steps `CAP-077` did not run (`CAP-078`)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0096`, 2026-10-16). Round 1 of the plan agreed in chat on 2026-10-16: an
**experiment build** of 1.3.1 — the build type `experiment`, package `io.github.tedsluis.opencontrolpixelbuds.experiment`, versionName `1.3.1-exp.1`,
debug-signed, **never released** — with one card on gear → Debug that sends, on a tap and after a confirmation naming the bytes, one request each of five kinds
no capture has held; every send approved by the maintainer in chat 2026-10-16 (`ai-sessions/0096` Checkpoint 1). The released 1.3.1 stays installed beside it.
A later CAPTURE session analyses the run; only then can the maintainer promote anything and write the ADRs for round 2 (`AGENTS.md` §6). Storage per
`DECISIONS.md` ADR-066: the films and the full logcats stay local (their SHA-256 in CAP-078-sha256sums.txt); the small files are committed.

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-078` |
| Group(s) | BN (new) |
| Date | TBD |
| Phone | Pixel 9a, GrapheneOS (Android 17) — **user 10 without Google Play** (parts 1–2) and **the Owner user 0 with Google Play** (part 3) |
| Apps | OpenControl **1.3.1** (release, `7aa524b`, package `io.github.tedsluis.opencontrolpixelbuds`) and the **experiment build 1.3.1-exp.1** (package `….experiment`; its SHA-256 in `ai-sessions/0096`'s RESULT) — **never both running at once** (one RFCOMM connection per channel across all apps, `ARCHITECTURE.md` §6.0b) |
| Buds | Pixel Buds Pro 2, `release_5.203` expected (any other ⇒ Safe Mode: say so, stop) |
| Video files | TBD — the screen recording **with sound** (say every action aloud: "A, write 2"), **and a camera on the ears and the case** (D and the wear changes need it); both kept local (ADR-066) |
| Log files | TBD — the HCI snoop log(s), the debug exports of both apps (Debug mode on), the app logcats, `adb logcat -b all` (kept local; an extract is committed), the shell log |

## Preparation

| # | Check | Done |
|---|---|---|
| P0 | `adb shell am get-current-user`; `adb shell pm list packages --user 10 \| grep -E "com.google.android.gms$\|com.android.vending$"; echo "exit=$?"` (nothing, exit 1) and the same for `--user 0` (both, exit 0) | ☐ |
| P1 | The experiment APK installed in **both users** (`adb install --user 10 …`, `--user 0 …`), `sha256sum` against the RESULT's value; `dumpsys package io.github.tedsluis.opencontrolpixelbuds.experiment \| grep -E "versionName\|lastUpdateTime"`. The release app stays at 1.3.1 | ☐ |
| P2 | HCI snoop log on (a fresh log per user part) | ☐ |
| P3 | Screen recording **with sound** + the camera; the status bar across a minute change at the start and the end of each part | ☐ |
| P4 | Do Not Disturb on; TalkBack off at the start (part 1 turns it on and off) | ☐ |
| P5 | Buds charged; in-ear detection **on** at the start (Controls); all four press-and-hold modes ticked | ☐ |
| P6 | **One app at a time:** before opening an app, force-stop the other (`adb shell am force-stop --user N <package>` or App info → Force stop) | ☐ |
| P7 | The experiment app's first start: allow Bluetooth and notifications; *Pair a device* → choose the Buds in Android's dialog (its own association); Debug mode **on** (gear → Debug) | ☐ |

**Rhythm:** one action, then 5–10 s; say what you do and what you hear. Never ring with a bud in an ear (C, B).

## Reference frames (built by the app's codecs; `ExperimentFramesTest` pins them — structural until this run puts them on the wire)

| Experiment | Channel 19 (request address `00 3b`) | Channel 21 (`00 4b`) | Real-byte control |
|---|---|---|---|
| A `WriteSetting 4:{13:1}` Off | `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 04 22 02 68 01 f8 89 dc 6c 7e` | `7e 00 4b 03 10 15 … 2a 04 22 02 68 01 c8 65 2a 04 7e` | the same envelope gives `CAP-077` H 2478 `WriteSetting 4:{2:1}` byte for byte |
| A `4:{13:2}` Noise cancellation | `… 68 02 42 d8 d5 f5 7e` | `… 68 02 72 34 23 9d 7e` | |
| A `4:{13:3}` Transparency | `… 68 03 d4 e8 d2 82 7e` | `… 68 03 e4 04 24 ea 7e` | |
| A `4:{13:4}` Adaptive | `… 68 04 77 7d 5d b6 1c 7e` | `… 68 04 47 91 40 74 7e` | |
| E `ReadSetting 4:8` / `4:9` / `4:10` | `… 51 ae d0 ae 2a 02 20 08 ab 36 53 37 7e` / `… 20 09 3d 06 54 40 7e` / `… 20 0a 87 57 5d d9 7e` | `… 20 08 11 76 c1 dc 7e` / `… 20 09 87 46 c6 ab 7e` / `… 20 0a 3d 17 cf 32 7e` | `CAP-077` `ReadSetting 4:2` ch 19/21 |
| D `SubscribeToOobeActions` (`0x57f3becb`), empty | `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 cb be f3 57 8c b7 ae cf 7e` | `… 25 cb be f3 57 8a ce 74 cd 7e` | `CAP-077` `SubscribeRuntimeInfo` |
| B Ring Left / Right (DLCI 0x04) | `04 01 00 01 02` / `04 01 00 01 01` | — | `CAP-025` 2131 / 2040 |
| B2 the phone's ACK of a Buds status | `ff 01 00 02 04 01` | — | `CAP-025` 2048 (the official app) |
| C Ring both | `04 01 00 01 03` | — | none (spec value) |

Every experiment line in the export starts "Experiment A/B1/B2/C/D/E: …" and names the bytes it sent; the Buds' answers are in the Debug-mode hex lines and the HCI log.

## Steps

| Step | Action (say it aloud) | Expected on screen | Expected on the wire | Test-ID | Result / "Refuted if" |
|---|---|---|---|---|---|
| **Part 1 — user 10, the release app 1.3.1** (the steps `CAP-077` did not run) | | | | | |
| BN-1 | TalkBack on: Settings → Case sounds (touch each switch row), Controls → a switch row, Sound → the Bass slider, the bottom tabs; TalkBack off | each row read with its name and state; "Bass, … dB"; each tab once | — | — (A93-APP-07) | refuted if a switch is read without its label |
| BN-2 | Disconnect; **two press-and-holds while disconnected** (the mode changes, heard); Connect; ANC (i) | **"Changed since the app last saw it (read at …)"** stays after the Connect | the snapshot `Get`'s `Notify` (new mode) and the equal `4:13` answer | `TOUCH-007`, `PAIR-003` (A93-APP-02) | refuted if the (i) shows no "changed since" line |
| BN-3 | Tap Connect and **hold a bud within ≈ 1 s** | the held mode shown | a `4:{13:n}` stream packet between a `ReadSetting` request and its answer (if timed right); the read ends with its RESPONSE | `TOUCH-007` (A93-APP-04) | refuted if a read fails while its RESPONSE came |
| BN-4 | Use different Buds → **Cancel**; rotate the phone with the dialog open; Cancel again | nothing changes; still connected | no `DISC` on DLCI 0x02 | (A93-APP-08) | refuted if Cancel forgets the Buds |
| **Part 2 — user 10, the experiment app** (force-stop the release app first) | | | | | |
| BN-5 | Connect; gear → Debug: the card "Experiments (round 1, not a release)" | the card with A, E, D, B, C | the 1.3.1 Connect sequence (17 requests) | `PAIR-003` | — |
| BN-6 | **E**: Read 8, Read 9, Read 10 (both buds worn) | "Experiment E: sent …" | three `ReadSetting` + their answers (a value or an error status) | `ANC-008` | — (the answer is the result) |
| BN-7 | **A**, both buds worn: Off, Noise canc., Transparency, Adaptive, 8 s apart; listen | the mode follows (through the stream) and "Experiment A: RESPONSE OK" | per tap: the `WriteSetting`, its `RESPONSE`, `4:{13:n}` stream packet(s); a Message Stream `Notify`? | `ANC-007` | refuted (as a way to switch) if the status is not OK or the mode does not change |
| BN-8 | E again (Read 8/9/10) after BN-7 | — | the answers | `ANC-008` | — |
| BN-9 | **A with one bud worn** (take the Left out, keep it out of the case): two modes; then E | — | as BN-7 | `ANC-007`, `ANC-008`, `INEAR-004` | — |
| BN-10 | **A with no bud worn** (both on the table — Settable `00`): Noise canc. | — | the `RESPONSE` (OK? an error?) and whether anything changes | `ANC-007` | — |
| BN-11 | **A with in-ear detection off** (Controls), both worn: two modes; in-ear detection on again | — | as BN-7 | `ANC-007`, `INEAR-001` | — |
| BN-12 | **D**: Subscribe; then, on camera, Left out of the ear, in again; Right out, in again (5 s apart) | "Experiment D: sent" | `SubscribeToOobeActions` and any `SERVER_STREAM` for it (`qhp` 10–13?) | `INEAR-007`, `INEAR-002`/`003` | not refuted by silence alone: say whether any packet came |
| BN-13 | **B1** (buds out of the ears, near the camera/microphone): "B1 Left"; when it rings, **touch the Left bud to stop it** | "Experiment B1: the Buds reported 00 (stopped)" or "no stop report in 30 s" | Ring `…02`, the Buds' ACK and status `…02`, then a status `…00` when touched? | `FIND-005`, `FIND-001` | refuted if no `04 01 00 01 00` comes while the claim is held |
| BN-14 | **B2** the same with "B2 Right + ACK"; touch the Right bud | as BN-13 | each Buds status answered by `ff 01 00 02 04 01` | `FIND-005`, `FIND-002` | — |
| BN-15 | **C** (both buds out of the ears): "Ring both" — listen to both; then Stop | "Experiment C: ACK" or "NAK reason …" | `04 01 00 01 03`, the ACK/NAK; Stop `…00` | `FIND-004` | refuted if NAK or only one bud rings |
| BN-16 | Export the debug log; force-stop the experiment app | "Debug log saved (N lines)." | — | — | — |
| **Part 3 — the Owner user 0 with Google Play** (the experiment app; force-stop the release app there too) | | | | | |
| BN-17 | Connect; **A** once per mode (both worn) | as BN-7 | who gets the Message Stream `Notify` — Play (`03 08 00 02 01 25` claims) or none | `ANC-007` | — |
| BN-18 | **B1** Left; touch to stop | — | how long the held claim lives before Play's recovery cuts it (`CAP-074`: ≈ 5.3 s) | `FIND-005` | — |
| BN-19 | **C** once | — | as BN-15 | `FIND-004` | — |
| BN-end | Export both apps' debug logs; the app logcats; `adb logcat -b all`; the HCI logs; stop the films | — | — | — | — |

## Run card (one page for the maintainer)

1. P0–P7 (two users, two apps, Debug mode, HCI on, film + camera with sound).
2. Part 1 with the **release** app: TalkBack (BN-1), two holds while disconnected → Connect → (i) (BN-2), hold right after Connect (BN-3), Use different Buds → Cancel, rotate, Cancel (BN-4).
3. Force-stop the release app; open the **experiment** app; gear → Debug.
4. E (8, 9, 10) → A (four modes) → E → A one bud worn → A no bud worn → A in-ear detection off → D (each bud out/in on camera) → B1 Left (touch to stop) → B2 Right (touch to stop) → C (buds out of ears) → export.
5. Owner user: experiment app: A per mode, B1, C; export.
6. Pull everything (HCI logs, exports, logcats, `adb logcat -b all`); keep the films and the full logcats local (ADR-066).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-078-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BN/CAP-078-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-078-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BN/CAP-078-EVENT-NOTES
