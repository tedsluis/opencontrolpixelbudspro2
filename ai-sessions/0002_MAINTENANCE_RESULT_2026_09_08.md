# 0002_MAINTENANCE_RESULT_2026_09_08.md — Session logging rule additions, 0001 sign-off implementation, and V1 protocol-readiness gap scan

**Number:** 0002
**Category:** MAINTENANCE
**Date:** 2026-09-08
**Title:** Session logging rule additions, 0001 sign-off implementation, and V1 protocol-readiness gap scan
**Status:** complete

---

## Mandatory reading order — completed

Per `AGENTS.md` §0.1 and this task's own Task 1 (which formalizes this requirement going forward),
the following were read in full before any edit was made: `AGENTS.md`, `PROJECT.md`,
`PROJECT_RULES.md`, `DECISIONS.md` (every ADR, ADR-001–ADR-025), `ARCHITECTURE.md`, `PROTOCOL.md`
(full, including §6's open questions and §8's changelog), `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`,
`ai-sessions/INDEX.md`, and `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` in full. The relevant
sections of `REVERSE_ENGINEERING.md` (the `qhr` entry, the class-entry template, the UUID/Message
Group registers, the Correlation-status table, Native libraries, Call graph notes) were also read
before editing that file, and `README.md` was read in full ahead of Task 4.

---

## Task 1 — done

