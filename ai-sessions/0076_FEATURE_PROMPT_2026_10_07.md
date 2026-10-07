# 0076_FEATURE_PROMPT_2026_10_07.md — Replace the labelled structural fixtures of 1.1.0 with the real CAP-070 frames, and close ADR-055's TODO(verify)

**Number:** 0076
**Category:** FEATURE
**Date:** 2026-10-07
**Title:** Replace the labelled, derived test fixtures of `ai-sessions/0074` (`Settings074`, the "supplementary structural" tests) with the real frames
`CAP-070` recorded — Multipoint and head gestures on channel 19, Volume EQ "on" on channel 21, balance "Right 4" on channel 19 — and remove the
`// TODO(verify)` on `SettingsCodec.FIELD_VOLUME_EQ` (ADR-055); test and comment changes only, no change in what the app sends

---

## 0. How to use this prompt

You are an expert Android/Kotlin engineer and technical auditor working in this repository. Run the phases of §4 **strictly in order**; record each
phase before starting the next.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action read **in full**, in this order:
`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md` (every ADR with its Updates — in particular ADR-034,
ADR-036, ADR-042, ADR-045 with its Update, ADR-052, ADR-053, ADR-054, ADR-055), `TODO.md`. Where a file is too large to hold at once, read it section by
section and say in the RESULT what was read in full and what in part. Then, per task:

- `ai-sessions/0074_FEATURE_RESULT_2026_10_06.md` (§A.1 the fixtures and their derivation, §C–G, §H the gate and mutation method) and
  `ai-sessions/0075_CAPTURE_RESULT_2026_10_07.md` (plain-language answers, "Deferred documentation").
- `captures/CAP-070-2026-10-07_06-10-41_06-40-59-Group_BF/CAP-070-FINDINGS.md` §0 (the build, the logs), §3 (every write against its expected frame) and §10;
  `CAP-070-EVENT-NOTES.md` (the frame prefixes "Z", "A", "B" and the clock offsets).
- `PROTOCOL.md` §2.2a (the channel and request address), §4.5.2, §4.5.4, §4.5.6, §4.5.7 including the dated notes of 2026-10-07; `ARCHITECTURE.md` §5a's
  settings row; `APP_TESTPLAN.md` section T.
- The Kotlin files this session touches or that the tests exercise, **each in full before it is changed**:
  `android/data/src/main/kotlin/io/github/tedsluis/opencontrolpixelbuds/data/codec/SettingFrame.kt`,
  `android/data/src/test/kotlin/io/github/tedsluis/opencontrolpixelbuds/data/codec/SettingsFixtures.kt`,
  `android/data/src/test/kotlin/io/github/tedsluis/opencontrolpixelbuds/data/codec/SettingsCodecTest.kt`,
  `android/data/src/test/kotlin/io/github/tedsluis/opencontrolpixelbuds/data/BudsRepositoryImplTest.kt` (at least the helpers and every test that uses
  `Settings074`, `Cap066Balance` or a "labelled"/"derived" fixture), and the files they import for those tests (`Maestro.kt`, `CodecRouter.kt`, `Hdlc`,
  `FakeBudsTransport.kt`).

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5).** Create **ai-sessions/0076_FEATURE_RESULT_2026_10_07.md** at the very start
with `**Status:** partial — resumed` and a **Progress** block; update it at the end of every phase and after every substantial step (each fixture added,
each test changed, each gate run): what is done, what is next, which files are touched but unverified, and where intermediate results live (the session
scratchpad). A resumed session reads this prompt, then the Progress block, re-reads the files the unfinished step touched, and continues; it never redoes a
recorded step and never assumes an unrecorded step was done.

**Memory and the machine.** The laptop has 15 GB. Run Gradle with `--offline --max-workers=2` (the dependency cache is present); never two Gradle builds at
once; mutation checks one at a time; after a crash look for a leftover mutation (`git diff -- android`) before anything else. Never `git worktree add` a
commit (it checks out `captures/`, gigabytes of LFS). Never delete with a wildcard (`rm -f *`); use a fresh `mktemp -d` directory for temporary output.

**Nothing is taken on trust**, including this prompt, `CAP-070-FINDINGS.md` and earlier RESULTs: every byte below is re-derived from the HCI log.

---

## 1. What the maintainer asked for (chat, 2026-10-07, translated from Dutch)

After the 1.1.0 release (`ai-sessions/0075`), the follow-up chosen in that session's checkpoint (*"TODO for a FEATURE session after the release
(Recommended)"*, `TODO.md` §4):

