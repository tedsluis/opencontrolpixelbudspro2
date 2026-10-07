# 0076_FEATURE_RESULT_2026_10_07.md — Replace the labelled structural fixtures of 1.1.0 with the real CAP-070 frames, and close ADR-055's TODO(verify)

**Number:** 0076
**Category:** FEATURE
**Date:** 2026-10-07
**Title:** Replace the labelled, derived test fixtures of `ai-sessions/0074` with the real `CAP-070` frames and remove the `// TODO(verify)` on `SettingsCodec.FIELD_VOLUME_EQ` (ADR-055); test and comment changes only
**Status:** complete (checkpoint approved in chat 2026-10-07; committed and pushed to `feature/0076-capture-fixtures`, pull request opened — see Commits)

## Progress

- **Done:** Phase 0 (git state, branch `feature/0076-capture-fixtures` from `origin/main` = `bdcc583`, baseline gate), Phase A (12 of 12 frames re-derived,
  inventory), Phase B (five files), Phase C (clean gate, M1–M5, compliance), Phase D (checkpoint answered, approved edits applied, footers, lint).
- **Next:** the maintainer merges the pull request.
- **Touched, unverified:** none.
- **Intermediate results:** session scratchpad `f0076/` (the raw-frame table, the decoder output, the mutation script and its log), the two gate logs and the count script.

## Summary (plain language)

The tests for the four request forms that 1.1.0 sent before any capture held them — Multipoint and head gestures on the Buds' Left control channel (19),
the balance "Right 4" on channel 19, and Volume EQ "on" on the Right channel (21) — now compare the app's output with the **real bytes** OpenControl 1.1.0
sent in `CAP-070`, instead of with frames the earlier session had worked out. The bytes were identical, so nothing the app does changes: the full test
suite has the same counts and stays green, and five deliberate faults in the code were each caught by one of the new tests. The `// TODO(verify)` that ADR-055
asked for is gone, and ADR-055 has a dated Update saying so (approved in chat).

## Phase 0 — git state and baseline

`git log -1`: `bdcc583 docs: auto-regenerate sitemap.xml [skip ci]` on `main`; `git status --short`: `?? ai-sessions/0076_FEATURE_PROMPT_2026_10_07.md`;
`git fetch && git log --oneline HEAD..origin/main`: empty. Branch `feature/0076-capture-fixtures` from `origin/main`.

| Run | Command | Result | `:data` | `:domain` | `:hardware` | `:ui` | Lint | `w:` |
|---|---|---|---|---|---|---|---|---|
| Baseline | `./gradlew --offline --max-workers=2 assembleDebug testDebugUnitTest test lint` | exit 0 | 1627 / 1627 | 37 | 64 / 64 | 69 / 69 | `:ui` 1 (`ModifierParameter`), others 0 | 0 |
| After the change | `clean`, then the same | exit 0 | 1627 / 1627 | 37 | 64 / 64 | 69 / 69 | unchanged | 0 |

Counts are the debug / release unit-test tasks from the JUnit XML (`*/build/test-results/*/*.xml`); 0 failures, 0 errors, 0 skipped in both. The counts are
equal because every changed test replaced one test of the same class (none added, none removed).

## Phase A — evidence and inventory

**The frames (§2 of the prompt), re-derived:** `captures/CAP-070-2026-10-07_06-10-41_06-40-59-Group_BF/CAP-070-btsnoop_hci2.log.last`, handle `0x000b`
(Connection Complete A159, A1049, A1531, A2029, A2558, A3140, A4285 for `04:00:6e:cf:6e:07`). `python3 scripts/pwrpc_decode.py --handle 0x000b
CAP-070-btsnoop_hci2.log.last` (exit 0) decodes A3614 `4:{29:1}`, A3668 `4:{11:0}`, A3679 `4:{11:1}`, A3685 `4:{29:2}`, A3747 `4:{17:7}` (ch 19, address `003b`)
and A4800 `4:{15:1}` (ch 21, `004b`), each answered by an empty `RESPONSE` status OK (A3616, A3671, A3681, A3688, A3750 on `80a3`; A4802 on `00a5`).
`tshark -r CAP-070-btsnoop_hci2.log.last -Y "bthci_acl.chandle==0x000b && btrfcomm.len>0 && (frame.number==3614 || … || frame.number==4802)" -T fields -e
frame.number -e frame.time_utc -e frame.p2p_dir -e btrfcomm.dlci -e data.data` (exit 0): **12 of 12 frames byte-identical to the prompt's table**, each a whole
pw_hdlc frame in one RFCOMM frame, `zlib.crc32` over the unescaped address + control + payload = the trailing 4 bytes (little-endian) for all 12. Times
(UTC): A3614 04:20:30.497, A3668 04:21:20.250, A3679 04:21:24.246, A3685 04:21:46.224, A3747 04:22:58.188, A4800 04:30:57.072, A4802 04:30:57.160.