Added a new §8 ("Mandatory reading order") to `AI_SESSION_LOG_PROCEDURE.md`, binding on both the
prompt author (every `NNNN_CATEGORY_PROMPT` file must itself state `AGENTS.md` §0.1's reading order)
and the executing session (must do the reading — `AGENTS.md`, `PROJECT.md`, `PROJECT_RULES.md`,
`DECISIONS.md` at minimum — regardless of whether a specific prompt restates it). Cross-references
`PROJECT_RULES.md` rule 13/13a and `AGENTS.md` §0.1 rather than duplicating their content.

## Task 2 — done

Added a new §4a ("Updating a `Status` field later") to `AI_SESSION_LOG_PROCEDURE.md`, immediately
after §4's `Status` field definition, stating that a `RESULT` file's `Status` field may be updated in
place by a later logged session once the specific condition it represents has actually been met
(`awaiting maintainer sign-off` → `complete`, citing where sign-off happened; `partial — resumed` →
`complete`/a further `partial — resumed`), and that this is an in-place header update, not a new
duplicate entry, matching `PROJECT_RULES.md` rule 9a's spirit.

## Task 3 — done, all four groups implemented

Per the AI-assistance-boundary note in the prompt: the maintainer explicitly approved all of
`ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` Phase 4's proposals in the chat session that
authored prompt `0002` — that approval is what this task executes. No item beyond what Phase 4 named
was promoted or edited.

### 3.1 — `PROTOCOL.md` §6: four items added

All four of 0001 Phase 4's proposed `PROTOCOL.md` §6 items were added:

- **(a)** An informational-context item on `IFastPairDeviceDetailService`/`IFastPairFmdProxyService`
  (added to "Commands & schemas") — how the reference app sources battery data and handles Find My
  Device consent, explicitly marked out of scope for this project's own Zero-GMS implementation.
- **(b)** A dated Update appended to the existing Find My Buds Case/"both" open item (in "Behavior")
  — `FmdWorker`/`ijp` construct only ToS accept/skip `FmdRequest`s; no ring/play-sound trigger exists
  anywhere in this APK's decompiled source.
- **(c)** A new open item (in "Commands & schemas") on `MaestroDeviceSettingsProviderService` as a
  second, previously-undocumented UI entry point into the `qhr`/`WriteSetting` pipeline (6 case IDs,
  field mapping not yet traced).
- **(d)** A new open item (in "Commands & schemas") on `MaestroEndpointService`'s undetermined,
  exported, no-permission-gated gRPC service registrations.

`PROTOCOL.md` §6 Commands & schemas' existing "what do DLCI 0x02's confirmed inner field numbers
actually represent" item was also updated to record fields 11 and 15 as resolved (see 3.4).

### 3.2 — `REVERSE_ENGINEERING.md`: five entries added

- New class entries for `ijk`/`ijp`/`ijm`/`iji`/`gsy`/`TrueWirelessHeadset`/`HeadsetPiece`/
  `FmdRequest`/`FmdResponse`/`FmdWorker` (the GMS Fast Pair client-library boundary), with full
  file:line citations.
- New class entry for `MaestroDeviceSettingsProviderService`/`fhk`/`ges` (the AOSP settings-extension
  boundary).
- New class entry for `frb`/`fuh`/`glk`/`gjv` (the `fxm.i()`/`GetSoftwareInfo` trigger structure).
- Field-register updates inside the existing `qhr` entry for fields 11 and 15 (full call-graph
  traces to `MultipointFragment`/`hlv`'s `"volume_eq_switch"` case), plus the field 5/`fyo.i()`
  no-op ("Loudness compensation") note — both folded into a new 2026-09-08 update block inside the
  existing `qhr` entry, and the entry's own bonus field-register table rows for 11/15 updated in
  place.
- New class entry for `MaestroEndpointService` (open questions only — its registered gRPC
  service(s) and binding caller(s) are undetermined).

Five new rows were also added to the "Correlation status with `PROTOCOL.md`" table.

### 3.3 — `TODO.md`: closed one item, added two

- Closed the "apply ADR-019's static-analysis method to `qhr` fields 11 and 15" item under
  "Targeted research follow-ups" — marked done, with a note that fields 17/19/22/27/28 had actually
  already closed 2026-09-03 (the bullet was simply never updated at the time).
- Added a new item: trace `MaestroDeviceSettingsProviderService`'s 6 case IDs to their specific
  `qhr` field numbers.
- Added a new item: `apktool` smali-fallback read of `MaestroEndpointService.onCreate()`.

### 3.4 — `DECISIONS.md`: ADR-025 Update note added, quoted in full below

Two dated Update entries were appended to the existing **ADR-025** (non-destructively — the ADR's
original Decision/Consequences text is unchanged, per `PROJECT_RULES.md` §3 rule 9). Quoted in full:

> - **Update (2026-09-08, maintainer sign-off via the chat session that authored prompt
>   `ai-sessions/0002_MAINTENANCE_PROMPT_2026_09_08.md`, implementing
>   `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` Phase 1/Phase 4's approved proposals) —
>   GMS-boundary finding strengthened, not weakened.** A deeper Phase 1 search (structural
>   AIDL/`ServiceConnection` pattern search plus a full manifest read, going beyond the original
>   audit's literal-keyword grep) found that Google Play Services' Fast Pair module *is* reachable
>   from this companion app's own decompiled code, via two genuinely named, unobfuscated AIDL
>   interfaces (`com.google.android.libraries.bluetooth.fastpair.IFastPairDeviceDetailService`,
>   `...fmd.IFastPairFmdProxyService`) bound through Google's Chimera dynamic-module broker
>   (`com.google.android.gms.chimera.GmsBoundBrokerService`). **This does not weaken this ADR's
>   decision or its "DLCI 0x04/0x08 transport code is absent from this APK" evidentiary basis — it
>   strengthens it.** The newly-found boundary carries only already-decoded objects (a
>   `TrueWirelessHeadset` battery summary; an `FmdRequest`/`FmdResponse` consent-flow pair) — not raw
>   Message-Stream/private-envelope frame bytes — and an exhaustive sweep of every
>   `queryLocalInterface(...)` call in this APK version (31 total) found no third, ANC/settings-shaped
>   GMS interface. The original conclusion — that DLCI 0x04/0x08's actual frame
>   construction/parsing lives inside GMS itself, not this companion app — is now supported by a
>   *positive* architectural finding (a concrete, named, working example of exactly this kind of
>   higher-level GMS boundary existing and being reachable) in addition to the original *negative* one
>   (no transport code found). **No change to this ADR's Decision or Consequences sections** — this
>   Update records the strengthening finding per `PROJECT_RULES.md` §3's non-destructive-update
>   convention. See `REVERSE_ENGINEERING.md`'s `ijk`/`ijp`/`TrueWirelessHeadset`/`FmdWorker` entry and
>   `PROTOCOL.md` §6 for the full trace.
> - **Update (2026-09-08, same maintainer sign-off) — `qhr` fields 11 (Multipoint) and 15 (Volume EQ)
>   promoted to 🟢 FACT for full field-number/semantic identity.** Applying `DECISIONS.md` ADR-019's
>   same static-analysis method (a forward trace from a named UI fragment/preference key to the write
>   call site, rather than the log-message-backward technique used for ADR-019's own fields) to the
>   two fields explicitly flagged as still-unchecked in `TODO.md`'s "Targeted research follow-ups":
>   **field 11 = "Multipoint"** (`MultipointFragment`'s `key_multipoint_main_toggle` toggle →
>   `hiy.java:32`'s self-describing `"Set device Multipoint as: %s"` log → `fyo.java:146-166`) and
>   **field 15 = "Volume EQ"** (`hlv.java:2127`'s self-describing `"Set volume eq: %s"` log, gated on
>   the literal Android preference-key string `"volume_eq_switch"` → `fyo.java:376-396`) — both
>   readings match the pre-existing wire-derived HYPOTHESIS labels exactly, with no naming-equivalence
>   gap of the kind that kept fields 12/22/27 at field-number-only status. See `PROTOCOL.md`
>   §4.5.2/§4.5.6 and `REVERSE_ENGINEERING.md`'s `qhr` entry (2026-09-08 update) for the full evidence.

The two FACT-promotion lines this Update records, quoted from `PROTOCOL.md` in full:

> `PROTOCOL.md` §4.5.2 (Multipoint): **"Opcode/payload — full identity 🟢 FACT, promoted 2026-09-08
> (maintainer sign-off, prompt `0002`, implementing
> `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` Phase 2/Phase 4)**: `field5(len4){ field4(len2){
> field11 = 0|1 } }`, `field 11` = `libmaestro`'s own `qhr` schema field 11, independently confirmed
> by APK static analysis tracing forward from the UI [...] A self-describing app-code match to this
> section's own "Multipoint" reading, at the ViewModel layer rather than `fxb.java`'s response
> handler (which has no distinct log for case 11). ON direction video-correlated on the wire; OFF
> direction not captured."

> `PROTOCOL.md` §4.5.6 (Volume EQ): **"Opcode/payload — full identity 🟢 FACT, promoted 2026-09-08
> (maintainer sign-off, prompt `0002`, implementing
> `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` Phase 2/Phase 4)**: `field5(len4){ field4(len2){
> field15 = 0|1 } }`, `field 15` = `libmaestro`'s own `qhr` schema field 15, independently confirmed
> by APK static analysis via a literal Android preference-key string match [...] This combines a
> self-describing log message *and* the literal preference-key string, a stronger evidence type than
> any other `qhr` field promoted to date. Both directions video-confirmed on the wire."

### Closing per Task 2's own new rule

`ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md`'s header `Status` field updated from `awaiting
maintainer sign-off` to `complete`, citing this prompt (`0002`) as where sign-off happened, per
`AI_SESSION_LOG_PROCEDURE.md` §4a. `ai-sessions/INDEX.md`'s row for `0001` updated to match, and its
row for `0002` updated from "pending — prompt only, not yet run" to `complete`.

### Files created or changed for Tasks 1–3

- `AI_SESSION_LOG_PROCEDURE.md` — Tasks 1 and 2 (new §8, new §4a).
- `PROTOCOL.md` — Task 3.1 (§6 four items), Task 3's field 11/15 promotion (§4.5.2, §4.5.6, and the
  matching §6 "confirmed inner field numbers" item update), and a new §8 changelog row.
- `REVERSE_ENGINEERING.md` — Task 3.2 (five entries: 3 new class write-ups, the `qhr` entry's field
  11/15 update block, the `MaestroEndpointService` entry; plus Correlation-status table rows and
  `qhr` bonus-register table row edits).
- `TODO.md` — Task 3.3 (one item closed, two items added).
- `DECISIONS.md` — Task 3.4 (ADR-025, two dated Update notes appended).
- `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` — `Status` field updated.
- `ai-sessions/INDEX.md` — rows for `0001` and `0002` updated.
- `ai-sessions/0002_MAINTENANCE_RESULT_2026_09_08.md` — this file (new).

`python3 scripts/lint_docs.py` was run after all edits; the only new finding was a transient dead
filename reference to this very RESULT file from `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md`
(cited before it existed), resolved by writing this file. All other lint findings are pre-existing,
unrelated to this session's edits (stale PNG/log filenames in `CAP-036`/`CAP-038`/`CAP-039`/`CAP-040`
capture folders).

---

## Task 4 — Gap scan: what's still needed before `PROTOCOL.md` is complete enough for V1 app development

Based on an actual read of `PROTOCOL.md` §6's own 🔴/🟡 markers (post-Task-3), `TODO.md`'s current
task list (post-Task-3), `DECISIONS.md`'s open items, and `README.md`'s "Still open" list — not a
generic guess. This is a report; nothing below was implemented this session.

### Captures still needed

- **A clean, connection-free repeat of the Battery Notification BLE scan.** `CAP-011`
  (2026-08-21) is the only attempt and is inconclusive — an active RFCOMM connection was present
  throughout (a procedure deviation), and the sampled payloads don't structurally match §4.3 Option
  A's documented layout. Blocks V1-readiness because Option A is `ARCHITECTURE.md` §4's
  lowest-cost, connection-free battery path; until this is resolved, only the higher-cost Options
  B–D remain confirmed cheap fallbacks. AI-assistable for planning (a `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
  procedure), but the capture itself is maintainer-only (hardware access).
- **A proper isolation-clean repeat of `SDP-001`.** `CAP-033`'s own procedure deviation (a
  Forget-before-Force-stop ordering issue, and a never-executed step 3) caps its "default internal
  rfcomm socket never appears" result at 🟡 HYPOTHESIS rather than a clean FACT. Lower priority —
  doesn't block any v1 feature directly, but leaves a `DECISIONS.md`-adjacent claim
  under-evidenced. Maintainer-only (hardware access).
- **`CAP-018` (Group Y, `0x0044` BLE-notification-burst isolation), `CAP-026` (Group L, passive
  observation), `CAP-028` (Group O, head gestures — needs `CAP-020`'s Head-gestures toggle left on,
  never attempted), `CAP-029` (Group P, Conversation Detection voice trigger + optional destructive
  factory-reset comparison + the still-open shorter-press pairing-mode question), `CAP-030` (Group
  Q, Loud Noise Protection/Adaptive Audio — needs firmware ≥4.467, worth checking against this
  project's `release_5.203` baseline first per `PROTOCOL.md` §0.1's still-open version-reconciliation
  note).** All already have Capture-Index rows but have never been run. `CAP-028` is the
  highest-value of these (head gestures is one of README's own "Still open" v1-scope items).
  Maintainer-only.
- **A purpose-built repeat isolating `HOLD-005`'s Left/Right ANC-mode-rotation-checklist split**
  (the envelope carries no Left/Right-distinguishing field for this one write, unlike
  `HOLD-001`–`HOLD-004`) — flagged in `TODO.md` as a "new-capture idea, not yet designed." Blocks
  full FACT status for one of README's explicitly-listed "Still open" v1 sub-features. AI-assistable
  for designing the procedure; maintainer-only for execution.
- **A purpose-built capture isolating Volume balance (`field 17`)'s scale/direction** (isolated
  extreme-position samples, not a continuous drag, with tighter video correlation) — same "not yet
  designed" status in `TODO.md`. Blocks full confidence in the one already-FACT-identified field
  whose numeric range/direction is still open.
- **A purpose-built hypothesis test bracketing the `CAP-021` DLCI 0x0a burst's trigger** (app
  backgrounded/foregrounded, a scheduled sync window, a charge-state change, one at a time) — the
  burst recurred in exactly 1 of 16+ sessions checked, so passively waiting for it is not expected
  to work. Lowest priority (out-of-scope research per `PROJECT.md`'s audio-codec non-goal — the
  burst is plausibly a capability/diagnostic dump, not itself a v1 feature) but still an open item.
- **Open questions with *no* capture planned at all**, distinct from the above: the DLCI 0x08 Group
  `0x01`/`0x02`/`0x05`/`0x09` semantics; the DLCI 0x02 Address-field renegotiation mechanism (why a
  request duplicates on two addresses at once); the Ring action's two ACK variants' extra byte(s)
  (now that neither matches the corrected spec worked example); `CASE-001`'s explicit-tap-vs.-
  screen-open-sync ambiguity; the RFCOMM multiplexer channel-bounce trigger (`CAP-016`); a second,
  independent isolated-ANC-tap-plus-physical-gesture capture to wire-confirm `qhr` field 13's parallel
  DLCI-0x02 ANC-write path (code-side tracing is now complete per Task 3, per `REVERSE_ENGINEERING.md`'s
  `qhr` entry — this is now purely a capture task, not a code-tracing one); a capture bracketing
  `CAP-041`'s Case-battery-field lead against an actual mid-session Case-percentage change; `CAP-037`'s
  dock-state-anomaly-with-ACL-open re-test; `CAP-039`'s 5x unexplained disconnect/reconnect cycling.
  None of these block v1's core "Definition of done" features (ANC, battery, EQ are already
  FACT/implementation-ready) — they matter for completeness and for features beyond the v1 floor.

### APK reverse-engineering still needed

- **Trace `MaestroDeviceSettingsProviderService`'s 6 case IDs to their specific `qhr` field
  numbers** (added to `TODO.md` this session, Task 3.3) — same forward-trace technique just used for
  Multipoint/Volume EQ. Concretely scoped, AI-assistable (search/list/explain, `DECISIONS.md`
  ADR-017 boundary), maintainer decides relevance/promotion.
- **`apktool` smali-fallback read of `MaestroEndpointService.onCreate()`** (added to `TODO.md` this
  session) — its bytecode is JADX-undecompilable; would reveal which gRPC service(s) this exported,
  no-permission on-device server actually registers. AI-assistable per ADR-017 (native/smali
  disassembly explanation is explicitly in scope).
- **Trace `gjv.p()`'s own caller** (from this session's Phase-2-deepened `frb`/`fuh`/`glk`/`gjv`
  write-up) — the remaining open link needed to determine whether `fxm.i()`'s `GetSoftwareInfo`
  fetch genuinely fires inside the `CAP-036`/`CAP-041` connect-time settling window. **Not yet added
  to `TODO.md`** — this is a gap in Task 3.3's own scope (the task only asked to add the
  `MaestroDeviceSettingsProviderService` and `MaestroEndpointService` items) — flagging it here as a
  proposal for a future session's `TODO.md` addition, not adding it now per this task's own Task 4
  guardrail against implementing findings.