1. Replace the labelled structural fixtures (`Settings074`, the "supplementary structural (derived, not captured)" tests in `SettingsCodecTest.kt` and the
   "labelled supplementary structural" tests in `BudsRepositoryImplTest.kt`) with the real `CAP-070` frames, each with frame number, time and command in its
   comment (`AGENTS.md` §11 "Fixtures are real bytes").
2. Remove the `// TODO(verify)` on `SettingsCodec.FIELD_VOLUME_EQ` (ADR-055) — its KDoc also names the old placeholder folder of `CAP-070`.
3. Nothing the app sends or shows changes; no new feature; no release.

---

## 2. The evidence (re-derive every value)

Log: `captures/CAP-070-2026-10-07_06-10-41_06-40-59-Group_BF/CAP-070-btsnoop_hci2.log.last` ("A"); the Buds' ACL handle is `0x000b` (Connection Complete
A159, A1049, A1531, A2029, A2558, A3140, A4285 — `tshark -r <log> -Y "bthci_evt.code==0x03" -T fields -e frame.number -e bthci_evt.bd_addr -e
bthci_evt.connection_handle`). Times: phone (local) = UTC + 2 h; the existing fixtures of `Settings074` quote UTC — follow that.

| Item | Frame · time (local / UTC) | Raw pw_hdlc frame (as measured by `ai-sessions/0075`) | Decoded | Answer |
|---|---|---|---|---|
| Multipoint off, ch 19 | A3668 · 06:21:20.250 / 04:21:20.250 | `7e003b0310131dea71de7d5e251d9a8c9e2a04220258009d8f9dc47e` | `4:{11:0}` | A3671 |
| Multipoint on, ch 19 | A3679 · 06:21:24.246 / 04:21:24.246 | `7e003b0310131dea71de7d5e251d9a8c9e2a04220258010bbf9ab37e` | `4:{11:1}` | A3681 |
| Head gestures off, ch 19 | A3614 · 06:20:30.497 / 04:20:30.497 | `7e003b0310131dea71de7d5e251d9a8c9e2a052203e80101fcd6da847e` | `4:{29:1}` | A3616 |
| Head gestures on, ch 19 | A3685 · 06:21:46.224 / 04:21:46.224 | `7e003b0310131dea71de7d5e251d9a8c9e2a052203e801024687d31d7e` | `4:{29:2}` | A3688 |
| Balance Right 4, ch 19 | A3747 · 06:22:58.188 / 04:22:58.188 | `7e003b0310131dea71de7d5e251d9a8c9e2a052203880107e9b86e257e` | `4:{17:7}` (zigzag −4) | A3750 |
| Volume EQ on, ch 21 | A4800 · 06:30:57.072 / 04:30:57.072 | `7e004b0310151dea71de7d5e251d9a8c9e2a04220278019977e84e7e` | `4:{15:1}` | A4802 |
| Empty `RESPONSE`, ch 19 | A3671 (and A3681, A3616, A3688, A3750) | `7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e` | OK | — |
| Empty `RESPONSE`, ch 21 | A4802 · 06:30:57.160 / 04:30:57.160 | `7e00a503080110151dea71de7d5e251d9a8c9e036d4ed87e` | OK | — |

Commands to re-derive (rule 4a): `python3 scripts/pwrpc_decode.py --handle 0x000b CAP-070-btsnoop_hci2.log.last | grep -E "^ *(3614|3616|3668|3671|3679|3681|3685|3688|3747|3750|4800|4802) "`
(exit 0), and for the raw bytes `tshark -r CAP-070-btsnoop_hci2.log.last -Y "bthci_acl.chandle==0x000b && btrfcomm.len>0 && (frame.number==3614 || …)" -T
fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e data.data`, then split on `0x7e`, HDLC-unescape and check `zlib.crc32` over
address + control + payload against the trailing four bytes (little-endian) — the method of `ai-sessions/0074` §A.1. A frame split over two RFCOMM frames
is reassembled first (`pwrpc_decode.py` does this). **Any byte that differs from the table above is a finding: stop and report it before changing a test.**

The same table's expected values are the ones `0074` derived (`Settings074.VEQ_ON_CH21_DERIVED`, the derived ch-19 frames built in
`SettingsCodecTest`/`BudsRepositoryImplTest`); `CAP-070-FINDINGS.md` §3 reports them byte-identical. Earlier unfilmed samples of two of them exist in
`CAP-070-btsnoop_hci1.log.last` ("Z": `4:{11:1}` ch 19 Z2051, `4:{29:2}` ch 19 Z2048) — the filmed "A" frames are preferred.

---

## 3. Rules (binding, in addition to `AGENTS.md`)