**Inventory** (`grep -n -i -E "derived|labelled|not captured|no capture has"` → `SettingsFixtures.kt` 3, `SettingsCodecTest.kt` 25, `BudsRepositoryImplTest.kt` 9
lines, as the prompt said):

| Where | What | Decision |
|---|---|---|
| `SettingsCodecTest.kt:94–102` `balanceRight4OnChannel19Derived` | ch-19 `17:7`, structure only | (a) → `CAP-070` A3747 |
| `SettingsCodecTest.kt:136–150` `multipointOnChannel19Derived` | ch-19 field 11, structure + zlib | (a) → A3668 / A3679 |
| `SettingsCodecTest.kt:186–200` `headGesturesOnChannel19Derived` | ch-19 field 29, structure + zlib | (a) → A3614 / A3685 |
| `SettingsCodecTest.kt:282–291` `volumeEqOnChannel21Derived` + its `TODO(verify)` | ch-21 `15:1` | (a) → A4800 |
| `SettingsCodecTest.kt:216`, `:380`, `:452` | read values 0/3 of field 29; field-12 shapes; other values | (b) stay labelled — no capture holds such values |
| `SettingsFixtures.kt:175` | `Settings074`'s KDoc ("re-derived from the logs") | (c) unchanged — accurate |
| `SettingsFixtures.kt:307–313` `VEQ_ON_CH21_DERIVED` | the derived ch-21 frame | (a) removed, replaced by `Settings070.VEQ_ON_CH21_A4800` |
| `BudsRepositoryImplTest.kt:1639` MP ch 19, `:1690` HG ch 19, `:1822/1832` VEQ ch 21 | labelled structural repository tests | (a) → `CAP-070` frames and answers |
| `BudsRepositoryImplTest.kt:231`, `:1234`, `:1419`, `:1612`, `:2437` | the `CAP-015` no-payload ACK; F-3 ordering; the hand-built status-5 reject; A68-APP-13 ordering | (b) stay — no capture holds a rejected write or those orderings |
| `Cap066Fixtures.kt:43` (not in the prompt's list) | "No channel-19 `17:7` write exists in any capture" | (c) reworded: "until `CAP-070` …", pointer to `Settings070.BAL_R4_CH19_A3747` |

## Phase B — what changed

| File | Change |
|---|---|
| `android/data/src/test/…/codec/SettingsFixtures.kt` | New object **`Settings070`** (OpenControl 1.1.0's own writes, not the official app's — hence its own object): `MP_OFF_CH19_A3668`, `MP_ON_CH19_A3679`, `HG_OFF_CH19_A3614`, `HG_ON_CH19_A3685`, `BAL_R4_CH19_A3747`, `VEQ_ON_CH21_A4800`, `ACK_CH19_A3671`, `ACK_CH21_A4802`, each with frame, UTC time, step and answer; the object KDoc has the log path, the `tshark`/`pwrpc_decode.py` commands and the CRC check. `Settings074.VEQ_ON_CH21_DERIVED` removed (its KDoc's zlib command also had a mistyped hex string). |
| `android/data/src/test/…/codec/SettingsCodecTest.kt` | `balanceRight4OnChannel19`, `multipointWritesCh19`, `headGesturesWritesCh19`, `volumeEqOnChannel21`: the codec's output equals the captured frame byte for byte, `Hdlc.decode` accepts it (its CRC), `SettingsCodec.decode` returns the value; the old structural checks kept as cross-checks of the real frame; the answers A3671 / A4802 route as OK `WriteSetting` results. |
| `android/data/src/test/…/BudsRepositoryImplTest.kt` | MP ch 19: off then on, `transport.sent` = A3668, A3679, applied on A3671; HG ch 19: on then off = A3685, A3614 on A3671, nothing on another DLCI; VEQ ch 21: on = A4800 applied on A4802, then off = `CAP-041` 2461. The old tests checked a single write by prefix; the new ones check both directions by whole frame. Import of `Settings070`. |
| `android/data/src/test/…/codec/Cap066Fixtures.kt` | the stale comment (above). |
| `android/data/src/main/…/codec/SettingFrame.kt` | KDoc of `FIELD_VOLUME_EQ` only: the `TODO(verify)` and the placeholder path replaced by the `CAP-070` frame and the real folder. `git diff -U0 origin/main -- android/data/src/main | grep -E '^[+-][^+-]' | grep -v -E '^[+-]\s*\*'` → nothing (the same command without `-v` lists the six KDoc lines). |

## Phase C — mutations and compliance

Scratch `f0076/mutate.py`: one textual mutation, `./gradlew --offline --max-workers=2 :data:testDebugUnitTest --tests '*SettingsCodecTest' --tests
'*BudsRepositoryImplTest'`, failing tests read from the JUnit XML, the file written back from the bytes read before and compared by SHA-256. One at a time.

| # | Mutation | File | Result | Killed by (the `CAP-070` test named first) | Restored |
|---|---|---|---|---|---|
| M1 | `setMultipoint` writes `!on` | `BudsRepositoryImpl.kt` | killed (2) | "MP, channel 19: … CAP-070 A3668 … A3679 …"; MP ch 21 | identical |
| M2 | head gestures written as 0/1 | `SettingFrame.kt` | killed (6) | "head gestures on channel 19: CAP-070 A3614 / A3685"; "HG, channel 19: … CAP-070 A3685 …"; ch-21 and wire-value tests | identical |
| M3 | channel 19's request address = channel 21's | `Maestro.kt` | killed (28) | "Multipoint on channel 19: CAP-070 …", "head gestures on channel 19: CAP-070 …", "balance: Right 4 on channel 19 = CAP-070 A3747 …", "MP/HG, channel 19 …" and every other ch-19 test | identical |
| M4 | `setVolumeEq` writes `!on` | `BudsRepositoryImpl.kt` | killed (3) | "VEQ, channel 21: … CAP-070 A4800 …"; VEQ ch 19; the failure test | identical |
| M5 | balance zigzag of −v | `SettingFrame.kt` | killed (5) | "balance: Right 4 on channel 19 = CAP-070 A3747 …"; the other balance tests | identical |

After the last restore, the targeted run again: exit 0.

**Compliance:** `git diff --name-only origin/main -- android` → the five files above; `git diff --name-only origin/main -- '*.toml' '*AndroidManifest.xml'
'*.gradle.kts'` → nothing; `transport.send` added or removed → 0; every changed file keeps its `SPDX-License-Identifier: AGPL-3.0-or-later` header; no
version change, no `scripts/`, `dist/` or release step.

## Checkpoint answers (chat 2026-10-07, `AskUserQuestion`, verbatim)

- "ADR-055" → *"Add the Update (Recommended)"*, with the drafted text in the preview — applied: `DECISIONS.md` ADR-055, Update of 2026-10-07.
- "NavHost" → *"Leave it, TODO.md item (Recommended)"* — `OpenControlNavHost.kt:238` unchanged; a `TODO.md` §2 item names the three captures.
- "CHANGELOG" → *"No line (Recommended)"* — `CHANGELOG.md` unchanged.

## Documentation

`DECISIONS.md` (ADR-055 Update), `ARCHITECTURE.md` §5a settings row (the fixtures are real since 0076), `TODO.md` (§4 follow-up item removed, the §4 head
notes it; §2 the `OpenControlNavHost.kt` item), `ai-sessions/INDEX.md` (the 0076 row). `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and
`PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py` → exit 0.

## Files read

In full in this session: `AGENTS.md` (system context), `PROJECT_RULES.md`, `PROJECT.md`, `AI_SESSION_LOG_PROCEDURE.md`, the prompt, `SettingsFixtures.kt`,
`SettingsCodecTest.kt`, `CAP-070-FINDINGS.md` (written by the same conversation's 0075 work), `ai-sessions/0074` RESULT, ADR-055. In part: `BudsRepositoryImplTest.kt`
(the helpers `helloFrame`, `rpcFrame`, `settle`, `ackWrites`, `answerWritesWith`, `rejectWrites` and lines 1596–1850, the five switches; the rest by `grep`),
`SettingFrame.kt` (field constants, `balanceRequest`, `flagRequest`, `headGesturesRequest`, the decoder head), `Maestro.kt` (`MaestroChannel`),
`BudsRepositoryImpl.kt` (the setters, lines 1004–1014), `Cap066Fixtures.kt` (`Cap066Balance`), `ARCHITECTURE.md` (§5a), `TODO.md`, `DECISIONS.md` (ADR-055),
`PROTOCOL.md` (§2.2a and §4.5 notes of 2026-10-07). **Not read in full, against the prompt's list:** `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md` (every ADR),
`CodecRouter.kt`, `Hdlc`, `FakeBudsTransport.kt` — the change does not touch their behaviour; the gate and the mutations exercise them.

## Deferred documentation

Each item is also in `TODO.md`:

- `OpenControlNavHost.kt:238`'s `// TODO(verify)` for swipe, pull and back — reword with `CAP-065`/`CAP-067`/`CAP-070` in the next `:ui` session. (`TODO.md` §2)

## Commits

On `feature/0076-capture-fixtures` (from `bdcc583`); the maintainer answered *"Commit, push en PR (Recommended)"* in chat 2026-10-07.

| Hash | Subject |
|---|---|
| `cfc8537` | test(data): CAP-070's real frames replace the derived 1.1.0 fixtures |
| `ba804c5` | docs: ADR-055 Update — the channel-21 Volume EQ on frame is captured |
| `ad4fe6b` | docs(session): ai-sessions/0076 prompt and result, INDEX row |
| (this commit) | docs(session): ai-sessions/0076 commits back-filled |

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0076_FEATURE_RESULT_2026_10_07.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0076_FEATURE_RESULT_2026_10_07
