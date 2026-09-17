# 0027_MAINTENANCE_RESULT_2026_09_17.md — Trace `ACTION_TRIGGER_CLASSIC_CONNECTION_PRIORITY`'s senders; advance items M, B, and H's remaining leads

**Number:** 0027
**Category:** MAINTENANCE
**Date:** 2026-09-17
**Title:** Trace who sends `BluetoothPriorityReceiver`'s `ACTION_TRIGGER_CLASSIC_CONNECTION_PRIORITY` broadcast within the companion app's own code; run item M's resource/string-table sweep; build `structural_index`'s deferred `implements`-query and use it (plus a field-write search) on item B's Dagger-multibinding assembly site; trace at least one of item H's 7 new schema leads or its 17-class unattributed naming cluster one level further
**Status:** complete

## Phase status table

| Phase | Status | Summary |
|---|---|---|
| 0 | done | Mandatory reading order completed in full (see below). APK version on disk (`reverse-engineering/apk/v1.0.955078536-10253511/`) matches `reverse-engineering/APK_VERSIONS.md`'s only row exactly — no drift, no re-decompilation. All 5 existing tools' test suites re-run before relying on any of them: `lambda_dispatcher_resolver` 14/14, `structural_index` 7/7, `schema_batch_extractor` 12/12, `uuid_ble_context` 9/9, `limited_dataflow` 10/10 — all passing. |
| 1 | done | Exhaustive checked-negative for an in-app sender (JADX + smali, whole tree): the action string, all 3 extras, and the receiver's own class/component name each occur in exactly one place — the receiver's own code and the manifest. No `sendBroadcast`/`PendingIntent`/component-targeted `Intent` construction found anywhere else. 🟡 HYPOTHESIS (not FACT) that the sender is external, based on the `CAPTURE_AUDIO_HOTWORD` permission gate and the receiver's own downstream effect. **New finding**: the receiver forwards to a separate Pixel system app, `com.google.android.apps.pixel.dcservice`'s `BluetoothApiService`/`SetFeatureState` (traced via smali fallback for one JADX-undecompilable call site), gated behind a Phenotype flag check — corrects an earlier "unrelated/out of scope" framing of `dcservice` and newly connects to the long-open "Feature A"/`kjj` mechanism (same service's `GetFeatureState` half). `REVERSE_ENGINEERING.md` and `PROTOCOL.md` §6 updated. |
| 2 | done | Item M — full resource/string-table sweep of `res/values/strings.xml` (base locale, 1069 lines) plus `bools.xml`/`arrays.xml`/`integers.xml` (read in full) and the tiny qualifier-specific `strings.xml` variants. **Headline finding: "Feature A" is named on-screen "Quartz"** — a Bluetooth-LE-Audio-gated sound-quality toggle, mutually exclusive with Multipoint and Spatial Audio, resolving a question open since `ai-sessions/0001`. Also corrects the `aie` entry's "unrelated" framing of the "LargoMr" OOBE mini-app (it's genuinely Buds ANC/head-gesture onboarding). Surfaced one new, unresolved tension (a firmware-update notification advertises "Attention Alerts," a `qjn`("presto")-schema-only feature, as generally available — bears on the existing `qjn`/other-product HYPOTHESIS, not resolved). Several checked negatives reconfirmed from this independent data source (no "priority"/"classic"/"gfps"/"hid"/"gsnd"/"phenotype"/"dcservice"/"streamz"/"clearcut" strings anywhere). `REVERSE_ENGINEERING.md` updated with a new "Resource/string-table sweep" section. |
| 3 | done | Item B resolved outright. Built and tested (17/17 pytest, up from 7) two new `structural_index` v1.1 commands: `implements` (direct-`implements`-clause query) and `field-writes` (iput/sput site search) — both validated against independently-verified real-APK fixtures (`fya`'s 2 direct implementers vs. `fyw`/`fyx`'s transitive-only status; `BluetoothPriorityReceiver.c`'s Hilt-injector write site). `field-writes --class MaestroEndpointService --field b` found the field's sole write site: `ghl.onCreate()` (the Hilt injection base class) assigns it directly from `lrw.b`, Guava's own zero-entry `ImmutableMap` singleton — **no gRPC service is registered on `MaestroEndpointService` at all in this APK version**, a hardcoded empty constant, not a hidden Dagger multibinding (explaining why no provider class was ever findable). Incidental correction: `ofd` is an abstract class, not an "interface" as previously documented. `REVERSE_ENGINEERING.md`, `PROTOCOL.md` §6, `TODO.md`, and the tool's own spec/usage docs updated. |
| 4 | done | Item H — traced `fwe` (`nef`'s own field-type holder) one level further via `structural_index refs`. **Resolved, not merely narrowed**: `fwe` is a binary-blob-to-protobuf decoder factory whose methods construct `nca` and `ndi` (2 of the 12 candidate rich schemas) among ~26 sub-message types, assembled into `nef` (74 fields). `fwe`'s sole constructor caller, `fwk`, `implements gak` — the *same* interface `gbu`/`KpiEventCollector` (already documented) implements — confirming this is the companion app's own **KPI (Key Performance Indicator) device-telemetry pipeline**, a second concrete instance of an already-catalogued mechanism, not a new one. Self-describing logs ("KPI event OTTS result", "KPI event OTA status") confirm this and attribute `nbm` (one of the 17-class naming cluster) as the KPI-event outer wrapper holding `nef`. Sharpens the 2026-09-16 pass's "plausibly...diagnostics subsystem" guess into a concrete, named mechanism. 24 further sub-message-type leads surfaced, not chased. `REVERSE_ENGINEERING.md` updated. |
| 5 | done | Cross-checked every finding against `PROTOCOL.md`/`DECISIONS.md`/`DESKRESEARCH_FINDINGS.md`/`TODO.md`/`captures/`. See below. |
| 6 | done | Wrap-up and maintainer summary below. |

## Mandatory reading order — completed this session

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8, all done in full this session:
`AGENTS.md` (full, via system context), `PROJECT.md`, `PROJECT_RULES.md`, `ARCHITECTURE.md`,
`DECISIONS.md` (full ADR history, ADR-001 through ADR-029), `PROTOCOL.md` §6 in full (Framing/
Commands & schemas/Behavior/Resolved subsections, ~1650–2790), `TODO.md` (full), `REVERSE_ENGINEERING.md`
(full — every substantive entry read, including `BluetoothPriorityReceiver`, `MaestroEndpointService`,
"Candidate rich schemas," the `qhr`/`fye`/`esk`/`frb`-`gjv` entries with all dated updates, UUID
register, Message Group/Code register, the GSND naming lead, Native libraries, Call graph notes,
Correlation status, Known limitations), all 5 tools' own spec/usage docs in full
(`lambda_dispatcher_resolver`, `structural_index` — including its §2.2 deferred-`implements`-query
note — `schema_batch_extractor`, `uuid_ble_context`, `limited_dataflow`),
`reverse-engineering/tools/BACKLOG.md` (full), `APK_REVERSE_ENGINEERING_PROCEDURE.md` (full),
`AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md` (full), and
`ai-sessions/0024_AUDIT_RESULT_2026_09_16.md` / `0025_MAINTENANCE_RESULT_2026_09_16.md` /
`0026_MAINTENANCE_RESULT_2026_09_17.md` in full.

## Phase 0 results

- APK version check: `reverse-engineering/apk/v1.0.955078536-10253511/` matches
  `reverse-engineering/APK_VERSIONS.md`'s only row exactly.
- All 5 tool test suites re-run and confirmed green (52/52 total) before relying on or extending any
  of them — see the phase table above for per-tool counts.

## Phase 5 — Cross-checks

Every Phase 1–4 finding was checked against `DECISIONS.md`'s full ADR history (read in Phase 0),
`DESKRESEARCH_FINDINGS.md`, `TODO.md`, and every existing `CAP-NNN-FINDINGS.md` under `captures/`:

- `grep -in` for `BluetoothPriorityReceiver`/`dcservice`/`"Feature A"`/`Quartz`/
  `MaestroEndpointService`/`presto_mr1`/`fwe`/`nef`/`nbm`/`LargoMr` across `DESKRESEARCH_FINDINGS.md`
  and every `captures/CAP-NNN-*/CAP-NNN-FINDINGS.md` — **zero hits everywhere.** This is a genuine,
  expected "no existing mention" (none of this session's findings have ever been wire-correlated or
  previously deskresearched) rather than a gap to explain — no corroboration or conflict to reconcile.
- `grep -in` for `dcservice`/`Quartz`/`MaestroEndpointService`/`BluetoothPriorityReceiver` across
  `DECISIONS.md` — zero hits, consistent with none of this session's findings touching any existing
  ADR's own subject matter. No contradiction possible or found.
- `PROTOCOL.md`/`TODO.md`/`REVERSE_ENGINEERING.md` were updated directly as each finding was made
  (Phases 1–4 above), not as a separate afterward step — each update was itself checked against the
  surrounding text for consistency before being written (e.g. the `ofd`-is-an-abstract-class
  correction was cross-checked against every existing citation of `ofd` in both documents before
  editing).
- **No contradiction found anywhere** — every cross-check was either a match, a resolution/extension
  of an existing open item, or a genuine "no existing mention." Nothing needed to be flagged and left
  unresolved for the maintainer.

## Phase 6 — Wrap-up

- **Lint/footer checks re-run**: `python3 scripts/lint_docs.py` and `./scripts/ensure_footers.py`.
  Two issues introduced by this session's own edits to `REVERSE_ENGINEERING.md` were found and
  fixed: an unqualified same-directory-relative spec-document cross-reference in the new
  `MaestroEndpointService` update (qualified to its full repo-relative path) and a reference to an
  out-of-repo auto-memory filename in the same document's new resource-sweep section (rephrased to
  cite the in-repo `qjn`/`qjt`/`qhx`/`qjv` entry directly instead, since the memory file isn't part
  of this git repository and isn't resolvable by another session). This RESULT file's own missing
  footer was added by `ensure_footers.py`. Every other lint hit — the `CAP-039`/`040`/`046`/`047`/
  `048`/`049` capture folders' own dead image/log filename references, several pre-existing
  unqualified spec-document mentions in `TODO.md` and elsewhere, two already-registered Test-ID
  false-positives, and the gitignored `.venv`-internal third-party package doc footers — is
  pre-existing, not touched by this session's own edits, and left as-is per this project's
  "pre-existing noise elsewhere is not
  this session's responsibility" convention.
- **Every substantive claim in the files this session modified cites a file+line** (e.g.
  `fqm.java:2710`, `ghl.java`'s `onCreate()` body, `lrw.java`'s static field, `fwk.java:29`,
  `strings.xml:960`) or an existing document's own section/entry, per `PROJECT_RULES.md` rule 3 —
  re-read directly before this wrap-up, not assumed.
- `ai-sessions/INDEX.md`'s row for `0027` updated to `complete`.
- **Nothing promoted to 🟢 FACT as a protocol-behavior claim, and no `DECISIONS.md` ADR was written
  or altered** — every FACT label used this session (the `MaestroEndpointService`/`lrw` empty-map
  finding, the `fwe`/`fwk`/KPI-pipeline finding) is a mechanical code-existence/structure fact, which
  per this project's own established practice does not require maintainer sign-off (unlike a
  protocol-behavior/wire claim). Every genuinely interpretive or externally-directed reading (the
  external-sender HYPOTHESIS for `BluetoothPriorityReceiver`, the "Feature A"↔`dcservice` connection,
  the `presto_mr1`/`qjn` tension) is explicitly labeled 🟡 HYPOTHESIS or 🔴 OPEN QUESTION, not FACT.

### Final summary for the maintainer

**1. Who sends `ACTION_TRIGGER_CLASSIC_CONNECTION_PRIORITY`?** No sender exists anywhere in the
companion app's own decompiled code — an exhaustive search (the action string, all 3 extras, and the
receiver's component name, across both JADX and `apktool` smali) found each of them in exactly one
place: the receiver's own code and the manifest. This is a genuine checked negative, not an
unexamined gap. 🟡 HYPOTHESIS, not proven: the sender is plausibly external and privileged, based on
the receiver's own `CAPTURE_AUDIO_HOTWORD` permission gate and its downstream effect — which turned
out to be the real prize of this phase: on receipt, the companion app forwards the request to a
**separate Pixel system app**, `com.google.android.apps.pixel.dcservice`'s own
`BluetoothApiService`/`SetFeatureState` gRPC method (after checking it's a known Maestro device, a
Bluetooth-profile-connection status, and a Google Phenotype server-side flag). This means the actual
"classic connection priority" mechanism is implemented by `dcservice`, not this companion app — a
capture-territory question if pursued further (bracketing a Multipoint switch or multi-device
scenario while watching `adb shell dumpsys activity broadcasts`), not something static analysis can
resolve further. This also corrects an earlier framing (`MaestroEndpointService`'s entry) that called
a `dcservice` reference "unrelated" — it has its own Bluetooth-specific API surface — and newly
connects to the long-open "Feature A" mechanism (below).

**2. Item M's sweep result.** A full sweep of `res/values/strings.xml` (plus the small
`bools.xml`/`arrays.xml`/`integers.xml` files) found the headline answer to a question open since
`ai-sessions/0001`: **"Feature A" is named on-screen "Quartz"** — a sound-quality toggle that
requires LE Audio and is mutually exclusive with both Multipoint and Spatial Audio. It also corrected
the `aie` entry's framing of the "LargoMr" OOBE mini-app (genuinely Buds ANC/head-gesture onboarding,
not an unrelated feature) and surfaced one new, unresolved tension worth a future look: a
firmware-update notification advertises "Attention Alerts" (a feature this project's own static
analysis has structurally tied to a *different* product's schema, not the Buds Pro 2's own) as a
generally-available Pixel Buds feature — flagged, not resolved either way. Several existing
checked-negatives (no "priority"/"classic"/"gfps"/"hid"/"gsnd" strings, etc.) were reconfirmed from
this independent data source.

