# 0017_MAINTENANCE_PROMPT_2026_09_13.md — Close out Phase 1–3's remaining open items, inventory pending maintainer decisions, and assess Phase 3/4 readiness

**Number:** 0017
**Category:** MAINTENANCE
**Date:** 2026-09-13
**Title:** Close out Phase 1–3's remaining open items, inventory pending maintainer decisions, and assess Phase 3/4 readiness

---

## Purpose and scope

`TODO.md`'s Phase 1–3 sections still list several open items the maintainer asked about directly:
head gestures (`CAP-028`), the ANC-rotation Left/Right split (`CAP-045`), EQ persistence (`qhr` field
16 vs. 18), Battery Option A (`CAP-043`), per-component serial numbers, and the general state of
pending maintainer sign-offs. This prompt investigates each with the evidence already on disk (real
captures, decompiled APK source) — it does **not** perform any new physical capture itself (no
hardware access in this environment); where a genuinely new capture is the only way to close a
question, this prompt's job is to **design that capture as a ready-to-run skeleton** (per the existing
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group convention), not to attempt filming or button-pressing.

**This prompt's own research already did the first pass on every item below** (see the chat session
that produced this file) — re-derive it independently rather than trusting that summary at face
value, per `PROJECT_RULES.md` §1's "never speculate, always re-check the evidence" rule. Where this
prompt states a conclusion, treat it as a starting hypothesis to verify against the actual files, not
as settled fact.

**End state this prompt must reach:** `TODO.md`, `PROTOCOL.md`, `DECISIONS.md`, `REVERSE_ENGINEERING.md`,
`CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `id_registry.csv`, and
`ai-sessions/INDEX.md` all accurately reflect this session's work — the Final phase is a mandatory
consistency sweep across exactly these eight, not optional once the numbered phases "feel" done.

## Hard rule for every proposal this prompt produces

Per `AGENTS.md` §6/§15 (unchanged): this session may **propose** a 🟢 FACT promotion, a new
`DECISIONS.md` ADR, or a new capture skeleton — clearly labeled as a proposal — but must **never**
commit a FACT promotion or a new ADR as settled, and must never treat a proposed capture as already
executed. For every decision point below:

- Write a clear summary (what the evidence shows) and a concrete recommendation into the RESULT file,
  labeled `PROPOSAL — awaiting maintainer sign-off` (FACT/ADR) or `PROPOSAL — new capture` (a
  not-yet-run skeleton).
- Do not edit `PROTOCOL.md`'s status emoji for anything still contested, do not write/edit a
  `DECISIONS.md` ADR, and do not mark a proposed capture's Capture Index row as anything but
  `planned`.
- Continue to the other phases regardless of any single pending decision.
- In the **chat response** at the end of the session (not only the RESULT file), give the maintainer a
  compact, numbered, prioritized list of every decision still needed, each with a recommendation.

## How to (re)start this prompt

Before anything else, check whether `ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13.md` already
exists.
- **It does not exist** → first run. Create it in Phase 0 with a phase status table (rows: 1–8,
  Final) and start from Phase 1.
- **It already exists** → read it in full, including any `PROPOSAL` sections, then resume from the
  first phase not marked `done`. If the chat context for *this* resumed session contains the
  maintainer's answer to a previously-surfaced decision, apply it now exactly as instructed (drafting
  an ADR still requires the maintainer's explicit go-ahead in this same conversation before it is
  committed, never an inference from silence).

Update the RESULT file's phase status table and the header `Status` field
(`partial — resumed` while incomplete, `awaiting maintainer sign-off` once every phase not gated on a
pending decision is done, per `AI_SESSION_LOG_PROCEDURE.md` §4/§5) before ending any turn.

## Mandatory reading order (do this first, in this order)

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8:

1. `AGENTS.md` (full) — especially §6/§15 (FACT/ADR sign-off gate), §13.6 (zero-creativity hex
   parsing), §13 Workflow ("Analyzing a Bluetooth capture" / "Writing a protocol specification
   entry" / "Reverse engineering the APK").
2. `PROJECT_RULES.md` (full) — especially §1 (evidence rules, rule 1–4a), §3 rule 9/9a (non-destructive
   `DECISIONS.md`/`PROTOCOL.md` updates vs. rewrite-in-place `CAP-NNN-FINDINGS.md` corrections), §4
   (hypothesis-test template).
3. `PROJECT.md`, `ARCHITECTURE.md` (full).
4. `PROTOCOL.md` (full — especially §4.2 EQ, §4.3 Battery Option A, §4.5.3 ANC rotation checklist,
   §5.1 pairing paths (CTKD), §6).
5. `DECISIONS.md` (full, every ADR).
6. `TODO.md` (full — especially the "Recommended priority order" section and Phases 1–3).
7. `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (full,
   including §9 Capture Index and the existing Group AJ/AK/AL/AM/AN skeletons for the format to
   match), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (`HOLD-005`, `HEAD-002`, `HEAD-003`, `BATT-002`,
   `BATT-003` rows specifically).
