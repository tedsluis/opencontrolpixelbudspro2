# 0090_CROSSCHECK_PROMPT_2026_10_14.md — Point-by-point check of the pbpctrl-derived findings list, led by one question: can the app learn a press-and-hold's noise-control change on its own channel (DLCI 0x02) instead of the contested Message Stream?

**Number:** 0090
**Category:** CROSSCHECK
**Date:** 2026-10-14
**Title:** Point-by-point check of the pbpctrl-derived findings list (E-01 … E-23) against the external sources at pinned commits and this repository's own
captures, APK notes and documents — first the noise-control items (E-01, E-02, E-03), then the rest; proposals only where the rules require the maintainer;
no app code, no new bytes to the Buds, no hardware run

---

## 0. How to use this prompt

You are a reverse engineer and technical auditor working in a **fresh** Claude Code session in this repository. Run the phases of §6 **strictly in order**; do
not start a phase before the previous one is done and recorded in the RESULT.

**Number check first.** `0090` is the next free number after `0089` (`ai-sessions/INDEX.md`, checked 2026-10-14). If another session took it meanwhile, use the
next free number and rename this file and its RESULT (`AI_SESSION_LOG_PROCEDURE.md` §3).

**Resumability (`AI_SESSION_LOG_PROCEDURE.md` §5).** Create ai-sessions/0090_CROSSCHECK_RESULT_2026_10_14.md at the very start with
`**Status:** partial — resumed` and a **Progress** block (which E-NN are done, which phase is running). Update it after every item and every phase. A resumed
session reads this prompt, then the Progress block, re-reads what the unfinished item touched, and continues; it never redoes a recorded item and never
assumes an unrecorded one. Never renumber.

**Nothing is taken on trust** — not the two AI lists of §4, not this prompt's summaries, not a FINDINGS sentence. Every frame number, count and file:line you rely
on is re-derived and quoted with its command.

## 1. Background (why this session exists)

- **State of the app.** 1.2.2 was published on 2026-10-14 (tag `v1.2.2` on build commit `bfd7891`, `ai-sessions/0089`; ADR-062 Update 2, ADR-063). Its
  hardware run `CAP-076` (Group BL) was **not** made: the maintainer tested 1.2.2 on the phone without a capture and released it (chat 2026-10-14), because he
  sees a different route for the actual problem.
- **The problem.** A press-and-hold on a bud changes the noise-control mode; the Buds report it with `Notify ANC state` on the Message Stream (DLCI 0x04) to
  **the one client that holds that channel** (`PROTOCOL.md` §4.1 notes of 2026-10-12/13). On a phone with Google Play services, Play's Fast Pair fights the app
  for that channel (ADR-032, ADR-061, ADR-062 and its Updates; `CAP-074`, `CAP-075`); 1.2.2's re-claim chain shows about half of the holds at best.
- **The maintainer's new lead.** Studying [`qzed/pbpctrl`](https://github.com/qzed/pbpctrl) suggests that the mode lives in Maestro setting 13 on DLCI 0x02 — the
  app's own session channel, which no other client contends for — readable with `ReadSetting 4:13`, writable with `WriteSetting 4:{13:n}` and, perhaps,
  pushed by `SubscribeToSettingsChanges`. If that holds, the app could learn every press-and-hold without touching DLCI 0x04, and switch ANC without the
  Message Stream's 16 zero MAC bytes.
- **The list.** On 2026-10-14 two AI models (Claude in a chat session and a second model) read pbpctrl and two derived projects (MagicPodsCore's Pixel Buds
  branch, omarchy-pixelbuds), compared them with this repository and listed what OpenControl should investigate. §4 merges both lists into **E-01 … E-23**, with
  Claude's corrections ("Review note"). **Neither list is evidence** (`WORKSTATION_PREPARATIONS.md`, "Cross-validation between AI models": agreement raises
  confidence, it proves nothing).

**The session's job:** check every item against (a) the external source at the pinned commit, (b) this repository's captures, APK notes and documents; judge
validity and usefulness; apply only what is valid, useful and allowed (§3); put everything else to the maintainer. **The leading question** — answer it first and
answer it plainly in the RESULT's summary: *Is there evidence, in this project's own captures of the official app, that DLCI 0x02 carries the current
noise-control mode (setting 13) and reports a Buds-side change (a press-and-hold) — and what experiment would settle it for OpenControl?* This is a
documentation-and-desk-research session: **no app code, no new writes to the Buds, no hardware run.**

## 2. Reading

