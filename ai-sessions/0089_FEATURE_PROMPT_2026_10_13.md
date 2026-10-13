# 0089_FEATURE_PROMPT_2026_10_13.md — The noise-control channel in the Play user after CAP-075: a constant 3-s re-claim step with a longer chain, a "Take it back again" action on the hint card, a cause label for a mode learned by a re-claim's read — and a clearer switch (a better name, options that say whether the phone has Google Play services)

**Number:** 0089
**Category:** FEATURE
**Date:** 2026-10-13
**Title:** Build the follow-up of `CAP-075` (`ai-sessions/0088`, the maintainer's choice in chat 2026-10-13, "FEATURE" → "P1 + P2 + P4"): in the
"Take it back" mode the app re-claims the noise-control channel **3 s after every cut, for a longer chain** (no 6/12-s back-off — Play's timers do not
change, so the back-off only lengthens the dead time), the hint card gets a **"Take it back again"** action that restarts the chain without a mode write,
and a mode the app learns from **its own re-claim's read** gets a cause line (today it shows no cause). At the same time the switch becomes
understandable for a user who has never read a capture: **a better card name** (the maintainer's example: "Channel control"), **options that say
whether the phone has Google Play services**, and plainer texts on the card, its (i), the hint line and the ANC (i). Real capture bytes as fixtures, a
green gate with mutation checks, a skeleton for the hardware run (`CAP-076`), the documents, the release preparation (no tag, no publication).

---

## 0. How to use this prompt

You are an expert software architect, Android/Kotlin engineer, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in
this repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action, read **in full**, in this order: `AGENTS.md`,
`PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md` (in particular §2.4, §3.1's ANC row, §3.2 the timing constants, §5a's "Changed by the Buds" row, **§6.0b**
with its paragraphs of 2026-10-10, 2026-10-12 and 2026-10-13, §7, §12), `PROTOCOL.md` (§2.3, **§4.1 with its notes of 2026-10-12 and 2026-10-13** — Play's
timers and the stack's refusal are 🟢 there, §8's last rows), `DECISIONS.md` (**every** ADR, ADR-001 … ADR-062 with every dated Update — in particular ADR-032
with its Update of 2026-10-12, ADR-044, ADR-048, ADR-061 with its Updates, **ADR-062 with its Update of 2026-10-13**; the draft ADR-060 is not a decision),
`TODO.md` (§1, §2, **§4's `CAP-075` item**, **§5's "Next FEATURE prompt (0089)" item**, §6). Then: `AI_SESSION_LOG_PROCEDURE.md` (§4b, §9),
`ai-sessions/INDEX.md`, `RELEASING.md` (the Release checklist, "Repository rules", §4–§8, §11a, §13), `APP_TESTPLAN.md` (§X and the Summary), `CHANGELOG.md`
(`[1.2.1]`, Known issues), `README.md` (status, known issues), `id_registry.csv`, and:

- **`ai-sessions/0088_CAPTURE_PROMPT_2026_10_12.md` and `ai-sessions/0088_CAPTURE_RESULT_2026_10_12.md`** — the analysis this build answers and the maintainer's
  answers (the "Checkpoints" table, verbatim);
- **`captures/CAP-075-2026-10-12_17-22-31_2026-10-13_06-34-32-Group_BK/CAP-075-FINDINGS.md`** — **§1 (Play's two timers: 18 + 15 values)**, §2 (the Connect
  reads), **§4 (the 31 holds of set 2, one row each; the cycle lengths 9.2 / 12.0 / 18.2 s and the 43 % coverage)**, **§5 (the stack path of each cut, 15 of
  15)**, §8 (the four-way comparison), **§9 (the proposals P1 … P7 with their evidence — P1, P2 and P4 are this build; P3 and P5 are not; P6 was rejected on
  evidence)**, §10, §12; and `CAP-075-EVENT-NOTES.md` (the set-2 timeline — what the user saw at each re-claim; the steps as run; the appendix's reference
  frames);
- `captures/CAP-074-2026-10-12_06-40-16_06-45-31-Group_BJ/CAP-074-FINDINGS.md` §4, §8, §9 (where the design of ADR-062 came from) and
  `captures/CAP-073-2026-10-10_08-23-35_08-33-24-Group_BI/CAP-073-FINDINGS.md` §4 (the user without Play — the behaviour that must not change);
- `ai-sessions/0087_FEATURE_PROMPT_2026_10_12.md` and `ai-sessions/0087_FEATURE_RESULT_2026_10_12.md` — how ADR-062 was built (§A the design, §B the
  checkpoint answers — the texts, the default, the delays —, §D the mutation checks M1–M7);
- **Every Kotlin source this build touches, in full, before changing it** — at least: `BudsRepositoryImpl.kt` (`onHeldClaimTaken` ≈ `:525`,
  `startReclaimChain`, `cancelReclaim`, `reconcileHold`, `endHold`, the `transport.channelClosed` collector, `causeOfNotify` ≈ `:363`, `emitAncMode`,
  `refreshAncMode`, the constants ≈ `:1776` — `HOLD_RECLAIM_DELAYS_MS = listOf(3_000L, 6_000L, 12_000L)`), `BudsRepository.kt` (`ancChannelTaken`,
  `ancModeCause`), `HoldReclaimMode.kt` (`HoldReclaimMode`, `ChannelTaken`), `AncModeCause.kt` (`READ`, `SET_BY_APP`, `CHANGED_BY_BUDS`),
  `HoldReclaimSettingsStore.kt` (the key `hold_reclaim_mode`, the stored names), `AncScreen.kt` (`ANC_HOLD_HINT_TEXT` ≈ `:203`, `ancChannelTakenLine` ≈ `:213`,
  `ancChangedByBudsLine` ≈ `:236`, `ancDetailLines`), `SettingsMenu.kt` (`HOLD_RECLAIM_TITLE`, `HOLD_RECLAIM_INTRO`, `holdReclaimLabel`,
  `HOLD_RECLAIM_DETAIL_LINES`, `HoldReclaimCard` ≈ `:213–260`), `ConnectionBanner.kt` (`MESSAGE_STREAM_HINT_TEXT`), `OpenControlNavHost.kt`
  (`OpenControlUiState.holdReclaimMode`, `ancChannelTaken`, `OpenControlActions.onHoldReclaimModeChanged`), `MainActivity.kt` (the settings-store wiring),
  `BudsRepositoryImplTest.kt` (the hold and re-claim tests of `ai-sessions/0084`/`0087`), `Cap074Fixtures`, `FakeBudsTransport`, `AncScreenTest.kt`,
  `SettingsMenuTest.kt`, `UserMessageTest`'s pattern, `android/app/build.gradle.kts`.

Say in the RESULT what was read in full and what in part.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5).** Create **ai-sessions/0089_FEATURE_RESULT_2026_10_13.md** at the very start with
`**Status:** partial — resumed` and a **Progress** block; update it at the end of every phase and after every substantial step (each file changed, each test class
green, each gate run). A resumed session reads this prompt, then the Progress block, re-reads the files the unfinished step touched, and continues; it never redoes a
recorded step and never assumes an unrecorded one.

**Memory and the machine.** 15 GB laptop. Never two Gradle builds at once; run mutation checks one at a time with `--max-workers=2` and check for leftover
mutations after a crash (the memory "Gradle OOM on mutation runs"). In `BudsRepositoryImplTest` use `runCurrent()`/a settle helper for `backgroundScope`
collectors, not `advanceUntilIdle()` alone (the memory "Repo tests: runCurrent"). Never `git worktree add` a commit (it checks out `captures/`, gigabytes) and
never run `scripts/ensure_footers.py` from inside a worktree; never a bare `git stash` (the memory "lint_docs: leftover worktrees"). `android/domain/bin/` is
build output: never stage it.

**Nothing is taken on trust**, including this prompt, the 0088 RESULT and the ADR texts: re-derive every frame number, delay and code location you rely on from
`CAP-075`'s logs and the sources.

---

## 1. What the maintainer asked for (chat, 2026-10-13)

1. The finding (`CAP-075`, `ai-sessions/0088`, 🟢): with Google Play services, "Take it back" ran three chains of exactly three re-claims scheduled 3 000 / 6 000 /
   12 000 ms after the cut; **every re-claim collided** with Play's retry (2.1–2.8 s after the cut: attempt 1 refused by the stack, attempt 2 connecting 0.9–1.5 s
   later by killing Play's socket), so each re-claim reached the wire 4.0–4.8 s after the cut; every re-claimed hold was cut again **5.3–5.8 s after Play's loss**
   (4.55–4.92 s after its `SABM`). The held window is the same in every cycle; the cycles were 9.2–9.5 s, 12.0–12.4 s and 17.7–18.2 s, so the coverage fell
   from ≈ 51 % to 39 % to 26 % per cycle — ≈ 43 % of a 44-s chain, then 0 % until a tap. **Play's timers did not change over 15 cycles: the back-off buys
   nothing.** 8 of 31 holds were on screen within a second (5 unprovoked `Notify` frames on the app's claim, 3 read by a re-claim's own `Get`); a mode learned by a
   re-claim's read shows **no cause line** in the ANC (i) (film 2 s265–269). Without Play both modes were identical (41 of 41 holds shown). The maintainer restarted
   a chain with **Refresh** (06:33:37) — there is no action meant for that.
2. The maintainer's choice at `ai-sessions/0088`'s Phase D ("FEATURE" → *"P1 + P2 + P4 (Recommended)"*): **P1** a constant 3-s step and a longer chain (capped by
   count or time, then the hint); **P2** a "Take it back again" action on the hint card; **P4** a cause label for a mode learned by a re-claim's own read. Not
   chosen: P3 (a shorter attempt-2 latency), P5 (the mode on the app's own channel — `TODO.md` §4's lead), P6 (rejected on evidence), P7 (done: the CHANGELOG text).
3. **This prompt's request (chat 2026-10-13, the maintainer, Dutch):** *"Geef de switch een betere naam (bijvoorbeeld Channel control), geef in de opties aan dat
   het om wel of niet play services gaat en zorg er voor dat het voor de gebruiker duidelijker te begrijpen is."* — give the switch a better name (for example
   "Channel control"), say in the options whether it is about a phone **with or without** Google Play services, and make it clearer for the user.
