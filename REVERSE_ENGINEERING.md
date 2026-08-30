# REVERSE_ENGINEERING.md

Findings from static analysis of the official Pixel Buds companion app APK
(`com.google.android.apps.wearables.maestro.companion`, see
`SCREENSHOTS_PIXEL_BUDS_APP.md`). Every entry references a concrete file/path
under `reverse-engineering/apk/v<versionName>-<versionCode>/jadx-output/` or
`apktool-output/` (see `reverse-engineering/APK_VERSIONS.md` for which
version(s) have been pulled/analyzed, and
`APK_REVERSE_ENGINEERING_PROCEDURE.md` for the pull/decompile/search
procedure — including the current, `DECISIONS.md` ADR-017 AI-assistance
boundary for this kind of work).

> **Status:** no analysis session has been logged yet — this document currently
> defines the structure and workflow. Populate the sections below as findings
> come in, one class/finding at a time, following `PROJECT_RULES.md` §1
> (FACT / HYPOTHESIS / ASSUMPTION) and §3.

**Non-destructive-update convention:** this document follows the same
convention `CAP-NNN-FINDINGS.md` files use (`PROJECT_RULES.md` §3 rule 9a) —
rewrite a finding in place as understanding changes; don't stack
"Correction"/"Update" addendums under the original text. The history of *how*
a finding changed belongs in `git log`/`git blame` and, for anything
significant, `CHANGELOG.md` — not in this document's own prose. (This differs
from `DECISIONS.md`/`PROTOCOL.md`'s convention, where dated `Update` notes are
kept alongside the original text — see `PROJECT_RULES.md` §3 rule 9a for the
scope distinction.)

**Scope reminder** (see `PROJECT_RULES.md` §8, `PROJECT.md` non-goals): this
analysis covers software the maintainer has legally installed themselves,
performed solely for interoperability with hardware the maintainer personally
owns. No code from the official app is reused in this project's own codebase —
only the *behavior* it reveals (protocol structure, UUIDs, opcodes) is
reconstructed. Findings correlated with Bluetooth captures are promoted
directly into `PROTOCOL.md`, per `PROJECT_RULES.md` §2.

Status legend (consistent with `PROTOCOL.md` §0):

- 🟢 **FACT** — directly observed in the decompiled/disassembled code.
- 🟡 **HYPOTHESIS** — plausible reading of obfuscated or ambiguous code, not yet
  cross-checked against a capture.
- ⚪ **ASSUMPTION** — inferred from naming conventions, structure, or comparison
  to a similar/known library, not directly confirmed.
- 🔴 **OPEN QUESTION** — genuinely unresolved: no specific hypothesis or working
  assumption exists yet, only an identified gap.

---

## APK metadata

Version identity, SHA-256, pull date, source device, and provenance are tracked per-version in
`reverse-engineering/APK_VERSIONS.md` (the git-tracked index — see `APK_REVERSE_ENGINEERING_PROCEDURE.md`),
not duplicated here. This table covers the technical fields of whichever version is currently the
main analysis target.

| Field | Value |
|---|---|
| Package name | `com.google.android.apps.wearables.maestro.companion` |
| Current analysis target version | `v1.0.955078536-10253511` (see `reverse-engineering/APK_VERSIONS.md`) |
| Min/target/compile SDK | minSdk 32, targetSdk 36 (`adb shell dumpsys package`, 2026-08-30); compileSdk not separately confirmed |
| Obfuscation present? | Yes — R8/ProGuard: almost all app-internal classes are flattened into a single `defpackage` package with short (2–4 char) obfuscated names (e.g. `fxm`, `gbm`, `fzd`, `goq`), grouped ~19 unrelated lambda bodies into shared synthetic dispatch classes (e.g. `gau`, see its entry below). A handful of third-party library classes survive unobfuscated (`dev.pigweed.pw_tokenizer.Detokenizer`, `androidx.*`) — one Kotlin function-reference metadata string also survives with its pre-obfuscation signature intact (see `fsz` entry below), which is how the `pw_hdlc`/`pw_rpc` link below was found at all. |
| Native libraries present? | Yes, but not `libmaestro.so`/`libgfps.so` — only `lib/arm64-v8a/libandroidx.graphics.path.so` and `lib/arm64-v8a/libpw_tokenizer_jni.so`, both present only in `split_config.arm64_v8a.apk` (absent from `base.apk`). No file named `libmaestro`/`libgfps` exists anywhere across base + both splits, and no `System.loadLibrary` call in the decompiled sources names one either — see §Native libraries and the Open questions on the `fxm`/`gbm` entries below for what this means for AGENTS.md §0/§6's native-binary framing. |
| Firmware/library versions referenced in-app | _(not yet checked this pass)_ |

## Method

See `APK_REVERSE_ENGINEERING_PROCEDURE.md` for the full step-by-step procedure
(pulling/storing a new version, the diff-against-previous-version pass, the
keyword-search efficiency techniques, and the exclusion list for
out-of-scope areas). Summary:

1. `apktool d base.apk -o apktool-output/` — for resources, manifest,
   smali.
2. `jadx -d jadx-output/ base.apk` — for readable (decompiled)
   Kotlin/Java.
