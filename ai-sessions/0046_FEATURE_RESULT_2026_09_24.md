# 0046_FEATURE_RESULT_2026_09_24.md — Full analysis of CAP-061 and fixes for the 0045 build's first hardware run

**Number:** 0046
**Category:** FEATURE
**Date:** 2026-09-24
**Title:** Fully analyse `CAP-061` (video, HCI snoop log, app debug export, app logcat, system log), record it as `CAP-061-EVENT-NOTES.md`/`CAP-061-FINDINGS.md`, root-cause every failure the maintainer saw, and fix the OpenControl app accordingly, with maintainer sign-off wherever the rules require it
**Status:** complete

> All phases done (the session ran over a usage-limit break on 2026-09-24/25 and resumed from the Progress block). Nothing is committed:
> that waits for the maintainer's go-ahead (Phase F task 23). No subagent was used; every read and write was done in this session.

## Progress (final)

- Phase 0: files identified, privacy scan of every frame, clock offsets measured; maintainer answers recorded (§5); folder moved with
  identical checksums, mode 644, LFS attributes confirmed.
- Phases A–D: `CAP-061-EVENT-NOTES.md`, `CAP-061-FINDINGS.md`.
- Phase E: parser fix + real-bytes tests; checkpoint (four questions); ADR-043, dock-line removal and tile message implemented.
- Phase F: build/test/lint gate green; `PROTOCOL.md`, `DECISIONS.md`, `ARCHITECTURE.md`, capture/testplan docs, registry, TODO,
  CHANGELOG, INDEX, README, PROJECT updated; `lint_docs.py` exit 0.

---

## 1. Plain-language answers

1. **What went wrong (ANC, EQ, Find, the Safe Mode message):** one bug. When the Buds connect they announce their firmware. That message
   contains one field of a type (an 8-byte number) the app's small protobuf reader did not know, and the reader then threw the whole message
   away. So the app never "saw" `release_5.203`, and Safe Mode — built in 0045 to block writes on unknown firmware — blocked **every** ANC, EQ and
   Find command. Nothing was ever sent (the HCI log has zero ANC/Ring/EQ writes). The tests had used a hand-made announcement without that
   field, which is why they passed. **Fixed**, and the tests now use the real bytes from `CAP-061`.
