# 0005_MAINTENANCE_PROMPT_2026_09_09.md — Create capture-session skeletons for the 10 outstanding non-destructive captures that are new or need re-execution

**Number:** 0005
**Category:** MAINTENANCE
**Date:** 2026-09-09
**Title:** Create capture-session skeletons for the 10 outstanding non-destructive captures that are new or need re-execution

---

## Mandatory reading order (do this first, in this order)

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8, read, in order, before taking any other
action:

1. `AGENTS.md` (full)
2. `PROJECT.md` (full)
3. `PROJECT_RULES.md` (full)
4. `DECISIONS.md` (every ADR, ADR-001 through the most recent)
5. `ARCHITECTURE.md`
6. `PROTOCOL.md` (full, especially §4.3 Option A, §4.5.3/§4.5.7, §5.2, §6's open items referenced
   per-capture below)
7. `TODO.md` (full, especially the "Recommended priority order" §4/§5 and Phase 1's capture backlog)
8. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`
9. `CAPTURE_BLUETOOTH_HCI_SNOOP.md` in full — §4's existing Group definitions (especially Groups Q,
   AA, AD, AF, AG, whose letters get reused below) and §9's Capture Index (its current last row, to
   confirm the next free `CAP-NNN` number)
10. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §0's Test-ID convention and `id_registry.csv` in full (the
    next free `CAP-NNN` and Group-letter both get assigned from here — **do not** trust the specific
    numbers/letters named later in this prompt without re-checking against this file first; they are
    this prompt's own best-as-of-2026-09-09 estimate, not a final assignment, per this project's own
    ID-reuse-incident lesson, `DECISIONS.md`/`CHANGELOG.md`'s 2026-08-18 `CAP-005`/`CAP-007`/`CAP-010`
    entry)
11. One already-existing placeholder capture skeleton in full, to match its exact structure and
    tone: `captures/CAP-018-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Y/CAP-018-EVENT-NOTES.md` (and, for
    comparison, `CAP-026`/`CAP-028`/`CAP-029`/`CAP-030`'s sibling skeleton folders — all created
    2026-08-20, all currently un-executed)

## Context (from the chat session that authored this prompt, not re-derived here)

The maintainer asked for a table of every outstanding capture, categorized as **already planned**
(has an existing placeholder skeleton, e.g. `CAP-018`/`CAP-026`/`CAP-028`/`CAP-029`/`CAP-030` —
**out of scope for this prompt, they already have skeletons**), **needs re-execution** (an existing
`CAP-NNN` whose result was inconclusive/incomplete and needs a fresh session), or **completely new**
(no `CAP-NNN` assigned yet, no skeleton exists). This prompt covers exactly the "needs re-execution"
and "completely new" categories — **10 capture sessions total** — excluding the one destructive,
optional item from that table (the bonus factory-reset re-pair, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
§4.1 Group P #16, already tracked in `TODO.md`'s Phase 1 section on its own terms, not part of this
prompt).

**Naming/numbering convention, as instructed by the maintainer, matching this project's own
already-established practice** (see e.g. `CAP-006`/`CAP-013`/`CAP-031`/`CAP-032`, all labeled
`<original letter> (repeat...)`)):
- A capture that **re-executes an existing test** keeps that test's **original Group letter**
  (annotated `(repeat)` or `(repeat, Nth attempt)` as appropriate) but gets a **new**, sequentially-
  assigned `CAP-NNN` number — it is a new session, not an edit to the old one.
- A capture that is **completely new** (no prior `CAP-NNN`/Group letter at all) gets **both** a new
  `CAP-NNN` number **and** a new, sequentially-assigned Group letter.
- Every directory follows the existing placeholder-skeleton convention exactly:
  `CAP-<number>-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_<letters>` (literal `yyyy-MM-dd_HH-mm-ss_HH-mm-ss`
  until the maintainer actually runs the session and renames the folder to the real timestamps, per
  `CAP-018`'s own skeleton instructions).

**As of this writing (2026-09-09), the next free `CAP-NNN` is `CAP-043` and the next free Group
letter is `AJ`** (`id_registry.csv`/`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's last row is `CAP-042`,
Group `AI`) — **re-verify both against the registry at execution time**, per Task 1 below, rather
than assuming these are still free.