8. `REVERSE_ENGINEERING.md` — especially the `qjw` (EQ save handler), `qjb`/`gaa`/`qie` (GetSoftwareInfo/
   GetHardwareInfo), and `MaestroDeviceSettingsProviderService` entries.
9. `captures/CAP-028-2026-09-12_07-16-15_07-17-18-Group_O/CAP-028-FINDINGS.md`,
   `captures/CAP-045-2026-09-12_08-22-51_08-24-32-Group_AJ/CAP-045-FINDINGS.md`,
   `captures/CAP-043-2026-09-13_09-50-31_09-53-41-Group_Q/CAP-043-FINDINGS.md`,
   `captures/CAP-036-.../CAP-036-FINDINGS.md` (the frame-1423 serial-number candidate decode) — full.
10. `id_registry.csv` — needed to check the next free `CAP-NNN`/`ADR-NNN` numbers before assigning
    any (do not assume the numbers named as examples anywhere in this prompt are still free by the
    time this prompt actually runs — re-check).

## Guardrails

- Never promote anything to 🟢 FACT and never write/alter a `DECISIONS.md` ADR without the
  maintainer's explicit, in-conversation sign-off.
- The hex-and-script rule (`PROJECT_RULES.md` rule 4a) applies to every decode in phases 1–5: show
  the exact command and the raw bytes it ran against, not just the stated conclusion.
- `CAP-NNN-FINDINGS.md` edits are rewrite-in-place corrections, not accumulating blockquote
  corrections (`PROJECT_RULES.md` rule 9a) — this prompt's phases mostly *add* new capture skeletons
  and *read* existing findings rather than editing them, but any genuine correction found along the
  way follows rule 9a.
- APK work stays inside `DECISIONS.md` ADR-017's mechanical-assistance boundary: search, list, and
  explain already-decompiled code; never decide relevance of a new class/string on your own
  authority; never record a new `REVERSE_ENGINEERING.md` HYPOTHESIS as settled without flagging it as
  a proposal.
- This environment has no physical Pixel Buds Pro 2 hardware and cannot film or perform gestures —
  every phase below that would otherwise need new data produces a **capture skeleton for the
  maintainer to run**, not a claim that the capture happened.
- New capture skeletons follow `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s existing Group format exactly
  (see Group AJ/AK/AL/AM/AN for the template: Purpose, numbered procedure steps, Analysis) and get a
  `planned` row in `id_registry.csv` and `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's Capture Index — do not
  invent a different skeleton format.
- Do not restructure or rewrite any document beyond what a specific phase justifies.
- MAC addresses in this project's own captures are intentionally unredacted (`DECISIONS.md` ADR-010)
  — not a finding to flag or fix.

## Phase 0 — Setup

Complete the mandatory reading order. Create `ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13.md` with
the required header block (`Status: partial — resumed` to start) and a phase status table (rows: 1–8,
Final). Add a row for `0017` to `ai-sessions/INDEX.md` (check the next free number first — it should
be `0017`, but re-verify).

