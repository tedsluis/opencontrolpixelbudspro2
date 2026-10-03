# TODO.md

Open tasks, grouped by phase. Check items off and move completed major items
to `CHANGELOG.md` (see `PROJECT_RULES.md` §6, rule 15, on technical debt
tracking).

## Recommended priority order (added 2026-08-23)

A cross-phase execution order, distinct from the phase grouping below (which organizes tasks by
*kind*, not by *when to do them*). This section is a sequencing layer only — each item's full
description still lives in its own phase/section below (or in `PROTOCOL.md`/`ARCHITECTURE.md` for
protocol/architecture open questions, per this file's own "Open questions" section at the bottom);
nothing here is a second copy of that detail, only a pointer plus the reasoning for the ordering.

1. **Decisions & sign-offs (no new data needed — cheapest, unlocks the most):**
   - Maintainer sign-off on pending 🟢 FACT promotions — see the new Phase 3 item below for the
     current list. Per `AGENTS.md` §6 this step can only be done by the maintainer, not an agent.
   - ~~DI approach (Hilt vs. manual) — Phase 4.~~ **Resolved 2026-09-13: Hilt** (`DECISIONS.md`
     ADR-028).
   - ~~Minimum Android API level — Phase 5.~~ **Resolved 2026-09-13: API 34** (`DECISIONS.md` ADR-029).
   - ~~Find My Buds Case/"both simultaneously" — whether to accept a Google Find Hub/account-mediated
     fallback for this one sub-feature or ship v1 without local Case-ring support (`PROTOCOL.md` §6,
     Behavior) — a genuine Zero-GMS scope trade-off, not a research gap; no capture or static
     analysis can resolve this, only a maintainer product decision can.~~ **Resolved 2026-09-13: ship
     v1 with Left/Right ring only** (`DECISIONS.md` ADR-027, `PROJECT.md` non-goals).
2. ~~**Start Phase 4 app development, ANC-first.**~~ **Done** (`ai-sessions/0013`–`0045`): ANC, EQ, Find My Buds Left/Right, battery
   (Left/Right via DLCI 0x04, ADR-033; Case via DLCI 0x02 `SubscribeRuntimeInfo`, ADR-043) and the Safe-Mode write gate (ADR-042) are implemented.
   Battery via HFP was removed — wire-confirmed but not consumable by an app (ADR-040). The remaining v1 gap is hardware verification
   (the re-test list in the latest `ai-sessions/` RESULT) and a first release.
3. **Phase 2 (APK reverse engineering) — updated 2026-08-30, no longer 0% done.** APK pulled,
   JADX/apktool-decompiled, and multiple `§4` keyword-search/follow-up passes done
   (`REVERSE_ENGINEERING.md`'s growing class-entry list, 30+ entries as of the last pass), and
   `DECISIONS.md` ADR-018 accepted (DLCI 0x02 channel-ownership → 🟢 FACT), and `.proto`/pw_rpc
   schema recovery — updated 2026-09-03, done via a different route than planned here: `pbtk`
   confirmed structurally incapable of this APK's codegen (both the whole-APK and a 2026-09-03
   targeted-class re-run wrote 0 `.proto` files; root cause is now source-cited, not just the
   tool's own caveat — see the Phase 2 checklist item below). The schemas were instead recovered
   by hand (`scripts/decode_rawmessageinfo.py`, `DECISIONS.md` ADR-019) and cross-correlated
   against wire captures for `qhr` fields 4, 7, 12, and 29. **Updated 2026-09-08 — the "remaining
   confirmed-but-unchecked field numbers" item this bullet used to point to is now fully closed**:
   fields 17/19/22/27/28 closed 2026-09-03, and fields 11/15 (the two the maintainer's `0002`
   sign-off was scoped to) closed 2026-09-08 — see this file's "Targeted research follow-ups"
   section below, `PROTOCOL.md` §4.5.2/§4.5.6/§4.5.7/§4.5.5a/§4.5.8/§4.5.1, `DECISIONS.md` ADR-019
   and its two Updates. **Correction, 2026-09-17 (`ai-sessions/0030`): the "current highest-leverage
   single next step" sentence this bullet used to carry here is stale and removed.** All three open
   APK-RE leads it named are now closed: the `MaestroDeviceSettingsProviderService` 6-case-ID→`qhr`-field
   forward trace closed 2026-09-08 (`DECISIONS.md` ADR-019 Update, this file's "Targeted research
   follow-ups" section below); `MaestroEndpointService`'s smali fallback read closed 2026-09-17
   (`ai-sessions/0027`, `structural_index field-writes` — see `REVERSE_ENGINEERING.md`'s
   `MaestroEndpointService` entry); `gjv.p()`'s caller trace closed 2026-09-15 (`ai-sessions/0023`). No
   single-item replacement is named here — see `PROTOCOL.md` §6 for the current, full open-questions
   list (this file's own "Open questions" section at the bottom points there rather than duplicating
   it) and `ai-sessions/0030_MAINTENANCE_RESULT_2026_09_17.md`'s own Phase 2 worklist for a
   static-analysis-tractable subset of it. **`CAP-033` (Group AA, `SDP-001`/`SDP-002`) is done
   (2026-08-30)** — see below.
4. **Remaining planned captures** (updated 2026-09-13 — see below for the next-test-session queue;
   `CAP-008`, `CAP-009`, `CAP-013`, `CAP-014`, `CAP-027`, `CAP-033`–`CAP-042` are done, see
   `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9):
   - **Next test session (added 2026-09-13, `ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13.md`) —
     5 short, self-contained captures, each with its own placeholder capture folder and event-notes
     skeleton already prepared under `captures/` (see each `CAP-0NN`'s own row in
     `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9), each ~3–10 minutes, no destructive/one-time action among
     them (all safely repeatable if a retry is needed):**
     1. `CAP-053` (Group AO) — EQ outer field 16-vs-18: isolate Save-tap / navigate-away /
        genuine slider-release from each other.
     2. `CAP-054` (Group AP) — Battery Notification, connection-free, bracketing a single-bud
        insertion/removal (the Fast Pair spec's own "optional" trigger, untested so far). *(2026-09-30, `ai-sessions/0059`: the spec
        names no such "optional" trigger — premise corrected in `CAP-054-EVENT-NOTES.md`; the capture stays planned as a plain re-test.)*
     3. `CAP-055` (Group AQ) — Nod/Shake head gestures with an actual active call/notification,
        camera also framing the gesture itself (needs a second phone to place the call).
     4. `CAP-056` (Group AR) — ANC-rotation-checklist Left/Right split, genuine re-run — **read the
        skeleton's anti-repeat safeguard first**, `CAP-045` skipped the actual checklist screen. **Done** (`ai-sessions/0055`, analyzed: no
        Left/Right field in the write, ADR-046).
     5. `CAP-057` (Group AS) — live `GetSoftwareInfo`/`GetHardwareInfo` correlation against the
        connect-time burst, using the firmware/serial-number screen. **Withdrawn** (`id_registry.csv`).
     6. `CAP-058` (Group AT, added 2026-09-18, `ai-sessions/0031`) — 3rd `SDP-001`/`SDP-002` attempt,
        adding an explicit on-device process-liveness check before the "Pair" tap (see
        `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AT and `CAP-044-FINDINGS.md` §5) — maintainer approved
        the go/no-go below.
   - Also still queued from before, lower priority than the 5 above: **`CAP-043` (Group Q repeat,
     Battery Notification BLE scan) is done (2026-09-13)** — a second confirmed non-match, see
     Phase 1 above and `CAP-054` in the next-test-session queue for its own follow-up.
   - **`CAP-044` (Group AA repeat, 2nd attempt, `SDP-001`/`SDP-002`) is done (2026-09-13)** — still
     🟡 HYPOTHESIS, a different isolation gap than `CAP-033`'s. **The Tier-2 go/no-go decision item in
     `ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13.md` Phase 6 is now resolved: go** — maintainer
     approved a 3rd attempt 2026-09-18 (`ai-sessions/0031`), now designed as `CAP-058` (Group AT)
     above.
   - `CAP-018` and the still-uncaptured main-run-through remainder
   (`CAP-026`, `CAP-029`–`CAP-030`) — **`CAP-028` (head gestures) is done (2026-09-12, inconclusive) and
   re-verified (2026-09-13); its follow-up is now tracked as planned `CAP-055` (Group AQ), not this
   bullet.** **Closed this update:** Group W's own untried GATT cache-busting methods —
   `CAP-034` (2026-09-01) combined `pm clear com.android.bluetooth` with a Pixel 9a never before
   connected to this Buds unit and fully resolved the `0x0c0X`/`0x0f2X` handle↔UUID mapping (see
   `PROTOCOL.md` §6, §4.3 Option D) — this bullet's own "untried" framing is now stale and removed.
5. **Targeted research follow-ups**, lowest priority, tracked at their source per this file's
   "Open questions" section: the `CAP-021` DLCI 0x0a burst trigger (`PROTOCOL.md` §6). (The DLCI 0x02 AES-128 hypothesis is closed:
   the Sent blocks decode as plaintext pw_rpc, ADR-034.) **Added 2026-08-28
   (2026-08-28 project-wide audit, Phase 5), three specific new-capture ideas, none yet designed
   in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`:**
   - `HOLD-005`'s Left/Right ANC-rotation-checklist split (`PROTOCOL.md` §6) — a purpose-built
     capture isolating one earbud's rotation list at a time (the envelope carries no
     Left/Right-distinguishing field for this specific write, unlike `HOLD-001`–`HOLD-004`). **Now
     designed, 2026-09-09: skeleton created as `CAP-045` (Group AJ, new — see
     `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1 and `ai-sessions/0005_MAINTENANCE_RESULT_2026_09_09.md`).**
     **`CAP-045` run 2026-09-12 but did not exercise Group AJ's own procedure** (physical
     press-and-hold ANC cycling was captured instead — the rotation-checklist screen was never
     opened); the question remains fully open. A genuine re-run, with a mandatory on-camera
     anti-repeat safeguard, is designed as Group AR (planned `CAP-056`,
     `ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13.md` Phase 4).
   - Volume balance (`field 17`) scale/direction (`CAP-022-FINDINGS.md` §5, `PROTOCOL.md` §4.5.7/§6)
     — a capture with isolated extreme-position samples (not a continuous drag) plus tighter video
     correlation. **Now designed, 2026-09-09: skeleton created as `CAP-046` (Group AK, new).**
   - The `CAP-021` DLCI 0x0a burst trigger, more precisely: a purpose-built hypothesis test
     (`PROJECT_RULES.md` §4's fixed template — hypothesis, setup, expected outcome, actual outcome,
     conclusion) bracketing candidate triggers one at a time (app backgrounded/foregrounded, a
     scheduled sync window, a charge-state change) — the burst recurred in exactly 1 of 16 sessions
     checked so far, so passively waiting for it to reappear is not expected to work. **Now designed,
     2026-09-09: skeleton created as `CAP-047` (Group AL, new; no existing Test-ID, flagged as a
     `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` follow-up).**
   - **Added 2026-08-30 (audit finding), closed 2026-09-08 (prompt `0002`, implementing
     `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` Phase 2/Phase 4):** apply `DECISIONS.md`
     ADR-019's same static-analysis method (matching a confirmed wire field number against the
     recovered `qhr` schema) to the remaining confirmed-but-unchecked DLCI 0x02 field numbers —
     `field` 11, 15, 17, 19, 22, 27, 28 (`PROTOCOL.md` §6's "what do DLCI 0x02's confirmed inner
     field numbers actually represent" item). Fields 17/19/22/27/28 were closed 2026-09-03
     (`DECISIONS.md` ADR-019 Update), though this bullet was never updated at the time to reflect
     that. Fields 11 (Multipoint) and 15 (Volume EQ) — the two the maintainer's sign-off for prompt
     `0002` was scoped to — are now closed too, via a forward trace from a named UI
     fragment/preference key to the write call site (`PROTOCOL.md` §4.5.2/§4.5.6,
     `REVERSE_ENGINEERING.md`'s `qhr` entry, `DECISIONS.md` ADR-025 Update). All 7 field numbers
     this item originally listed are now checked against the recovered `qhr` schema — item closed.
   - **Added 2026-09-03 (audit finding), closed 2026-09-08
     (`ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md` Phase 3 item 4):** traced `fyd.d`/`fyd.e`'s
     own call sites in the EQ UI fragment — field 16 fires from the slider-drag/preset path; field 18
     is reachable **only** via a dedicated, self-describing "On click save EQ button" handler, with
     no slider-release code path found anywhere. This closes the static-analysis question but
     **contradicts** `CAP-015`'s own wire-timing "fires on slider-release" hypothesis rather than
     confirming it — a genuine, unreconciled tension, proposed for maintainer review.
     **Re-verified 2026-09-13 (`ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13.md` Phase 1):** the
     "reachable only via Save button" reading was incomplete — a second call path exists
     (`hod.java`, a navigate-away-with-unsaved-changes trigger), adding a third candidate. A capture
     isolating all three (Save tap / navigate-away / genuine slider-release only) is proposed as
     `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AO (planned `CAP-053`). See
     `REVERSE_ENGINEERING.md`'s `qjw` entry and `PROTOCOL.md` §4.2/§6.
   - **Closed 2026-09-08 (`ai-sessions/0003`), re-confirmed 2026-09-24 (`ai-sessions/0045`: the page states no timing at all):** ~~re-verify `PROTOCOL.md` §4.3 Option A's "shown ≥8s,
     auto-hidden after 20s" Battery Notification visibility-timing claim directly against the
     official Fast Pair spec pages (a 2026-09-03 re-check found no such text on the
     `batterynotification` extension page specifically — downgraded to 🟡 HYPOTHESIS pending this
     check; the detail may live on a different spec page not checked yet, e.g. the base Message
     Stream spec).~~
   - **Added 2026-09-08 (`ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` Phase 1/Phase 4, prompt
     `0002`), closed 2026-09-08 (`ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md` Phase 3 item 1):**
     traced `MaestroDeviceSettingsProviderService`'s 6 case IDs to their exact accessor call —
     `2102`→`qhr` field 2, `2103`→field 27, `2104`→field 11 (Multipoint — the head-gestures lead
     below did **not** pan out; field 29 is not among these 6 mappings), `2113`→field 5, `2115`→no
     `qhr` field at all (a separate "Feature A" toggle), `2116`→field 32 (new). See
     `REVERSE_ENGINEERING.md`'s `MaestroDeviceSettingsProviderService` entry for the full trace,
     including two byproduct `qhr` register corrections (fields 6 and 32) and an unreconciled
     `CATEGORY_MULTIPOINT`-vs-`fpm.ENABLED_HEAD_GESTURES` naming tension on case 2104 — proposed for
     `PROTOCOL.md` promotion, pending maintainer review.
   - **Added 2026-09-08 (`ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` Phase 1, prompt `0002`),
     largely closed 2026-09-08 (`ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md` Phase 3 item 2):**
     `apktool` smali-fallback read of `MaestroEndpointService.onCreate()` done — the registered
     services come from a Dagger multibinding (`Map<String, Optional<ofd>>`) assembled elsewhere
     (names not recovered), and `ofd`'s method is a per-call, UID-based authorization check (not a
     service dispatcher as its shape first suggested) — two policies found, an internal-UID-only
     check and an allowlisted-Google-signed-caller check. See `REVERSE_ENGINEERING.md`'s
     `MaestroEndpointService` entry. **Closed 2026-09-17 (`ai-sessions/0027`, `structural_index
     field-writes`):** the "literal registered service names" question has a definitive answer —
     `MaestroEndpointService.b`'s own sole write site (`ghl.onCreate()`, the Hilt injection base
     class) assigns it directly from `lrw.b`, Guava's own zero-entry `ImmutableMap` singleton, with
     no Dagger provider call involved. **No gRPC service is registered on this endpoint at all, in
     this APK version** — not a hidden multibinding, a hardcoded empty constant. Whether GMS is ever
     in the allowlist remains moot given nothing is registered to allowlist a caller for.
   - **Formalized 2026-09-08 (`ai-sessions/0002_MAINTENANCE_RESULT_2026_09_08.md`'s own gap scan
     flagged this as never added to this list; re-attempted and still not closed by
     `ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md` Phase 3 item 3):** trace `gjv.p()`'s own
     caller — the remaining open link needed to determine whether `fxm.i()`'s `GetSoftwareInfo` fetch
     genuinely fires inside the `CAP-036`/`CAP-041` connect-time settling window
     (`REVERSE_ENGINEERING.md`'s `frb`/`fuh`/`glk`/`gjv` entry). Two static-analysis passes have now
     failed to locate this caller (generic-token searches on `.p()`/`.u = ` are unproductive against
     this app's R8 obfuscation) — the byte-level capture-correlation alternative (`PROTOCOL.md` §6's
     matching item) is now the recommended path, not a further static-analysis attempt, unless a
     future session identifies a more targeted search strategy.
     **Closed 2026-09-15 (`ai-sessions/0023`), found via a genuinely different strategy — a smali
     cross-reference on the abstract supertype's call descriptor (`Lgiz;->p(`) rather than a search
     for `giz`-typed fields.** The sole call site is `ftw.java`'s discriminator-9 lambda (an
     `OtaApplyWorker` completion callback, `"On apply finished."`), reached via an inline
     `ftj.i(str).p()` chain — never stored in a field, which is exactly why the field-search
     strategy in both prior passes structurally could not find it. **This makes `gjv.p()` an
     OTA-firmware-update-apply-completion trigger, not a generic connect-time/settling trigger** —
     it sharpens, rather than confirms, the original "connect-adjacent" reading, and makes it *less*
     likely (not more) that this specific call is what's inside `CAP-036`/`CAP-041`'s ordinary
     connect-time burst (neither session involved an OTA update). See
     `REVERSE_ENGINEERING.md`'s `frb`/`fuh`/`glk`/`gjv` entry's 2026-09-15 update for the full trace.
   - **Added 2026-09-11 (`ai-sessions/0008_CROSSCHECK_RESULT_2026_09_11.md`), full non-sampled
     validation of Gemini's `ai-sessions/0007_CROSSCHECK_RESULT_2026_09_11.md`.** Independently
     re-derived every citation in `0007`; roughly half of its line-number citations from §2.2 onward
     point to the wrong location, two (the claimed field-19 write site and the claimed field-17 write
     site) point to code with no connection to the claim at all. `0007`'s §2.6 "NEW INDEPENDENT
     FINDING" (`qhr` field 19 as a "Volume Balance extreme/gate boolean") is not new — it restates
     `REVERSE_ENGINEERING.md`/`DECISIONS.md` ADR-019's 2026-09-03 Update — and omits field 19's actual,
     already-approved primary identity ("Mono audio"), and its reported field-17 value (10) skips the
     zigzag-decode correction ADR-019 already documents (correct value: 5). `0007`'s Executive
     Summary's "100%/absolute certainty" language is not supported. **Do not act on `0007`'s
     recommendations directly** — see `0008`'s §3 for the maintainer decisions this raised (field 19's
     documentation should not be changed to "limit gate"; `0007`'s "approve all Phase 4 Promotions"
     recommendation needs to be evaluated per-item, not as a bundle).
   - **Added 2026-09-13 (`ai-sessions/0013_FEATURE_RESULT_2026_09_13.md`), housekeeping pointer for
     `0012`.** `ai-sessions/0012_CROSSCHECK_RESULT_2026_09_12.md` ran a full, non-sampled independent
     re-derivation of all 130 findings in Gemini CLI's `0011` review. Headline result: `0011`'s core
     protocol-decode content (opcodes, field mappings, byte-level payload claims) held up with zero
     errors; 5 citation errors and 1 overclaim were found, all in secondary/background material
     (co-occurring unrelated devices, one-off vendor commands, procedural timestamps), never in a
     primary protocol claim. Two pre-existing capture-count discrepancies `0012` itself surfaced
     (`CAP-036`'s "34-frame" burst count, `CAP-037`'s "34 reconnects" count) are now resolved — see
     `0012` §4 for both.

