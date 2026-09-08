# 0001_CROSSCHECK_RESULT_2026_09_07.md — Deep V1-without-GMS iterative cross-check (2nd pass)

**Number:** 0001
**Category:** CROSSCHECK
**Date:** 2026-09-07
**Title:** Deep V1-without-GMS iterative cross-check (2nd pass)
**Status:** complete — Phase 4's proposals approved and implemented via `ai-sessions/0002_MAINTENANCE_PROMPT_2026_09_08.md`/`ai-sessions/0002_MAINTENANCE_RESULT_2026_09_08.md` (per `AI_SESSION_LOG_PROCEDURE.md` §4a)

> **Retrofit note:** this file originated as `DEEP_CROSSCHECK_PROGRESS_2026-09-07.md` at the repo
> root, written before `ai-sessions/`/`AI_SESSION_LOG_PROCEDURE.md` existed. It has been moved here
> as entry `0001` and given the required header block above; its body is otherwise unchanged from
> the original.

---

Deep, iterative re-investigation of the V1-without-GMS scope, cross-checking every relevant capture
against the decompiled APK source. This is the second, more thorough pass at what
`AUDIT_REPORT_2026-09-07.md` Phase 1 attempted in a rushed, single-pass form (two of three planned
research passes failed mid-run on a session-wide rate limit; the surviving pass was a single-pass
manual investigation, not the broader iterative search a dedicated pass would run).