- **Trace `fyd.d`/`fyd.e`'s own call sites** in the EQ UI fragment — the one specific step already
  identified (in `TODO.md`, pre-existing) as still missing to connect `qjw` field 16/18's code-derived
  "live vs. persisted" reading to the wire-observed "drag vs. release" timing. Directly relevant to
  whether EQ's "Save as preset" UI can ship (`DECISIONS.md` ADR-020's own scope note).
  Not yet attempted.
- **Apply the same field-register method to `qhr`'s remaining, not-yet-traced fields** (1, 6, 8, 14,
  20, 24–26, 30–38) — most are plausibly write-silent/read-inert like fields 6/8/9/10, but this
  hasn't been individually confirmed for most of them. Low priority — no v1-scope feature is known
  to depend on these.
- **Re-attempt decoding DLCI 0x02's opaque "Sent" blocks against the now-recovered `qhr` schema** —
  the AES-128-encryption hypothesis (`PROTOCOL.md` §6) was explicitly blocked pending "a
  pw_rpc/protobuf schema to check against"; that schema now substantially exists (`qhr`, `qjc`/`qja`,
  `nqx`). Worth a dedicated pass checking whether the opaque blocks decode structurally now, before
  concluding they're encrypted.

### Cross-checks still needed

- **`CAP-041`'s DLCI 0x02 connect-time-burst content diff across differing settings states** — the
  *length*-level negative result (invariant across 3 non-default states) is done; a full byte-for-byte
  content diff was explicitly out of that session's time budget and remains open. Directly relevant
  to `ARCHITECTURE.md` §3.1's "does `libmaestro` carry a settings-state read-back" question.