## Phase 1 — EQ persistence: resolve field 16 vs. field 18 (slider-release vs. explicit Save)

`PROTOCOL.md` §4.2's "Outer field 16 vs. 18" entry and `REVERSE_ENGINEERING.md`'s `qjw` entry already
record a genuine, unreconciled tension: `CAP-015`'s wire timing reads as "field 18 fires on
slider-release," but a full call-graph trace of `fyd.d`/`fyd.e` found field 18 (`fyd.d`) reachable
**only** through a dedicated `"On click save EQ button"` handler, with no slider-release code path to
it found anywhere, and no other caller of the save-trigger method in the tree.

1. **Re-verify, do not re-derive from scratch**, that this trace is still accurate against the current
   decompiled source (`reverse-engineering/apk/<current version per APK_VERSIONS.md>/jadx-output/`):
   confirm `fyd.d`'s call sites are still limited to the save-button handler (a bounded, targeted
   re-check — do not repeat the full original search from zero if the class/method still exists as
   described). Record the exact search commands used, per the hex-and-script rule's spirit for code
   citations (`AGENTS.md` §13.6).
2. If the APK-side trace is confirmed exhausted (no plausible untried search strategy), the tension
   can only be resolved by a new capture, exactly as `PROTOCOL.md` §4.2 already names: **"a capture
   that drags-and-releases without ever tapping Save."** Design this as a new
   `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group skeleton (next free letter after Group AN, e.g. Group AO —
   check `CAPTURE_BLUETOOTH_HCI_SNOOP.md` for the actual next free letter) with an explicit procedure:
   drag one EQ slider, release, wait ≥10s with the Save button never touched (video must clearly show
   the Save button not being tapped), repeat for a second slider, and *then*, as a clearly separated
   second half of the same session, drag a third slider and deliberately tap Save — so the same
   session gives a clean within-session contrast between "release only" and "release + Save." Register
   it in `id_registry.csv` and `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 as `planned`.
3. Write a `PROPOSAL — new capture` block into the RESULT file summarizing the tension, why static
   analysis alone cannot close it, and the new skeleton's purpose. Do not touch `PROTOCOL.md` §4.2's
   existing HYPOTHESIS status — this phase adds a resolving-experiment design, it does not resolve the
   tension itself.

## Phase 2 — Battery Option A (Fast Pair BLE Battery Notification): what CAP-043 established and what's next

`CAP-043-FINDINGS.md` already closed one confound cleanly (a genuinely connection-free capture still
shows a non-matching payload, ruling out "active RFCOMM connection suppresses the advertisement" as
the explanation) and left two things open: whether the extension ever fires under *any* condition
(only idle/case-closed was tested), and what the actually-observed stable `0xFE2C` payload's real
sub-type is.

1. Re-verify `CAP-043-FINDINGS.md` §3's byte-level non-match claim directly (re-run the `tshark`
   command against `CAP-043-btsnoop_hci.log`, confirm the same 19-byte payload and lack of a
   `0x33`/`0x34` marker) — a bounded, quick sanity check before building on the finding, not a full
   re-analysis.