4. "Commit and push only after I confirm" (standing practice); no tag, no release (`RELEASING.md`: publishing is the maintainer's own act).

---

## 2. The items (verify each against `CAP-075`, the ADRs and the code before building)

### Item 1 — P1: a constant 3-s re-claim step with a longer chain (the main item)

Today (`onHeldClaimTaken`, ADR-062): after a foreign close of the held claim in mode `RECLAIM`, one bounded delay from `HOLD_RECLAIM_DELAYS_MS` (3 / 6 / 12 s),
then the existing claim (`refreshAncMode`, the `Get` first); after the third re-claim's cut the hint "…; the app took it back 3 times and stopped." and nothing
until a tap, a pull, Refresh, a re-entry of the tab, the app's return or a Connect (`startReclaimChain`). **New:** the delay is **the same after every cut** (3 s —
every re-claim finds Play back on the channel, since Play's retry lands 2.1–2.8 s after the cut: attempt 1 is refused, attempt 2 takes the channel; `CAP-075`
§4/§9 P1) and **the chain is longer**, capped **by count or by time** — the checkpoint decides (candidates with their evidence: a count of 20 ≈ 3 minutes of
9.3-s cycles; a time of 3 minutes from the chain's start; "while the tab is shown" without a cap is **not** a candidate — ADR-032/ADR-044 require a cap). After the
cap: the hint with the count ("took it back N times and stopped") and Item 2's action. Everything else of ADR-062 stays: never in the background, never after a
session loss, never when the tab or the app was left, a pending re-claim cancelled by the tab, the app's visibility, the session, the setting; the chain restarted
by a tap, a pull, Refresh, a re-entry, the app's return, a Connect. **Nothing new on the wire** (`SABM` DLCI 0x04, `08 11 00 00`, the phone's `DISC`); the
always-on log line per re-claim keeps its form ("Message Stream re-claim N/M after a foreign close, D ms later (ADR-062)" — M = the cap, or the elapsed time),
payload-free (`AGENTS.md` §9). Expected on hardware: ≈ 4.6–4.9 s held of every ≈ 9.3 s for the whole chain (≈ 50 %), against 43 % for 44 s today.

