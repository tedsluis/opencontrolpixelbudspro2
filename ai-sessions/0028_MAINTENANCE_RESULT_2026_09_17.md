# 0028_MAINTENANCE_RESULT_2026_09_17.md — Record maintainer sign-off on `ai-sessions/0027`'s findings

**Number:** 0028
**Category:** MAINTENANCE
**Date:** 2026-09-17
**Title:** Record the maintainer's review and sign-off, given directly in this chat session, on every code-level finding produced by `ai-sessions/0027` and its three same-session continuations
**Status:** complete

## What happened, in order

1. **`ai-sessions/0027`'s own 6-phase pass** (commit `f1b8eb8`) traced `BluetoothPriorityReceiver`'s
   sender (checked negative; found it forwards to `dcservice`'s `BluetoothApiService`), swept item M's
   resource strings (found "Feature A" = "Quartz"), resolved item B (`MaestroEndpointService.b` is a
   hardcoded empty map), and resolved part of item H (the KPI/device-telemetry pipeline).
2. **Continuation 1** (commit `afd5079`) traced item M's `presto_mr1` tension: `"markPrestoPreMR1Device"`
   is a live, per-already-known-device firmware-capability check, not a hardcoded different-product
   marker.
3. **Continuation 2** (commit `b8a8a41`) resolved 2 more of item H's candidate schemas (`qaa`, `qbu`)
   and 7 more of its 17-class naming cluster as Google's **Primes** library.
4. **Continuation 3** (commit `f5233e0`) closed item H's entire remaining inventory: the last
   unattributed cluster (`msw` and its holders, plus the 7 new schema leads) resolved to Google's
   **GNP** notification-platform SDK and, via nesting, more of the Primes cluster.
5. **Continuation 4** (commit `fcbe1cb`) closed item M's own last thread: feature index 6's name is a
   genuine, exhausted static-analysis dead end (a large generic ~40-feature remote capability-gate
   system with no string labels anywhere in the APK).
6. **Verification pass** (this same chat session, immediately before this entry): checked every 🟢
   FACT label introduced across all four passes against `AGENTS.md` §6's actual gate — confirmed none
   is a protocol-behavior/wire claim (all are mechanical code-existence/structure facts: literal
   `toString()` output, literal logger-tag strings, a literal field assignment), and confirmed no
   `DECISIONS.md` entry was touched — so none of them strictly required the formal sign-off gate in
   the first place, per this project's own established convention (`ai-sessions/0025`'s own precedent:
   "code-level-only, not requiring maintainer FACT sign-off since it makes no protocol-behavior
   claim"). One internal-consistency nit was flagged (not fixed): `PROTOCOL.md`'s own restatement of
   the `BluetoothPriorityReceiver`→`dcservice` finding doesn't carry an explicit 🟢 FACT tag the way
   its `REVERSE_ENGINEERING.md` source entry does.
7. **Sign-off list compiled and presented** in chat, split into (A) 11 code-level findings (the
   `BluetoothPriorityReceiver` sender checked-negative and `dcservice` forwarding, the `Quartz`
   naming, the `LargoMr` correction, the `presto_mr1` mechanism and its feature-6 dead end, the
   `MaestroEndpointService` empty-map resolution and the `ofd`-is-abstract correction, and item H's
   full KPI/Primes/GNP attribution) and (B) 3 already-correctly-labeled HYPOTHESIS/OPEN QUESTION items
   left untouched, each with a plain-language summary and its file/section location.
8. **Maintainer decision**: approved list A in full, verbatim — **"ik heb alle bevindingen gelezen en
   akkoord bevonden"** ("I have read all the findings and found them acceptable"). List B's items were
   not asked to be promoted and remain exactly as labeled.

## Scope note — what this sign-off does and does not cover

This sign-off closes the *review* of `ai-sessions/0027`'s own code-level findings (list A above). It
does **not**:

- Promote any 🟡 HYPOTHESIS or 🔴 OPEN QUESTION item to 🟢 FACT — every confidence tier `ai-sessions/0027`
  itself assigned (the external-sender reading, the `presto_mr1`/`qjn`-other-product tension, the
  "Feature A"↔`dcservice`-`GetFeatureState` connection) is unchanged.
- Constitute a `DECISIONS.md` ADR — none was written, and none is implied by this entry. Every 🟢 FACT
  label this sign-off covers was already a mechanical code-existence/structure fact under this
  project's own standing convention, not a protocol-behavior claim requiring `AGENTS.md` §6's formal
  gate — this sign-off was sought and given as good practice/transparency (matching `ai-sessions/0026`'s
  own precedent for `0024`/`0025`), not because the gate required it here.
- Resolve any of the genuinely open leads `ai-sessions/0027` itself left open (whether the external
  sender exists, what "Feature A"'s exact backing feature-state key is, whether the `presto_mr1` check
  has ever fired against the maintainer's own paired unit) — all remain capture-territory or
  backend-config-territory, unaffected by this entry.

## What remains genuinely open (not closed by this sign-off)

Everything `ai-sessions/0027`'s own four passes already flagged as open stays open: the external
sender for `ACTION_TRIGGER_CLASSIC_CONNECTION_PRIORITY` (capture-territory); whether "Feature A" and
`BluetoothPriorityReceiver`'s own request are the *same* feature-state key within `dcservice`'s
`BluetoothApiService` (not determined); and whether the `presto_mr1`/feature-6 mechanism has ever
actually fired against the maintainer's own Buds Pro 2 unit (capture-territory, explicitly not
attempted per `ai-sessions/0027`'s own guardrails).

## Uncommitted

Nothing from this specific entry has been committed yet as of this file's own creation — this
prompt/result pair and the `ai-sessions/INDEX.md` row update are the only artifacts of this session
and remain to be committed and pushed in the same turn this file is written.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0028_MAINTENANCE_RESULT_2026_09_17.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0028_MAINTENANCE_RESULT_2026_09_17