---

## Task 1 — Confirm the actual next-free `CAP-NNN` and Group letter

Check `id_registry.csv` and `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's Capture Index directly (not this
prompt's own estimate above) for the actual next free `CAP-NNN` and Group letter. Assign the 10
sessions below **numbers sequentially in the order listed** (repeats and new tests share one
increasing `CAP-NNN` sequence, per this project's existing practice) and assign the 5 **new** Group
letters sequentially in the order they appear below.

## Task 2 — Create the 10 capture-session skeleton directories

For each of the 10 sessions below, create the directory
`captures/CAP-<number>-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_<letters>/` and, inside it, a single
`CAP-<number>-EVENT-NOTES.md` file, matching `CAP-018-EVENT-NOTES.md`'s exact structure and tone:
a `🔲 Not yet captured — skeleton only` status banner with the fill-in/rename instructions, a
**Purpose** paragraph, a **Log Metadata** table, a **Procedure** section (numbered steps, `TBD` for
anything session-specific), an **Event Timeline** table template, an **Analysis checklist**, a
**Next steps after filling this in** checklist (adapt `CAP-018`'s own 5 items: Test-ID
cross-reference, write the matching `CAP-NNN-FINDINGS.md`, update the Capture Index row status from
`planned` to `analyzed`, update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s relevant Test-ID row(s), rename
the folder from the placeholder), and the standard repo footer line. Use the **Purpose**/**Procedure**
text given per session below as the basis — expand it into the full skeleton structure, do not
invent new test content beyond what's specified.

### Session A — repeat of `CAP-011` (Battery Notification BLE scan), Group Q (repeat)

**Purpose:** `PROTOCOL.md` §4.3 Option A's Fast Pair Battery Notification mechanism is still 🟡
HYPOTHESIS for the Buds Pro 2 specifically — `CAP-011` (2026-08-21) found the `0xFE2C` Fast Pair
Service BLE advertisement present, but the sampled payloads did not structurally match the
documented byte layout (no `0x33`/`0x34` Length&Type marker at any offset), and the session had a
procedure deviation: an active classic RFCOMM+GATT connection was present throughout (the official
app was left open on "Device details"), which this repeat must avoid.

**Procedure:**
1. Ensure the official Pixel Buds Companion App is **not running** (force-stop it) and no other app
   holds an active classic Bluetooth connection or GATT session to the Buds for the entire capture.
2. With the Buds already bonded (do not re-pair), start HCI snoop logging, then passively wait
   (case closed and idle, or worn but with no in-app interaction) for at least 60–90 seconds —
   long enough to observe multiple Battery Notification advertisement cycles per
   `PROTOCOL.md` §4.3 Option A.
3. Confirm via `bthci_evt`/`android.bluetooth` logs (or simply by not touching the phone) that no
   classic RFCOMM connection was ever established during the window.
4. Stop logging; extract per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3, preferring the raw
   `btsnoop_hci.log` path over the lossy `btsnooz.py`-from-bugreport fallback (per `TODO.md`'s own
   "Known technical debt" note on extraction-path truncation).
5. **Analysis focus**: filter for `0xFE2C` service-data advertisements and check every sampled
   payload against §4.3 Option A's documented byte layout (`Flags`/`Account Key Data`/`Battery
   level length & type` [`0x33` show / `0x34` hide]/three battery-percentage octets) — this closes
   `CAP-011-FINDINGS.md` §4's own open item either way (a structural match, or a second confirmed
   non-match worth re-examining the hypothesis itself).

### Session B — repeat of `CAP-033`/`SDP-001` (SDP UUID branch isolation), Group AA (repeat, 2nd attempt)

**Purpose:** `CAP-033-FINDINGS.md` §8 found the "default internal rfcomm socket" UUID still absent
from the SDP browse (an interesting negative), but the session's own procedure deviated — "Forget"
preceded "Force-stop" by ~10s (the reverse of the intended order) and step 3 (an app-open baseline
comparison) was never executed at all — capping the result at 🟡 HYPOTHESIS rather than a clean
FACT either way.

**Procedure:**
1. **Force-stop** the official Pixel Buds Companion App **first**, confirmed via Settings → Apps
   (screenshot/video timestamp), *before* touching the Buds' pairing at all — the reverse of
   `CAP-033`'s own accidental ordering.
2. With the app still force-stopped, perform the per-device "Forget" (not a broader Bluetooth reset)
   on the Buds, then re-pair via system Bluetooth settings only (no companion app involvement) and
   capture the SDP Service Search Attribute Response during that re-pair.
3. **Execute step 3 this time** — after the SDP browse above is captured, *open* the official
   companion app (still logging) and capture a second SDP browse triggered by the app itself, to
   compare against step 2's system-only browse.
4. Extract via the raw log path (not `btsnooz.py`), per the same technical-debt note as Session A.
5. **Analysis focus**: does the "default internal rfcomm socket" UUID (`3a046f6d-...`, either byte
   order) appear in *either* SDP browse this time? Does the "MAESTRO APP"/"GSND CONTROL"/"GSND
   AUDIO" naming (`CAP-033-FINDINGS.md` §3) reproduce identically in the system-only browse as well
   as the app-triggered one?

### Session C — HOLD-005 Left/Right ANC-rotation-checklist split (completely new, new Group letter)

**Purpose:** `PROTOCOL.md` §4.5.3's ANC-mode rotation checklist (`qhr` field 12, confirmed
field-number identity as `qht`) has 16 wire-observed boolean flags (`HOLD-005`, `CAP-021`) but no
Left/Right-distinguishing field for this specific write — unlike `HOLD-001`–`HOLD-004`, it's unknown
which frames belong to which earbud's own rotation list.

**Procedure:**
1. Open Device details → Controls and gestures → the ANC-mode rotation checklist for the **Left**
   earbud specifically (the UI is confirmed to expose this per-earbud, per `PROTOCOL.md` §4.5.3's
   own UI description).
2. Toggle each of the 4 checklist items (Noise cancellation / Off / Adaptive / Transparency) for
   the Left earbud **one at a time**, with a clear pause (≥10s) and a distinct video-visible action
   between each toggle, so each write can be isolated to one specific checklist item.
3. Repeat step 2 for the **Right** earbud's own rotation checklist, again one item at a time.
4. **Analysis focus**: do the Left-earbud toggles and Right-earbud toggles produce distinguishable
   wire patterns (e.g. a different inner field position, a different correlation-ID pattern, or
   simply a confirmed video-to-frame 1:1 timing correlation good enough to assign each frame to a
   side by elimination)? This directly closes `PROTOCOL.md` §6's open item on this question.

### Session D — Volume balance (`field 17`) scale/direction (completely new, new Group letter)

**Purpose:** `PROTOCOL.md` §4.5.7 confirms `qhr` field 17 = Volume balance (full identity, 🟢 FACT),
but its numeric scale/range beyond the 7 samples in one continuous drag (`CAP-022`) and which
direction (Left/Right) corresponds to negative vs. positive zigzag-decoded values remain 🔴 open —
a single continuous drag at 1fps video-sampling resolution wasn't enough to resolve this.

**Procedure:**
1. Open Device details → Sound → the Balance slider.
2. Drag the slider to its **full Left extreme**, release, and hold for ≥3s before any further
   action (an isolated, discrete sample, not a continuous drag) — video-confirm the slider's own
   on-screen position/label at this extreme.
3. Return the slider to center, pause ≥5s, then drag to its **full Right extreme**, release, hold
   ≥3s, video-confirm.
4. Repeat steps 2–3 at least once more for a second independent sample of each extreme.
5. Optionally, sample 1–2 clearly-labeled intermediate positions (e.g. "25% Left", "25% Right" if
   the UI shows a numeric/percentage label) the same isolated way.
6. **Analysis focus**: zigzag-decode (`(n>>1) ^ -(n&1)`) each isolated sample's `field 17` value and
   match it against the video-confirmed slider position/label at that exact moment — this directly
   closes `PROTOCOL.md` §6's open item on this field's scale/direction.

### Session E — DLCI 0x0a burst trigger, purpose-built hypothesis test (completely new, new Group letter)

**Purpose:** `CAP-021`'s 1123-frame DLCI 0x0a burst (`PROTOCOL.md` §6) has recurred in exactly 1 of
16+ sessions checked — passively waiting for it is not expected to work. `TODO.md`'s own
"Recommended priority order" §5 asks for a purpose-built hypothesis test bracketing candidate
triggers **one at a time**, per `PROJECT_RULES.md` §4's fixed template (hypothesis, setup, expected
outcome, actual outcome, conclusion).

**Procedure** (run as up to 3 separate bracketed sub-sessions, each testing exactly one candidate
trigger, logged either as 3 rows under this one Group or as 3 short sequential capture windows in
one continuous log with clear boundary timestamps — the maintainer's choice at execution time,
record which was used):
1. **Trigger candidate 1 — app backgrounded/foregrounded**: with the Buds connected and idle,
   background the official app for ≥2 minutes, then foreground it again; log throughout.
2. **Trigger candidate 2 — a scheduled sync window**: leave the Buds connected and the phone
   otherwise idle (screen off, app backgrounded) for an extended window (≥15 minutes, matching
   `CAP-042`'s own idle-bracket precedent) to see if the burst appears without any explicit action.
3. **Trigger candidate 3 — a charge-state change**: dock one or both Buds into the case (charging
   begins) or remove them (charging stops) while logging, isolating this specific transition.
4. **Analysis focus, per `PROJECT_RULES.md` §4's template**: for each bracketed trigger, record
   explicitly whether the 1123-frame (or any size) DLCI 0x0a burst appeared in or shortly after that
   specific window — a negative result for all three is itself a valuable, reportable outcome,
   not a failed session.

### Session F — repeat of `CAP-037`'s dock-state anomaly with an open ACL, Group AD (repeat)

**Purpose:** `CAP-037-FINDINGS.md`'s own open anomaly: on one of 26 same-session `DECISIONS.md`
ADR-022 replications, a second "Notify ANC state" frame appeared 18 seconds after the first, with no
new `08 11` Get frame in between, and its `Settable-toggles` value flipped `0xe8`→`0x00` — genuinely
open whether this reflects a real dock-state change while the ACL connection stayed open (in tension
with ADR-016's "ACL disconnects the instant both buds are re-docked" finding) or a spontaneous,
unprompted Notify unrelated to dock state.

**Procedure:**
1. Reproduce `CAP-037`'s own procedure (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AD): multiple
   isolated reconnects, alternating docked/undocked states, at least 20+ minutes total.
2. This time, keep a continuous, timestamped video specifically of the Buds/case's own physical
   dock state throughout (not just the phone screen) — the anomaly needs sub-second dock-state
   correlation that `CAP-037`'s own pass didn't have.
3. **Analysis focus**: if a `Settable-toggles` flip occurs with no preceding `08 11` Get and no ACL
   disconnect nearby, check the physical video at that exact wire timestamp — was a bud actually
   moved in/out of the case at that moment (reconciling with ADR-016), or not (a genuine open
   anomaly, now with video evidence either way)?

### Session G — repeat of `CAP-039`'s unexplained disconnect/reconnect cycling, Group AF (repeat)

**Purpose:** `CAP-039-FINDINGS.md` §6 found the classic ACL connection disconnected and reconnected
5 times across a single ~6-minute session with no clearly camera-visible trigger for most of them
(4 of 5 locally terminated, reason `0x16`) — genuinely open what caused the repeated cycling.

**Procedure:**
1. Reproduce `CAP-039`'s own general setup (Settable-toggles Set-vs-Get comparison at a fixed dock
   state, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AF) but this time with the phone screen recorded
   continuously throughout (not just at action moments) so any background system event (a
   Bluetooth-settings toggle, a notification, a screen-off/on cycle) is video-visible.
2. If the cycling reproduces, note the phone's own state (screen on/off, any visible notification,
   any app in foreground) at each disconnect/reconnect moment.
3. If it does *not* reproduce, that is itself a useful negative result — record it as such rather
   than treating the session as failed.
4. **Analysis focus**: correlate each `Disconnection Complete`/reconnect pair against the
   continuous phone-screen video to identify (or rule out) a phone-side trigger.

### Session H — repeat of `CAP-040` with a genuine trigger, Group AG (repeat)

**Purpose:** `CAP-040-FINDINGS.md` §3's own correlation attempt for DLCI 0x08's 7 unmapped
zero-length `[Group][Code][00 00]`-shaped frames failed because the session used the app's own
in-app "Connect"/"Disconnect" buttons as the bracketing trigger — but `CAP-040-FINDINGS.md` §1
separately found those buttons produce **zero wire-visible signal** at all, invalidating the
intended correlation (DLCI 0x08 opened exactly once, so all 7 codes had only one sample each).

**Procedure:**
1. Use a trigger confirmed to actually reopen DLCI 0x08 — either the OS-level Bluetooth toggle
   (Settings → Bluetooth off/on, per `CAP-037`'s own procedure) or physical case/bud
   docking/undocking — **not** the app's own Connect/Disconnect buttons.
2. Repeat this trigger ~10–15 times, each cycle bracketing one moment where a plausibly-relevant
   value might change (e.g. dock state, or whatever else the maintainer can identify as
   independently variable and video-observable at each DLCI-0x08-reopen moment).
3. **Analysis focus**: for each of the 7 unmapped codes (`05 0c`, `04 02`, `04 04`, `04 11`,
   `04 13`, `04 15`, `0e 04`), check whether its value (if any beyond the zero-length shape itself)
   changes across the now-multiple reopen samples, and whether that change correlates with the
   bracketed condition.

### Session I — `qhr` field 13 ANC-parallel-path wire confirmation (completely new, new Group letter)

**Purpose:** `REVERSE_ENGINEERING.md`'s `qhr` entry fully traces field 13 (ANC state, DLCI 0x02) to
exactly two code-side callers — an in-app `QuickActionsFragment` toggle-group tap, and a physical
press-and-hold gesture (`gvi`/`gvj`) — but neither has ever been wire-confirmed: no capture has yet
correlated a `qhr`-field-13 write on DLCI 0x02 with an itself otherwise-unexplained DLCI-0x04 Notify
(the specific pattern `CAP-038-FINDINGS.md` §5 observed twice with no preceding Get/Set on DLCI
0x04).

**Procedure:**
1. With full DLCI 0x02 traffic retained (raw-path extraction, not `btsnooz.py`), perform an
   isolated ANC-mode change via the **in-app** `QuickActionsFragment` toggle group (a single tap,
   pause ≥10s before the next action, matching `CAP-006`'s own isolated-tap discipline).
2. Separately, perform an isolated ANC-mode change via a **physical press-and-hold gesture** on one
   earbud (again, single gesture, pause ≥10s).
3. **Analysis focus**: for each of the two actions, check whether a `field5{field4{field13=N}}}`
   write appears on DLCI 0x02 at that moment — a positive match (especially time-correlated with
   the tap/gesture, mirroring `CAP-006`'s DLCI-0x04 confirmation methodology) would be strong
   evidence DLCI 0x02 *also* carries ANC state in parallel to DLCI 0x04's already-confirmed path.

### Session J — `CAP-041` Case%-change bracket (completely new, new Group letter)

**Purpose:** `CAP-041-FINDINGS.md` §4 and `CAP-036-FINDINGS.md` §12.6 both found a recurring 2-field
sub-message inside DLCI 0x02's connect-time/periodic burst holding a constant value that happens to
match the on-screen Case battery percentage throughout the session — but because the value never
changed in either session, this is consistent with, but does not confirm, the field tracking Case
battery (it could equally be any other session-constant value that happens to match).

**Procedure:**
1. Start a session with the Case at a known, video-confirmed battery percentage (check via the
   official app's Device details screen before starting).
2. Over an extended session (≥30 minutes, allowing genuine charge/discharge to occur — e.g. leave
   the Case charging via USB for part of the window, or simply let it discharge naturally if a
   bud is docked), periodically re-check and video-confirm the on-screen Case percentage.
3. Ensure the DLCI 0x02 periodic push (already confirmed to recur every few minutes when the app is
   foregrounded, per `CAP-036-FINDINGS.md` §12.5) is captured throughout.
4. **Analysis focus**: does the recurring 2-field sub-message's value actually change in step with
   the video-confirmed Case percentage changes? A positive correlation across a genuine change would
   promote this from "consistent with" to real evidence; a mismatch would be an equally useful,
   reportable negative result.

## Task 3 — Add new `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4 Group definitions for the 5 completely-new sessions