**Design questions for the checkpoint (one each):** the cap (count / time / both, and the value); whether the first step stays 3.0 s or becomes 3.0 s after
the **cut** as today versus 3.0 s after the **close line** (today's measure — keep it; say so); the hint text after the cap; whether the chain also restarts when
the user **switches the setting to "Take it back" with the tab shown** (today a switch cancels a pending re-claim and nothing starts one — `CAP-075-EVENT-NOTES.md`
06:32:26: the maintainer re-entered the tab to get a claim).

**ADR:** the constant step and the new cap change ADR-062's Decision (3 / 6 / 12 s, three times). Draft an **ADR-062 Update 2** ("Update 2026-10-xx: the constant
step and the longer chain, after `CAP-075`") — or, if the switch's new model of Item 4 changes what the setting *means* (a phone with / without Play services
rather than "what to do when another app takes the channel"), a **new ADR-063** that supersedes ADR-062's Decision items 1–4 and keeps its Context/Options: propose
which at the checkpoint, with the text. **Write nothing into `DECISIONS.md` before the maintainer's answer in this chat** (`AGENTS.md` §6).

### Item 2 — P2: a "Take it back again" action on the hint card

When `ancChannelTaken` is set (the hint line on the noise-control card: `ancChannelTakenLine`), the card gets **a text button** (Material 3, next to or under
the hint) that starts a new chain: `startReclaimChain()` + the existing claim with its `Get` (`refreshAncMode` — the "a tap, a pull or re-entering" path of
`BudsRepository`), **without a `Set`** (a mode tap would write the mode the user did not mean) and **without the battery reads** of Refresh. In mode
`HINT_ONLY` the same button reads the mode once ("Read again") — or is hidden: the checkpoint decides the label(s) and whether one button serves both modes.
Visible only while the hint is shown; disabled while the session is not `Ready`. One always-on log line ("Message Stream claim by the hint card's action
(ADR-062 …)"). A `:ui` test pins the label and the click → `OpenControlActions` call; a `:data` test shows the chain counter reset and one claim, no `Set`.

### Item 3 — P4: a cause label for a mode learned by a re-claim's own read

Today `causeOfNotify` labels the answer to the app's own `Get` as `AncModeCause.READ` even when the mode differs from the last mode the app knew — the ANC (i)
then shows "ANC mode: TRANSPARENT (updated 06:33:21)" with no cause line (`CAP-075` film 2 s265–269; `CAP-075-FINDINGS.md` §4 rows 1, 5, 22, 25, 27, 28, 30
are such reads). **New:** a `Get` answer that differs from the last known mode of this session, with no `Set` pending, gets a new cause (e.g.
`AncModeCause.CHANGED_SINCE_LAST_READ`) and an (i) line such as *"Changed since the app last saw it (read at HH:MM:SS) — a press-and-hold on a bud, the Buds'
own change, or a change made while another app had the channel."* — the wording is a checkpoint question. The first read of a session (`_ancMode.value == null`)
stays `READ`; the app's own `Set` stays `SET_BY_APP`; an unprovoked `Notify` stays `CHANGED_BY_BUDS`. Fixture: `CAP-075-btsnoop_hci2.log` 2980 (the re-claim's
`Get` answered `… e8 e8 80` after `… 40`), 5102 (`08` after `40`), 5361 (`80` after `08`) — re-derive with `tshark -r <log> -Y "frame.number==N" -T fields -e
data.data`. Tests in `:data` (the cause per case) and `:ui` (the line pinned as a literal).

