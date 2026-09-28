# 0056_FEATURE_RESULT_2026_09_28.md — Build the press-and-hold ANC-mode list (`qhr` field 12, ADR-046) and the "In-ear detection" switch (`qhr` field 2, ADR-047) with real `CAP-056` bytes as fixtures, extend the Group AZ re-test skeleton, and list what else can be improved now

**Number:** 0056
**Category:** FEATURE
**Date:** 2026-09-28
**Title:** Implement the items of `ai-sessions/0055_CAPTURE_RESULT_2026_09_28.md` §8 (field 12 read + write, field 2 write) with real `CAP-056` bytes as fixtures, a green gate with mutation checks, `CAP-064` (Group AZ) skeleton additions, and an inventory of further improvements
**Status:** complete

## Progress (final)

- Phases 0, A, B (checkpoint §7), C (F12), D (F2), E (U-1, U-2), F (gate §9, mutations §10, compliance §11) and G (skeleton §12, documentation §13) are done.
  No subagent was used; every read and write was done in this session.
- Git at start: `a30fc1b`, only `?? android/.kotlin/` (not this session's); `origin/main` one commit ahead (`fc910f8`, CI's sitemap) — rebase before pushing.
- Scratchpad (session-local, not in the repo): `/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/fd311b55-2d6d-4202-a8dd-7ecb8dc372e3/scratchpad` —
  baseline*.log, final.log, cap056_fixtures.tsv, other2.tsv, cap0*_pwrpc.txt, all_field12.txt, crccheck.py, mutate.py, mutations.txt.
- Waiting only for the maintainer's commit/push decision (prompt task 18).

## 0. Plain-language summary (what you will see differently)

1. **Controls tab — "Modes for press and hold (both buds)".** Four boxes — Noise cancellation, Off, Adaptive, Transparency — with the Buds' own ticks and "read
   HH:MM:SS". A tap changes one mode; the box moves only after the Buds say OK ("changed HH:MM:SS"). When two are left, those two are greyed and the line "At least
   two modes must stay selected." appears. The list is shown only while the Left or Right press and hold is set to Noise control (as in the official app).
2. **"In-ear detection" is now a switch**, with "Pauses audio when you take a bud out and resumes it when you put it back." and always the note "With it off, audio
   does not pause when you take a bud out, and the 'only while worn' check for changing noise control may not apply."
3. **A setting the app has not read from the Buds yet is greyed** instead of showing "off" (U-1), and a tap while the app is reopening its channel says "The app's
   channel is being reopened — try again in a moment." (U-2).
4. On the wire only three things are new, each a copy of the official app's bytes: one more read at Connect (field 12), and the two writes. Nothing is
   hardware-verified yet: the next capture `CAP-064` has a new section VII for it.

## 1. Reading (prompt §0) — what was read in full and what not

- **In full:** `AGENTS.md` (in context), `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md` (all 858 lines), `AI_SESSION_LOG_PROCEDURE.md`,
  `ai-sessions/0055` RESULT, `CAP-056-FINDINGS.md`, `ai-sessions/0052` RESULT, `ai-sessions/0054` RESULT §2–§14, `CAP-064-EVENT-NOTES.md`; DECISIONS.md ADR-019,
  ADR-024, ADR-034, ADR-035, ADR-036, ADR-042 … ADR-047 with every dated Update.
- **Partly (disclosed, a deviation from "in full", taken to keep the context usable):** `PROTOCOL.md` — §0–§4.2, §4.5 (all subsections), the §6 entries
  that mention field 12 / field 2 / in-ear / `qht` (grep of lines 1938–3233) and §7; not the rest of §4.3, §4.4, §5, §6. `DECISIONS.md` — for ADR-001 … 018,
  020 … 023, 025 … 033, 037 … 041 only the Decision lines. `TODO.md` — lines 663–892 (Phase 4, Phase 5, Known technical debt, Open questions); not 1–662.
  `APP_TESTPLAN.md` — header, §0, A–C, M, N and the section list. `ai-sessions/INDEX.md` — the last rows. `id_registry.csv` — the ADR-045…047, CAP-056/063/064,
  HOLD-005, INEAR rows. `CAP-056-EVENT-NOTES.md` — not re-read (the FINDINGS tables were used and every frame was re-derived from the log).
