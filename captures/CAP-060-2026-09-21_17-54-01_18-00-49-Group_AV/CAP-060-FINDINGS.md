# Findings: `CAP-060` (Group AV — second hardware run of the `ai-sessions/0042` build; four maintainer questions answered)

Standardized, evidence-based extraction from `CAP-060-btsnoop_hci.log`, `CAP-060-recording.mp4`,
`CAP-060-debug-export.log`, `CAP-060-OpenControl-for-Pixel-Buds-log-c574d45537fa.txt` (app logcat),
and `CAP-060-System-log-62cb790b4007.txt` (Android system log), per `ai-sessions/0043`.

- 🟢 **FACT** — directly observed in this capture, with a frame number/timestamp.
- 🟡 **HYPOTHESIS** — plausible reading, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-060` · **Date:** 2026-09-21, 17:54:01–18:00:49 local · **Firmware:** 🟢 FACT
`release_5.203` · **Phone:** Pixel 9a, GrapheneOS, Android 17 `CP2A.260805.005`, Google Play services
present and active · **App under test:** OpenControl for Pixel Buds, `ai-sessions/0042` build
(`1efa86b`). **Log file:** `CAP-060-btsnoop_hci.log`, 7,044 packets, 0 `cap_len`≠`len` mismatches.
**Video:** `CAP-060-recording.mp4`, 408.65s, no address overlay (confirmed across the full duration).
**Buds MAC (partial):** `04:00:6e:cf:6e:07`.

---

## 1. Why did the session sometimes lose its connection? (`ai-sessions/0043` question 1)

This capture shows **six non-user drops** plus **one deliberate user disconnect** and **two failed
reconnect attempts** — substantially more activity than `CAP-059`'s three drops, letting this capture
distinguish several genuinely different mechanisms rather than one.

| # | Time | HCI evidence | Video/system-log context | Mechanism |
|---|---|---|---|---|
| 1 | 17:55:23.097 | Buds→phone `DISC` on DLCI 0x02 then 0x04, **1.5ms apart** (frames 2188/2189); ACL (`0x000b`) untouched; phone acks `UA` (2190/2191) | — | 🟢 **Buds-initiated RFCOMM-only closure**, ACL/audio unaffected. Identical mechanism to `CAP-059`'s third drop (`CAP-059-FINDINGS.md` §1). |
| 2 | 17:57:29.912 | Buds→phone `DISC` on DLCI 0x02 only (frame 3651; DLCI 4 was already on-demand-released, not simultaneously open); phone acks (3652) | — | 🟢 Same mechanism as #1 — DLCI 4 simply wasn't open at that instant. |
| — | 17:58:37.339 | Phone→Buds `DISC` on DLCI 0x02 (frame 4116, dir=0) | App logs "Session ended by the user's Disconnect tap" | 🟢 **User's own Disconnect tap.** Not a "drop." |
| 3 | 17:58:54.642 | Full `Disconnection Complete`, handle `0x000b`, reason `0x13`; preceded by a phone-initiated `DISC` on the HFP DLCI 0x0c (frame 4263, 1.4s earlier) | **Video-confirmed** (`t=292s`): Android's own Bluetooth settings panel open, finger directly on the Buds' row ("Actief. Batterijniveau 100%"); system log shows `input_interaction: … SystemUIDialog` and a `com.android.settings` process unfreeze at the identical timestamp (15:58:53.164–.275 UTC) | 🟢 **User tapped the Buds' row in Android's own Bluetooth panel.** Identical mechanism to `CAP-059`'s second drop. |
| 4 | 17:59:35.008 | Full `Disconnection Complete`, handle `0x000b`, reason `0x13`; **no** RFCOMM `DISC` frame on any DLCI precedes it | Video (`t=331s`, ~3s earlier): maintainer picks up the case/right earbud. System log: **no** `input_interaction`/`SystemUIDialog` line anywhere near this timestamp — only `Telecom` reacting to the link already being gone | 🟡 **HYPOTHESIS:** possibly triggered by physical handling of the earbud/case, occurring ~3s prior — not a Settings-panel tap (ruled out by the system log), not an app action, and not preceded by any RFCOMM-level teardown attempt from either side. |
| 5 | 17:59:59.991 | Full `Disconnection Complete`, handle `0x000b`, reason `0x13`; no preceding RFCOMM `DISC` | Video (`t=358s`): maintainer is **actively holding** the case/buds in hand at this exact moment. No system-log UI-interaction line nearby | 🟡 **HYPOTHESIS**, same as #4 — coincides directly with physical handling this time (not just preceding it). |
| — | 18:00:12.984–27.668 | Two HCI Page Timeouts (status `0x04`, frames 6150/6214) before a third reconnect attempt succeeds (frame 6264, status `0x00`) | — | 🟢 The Buds were briefly unreachable at the link layer for ~15s (a page/inquiry-level failure, not an RFCOMM or ACL teardown — the ACL from drop #5 was already down by this point). |
| 6 | 18:00:45.206 | Full `Disconnection Complete`, handle `0x000b`, reason `0x13`; no preceding RFCOMM `DISC` | Video (`t=404s`): buds sitting untouched on the phone case, no hand visible, app already showing "Not connected" | 🔴 **OPEN.** No video-confirmed handling, no Settings-panel tap, no HFP-teardown-first pattern — the one drop in this capture with no candidate trigger at all. |

**Reason-code note:** all four ACL-level `Disconnection Complete` events in this capture report reason
`0x13` ("Remote User Terminated Connection" per the Bluetooth Core spec's HCI reason-code table) —
including drop #3, which is video-confirmed as a **local** user action (a Settings-panel tap). This
matches `CAP-059-FINDINGS.md` §1's own drop 2, which also reported `0x13` for a video-confirmed local
Settings-panel disconnect. 🟡 **HYPOTHESIS, now reproduced across two independent captures:** this
Android/Fluoride Bluetooth stack reports `0x13` for what its own host requested via a normal
`HCI_Disconnect`, not literally "the peer initiated it" — the reason code alone cannot distinguish a
local-Settings-triggered disconnect from a genuinely Buds-initiated one; only the presence/absence of
a preceding phone-initiated HFP/RFCOMM teardown and the system-log `input_interaction` signature can.

**Answer to the capture's implicit question:** drops #1/#2 are the same "Buds close RFCOMM while ACL
stays up" phenomenon already characterized in `CAP-059`. Drop #3 is a routine, expected
Settings-panel-triggered full disconnect (not a bug). Drops #4/#5/#6 are a **third, previously
uncharacterized pattern** — full ACL-level teardowns with no RFCOMM handshake and no clear phone-side
trigger — two of three temporally correlated with physical bud/case handling (🟡, not proven for all
three), one (#6) genuinely unexplained (🔴). This is new information `ai-sessions/0042`'s own
open-questions list did not anticipate (see §4 below).

## 2. Why was the Case battery status unavailable? (`ai-sessions/0043` question 2)

**8/8 `Case battery not read: Timeout`** (`grep -c "Case battery not read" CAP-060-debug-export.log` → 8). This capture ran the
`ai-sessions/0042` build: a **receive-only** claim (ADR-035 item 2) with no retry.

**Every Case push that follows a channel open answers a phone-side `0e 04 00 00` request; the app's receive-only claims got
none.** 🟢 FACT. Command: per-open reassembly of `tshark -r CAP-060-btsnoop_hci.log -Y "btrfcomm.dlci==8" -T fields -e frame.number
-e frame.time_relative -e frame.p2p_dir -e btrfcomm.frame_type -e data.data` (each direction's `[Group][Code][Len:2BE][Value]` stream
is rebuilt per `SABM`; script and full table in `ai-sessions/0045_MAINTENANCE_RESULT_2026_09_24.md` §3.1). Per open:

| `SABM` (t) | Opened by | Phone `0e 04` | First `0e 01` push | Channel ended |
|---|---|---|---|---|
| 1223 (38.689 s) | Play services | 1284 (+0.431 s) | 1307 (+0.595 s) | 1822, phone `DISC` (+12.09 s) |
| 1864 (51.574 s) | **app**, attempt 2 | — | — | 1870, **Buds** `DISC` (+0.01 s) |
| 1928 (52.942 s) | Play services | 1979 (+0.263 s) | 1993 (+0.286 s) | 2216 |
| 2245 (78.295 s) | Play services | 2279 (+0.586 s; `04 15 00 00 0e 04 00 00`) | 2310 (+0.964 s) | 2498 |
| 2520 (86.641 s) | **app**, attempt 2 | — | — | 2525, **Buds** `DISC` (+0.13 s) |
| 2584 (88.729 s) | Play services | 2592 (+0.199 s, bundled) | 2628 (+0.755 s) | 3124 |
| 3770 (213.085 s) | **app** | — | none; **zero** data frames | 3783, app release (+2.78 s) |
| 4221 (277.014 s) | **app** | — | none; zero data frames | 4246, app release (+2.47 s) |
| 4460 (294.878 s) | **app** | — | none; zero data frames | 4698, phone-side `DISC` (+1.85 s) |
| 4792 (299.231 s) | Play services | 4830 (+0.591 s) | 4856 (+0.771 s) | 4948 |
| 4975 (310.300 s) | Play services | 5015 (+0.542 s) | 5041 (+0.742 s) | 5084 |
| 5139 (318.615 s) | Play services | 5149 (+0.238 s, bundled) | 5168 (+0.623 s) | 5226 |
| 5262 (328.897 s) | Play services | — | none; zero data frames | ACL Disconnection Complete, frame 5277 |
| 5449 (335.540 s) | **app** | — | none; zero data frames | 5749, phone-side `DISC` (+2.15 s) |
| 5818 (342.729 s) | Play services | 5828 (+0.179 s, bundled) | 5856 (+0.610 s) | 5968 |
| 5989 (348.652 s) | **app**, attempt 2 | — | — | 5996, **Buds** `DISC` (+0.13 s) |
| 6058 (350.949 s) | Play services | 6068 (+0.170 s, bundled) | 6091 (+0.460 s) | — |
| 6419 (383.865 s) | **app** | — | none; zero data frames | 6728, phone-side `DISC` (+2.04 s) |
| 6789 (388.070 s) | Play services | 6833 (+0.069 s) | 6859 (+0.097 s) | — |

The app's opens are identified by its own log at the same wall-clock second ("RFCOMM channel 0x08 connected (attempt n/3)",
`CAP-060-debug-export.log` lines 36, 62, 218, 300, 328, 356, 372, 403; each followed by "Case battery not read: Timeout"); the first
open (1223) precedes the app's Connect (log line 17:54:54.609), so it is Play services'. The phone-side `DISC`s at 1822, 2498 and 5968
coincide with the app's failed *first* attempts (log lines 35, 61, 371): the stack's failure path closed Play services' port
(ADR-032's mechanism), after which the app's second attempt connected and was closed by the Buds 10–130 ms later.

- 🟢 FACT: **all 10 payload-carrying Play-services opens** send `0e 04 00 00` before the first `0e 01`, which follows it by 23 ms–0.38 s.
  (`CAP-059` agrees: 3/3, `CAP-059-FINDINGS.md`.)
- 🟢 FACT: **all 8 app claims** (which send nothing) received **no** `0e 01`: 5 held the channel open 1.85–2.78 s with zero data frames,
  3 were closed by a Buds-side `DISC` within 0.13 s.
- 🟢 FACT: unrequested pushes exist, but only 10 s–2 min into a long-held Play-services channel: 1653, 2196, 2760, 2928, 3065, 3099,
  4938, 5080, 5196 (payload e.g. 1993 `0e 01 00 23 0a 21 0a 03 61 6c 6c 12 1a 0a 06 08 64 10 01 18 01 0a 06 08 64 10 01 18 02 0a 06 08 54
  10 01 18 03 20 01` = Left 100, Right 100, Case `0x54` = 84 %).
- 🟡 HYPOTHESIS: sending `0e 04 00 00` on the app's own claim is sufficient to get the post-open push (it always precedes the push here;
  not yet tested from the app). Implemented by `DECISIONS.md` ADR-039; hardware re-test in `ai-sessions/0045`.
- 🔴 OPEN QUESTION: why the Buds closed the app's second attempt within 0.13 s three times (always right after the stack had closed Play
  services' port).

**Answer to ADR-035's open item** ("do the Buds push the Case level without the phone-side `0e 04`?"): **not after an open** — the post-open
push the app depends on follows `0e 04` every time; only later, spontaneous pushes on a long-held channel come without it. Contention with
Play services is real (the retry of ADR-038 addresses it) but is not the reason the receive-only claims failed.

## 3. Why can't you see which bud is docked? (`ai-sessions/0043` question 3)

**The joint (both-earbuds) signal works correctly and reached the UI in this capture.** The
`Settable-toggles` byte (`DECISIONS.md` ADR-024) — the 3rd value byte of the "Notify ANC state"
frame (`08 13 00 04 01 e8 <toggles> <mode>` on DLCI 4) — is directly readable in the debug export and
shows a clean, video-corroborated transition:

| Time window | Value | Video correlation |
|---|---|---|
| 17:54:56.047–17:55:41.708 | `00` (both docked) | Session start — buds shown seated in the case |
| 17:56:36.662–17:59:00.627 | `e8` (not both docked) | Spans the entire ANC-cycling window (17:56:36–17:58) and the EQ-preset window (17:57:45–18:06) — consistent with the buds being out/worn during active use |
| 17:59:41.231–18:00:29.610 | `00` (both docked) again | Trailing part of the session |

This is 🟢 FACT, directly matching the video's own "Both earbuds are in the case (as of the last
update)" text shown at `t=358s` (17:59:59), which correctly reflected `00` (the most recent read,
17:59:53.226) — **the joint dock-state signal did arrive and did reach the UI correctly** in this
capture; it is not a bug in what was built, confirming `ai-sessions/0043`'s own framing that this is a
scope question, not a defect.

**`Group 0x04 Code 0x12` (the `INEAR-002`–`004` candidate for a *per-earbud* signal) does not track
anything in this capture.** `tshark -Y "btrfcomm.frame_type==0xef && data.data[0:2]==04:12"` finds 13
occurrences on DLCI 0x08 (Buds→phone), all carrying the **identical** payload `0412000408021001`
(frames 1300, 1981, 2301, 2603, 2941, 4846, 4890, 5031, 5047, 5157, 5837, 6075, 6846) — spanning
17:54:45 through 18:00:33, including the entire window where the joint `Settable-toggles` byte
changed from docked to not-docked and back, and including the moment (17:59:32–59:59) where the
maintainer visibly picked up and handled a single earbud. **🔴 OPEN / negative result:** in this
capture, `Group 0x04 Code 0x12` shows no correlation with any dock-state or handling event —
this specific capture provides no positive evidence for the `INEAR-002`–`004` hypothesis and, if
anything, weakens it (a genuine per-earbud in-ear/dock sensor would be expected to change at least
once across 6 minutes including a visible single-bud removal). This does not rule the hypothesis out
(the field could require a longer settle time, or respond only to a true in-ear insertion rather than
a brief out-of-case handling) — it remains 🔴 OPEN QUESTION, now with one negative data point.

**Conclusion:** per-earbud dock/in-ear status remains a genuinely unimplemented signal, as ADR-024's
own text already states — this capture found no working candidate for it. The UI's "as of the last
update" wording (joint state only) is accurate to what the protocol currently exposes; the fix here is
the timestamp work in Phase H, not a new dock-state channel.

## 4. What is the ANC tile? Why couldn't the maintainer find it? (`ai-sessions/0043` question 4)

**Mechanism, confirmed against the official Android documentation** (fetched
`developer.android.com/develop/ui/views/quicksettings-tiles`, 2026-09-22): *"Creating a `TileService`
for your app does not add it to the user's Quick Settings panel. Your `TileService` acts as an
interface with the tile only after the user has added it."* The current manual steps: *"1. Swipe down
to open the Quick Settings panel. 2. Tap the edit button. 3. Scroll through all tiles on their device
until they locate your tile. 4. Hold down your tile, and drag it to the list of active tiles."* A
programmatic alternative exists from Android 13+: `StatusBarManager.requestAddTileService()`, which
prompts the user directly instead of requiring manual discovery — 🟡 **not currently used by this
app** (a genuine, low-risk UX improvement candidate for a future session, not attempted here without
a decision).

This is a fundamentally different UI surface from a home-screen widget (`AppWidgetProvider`) — no
widget was ever built, so there is nothing to find there; the tile exists but was never manually added
to the Quick Settings panel, which is expected default behavior, not a bug.

**Manifest/code review — nothing wrong found.** `AndroidManifest.xml`'s `AncTileService` declaration
has all three elements the official docs require:
`android:exported="true"`, `android:permission="android.permission.BIND_QUICK_SETTINGS_TILE"`, and
the `android.service.quicksettings.action.QS_TILE` intent-filter action — an exact match to the
documented template. `AncTileService.kt` correctly extends `TileService`, implements
`onStartListening`/`onStopListening`/`onClick`/`render`, and calls `qsTile.updateTile()`.
`ic_anc_tile.xml` is a valid vector drawable with a non-zero viewport and path data — nothing
malformed. **No evidence found of anything in the app preventing discovery.**

**Confirmed exhaustively (not just the prompt's spot-check): zero tile activity in this capture.**
`grep -c` for `AncTileService`/`QS_TILE`/`BIND_QUICK_SETTINGS_TILE`/`ANC tile` across both the app
logcat (212 lines) and the system log returns **0** matches — consistent with the tile never having
been added to this phone's Quick Settings panel at all, not with a binding failure (a bound-but-broken
tile would leave `onStartListening`/service-lifecycle traces in logcat; there are none).

**GrapheneOS-specific behavior:** 🔴 no documentation found (in this session's research) describing
GrapheneOS altering the standard AOSP Quick Settings tile-discovery flow — Quick Settings/System UI is
not an area GrapheneOS is known to modify, but this is not independently confirmed here.

---

**Result:** see the findings above; nothing here is promoted to `PROTOCOL.md` as FACT by this document
(`AGENTS.md` §6). The ADR-035-superseding proposal in §2 is a proposal only, taken to the Phase F
checkpoint (`ai-sessions/0043_FEATURE_RESULT_2026_09_22.md`).

**Promoted to `PROTOCOL.md`:** nothing yet from this capture.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-060-2026-09-21_17-54-01_18-00-49-Group_AV/CAP-060-FINDINGS.md

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-060-2026-09-21_17-54-01_18-00-49-Group_AV/CAP-060-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-060-2026-09-21_17-54-01_18-00-49-Group_AV/CAP-060-FINDINGS