For Sessions C, D, E, I, and J (the ones needing a brand-new Group letter), add a new `#### Group
<letter> — <title> (occasional/one-time, added 2026-09-09)` entry to `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
§4, matching the existing Group Y/AA entries' structure (Purpose paragraph + numbered procedure +
analysis guidance) — base the content directly on the Purpose/Procedure text already written for
each session above. Sessions A, B, F, G, H reuse their original Group's existing §4 definition
unchanged (no new Group-definition text needed for those five).

## Task 4 — Add `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index rows for all 10 sessions

Add one new row per session (all 10), `Status` = `planned`, matching the existing `CAP-018`-style
placeholder rows' column format (Phone/Android/Buds FW/App version columns = `TBD`, Bugreport
file/Extracted log columns = `—`). Use each session's own Test-ID(s) where one already exists in
`id_registry.csv`/`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (e.g. `BATT-002`/`BATT-003` for Session A,
`SDP-001`/`SDP-002` for Session B) — for the completely-new sessions with no pre-existing Test-ID
(C, D, E, I, J), note this explicitly rather than inventing one; a new Test-ID is a `TESTPLAN`
addition outside this prompt's own scope (flag it as a follow-up, do not add one unilaterally beyond
what this prompt already authorizes).

## Task 5 — Register all 10 sessions in `id_registry.csv`

One row per new `CAP-NNN` (per `id_registry.csv`'s own existing format for capture entries), plus
one row per new Group letter if `id_registry.csv` tracks Group letters separately from `CAP-NNN`
(check its existing structure first — do not assume, follow whatever the file's own established
practice already is).

## Task 6 — Cross-reference the new `CAP-NNN` assignments in `TODO.md`

Update the specific `TODO.md` bullets that currently describe these 10 items only vaguely ("a clean
repeat is needed," "not yet designed in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`," etc. — see the
"Recommended priority order" §4/§5 section, and Phase 1's own capture backlog list) to instead name
the actual assigned `CAP-NNN` explicitly, now that a concrete session exists to point to. Do not
change the priority ordering itself — only make the existing bullets concrete.

