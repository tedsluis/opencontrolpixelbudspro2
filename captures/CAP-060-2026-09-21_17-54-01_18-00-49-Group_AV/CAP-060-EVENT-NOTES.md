# Event Notes: OpenControl for Pixel Buds on Pixel 9a (GrapheneOS) — Group AV, second hardware run of the `ai-sessions/0042` build (`CAP-060`)

**Status:** 🟢 **Video pass + full log analysis complete** (`ai-sessions/0043`). See `CAP-060-FINDINGS.md`
for the full wire-level analysis, hex+command evidence, and conclusions this timeline references,
including the answers to `ai-sessions/0043`'s four specific questions (Case battery, connection
drops, per-earbud dock state, the ANC Quick Settings tile).

**Purpose (inferred — no maintainer-authored purpose statement exists for this capture, unlike
`CAP-059`'s events-file template):** a full end-to-end validation pass of the `ai-sessions/0042`
build — pairing from scratch, ANC mode cycling, Find My Buds (both earbuds), EQ presets, and an
extended idle/handling period — run the evening after that build's commit (`1efa86b`, 21:35:41 local
the prior evening). The session incidentally captured **six** non-user connection drops (far more
than `CAP-059`'s three), which became this capture's most significant finding.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-060`                     |
|      Group(s)    |                        AV                          |
|       Date       |                    2026-09-21                      |
| Firmware version |                  `release_5.203`                   |
|   Test device    |  Pixel 9a, GrapheneOS, Android 17 `CP2A.260805.005` (build number confirmed on-screen, video `t=9s`). App: OpenControl for Pixel Buds, built from the `ai-sessions/0042` commit (`1efa86b`) — confirmed both by timing (commit 21:35:41 local the prior evening) and by log-line content unique to that commit (`Android link: X -> Y (trigger: …)`, `Pairing: bond state BONDING`/`BONDED`, `Case battery not read: Timeout`, all present and exercised throughout). Google Play services present and active throughout (confirmed by DLCI 0x04/0x08 traffic attributable only to it, see `CAP-060-FINDINGS.md` §2/§4). |
| Video file       | `CAP-060-recording.mp4`, 408.65s (`ffprobe`), 1280×720 (portrait via rotation metadata), 17:54:01–18:00:49 local (burned-in overlay, timestamp-only — no address line, confirmed by sampling frames across the full duration). |
| Log file         | `CAP-060-btsnoop_hci.log` — `capinfos`: 7,044 packets, "Packet size limit: (not set)" (untruncated, raw path), 2026-09-21 17:54:05.797792–18:02:45.796677 (519.998885s — the HCI capture runs ~2 min past the video's end, with no further Buds activity in that tail). `tshark … cap_len≠len` → 0 mismatches. |
| App debug export | `CAP-060-debug-export.log` (416 lines, 17:54:15.503–18:00:45.353) |
| App logcat       | `CAP-060-OpenControl-for-Pixel-Buds-log-c574d45537fa.txt` (212 lines) |
| System log       | `CAP-060-System-log-62cb790b4007.txt` |
| Buds MAC (partial, `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` (same physical unit as every other capture) |
| Clock offset     | Video overlay ≈ phone wall-clock, measured at ±0s at both start (`t=0`→17:54:01, matches debug export's first line `17:54:15.503` occurring at `t≈14.5s`) and end (`t≈404s`→18:00:45, matches the final debug-export line `18:00:45.353`) — no measurable drift across the recording. HCI/logcat/system-log timestamps are UTC (local−2h); debug export and video overlay are phone-local. |

## Capture-integrity pre-flight

```
$ capinfos CAP-060-btsnoop_hci.log
Packet size limit: (not set)
Number of packets: 7044

$ tshark -r CAP-060-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
mismatches: 0
```

## Video review method

Full-duration scene-change detection (`ffmpeg -vf "select='gt(scene,0.02)',showinfo"`) found 73 raw
change points, clustering into ~21 distinct visually-meaningful moments; every cluster was extracted
and read, plus a frame at or within a few seconds of every wire-level event of interest from the text
logs (all 6 non-user drops, the user disconnect, every ANC/EQ/Find interaction window). The privacy
overlay was independently re-verified across the same set of frames plus additional samples spanning
the full 408s (start, ~1/8, ~1/4, ~3/8, ~1/2, ~5/8, ~3/4, ~7/8, end) — no address line found anywhere,
consistent with `ai-sessions/0043`'s Phase 0 finding.

## Event Timeline

> **Correction note (2026-09-24, `ai-sessions/0045`, maintainer-approved in chat 2026-09-24):** the rows below describing DLCI 0x08 as
> "contention" and Play services' pushes as unprompted predate a per-open re-derivation. Every push within 1 s of a Play-services open
> answers that open's phone-side `0e 04 00 00` (e.g. 1307 answers 1284); of the app's 8 receive-only claims, 3 were closed by the Buds
> within 0.13 s while Play services held the channel (17:54:58 and 17:55:33 are two of them) and 5 were held 1.85–2.78 s with **zero**
> data (17:57:40 onward) — those Timeouts are "no request sent", not contention. Per-open table: `CAP-060-FINDINGS.md` §2; consequence:
> `DECISIONS.md` ADR-039. The rows themselves are kept as written.

Times are `wall-clock (local)`. Test-IDs are assigned only where independently confirmed by wire +
video/log evidence together.

| Time (local) | Action | Initiator | Test-ID | Wire/video/log evidence |
|---|---|---|---|---|
| 17:54:01 | Recording starts. Quick Settings panel open, Bluetooth toggle visible. Build `17 (CP2A.260805.005)` visible. | — | — | video `t=0`; `t=9` frame |
| 17:54:15.503–23.076 | App launched; Bluetooth + notification permission prompt shown and granted. | User | `PAIR-001` | debug export lines 1–4; video `t≈14–22s` |
| 17:54:38.184–40.834 | CompanionDeviceManager association requested and granted ("Toestaan dat de app … toegang heeft tot de Pixel Buds Pro 2 van Ted?"); `createBond()` called. | User (CDM picker) | `PAIR-001` | debug export lines 9–12; video `t=38s` frame shows the CDM dialog directly |
| 17:54:40.912–42.887 | Bond BONDING→BONDED; Android link UNKNOWN→NOT_CONNECTED→CONNECTED. | Hardware | `PAIR-001` | debug export lines 13–16 |
| 17:54:44.486–17:54:56.579 | A non-app process (Google Play services, per `CAP-060-FINDINGS.md` §4) opens DLCI 0x08 and receives at least 2 `Group 0x0e Code 0x01` (Case battery) pushes over ~12s, **before** the app's own first DLCI 0x08 attempt begins. | Hardware/GMS | — | frames 1223–1822 (SABM→DISC), pushes at 1307/1308 and 1653 |
| 17:54:54.609–17:54:58.070 | App's first full connect cycle: RFCOMM 0x02 connects, DLCI 0x02 (`pw_rpc`) and 0x04 succeed; DLCI 0x08 (Case battery) attempt collides with the still-open GMS session and is bounced within ~15ms of opening. | App / Hardware | `PAIR-001` | debug export lines 17–39; frames 1822–1871 |
| 17:54:58.062 | First **"Case battery not read: Timeout."** | App | — | debug export line 38; `CAP-060-FINDINGS.md` §4 |
| 17:55:07.666 | The secondary BLE (LE) link is closed locally (handle `0x0042`, reason `0x16`, local host terminated) — routine post-pairing BLE teardown, not the classic RFCOMM link. | App/OS | — | frame 2170 |
| 17:55:23.097 | **Drop 1 (non-user):** Buds send `DISC` on DLCI 0x02 then 0x04 within 1.5ms; ACL (`0x000b`) stays up. Same phenomenon as `CAP-059`'s third drop. | Hardware (Buds) | — | frames 2188/2189 (dir=1, `DISC`); `CAP-060-FINDINGS.md` §1 |
| 17:55:29.322 | Reconnect (attempt 1/3, first try succeeds this time). | App | `PAIR-003` | debug export lines 44–46 |
| 17:55:33.145 | Second **"Case battery not read: Timeout."** — same DLCI 0x08 contention pattern. | App | — | debug export line 64 |
| 17:55:47.287–17:56:24.269 | Find My Buds exercised: 4 ring/stop commands cycling Left/Right/Stop; video confirms the Find screen and finger taps at `t=139s` ("Ringing: Right earbud"). | User (app) | `FIND-001`, `FIND-002` | debug export lines 81–113; frame `t=139` |
| 17:56:36.558–17:56:58.494 | ANC cycled 6 times: Transparency(`80`)→Adaptive(`40`)→Off(`20`)→Active(`08`)→Transparency(`80`)→Adaptive(`40`) — each `Set`/`Notify` pair on DLCI 4. | User (app) | `ANC-001`, `ANC-002`, `ANC-003`, `ANC-004` | debug export lines 121–180 |
| 17:57:11 | Maintainer switches to Spotify. | User | — | video `t=190s` |
| 17:57:38.283–17:57:41.658 | Reconnect after `PAIR-003`-style drop (see Drop 2 below is later — this is a routine app-triggered reconnect cycle continuing the session); DLCI 0x08 opened and released cleanly this time (no Timeout logged for this cycle alone, but a further Timeout follows immediately at 17:57:40.645). | App | — | debug export lines 201–221 |
| 17:57:45.421–17:58:06.476 | EQ screen used: 13 `WriteSetting` calls in ~21s (video `t=228s` shows the Presets row, finger over Vocal Boost/Clarity) — a preset-tapping sequence, not isolated single-slider drags (see `CAP-060-FINDINGS.md` §5 for why individual `EQS-*`/`EQP-*` Test-IDs are not claimed with confidence). | User (app) | `EQP-005`/`EQP-006` (🟡 — preset area confirmed on video, exact preset(s) not disambiguated) | debug export lines 222–253; frame `t=228.5s` |
| 17:57:29.912 | **Drop 2 (non-user):** Buds `DISC` on DLCI 0x02 only (DLCI 4 already on-demand-released at the time); ACL stays up. | Hardware (Buds) | — | frames 3651/3652 |
| 17:58:37.339 | **User's own Disconnect tap** — phone-initiated `DISC` on DLCI 0x02, confirmed on the wire (dir=0). | User (app) | — | frame 4116 (dir=0, `DISC`); debug export line 281 |
| 17:58:41.172 | Reconnect. | App | `PAIR-003` | debug export lines 283–286 |
| 17:58:53.164–53.417 | **User taps the Buds' row in Android's own Bluetooth settings panel** — video-confirmed directly: `t=292s` frame shows the panel open, "Pixel Buds Pro 2 van Ted — Actief. Batterijniveau 100%" highlighted, finger touching that row. System log confirms `input_interaction: … SystemUIDialog` and a `com.android.settings` unfreeze at the same instant. Android tears down HFP first (phone-initiated `DISC` on DLCI 0x0c, frame 4263). | User (Android Settings) | — | frame `t=292s` (visual confirmation); system log 15:58:53.164/.275; HCI frames 4263/4276 |
| 17:58:54.642 | **Drop 3 (ACL-level):** full `Disconnection Complete`, handle `0x000b`, reason `0x13`. The only one of the four ACL-level drops with a clear, video-confirmed user trigger. | User (Android Settings), consequence | — | frame 4290; same pattern as `CAP-059`'s second drop |
| 17:59:00.282 | Reconnect. | App | `PAIR-003` | debug export lines 311–314 |
| 17:59:32 | Maintainer picks up/handles the case and right earbud (video-confirmed, `t=331s`). | User (hardware) | — | frame `t=331s` |
| 17:59:35.008 | **Drop 4 (ACL-level, no video-confirmed Settings-panel tap):** `Disconnection Complete`, handle `0x000b`, reason `0x13`. No `input_interaction`/`SystemUIDialog` in the system log near this timestamp (unlike Drop 3) — occurs ~3s after physical handling of the earbud/case. | Hardware (Buds) — 🟡 possibly handling-triggered | — | frame 5277; system log 15:59:35.037 onward (Telecom-only, no UI interaction line) |
| 17:59:40.867 | Reconnect. | App | `PAIR-003` | debug export lines 340–343 |
| 17:59:59 | Maintainer holding the case/buds in hand, Connection tab open showing "Both earbuds are in the case (as of the last update)" and "Case: Battery unavailable" — both now stale/wrong at the moment of the screenshot given the buds are in hand. | User (hardware) | — | frame `t=358s` |
| 17:59:59.991 | **Drop 5 (ACL-level):** `Disconnection Complete`, handle `0x000b`, reason `0x13`. Directly coincides with the physical handling above (within ~1s). No system-log UI-interaction line nearby. | Hardware (Buds) — 🟡 possibly handling-triggered | — | frame 6114 |
| 18:00:12.984–18:00:27.668 | Two reconnect attempts **fail** with HCI Page Timeout (status `0x04`) before a third succeeds — the Buds were briefly unreachable at the link layer for ~15s. | App / Hardware | — | frames 6150 (status `0x04`), 6214 (status `0x04`), 6264 (status `0x00`, success); debug export lines 380–387 |
| 18:00:29.500 | Reconnect succeeds. | App | `PAIR-003` | debug export lines 387–410 |
| 18:00:44–45 | App shown on the EQ tab, "Not connected to the Buds" — already disconnected by this point; buds sitting untouched on the case, no hand visible. | — | — | frame `t=404s` |
| 18:00:45.206 | **Drop 6 (ACL-level, final):** `Disconnection Complete`, handle `0x000b`, reason `0x13`. No video-confirmed handling or Settings-panel tap nearby (the one "unexplained" drop of the three ACL-level non-Settings drops). | Hardware (Buds) | — | frame 6944; last event in both the HCI log's Buds-relevant traffic and the debug export |
| 18:00:49 | Recording ends. No further reconnect attempt in either the debug export or the HCI log's remaining ~2 minutes. | — | — | video end; HCI log continues silently to 18:02:45 |

## Analysis checklist

- [x] Correlate video, HCI log, app debug export, app logcat, and system log against each other —
      done, `CAP-060-FINDINGS.md`.
- [x] Answer `ai-sessions/0043`'s four specific questions — done, `CAP-060-FINDINGS.md` §1 (Case
      battery/drops), §2 (per-earbud dock state), §3 (ANC tile).
- [x] Cross-reference Test-IDs actually exercised — `PAIR-001`, `PAIR-003` (four reconnect cycles),
      `ANC-001`–`004`, `FIND-001`/`002` are 🟢-confirmed. `EQP-005`/`EQP-006` are 🟡 (preset-area
      video confirmation, not individually disambiguated). No `EQS-*` Test-ID is claimed — the
      13-write burst is more consistent with preset taps than slider drags, unconfirmed either way.

## Next steps

- [x] Write `CAP-060-FINDINGS.md` — done.
- [x] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status `planned` → `analyzed` — done.
- [x] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s relevant Test-ID rows' Evidence columns — done.
- [x] Folder already named with its actual session date/start-time/end-time
      (`CAP-060-2026-09-21_17-54-01_18-00-49-Group_AV`).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-060-2026-09-21_17-54-01_18-00-49-Group_AV/CAP-060-EVENT-NOTES.md

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-060-2026-09-21_17-54-01_18-00-49-Group_AV/CAP-060-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-060-2026-09-21_17-54-01_18-00-49-Group_AV/CAP-060-EVENT-NOTES