### Item 4 — The switch, renamed and explained (the maintainer's request; checkpoint)

Today (`SettingsMenu.kt`): the card **"Noise-control channel"**, the intro "When another app takes the channel:", the options **"Show a hint, do not take it
back"** (the default) and **"Take it back while the noise-control tab is open (may fight that app)"**, four (i) lines; the ANC (i)'s last sentence points to
"the Settings tab under Noise-control channel"; the hint line names "another app … (for example Google Play services' Fast Pair)". A user who has not read
`CAP-074`/`CAP-075` cannot tell what "the channel" is or which option is for them.

**New — propose at the checkpoint, with the exact texts in the preview, one question per text:**
- **The card's name:** candidates "Channel control" (the maintainer's example), "Google Play services", "Noise control with Google Play services",
  "Press-and-hold with Google Play services" — recommend one and say why (the name must say what the user is choosing about; "Channel control" is short but
  still names the channel — pair it with an intro line that explains it in one sentence).
- **The options say whether the phone has Google Play services** — for example:
  *(•) "This phone has no Google Play services — nothing else uses the Buds' noise-control channel; if something does, show a hint"* (the default; today's
  `HINT_ONLY`) and *( ) "This phone has Google Play services — while the noise-control tab is open, take the channel back from Play services' Fast Pair every
  3 seconds, so a press-and-hold on a bud shows about half of the time"* (today's `RECLAIM`). The stored names `HINT_ONLY`/`RECLAIM` and the DataStore key
  **stay** (a stored value of 1.2.1 must keep its meaning — `HoldReclaimMode.fromStored`); only the labels, the title, the intro and the (i) change — unless the
  checkpoint decides on a third option (see below).