2. Design a new capture skeleton (next free Group letter) per `CAP-043-FINDINGS.md` §7's own
   recommended next step: a still connection-free, still filtered/foreground-triggered/time-boxed
   scan (per `AGENTS.md` §7's bounded exception) that specifically brackets a **single-bud
   insertion/removal event** (not idle/case-closed), to test the Fast Pair spec's own "optional when a
   single bud is inserted/removed" trigger condition. Register it as `planned`.
3. Write a `PROPOSAL — awaiting maintainer decision` block (not a FACT/ADR — a scoping question): if
   this next capture also comes back negative, is it time to recommend `PROTOCOL.md` §4.3 Option A be
   reframed from "HYPOTHESIS, confirmed as used by the Buds Pro 2" toward a more skeptical framing (per
   `CAP-043-FINDINGS.md` §7's own suggestion)? State the recommendation, do not edit `PROTOCOL.md`'s
   framing yet.

## Phase 3 — Head gestures (Nod/Shake, `HEAD-002`/`HEAD-003`): exhaustive re-check, then a correctly-scoped new capture

`CAP-028-FINDINGS.md` §4 already checked the Buds' own DLCI 0x02/0x04/0x08 traffic across the specific
claimed gesture window and found zero frames — and separately established (from `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s
own description of what Nod/Shake *do*: answer/reject a call, or dismiss a text reply) that this
silence is the **expected** result whether or not the gestures were physically performed, since no
call or notification was active during that capture. This is why the maintainer's report ("gestures
were performed, just not on video") does not, by itself, change the wire-evidence conclusion.

1. **Exhaustive re-check, addressing the maintainer's specific question directly**: re-run a DLCI
   0x02/0x04/0x08 scan across `CAP-028-btsnoop_hci.log`'s **entire** 227.71s span (not only the
   07:16:25–07:17:15 window `CAP-028-FINDINGS.md` §4 checked), and separately check for *any*
   HID/HFP/AVRCP/GATT traffic anywhere in the full log that wasn't already covered by that section's
   existing checks. Show the exact `tshark` commands and full output per the hex-and-script rule. If
   this turns up anything beyond what `CAP-028-FINDINGS.md` already documents, update that file in
   place (rule 9a); if it reproduces the existing clean-negative finding, record that explicitly as a
   completed, deliberate re-verification rather than silently skipping it.
2. **APK cross-reference**: search the decompiled source for the `HeadGesture` pw_rpc service/message
   already catalogued in `REVERSE_ENGINEERING.md` (found during the original `§4` keyword pass) for
   any Nod/Shake-specific opcode, message code, or constant name that could give the next capture a
   concrete byte pattern to look for, the same way `PROTOCOL.md` §4.1's ANC opcode was pinned down.
   Record findings (including a clean negative) in `REVERSE_ENGINEERING.md` per its own template,
   labeled as a proposal if it suggests any new `PROTOCOL.md` entry.
3. Design a new capture skeleton (next free Group letter) that fixes `CAP-028`'s actual gap: Nod/Shake
   performed **while an actual incoming call or notification is active** (per `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s
   own already-identified need), with the camera angled to also catch the gesture itself this time
   (not only the phone screen), so a future session isn't left with the same "gesture not physically
   confirmed" ambiguity even if the wire result is again silent. Register it as `planned`. Also update
   `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `HEAD-002`/`HEAD-003` rows (already flagged "inconclusive" by
   `CAP-028`) to point at this new planned capture.

## Phase 4 — ANC-mode rotation checklist Left/Right split (`HOLD-005`): a genuine re-run, this time avoiding CAP-045's own mistake

`CAP-045-FINDINGS.md` is unambiguous: the planned procedure (Device details → Controls and gestures →
the ANC-mode rotation checklist, toggling items per earbud) was **never actually run** — the session
captured physical press-and-hold ANC cycling instead, a different, already-understood mechanism.
`HOLD-005`'s original question is exactly as open as before `CAP-045`.

1. Confirm this reading directly against `CAP-045-FINDINGS.md` §2/§4 (a quick verification, not a
   re-analysis — the file's own conclusion is unambiguous).
2. Design the replacement capture skeleton (next free Group letter) with a procedure written to be
   unambiguous about what screen must be open — name the exact navigation path
   ("Device details → Controls and gestures → [ANC mode rotation checklist screen name, confirm the
   current app's actual label from `SCREENSHOTS_PIXEL_BUDS_APP.md` if available]") and require the
   video to show that screen on-camera for at least one full toggle before crediting the session as
   having run the procedure at all — an explicit anti-repeat safeguard, called out as such in the
   skeleton text, referencing `CAP-045` by name as the reason for the extra emphasis. Follow Group
   AJ's own existing procedure structure (already correct) as the base. Register it as `planned`,
   distinct from the exhausted `CAP-045` Group AJ slot (a new Group letter, not a `CAP-045` v2 reusing
   AJ).

## Phase 5 — Per-component serial numbers (Left/Right/Case)

`PROTOCOL.md` §6 (~line 2195–2221) already has a plausible, unconfirmed candidate: `CAP-036` frame
1423 (DLCI 0x02, Rcvd) decodes a nested sub-message to three length-14 strings —
`"57071WRBEC0251"`, `"57081WRBDR2309"`, `"57071WRBDL3147"` — structurally matching `qie`'s
three-optional-sub-field shape, plausibly Case/Right/Left given the "EC"/"DR"/"DL" substrings, but
**not yet confirmed** to be `qjb`'s own top-level schema (fields 3/4/5/6) as opposed to a sibling
response type sharing the same nested shape (e.g. `GetHardwareInfo`).

1. Do the field-by-field top-level match this prior pass explicitly deferred: trace `qjb`'s own wire
   tags around the nested `qie`-shaped sub-message inside `CAP-036` frame 1423, and confirm (or rule
   out) that the outer tags match `qjb`'s documented 4-field schema specifically, not a sibling type.
   Use `scripts/decode_rawmessageinfo.py` (already used for the original `qjb`/`qie` decode) and show
   the exact commands/output.
2. Cross-reference `REVERSE_ENGINEERING.md`'s `qjb`/`gaa`/`qie` entries and search the decompiled
   source for any additional caller/response type that could disambiguate `GetSoftwareInfo` from
   `GetHardwareInfo` at this specific frame, if not already fully resolved by step 1.
3. If step 1 confirms the `qjb` identity, propose promoting the field-number/schema-level identity
   claim (not the "EC=Case/DR=Right/DL=Left" semantic guess, which stays 🟡 HYPOTHESIS per
   `AGENTS.md` §13.6's zero-creativity rule) as a `PROPOSAL — awaiting maintainer sign-off` for
   `PROTOCOL.md` §6. If step 1 cannot disambiguate further from existing captures, say so plainly and
   assess whether a new purpose-built capture (a single fresh connect-time burst, correlated against
   the maintainer directly reading `GetSoftwareInfo`'s decoded value against the physical
   serial-number labels on the Buds/case, if accessible without disassembly) is the only remaining
   path — if so, design that skeleton (next free Group letter) rather than asserting the identity is
   settled.

## Phase 6 — Inventory every item currently awaiting a maintainer decision

Compile a single, complete, current list — do not rely on memory of what past sessions flagged; grep
freshly for the actual current state, since some past proposals may have already been resolved (e.g.
`ai-sessions/0015`'s work already closed some). At minimum, check and include (with each item's exact
file/line and a one-line summary + recommendation):

1. `PROTOCOL.md` §5.1's Cross-Transport Key Derivation (CTKD) pairing path — `PROPOSAL awaiting
   maintainer sign-off for promotion to 🟢 FACT`, strengthened by `CAP-012`'s controlled test.
2. `PROTOCOL.md` §4.3 Option A's Message Group `0x03`/Code `0x03` "Battery updated" candidate code —
   strengthened by the official Fast Pair Device Information extension spec, still pending
   independent-session reproduction before promotion.
3. `PROTOCOL.md`'s "Forget clears prior BLE association" open item (~line 2489) — pending maintainer
   judgment on whether `CAP-032`'s clean counter-example settles the general claim or only
   demonstrates it's non-universal.
4. `REVERSE_ENGINEERING.md`'s `MaestroDeviceSettingsProviderService` 6-case-ID→`qhr`-field trace —
   proposed for `PROTOCOL.md` §6/§4.5.5 promotion, pending maintainer review.
5. `REVERSE_ENGINEERING.md`'s "GSND" naming lead for DLCI 0x08/0x0a — a proposed addendum, pending
   maintainer review before either section is edited (low priority, informational only).
6. `CAP-044-FINDINGS.md` §5's on-device process-liveness-check proposal for a possible 3rd `SDP-001`
   attempt, or reframing `SDP-001`'s own question — awaiting a maintainer go/no-go.
7. Every new `PROPOSAL` this session produced in phases 1–5 above (EQ capture design, Battery Option A
   reframing question, head-gesture capture design, ANC-rotation capture design, serial-number
   promotion or new-capture design).

Present all of these together in one prioritized section of the RESULT file, ordered by how cheaply
each can be resolved (a maintainer sign-off with no new data needed first, a new capture last), not by
discovery order.

## Phase 7 — Full documentation cross-check and consistency pass

Run `scripts/lint_docs.py` and `scripts/ensure_footers.py` and fix anything they flag that this
session's own edits introduced (do not fix long-standing unrelated pre-existing noise unless it's
trivial and directly adjacent to a file this session already touched — note it instead). Then,
document-by-document, confirm each of the following is consistent with everything phases 1–6 produced
— for each, either make the update or explicitly write "checked, no change needed":

- **`TODO.md`** — Phase 1's `CAP-028`/`CAP-045` bullets, Phase 3's field-16/18 tension bullet, and the
  Battery Option A bullet all reflect this session's new capture designs and any status changes.
- **`PROTOCOL.md`** — §8 changelog entry for this session; no HYPOTHESIS/FACT status changed unless
  the maintainer answered inline during this conversation.
- **`DECISIONS.md`** — no new ADR committed unless explicitly approved inline this session.
- **`REVERSE_ENGINEERING.md`** — phase 3's `HeadGesture` search and phase 5's `qjb` field-match search
  results recorded, including clean negatives.
- **`CAPTURE_BLUETOOTH_HCI_SNOOP.md`** — every new Group skeleton from phases 1–5 actually added,
  correctly formatted, §9 Capture Index updated.
- **`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`** — `HOLD-005`/`HEAD-002`/`HEAD-003`/`BATT-002`/`BATT-003` rows
  point at the new planned captures where designed this session.
- **`id_registry.csv`** — every new `CAP-NNN` from phases 1–5 registered as `planned`, no number
  reused or skipped.
- **`ai-sessions/INDEX.md`** — `0017`'s row present and accurate.

## Final phase — Phase 3/4 readiness assessment and maintainer summary

Answer these directly in the RESULT file, grounded in phases 1–7's actual findings (not a restatement
of this prompt's own framing):

1. **Can `TODO.md`'s Phase 3 (Protocol reconstruction) be closed further right now?** State exactly
   which of Phase 3's remaining open checklist items are genuinely capture-blocked (need a physical
   session the maintainer must run — list which ones from phases 1–5 above), which are
   decision-blocked (need only a maintainer sign-off, no new data — list from phase 6), and which, if
   any, could still be advanced by pure static analysis/documentation work in a future session.
2. **What should happen before more Phase 4 (app development) work continues?** Cross-reference
   `ARCHITECTURE.md` §5's per-channel/per-feature implementation gate and `DECISIONS.md`'s existing
   ADR-009/ADR-011/ADR-015/ADR-020/ADR-023 unblocks: confirm explicitly that none of phases 1–6's open
   items block ANC/Battery-Option-C/Find-My-Buds/EQ-envelope implementation (they don't gate the
   already-unblocked features) — but that the EQ field-16/18 tension specifically blocks only the
   "Save as preset" UI affordance, per `DECISIONS.md` ADR-020's own scope note, not EQ generally.
3. **What else is realistically doable now, without new captures, to make `PROTOCOL.md` more
   app-ready?** A short, honest list — do not pad it if the honest answer is "very little, most
   remaining gaps are genuinely capture- or maintainer-decision-blocked."

Set the RESULT file's `Status` to `awaiting maintainer sign-off` (expected on a first pass, given
phase 6's inventory) or `complete` only if nothing is pending. In the **chat response** (not only the
RESULT file), give the maintainer:

1. A compact status of what was completed (phases 1–5's investigations and new capture designs, phase
   7's consistency sweep).
2. Phase 6's full prioritized decision inventory, each item with its recommendation, ready for a
   direct answer.
3. The Final phase's three-question readiness assessment, stated plainly.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0017_MAINTENANCE_PROMPT_2026_09_13.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0017_MAINTENANCE_PROMPT_2026_09_13
