# BACKLOG.md — Future APK-analysis tooling ideas

A parking lot for tooling ideas raised while designing and building
[`lambda_dispatcher_resolver/`](lambda_dispatcher_resolver/SPEC.md), so they aren't lost and can be
triaged later. **Nothing here is designed, approved, or committed to being built** — this is
deliberately one level less formal than `lambda_dispatcher_resolver/SPEC.md`. An idea graduates out
of this file by getting its own spec document (following `lambda_dispatcher_resolver/SPEC.md` as
the template) once the maintainer decides to pursue it.

## Governance that applies to every idea below, unconditionally

Any tool built from this list operates inside the exact same boundary
`lambda_dispatcher_resolver/SPEC.md` §8 already established, not a fresh negotiation each time:

- Mechanical assistance only (`DECISIONS.md` ADR-017) — a tool may search, extract, and correlate;
  it never decides a finding's relevance to the Bluetooth protocol, and never writes into
  `REVERSE_ENGINEERING.md`/`PROTOCOL.md`/`DECISIONS.md`/any `CAP-NNN-FINDINGS.md` itself.
- **No decompiled APK content is ever committed, in any form, including as test fixtures** —
  `lambda_dispatcher_resolver/SPEC.md` §11 documents a real mistake this project's own first attempt
  at this made (committing byte-exact smali/JADX excerpts as "just test fixtures"), caught only
  after the fact against this repo's own `.gitignore` comment quoting `PROJECT_RULES.md` §8 rule 20.
  Every future tool's tests read from the maintainer's local, gitignored
  `reverse-engineering/apk/<version>/` tree and skip themselves when it's absent — never a
  committed copy.
- Reuse this project's own FACT/HYPOTHESIS/ASSUMPTION/OPEN QUESTION vocabulary (`PROJECT_RULES.md`
  §1) for anything a tool's output implies about protocol meaning — never invent a parallel
  confidence taxonomy (an earlier draft proposal for this tooling area used
  observed/inferred/confirmed; rejected for exactly this reason).
- Test against already-confirmed findings as regression fixtures before trusting a tool on
  something new (`lambda_dispatcher_resolver/SPEC.md` §10's own worked-example discipline,
  generalizing `AGENTS.md` §11's "test against real capture fixtures" rule to APK-analysis tooling).
- Decide each tool's own git-tracking boundary explicitly (source code: yes; anything derived from
  or copied out of the decompiled APK: no) rather than assuming — per the point above.

## Suggested (not binding) priority order, if any of these are pursued

1. An androguard-based general structural code index (below) — natural next step once
   `lambda_dispatcher_resolver` already has androguard wired up; replaces ad hoc
   `grep -rl "extends X"`/`grep -rn "LFoo;->bar("` searches with a real query.