- **A byte-level correlation between a fresh capture's DLCI 0x02 connect-time burst and the decoded
  `GetSoftwareInfo` response shape (`qjb`)** — the other half of closing this session's `gjv.p()`
  trigger question (a capture task, not further static analysis, per this session's own Phase 2
  write-up).
- **`TrueWirelessHeadset.modelId` cross-referenced against DLCI 0x04's already-FACT Device
  Information "Model ID" (`da 2d b1`)** — a cheap cross-check, once a live device value dump is
  available. Purely informational (doesn't affect any implementation path, since the GMS boundary is
  out of scope for this project's own app), but would close a loose end.
- **A second, independent capture wire-confirming `qhr` field 13's parallel ANC-write path** (see
  Captures section above) — the code side is fully traced; only the wire correlation is missing.

### Consistency checks still needed

- **`TODO.md`'s "Recommended priority order" section and Phase 1 checklists are significantly
  stale relative to `PROTOCOL.md`/`DECISIONS.md`.** Last substantively touched around 2026-08-30;
  it does not reflect `CAP-034` (2026-09-01, which *closed* the Group W GATT cache-busting item
  `TODO.md` still lists as needing "a genuine attempt at Group W's own untried cache-busting
  methods"), `CAP-035`–`CAP-042`, `DECISIONS.md` ADR-020 through ADR-025, or this session's own
  Multipoint/Volume EQ promotions. **This is a genuine gap in the existing priority ordering** (not
  just missing detail) — `TODO.md`'s own "current highest-leverage single next step" language in its
  "Recommended priority order" §3 still names fields 11/15/17/19/22/27/28 as the target, when 5 of
  those 7 were already closed by 2026-09-03 and the remaining 2 were closed today. Flagged as a
  proposal for the maintainer to prioritize a `TODO.md` sync pass, not decided here.
- **`README.md`'s "Current state (2026-09-07)" section and "Still open" list are now one day
  stale** — Multipoint (field 11) and Volume EQ (field 15) are FACT/traced as of today and should
  move out of "Still open"; the date header itself should advance once the maintainer reviews this
  session's changes.
- **`CHANGELOG.md`** — not checked this session for whether it needs an entry summarizing today's
  `AI_SESSION_LOG_PROCEDURE.md`/`PROTOCOL.md`/`REVERSE_ENGINEERING.md`/`TODO.md`/`DECISIONS.md`
  changes; the project's own convention (`PROJECT.md` §7, `README.md`'s documentation table) treats
  `CHANGELOG.md` as tracking "changes per release," so whether a mid-development-phase maintenance
  session like this one warrants an entry is itself a judgment call worth the maintainer's input.