2. **Case battery:** the 0045 idea (send `0e 04` on DLCI 0x08) did not work: 20 of 20 tries got nothing — the Buds either closed the
   channel at once or ignored the request — and every try knocked another app (most likely the Google app's Assistant-on-headphones service)
   off that channel. With your approval the app now reads the Case from a status stream on its **own** channel (DLCI 0x02,
   `SubscribeRuntimeInfo`), which matched the real Case level in 13 of 13 older captures, and it no longer touches DLCI 0x08 (ADR-043).
3. **"Both earbuds seem to be in the case" with one out:** the Buds themselves reported "no switchable ANC modes" (the byte the line was
   derived from) while the Left bud was still in your hand — the same message said that bud was not charging. The derived sentence was
   simply wrong here. With your approval the line is **removed**; each bud's "(charging)" on the battery lines stays.
4. **ANC tile:** Android answered "already added" (code 1) — the tile is in your Quick Settings (on a page not visible on film). The app
   now tells you so instead of doing nothing visible.
5. **Still needs the Buds:** everything above — nothing in this session is hardware-verified. Re-test instructions in §9.

## 2. Reading done before acting

`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md` (full), `PROTOCOL.md` (§0–§5 in full, §6 up to line ~2720 in full, the
remaining APK-research open items by heading only, §7–§8 in full — disclosed), `DECISIONS.md` (all 42 ADRs), `TODO.md` (priority list,
Phase 3–5, debt, open questions in full; Phase 1/2 history skimmed — disclosed), `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`,
`0045` RESULT (full), `CAP-050`/`CAP-060` EVENT-NOTES and FINDINGS (layout and content), the Capture Index, the Test-IDs used, `id_registry.csv`
rows used. The 0042 §2–§5 and 0043 RESULT were not re-read line by line (their method is embodied in `CAP-060`'s files, which were) — disclosed.

## 3. Per-symptom table

| Symptom (maintainer) | Root cause | Evidence | Status / fix |
|---|---|---|---|
| Safe Mode message | `Proto.fields` returned `null` on wire type 1; the announcement's field 5 is fixed64 → empty firmware list → ADR-042 gate refused | frame 1508 bytes; 140/140 announcements; export: 30 "Safe Mode: write refused"; `CaseBatteryFrame.kt:104-131` (before) | 🟢 FACT; fixed (skip fixed64/fixed32), real-bytes tests, M1 |
| ANC does not change | same — no `08 12` ever sent | zero `08 12` in the log | 🟢 FACT; fixed by the above; hardware re-test (§9 R2) |
| EQ does not work | same — no `WriteSetting` sent | zero `WriteSetting`; export "EQ request failed: UnsupportedFirmware" ×7 | 🟢 FACT; fixed by the above |
| Find does not work | same — no `04 01` sent | zero `04 01` | 🟢 FACT; fixed by the above |
| Case unavailable | the DLCI 0x08 claim: 12 opens closed by the Buds in 10–198 ms, 8 held with `0e 04` unanswered | FINDINGS §2 per-open table | 🟢 FACT; ADR-043 (runtime-info source), M2/M3 |
| "Both in the case" with one out | the Buds' Settable byte `0x00` while the Left bud was in the hand | frame 5560 vs 5557 and 30 fps film | 🟢 FACT; dock line removed (ADR-024 Update) |

§3 leads of the prompt: **lead 1 held** (and the harness default was synthetic too); **lead 2 held and went further** (the per-open table shows
Buds-side `DISC`s, then unanswered `0e 04`; the 0x04 release did complete first — `DISC` 0x04 before every 0x08 `SABM`); **lead 3 held**
(the derived reading was wrong at frame 5560; 19 of 20 readings were right); **lead 4:** result 1 = already added (docs quoted in §6).

## 4. `ai-sessions/0045` §9 verdicts

(A) Case with `0e 04`: 🔴 refuted (8/8 unanswered). (B) ANC answers: ⚪ not exercised (nothing sent). (C) Safe Mode on `release_5.203`: 🔴
refuted (root cause fixed). (D) Dock line: 🟡 partly (provisional marking worked; one wrong reading from the Buds) → line removed. (E), (F): ⚪
expected but not observed. (G) Notification: 🟢 service start/stop at every Connect/Disconnect/loss (system log). (H): tile already added; EQ
audibility and Find ringing ⚪ not exercised. Detail: `CAP-061-FINDINGS.md` §8.

## 5. Maintainer decisions (this chat)

- **Phase 0 (2026-09-24, `AskUserQuestion`):** Privacy → **"Keep it unblurred"** (the two notification-shade windows with third-party names stay
  in `CAP-061-recording.mp4`). Migration → **"Yes, as proposed (Recommended)"**.
- **Checkpoint (2026-09-24, `AskUserQuestion`, previews carried the exact texts now in the files):**
  - Case → **"Runtime info, drop 0x08 (Recommended)"** → ADR-043 + ADR-039 Update.
  - FACT → **"Promote to 🟢 FACT (Recommended)"** → `PROTOCOL.md` §4.3 Option F.
  - Dock line → **"Remove the dock line (Recommended)"** → ADR-024 Update, UI line removed.
  - Announce → **"Record as 🟢 FACT (Recommended)"** → `PROTOCOL.md` §2.2a update.
- **Not asked, proposed now:** `AGENTS.md` §5's note still names "DLCI 0x08's `Group 0x0e Code 0x01` (Case, ADR-035/039)" as an implemented
  source. Replacement "… and DLCI 0x02's `SubscribeRuntimeInfo` entry 6.1 (Case, ADR-043)" — **approved in chat 2026-09-25 ("ja, pas AGENTS.md
  aan") and applied**, together with the go-ahead to commit and push.

## 6. What was changed

**App (`android/`):**
- `data/codec/CaseBatteryFrame.kt` — `Proto.fields` skips wire types 1/5 by size and reads 64-bit varints; `GsndMessageStream.batteryRequest`
  removed (ADR-043). `Varint.decodeLong`. New `codec/RuntimeInfo.kt` (`RuntimeInfoDecoder`, entry 6.1 only). `Maestro.subscribeRuntimeInfoRequest`.
  `CodecRouter`: `RoutedFrame.RuntimeInfoCase`.
- `BudsRepositoryImpl` — subscribes once per Connect after the EQ read; `readCaseBattery` and the DLCI 0x08 claim removed; *Refresh battery*
  re-reads Left/Right only.
- `hardware/BudsSdpUuids` — `GSND_CONTROL` removed (unused).
- `ui/ConnectionScreen`, `OpenControlNavHost`, `app/MainActivity` — dock line removed; Case copy ("The Buds haven't reported the Case level on this
  connection."); the tile-request result shown as a message (`ancTileResultText`).
- Tests: `Cap061Fixtures.kt` (frame 1508, serial redacted, CRC derivation verified offline), 4 new codec tests + fuzz extension in
  `CaseBatteryCodecTest`, new `RuntimeInfoCodecTest` (CAP-036 1410, CAP-041 782, CAP-050 1163), repository harness announces the real bytes by
  default, `helloWithFirmware` keeps the real layout, 6 DLCI 0x08 claim tests replaced by 4 ADR-043 tests, one synthetic test replaced by a
  real-bytes gate test.

**Docs:** `DECISIONS.md` (ADR-043; Updates ADR-024/035/038/039; status lines), `PROTOCOL.md` (§0.1 row, §2.2a, §4.1, §4.3 current state, Option E
update, new Option F, §6 note, §8 row), `ARCHITECTURE.md` (§1, diagram, §3.1, §4, §5, §5a, §6.0b, §8.1), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group AW,
Capture Index row), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (13 evidence cells), `id_registry.csv` (ADR-043, CAP-061, ADR-035/038/039 rows), `TODO.md`,
`CHANGELOG.md`, `README.md`, `PROJECT.md`, `ai-sessions/INDEX.md`, the two capture documents.

## 7. Verification

`cd android && ./gradlew --offline assembleDebug testDebugUnitTest test lint` → **BUILD SUCCESSFUL**. Unit tests (debug variant): `:data` **1480**,
`:hardware` **49**, `:domain` **11** — 0 failures (release variants also ran). Lint: `:data`, `:hardware`, `:ui` "No issues found"; `:app` 0
errors, the 2 pre-existing warnings (`DataExtractionRules`, `MissingApplicationIcon`); no new suppression.

**Mutation checks** (each applied, suite run, restored byte-identical with `cmp`):
- M1 — Safe Mode input: the fixed-width skip removed from `Proto.fields` → **18 failures**.
- M2 — Case request path: `subscribeRuntimeInfo()` not called at Connect → **3 failures**.
- M3 — Case decode: entry index 1 → 2 → **3 failures**.

`python3 scripts/lint_docs.py` → exit 0.

## 8. External sources (fetched 2026-09-24)

| URL | Quoted text | Used for |
|---|---|---|
| developer.android.com/reference/android/app/StatusBarManager | `TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED`: "Response indicating that the tile was already added and the user was not prompted. Constant Value: 1 (0x00000001)"; `…_TILE_ADDED` "…the tile was added. Constant Value: 2"; `…_TILE_NOT_ADDED` "…the tile was not added. Constant Value: 0" | result code 1, tile message |
| protobuf.dev/programming-guides/encoding/ | "The wire type tells the parser how big the payload after it is. This allows old parsers to skip over new fields they don't understand."; "1 I64 fixed64, sfixed64, double … 5 I32 fixed32, sfixed32, float" | parser fix |

## 9. Re-test instructions (nothing here is hardware-verified)

Checklist: phone clock with seconds in shot; HCI snoop ON, then Bluetooth off/on **on film**; write the build commit, every tap time and Play
services' *Nearby devices* permission state in an events file during the run; take all exports within 1 minute of the last action. Pre-filter
the HCI log by the Buds' connection handle (this encapsulation leaves `bluetooth.addr` empty).

- **R1 — Firmware / Safe Mode (the main fix).** Connect. *Screen:* "Firmware: release_5.203", **no** Safe Mode card. *HCI bracket:* DLCI 0x02
  `SABM`/`UA` → Buds `GetSoftwareInfo` (ch N) → app `ReadSetting 4:16` → Buds `RESPONSE` → app `SubscribeRuntimeInfo` (`10 <N> 1d ea 71 de 7d 5e 25 90
  82 1e e6`, no payload) → Buds `SERVER_STREAM`. *Refuted if* the Safe Mode card appears or the export has "Safe Mode: write refused" on
  `release_5.203`.
- **R2 — ANC.** Tap each mode. *Screen:* the mode changes, no error. *HCI:* DLCI 0x04 `SABM` → Buds `03 0a …`, `03 01 00 03 da 2d b1` → app
  `08 12 00 14 01 e8 e8 <mode> 00×16` → Buds `ff 01 00 06 08 12 01 e8 e8 <mode>` and/or `08 13 …` → phone `DISC` ≈ 1.5 s later. *Refuted if* no `08 12`
  leaves the phone, or the screen shows a mode other than the Buds' ACK/`Notify`, or an `ff 02` NAK is shown as success.
- **R3 — EQ.** Move one slider; pick a preset. *HCI:* DLCI 0x02 `WriteSetting` on the announced channel → empty `RESPONSE`. Listen for the change.
  *Refuted if* no `WriteSetting` or a `status ≠ OK` is shown as success.
- **R4 — Find.** Ring Left, then Stop. *HCI:* `04 01 00 01 02` → ACK; `04 01 00 01 00` → ACK; the ring must keep sounding until Stop. *Refuted if*
  nothing is sent or the ring stops at the 1.5 s release.
- **R5 — Case (ADR-043).** Buds in the case, lid open, Connect; then take one bud out and back. *Screen:* a Case percentage (or "The Buds haven't
  reported the Case level on this connection."). *HCI:* **no** app `SABM` on DLCI 0x08 at all; `SubscribeRuntimeInfo` `SERVER_STREAM` packets — when one
  carries entry 6.1 the screen shows that value. *Refuted if* the app opens DLCI 0x08, if a packet with entry 6.1 arrives and the screen stays
  "unavailable", or the value differs from Android's own Case level for the Buds.
- **R6 — Dock line.** No "…seem to be in the case" sentence anywhere; each bud's "(charging)" follows it into and out of the case.
- **R7 — ANC tile.** Tap "Add ANC Quick Settings tile": a message says added / already added / not added.
- **R8 — carried over:** 0045 (E) forget the Buds in Android, open the app, tap Connect → "No paired Pixel Buds found …"; 0045 (F) tap Refresh
  battery and switch apps at once — the result is there on return and DLCI 0x04 was released (`DISC`).

## 10. Open (in `TODO.md`)

When entry 6.1 is present; who owns DLCI 0x08/0x0a (🟡 the Google app's Assistant-headphones service); why Play services stops re-claiming
DLCI 0x04; the announcement's field 5/6 meaning; the GmsCompat "has crashed" notification seen on film.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0046_FEATURE_RESULT_2026_09_24.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0046_FEATURE_RESULT_2026_09_24