- **Kotlin read in full before any change:** `SettingFrame.kt`, `Maestro.kt`, `BudsSettings.kt`, `BudsRepository.kt`, `BudsRepositoryImpl.kt` (all 1238 lines),
  `CodecRouter.kt`, `SafeModeGate.kt`, `FakeBudsTransport.kt`, `SettingsFixtures.kt`, `SettingsCodecTest.kt`, `ControlsScreen.kt`, `SettingsUi.kt`.
  **Partly:** `BudsRepositoryImplTest.kt` (lines 60–470 and 995–1130 — the helpers, EQ/settings reads and ADR-045 writes; the rest by outline), `OpenControlNavHost.kt`
  (actions, state, the Sound/Controls routes), `MainActivity.kt` (the settings action lines), `PwRpcTest.kt` (the readable-fields test), `BudsError.kt`,
  `ConnectionScreen.kt` (`userMessage`). There is no ViewModel: the Controls tab is fed by `MainActivity` → `OpenControlNavHost` (ARCHITECTURE.md §2).
- **APK (for the "Digital assistant" question):** `hgj.java` lines 1–240, `hgr.java`, `hcy.java` 30–60, `ExpressiveHoldCustomizationFragment.java` (grep).

## 2. Phase 0 — baseline

- `cd android && ./gradlew --offline assembleDebug testDebugUnitTest test lint` → **BUILD SUCCESSFUL** (dependency cache complete, `--offline` kept; mostly
  up to date). Then `./gradlew --offline clean` + the same gate → **BUILD SUCCESSFUL** (to see the compiler warnings).
- JUnit XML: `:data` **1546**, `:hardware` **51**, `:domain` **23** tests; 0 failures / errors / skipped.
- Lint: `:data`, `:hardware`, `:ui` "No issues found"; `:app` 0 errors, 1 warning (`DataExtractionRules`, pre-existing).
- Kotlin warnings (clean build): the 3 pre-existing — `BudsCompanionPairing.kt:263`, `:293` (delicate API), `LinkEvaluation.kt:84` (opt-in).

## 3. Phase A — fixtures re-derived from the captures (rule 4a)

Commands (the capture is the authority):

```
cd captures/CAP-056-2026-09-28_17-30-53_17-35-58-Group_AR
tshark -r CAP-056-btsnoop_hci.log -Y 'bthci_acl.chandle==0x0005 && btrfcomm.len>0 && frame.number in {1489,1502,1529,1531,1689,1696,1697,1725,…,4370}' \
       -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e data.data
python3 ../../scripts/pwrpc_decode.py CAP-056-btsnoop_hci.log
python3 <scratchpad>/crccheck.py <tsv>        # split on 7e, HDLC-unescape, zlib.crc32(body) little-endian == last 4 bytes
```

**Correction to the prompt:** `bluetooth.addr==04:00:6e:cf:6e:07` matches **0** packets in this log (the prompt said it works "but check"); the
pre-filter used is `bthci_acl.chandle==0x0005` (2552 packets), as `CAP-056-FINDINGS.md` does.

