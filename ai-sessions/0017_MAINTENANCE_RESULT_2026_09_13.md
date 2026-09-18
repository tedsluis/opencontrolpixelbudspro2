# 0017_MAINTENANCE_RESULT_2026_09_13.md — Close out Phase 1–3's remaining open items, inventory pending maintainer decisions, and assess Phase 3/4 readiness

**Number:** 0017
**Category:** MAINTENANCE
**Date:** 2026-09-13
**Title:** Close out Phase 1–3's remaining open items, inventory pending maintainer decisions, and assess Phase 3/4 readiness
**Status:** complete — reconciled 2026-09-18 (`ai-sessions/0031`, per `AI_SESSION_LOG_PROCEDURE.md` §4a):
all 8 sign-off items resolved (5 Tier-1 items already closed by the 2026-09-16 sign-off or requiring
no sign-off; Tier-2 items 6/7 approved 2026-09-18); Tier-2 item 8 and Tier-3's 5 designed captures
(`CAP-053`–`057`, plus `CAP-058`) remain open as ordinary `TODO.md` capture-queue items, not a
sign-off blocker. This field was previously left stale at `awaiting maintainer sign-off` after
`ai-sessions/INDEX.md`'s own row was updated 2026-09-18 — fixed to match, per §4a's requirement that
an earlier file's `Status` field be updated in place, not just its `INDEX.md` summary row.

## Phase status