- **The intro and the (i)** in plain words: what the channel is ("the Buds tell one app at a time when a bud's press-and-hold changed the noise control"), what
  Play services does (takes it back 5–6 seconds after it lost it, every time — measured with one Play version on 2026-10-13, `CAP-075`), what each option does
  for the user (the hint; the re-claim every 3 s with its ≈ 50 % and its tug of war; the other app's own view of the Buds may lag meanwhile), and that without
  Play services both options behave the same.
- **The hint line and the ANC (i)** reworded to the same vocabulary (the card's new name in the pointer sentence; "Play services' Fast Pair" named as the usual
  taker, "another app" otherwise).
- **Optional, propose only if it is clean:** a sandboxed Google Play on GrapheneOS is an ordinary app (`com.google.android.gms`, uid 10205 in `CAP-074`/`075`).
  The app could **suggest** the right option by checking whether that package is installed (`PackageManager.getPackageInfo`, with a `<queries><package
  android:name="com.google.android.gms"/></queries>` manifest entry — no permission, no `com.google.android.gms.*` import, no network: `AGENTS.md` §1's ban is on
  the GMS libraries and the network, not on asking the package manager whether a package exists — **say this reading at the checkpoint and let the maintainer
  decide; it is not approved by this prompt**). If chosen: the card shows "Google Play services: found / not found on this phone" as information, the default
  stays the maintainer's choice, nothing is read from Play. A `<queries>` entry is a manifest change — justify it in the manifest comment and the PR (`AGENTS.md` §2).
- **The README/CHANGELOG/release-notes texts** for the new version, in the same plain words.

### Item 5 — Tests, mutations, the hardware-run skeleton `CAP-076`

**Unit tests (real bytes, `AGENTS.md` §11):** with `FakeBudsTransport` and virtual time — (a) mode `RECLAIM`: a foreign close → `openChannel` exactly 3 000 ms
later (not before), again 3 000 ms after the next close, … up to the cap, then no claim and the hint with the count; the tab left / the app hidden / a session
loss / a Disconnect / the switch to `HINT_ONLY` during a delay → no claim; (b) the hint card's action → one claim, no `Set`, the counter reset, a new chain after
the next close; (c) the cause of Item 3 per case (first read, a differing read, an own `Set`, an unprovoked `Notify`), with `CAP-075` bytes; (d) the stored names
unchanged (`fromStored("RECLAIM")`, `fromStored("HINT_ONLY")`, an unknown value → the default); (e) `:ui`: the card's title, intro, labels and (i) lines pinned as
literals, the hint line and the new cause line pinned, the button's visibility per state. The tests of `ai-sessions/0084`/`0087` keep passing (adjusted only
where the delays changed — say which).

**Mutation checks** (one at a time, `--max-workers=2`): (M1) the second re-claim uses 6 s → a test fails; (M2) the chain continues past the cap → a test fails;
(M3) the hint card's action writes a `Set` → a test fails; (M4) the action does not reset the counter → a test fails; (M5) a differing `Get` answer labelled `READ`
→ a test fails; (M6) a re-claim while the tab is not shown → a test fails. Revert each; `git diff` clean afterwards.

**The skeleton `CAP-076` (Group BL — verify both are free in `id_registry.csv` and `CAPTURE_BLUETOOTH_HCI_SNOOP.md`):** in `CAP-075`'s skeleton layout (the
appendix of its EVENT-NOTES; reference frames re-derived from `CAP-075-btsnoop_hci2.log`: the cut 2369, the re-claim's `SABM` 2502 after the refused attempt,
Play's retry 2401, the shown hold 3006, a re-claim's changed read 2980), **both users, ≤ 20 minutes on film with sound**: the Play user — the renamed card and
its (i) on film, mode "Take it back" with holds every ≈ 4 s for **3 minutes** (the whole chain: re-claims every ≈ 9.3 s, ≈ 50 % shown, the stop at the cap, the
hint with the count), the hint card's action (a new chain, no `Set` on the wire), the cause line after a re-claim's changed read, the switch to the other option
with the tab shown; the user without Play — the `CAP-073` §4 regression in both options (no re-claim, no hint, every hold shown) and the renamed card. P0 with
Play's *Nearby devices* permission state **on film** (`TODO.md` §3's standing item — not done in `CAP-075` either), the system log with the GrapheneOS log
viewer right after the chain (its main buffer covered every cut in `CAP-075`), the APK pull. Test-IDs: `TOUCH-007`, `ANC-001`…`ANC-004`, `PAIR-003`, `BATT-004`,
`FW-003`; `APP_TESTPLAN.md` gains a section **Y** and its Summary row.