- **`scripts/lint_docs.py`'s existing dead-filename findings** (pre-existing, in
  `CAP-036`/`CAP-038`/`CAP-039`/`CAP-040`'s event-notes/findings files, plus one in `CAP-036`'s own
  event notes) were not investigated or fixed this session — out of this task's scope, but flagged
  since they've apparently persisted across several sessions without being triaged.
- **A general `PROJECT_RULES.md` rule 9a compliance sweep** (checking for `CAP-NNN-FINDINGS.md`
  files dated on/after 2026-08-15 that still accumulate inline "Correction"/"Update" addenda instead
  of being rewritten in place) has not been run recently — not attempted this session.

### Validation against outside sources

- **Re-verify `PROTOCOL.md` §4.3 Option A's "shown ≥8s, auto-hidden after 20s" Battery Notification
  visibility-timing claim** directly against the official Fast Pair spec pages — already flagged in
  `TODO.md`, still not done. A 2026-09-03 re-check found no matching text on the
  `batterynotification` extension page specifically; the detail may live elsewhere (e.g. the base
  Message Stream spec) and hasn't been checked there yet.
- **Cross-check the Ring action's ACK-variant extra byte(s)** against the corrected spec's own
  worked-example tail (`01 3C`) more thoroughly, or against `qzed/pbpctrl`'s own notes if they cover
  this specific Action-group ACK shape (not yet re-checked since the 2026-08-23 spec-citation
  correction).
