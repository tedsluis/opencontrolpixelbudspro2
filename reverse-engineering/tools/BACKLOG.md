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

> **Proposed re-ordering, added 2026-09-16 (`ai-sessions/0024`, awaiting maintainer sign-off — a
> proposal, not applied as a decision by this edit alone) — item 1 unchanged, but item 4's own
> "protobuf/`RawMessageInfo` schema batch-extractor" sub-item promoted ahead of items 2/3 below.**
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
> helper) unordered relative to each other, unchanged from the original text.** This is a proposal for
> the maintainer to approve or reject, per this file's own header note that nothing here is committed
> to being built.

---

## Idea: general androguard-based structural code index

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

The original, broader ambition this backlog's own tooling effort started from: UUID → usage
location → BLE operation (`BluetoothGattCharacteristic`/`writeCharacteristic`/
`setCharacteristicNotification`/etc.) → possible protocol role, as a relationship graph rather than
a bare list of UUID strings. Large scope — likely its own multi-phase spec document if pursued, and
depends on the structural code index above for the "usage location" half.

## Idea: limited dataflow analysis

Tracking a byte-array's construction (`new-array`/`fill-array-data`/`aput-byte`, or equivalent
protobuf-builder calls) forward to the `writeCharacteristic`/`WriteSetting` call site it ends up
at. The highest-risk item on this list — general dataflow across branches/loops is genuinely hard
to get right and to trust. If ever attempted, scope the first version to straight-line,
single-basic-block flows only, and require the same worked-example test discipline as
`lambda_dispatcher_resolver` before it's used on anything not already manually verified.

## Idea: protobuf/`RawMessageInfo` schema batch-extractor

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