- **Evidence.** Every fixture: frame number, log file, time (UTC, as the others), the command, and what it is (`AGENTS.md` §11, `PROJECT_RULES.md` rule 4a).
  Zero creativity with hex (`AGENTS.md` §13.6). A negative needs its command, exit status and a positive control (`AGENTS.md` §13 step 8).
- **No behaviour change.** Under `android/*/src/main` only comments/KDoc may change (the `FIELD_VOLUME_EQ` KDoc). No new `transport.send`, no new field,
  DLCI, method or subscription; `SettingsCodec`'s output must stay byte-identical (the tests prove it). No version change, no `scripts/release.sh`, no tag.
- **Tests are not weakened.** A labelled structural test is replaced by a real-bytes test of at least the same strength (the same assertions, now against
  the captured frame); a structural check worth keeping (e.g. the independent `zlib` cross-check of `0074`) stays, relabelled as a cross-check of a real
  frame. Prove each new assertion bites with a mutation (§4 Phase C).
- **The `AGENTS.md` §6 gate.** No 🟢 promotion and no ADR or ADR Update without the maintainer's approval **in this chat** (memory: "Approvals: confirm in
  chat"). ADR-055's text mentions the uncaptured channel-21 frame; any change to it is a dated Update drafted for the checkpoint, not written before.
- **Scope.** Exactly the items of §1. Anything else found (another `// TODO(verify)`, a stale comment, a test smell) goes to the checkpoint or `TODO.md`,
  not into the diff. Known neighbours to **mention, not fix** unless approved: the `// TODO(verify)` on swipe/pull/back in `OpenControlNavHost.kt:238`
  (swipe filmed in `CAP-070`, pull in `CAP-065`, back in `CAP-067`), the ones in `EqFrame.kt:39` and `CaseBatteryFrame.kt:53`.
- **Files.** Look before overwriting; never delete with a wildcard; no `git worktree add`.
- **Commits.** Only after the maintainer confirms the final summary (Phase E).

---

## 4. Tasks

### Phase 0 — set-up

1. Create the RESULT file (§0). Record `git log -1`, `git branch --show-current`, `git status --short`, `git fetch && git log --oneline HEAD..origin/main`.
   Branch `feature/0076-capture-fixtures` from an up-to-date `origin/main`.
2. **Baseline gate** before any change: `cd android && ./gradlew --offline --max-workers=2 assembleDebug testDebugUnitTest test lint` → record exit code and
   the per-module test counts from the JUnit XML (`*/build/test-results/*/*.xml`), lint findings, and `w:` lines (the `0074` §A.0/§H.1 method).

### Phase A — evidence and inventory (no code change)

3. Re-derive every row of §2 (commands and outputs into the scratchpad; CRC per frame). Report any difference.
4. Inventory, with `file:line`: every fixture and test labelled "derived", "supplementary structural", "labelled", "not captured" or "no capture has" in
   `SettingsFixtures.kt`, `SettingsCodecTest.kt`, `BudsRepositoryImplTest.kt` (a `grep -n -i -E "derived|labelled|not captured|no capture has"` gives 3, 25 and 9
   hits today — re-count), and decide per hit: (a) replaced by a `CAP-070` frame, (b) a different, still-uncaptured item that stays labelled (say which and
   why), or (c) wording only. Include `Cap066Balance`'s comment that "no channel-19 `17:7` write exists in any capture" and `VEQ_ON_CH21_DERIVED`.
5. Read the four test classes' helpers so the new tests use the existing patterns (`wire`, `hex`, `rpcPayload`, `route`, `answerWritesWith`, `settle()`;
   memory: "Repo tests: runCurrent").

### Phase B — the change

6. `SettingsFixtures.kt`: add the `CAP-070` fixtures to `Settings074` (or a new `Settings070` object, if that reads better — say why), named in the existing
   style (e.g. `MP_OFF_CH19_A3668`, `HG_ON_CH19_A3685`, `BAL_R4_CH19_A3747`, `VEQ_ON_CH21_A4800`, `ACK_CH19_A3671`, `ACK_CH21_A4802`), each with its KDoc:
   capture, log file, frame, UTC time, decoded value, its answer frame, and "filmed" (`CAP-070-EVENT-NOTES.md` row). Remove `VEQ_ON_CH21_DERIVED` once nothing
   uses it.
7. `SettingsCodecTest.kt`: each "supplementary structural (derived, not captured)" test for these four items becomes a real-bytes test — the codec's output
   equals the captured frame byte for byte, `Hdlc.decode` succeeds (its CRC), and `SettingsCodec.decode` returns the right `SettingValue`; keep the independent
   `zlib` cross-check as a check of the real frame. Update `@DisplayName`s.
