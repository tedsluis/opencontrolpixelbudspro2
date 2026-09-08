# 0003_MAINTENANCE_RESULT_2026_09_08.md — Execute 0002's Task 4 gap scan (APK RE, cross-checks, consistency, external validation, documentation)

**Number:** 0003
**Category:** MAINTENANCE
**Date:** 2026-09-08
**Title:** Execute 0002's Task 4 gap scan (APK RE, cross-checks, consistency, external validation, documentation)
**Status:** complete

---

## Mandatory reading order — completed

Per `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8, and this prompt's own reading-order section,
the following were read in full before any edit was made: `AGENTS.md` (via the session's own system
context), `PROJECT.md`, `PROJECT_RULES.md`, `DECISIONS.md` (every ADR, ADR-001–ADR-025, including all
dated Update notes), `ARCHITECTURE.md`, `PROTOCOL.md` (full, including §0.1's firmware note, §2's
framing sections, §4's per-command sections, §5's connection lifecycle, §6's open questions in full,
§7's error table, §8's changelog), `TODO.md` (full), `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`,
`ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` (full), `ai-sessions/0002_MAINTENANCE_RESULT_2026_09_08.md`
(full), and `REVERSE_ENGINEERING.md`'s specific named entries (`qhr` in full, the `ijk`/`ijp`/GMS-boundary
entry, the `MaestroDeviceSettingsProviderService` entry, the `frb`/`fuh`/`glk`/`gjv` entry, the
`MaestroEndpointService` entry, plus the APK metadata, Method, UUID register, Message Group/Code
register, Native libraries, Call graph notes, Correlation-status table, and Known limitations sections
for cross-check context). Confirmed the workspace has `reverse-engineering/apk/v1.0.955078536-10253511/`
present on disk (`jadx-output/`, `apktool-output/`, `apktool-output-arm64_v8a/`, `pbtk-output/`) and
`captures/` populated (`CAP-001` onward) — both needed for Phase 3/4 below.

**AI-assistance boundary acknowledged**: per `AGENTS.md` §6/§15 and `DECISIONS.md` ADR-017, this
session runs searches, lists candidates, and explains already-surfaced code/captures, but does not
decide relevance, does not self-promote anything to 🟢 FACT, and does not write/amend a `DECISIONS.md`
ADR. Findings below are proposals for maintainer review unless explicitly marked otherwise.

---

## Phase 1 — Consistency checks & plain documentation updates — done

**Decision points, put to the maintainer directly this session (per the prompt's own instruction),
both answered before any edit was applied:**

1. **`TODO.md`'s stale "Recommended priority order" §3/item 4** — approved as proposed. Applied:
   - §3: closed the "apply ADR-019's method to `qhr` fields 11/15/17/19/22/27/28" language (all 7 now
     closed — 5 on 2026-09-03, the last 2 on 2026-09-08); named the new "current highest-leverage
     single next step" as the `MaestroDeviceSettingsProviderService` 6-case-ID→`qhr`-field forward
     trace (the cheapest of the three open APK-RE leads from `ai-sessions/0001`'s Phase 1/Phase 2 —
     the other two, `MaestroEndpointService`'s smali read and `gjv.p()`'s caller trace, both need
     either that same untried smali fallback or a fresh capture).
   - Item 4: folded `CAP-034`–`CAP-042` into the "done" list; removed the now-stale "Group W's own
     untried cache-busting methods" bullet (`CAP-034`, 2026-09-01, already closed it via
     `pm clear com.android.bluetooth` on a Pixel 9a never before connected to this Buds unit).
   - Also found and fixed one more staleness item while syncing this section (not separately asked,
     since it's the same category of mechanical closure the maintainer just approved): the "Added
     2026-09-05 — six new planned captures" bullet was still phrased as "planned" and unchecked
     (`[ ]`) even though `PROTOCOL.md` §8's 2026-09-06 changelog row already syncs all six captures'
     findings — checked it off and rewrote it to summarize each of `CAP-037`–`CAP-042`'s actual
     results.
2. **`CHANGELOG.md` policy for `MAINTENANCE` sessions** — maintainer chose "No — stays release-scoped."
   No `CHANGELOG.md` entry added for this session; `ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md`
   (this file) remains the permanent record, per `AI_SESSION_LOG_PROCEDURE.md`.

**`README.md` sync** (item 2, no decision point needed — a mechanical staleness fix, not a protocol
claim): "Current state" date header advanced to 2026-09-08; moved Multipoint out of "Still open" and
added it, plus Volume EQ and Volume balance (also promoted to FACT since the 2026-09-07 date this
section last synced against, per `PROTOCOL.md` §4.5.6/§4.5.7), into "Confirmed and implementation-ready".

**Item 4 (spare-time only, not blocking)**: not attempted this session — Phase 2 onward took priority
per the prompt's own instruction not to let this block later phases. Flagged again in Phase 5 below.

**Lint check**: `python3 scripts/lint_docs.py` run after the edits above; introduced one transient
dead-filename false-positive from this session's own `TODO.md` wording (a generic filename-like
pattern), fixed by rewording to avoid the lint pattern. `python3 scripts/ensure_footers.py` applied
the missing footer to this file itself. Remaining lint findings (stale PNG/log filenames in
`CAP-036`/`CAP-038`/`CAP-039`/`CAP-040`'s event-notes/findings files) are pre-existing, unrelated to
this session, and match `ai-sessions/0002`'s own already-flagged item 4 note — not fixed this session
(that item 4 sweep was not reached, see above).

**Files changed this phase**: `TODO.md`, `README.md`, `ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md` (this file, created).

## Phase 2 — Validation against outside sources — done

All five items are negative-result reconfirmations or clarifications, not FACT promotions — applied
directly to `PROTOCOL.md` per the same standard prior HYPOTHESIS-level deskresearch passes used
(`AI_SESSION_LOG_PROCEDURE.md`/`AGENTS.md` §6 gate only its FACT promotions and ADRs, not this kind of
edit). Full quoted text is in `PROTOCOL.md` itself (§4.3 Option A, §4.4/§6's Ring-ACK item, §6's DLCI
0x02 Address item, §2.3's `pbpctrl` addendum, §6's `FE2C1238…`/"Unknown Service" item) and its new
2026-09-08 changelog row — summarized here rather than re-quoted in full a second time:

1. **Battery Notification "shown ≥8s, auto-hidden after 20s" timing claim** — fetched the base
   Message Stream spec page directly (the alternate location `PROTOCOL.md` §4.3 Option A's own
   2026-09-03 note proposed but never checked). Also absent there. Both candidate official pages
   are now checked and both come up empty — this claim currently has no locatable spec citation at
   all. Added a dated note; did not change the tier (already 🟡 HYPOTHESIS) since a downgrade past
   HYPOTHESIS with no better label available isn't warranted, but flagged the claim as
   effectively uncited pending a different source being found.
2. **Ring action's two ACK variants' extra byte(s)** — fetched the acknowledgement spec page's exact
   literal text (not a paraphrase): the worked example's trailing `0x013C` is explicitly glossed by
   the spec itself as *"the current state of the action message group and code, ring right and 60
   seconds timeout"* — a documented 2-byte state (channel + timeout-in-seconds), not an
   undifferentiated status code. Checked both observed variants against this: neither fits (one has
   0 extra bytes, the other exactly 1, not 2) — rules out one candidate explanation (truncated/
   reordered version of the spec's 2-byte state) without resolving the question. Also fetched the
   spec's separately-documented NAK format (leading reason byte) and checked it against both
   variants — doesn't fit either. Genuinely still open, sharpened not closed.
3. **DLCI 0x02 Address-field-renegotiation hypothesis vs. Pigweed's public `pw_rpc` docs** — fetched
   `pigweed.dev/pw_hdlc/`, `pigweed.dev/pw_rpc/`, and its linked design page directly. None of the
   three documents how an HDLC address or RPC channel ID is assigned/negotiated — the `pw_hdlc`
   page's own code example just hardcodes a literal address with no discussion of assignment.
   Genuinely undocumented upstream, not merely unread — this project's own wire-observed behavior
   remains the only evidence for or against the HYPOTHESIS.
4. **`qzed/pbpctrl`'s own published notes, re-checked beyond the already-quoted paragraph** — fetched
   the full document again. Found nothing beyond the same transport-framing paragraph already cited
   (HDLC U-frames wrapping pw_rpc) plus a bare feature list ("noise-cancelling, equalizer, balance,"
   "hardware/firmware information") with zero opcode/byte-layout detail for any of them. Clean
   negative — this project's own independently-derived EQ/ANC/settings findings are neither
   strengthened nor contradicted by anything further here.
5. **`FE2C1238…`'s official name and "Unknown Service" (`109b862f-…`)'s purpose** — fetched the live
   Fast Pair GATT characteristics page directly: lists exactly the 5 already-known spec characteristics
   (`FE2C1233`–`FE2C1237`) and nothing named `FE2C1238`. A web search for the "Unknown Service" UUID
   found zero matches anywhere (not a Bluetooth SIG standard service by that name, no vendor
   documentation). Both reconfirm `CAP-034-FINDINGS.md` §8's existing negative result rather than
   finding anything new. Noted that the Bluetooth SIG's assigned-numbers database is only published
   as a bulk PDF/YAML, not a searchable web page — checking either UUID against that bulk data
   directly is a possible further step, not completed this pass (flagged, not chased, since a web
   search already found zero hits and the marginal value of a bulk-file grep is low).

**Files changed this phase**: `PROTOCOL.md` (5 dated notes + 1 new changelog row).

## Phase 3 — APK reverse-engineering (in progress)

### Item 1 — `MaestroDeviceSettingsProviderService`'s 6 case IDs traced — done

Read `MaestroDeviceSettingsProviderService.java`'s `ct(DeviceInfo, DeviceSettingState)` method in
full, then followed every accessor it calls (`ftj.A(String)` → confirmed to return `fyc`, the same
class already used for the Multipoint/Volume EQ traces) through `fyc.java`'s own source to `fyb`'s
numbered dispatcher (`fyb.java`, read in full) to `fya`'s implementation (`fyo.java`, read in full —
this also surfaced the full field register, correcting two entries). Also traced `ght.p()`/`q()`'s
two `int` arguments through `fms.m(int)`/`fjm.H(int)`, which resolve to self-describing internal
`CATEGORY_*` names — a genuinely new, third kind of code-internal naming evidence (distinct from a
UI-fragment/preference-key trace or a read-side log message).

**Result, quoted in full from the `REVERSE_ENGINEERING.md`/`PROTOCOL.md` updates**:

- `2102` → `qhr` field 2, internal category `CATEGORY_OHD` (corroborates the existing "In-ear
  detection" HYPOTHESIS, not a verbatim match).
- `2103` → `qhr` field 27 (matches the existing category-level identity).
- `2104` → `qhr` field 11 (**Multipoint** — internal category `CATEGORY_MULTIPOINT`, a second
  independent internal-name confirmation), **plus** a second, non-`qhr` write to an unnamed internal
  "Feature A" mechanism, **plus** an unreconciled naming tension (the same logging call also passes
  `fpm.ENABLED_HEAD_GESTURES`, a different enum, as an outcome tag).
- `2113` → `qhr` field 5 (still unnamed).
- `2115` → does **not** touch `qhr` at all — a standalone "Feature A" toggle (`kjj`,
  "premiumAudioHelper" internally).
- `2116` → `qhr` field 32 — a field not previously in this project's register at all, internal
  category `CATEGORY_RV_BLOCK_AUTO_TEST` (diagnostic-sounding, not a recognizable feature).
- **Byproduct**: two `qhr` field-register corrections found while reading `fyo.java` in full — field
  6 has a real write site (`fyo.m`, `fyo.java:191-210`; the register's prior "not found" entry was
  wrong; its own caller wasn't found either, checked exhaustively against `fyb`'s 0–18 cases and the
  only other `fya`-referencing dispatcher, `hqy.java`); field 32 (`fyo.r`) was entirely absent
  before this pass.
- **Checked negative, not just "still open"**: head gestures (`qhr` field 29) is **not** among these
  6 case-ID mappings — `TODO.md`'s own "promising but unconfirmed lead" for case 2104 did not pan
  out (that case maps to Multipoint, field 11, not field 29).

**Decision-point flag for the maintainer (batched with other Phase 3 findings — see the consolidated
question at the end of this phase)**: this finding is strong enough to be a candidate for maintainer
sign-off now — specifically, field 2's `CATEGORY_OHD` corroboration and field 11's second
`CATEGORY_MULTIPOINT` confirmation. Not committed to FACT here; proposed in `PROTOCOL.md`'s new
2026-09-08 update block per `AGENTS.md` §6/ADR-017.

**Files changed**: `REVERSE_ENGINEERING.md` (`MaestroDeviceSettingsProviderService` entry rewritten
with the full trace, `qhr` entry's bonus register corrected for fields 6/32, 2 new correlation-status
rows), `PROTOCOL.md` (§6 open item updated with the full trace, proposed not committed), `TODO.md`
(item closed).

### Item 2 — `apktool` smali fallback read of `MaestroEndpointService.onCreate()` — done, largely closed

Read the 1376-line smali file directly (`apktool-output/smali_classes2/.../MaestroEndpointService.smali`,
method starting at line 123), since JADX cannot decompile this method's bytecode. Traced the
registered-services `Map` field and the interface (`ofd`) whose instances populate it, then followed
its two concrete implementations.

**Result**: the registered-service list is a Dagger/Hilt multibinding (`Map<String, Optional<ofd>>`)
assembled elsewhere in the app — this class itself contains no hardcoded service list, only iteration/
logging code (`"Service %s included"`/`"...is not included"`). The multibinding's own assembly site
wasn't found (no Dagger-generated class survives with a findable name; a full search across every
R8-merged factory dispatcher for the specific map-construction site was judged out of proportion to
this item). **The bigger finding**: `ofd`'s single method (`oei a(int i)`) is **not** a service
dispatcher despite its shape — `i` is a **calling UID**, and the method is an **authorization-policy
check** (allow/reject with a reason string). Two concrete policies exist: `ofb` (allow only the app's
own UID) and `mie` (allow only an allowlisted, Google-signed caller). This substantively answers this
project's own "is anything (GMS included) actually gated to bind to this" question, without naming
GMS specifically: the exported-with-no-Android-permission shape is not the whole picture — the
service applies its own per-service, application-layer authorization. Whether GMS specifically is
ever allowlisted for *this* service's own methods remains open (the one `mie` construction site found
belongs to an unrelated outbound gRPC client connection this app makes to a different Pixel system
app, `com.google.android.apps.pixel.dcservice` — a genuinely new, incidental finding, flagged but not
pursued further, out of this project's Bluetooth scope).

**Files changed**: `REVERSE_ENGINEERING.md` (`MaestroEndpointService` entry rewritten with the full
trace, 1 new correlation-status row), `PROTOCOL.md` (§6 open item updated, proposed not committed),
`TODO.md` (item closed, with the two remaining open sub-questions noted).

### Item 3 — trace `gjv.p()`'s own caller — re-attempted, not resolved (same conclusion as `ai-sessions/0001`)

Confirmed `giz` is an abstract class with `gjv` as its sole subclass (`grep -rl "extends giz"` finds
only `gjv.java`), so any external `.p()` call on a `giz`-typed reference reaches `gjv.p()`. Found the
three classes holding a `giz`-typed field (`gpk`/`gpr`/`hke`, all reached via `ftj.i(String)`) and
confirmed none of them calls `.p()` anywhere in their own source. `giz`'s own `u` field (the
device-type/variant int `gjv.p()` gates on) is a bare `public int`, not a named setter — ruling out a
setter-name search; a generic `.u = `/`.p()` token search returned 20+ unrelated matches, not worth
pursuing further. Found useful adjacent context instead: `gaa.java` is the `GetSoftwareInfo`
**response**-side handler (unpacks `qjb`, the already-decoded response type) and calls a *different*
`giz` method (`n(gdm)`) once a response arrives — confirming the response-side counterpart to
`gjv.p()`'s request-side trigger, without resolving the trigger itself.

**Conclusion**: this static-analysis avenue is now exhausted twice (this session and `ai-sessions/0001`)
without success — recommending the byte-level capture-correlation path instead of a third
static-analysis attempt, unless a future session has a genuinely new search strategy. Formalized into
`TODO.md`'s "Targeted research follow-ups" per the prompt's own Phase 5 item 1 instruction (done now,
while the context was fresh, rather than deferred to Phase 5).

**Files changed**: `REVERSE_ENGINEERING.md` (`frb`/`fuh`/`glk`/`gjv` entry updated), `PROTOCOL.md`
(§6 open item updated), `TODO.md` (new item added to "Targeted research follow-ups").

### Item 4 — trace `fyd.d`/`fyd.e`'s call sites in the EQ UI fragment — done; found a genuine contradiction, not a confirmation

Traced `UserEqFragment.java` and `hom.java` (the EQ screen's ViewModel-like controller) in full.
**Field 16** (`fyd.e`) is confirmed fed reactively from the slider-drag/preset-selection path, as
already understood. **Field 18** (`fyd.d`) is reachable **only** through a dedicated RxJava "sample"
chain whose sole trigger is `hom.k()`, whose sole caller in the entire decompiled tree is `hju.java`
case 19 — a click handler wired to a real `key_eq_save_button`/`title_eq_save_button` button, logging
the self-describing `"On click save EQ button"` before calling it. **No code path from
slider-release (finger lift) to field 18 exists anywhere in this app version's source** — this
directly contradicts `CAP-015-FINDINGS.md` §6's own revised hypothesis ("field 18 fires on
slider-release, no video-visible Save tap") for all 15 of that capture's drag-cycles.

This closes the item's own static-analysis ask (`TODO.md`) but sharpens the open question into a
genuine, unreconciled tension rather than resolving it — recorded as such, with plausible-but-
unconfirmed explanations noted (a missed fast/off-screen Save tap in that capture's video; an
app-version difference) and none asserted, per the project's zero-creativity rule. Directly relevant
to `DECISIONS.md` ADR-020's own note that this should be resolved before EQ ships a "Save as preset"
UI affordance.

**Files changed**: `REVERSE_ENGINEERING.md` (`qjw` entry updated, 1 new correlation-status row),
`PROTOCOL.md` (§4.2 and §6 updated, proposed not committed), `TODO.md` (item closed).

### Items 5 and 6 — deferred to a future session (explicitly lower priority per the prompt)

- **Item 5** (apply the field-register method to `qhr`'s remaining untraced fields — 1, 6, 8, 14, 20,
  24–26, 30–38): the prompt itself marks this "lower priority... do this only if time/budget remains
  after 1–4 above." Given the depth items 1–4 above already reached (including two byproduct
  register corrections for fields 6 and 32 found along the way, and field-5/field-32 identities
  surfacing from item 1), and the remaining phases (4 cross-checks, 5 synthesis) still to do, this is
  deferred rather than attempted this session. Not closed in `TODO.md` — it was already listed there
  as "low priority — no v1-scope feature is known to depend on these" and remains accurate.
- **Item 6** (re-attempt decoding DLCI 0x02's opaque "Sent" blocks against the now-recovered `qhr`
  schema — the AES-128-encryption hypothesis): also deferred. This item needs an actual capture's raw
  "Sent" bytes re-examined byte-by-byte against the full `qhr` schema now that it substantially
  exists — a genuinely different kind of task from items 1–4's static-code tracing (it's a
  capture-re-analysis task, closer in kind to Phase 4's items), and time/budget in this session is
  better spent completing Phase 4's cross-checks (which several of Phase 3's own findings feed into)
  and Phase 5's synthesis than starting a new, open-ended byte-decoding attempt. Flagged for a future
  session; not closed in `TODO.md`/`PROTOCOL.md` §6 (both already track it accurately as open).

## Phase 4 — Cross-checks — done

### Item 1 — `CAP-041`'s connect-time-burst content diff — done, genuine content-level closure

Wrote a Python script (using `tshark`/`data.data` extraction) to pull every Sent-direction DLCI 0x02
payload in the connect-time burst window for `CAP-041`'s 3 windows (A/B/C, genuinely different
settings states each) plus `CAP-036`'s default-settings baseline, HDLC-unescaped and CRC-32-verified
all 184 resulting sub-frames (zero failures), then diffed content position-by-position and via set
comparison.

**Result**: the burst's dominant tail run (30 of ~46 sub-frames) is byte-for-byte **identical** across
all 4 sessions. The header portion contains the exact same *set* of sub-frame values in every
session — the only "difference" at the raw positional level is transmission **order**, not content —
with exactly one true exception per session: a single sub-frame matching the project's own documented
correlation-ID-prefix shape, whose trailing 6 bytes vary session-to-session in a way plausibly (not
confirmed) consistent with a per-session timestamp or nonce (`fux.java`'s already-documented
`SetWallclock` RPC is a candidate). **`CAP-041`'s own `OBS-007` clean-negative result is now closed at
the content level**, not merely the length level its own initial pass reached — no evidence found
that this burst carries any settings-state read-back.

**Files changed**: `CAP-041`'s own findings file (new §8 with full command+hex evidence,
§6/§7 updated in place per that document's own rewrite-in-place convention), `PROTOCOL.md` (§6 item
closed `[x]`).

### Item 2 — byte-level correlation of an existing burst against `qjb`'s decoded shape — a plausible lead found, not a confirmation

Per the prompt's own scoping (a fresh capture is out of scope), checked existing capture data instead.
Within `CAP-036`'s own connect-time burst (frame 1423, Rcvd, DLCI 0x02), found a sub-message decoding
to three length-14 strings (`"57071WRBEC0251"`, `"57081WRBDR2309"`, `"57071WRBDL3147"`) — structurally
matching `qie`'s documented 3-optional-sub-field shape (one of `qjb`'s two oneof alternatives) closely
enough to be a plausible, not coincidental, match. **What this does not establish**: whether this
specific frame is `qjb` itself vs. a sibling response type (e.g. `GetHardwareInfo`) sharing the same
nested shape — the outer tags weren't matched field-by-field against `qjb`'s own 4-field schema. A
separate frame (1421) in the same burst contains a long run of repeated `(index, float32)` pairs,
structurally telemetry-shaped and plausibly a different RPC entirely. Recorded as a genuine advance
(a concrete structural lead exists now, where none did before) but not a closure — full confirmation
still needs a fresh, purpose-built capture per the prompt's own framing.

**Files changed**: `PROTOCOL.md` (§6 item extended with this finding, explicitly not marked closed).

### Item 3 — `TrueWirelessHeadset.modelId` cross-reference — confirmed as needing maintainer device access

Searched `captures/`, `DESKRESEARCH_FINDINGS.md`, and `ai-sessions/` for any existing value dump of
this GMS-internal field — none exists (only this task's own prompt/result files mention the open
question itself, not a value). Confirmed this cannot be resolved via static analysis or existing
capture re-analysis — it requires the maintainer's own live-device access (an `adb` dump or debug log
of the GMS-internal object), since the field is never carried on the wire in any capture on disk.

**Files changed**: `REVERSE_ENGINEERING.md` (open-questions note updated to record the check and its
negative result).

## Decision points — resolved with the maintainer mid-session

Per the prompt's own instruction, two candidate FACT-promotion-adjacent findings from Phases 3
were put to the maintainer directly (not just written up as "awaiting sign-off"):

1. **`qhr` field 2 ("On-Head/In-ear Detection")** — approved for promotion to category-level
   identity (the same evidence bar already used for fields 22/27: a self-describing internal
   name, `"CATEGORY_OHD"`, not a verbatim UI-label match). Applied: `PROTOCOL.md` §4.5.5,
   `DECISIONS.md` ADR-019 (new dated Update), `REVERSE_ENGINEERING.md`'s `qhr` entry and
   `MaestroDeviceSettingsProviderService` entry, correlation-status table, and a new `PROTOCOL.md`
   §8 changelog row.
2. **EQ `qhr` field 18's Save-button-vs-slider-release tension** — maintainer chose "record as an
   open tension, no capture yet." No further edit needed beyond what Phase 3 item 4 already wrote;
   confirmed this is the maintainer's own explicit call, not left ambiguous.

**Files changed for this decision**: `PROTOCOL.md` (§4.5.5, §6, new §8 changelog row), `DECISIONS.md`
(ADR-019 new dated Update), `REVERSE_ENGINEERING.md` (`qhr` entry, `MaestroDeviceSettingsProviderService`
entry, correlation-status table).

## Phase 5 — Anything else

1. **Formalize the `gjv.p()`-caller-trace item into `TODO.md`** — done during Phase 3 item 3 (see
   above), not deferred to this phase.
2. **`CAP-041`/`CAP-037` re-analysis-only categorization** — confirmed still accurate. This session's
   own Phase 4 item 1 (`CAP-041`) is itself a further instance of this same category (existing-capture
   re-analysis, no new capture, no APK work) — consistent with `ai-sessions/0002`'s original framing.
3. **Final synthesis** — per this task's own instruction to "pull together every proposal from
   Phases 2–4 into the specific edits they would produce": this session applied that synthesis
   *inline*, as each phase completed, rather than holding every finding for a single end-of-session
   write-up — matching the pattern already established by `ai-sessions/0001`/`0002` (propose directly
   in the target document, clearly labeled, rather than only in the `RESULT` file). Every
   `PROTOCOL.md`/`REVERSE_ENGINEERING.md`/`TODO.md` edit this session made is quoted or closely
   paraphrased above, phase by phase, with the file changed named at the end of each phase's write-up.

**Deferred, explicitly, not forgotten:**
- Phase 3 items 5 (remaining untraced `qhr` fields 1/6/8/14/20/24-26/30-38) and 6 (re-attempt decoding
  DLCI 0x02's opaque "Sent" blocks against the recovered `qhr` schema) — both explicitly lower-priority
  per the prompt's own framing; not attempted this session given the depth items 1–4 and Phase 4
  already reached. `TODO.md`/`PROTOCOL.md` §6 already track both accurately as open; no edit needed to
  reflect the deferral itself.
- Phase 1 item 4 (the `scripts/lint_docs.py` dead-filename findings triage and the `PROJECT_RULES.md`
  rule 9a compliance sweep) — explicitly marked "spare time only, do not let it block Phase 2 onward"
  in the prompt; Phase 2 onward took priority for the reasons already given in Phase 1's own write-up.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08