**Mandatory, in full, in this order, before any other action** (`AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8): `AGENTS.md`, `PROJECT_RULES.md`,
`PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md` (**every** ADR with every dated Update — ADR-001 … ADR-063; ADR-060 is a draft, not a
decision), `TODO.md`. `PROTOCOL.md` (≈ 420 KB) and `DECISIONS.md` (≈ 270 KB) are large: read them in chunks, but whole. Then `AI_SESSION_LOG_PROCEDURE.md`
(§4b, §9) and `ai-sessions/INDEX.md`.

**Per task, each section in full before it is changed or relied on:**

- `PROTOCOL.md`: §2.2 (Hypothesis B, "pbpctrl-derived"), §2.2a (pw_hdlc, channels and addresses, L-1, the announcement, `GetHardwareInfo`), §4.1 (ANC; the MAC
  risk; the notes of 2026-10-07, 2026-10-12 and 2026-10-13), §4.3 Option F (runtime info), §4.4 (Ring, the 2026-10-03 correction), §4.5.3 (fields 7 and 12),
  §4.5.7 (balance), §4.5.8a (field 21), §6 (the items on setting 13, `SubscribeToSettingsChanges`, `CAP-056`, `CAP-069`, the burst / `SetWallclock` item).
- `DECISIONS.md` in particular: ADR-008, -009, -011, -017, -019, -021, -022, -026, -027, -032, -034, -036, -042, -044, -045, -046, -049, -056, -058, -059, -060
  (draft), -061, -062 (with Update 2), -063.
- `REVERSE_ENGINEERING.md`: the `qhr` register (the field table and its Updates of 2026-10-03 and 2026-10-06), the `fux.java` service table, the 2026-10-06
  Update on device types (`fsf.java`: 4/5 `ALLEGRO`, 6 `LARGO`), the entries `fye`, `fxb`, `goq`, `qje`, `qir`/`qhz`/`qia`.
- `DESKRESEARCH_FINDINGS.md`: the entries of 2026-10-03 (three cross-capture checks; item 3, runtime-info field 2 = wall clock) and 2026-10-06 (the
  cross-capture censuses: the `ReadSetting` sweep, setting 1 `UNKNOWN`, `5:0`).
- `TODO.md` §2, §4 (the `CAP-075` §12 lead (4) — "nothing on 0x02 moved at any of 31 holds"), §5; `ARCHITECTURE.md` §3.2 (timing constants), §5, §6.0b, §8.1,
  §9.0a; `README.md` "Attribution"; `AGENTS.md` §5 and §12; `PROJECT_RULES.md` rules 4a, 9a, 20; `RELEASING.md` §11 item 4.
- The FINDINGS (and, where a frame is cited, the EVENT-NOTES) of every capture a desk check names in §4 — at least `CAP-027`, `CAP-038`, `CAP-045`, `CAP-051`,
  `CAP-056`, `CAP-064`, `CAP-067`, `CAP-069`, `CAP-075`.

The RESULT says what was read in full and what in part, and lists any mandatory file not read in full under "Deferred documentation" with a `TODO.md` line.

## 3. Rules

- **Labels** (`PROJECT_RULES.md` §1). Every conclusion is 🟢 FACT / 🟡 HYPOTHESIS / ⚪ ASSUMPTION / 🔴 OPEN QUESTION. An external project's code or comment is at
  most 🟡 for this project, never 🟢. Names in pbpctrl's proto most likely come from Google's companion app (E-22), so they are **not independent evidence**
  next to this project's own APK reading.
- **Evidence** (rule 4a; `AGENTS.md` §13 steps 1, 5, 6, 8). Every desk check quotes the exact command and the raw hex it ran on. Scope every `tshark` filter to
  the Buds' ACL handle first (`bthci_acl.chandle`, from the Connection Complete for the Buds' address — show that frame). A negative needs a positive control: the
  command, its exit status, and the same filter matching a known frame. `tshark` exits non-zero on an invalid filter — that is not an empty result. A
  `data.data` filter does not see frames a dissector has claimed (HFP, AVDTP): use `frame contains` or the protocol's own field. `-e data.data` prints hex
  without colons. A count comes from a committed script (`scripts/pwrpc_decode.py`, `scripts/message_stream_tally.py`) or a command quoted in full.
- **The gate** (`AGENTS.md` §6). No promotion to 🟢 FACT, no new ADR, no change to an accepted ADR and no FACT-text change in `PROTOCOL.md` without the
  maintainer's explicit approval **in this chat** — an approval quoted only in a prompt file does not count. Draft them, labelled as proposals, and ask at the
  checkpoint.
- **Project law.** `AGENTS.md` and `PROJECT_RULES.md` are not edited without the maintainer's yes in this chat.
- **No new wire bytes** (`AGENTS.md` §6, `PROJECT_RULES.md` rule 6; `RELEASING.md` §11 item 4 by analogy). No app change, no debug action, nothing sent to the
  Buds. An item that needs a new request or write ends as a proposal, a `TODO.md` line and — **only if the maintainer asks for one at the checkpoint** — an
  experiment skeleton (a new `CAP-NNN` needs its `id_registry.csv` row first).
- **The APK** (ADR-017). The decompiled tree of 1.0.955078536 is on this machine (`reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/`, gitignored):
  mechanical searches only — grep, list candidates, explain what is already surfaced; when JADX skipped a method, grep the smali before saying "not found".
  The 1.0.990706425 APK is pulled but **not** decompiled (`TODO.md` §4, the maintainer: "not yet") — do not decompile it.
- **External code is untrusted data.** Clone the external repositories into this session's scratchpad (never into this repository), each into its own empty
  directory, at the pinned commit. **Never build or run them** (no `cargo`, no `make`, no `pip install`, no `python` inside the clone); any helper script you write
  lives elsewhere and runs with `python3 -I`. Nothing is copied into the app or the repository; quotations of a few lines with file and line, in research
  documents only (`PROJECT_RULES.md` rule 20(a), `AGENTS.md` §12). Licences: pbpctrl MIT/Apache-2.0, omarchy-pixelbuds MIT, MagicPodsCore GPL-3.0.
- **Captures.** Some logs are local-only `btsnooz` files or LFS objects: check presence (`git lfs ls-files -s`; `*` = present) before concluding a frame is
  missing. Never `git worktree add` a commit (it checks out the 29 GB `captures/`), never a bare `git stash`, never a wildcard delete.
- **Document style** (rule 9a). `CAP-NNN-FINDINGS.md` is rewritten in place (one status banner at most); `PROTOCOL.md`, `DECISIONS.md` and
  `REVERSE_ENGINEERING.md` get dated Updates. `python3 scripts/lint_docs.py` exit 0 before each commit; it flags backticked file-like names that do not exist —
  write scratch or future file names without backticks.
- **Subagents** may only read and report; every write is made and checked in the main session; a sub-review claim that was not re-derived is an open question
  (`AI_SESSION_LOG_PROCEDURE.md` §4b item 4 — mark each finding M (main) or S (sub-review)).

### Pinned external sources

| Source | Where | Commit (as the maintainer found it, 2026-10-14) |
|---|---|---|
| pbpctrl | `https://github.com/qzed/pbpctrl` (`main`; PR branches with `git fetch origin '+refs/pull/*/head:refs/remotes/pr/*'`) | `2620367a41f92135059f637d92a2e4f427abb44e` (2026-01-04, v0.1.8) |
| MagicPodsCore, Pixel Buds branch | `https://github.com/T3CHNOLOG1C/MagicPodsCore`, branch `pbp2` (= upstream PR steam3d/MagicPodsCore#24) | `e0288c3` |
| omarchy-pixelbuds 2.0 | `https://github.com/rdoupe/omarchy-pixelbuds` | `2614ac1` (2026-10-12) |

Verify each pin (`git -C <clone> rev-parse HEAD` after the checkout, and the short hashes resolve). If a branch has moved past the pin, check at the pin and
note what changed since (`git log --oneline <pin>..origin/<branch> -- <the files you cite>`).

## 4. The list to check (E-01 … E-23)

Per item: **claim** (what the AI lists say), **external source** (where to verify it), **project link** (what in this repository it touches), **check**
(what this session does). "Review note" marks a correction Claude made to the second model's wording. Do not take any claim as given.

### The leading items — the noise-control mode on DLCI 0x02 (do these first, in this order)

**E-02 — Buds-side mode changes via `SubscribeToSettingsChanges` / `ReadSetting 4:13`.**
- **Claim:** MagicPodsCore (`PixelBudsAncWatcher.cpp`) and pbpctrl PR #22 (an unmerged TUI) take the mode from `ReadSetting 4:13` and from the settings
  stream. `TODO.md` §4's "nothing on 0x02 moved at any of 31 holds" (`CAP-075` §12 item 4) is explained by OpenControl never subscribing (ADR-034/036:
  "No `SubscribeToSettingsChanges`") and never reading 13.
- **Project link:** `PROTOCOL.md` §6 (`CAP-056` 🟡: the stream's `4:{13:n}` mirrors the Notify at wear changes; `CAP-069` 🟡 10 of 10).
- **Check (desk):**
  1. Which official-app captures were subscribed (`SubscribeToSettingsChanges` REQUEST in the connect burst, `scripts/pwrpc_decode.py`)?
  2. In those, every Buds-initiated `08 13` Notify on DLCI 0x04 with **no** `08 12` Set before it (a press-and-hold or the Buds' own change) — candidates:
     `CAP-051` (filmed holds; re-derive how many), `CAP-027` (Group N touch gestures, `TOUCH-007`), `CAP-038`, `CAP-069`. **Review note:** the second model cited
     `CAP-045`; that capture did not run Group AJ's procedure as planned — check what it holds before using it.
  3. For each such Notify: is there a `SERVER_STREAM` `4:{13:n}` on DLCI 0x02 within the same second, with the mode mapping of E-01? Tabulate: Notify frame and
     time, mode byte, the stream frame and time, `n`, Δt. Also every `4:{13:n}` stream packet **without** a Notify (and why — wear change, the app's own Set).
  4. In OpenControl's own captures (`CAP-059` … `CAP-075`): confirm 0 `SubscribeToSettingsChanges` requests and 0 `ReadSetting 4:13` (negatives with positive
     controls from an official-app capture).
- **Outcome:** a `DESKRESEARCH_FINDINGS.md` entry with the table. If positive: a **draft** ADR (subscribe on Connect, decode only field 13's value, nothing new
  written) that would supersede parts of ADR-034 item 2 / ADR-036 ("no subscription") — and say what it would make of ADR-061/062/063 (the hold, the
  re-claim, the card) — for the maintainer; plus the hardware experiment that settles it for OpenControl (E-01's outcome).

**E-01 — ANC via `WriteSetting 4:{13:n}` on DLCI 0x02, and `ReadSetting 4:13`.**
- **Claim:** pbpctrl, MagicPodsCore and omarchy-pixelbuds switch ANC only through Maestro setting 13 (1 Off, 2 Active, 3 Aware, 4 Adaptive), never the
  Message Stream; MagicPodsCore says the Pro 2 was "extensively tested", including control after a primary-bud handover. Here it would remove the Play-services
  contention (ADR-032/061/062/063) and the MAC risk (`PROTOCOL.md` §4.1, 16 zero bytes).
- **External source:** pbpctrl `cli/src/main.rs` (`SetSetting::Anc` → `SettingValue::CurrentAncrState`), `libmaestro/examples/maestro_write_settings.rs`, commits
  `ba253a7` (Pro 2 = Class of Device only) and `003a3fc` (Adaptive); MagicPodsCore `src/sdk/pbp/setters/PixelBudsSetAnc.cpp`, `PixelBudsHelper.h`
  (`CurrentAncrStateValueField = 13`); omarchy `bridge/maestro.py` (`S_ANC = 13`).
- **Project link:** ADR-009; `PROTOCOL.md` §6, note of 2026-10-06 (the official app's ANC row writes only setting 13 — `fye.a`; 0 field-13 writes in 80 logs —
  re-derive that count); the `qhr` register's field 13.
- **Check (desk):** every `ReadSetting 4:13` answer in the official-app captures against the DLCI 0x04 mode at that moment (the latest `Get` answer or
  Notify). Predicted mapping: 1 ↔ `0x20` Off, 2 ↔ `0x08` Active, 3 ↔ `0x80` Transparency, 4 ↔ `0x40` Adaptive; and what 13 reads while Settable is `00`.
  Commands: `scripts/pwrpc_decode.py <log>` and `tshark -r <log> -Y "bthci_acl.chandle==<h> && btrfcomm.dlci==4 && btrfcomm.len>0" -T fields -e frame.number
  -e frame.time -e frame.p2p_dir -e data.data`, with positive controls. APK: the type of field 13 in the recovered `qhr` schema and the enum's values (`fye`).
- **Outcome:** a **proposal** for one hardware experiment (no skeleton unless asked): from a debug build, both GrapheneOS users, on film — `ReadSetting 4:13` at
  rest and after a press-and-hold; `WriteSetting 4:{13:1…4}` with its RESPONSE status, the `08 13` Notify on DLCI 0x04 (to whom, if Play holds it), the
  audible change; variants with one bud worn and with Settable `00` (the second model's addition). Say which ADRs a positive result would need (a read: an ADR-036
  amendment; a write: its own ADR like ADR-045; the `PROTOCOL.md` entry first).

**E-03 — Settings 8 / 9 / 10 named `ANC_ACCESSIBILITY_MODE` / `ANCR_STATE_ONE_BUD` / `ANCR_STATE_TWO_BUDS`.**
- **Claim:** pbpctrl names these settings; on the Pixel Buds Pro (gen 1) reading them returns status 2. Hypothesis: the Buds keep one mode for one bud worn and
  one for two, and switch setting 13 on a wear change — which would explain `TODO.md` §4 "the Buds switched ANC Transparent → Active between 08:07:34 and
  08:12:44 without an app command" (`CAP-067` §2).
- **External source:** `libmaestro/proto/maestro_pw.proto` (`AllegroSettingType`, the "returns non-zero status (code: 2)" comments, commit `4ca2adf`);
  `libmaestro/src/service/settings.rs`.
- **Project link:** `REVERSE_ENGINEERING.md` `qhr` register (8 BOOL, 9/10 ENUM `qhs`, no reader or writer; not in the official sweep).
- **Check:**
  - **`CAP-067`:** did a wear change (film, the Settable byte, the runtime-info stream) happen between 08:07:34 and 08:12:44? (`CAP-067` is OpenControl in the
    user without Play — no GSND Code `0x05` there; say which signals exist.)
  - **`CAP-056`:** do the mode changes coincide with one-bud ↔ two-bud transitions?
  - **APK:** grep the decompiled tree for `ANCR_STATE_ONE_BUD`, `ALLEGRO_`, `ANC_ACCESSIBILITY` (literals), and for the readers/writers of `qhr` fields 8–10.
- **Outcome:** the names in the `qhr` register as a dated Update, labelled "pbpctrl naming (most likely from Google's app), not evidence"; the hypothesis as 🟡;
  a read-only `ReadSetting 4:8/9/10` test as a proposal. **Review note:** the second model wrote that this "directly solves two open questions" — it does not; it
  is a hypothesis.

### High priority — other design items

**E-04 — Ring status from the Buds and its ACK (the known issue since 1.0.1).**
- **Claim:** after the ACK the Buds send `04 01 00 01 <mask>` updates (bit 0 Right, bit 1 Left, 0 = stopped) and the phone answers `ff 01 00 02 04 01`;
  "Pixel Buds Pro ignore messages with a timeout" (no duration byte). Fix: hold the DLCI 0x04 claim while ringing, ACK the Buds' message, clear "Ringing" at
  mask 0.
- **External source:** pbpctrl `libgfps/examples/ring.rs`.
- **Project link:** `PROTOCOL.md` §4.4 (the 2026-10-03 correction: `ff 01 00 02 04 01` is already identified as the phone's ACK of the Buds' `04 01 00 01 xx`;
  🟡 "syncing ringing status back"; the `CAP-068`/`CAP-069` notes); README "Known issue since 1.0.1"; `TODO.md` §4 Ring status; `MESSAGE_STREAM_LINGER_MS`.
- **Check (desk):** every `04 01 00 01 xx` from the Buds in the captures with a Ring, with the client that held the channel and what the phone answered.
- **Outcome:** a draft proposal (sending an ACK is a new send → ADR) and an experiment step (a Ring stopped by touching the bud, with the claim held); a
  `TODO.md` line.

**E-05 — "Ring both" = `0x03` locally; the case has no Bluetooth receiver of its own.**
- **External source:** `ring.rs` (`0b11: both`); pbpctrl `README.md` ("the case does not have a Bluetooth receiver itself").
- **Project link:** ADR-027 and its 2026-09-24 Update (the Device Action spec's `0x03` = both, already quoted there), `PROTOCOL.md` §4.4, `TODO.md` §4 "Ring
  both (`0x03`, `FIND-004`): never sent — needs its own ADR".
- **Check:** whether the Fast Pair Device Action page still documents the bit mask (fetch it; quote it with the fetch date).
- **Outcome:** a proposal for one filmed test (buds not in the ears); if positive, a draft ADR-027 Update for "both" only — the Case stays out. **Review note:**
  one ACK from the pair, not "both buds ACK".

### Medium — corrections and additions to the documents

**E-06 — `PROTOCOL.md` §2.2 attributes to pbpctrl a format pbpctrl never had.**
- **Claim:** §2.2 "Hypothesis B — proprietary envelope (pbpctrl-derived)" (magic `0x5A`, 2-byte length, optional CRC); pbpctrl has used Pigweed HDLC with CRC-32
  since its first Maestro commits.
- **External source:** `libmaestro/src/hdlc/` (`crc.rs`, `encoder.rs`, `decoder.rs`), history 2022-12-17 … 12-19 (`git log --reverse -- libmaestro/src/hdlc`).
- **Also:** a third-party summary (KashaMalaga/PixelBudsMacOS, file Reference/README.md,) says "CRC-16"; pbpctrl itself never does — check and cite, do not clone it
  unless needed.
- **Outcome:** a dated correction note for §2.2 (it changes an attribution, not a FACT — still drafted and approved at the checkpoint).

**E-07 — Balance (field 17): the same bytes, the opposite sign convention.**
- **Claim:** pbpctrl `VolumeAsymmetry::from_raw`: odd raw → `+((raw>>1)+1)`, even → `−(raw>>1)`; its CLI says "-100 (left) to +100 (right)". That equals
  −zigzag, so this project's +100 = Left = pbpctrl's −100 and the physical mapping agrees. pbpctrl's `Display` shows the other side attenuated linearly
  (`left% = min(100, 100−v)`). **Review note:** both lists first called pbpctrl's reading wrong; it is not.
- **Project link:** ADR-026; `PROTOCOL.md` §4.5.7 (🔴 intermediate scaling).
- **Check:** re-derive the conversion for raw 0…200 against `(n>>1) ^ -(n&1)` with a small script (quote it and its output summary).
- **Outcome:** a dated note in §4.5.7: the same wire mapping (by arithmetic), pbpctrl's linear-attenuation reading as an external 🟡 for the open scaling.

**E-08 — Firmware entry order in `GetSoftwareInfo`.**
- **Claim:** pbpctrl's proto guesses case = 1, right = 2, left = 3 ("order might not be correct"); this project's code trace says 1 Case, 2 Left, 3 Right
  (`PROTOCOL.md` §2.2a, 2026-10-01). The serial order of `GetHardwareInfo` (Case, Right, Left, ADR-058) agrees in both. The wire cannot tell them apart.
- **Outcome:** a one-line note, no change.

**E-09 — Setting 18 readable on the Pro 2, status 2 on the gen 1 (pbpctrl comment).**
- **Outcome:** record as 🟡 "generation or firmware difference". **Review note:** not as a fact.

**E-10 — Wall clock and `SetWallclock`.**
- **Claim:** pbpctrl never sends `SetWallclock` (`// TODO: SetWallClock` in `service/impls/maestro.rs`); its proto says runtime field 2 is "maybe unix time in ms
  (consistent ~60s difference to actual time)".
- **Project link:** `DESKRESEARCH_FINDINGS.md` 2026-10-03 item 3 (52–548 ms in official-app captures, absent in OpenControl captures — 🟡 "present only while the
  Buds hold a clock set by `SetWallclock`"); draft ADR-060 🟡 (iii), the day index.
- **Check:** does pbpctrl's observation (a clock present but ~60 s off, never set by pbpctrl) fit that 🟡? A clock set earlier by the official app and drifted
  would fit. **Review note:** the second model wrote that pbpctrl "proves" it — it does not.
- **Outcome:** a note at the 2026-10-03 entry; a proposal to add "set the clock first; clock drift" to ADR-060's 🟡 list — a draft ADR, so ask before editing it.

**E-11 — Dosimeter.**
- **Claim:** the live value is a linear intensity, dB = `round(10·log10(x))` (pbpctrl `libmaestro/examples/maestro_listen.rs`, proto comment);
  `SubscribeToLiveDb` is a server stream in pbpctrl's code (`ServerStreamRpc`, `service/impls/dosimeter.rs`) although its proto calls it unary;
  `DosimeterSummary` = {1 int32, 2 repeated {1 int32, 6 float}, 4 int32, 5 float}.
- **Project link:** draft ADR-060 🟡 (i) and (ii); `REVERSE_ENGINEERING.md` `qir`/`qhz`/`qia`.
- **Check:** compare the field layout with the APK schemas; compute `10·log10` of the `qir` field-2 values in `CAP-027` (frames 1107/1214 onward) and see whether
  they fall in a plausible dB range.
- **Outcome:** a note for the `WELL-002` plan — it does not replace the sound-level meter.

**E-12 — OOBE.**
- **Claim:** setting 6 "OOBE mode" blocks touch gestures during the setup wizard; setting 3 "OOBE finished" should not be changed (pbpctrl `cli/src/cli.rs`
  help texts). `SubscribeToOobeActions` has an enum with `LEFT_ON_HEAD` / `LEFT_OFF_HEAD` / `RIGHT_ON_HEAD` / `RIGHT_OFF_HEAD`, taps, swipes and hold end/cancel.
- **Project link:** ADR-059 ("probably worn", never per bud); `REVERSE_ENGINEERING.md` (`qje`, 1 `ENUM`; never on any wire); `guy.java` "Enable/Disable OOBE
  mode".
- **Check:** compare the enum with the APK's `qje` values.
- **Outcome:** a proposal for a "never write 3 or 6" rule (where it belongs — `ARCHITECTURE.md` or a project-law note — is the maintainer's choice); a
  proposal for a read-only subscription experiment **without** OOBE mode, to see whether per-bud head events arrive.

**E-13 — Setting 1 (`AUTO_OTA_ENABLE`) and setting 5 (`DIAGNOSTICS_ENABLE`).**
- **Claim:** on the Pro 2 `ReadSetting 4:1` answers `UNKNOWN` (138 of 138) and setting 5 reads a constant `5:0` (`DESKRESEARCH_FINDINGS.md` 2026-10-06; the
  second model's addition); pbpctrl's help says setting 1 controls automatic updates when the Buds are not in use, setting 5 makes the official app send
  diagnostics to Google.
- **Check:** re-run the census line for settings 1 and 5 (`ai-sessions/0073`'s census counted `CAP-041` twice through `CAP-042`'s `.log.last` — `TODO.md` §6;
  say whether that affects these counts).
- **Outcome:** name setting 1 in the `qhr` register (pbpctrl naming, 🟡); nothing in the UI (the read is unsupported). **Review note:** `UNKNOWN` on read shows
  the read is unsupported, not that the Pro 2 "no longer handles OTA via this field".

**E-14 — Hold actions 1–4 (`CHECK_NOTIFICATIONS`, `PREVIOUS_TRACK_REPEAT`, `NEXT_TRACK`, `PLAY_PAUSE_TRACK`).**
- **External source:** the proto's `RegularActionTarget`; `settings.rs`.
- **Outcome:** names in the register (🟡); never written (the official app does not offer them on the Pro 2; firmware behaviour unknown).

### Low — confirmations and nice-to-haves

**E-15 — Peer and channel table.**
- **Claim:** pbpctrl `libmaestro/src/protocol/addr.rs`: Host 1, Case 2, LeftBtCore 3, RightBtCore 4, LeftSensorHub 5, RightSensorHub 6, LeftSpiBridge 7,
  RightSpiBridge 8, **DebugApp 9**, MaestroA 10, LeftTahiti 11, RightTahiti 12, MaestroB 13; channels 18–27; the same address formula as `fux.java` (`00 3b`
  for 19, answer `80 a3`). `resolve_channel` (`protocol/utils.rs`) also waits on 18 and 23 (Case) — passively, through registered calls with `call_id
  0xFFFFFFFF`.
- **Project link:** `PROTOCOL.md` §2.2a, ADR-034 item 3 (19/21/24/26 only) and its 2026-09-30 Update (b); the SDP UUID `25e97ff7-24ce-4c4c-8951-f764a708f7b4`
  "DEBUG APP" (DLCI 0x06; `REVERSE_ENGINEERING.md`, `PROTOCOL.md` §2.3) — one digit from the MAESTRO UUID `…f7b5`.
- **Check:** compare with `goq` in the APK.
- **Outcome:** the peer names (🟡); DebugApp 9 ↔ "DEBUG APP" only as a speculative note; read the app code for what happens on an announcement on 18/23 (never
  seen — re-derive the announcement count); if not handled, a `TODO.md` line.

**E-16 — Handover reset and reconnect timings.**
- **Claim:** pbpctrl `maestro_listen.rs`: "On a switch, the connection is reset", reconnect after 500 ms; MagicPodsCore PR #24: after a link loss the first
  RFCOMM connect was refused and a retry 1 s later succeeded; 12 retries, 1 s apart.
- **Project link:** L-1 (🟡 "the channel names the hosting bud"), ADR-044 (`LOSS_REOPEN_DELAY_MS` 1 500 ms, `CHAIN_GUARD_MS` 10 000 ms), `CAP-040` (about nine
  refused re-opens after docking).
- **Outcome:** a note as external support for L-1. **Review note:** the second model called 1 500 ms "optimal" — no basis; compare the timings, conclude nothing.

**E-17 — An explicit `GetSoftwareInfo` request is answered.**
- **External source:** pbpctrl `cli/src/main.rs` `cmd_show_software`, `call_id 0`; commit `7e75204` (the unsolicited `0xFFFFFFFF` since firmware 5.9).
- **Check:** has an announcement ever been missing in this project's captures (re-derive the "43 of 43" / "140 of 140" counts)?
- **Outcome:** at most a low-priority `TODO.md` idea (a fallback after `MAESTRO_ANNOUNCE_WAIT_MS`). **Review note:** the second model's example `CAP-061` was a
  parser bug (wire type 1 not skipped); an explicit request would have returned the same bytes and failed the same way.

**E-18 — Fast Pair details.**
- **Claim:** battery bytes in the order L, R, Case with `0x7F` = unknown (`libgfps/src/msg/types.rs`), as here; Device Information code `0x04` "remaining battery
  time" = 1 or 2 bytes big-endian, in minutes (`examples/gfps_listen.rs`); NAK reasons 0 not supported, 1 busy, 2 not allowed in current state; SASS group `0x07`
  codes.
- **Check:** do `AncFrameDecoder`, `MessageStreamAck.reasonName` / `BudsError.CommandRejected` and the Debug tab name reasons 0–2, or only pass the code on?
- **Outcome:** a `TODO.md` line for decoder names if missing; SASS stays out of scope (account key, ADR-008).

**E-19 — `HardwareInfo` fields 1, 2, 5, 6 and `SoftwareInfo` field 2.**
- **Claim:** pbpctrl calls them `unknown`; field 1 is already the device type here (`fsf.java`: 6 = `LARGO`, `GetHardwareInfo 1:6`).
- **Check:** decode fields 2, 5 and 6 in existing answers (e.g. `CAP-036` 1423) against `qiv` / `qjm` / `qjr`. Serials stay redacted (first 4 + last 2,
  ADR-058 item 5).
- **Outcome:** a desk entry.

**E-20 — Class of Device `0x244404` (Pro 2) vs `0x240404` (Pro).**
- **External source:** pbpctrl `cli/src/bt.rs` (PR #15), MagicPodsCore `PixelBudsHelper.h`.
- **Check:** find the Buds' Class of Device in a capture (Connection Request / Inquiry Result / EIR) with the command.
- **Outcome:** document it in `PROTOCOL.md` §0.1 if it matches (a proposal). The second model's UX idea (name a gen 1 explicitly instead of the generic Safe
  Mode card) goes into `TODO.md` §5 as an idea for the maintainer.

**E-21 — Confirmations, no action expected.**
- pbpctrl's ANC cycle order Active → Off → Aware → Adaptive vs the cycle the Buds ran in `CAP-064` (Transparency → Noise cancellation → Off, with Adaptive
  unticked — ADR-046 Update).
- The gesture loop needs at least two modes; the EQ is clamped to ±6.0.
- The case battery is available only with a bud in the case (pbpctrl `README.md`).
- **Outcome:** verify each in one line; record only where a document lacks it.

**E-22 — Provenance of pbpctrl's knowledge.**
- **Claim:** pbpctrl does not say how it obtained the protocol. Its history shows public sources (the Fast Pair spec and AOSP in `docs/Notes.md`; Pigweed's
  `pw.rpc.packet.proto`), listening and probing from Linux (the `listen` examples first; `EncodedMessage` "for reverse-engineering … `protoscope`"; comments
  such as "returns non-zero status", "might be bool"), and — most likely — names from Google's app: pw_rpc ids are hashes, so the service and method names must
  come from a source holding the strings; `AllegroSettingType` matches "handleAllegroSetting" (`fxb.java:43`) and `fsf.java` `ALLEGRO` = device types 4/5;
  pbpctrl's battery enum differs from the APK's (`qhw.java`), which looks like its own guess.
- **Outcome:** a short provenance note (in `DESKRESEARCH_FINDINGS.md` or at `README.md` "Attribution" — the maintainer's choice) stating that pbpctrl's names
  weigh as "APK name", not as independent evidence.
- **Also check:** `AGENTS.md` §5 says pbpctrl "depends on BlueZ, D-Bus, AVRCP, and UPower for battery state"; pbpctrl's README uses UPower only for BlueZ's
  experimental AVRCP battery and reads its detailed battery via Maestro and GFPS. A wording proposal only — project law.

**E-23 — The second model's overclaims (for the record).** Confirm each correction while checking: E-03 "solves", E-10 "proves", E-13 "proves", E-16 "optimal",
E-09 "generation difference" as fact; E-02 `CAP-045`; E-05 "both buds ACK"; E-17 `CAP-061`; "no contention on DLCI 0x02" — true for other clients, but the Buds
close DLCI 0x02 themselves (`CAP-062` §3, ADR-044; L-1), so a 13-based ANC path still needs ADR-044's re-open.

## 5. Method, per item

1. Verify the external claim at the pinned commit (file and line; a quotation of at most a few lines).
2. Verify the project link (file, section or line; capture frames with commands and hex).
3. Run the desk check where one is named.
4. Judge on two separate axes — **validity:** `valid` / `partly valid` (say which part) / `not valid` / `not checkable here` (say what would check it);
   **usefulness:** `high` / `medium` / `low` / `none` — for what (the noise-control question, a fix, a known issue, an open question, documentation accuracy).
5. One **verdict:** **APPLY** (a documentation change this session may make after the checkpoint) · **PROPOSE** (needs the maintainer: an ADR, a FACT text,
   project law, a scope change) · **EXPERIMENT** (needs hardware: a `TODO.md` line, or a skeleton if asked) · **NOTE** (record only) · **REJECT** (with the reason).

## 6. Tasks

### Phase 0 — set-up

1. Create the RESULT (§0). Record `git log -1`, `git status --short`, `git fetch` and `git log --oneline HEAD..origin/main`. Branch from an up-to-date `main`:
   `git switch main && git pull --ff-only && git switch -c research/pbpctrl-crosscheck` (say the name used). `android/domain/bin/` is build output: never stage it.
2. `python3 scripts/lint_docs.py` once before any change (record the exit status).
3. Clone the three external repositories into the scratchpad at the pins (§3); record each `rev-parse HEAD` and any later commit on the cited files.

### Phase A — the leading items (E-02, E-01, E-03)

4. Run their checks; write each evidence section into the RESULT as it is done; answer the leading question (§1) in a short paragraph with its labels.

### Phase B — the other items (E-04 … E-23)

5. In the order of §4; for each, the evidence section and its row in the verdict table.

### Phase C — checkpoint (stop and ask, in this chat, via `AskUserQuestion`)

6. Before any edit, put the verdicts to the maintainer — one question per decision, options with their pros and cons, "(Recommended)" on the option this
   session would pick with a one-line reason, the exact text in the preview: which APPLY items go in; each PROPOSE item (each ADR draft and FACT text shown in
   full); whether any EXPERIMENT gets a skeleton now, and which; where the provenance note (E-22) goes. Record the answers verbatim in the RESULT. Do not build
   an option that was not chosen.

### Phase D — apply (only what was approved)

7. Dated Updates in `PROTOCOL.md` / `REVERSE_ENGINEERING.md` / `DECISIONS.md` (rule 9a); new `DESKRESEARCH_FINDINGS.md` entries with commands and hex
   (rule 4a); `TODO.md` lines in the right group, citing this session; `id_registry.csv` rows where needed (a new ADR or `CAP-NNN`). No file under `android/`
   changes; nothing from the external repositories is copied in. `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and
   `python3 scripts/lint_docs.py` (exit 0).

### Phase E — finish

8. The RESULT (§8), the `ai-sessions/INDEX.md` row. Show a short summary and **ask whether to commit and push**. Only after a yes: commits per concern
   (Conventional Commits: `docs:` / `re:`), each with its *why* and the session's attribution line; `git fetch` and rebase; push without force. Opening the pull
   request is a separate question; merging is the maintainer's act (`RELEASING.md` "Repository rules").

## 7. Guardrails (binding, in addition to `AGENTS.md`)

- **Scope:** only the items above. No app code, no wire bytes, no hardware run, no decompiling of 1.0.990706425.
- **Approvals only in this chat;** ADR-060 stays a draft unless the maintainer accepts it; accepted ADRs are not rewritten — a superseding text is a proposal.
- **Privacy:** no serial unredacted, no MAC, no account name or Wi-Fi name in any committed text.
- **Files:** before moving or deleting any file, diff a fresh listing; never wildcard deletes; ask before any delete.

## 8. The RESULT (0090_CROSSCHECK_RESULT_2026_10_14.md) must contain

1. The header block with `Status:` (`awaiting maintainer sign-off` while proposals are open; one of the three values of `AI_SESSION_LOG_PROCEDURE.md` §4).
2. A plain-language summary first: the answer to the leading question, what was applied, what the maintainer decides next.
3. "Files read": in full / in part.
4. The verdict table (ID · claim (short) · validity · usefulness · verdict · evidence pointer · M/S) and one evidence section per item.
5. What was applied, with the maintainer's approval quoted (question name and option chosen).
6. **Deferred documentation:** each deferred item with its `TODO.md` wording (`AI_SESSION_LOG_PROCEDURE.md` §4b item 1).
7. **Commits:** hash and subject of each commit, or "not committed — reason".

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0090_CROSSCHECK_PROMPT_2026_10_14.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0090_CROSSCHECK_PROMPT_2026_10_14