| Frame | Time (+02:00) | Dir | Decode (`pwrpc_decode.py`) | Raw (RFCOMM payload) | CRC |
|---|---|---|---|---|---|
| 1529 | 17:31:17.163 | → | REQUEST ch 19 `ReadSetting 4:12` | `7e003b0310131dea71de7d5e2551aed0ae2a02200cb2f23e307e` | OK |
| 1531 | 17:31:17.224 | ← | RESPONSE ch 19 `4:{12:{1:1 2:1 3:1 4:1}}` | `7e80a3032a0c220a62080801100118012001080110131dea71de7d5e2551aed0aebe26a1e07e` | OK |
| 1689 | 17:31:30.889 | → | `WriteSetting 4:{12:{1:0 2:1 3:1 4:1}}` ch 19 | `7e003b0310131dea71de7d5e251d9a8c9e2a0c220a62080800100118012001f81ce2ad7e` | OK |
| 1696 | 17:31:31.425 | ← | SERVER_STREAM `SubscribeToSettingsChanges 4:{12:{1:0 2:1 3:1 4:1}}` | `7e80a3032a0c220a62080800100118012001080710131dea71de7d5e25f5ad21281fbcad167e` | OK |
| 1697 | 17:31:31.530 | ← | RESPONSE `WriteSetting`, empty, status OK | `7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e` | OK |
| 1725 | 17:31:36.773 | → | `4:{12:{1:1 2:1 3:1 4:1}}` | `7e003b0310131dea71de7d5e251d9a8c9e2a0c220a620808011001180120014c17950b7e` | OK |
| 1786 | 17:31:42.598 | → | `4:{12:{1:1 2:0 3:1 4:1}}` | `7e003b0310131dea71de7d5e251d9a8c9e2a0c220a62080801100018012001fc3ef5367e` | OK |
| 1815 | 17:31:54.306 | → | `4:{12:{1:1 2:1 3:1 4:0}}` (Adaptive unticked) | `7e003b0310131dea71de7d5e251d9a8c9e2a0c220a62080801100118012000da27927c7e` | OK |
| 1843 | 17:32:05.982 | → | `4:{12:{1:1 2:1 3:0 4:1}}` (Transparency unticked) | `7e003b0310131dea71de7d5e251d9a8c9e2a0c220a620808011001180020017b7d5d570a7e` | OK |
| 1489 / 1502 | 17:31:16.249 / .524 | →/← | `ReadSetting 4:2` / RESPONSE `4:{2:0}` | `…2551aed0ae2a022002b5df86d77e` / `7e80a3032a0422021000080110131dea71de7d5e2551aed0ae9095ad297e` | OK |
| 2173 / 2179 | 17:33:35.629 / .653 | →/← | `4:{2:1}` ch 19 / empty OK | `7e003b0310131dea71de7d5e251d9a8c9e2a0422021001067a3a8b7e` / (= 1697) | OK |
| 4048 / 4056 | 17:35:33.419 / .504 | →/← | `4:{2:0}` ch 19 / empty OK | `7e003b0310131dea71de7d5e251d9a8c9e2a0422021000904a3dfc7e` | OK |
| 2849 / 2855 | 17:34:25.565 / .609 | →/← | `4:{2:0}` ch 21 / empty OK | `7e004b0310151dea71de7d5e251d9a8c9e2a0422021000a0a6cb947e` / `7e00a503080110151dea71de7d5e251d9a8c9e036d4ed87e` | OK |
| 3627 | 17:35:18.806 | → | `4:{2:1}` ch 21 | `7e004b0310151dea71de7d5e251d9a8c9e2a04220210013696cce37e` | OK |
| 4344 / 4360 / 4361 / 4362 / 4369 / 4370 | 17:35:52.190 … .528 | | `4:{2:1}`; phone `CLIENT_ERROR CANCELLED`; the same write again; push + OK; OK; phone `CLIENT_ERROR FAILED_PRECONDITION` | 4360 `7e004b03080410151dea71de7d5e251d9a8c9e30012100bc927e`, 4370 `…30091388679c7e` | OK |

Byte comparisons (other2.tsv): `CAP-056` 1689 = `CAP-021` 5237, 1725 = 5247, 1786 = 5255, 2173 = `CAP-024` 1912, 4048 = `CAP-024` 1850 — **all identical**, CRCs OK.

**Correction to the prompt (the capture wins): channel 21 for field 12 has real bytes.** `for f in captures/*/*btsnoop_hci*.log; do python3
scripts/pwrpc_decode.py $f | grep -E '4:\{?12[:\}]|\| 4:12$'; done` → 40 field-12 writes in the captures, 8 of them on channel 21, all in **`CAP-041`** (Group AH, Pixel 7a,
official app, 2026-09-06), each answered by an empty OK and mirrored:

| `CAP-041` frame | Time | Decode | Raw | CRC |
|---|---|---|---|---|
| 2176 → 2181 | 17:14:22.831 | `4:{12:{1:1 2:1 3:0 4:1}}` ch 21 → OK | `7e004b0310151dea71de7d5e251d9a8c9e2a0c220a62080801100118002001ed2413217e` | OK |
| 2192 → 2195 | 17:14:24.391 | `4:{12:{1:1 2:1 3:1 4:0}}` ch 21 → OK | `7e004b0310151dea71de7d5e251d9a8c9e2a0c220a620808011001180120004c7d5ed6577e` | OK |
| 2198 | 17:14:24.840 | `4:{12:{1:1 2:0 3:1 4:0}}` (two selected) | `7e004b0310151dea71de7d5e251d9a8c9e2a0c220a62080801100018012000fc57b66a7e` | OK |
| 2255 | 17:14:37.906 | `4:{12:{1:0 2:1 3:0 4:1}}` (two selected) | `7e004b0310151dea71de7d5e251d9a8c9e2a0c220a62080800100118002001592f64877e` | OK |

