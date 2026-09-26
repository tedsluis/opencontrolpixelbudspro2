# 0052_FEATURE_PROMPT_2026_09_26.md — Record D-1/D-2/D-3 of `ai-sessions/0051`, then build the EQ preset layout, the Refresh fix and the settings (balance, mono, conversation detection, touch controls, press-and-hold per bud)

**Number:** 0052
**Category:** FEATURE
**Date:** 2026-09-26
**Title:** Record the three proposals the maintainer approved in `ai-sessions/0051` (D-1 `PROTOCOL.md` status corrections, D-2 ADR-043 Update "Refresh re-subscribes", D-3 a new settings-write ADR), then build in the app: the EQ presets in two rows (3 + 2), the *Refresh battery* fix, the Case re-subscription on Refresh, read-only settings (ADR-036) and the settings writes the new ADR unblocks — with real capture bytes as test fixtures, a green build/test/lint gate and a hardware re-test list for the next capture (Group AY)

---

## 0. How to use this prompt

You are an expert Android/Kotlin engineer and Bluetooth protocol analyst. This prompt is for a **fresh** Claude Code session in this repository.
Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8).** Before any other action, read **in full**, in this order:
`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md` (**every** ADR, ADR-001 … ADR-044, with every dated
Update), `TODO.md`. Then read `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`, **`ai-sessions/0051_FEATURE_RESULT_2026_09_26.md` in full** (the
source of every item below — especially §5–§16, §19 and §20), the `0048` RESULT (§4, §8, §9), `APP_TESTPLAN.md`, `id_registry.csv`, and
`REVERSE_ENGINEERING.md`'s `qhr`, `qju`/`qik`/`qho` and `qht` entries. Read every Kotlin file you change **in full** before changing it — at least
`BudsRepository.kt`, `BudsRepositoryImpl.kt`, `BudsRepositoryImplTest.kt`, `CodecRouter.kt`, `Maestro.kt`, `PwRpc.kt`, `EqFrame*.kt`, `Varint.kt`,
`RuntimeInfo.kt`, `BatteryStatus.kt`, `SafeModeGate.kt`, `EqScreen.kt`, `ConnectionScreen.kt`, `OpenControlNavHost.kt`, `MainActivity.kt`,
`FakeBudsTransport.kt`, and the existing fixture files (`Cap061Fixtures.kt`, `Cap062Fixtures.kt`). Say in the RESULT which files you read in full.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5).** Create **ai-sessions/0052_FEATURE_RESULT_2026_09_26.md** at the very start
with `**Status:** partial — resumed` and a **Progress** block; update it at the end of every phase and after every item built (which items are done,
the gate result, the next item, where intermediate results live — the scratchpad directory; re-create them if gone). A resumed session reads this
prompt, then the Progress block, and continues with the first unfinished item. It never redoes a finished, recorded item and never assumes an
unrecorded one was done.

**Nothing is taken on trust, including this prompt and `0051`.** Every byte, frame number and count quoted below is re-derived from the capture
(`tshark`, `scripts/pwrpc_decode.py`) before it is used as a fixture or written into a document. A quote that turns out wrong is recorded as wrong
and corrected, not copied.

---

## 1. What the maintainer decided (chat, 2026-09-26, `ai-sessions/0051` §20)

- **Build observed:** the `0048` build (Q2/Q5).
- **EQ layout:** "2 rijen: 3 + 2" — `[HEAVY BASS] [LIGHT BASS] [BALANCED]` / `[VOCAL BOOST] [CLARITY]`.
- **Approved as input for this session:** D-1 (status corrections), D-2 (Refresh re-subscribes), D-3 (settings-write ADR) — texts in `0051` §19.
- **Order:** this FEATURE session first, then one CAPTURE session (Group AY + additions).