3. Search `jadx-output/` for keywords:
   - `BluetoothSocket`, `BluetoothGatt`, `BluetoothGattCallback`,
     `BluetoothGattCharacteristic` (this app uses RFCOMM as the primary control
     transport per `ARCHITECTURE.md` §1 — don't assume GATT-only)
   - `UUID.fromString`
   - `MessageStream`, `AccountKey`, `FastPair` (Fast Pair SDK usage, relevant to
     `PROTOCOL.md` §2.1/§4.3)
   - `BluetoothHidDevice`, `HidDeviceProfile`, `HidDeviceAppSdpSettings` (added
     2026-08-23 — the project has a live, evidenced Bluetooth HID hypothesis:
     HID-Control/HID-Interrupt L2CAP channels observed during SDP in `CAP-002`,
     and an `AndroidHeadTracker` HID Feature report decoded in `CAP-016`; see
     `ARCHITECTURE.md` §1/§15. Significant enough that missing it on the first
     APK pass would be a real gap, not a cosmetic one — check for it explicitly
     rather than relying on the generic `ble`/`bluetooth` keywords below to
     surface it.)
   - Package/class names containing `ble`, `bluetooth`, `buds`, `wearable`,
     `headset`, `maestro`, `gfps`, `hid`
   - `.proto`-generated classes (look for `GeneratedMessageLite`,
     `builder()`, field names matching known UI strings like `anc_mode`,
     `eq_band`)
4. For `.proto` schema extraction specifically, use `pbtk`
   (`AGENTS.md` §4/§6) rather than hand-reconstructing schemas from
   decompiled getter/setter names alone — protobuf field numbers are not
   always recoverable from JADX output.
5. Document every relevant class below, even if its name is obfuscated —
   record the obfuscated name plus a readable alias you assign for reference.
6. Cross-check any candidate opcode, UUID, or message code against a real
   capture before promoting it from 🟡/⚪ to 🟢 in `PROTOCOL.md` — static
   analysis alone does not confirm runtime behavior.

## Identified relevant classes

> Template per class — copy for each new finding. **Every finding must cite
> the exact decompiled file *and line number*** (e.g.
> `reverse-engineering/apk/v1.2.3-45/jadx-output/sources/com/google/.../Xy2.java:142`),
> not only a class name — this is what `PROJECT_RULES.md` §1 rule 3 already
> requires of a `REVERSE_ENGINEERING.md` evidence source; the **Path** field
> below must always include `:line_number`, never a bare file path.

All entries below are from `v1.0.955078536-10253511` (`reverse-engineering/APK_VERSIONS.md`),
surfaced by an AI mechanical §4 keyword-search pass (`DECISIONS.md` ADR-017) and written up here at
the maintainer's explicit request — the FACT/HYPOTHESIS/ASSUMPTION labels below follow
`PROJECT_RULES.md` §1's rule that static analysis alone is 🟢 FACT only for "this code exists and
does X," never for a protocol-behavior claim; every protocol-relevance reading stays 🟡/⚪ with a
**Hypothesis test** until cross-checked against a capture. None of this has been promoted to
`PROTOCOL.md`, and no `DECISIONS.md` ADR has been written or superseded — both remain a maintainer
call per `AGENTS.md` §6/§15.

### `defpackage.fzd` — InternalRfcommUuidRegistry

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/fzd.java:9`
- **Readable alias**: InternalRfcommUuidRegistry
- **Role**: 🟢 FACT: interface holding two 128-bit UUID constants used elsewhere (see `gbm` below)
  to select between two internal RFCOMM sockets. Each UUID appears in both a canonical byte order
  and a byte-reversed form — 4 `UUID.fromString` literals total; confirmed by manually reversing the
  byte sequence of each pair, they are the same 2 UUIDs, not 4. Static field `b` (`fzd.java:12-18`)
  is a map normalizing any of the 4 string forms to one of the 2 canonical UUIDs.
- **Relevant UUIDs found**: `3a046f6d-24d2-7655-6534-0d7ecb759709` / `099775cb-7e0d-3465-5576-d2246d6f043a`
  (same UUID, byte-reversed pair) and `25e97ff7-24ce-4c4c-8951-f764a708f7b5` / `b5f708a7-64f7-5189-4c4c-ce24f77fe925`
  (same UUID, byte-reversed pair) — cross-reference §UUID register.
- **Relevant message groups/codes found**: none (this is RFCOMM/SDP-layer, not Fast Pair Message
  Stream).
- **Relevant methods**:
  - static initializer `fzd.java:9-18` — builds the UUID list (`a`) and the normalization map (`b`).
- **Hypothesis test**: not applicable to this entry alone (constants only) — see `gbm`'s entry.
- **Open questions**: which of these two UUIDs (if either) matches the SDP record UUID actually
  observed on the wire for DLCI 0x02 (Pigweed `pw_hdlc`) vs. DLCI 0x08 (still-unidentified private
  envelope) in `PROTOCOL.md` §2.2a/§2.3's table — not yet checked against a capture.

### `defpackage.gbm` — InternalRfcommSocketSelector

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/gbm.java:35`
- **Readable alias**: InternalRfcommSocketSelector
- **Role**: 🟢 FACT: `a()` (`gbm.java:20-47`) selects between two RFCOMM socket implementations based
  on which UUID(s) are present in a discovered UUID set, logging `"Provide pigweed internal rfcomm
  socket"` (`gbm.java:36`, if either "pigweed" UUID form from `fzd` matches), `"Provide default
  internal rfcomm socket"` (`gbm.java:39`, if either "default" UUID form matches), or falling back to
  the pigweed implementation otherwise (`gbm.java:42-43`). 🟡 HYPOTHESIS, now capture-corroborated
  (see Hypothesis test below — still 🟡, promotion to 🟢 in `PROTOCOL.md` is a maintainer call per
  `AGENTS.md` §6, not made here): the "pigweed"-labeled UUID (`25e97ff7-...`) is the RFCOMM service
  UUID for the Pigweed `pw_hdlc`-encoded channel AGENTS.md §6 already identifies as DLCI 0x02.
- **Relevant UUIDs found**: the same 4 forms (2 canonical UUIDs) as `fzd`, imported via `fzd`'s
  registry.
- **Relevant methods**:
  - `a():20-47` — the selection logic described above.
- **Hypothesis test — result (checked 2026-08-30, three independent capture sessions)**: for each of
  `CAP-001`, `CAP-002`, `CAP-032` (`bluetooth.addr == 04:00:6e:cf:6e:07`), the SDP Service Search
  Attribute Response lists `25e97ff7-24ce-4c4c-8951-f764a708f7b5` among the discovered service UUIDs,
  and the matching Protocol Descriptor List response resolves it to **RFCOMM server channel 1** in
  every session (`btsdp.data_element.value.uuid_128`/`btsdp.protocol.channel`: `CAP-001` frame 1327 @
  42.534s, `CAP-002` frame 1327 @ 42.534s — same elapsed offset, likely the same setup ritual, not a
  bug — `CAP-032` frame 1632 @ 104.943s). Channel 1 is then opened with `Sent SABM Channel=1` /
  `Rcvd UA Channel=1`, and `tshark`'s own `btrfcomm.dlci` field reads **`0x02`** for every frame on
  that channel, in all three captures (`CAP-001` frame 1334 @ 42.545s, `CAP-032` frame 1645 @
  105.173s) — i.e. RFCOMM server channel 1 *is* DLCI 0x02 on the wire, not just by the `2×channel`
  arithmetic. This is a direct, three-session-reproducible SDP-to-DLCI correlation: the "pigweed"
  UUID this pass found in `gbm.java`/`fzd.java` is the actual SDP-advertised service UUID for the
  channel AGENTS.md §6 calls DLCI 0x02. **Maintainer sign-off obtained 2026-08-30 (`DECISIONS.md`
  ADR-018, Option 2 — narrow promotion):** channel *ownership* (this is the companion app's own
  internal RFCOMM socket) is now 🟢 FACT in `PROTOCOL.md` §2.2a/§2.3; that the Sent-direction
  payload *content* specifically carries `libmaestro`'s settings-write commands remains 🟡
  HYPOTHESIS (strong), unchanged — see `PROTOCOL.md` §2.2a for the exact scope of what was and
  wasn't promoted.
- **Open questions**: what the "default internal rfcomm socket" (`3a046f6d-...`) corresponds to —
  DLCI 0x08's still-unidentified private envelope, or something else entirely. **Exhaustive
  negative result (checked 2026-08-30):** a raw-byte scan (both byte-order forms) of every capture
  file in this project's possession — all 23 `*btsnoop_hci.log` files, the 3 additional
  `*btsnooz_hci.log` files (`CAP-012`, `CAP-013`, `CAP-031` — missed by the first pass, since that
  used a `*btsnoop_hci.log`-only glob; `file` confirms all three are genuine "BTSnoop version 1,
  HCI UART (H4)" despite the "btsnooz" filename, not a compressed/different format), and the two
  nRF Connect logs (`CAP-014-nrf-connection.log`, `CAP-017-nRF.txt`) — found **zero occurrences**
  of either UUID form anywhere. The two nRF logs were confirmed to be BLE-only GATT connection logs
  (`gatt = device.connectGatt(..., TRANSPORT_LE, ...)`), which cannot structurally contain a
  Bluetooth Classic SDP/RFCOMM service UUID at all — a non-result by construction, not a gap. Also
  checked: every `CAP-NNN-FINDINGS.md`/`CAP-NNN-EVENT-NOTES.md` for a prior manual mention (none
  found). **Conclusion:** the "default internal rfcomm socket" has not been observed on the wire in
  any capture taken so far, in either byte order, across every capture format this project has
  collected — genuinely unexplained, not merely unsearched. Plausible next step: a fresh capture
  specifically covering whatever app action/condition triggers the app to pick the "default" branch
  in `gbm.a()` instead of "pigweed" (not yet identified what that condition is — `gbm.java` alone
  doesn't show what determines which UUID ends up in the discovered SDP set).

### `defpackage.gau` (switch arm `case 3` only) — RfcommUuidNormalizer

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/gau.java:39`
- **Readable alias**: RfcommUuidNormalizer
- **Role**: 🟢 FACT: `gau` is an R8-merged synthetic lambda-dispatch class (`implements orz`,
  constructed as `new gau(<int>)` from ~20 unrelated call sites) — **only `case 3` is
  protocol-relevant**; the other arms handle unrelated features (OOBE state, device SKU lookup,
  hearing-wellness notifications, screen-on/off listening, etc.) and are out of scope here. `case 3`
  (`gau.java:39`): `UUID.fromString((String) fzd.b.get(((UUID) obj).toString()))` — normalizes an
  input UUID (potentially the byte-reversed form) to its canonical form via `fzd`'s registry.
- **Relevant methods**:
  - `a(Object):39` (one arm of the shared `switch` in `a(Object)`, `gau.java:22-156`).
- **Open questions**: none beyond `fzd`/`gbm`'s.

### `defpackage.gbd` — InternalRfcommConnection

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/gbd.java:10`
- **Readable alias**: InternalRfcommConnection
- **Role**: 🟢 FACT: wraps a `BluetoothSocket` with a `DataOutputStream q` / `DataInputStream r` pair
  (`gbd.java:20-21`) for whichever "internal" RFCOMM channel `gbm` selected. `gbd.java:211` closes the
  underlying `BluetoothSocket`. This is the actual read/write connection object for whichever of the
  two `fzd` UUIDs was matched.
- **Relevant methods**:
  - constructor `gbd.java:26-35`.
  - socket close path around `gbd.java:211`.
- **Open questions**: full read/write/frame-decoding logic not traced this pass — this pass only
  located the class, not its `pw_hdlc` frame handling in detail.

### `defpackage.fxm` — MaestroSoftwareInfoAndHidUuidCheck

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/fxm.java:10`
- **Readable alias**: MaestroSoftwareInfoAndHidUuidCheck
- **Role**: 🟢 FACT, two behaviors in one class: (a) holds the official Bluetooth SIG HID Profile
  UUID `00001124-0000-1000-8000-00805f9b34fb` (`fxm.java:12`) and, in `i()` (`fxm.java:78-117`),
  checks whether a connected device's discovered UUID set already contains it — if not, logs
  `"Could not find HID UUID, fetchUuidsWithSdp."` (`fxm.java:107`) and calls
  `BluetoothDevice.fetchUuidsWithSdp()` (`fxm.java:110`) to trigger a fresh SDP UUID fetch. (b) In
  `c()`/`i()` (`fxm.java:46-91`), issues four `maestro_pw.Maestro`/`GetSoftwareInfo` pw_rpc unary
  calls, one per combination of `{MAESTRO_A, MAESTRO_B} x {LEFT_BT_CORE, RIGHT_BT_CORE}` (see `goq`
  entry below).
- **Relevant UUIDs found**: `00001124-0000-1000-8000-00805f9b34fb` (official Bluetooth SIG HID
  Profile UUID).
- **Relevant message groups/codes found**: n/a (this is pw_rpc, not Fast Pair Message Stream).
- **Relevant methods**:
  - `c(goq,goq):46-65` — builds one `GetSoftwareInfo` pw_rpc unary call for a given source/target
    route pair.
  - `i():78-117` — fires `GetSoftwareInfo` for all 4 route pairs, then runs the HID-UUID-presence
    check described above.
- **Hypothesis test**: correlate against `CAP-002`/`CAP-016`'s existing HID captures — does an SDP
  fetch (triggered by this code path) precede the HID-Control/HID-Interrupt L2CAP channel setup
  already observed there?
- **Open questions**: what `GetSoftwareInfo`'s actual pw_rpc request/response payload looks like on
  the wire — not yet captured or decoded.

### `defpackage.fsz` — MaestroWriteSettingRpcClient

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/fsz.java:75`
- **Readable alias**: MaestroWriteSettingRpcClient
- **Role**: 🟢 FACT: issues a `maestro_pw.Maestro`/`WriteSetting` pw_rpc call (`fsz.java:75`). A
  Kotlin function-reference metadata string at `fsz.java:223` —
  `"getWriteSettingMethodClient(Lcom/google/android/apps/wearables/maestro/companion/pw/hdlc/RouteProto$Route;)Ldev/pigweed/pw_rpc/MethodClient;"`
  — literally names the app's own `com.google.android.apps.wearables.maestro.companion.pw.hdlc.RouteProto$Route`
  class and the upstream `dev.pigweed.pw_rpc.MethodClient` class. This string escaped R8 renaming,
  unlike almost everything else in `defpackage`, because Kotlin method-reference metadata must retain
  original signatures for reflection — it is the single strongest static-analysis confirmation this
  pass found that the app's own vocabulary for this transport is literally Pigweed `pw_rpc` over
  `pw_hdlc`. 🟡 HYPOTHESIS: `WriteSetting` is likely the generic pw_rpc call used for settings this
  project cares about (ANC, EQ, etc.) not already covered by the Fast Pair Message Stream's Group
  `0x08` opcode (`PROTOCOL.md` §4.1) — not yet confirmed against a capture or a decoded request
  payload.
- **Relevant methods**:
  - the method whose reference is captured at `fsz.java:223` (`getWriteSettingMethodClient`; the call
    site itself is obfuscated and not further traced this pass).
- **Hypothesis test**: capture a setting change made through DLCI 0x02 (the Pigweed channel candidate
  from `gbm`'s entry) and check whether its payload, once `pw_hdlc`-decoded, resolves to a
  `WriteSetting` pw_rpc unary call for a setting not already explained by the Message Stream Group
  0x08 opcode.
- **Open questions**: full list of settings routed through `WriteSetting` vs. the Message Stream path.

### `defpackage.fut` — HdlcSendLogger (minor supporting evidence)

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/fut.java:220`
- **Readable alias**: HdlcSendLogger
- **Role**: 🟢 FACT: logs `"Fail to send hdlc encoded message"` on a send failure — corroborates
  `fsz.java:223`'s `pw_hdlc` package reference using the app's own log vocabulary, independently of
  that one surviving metadata string.
- **Open questions**: which higher-level class calls into this failure path, and under what
  conditions — not traced this pass.

### `defpackage.fux` — MaestroPwRpcServiceCatalog

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/fux.java:9-120`
  (`a()` is a single `switch (this.a)` dispatch method, same R8-merged-lambda pattern as `gau`; each
  `case` builds one `nqs` service-descriptor object)
- **Readable alias**: MaestroPwRpcServiceCatalog
- **Role**: 🟢 FACT: this pass re-read `fux.java` in full (superseding the earlier partial read, which
  covered only 5 of the 6 `maestro_pw.*` services and, critically, **missed `maestro_pw.Maestro`
  itself** — `case 8`, `fux.java:55-66` — the service `WriteSetting`/`GetSoftwareInfo`/`ReadSetting`
  actually belong to). Each `nqs.c(name, requestDefaultInstance, responseDefaultInstance)` call names
  the exact request/response message class for that RPC method (via each class's own `X.a` singleton
  default-instance field, e.g. `qjc.a`) — this is a strictly richer reading than method names alone,
  now decodable with `scripts/decode_rawmessageinfo.py` (see the new class entries below for what each
  type actually contains). Concrete service/method/type table, all still 🟢 FACT-for-code-existence
  only (protocol *meaning* stays 🟡/⚪ per entry):

  | Service | Method | Kind | Request type | Response type | `fux.java` line |
  |---|---|---|---|---|---|
  | `maestro_pw.Dosimeter` | `FetchDailySummaries` | unary | `nia` (empty) | `qhz` | `:27` |
  | `maestro_pw.Dosimeter` | `SubscribeToLiveDb` | server-stream | `nia` (empty) | `qir` | `:27` |
  | `maestro_pw.EartipFitTest` | `StartTest` | unary | `qii` (empty) | `nia` (empty) | `:41` |
  | `maestro_pw.EartipFitTest` | `SubscribeToResults` | server-stream | `qih` (empty) | `qig` | `:41` |
  | `maestro_pw.EartipFitTest` | `EndTest` | unary | `qif` (empty) | `nia` (empty) | `:41` |
  | `maestro_pw.JitterBuffer` | `SetJitterBufferSizePreference` | unary | `qjs` | `nia` (empty) | `:50` |
  | `maestro_pw.Maestro` | `GetSoftwareInfo` | unary | `nia` (empty) | `qjb` | `:57` |
  | `maestro_pw.Maestro` | `GetHardwareInfo` | unary | `nia` (empty) | `qiv` | `:58` |
  | `maestro_pw.Maestro` | `SubscribeRuntimeInfo` | server-stream | `nia` (empty) | `qiy` | `:59` |
  | `maestro_pw.Maestro` | `SetWallclock` | unary | `qiz` | `nia` (empty) | `:60` |
  | `maestro_pw.Maestro` | **`WriteSetting`** | unary | **`qjc`** | `nia` (empty) | `:61` |
  | `maestro_pw.Maestro` | `ReadSetting` | unary | `qix` | `qja` | `:64` |
  | `maestro_pw.Maestro` | `SubscribeToSettingsChanges` | server-stream | `nia` (empty) | `qja` | `:64` |
  | `maestro_pw.Maestro` | `SubscribeToOobeActions` | server-stream | `nia` (empty) | `qje` | `:64` |
  | `maestro_pw.Multipoint` | `SubscribeToQuietModeStatus` | server-stream | `nia` (empty) | `qjp` | `:69` |
  | `maestro_pw.Multipoint` | `ForceMultipointSwitch` | unary | `qij` | `nia` (empty) | `:69` |
  | `maestro_pw.HeadGesture` | `StartDetection` | unary | `qip` (empty) | `nia` (empty) | `:112` |
  | `maestro_pw.HeadGesture` | `SubscribeToResults` | server-stream | `qio` (empty) | `qin` | `:112` |
  | `maestro_pw.HeadGesture` | `EndDetection` | unary | `qim` (empty) | `nia` (empty) | `:112` |

  Also present in `fux.java` but not pursued this pass (out of this session's WriteSetting/EQ/ANC
  focus, not out of project scope — plain infra/OTA services, not settings-relevant): `case 1`/`12`
  build `pw.software_update`-style OTA method catalogs (`SetManualOta`, `StartRightBudUpdate`,
  `GetRunningVersion`, etc., `fux.java:31-37`/`82-88`); `case 3` is the generic pw_rpc `EchoService`
  (`:44`); `case 6` is `a10a20.kpi.Kpi`/`KpiStream` (`:52`); `case 7` is `pw.log.Logs`/`Listen` (`:54`);
  `case 10` is `pw.software_update.BundledUpdate` (`:74`); `case 13`/`14` build `goq`-keyed integer
  maps unrelated to any RPC service (`:89-104`); `case 4`/`11`/`18`/`19` return unrelated pre-built
  objects (`gok`/`nse`/`gam`/`gay`, not traced).

  ⚪ ASSUMPTION (unchanged from before): service names map fairly literally to app features
  (Dosimeter = sound-exposure/hearing-health dosimeter; EartipFitTest = the `eartipseal` fit-test UI
  already found under `ui/settings/sound/eartipseal/`; HeadGesture = the head-gesture feature
  AGENTS.md §6 already references) — inferred from naming, not confirmed against UI flow or a capture.
- **Hypothesis test**: for each service, capture the corresponding UI action (e.g. running an ear-tip
  fit test, triggering a head gesture, changing ANC/EQ) per `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, and
  check whether the resulting DLCI 0x02 traffic decodes (via `pw_hdlc`) to a call against that
  service/method name, then whether the payload's field layout matches the corresponding type's
  decoded structure below.
- **Open questions**: whether all `maestro_pw.*` services travel over the same RFCOMM channel (the
  "pigweed" UUID from `gbm`) or are split across more than one.

### Tooling note — `scripts/decode_rawmessageinfo.py` (protobuf-lite `RawMessageInfo` compact-schema-string decoder)

This APK's protobuf-lite runtime uses `GeneratedMessageLite.newMessageInfo(default, infoString,
objects)` (the compact schema-string codegen style), not the older per-field
`CodedInputStream.readXxx()` calls `pbtk`'s `jar_extract.py` regex-parses — `pbtk` cannot parse this
codegen style at all (confirmed root cause, not a scope/targeting issue; see
`WORKSTATION_PREPARATIONS.md`'s pbtk section). `scripts/decode_rawmessageinfo.py` is a small,
dependency-free Python decoder for this format, written this pass. It is a direct, field-for-field
port of the real parsing algorithm — not a guess or approximation, per `PROJECT_RULES.md` §1's
"operate with zero creativity" rule — copied from three files fetched from the upstream
`protocolbuffers/protobuf` GitHub repo (`main` branch, fetched 2026-08-30):
`java/core/src/main/java/com/google/protobuf/RawMessageInfo.java` (info-string header/flags
decoding), `.../MessageSchema.java`'s `newSchemaForRawMessageInfo` method (the full field-entry
parsing loop, byte offsets, and the `Object[]` array consumption order), and
`.../FieldType.java` (the field-type id table, values 0–50; oneof types are `51 + <base type id>`
per `MessageSchema.ONEOF_TYPE_OFFSET`). Validated against this project's own two already-documented
trivial marker types (`nia`, `qib`, both `\u0004\u0000`/`\u0001\u0000` → 0 real fields, matching
their known-trivial role) before being trusted on anything else. Usage:
`python3 scripts/decode_rawmessageinfo.py <path/to/Class.java> [more files...]` — finds every
`new naa(<default>, "<info-string>", <objectsArrayOrNull>)` construction in each file (the pattern
`REVERSE_ENGINEERING.md`'s `naa`/`myp` mapping above already confirmed byte-for-byte matches Google's
own public `RawMessageInfo` class) and prints each message's decoded field list: field number, wire
type, oneof/map-ness, and — where the `Object[]` array entry is a Java string literal — the raw
(usually short, obfuscated: `"b"`, `"c"`, ...) declared field name. This is a purely mechanical,
code-structure decode (🟢 FACT-for-code-existence per field, exactly as strong as reading the same
structure by hand from the smali/JADX output would be) — it does not itself establish wire-protocol
*meaning*; every semantic reading below is separately labeled 🟡/⚪ and requires its own capture
correlation, per `AGENTS.md` §6/§15 and `PROJECT_RULES.md` §1.

### `defpackage.qjc` / `defpackage.qja` — MaestroSettingOneof (`WriteSetting` request body / `ReadSetting`+`SubscribeToSettingsChanges` response body)

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/qjc.java:28`
  and `.../qja.java:28`
- **Readable alias**: MaestroSettingOneof
- **Role**: 🟢 FACT (decoded via `scripts/decode_rawmessageinfo.py`, cross-checked by hand against the
  raw info-string before trusting the script's output): `qjc` is the exact `WriteSetting` request
  type named in `fux.java:61` (`nqs.c("WriteSetting", (mzw) qjc.a.a(7, null), ...)`), and `qja` is the
  exact `ReadSetting` response / `SubscribeToSettingsChanges` response type (`fux.java:64`). Both
  decode to **byte-for-byte the same 5-field, single-oneof shape**: a `oneof` (Java fields `b`=case
  selector `int`, `c`=value `Object`, matching each class's own field declarations,
  e.g. `qjc.java:8-9`: `public int b = 0; public Object c;`) with 5 alternatives, one `MESSAGE`-typed
  field per proto field number 1–5:
  - field 1 → `qhx` (1 field — see below)
  - field 2 → `qjn` (12 fields — see below)
  - field 3 → `qjt` (10 fields, sparse field numbers 1–14 — see below)
  - field 4 → `qhr` (**38 fields** — see below, the headline finding of this pass)
  - field 5 → `qjv` (3 fields — see below)

  This is a genuine "one `Setting` value, one of 5 top-level categories, each category itself
  containing a further oneof of individual settings" shape — used identically as the `WriteSetting`
  request payload and as the `ReadSetting`/`SubscribeToSettingsChanges` response payload, which is
  itself evidence (🟡 HYPOTHESIS, code-structure-only) that these three RPC methods share one generic
  "the current/desired value of setting X" representation, consistent with a request/response-mirror
  pattern rather than 3 independently-shaped payloads.
- **Cross-reference to `PROTOCOL.md` §4.5 / `DECISIONS.md` ADR-013**: ADR-013 promoted to 🟢 FACT
  (from wire evidence alone, independent of this static-analysis pass) that DLCI 0x02's
  general-purpose settings-write envelope has the outer shape `field 5 { field 4 { ... } }`. This
  pass's independent, code-derived finding is structurally consistent with that: `qjc`'s field-number
  **4** alternative is `qhr` — i.e. the wire-observed "field 4" *is* plausibly `qjc`'s `qhr`
  alternative, and `qhr` in turn contains its own 38-way inner oneof (the "..." ADR-013 did not
  further decompose). This is **not** a new promotion — ADR-013's own scope note already stated only
  the *outer wrapper shape* was promoted, not any individual field's meaning — but it is new,
  independently-derived static evidence for the *same* structural claim, from a completely different
  evidence path (APK code vs. wire bytes), which strengthens without changing either's status. (ADR-013
  describes only a 2-level `field5{field4{...}}` nesting from the wire; this pass's 3-level
  `qjc.field4→qhr.fieldN` reading is a finer decomposition of what ADR-013's own "..." covered, not a
  contradiction of it — see Open questions below for the one thing that doesn't fully reconcile.)
- **Open questions**: ADR-013 describes the *outer* wrapper as `field5{field4{...}}` — i.e. field 5 is
  the outermost tag observed on the wire. This pass's reading has `qjc` itself (containing field-4
  `qhr`) as one level *inside* whatever carries it — plausibly the `payload` field (field number 5) of
  Pigweed's own `pw_rpc.RpcPacket` wire envelope (the standard `pw_rpc` packet proto has `payload` at
  field 5), which would make the full nesting `RpcPacket.field5(payload) → qjc.field4(qhr) → qhr.fieldN`
  — three levels, of which ADR-013 (working from raw wire bytes, not from this session's just-recovered
  `pw_rpc.RpcPacket`/`qjc` schemas) apparently only distinguished two. This reconciliation is a
  plausible reading, not confirmed — no `pw_rpc.RpcPacket`-shaped class has been located/decoded this
  pass. A capture correlation (redecoding an existing DLCI 0x02 "Sent" frame's raw hex against this
  3-level structure) would be needed to confirm or refute it.
- **Hypothesis test**: pick any existing capture with a DLCI-0x02 Sent-direction frame already
  identified as `field5{field4{...}}` (`CAP-019`–`CAP-024` per ADR-013), and check whether the "..."
  bytes further decompose as `qhr`'s own oneof tag structure (`\x08`-prefixed varint field-number,
  wire-type 0/2/5 depending on field 9/10/13's `ANC_STATE` enum vs. field 16/18's `qjw` message vs. a
  plain bool) — this would be a direct, byte-level confirmation or refutation of the reconciliation
  above.

### `defpackage.qhr` — MaestroSettingGroup4 (38-field oneof; contains 3 fields of the `qhs` **`ANC_STATE_*`** enum)

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/qhr.java:29`
- **Readable alias**: MaestroSettingGroup4 (field-4 alternative of `qjc`/`qja`'s top-level oneof)
- **Role**: 🟢 FACT (code existence/structure): a single oneof with **38** `MESSAGE`/`BOOL`/`ENUM`/
  `SINT32` alternatives, proto field numbers 1–38, all sharing the same `b`(case)/`c`(value) field
  pair. Full decode (`python3 scripts/decode_rawmessageinfo.py qhr.java`):
  fields 1–6, 8, 11, 14, 15, 19–22, 25, 27, 28, 30, 32–34, 36, 38 = plain `BOOL`; field 17 = `SINT32`;
  fields 7, 12, 16, 18, 23, 31, 35, 37 = `MESSAGE` (nested types `qju`, `qht`, `qjw`×2, `qhq`, `qiq`,
  `qjf`, `qis` — see their own entries below); fields 9, 10, 13 = `ENUM` of type `qhs`; fields 24, 26,
  29 = `ENUM` of types referenced only as `qgx.n`/`qgx.s`/`qgx.r` (opaque validity-checker instances
  delegating to an obfuscated shared helper `a.aO(int)` — not independently named, unlike `qhs`, so no
  further reading attempted this pass).
  **Headline finding**: the enum class backing fields 9, 10, and 13 — reached via
  `qgx.j` → `case 9: return qhs.b(i2) != null;` (`qgx.java:56`) — is `defpackage.qhs`
  (`qhs.java:5-10`), and **its constants are unobfuscated, human-readable literals**:
  `ANC_STATE_UNKNOWN(0)`, `ANC_STATE_OFF(1)`, `ANC_STATE_ACTIVE(2)`, `ANC_STATE_AWARE(3)`,
  `ANC_STATE_ADAPTIVE(4)`. This is a direct, code-level (not naming-convention-inferred) match in
  *kind* to `PROTOCOL.md` §4.1's already-🟢-FACT ANC mode set (Off / ANC("Active") / Transparency
  ("Aware") / Adaptive) confirmed over the *official Fast Pair Message Stream* (DLCI 0x04, Group
  `0x08`, ADR-009) — i.e. `libmaestro`'s own DLCI-0x02 settings-write/read vocabulary independently
  has its own concept of the same 4 ANC states (plus `UNKNOWN`), under a Google-internal name
  (`qhs`/`ANC_STATE_*`) unrelated to the public Fast Pair spec's own bit values.
- **Why this is 🟡 HYPOTHESIS, not 🟢 FACT, for wire meaning**: this establishes that the *code*
  contains an `ANC_STATE`-typed field reachable from `WriteSetting`'s request/`ReadSetting`'s response
  — it does **not** establish that DLCI 0x02 traffic is ever observed setting/reading it, nor which of
  fields 9/10/13 (three separate proto field numbers, all the same enum type) corresponds to which
  actual behavior (e.g. one could be a write-request field, another a read-response mirror, another
  something else entirely — the same `qhr` class is reused for both `WriteSetting` requests and
  `ReadSetting`/`SubscribeToSettingsChanges` responses via `qjc`/`qja`, so field role is not
  determined by class identity alone). No claim is made here about *why* there are three, or which
  (if any) is the one a UI ANC-mode change would populate.
- **Hypothesis test**: capture an isolated ANC-mode change (repeat of `CAP-006`'s isolated-tap
  methodology) with full DLCI 0x02 traffic retained, and check whether any Sent frame's `pw_hdlc`
  payload decodes (per the `field5{field4{...}}`/`qjc.field4→qhr.fieldN` structure above) to a `qhr`
  field-9, field-10, or field-13 write carrying a value 1–4. A positive match — especially one
  time-correlated with the tap, mirroring `CAP-006`'s DLCI-0x04 confirmation methodology — would be
  strong evidence DLCI 0x02 *also* carries ANC state (parallel to, not instead of, DLCI 0x04's already-
  confirmed path); a clean negative result across several isolated taps would suggest these `qhr`
  fields are written by some other trigger (e.g. periodic state sync, OOBE) or an entirely different
  UI action.
- **Open questions**: what fields 24/26/29's enum types actually are (validity-checker delegates to an
  unnamed obfuscated helper, unlike `qhs`); what the 8 nested `MESSAGE` fields' own semantics are
  beyond their raw shape (see their individual entries below); why the same `ANC_STATE` enum appears
  at 3 different field numbers.

### `defpackage.qjn` / `defpackage.qjt` / `defpackage.qhx` / `defpackage.qjv` — `qjc`/`qja`'s other 4 oneof-group alternatives

- **Path**: `qjn.java:28` (field 2 of `qjc`/`qja`), `qjt.java:28` (field 3), `qhx.java:28` (field 1),
  `qjv.java:28` (field 5) — same directory as above.
- **Readable alias**: MaestroSettingGroup2 / MaestroSettingGroup3 / MaestroSettingGroup1 /
  MaestroSettingGroup5
- **Role**: 🟢 FACT (code existence/structure only), each a single oneof like `qhr`, but far smaller:
  - `qhx` (Group 1): 1 field — field 1, plain `BOOL`. The simplest of the 5 groups; plausibly a
    single-flag category, not otherwise identified this pass.
  - `qjn` (Group 2): 12 fields, numbers 1–12. Fields 1,2,3,5,6,7,8,10,11 = `BOOL`; field 4 = `MESSAGE`
    (`qjo`, 2-field message-of-messages, see below); field 9 = `MESSAGE` (`qjg`, 4×`BOOL`, see below);
    field 12 = `ENUM` (type reached via `qgx.o` → `case 14: return qic.a(i2) != 0;` — delegates to
    another unnamed obfuscated helper, not independently identified).
  - `qjt` (Group 3): 10 fields at **sparse** numbers {1,3,5,7,8,10,11,12,13,14} (2,4,6,9 unused/
    reserved — not explained by this pass). Fields 1,3,5,7,8,10,11,13 = `BOOL`; field 12 = `ENUM`
    (same `qgx.o` reference as `qjn` field 12 — i.e. `qjn` field 12 and `qjt` field 12 share the exact
    same enum type, worth noting for anyone pursuing this further); field 14 = `INT32`.
  - `qjv` (Group 5): 3 fields. Field 1 = `MESSAGE` (`qit`, trivial 0-field marker type — see below);
    field 2 = plain `BOOL`; field 3 = `MESSAGE` (`qjd`, also a trivial 0-field marker type).
- **Open questions**: no field in `qjn`/`qjt`/`qhx`/`qjv` (unlike `qhr`'s `ANC_STATE` fields) resolved
  to an unobfuscated, self-describing name this pass — all remain unidentified as to which app feature
  they belong to. None of the 4 groups' own field numbering/count was cross-checked against
  `PROTOCOL.md` §4.5.1–§4.5.8's already-HYPOTHESIS individual settings list (Conversation Detection,
  Multipoint, Touch controls, Head gestures, press-and-hold ×4, Mono audio, Volume balance, In-ear
  detection, Case-sound toggles ×2) — a follow-up pass matching those to specific field numbers across
  all 5 groups (61 total top-level fields: 1+12+10+38+3 = 64, minus `qjt`'s 4 unused numbers = 60
  actually-present) would be a natural next step, but was not attempted here to avoid guessing field
  semantics from count/position alone.

### `defpackage.qjw` — 5×`FLOAT` message, referenced twice inside `qhr` (fields 16 **and** 18)

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/qjw.java:32`
- **Readable alias**: MaestroFloatQuintet
- **Role**: 🟢 FACT (code existence/structure): 5 plain `FLOAT` fields (proto numbers 1–5, Java fields
  `c,d,e,f,g`, each with its own hasbit/presence). This is the same *shape* (5 floats) as the
  already-🟢-FACT EQ band quintet (`PROTOCOL.md` §4.2, `ADR-016`: Low bass/Bass/Mid/Treble/Upper
  treble, wire order reverse of on-screen order) confirmed over DLCI 0x02's `field5{field4{...}}`
  envelope via capture (`CAP-015`). **`qjw` is referenced from *two separate* field numbers inside
  `qhr`: field 16 and field 18** (both `ONEOF(MESSAGE) ref=qjw.class` in the decoder output) — this is
  a direct, code-level structural match to `PROTOCOL.md` §4.2's own already-recorded open question:
  *"EQ's outer field 16 vs. 18 ('preview' vs. 'fires on slider-release') reading remains 🟡
  HYPOTHESIS"*. This pass did not have that field-16-vs-18 question in mind when decoding `qhr` — it
  fell out of the mechanical decode — which makes the match a genuine independent corroboration
  (static structure now shows *both* candidate field numbers really do exist, both as the identical
  5-float message type) rather than a search targeted at confirming a preexisting guess.
- **What this does NOT establish**: which of field 16 / field 18 is "preview" and which is "commit on
  release" (or whether that framing is even correct) — this pass only confirms both fields exist and
  share `qjw`'s shape; it does not decode which is which, since that requires wire-level timing
  evidence `PROTOCOL.md` §4.2 already says is still missing.
- **Hypothesis test**: exactly the one `PROTOCOL.md` §4.2 already names — a capture with the EQ
  slider dragged and released, distinguishing intermediate-drag DLCI 0x02 traffic from the final
  release write, then checking which field number (16 or 18) each maps to. This finding does not
  change what evidence is needed, only confirms both candidate field numbers structurally exist.

### `defpackage.qjg` / `defpackage.qht` — 4×`BOOL` messages (press-and-hold ×4 shape candidates)

- **Path**: `qjg.java:31` (referenced from `qjn` field 9), `qht.java:31` (referenced from `qhr`
  field 12)
- **Readable alias**: MaestroBoolQuad
- **Role**: 🟢 FACT (code existence/structure): both are 4-field messages, each field a plain `BOOL`
  with its own hasbit (Java fields `c,d,e,f`). ⚪ ASSUMPTION (naming/count-based, not confirmed): the
  4-boolean shape is at least consistent with `PROTOCOL.md` §4.5.4's already-better-evidenced
  press-and-hold finding (4/4 Left/Right × ANC/Assistant combinations, `ADR-013`) — 4 independent
  booleans is exactly the shape 4 combinations of a 2×2 matrix would take if each combination is its
  own flag rather than 2 separate 2-way enums. Not claimed as confirmation; two different message
  types (`qjg`, `qht`) share this identical shape, so shape alone does not distinguish which (if
  either) is press-and-hold versus some unrelated 4-flag setting.
- **Open questions**: which (if either) of `qjg`/`qht` is press-and-hold; what the other one is.

### `defpackage.qjo` / `defpackage.qju` — 2×`MESSAGE` wrapper messages (possible Left/Right or two-part containers)

- **Path**: `qjo.java:29` (referenced from `qjn` field 4), `qju.java:29` (referenced from `qhr`
  field 7)
- **Readable alias**: MaestroPairWrapper
- **Role**: 🟢 FACT (code existence/structure): both are 2-field messages where *each field is itself
  a nested `MESSAGE`* with its own hasbit/presence (Java fields `c,d`) — i.e. a small container
  holding two independently-optional sub-messages, not two scalars. The nested message classes
  themselves were not traced this pass (not visible in the decoder's summary output beyond "MESSAGE";
  a full run would need `objects`-array class-reference resolution one level deeper, not attempted
  here to control scope). ⚪ ASSUMPTION: the two-slot shape is consistent with a Left/Right earbud
  pair container, but equally consistent with any other 2-part structure (e.g. "current"/"target",
  or two unrelated sub-features) — not distinguished by structure alone.
- **Open questions**: what `qjo`'s and `qju`'s two nested message types actually are; whether the
  Left/Right reading has any support beyond shape.

### `defpackage.qhq` / `defpackage.qiq` / `defpackage.qjf` / `defpackage.qis` / `defpackage.qit` / `defpackage.qjd` — trivial 0-field marker types (action-trigger candidates)

- **Path**: `qhq.java:26`, `qiq.java:26`, `qjf.java:26`, `qis.java:26` (all referenced as `MESSAGE`
  alternatives inside `qhr`, fields 23/31/35/37 respectively), `qit.java:26`, `qjd.java:26`
  (referenced from `qjv` fields 1/3)
- **Readable alias**: MaestroEmptyMarker (6 distinct classes, same trivial shape as the
  already-documented `nia`/`qib`)
- **Role**: 🟢 FACT: each decodes to 0 real fields (matching `nia`/`qib`'s info-string shape,
  `\u0001\u0000`-style), i.e. structurally an "empty"/marker message — a value whose *presence*
  (this oneof alternative being the one populated) is the entire signal, no payload. ⚪ ASSUMPTION:
  plausibly action-trigger-style settings (e.g. "force X now", akin to `ForceMultipointSwitch`'s
  request shape) rather than value-carrying settings, given the shape — not confirmed.
- **Open questions**: which specific action, if any, each corresponds to.

### `defpackage.qjb` — `GetSoftwareInfo` response type (cross-checked against live usage, not decode-only)

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/qjb.java:31`
- **Readable alias**: MaestroSoftwareInfoResponse
- **Role**: 🟢 FACT: named as `GetSoftwareInfo`'s response type in `fux.java:57`
  (`nqs.c("GetSoftwareInfo", (mzw) niaVar4.a(7, null), (mzw) qjb.a.a(7, null))`) and matches the type
  `fxm.java`'s response handler actually casts to (`fxk.java:23`: `qjb qjbVar = (qjb) mzqVar;`).
  Decodes to 4 fields: field 3 = `ONEOF(MESSAGE, qjj.class)`, field 4 = `ONEOF(MESSAGE, qie.class)`
  (both alternatives of one oneof), field 5 = plain `FIXED64` (Java field `d`), field 6 = plain `BOOL`
  (Java field `e`). **Cross-check**: `fxk.java:30` reads `if (qjbVar.e) { return; }` on the very
  response object this type describes — the decoder's field 6 → Java field `e` → `BOOL` matches this
  live usage exactly, which is direct evidence (not just decoder self-consistency) that the decode is
  correct. ⚪ ASSUMPTION: field 6/`e` is read as a "stop here, don't treat as primary route" flag in
  context (`fxk.java:30-35`: skips promoting this route to primary if `e` is true) — plausibly an
  "is this response from a *secondary*/non-primary core" flag, not independently confirmed as such
  beyond that one call site's control-flow behavior.
- **Open questions**: `qjj`/`qie` (the two oneof alternatives) and field 5's `FIXED64` value were not
  traced further this pass.



- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/ghd.java:37`
  (also `ghb.java:116`)
- **Readable alias**: MaestroDynamicServerConfigRpcClient
- **Role**: 🟢 FACT: defines/calls the `maestro_pw.DynamicServerConfigService`/`SetConfig` pw_rpc
  unary call.
- **Open questions**: what "dynamic server config" refers to here (feature-flag-style remote config
  pushed to the Buds themselves, vs. something purely client-side) — not yet clear from these two
  files alone.

### `defpackage.goq` — MaestroRouteTarget

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/goq.java:7`
- **Readable alias**: MaestroRouteTarget
- **Role**: 🟢 FACT: a protobuf-lite-style enum (`goq.java:8-22`, values 0–13 plus
  `UNRECOGNIZED(-1)`) enumerating pw_rpc routing targets: `UNKNOWN`, `HOST`, `CASE`, `LEFT_BT_CORE`,
  `RIGHT_BT_CORE`, `LEFT_SENSOR_HUB`, `RIGHT_SENSOR_HUB`, `LEFT_SPI_BRIDGE`, `RIGHT_SPI_BRIDGE`,
  `DEBUG_APP`, `MAESTRO_A`, `LEFT_TAHITI`, `RIGHT_TAHITI`, `MAESTRO_B`. Used as the source/target
  arguments to `fxm`'s `GetSoftwareInfo` calls (see above).
- **Relevant methods**:
  - `b(int):30+` — int-to-enum mapping (standard protobuf-lite `forNumber`-style method).
- **Open questions**: what `CASE`, `LEFT_TAHITI`/`RIGHT_TAHITI`, `LEFT_SENSOR_HUB`/`RIGHT_SENSOR_HUB`,
  and `LEFT_SPI_BRIDGE`/`RIGHT_SPI_BRIDGE` actually correspond to in hardware. ⚪ ASSUMPTION: `CASE`
  is the charging case being independently addressable over this same routing scheme, and `TAHITI`
  reads as a chip/SoC codename — neither is capture-correlated or otherwise confirmed.

### Other `maestro_pw.Maestro`/`Dosimeter`/`EartipFitTest`/`HeadGesture`/`Multipoint`/`JitterBuffer` request/response types — decoded shapes register

The remaining request/response types named in the `fux` service-catalog table above, decoded with
`scripts/decode_rawmessageinfo.py` but not individually traced beyond their raw shape this pass (🟢
FACT for shape/existence; no semantic claim beyond what's noted). `nia`/`qii`/`qih`/`qif`/`qip`/`qio`/
`qim` are all the same already-documented trivial 0-field marker shape (`nia.java:9`-style) and are
omitted from the table below (see the existing `nia` note near this doc's top and the
MaestroEmptyMarker entry above).

| Type | RPC role | Fields | Shape |
|---|---|---|---|
| `qiv` | `GetHardwareInfo` response | 6, sparse 1–7 | fields 1,2=`ENUM`/`UINT32`(plain); a 2-alt oneof at fields 4/7 (→`qjm`/`qjr`); fields 5,6=plain `ENUM` |
| `qiy` | `SubscribeRuntimeInfo` response | 4, sparse 4–7 | **2 separate oneofs**: oneof A (fields 4/6 → `qjh`/`qhv`), oneof B (fields 5/7 → `qji`/`qhy`) |
| `qiz` | `SetWallclock` request | 1 | plain `UINT64` (Java field `c`) — a timestamp |
| `qix` | `ReadSetting` request | 5 | single oneof, fields 1–5, all `ENUM` except field 5 (`MESSAGE`→`qjv`) — plausibly a "which setting-group, and within group-5 which sub-setting" selector, mirroring `qjc`/`qja`'s 5-group split; not confirmed |
| `qje` | `SubscribeToOobeActions` response | 1 | plain `ENUM` (Java field `c`) |
| `qig` | `EartipFitTest.SubscribeToResults` response | 2 | 2× plain `FLOAT` (Java fields `c,d`) — plausibly a per-ear fit-test score pair |
| `qin` | `HeadGesture.SubscribeToResults` response | 1 | plain `ENUM` (Java field `c`) — plausibly the detected gesture type |
| `qhz` | `Dosimeter.FetchDailySummaries` response | 5 | fields 1,3,4=`UINT32`, field 2=`MESSAGE_LIST`(→`qia`, repeated — plausibly one entry per day), field 5=`FLOAT` |
| `qir` | `Dosimeter.SubscribeToLiveDb` response | 1 (field 2 only) | plain `FLOAT` (Java field `b`) — plausibly a live dB reading |
| `qjs` | `JitterBuffer.SetJitterBufferSizePreference` request | 1 | plain `ENUM` (Java field `c`) |
| `qij` | `Multipoint.ForceMultipointSwitch` request | 2 | `INT32` + `BOOL` (Java fields `c,d`) |
| `qjp` | `Multipoint.SubscribeToQuietModeStatus` response | 1 | plain `BOOL` (Java field `c`) |

**Open questions**: none of the "plausibly" readings above are anything more than ⚪ ASSUMPTION from
field count/type/RPC-name context — none is capture-correlated. Included here only to keep this
register complete for whoever picks up the corresponding capture-correlation work next.

### Candidate rich schemas outside this pass's traced call graph (807-class sweep, unidentified)

Per this task's step 5: a header-only sweep (`field_count` only, no full field decode) was run across
**all 807** `new naa(` constructions found under
`reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/` (script:
a small header-only variant of `scripts/decode_rawmessageinfo.py`'s `decode_info_string`, not itself
committed — the logic is identical to that script's header-parsing prefix). This surfaces schemas
richer than anything traced above, whose owning RPC method/service is **not yet identified** — a
plain grep for `new <ClassName>(` and `<ClassName>.class` from other files found no direct callers for
the top 3, meaning they're most likely referenced only indirectly (e.g. via a service-catalog string
table like `fux`'s, not yet located for these). 🔴 OPEN QUESTION for all of these — no relevance
judgment is made here per `AGENTS.md` §6/ADR-017's boundary, this is a plain listing:

| Class | Field count | Oneof count | Notes |
|---|---|---|---|
| `nhm` | 76 | 0 | richest schema found in the entire sweep; no external `new nhm(`/`nhm.class` reference found |
| `nef` | 74 | 0 | same as above — no external reference found |
| `qaa` | 63 | 0 | same as above — no external reference found; `q`-prefix (like the `WriteSetting` family) but not reached from `fux`'s service catalog |
| `ndi` | 35 | 0 | — |
| `mtn` | 30 | 1 | has a oneof, like the `WriteSetting` family shapes |
| `nca` | 30 | 0 | — |
| `gdw` | 28 | 0 | — |
| `nfh` | 27 | 0 | — |
| `msw` | 26 | 1 | has a oneof |
| `qaj` | 24 | 1 | `q`-prefix, has a oneof |
| `qbu` | 24 | 0 | `q`-prefix |
| `qar` | 21 | 0 | `q`-prefix, has 1 map field (only map-typed schema in the top ~20) |

The full ranked list (top 60 by field count) is reproducible via the header-only sweep described
above; not reproduced here in full to keep this document focused — available on request. None of
these were traced to a specific `maestro_pw.*` (or other) service/method this pass; that would be the
natural next step for whoever picks this up (search for `X.class` and `X.a` reference sites the way
`qjc`/`qjb` were traced back to `fux.java`/`fxk.java` above).

---

> Template per class — copy for each new finding. **Every finding must cite
> the exact decompiled file *and line number*** (e.g.
> `reverse-engineering/apk/v1.2.3-45/jadx-output/sources/com/google/.../Xy2.java:142`),
> not only a class name — this is what `PROJECT_RULES.md` §1 rule 3 already
> requires of a `REVERSE_ENGINEERING.md` evidence source; the **Path** field
> below must always include `:line_number`, never a bare file path.

### `<package.ClassName>` (or obfuscated name, e.g. `a.b.c.Xy2`)

- **Path**: `reverse-engineering/apk/v<versionName>-<versionCode>/jadx-output/.../ClassName.java:<line_number>`
- **Readable alias**: _(e.g. "GattCallbackImpl")_
- **Role**: _(e.g. "🟢 FACT: implements `BluetoothGattCallback`, receives
  `onCharacteristicChanged` for battery updates" or "🟡 HYPOTHESIS: appears to
  serialize `AncCommand` based on field names, not yet confirmed against a
  capture")_
- **Relevant UUIDs found**: _(list, cross-reference §UUID register)_
- **Relevant message groups/codes found** (if Fast Pair Message Stream-related,
  cross-reference `PROTOCOL.md` §2.1): _(list)_
- **Relevant methods**:
  - `methodName(...):<line_number>` — _(what this method does, and why you
    think so)_
- **Hypothesis test** _(required whenever **Role** is marked 🟡 HYPOTHESIS —
  omit only for 🟢 FACT/⚪ ASSUMPTION/🔴 OPEN QUESTION entries)_: which
  action/Test-ID (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`) would need to be captured
  to confirm or refute this reading against real wire traffic — mirrors
  `PROJECT_RULES.md` §4's hypothesis-test discipline already used for
  captures, so code-derived and wire-derived evidence stay linked instead of
  building two separate, unlinked evidence trails.
- **Open questions**: _(what is still unclear)_

---

## UUID register

All UUIDs found in the APK, with status. Once a UUID's function is confirmed
via a capture, update its status here **and** promote it into `PROTOCOL.md`.

| UUID | Found in (file:line) | Suspected function | Status |
|---|---|---|---|
| `3a046f6d-24d2-7655-6534-0d7ecb759709` (byte-reversed alias: `099775cb-7e0d-3465-5576-d2246d6f043a`) | `fzd.java:9`, `gbm.java:38` | App's own log label: "default internal rfcomm socket" | 🟡 HYPOTHESIS — not yet matched against a capture's SDP record |
| `25e97ff7-24ce-4c4c-8951-f764a708f7b5` (byte-reversed alias: `b5f708a7-64f7-5189-4c4c-ce24f77fe925`) | `fzd.java:9`, `gbm.java:35` | App's own log label: "pigweed internal rfcomm socket" — SDP-confirmed (`CAP-001`/`CAP-002`/`CAP-032`) as RFCOMM server channel 1 = DLCI 0x02, AGENTS.md §6's Pigweed `pw_hdlc` channel | 🟢 FACT for channel ownership (confirmed by capture IDs `CAP-001`/`CAP-002`/`CAP-032`, `DECISIONS.md` ADR-018, `PROTOCOL.md` §2.2a); 🟡 HYPOTHESIS (strong) that Sent-direction payload content specifically carries `libmaestro`'s settings commands |
| `00001124-0000-1000-8000-00805f9b34fb` | `fxm.java:12` | Bluetooth SIG-assigned HID Profile UUID (public spec, not project-specific) — app checks for it before triggering `fetchUuidsWithSdp()` | 🟢 FACT (that this official UUID is checked for); whether the Buds actually expose it is capture-dependent — cross-reference `CAP-002`/`CAP-016` |

## Message Group / Code register (Fast Pair Message Stream)

If the Message Stream framing hypothesis (`PROTOCOL.md` §2.1) is confirmed,
use this table for vendor-specific Message Group/Code values found in the
APK, in addition to the officially documented ones.

| Group | Code | Found in (file:line) | Suspected function | Status |
|---|---|---|---|---|
| | | | | |

## Native libraries

| File | Architecture | Suspected function | Analyzed? |
|---|---|---|---|
| | | | No / Ghidra in progress / Done |

> **Updated 2026-08-30 (`DECISIONS.md` ADR-017, superseding ADR-003):** native
> `.so` disassembly is now in scope for AI *mechanical* assistance, on the
> same terms as DEX/Java-level work — an AI session may run `pbtk-from-binary`
> or disassembler tooling (Ghidra/radare2), search its output, and explain the
> syntax/structure of already-surfaced disassembly. An AI session still does
> **not** decide which disassembled function/struct is relevant, and does
> **not** decide that something becomes a recorded finding here or in
> `PROTOCOL.md` — both remain the maintainer's calls (`AGENTS.md` §6/§15,
> unaffected by ADR-017). This table tracks *what exists and what's been
> analyzed*, not a request for the AI to unilaterally decide what it means.

## Call graph notes

Build this up incrementally — start at the BLE/RFCOMM connect entry point and
work outward.

```
ClassicBTReceiver (AndroidManifest.xml:79, exported=true)
  listens for: android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED,
               android.bluetooth.action.LE_AUDIO_CONNECTION_STATE_CHANGED,
               android.bluetooth.device.action.ACL_CONNECTED / ACL_DISCONNECTED
  -> ??? (not traced this pass — next entry point to follow)

gbm.a()  [InternalRfcommSocketSelector]
  -> fzd.a / fzd.b   [InternalRfcommUuidRegistry — UUID list + normalization map]
  -> gau.a() case 3  [RfcommUuidNormalizer]
  -> gbd (via b or b2)  [InternalRfcommConnection — wraps BluetoothSocket]

fxm.i()  [MaestroSoftwareInfoAndHidUuidCheck]
  -> fxm.c(goq, goq) x4  [maestro_pw.Maestro/GetSoftwareInfo pw_rpc unary,
                          one per {MAESTRO_A,MAESTRO_B} x {LEFT_BT_CORE,RIGHT_BT_CORE}]
  -> BluetoothDevice.fetchUuidsWithSdp()  [only if HID UUID 0x1124 not yet present]
```

Not yet traced: how `ClassicBTReceiver`'s connection-state events lead into `gbm`'s socket selection,
and how `fsz`'s `WriteSetting` / `fux`'s per-service pw_rpc calls get their `MethodClient` — both are
plausible next steps for a follow-up §4 pass, not claimed here.

## Correlation status with PROTOCOL.md

Track which findings here have been cross-checked against a capture and
promoted into the protocol documentation, to avoid the same finding being
"rediscovered" independently in both documents.

| Finding (this doc) | Promoted to `PROTOCOL.md` section | Date | Capture/Finding ID |
|---|---|---|---|
| `gbm`/`fzd` — "pigweed internal rfcomm socket" UUID (`25e97ff7-...`) = RFCOMM channel 1 = DLCI 0x02 (channel ownership only, not Sent-payload content) | §2.2a "Channel ownership", §2.3 three-channel table, 2026-08-14 addendum Status line, §4.2 EQ entry | 2026-08-30 | `CAP-001`, `CAP-002`, `CAP-032`; `DECISIONS.md` ADR-018 |
| | | | |

## Known limitations of this analysis

- JADX can misdecompile some optimized/obfuscated constructs — when in doubt,
  check against the smali output from `apktool`.
- Reflection-based code can remain hidden from static analysis — dynamic
  analysis (Frida) may be needed here; log any such experiment in the
  relevant capture's `CAP-NNN-FINDINGS.md` first, per `PROJECT_RULES.md` §4.
- Protobuf field *names* recovered from JADX (via getter/setter naming) are
  not proof of the actual wire field *numbers* — field numbers, not names,
  determine binary compatibility, and must be confirmed via `pbtk` extraction
  or capture correlation before being treated as 🟢 FACT.
- A class or method being present in the APK does not prove it is actually
  exercised by the specific user actions listed in
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` — treat static findings as 🟡 HYPOTHESIS
  until correlated with a capture showing the corresponding traffic.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/REVERSE_ENGINEERING.md - https://tedsluis.github.io/opencontrolpixelbudspro2/REVERSE_ENGINEERING