| Phase | Status | Summary |
|---|---|---|
| 0 — Setup | done | Mandatory reading order completed in full: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md` (§0.1, §2.2a, §2.3, §4.1–§4.5, §5.1/§5.2, §6, §8), `DECISIONS.md` (all 29 ADR titles/topics scanned, ADR-006/ADR-016/ADR-017/ADR-020 read in full), `TODO.md` (full), `AI_SESSION_LOG_PROCEDURE.md` (full), `ai-sessions/INDEX.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Groups AJ–AN + §9 tail), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (full), `REVERSE_ENGINEERING.md` (`qjw`, `qjb`/`qie`/`qjj`/`qjm`/`qjr`, `MaestroDeviceSettingsProviderService`, `HeadGesture`/`qin` entries), `id_registry.csv`. `CAP-028-FINDINGS.md`, `CAP-045-FINDINGS.md`, `CAP-043-FINDINGS.md`, `CAP-036-FINDINGS.md` §4/§12 read in full. This is a first run — no prior `0017_MAINTENANCE_RESULT_*.md` existed. Next-free numbers confirmed: `CAP-053`, Group letter `AO`, and the next ADR slot after `ADR-029` (the latter two not used this session — no ADR committed, no new Group beyond what phases 1–5 register below). |
| 1 — EQ field 16 vs. 18 | done | Bounded APK re-check confirmed the 2026-09-08 trace (`fyp.f()`=field16, `fyp.d()`=field18, `hju` case 19=Save button) still holds byte-for-byte against the current decompiled source — **and found a genuine gap in that trace**: a second, previously-undocumented call site to the same save-write (`hod.java:36`, a "navigate away with unsaved changes" trigger, self-describing log `"Navigate away, save EQ"`) exists alongside the Save-button path. `REVERSE_ENGINEERING.md`'s `qjw` entry and `PROTOCOL.md` §4.2 updated with this correction. New capture skeleton designed: Group AO / `CAP-053`, isolating all three candidate triggers (Save tap / navigate-away / genuine slider-release) from each other. **PROPOSAL — new capture**, registered `planned`. |
| 2 — Battery Option A | done | `CAP-043-FINDINGS.md` §3's byte-level non-match re-run directly against the raw log (frame 133, same 19-byte payload, first byte `0x10` not `0x00`, no `0x33`/`0x34` marker anywhere) — reproduces exactly. New capture skeleton designed: Group AP / `CAP-054`, bracketing a single-bud insertion/removal event (the Fast Pair spec's own "optional" trigger condition), not yet tested by any capture to date. **PROPOSAL — new capture**, registered `planned`. **PROPOSAL — awaiting maintainer decision** on the conditional reframing question (§4.3 Option A's status wording) written up below. |
| 3 — Head gestures | done | Full-log DLCI 0x02/0x04/0x08 re-scan across `CAP-028`'s entire 227.71s span (not just the originally-checked ~50s window), plus AVRCP/AVCTP, genuine SCO/eSCO, and `AT+`/HFP checks across the whole log — reproduces the exact same clean negative, no new signal. APK cross-reference: no `"Nod"`/`"Shake"`-named constant exists anywhere in the decompiled tree, but `HeadGesture.SubscribeToResults`'s response enum (`qin`) is confirmed, via its shared validity-checker infrastructure, to be genuinely 3-valued (raw `{0,1,2}`) — structurally consistent with one unset/unknown sentinel plus exactly two real gesture types, though which raw value is Nod vs. Shake is not recoverable statically. `REVERSE_ENGINEERING.md` updated. New capture skeleton designed: Group AQ / `CAP-055`, gesture performed while an actual call/notification is active, camera also framing the gesture itself. **PROPOSAL — new capture**, registered `planned`. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `HEAD-002`/`HEAD-003` rows updated. |
| 4 — ANC-rotation checklist (`HOLD-005`) | done | Directly re-confirmed against `CAP-045-FINDINGS.md` §2/§4: the planned Group AJ procedure was never run (physical press-and-hold ANC cycling was captured instead; zero rotation-checklist writes anywhere in the log). New capture skeleton designed: Group AR / `CAP-056`, a distinct Group letter (not a `CAP-045` v2 reusing AJ), with a mandatory on-camera anti-repeat safeguard named after `CAP-045`'s own gap. **PROPOSAL — new capture**, registered `planned`. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `HOLD-005` row updated. |
| 5 — Serial numbers | done | Traced `qjb`'s own wire tags around `CAP-036` frame 1423 field-by-field using `scripts/decode_rawmessageinfo.py` plus direct byte inspection — found the existing "structurally matches `qie`" reading is a **non-match**: `qie`'s 3 fields are typed `MESSAGE` (requiring an extra nested tag+length per field), while frame 1423's actual 48-byte sub-message has 3 direct `STRING` fields, exactly matching `qjm`/`qjr` (`GetHardwareInfo`'s own two oneof alternatives) instead. This reverses which RPC is the better structural candidate — `GetSoftwareInfo`/`qjb` no longer the better fit, `GetHardwareInfo`/`qiv` now is, though RPC identity itself (service/method) is still not decoded. `REVERSE_ENGINEERING.md`/`PROTOCOL.md` §6 updated with this correction. New capture skeleton designed: Group AS / `CAP-057`, a live correlation between video-transcribed serial numbers and the wire burst. **PROPOSAL — new capture**, registered `planned`. |
| 6 — Decision inventory | done | Full prioritized inventory compiled below, re-derived fresh (not from memory) against current file state — see "Phase 6" section. |
| 7 — Documentation cross-check | done | `scripts/lint_docs.py` and `scripts/ensure_footers.py` run; findings and fixes below. `TODO.md`, `PROTOCOL.md` (§4.2, §6 ×2, §8 changelog), `REVERSE_ENGINEERING.md` (`qjw`, `qjb`, `qin`/HeadGesture entries), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (5 new Group skeletons + 5 new Capture Index rows), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (`HOLD-005`, `HEAD-002`/`HEAD-003`, `BATT-002`/`BATT-003`, `EQP-008` rows), `id_registry.csv` (`CAP-053`–`CAP-057`), `ai-sessions/INDEX.md` all updated/checked. `DECISIONS.md` — checked, no change (no ADR committed this session). |
| Final — Readiness assessment | done | Three-question assessment below. |

## Phase 1 — EQ persistence: field 16 vs. 18

### Re-verification method

Per the prompt's instruction to re-derive independently rather than trust the prior summary, the
`fyd.d`/`fyd.e` call-graph trace from `ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md` was
re-run against the current decompiled source
(`reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/`):

```
$ grep -n "implements fyd" defpackage/fyp.java
7:public final class fyp implements fyd {
```
`fyp.f()` (field 16, "user eq") and `fyp.d(gdy)` (field 18, "last saved user eq") both confirmed at
their documented line numbers, with `qhrVar.b = 16` / `qhrVar.b = 18` respectively — unchanged from
the 2026-09-08 trace.

```
$ grep -n "\.k()" defpackage/hlc.java defpackage/hod.java \
    com/google/android/apps/wearables/maestro/companion/ui/settings/sound/usereq/UserEqFragment.java \
    | grep -i "hom\|homVar"
defpackage/hod.java:36:  if (crmVar.d() != null && ((hpp) crmVar.d()).b && homVar.k()) {
```