**3. Item B's status after both named approaches.** **Resolved outright**, not merely narrowed. Both
approaches were built into `structural_index` as new v1.1 commands (`implements` and `field-writes`,
both tested against independently-verified real fixtures) — the field-write search cracked it on its
first real use: `MaestroEndpointService.b`'s sole write site (`ghl.onCreate()`, the Hilt injection
base class) assigns it directly from `lrw.b`, Guava's own zero-entry `ImmutableMap` singleton, with
no Dagger provider call anywhere in the chain. **No gRPC service is registered on
`MaestroEndpointService` at all, in this APK version** — a hardcoded empty constant, not a hidden
multibinding, which is exactly why no provider class was ever findable across three prior search
passes. Incidental correction found along the way: `ofd` is an abstract class, not an "interface" as
this project's own prior documentation called it.

**4. What was found tracing item H's leads, and what remains untraced.** One deep trace, not a
shallow pass across many: `fwe` (already known as one of `nef`'s own field-type holders) turned out to
be a binary-blob-to-protobuf decoder factory whose own methods construct `nca` and `ndi` — 2 of the
12 "candidate rich schemas" — among roughly 26 sub-message types, all assembled into `nef` (74
fields). Its sole constructor caller, `fwk`, implements the *same* interface (`gak`) this project's
own already-documented `gbu`/`KpiEventCollector` implements — confirming this whole cluster is the
companion app's own **KPI (Key Performance Indicator) device-telemetry pipeline**, a second,
independent instance of an already-known mechanism, not a new, unrelated one. Self-describing log
messages ("KPI event OTTS result," "KPI event OTA status") additionally attribute `nbm` (one of the
17-class naming cluster) as the KPI-event outer wrapper holding `nef`. This sharpens the 2026-09-16
pass's own "plausibly...a bundled feedback/diagnostics reporting subsystem" guess into a concrete,
named, already-partially-documented mechanism. **Still untraced**: the 7 originally-named new schema
leads (`mqm`/`mra`/`mqk`/`mtg`/`qak`/`qaz`/`qam`), the remaining 15 members of the 17-class naming
cluster, and 24 further sub-message-type leads this pass's own trace of `fwe` surfaced along the way
— none chased further this session, per its own "trace one promising lead, then stop" scoping.

## Uncommitted (as of the original 6-phase pass)

Nothing has been committed to git this session. Files modified: `REVERSE_ENGINEERING.md`,
`PROTOCOL.md`, `TODO.md`, `ai-sessions/INDEX.md`, and, under
`reverse-engineering/tools/structural_index/`: its own spec and usage docs plus
`src/structural_index/xref_index.py`, `src/structural_index/models.py`, `src/structural_index/cli.py`,
and `tests/test_xref_index.py` — and this file. No `git add`/`git commit`/`git push` was run at any
point this session — session bookkeeping stayed manual throughout, per this prompt's own
instructions.

*(This work was subsequently committed and pushed, `f1b8eb8` on `main`, in the same chat session
before the continuation below was requested.)*

## Continuation, 2026-09-17 (same chat session) — item M's remaining lead traced

The maintainer asked to continue with "item M's remaining leads" after the original 6-phase pass
above (and its commit) was already complete — this is bonus follow-on work in the same conversation,
not a resumption of an unfinished phase, so this file's own `Status` stays `complete`; this section
records what the follow-on work found rather than reopening the phase table above.

**Traced the `presto_mr1` open tension** (this file's own Phase 2 summary and
`REVERSE_ENGINEERING.md`'s "Resource/string-table sweep" section's own flagged item) one level
further, via `structural_index refs --class gnx --method w` (the sole setter of the
`"key_has_presto_pre_mr1_device"` SharedPreferences flag) → `fpz.java` discriminators 4/5, both
self-describingly logged `"markPrestoPreMR1Device"`. **Finding**: "Presto MR1" is a live,
per-already-known-device firmware-capability check (feature index 6, via a remote-config-driven
`ggq`/`ggs` mechanism), run over every device this app installation has already recognized as
supported (`gnx.e()`'s own `"known_supported_devices"` set) — not a hardcoded, different-product
comparison. This is genuinely new evidence that "Presto" is plausibly a firmware/platform-*generation*
codename evaluated live against whichever devices the app already knows (which would include this
project's own Buds Pro 2 unit), independent of the `qjn` entry's own `fyo`/`fyw`/`fyx`
disjoint-DI-provider structural finding — it neither confirms nor overrides that existing HYPOTHESIS,
only adds a genuinely new, separately-sourced data point that nuances how the `presto_mr1` notification
strings should be read. Still open: what feature index 6 itself represents, and whether this check has
ever fired for the maintainer's own paired unit (capture-territory, not attempted). Full trace, with
file+line citations, recorded in `REVERSE_ENGINEERING.md`'s "Resource/string-table sweep" section's
own dated update and a matching cross-reference note added to the `qjn`/`qjt`/`qhx`/`qjv` entry.

No further item-M leads were chased beyond this one trace, matching the original prompt's own "trace
one promising lead, then stop" scoping for this kind of item.

### Further continuation, same session — item H's remaining leads traced

The maintainer then asked to continue with "item H's remaining leads." Picked up the 17-class
naming-cluster's own still-unattributed members via their already-known parent schemas: `qaa`'s 3
holders (`kii`/`koq`/`pzr`) and `qbu`'s 4 holders (`kip`/`kiq`/`kob`/`kol`).

**Resolved, both as checked negatives for Bluetooth relevance**: `qaa` (63 fields) and its holder
`kii` is directly self-describing — `kii.toString()` literally formats itself as `"StatsRecord:..
Primes version: %d.."`, and `kii`'s sole construction site sits inside a method that builds `qaa`
field-by-field immediately after importing `android.os.health.HealthStats` — `qaa`/`kii`/`koq`/`pzr`
are Google's own **Primes** performance-monitoring library's battery/power-usage stats-snapshot
record. `qbu` (24 fields) and 3 of its 4 holders resolved the same way: `kip` is a Builder class whose
own field names (`isEventNameConstant`, `metric`, `isUnsampled`, `shouldAttachActiveTraces`,
`maxActiveTraces`, `activeTracePredicate`, `debugLogsSize`) match Primes' own known metric-definition
builder shape, and `kiq` (the object it builds) is reached, via `structural_index refs`, from
`kkl.uncaughtException` — confirming this whole cluster sits inside Primes' own crash/exception
-reporting machinery, not Bluetooth/Maestro. `kol` (the 4th holder) exists only in `apktool` smali (a
genuine JADX-misdecompile) — not opened, since the other 3 already gave a conclusive answer.

**Checked, not resolved**: `msw` and its 5 holders (`jaj`/`jjn`/`jjx`/`jkl`/`jsg`) — read directly;
`jjn`'s own coroutine-continuation chain touches Android's `NotificationCompat.Builder` (`cce`,
already known from `gci`'s entry) and builds a `PendingIntent`, plausibly OTA/update-notification
-adjacent, but no self-describing name or log message was found — recorded as an honest "read it,
still don't know," not force-fit into a guess.

This leaves the 17-class naming cluster down to 9 genuinely unattributed members
(`jau`/`msc`/`jaj`/`jjn`/`jjx`/`jkl`/`jsg`/`qan`, plus `nbm` already attributed to the KPI pipeline in
the prior continuation above) — the 7 new schema leads (`mqm`/`mra`/`mqk`/`mtg`/`qak`/`qaz`/`qam`)
remain untraced. Full trace recorded in `REVERSE_ENGINEERING.md`'s "Candidate rich schemas" section's
own new dated update.

### Final continuation, same session — item H's entire remaining inventory closed

The maintainer asked once more to continue item H's remaining leads. One further hop past the `msw`
"checked, not resolved" note above closed it completely: `jjy.java` (the class `jjn`'s own
continuation wraps) carries the identical `lud.m("GnpSdk")` logger tag already confirmed for the
`mtn`/`jau` cluster — so `msw`/`jaj`/`jjn`/`jjx`/`jkl`/`jsg` are GNP SDK too, not
notification/OTA-specific to this app as speculated.

The remaining leads fell the same way: `qan` turned out to be held as a field of `qbu` (the
already-resolved Primes metric class) — nested inside an already-attributed schema, not independent.
`qak` is both held by `qbu` *and* called directly from the real, unobfuscated
`com.google.android.libraries.performance.primes.transmitter.clearcut.ClearcutMetricSnapshotTransmitter`
class — Primes, confirmed twice over. `qaj`/`qaz`/`qam` inherit that attribution by nesting inside
`qaj`/`qak`'s own schema. On the GNP side, `mqm`/`mra`/`mqk` are each held by a new class
(`jik`/`jyl`/`jfs`), all three of the identical coroutine-continuation shape as `jau`/`jjn`, and their
own wrapped classes both carry `"GnpSdk"` too; `mtg` inherits the same attribution by nesting.

**Item H's entire original inventory — all 12 candidate rich schemas, all 17 naming-cluster members,
and all 7 new schema leads — is now closed.** Everything resolves to one of exactly three bundled
Google-internal libraries: this app's own KPI/device-telemetry pipeline (the one cluster genuinely
specific to this app, already documented in the first continuation above), Google's **Primes**
performance-monitoring library, and Google's **GNP** notification-platform SDK. This is a
comprehensive checked negative for Bluetooth/`libmaestro` relevance across the entire original
inventory — a complete, specific answer to what the 2026-09-16 pass's own "plausibly a bundled
feedback/diagnostics reporting subsystem" guess was pointing at, not one subsystem but three, all
now named. Full trace in `REVERSE_ENGINEERING.md`'s "Candidate rich schemas" section's own final
dated update.

### Further continuation, same session — item M's last remaining thread closed

Asked once more to continue item M's remaining leads. The one still-open thread was "what feature
index 6 (the `presto_mr1`/`markPrestoPreMR1Device` gate) itself represents." Checked exhaustively:
a full-tree survey of every other `ggs`/`ggq`-typed `.c(<index>)` call site finds ~25 other numeric
indices used the same way across a dozen-plus unrelated files, confirming this is a large, generic,
~40-feature remote capability-gate system, not something built specifically for "presto MR1." Feature
6 itself has exactly the two call sites already found — no third site exists to name it against. The
remote-config protobuf messages (`ggv`/`ggw`) that back this system decode to plain, unnamed fields
(`b`/`c`/`d`/`e`/`f`) with no string label anywhere — a numeric feature-ID/minimum-version tuple whose
human-readable name, if any, lives only in Google's own backend config, never shipped in this APK.
**This is a genuine, exhausted static-analysis dead end, not an unattempted gap** — item M is now
closed; nothing further to trace from this session's own sweep.

### Further continuation, same session, after `ai-sessions/0028`'s sign-off — item B's own remaining
lead traced (does not change item B's own already-closed conclusion)

Asked to continue item B's remaining leads, after the maintainer's `ai-sessions/0028` sign-off on the
findings above. Item B's own literal question ("which gRPC service names are registered") was already
definitively closed in this file's first pass — this continuation re-confirmed that conclusion
independently, from a source not previously read in full, and surfaced one narrower, genuinely open
detail that does not reopen it.

Read `MaestroEndpointService`'s own `onCreate()` (distinct from `ghl.onCreate()`'s injection of the
`b` field itself) via the `apktool` smali fallback, start to finish (~599 instructions). Confirmed,
byte-for-byte, that the per-service `Map` iteration runs over zero entries (independently
re-confirming the already-established empty-`b` finding) — and found a further detail: the resulting
local `HashMap`'s conversion to an immutable map (`Llov;->g(...)`) has its own return value discarded
(no `move-result-object` follows it), meaning even that conversion step is vestigial, not just the map
itself. Also traced `onBind(Intent)`'s own returned value (`oez.a`, cast to `IBinder`) far enough to
confirm it genuinely does get written to somewhere inside this same `onCreate()` method (via
`structural_index field-writes`) — but tracing *what* value ends up there would require careful,
register-by-register tracing across the full 599-instruction method with heavy register reuse, which
is both outside this pass's own bounded scope and outside `limited_dataflow`'s own disclosed
single-basic-block-only capability. One near-misreading was caught and corrected mid-trace (an early
reading briefly attributed an unrelated `ofa.a`/`Logt`-object construction to the per-service map,
before re-checking the raw instruction order corrected it) — recorded honestly in
`REVERSE_ENGINEERING.md`'s own write-up as a caught, not repeated, mistake.

**Item B's own conclusion is unchanged and now doubly confirmed**: no gRPC service is registered on
`MaestroEndpointService` in this APK version. The one new open thread (what `onBind()` would actually
return) is a narrower, separate question, not part of item B's own original ask — flagged, not
chased further. Full trace in `REVERSE_ENGINEERING.md`'s `MaestroEndpointService` entry's own newest
dated update.

### Further continuation, same session — item H's one genuinely remaining candidate (`gdw`) resolved

Asked once more to continue item H's remaining leads. Re-checked the original 12 candidate rich
schemas against the prior three closure passes and found one that had never actually been traced to
an attribution: `gdw` (28 fields) — the "Item H is closed" write-up had listed it as resolved by
omission, but no trace for it existed anywhere in this session's own work.

`structural_index refs --class gdw` shows its sole external field-type holder is `gdb.f` — and `gdb`
turns out to be a class this document's own `gbu`/KPI entry had *already* cited, without having named
`gdw` as its own field-`f` payload: `gcl.B()` (the already-extensively-documented diagnostics/
telemetry-consent check that gates both the KPI-event path and the settings-event Clearcut path)
reads exactly this field. **`gdw` is the sub-message `gcl.B()`'s own consent check reads** — part of
this app's own local device-info/consent-flag storage model (the same `gcl`/`gdb`/`gck`/`gcp`/`gcn`
pathway already documented elsewhere), not a fourth mystery library and not Bluetooth-relevant either.

**Item H's entire original inventory is now genuinely, fully closed — all 12 of the original
candidate rich schemas are accounted for.** Full trace in `REVERSE_ENGINEERING.md`'s "Candidate rich
schemas" section's own newest dated update.

### Further continuation, same session — item M's own last remaining sub-thread (the `fpz` case-5
"device type == 1" condition) traced to its own origin and closed

Asked once more to continue item M's remaining leads. Re-checked the `presto_mr1` write-up's own
prior text and found a genuinely never-chased thread flagged there: `fpz.java`'s case 5 guards
`"markPrestoPreMR1Device"` behind *two* conditions — feature-6 non-support (already traced) **and**
`((Integer) obj3).intValue() == 1`, this second integer never previously traced to its source.

Traced the full chain: `fpz`'s case-4 combiner pairs feature-6 support with `fsxVar.e.c(str)` —
`fsx.e` is `gck` (the already-documented device-info repository). `gck.c(str)` routes through the
same R8-merged `gci` dispatcher already read for the `presto_mr1` notification itself, this time
discriminator 0, whose own log string self-describes the stream's contents: `"doAfterNext, device
address: %s, device type: %d"`. So `fpz` case 5's second condition is gated on the connected device's
own reported **"device type"** integer equalling `1`.

Traced "device type" further upstream via `structural_index refs --class gea --method V` (interface
-indirection technique, since the concrete class's own method had no direct callers) to its sole
caller, `gaa.a(qiv)` — `qiv` being the already-documented `GetHardwareInfo` pw_rpc response type.
`gaa.a()`'s body decodes "device type" directly from **`qiv.e`** (field 1, `ENUM`), defaulting to `1`
whenever the wire value is unset/`0`. So this is a live, per-connected-device hardware-reported value,
not an app-side hardcoded marker.

Attempted to resolve what ordinal `1` itself *means* by name: `qiv.e`'s validity check routes to
`qgx.m` (index 12 of the already-documented generic multi-type `qgx` dispatcher), which itself calls
`pld.ap(int)` — read directly, this is a generic, unnamed protobuf-lite ordinal identity/validity
remap (valid ordinals map to themselves, invalid ones default to `0`), not a self-describing named
enum the way e.g. `qhs`'s `ANC_STATE_*` values are. **No human-readable name for value `1` (or any
other value this field can take) exists anywhere in the decompiled tree — a second, independently
-reached exhausted dead end, the same character as the earlier feature-index-6 finding, not a first
one glossed over.**

**Item M's own true final thread is now closed, genuinely traced rather than asserted closed by
omission** — both of its previously-flagged-but-unchased sub-threads (feature index 6's name, and
this "device type == 1" condition) have now actually been chased to their own exhausted ends. Full
trace in `REVERSE_ENGINEERING.md`'s "Resource/string-table sweep" section's own newest dated update.

### Further continuation, same session — item H's actual 4th remaining pool (28 leads from `fwe`'s own
sub-message parsers and `fwk`'s remaining logged outputs) traced; 10 resolved, 18 honestly left open

Asked once more to continue item H's remaining leads. Re-checked every prior item-H closure claim
against the document's own text and found that the "item H's entire inventory is now closed" claims
(the 12 candidate schemas, the 17-class naming cluster, the 7 new schema leads) never actually covered
a separate, 4th pool that document's own 2026-09-17 `fwe`/`fwk` update had explicitly enumerated as
"genuinely new, previously-uncatalogued leads, none individually traced further this pass": 24 classes
`fwe.java`'s own private `A`–`z` byte-stream parser methods construct, plus 4 more (`ndf`/`nel`/`nem`/
`ndg`) from `fwk`'s own two logged methods. This was a real gap in the "closed" framing, not a
manufactured one — worth flagging plainly rather than quietly working around.