**Operational note (per this task's own instructions):** working sequentially, one continuous
session, no parallel sub-agent/Task-tool passes. This file is the running record — append after
every phase and after any individually time-consuming step. A resumed session reads this file
first and continues from wherever it stops, never restarting a phase already checkpointed here.

**AI-assistance boundary throughout (ADR-017):** search, list candidates, explain already-surfaced
code — never decide relevance, never record a finding directly in `REVERSE_ENGINEERING.md`, never
self-promote to 🟢 FACT, never write/amend a `DECISIONS.md` ADR. Everything below is proposals for
maintainer review unless explicitly marked otherwise.

---

## Phase 0 — Setup and work plan

### Reading completed this session (2026-09-07), in the required order

- [x] `AGENTS.md` (full)
- [x] `PROJECT_RULES.md` (full)
- [x] `PROJECT.md` (full)
- [x] `ARCHITECTURE.md` (full)
- [x] `PROTOCOL.md` (full, including §6's changelog and every open item, §8's changelog)
- [x] `DECISIONS.md` (every ADR, ADR-001 through ADR-025 — ADR-017/018/019/024/025 read with extra
      care per this task's instructions)
- [x] `REVERSE_ENGINEERING.md` (full, including the `qhr`/`qjc`/`qja`/`qjn`/`qjt`/`qhx`/`qjv` family,
      the `nqx`/`npy`/`nqo`/`npw`/`nqm` pw_rpc plumbing trace, the KPI/Clearcut side-thread, the
      full-tree GATT/BLE negative-result sweep, and the UUID/Message-Group registers)
- [x] `APK_REVERSE_ENGINEERING_PROCEDURE.md` (full)
- [x] `reverse-engineering/APK_VERSIONS.md` (full — one analyzed version, `v1.0.955078536-10253511`,
      workspace confirmed present on disk at `reverse-engineering/apk/v1.0.955078536-10253511/`,
      `jadx-output/`+`apktool-output/`+`apktool-output-arm64_v8a/`+`pbtk-output/` all present)
- [x] `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index (full table, `CAP-001`–`CAP-042`) — this
      table's per-capture summary is detailed enough to stand in for a first pass over every
      individual `CAP-NNN-FINDINGS.md` file; individual FINDINGS.md files are opened directly in
      later phases only where a specific byte-level/line-level citation needs verification beyond
      what `PROTOCOL.md`/`DECISIONS.md`/this index already quote.
- [x] `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s Test-ID catalog (via `id_registry.csv`, which lists every
      Test-ID with its one-line description — used in place of re-reading the full narrative
      document, since the registry is the authoritative ID list `scripts/lint_docs.py` checks against)
- [x] `DESKRESEARCH_FINDINGS.md` (full — 6 dated entries: 2026-08-17 DLCI 0x02 cross-capture pass,
      2026-08-28 extraction-path truncation pattern, 2026-09-04 rounds 1+2 battery/firmware
      cross-checks, 2026-09-05 video verification, 2026-09-06 CAP-037–042 cross-correlation)
- [x] `TODO.md` (full, including the 2026-08-23 "Recommended priority order" section)
- [x] `id_registry.csv` (full — every ADR/CAP/Test-ID)
- [x] Confirmed `AUDIT_REPORT_2026-09-07.md`, `ANTIGRAVITY_AUDIT_REPORT_2026-09-07.md`,
      `EXTERNAL_REVIEW_VALIDATION_2026-09-07.md` were retired/deleted after processing
      (`CHANGELOG.md`'s "Removed" section) — their findings are already folded into
      `PROTOCOL.md`/`DECISIONS.md` ADR-025/`ARCHITECTURE.md`, confirmed by direct reading of those
      three files above. Not re-fetchable; not needed since their content survives in the
      documents that absorbed them.

### Not yet individually opened this session (scope note, not an oversight)

- Individual `CAP-NNN-FINDINGS.md` files (37 total) — not opened file-by-file yet. The Capture
  Index (§9) plus `PROTOCOL.md`'s per-finding citations plus `DESKRESEARCH_FINDINGS.md` already
  quote the specific frame numbers/hex/conclusions this task's Phase 3 needs for most features.
  Phase 3 below opens the specific ones needed for exact citation-checking, rather than reading all
  37 cover-to-cover up front — flagged here as a deliberate scoping choice per the practical limits
  of one continuous session, to be revisited if Phase 3 finds gaps this approach misses.
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §§1-8 (setup/procedure/analysis-method sections) — skimmed via
  section headers only; not needed for this task's actual research questions (Phase 1-3 are about
  protocol/code findings, not capture procedure).
- `SCREENSHOTS_PIXEL_BUDS_APP.md`/`SCREENSHOTS_PIXEL_BUDS_WEB_APP.md`, `WORKSTATION_PREPARATIONS.md`,
  `CONTRIBUTING.md`, `SECURITY.md` — not in this task's mandatory reading list; not opened.

### Work-plan checklist — every question this task needs to answer

**Phase 1 (GMS-boundary Q1–Q3 re-investigation):**
- [x] Q1: any AIDL-generated interface / `Binder`/`ServiceConnection` / callback interface with an
      ANC-state/settings-notification-shaped method signature (structural pattern search, not just
      keyword search for "AncState" etc.) — see Phase 1 below.
- [x] Q2: any class that only *receives* an already-decoded domain event (the `gck`/`gcl`/`eht`
      pattern `AUDIT_REPORT_2026-09-07.md` §1.0 found) — trace its registration/subscription call
      site to determine which system component it registers with — see Phase 1 below.
- [x] Q3: does `apktool-output/AndroidManifest.xml` declare any `<service>`/`<intent-filter>`
      binding to a GMS-side service relevant to this boundary — see Phase 1 below.
- [x] Per-question verdict — see Phase 1's "Verdict on Q1–Q3" section.

**Phase 2 (deepen DLCI 0x02 / `libmaestro` tracing):**
- [x] Q4: which `maestro_pw.*` services fire specifically inside the connect-time burst — traced
      further, not fully resolved (see Phase 2 below).
- [x] Trace `frb.java`'s `"primary route change"` callback — traced, plural triggers found, not
      fully resolved to connect-time (see Phase 2 below).
- [x] Fully resolve `fsz.java`'s shared-dispatcher structure — confirmed already closed by an
      earlier (2026-08-30) pass; re-verified, no gap (see Phase 2 below).
- [x] Trace `qhr` field 13's ANC-write call site trigger — re-verified complete, 2 callers, both
      correctly gated (see Phase 2 below).
- [x] Apply ADR-019's static-analysis method to `qhr` field 11 (Multipoint) and field 15 (Volume
      EQ) — both fully closed this session (see Phase 2 below).

**Phase 3 (per-V1-feature confirmation pass)** — done, see Phase 3 below:
- [x] Battery (Options 0/A/B/C/D/E)
- [x] ANC (Set/Get/Notify, Settable-toggles dock-state byte, qhr field 13 parallel path)
- [x] EQ (field 16/18, band mapping, presets)
- [x] Touch controls (top-level toggle, press-and-hold ×4, AVRCP-riding taps per CAP-027)
- [x] Head gestures (field 29)
- [x] Firmware/serial (release_5.203 vs. Revision 6 vs. cape2_sm/500m-500p)
- [x] Find My Buds Left/Right (vs. Case/"both" GMS/Find-Hub path)
- [x] In-ear detection (field 2)
- [x] Multipoint (field 11, SASS burst)
- [x] Case sounds (fields 27/28)

**Phase 4 (write-up):** done, see Phase 4 below.
- [x] Proposed `PROTOCOL.md` §6 updates
- [x] Proposed `REVERSE_ENGINEERING.md` updates
- [x] Proposed `TODO.md` closures/reprioritizations
- [x] Draft ADR-025 Update note (proposal only, not committed)

---

## Phase 1 — GMS-boundary re-investigation (Q1–Q3)

Status: **major new evidence found — proposals below, awaiting maintainer review. Not a FACT
promotion, not an ADR — this is a candidate write-up per ADR-017's boundary.**

### Method

Went beyond the prior pass's keyword grep for GMS/Nearby-specific literal strings. Concretely:
1. Read `apktool-output/AndroidManifest.xml` in full for `<service>`/`<receiver>`/`<intent-filter>`
   declarations (Q3) — the prior pass's own notes don't show this was done as a dedicated step.
2. For every exported service found, opened its backing class in `jadx-output/` and traced what it
   extends/implements and what it does (Q1's structural-pattern ask, not just a name-keyword search).
3. Grepped the *whole* decompiled tree for `nearby`/`fastpair`/`messages` case-insensitively (not
   just `MessageStream`/`HearableControls`/opcode literals, which the prior pass already tried and
   got zero hits for) — this is what actually surfaced the new material below.
4. Traced every call site of the classes found back to their registration/binding target (Q2).

### Finding 1 — a real, named, cross-process AIDL-shaped boundary to GMS exists, and it's now traced end-to-end (positive evidence, not another negative result)

**This changes the shape of the prior "not found in this APK" conclusion for one specific
sub-question:** `AUDIT_REPORT_2026-09-07.md` §1.0's search was for the DLCI 0x04/0x08 *transport*
code (frame construction/parsing) — genuinely absent, confirmed again this pass (see Finding 3
below). But a **different, higher-level boundary** — the companion app talking to GMS's Fast Pair
module to fetch already-decoded battery/device-detail objects — **does exist, is genuinely
AIDL-shaped, and is now traced concretely**, which the prior pass's negative result did not surface
because it was searching for the wrong layer (raw frame bytes) rather than a decoded-object RPC.

**The boundary, traced end-to-end:**

- `defpackage/ijk.java` (readable-name Parcelable/library classes, this class itself obfuscated) —
  `b()` (`ijk.java:42-52`) builds `Intent("com.google.android.gms.nearby.discovery.fastpair.ACTION_BIND_DEVICE_DETAIL")`,
  explicitly targets `intent.setClassName("com.google.android.gms", "com.google.android.gms.chimera.GmsBoundBrokerService")`
  (Google's Chimera dynamic-module broker — a real, documented GMS internal mechanism, not
  invented/guessed), and calls `context.bindService(intent, this.c, 1)`.
- `defpackage/iji.java`'s `onServiceConnected` (`iji.java:20-33`) calls
  `iBinder.queryLocalInterface("com.google.android.libraries.bluetooth.fastpair.IFastPairDeviceDetailService")`
  — a literal, unobfuscated AIDL interface descriptor string, falling back to
  `new ijm(iBinder)` (`defpackage/ijm.java:9`, `super(iBinder, "com.google.android.libraries.bluetooth.fastpair.IFastPairDeviceDetailService")`)
  if not a local interface — i.e. `ijm` is a hand-rolled Binder proxy for a real, named
  cross-process AIDL interface.
- `ijk.a(String)` (`ijk.java:29-40`) does a raw `Parcel`-marshalled `ijmVar.c(2, b)` transact call
  (transaction code 2) passing a device address string, and unmarshals the reply as a
  `com.google.android.libraries.bluetooth.fastpair.TrueWirelessHeadset` Parcelable
  (`jadx-output/sources/com/google/android/libraries/bluetooth/fastpair/TrueWirelessHeadset.java`).
- **`TrueWirelessHeadset`'s fields, recovered from `AutoValue_TrueWirelessHeadset.java`'s
  `toString()` (unobfuscated field names, even though the AutoValue class's own getters are
  obfuscated `a()`–`i()`):** `leftBud`/`rightBud`/`headsetCase` (each a nested `HeadsetPiece`),
  `lastUpdateElapsedRealtimeMillis`, `modelId`, `firstObservationTimestampMillis`,
  `mainIconContentUri`, `isBatteryAllTheTimeDevice` (boolean).
- **`HeadsetPiece`'s fields, same method
  (`jadx-output/sources/com/google/android/libraries/bluetooth/fastpair/AutoValue_HeadsetPiece.java`
  `toString()`):** `lowLevelThreshold` (int), `batteryLevel` (int), `imageUrl` (String), `charging`
  (boolean), `imageContentUri` (Uri).
- `ijk.b()` also registers a `ContentObserver` on
  `Uri: content://com.google.android.gms.nearby.fastpair/battery_status_update` — i.e. GMS notifies
  the companion app of a battery change via `ContentResolver.notifyChange()`, and the companion app
  re-queries `IFastPairDeviceDetailService` for the fresh `TrueWirelessHeadset` value.
- `defpackage/gsy.java:17` (`this.i = new ijk(context, this)`) is `ijk`'s sole construction site —
  a `gsy`-typed wrapper is this boundary's actual owner in the rest of the app.
- **A second GMS-side AIDL interface, same broker, different action — Find My Device (FMD):**
  `defpackage/ijp.java` (`b()`, `ijp.java:37-45`) builds
  `Intent("com.google.android.gms.nearby.discovery.fastpair.ACTION_BIND_FMD_PROXY")`, same
  `GmsBoundBrokerService` target, and `iji.java`'s `onServiceConnected` case 2 queries
  `"com.google.android.libraries.bluetooth.fastpair.fmd.IFastPairFmdProxyService"` — another
  literal, unobfuscated AIDL descriptor. `ijp.a(FmdRequest)` (`ijp.java:24-35`) sends an
  `FmdRequest` (fields: `address` (String), `acceptedTosVersion`... — via `AutoValue_FmdRequest`)
  and gets an `FmdResponse` back, over the same raw-Parcel transact-code-2 pattern.
- **`com.google.android.apps.wearables.maestro.companion.fmd.FmdWorker`** (a genuinely
  unobfuscated class name, `androidx.work` `ListenableWorker`) is the concrete consumer: its static
  `c(Context, String, int)`/`n(Context, String)` methods (`FmdWorker.java:51-66`) build `FmdRequest`s
  with operation code `3` ("Enqueued accept worker" log) / `4` ("Enqueued skip worker" log) — i.e.
  **this specific FMD-proxy path is the Find My Device network's Terms-of-Service accept/skip
  plumbing**, not the "ring" trigger itself. Consistent with, and now explaining at the code level,
  `PROTOCOL.md` §4.4's existing wire-evidence-only finding that Case/"both simultaneously" Find My
  Buds shows a "Connecting…" state and on-screen copy about "another device linked with your Google
  Account" with **zero** local `Group 0x04 Code 0x01` traffic — the actual "play sound" action for
  Case/"both" is not visible in this specific traced path either (only ToS accept/skip is), so the
  ring-trigger mechanism itself for Case/"both" is still not code-located — see "What this does NOT
  establish" below.
- **A third, ContentProvider-based channel to the same GMS authority** (not a bound service):
  `defpackage/hlf.java:705` and `com/google/android/apps/wearables/maestro/companion/slices/MaestroSliceProvider.java:233`
  both build/query
  `content://com.google.android.gms.nearby.fastpair/links?address=<addr>&caller=maestro` — a
  device-to-account "links" query/notify, `caller=maestro` explicitly tagging the request as coming
  from this companion app.
- **Also newly found, a different (non-GMS) real AIDL-shaped boundary** — Q1 asked specifically to
  search for callback-interface *shapes*, not just GMS: `com.google.android.apps.wearables.maestro.companion.settingprovider.service.MaestroDeviceSettingsProviderService`
  (manifest: `apktool-output/AndroidManifest.xml`, `exported=true`,
  `permission=android.permission.BLUETOOTH_PRIVILEGED`, custom
  `intent-filter action="com.google.android.apps.wearables.maestro.companion.services.BIND_SERVICE"`)
  extends `gpn` and implements callback methods annotated `@Override // defpackage.fhk` —
  `a(DeviceInfo)`/`cs(DeviceInfo)`/`ct(DeviceInfo, DeviceSettingState)` — using
  `com.android.settingslib.bluetooth.devicesettings.{DeviceInfo,DeviceSettingState,ActionSwitchPreferenceState}`.
  This is **Android's platform (AOSP `SettingsLib`) "Bluetooth Device Details" custom-settings
  extension framework**, not GMS — the binding counterpart is the system Settings app, not
  `com.google.android.gms.*`. Its `ct(...)` handler (case IDs 2102/2103/2104/2113/2115/2116) routes
  boolean toggle changes into the same `ftj`/`fya`-interface accessor chain (`.f()`, `.b()`, etc.)
  this document's `qhr` register already names for several fields — i.e. **the system Settings app's
  own Bluetooth-device-details page can also trigger a `qhr`/`WriteSetting` write**, a UI entry
  point into the DLCI 0x02 settings-write pipeline not previously documented anywhere in
  `REVERSE_ENGINEERING.md`. Also directly references `TrueWirelessHeadset` (via `defpackage/ges.java`,
  a shared lambda-dispatch class) — i.e. this settings-provider service is a second, independent
  consumer of the GMS battery boundary above.

### What this does NOT establish (scope, stated precisely)

- **Does not find DLCI 0x04/0x08 frame-construction/parsing code anywhere.** This confirms Finding 3
  below (the negative result stands, searched again this pass, still zero hits) — the boundary found
  here is a *higher-level, already-decoded-object* RPC (battery numbers, an FMD accept/skip request),
  not raw Message-Stream/private-envelope bytes crossing a process boundary. `qhr`/ANC/EQ/DLCI-0x02
  settings-write content is untouched by any of this — no `anc`/`noise`/`transparency`/`adaptive`
  string appears anywhere in `com.google.android.libraries.bluetooth.fastpair`'s package (checked
  directly, the only 2 grep hits were substring false-positives inside the word "instance").
- **Does not locate the actual "ring"/"play sound" trigger for Find My Buds Case/"both."** Only the
  FMD proxy's ToS accept/skip operations (codes 3/4) were found constructed anywhere in this APK
  version — no other `FmdRequest.d()` call site exists in the decompiled tree (checked via
  `grep -rn "FmdRequest\.d()"`, 2 call sites total, both accept/skip). The actual ring action for
  Case/"both" may be issued from *inside* GMS's own Find My Device app/UI once handed off (matching
  the wire-observed "Connecting…" Find Hub map view, which is a separate app surface, not this
  companion app's own screen) — this remains outside what static analysis of *this* APK can show,
  consistent with `PROTOCOL.md` §4.4's own framing.
- **Does not change any existing 🟢 FACT.** Every wire-level finding this project already has for
  battery (Options A–E) and Find My Buds (Left/Right, DLCI 0x04) stands unchanged — this is
  independent, code-level corroboration of the *architecture* (where the data comes from on the
  companion-app side), not a re-derivation of the wire values themselves.
- **Not proposed for `PROTOCOL.md`/`DECISIONS.md` promotion by this document** — presented as a
  candidate for maintainer review per `AGENTS.md` §6/ADR-017.

### Verdict on Q1–Q3, stated per-question

- **Q1** (AIDL/Binder/`ServiceConnection` callback interface with an ANC-state/settings-shaped
  method): **changes the picture, but not for ANC.** Two real, named AIDL interfaces were found
  (`IFastPairDeviceDetailService`, `IFastPairFmdProxyService`) plus one real AOSP (non-GMS) callback
  interface (`fhk`, the Bluetooth-Device-Details settings extension) with an
  `(DeviceInfo, DeviceSettingState)`-shaped method — structurally exactly what Q1 asked to search
  for. None of the three carries ANC state specifically (confirmed by direct string search within
  the relevant package). **Prior conclusion for ANC specifically: unchanged.** For
  battery/Find-My-Device-consent specifically: **strengthened from "not found" to "found, and its
  boundary partner is a documented Google mechanism (Chimera), not a hypothetical one."**
- **Q2** (trace a receive-only domain-event sink's registration to see which component it binds to):
  **resolved, more precisely than before, for the battery/FMD sinks.** The registration target is
  now a *named* component (`com.google.android.gms.chimera.GmsBoundBrokerService`, specific
  `ACTION_BIND_DEVICE_DETAIL`/`ACTION_BIND_FMD_PROXY` actions), not merely "presumed GMS" — this is
  materially more precise than `AUDIT_REPORT_2026-09-07.md` §1.0's `gck`/`gcl`/`eht` sink tracing,
  which stopped at "receives an already-decoded value from elsewhere" without finding the actual
  IPC registration call. The DLCI 0x04/0x08 *transport*-level sink (if one exists as a receive-only
  class analogous to `gck`) was not found this pass either — see Finding 3.
- **Q3** (does the manifest declare a binding to a GMS-side service): **the manifest itself declares
  no *inbound* binding to a GMS-side service** (expected — `ijk`/`ijp`'s bindings are outbound,
  Intent-targeted at runtime, which manifests don't need to declare for the caller). What the
  manifest *does* declare, newly catalogued this pass (not in `REVERSE_ENGINEERING.md` before):
  `MaestroCompanionDeviceService` (CDM, already expected per ADR-005), `MaestroDeviceSettingsProviderService`
  (the AOSP settings-extension service above), `MaestroEndpointService`
  (`grpc.ondevicegrpcserver` — an **exported, no-`android:permission`, on-device gRPC server**;
  traced its generic plumbing (`mig`/`oez`/`obh`/`mhx`) but could not determine which gRPC
  service(s) are actually registered on it or who binds to it in the time available this pass —
  flagged as a genuinely open, not-yet-closed thread, see below), and several unrelated
  Google-platform receivers (Phenotype, GrowthKit, Chime/notifications, Firebase messaging — none
  Fast-Pair/Nearby-relevant, not pursued further per this task's own scope boundary).

### Exhaustive confirmation pass — every AIDL interface in the APK enumerated

Re-ran the negative-result check (`grep -rli "MessageStream\|HearableControls\|GSND" jadx-output/sources/`
→ **0 hits, reconfirmed**) and additionally enumerated **every** `queryLocalInterface("...")` call in
the whole decompiled tree (31 unique descriptor strings) to make sure no other Fast-Pair/ANC-adjacent
AIDL interface was missed. Full list: generic Android/AndroidX plumbing
(`INotificationSideChannel`, `IMediaControllerCallback`, `IResultReceiver`,
`IMultiInstanceInvalidationCallback`), the AOSP settings-extension interface found above
(`com.android.settingslib.bluetooth.devicesettings.IDeviceSettingsListener` — confirms `fhk`'s real
name), 16 generic GMS common-services interfaces (`IAuthManagerService`, `IAuditService`,
`IGoogleAuthAangService`, `IAuthService`, `IClearcutLoggerService`, `IAccountAccessor`,
`ICertData`, `IGmsServiceBroker`, `IGoogleCertificatesApi`, `IClientNotificationTelemetryService`,
`IClientTelemetryService`, `IObjectWrapper`, `IDynamiteLoader`/`V2`, `IFeedbackService`,
`IGoogleHelpService`, `INotificationsCappingService`, `IPhenotypeService`, `IPseudonymousIdService`,
`ISignInService`, `IUsageReportingService` — standard boilerplate any GMS-integrated app pulls in via
`play-services-base`, auth, phenotype/feature-flags, clearcut logging, sign-in; none Fast-Pair/Nearby/
Bluetooth-specific), 2 Play Core interfaces (`IAppUpdateService`, `IInAppReviewService` — unrelated),
and the 2 Fast Pair interfaces already documented above. **This is exhaustive for this APK version:
`IFastPairDeviceDetailService`/`IFastPairFmdProxyService` are the *only* Fast-Pair/Nearby-relevant
AIDL interfaces anywhere in the app**, and neither carries ANC/settings state. The prior pass's
"not found in this APK" conclusion for DLCI 0x04/0x08 *transport* code specifically is reconfirmed,
now via a broader structural sweep (every AIDL interface, not just literal opcode/group-name
strings) rather than left resting on the earlier, narrower keyword grep alone.

### Genuinely open thread from this pass, not resolved

- `MaestroEndpointService` (`grpc.ondevicegrpcserver`) is exported with no permission gate and hosts
  a generic on-device gRPC server (`mig`/`oez` — a method-descriptor-keyed dispatch table, structurally
  like `io.grpc.ServerServiceDefinition`). No other reference to this class/package exists anywhere
  else in the decompiled tree (`grep -rln "MaestroEndpointService\|ondevicegrpcserver"` → only the
  class itself and its `ghl` base). What gRPC service(s) it actually serves, and whether anything
  (GMS included) ever binds to it, is **not determined** — `onCreate()` is JADX-undecompilable
  bytecode ("Method dump skipped... 599 instructions"), which per `APK_REVERSE_ENGINEERING_PROCEDURE.md`
  §6 would need the `apktool` smali fallback to read, not attempted this pass (time-boxed). Flagged
  as a candidate next step, not chased further this session — plausibly unrelated to the DLCI
  0x04/0x08 question entirely (could be a completely different feature, e.g. cross-device sync), but
  its exported-with-no-permission shape and its being unnamed/undocumented anywhere in this
  project's existing docs make it worth a maintainer look.

---

## Phase 2 — Deepening DLCI 0x02 (`libmaestro`) tracing

### Q4 / `frb.java` "primary route change" trigger — traced further; more nuanced than the prior "plausibly connect-adjacent" reading, not fully resolved

**Prior state** (`PROTOCOL.md` §6, `AUDIT_REPORT_2026-09-07.md`): `fxm.i()`'s 4× `GetSoftwareInfo`
calls were linked only to a `"Change primary route to %d"` log line (`frb.java` case 13, tag
`fxm.a`) as "a plausible, but not confirmedly, connect-adjacent trigger" — no caller of `fxm.i()`
itself, and no source for the primary-route stream, had been traced.

**This pass traced further:**

- `frb` is confirmed a 21-case (0–20) R8-merged `oru`/`Consumer`-shaped lambda dispatcher (the same
  pattern as `gau`/`fwy`/`hvn`/`epf`, already documented for other classes) — case 13's log line is
  one specific lambda instance, not `frb`'s "own" behavior; `frb.java:83`'s original citation was
  the lambda body, not the trigger.
- `fxm`'s constructor (`fxm.java:23-44`) wires `frb(13)` as a subscriber on `this.f` (`fxm`'s own
  `pfx`, an RxJava-style hot-publisher field) — i.e. the "Change primary route" log fires whenever
  *something else* pushes a new `goq` value onto `fxm.f`, not on a fixed schedule or directly on
  connect. `fxm.b()` (implements `fuh.b()`) pushes `false`/teardown to a *different* field
  (`this.f.f(false)` — note: `.f` here is `pfx`'s own setter method, not the field again) on
  teardown; the actual producer of `fxm.f`'s route values was not itself located this pass (out of
  time-boxed scope — `pfx`'s own emit-side API wasn't traced to its caller).
- `fxm.i()` (the `GetSoftwareInfo`×4 fetch, "Start/End fetch SoftwareInfo" logs) implements the
  same `fuh` interface's `i()` method (`interface fuh { void b(); void i(); }` — a two-method
  start/stop-shaped lifecycle interface). **6 classes implement `fuh`**: `fxm`, `fvp` (Dosimeter —
  case 8's "cancel SubscribeToLiveDb rpc" log), `fwc`, `fwx` (RuntimeInfo/low-latency-mode — cases
  10–12's logs), `fxg`, `fyq`, `geg`.
- **Two call sites invoke `.i()` on an `fuh`-typed `Optional`, in two structurally different
  contexts — not one single "on connect" trigger:**
  1. `glk.j()` (`glk.java:530-534`): `if (optional.isPresent() && this.g.F() && this.v.q()) { fuh.i() }`
     — inside a class (`glk`) whose surrounding code (`glk.java:220-260`, `500-530`) is unambiguously
     **firmware OTA bundle-transfer orchestration**: log strings `"Device is ready to transfer"`,
     `"Route %d transfer completed"`, `"Update bundles not staged"`, `"Transfer stopped after
     transfer failed"`, and `pw.software_update.BundledUpdate`-adjacent state values (`gke.t`/`gke.o`/
     `gke.H`/`gke.D`). **This reframes `fxm.i()`'s trigger, for this call site specifically, as
     OTA-transfer-readiness-gated, not a generic post-connect settling action** — a materially
     different and more specific reading than "plausibly connect-adjacent."
  2. `gjv.p()` (`gjv.java:743-749`, implementing an interface `giz`'s `p()` method): gated on
     `hwy.am(this.u)` (`this.u` — a device-type/variant int, not traced further) before calling
     `.i()`. `gjv` is a large per-device connection/settings-lifecycle class (other `giz` methods on
     the same class: `m()` — Auto-OTA check, `n(gdm)` — "wait for device type update to check
     firmware version" (fires only once device type is known), `o()` — calls into `gcl` (the
     already-known downstream domain sink from `AUDIT_REPORT_2026-09-07.md`'s original finding),
     `q()`). This second call site looks more plausibly connect/pairing-lifecycle-shaped than
     `glk`'s OTA-specific one, but **was not traced to its own ultimate caller this pass** (i.e.
     still not confirmed as firing specifically inside the `CAP-036`/`CAP-041` settling window).
- **Net effect on the open question**: `fxm.i()`/`GetSoftwareInfo`'s trigger is now known to be
  **plural, not singular** — at least one confirmed OTA-specific gate (`glk`) and at least one
  device-type-gated general-lifecycle candidate (`gjv`), neither fully traced to "fires at RFCOMM
  connect time." This is a genuine advance (the prior single, vague "plausibly connect-adjacent"
  reading undersold how structured and gated this actually is) but **does not itself close** the
  original ask (confirm/refute firing inside the `CAP-036`/`CAP-041` burst window) — that still
  needs either a full trace of `gjv.p()`'s own caller, or a byte-level correlation between a fresh
  capture's connect-time burst content and a `GetSoftwareInfo`-response-shaped payload (the
  response type `qjb` is already decoded, `REVERSE_ENGINEERING.md`'s own entry), which is a capture
  task, not further static analysis.
- **Proposed for `REVERSE_ENGINEERING.md`**: a new/updated `fxm`/`frb`/`fuh`/`glk`/`gjv` write-up
  reflecting the above — not committed, per ADR-017.

### `qhr` field 11 (Multipoint) and field 15 (Volume EQ) — ADR-019's static-analysis method applied, closing `TODO.md`'s outstanding follow-up

**Prior state**: `REVERSE_ENGINEERING.md`'s own bonus register already located the write call sites
(`fyo.java:146-166` method `j()` = field 11; `fyo.java:376-396` method `u()` = field 15) but found
**no distinguishing log message on either side**, for either field — the two fields most explicitly
flagged as unchecked by `TODO.md`'s "Targeted research follow-ups" and `PROTOCOL.md` §6.

**This pass closed both, via a different (but equally rigorous) technique than the log-message
method used for the fields already promoted: tracing forward from a named UI fragment/preference
key to the write call site, instead of backward from the write site looking for a log string.**

- **Field 11 = Multipoint — full trace, self-describing log message found on a different layer than
  `fxb.java`'s response handler (which itself has none for case 11):**
  `com/google/android/apps/wearables/maestro/companion/ui/settings/multipoint/MultipointFragment.java`
  (a genuinely unobfuscated class name) binds its `key_multipoint_main_toggle`
  `MainSwitchPreference`'s `OnCheckedChangeListener` (`cq(CompoundButton, boolean)`, line 71-75) to
  `hiy.a(boolean)` (`hiy.java:31-38`), which logs **`"Set device Multipoint as: %s"`** (`hiy.java:32`)
  before calling `((fyc) g.get()).e(z)` (`hiy.java:37`). `fyc.e(boolean)` (`fyc.java`: `return
  i(new fyb(z, 9));`) dispatches through `fyb`'s case 9 (`fyb.java:66-70`: `((fya) obj).j(this.a)`)
  — i.e. `fya.j(boolean)`, which is `fyo.j(boolean)` (`fyo.java:146-166`), the exact call site
  `REVERSE_ENGINEERING.md` already identified as writing `qhr.b = 11`. **Full chain, UI to wire
  field, with a self-describing log message at the ViewModel layer:**
  `MultipointFragment` (toggle: `key_multipoint_main_toggle`) → `hiy.a(z)` (`"Set device Multipoint
  as: %s"`) → `fyc.e(z)` → `fyb(z,9)` → `fya.j(z)` = `fyo.j(z)` → **`qhr` field 11**.
- **Field 15 = Volume EQ — full trace, with a literal Android preference-key string match, a
  stronger evidence type than any `qhr` field promoted so far:** `defpackage/hlv.java:2125-2134`
  (a large R8-merged preference-click dispatcher, `e(Preference)`) has a case gated on
  `str.equals("volume_eq_switch")` — the literal Settings-XML preference key for the "Volume EQ"
  toggle at the bottom of Device details → Sound → Equalizer (`PROTOCOL.md` §4.5.6's own UI
  description) — logging **`"Set volume eq: %s"`** (`hlv.java:2127`) before calling
  `((fyc) g2.get()).h(z2)` (`hlv.java:2133`). `fyc.h(boolean)` (`fyc.java`: `return i(new fyb(z,
  7));`) dispatches through `fyb`'s case 7 (`fyb.java:56-60`: `((fya) obj).u(this.a)`) — i.e.
  `fya.u(boolean)` = `fyo.u(boolean)` (`fyo.java:376-396`), the exact call site already identified
  as writing `qhr.b = 15`. **Full chain**: `hlv`'s preference-click handler (key:
  `"volume_eq_switch"`) → `"Set volume eq: %s"` log → `fyc.h(z)` → `fyb(z,7)` → `fya.u(z)` =
  `fyo.u(z)` → **`qhr` field 15**.
- **Bonus, adjacent finding (not part of this task's ask, found in the same dispatcher):** the case
  immediately before Volume EQ's, `str.equals("loudness_comp_switch")` ("Loudness compensation"),
  logs `"Set loudness compensation: %s"` and calls `((fyc) g.get()).i(new fyb(z, 5))` → `fyb`'s case
  5 → `fya.i(boolean)`. **`fyo.i(boolean)` (`fyo.java:411-412`) is an empty no-op method body** — on
  this app version's Pixel-Buds-Pro-2-specific schema (`fyo` implements `fya`), toggling "Loudness
  compensation" produces **no `qhr` write at all**; this method is presumably only implemented by
  one of the sibling `fyw`/`fyx` classes for a different product. Not one of `PROTOCOL.md`'s
  documented Buds Pro 2 features — recorded here as a genuine negative result (a real, named Sound
  settings toggle that is wired in the shared UI code but produces zero wire traffic for this
  device), not chased further.

**Evidentiary strength, stated precisely**: both traces combine (a) an exact, independently-verified
call-graph path with no ambiguous branch, and (b) a self-describing string — a log message for field
11, and *both* a log message *and* the literal Android preference-key string for field 15. This
meets or exceeds the evidence bar `DECISIONS.md` ADR-019 used for its already-promoted fields (which
relied on the read-side `fxb.java` log message alone in most cases) — **proposed as new full-identity
promotion candidates for `PROTOCOL.md` §4.5.2 (Multipoint, field 11) and §4.5.6 (Volume EQ, field
15)**, for maintainer review; not committed here per `AGENTS.md` §6/ADR-017. Both readings match the
pre-existing wire-derived HYPOTHESIS labels exactly (no naming conflict of the kind that kept fields
12/22/27 at field-number-only status) — this is a case where the maintainer's per-item sign-off
should be straightforward to grant in full, but that determination is explicitly left to them.

### `qhr` field 13 (ANC-write) trigger — re-checked, no further depth beyond what's already documented; the CAP-038 mystery is a hardware/wire question, not a code-tracing gap

`REVERSE_ENGINEERING.md`'s `qhr` entry already fully traces field 13's write path to exactly two
callers: (1) `QuickActionsFragment`'s in-app ANC toggle-group tap, and (2) a physical press-and-hold
gesture via `gvi.java`/`gvj.java`. This pass re-opened `gvj.java` specifically to check whether case
4 (the `HOLD` branch that triggers the ANC toggle) could fire spuriously from `gvj`'s *other* role
(it also implements `AudioManager.OnAudioFocusChangeListener`, for unrelated music-transport
cases 1/2/3/5/6) — **it cannot**: case 4 is gated on the same `crj`/`LiveData`-style stream of
already-classified `qhp` gesture-action-type events (`SINGLE_TAP`/`DOUBLE_TAP`/.../`HOLD`) that
every other case in the same switch uses, not on an audio-focus callback. This confirms the
existing two-path finding is already as complete as static analysis of the companion app can make
it — **`CAP-038-FINDINGS.md` §5's two ANC-Notify-without-Get/Set frames remain a hardware/wire
question** (did the earbud's own firmware misclassify a physical touch as a genuine `HOLD` gesture
with no camera-visible cause, independent of anything the companion app's code does) — not a gap
this pass's further code-tracing can close. No proposed change to `PROTOCOL.md`/`REVERSE_ENGINEERING.md`
from this specific check; recorded as a checked-and-confirmed-complete negative result.

### `fsz.java`'s dispatcher structure — already resolved by an earlier pass; re-verified, no remaining gap

The task prompt asked to "fully resolve `fsz.java`'s shared-dispatcher structure — trace
`WriteSetting`'s actual caller through the R8-merged-lambda dispatcher rather than leaving it as an
acknowledged gap." Re-reading `REVERSE_ENGINEERING.md`'s own `nqx`/`npy`/`nqo`/`npw`/`nqm` entry (its
2026-08-30 "follow-up pass" update) confirms **this was already fully closed on 2026-08-30, before
this session** — not an open gap this project's own docs still describe as acknowledged-but-unclosed.
The full chain is traced end-to-end and cited with file:line precision: `fsz.java` case 2
(`fsz.java:65-76`, `npy.f(int, "maestro_pw.Maestro", "WriteSetting")`) → `nqo` (`MethodClient`) →
`nqo.e(qjc)` → `npy.a(...)` (builds/serializes the `nqx`/`RpcPacket`, `payload`=serialized `qjc`) →
`npw.a(bytes)` (`Channel`) → `npv.a(bytes)` (one of `frg.java`'s 6 anonymous `ChannelOutput`
implementations, keyed by `(goq` route target `× goq` core`)` pairs) → `fut.f(bytes, goq)`
(HDLC-encode: flag+LEB128-address+control+payload+CRC-32+flag, byte-for-byte matching `PROTOCOL.md`
§2.2a's wire-derived framing from the *opposite*, encode-side, evidence direction) →
`ffd.j() = BluetoothSocket.getOutputStream()`. Re-verified this pass by re-reading `fsz.java` (case
2 at lines 65-76) and `npy.java`/`nqo.java`/`npw.java`/`fut.java` directly — the citation is accurate
and the chain has no missing link. **No further tracing needed or performed here** — flagging this
sub-item as already-closed rather than re-doing work already done, per this task's own "never
restart a phase already completed" instruction applied at the sub-item level.

---

## Phase 3 — Per-V1-feature confirmation pass

Per `PROJECT.md`'s v1 functional-scope checklist. For each feature: which evidence is
DLCI-0x02/companion-app-code-backed (deepened by Phase 2 and this session's new findings) vs.
DLCI-0x04/0x08/GMS-boundary (closed per Phase 1 — wire-evidence-only going forward, now with a
positive account of *where* the GMS-side implementation lives, not just an absence result).

### Battery (case/L/R)

- **Wire-confirmed, DLCI-0x04/0x08 (official Message Stream / private envelope), unaffected by this
  session**: Option A (BLE Battery Notification advertisement, 🟡 mechanism confirmed/payload-layout
  still inconclusive per `CAP-011`), Option B (DLCI 0x04 `Group 0x03 Code 0x03`, 🟡 strong candidate),
  Option C (HFP `AT+BIEV`/`AT+CIND`, 🟢 FACT, `ADR-015`/`ADR-023`), Option E (DLCI 0x08 `Group 0x0e
  Code 0x01`, 🟢 FACT, `ADR-014`).
- **Newly traced this session, companion-app-code-side, a *different* mechanism from all five above**:
  `IFastPairDeviceDetailService`'s `TrueWirelessHeadset{leftBud, rightBud, headsetCase}` (each a
  `HeadsetPiece{batteryLevel, charging, lowLevelThreshold, ...}`) — a genuine sixth data path, GMS-IPC
  based rather than RFCOMM/BLE-based, that the companion app (and the system Settings app, via
  `MaestroDeviceSettingsProviderService`) actually reads for on-screen battery display. This is a
  positive architectural finding, not a wire-protocol one — it does not add a 7th implementation
  option to `ARCHITECTURE.md` §4's priority list (this project's own app cannot use a
  `com.google.android.gms`-internal Chimera-broker AIDL interface without depending on GMS, which
  would violate `AGENTS.md` §1's Zero-GMS rule) — but it explains, for the first time at the code
  level, *how* the official app itself sources its battery display, which the project's docs
  previously only inferred from wire behavior. **Proposed**: a `PROTOCOL.md` §4.3 note documenting
  this as informational context (how the *reference* app works), explicitly marked out-of-scope for
  this project's own implementation, mirroring how `REVERSE_ENGINEERING.md`'s Clearcut/KPI findings
  are already framed ("describes the official app, not a model to follow").
- **`isBatteryAllTheTimeDevice` (a `TrueWirelessHeadset` field newly found this session)** is a
  plausible, not yet interpreted, lead for `PROTOCOL.md` §4.3's own "these do not all share one
  update model" framing (Options A/B are event-driven, C is periodic-then-idle) — flagged as a
  genuinely open question this session's static analysis cannot resolve (no getter/log/UI reference
  to this field's actual value was traced), not claimed as an explanation.

### ANC (Set/Get/Notify, Settable-toggles dock byte, `qhr` field-13 parallel path)

- **Wire-confirmed, DLCI-0x04 (official Message Stream), unaffected**: Set/Get/Notify (`0x11`/`0x12`/
  `0x13`), 🟢 FACT (`ADR-009`/`ADR-021`/`ADR-022`); Settable-toggles dock-state byte, 🟢 FACT
  (`ADR-024`).
- **Companion-app-code-side, DLCI-0x02, deepened this session**: `qhr` field 13 (`ANC_STATE`
  enum) — write path fully traced to exactly 2 callers (in-app `QuickActionsFragment` toggle-group
  tap; physical press-and-hold via `gvi`/`gvj`'s gesture-classification stream, re-confirmed this
  session as genuinely gesture-gated, not spuriously audio-focus-triggered). This is the
  code-evidenced "second candidate" `PROTOCOL.md` §6 already lists for `CAP-038-FINDINGS.md` §5's
  unexplained Notify-without-Get/Set frames — **still not wire-confirmed** (no capture has yet
  correlated a `qhr`-field-13 write on DLCI 0x02 with an immediately-following, otherwise-unexplained
  DLCI-0x04 Notify); this session's static analysis reconfirms the two trigger paths are complete and
  correctly gated, narrowing the open question to "is this ever actually what happens on the wire,"
  a capture task not a code-tracing one.
- **`qhr` fields 9/10** (same `ANC_STATE` enum type, different oneof case numbers) remain
  write-silent/read-inert in this app version (`REVERSE_ENGINEERING.md`'s own 2026-08-30 finding,
  unchanged this session) — not re-investigated further, no new angle found.

### EQ

- **Wire-confirmed, DLCI-0x02, unaffected**: envelope, field-to-band mapping, ±6.0 clamp, preset
  quintets — all 🟢 FACT (`ADR-016`/`ADR-020`).
- **Companion-app-code-side**: field 16/18 "live vs. persisted" reading (`qjw`, `fyp.java`) already
  traced in `REVERSE_ENGINEERING.md`; not re-opened this session (no new angle identified — the
  remaining gap, `fyd.d`/`fyd.e`'s own UI call sites, is `TODO.md`'s own already-tracked next step,
  not attempted this session for time-budget reasons — flagged as carried-over, not newly closed).

### Touch controls

- **Wire-confirmed**: top-level toggle (`qhr` field 4, 🟢 FACT), press-and-hold ×4 (`qhr` field 7/
  `qju`, 🟢 FACT), physical taps riding AVRCP not RFCOMM (`CAP-027`, 🟢 structural finding).
- **Newly found this session, a previously-undocumented second UI entry point**: the AOSP
  `MaestroDeviceSettingsProviderService` (system Settings app's Bluetooth-device-details extension,
  `com.android.settingslib.bluetooth.devicesettings.IDeviceSettingsListener`) routes at least 4
  distinct case IDs (2102/2103/2104/2113/2115/2116) into the same `ftj`/`fya`-interface accessor
  chain (`.f()`, `.b()`, etc.) — i.e. **the system Settings app's own Bluetooth device page can also
  trigger a `qhr`/`WriteSetting` write for touch/head-gesture-adjacent settings, not only this
  project's own already-documented in-app screens.** Which specific `qhr` fields these 6 case IDs
  map to was not individually traced this pass (the accessor-method names alone, `.f()`/`.b()`/etc.,
  are ambiguous without the same kind of forward-trace done for Multipoint/Volume EQ above — flagged
  as a concrete, well-scoped next step, not attempted here for time-budget reasons). **Proposed**:
  a new `REVERSE_ENGINEERING.md` entry for `MaestroDeviceSettingsProviderService`/`ges`/`ght`/`kjj`,
  and a `PROTOCOL.md` note that the settings-write envelope's triggering UI surface is broader than
  previously documented (the companion app's own screens are not the only path).

### Head gestures

- **Wire-confirmed**: `qhr` field 29, 🟡 HYPOTHESIS (wire-level match only, no self-describing code
  name found even after this session's searches — unchanged).
- No new code-side angle found this session for field 29 specifically; not re-investigated beyond
  the existing `REVERSE_ENGINEERING.md` entry (case 2104 in `MaestroDeviceSettingsProviderService`
  above passes `fpm.ENABLED_HEAD_GESTURES`/`fpm.UNKNOWN`, which is at least suggestive that this
  settings-provider case *is* head-gestures-related — flagged as a promising lead for the
  not-yet-done forward-trace mentioned above, not confirmed this session).

### Firmware/serial

- **Wire-confirmed**: `"release_5.203"` = the app's own displayed firmware version (🟢 FACT,
  `ADR-012`); `"Revision 6"`/`"cape2_sm"`/`"500m"`-`"500p"` remain unreconciled, unchanged.
- **Companion-app-code-side**: `TrueWirelessHeadset.modelId` (newly found this session) is a
  plausible candidate to cross-reference against DLCI 0x04's already-FACT Device Information "Model
  ID" (`da 2d b1`, Code `0x01`) — not checked this session (would need either a value dump from a
  live device or a deeper Parcelable-construction trace this pass didn't attempt); flagged as a
  concrete, cheap next step for a maintainer with device access, not chased further here.

### Find My Buds Left/Right (vs. Case/"both" GMS/Find-Hub path)

- **Wire-confirmed, Left/Right**: DLCI 0x04 `Group 0x04 Code 0x01`, 🟢 FACT (`ADR-011`), unaffected.
- **Case/"both" — substantially advanced this session, from wire-evidence-only to a partial
  code-level explanation.** `PROTOCOL.md` §4.4 previously only had wire evidence that Case/"both"
  routes through a different, GMS/Find-Hub-mediated path with zero local Ring traffic. **This session
  traced the actual companion-app-side code**: `com.google.android.apps.wearables.maestro.companion.fmd.FmdWorker`
  (genuinely unobfuscated class name) sends `FmdRequest`s (operation codes 3="accept"/4="skip") over
  `IFastPairFmdProxyService` (bound via `GmsBoundBrokerService`, same Chimera broker as the battery
  boundary) — but **only** for Find My Device Terms-of-Service accept/skip, not for an actual "play
  sound" trigger (no other `FmdRequest.d()` call site exists anywhere in this APK version, checked
  exhaustively via `grep -rn "FmdRequest\.d()"`, 2 hits total, both accept/skip). **This confirms, at
  the code level for the first time, that the companion app's own role in the Case/"both" path is
  limited to consent/onboarding plumbing — the actual ring-trigger is not constructed anywhere in
  this app's decompiled source**, consistent with (and now partially explaining) the wire-observed
  "Connecting…" Find Hub map-view hand-off to a separate app surface. **This is new, positive,
  code-level support for `PROTOCOL.md` §4.4's existing "potentially a hard Zero-GMS limit" flag** —
  strengthens it, does not overturn it. **Proposed**: cite this trace in `PROTOCOL.md` §4.4 and
  `TODO.md`'s Case/"both" scope-decision item as additional context for the maintainer's own product
  decision (`TODO.md`'s "Recommended priority order" §1 already flags this as a maintainer call, not
  a research gap) — not itself a resolution of that product decision.

### In-ear detection

- **Wire-confirmed**: `qhr` field 2, 🟡 HYPOTHESIS, unchanged.
- No new code-side angle found this session — `fyo.java:169-188 (l)`'s write site was already
  documented; not re-opened (the `qjn`-schema sibling field, "OHD state" — plausibly On-Head-Detection
  — reached via the *same* shared interface method `l()`, already noted in `REVERSE_ENGINEERING.md`'s
  `qjn` entry, is the closest thing to a naming lead already on record; not newly found this session).

### Multipoint

- **Full identity now closed this session** — see Phase 2's dedicated write-up above. `qhr` field 11
  = Multipoint, traced end-to-end from `MultipointFragment`'s toggle through a self-describing log
  message (`"Set device Multipoint as: %s"`), proposed for full promotion.
- SASS burst correlation (DLCI 0x04 Group `0x07`) remains wire-evidence-only, unaffected.

### Case sounds

- **Wire-confirmed**: fields 27 (category-level)/28 (full identity), 🟢 FACT (`ADR-019` Update),
  unchanged this session — not re-investigated (no new angle sought, out of this session's
  time-budget priority given Multipoint/Volume-EQ were the explicitly-flagged outstanding items).

---

## Phase 4 — Write-up (proposals, none committed)

Per `AGENTS.md` §6/ADR-017: everything below is a proposal for the maintainer's own review and
decision. Nothing has been written into `PROTOCOL.md`, `REVERSE_ENGINEERING.md`, `TODO.md`, or
`DECISIONS.md` by this session.

### Proposed `PROTOCOL.md` §6 additions

1. A new "Commands & schemas" item: `IFastPairDeviceDetailService`/`IFastPairFmdProxyService` (GMS
   Chimera-brokered AIDL interfaces in `com.google.android.libraries.bluetooth.fastpair`) are the
   companion app's own source for battery display and Find-My-Device ToS plumbing — informational
   context on the *reference app's* architecture, explicitly out of scope for this project's own
   Zero-GMS implementation (`AGENTS.md` §1), not a new implementation option.
2. A refinement to the Find My Buds Case/"both" open item (§4.4/§6 Behavior): the companion app's
   own `FmdWorker`/`ijp` code only constructs ToS accept/skip `FmdRequest`s — no ring/play-sound
   trigger exists anywhere in this APK's decompiled source, code-level support (not proof) for the
   existing wire-only "possible hard Zero-GMS limit" flag.
3. A new open item: `MaestroDeviceSettingsProviderService` (system Settings app's Bluetooth-device
   page extension) is a second, previously-undocumented UI entry point into the `qhr`/`WriteSetting`
   pipeline for at least 6 case IDs (2102/2103/2104/2113/2115/2116) — which specific `qhr` fields
   these map to is not yet traced.
4. A new open item: `MaestroEndpointService` (`grpc.ondevicegrpcserver`) is an exported,
   no-permission on-device gRPC server whose registered service(s) and binding caller(s) were not
   identified this pass (JADX-undecompilable `onCreate()`; would need an `apktool` smali fallback
   read).

### Proposed `REVERSE_ENGINEERING.md` additions

1. New entries for `ijk`/`ijp`/`ijm`/`iji`/`gsy`/`TrueWirelessHeadset`/`HeadsetPiece`/`FmdRequest`/
   `FmdResponse`/`FmdWorker` (the GMS Fast Pair client-library boundary, Finding 1 of Phase 1 above),
   with full file:line citations as already written into this progress file's Phase 1 section.
2. New entries for `MaestroDeviceSettingsProviderService`/`fhk`/`ges` (the AOSP settings-extension
   boundary).
3. New entries for `frb`/`fuh`/`glk`/`gjv` (the `fxm.i()`/`GetSoftwareInfo` trigger structure, Phase
   2's Q4 write-up).
4. Field-register updates for `qhr` fields 11 and 15 (full call-graph traces to `MultipointFragment`/
   `hlv`'s `"volume_eq_switch"` case), and a note on field 5/`fyo.i()`'s no-op status for "loudness
   compensation."
5. A new "Native libraries"/open-questions note for `MaestroEndpointService`'s undetermined gRPC
   service registrations.

### Proposed `TODO.md` updates

- Close the "apply ADR-019's same static-analysis method to `qhr` fields 11 and 15" item (`TODO.md`'s
  "Targeted research follow-ups" list) — both fields traced end-to-end this session with strong
  evidence (see Phase 2/3 above), pending maintainer sign-off for the `PROTOCOL.md` promotion itself.
- Add a new, concretely-scoped item: trace `MaestroDeviceSettingsProviderService`'s 6 case IDs to
  their specific `qhr` field numbers (the same forward-trace technique used for Multipoint/Volume EQ
  this session).
- Add a new item: `apktool` smali-fallback read of `MaestroEndpointService.onCreate()` to determine
  what it serves and who binds to it.

### Proposed `DECISIONS.md` ADR-025 Update note (draft only — not added to `DECISIONS.md`)

> **Proposed Update (2026-09-07, this deep cross-check pass) — not yet reviewed or approved by the
> maintainer, drafted here per this task's own instruction not to add it directly.** A deeper Phase-1
> search (structural AIDL/`ServiceConnection` pattern search plus a full manifest read, going beyond
> the original audit's literal-keyword grep) found that Google Play Services' Fast Pair module *is*
> reachable from this companion app's own decompiled code, via two genuinely named, unobfuscated AIDL
> interfaces (`com.google.android.libraries.bluetooth.fastpair.IFastPairDeviceDetailService`,
> `...fmd.IFastPairFmdProxyService`) bound through Google's Chimera dynamic-module broker
> (`com.google.android.gms.chimera.GmsBoundBrokerService`). **This does not weaken this ADR's
> decision or its "DLCI 0x04/0x08 transport code is absent from this APK" evidentiary basis — it
> strengthens it.** The newly-found boundary carries only already-decoded objects (a `TrueWirelessHeadset`
> battery summary; an `FmdRequest`/`FmdResponse` consent-flow pair) — not raw Message-Stream/private-
> envelope frame bytes — and an exhaustive sweep of every `queryLocalInterface(...)` call in this APK
> version (31 total) found no third, ANC/settings-shaped GMS interface. The original conclusion —
> that DLCI 0x04/0x08's actual frame construction/parsing lives inside GMS itself, not this companion
> app — is now supported by a *positive* architectural finding (a concrete, named, working example of
> exactly this kind of higher-level GMS boundary existing and being reachable) in addition to the
> original *negative* one (no transport code found). Recommendation: no change to this ADR's Decision
> or Consequences sections; optionally add this Update note as a dated strengthening record, per
> `PROJECT_RULES.md` §3's non-destructive-update convention for `DECISIONS.md`.

---

## Summary (for the maintainer)

**What's now more strongly confirmed:**
- ADR-025's core conclusion (GMS reverse-engineering out of scope, DLCI 0x04/0x08 proceeds
  clean-room) — strengthened by a positive finding (a real, named, traced GMS AIDL boundary exists,
  for battery/FMD-consent specifically, carrying only decoded objects) rather than resting solely on
  an absence-of-evidence negative result.
- `fsz.java`'s `WriteSetting` dispatch chain — re-verified complete and accurate, no gap remains.
- `qhr` field 13's ANC-write trigger paths — re-verified complete (2 callers, both genuinely gated),
  narrowing `CAP-038`'s open mystery to a hardware/wire question rather than a code-tracing gap.

**What's newly found, not in any prior pass's docs:**
- A real, positive, code-level account of how the official app sources battery data and handles
  Find My Device consent — via GMS's Chimera-brokered Fast Pair client library
  (`IFastPairDeviceDetailService`/`IFastPairFmdProxyService`), including the exact
  `TrueWirelessHeadset{leftBud,rightBud,headsetCase}`/`HeadsetPiece{batteryLevel,charging,...}` data
  shapes.
- A previously-undocumented second UI entry point into the `qhr`/`WriteSetting` pipeline: the AOSP
  system-Settings-app Bluetooth-device-details extension (`MaestroDeviceSettingsProviderService`).
- A previously-undocumented, unexplained, exported-with-no-permission on-device gRPC server
  (`MaestroEndpointService`) whose purpose is not yet determined.
- `qhr` field 11 (Multipoint) and field 15 (Volume EQ) fully traced end-to-end with strong
  self-describing evidence (log message + literal preference-key string for field 15), closing
  `TODO.md`'s explicitly-flagged outstanding item.
- `fxm.i()`/`GetSoftwareInfo`'s trigger is confirmed plural and more structured than previously
  characterized (at least one OTA-transfer-specific gate, one device-type-gated general-lifecycle
  candidate) — not fully resolved to "fires inside the connect-time burst," but materially advanced
  from the prior vague "plausibly connect-adjacent" reading.
- A code-level explanation (not proof) for why Case/"both" Find My Buds shows zero local Ring
  traffic: the companion app's own FMD code only handles ToS accept/skip, never a ring trigger.

**What changed from the previous pass's conclusions:** nothing was overturned. Every finding above
either closes a previously-open item in the direction the existing HYPOTHESIS already pointed
(Multipoint, Volume EQ), or adds genuinely new, positive architectural context that strengthens
rather than contradicts ADR-025 and the project's existing wire-based findings.

**What's proposed and awaiting maintainer sign-off:** every item in Phase 4 above — new
`REVERSE_ENGINEERING.md` entries, `PROTOCOL.md` §6 additions, `TODO.md` updates, and the draft
ADR-025 Update note. No FACT promotion, no ADR write/amendment, and no `REVERSE_ENGINEERING.md` edit
was made directly by this session, per `AGENTS.md` §6/ADR-017.

**Scope not reached this session** (should a resumed session continue from here): individual
`CAP-NNN-FINDINGS.md` files were not opened file-by-file (see Phase 0's scope note); the
`MaestroDeviceSettingsProviderService` case-ID-to-`qhr`-field forward trace was identified but not
executed; `MaestroEndpointService`'s smali fallback read was not attempted; the EQ field-16/18
`fyd.d`/`fyd.e` UI call-site trace (`TODO.md`'s own pre-existing item) was not attempted this
session.

**Status: session complete for this pass** — Phases 0–4 all have entries above; not a rate-limit
stop, a natural completion point given the depth achieved. A future session extending this work
should start with the "Scope not reached" list immediately above.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07