And a real channel-21 **read** of field 12 with a mode unticked: `CAP-036` 1514 `ReadSetting 4:12` (`7e004b0310151dea71de7d5e2551aed0ae2a02200c08b2acdb7e`) →
1516 `4:{12:{1:1 2:0 3:1 4:1}}` (`7e00a5032a0c220a62080801100018012001080110151dea71de7d5e2551aed0ae115784487e`), both CRC OK. So the prompt's "supplementary
structural byte array" for channel 21 is **not needed**: the channel-21 tests use these real frames.

**External check (protobuf wire format, fetched 2026-09-28):** protobuf.dev/programming-guides/encoding — *"The "tag" of a record is encoded as a varint formed from
the field number and the wire type via the formula `(field_number << 3) | wire_type`."*; bool = wire type 0 (VARINT); *"Submessage fields also use the `LEN` wire
type."* protobuf.dev/programming-guides/field_presence — *"Explicitly set values are always serialized, including default values."* / (implicit presence) *"Default
values are not serialized."* This matches `qht`'s has-bits (`qht.java:31`, field `b`) and the wire: a `false` is sent as `08 00`, never omitted — so the decoder may
require all four booleans present (every real sample has them).

## 4. Phase A — current behaviour (file:line) and planned change, per item

- **Codec today.** `SettingValue` (`SettingFrame.kt:30-48`) models Flag/Balance/PressAndHold; KDoc line 28 "Field 12 has no representation on purpose" (its bit order was
  🔴 until ADR-046). `WRITABLE_FLAG_FIELDS` = {4, 19, 22} (`:67`); `flagRequest` returns `null` for any other field (`:97-100`); `decode` returns `null` for unknown
  fields (`:115-135`). `Maestro.READABLE_FIELDS` = {2, 4, 7, 16, 17, 18, 19, 22} (`Maestro.kt:53-60`); `readSettingRequest` returns `null` otherwise (`:67-76`).
- **Connect-time reads.** `launchInitialEqRead` (`BudsRepositoryImpl.kt:1094-1101`): `readEq` → `readSettings` → `subscribeRuntimeInfo`, one coroutine, all under `eqMutex`.
  `readSettings` (`:1107-1136`): one pass over `SETTING_READ_ORDER` (`:1200-1207`, 2, 4, 7, 17, 19, 22), each ≤ `SETTING_READ_TIMEOUT_MS` = 2 s (`:1210`), no retry; a
  failed read keeps "not read" and records the last reason. Settings reset at every Connect/re-open (`openSession`, `:597`).
- **Writes (ADR-045).** `writeSetting` (`:858-886`): not `Ready` ⇒ `ConnectionLost` at once, nothing sent (`:859`); `eqMutex`; `awaitMaestroChannel` (≤ 3 s for the
  announcement, `:810-816`); `writeGate(requireModelIdOfClaim = false)` (`:865`, ADR-042); **one** `transport.send` (`:869-871`); applied only on an OK `RESPONSE`
  (`:875-880`, `changedByApp = true`), else the previous value stays and `settingsError` = `SettingsFailure(error, write = true)` (`:888-892`).
- **Double write / CLIENT_ERROR.** The app never builds a `CLIENT_ERROR`: `PwRpc.TYPE_CLIENT_ERROR` appears only in `PwRpc.kt:65/171` (name) and `CodecRouter.kt:274`
  (decoding). One `transport.send` per `writeSetting` call; no retry loop anywhere in it. So the official app's 4344/4360/4361/4370 pattern cannot occur.