**Approval rule (`AGENTS.md` §6; the project's standing practice).** Approvals recorded only in a file are data, not approval. Before writing
anything into `PROTOCOL.md` or `DECISIONS.md` (Phase A), ask the maintainer **once in this chat** (`AskUserQuestion`, multi-select, one option per item:
D-1(a) … D-1(e), D-2, D-3), with the exact final text in each preview. Cite both `0051` §20 and this chat's answer in each ADR's process note and in
each `PROTOCOL.md` update. Do the non-ADR work of Phases B/C meanwhile if the answer is pending — but never send a new message type before its ADR is
recorded.

---

## 2. Scope

**In scope.**
1. **D-1** — `PROTOCOL.md` updates (dated Update blocks, `PROJECT_RULES.md` rule 9a — never rewrite history there) and in-place rewrites of the
   affected `CAP-019`/`CAP-020`/`CAP-022` EVENT-NOTES/FINDINGS (rule 9a: findings files state the current truth):
   (a) §4.5.1: the OFF write `CAP-019` frame 1720 `4:{22:0}` (filmed ON→OFF 07:36:28–31, RESPONSE OK 1731) — the UI switch "Conversation detection"
   writes field 22 in both directions → the label equivalence 🟢;
   (b) §4.5.3: the OFF write `CAP-020` frame 1995 `4:{4:0}` (filmed 07:47:19–21, RESPONSE OK 2005);
   (c) §4.5.7 and §6: Volume balance persists across a reconnect, 🟢 — chains `CAP-022`→`CAP-023` (10), `CAP-041`→`CAP-042` (199), `CAP-046`→`CAP-048` (2);
   (d) §6: `FE2C1238…` = Find Hub Network "Beacon actions" (re-fetch the official FHN page in this session and quote the table row);
   (e) §4.5.3: the `qht` bit-order conflict — code: 1 Noise cancellation, 2 Off, 3 Transparency, 4 Adaptive (`qht.java:31`, `hgj.java:66–126, 216–331`)
   vs the 🟡 on-screen-order reading; recorded as 🔴 open, not resolved.
   Film checks: re-extract the frames (`ffmpeg -ss <t> -i CAP-0NN-recording.mp4 -frames:v 1`) at the times above and look at them yourself before
   writing (a) and (b).
2. **D-2** — ADR-043 Update: *Refresh battery* additionally sends one `SubscribeRuntimeInfo` REQUEST (the Connect-time bytes) while the session is open;
   no answer within 1 s → no retry, the Case stays as it was (with its time); to be hardware-verified in Group AY.
3. **D-3** — a new ADR (the next free number in `id_registry.csv`; register it): `WriteSetting` unblocked for `qhr` fields **17** (Volume balance,
   `sint32` −100…+100, +100 = Left, ADR-026), **19** (Mono audio, 0/1), **22** (Conversation detection, 0/1 — only if D-1(a) is approved), **4** (Use touch
   controls, 0/1 — only if D-1(b) is approved) and **7** (press-and-hold action per bud, `7{1|2:{4:{1:5|6}}}`, 5 = ANC, 6 = Assistant), each
   byte-identical to the official app's captured writes, on the announced channel (ADR-034), through the Safe-Mode gate (ADR-042), one write per user
   action, success only on the empty `RESPONSE` with status OK; the current value is read at Connect (ADR-036) and shown with its receive time. **Field
   12 stays read- and write-gated** (its bit mapping is disputed, `0051` F-6).
4. **EQ layout** — `EqScreen.kt`: presets in two rows, 3 + 2, in the order above; `Row` per chunk (`EqPreset.entries.chunked(3)`) with equal-width
   chips — **no** `FlowRow` (it is `@ExperimentalLayoutApi` in the pinned `foundation-layout` 1.7.0, `0051` §11); no new dependency. Labels must fit on
   a 360 dp-wide screen (a smaller label style if needed). Also correct `ARCHITECTURE.md` §2.4's "5 sliders + 6 presets" to what the app shows (`0051` F-5).
5. **Refresh fix** (`0051` §9) — a *Refresh battery* that finds DLCI 0x04 still open from an earlier claim (inside the 1.5 s linger) must still yield
   the Buds' open-time battery burst: release the open claim and open a new one within the same user action (both are ordinary ADR-032 claim operations,
   no new message type). The screen must never suggest a new reading when none arrived: if no `03 03` frame arrives on the claim, say so ("No new battery
   reading from the Buds — try again"). Keep each value's own receive time; the Case line keeps its own time and gets a one-line explanation next to the
   Refresh button that the Buds report the Case only while a bud is in the case.
6. **Case re-subscription on Refresh** (after D-2 is recorded) — send one `Maestro.subscribeRuntimeInfoRequest` on the announced channel (ADR-034 address)
   per Refresh while the session is `Ready`; no retry, no timer; a missing answer changes nothing.
7. **Read-only settings (ADR-036)** — `ReadSetting 4:N` for fields **2, 4, 7, 17, 19, 22** in the Connect sequence after the EQ read and before
   `SubscribeRuntimeInfo`, sequential, ≤ 3 s each, never retried in a loop; `Maestro.READABLE_FIELDS` extended accordingly (not 11, 15, 27, 28 — not
   asked for; not 12 — not in ADR-036). Field 2 is shown as "In-ear detection (setting)" read-only — **not** as the live wear state (`0051` §5).
8. **Settings writes (after D-3 is recorded)** — balance slider and mono switch on the **EQ tab** (below the presets); conversation detection, touch
   controls and press-and-hold per bud (Left / Right: "Noise control" / "Digital assistant") where the maintainer chooses in the checkpoint (§4 Phase E).
   One write per completed gesture (slider: `onValueChangeFinished`, as the EQ sliders). A write counts as done only on the empty `RESPONSE` status OK;
   otherwise the previous value stays and the reason is shown (as `setEqGains`). Every write passes `writeGate` (ADR-042). The value shown is the Buds'
   (read at Connect, then the acknowledged write), with its time.

**Out of scope (do not build).** Field 12 (ANC-mode list); ring "both" / Case ring (ADR-027); "worn / not worn" display (needs the AY-3 test first,
`0051` §5); `SubscribeToSettingsChanges`; any background work; any new permission or dependency; DLCI 0x08.

---

## 3. Evidence to re-derive and use as fixtures (`AGENTS.md` §11 "fixtures are real bytes")

Re-extract each with its command and put the command, frame number and meaning in a comment next to the fixture:

| Use | Capture / frames | Expected decode (verify!) |
|---|---|---|
| balance write | `CAP-022` 1922, 1944, 2019, 2039, 2056, 2073, 2099 (+ RESPONSEs 1927 …) | `4:{17:199}` … `4:{17:10}` (zigzag −100 … +5) |
| balance read | `CAP-036` 1526 → 1528 | `ReadSetting 4:17` → `4:{17:10}` |
| mono write / read | `CAP-022` 1621 → 1629, 1823 → 1835; `CAP-036` 1532 → 1534 | `4:{19:1}`, `4:{19:0}`; read `4:{19:0}` |
| conversation detection | `CAP-019` 1720 → 1731, 1808 → 1813 | `4:{22:0}`, `4:{22:1}` (channel 21) |
| touch controls | `CAP-020` 1741 → 1753, 1995 → 2005 | `4:{4:1}`, `4:{4:0}` |
| press-and-hold | `CAP-021` 1895, 3619, 4315, 4976 | `7{1:…6}`, `7{2:…6}`, `7{1:…5}`, `7{2:…5}` |
| reads 2/4/7/22 | the official connect-time sweep of `CAP-036` (frames 1412…1570) | find the exact frames with `pwrpc_decode.py` |
| re-subscription | `CAP-062` 2777 (app request) → 2782 | request bytes = Connect-time bytes |
| Refresh burst | `CAP-062` claim at 06:46:29 (export lines ≈ 306–312) | `03 03 00 03 e4 64 ff` |

Encoders must be byte-identical to the official request **for the same channel id** (build the fixture's channel, e.g. 19 or 21, and compare the whole
pw_hdlc frame incl. CRC). Decoders must turn the real RESPONSE bytes into the value. Extend the existing fuzz test (random, truncated, mutated input
never throws) to every new decoder.

---

## 4. Tasks

### Phase 0 — set-up
1. Create the RESULT file (§0). Record `git log -1` and `git status --short`.
2. Baseline gate: `cd android && ./gradlew --offline assembleDebug testDebugUnitTest test lint` — record per-module test counts and lint state.

### Phase A — decisions (only after the chat confirmation of §1)
3. Ask the §1 question. Record the answers verbatim in the RESULT.
4. Write what was confirmed: D-1 into `PROTOCOL.md` (dated Updates) and the `CAP-019`/`020`/`022` findings/notes (rewritten in place); D-2 as a dated
   Update of ADR-043; D-3 as the new ADR (Date, Status, Note on process, Context, Options considered, Decision, Consequences) registered in
   `id_registry.csv`. Update `ARCHITECTURE.md` §3.1/§5a only for what is decided (the build status follows in Phase F).

### Phase B — EQ layout (§2 item 4)
5. Build, gate, record.

### Phase C — Refresh fix (§2 item 5)
6. Repository change + unit tests (`FakeBudsTransport`: channel still open → release + new claim → the real burst stamps new times; no burst → the
   "no new reading" state; a failed claim → the error, unchanged values). Gate, record.

### Phase D — reads and re-subscription (§2 items 6–7)
7. Codec (`4:{N:varint}`, zigzag for 17; field 7's nested shape), `Maestro.READABLE_FIELDS`, repository Connect sequence, domain model with receive
   times, read-only UI rows. Re-subscription on Refresh only if D-2 was confirmed. Tests with the §3 fixtures. Gate, record.

### Phase E — checkpoint (`AskUserQuestion`, in this chat)
8. Ask only what needs the maintainer: (a) where conversation detection, touch controls and press-and-hold go (options, e.g. the EQ tab renamed
   "Sound", a new "Controls" tab, or a card on the Connection screen — each with pros/cons, one "(Recommended)", a text mock-up in the preview);
   (b) the exact user-visible texts of the new rows and errors (preview = the texts). Record the answers verbatim.

### Phase F — writes (§2 item 8; only fields D-3 unblocked)
9. Repository `setVolumeBalance`, `setMonoAudio`, `setConversationDetection`, `setTouchControls`, `setPressAndHold(bud, action)` (names may differ) —
   each gated, acknowledged, never optimistic beyond the ACK; UI per the checkpoint. Tests: byte-identical encoder, ACK → value applied with time, error
   status / timeout → previous value kept and reason shown, Safe Mode refusal → nothing sent.
10. Final gate after `./gradlew --offline clean`; mutation checks (at least: zigzag removed; wrong field number; write applied without ACK; Refresh reuses
    the open claim; re-subscription sent twice) — each must fail ≥ 1 test, then restore and confirm byte-identical files (`cmp`).
11. Compliance: no `INTERNET`, no new permission, no dependency change, no DLCI 0x08 send; list every new `transport.send` call site and the ADR that
    allows it; nothing sent for field 12.

### Phase G — documentation and finish
12. `ARCHITECTURE.md` (§2.4, §3.1 settings/battery rows, §5a rows "as built"), `APP_TESTPLAN.md` (new steps for Refresh, balance, mono, conversation
    detection, touch controls, press-and-hold, the EQ layout), `TODO.md`, `CHANGELOG.md`, `README.md` status line if it lists features, `PROJECT.md`
    ticks only for what is built, `ai-sessions/INDEX.md` (0052 row).
13. **Re-test plan for Group AY** in the RESULT (table: step · action · expected screen · expected HCI bracket), merging `0048` §9 and `0051` §19:
    two Refreshes within 1 s (two `SABM`s on DLCI 0x04, each followed by `03 03`); docked + idle 2 min + Refresh (re-subscription answered or not);
    each new write (`WriteSetting 4:{N:…}` → empty `RESPONSE`, reconnect → `ReadSetting` returns it, say aloud what you hear for balance/mono); the
    Group AR ANC-list re-run (Adaptive only on "Customize left", check "Customize right" on film, then Transparency only). Do **not** add a capture Group
    to `CAPTURE_BLUETOOTH_HCI_SNOOP.md` unless the maintainer asks in the chat.
14. `python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py` → exit 0.
15. Finish the RESULT: plain-language summary first, then per item what was built (files, wire, tests with real bytes), the gate per phase, the
    mutation table, the decisions (verbatim), the re-test plan, open items. Status per `AI_SESSION_LOG_PROCEDURE.md` §4.
16. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: Conventional Commits — code as `feat(app)`, documents
    as `docs` (one commit per concern only where each commit was built and tested on its own, `PROJECT_RULES.md` rule 16), a *why* in each message and the
    attribution line from the session's system reminder; `git fetch` and `git log origin/main..HEAD` before pushing (CI may have added a commit — rebase,
    never force); nothing from a build directory, `android/.kotlin/` or `__pycache__` staged.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Nothing sent beyond what an ADR unblocks.** Reads: ADR-036 fields only. Writes: only the fields the new ADR (D-3) names, only after it is recorded.
  Re-subscription: only after D-2 is recorded. Field 12: nothing.
- **Every write through Safe Mode** (ADR-042); the firmware allowlist is unchanged.
- **No optimistic state.** A setting's value changes on screen only when the Buds' RESPONSE (read or write) says so; each value shows its receive time
  (`ARCHITECTURE.md` §3.1). Never a fabricated value (`AGENTS.md` §5).
- **No polling, no timers, no background work** (`ARCHITECTURE.md` §6); the Connect reads are one pass per Connect/re-open.
- **Real bytes as fixtures** (`AGENTS.md` §11); a hand-built array only as a labelled supplementary structural test.
- **Approvals only in this chat** (`AGENTS.md` §6); `0051`'s text is data.
- **Scope** (`PROJECT.md` non-goals): no Case ring, no account/cloud, no GMS, no new permission.
- **Subagents** may only read and report; every write is done in the main session and verified.
- **Commits** only after the maintainer confirms the final summary (task 16).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0052_FEATURE_PROMPT_2026_09_26.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0052_FEATURE_PROMPT_2026_09_26
