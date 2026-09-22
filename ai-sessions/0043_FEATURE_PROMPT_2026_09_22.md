# 0043_FEATURE_PROMPT_2026_09_22.md — Move LOGS-001/LOGS-002 into `captures/` as CAP-059/CAP-060, fully analyse CAP-060, root-cause the Case-battery/dock-state/ANC-tile/connection-drop questions, and extend the app (settings UI, tab-swipe navigation, timestamped state)

**Number:** 0043
**Category:** FEATURE
**Date:** 2026-09-22
**Title:** Reclassify the two full `LOGS-00x` hardware-capture sets as `captures/CAP-059`/`CAP-060` (maintainer's reversed decision on where full capture sets live), fully analyse `CAP-060` (the `ai-sessions/0042` build), root-cause four specific app questions with evidence, review the app's architecture for weaknesses, and implement approved fixes/features (Case battery, dock state, tab-swipe navigation, "last known" → timestamp)
**Status:** prompt only — not yet run

---

## 0. How to use this prompt

This prompt is meant to be handed to a Claude Code session **on its own**, as a direct follow-up to
`ai-sessions/0042_FEATURE_PROMPT_2026_09_20.md` / `ai-sessions/0042_FEATURE_RESULT_2026_09_20.md` (read both in full — the
second is this project's current understanding of the app and of the `LOGS-001` capture, and records exactly what was built,
including four new `DECISIONS.md` ADRs: **ADR-035** Case battery via an on-demand, **receive-only** claim of DLCI 0x08, and
**ADR-036** a read-only `ReadSetting` unblock with **nothing implemented**).

**Read, in this order, before doing anything else** (`AGENTS.md` §0.1, binding per `AI_SESSION_LOG_PROCEDURE.md` §8): `AGENTS.md`
→ `PROJECT_RULES.md` → `PROJECT.md` → `ARCHITECTURE.md` → `PROTOCOL.md` → `DECISIONS.md` (**every** ADR — ADR-005/006/007/010/014/
024/032/033/034/035/036 are the ones this session touches most, but read all of them per `PROJECT_RULES.md` rule 13) → `TODO.md`.
Then `ai-sessions/INDEX.md`, `ai-sessions/0039`–`0042` (both PROMPT and RESULT), the three most recent `DESKRESEARCH_FINDINGS.md`
entries, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (all of it — its Capture Index in §9, and note that its own intro frames captures as
"official app on a Pixel 7a" vs. "GrapheneOS/no app validation on a Pixel 9a" and **says nothing about capturing this project's own
OpenControl app** — that gap is this session's first task), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `REVERSE_ENGINEERING.md`, and
`id_registry.csv`. Session numbering: check `ai-sessions/INDEX.md` — this is `0043`; the next free `CAP-NNN` is `CAP-059`
(`CAP-058` is already reserved/planned per `id_registry.csv`) and the next free `ADR-NNN` is `ADR-037`.

**Follow every guardrail in `AGENTS.md` throughout** — most relevantly §1 (zero GMS, no network), §2 (minimal permissions,
GrapheneOS), §5 (never fabricate a battery value or a dock state), §6 (FACT/ADR sign-off gate — **you may draft ADR text and
FACT-promotion proposals, you may never write them into `DECISIONS.md`/`PROTOCOL.md` yourself**), §7 (no discovery scanning, no
persisted/logged MAC), §8 (error model), §9 (logging), §11 (tests), §13 (capture-analysis workflow, and note its worked example
already uses `captures/CAP-NNN-*` file names directly — once a capture is moved there, citing it **by its new `CAP-NNN-*`
filename is the normal, expected practice**, not a violation of anything).

**The maintainer's own instructions for this session (apply throughout, not just where restated below):**
- **Validate and check before asserting anything.** Every claim about the app's behaviour needs a specific frame number, log
  line, or video timestamp — never "this is probably why."
- **No assumptions — base everything on facts.** Where the evidence is genuinely insufficient to answer a question, say so
  explicitly (🔴 OPEN QUESTION, per `PROJECT_RULES.md` §1) rather than filling the gap with a plausible guess.
- **No sampling — analyse fully.** Read every frame of `CAP-060-recording.mp4` (not a subset), every line of every log file, the
  whole HCI capture — the same standard `ai-sessions/0042` applied to `LOGS-001`.
- **Cross-check and run consistency checks** between the video, the app's own debug export, the app's logcat, the system log,
  the HCI capture, `ai-sessions/0042`'s findings, and this project's reference documents (`PROTOCOL.md`, `DECISIONS.md`,
  `ARCHITECTURE.md`, `DESKRESEARCH_FINDINGS.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`,
  `REVERSE_ENGINEERING.md`) — flag, don't silently resolve, any place they disagree.
- **Whenever something is genuinely the maintainer's decision to make** (an ADR, a scope change, a naming/process convention,
  anything that trades off privacy, risk, or user experience one way vs. another) — **stop, write a short summary with your
  recommendation and its trade-offs, and ask via `AskUserQuestion`.** Do not proceed past a real decision point on your own
  judgment alone, even when you are confident. This applies in addition to, not instead of, `AGENTS.md` §6's existing sign-off
  gate.

---

## 1. What the maintainer asked for (chat, 2026-09-22)

1. **Reverse the earlier "`android/logs/` only, gitignored, never committed" decision for full capture sets.** Two complete
   hardware-capture sets of the OpenControl app itself now exist under `android/logs/LOGS-001/` and `android/logs/LOGS-002/`
   (recording(s), raw `btsnoop_hci.log`, the app's debug export, the app's own logcat export, the Android system log, and — for
   `LOGS-001` only — two screenshots and a maintainer-authored events file). These are to be moved into `captures/CAP-<nnn>-
   yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_<letters>/`, in the same form as every existing capture under `captures/`, **without loss
   of information** — `LOGS-001` becomes `CAP-059`, `LOGS-002` becomes `CAP-060`.
2. **Update every relevant project convention and reference** so the `android/logs/LOGS-0xx` → gitignored-forever rule is
   replaced, for *full capture sets specifically*, by the rule that already governs `captures/CAP-NNN-*` (real identifiers
   allowed per ADR-010, Git LFS per `PROJECT_RULES.md` rule 18, registered in `id_registry.csv` and `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
   §9). Register both new `CAP-NNN` numbers everywhere relevant. Run a full consistency/cross-check pass afterwards.
3. **Fully analyse `CAP-060`** (the capture not yet analysed — `CAP-059`/`LOGS-001` already went through `ai-sessions/0042`):
   video → timestamped event notes; validate the files against the capture's own purpose; correlate the HCI log, the app debug
   export, the app logcat, and the system log with each other and with the event notes; answer `ai-sessions/0042`'s open
   questions where this capture speaks to them; and answer four specific questions (§5 below) with evidence, cross-checked
   against prior captures and the reference documents, and against authoritative external sources where relevant.
4. **Review the app for what can be improved**, including anything "fundamentally wrong," and fix what the evidence supports
   fixing without a new maintainer decision; flag what needs one.
5. **Add new functionality** the evidence and the logs support, including (now that `ai-sessions/0042`'s Case-battery and
   dock-state work is running on real hardware) working out *why* those two specific features are not delivering a value to the
   maintainer today, and fixing that if the fix needs no new decision, or proposing the fix as a checkpoint item if it does.
6. **Improve the UI**: swipe left/right between the Connection/ANC/EQ/Find/Debug tabs, and replace every "(last known)" label
   with an actual timestamp.

## 2. Evidence at hand (identify by role and, once moved, by `CAP-NNN` filename — see §3's transition rule)

| Role | What it is (measured by the prompt author — verify) |
|---|---|
| `LOGS-001` (→ `CAP-059`) | Already fully analysed in `ai-sessions/0042` (`DESKRESEARCH_FINDINGS.md`, 2026-09-20, second entry). 9 files: the maintainer's events-file template (fully filled by `0042`), the app's debug export, the raw HCI snoop log, the app's own logcat export, the Android system-log export, two camera recordings (40.1 s and 286.5 s), two screenshots. Built on commit `9fe4b70` (`ai-sessions/0041`) — 🟡 timing-consistent (commit landed 15:17:40 local the same day the capture starts 17:16:59), not independently re-verified against the log content this session inherits; do so as part of the move (§4b). **Both recordings carry a street-address line burned into their overlay** (verified this session) — see §4's privacy checkpoint, which blocks committing either file until answered. |
| `LOGS-002` (→ `CAP-060`) | **Not yet analysed.** 5 files: the app's debug export (416 lines, 17:54:15–~18:00 local), the raw HCI snoop log (355 KB), the app's own logcat export, the Android system-log export, one camera recording (408.65 s ≈ 6:49, 1280×720, `creation_time` 2026-09-21T16:00:50Z = 18:00:50 local). **No events file, no screenshots were supplied for this one.** The recording's overlay was sampled across its full duration this session (5 frames, `ss` 1/60/150/250/350/400) and shows **only a date/time stamp, no address line** — unlike `LOGS-001`'s recordings; re-verify this over the *entire* video (a full read, not a 5-frame sample) rather than trusting this spot-check. Built on commit `1efa86b` (`ai-sessions/0042`) — 🟡 timing-consistent (commit landed 21:35:41 local the evening before the capture) **and** a quick, non-exhaustive read of the debug export already shows log lines that only exist in that commit's code (`Android link: X -> Y (trigger: …)`, `Pairing: bond state BONDING`/`BONDED`, `Case battery not read: Timeout` ×8) — confirm this fully and exhaustively rather than treating this spot-check as the analysis. |
| `LOGS-002`'s own leads (unverified spot-check by the prompt author — **verify independently before relying on any of this**) | `Case battery not read: Timeout` appears **8 times in 416 lines, with zero successes** — worth checking first against ADR-035's known open item ("whether the Buds push the Case level *without* the phone-side `0e 04` request is 🟡 HYPOTHESIS… if not, the next step is an ADR to send `0e 04`"). Zero occurrences of `ANC tile tapped` in either the debug export or the app's own logcat, and zero mentions of `AncTileService`/`QS_TILE`/`BIND_QUICK_SETTINGS_TILE` anywhere in the system-log export — consistent with (but not proof of) the tile never having been added to the maintainer's Quick Settings panel at all, which is a discoverability question distinct from a code defect (a `TileService` never appears automatically; the user must open Quick Settings' own edit/pencil screen and drag it in — confirm this is in fact how it works via an authoritative source, §5). |

## 3. Standing rule — before vs. after the move

**Before the move** (while a file still lives under `android/logs/`): cite it by role and time window only, exactly as every
earlier session did — no committed file may name an `android/logs/` file. **After the move** (once a file is `captures/CAP-059-…`
or `captures/CAP-060-…`): cite it **by its new filename**, exactly like every existing `CAP-NNN-FINDINGS.md`/`CAP-NNN-EVENT-NOTES.md`
already does (e.g. `CAP-009-btsnoop_hci.log`, frame 1044) — this is the established, expected practice for `captures/`, not an
exception to the anti-naming rule, which only ever applied to the still-gitignored `android/logs/` location. Get the move done
(§4b) before writing the analysis (§5) so the analysis can cite frame numbers against the final, committed filenames directly.

## 4. Tasks

### Phase 0 — a decision that blocks any `git add`, ask first (do this before any other work touching `captures/`)

This repository is **public** (`git remote -v` → `github.com/tedsluis/opencontrolpixelbudspro2`, and its own docs site is public).
`ADR-010` covers real MAC addresses/device identifiers in `captures/CAP-NNN-*` as the maintainer's own, already-made, informed
decision — but a **home street address burned into a video's every frame** is a different, more consequential kind of personal
data (physical-location exposure) that `ADR-010`'s text never discusses, and publishing it to a public, permanent (LFS-backed)
git history is hard to walk back once pushed (forks, caches, and GitHub's own history retention all outlive a later force-push).
**Do not add, commit, or push either of `LOGS-001`'s two camera recordings (they would become `CAP-059-recording1.mp4`/
`CAP-059-recording2.mp4`) until this is answered.**

1. Confirm, by reading **every** frame of both `LOGS-001` recordings (not a sample), exactly which frames carry the address line,
   its exact on-screen position, and whether it is present for the recordings' full duration or only part of it.
2. Summarise this for the maintainer with a recommendation (e.g.: crop/blur the fixed-position overlay region with `ffmpeg`
   before committing, verified afterwards by re-reading sampled output frames to confirm the address is genuinely unreadable;
   vs. committing the files unedited on the same "my own data, my own informed choice" reasoning ADR-010 already accepted for
   MAC addresses; vs. not committing the video files at all and keeping only the non-video evidence under `captures/CAP-059`)
   and **ask via `AskUserQuestion`** before proceeding. `LOGS-002`'s single recording showed no address line in this session's
   spot-check, but confirm that fully (task 1's method) before assuming it needs no such treatment either.

### Phase A — design the migration, then ask (a second, smaller decision)

3. Work out, from `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (all of it, including `DECISIONS.md` ADR-007's "a Group is a capture
   scenario, not a test" framing) and `id_registry.csv`, what `CAP-059`/`CAP-060` need: a `Group` letter (the next free ones are
   `AU`/`AV`, per `id_registry.csv`'s highest-registered `Group_AT`, `CAP-058`) bundling whichever existing `TESTPLAN_BLUETOOTH_
   HCI_SNOOP.md` Test-IDs the two capture sessions actually exercised (candidates worth checking, not to be trusted without
   verifying against the actual event timeline: `PAIR-001`, `ANC-001`–`004`, `EQP-005`/`006`, `EQS-001`–`005`, `FIND-001`/`002` for
   `CAP-059`; work out `CAP-060`'s own list from its event notes, task 8). Note for the maintainer that `CAPTURE_BLUETOOTH_HCI_
   SNOOP.md`'s own intro currently only frames captures as "official app, Pixel 7a" vs. "GrapheneOS validation, no app, Pixel
   9a" and has no language at all for "this project's own OpenControl app, captured for app-validation/debugging purposes" —
   propose a short addition to that document's intro/§9 covering this third purpose (this is a documentation-convention change
   with real, lasting consequences for how evidence is organised — treat it as the kind of decision `AGENTS.md` §6's spirit
   requires a maintainer sign-off for, even though it is not itself a protocol/architecture FACT).
4. Also draft (do not write into `DECISIONS.md`): whether this convention change deserves a full ADR of its own (a short one,
   analogous in spirit to ADR-007, formally retiring the "`android/logs/LOGS-0xx`, always gitignored" standing rule for *full
   capture sessions* while keeping it for ad hoc individual debug-log/screenshot pulls that are not a full capture session) —
   give your recommendation with pros/cons.
5. **Ask via `AskUserQuestion`**, in one batch with Phase 0's question if that is still open: the Group-letter/Test-ID approach
   from task 3 (or an alternative, if you find one genuinely better after researching ADR-007), whether to write the short ADR
   from task 4, and confirm the exact target directory names (`captures/CAP-059-2026-09-20_17-16-59_17-22-30-Group_AU/` and
   `captures/CAP-060-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AV/` — `CAP-060`'s start/end times come from task 8's video analysis, done
   before this directory is finally named, matching how the still-unrun `CAP-052`–`CAP-058` placeholders are named
   `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` until their session happens).

### Phase B — execute the approved migration

6. Create both `captures/CAP-0NN-…/` directories; move every file out of the still-gitignored `android/logs/LOGS-001/`/
   `LOGS-002/` into the matching one, renamed from its old `LOG-00N-` prefix to the approved `CAP-0NN-` prefix (same suffix,
   otherwise unchanged), preserving every byte — verify with a checksum before deleting the source. Confirm Git LFS will
   actually track the moved `.mp4`/`.log` files (`.gitattributes` already has `captures/**/*.mp4`/`captures/**/*.log` patterns —
   confirm with `git check-attr filter -- <path>` for each moved file, not by assumption) before `git add`.
7. Reshape the maintainer's events-file template (`LOGS-001`'s own, filled by `ai-sessions/0042`) into the standard
   `CAP-NNN-EVENT-NOTES.md` filename and structure used by every other **already-analysed** capture — read several real,
   filled examples first (not an unrun placeholder skeleton), e.g. `captures/CAP-050-2026-09-14_21-01-01_21-13-30-Group_AG/
   CAP-050-EVENT-NOTES.md` and `captures/CAP-051-2026-09-14_21-42-55_21-44-09-Group_AM/CAP-051-EVENT-NOTES.md`, and match
   their actual structure, not a guess → `captures/CAP-059-…/CAP-059-EVENT-NOTES.md`. Preserve every FACT/HYPOTHESIS-labelled
   observation from the original file; do not lose content in the reshape.
8. Migrate `ai-sessions/0042`'s `LOGS-001` findings (the `DESKRESEARCH_FINDINGS.md` 2026-09-20 second entry) into a new
   `captures/CAP-059-…/CAP-059-FINDINGS.md`, per that document's own stated scope ("a `CAP-NNN-FINDINGS.md` file… documents
   first-pass findings from **one specific capture**"; `DESKRESEARCH_FINDINGS.md` is for cross-capture/pattern analyses, which
   this entry — being about exactly one capture — is not really an example of). Rewrite it in the `CAP-NNN-FINDINGS.md` "current
   truth" style `PROJECT_RULES.md` rule 9a requires (not an accumulating changelog), citing the new `CAP-059-*` filenames.
   Replace the `DESKRESEARCH_FINDINGS.md` entry with either a short pointer to the new file or remove it, per that document's own
   stated purpose — check which is more consistent with how a prior migration (if any) was handled, or ask if genuinely unclear.
9. **Do not rewrite `ai-sessions/0039`–`0042`'s own prompt/result files** to say `CAP-059`/`CAP-060` in place of `LOGS-001`/
   `LOGS-002` — they are dated historical records of what was true when written (`AI_SESSION_LOG_PROCEDURE.md` §4a only permits
   updating a `Status` field in place, with the update self-documented in a new session's own pair, which this one is). If a
   cross-reference is useful, add it the way §4a's own worked example does (a short, clearly-dated addition), not a silent
   find-and-replace across historical prose.
10. Update every **living** reference document that currently names `LOGS-001`/`android/logs` (grep the whole repo first —
    do not rely on the list here, which is illustrative, not exhaustive): `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md`
    (ADR-024's and ADR-034's 2026-09-20 update bullets, and ADR-035's own context section, all currently say "`LOGS-001`, kept
    locally" or similar), `TODO.md`, `CHANGELOG.md`, `ai-sessions/INDEX.md`'s `0042` row, `id_registry.csv` (two new `CAP-NNN`
    rows plus, if approved, one new `ADR-NNN` row), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's Capture Index (two new rows). Also
    update `.gitignore`'s `android/logs/` comment block to state the narrowed scope from Phase A (ad hoc pulls stay gitignored;
    a full capture session is moved to `captures/`), and remove the now-stale "as in every earlier session"/"standing rule"
    boilerplate this prompt itself still contains only where a **future** prompt would otherwise copy it forward — do not edit
    `ai-sessions/0039`–`0042` themselves (task 9).
11. **Consistency/cross-check pass**: re-run `python3 scripts/lint_docs.py` and fix everything it flags that this session's
    changes touched; confirm every new `CAP-NNN`/`ADR-NNN` reference resolves in `id_registry.csv`; confirm the Capture Index
    row's Group/Test-ID columns actually match what `CAP-059-EVENT-NOTES.md`/`CAP-060-EVENT-NOTES.md` record (not what task 3
    guessed); confirm no committed file anywhere still says `android/logs/LOGS-00` after the move.

### Phase C — full analysis of `CAP-060` (nothing here is a spot-check — see §0's rules)

12. **Video, in full.** `ffprobe` the recording (duration, frame rate, audio track). Extract frames at least every 2 seconds
    (every 1 second where the UI changes; add scene-change frames), read **every** extracted frame, and separately verify the
    overlay's privacy content across the **entire** duration (Phase 0 task 1's method, applied here too, not inherited from this
    prompt's own 5-frame spot-check). Derive and state the video-clock-to-phone-clock offset the same way `ai-sessions/0042`
    did for `LOGS-001` (measure it — don't assume the same 1.3 s offset carries over to a different recording).
13. Record the timeline in `CAP-060-EVENT-NOTES.md` (a fresh file — none existed for this capture; follow the same structure
    task 7 used for `CAP-059`): every action, every visible status text (exact wording), every visible battery/dock-state value,
    each with wall-clock and video time, labelled FACT/HYPOTHESIS. Fill in the metadata table (build/commit — verify task-6's
    inherited `1efa86b` claim fully, not just the spot-check in §2 — video length, HCI length, clock offset, Group/Test-IDs) and
    the integrity pre-flight (`capinfos`, the `cap_len`/`len` mismatch check).
14. **Validate the files against the capture's own purpose.** State plainly what this capture's purpose appears to have been
    (inferred from the events themselves — there is no maintainer-authored purpose statement this time, unlike `LOGS-001`'s
    template) and whether the five supplied files are sufficient to serve it, same as `ai-sessions/0042`'s task 3 did for
    `LOGS-001`'s coverage gaps.
15. **Correlate the HCI log to the events**, following `AGENTS.md` §13 and the exact method `ai-sessions/0042` used (pre-filter
    by the Buds' connection handle, not by address; RFCOMM SABM/UA/DISC/DM per DLCI; DLCI 0x02 via `scripts/pwrpc_decode.py`;
    DLCI 0x04/0x08 via the `[Group][Code][Length][Value]` reassembly). Every conclusion needs a frame number, the exact
    command, and the raw hex (`PROJECT_RULES.md` rule 4a).
16. **Correlate every other log to the events and to each other**: the app's debug export, the app's own logcat, the Android
    system log. Note every coverage gap or inconsistency explicitly (which log covers which time window, at what precision, any
    clock disagreement) — the same standard `ai-sessions/0042`'s §2 applied.
17. **Answer `ai-sessions/0042`'s open items where `CAP-060` speaks to them** — re-read that RESULT's §4 (open questions), §5
    (what went wrong), and §12 (re-test instructions, items a–j) and, for each one this capture's evidence can confirm, refute,
    or add to, do so explicitly with frame numbers/log lines; for anything it cannot speak to, say so rather than silently
    dropping it.

### Phase D — the four specific questions (task 3's leads are a *starting point to verify*, never a conclusion to copy)

18. **Why did the OpenControl session sometimes lose its connection?** Apply the exact method `ai-sessions/0042` used for
    `LOGS-001`'s three drops (§4 of that RESULT: was it the user's own tap, Android's own Bluetooth panel, or the Buds
    themselves closing RFCOMM while the ACL stayed up — cite the HCI `Disconnection Complete` reason code or the RFCOMM
    `DISC`/`DM` frame and direction for every drop in `CAP-060`, cross-checked against the video). State whether `CAP-060`'s
    drops are the same phenomenon as `CAP-059`'s unexplained one (17:21:16 in `LOGS-001`) or something new.
19. **Why was the Case battery status unavailable?** §2's spot-check found `Case battery not read: Timeout` 8/8 times with zero
    successes. Confirm fully: was DLCI 0x08 actually opened by the app every time (HCI `SABM Channel=4`/`UA`)? Did **any** `Group
    0x0e Code 0x01` push ever arrive on it in this entire capture, from the app's claim or from anyone else (e.g. Google Play
    services requesting `0e 04` and getting an answer, which would prove the Buds *can* push without the app's own request being
    the trigger)? This bears directly on `DECISIONS.md` ADR-035's own stated open item ("receive-only… if [the Buds] do not
    [push unrequested], the Case is reported unavailable… and sending `0e 04` needs its own ADR") — if the evidence shows the
    Buds never push without a request, draft the ADR-035-superseding text (sending `0e 04` on the on-demand claim) as a proposal
    and take it to the consolidated checkpoint (Phase F); do not implement it without approval.
20. **Why can't you see which bud is docked?** First, re-derive precisely what `DECISIONS.md` ADR-024 actually promoted: the
    `Settable-toggles` byte is a **joint** state ("both earbuds seated" vs. "not both") per the ADR's own text — if that is still
    accurate, the honest answer may be that per-earbud dock/in-ear status is a **different, not-yet-implemented** signal, not a
    bug in what was built. Check whether `PROTOCOL.md` §4.5.5 / `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `INEAR-002`–`004` (DLCI 0x08
    `Group 0x04 Code 0x12`'s alternating value, flagged there as possibly event-driven and per-bud) is a more promising, already
    partly-identified candidate for a *per-earbud* signal — check whether `CAP-060`'s HCI log contains any `Group 0x04 Code
    0x12` traffic and whether it correlates with a bud actually being taken out (video-confirmed) on either side specifically.
    Also check, separately, whether the dock-state line the app *does* have ever actually reached the UI in this capture (was an
    ANC action/refresh/Connect-time snapshot performed at all, so a `Notify` had a chance to arrive?) — distinguish "the signal
    never arrived this session" from "the signal arrived but the UI never showed it" with evidence for whichever is true.
21. **What is the ANC tile? Is it a widget? Why couldn't the maintainer find an ANC widget on GrapheneOS?** Answer this
    precisely and explain it to the maintainer in plain language: `AncTileService` (`android/app/src/main/kotlin/…/app/
    AncTileService.kt`) is an Android **Quick Settings tile** (`android.service.quicksettings.TileService`), which is a
    different UI surface from a **home-screen widget** (`AppWidgetProvider`) — a home screen has no ANC widget because none was
    built; a Quick Settings tile exists but is never added to the panel automatically. **Verify this mechanism against an
    authoritative source** (the official Android developer documentation for `TileService`/adding a custom Quick Settings tile —
    fetch and cite it, do not rely on background knowledge alone) and state the exact, current steps to add it (typically:
    open Quick Settings fully, tap the edit/pencil icon, drag the "ANC" tile from the available-tiles list into the active
    panel) — check whether GrapheneOS changes this flow in any documented way. Separately, confirm from `CAP-060`'s system log
    and app logs whether the tile was ever bound/invoked at all (§2's spot-check found zero matches — confirm exhaustively) and
    whether anything in the manifest/service declaration could itself be preventing discovery (re-read `AndroidManifest.xml`'s
    `AncTileService` entry and `ic_anc_tile.xml` for anything wrong, not assumed correct just because it compiled).

### Phase E — architecture review (write-up first, fixes/features in Phase F/G, decisions to Phase-F's checkpoint)

22. **How the app is put together.** A concise, accurate description of the current architecture as actually built (not as
    `ARCHITECTURE.md` originally envisioned, if the two have drifted) — module boundaries, the on-demand-channel pattern
    (DLCI 0x04/0x08 claims), the state-mirroring design, the error model.
23. **What could be improved**, **what is fundamentally wrong (if anything) and how you would change it**, and **the app's weak
    points and how to resolve them** — grounded in this session's own `CAP-060` findings and `ai-sessions/0042`'s `CAP-059`
    findings, not a generic code-review pass. For each weak point: severity, evidence, and whether the fix needs a new
    maintainer decision (→ Phase F checkpoint) or not (→ fix now, Phase F).

### Phase F — fixes and the consolidated checkpoint

24. **Fix everything Phase C/D/E found that needs no new maintainer decision**, each with a regression test (real bytes/fakes,
    per `AGENTS.md` §11) and, for the two most safety-critical new guards, a mutation check (same discipline as `ai-sessions/
    0042` §6).
25. **Consolidated checkpoint — stop and ask**, via `AskUserQuestion`, one question per decision, recommended option first with
    "(Recommended)" and pros/cons in each option's description, covering at minimum: (i) anything still open from Phase 0/A if
    not already resolved; (ii) the ADR-035-superseding proposal from task 19, if the evidence points that way; (iii) any other
    ADR-level proposal Phase D/E surfaced (e.g. an `INEAR`-based per-bud dock signal, if task 20 finds one worth pursuing); (iv)
    which new-functionality items (Phase G) to build now vs. leave as `TODO.md` proposals; (v) any `PROTOCOL.md` FACT promotion
    this session's analysis supports. Never assume an answer; if a question cannot be asked in this environment, say so and stop
    before the gated work.

### Phase G — new functionality (build what needs no new decision; describe what the rest needs)

26. Implement whatever Phase F's checkpoint approved. For anything requiring more protocol groundwork than this session can
    responsibly do (e.g. a new DLCI 0x02 field under ADR-036, which unblocked reading but implemented nothing), describe
    concretely what is needed (the exact `qhr` field, its FACT status, the UI it would need) rather than attempting it
    speculatively.

### Phase H — UI improvements

27. **Swipe between tabs.** Add left/right swipe gesture navigation across the Connection/ANC/EQ/Find/Debug tabs in
    `OpenControlNavHost.kt`, keeping the existing bottom-navigation bar in sync (a swipe updates the selected tab's highlight;
    a tab tap still works and updates the swipe position) — research the current Compose-idiomatic way to do this (e.g.
    `HorizontalPager` from `androidx.compose.foundation.pager`, already available via the Compose BOM already in
    `libs.versions.toml` — confirm the exact dependency situation rather than assuming) without breaking the existing
    single-top back-stack semantics `OpenControlNavHost.kt`'s own doc comment describes (a real, previously-fixed bug,
    `ai-sessions/0037`) or Debug's "never highlighted as primary" rule (`ARCHITECTURE.md` §2.4). Add tests where the new logic
    is pure/testable; note plainly what is Android-framework-only and therefore untested, per this project's established
    pattern.
28. **Replace "(last known)" with an actual timestamp.** `AncScreen.kt`'s "ANC mode (last known): X" (and any equivalent EQ/
    battery wording) should show *when* the value was last updated instead of the vague qualifier — prefer a fixed, absolute
    wall-clock time string over a ticking relative "N seconds ago" counter, since a ticking display would need a recomposition
    timer this project's architecture deliberately avoids (`ARCHITECTURE.md` §6, "nothing in this app runs a fixed-interval
    timer loop") — if you judge a relative counter is genuinely what the maintainer wants, ask rather than assume. Thread the
    timestamp through the same per-feature flows already carrying the value (`ancMode`, `eqProfile`, `batteryStatus`,
    `dockState`) without adding a new polling mechanism — the timestamp is recorded at the moment each value is actually
    received, nothing more.

### Phase I — verification, documentation, commit

29. `cd android && ./gradlew assembleDebug testDebugUnitTest test lint` must be clean — report exact test counts per module and
    the lint result, no new suppressions.
30. Update `TODO.md`, `CHANGELOG.md` (matching the `0034`–`0042` style), `ARCHITECTURE.md`, `PROTOCOL.md` (only approved
    promotions), `DECISIONS.md` (only approved ADRs, with a process note citing the chat approval, registered in
    `id_registry.csv`), `ai-sessions/INDEX.md`, and write this session's own `0043_FEATURE_RESULT_2026_09_22.md` per
    `AI_SESSION_LOG_PROCEDURE.md` §1/§4 — plain-language answers to every one of the maintainer's requests at the top, `Status`
    field per §4's three-value rule. Update `ai-sessions/0042`'s `Status` field in place per §4a if this session's checkpoint
    answers settle any of its still-open items, citing exactly how.
31. Give the maintainer re-test instructions for anything hardware-unverified, structured so each fix/feature is confirmed or
    refuted on its own (matching `ai-sessions/0042` §12's format).
32. **Commit and push once verified.** Run `git log origin/main..HEAD` first and push any unpushed earlier commit together with
    this one. Confirm `git lfs status`/`git check-attr` show the moved video/log files as LFS-tracked before pushing (Phase B
    task 6). Conventional Commits, a *why*-message (`PROJECT_RULES.md` rules 7/17), and the attribution line this environment's
    own instructions specify.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0043_FEATURE_PROMPT_2026_09_22.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0043_FEATURE_PROMPT_2026_09_22