- **A write while the session is being re-opened** (CAP-056: Buds `DISC` 4168 at 17:35:48.467, the official app's re-open 4304 at 17:35:51.800): in OpenControl
  the state is not `Ready` during the re-open ⇒ `ConnectionLost` ("The setting was not changed: Connection lost."), nothing sent, no queue; if the tap lands after
  `Ready` but before the new announcement, `awaitMaestroChannel` waits ≤ 3 s, then sends once. Kept as is (no queue, no retry).
- **Pushes.** The app never sends `SubscribeToSettingsChanges`; in `CAP-063` (OpenControl only) the Buds sent **0** such pushes (`pwrpc_decode.py … | grep -c
  SubscribeToSettingsChanges` → 0). The router would decode one (`CodecRouter.kt:256-265`) and the repository would apply it as a Buds report — unchanged.
- **UI today.** `ControlsScreen.kt:69-77`: touch controls switch, "Press and hold" + note + Left/Right chips, then the in-ear detection text "…: on — read-only; not
  whether a bud is worn" (`:110-117`) with its time. Times: `settingTime` "read HH:MM:SS" / "changed HH:MM:SS" (`SettingsUi.kt:47-51`).

**Planned change.**
- `:domain`: `AncModeList(noiseCancellation, off, transparency, adaptive)` with `selectedCount`, `with(mode, selected)`, `MIN_SELECTED = 2`; `BudsSettings.ancModeList`;
  a new `BudsError.AncModeListTooShort` (nothing sent); `BudsRepository.setAncModeSelected(mode, selected)` and `setInEarDetection(on)`.
- `:data`: `SettingValue.AncModes(list)`; `SettingsCodec.ancModeListRequest(channel, list)` = `4:{12:{1:b 2:b 3:b 4:b}}`, `null` if fewer than two; `decode` of field 12
  (exactly fields 1–4 once each, 0/1); `WRITABLE_FLAG_FIELDS` + 2; `READABLE_FIELDS` + 12; `SETTING_READ_ORDER` 2, 4, 7, **12**, 17, 19, 22 (the official sweep order,
  `CAP-036` 1457 → 1514 → 1526). The repository write needs the list read at Connect (all four must be sent; without a read it refuses with a reason).
- `:ui`: the list and the switch as chosen at the checkpoint.

## 5. Phase A — new `transport.send` call sites (prompt task 5)

**None.** The field-12 read goes through the existing `readSettings` send (`:1122`); both writes through the existing `writeSetting` send (`:869-871`). New
*message contents* only: `ReadSetting 4:12` (ADR-046, amends ADR-036), `WriteSetting 4:{12:{…}}` (ADR-046), `WriteSetting 4:{2:v}` (ADR-047).

## 6. Phase A — further improvements possible now (prompt task 6)

Sources gone through: `0055` §9, `CAP-056-FINDINGS.md` §11, `0053` §6 (via `TODO.md`/`0054`), `0054` §14, `TODO.md` 663–892, `ARCHITECTURE.md` §5a/§3.1, `APP_TESTPLAN.md`, the code.

| # | Problem (evidence) | Change | Layer / files | Wire | Class | Risk / test / effort |
|---|---|---|---|---|---|---|
| U-1 | A settings switch not read yet shows **off** and is tappable: `SettingSwitchRow` uses `reading?.value ?: false` and `enabled` (`SettingsUi.kt:101-105`) — a tap then writes "on" blind, and the screen shows "off" that the Buds never said (AGENTS.md §5 honest state) | disable a switch (and the new list) while its value is not read; the time line already says "Not read from the Buds yet" | `:ui` `SettingsUi.kt` | none | **A** | low; no Compose tests → APP_TESTPLAN step; S |
| U-2 | A tap during an automatic re-open says only "Connection lost." (`writeSetting` `:859` → `ConnectionScreen.kt:319`), though the app is reopening (ADR-044; `CAP-056` 4168→4304 3.3 s) | word it "The app's channel is being reopened — try again in a moment." when the state is Connecting/Discovering | `:data` (a distinct error or the state at refusal), `:ui` text | none | **A** | low; repository test; S |
| U-3 | Stale documentation: `TODO.md:855` "ADR-036 read-only settings UI … nothing implemented" (built in `0052`); `ARCHITECTURE.md` §3.1 settings row and codec-scope paragraph "field 12 is never read or written"; §5a "12 gated"; `PROJECT.md:49` "In-ear detection status" (the *setting* becomes writable; the live status stays unbuilt) | rewrite those lines | docs | none | **A** | none; S — done with Phase G regardless |
| U-4 | The ANC screen's "ANC can only be changed while you wear the Buds…" (I-1) relies on Settable `00`; with in-ear detection off that is 🔴 (`CAP-056-FINDINGS.md` §4) | none now — the F2-NOTE covers it; decide after `CAP-064` AZ(b) | — | — | **C** (evidence: AZ(b) "ANC Refresh with no bud worn, OHD off") | — |
| U-5 | The ANC tile cycles NC → Transparent → Adaptive → Off regardless of the press-and-hold list (`AncMode.nextInTileCycle`) | optionally cycle only the modes ticked in the list | `:domain` `AncMode`, `:app` tile | none | **B** (a product choice: the list is the Buds' *press-and-hold* loop, not the tile's; would need a maintainer decision, no ADR since no wire change) — not recommended | M |
| U-6 | A change of the list/in-ear setting by another device or the official app is not seen until the next Connect (no subscription) | `SubscribeToSettingsChanges` | `:data` | new request | **B** — needs its own ADR (ADR-034/036/045 exclude it). Draft: "ADR-0XX — send one `SubscribeToSettingsChanges` per Connect (the official app's `CAP-056` 1451 shape) and apply its pushes as Buds reports." | M; not recommended now |
| U-7 | Debug tab raw-settings view (ADR-036 "non-FACT fields raw in Debug"): no such view exists; field 12 now decodes to a value, so nothing to add | — | — | — | **D** (nothing to do) | — |
| U-8 | W-12a "worn" indicator, I-6 L/R % from the stream | — | — | — | **C** (AY-3 / evidence first) | — |
| U-9 | Settings reads on every ADR-044 re-open: 7 reads instead of 6 (one more ≤ 2 s) | none — within ADR-036 ("sequential, ≤ 3 s, never retried") | — | — | — | noted only |

**"Digital assistant" handling in the official app (checked in code, as the prompt asked):** each bud's "Customize left/right" screen
(`ExpressiveHoldCustomizationFragment.java:74`, title `:78`) holds a segmented button with two segments, "Active noise control" (`anc_preference_tag`) and "Digital
assistant" (`assistant_preference_tag`) (`hcy.java:42-44`); `hgr.java:40-56` shows only the selected segment's fragment, and the ANC checklist is `hgj`
(`R.xml.anc_preference`, `hgj.java` `aE`). So the list is visible only under "Active noise control". Whether selecting a segment is itself the field-7 write was not
traced (⚪).

## 7. Phase B — checkpoint answers (this chat, 2026-09-28, `AskUserQuestion`; recorded verbatim)

- **"F12 list"** → **"Hidden unless Noise control"** — preview chosen: *"Press and hold / Left: [Noise control] [Digital assistant] / Right: [Digital assistant]... /
  Modes for press and hold (both buds) / (shown only if Left or Right = Noise control) / [x] Noise cancellation / [x] Off / [x] Adaptive / [x] Transparency"*.
  Applied: title "Modes for press and hold (both buds)", the four labels in the official on-screen order (Noise cancellation, Off, Adaptive, Transparency — mapped to
  booleans 1, 2, 4, 3), shown only while the Left **or** Right press-and-hold reads Noise control (a not-read action hides it — nothing claimed), with its time.
- **"Min 2"** → **"Box disabled + line (Recommended)"**: the last two ticked boxes are disabled and *"At least two modes must stay selected."* is shown.
- **"F2 note"** → **"Always, full text (Recommended)"**: switch "In-ear detection", subtitle *"Pauses audio when you take a bud out and resumes it when you put it
  back."* and always *"With it off, audio does not pause when you take a bud out, and the 'only while worn' check for changing noise control may not apply."*, then
  the time.
- **"Extras"** (multi-select) → **"U-1 not-read = disabled, U-2 reopen wording"**. Not chosen: U-5, U-6. U-3 (stale docs) is done as part of Phase G.

## 8. What was built, per item

| Item | What | Where | Tests (real bytes) |
|---|---|---|---|
| **F12-R** | `ReadSetting 4:12` in the Connect sequence (between 7 and 17); `SettingValue.AncModes`, decoder accepts exactly booleans 1–4, each 0/1 once | `Maestro.kt` (`READABLE_FIELDS`), `SettingFrame.kt` (`decodeAncModes`), `BudsRepositoryImpl.kt` (`SETTING_READ_ORDER`, `applySetting`), `BudsSettings.kt` (`AncModeList`, `ancModeList`) | request = `CAP-056` 1529 (ch 19) / `CAP-036` 1514 (ch 21); 1531 → all four; 1516 → Off unticked; push 1696 decoded; Connect sequence now 7 reads (updated tests); 5 labelled supplementary shape tests; fuzz extended |
| **F12-W** | `setAncModeSelected(mode, selected)`: builds the new list from the Buds' last report **inside** the Maestro lock; unread ⇒ `AncModeListNotRead`, < 2 ⇒ `AncModeListTooShort` (nothing sent); else one `WriteSetting` with all four booleans through `writeSetting` (gate, one send, applied on OK only) | `BudsRepository.kt`, `BudsRepositoryImpl.kt` (`writeSetting(prepare, request)`), `SettingFrame.kt` (`ancModeListRequest` → `null` below two), `BudsError.kt` | ch 19: the whole `CAP-056` tap sequence 1689, 1725, 1786, 1725, 1815, 1725, 1843 byte for byte, OK 1697; bit order (1815/1843); ch 21: `CAP-041` 2192, 2176, 2198 (real, no hand-built frame), OK 2855; refusal below two; unread refusal; Timeout / error status keep the list; Safe Mode sends nothing |
| **F12-UI** | "Modes for press and hold (both buds)", four `Checkbox`es in the official on-screen order, locked boxes, the "at least two" line, the time; shown only while a bud reads Noise control | `ControlsScreen.kt`, `OpenControlNavHost.kt`, `MainActivity.kt` | no Compose tests in this project → `APP_TESTPLAN.md` N1, N7–N9; `CAP-064` AZ-1…AZ-5, AZ-8 |
| **F2-W** | field 2 in `WRITABLE_FLAG_FIELDS`; `setInEarDetection(on)` | `SettingFrame.kt`, `BudsRepositoryImpl.kt`, `BudsRepository.kt` | `CAP-056` 2173/4048 (ch 19) and 3627/2849 (ch 21) byte for byte; read 1502 → off; OK 1697/2855 apply with the time; Timeout keeps; Safe Mode |
| **F2-NOTE** | the switch with the chosen subtitle and the always-visible note | `ControlsScreen.kt` | APP_TESTPLAN N1, N10, N11; `CAP-064` AZ-6 |
| **U-1** | `SettingSwitchRow` disabled while its value is not read (mono, conversation detection, touch controls, in-ear detection); the list's boxes likewise | `SettingsUi.kt`, `ControlsScreen.kt` | APP_TESTPLAN N1 |
| **U-2** | `writeSetting` in `Connecting`/`Discovering` ⇒ `BudsError.SessionOpening` ("The app's channel is being reopened — try again in a moment."), nothing sent or queued | `BudsRepositoryImpl.kt`, `BudsError.kt`, `ConnectionScreen.kt` | repository test (Connecting → refused; after Ready nothing goes out later); APP_TESTPLAN C12 |

Also: `AncModeListTest` (`:domain`, the mode ↔ boolean mapping and the lock rule). Not changed: the balance slider still shows its knob at the centre while unread
(its text says "Not read from the Buds yet"; U-1 as chosen covers switches and the list only). The `AncModeListNotRead` text ("The list of modes has not been read
from the Buds on this connection, so nothing was sent.") was not in the checkpoint — it is only reachable if the UI's own "not read ⇒ disabled" is bypassed.

## 9. Gate (Phase F, task 12)

`./gradlew --offline clean`, then `./gradlew --offline assembleDebug testDebugUnitTest test lint` → **BUILD SUCCESSFUL** (1 min 44 s).

| After | `:data` | `:hardware` | `:domain` | Lint | Kotlin warnings |
|---|---|---|---|---|---|
| baseline (`a30fc1b`) | 1546 | 51 | 23 | `:app` 0 errors / 1 warning (`DataExtractionRules`); others "No issues found" | the 3 pre-existing (`BudsCompanionPairing.kt:263`, `:293`, `LinkEvaluation.kt:84`) |
| **final, after clean** | **1564** | **51** | **25** | `:ui` first reported a new `ModifierParameter` warning (the two new `ControlsScreen` callbacks had defaults after `modifier`) — fixed by making them required; `:ui:lintDebug` then "No issues found"; `:app` unchanged | the same 3, none new |

No new suppression (`@Suppress`/`tools:ignore` grep over the changed files: none). Tests changed because they encoded the old rule (field 12 never readable): `PwRpcTest`
readable set, `SettingsCodecTest` read-request and flag-field tests, and three repository Connect-sequence tests (7 reads, 9 requests; the "no 12" assertion is now
"12 read once").

## 10. Mutation checks (task 13; mutate.py: apply, run `:data:testDebugUnitTest` + `:domain:test`, restore, compare SHA-256)

| # | Mutation | Failing tests | Restored identical |
|---|---|---|---|
| M1 | booleans 3 and 4 swapped in the field-12 encoder | 7 | yes |
| M2 | the same swap in the decoder | 1 | yes |
| M3 | the "at least two" check removed (encoder and repository) | 2 | yes |
| M4 | only the changed (false) boolean sent instead of all four | 7 | yes |
| M5 | field 2 written with the inverted value | 2 | yes |
| M6 | the write applied before its `RESPONSE` | 4 | yes |
| M7 | U-2 removed (a tap during the re-open reports "Connection lost") | 1 | yes |

## 11. Compliance (task 14)

- No `INTERNET`, no new permission; `git diff --name-only -- '*.gradle.kts' '*.toml' '*AndroidManifest.xml'` → empty.
- `transport.send(` call sites in `BudsRepositoryImpl.kt`: 8, unchanged; the diff adds or removes none. New request *contents* only: `ReadSetting 4:12` (ADR-046),
  `WriteSetting 4:{12:{…}}` (ADR-046), `WriteSetting 4:{2:0|1}` (ADR-047) — each through the existing read/write paths.
- Nothing on DLCI 0x08; the codec gained only field 12 (read + write) and field 2 (write).
- Every settings write passes `writeGate` (ADR-042) — the one call in `writeSetting` covers the two new writes (Safe-Mode tests for both).
- AGPL-3.0 headers on the two new Kotlin files (`Cap056Fixtures.kt`, `AncModeListTest.kt`). No new log line; no MAC address logged.
- No `PROTOCOL.md` or `DECISIONS.md` change (none was needed or approved in this chat).

## 12. Group AZ re-test skeleton (task 15)

`captures/CAP-064-…-Group_AZ/CAP-064-EVENT-NOTES.md` gained **section VII (AZ-1 … AZ-8)**: AZ-1 the list read at Connect (`ReadSetting 4:12`); AZ-2 untick Adaptive →
`4:{12:{1:1 2:1 3:1 4:0}}` → OK; AZ-3 three long presses on film with an ANC Refresh after each → `Notify` modes only `08`/`20`/`80`; AZ-4 down to two, the third untick
refused with nothing on the wire; AZ-5 restore; AZ-6a–d in-ear detection off via OpenControl → `4:{2:0}` OK (+ SASS `… b0 00` if DLCI 0x04 is open), a bud out → no pause,
both buds on the table + ANC Refresh → the Settable byte with it off (settles the 🔴), on again → `4:{2:1}` + `… b8 00`; AZ-7 read-back after Connect; AZ-8 the list
hidden while both buds are Digital assistant. Its own "Refuted if" lines; the A.5 don't, Z1 restore, A.7 checklist, Test-ID list and the global "Refuted if" updated;
the header names the `0056` build. `CAP-064` stays a Pixel 9a/GrapheneOS session; the Pixel 7a "open Customize right while a mode is unticked on the left" check is a
**separately marked note** for a later capture with its own ID. `id_registry.csv`'s `CAP-064` row says so.

## 13. Documentation (task 16)

`ARCHITECTURE.md` (§2.4 Controls tab; §3.1 settings row and the codec-scope paragraph; §5a settings row), `APP_TESTPLAN.md` (header note; C12; N1 reworded; N7–N11;
summary C 12, N 11), `PROJECT.md` (touch-controls line; in-ear detection line), `TODO.md` (the 0055 "next FEATURE" item done; the stale "ADR-036 … nothing
implemented" line corrected — U-3), `CHANGELOG.md`, `README.md` (status block), `ai-sessions/INDEX.md` (0056 row), `id_registry.csv` (`CAP-064`),
`CAP-064-EVENT-NOTES.md`. `python3 scripts/ensure_footers.py` → "all footers already up to date"; `python3 scripts/lint_docs.py` → **exit 0** (a first run flagged
scratchpad file names written in backticks in this file — fixed; the historical dead-reference list it prints is informational).

## 14. External sources (fetched 2026-09-28)

- protobuf.dev/programming-guides/encoding — *"The "tag" of a record is encoded as a varint formed from the field number and the wire type via the formula
  `(field_number << 3) | wire_type`."*; bool uses wire type 0 (VARINT); *"Submessage fields also use the `LEN` wire type."*
- protobuf.dev/programming-guides/field_presence — *"Explicitly set values are always serialized, including default values."*; for implicit presence *"Default
  values are not serialized."* (why a `false` appears as `08 00` in `qht`, and why the decoder may require all four fields).
- The SASS and help-page quotes behind the F2 note are `ai-sessions/0055` §7's (not re-fetched).

## 15. Open items

- Nothing of this session is hardware-verified: `CAP-064` section VII. In particular whether a long press follows the list (AZ-3) and the Settable byte with in-ear
  detection off and no bud worn (AZ-6c) — the latter may require rewording the ANC "only while worn" line (U-4, class C).
- One list or two on the Buds (🟡) — the Pixel 7a note after section VII.
- Not built by choice: U-5 (tile follows the list), U-6 (`SubscribeToSettingsChanges`, needs an ADR).
- Compose texts are not unit-testable in this project (no Compose tests).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0056_FEATURE_RESULT_2026_09_28.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0056_FEATURE_RESULT_2026_09_28