- **Cross-check the DLCI 0x02 Address-field-renegotiation hypothesis** against Pigweed's public
  `pw_rpc` documentation for how channel/client addressing is typically negotiated — would help
  distinguish "per-connection-negotiated pw_rpc handle" from other explanations, without violating
  the no-code-reuse rule (`AGENTS.md` §12).
- **Re-check `qzed/pbpctrl`'s own published notes** (`https://raw.githubusercontent.com/qzed/pbpctrl/main/docs/Notes.md`)
  for anything beyond the already-consulted transport-framing paragraph — this project has only ever
  quoted one specific sentence from that document (§2.2a/§2.3); worth confirming there isn't further
  published detail on EQ/settings semantics that would strengthen (or contradict) this project's own
  independently-derived findings.
- **`FE2C1238…`'s official name and "Unknown Service" (`109b862f-…`)'s purpose** — both remain 🔴
  OPEN QUESTION per `CAP-034-FINDINGS.md` §8; worth a fresh check against the live, evolving Fast
  Pair spec pages and the Bluetooth SIG assigned-numbers database, since the spec has already been
  found to have drifted once this project (§4.3 Option A's timing-claim downgrade).

### Plain documentation updates

- `README.md`'s "Current state" date and "Still open"/"Confirmed and implementation-ready" lists
  (see Consistency checks above — listed here too since it's simultaneously a staleness gap and a
  trivial fix once the maintainer wants it done).
- `TODO.md`'s "Recommended priority order" §3 and Phase 1 "Top priority"/"Next" checklists — a sync
  pass adding checkmarks/notes for `CAP-034`–`CAP-042` and closing the now-stale Group W item.
- Possibly `CHANGELOG.md` (see Consistency checks above — maintainer's call whether this session
  warrants an entry).

### Anything else

- **`ARCHITECTURE.md` §15's open architecture questions** (Hilt vs. manual DI, minimum Android API
  level, whether the observed Bluetooth HID surface needs a third `BudsTransport` input path) are
  unaffected by this session and remain maintainer-only product/design decisions, not research gaps
  — already correctly tracked as such.
