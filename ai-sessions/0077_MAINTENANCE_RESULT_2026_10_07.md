# 0077_MAINTENANCE_RESULT_2026_10_07.md — The maintainer's pending decisions: open proposals, two ADR candidates from 1.0.1, the tracked .pyc, and the Buds' state before the next run

**Number:** 0077
**Category:** MAINTENANCE
**Date:** 2026-10-07
**Title:** Prepare and ask every decision that waits only for the maintainer — desk-research proposals, older capture-FINDINGS proposals, ADRs for two 1.0.1 behaviours, untracking the .pyc, restoring the Buds' state
**Status:** awaiting maintainer sign-off — every decision answered and applied; the commit question is open

## Progress

- **Done:** Phase 0 — `git log -1` `6167843 Merge pull request #9 …` on `main`; `git status --short`: only this session's prompt untracked;
  `HEAD..origin/main` empty; branch `maintenance/0077-decisions` from `origin/main`.
- **Done:** Phase A (D1–D7 prepared, §A), Phase B (overview in chat; 13 questions in four `AskUserQuestion` calls, answers below), Phase C (approved edits
  applied; D6 untracked in the index, to be its own commit; footers; lint).
- **Next:** the maintainer's answer to the commit question; the Buds restore (D7) is the maintainer's phone step.
- **Touched, unverified:** none.
- **Intermediate results:** session scratchpad `f0077/`.

## Summary (plain language)

All seven waiting decisions are made. The three desk-research proposals (there were three, not four) are applied: a misleading sentence about a DLCI 0x08
battery message is corrected, two codes are added to that channel's description, and a field the official app's schema does not name is labelled "the Buds'
clock (🟡)". Of the seven older capture proposals, one is applied in reworded form (a Notify that says "not settable" always reads "Off"), one becomes a
`TODO.md` capture idea, and five are closed (done, superseded or out of scope). The two 1.0.1 behaviours now have their ADRs: **ADR-056** (which bonded Buds
the app controls) and **ADR-057** (when a value counts as current). The compiled `.pyc` is out of git (it stays on disk, ignored). Your Buds still have to be
set back to Balanced with Off unticked — a step for you on the phone, now in `TODO.md` and to be P7 of the next capture skeleton. Nothing in the app changed.

## Checkpoint answers (chat 2026-10-07, `AskUserQuestion`, verbatim)