**This is the new finding**: `hom.k()` (the method that ultimately fires `fyd.d`/field 18) has a
**second** caller, not found by the 2026-09-08 pass, whose own "sole caller in the entire decompiled
tree" claim is therefore corrected (not silently left standing — see `REVERSE_ENGINEERING.md`'s
`qjw` entry, 2026-09-13 Update, for the full trace including `hod`'s registration site
(`UserEqFragment.java:82`/`:556`) and the self-describing `"Navigate away, save EQ"` log message).

### What this does and does not establish

- **Does establish** (🟢 FACT, code existence + self-describing log): a second, code-real trigger
  for the field-18 write exists, gated on an unsaved-changes-shaped condition, distinct from the
  Save button.
- **Does not establish**: which of the now-three candidate triggers (Save button / navigate-away /
  the still-unconfirmed slider-release reading) actually fired in `CAP-015`'s 15 drag-cycles. No
  call site connects the navigate-away trigger to a literal slider-release gesture either — it
  requires leaving the EQ screen, which `CAP-015`'s continuous within-screen drag sequence doesn't
  obviously match, but this project's own zero-creativity rule means that isn't asserted as a
  disproof either, only left open.

### PROPOSAL — new capture (Group AO / `CAP-053`)

Registered `planned` in `id_registry.csv` and `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's Capture Index.
Full skeleton added at `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1 (between Group AN and the Pixel 9a
section) — isolates release-only, release+Save, and release+navigate-away as three separated
sub-sessions within one capture, so a single session gives a clean within-session contrast between
all three candidate triggers, closing the tension `PROTOCOL.md` §4.2 has carried since 2026-08-18.

**No `PROTOCOL.md` §4.2 status changed** — it remains 🟡 HYPOTHESIS, exactly as before this session;
only the supporting trace and the pointer to a resolving capture were added.

## Phase 2 — Battery Option A (Fast Pair BLE Battery Notification)

### Re-verification

```
$ tshark -r captures/CAP-043-.../CAP-043-btsnoop_hci.log -Y "frame.number==133" -V
    Service Data - 16 bit UUID
        Length: 22
        UUID 16: Google LLC (0xfe2c)
        Service Data: 105213d38028442154a62ab029be65f6a05016
```
Raw bytes: `10 52 13 d3 80 28 44 21 54 a6 2a b0 29 be 65 f6 a0 50 16`. First byte `0x10`, not the
required `0x00` Flags value; no byte at any offset equals `0x33`/`0x34`. Reproduces
`CAP-043-FINDINGS.md` §3 exactly — the non-match is confirmed, not force-fit.

### What `CAP-043` established and what's next

`CAP-043` closed one confound cleanly (a genuinely connection-free capture still shows the
non-matching payload, ruling out "an active RFCOMM connection suppresses the advertisement"). Two
things remain open, per `CAP-043-FINDINGS.md` §7's own recommended next steps:

1. **Whether the extension ever fires under *any* condition** — only idle/case-closed has been
   tested (`CAP-011`, `CAP-036`, `CAP-043`, three sessions, three non-matches, all under
   idle/case-closed or an unaccounted-for confound). The Fast Pair spec's own text calls the
   extension "**optional** when a single bud is inserted/removed" — a materially different, untested
   trigger condition.
2. **What the actually-observed stable `0xFE2C` payload's real sub-type is** — plausibly an Account
   Key Filter frame per its shape, not decoded further (out of scope for a "don't guess" pass).

### PROPOSAL — new capture (Group AP / `CAP-054`)

Registered `planned`. Brackets a single-bud insertion/removal event, still connection-free
(app force-stopped, per `AGENTS.md` §7's bounded exception), two independent samples (one per
earbud).

### PROPOSAL — awaiting maintainer decision (not a FACT/ADR — a scoping question)

If Group AP / `CAP-054` also comes back negative (no Battery-Notification-shaped frame under the
bud-insertion/removal trigger either), is it time to recommend `PROTOCOL.md` §4.3 Option A be
reframed from its current wording — "🟡 HYPOTHESIS (confirmed as used by the Buds Pro 2
specifically)" — toward a more skeptical framing (e.g. "🟡 HYPOTHESIS, three independent non-matches
across the two documented trigger conditions, increasingly doubtful this device uses this extension
at all")? This is exactly what `CAP-043-FINDINGS.md` §7 itself already proposed. **Not decided
here** — stated as a recommendation for the maintainer's review once `CAP-054`'s own result is in,
per `AGENTS.md` §6. `PROTOCOL.md` §4.3 Option A's own framing is unchanged this session.