---

## 3. Decisions already taken, and what still needs the maintainer

Taken (do not reopen): P1 + P2 + P4 as the scope (the maintainer, chat 2026-10-13); ADR-061's hold; ADR-032's claim model and "no reconnect loop" (a cap stays);
the default is today's `HINT_ONLY` behaviour unless the checkpoint changes it; the stored setting keeps its meaning across the update; no background claim; no
new wire request; `CAP-075`'s findings and Play's timers as 🟢 in `PROTOCOL.md` §4.1 (one Play version).

For the checkpoint (Phase B, one question per decision, options with pros and cons, "(Recommended)" on one, the exact text in the preview):
- Item 1: the cap (count / time, the value); the chain restart on a switch to "Take it back" with the tab shown; the hint text after the cap;
- Item 2: the button's label(s) and its behaviour in `HINT_ONLY`;
- Item 3: the cause line's wording;
- Item 4: the card's name, the intro, the two option labels, the (i) lines, the hint line, the ANC (i) sentence; the optional "Google Play services found / not
  found" information line (with the `<queries>` manifest entry) — yes / no;
- the ADR form (ADR-062 Update 2, or ADR-063 superseding ADR-062's Decision) and its text;
- the version (**1.2.2**, versionCode 10202, as a refinement of 1.2.1 — or 1.3.0 if the maintainer wants the rename to be a minor release) and the
  `CHANGELOG.md`/`README.md`/release-notes texts;
- Item 5's skeleton scope (both users, as `CAP-075`).

---

## 4. Tasks

### Phase 0 — set-up

1. Create the RESULT (§0). Record `git log -1`, `git status --short`, `git fetch` and `git log --oneline HEAD..origin/main`. Work on a branch from an up-to-date
   `main`: `git switch main && git pull --ff-only && git switch -c feature/channel-control` (say the name you used). `main` takes no direct push.
2. Run the gate once **before** any change (`cd android && ./gradlew assembleDebug testDebugUnitTest test lint --max-workers=2`, `python3 scripts/lint_docs.py`)
   and record the counts, so a later failure is attributable.

### Phase A — evidence and design check (no code change)

3. Re-derive from `CAP-075-btsnoop_hci2.log` (handle `0x000b`) for at least five re-claims: the cut (`DISC`), Play's retry `SABM`, the app's refused attempt
   (the export's "connect failed (attempt 1/3)" line and the logcat extract's `already at opened state` line), the app's `SABM`, the next cut — and the
   fixture bytes of Item 3; read the code paths Items 1–4 touch; write the design (the state, who calls what, the log lines, the tests, the constants) into the
   RESULT, with the expected coverage of a 3-minute chain (≈ 19 cycles of 9.3 s).
4. Items 2–4's text locations; Item 5's free IDs (`CAP-076`, Group BL, section Y).

### Phase B — checkpoint (stop and ask, in this chat, via `AskUserQuestion`)

5. The questions of §3. Record the answers verbatim in the RESULT. Do not build an option that was not chosen. Write the ADR text only after its answer.

### Phase C — Items 1–4

6. Build them in `:domain` (the cause value; the mode enum unchanged in its stored names), `:data` (the constant step, the cap, the chain restart, the action's
   claim, the cause), `:ui` (the card, the button, the lines), `:app` (the wiring; the optional `<queries>` entry only if approved); the unit tests of Item 5
   first or alongside; `ARCHITECTURE.md` in the same commit.

### Phase D — gate, mutations, compliance

7. The full gate green; the mutation checks M1–M6 of Item 5.
8. Compliance: no new permission, no `INTERNET`, no dependency (`./gradlew :app:dependencies --configuration releaseRuntimeClasspath` unchanged), every new or
   changed Kotlin file with the AGPL header, no hidden API, no MAC or serial logged at INFO or above, the always-on log lines payload-free; no new wire request;
   the stored setting of 1.2.1 readable unchanged.

### Phase E — the skeleton and the test plan

9. Item 5 (`CAP-076` skeleton, a *planned* registry row, a Group BL section and index row) and `APP_TESTPLAN.md` §Y.

### Phase F — documentation and release preparation

10. The approved texts; `DECISIONS.md` (the approved ADR form); `id_registry.csv`; `TODO.md` (§5's item done; §4's leads kept; anything deferred);
    `PROJECT.md`; `README.md`; `CHANGELOG.md` (`[1.2.2]` or `[1.3.0]`, no date, Known issues with the expected ≈ 50 %); `scripts/release_notes.template`;
    `android/app/build.gradle.kts` (`versionName`/`versionCode`); `ARCHITECTURE.md` §2.4, §3.2, §5a, §6.0b.
11. `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and `scripts/lint_docs.py` (exit 0).

### Phase G — finish

12. The RESULT: plain-language summary first (what the app does differently, what the user sees on the card, what the maintainer does next); the design; the
    tests and mutation results; the files read; "Deferred documentation" (each in `TODO.md`) and "Commits".
13. **The maintainer's next steps as instructions, never executed:** push and open the pull request (`gh pr create --base main --fill`, with the Release
    checklist); both CI workflows green; **B1** `scripts/release.sh <version>` on the branch tip; B2; **B3** `cp -a dist/<version> ~/opencontrol-<version>-tested`;
    C1–C2 the `CAP-076` run; then a CAPTURE session for C3/C4.
14. Show a short summary and **ask whether to commit and push**. Only after a yes: commits per concern (Conventional Commits: `feat`, `test`, `docs`), each with
    its *why* and the session's attribution line; `git fetch` and rebase; push without force; no file from a build directory, `dist/`, `.vscode/` or
    `__pycache__`. Opening the pull request is a separate question; never merge it.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Scope.** Only the items above. **No new wire request:** the re-claim and the hint card's action re-use the existing claim code; they change *when* a claim is
  made, nothing else. No background claim, no periodic claim, no unbounded loop (ADR-032, ADR-044): every chain has a cap and ends with the tab, the app's
  visibility or the session. No new permission, dependency or service. Nothing that talks to Play services; the optional package check of Item 4 only with the
  maintainer's yes.
- **Approvals only in this chat** (`AGENTS.md` §6): no 🟢 FACT promotion, no new ADR or ADR Update without the maintainer's answer in this chat. ADR-060 stays a
  draft. ADR-061 and ADR-062 are accepted — do not rewrite them; the approved form (Update 2 or ADR-063) supersedes what it names, after approval.
- **Evidence.** Fixtures are real bytes with file and frame in a comment (`AGENTS.md` §11); a hand-built array only as a labelled supplementary test. Every claim in
  the RESULT and the docs cites a frame, a log line or `file:line`. A negative needs its command, its exit status and a positive control. Play's timers are
  `CAP-074`/`CAP-075`'s measurements of one Play version — label them so in every text that names them.
- **The user's stored choice** of 1.2.1 keeps its meaning after the update (the stored names and the key unchanged; the update is tested with
  `fromStored`).
- **Privacy.** No serial unredacted and no MAC in any committed text; the capture originals stay in `~/opencontrol-capture-originals/`.
- **Release.** No `scripts/release.sh`, nothing written into `dist/`, no tag, no GitHub release, no merge. The maintainer builds and publishes.
- **Files.** Before moving or deleting any file, diff a fresh listing; never wildcard deletes; ask before any delete.
- **Subagents** may only read and report; every write is done and verified in the main session.

---

## 6. Deliverables

- The app changes for Items 1–4 (the constant step with the longer chain, the hint card's action, the cause line, the renamed and explained switch) with their
  tests, green gate and mutation results.
- `DECISIONS.md` (the approved ADR form), `id_registry.csv`.
- The `CAP-076` skeleton (Group BL), registry and index rows, `APP_TESTPLAN.md` §Y.
- The documentation of Item 4 (as approved), `TODO.md`, `PROJECT.md`, `README.md`, `CHANGELOG.md`, `scripts/release_notes.template`, `ARCHITECTURE.md`.
- ai-sessions/0089_FEATURE_RESULT_2026_10_13.md and the `ai-sessions/INDEX.md` row.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0089_FEATURE_PROMPT_2026_10_13.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0089_FEATURE_PROMPT_2026_10_13