| Header | Answer | Applied |
|---|---|---|
| "D1 Option E" | *"Dated Update toepassen (Recommended)"* | `PROTOCOL.md` §4.3 Option E Update |
| "D2 Codes" | *"Alleen het ontbrekende toevoegen (Recommended)"* | `PROTOCOL.md` §2.3 note |
| "D3 Wallclock" | *"Toepassen als dated Update (Recommended)"* | `REVERSE_ENGINEERING.md` Update at the schema table |
| "D4a CAP-008" | *"Afsluiten, §4 als pointer (Recommended)"* | `PROTOCOL.md` §6 Behavior note; `CAP-008-FINDINGS.md` §12 item 1 rewritten |
| "D4b CAP-026" | *"Afsluiten als achterhaald (Recommended)"* | `CAP-026-FINDINGS.md` §7 status line |
| "D4c CASE-008" | *"Naar TODO.md §3, hier afsluiten (Recommended)"* | `TODO.md` §3; `CAP-029-FINDINGS.md` §8 status line |
| "D4d CAP-037" | *"Geherformuleerd toepassen (Recommended)"* | `PROTOCOL.md` §4.1 note; `CAP-037-FINDINGS.md` §7 status line |
| "D4e CASE-009" | *"Afsluiten als gedaan (Recommended)"* | `CAP-047-FINDINGS.md` §9 status line |
| "D4f DLCI 0x0a" | *"Afsluiten als achterhaald (Recommended)"* | `CAP-047-FINDINGS.md` §9 status line |
| "D4g CASE-007" | *"🔵 houden, voorstel sluiten (Recommended)"* | no `TESTPLAN` change; recorded here |
| "D5 ADRs" | *"Twee ADR's nu (Recommended)"* | `DECISIONS.md` ADR-056, ADR-057 (the preview's text); `id_registry.csv`; `ARCHITECTURE.md` pointers; `TODO.md` §1 closed |
| "D6 .pyc" | *"Nu uit git halen (Recommended)"* | `git rm --cached scripts/__pycache__/lint_docs.cpython-314.pyc` (file on disk, `git check-ignore` → `.gitignore:48`); `TODO.md` §6 item removed |
| "D7 Buds" | *"Nu terugzetten én als P7 opnemen (Recommended)"* | `TODO.md` §4 item replaced by the step list (**M**) |

Also: `DESKRESEARCH_FINDINGS.md` — a dated line under the 2026-10-03 proposals (a/b/c applied); `PROTOCOL.md` — a history row. One count in my preparation
was corrected before it was asked: `CAP-037` has 17 (not 16) Notifies with Settable `00`, all Current `20` (`tshark -r CAP-037-btsnoop_hci.log -Y
"btrfcomm.dlci==4 and btrfcomm.len>0" -T fields -e data.data | grep -o "0813000401e8.\{4\}" | sort | uniq -c` → 17 × `0020`, 10 × `e880`; exit 0).

## A. The decisions, prepared (Phase A)

**Count of the desk-research proposals:** the entry "2026-10-03 — Three cross-capture checks …" (`DESKRESEARCH_FINDINGS.md:882–973`) lists **three** —
(a), (b), (c) at line 970. The entries of 2026-10-04 and 2026-10-06 add none ("Promoted to: nothing promoted"; the 2026-10-06 census has no proposal line).
The earlier chat's "four" was wrong.

### D1 — (a) `PROTOCOL.md` §4.3 Option E: "cross-confirms the Right value"
- **What:** `PROTOCOL.md:1360–1361` says the DLCI 0x08 message `Group 0x04 Code 0x03` "cross-confirms the Right value at all 4 of `CAP-011`'s occurrences". The
  desk check (`DESKRESEARCH_FINDINGS.md` 2026-10-03 item 1) found over all captures that its field 3 equals the **lower** of the two bud levels wherever they
  differ — 106 of 118, never the higher one (63 times that is Left, 43 Right); 12 "neither" carry the previous triple's lower value. In `CAP-011` Right
  happened to be the lower bud. A dated pointer already sits right after the sentence (`:1362`, 2026-10-03, 🟡, no status change).
- **Why it waits:** a change of a `PROTOCOL.md` text inside a 🟢 entry (ADR-014's Option E) — `AGENTS.md` §6.
- **If yes:** a dated Update under Option E: the 2026-10-03 sentence's "cross-confirms the Right value" is superseded by "equals the lower of the two bud
  levels (🟡 HYPOTHESIS, 106 of 118, `DESKRESEARCH_FINDINGS.md` 2026-10-03 item 1)"; the original sentence stays (rule 9a). **If no:** the pointer stays the
  only correction.
- **Consequence for the app:** none — the app does not open DLCI 0x08 (ADR-039 Update, ADR-043).
- **Advice:** apply as a dated Update — the current text states something the evidence contradicts; the pointer exists but the misleading sentence is
  what a reader sees first.

### D2 — (b) Codes `0x03` and `0x05` in `PROTOCOL.md` §2.3's DLCI 0x08 table
- **What:** the proposal: add both Codes with their 🟡 readings. **Partly done since:** `PROTOCOL.md:577–581` ("GSND CONTROL in `CAP-069`", 2026-10-04,
  signed off in chat) already lists Code `0x05` = wear state (🟡 `03` no bud worn, `04` one, `06` both; 🔴 `05`), Code `0x16`, Code `0x14`. Code `0x03`
  (field 3 = lower bud level, 🟡) is **not** in §2.3 — it appears only in Option E's pointer. The 2026-10-03 table of `04 05` values against Settable (1, 3, 4,
  5, 6) adds a value `1` (24 samples) and `0` (1) that §2.3 does not mention.
- **If yes:** a dated line in that §2.3 paragraph: "Code `0x03` field 3 = the lower of the two bud levels (🟡, `DESKRESEARCH_FINDINGS.md` 2026-10-03 item 1);
  Code `0x05` also takes `01` (24 samples, with in-ear detection off — `PROTOCOL.md` §6, 2026-09-28) and once `00` (`CAP-031` 983)". **If no:** close (b) as
  superseded by the 2026-10-04 paragraph.
- **Consequence for the app:** none.
- **Advice:** add only the Code `0x03` line and the two extra `0x05` values, and mark (b) otherwise superseded — the rest is already there.

### D3 — (c) field 2 = "wall clock, ms (🟡)" in `REVERSE_ENGINEERING.md`
- **What:** in the runtime-info (`SubscribeRuntimeInfo`) and software-info (`GetSoftwareInfo`) answers, top-level field 2 is within 52–548 ms before the
  frame's capture time in three official-app captures and absent in OpenControl's (2026-10-03 item 3). The app 1.0.955078536's schema does not have fields
  2/3 (`qiy` fields 4–7, `REVERSE_ENGINEERING.md:2463`; `PROTOCOL.md` §4.3 Option F note of 2026-10-06). **New check this session:** `CAP-070` (OpenControl
  1.1.0): 0 of 133 runtime-info packets and 0 of 22 software-info answers carry field 2, 0 `SetWallclock` (`grep -c` over the `pwrpc_decode.py --handle
  0x000b` output of Z, A, B); positive control `CAP-036-btsnoop_hci.log`: 8 runtime-info packets with field 2, 2 `SetWallclock` (exit 0).
- **If yes:** a dated Update at the `qiy` row (`REVERSE_ENGINEERING.md:2463`) and the `qjb` entry: "the wire carries a top-level field 2 the schema does not
  name — 🟡 the Buds' wall clock in ms since the epoch, present only after a `SetWallclock` (`DESKRESEARCH_FINDINGS.md` 2026-10-03 item 3; `CAP-070` 0/155
  without one)". **If no:** stays in the desk entry only.
