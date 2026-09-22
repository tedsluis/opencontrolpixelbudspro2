# Event Notes: OpenControl for Pixel Buds on Pixel 9a (GrapheneOS) — Group AU, first hardware run of the `ai-sessions/0041` build (`CAP-059`)

**Status:** 🟢 **Video pass + full log analysis complete** (originally `ai-sessions/0042`, migrated into
this file's structure by `ai-sessions/0043`). See `CAP-059-FINDINGS.md` for the full wire-level
analysis, hex+command evidence, and conclusions this timeline references.

**Provenance note:** this capture was originally kept locally as `android/logs/LOGS-001/` (per the
maintainer's own hand-edited events file, `LOG-001-EVENTS.md`) and analysed by `ai-sessions/0042` before
`captures/CAP-NNN-*` classification existed for this project's own app-validation captures.
`ai-sessions/0043` moved it here as `CAP-059`. The actual timestamped timeline below is reconstructed
from `ai-sessions/0042`'s full video/HCI/log correlation (`DESKRESEARCH_FINDINGS.md`'s 2026-09-20
second entry, migrated in full into `CAP-059-FINDINGS.md`), which read the maintainer's hand-edited
file at the time and incorporated its content — not transcribed fresh from that file this session.

**Data-loss note, self-flagged (`ai-sessions/0043`):** during this migration, the source directory
`android/logs/LOGS-001/` was deleted before all of its files had been copied, and the maintainer's own
`LOG-001-EVENTS.md` — gitignored, so with no git history to fall back on — was lost as a result. A file
recovered from local editor history afterwards turned out to be the blank original template, not the
maintainer's filled-in version; the maintainer confirmed (chat, 2026-09-22) they have no other copy and
that proceeding without it is fine, since its substantive content was already captured by
`ai-sessions/0042`'s own analysis before this session ran (see above) — nothing analytically load-bearing
is believed lost, but the original artifact itself is gone.

**Purpose:** examine whether the app works as intended; explain why, with Google Play services'
*Nearby devices* permission denied, a prior session's connection had ended after ~89s when the Buds
themselves closed both RFCOMM channels while the audio link stayed up (`ai-sessions/0040` §2.1).

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-059`                     |
|      Group(s)    |                        AU                          |
|       Date       |                    2026-09-20                      |
| Firmware version |                  `release_5.203`                   |
|   Test device    |  Pixel 9a, GrapheneOS, Android 17 `CP2A.260805.005`. App: OpenControl for Pixel Buds, built from the `ai-sessions/0041` commit (`9fe4b70`) — 🟡 timing-consistent, not exhaustively re-verified this migration. Google Play services present; *Nearby devices* permission state at capture time was **not recorded** (`CAP-059-FINDINGS.md` §4, a capture-procedure gap). |
| Video files      | `CAP-059-recording1.mp4` (40.14s, 17:16:59–17:17:39 local) + `CAP-059-recording2.mp4` (286.49s, 17:17:46–17:22:29 local) — two camera films of the phone/case/buds, burned-in wall-clock overlay + microphone audio. Both had their address-overlay lines cropped/blacked-out before commit; the timestamp line is untouched (`ai-sessions/0043` Phase 0). |
| Log file         | `CAP-059-btsnoop_hci.log` (6,156 packets, 0/6,156 `cap_len`≠`len` mismatches — untruncated, raw path) |
| App debug export | `CAP-059-debug-export.log` (149 lines) |
| App logcat       | `CAP-059-OpenControl-for-Pixel-Buds-log-6a07477ae6ec.txt` |
| System log       | `CAP-059-System-log-ce75839464e2.txt` |
| Buds MAC (partial, `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` |
| Clock offset     | Phone ≈ video overlay + 1.3s (±0.5s), measured at three status-bar minute-flip moments (`CAP-059-FINDINGS.md` header). HCI/logcat are UTC; app debug export and video overlay are phone-local (UTC+2) — a fixed +2h shift aligns them to ≤70ms. |

## Capture-integrity pre-flight

```
$ capinfos CAP-059-btsnoop_hci.log
Packet size limit: (not set)
Number of packets: 6156

$ tshark -r CAP-059-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
mismatches: 0
```

## Event Timeline

Times are `wall-clock (local)`, phone-side unless stated. Test-IDs are assigned from what this
session's evidence actually confirms (`CAP-059-FINDINGS.md`), not from the candidate list the
migrating prompt proposed — anything not independently confirmed by wire+film evidence is left
unassigned rather than guessed.

| Time (local) | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 17:16:59 | Recording 1 starts. Phone shows Settings → Bluetooth, toggle OFF. | — | — | `CAP-059-recording1.mp4` t=0 |
| 17:17:41.507 / .589 | Two `association requested` calls fire in quick succession (double-request bug); system log shows the second processed request answered "More than one AssociationRequests are processing." | User (app) | `PAIR-001` | `CAP-059-FINDINGS.md` §3 |
| 17:17:48.024 | `createBond()` called. | App | `PAIR-001` | §3 |
| 17:17:49.121 | HCI `Authentication Complete` — bond actually succeeds here. | Hardware | `PAIR-001` | frame 691, §3 |
| 17:17:49.6–50.76 | HFP connects (state 1→2); A2DP comes up. | Hardware | `PAIR-001` | §2/§3 |
| 17:17:50.299–17:18:53.44 | DLCI 0x04 (Fast Pair Message Stream) contention window: 4 app claim attempts collide with Google Play services (attempt 1 fails, attempt 2 succeeds each time); after 17:18:53.44, Play services never re-claims again this session (11 further app claims all succeed on attempt 1). | Hardware/GMS | — | §4 |
| 17:17:50.895 onward | DLCI 0x08 pushes `Group 0x0e Code 0x01` (Left/Right/Case battery) 19 times across the session, identical value (Left 100, Right 100, Case 97%) every time. | Hardware | — | §6, frame 1211 first instance |
| 17:18:33.046 | App logs `Pairing: bond timed out` — contradicts the already-successful 17:17:49.121 bond; no `bond state` line logged at all. | App (defect) | `PAIR-001` | §3 |
| 17:18:35.842 / :50.48 / :51.90 / 17:19:03.219 | Find My Buds: Left ring start, Right ring start, (a repeat), Stop — all 4 ACKed on the wire; ringing audible on the camera mic (3.0s-interval tone bursts) starting ≈3.1s after each command and continuing past the Message Stream socket's own release. | User (app) | `FIND-001`, `FIND-002` | §6, frames 2310/2491/2506/2600 |
| ~17:19:03–22 | Both earbuds removed from the case (video-confirmed); battery-claim field on DLCI 4 transitions from `e4 e4 ff` (both seated/charging) to `64 64 ff` (both out, unplugged). | User (hardware) | — | §6, frames 1056–2595 vs. 2776–4882 |
| 17:19:21–33 | ANC cycled through all four modes: Off → Active → Adaptive → Transparency (UI-confirmed on film, byte values `20`/`08`/`40`/`80` on the wire, Notify follows Set by 0.26s each time). | User (app) | `ANC-001`, `ANC-002`, `ANC-003`, `ANC-004` | §6, frames 2768–3001 |
| 17:19:43 | User taps **Disconnect** in-app. | User (app) | — | §1 row 1, frame 3068 |
| 17:19:45.720 | `ConnectionState: Ready -> Disconnected`. | App | — | §1 |
| ~17:20:13–15 | User taps the Buds' row in Android's own Bluetooth settings panel (off-camera tap, on-camera state flip "Actief"→"Opgeslagen"); Android tears down HFP then the ACL. | User (Android Settings) | — | §1 row 2, frame 3390 |
| 17:20:35–36 | Buds reconnect; app's Android-link mirror is re-created on resume and reads correctly this time (`CONNECTED via profiles [2]`→`[1, 2]`). | Hardware / App | — | §2 |
| EQ screen viewed at some point in this window (exact tap not separately isolated this pass) | App's first `ReadSetting 4:16` returns `Low bass −5, Bass −1.5`, matching the film's EQ screen; a later read returns `[-2, 0, 2, 3, 5]`, consistent with the Clarity preset having been applied and persisted. | User (app) | `EQP-006` (🟡 — preset value pattern, not independently confirmed against film for the exact tap) | §6, frames 1455/3202/3558/4855 |
| 17:21:13.900–17:21:14.460 | App releases and re-claims DLCI 4 within 0.56s (unusually fast re-open for this session). | App | — | §1 |
| 17:21:16.023 | **Buds themselves** send `DISC` on DLCI 2, then DLCI 4 five ms later — the phenomenon this capture was made to investigate. ACL/audio unaffected (Spotify keeps playing). | Hardware (Buds) | — | §1 row 3, frames 4595/4597 |
| 17:22:29 | Recording 2 ends. | — | — | `CAP-059-recording2.mp4` end |

## Analysis checklist

- [x] Correlate video, HCI log, app debug export, app logcat, and system log against each other —
      done, `CAP-059-FINDINGS.md` §1–§9.
- [x] Answer the capture's own three questions (drop cause, idle/periodic/second-host, `07 34`
      answered?) — done, `CAP-059-FINDINGS.md` §1/§5.
- [x] Cross-reference Test-IDs this Group actually exercised (`AGENTS.md` §13 traceability) — table
      above; only `PAIR-001`, `ANC-001`–`004`, `FIND-001`/`002` are 🟢-confirmed by wire+film. `EQP-006`
      is 🟡 (value pattern only). `EQS-001`–`005` and `EQP-005` are **not** claimed for this capture —
      no evidence in `CAP-059-FINDINGS.md` independently isolates individual slider drags or the Vocal
      Boost preset.

## Next steps after filling this in

- [x] Write `CAP-059-FINDINGS.md` per `PROJECT_RULES.md` §2 — done.
- [x] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status `planned` → `analyzed`
      (`ai-sessions/0043`).
- [x] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `PAIR-001`/`ANC-001`–`004`/`FIND-001`/`002` rows'
      Evidence columns with a pointer to `CAP-059` — done.
- [x] Folder already named with its actual session date/start-time/end-time
      (`CAP-059-2026-09-20_17-16-59_17-22-29-Group_AU`).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-059-2026-09-20_17-16-59_17-22-29-Group_AU/CAP-059-EVENT-NOTES.md

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-059-2026-09-20_17-16-59_17-22-29-Group_AU/CAP-059-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-059-2026-09-20_17-16-59_17-22-29-Group_AU/CAP-059-EVENT-NOTES