## Phase 3 — Head gestures (Nod/Shake, `HEAD-002`/`HEAD-003`)

### Exhaustive re-check

```
$ tshark -r captures/CAP-028-.../CAP-028-btsnoop_hci.log \
    -Y '(btrfcomm.dlci==2 or btrfcomm.dlci==4 or btrfcomm.dlci==8) and btrfcomm.len>0' \
    -T fields -e frame.number -e frame.time -e btrfcomm.dlci -e frame.p2p_dir
(161 total frames across the full 227.71s log)

$ tshark -r captures/CAP-028-.../CAP-028-btsnoop_hci.log \
    -Y '(btrfcomm.dlci==2 or btrfcomm.dlci==4 or btrfcomm.dlci==8) and btrfcomm.len>0 \
        and frame.time>="2026-09-12 07:16:25" and frame.time<="2026-09-12 07:17:15"'
(0 rows — reproduces CAP-028-FINDINGS.md §4's original window finding exactly)

$ tshark -r ... -Y '... and frame.time>"2026-09-12 07:17:15"'
(19 frames, all falling into 3 clusters at 07:17:15.55/07:18:02.17/07:19:35.78 — byte-identical in
shape to the already-documented routine periodic battery/keepalive push, e.g. `030300036464ff` =
DLCI 0x04 Group 0x03 Code 0x03, L=100%/R=100%; `0403000410051864` = DLCI 0x08 Option E push)

$ tshark -r ... -Y "avctp or avrcp"          → 0 rows (whole log)
$ tshark -r ... -Y "bthci_evt.code==0x2c"    → 0 rows (whole log — no genuine SCO/eSCO)
$ tshark -r ... -Y "btrfcomm.data contains \"AT+\""  → 0 rows (whole log)
$ tshark -r ... -Y "btl2cap.psm==0x0011 or btl2cap.psm==0x0013"
(12 frames, all HID-Control/HID-Interrupt connect-then-immediate-disconnect, at 07:16:21 and
07:16:27 — both before the 07:16:29 claimed gesture-window start, matching CAP-028-FINDINGS.md §5)
```

**Conclusion: the full-span re-check reproduces the exact same clean negative `CAP-028-FINDINGS.md`
§4 already found**, extended from the originally-checked ~50s window to the entire 227.71s log, with
zero new signal on any channel checked (DLCI 0x02/0x04/0x08, AVRCP, SCO/eSCO, HFP `AT+`, HID). This
is recorded as a completed, deliberate re-verification, not silently skipped, per the prompt's own
instruction.

### APK cross-reference

`fux.java`'s service catalog already names `HeadGesture.StartDetection`/`SubscribeToResults`/
`EndDetection`. A fresh, independent search:

```
$ grep -rniE '"nod"|"shake"|NOD_GESTURE|SHAKE_GESTURE|HeadGestureType' \
    reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/ --include=*.java
(0 matches — clean negative, no gesture-name string survives anywhere in the decompiled tree)
```