2. UUID extraction + BLE/GATT context reconstruction — the original idea's own core ambition.
3. Limited dataflow analysis — highest risk/effort of the group; do this only after (1) and (2)
   prove the approach is worth the investment, and scope it to single-basic-block tracking only at
   first (per the original proposal's own §6).
4. Schema/payload/version tooling (items below) — independently useful, no dependency on 1-3.

This ordering is a lens, not a commitment — the maintainer may pick any subset in any order.

> **Re-ordering approved 2026-09-16 — `ai-sessions/0025` is the sign-off event, per
> `AI_SESSION_LOG_PROCEDURE.md` §4a's citation requirement (this class of tooling-priority decision
> does not go through the FACT/HYPOTHESIS/ADR gate; see that prompt's own "On authorization to
> proceed" note in its Context section).** Originally proposed 2026-09-16 (`ai-sessions/0024`) —
> item 1 unchanged, but item 4's own "protobuf/`RawMessageInfo` schema batch-extractor" sub-item
> promoted ahead of items 2/3 below.
>
> Reasoning, drawn from a real usage pass (`lambda_dispatcher_resolver resolve-all` run against
> `aie`/`esk`'s full case sets, `ai-sessions/0024_AUDIT_RESULT_2026_09_16.md` Phase 2/Phase 3): items
> 2/3 are both gated on genuinely *new wire/GATT evidence* that this project's own APK-side search has
> already exhausted for the current APK version (`REVERSE_ENGINEERING.md`'s "Full-tree GATT/BLE
> reference sweep" entry found zero `BluetoothGatt` references anywhere in this app's decompiled
> source) — building tooling to *organize* UUID/BLE findings doesn't manufacture new findings to
> organize. The schema batch-extractor has real, current work waiting the moment a new APK version is
> pulled (this procedure's own §2.1 diff-pass) or the 12 never-attributed "candidate rich schemas"
> (`REVERSE_ENGINEERING.md`) are picked up. Revised order: **(1) structural code index, (2) schema
> batch-extractor, (3) UUID/BLE-context reconstruction, (4) limited dataflow, (5) the three remaining
> independently-useful items (wire-payload-vs-schema decoder, APK version-diff, tshark/DLCI-reassignment
> helper) unordered relative to each other, unchanged from the original text.** This ordering is now
> adopted, not merely proposed — the structural code index (item 1) has already been built against
> it (see that idea's own "Implemented 2026-09-16" note above); items 2-5 remain not-yet-built and
> still follow this same order per this file's own header note that nothing here is committed to
> being built until it graduates its own spec document.
>
> **Update (2026-09-16, `ai-sessions/0025` resumption):** item 2 (schema batch-extractor) and item 3
> (UUID/BLE-context reconstruction) are now also built (see each idea's own "Implemented 2026-09-16"
> note above). Item 4 (limited dataflow) and the three remaining unordered items remain not-yet-built
> and still follow this same order.
>
> **Update (2026-09-17, same resumption, continued after a mid-task rate-limit interruption):** item
> 4 (limited dataflow) is now also built (see that idea's own "Implemented 2026-09-16/17" note below).
> The three remaining unordered items (wire-payload-vs-schema decoder, APK version-diff, tshark/DLCI-
> reassignment helper) remain not-yet-built.

---

## Idea: general androguard-based structural code index

**Implemented 2026-09-16 (`ai-sessions/0025`) — v1 only, per the sketch below.** See
[`structural_index/SPEC.md`](structural_index/SPEC.md) for the full design, and
[`structural_index/README.md`](structural_index/README.md) for usage. v1 delivers exactly the
narrowest-useful-version sketch's own single capability (construct/call/field-type-holder search
for one class) and passes all three of its own named acceptance criteria against the real,
locally-decompiled APK (`esk`'s 21 construction sites; `giz.p()`'s sole `ftw.a` caller, reproducing
`ai-sessions/0023`'s `Lgiz;->p(` smali-grep finding from a structured query; the `aie`
non-catalogued-class zero-reference check). The `implements`-query and resource/string-table search
named below as "explicitly deferred to a later version" remain not built — this entry stays open for
whoever picks either of those up next, rather than being marked fully closed.

Generalizes `lambda_dispatcher_resolver`'s Layer 1 beyond just lambda-dispatcher detection into a
standing query tool: "list every class implementing interface X," "find every caller of method
Y" (an XREF search — exactly what a manual `grep -rn "Lgiz;->p("` search stood in for during the
2026-09-15 `gjv.p()` trace, `REVERSE_ENGINEERING.md`'s `frb`-`gjv` entry), "list every subclass of
Z." Cheap once androguard is already a dependency; would remove most remaining need for ad hoc
smali `grep` during a research session.

> **Sketch of a first, narrowest useful version, added 2026-09-16 (`ai-sessions/0024`) — not a
> spec document, per this file's own "graduates out by getting its own spec document" rule; a
> starting point for whoever writes that spec.** Single capability for v1: given a class name
> (short or `defpackage.`-qualified), list every other class/method that (a) constructs it
> (`new X(`-shaped invokes), (b) calls one of its methods, or (c) holds it as a field type — the
> exact query `ai-sessions/0024_AUDIT_RESULT_2026_09_16.md`'s Phase 1 ran by hand three times in one
> session (`esk`'s 20 constructor sites; an 11-class "already catalogued?" check against
> `REVERSE_ENGINEERING.md`; the `gwv` cross-reference collision), and `ai-sessions/0023` ran by hand
> across three separate sessions for `gjv.p()`'s caller before finding it. Reuse
> `lambda_dispatcher_resolver`'s own Layer 1 (`androguard_index.py`) directly — same DEX-structure
> foundation, different query shape. Same output/governance contract as `lambda_dispatcher_resolver`
> (plain JSON, never decides relevance, never writes into `REVERSE_ENGINEERING.md`/`PROTOCOL.md`/
> `DECISIONS.md`). Explicitly deferred to a later version: an `implements`-query (different lookup
> shape, needed for the `MaestroEndpointService` Dagger-multibinding search) and a resource/string-
> table search (a different data source, `apktool-output/res/`, not DEX structure at all).
> Acceptance criterion, mirroring `lambda_dispatcher_resolver/SPEC.md` §10's own worked-example
> discipline: reproduce, from a single query each, the three by-hand lookups named above.

## Idea: UUID extraction + BLE/GATT context reconstruction

**Implemented 2026-09-16 (`ai-sessions/0025` resumption) — v1 only, per the design below.** See
[`uuid_ble_context/SPEC.md`](uuid_ble_context/SPEC.md) for the full design, and
[`uuid_ble_context/README.md`](uuid_ble_context/README.md) for usage. v1 runs a genuinely unseeded
regex sweep for every UUID-shaped literal across `jadx-output/sources/`, tags each occurrence's file
with a textual BLE/GATT/RFCOMM-API co-occurrence signal, and reuses `structural_index`'s own
`find_refs`/`load_apk` directly for the "usage location" half, exactly as this idea's own text below
already anticipated. Passes all 6 of its own named acceptance criteria against the real,
locally-decompiled APK. Used for real APK-RE work the same session: found exactly 6 distinct
UUID-shaped literals in this APK version (5 already known, plus one genuinely new, non-Bluetooth
find — see `REVERSE_ENGINEERING.md`'s UUID register for the full write-up). Byte-reversed-alias
detection and GATT-semantic mapping remain explicitly deferred (`SPEC.md` §2.2/§9), not built this
pass.

The original, broader ambition this backlog's own tooling effort started from: UUID → usage
location → BLE operation (`BluetoothGattCharacteristic`/`writeCharacteristic`/
`setCharacteristicNotification`/etc.) → possible protocol role, as a relationship graph rather than
a bare list of UUID strings. Large scope — likely its own multi-phase spec document if pursued, and
depends on the structural code index above for the "usage location" half.

## Idea: limited dataflow analysis

**Implemented 2026-09-16/17 (`ai-sessions/0025` resumption, continued after a mid-task rate-limit
interruption) — v1 only, exactly the risk-bounded scope this idea's own text below already
prescribed ("scope the first version to straight-line, single-basic-block flows only").** See
[`limited_dataflow/SPEC.md`](limited_dataflow/SPEC.md) for the full design, and
[`limited_dataflow/README.md`](limited_dataflow/README.md) for usage. v1 traces one register
forward, instruction by instruction, through a resolved dispatcher branch or a plain method body
(reusing `lambda_dispatcher_resolver` directly for method/branch location), recognizing exactly
four shapes — alias (`move-object`), cast (`check-cast`), sink use (`invoke-*` argument), and a
basic-block boundary (label/`goto`/`if-*`/switch) — and stopping, never guessing, on anything else
(`ambiguous_redefinition`). Passes all of its own named acceptance criteria (10 pytest cases, 6
against the real APK + 4 synthetic fixtures that always run) against the real, locally-decompiled
APK. **Two of `limited_dataflow/SPEC.md` §10's own acceptance-criteria descriptions were found to be wrong against
the real APK during this build and corrected in place** (not weakened to match a bug): item 1's
claimed `final_status: "reached_end_of_block"` for the `esk` discriminator-19 `v0` trace is actually
`"ambiguous_redefinition"` (the same register slot is legitimately reused later in the same branch
by the RPC send's own 5-second timeout setup, `const-wide/16 v0, 0x5`); item 2's named register
`p1` does not exercise the basic-block-boundary case it's meant to (a `move-result-object p1`
intervenes first) — `v1` does, and is what the test actually uses. **Used for real APK-RE work the
same session**: confirmed the primary regression fixture (`esk` discriminator 19's already-known
`WriteSetting` chain, including the disclosed non-reach of `nqo.e(...)`), the adversarial
basic-block-boundary fixture, and one genuinely new trace on `esk` discriminator 18 (the "Feature
A" write site, `REVERSE_ENGINEERING.md`'s `esk` entry) — see that entry's own update for the
finding. Cross-method/cross-block tracing, constant-propagation, and array-content tracking remain
explicitly deferred (`limited_dataflow/SPEC.md` §2.2/§9), not built this pass; a v2 would need a real interprocedural
call graph, a materially larger undertaking not attempted here.

Tracking a byte-array's construction (`new-array`/`fill-array-data`/`aput-byte`, or equivalent
protobuf-builder calls) forward to the `writeCharacteristic`/`WriteSetting` call site it ends up
at. The highest-risk item on this list — general dataflow across branches/loops is genuinely hard
to get right and to trust. If ever attempted, scope the first version to straight-line,
single-basic-block flows only, and require the same worked-example test discipline as
`lambda_dispatcher_resolver` before it's used on anything not already manually verified.

## Idea: protobuf/`RawMessageInfo` schema batch-extractor

**Implemented 2026-09-16 (`ai-sessions/0025` resumption) — v1 only, per the design below.** See
[`schema_batch_extractor/SPEC.md`](schema_batch_extractor/SPEC.md) for the full design, and
[`schema_batch_extractor/README.md`](schema_batch_extractor/README.md) for usage. v1 batch-decodes
every `new naa(...)` compact-schema construction under `jadx-output/sources/` (807 in this APK
version, matching `REVERSE_ENGINEERING.md`'s own independently-obtained count), reusing
`scripts/decode_rawmessageinfo.py` directly rather than modifying it, and passes all 6 of its own
named acceptance criteria (exact reproduction of `qhr`/`qjc`/`qja`/`nqx`/`qjb`, the whole-tree count,
and a disclosed-limitation regression) against the real, locally-decompiled APK. Used for real APK-RE
work the same session on the 12 "candidate rich schemas" (see `REVERSE_ENGINEERING.md`'s own section
for the full findings — a new `mtn`⊃`msw` nesting and 7 new, previously-uncatalogued class leads). A
follow-up cross-referencing this tool's own register against `structural_index`'s bytecode
field-holder query — to close the "plain `MESSAGE` field" gap `schema_batch_extractor/SPEC.md` §3/§9
discloses — remains
explicitly deferred, not built this pass.

`scripts/decode_rawmessageinfo.py` already recovers one `GeneratedMessageLite` class's schema at a
time (this is how `qhr`/`qjc`/`qjb`/etc. were originally recovered). A batch mode that scans the
whole decompiled tree for every class matching that same generated-message shape and builds a full
schema register automatically would remove most of the remaining manual work in
`REVERSE_ENGINEERING.md`'s own schema-recovery entries.

## Idea: wire-payload-vs-schema auto-decoder

Given raw hex bytes + a DLCI, attempt to decode against every schema/envelope this project already
knows about (the DLCI 0x08 `[Group][Code][Length][Value]` TLV shape, `qhr`'s protobuf oneof, the
Fast Pair Message Stream shape) and report the best structural match. Generalizes the manual
TLV-walk/protobuf-oneof-decode work redone by hand in most `CAP-NNN-FINDINGS.md` sessions (e.g.
`CAP-050-FINDINGS.md` §3's own `scripts/decode_dlci08_tlv.py` one-off) into one reusable, testable
decoder.

## Idea: APK version-diff tool

A class-level and string-level diff between two decompiled APK versions, keyed off
`reverse-engineering/APK_VERSIONS.md`'s own version index. `APK_REVERSE_ENGINEERING_PROCEDURE.md`
already names "the diff-against-previous-version pass" as a planned step; this would be its actual
implementation, relevant the next time Google ships a companion-app update.

## Idea: tshark/DLCI-reassignment helper library

Every capture-analysis session re-derives, by hand, which session-local DLCI number currently
carries which logical channel (RFCOMM DLCI numbers are session-local, not fixed — `CAP-001-FINDINGS.md`
§2, `DECISIONS.md` ADR-018) by matching content signatures (e.g. `CAP-050-FINDINGS.md` §2's own
`"google-pixel-buds-pro-v1"` re-identification after DLCI 0x08 turned out to carry HFP on one
reconnect). A small Python wrapper standardizing "pull every payload for the channel matching
content signature X, across every reconnect in this log" would remove a real, recurring
misattribution risk from that manual process.

---

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/reverse-engineering/tools/BACKLOG.md - https://tedsluis.github.io/opencontrolpixelbudspro2/reverse-engineering/tools/BACKLOG