---

## Guardrails

- This is preparatory/administrative work (creating skeletons, registry entries, cross-references)
  — it does not touch any protocol claim and does not go through the FACT/ADR sign-off gate
  (`AGENTS.md` §6/§15) any more than the existing `CAP-018`/`CAP-026`/`CAP-028`/`CAP-029`/`CAP-030`
  skeletons did when they were created.
- Do not perform any of the 10 captures yourself — this prompt only creates the skeletons/paperwork
  the maintainer needs to actually run each session with their own hardware. Do not fill in any
  `TBD` field with a guessed or invented value.
- Do not touch the destructive, optional factory-reset re-pair item — explicitly out of scope, per
  the maintainer's own instruction.
- Do not create a skeleton for any of the 5 already-planned-with-existing-skeletons captures
  (`CAP-018`/`CAP-026`/`CAP-028`/`CAP-029`/`CAP-030`) — they already have one; this prompt is scoped
  to the "needs re-execution" and "completely new" categories only.
- Re-verify the actual next-free `CAP-NNN`/Group letter against the registry at execution time
  (Task 1) rather than trusting this prompt's own as-of-writing estimate, in case other work has
  landed in the meantime.
- Log this session per `AI_SESSION_LOG_PROCEDURE.md`: write
  `ai-sessions/0005_MAINTENANCE_RESULT_2026_09_09.md` with `Status: complete` if all 6 tasks finish,
  `partial — resumed` if not.

## Output

List every directory/file created, the final `CAP-NNN`↔session and Group-letter↔session mapping
actually assigned (which may differ from this prompt's own as-of-writing estimate, per Task 1), and
every other file changed (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `id_registry.csv`, `TODO.md`).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0005_MAINTENANCE_PROMPT_2026_09_09.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0005_MAINTENANCE_PROMPT_2026_09_09