- **Consequence for the app:** none (it reads fields 4, 6, 7).
- **Advice:** apply — it is a naming note with a clear 🟡 label, and `CAP-070` adds a fourth OpenControl sample.

### D4 — the older proposals from capture FINDINGS
| Item | What it proposes (quoted at its source) | Since then | Advice |
|---|---|---|---|
| `CAP-008` §12 item 1 | "Promote §5 (eSCO/mSBC establishment) and §4 (`CALL-001` wire/video correlation) to `PROTOCOL.md`" | nothing new; `PROJECT.md` Non-goals: "identifying precise audio codec parameters … is out of scope research" | **close**: §4 as a pointer line in `PROTOCOL.md` §6 at most; §5 (codec) out of scope |
| `CAP-026` §7 item 3 | "ADR-014's short/no-flag-form-Case-value hypothesis — this session's own reading (95 vs. on-screen 93%) proposed as a fourth supporting data point" | the app takes the Case from Option F since ADR-043 (2026-09-24), not DLCI 0x08 | **close as superseded** (no app relevance); optionally one dated line in ADR-014 |
| `CAP-029` §8 item 3 | "`CASE-008`'s own open item — unchanged, still needs a dedicated attempt" | still untested (`TESTPLAN` row `CASE-008` 🔴) | not a text proposal — **move to `TODO.md` §3** as a capture idea, close here |
| `CAP-037` §7 item 3 | "The Settable-toggles↔Current-state co-occurrence pattern (§3) is proposed as a new 🟡 HYPOTHESIS entry for `PROTOCOL.md` §4.1" | ADR-049 (2026-09-30) made `00` ⇔ not worn the 🟡 reading; in `CAP-070` all 19 `00` Notifies read Current `20` (Off) and the 4 `e8` read `80`/`08` | **apply reworded**: a dated 🟡 note in §4.1 "a `Notify` with Settable `00` has carried Current `0x20` (Off) in every sample checked (`CAP-037` 17/17, `CAP-070` 19/19; `tshark … -Y "btrfcomm.dlci==4 and btrfcomm.len>0" -e data.data | grep -o "0813000401e8.\{4\}" | sort | uniq -c`)" |
| `CAP-047` §9 item 4 | a new Test-ID for swapped-slot docking | **done**: `CASE-009` registered 2026-09-18 (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md:187`) | **close as done** (the FINDINGS' status line is stale) |
| `CAP-047` §9 item 5 | DLCI 0x0a trigger candidates 1 (app back/foreground) and 2 (scheduled sync) | `CAP-069` §3: the DLCI 0x0a waves are assistant sessions opened by `01 09 … 0a 01 03` (3 of 3, 🟢 for `CAP-069`) | **close as superseded** |
| `CASE-007` 🔵 → 🟢 (`A68-SES-03`) | turn the existence icon of the factory reset to 🟢 | the icon column is the *existence source*: 🟢 = "seen directly in the project's own app screenshots", 🔵 = official (`TESTPLAN:63–72`); the capture proof is already in the Evidence cell (`CAP-029` §3) | **keep 🔵, close** — a 🟢 would misuse the legend |

### D5 — ADRs for the two 1.0.1 behaviours
- **(a) Device choice** (`ARCHITECTURE.md` §8 item 8, built `ai-sessions/0069` A68-APP-04/05, the maintainer's checkpoint 14 *"No silent pick + 'Use different
  Buds' (Recommended)"*, 2026-10-03): the device is the one of this app's CDM association; without one a single bonded "Pixel Buds" device; with two or more
  none is picked (`SeveralBudsPaired`) and Android's picker decides; "Use different Buds" removes the app's associations, the Bluetooth bond stays; no
  address stored, no in-app list. On hardware: `CAP-068` S7/S8 (§7). Untested: S9 (two Buds).
- **(b) "Current value" rule** (`ARCHITECTURE.md:269–275`, checkpoint 13 *"Keep the value, mark it (Recommended)"*): a value is current only while `Ready`
  and reported on this connection (`isCurrent`, `ValueCurrency.kt`); otherwise visible, dimmed, "from the last connection"; never read → "—" (1.1.0: also
  switches). On hardware: `CAP-068` S1, S2, S4; `CAP-070` "—" on film.
- **Why it waits:** `PROJECT_RULES.md` rule 8 ("significant architecture choices … are recorded in `DECISIONS.md` **before** they are implemented broadly") —
  they were built on chat answers, recorded only in `ARCHITECTURE.md` and the 0069 RESULT; `AGENTS.md` §6: no ADR without the maintainer. "Not now" was
  chosen on 2026-10-03 and 2026-10-04 (`TODO.md` §1).
- **Next free numbers:** ADR-056, ADR-057 (`id_registry.csv` ended at ADR-055; `grep "^## ADR-05" DECISIONS.md` ended at ADR-055).
- **Advice:** ADR now, two separate ADRs (each touches a different layer and may be superseded separately); written as a record of what was decided and
  built, dated to today with the original decision date in Context.

### D6 — `scripts/__pycache__/lint_docs.cpython-314.pyc`
- **Facts:** tracked since `235de00` (2026-09-24, "chore(scripts): make lint_docs.py pass …"; also touched by `dcc83fb`); `.gitignore:48` already has
  `**/__pycache__/`, which does not untrack a file already in the index; Python rewrites it whenever `lint_docs.py` runs without `PYTHONDONTWRITEBYTECODE=1`, so
  every session's prompt carries "`git checkout` it if it changed"; nothing reads it (`grep -rn "pycache\|\.pyc" .github/ scripts/*.py scripts/*.sh` → no
  match).
- **If yes:** `git rm --cached scripts/__pycache__/lint_docs.cpython-314.pyc` in its own commit; the file stays on disk and is ignored from then on; later prompts
  can drop the "`git checkout` the .pyc" line. **If no:** the workaround stays.
- **Advice:** untrack now — a compiled artefact in git is noise and a trap for every session; the deletion is index-only and reversible.

### D7 — the Buds' state before the next run
- **Now (last known, `CAP-070`, 2026-10-07):** EQ field 16 = `[−1.0, 0.0, 4.0, 2.0, 3.72]` (band order Low bass, Bass, Mid, Treble, Upper treble —
  `PROTOCOL.md:863`; the first four = the Vocal boost preset, Upper treble dragged in BF-21; `CAP-070-btsnoop_hci2.log` B428); field 12 = `{1:1 2:1 3:1 4:0}` =
  Noise cancellation, **Off** and Transparency ticked, Adaptive not (bit order 1 NC / 2 Off / 3 Transparency / 4 Adaptive, `PROTOCOL.md` §4.5.3 🟢; film
  06:21:16).
- **Target ("P7" of the skeletons):** preset **Balanced** `[-3.5, 0.5, 1.0, -1.0, 2.5]`, mode list with Off unticked → `{1:1 2:0 3:1 4:0}`.
- **How (the maintainer, with OpenControl 1.1.0, Buds connected, at least one bud worn or out of the case):** (1) Sound → tap preset **BALANCED** → the app
  writes `WriteSetting 4:{16:[-3.5, 0.5, 1.0, -1.0, 2.5]}` — the same quintet is a real frame in `CAP-015` 2303 (ch 19) and `CAP-059` 2188 (ch 21, OpenControl);
  the sliders show −3,5 / 0,5 / 1,0 / −1,0 / 2,5 and the Equalizer (i) "changed HH:MM:SS". (2) Controls → Press and hold → untick **Off** → `4:{12:{1:1 2:0
  3:1 4:0}}` — on channel 21 the real frame `CAP-041` 2198 (`Settings056.WRITE_TWO_LEFT_CH21_2198`); the list keeps two ticked, which the app allows (it
  refuses fewer than two). (3) Confirm: Disconnect, Connect, and check that the values come back "read HH:MM:SS".
- **Advice:** do it before the next capture, and also write it as P7 of the next skeleton — a known start state makes the next run's reads comparable.

## Files read

In full: `AGENTS.md` (system context), `PROJECT_RULES.md`, `PROJECT.md`, `AI_SESSION_LOG_PROCEDURE.md`, the prompt, `DESKRESEARCH_FINDINGS.md` entries of
2026-10-03 and 2026-10-04, ADR-055. In part: `PROTOCOL.md` (§2.3 lines 468–600, §4.1 end, §4.2 preset table, §4.3 Option E and F, §6 headings), `ARCHITECTURE.md`
(§3.1 "One rule", §8 item 8), `REVERSE_ENGINEERING.md` (the schema table at 2455–2480, `grep` for wall clock), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (legend 45–72,
rows `CASE-007`…`009`), `CAP-008-FINDINGS.md` §11–§12, `CAP-026-FINDINGS.md` §7–§8, `CAP-029-FINDINGS.md` §3, §8–§9, `CAP-037-FINDINGS.md` §3, §7–§8,
`CAP-047-FINDINGS.md` §9–§10, `ai-sessions/0069` RESULT (checkpoint 13/14 rows), `ai-sessions/0068` RESULT (`A68-SES-03`), `ai-sessions/0059` RESULT
(`CASE-007` line), `TODO.md`, `id_registry.csv` (ADR rows), `.gitignore`. **Not read in full, against the prompt's list:** `ARCHITECTURE.md`, `PROTOCOL.md`,
`DECISIONS.md` (every ADR), `REVERSE_ENGINEERING.md` — the edits are dated notes at located places; a later restructure session reads them whole.

## Deferred documentation

Each item is also in `TODO.md`:

- **M** Restore the Buds (Balanced, Off unticked) before the next run and write it as P7 of the next skeleton. (`TODO.md` §4)
- `CASE-008` as a capture idea. (`TODO.md` §3)
- Later session prompts can drop the "`git checkout` the tracked `.pyc` if it changed" line. (This RESULT only — no task in `TODO.md`.)

## Commits

Not committed yet — the commit question is open (task 10).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0077_MAINTENANCE_RESULT_2026_10_07.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0077_MAINTENANCE_RESULT_2026_10_07