8. `BudsRepositoryImplTest.kt`: the "labelled supplementary structural" channel-19 Multipoint and head-gesture tests and the channel-21 Volume-EQ-on test send
   and are answered with the captured frames (the empty `RESPONSE`s A3671 / A4802 — or the existing `ACK_CH19_1629` / `ACK_CH21_1731` if byte-identical; say
   which); assertions on `transport.sent` compare against the captured frames.
9. `SettingFrame.kt`: rewrite the `FIELD_VOLUME_EQ` KDoc — the channel-21 `4:{15:1}` write is captured (`CAP-070` A4800, folder path in full), the
   `TODO(verify)` goes. Comments only; `git diff -- android/*/src/main` must show no line outside KDoc/comments.

### Phase C — gate, mutations, compliance

10. Full gate (`clean`, then the Phase 0 command); counts must be ≥ baseline (equal is fine; explain any change); 0 failures/errors/skipped; lint unchanged;
    0 `w:` lines.
11. **Mutations** (one at a time, `--max-workers=2`, restore byte-identically and check with `sha256sum`): at least (M1) field 11 on channel 19 written with
    `!on`; (M2) field 29 on channel 19 written as 0/1; (M3) the channel-19 address `00 3b` replaced by channel 21's for these writes; (M4) field 15's value
    inverted on channel 21; (M5) balance zigzag sign flipped. Each must be killed by a test that uses a `CAP-070` frame; name the test.
12. Compliance: `git diff --name-only origin/main -- android` lists only the four files (plus none other); no `*.toml`, manifest or `*.gradle.kts` change; no
    `transport.send` added or removed (`git diff -U0 -- android | grep -E '^[+-]' | grep transport.send` → nothing); AGPL headers intact.

### Phase D — checkpoint (stop and ask, in this chat, `AskUserQuestion`, one question per decision, each option with pros and cons, one "(Recommended)")

13. Ask at least: (a) the drafted **ADR-055 dated Update** (exact text in the preview: the channel-21 "on" frame captured in `CAP-070` A4800, byte-identical,
    the `TODO(verify)` removed; status unchanged) — or no ADR text change; (b) what to do with the neighbouring `// TODO(verify)` on swipe/pull/back
    (`OpenControlNavHost.kt:238`): leave, or reword with the capture pointers (a comment-only change); (c) anything Phase A found that differs from §2 or that
    needs a decision. Record the answers verbatim in the RESULT.
14. Apply only what was approved. Documentation: `ARCHITECTURE.md` §5a's settings row (the labelled tests and the `TODO(verify)` are gone),
    `TODO.md` §4 (the follow-up item removed), `CHANGELOG.md` `[Unreleased]` (a "Changed — tests" line only if the maintainer wants test changes in the
    changelog; ask in 13), `ai-sessions/INDEX.md` (the 0076 row). `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and
    `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py` → exit 0.

### Phase E — finish

15. Finish the RESULT: plain-language summary first (what changed, what did not, the gate and mutation tables, the files read in full / in part), then
    **"Deferred documentation"** (each item also in `TODO.md`) and **"Commits"** (hashes, or "not committed — reason"), per `AI_SESSION_LOG_PROCEDURE.md` §9
    and §4b. Set the Status per §4.
16. Show the maintainer a short summary and **ask whether to commit, push and open a pull request**. Only after a yes: Conventional Commits, one per concern
    (`test(data): …` for the fixtures and tests, `docs: …` for the KDoc-adjacent documents, `docs(session): …` for the prompt/RESULT/INDEX), each with a *why*
    and the attribution line from the session's system reminder; `git fetch` and rebase before pushing, never force; nothing from a build directory,
    `dist/`, `android/.kotlin/`, `.vscode/` or `__pycache__` staged (`scripts/__pycache__/lint_docs.cpython-314.pyc` is tracked — `git checkout` it if it
    changed). Merging is the maintainer's.

---

## 5. Guardrails (summary)

- Test and comment changes only; the app's behaviour and bytes on the wire are unchanged and proven so by the gate.
- Real bytes with their frame, file, time and command; anything derived that stays is labelled and explained.
- No FACT promotion, ADR or ADR Update without the maintainer's approval in this chat.
- Gradle one at a time, `--max-workers=2`; no worktrees; no wildcard deletes.
- Commit and push only after the maintainer confirms.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0076_FEATURE_PROMPT_2026_10_07.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0076_FEATURE_PROMPT_2026_10_07