## Setup

- [x] Set up the Fedora development workstation (`WORKSTATION_PREPARATIONS.md`)
- [x] Claude Code and Google Antigravity installed and configured
- [x] GitHub repository created + first commit
- [x] License chosen — AGPL-3.0 (see `DECISIONS.md` ADR-002, `LICENSE`)
- [x] Core project documentation drafted: `AGENTS.md`, `PROJECT.md`,
      `PROJECT_RULES.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `PROTOCOL_NOTES.md`
      (retired 2026-08-15, see `CHANGELOG.md`),
      `REVERSE_ENGINEERING.md`, `DECISIONS.md`, `CHANGELOG.md`, `README.md`,
      `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`,
      `SCREENSHOTS_PIXEL_BUDS_APP.md`, `SCREENSHOTS_PIXEL_BUDS_WEB_APP.md`
- [ ] Review the repository for inconsistencies, vagueness, ambiguity,
      contradictions, errors, or undocumented choices — both within each file
      and between files — and address what's found. Status so far:
  - [x] `ARCHITECTURE.md` — reviewed and revised (transport layer naming
        aligned with RFCOMM-primary reality, DI/scanning-policy open questions
        added)
  - [x] `AGENTS.md` — reviewed and revised (stale license section, BLE-only
        framing assumption, and duplicate heading artifacts fixed)
  - [x] `PROJECT_RULES.md`, `PROJECT.md`, `DECISIONS.md`, `PROTOCOL.md`,
        `REVERSE_ENGINEERING.md`, `README.md`,
        `CHANGELOG.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`,
        `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`,
        `WORKSTATION_PREPARATIONS.md` — given a dedicated cross-consistency
        pass via the 2026-08-20 comprehensive documentation audit (see
        `CHANGELOG.md`) and a further external audit on 2026-08-22/23 (see
        `AUDIT_REPORT_2026-08-22.md` and `CHANGELOG.md`'s matching entry).
        This checklist item was left unchecked after that work already
        completed it — closed here to fix the staleness itself.

## Phase 1 — Bluetooth analysis

- [x] **Pipeline validation** — the HCI snoop → bugreport → `btsnooz.py`
      extraction → Wireshark chain (RFCOMM/SPP + BLE dissectors) confirmed
      working via `CAP-001` (Group Z), 2026-08-09. Logged in the Capture
      Index (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9).
- [x] **Pairing/bonding baseline** — forget-and-re-pair captured via `CAP-002`
      (Group A), 2026-08-09; a second, independent baseline via `CAP-003`
      (Group R) and a third via `CAP-004` (Group S). Logged in the Capture
      Index.
- [ ] Log every capture session in the Capture Index
      (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9) with a unique `CAP-NNN` ID and
      metadata (firmware version, Android version, app version, capture
      method — per `PROJECT_RULES.md` rule 11 and rule 14) — ongoing practice,
      not a one-time task; kept unchecked deliberately.
- [ ] Optionally, as a deliberate one-time capture (not before), trigger the
      factory-reset re-pair for comparison (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`
      §4.1 Group P #16 — destructive, also resets the Find My Device link, so
      this is a bonus capture, not a prerequisite). See also
      `WORKSTATION_PREPARATIONS.md`'s Disaster Recovery section — this is the
      same procedure, deliberately triggered as an experiment rather than as
      an emergency recovery step.

**Top priority (updated 2026-08-18) — these block implementation-readiness
for the app's core v1 features and outrank everything else below, including
the still-open edge-case protocol questions (DLCI 0x08's identity, Groups
0x04/0x05/0x09's semantics, the CTKD generalization, HFP `battchg` vs.
`AT+BIEV` discrepancy, etc. — those stay valuable research but are explicitly
lower priority than finishing ANC/Battery/EQ):**

- [x] **`CAP-005`/`CAP-015` (Group T) — EQ command isolation.** **Done
      2026-08-18** via `CAP-015`, a second, independent Group T session:
      field-to-band mapping promoted to 🟢 FACT (all 5 sliders individually
      isolated, 3 passes each), plus the ±6.0 band-gain clamp and a confirmed
      preset-quintet reference table (`CAP-015-FINDINGS.md`, `PROTOCOL.md`
      §4.2).
- [x] **`CAP-006` (Group B repeat) — ANC reliability confirmation.** **Done
      2026-08-15** — isolated single-tap repeat of all four ANC modes;
      exactly 4 `0x12` "Set ANC state" frames in the whole log, one per tap,
      zero misses (`CAP-006-FINDINGS.md` §3). `CAP-001`'s 2/6 gap does not
      reproduce under isolated conditions. `DECISIONS.md` ADR-009 updated,
      `FrameEncoder` implementation block for the ANC command **lifted**.
- [x] **`CAP-010`/`CAP-017`/`CAP-014` (Group W) — stronger GATT cache-busting for live
      service discovery.** **Discovery goal achieved 2026-08-16** via
      `CAP-017`, a fresh-GATT-client-app path not originally in this row's
      scope — 137 live discovery frames, full 15-service GATT profile
      recovered. **`CAP-014` (2026-08-27) fixed that session's snaplen truncation but still
      did not close the mapping** at the time (`CAP-014-FINDINGS.md` §4/§8). **Closed 2026-09-01 by `CAP-034`**
      (`pm clear com.android.bluetooth` on a never-connected Pixel 9a): the full handle↔UUID mapping is 🟢 FACT (`PROTOCOL.md` §6).
- [x] **`CAP-016` (Group U re-run) — case/bud-removal hardware events.**
      **Synced into `PROTOCOL.md` 2026-08-18** — promotes 3 🟢 FACTs (§5/§7):
      Buds-initiated reconnect on bud removal, ACL disconnect the instant
      both buds are re-docked, and case-lid open/close producing zero wire
      signal (now 2-capture-confirmed). New open items (RFCOMM channel-bounce
      trigger, ANC settable-toggles byte, a `0x0044` BLE notification burst,
      an `AndroidHeadTracker` HID Feature report) tracked in `PROTOCOL.md` §6
      and `CAP-016-FINDINGS.md`.
- [x] **`CAP-008` (Group V) — first real phone call.** **Done 2026-08-26.** Both open
      questions resolved: the full HFP AT-command SLC handshake reoccurs on a fresh classic-link
      connection, and two clean SCO/eSCO pairs appear, one per call. DLCI 0x0a stayed silent
      through both calls, ruling it out as the call's audio path (`CAP-021`'s later, unrelated
      1123-frame burst on that same DLCI remains a separate, still-open question — `PROTOCOL.md`
      §6). See `CAP-008-FINDINGS.md`.