`qin` (`HeadGesture.SubscribeToResults`'s response type) decodes to exactly 1 field, a plain `ENUM`:
```
$ python3 scripts/decode_rawmessageinfo.py .../defpackage/qin.java
qin: 1 field(s), 0 oneof(s), 0 map field(s), fieldNum range [1-1]
  field 1: ENUM java_field='c' hasbit=0 has_presence
```
Tracing the enum's own validity-checker reference (`qgx.q`, instance index 16) to `a.aI(int)`:
returns non-zero **only for raw input `{0, 1, 2}`**, i.e. the wire enum is genuinely **3-valued** —
structurally consistent with one unset/unknown sentinel plus exactly the two real gesture types
(Nod, Shake) this project already expects. No name for any of the 3 raw values survives — which
value is Nod and which is Shake is not recoverable from static analysis. Recorded in
`REVERSE_ENGINEERING.md`'s decoded-shapes register as a 2026-09-13 Update.

### PROPOSAL — new capture (Group AQ / `CAP-055`)

Registered `planned`. Fixes `CAP-028`'s actual gap: Nod/Shake performed while an actual incoming
call or notification is active (per `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s own description of what these
gestures do), camera angled to capture the gesture itself as well as the phone screen.
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `HEAD-002`/`HEAD-003` rows updated to point at it.

## Phase 4 — ANC-mode rotation checklist Left/Right split (`HOLD-005`)

### Re-confirmation

Directly re-read `CAP-045-FINDINGS.md` §2/§4: unambiguous. The session's own video review shows the
top-level ANC toggle row on the main Device details screen at every checked frame — "Controls and
gestures" → the rotation-checklist sub-screen was never opened. Confirmed independently via the
wire: all 38 DLCI 0x02 `Sent` frames in the session fall inside the ordinary connect-time/reopen
settling-burst shape, none carrying the `field5(len12){field4(len10){field12(len8){...}}}`
rotation-checklist envelope. **Zero rotation-checklist writes occur anywhere in this session** —
`HOLD-005`'s question is exactly as open as before `CAP-045`.

### PROPOSAL — new capture (Group AR / `CAP-056`)

Registered `planned`, a **distinct Group letter from AJ** (Group AJ's own procedure is unchanged and
still valid — this is not a "`CAP-045` v2" reusing it). Carries a mandatory, named anti-repeat
safeguard: the checklist screen (a checkbox list titled with the four ANC modes) must be
video-confirmed on-camera for at least one full toggle before the session counts as having run the
procedure at all — explicitly citing `CAP-045` as the reason for the extra emphasis, per the
prompt's own instruction. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `HOLD-005` row updated.

## Phase 5 — Per-component serial numbers (Left/Right/Case)

### Field-by-field trace

```
$ python3 scripts/decode_rawmessageinfo.py .../defpackage/qjb.java
qjb: 4 field(s), 1 oneof(s), 0 map field(s), fieldNum range [3-6]
  field 3: ONEOF(MESSAGE) ref=qjj.class      field 4: ONEOF(MESSAGE) ref=qie.class
  field 5: FIXED64   field 6: BOOL

$ python3 scripts/decode_rawmessageinfo.py .../defpackage/qie.java
qie: 3 field(s), 0 oneof(s), 0 map field(s), fieldNum range [1-3]
  field 1: MESSAGE java_field='c'   field 2: MESSAGE java_field='d'   field 3: MESSAGE java_field='e'

$ python3 scripts/decode_rawmessageinfo.py .../defpackage/qjm.java   (qiv's field-4 oneof alt)
qjm: 3 field(s) — field 1/2/3 all STRING

$ python3 scripts/decode_rawmessageinfo.py .../defpackage/qjr.java   (qiv's field-7 oneof alt)
qjr: 3 field(s) — field 1/2/3 all STRING
```

`CAP-036` frame 1423 (`bthci_acl.chandle==0x0005`, DLCI 0x02, Rcvd), raw bytes for its pw_rpc
payload field (`3a 30`, length 0x30=48):

```
0a 0e 35 37 30 37 31 57 52 42 45 43 30 32 35 31   → field1, len14, "57071WRBEC0251"
12 0e 35 37 30 38 31 57 52 42 44 52 33 32 30 39   → field2, len14, "57081WRBDR2309"
1a 0e 35 37 30 37 31 57 52 42 44 4c 33 31 34 37   → field3, len14, "57071WRBDL3147"
```
All 48 bytes accounted for exactly (2+14 × 3 = 48, matching the declared length). Each field's
string bytes follow its own tag+length **directly** — no intervening nested tag+length, which a
`MESSAGE`-typed field (as `qie`'s 3 fields all are) would require.

**Conclusion: this does not match `qie`'s shape.** It matches `qjm`/`qjr`'s shape exactly — both in
field numbering (1/2/3) and field typing (`STRING`, not `MESSAGE`). The existing `PROTOCOL.md` §6
entry's "structurally matching `qie`'s documented shape... far more closely than a coincidence would
predict" reading is corrected, not confirmed, by this pass. This reverses which RPC is the better
structural candidate: `GetHardwareInfo` (`qiv`, whose two oneof alternatives are `qjm`/`qjr`) is now
the better fit than `GetSoftwareInfo` (`qjb`, whose oneof alternative is `qie`) — though **RPC
identity itself (which service/method call this actually is) was not decoded this pass**, only the
payload's field shape; the outer pw_rpc/`pw_hdlc` envelope bytes were not fully parsed.

### Cross-reference

`REVERSE_ENGINEERING.md`'s existing `qjb` entry and `fux.java`'s service catalog were re-checked; no
additional caller/response type beyond `qjm`/`qjr`/`qie`/`qjj` was found this pass that would further
disambiguate. `gjv.p()`'s own trigger (a candidate for what schedules `GetSoftwareInfo`'s own fetch)
was separately found by `ai-sessions/0013` to plausibly be a ~weekly staleness check, not a
connect-time-adjacent one — this doesn't change today's field-shape finding, but is worth noting as
additional context for why frame 1423 (captured inside a *connect-time* burst) may be less likely
to be `GetSoftwareInfo` than previously assumed.

### PROPOSAL — new capture (Group AS / `CAP-057`)

Registered `planned`. Since static analysis cannot go further (no service/method identifier
decoded), a live correlation is the only remaining path: video-transcribe the app's own displayed
serial numbers per component, then capture a fresh connect-time burst and check both (a) whether the
wire strings match the transcribed serials, confirming the Case/Right/Left semantic reading (which
stays 🟡 HYPOTHESIS per `AGENTS.md` §13.6 even if RPC identity resolves), and (b) whether the
surrounding RPC envelope decodes as `GetHardwareInfo` rather than `GetSoftwareInfo`.

## Phase 6 — Full inventory of items awaiting a maintainer decision

Ordered by how cheaply each can be resolved — a sign-off with no new data needed first, a new
capture last. Re-derived fresh against current file state, not from memory of past sessions.

### Tier 1 — sign-off only, no new data needed

1. **`PROTOCOL.md` §5.1 Cross-Transport Key Derivation (CTKD) pairing path** — a clean 3-vs-3 split
   (classic SSP whenever no pre-existing LE link; CTKD whenever one exists), strengthened by
   `CAP-012`'s controlled isolation test. **Recommendation: approve** — the causal isolation looks
   solid across `CAP-002`/`CAP-003`/`CAP-012` (no LE link → SSP) vs. `CAP-004`/`CAP-014` (LE link →
   CTKD), with `CAP-012` specifically designed to rule out the one live confound.
2. **`PROTOCOL.md` §6's "Forget clears prior BLE association" item (`PAIR-004`)** — `CAP-032`'s clean
   counter-example (no BLE link/valid key existed pre-Forget, under a rigorously verified
   pre-clearing-action log window) vs. `CAP-001`'s original session showing the opposite. **Decision
   needed**: does `CAP-032` settle the general claim ("Forget does clear the association, `CAP-001`'s
   session was anomalous") or only demonstrate non-universality ("both behaviors are possible,
   depending on some still-unidentified factor")? **Recommendation**: the latter — one clean session
   against one dirty session is not enough to call the general claim settled either way; keep as
   OPEN QUESTION but record both results plainly (already done).
3. **`REVERSE_ENGINEERING.md`'s `MaestroDeviceSettingsProviderService` 6-case-ID→`qhr`-field trace**
   — proposed for `PROTOCOL.md` §6/§4.5.5 promotion (case 2102→field 2, 2103→field 27, 2104→field 11,
   2113→field 5, 2116→field 32; case 2115 has no `qhr` field). **Recommendation: approve** — this is
   static call-graph tracing with no ambiguous branch, the same evidentiary tier already used for
   fields 17/19/22/27/28's existing promotions.
4. **`REVERSE_ENGINEERING.md`'s "GSND" naming lead for DLCI 0x08/0x0a** — a low-priority, purely
   informational addendum (SDP browse found the names "GSND CONTROL"/"GSND AUDIO"). **Recommendation:
   approve** as informational context; does not resolve DLCI 0x08's identity.
5. **This session's `qjw`/`hod.java` finding (Phase 1)** and **`qjb`/`qie`/`qjm`/`qjr` finding (Phase
   5)** — both already added to `REVERSE_ENGINEERING.md`/`PROTOCOL.md` at HYPOTHESIS level (no
   sign-off required to add HYPOTHESIS-level findings, per established project practice — only 🟢
   FACT promotions and new ADRs are gated). Flagged here only so the maintainer is aware both exist
   and reverse two previous readings.

### Tier 2 — a decision needed, but not itself resolvable without new data

6. **`PROTOCOL.md` §4.3 Option B's `Group 0x03 Code 0x03` "Battery updated" candidate** —
   strengthened by the official Fast Pair Device Information extension spec's exact code match, but
   still only single-session (`CAP-009`) evidence. **Recommendation**: either (a) accept the existing
   208-occurrence, 2-earbud-transition evidence as sufficient for promotion now the spec citation
   exists, or (b) hold for an independent-session reproduction as originally scoped — genuinely the
   maintainer's call on evidentiary bar, not something this session can resolve unilaterally.
7. **`CAP-044-FINDINGS.md` §5's on-device process-liveness-check proposal** for a possible 3rd
   `SDP-001` attempt (or reframing `SDP-001`'s own question) — a go/no-go on whether a 3rd attempt is
   worth the effort given two prior attempts (`CAP-033`, `CAP-044`) each capped at 🟡 HYPOTHESIS by a
   different isolation gap. **Recommendation**: worth one more attempt given the proposed refinement
   (an explicit on-device process-liveness check) directly targets the specific gap `CAP-044` found,
   but this is a product-priority call, not a research one.
8. **Phase 2's Battery Option A reframing question** (above) — conditional on `CAP-054`'s own result.

### Tier 3 — capture-blocked (a maintainer-run physical session is the only path)

9. Group AO / `CAP-053` — EQ field 16-vs-18 isolation (Phase 1).
10. Group AP / `CAP-054` — Battery Notification bud-insertion/removal bracket (Phase 2).
11. Group AQ / `CAP-055` — Head gestures with an active call/notification (Phase 3).
12. Group AR / `CAP-056` — ANC-rotation-checklist Left/Right split, genuine re-run (Phase 4).
13. Group AS / `CAP-057` — Live `GetSoftwareInfo`/`GetHardwareInfo` correlation (Phase 5).
14. (Conditional on item 7's go-ahead) A 3rd `SDP-001` attempt.

## Phase 7 — Documentation cross-check and consistency pass

```
$ python3 scripts/lint_docs.py
$ python3 scripts/ensure_footers.py
```
Both run against the full repository after this session's edits. No new issues introduced by this
session's own changes were flagged (all new/edited files carry the standard footer and use only
`CAP-NNN`/`ADR-NNN`/Test-ID references now present in `id_registry.csv`/the catalogs). Pre-existing,
unrelated noise from earlier sessions was left untouched, per the prompt's own instruction not to fix
long-standing noise outside this session's scope.

Document-by-document:

- **`TODO.md`** — updated: Phase 1's `CAP-028`/`CAP-045` status (both now accurately reflect they
  were run and what happened, with pointers to the new follow-up captures), the field-16/18 tension
  bullet (Phase 1's `hod.java` finding + Group AO pointer), the Battery Option A `CAP-043`-done
  status + Group AP pointer.
- **`PROTOCOL.md`** — §4.2 (field 16/18 correction + Group AO pointer), §6 (serial-number correction
  + Group AS pointer; head-gesture full-log re-check note + Group AQ pointer), §8 changelog entry
  added for this session. No HYPOTHESIS/FACT status changed (no maintainer answer was given inline
  this session — this is a first run with no prior maintainer response to apply).
- **`DECISIONS.md`** — checked, no change needed. No new ADR committed this session (per the prompt's
  hard rule); Tier 1's inventory items above are proposals for a future maintainer-approved ADR, not
  drafted as one here.
- **`REVERSE_ENGINEERING.md`** — Phase 3's `HeadGesture`/`qin` search and Phase 5's `qjb`/`qie`/
  `qjm`/`qjr` field-match search results recorded, including the clean negative on gesture-name
  strings. Phase 1's `qjw`/`hod.java` correction recorded.
- **`CAPTURE_BLUETOOTH_HCI_SNOOP.md`** — 5 new Group skeletons (AO–AS) added at §4.1, correctly
  formatted per the Group AJ–AN template; §9 Capture Index updated with 5 new `planned` rows
  (`CAP-053`–`CAP-057`).
- **`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`** — `HOLD-005`, `HEAD-002`/`HEAD-003`, `BATT-002`/`BATT-003`,
  `EQP-008` rows all updated to point at the new planned captures designed this session.
- **`id_registry.csv`** — `CAP-053`–`CAP-057` registered as `planned`, no number reused or skipped
  (checked against the existing highest entry, `CAP-052`, before assigning).
- **`ai-sessions/INDEX.md`** — `0017`'s row present (added when the prompt file was created);
  updated below from `not started` to `awaiting maintainer sign-off`.

## Final phase — Phase 3/4 readiness assessment

### 1. Can `TODO.md`'s Phase 3 (Protocol reconstruction) be closed further right now?

**Capture-blocked** (need a physical session the maintainer must run — all designed this session):
EQ field 16/18 (Group AO), Battery Option A's insertion/removal trigger (Group AP), head gestures
with an active call (Group AQ), the ANC-rotation-checklist Left/Right split (Group AR), and the
serial-number RPC-identity confirmation (Group AS). Also capture-blocked, from before this session:
the general Battery Option A question if `CAP-054` also comes back negative, and (conditionally)
a 3rd `SDP-001` attempt.

**Decision-blocked** (need only a maintainer sign-off, no new data): Tier 1's 5 items above (CTKD
promotion, the `PAIR-004`/`CAP-032` judgment call, the `MaestroDeviceSettingsProviderService` case-ID
trace promotion, the GSND naming-lead addendum, and this session's own two new HYPOTHESIS-level
corrections which need no sign-off to stand but whose downstream implications — e.g. whether to
revise the `GetHardwareInfo`-vs-`GetSoftwareInfo` framing further — are worth the maintainer's
awareness). Tier 2's Battery Option B code promotion and the `SDP-001` 3rd-attempt go/no-go are
partially decision-blocked (a bar-setting judgment call) and partially still capture-contingent.

**Advanceable by pure static analysis/documentation work in a future session**: genuinely limited.
The most promising remaining thread is tracing `gag`'s own 16 construction sites
(`REVERSE_ENGINEERING.md`'s `frb`/`fuh`/`glk`/`gjv` entry, 2026-09-13 Update) to pin down what
schedules the "~weekly staleness check" candidate trigger for `GetSoftwareInfo`'s own fetch — flagged
by that entry itself as a reasonable next step, not attempted this session (out of this prompt's own
bounded scope). Field 5/32/6's semantic identity and "Feature A"/`kjj` remain open but are
diagnostic-toggle-shaped leads, not high-value.

### 2. What should happen before more Phase 4 (app development) work continues?

**Nothing in phases 1–6's open items blocks the already-unblocked features.** Cross-referencing
`ARCHITECTURE.md` §5's per-channel/per-feature implementation gate and `DECISIONS.md`'s existing
unblocks: ANC (`ADR-009`), Battery Option C/HFP (`ADR-015`/`ADR-023`), Find My Buds Left/Right
(`ADR-011`), and the EQ envelope/field-mapping/clamp/presets (`ADR-020`) are all still fully 🟢 FACT
and implementation-unblocked — none of this session's findings touch any of their evidentiary basis.

**The EQ field-16/18 tension specifically blocks only the "Save as preset" UI affordance**, per
`DECISIONS.md` ADR-020's own scope note — this session's new `hod.java` finding sharpens, but does
not resolve, that tension, so the same narrow scope limit still applies: EQ generally (bands, clamp,
presets, live-value write) remains implementable now; only the specific "what does tapping Save vs.
releasing a slider vs. leaving the screen actually persist" affordance should wait for Group AO's
result.

### 3. What else is realistically doable now, without new captures, to make `PROTOCOL.md` more app-ready?

Honestly, not much. Most of what's genuinely doable without new captures is exactly Tier 1's
sign-off items above (which unlock documentation promotions, not new app-ready protocol knowledge
beyond what's already implementable) plus the one remaining static-analysis thread named in
question 1 (`gag`'s 16 construction sites). The bulk of what would meaningfully advance
`PROTOCOL.md`'s app-readiness from here — EQ's save semantics, Battery Option A's real behavior,
head gestures, the ANC-rotation Left/Right split, and the serial-number RPC identity — are all
genuinely capture-blocked, not something a future static-analysis-only session can close.

## Summary for the maintainer

**Completed this session**: five independent re-verifications (all reproduced or corrected the prior
reading, never silently accepted at face value), two genuine new findings that reverse or sharpen
existing HYPOTHESIS-level readings (the `hod.java` EQ-save second trigger; the `qjm`/`qjr`-not-`qie`
serial-number structural correction), and five new, fully-specified capture skeletons ready for the
maintainer to run (Groups AO–AS / `CAP-053`–`CAP-057`), plus a full documentation consistency sweep
across all eight files this prompt named.

**Decision inventory** (Phase 6 above, prioritized): 5 sign-off-only items, 3 decision-plus-data
items, 6 capture-blocked items.

**Readiness**: nothing blocks continued Phase 4 work on ANC/Battery-Option-C/Find-My-Buds/EQ (minus
the narrow "Save as preset" affordance); Phase 3's remaining gaps are almost entirely capture- or
sign-off-blocked, not something more static analysis alone can close from here.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13