- **Find My Buds Case/"both simultaneously"** — `TODO.md`'s "Recommended priority order" §1 already
  correctly identifies this as a maintainer product decision (accept a GMS/Find-Hub-mediated
  fallback for this one sub-feature, or ship v1 without local Case-ring support), not a research
  gap. This session's Task 3.1(b) strengthened the code-level evidence that no local-only mechanism
  exists in the companion app's own source, but did not — and could not — resolve the product
  question itself.
- **`CAP-041`'s connect-time-burst content diff and `CAP-037`'s dock-state anomaly** are existing-
  capture re-analysis tasks (the raw logs are already on disk) rather than either a new capture or
  APK work — a distinct third category from the buckets above, worth the maintainer's awareness when
  prioritizing analysis-only sessions that don't need hardware access.
- **This session's own two new `TODO.md` items** (`MaestroDeviceSettingsProviderService` case-ID
  trace, `MaestroEndpointService` smali read) and the not-yet-added `gjv.p()`-caller-trace item (see
  APK reverse-engineering above) are all AI-assistable research tasks per `DECISIONS.md` ADR-017's
  boundary — none require hardware access or a product decision.

No priority-ranking change is proposed beyond what's flagged above (`TODO.md`'s stale "Recommended
priority order" §3 sub-item and the Group W item) — everything else in this scan sits within
`TODO.md`'s existing ordering (captures and APK work behind the already-shipped v1-critical features,
ahead of the lower-priority research items already marked lowest-priority there).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0002_MAINTENANCE_RESULT_2026_09_08.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0002_MAINTENANCE_RESULT_2026_09_08