- [x] **`CAP-009` (Group X) — battery-level discrepancy bracket.** **Done 2026-08-23.**
      `AT+CIND`/`battchg` confirmed a stale single snapshot; `AT+BIEV` confirmed per-earbud
      (Right, this session), not a fixed aggregate, and non-fixed-cadence — `BATT-006` closed,
      maintainer-approved (`DECISIONS.md` ADR-015). See `CAP-009-FINDINGS.md`.

**Next, still important but behind the above:**

- [x] **Capture the "Play sound on Left/Right earbud" (Find My Buds) action —
      done, `CAP-025` (2026-08-21).** Left/Right promoted to 🟢 FACT 2026-08-23 (`DECISIONS.md` ADR-011). **New finding:** Case/"both"
      route through a separate Find Hub/Find-My-Device-Network mechanism
      with no local wire command — possibly a Zero-GMS hard limit, flagged
      to the maintainer in `PROTOCOL.md` §6 (Behavior) and
      `CAP-025-FINDINGS.md` §7/§8.
- [x] **Passively capture a BLE scan to confirm the Battery Notification
      advertisement — attempted, `CAP-011` (2026-08-21), inconclusive.**
      Fast Pair Service (`0xFE2C`) traffic confirmed present, but the
      procedure deviated (an active RFCOMM connection was present
      throughout, not the intended connection-free scan) and the sampled
      payloads don't structurally match the documented byte layout — see
      `PROTOCOL.md` §4.3 Option A and `CAP-011-FINDINGS.md`. **Still open:**
      a genuinely clean, connection-free repeat is needed — skeleton created 2026-09-09 as
      `CAP-043` (Group Q repeat, see `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 and
      `ai-sessions/0005_MAINTENANCE_RESULT_2026_09_09.md`). **`CAP-043` done 2026-09-13**: genuinely
      clean isolation confirmed, a second confirmed non-match against Option A's documented layout —
      closes the active-connection-confound question, but only the idle/case-closed trigger condition
      was tested. A single-bud-insertion/removal bracket (the Fast Pair spec's own "optional" trigger)
      is proposed as `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AP (planned `CAP-054`,
      `ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13.md` Phase 2).
- [x] **`CAP-013`/`CAP-031`/`CAP-032` (Group A repeat) — whether "Forget" fully clears prior BLE
      association.** **Done 2026-08-27**, on the fourth attempt (`CAP-032`) — the first three
      (`CAP-001`'s original session, `CAP-013`, `CAP-031`) all either predate the question or
      failed to capture the pre-clearing-action window; `CAP-032`, extracted via the raw path
      instead of the lossy `btsnooz` fallback, finally captured it and found a clean
      counter-example (no prior BLE link/valid key for that session) — `CAP-001`'s own
      session-specific puzzle (why *that* session had residual state) remains independently open,
      see `PROTOCOL.md` §6 (Behavior). See `CAP-032-FINDINGS.md`.
- [ ] **Updated 2026-08-28 — remaining planned captures not yet individually tracked here** (each
      already has its own row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's Capture Index; listed here
      only so this file's priority ordering covers them too, not as a duplicate description):
      `CAP-018` (Group Y, `0x0044` BLE-notification-burst isolation), and the still-uncaptured main
      run-through remainder — `CAP-026` (Group L, passive observation; **analyzed 2026-09-12**, `id_registry.csv`). **`CAP-027` (Group N, touch
      gestures) is done, 2026-08-30** — see `CAP-027-FINDINGS.md`. **`CAP-028` (Group O, head
      gestures) run 2026-09-12 — inconclusive**: zero wire-visible traffic during the claimed
      gesture window, but no active call/notification existed for a gesture to act on, so this
      cannot distinguish "functionally inert, as expected" from "gesture not triggered." Re-verified
      across the full log 2026-09-13, same clean negative (`ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13.md`
      Phase 3) — a correctly-scoped repeat with an active call/notification is designed as Group AQ
      (planned `CAP-055`). `CAP-029` is **analyzed 2026-09-12** (Conversation Detection, `PROTOCOL.md` §6). Still to do from its original Group P scope: (Conversation Detection voice trigger + the optional,
      destructive factory-reset comparison + the still-open shorter-press pairing-mode question),
      and `CAP-030` (Group Q items #19–20, Loud Noise Protection/Adaptive Audio, needs firmware
      ≥4.467 — worth double-checking this against the project's `release_5.203` baseline first,
      since the two version identifiers have never been explicitly reconciled, `PROTOCOL.md` §0.1).
      Lower priority than a clean `CAP-011` repeat and a properly-done Group W attempt above.
- [x] **Six captures planned 2026-09-05, follow-ups to `CAP-036`'s `OBS-004` session — done and
      analyzed 2026-09-06, updated here 2026-09-08 (was stale: still listed as "planned" though
      `PROTOCOL.md` §8's 2026-09-06 changelog row already syncs their findings).** `CAP-037` (Group
      AD, purpose-built repeat of the "Get ANC state" reconnect-reliability + dock-state-transition
      question) ran far longer than planned — 34 reconnects, 26/26 zero-miss `DECISIONS.md` ADR-022
      replications and 26/26 ADR-024 dock-state matches, the largest single-session replication of
      either on file. `CAP-038` (Group AE, realistic buds-out-of-case-and-worn reconnect) found a
      `Settable-toggles=0x00` reading immediately after physical case-removal, in unreconciled
      tension with ADR-024 (`PROTOCOL.md` §6, still open). `CAP-039` (Group AF, Set-vs-Get
      comparison) gave 10/10 same-session confirmations of ADR-024's trigger-independence. `CAP-040`
      (Group AG, DLCI 0x08 unmapped codes) found the app's own in-app Connect/Disconnect buttons
      produce zero wire signal, leaving its 7 target codes at N=1 each — inconclusive, still open.
      `CAP-041` (Group AH, connect-time burst vs. non-default settings) found the burst's
      length/shape signature invariant across 3 non-default states — a scoped negative at the
      length level; a full content diff remains open (Phase 4 item 1 below). `CAP-042` (Group AI,
      long idle bracket) found the periodic cross-channel push far sparser than `CAP-036`'s short
      sample suggested, with HFP dropping out of the sync entirely. See each capture's own findings
      file (in its `captures/CAP-0NN-...` folder) and `PROTOCOL.md` §6's matching open items for
      full detail.

## Phase 2 — APK reverse engineering

- [x] **Added 2026-09-16, implemented same day — Lambda Dispatcher Resolver.**
      `reverse-engineering/tools/lambda_dispatcher_resolver/` (`SPEC.md` for the design; `README.md`
      for usage) mechanically resolves R8-merged synthetic lambda-dispatcher classes (the
      `krb`/`aie`/`esk`/`ftw` pattern this project has repeatedly had to resolve by hand — see
      `REVERSE_ENGINEERING.md`'s `qhr`/`fye` entry and `frb`-`gjv` entry) to their exact smali+JADX
      source evidence, via a `list`/`resolve` CLI over androguard (DEX structure) + a narrow smali
      reader (packed-switch/sparse-switch/if-chain resolution) + a JADX case-block correlator. All of
      `SPEC.md` §10's acceptance criteria pass (12 pytest cases, including the adversarial
      out-of-range-discriminator fixture) against the real, locally-decompiled APK — no decompiled
      content is committed anywhere in the tool's own tree (a design mistake in `SPEC.md`'s original
      §11 text, caught and corrected during implementation; see that section's own note). Deliberately
      scoped to only this one capability — a possible future broader UUID/BLE-analysis pipeline is
      tracked as ideas, not designed, in `reverse-engineering/tools/BACKLOG.md`.
      **Update (2026-09-16, `ai-sessions/0024`) — now actually used, `resolve-all` on `aie`/`esk` full
      case sets.** `.venv/bin/python3 -m pytest tests/ -v` re-run before use: **14/14 passing**
      (`reverse-engineering/tools/lambda_dispatcher_resolver/SPEC.md`'s own count grew from 12 to 14 once the `resolve-all`-specific fixtures were added —
      this bullet's "12 pytest cases" phrasing above is now stale, noted here rather than silently
      left). `resolve-all --class aie` and `--class esk` both ran cleanly (21 files each, no
      `resolution_status: "ambiguous"`, no tool bug) — see `ai-sessions/0024_AUDIT_RESULT_2026_09_16.md`
      Phase 1 for the full per-case read (all 42 cases, no sampling). Headline: only 1 of `aie`'s 20
      real cases (discriminator 7) and 3 of `esk`'s 20 real cases + default (discriminators 18/19,
      plus the default branch) are Bluetooth/Maestro-relevant; the rest are checked negatives
      (unrelated app-UI code for `aie`; bundled AndroidX WorkManager DAO internals for 17 of `esk`'s
      cases). Two new leads recorded in `REVERSE_ENGINEERING.md`'s new `esk` entry: a located write
      site for the previously-unlocated "Feature A" mechanism (discriminator 18, `ftf.java:312`), and
      a previously-uncatalogued `device_info`-table Room DAO pair (`gcp`/`gcn`, the default branch,
      `gcp.java:51`). **Relationship to `gcl`/`gck`/`eht` resolved 2026-09-16 (`ai-sessions/0025`,
      via the new `structural_index` tool below): they are the same `device_info` data pathway at
      different layers** (`gck` holds a `gcn`-typed field; `gcn.f()` itself constructs `gcl`) —
      see `REVERSE_ENGINEERING.md`'s `esk` entry's own 2026-09-16 update for the full trace. Not a
      second, distinct accessor after all — this specific open question is closed, code-level-only,
      not requiring maintainer FACT sign-off since it makes no protocol-behavior claim.
      **`MaestroEndpointService`'s own multibinding-assembly search advanced but not closed,
      2026-09-16 (`ai-sessions/0025`)** — `structural_index` found `ofd`'s 3 implementations
      (`mie`/`oex`/`ofb`, confirming no 4th exists) and 3 new field-type holders (`ofh`/`ofi`/`ofj`),
      but reading them showed they are generic gRPC transport-builder plumbing, not the assembly
      site — a checked negative, not a resolution; see `REVERSE_ENGINEERING.md`'s
      `MaestroEndpointService` entry's own 2026-09-16 update. **Resolved 2026-09-17
      (`ai-sessions/0027`, `structural_index field-writes` — a new v1.1 capability built this
      session):** the field's sole write site (`ghl.onCreate()`) assigns it directly from `lrw.b`,
      Guava's own zero-entry `ImmutableMap` singleton — no gRPC service is registered on this
      endpoint at all, in this APK version; a hardcoded empty constant, not a hidden multibinding.
- [x] **Added 2026-09-16, implemented same day — Structural Index (`ai-sessions/0025`, implementing
      `ai-sessions/0024`'s top-1 `reverse-engineering/tools/BACKLOG.md` priority).**
      `reverse-engineering/tools/structural_index/` (`reverse-engineering/tools/structural_index/SPEC.md` for the design; `README.md` for
      usage) generalizes `lambda_dispatcher_resolver`'s own Layer 1 (`androguard_index.py`, reused
      directly, not re-implemented) into a standing reference-search query: given a class, list
      every other class/method that (a) constructs it, (b) calls one of its methods, or (c) holds
      it as a field type, via a `refs`/`unreferenced` CLI. All 3 of `reverse-engineering/tools/structural_index/SPEC.md` §10's named
      acceptance criteria pass (7 pytest cases) against the real, locally-decompiled APK — `esk`'s
      21 construction sites (matching `ai-sessions/0024`'s own by-hand `grep` count exactly),
      `giz.p()`'s sole caller (`defpackage.ftw`, method `a`, matching `ai-sessions/0023`'s
      `Lgiz;->p(` smali-grep finding from one structured query instead of a raw grep), and the
      "which of `aie`'s never-catalogued referenced classes have zero external references" check
      (`Laly`/`Lcvo`/`Lgza` all confirmed externally referenced). No decompiled content committed
      anywhere in the tool's own tree (`.gitignore`'s tool-glob pattern covers it automatically).
      **Used for real APK-RE work the same session** — see this file's Phase 2 section and
      `REVERSE_ENGINEERING.md` for what it found on open questions B/F/G/H/J.
      Deliberately v1-only: an `implements`-query (needed for item B's Dagger-multibinding search)
      and a resource/string-table search (item M) are both explicitly deferred, per
      `reverse-engineering/tools/BACKLOG.md`'s own "start narrow" sketch — not built this pass.
- [x] **Added 2026-09-16, implemented same day — Schema Batch-Extractor (`ai-sessions/0025`
      resumption, implementing `ai-sessions/0024`'s top-2 `reverse-engineering/tools/BACKLOG.md`
      priority).** `reverse-engineering/tools/schema_batch_extractor/` (`SPEC.md` for the design;
      `README.md` for usage) generalizes `scripts/decode_rawmessageinfo.py` (reused directly via a
      `sys.path` insertion, not re-implemented or modified) into a batch mode: scans the whole
      `jadx-output/sources/` tree (12,545 files) for every `new naa(...)` compact-schema
      construction and decodes each one's full field-level schema in one pass, plus a derived
      reverse index (`refs`) of which other classes' own schema references a given class via a
      oneof/repeated/map field. All 6 of `SPEC.md` §10's named acceptance criteria pass (12 pytest
      cases) against the real, locally-decompiled APK: `qhr`/`qjc`/`qja`/`nqx`/`qjb` all reproduce
      their already-known field counts/types/message-refs exactly; the whole-tree scan finds exactly
      807 candidates (matching `REVERSE_ENGINEERING.md`'s own independently-obtained header-only-sweep
      count) with zero unparsable constructions; and a disclosed-limitation regression confirms the
      tool correctly returns no incoming reference for `ndi` despite `nef` genuinely holding it as a
      plain (non-oneof) field — a real, documented scope boundary (a plain `MESSAGE` field's type is
      not encoded in the compact schema string at all, per protobuf-lite's own algorithm), not a bug.
      No decompiled content committed anywhere in the tool's own tree.
      **Used for real APK-RE work the same session** — see `REVERSE_ENGINEERING.md`'s "Candidate rich
      schemas" section's 2026-09-16 update for the full field-level schemas of all 12 candidates
      (previously only header counts were known) and a newly-found nesting/reference graph one layer
      further out (`mtn`⊃`msw`; `mtn`⊂{`mqm`,`mra`,`mqk`}; `qaj`⊂`qak`, `qaj`⊃`qaz`; `qar`'s map field
      → `qaq.a`) — 7 genuinely new, previously-uncatalogued class leads (`mqm`/`mra`/`mqk`/`mtg`/
      `qak`/`qaz`/`qam`), none traced further this pass.
- [x] **Added 2026-09-16, implemented same day — UUID Extraction + BLE/GATT Context Reconstruction
      (`ai-sessions/0025` resumption, implementing `ai-sessions/0024`'s top-3
      `reverse-engineering/tools/BACKLOG.md` priority).**
      `reverse-engineering/tools/uuid_ble_context/` (`SPEC.md` for the design; `README.md` for usage)
      runs a genuinely unseeded, blind regex sweep of the whole `jadx-output/sources/` tree for every
      UUID-shaped literal, tags each occurrence's file with a textual BLE/GATT/RFCOMM-API
      co-occurrence signal (a fixed name list, never a call-graph trace), and reuses
      `structural_index`'s own `find_refs`/`load_apk` directly (a two-hop reuse chain down to
      `lambda_dispatcher_resolver`'s Layer 1) for the "usage location" half, via an `extract`/`context`
      CLI. All 6 of `SPEC.md` §10's named acceptance criteria pass (9 pytest cases) against the real,
      locally-decompiled APK. No decompiled content committed anywhere in the tool's own tree.
      **Used for real APK-RE work the same session** — found exactly 6 distinct UUID-shaped literals
      in this APK version (the 5 already-registered forms, reconfirmed, plus one genuinely new,
      non-Bluetooth find: an AndroidX WorkManager `Data`-serialization sentinel string,
      `95ed6082-b8e9-46e8-a73f-ff56f00f5d9d`, in `defpackage/ehs.java`) — see `REVERSE_ENGINEERING.md`'s
      UUID register's 2026-09-16 update for the full write-up, including a demonstrated real-data
      limitation of the co-occurrence heuristic itself (`fqg.java` is genuinely Bluetooth-adjacent but
      shows `false`, since it never names a BT API directly).
- [x] **Added 2026-09-16, implemented 2026-09-17 — Limited Dataflow Analysis (`ai-sessions/0025`
      resumption, continued after a mid-task rate-limit interruption; implementing
      `ai-sessions/0024`'s top-4 `reverse-engineering/tools/BACKLOG.md` priority).**
      `reverse-engineering/tools/limited_dataflow/` (`reverse-engineering/tools/limited_dataflow/SPEC.md`
      for the design; `README.md` for usage) traces one register forward, instruction by instruction,
      through a resolved dispatcher branch or a plain method body — reusing `lambda_dispatcher_resolver`
      directly (via a `sys.path` insertion to its `src/`, not re-implemented) for method/branch
      location — recognizing exactly four shapes (alias/`move-object`, cast/`check-cast`, sink
      use/`invoke-*` argument, basic-block boundary) and stopping, never guessing, on anything else.
      Strictly single-basic-block only, per `reverse-engineering/tools/BACKLOG.md`'s own risk-scoping
      for this idea. All of `reverse-engineering/tools/limited_dataflow/SPEC.md` §10's acceptance
      criteria pass (10 pytest cases: 6 against the real APK, 4 synthetic fixtures that always run)
      against the real, locally-decompiled APK. **Two of that document's own §10 acceptance-criteria
      *descriptions* were found wrong against the real APK while building the tests, and corrected in
      place rather than weakened to match a bug** — item 1's claimed `final_status` for the `esk`
      discriminator-19 `v0` trace, and item 2's named register (`p1` does not exercise the case it's
      meant to; `v1` does) — see that document's own corrected §10 text for the full explanation. No
      decompiled content committed anywhere in the tool's own tree.
      **Used for real APK-RE work the same session** — confirmed the primary regression fixture
      (`esk` discriminator 19's already-known `WriteSetting` chain, including the disclosed
      non-reach of `nqo.e(...)`, per that document's own §1/§9), the adversarial basic-block-boundary
      fixture, and one genuinely new trace on `esk` discriminator 18 (the "Feature A" write site) —
      see `REVERSE_ENGINEERING.md`'s `esk` entry for the finding. Cross-method/cross-block tracing,
      constant-propagation, and array-content tracking remain explicitly deferred (that document's
      §2.2/§9), not built this pass.
- [x] **Groundwork/tooling — done 2026-08-30.** Governance, storage, and procedure now in place so
      the actual analysis work below can start; none of it constitutes analysis having happened yet:
      `DECISIONS.md` ADR-017 (supersedes ADR-003) permits AI mechanical assistance — search, `pbtk`
      extraction, native `.so` disassembly explanation — within a maintainer-decides-relevance
      boundary; `WORKSTATION_PREPARATIONS.md` documents `pbtk` installation/real scope/dependencies;
      a versioned APK storage structure exists (`reverse-engineering/apk/v<versionName>-<versionCode>/`,
      indexed in the git-tracked `reverse-engineering/APK_VERSIONS.md`, itself gitignored for the
      APK/decompiled output per `.gitignore`); `APK_REVERSE_ENGINEERING_PROCEDURE.md` documents the
      full pull → decompile → extract → search → analyze procedure, including the
      diff-against-previous-version pass and the out-of-scope exclusion list (AccountLinking/
      OwnershipTransfer/AccessoryNonOwner/Firebase-Analytics-Crashlytics); `REVERSE_ENGINEERING.md`'s
      template now requires a file+line citation per finding and a hypothesis-to-capture-test link.
- [x] **APK pulled — done 2026-08-30.** `v1.0.955078536-10253511` (base + `arm64_v8a`/`xxhdpi`
      splits), pulled from the maintainer's own Pixel 7a, hashed, and recorded in
      `reverse-engineering/APK_VERSIONS.md` per `APK_REVERSE_ENGINEERING_PROCEDURE.md` §2.
- [x] **JADX decompilation — done 2026-08-30.** `jadx-output/` (12,545 Java/Kotlin files); 22
      non-fatal per-class errors, typical for an obfuscated multi-dex app of this size.
- [x] **apktool decompilation — done 2026-08-30.** `apktool-output/` (base) and
      `apktool-output-arm64_v8a/` (native libs live only in that split, not in base.apk).
- [x] **Keyword search (§4 pass) — done 2026-08-30, one pass; more passes still valuable.**
      Found: no `libmaestro.so`/`libgfps.so` anywhere (only `libandroidx.graphics.path.so`/
      `libpw_tokenizer_jni.so` — the app's Maestro logic is pure Kotlin, not a native binary,
      contra this project's original assumption); the app's own RFCOMM-socket-selection logic
      (`gbm.java`/`fzd.java`) and its two candidate SDP UUIDs ("pigweed"/"default"); literal
      `maestro_pw.*` pw_rpc service/method names (`Maestro.WriteSetting`/`GetSoftwareInfo`,
      `HeadGesture`, `EartipFitTest`, `Dosimeter`, `JitterBuffer`, `Multipoint`,
      `DynamicServerConfigService`) and a surviving `dev.pigweed.pw_rpc.MethodClient` reference
      confirming the app's own transport vocabulary. Full write-up: `REVERSE_ENGINEERING.md`'s
      "Identified relevant classes" section (10 entries). **Not yet done:** a second pass tracing
      how `ClassicBTReceiver`'s connection-state events lead into `gbm`'s socket selection, and how
      `fsz`'s `WriteSetting`/`fux`'s per-service calls obtain their `MethodClient` — flagged as
      untraced in `REVERSE_ENGINEERING.md`'s Call graph notes.
- [x] **Extract real `.proto`/pw_rpc schemas — done 2026-08-30/2026-09-03, via manual decode, not
      `pbtk`.** `pbtk-jar-extract` against `base.apk` wrote 0 `.proto` files, and a 2026-09-03
      follow-up confirmed this isn't a scope/targeting problem: `pbtk-jar-extract` has no
      class-filter flag, and a manually-built 32-class targeted JAR (`qjc`/`qja`/`qhr`/`nqx`/`fux`/
      `fsz`/etc., plus the one legacy `CodedInputStream`/`CodedOutputStream`-signature class pair
      still present elsewhere in the APK) still produced 0 files — confirmed against `pbtk`'s own
      `jar_extract.py` source: its extraction requires a per-class `mergeFrom(CodedInputStream)`
      `switch`-structure in the generated class's own bytecode, which this APK's
      `GeneratedMessageLite.newMessageInfo(default, infoString, objects)` reflection-based codegen
      never emits, for any class. `pbtk`'s GUI shares the same extractor module, so it is not
      expected to differ. **Solved instead via `scripts/decode_rawmessageinfo.py`** (a
      dependency-free `RawMessageInfo` compact-schema-string decoder, ported field-for-field from
      the public `protobuf` runtime source): `qjc`/`qja` (5-alternative oneof), `qhr` (38 fields,
      all field-type/reference info recovered), and `nqx` (`pw_rpc.RpcPacket`, 7 fields) all
      decoded and cross-correlated against real wire bytes (`CAP-020` frames 1741/1935). See
      `DECISIONS.md` ADR-019 (maintainer sign-off obtained) for the accepted findings. Fields 11, 15, 17, 19, 22, 27, 28 were
      run through the same method later (closed 2026-09-03/08, ADR-019 Updates). No `.proto` build input exists by decision (ADR-041).
- [x] **DLCI 0x02 channel-ownership question — resolved 2026-08-30 (narrow promotion).**
      `DECISIONS.md` ADR-018 (Option 2, maintainer-approved): DLCI 0x02 confirmed 🟢 FACT as the
      companion app's own internal RFCOMM channel (SDP UUID `25e97ff7-...` = RFCOMM channel 1 =
      DLCI 0x02, cross-checked against `CAP-001`/`CAP-002`/`CAP-032`), via the app's own
      `gbm.java`/`fzd.java` selection logic — see `PROTOCOL.md` §2.2a. **Not fully resolved:**
      whether the Sent-direction payload *content* specifically carries `libmaestro`'s settings
      commands, in general — still 🟡 HYPOTHESIS (strong) per `DECISIONS.md` ADR-019's own scope
      note, though now substantially strengthened for the 4 `qhr` fields ADR-019 sampled. Settling
      it further means running more fields through the manual-decode item above, not `pbtk`.
- [x] **`CAP-033` (Group AA) — done 2026-08-30.** Tested whether the second, never-observed-on-the-wire
      "default internal rfcomm socket" SDP UUID (`gbm`/`fzd`) ever appears when SDP is queried by the
      OS's own pairing flow before the companion app opens (`SDP-001`); `SDP-002` not attempted (no
      firmware update pending). Result: the "default" UUID still does not appear; the full named
      service list (including "MAESTRO APP") is returned even with the app force-stopped — but a
      confirmed Forget-before-Force-stop procedure deviation and a never-executed step 3 (opening the
      app for a baseline comparison) cap `SDP-001` at 🟡 HYPOTHESIS, not a clean result either way. A
      proper isolation-clean repeat is still needed (see `CAP-033-FINDINGS.md` §8). **New lead,
      unplanned:** the session's SDP browse also named DLCI 0x08 "GSND CONTROL" and DLCI 0x0a "GSND
      AUDIO" for the first time — see `PROTOCOL.md` §2.3/§6.

- [x] **GMS/Play Services reverse-engineering — decided out of scope, 2026-09-07 (`DECISIONS.md`
      ADR-025).** A project-wide audit (`AUDIT_REPORT_2026-09-07.md` §1.0) found no trace of DLCI
      0x04's Fast Pair Message Stream or DLCI 0x08's private envelope anywhere in the companion app's
      own decompiled code — both appear to be implemented inside Google Play Services itself. The
      resulting scope question (should this project decompile GMS to close that gap?) is now decided:
      **no.** DLCI 0x04/0x08 `FrameEncoder`/`FrameDecoder` work proceeds independently (not "clean-room" — ADR-025's 2026-09-24 Update), from wire-capture
      evidence alone (plus, for DLCI 0x04, the public Fast Pair spec) — the same method already used
      for ANC/Find My Buds/EQ, none of which ever needed a companion-app code cross-reference. No
      further APK-search effort should be spent trying to locate DLCI 0x04/0x08 transport code in this
      companion app; `REVERSE_ENGINEERING.md`'s empty Message Group/Code register for these two
      channels reflects this, not an unfinished search.

## Phase 3 — Protocol reconstruction

- [ ] **Fill in the UUID register (`REVERSE_ENGINEERING.md` §UUID register) — cross-referencing pass
      done 2026-09-09 (`ai-sessions/0004_MAINTENANCE_RESULT_2026_09_09.md` Task 2), clean negative.**
      `CAP-034`'s 8 wire-confirmed GATT UUIDs (Fast Pair Service `0xFE2C`/`FE2C1233`–`FE2C1239`,
      Device Information `0x180A`, Battery Service `0x180F`/`0x2A19`, Firmware Revision `0x2A26`,
      Accessory Non-Owner Service `15190001-...`, "Unknown Service" `109b862f-...`) were searched for
      across the entire decompiled tree (`jadx-output/` and `apktool-output/smali*/`, both full and
      short UUID forms) — **zero genuine matches found**, corroborating `DECISIONS.md` ADR-025's
      existing finding that this companion app's own code contains no Fast-Pair-GATT handling at all.
      Recorded as a checked negative in the register itself rather than left silently untried. Kept
      unchecked as "exhaustive" is still not claimed — a future APK version or a different keyword
      angle could still surface something — but this specific, concretely-scoped next step is done.
- [x] **Message Group/Code register — confirmed empty by design, not stalled (updated 2026-09-09).**
      The Fast Pair Message Stream framing hypothesis *is* confirmed (`PROTOCOL.md` §2.1/§4.1), but
      `DECISIONS.md` ADR-025 and `REVERSE_ENGINEERING.md`'s own register note already establish this
      table is expected to stay empty for DLCI 0x04/0x08 specifically — their transport lives inside
      Google Play Services, not the companion app's own decompiled code, so no vendor-specific
      Group/Code value will ever be found there to fill this table with. Checked here, not
      unfinished work.
- [ ] **Resolve the framing question — updated 2026-09-09, resolved per channel, not monolithic.**
      Per `PROTOCOL.md` §2.3/`ARCHITECTURE.md` §5's own per-channel implementation gate: DLCI 0x02
      (Pigweed `pw_hdlc`) and DLCI 0x04 (official Fast Pair Message Stream) are both 🟢 FACT and
      implementation-unblocked — `FrameEncoder`/`FrameDecoder` work for either is **not** blocked on
      this item. Only DLCI 0x08's own identity remains 🔴 open (structurally decodable, `[Group][Code]
      [Length][Value]`, but which protocol it belongs to is unresolved) — kept unchecked for that one
      remaining channel only, not for the framing question as a whole.
- [ ] **Document the full connection lifecycle with real capture evidence — updated 2026-09-09,
      step 3 now analyzed and maintainer-reviewed.** `PROTOCOL.md` §5.1 already promotes the classic
      BR/EDR link-establishment mechanics (steps 1–2) to 🟢 FACT across seven independent captures.
      **Step 3 (the RFCOMM channel-opening sequence) is now analyzed**: a new `PROTOCOL.md` §5.2
      records 🟡 HYPOTHESIS (strong) that DLCI 0x02 (`libmaestro`) reliably opens *last* of the five
      data-carrying RFCOMM channels on a fresh reconnect (6/6 independent instances across
      `CAP-036`/`CAP-037`/`CAP-041`, zero counter-examples in that condition, one honestly-scoped
      exception during a mid-session channel-bounce) — the maintainer reviewed this directly and
      explicitly chose to keep it at HYPOTHESIS rather than promote, pending more evidence or an
      explanation for the exception (see `ai-sessions/0004_MAINTENANCE_RESULT_2026_09_09.md` Task 3
      for the full analysis). **Still open**: steps 4/6 (the Message Stream/`libmaestro` handshake's
      own internal content ordering beyond channel-open timing, and user-triggered-command timing) —
      not attempted this pass.
- [ ] **Bring the first command to full 🟢 FACT status — updated 2026-09-24:** ANC, battery Option B (ADR-031/033) and the Case message
      are FACT and implemented — the Case from DLCI 0x02 `SubscribeRuntimeInfo` (ADR-043; ADR-014's DLCI 0x08 path was withdrawn, corrected
      2026-09-30, `ai-sessions/0059` A58-HK-03); Option C is FACT on the wire but not app-consumable (ADR-040). Only battery Option A remains, and
      that item is capture-blocked. **Original 2026-09-09 text:** ANC
      (`PROTOCOL.md` §4.1) reached full FACT status 2026-08-12 (see the checked item immediately
      below). Battery via HFP (`PROTOCOL.md` §4.3 Option C) is also already 🟢 FACT
      (`DECISIONS.md` ADR-015/ADR-023). Only battery Option A (the Fast Pair BLE Battery
      Notification) remains open, and it is specifically blocked on the still-outstanding clean,
      connection-free BLE-scan repeat this file's Phase 1 section already tracks (`CAP-011` was
      inconclusive; skeleton now created as `CAP-043`, see Phase 1 above) — not a research gap an AI
      session can close without that capture.
- [x] Bring ANC mode switching to full 🟢 FACT status (`PROTOCOL.md` §4.1) —
      **done 2026-08-12** via deskresearch correlation against the official
      Fast Pair "Hearable Controls" spec + `CAP-001`'s existing capture.
      `DECISIONS.md` ADR-009 (added 2026-08-15) blocked `FrameEncoder` for
      this command pending `CAP-006`, since 2 of `CAP-001`'s 6 ANC taps
      produced no command frame. **`CAP-006` (2026-08-15) resolved this — 4/4
      isolated taps produced a matching frame, zero misses — and ADR-009 was
      updated to lift the block.** `FrameEncoder`/`FrameDecoder` for the ANC
      command is now implementation-ready per `AGENTS.md` §6.
- [ ] Log every hypothesis test in the relevant capture's `CAP-NNN-FINDINGS.md` before promoting a finding
      from HYPOTHESIS to FACT (`PROJECT_RULES.md` §4)
- [x] **Added 2026-08-23 — maintainer sign-off session on pending FACT promotions. Done
      2026-08-23.** Per `AGENTS.md` §6 an agent may propose but never commit these; the maintainer
      reviewed all three and approved: Find My Buds Left/Right → 🟢 FACT (`PROTOCOL.md` §4.4,
      `DECISIONS.md` ADR-011); the wire-baseline firmware version `"release_5.203"` → 🟢 FACT
      (`PROTOCOL.md` §0.1, ADR-012); the general-purpose DLCI 0x02 settings-write envelope
      **shape** → 🟢 FACT, but explicitly **not** its 9+ individual field mappings, which stay 🟡
      HYPOTHESIS per the maintainer's own narrower decision (`PROTOCOL.md` §4.5, ADR-013).

## Phase 4 — App development

- [x] **Set up the Android Studio project per `ARCHITECTURE.md` — done and verified
      2026-09-13 (`ai-sessions/0013_FEATURE_RESULT_2026_09_13.md` Phase 7).** Five Gradle modules
      (`:app`, `:ui`, `:domain`, `:data`, `:hardware`) at `android/`, version catalog
      (`android/gradle/libs.versions.toml`, pinned versions per `AGENTS.md` §10). `./gradlew
      assembleDebug testDebugUnitTest test` actually run in this environment (Gradle 9.5.1 wrapper
      pinned to 8.9, JDK 21, Android SDK `android-34`/build-tools `34.0.0`) — builds a real
      `app-debug.apk`, 232 unit tests, 0 failures.
- [x] **Decide dependency injection approach — resolved 2026-09-13: Hilt** (`DECISIONS.md`
      ADR-028, `ARCHITECTURE.md` §10/§15 updated). `:app`'s composition root wired accordingly this
      same session — see `ai-sessions/0013_FEATURE_RESULT_2026_09_13.md`.
- [x] Decide the passive-scanning policy for the Fast Pair Battery
      Notification — resolved as a bounded exception (filtered,
      foreground-triggered, time-boxed); see `DECISIONS.md` ADR-006,
      `AGENTS.md` §7, `ARCHITECTURE.md` §9.1
- [x] **Implement `ProtocolCodec` (`FrameEncoder`/`FrameDecoder`) with unit tests for ANC, EQ, and
      Find My Buds Left/Right — done and verified 2026-09-18** (`ai-sessions/0033_FEATURE_RESULT_2026_09_18.md`
      Phases 3/8). `:data`'s `AncFrameEncoder`/`AncFrameDecoder` (2026-09-13, unchanged),
      `EqFrameEncoder`/`EqFrameDecoder` (DLCI 0x02, byte layout re-derived from `CAP-015` frames
      2111/2165/2227, encoder output matches the real capture byte-for-byte) and
      `RingFrameEncoder`/`RingFrameDecoder` (DLCI 0x04, `CAP-025` fixtures) all tested against real
      `tshark`-extracted fixture bytes, plus a `CodecRouter` doing per-DLCI stream buffering/frame-
      boundary detection (HDLC-flag-delimited for 0x02, length-prefixed for 0x04) that didn't exist
      in code before this session. (Later: `HfpAtParser` removed with HFP, ADR-040; Battery Option B implemented, ADR-033;
      the other DLCI 0x02 settings: reads (ADR-036) and writes of 17/19/22/4/7 (ADR-045) built `ai-sessions/0052`, `SettingsCodec`.)
- [x] **Implement `BudsTransport` (RFCOMM primary, secondary GATT for case/charging characteristics)
      and `ConnectionStateMachine` — `RfcommBudsTransport`'s per-DLCI multiplexing done 2026-09-18**
      (`ai-sessions/0033_FEATURE_RESULT_2026_09_18.md` Phase 4), resolving the `2026-09-13` `//
      TODO(verify)` this item used to describe: one `BluetoothSocket` per DLCI (`BudsSdpUuids.kt`'s
      two confirmed SDP UUIDs), a reader coroutine per socket. **Still not hardware-verified** — no
      physical Buds in this environment, so `connect()` itself remains unexercised. Secondary GATT
      client still not built (no v1 feature needs it yet).
- [x] **Implement `BudsRepository` / `BudsRepositoryImpl` wiring `:data` to
      `:domain` (`ARCHITECTURE.md` §2.1, `DECISIONS.md` ADR-001) — done 2026-09-18**
      (`ai-sessions/0033_FEATURE_RESULT_2026_09_18.md` Phase 5). Implements `ARCHITECTURE.md` §3.1's
      per-feature state-reconciliation table for all four features; 11 unit tests against
      `FakeBudsTransport`.
- [x] **First working end-to-end connection + battery status shown in the UI — UI-complete, not
      hardware-verified, 2026-09-18** (`ai-sessions/0033_FEATURE_RESULT_2026_09_18.md` Phases 5-6).
      (Historical — HFP was later removed, ADR-040; battery now comes from DLCI 0x04/0x02.) HFP Option C was wired end-to-end from `HfpBatteryReader` through
      `BudsRepositoryImpl` to `ConnectionScreen`'s battery card — but `MainActivity`'s `onConnect`/
      `onDisconnect` actions are still placeholders (no real device to connect to in this
      environment) and `HfpBatteryReader`'s actual broadcast delivery is itself unverified (see its
      own `// TODO(verify)`), so "end-to-end" here means "every layer is wired and compiles," not
      "confirmed working against hardware."

## Phase 5 — Testing & documentation

- [ ] Execute `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` on at least 2 devices
      (differing Android version and/or OEM, including GrapheneOS as the
      primary reference target per `ARCHITECTURE.md` §1)
- [x] **Update `README.md` with build instructions once the app builds — done 2026-09-18**
      (`ai-sessions/0033_FEATURE_RESULT_2026_09_18.md` Phase 9).
- [x] **Decide minimum supported Android API level — resolved 2026-09-13: API 34 (Android 14),
      matching compile/target SDK** (`DECISIONS.md` ADR-029, `ARCHITECTURE.md` §1/§15). Applied to
      `android/`'s `:app`/`:hardware`/`:ui` modules (`minSdk = 34`).
- [x] **Multi-device (multiple paired Buds) support for v1 — already decided, this item was stale,
      fixed 2026-09-18** (`ai-sessions/0033_FEATURE_RESULT_2026_09_18.md` Phase 0). `PROJECT.md`'s
      own non-goals section already states this as settled scope ("No simultaneous multi-device
      support in v1 — the app targets exactly one paired Pixel Buds Pro 2 at a time"), matching
      `ARCHITECTURE.md` §15's "Already decided, not open" list — this checklist item was the one
      document that had drifted out of sync, not an actually-open question. Fixed here, a plain
      documentation correction per `PROJECT_RULES.md` §3 (no new `DECISIONS.md` ADR needed since
      nothing new was decided).
- [ ] Prepare the first public release (tag, `CHANGELOG.md` entry, GitHub
      Release per the manual-update-distribution decision in `AGENTS.md` §1)
      — **prepared 2026-10-02 (`ai-sessions/0065`)**: signing config, version 1.0.0, `RELEASING.md`, `scripts/release.sh`, README, `[1.0.0]` CHANGELOG
      block. **Open:** the maintainer's own steps of `RELEASING.md` (key, `CAP-067` on the signed APK, tag, release) — see "Open after `ai-sessions/0065`".
- [x] **Added 2026-09-18** (`ai-sessions/0033`), **done 2026-09-18** (`ai-sessions/0037`): wire
      `MainActivity`'s `onConnect`/`onDisconnect` actions to a real `RfcommBudsTransport.connect()`/
      `disconnect()` call. Required extending `BudsTransport`/`BudsRepository` (domain) with
      `connect()`/`disconnect()`, changing `BudsRepositoryImpl`'s constructor to hold the real
      `ConnectionStateMachine` object (not just its read-only `Flow`) plus a lazily-resolved
      `bondedDeviceProvider`, and swapping Hilt's `TransportModule` from `FakeBudsTransport` to the
      real `RfcommBudsTransport`. Also fixed a latent bug found during this refactor:
      `RfcommBudsTransport.disconnect()` used to cancel a single object-lifetime `CoroutineScope`,
      which would have permanently broken any later reconnect attempt — now a fresh scope per
      `connect()` call. **Still not hardware-verified**: this is this project's first real
      `BluetoothSocket`-level connect attempt against actual Pixel Buds Pro 2 hardware; everything
      downstream of "sockets opened" remains as untested as before.
- [x] **Added 2026-09-18** (`ai-sessions/0033`), **done 2026-09-18** (`ai-sessions/0035`): launch
      `BudsCompanionPairing`'s returned `IntentSender` via an `ActivityResultLauncher`
      (`pairingLauncher`) in `MainActivity`'s `onPending` callback — closed while investigating a
      real crash the maintainer hit tapping "Pair a device" (a separate, now-fixed manifest bug,
      `android.software.companion_device_setup`'s missing `uses-feature` declaration). Still not
      hardware-verified beyond "the picker now launches instead of doing nothing."
- [x] **Added 2026-09-18** (`ai-sessions/0036`), **done same session**: `BudsCompanionPairing`'s
      device filter and the classic-bonding step. Found via a real hardware report: the CDM picker
      offered arbitrary nearby Bluetooth devices one at a time (no filter was ever set), and
      selecting the actual Pixel Buds Pro 2 and granting CDM permission never resulted in a paired
      device with no error shown — because `CompanionDeviceManager.associate()`'s own success
      callback only grants an association (permission to see the device), it does not perform
      Bluetooth bonding itself; `ARCHITECTURE.md` §9.0a's own design already documented the needed
      `BluetoothDevice.createBond()` step, it had just never actually been wired to a real call.
      Both fixed: a `BluetoothDeviceFilter` name pattern (`Pixel Buds`, confirmed against the
      maintainer's own device) now scopes the picker, and `BudsCompanionPairing.observeBonding()`
      calls `createBond()` and reports `Bonding`/`Bonded`/`Failed` via `ACTION_BOND_STATE_CHANGED`,
      surfaced on the Connection screen. Also fixed in the same pass: the app never re-checked for
      a bonded device on resume, so pairing via Android's own Bluetooth settings (bypassing the app
      entirely) was invisible to it — now re-checked on every `ON_RESUME`. **Still not
      hardware-verified**: this session has no device of its own to confirm the picker now shows
      only Pixel Buds, or that a full pairing attempt actually reaches `Bonded`.
- [x] **Added 2026-09-18** (`ai-sessions/0033`), **done 2026-09-18** (`ai-sessions/0038`): start/stop
      `BudsForegroundService` from `ConnectionStateMachine` transitions (ARCHITECTURE.md §6.0a) —
      `MainActivity` now observes `connectionState`/`ancMode` and calls `startForegroundService()`
      for any non-`Disconnected`/non-`Failed` state (notification text: `Connecting…` /
      `Discovering services…` / `Connected — ANC: <mode>`), `stopService()` back at `Disconnected`/
      `Failed`. **Still not hardware-verified**: no physical Buds in this environment to confirm the
      notification actually behaves correctly through a real connect/disconnect cycle.
- [x] **Added 2026-09-18** (`ai-sessions/0033`), **done 2026-09-18** (`ai-sessions/0038`): an "Export
      debug log" UI action reading `BleLogger.exportLog()` — a button on the Debug screen now hands
      the ring-buffer snapshot to the system share sheet (`Intent.ACTION_SEND`, local-only per
      AGENTS.md §9 — the destination is the user's own choice, never a network call this app makes
      itself).
- [x] **Decided 2026-09-20 as `DECISIONS.md` ADR-036 (read-only `ReadSetting` for fields 2, 4, 7, 11, 15, 17, 19, 22, 27, 28 — no writes); the read-only
      Settings UI is open work (Known technical debt below).** PROPOSAL, added 2026-09-18 (`ai-sessions/0033`, `ARCHITECTURE.md` §5a): a consolidated
      `DECISIONS.md` ADR explicitly unblocking DLCI 0x02's generic settings-write `FrameEncoder`/
      `FrameDecoder` for the fields already at full/category-level FACT identity (touch controls,
      multipoint, volume EQ, volume balance, mono audio, in-ear detection, case sounds — fields 2,
      4, 7, 11, 15, 17, 19, 27, 28), modeled on `ADR-020`'s own EQ precedent — decided as ADR-036 (above).

## Known technical debt

_(Fill in as quick fixes are made — see `PROJECT_RULES.md` rule 15. Every
entry here should be short-lived: either resolved properly or promoted to a
tracked task above.)_

**Open after `ai-sessions/0067` (added 2026-10-03, `CAP-067` analysed; the maintainer chose to release 1.0.0 from `8d8af4b`):**
- ~~**Publish 1.0.0**~~ — **done 2026-10-03** (v1.0.0 on tag `8d8af4b`, published 08:30:07 UTC; CHANGELOG dated, README updated). Was: (`RELEASING.md` §5 copy aside, §7, §8 — the maintainer's own steps): keep `dist/1.0.0` (do not run `scripts/release.sh 1.0.0` again); tag
  **`8d8af4b`** (`git tag -s v1.0.0 8d8af4b …`), push the tag, draft release with the three files, check, publish; afterwards date the `[1.0.0]` CHANGELOG block,
  add an empty `[Unreleased]`, and update the README's "No release has been published yet" note. Known issue for the notes (approved in chat): the connect-failure
  text blames another app (Play services) also when the Buds are unreachable — open the case and tap Retry.
- **Re-test before the next release (the maintainer, chat 2026-10-03):** in the user without Play — ANC taps on the tab and the tile **on film** (`08 12` → ACK);
  a Bluetooth off/on with the app on the Connection tab and **an export right after it** (F-3, never hardware-verified); the Left out on channel 19 with the head in
  view (L-1); B4 double tap (`AlreadyInProgress`); the channel-19 `17:7` frame. K5/BC-12 (auto-off) only in a user where the setting exists (the Owner).
  Export the debug log before any step that ends the process (A5, a force-stop).
- **Connect-failure wording** (`CAP-067-FINDINGS.md` §9 item 2, §11 item 2): `ConnectionScreen.kt` `ChannelUnavailable` names "another app … Google Play services'
  Fast Pair" also for a slow failure (page timeout, case closed) — candidate for the next FEATURE session.
- **`APP_TESTPLAN.md` A5:** revoking *Nearby devices* in Settings ends the process; the app then shows Android's prompt at once (by design) — the "You denied the
  permission" screen appears only after a denial in the prompt (`CAP-067` §7).
- 🔴 Why the Buds switched ANC Transparent → Active between 08:07:34 and 08:12:44 without an app command (`CAP-067` §2).
- **`scripts/lint_docs.py` exits 1** on `ai-sessions/0067`'s prompt naming the pre-rename `CAP-067` folder (historical once the next prompt exists) — and it
  already exited 1 at `1752667`; re-check after the next session.

**Open after `ai-sessions/0065` (added 2026-10-02; nothing is published yet):**
- ~~**The maintainer's release steps** up to the test~~ — key, `scripts/release.sh 1.0.0` and `CAP-067` done; Definition of done ticked (`ai-sessions/0067`); publishing
  is in "Open after `ai-sessions/0067`". History: (`RELEASING.md` §1–§8): create and back up the key, set the four values in `~/.gradle/gradle.properties`, note the
  certificate fingerprint; `scripts/release.sh 1.0.0`; run **`CAP-067` on that signed APK in a GrapheneOS secondary user without sandboxed Google Play** (it is
  also the evidence for `PROJECT.md`'s Definition of done 1–3 — tick them only then); then tag, draft release, publish; date the `[1.0.0]` CHANGELOG block
  and update the README's "No release has been published yet" note and Status.
- **Repository settings** (`RELEASING.md` §9, the maintainer's commands): turn on private vulnerability reporting (off on 2026-10-02 — `SECURITY.md` and the
  issue template's contact link rely on it); fix the topic "graphenos".
- **Media:** the screenshot `…_212805.jpg` and the recording show the `0064` balance steps (captioned in the README). Re-take them on the release build and run
  `scripts/readme_media.sh` again.
- 🔴 **`gh … --attach`** (GitHub Docs) is not in the installed `gh 2.97.0`; what happens to a `user-attachments` file when its comment is deleted is not
  documented — open, only relevant if the video is ever uploaded that way.
- **Third-party notices in the app:** releases carry `THIRD_PARTY_NOTICES.txt`; showing it on the Info tab (bundled like `android/ui/src/main/res/raw/license.txt`) is a possible later step.

**Open after `ai-sessions/0066` (added 2026-10-02):**
- **Balance precision:** `CAP-067` reached Right 4 on the 17th drag (6 of 17 snapped to Centre) — usable, still imprecise. Was: the `0064` steps were removed (the maintainer's choice); `CAP-066` reached Right 4 in none of 32 drags. `CAP-067` BC-3 counts
  the drags; if it stays impractical, options are a live value label while dragging, slider `steps`, or the steps back.
- ~~**Definition of done "without Google Play Services":**~~ — done: `CAP-067`, 0 Play-services claims; ticked in `PROJECT.md` (`ai-sessions/0067`). Was: test in a GrapheneOS secondary user without sandboxed Play (the install path and the checks are in
  `ai-sessions/0066` RESULT); the HCI log must show no Play-services claim on the Message Stream (first message `03 08 00 02 01 25`).
- ~~**Before the first release**: the app name / trademark question, R8 (not now), the README video~~ — done in `ai-sessions/0065` (ADR-051; R8 not now;
  `scripts/readme_media.sh`, 0.43 MB MP4 + GIF preview).

**Open after `ai-sessions/0064` (added 2026-10-01):**
- ~~**Run `CAP-067` (Group BC)**~~ — done 2026-10-03, analysed in `ai-sessions/0067` (F-1 13/13, F-6 on film; F-3 and F-4 not verifiable). Was: skeleton `captures/CAP-067-2026-10-03_07-57-35_08-18-29-Group_BC/CAP-067-EVENT-NOTES.md`: the Info tab's licence and links on film (F-6,
  ADR-050), a rotation on every tab and on Settings → Info (F-1), the balance steps to Right 4 = `17:7` and back (F-2; the channel-19 `17:7` frame has never been
  captured — the skeleton gives the derived bytes), a Bluetooth off/on with the export's "Bluetooth was switched off on this phone" (F-3) and StrictMode (F-4), BB-12,
  BB-15 watch, K4d Off/System, L3, K5, then A5, (E), B4, Z1. Nothing of the `0064` build is hardware-verified.
- 🟡 **F-4 may not silence StrictMode:** in AOSP `android16-qpr2-release` `BluetoothLeAudio.close()` never closes its `CloseGuard` (`CAP-066-FINDINGS.md` §8), so an LE
  Audio warning can remain although the app now closes every proxy it obtained — read `CAP-067` BC-11s.
- ⚪ **F-3 assumes** the app's own `ACTION_STATE_CHANGED` receiver gets the adapter broadcast within milliseconds of the system log's `BluetoothAutoOff` line (the
  `CAP-066` build logged no adapter state); the `0064` build logs every change — `CAP-067` BC-10 checks it. A Bluetooth-off while the app is **not** visible is still
  "undetermined" / decided on return (readings are visibility-bound, as the link readings).
- **The bundled licence** (`android/ui/src/main/res/raw/license.txt`) must be updated together with `LICENSE` — `SettingsMenuTest` fails until it is (`cp LICENSE
  android/ui/src/main/res/raw/license.txt`).
- **Optional (the maintainer's call — project law):** a dated note in `AGENTS.md` §1 pointing to ADR-050 (the Info links hand a URL to the browser on a tap; the
  app itself makes no network request).

**Open after `ai-sessions/0063` (added 2026-10-01, `CAP-066` analysed):**
- ~~**Next FEATURE session (the maintainer's choice, chat 2026-10-01, "FEATURE"):**~~ **Done in `ai-sessions/0064`** (F-1 … F-4 built and unit-tested, plus F-6, the Info
  links, ADR-050; not hardware-verified — `CAP-067`). (1) **keep the tab across a configuration change** — rotation reset Sound →
  Connection in `CAP-066` (§6 there; 🟡 cause `OpenControlNavHost.kt:243–258`, the pager synced to index 0 before the restored back stack is known; test with
  Robolectric `StateRestorationTester`); (2) **balance precision** — steps or −/+ buttons so a small value like Right 4 (`17:7`) is reachable (32 drags never hit it,
  `CAP-066` §5; wire unchanged, ADR-045; step size to be chosen at that session's checkpoint); (3) **name a Bluetooth-off loss** in the loss log instead of a lasting
  "undetermined (provisional)" (T-1, `CAP-066` §7); (4) **close unbound profile proxies** in `OsConnectionObserver` (🟡 hygiene, `CAP-066` §8).
- **Still not done on hardware** — now in the `CAP-067` skeleton (`ai-sessions/0064`), except `ANC-002` tapped in the app (not in the maintainer's F-5 list): BB-12 (the Left out with both worn on channel 19 — predicted Buds
  `DISC` + 21, the open half of L-1), F-3 / BB-15 (no cut-off happened in `CAP-066`), K4d "Off" and Android's own dark switch with "System", BB-10 (balance back to
  Right 4 — after the FEATURE fix), L3 (an export with Debug mode off), K5, A5, (E), B4, Z1 (`PAIR-001`), `ANC-002` tapped in the app.
- 🔴 **Why `CAP-064` read Settable `e8` for 28 s** with both buds straight from the case — `CAP-065` and `CAP-066` (14 samples, 2.6–175 s) always read `00`.
- 🔴 **Why the phone pages the Buds 0.1 s after an ACL drop at docking** (`CAP-064` 8399, `CAP-066` B2438 after the film) — no Bluetooth-process log then.
- ~~**`scripts/lint_docs.py` exits 1 on one entry:** `ai-sessions/0063`'s prompt names the pre-rename `CAP-066` folder~~ — resolved 2026-10-01: with prompt `0064` in
  place it is in the "historical" bucket; `lint_docs.py` exits 0 (`ai-sessions/0064`).
- 🟡 **The Buds' stray bytes after `AT+NREC=0`** on HFP (`CAP-066` §9; A6112's tail = the app's `SubscribeRuntimeInfo` request bytes) — a firmware buffer reuse;
  out of scope, note only.

**Open after `ai-sessions/0062` (added 2026-10-01):**
- ~~**Run `CAP-066` (Group BB) on the `ai-sessions/0062` build**~~ **Done 2026-10-01, analysed in `ai-sessions/0063`:** F-1/F-2 hardware-verified, F-3 not exercised, F-4/F-5/F-6 (On, System)/F-7 seen on film — `CAP-066-FINDINGS.md`. The original item: the skeleton is adapted (Info tab on film first; `08 11` before every `08 12`; BB-4t, BB-15,
  BB-16, K4d). Nothing of the 0062 build is hardware-verified yet: F-1 (`Get` first, the tile from the fresh `Notify`), F-2 (wording, "Not allowed now"),
  F-3 (the cut-off result and the not-confirmed mark — watch only), F-4/F-5/F-6 (menu, Info, dark mode), F-7 (the Disconnect label's contrast on the dark card).
- ~~**F-8 — read the StrictMode output**~~ **Done (`ai-sessions/0063`):** 5 violations, all framework objects — a `BluetoothSocket`'s `ParcelFileDescriptor` after end-of-stream and a `BluetoothLeAudio` proxy's `CloseGuard` (`CAP-066-FINDINGS.md` §8, `ARCHITECTURE.md` §12); no app leak. The original item: read the StrictMode output of the first debug run (logcat `StrictMode` lines with "A resource was acquired … but never released") and name the leaked
  object; propose a fix if it is the app's own. Release builds set no policy.
- **T-1 — known limit:** a cause logged "(provisional)" stays the last line when no later reading of Android's link arrives (no timer re-logs it); read the last
  "Session loss cause" line as the verdict.
- 🟡 **`PROTOCOL.md` §2.2a 2026-10-01 Update:** whether the unsolicited announcement reaches the official app's `gaa.d` (the Case/Left/Right mapping is 🟢 for
  the app's screen); a capture where the three entries differ (e.g. after a firmware update of one part) would show it on the wire.
- **`TabRow`** (the settings menu, `SettingsMenu.kt`) is used instead of the experimental `PrimaryTabRow` of material3 1.3.0 — re-check on the next BOM bump
  (with the `TopAppBar`/`PullToRefreshBox` opt-ins below).

**Open after `ai-sessions/0061` (added 2026-10-01):**
- ~~**Next FEATURE session — written as `ai-sessions/0062_FEATURE_PROMPT_2026_10_01.md`**~~ **Done 2026-10-01 (`ai-sessions/0062`):** F-1…F-8 built and
  unit-tested, T-1 and T-3 built, T-2 left as is (the maintainer's choice); the original item: (items of the 0060 block below + these; optional T-1 loss-cause log, T-2 notification flash, T-3 manifest points at its checkpoint). **Additions from `CAP-065` (the maintainer's choice, chat 2026-10-01):** (a) a `Set`/`Get` whose Message Stream claim is closed before its
  answer is reported as "answer cut off — tap Refresh" and the mode marked unconfirmed (`CAP-065-FINDINGS.md` §9 item 1; fixtures `CAP-065` 2640/2649/2651 and
  10321/10344/10356; no retry loop, no new permission); (b) a dark-mode contrast check of the Connection card's Disconnect label (Robolectric screenshot test,
  §9 item 4); (c) `StrictMode` `detectLeakedClosableObjects` in debug builds to find the unclosed resource behind the three `CloseGuard` warnings (§0, §9 item 6).
- 🔴 **Does the announced channel switch when the hosting bud is taken out with the other worn?** (`PROTOCOL.md` §2.2a 2026-10-01 🟡) — `CAP-066` BB-12. **Half answered (`ai-sessions/0063`):** the Right out on 21 ⇒ `DISC` + 19, 2/2 (🟢); the Left out on 19 not tested (see the 0063 block).
- 🔴 **Why `CAP-064` read Settable `e8` for 28 s with both buds on the table straight from the case, while `CAP-065` read `00` every time** — `CAP-066` BB-5.
- 🔴 **What closed the app's claims at `CAP-065` 11:10:06.49 and 11:21:35.24** (no Bluetooth-process lines then; 🟡 Play services' collision as logged at
  11:27:10.450) and **why the Buds closed the session at 11:17:46 with both buds worn**.
- ~~🔴 **GrapheneOS `BluetoothAutoOff … delayMillis: 0`**~~ **Answered (`ai-sessions/0063`, maintainer-approved):** the auto-off is disabled — GrapheneOS's `DelayedConditionalAction.java` schedules no alarm when the delay is 0; `CAP-066`: 0 "scheduled alarm" lines (`CAP-066-FINDINGS.md` §0).

**Open after `ai-sessions/0060` (added 2026-10-01):**
- ~~**Next FEATURE session (the maintainer's choice, chat 2026-10-01):**~~ **Done in `ai-sessions/0062`.** (1) ANC **`Get` before every `Set`** in one claim (no `Set` if that claim's `Notify` reads
  `00`) and the wording "The Buds don't allow changing noise control right now (usually because no bud is in an ear). Tapping a mode checks again first." —
  fixture `CAP-064` 3433/3440/3443 (`CAP-064-FINDINGS.md` §9 items 2–3); (2) a **settings menu behind a gear icon** in place of the top bar's bug icon, with three
  tabs: **Settings** (dark mode On / Off / System), **Debug** (today's Debug screen: Debug mode switch, Export debug log, unidentified frames), **Info** (the Buds'
  and the Case's firmware, the app's build number). Open points for that session: which entry of the `GetSoftwareInfo` announcement is the Case (`PROTOCOL.md`
  §2.2a); how the build number/commit is put into the APK without the network (a Gradle `BuildConfig` field from `git`); dark-mode persistence in the existing
  DataStore. Optional: one loss-cause log line instead of three (§9 item 5).
- ~~**Analyse `CAP-065`**~~ — done in `ai-sessions/0061` (2026-10-01).
- **Run `CAP-066`** (Group BB, skeleton by `ai-sessions/0060`, extended by `0061`): the real I-1 path, Settable `00` timing (buds straight from the case), Transparency
  and Off in the app, the I-4 (i) words, balance restore, the L-1 hosting-bud test, the tile subtitle (enlarged tile, real force-stop), and the `CAP-065` robustness
  steps not done (K4 rotation, K1–K3, L3, K5, A5, (E), B4, Z1).
- 🔴 **Why Android re-paged the docked Buds 3.4 ms after an ACL drop at 10:17:27 but not at 10:15:55** (`CAP-064-FINDINGS.md` §1a) — the system log has no
  Bluetooth-process lines; a bug report's `btsnoop`-side logs or `dumpsys bluetooth_manager` right after a drop would show the reason.
- ~~**`scripts/lint_docs.py` exits 1 on one entry**~~ — resolved 2026-10-01: with prompt `0062` in place the `0060`/`0061` prompts' pre-rename folder names
  are in the "historical" bucket; `lint_docs.py` exits 0.
- **Auto-connect on lid-open** — not possible from any event the app sees (`CAP-064-FINDINGS.md` §1); options (wording, one page on resume, CDM presence) recorded,
  the maintainer chose "Nothing now" (chat 2026-10-01).

**Open after `ai-sessions/0059` (added 2026-09-30):**
- **Open proposals from capture FINDINGS (each needs the maintainer, `AGENTS.md` §6):** `CAP-008` §5/§4 — promote eSCO/mSBC establishment and
  `CALL-001`'s wire/video correlation to `PROTOCOL.md`; `CAP-026` item 3 — the short/no-flag Case form (🔴, `PROTOCOL.md` §4.3 Option E);
  `CAP-029` item 3 — `CASE-008`; `CAP-037` item 3 — record the Settable↔Current co-occurrence in `PROTOCOL.md`; `CAP-047` items 4 (a swapped-slot
  Test-ID) and 5 (trigger candidates 1–2).
- **Rule-9a fold of the remaining dated addenda** in the capture FINDINGS (`ai-sessions/0058` A58-CAP-05): three were folded in 0059 (`CAP-010`,
  `CAP-015`, `CAP-021`); every FINDINGS file now opens with a "Status as of 2026-09-30" banner (`scripts/stale_capture_status.py`), so the rest are
  read under it. A full fold is a MAINTENANCE pass of its own.
- ~~**Run `CAP-065`**~~ — run 2026-10-01, analysed in `ai-sessions/0061` (L-1: one bud out ⇒ Left 19 / Right 21, 🟢; the hosting bud 🟡).
- ~~**Needs a manifest change (none was allowed in 0059):**~~ **Done in `ai-sessions/0062` (T-3, allowed in chat 2026-10-01):** `android:dataExtractionRules`
  (`res/xml/data_extraction_rules.xml`, everything excluded — `:app` lint 0 issues) and the manifest comment on AndroidX Core's app-private
  `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` (A58-GOV-08).
- **`RfcommBudsTransportTest`'s 10 s timeout** (0057, A58-APP-06) was not reproduced in 0059 (200 runs under full CPU load); the test helper now
  prints every thread's stack on that timeout — if it recurs, the dump names the blocked call.
- **`maestro_pw.Dosimeter`** (`PROTOCOL.md` §2.2a, L-4): its values are known on the wire, their meaning is 🟡; a display is out of scope for now
  (maintainer, chat 2026-09-30, "Not now — note only").

**Restructured 2026-09-24 (`ai-sessions/0045`, 0044 finding T-2):** this section now lists **open** items only. The resolved history
that used to sit here (sessions `0037`–`0043`: back-stack fix, unreachable methods, peer-disconnect detection, connect flicker, on-demand
Message Stream, pairing/permissions, `CAP-059` fixes) lives in `CHANGELOG.md` and each session's RESULT file. Removed as stale: the
2026-09-22 claim that the Buds push the Case level without the phone-side `0e 04` — re-derived and corrected in `ai-sessions/0045`
(`PROTOCOL.md` §4.3 Option E correction, `DECISIONS.md` ADR-038 Update, ADR-039).

- [x] **Done 2026-09-27/28 (`CAP-063`, `ai-sessions/0053`) —** **Hardware re-test of the `ai-sessions/0048` build (Group AY):** the automatic re-open (ADR-044), ANC disabled while not worn, per-bud
  "charging in the case", the last-seen Case, the loss wording and the ring notice — step list with HCI brackets in
  `ai-sessions/0048_FEATURE_RESULT_2026_09_25.md` §9. (The 0047 build item is done, `ai-sessions/0048`.)
- [x] **Done 2026-09-26/27 (`ai-sessions/0052`):** D-1/D-2/D-3 recorded (`PROTOCOL.md` 2026-09-26 Updates, ADR-043 Update, ADR-045); EQ presets
  3 + 2; *Refresh battery* on a fresh claim + "No new battery reading" + one re-subscription per Refresh; settings read at Connect (2, 4, 7, 17, 19,
  22) and written (17, 19, 22, 4, 7) — tabs "Sound" and "Controls". Not hardware-verified: the next capture is `CAP-063` (skeleton with every step:
  `captures/CAP-063-2026-09-27_15-57-33_16-25-03-Group_AY/CAP-063-EVENT-NOTES.md`). The item below is kept as the record of what was asked.
- ~~**Next FEATURE session (maintainer's choice, `ai-sessions/0051` §20 — before the Group AY capture):**~~ (1) record the approved texts of `0051`
  §19: D-1 (`PROTOCOL.md` §4.5.1/§4.5.3/§4.5.7/§6 status corrections — the OFF writes `CAP-019` 1720 and `CAP-020` 1995 on film, balance persistence,
  `FE2C1238…` = Find Hub "Beacon actions", the `qht` bit-order conflict), D-2 (ADR-043 Update: *Refresh battery* re-sends one `SubscribeRuntimeInfo`),
  D-3 (a new ADR, the next free number: `WriteSetting` for fields 17, 19, 22, 4, 7; field 12 stays gated); (2) EQ presets in 2 rows (3 + 2); (3) the Refresh fix of
  `0051` §9 (a Refresh inside the 1.5 s linger gets no battery burst); (4) read-only settings (ADR-036) and balance/mono/conversation detection/touch
  controls/press-and-hold per bud under that new ADR. Unit tests with the capture frames named in `0051` §7–§15.
- [x] **Done in `CAP-063` (`ai-sessions/0053`; the AR part stays below) —** **Next capture (Group AY, `CAP-063` on the Pixel 9a — every step, with the `ai-sessions/0052` settings and Refresh steps, is in the skeleton
  `CAP-063-EVENT-NOTES.md`), additions from `ai-sessions/0051` §19:** two *Refresh battery* taps within 1 s (a second DLCI 0x04 burst?); both buds
  docked, idle 2 min, Refresh → is the re-sent `SubscribeRuntimeInfo` answered?; the new settings writes (balance, mono, conversation detection,
  touch controls) with an HCI bracket each; the Group AR ANC-list re-run (planned `CAP-056`): untick only Adaptive on "Customize left", check
  "Customize right" on film, then only Transparency — settles which `qht` bit is which (`0051` F-6) and whether the list is shared.
- [x] **Done 2026-09-28 in `CAP-056` (`ai-sessions/0055`): bit order 1 NC / 2 Off / 3 Transparency / 4 Adaptive 🟢, no side field 🟢, one list 🟡 (the
  "check Customize right" step was skipped); ADR-046 unblocks field 12.** — was: **Group AR (`CAP-056`, Pixel 7a, official app) additions from `0051` F-6:** untick only Adaptive on "Customize left", check "Customize right" on
  film, then only Transparency — settles the `qht` bit order (`PROTOCOL.md` §4.5.3 2026-09-26 Update, 🔴) and whether the list is shared; see
  `CAP-063-EVENT-NOTES.md` A.8. Only after that can field 12 get a read/write ADR.
- [x] **Done 2026-09-28 (`ai-sessions/0054`) —** I-1 (a disabled ANC tap re-checks with the claim's `Get`, `Set` only on Settable non-zero), I-2 (a loss
  while the app was not visible is worded from the first link reading on return), I-3 (balance snaps to "Centre" within ±3), I-5 (the Digital-assistant
  note), I-4 (the previous connection's per-bud lines marked at Connect) — real `CAP-063` bytes (4774, 4184, 4233/4241, 4497/4508, 2723/2756, 3046/3059,
  export 586–593) and `CAP-046` 1873 (`17:0`). **Not hardware-verified:** the next capture is `CAP-064` (Group AZ, skeleton
  `captures/CAP-064-2026-10-01_10-04-14_10-29-28-Group_AZ/CAP-064-EVENT-NOTES.md`, which also carries AY-3 and the `APP_TESTPLAN.md` steps `CAP-063` skipped).
- **Next captures (maintainer's choice, chat 2026-09-28; Group AR done as `CAP-056`, `ai-sessions/0055`):** Group AR (`CAP-056`, Pixel 7a) **first** for the ANC-mode checkboxes (field 12, wish 11) —
  its skeleton now also holds the W-12b in-ear-detection-off steps (the maintainer's request in chat 2026-09-28, `ai-sessions/0054`); then `CAP-064`
  (Group AZ, Pixel 9a) with **AY-3** (one bud visibly in an ear, the other on the table, `08 11` each time) for "Settable `0x00` = no bud worn" and a "worn"
  indicator (wish 12a).
- [x] **Done 2026-09-28 (`ai-sessions/0055`): label promoted from `CAP-056` film (5/5 + SASS bit 4), behaviour with it off recorded, ADR-047 accepted.** — was: **Prepare the draft in-ear-detection-write ADR (in-ear detection writable, field 2; draft in `ai-sessions/0053` §6):** first the label promotion from film
  (`CAP-056` W2/W5, or `CAP-024` 1850/1912); 🔴 what OHD off does to the Buds' `DISC` on wear changes (ADR-044) and to Settable — `CAP-056` W1/W3/W4.
- [x] **Done 2026-09-29 (`ai-sessions/0057`): Material 3 UI overhaul built** — top bar + Debug action, five tabs, (i) details dialogs with a non-text
  not-current marker, graphical battery, own Kotlin icons (Material Symbols, Apache-2.0), pull to refresh / reconnect, `refreshSettings()` (D-11), app theme
  (F-2), slider honesty (F-1), Compose UI tests (Robolectric); not hardware-verified — `APP_TESTPLAN.md` section O and `CAP-064` section VIII.
- [ ] **Open (`ai-sessions/0057`):** F-3 (grey out Sound/Controls in Safe Mode) was offered and **not** chosen — revisit only if a Safe-Mode run shows the
  per-tap refusal is confusing. `TopAppBar`/`PullToRefreshBox` are `@ExperimentalMaterial3Api` in `material3` 1.3.0 — re-check the two opt-ins on the next BOM bump.
- [x] **Done 2026-09-28 (`ai-sessions/0056`): the list and the switch built (plus U-1 not-read = disabled, U-2 re-open wording), `CAP-064` section VII written;
  not hardware-verified — the next capture is `CAP-064` (Group AZ); the Pixel 7a "open Customize right" check is a separate later capture.** — was:
  **Next FEATURE session (maintainer's choice, chat 2026-09-28, `ai-sessions/0055`):** build the "Modes for press and hold" list (field 12, ADR-046: read at
  Connect, four checkboxes shown once for both buds, never fewer than two, fixtures `CAP-056` 1689/1725/1786/1815/1843 and read 1531) and the "In-ear detection"
  switch (field 2, ADR-047: fixtures `CAP-056` 2173/4048 ch 19, 2849/3627 ch 21, read 1502; the note on what "off" changes); add to the `CAP-064` (Group AZ)
  skeleton: untick Adaptive + long-press cycle on film, in-ear detection off + an ANC Refresh with no bud worn (Settable?), and "open Customize right while a mode
  is unticked on the left" on a Pixel 7a run if one is made (one list?).
- 🔴 **Open from `CAP-056`:** what makes the Buds close DLCI 0x02 on a wear change; the pause route with GSND closed (Google app disabled); DLCI 0x08 `04 05`/`04 16`
  meaning; Settable with in-ear detection off and no bud worn — `PROTOCOL.md` §6.
- ~~**ANC tile after re-wearing (`ai-sessions/0054`, known limit):**~~ **Closed in `ai-sessions/0062` (F-1):** the tile's next mode is computed from the
  `Notify` of its own claim (`BudsRepository.stepAncMode`), not from the mode shown.
- 🔴 **Android did not re-create the ACL after an ADR-016 drop with the lid open** in `CAP-063` (unlike `CAP-062`) — `PROTOCOL.md` §6.
- [x] **Done in `CAP-063`:** docked ring (ACKed), EQ/mono writes while docked (OK), audibility recorded as observations; the one-bud-in-an-ear test was
  **not** run (see the capture item above). **Next capture (Group AY):** I-9 Find with both buds docked (does a docked bud ring?), an EQ write while docked, the one-bud-in-an-ear test of
  "Settable `0x00` = not worn" (ADR-024 Update), **EQ audibility** (say aloud what you hear at each preset and at the ±6 extremes, APP_TESTPLAN
  H2–H4 — never recorded so far, `PROTOCOL.md` §4.2 "not established"; `ai-sessions/0050` UX-01), and the APP_TESTPLAN steps not run in `CAP-062` (A5, B4, C5, F5–F7, H5, J4, K1–K5, L3, 0045 (E)/(F));
  record the build hash and Play services' *Nearby devices* state.
- 🔴 **Runtime-info stream:** what field 3 and 7.3 mean; whether field 2 means "in the case" or only "charging" (`PROTOCOL.md` §4.3 Option F; the
  per-bud correlation is 🟢 since 2026-09-25).
- 🟡 **Who owns DLCI 0x08/0x0a** (`CAP-061-FINDINGS.md` §2): the Google app's Assistant-on-headphones service is the lead; test with the Google app
  disabled.
- **Ring "both" (`0x03`) untested on the wire** (ADR-027 Update 2026-09-24, `FIND-004`): the spec defines it, this project has never sent it (not implemented; sending it needs its own ADR) and no capture shows it.
- **Hearable Controls MAC not enforced?** (`PROTOCOL.md` §4.1, 🟡): the ANC Set is sent without a session-nonce MAC and was ACKed in the
  captures; a firmware that starts enforcing it would NAK with reason `0x03`, which the app now reports — verification test in §4.1.
- 🔴 **Why the Buds close both RFCOMM channels** (`CAP-059`/`CAP-060-FINDINGS.md` §1): the idle / periodic / second-host / <0.6 s re-claim
  experiments (`ai-sessions/0042` RESULT §12 e) have not been run as isolated tests.
- 🔴 **Why Play services stops re-claiming DLCI 0x04** (after 17:18:53 in `CAP-059`, after 17:27:01 in `CAP-061`) — its *Nearby devices*
  permission state was not recorded in `CAP-060`/`CAP-061` either; record it in the next capture.
- [x] **ADR-036 settings UI** — built in `ai-sessions/0052` (reads 2, 4, 7, 17, 19, 22) and `0056` (12); 11, 15, 27, 28 are unblocked for reading but not asked for
  (line corrected `ai-sessions/0056`; it said "nothing implemented").
- **Fold the tightened capture checklist** (`ai-sessions/0042` RESULT §12) into `CAPTURE_BLUETOOTH_HCI_SNOOP.md` — maintainer procedure,
  proposal only.
- **Undecided, each needs its own ADR:** a *background* session via CDM device presence (`ARCHITECTURE.md` §6.0b; the foreground variant is
  ADR-044, built in `ai-sessions/0048`); per-channel "degraded" state; the BLE Fast Pair battery advertisement (ADR-006's bounded exception);
  `SubscribeToSettingsChanges` and other DLCI 0x02 settings beyond ADR-036.
- **Remaining battery time** (Fast Pair Device Information code `0x04`, 0044 SYN-1): defined by the spec, never seen on the wire —
  🔴 whether the Buds send it at all; check the next captures' DLCI 0x04 opens.
- **Spatial audio / LE Audio visibility** (`SPATIAL-001`, `LEAUDIO-001`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §4b): 🔴 candidates, not captured.
- **Notification flash on a failed connect:** `OpenControlApplication` starts `BudsForegroundService` at `Connecting` and stops it on
  `Failed`, so a connect that fails fast still posts and removes the notification (`ARCHITECTURE.md` §6.0a). **Left as is (the maintainer's choice, chat
  2026-10-01, `ai-sessions/0062` T-2):** a start only at `Ready` could come after the user left the app — Android 14 refuses a background foreground-service start.
- **ADR-044's success path is unit-tested only through `SessionReopenerTest`** (a scripted re-open): `BudsRepositoryImpl.connect()` needs a real
  `BluetoothDevice`, so the repository tests count attempts at the bonded-device lookup. The hardware step in `ai-sessions/0048` §9 closes it —
  done: the automatic re-opens were exercised in `CAP-063` (ADR-044 Update 2026-09-30).
- **Not unit-testable with the current test setup** (no instrumented tests): `OsConnectionObserver`, `BudsForegroundService`/`AncTileService` (the
  tile's state mapping is tested as a pure function, `AncTileTest`, `ai-sessions/0059`), and the `BluetoothDevice`-dependent part of
  `BudsRepositoryImpl.connect()`. Compose wording and enabled states are covered by `:ui`'s Robolectric tests since `ai-sessions/0057`
  (`BatteryCardTest`, `PullActionTest`; `EqScreenTest` since 0059). *(corrected 2026-09-30, A58-HK-03)*
- **Capture extraction path matters** (added 2026-08-28, verified 2026-08-30): `CAP-012`, `CAP-013`, `CAP-017`, `CAP-031` lost payload
  bytes to ACL truncation on the `btsnooz.py`-from-bugreport path (`CAP-017` via a phone-side snaplen, despite its `-btsnoop_hci.log`
  name); always prefer the raw `btsnoop_hci.log` (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3 step 3).

## Open questions

Open architectural and protocol questions are tracked **at their source only**
— this file does not keep a second, synchronized checkbox list of them, since
that duplication is exactly what caused this file to fall out of sync with
`ARCHITECTURE.md` once already (see `CHANGELOG.md`). Each question has exactly
one home:

- **Protocol-level open questions** (framing hypothesis, unconfirmed opcodes,
  wire-visibility of on-device-only features, etc.) → `PROTOCOL.md` §6.
- **Architecture-level open questions** (DI framework, minimum Android API
  level, multi-device scope, etc.) → `ARCHITECTURE.md` §15.

Check those sections directly when deciding what's still undecided; resolving
one only requires updating it in that one place, plus a `DECISIONS.md` entry
where the rule requires one (`PROJECT_RULES.md` §3).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/TODO.md - https://tedsluis.github.io/opencontrolpixelbudspro2/TODO