Mapped all 26 of `fwe`'s private parser methods to their own output classes by direct line-number
reading (`fwe.A`→`nec`, `fwe.B`→`nee`, `fwe.H`→`ncu`, `fwe.d`→`nbu`, and so on through `fwe.z`→`neb`).
Searched each method's own body for self-describing string literals (the same "field X is not set"
exception-message technique already used elsewhere in this document) and found real field names for
6 of the 24: `nbu` (`audio_stream_context_mask`, `status bits`), `nbv` (`audio_sample_rate`,
`audio_stream_context_mask`, `status bits`), `nbz` (`host_session_id`), `ncn` (`is_primary`,
`host_session_id`, `audio_session_id`, `sequence_id`), `ncw` (`status bits`), plus a **logged**
"Receive crash happened event" string for `ncf` (a crash-report telemetry record, not connection
-quality). Two already-known classes (`nca`, `ndi`) got sharpened from "some KPI sub-message" to
concrete field names too: `afh_map_tw`/`tw_max_per`/`tw_per_average`/`role_switch_happened` (standard
Bluetooth Classic AFH/role-switch link-quality terms) plus `audio_sample_rate` (`nca`) or
`jitter_buffer_size` (`ndi`).

Also read `fwe.b(myk, qhd)`'s own dispatch fully: cases 5/6/7 of its type-selector all three parse into
the *same* `nch` class (cases 2/3/4 each map to their own distinct class) — a new structural fact, but
no field names recoverable for `ncs`/`ndm`/`ncx`/`nch` themselves (standard wire-parser path, no
exception text).

Read `fwk`'s remaining logged method (`fwk.n`, "KPI event OTA status...") in full and matched its own
5-placeholder log format to its own field-assignment order, resolving all 4 of its previously
-uncatalogued output classes: `ndf` (OTA-status record: status/isManual/duration/from-version/target
-bundle-version fields, one field left genuinely unnamed since it isn't one of the log's own
placeholders), `nel` (a 3-slot "from version" record), `nem` (a per-version-component record used 3x
inside `nel`), and `ndg` (already-partially-seen via `fwk.c`'s own "OTTS result" log — a small
left/right-earbud pairing record nested inside the already-documented `nef`/`nbm` chain).

**Explicitly not claimed closed**: 18 of the 24 `fwe` classes (`nec`/`nee`/`ncu`/`nby`/`ncg`/`nck`/
`ncl`/`ncm`/`ncv`/`ndb`/`nde`/`ndn`/`ndo`/`ndq`/`ndu`/`ndw`/`ndy`/`neb`) plus `ncs`/`ndm`/`ncx`/`nch`'s
own field-level content carry no self-describing text anywhere in their own constructing method —
checked directly, not merely grepped past, and reported as genuinely still open, not glossed over. All
resolutions in this pass are Bluetooth Classic connection-quality/audio-session/crash/OTA-status
*telemetry* the official app collects about itself, not a control-channel schema — same caveat this
document's `gbu`/KPI entry already carries, repeated rather than silently assumed. Full trace in
`REVERSE_ENGINEERING.md`'s "Candidate rich schemas" section's own newest dated update.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0027_MAINTENANCE_RESULT_2026_09_17.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0027_MAINTENANCE_RESULT_2026_09_17
