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
- **Update (2026-08-30, follow-up pass) — the "default" branch's own construction traced; found to
  build a plain `gbd`, not `fut`; that plain `gbd`'s base read/write methods are a structural match to
  DLCI 0x08's still-unidentified envelope shape.** This was found while tracing `gbb`/`gbc`/the UUID
  connection wiring behind `npv` (see the `nqx`/`npy`/`nqo`/`npw`/`nqm` entry's own Update above) —
  included here, not there, since it's squarely this entry's own open question.
  - 🟢 FACT (code existence/structure): `gbm`'s constructor (`gbm(npo npoVar, npo npoVar2, npo
    npoVar3)`, fields `a`/`b`/`c`) is called from exactly one site,
    `defpackage/fqg.java:465`: `new gbm(this.u, c8, this.x)` — i.e. `this.b` ("default", `gbm.java:39`)
    = the local `c8`, and `this.c` ("pigweed", `gbm.java:36`) = `this.x`. Tracing `c8`'s own
    definition (`fqg.java:463`): `npo c8 = nph.c(new fwy(this.r, this.v, ..., 3, ...));` — i.e. the
    "default" provider is built from **`fwy` case arm `3`** (`defpackage/fwy.java:44-46`), a
    *different* factory class from `fuu`'s case 0 (which builds `fut`, the "pigweed" branch, per the
    already-documented chain: `frg.java` cases 7–12 → `fut.f` → HDLC). `fwy` case 3's body:
    `return new gbd(((gbc) this.c).a(), ((gbn) this.e).a(), fux.b(), (ExecutorService)
    npoVar2.a(), (gos) this.a.a(), (ftk) this.b.a());` — **a bare `new gbd(...)`, not `new fut(...)`**.
    Since `fut` is confirmed the *only* class extending `gbd` anywhere in this APK (per the prior
    pass's `grep -rl "extends gbd" .`), this plain `gbd` instance runs with **no overridden behavior**
    — it uses `gbd`'s own base-class `c()` (read loop) and `d(int, int, byte[])` (write) methods
    directly, not `fut`'s HDLC-specific overrides. Notably, this "default" branch's `gbd` is
    constructed with the **same `gbn` instance** as an argument (`((gbn) this.e).a()`,
    `fwy.java:46`) — since `gbn` has only one construction site total in the app
    (`defpackage/fqg.java:623`), this is very likely the identical singleton object also feeding
    `fut`'s construction, i.e. **both the "default" and "pigweed" branches are wired through the same
    UUID-discovery/`fzd`-registry chain** — the two branches differ in which wire-protocol wrapper
    object (`fut` vs. plain `gbd`) is attached, not in an independent UUID-matching mechanism each.
    (This last point about `gbn` instance identity was not independently confirmed against the
    surrounding `fqg.java` constructor's full field-reuse pattern — a giant, heavily R8-merged
    generated-DI class where field slots are visibly reused across unrelated construction phases, so
    it is stated here as a strong but not fully certain reading, not confirmed FACT.)
  - **The structural match, found on reading `gbd`'s own base methods (already partly transcribed in
    this doc's own `gbd` entry above, re-examined here for this specific comparison)**: `gbd.c()`
    (read loop) does `int readUnsignedByte = this.r.readUnsignedByte(); byte readByte =
    this.r.readByte(); short readShort = this.r.readShort(); ... mxr t = mxr.t(bArr, 0, readShort);`
    — i.e. **1-byte + 1-byte + 2-byte-length + value**. `gbd.d(int i, int i2, byte[] bArr)` (write)
    mirrors it exactly: `this.q.writeByte(i); this.q.writeByte(i2); this.q.writeShort(length);
    this.q.write(bArr);`. Both use plain `java.io.DataInputStream`/`DataOutputStream`
    (`gbd.java:4-5`, confirmed stock JDK classes, no custom byte-order override), whose `readShort`/
    `writeShort` are **big-endian by Java Language Specification**. **This is a structural,
    field-for-field match to `PROTOCOL.md` §2.1/§2.3's already-documented DLCI 0x08 envelope shape,
    `[Group:1][Code:1][Length:2B-BE][Value]`** (`CAP-001-FINDINGS.md` §2, `CAP-004-FINDINGS.md` §5a).
  - **🟡 HYPOTHESIS (strong, code-level structural match only — not capture-cross-checked with a real
    UUID observed on DLCI 0x08's own wire traffic):** `gbm`'s "default internal rfcomm socket" branch
    (UUID `3a046f6d-24d2-7655-6534-0d7ecb759709` / `099775cb-...`, via the plain `gbd` built in
    `fwy.java` case 3) is a strong candidate for the code-level owner of `PROTOCOL.md` §2.3's
    still-unidentified DLCI 0x08 channel — the "third, still-unidentified private
    `[Group][Code][Length][Value]` envelope" that document's own table already flags as structurally
    similar to §2.1's Fast Pair shape but with its own Group/Code numbering. This is consistent with,
    and offers one plausible explanation for, this same entry's own already-recorded "exhaustive
    negative result" above (the `3a046f6d-.../099775cb-...` UUID has never been observed in any
    capture's SDP records) — if this reading is correct, that non-observation would simply mean none
    of this project's captures happened to include the exact moment the app performs SDP discovery
    while this "default" service is being advertised/queried, not that the UUID/socket doesn't exist.
    **This is not itself capture evidence and does not, by itself, resolve §2.3's open question** — it
    is a code-level structural lead pointing at *which app-side code path* to look for on the far
    (transport) side if/when a capture does show DLCI 0x08's SDP UUID; per `AGENTS.md` §6, an AI agent
    does not promote this in `PROTOCOL.md` — it is recorded here as a candidate for the maintainer.
  - **Hypothesis test**: a fresh capture that (a) captures the full SDP Service Search Attribute
    Response/Protocol Descriptor List exchange (not just post-connection RFCOMM traffic, which is
    what most existing captures focus on), and (b) opens DLCI 0x08, would let two things be checked
    directly: whether `3a046f6d-24d2-7655-6534-0d7ecb759709` (or its byte-reversed form) appears in
    the SDP response and resolves to the same RFCOMM channel number DLCI 0x08 is observed on
    (mirroring exactly the methodology this entry's own Hypothesis test already used to confirm the
    "pigweed" UUID → DLCI 0x02, above) — a positive match would be strong, direct wire-level
    confirmation of this pass's code-level reading.
  - **Update (2026-08-30, Tier 0 re-decode task) — a narrower, cheaper hypothesis test than the SDP-UUID
    one above run and confirmed: the `[Length:2B]` field's byte order is empirically big-endian, not
    just "consistent with `gbd`'s `DataInputStream`/`DataOutputStream` usage" as this entry stated
    structurally.** 🟢 FACT (empirical, re-extracted from the raw logs, not merely re-read from a prior
    finding): every non-zero-length DLCI 0x08 frame in `CAP-001-btsnoop_hci.log` (9 frames,
    `tshark -r CAP-001-btsnoop_hci.log -Y "btrfcomm.dlci==0x08 and btrfcomm.len>0" -T fields -e
    frame.number -e data.data`) and `CAP-004-btsnoop_hci.log` (5 complete, non-fragmented frames, same
    filter) was checked by comparing the 2-byte Length field interpreted as big-endian vs.
    little-endian against the frame's own actual trailing byte count. **14/14 matched big-endian
    exactly; 0/14 matched little-endian** (e.g. `CAP-001` frame 1089, `05 0a 00 0d <13 bytes>`: BE
    reading of `00 0d` = 13 = the actual value length; LE reading = 3328, nowhere close). This is a
    direct, cross-capture (2 independent sessions) empirical confirmation of the byte-order half of
    this entry's own structural claim (`gbd.c()`/`d()` using `DataInputStream.readShort()`/
    `DataOutputStream.writeShort()`, which are big-endian per the Java Language Specification) — it
    does **not** by itself confirm the SDP-UUID attribution (the Hypothesis test immediately above,
    which remains untested), only the framing-mechanism-level match already asserted. See
    `CAP-004-FINDINGS.md`'s 2026-08-30 addendum for the full per-frame table and reproduction script
    (added there rather than here, since that file is this project's existing authority for DLCI
    0x08's envelope decode).

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

### `defpackage.fua` / `defpackage.gax` / `defpackage.gbo` / `defpackage.gba` / `defpackage.hjy` — Group-numbered `SparseArray` router on `gbd` (DLCI 0x02), and its per-device-variant handler registration

> Added 2026-08-30, maintainer-approved write-up of a candidate cluster first surfaced during an
> independent verification of a now-deleted, unofficial `REVIEW_REPORT.md`. That document's central
> technical claim — that this router says something about DLCI 0x04 (the official Fast Pair Message
> Stream) being GMS-exclusive — is a
> **channel conflation** and is explicitly not repeated here: `fua` sends via `gbd`
> (`InternalRfcommConnection`, entry above), which `DECISIONS.md` ADR-018 already confirms wraps
> **DLCI 0x02** (the companion app's own "pigweed"/Maestro internal socket), not DLCI 0x04 at all.
> Nothing below concerns DLCI 0x04.
>
> **Second occurrence (2026-08-30, same day):** an independently-submitted cross-validation report
> (Antigravity/Gemini 3.1 Pro), reviewed the same day as the write-up above, reproduced this
> identical conflation over the identical file set, plus an additional, likewise-unsupported
> "battery handler" framing for `gba`/`gaa` — see the dated update at the end of this entry.

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/fua.java`
  (class declaration; methods `l()`/`m()`/`o()`)
- **Readable alias**: MaestroGroupRouter
- **Role**: 🟢 FACT (code exists and does this): implements `gbk, ftm`. Holds a `SparseArray` keyed by
  integer Group ID (`this.m.put(i, fzyVar)` to register a handler, `(fzy) this.m.get(i)` to dispatch
  an inbound message) and sends outbound messages via `gbdVar.d(i3, i2, bArr)` — i.e. through `gbd`'s
  own `d(int, int, byte[])` write method (`[Group:1][Code:1][Length:2][Value]`, see `gbd`'s entry
  above), on the DLCI 0x02 connection `gbm` already selected. 🟡 HYPOTHESIS: this is the concrete
  `ftm`-interface router implementation the `gbm`/`fzd` cluster's Group-numbered dispatch was
  inferred to need but hadn't been located until this pass.
- **Relevant message groups/codes found**: registers whichever Group IDs its callers pass in (see
  `gax`/`gbo` below) — not itself a fixed list.
- **Hypothesis test**: not yet run against a capture — no DLCI 0x02 payload has been decoded to the
  point of reading its own leading Group byte against this router's registered set (§2.2a's own
  "Sent"-direction content remains opaque).
- **Open questions**: how an inbound DLCI 0x02 payload's Group byte is actually extracted before
  reaching `this.m.get(i)` — not traced this pass.

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/gax.java:20-26`
- **Readable alias**: PrestoGroupHandlerRegistration
- **Role**: 🟢 FACT: logs `"Registering handlers for Presto device: %s"` and calls
  `ftmVar.h(135, this.c); ftmVar.h(136, this.d); ftmVar.h(130, this.e);` — registering handlers for
  Group `130`/`135`/`136` (decimal) against a `fua`-shaped router, for a device variant internally
  labeled "Presto."
- **Open questions**: 🔴 whether "Presto" refers to the Pixel Buds Pro 2 specifically is
  **unconfirmed, not settled** — this project's own memory/`CHANGELOG.md` 2026-08-30 entry already
  flags a *different* internal codename, "presto" (via `qjn`/`gaa.java`), as very likely an
  **alternate product's** settings schema, not the Buds Pro 2's own. Whether `gax`'s "Presto" is the
  same product-labeling scheme as `qjn`'s is not checked this pass. Groups 130/135/136 are not yet
  cross-checked against any capture.

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/gbo.java:15-20`
- **Readable alias**: StrettoGroupHandlerRegistration
- **Role**: 🟢 FACT: same pattern as `gax`, logging `"Registering handlers for Stretto device"` and
  registering Groups `135`/`136`/`5` for a device variant labeled "Stretto." Not cross-checked
  against any capture.

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/gba.java`
  (class declaration, `implements fzy`; method `b(int, mxr)`)
- **Readable alias**: GroupHandlerCandidate130
- **Role**: ⚪ ASSUMPTION only — JADX reports `b(int, mxr)`'s method body as "Method not decompiled."
  Per `APK_REVERSE_ENGINEERING_PROCEDURE.md` §6's JADX-misdecompile note, this needs an `apktool`
  smali-output fallback read before any content claim; not attempted this pass.
- **Open questions**: full handler body content — blocked on the smali fallback above.

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/hjy.java:1-40`
- **Readable alias**: ScheduledTimeoutHandler (tentative)
- **Role**: 🟢 FACT: implements `fzy`; owns its own `ScheduledExecutorService`/`ScheduledFuture` with
  a 10000ms schedule, in a context that looks OTA-timeout-related. 🔴 **Explicitly not established**:
  that this is *the only* scheduler used by any battery-related handler — `ScheduledExecutorService`
  appears in roughly 78 files project-wide, so a claim like "the only `ScheduledExecutorService` used
  by the battery-Group handlers is `hjy`, for OTA" (as the now-deleted `REVIEW_REPORT.md` asserted) is
  not supported by this alone and should not be repeated without a dedicated scope read.
- **Open questions**: what Group(s), if any, actually route to this class via `fua`'s router; full
  relevance read not done this pass.

- **Update (2026-08-30, second cross-validation pass, independent full-tree re-check on `gba`/`gaa`/
  `hjy`):** the same external report referenced in this entry's header note named `gba.java` (Group
  `130`) and `gaa.java` (Group `135`) as "the battery handlers" and concluded, from those two files
  plus `hjy.java`, that no battery-polling loop exists in this cluster. Re-checked this pass,
  independently:
  - **The "battery handler" premise itself is unsupported.** `gba.java`'s only method body remains
    undecompiled (see its own entry above — no claim about its content is possible either way).
    `gaa.java` has **zero** `battery`/`Battery` string references anywhere in its 1068 lines
    (`grep -n "battery\|Battery" gaa.java` → no hits), and its own independently-documented role (see
    the `qhx`/`qjn`/`qjt` entry below) is a **settings-response notification dispatcher** (OOBE mode,
    the "presto"-labeled possibly-different-product settings groups) — nothing ties it to battery
    telemetry.
  - **Full-tree grep (`jadx-output/sources/`, not a sample) for the two Android battery
    intent/action strings the external report cited**: `ACTION_BATTERY_LEVEL_CHANGED` → 0 matches
    project-wide; `ACTION_VENDOR_SPECIFIC_HEADSET_EVENT` → 0 matches project-wide. This specific
    narrow claim is confirmed true, independently of the unsupported "battery handler" framing
    around it.
  - **`hjy.java`'s scope reconfirmed as OTA-only, not general battery telemetry**: its
    `ScheduledExecutorService`-driven timeout (`hjy.java:30`, 10000ms) and its logic (`hjy.java:33-
    65`) reference `"Attempt manual OTA"`, `OTA_ERROR_BATTERY_LOW`, `OTA_ERROR_NOT_DOCKED`,
    `OTA_SUCCESS` — "battery low" appears only as one *OTA precondition failure reason*, not as a
    battery-telemetry channel.
  - **This project's own already-🟢-FACT battery-push mechanism (`PROTOCOL.md` §4.3 Option E, DLCI
    0x08's private envelope, `Group 0x0e Code 0x01`/`Group 0x04 Code 0x03`) is a different channel
    entirely from this DLCI-0x02 `fua`/`gbd` cluster** — so even where the report's high-level claim
    ("battery updates are event-driven, not app-polled") happens to be directionally consistent with
    what this project has independently confirmed by capture, it is not actually supported by the
    APK evidence the report presents.

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
- **Update (2026-08-30, follow-up pass) — the `pw_rpc.RpcPacket`-shaped class this reconciliation
  needed has now been located and decoded; the 3-level nesting reading is structurally confirmed at
  the code level (still not capture-correlated).** 🟢 FACT (code existence/structure, via
  `scripts/decode_rawmessageinfo.py`): `defpackage/nqx.java` decodes to exactly 7 fields, numbers 1–7,
  matching the standard upstream Pigweed `pw_rpc/internal/packet.proto` `RpcPacket` message
  field-for-field:

  | Proto field | `nqx`'s type (decoder output) | Java field | Standard `pw_rpc.RpcPacket` field |
  |---|---|---|---|
  | 1 | ENUM | `b` | `type` |
  | 2 | UINT32 | `c` | `channel_id` |
  | 3 | FIXED32 | `d` | `service_id` |
  | 4 | FIXED32 | `e` | `method_id` |
  | 5 | BYTES | `f` | `payload` |
  | 6 | UINT32 | `g` | `status` |
  | 7 | UINT32 | `h` | `call_id` |

  This is not just a shape match — the class that actually *populates* `nqx` was also traced and its
  field assignments match this table exactly: `defpackage/npy.java`, method `a(int, nqm, BiFunction,
  mzq)` (`npy.java:34-80`), builds an `nqx` and sets `nqxVar.b = a.at(2)` (field 1/`type`, from a
  channel-ID-adjacent int — see caveat below), `.c = npwVar.a` (field 2/`channel_id`, taken directly
  from `defpackage/npw.java`'s own `a` field — `npw`'s constructor rejects `a <= 0` with the message
  `"The channel ID must be positive"`, i.e. `npw` is itself Pigweed's `Channel` class), `.d`/`.e` from
  `nqpVar.a().b`/`nqpVar.b.a()` (fields 3/4, `service_id`/`method_id` — both ultimately derived from
  `mne.E(<fully-qualified name string>)`, a string-hash function, exactly matching upstream `pw_rpc`'s
  own name-hashing scheme for service/method IDs, not sequential integers), and — the critical field
  for this reconciliation — **`.f = mzqVar.d()`** (`npy.java:67-73`, only when `mzqVar != null`) where
  `mzqVar` is the outgoing protobuf request message itself (`qjc` for `WriteSetting`) and `.d()`
  serializes it to bytes. **This directly confirms field 5 (`payload`, `BYTES`) is populated with the
  serialized `qjc` message** — i.e. the full nesting is confirmed at the code level exactly as this
  entry's Open Questions originally speculated:
  `RpcPacket.field5(payload, BYTES) → qjc(deserialized).field4(qhr) → qhr.fieldN`. The chain
  `fyv.a(qjc)` (`fyv.java:44-49`) → `esk`'s `nqoVar.e(qjcVar)` (already documented) → `nqo.e(mzq)`
  (`nqo.java:64-67`, `c(nql.UNARY); return (nqe) b(new nqn(), mzqVar);`) → `nqo.b(...)`
  (`nqo.java:34-40`) → `((npy) this.b).a(...)` was traced end-to-end this pass and is consistent
  throughout — `nqo` is `pw_rpc.MethodClient` (its own constructor `nqo(npy, int, nqm, nqu)` at
  `nqo.java:94-99` takes exactly a `Client`, a channel id, a `Method` descriptor, and call options,
  matching that class's real API shape) and `npy` is `pw_rpc.Client`.
  - **One caveat, not fully resolved**: field 1/`type`'s value comes from `a.at(2)`
    (`defpackage/a.java:842-844`: `i == 1 ? myy.b() : i - 2`), a generic, widely-reused protobuf-lite
    enum ordinal-to-wire-value helper (the same three-line body appears as `a.as`/`a.at`/etc. for many
    unrelated enum types across this codebase) — for input `2` this evaluates to `0`. Whether wire
    value `0` is the correct `PacketType.REQUEST` (or similar) enum number for this specific call, or
    whether this generic helper is being applied to the wrong enum type by this reading, was **not**
    independently verified this pass (would require locating the actual `PacketType`-equivalent enum
    class and confirming its declared values) — this one field's exact value is left open; the
    field-position/field-5-payload finding above does not depend on it.
  - **What this confirms vs. does not**: confirms, at the code level, that `qjc`/`qhr`'s 3-level
    nesting reading is structurally real (not merely plausible) — the app's own RPC-send code
    genuinely wraps a serialized `qjc` inside `RpcPacket.payload` (field 5). It does **not** confirm
    this against real wire bytes from a capture — the `Hypothesis test` above (redecoding a DLCI 0x02
    Sent frame's raw hex against this 3-level structure) is unchanged and still the needed next step
    for a capture-level confirmation; this update only removes the "no `pw_rpc.RpcPacket`-shaped class
    has been located" caveat that previously blocked even attempting that redecode.
- **Hypothesis test — result (2026-08-30, Tier 0 re-decode task, byte-level, capture-correlated):**
  🟢 FACT (mechanical byte decode against the confirmed structures above, independently re-extracted
  from the raw `.log` files, not merely re-read from a prior `CAP-NNN-FINDINGS.md`): two existing
  DLCI 0x02 Sent frames already identified as `field5{field4{...}}` in `CAP-020-FINDINGS.md` §3/§4
  (`CAP-020`, `TOUCH-001`/`HEAD-001`) were re-pulled directly from `CAP-020-btsnoop_hci.log` via
  `tshark -r CAP-020-btsnoop_hci.log -Y "btrfcomm.dlci==0x02 and frame.number==1741" -T fields -e
  data.data` (frame 1741: `7e004b0310151dea71de7d5e251d9a8c9e2a0422022001c5a08a3c7e`; frame 1935:
  `7e004b0310151dea71de7d5e251d9a8c9e2a052203e801020641623b7e` — byte-identical to the file's own
  citation), HDLC-unescaped/CRC-verified per `PROTOCOL.md` §2.2a, and the inner "..." bytes (after
  the 13-byte prefix, `field5`, `field4`) decomposed one level further as standard protobuf wire-format
  tags. **Frame 1741** (`TOUCH-001`): inner bytes `20 01` decode to tag `0x20` = field **4**, wiretype
  0 (varint), value `1` — an exact match, at the tag-byte level, to `qhr`'s own field 4 (BOOL,
  "Head/touch gestures master enable toggle" per this document's own `qhr` field-register table,
  write site `fyo.java:124-144`). **Frame 1935** (`HEAD-001`): inner bytes `e8 01 02` decode to a
  2-byte LEB128 tag `0xe8 0x01` = `232` = field **29**, wiretype 0 (varint), value `2` — matching
  `PROTOCOL.md` §4.5.4's existing wire-derived HYPOTHESIS ("field 29 = Head gestures") and confirming,
  for the first time at the byte level, that this document's own `qhr` field-register table's "field
  29 | ENUM | not found [write site]" entry was a gap in the mechanical call-site search, not a sign
  the field is unwritten — a real `Sent` frame does populate it, with a plain 1-byte varint value (`2`)
  consistent with a small-ordinal `ENUM`. This is a direct confirmation of the reconciliation this
  entry's Open Questions section proposed: the wire's "..." decomposes exactly as `qhr`'s own oneof
  tag structure, field-number-for-field-number, not merely "plausible." Reproducible directly from the
  hex above plus `PROTOCOL.md` §2.2a's published unescape/CRC method and standard protobuf wire-format
  tag decoding (`field = tag>>3`, `wiretype = tag&7`) — no APK-derived script needed. See
  `CAP-020-FINDINGS.md`'s 2026-08-30 addendum for the full capture-side write-up, including the
  reproduction script. **Promoted to `PROTOCOL.md` §2.2a as 🟢 FACT, 2026-08-30, maintainer sign-off
  (`DECISIONS.md` ADR-019)** — scoped exactly as stated above (2 sampled fields, not full-schema
  coverage).

### `defpackage.nqx` / `defpackage.npy` / `defpackage.nqo` / `defpackage.npw` / `defpackage.nqm` — Pigweed `pw_rpc` wire-packet & client plumbing (`nqx` = `RpcPacket`)

- **Path**: `nqx.java:1-52`, `npy.java:1-133`, `nqo.java:1-114`, `npw.java:1-67`, `nqm.java:1-53`, all
  `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/`
- **Readable alias**: `nqx`=RpcPacket, `npy`=Client, `nqo`=MethodClient, `npw`=Channel, `nqm`=Method
  (descriptor)
- **Role**: 🟢 FACT (code existence/structure, `nqx`'s shape via `scripts/decode_rawmessageinfo.py`;
  the rest via direct reading and call-graph tracing) — found this pass while pursuing Goal 2 of a
  2026-08-30 follow-up task (locate the `pw_rpc` packet envelope class). `dev.pigweed.pw_rpc` does
  **not** survive as an actual package/class path in this APK (`find . -path '*/dev/pigweed/*'` only
  turns up `dev/pigweed/pw_tokenizer/Detokenizer.java` — real, unobfuscated); the only prior evidence
  of `pw_rpc.MethodClient`'s existence was a surviving Kotlin function-reference metadata string in
  `fsz.java:223` (`"getWriteSettingMethodClient(...)Ldev/pigweed/pw_rpc/MethodClient;"`), naming the
  return type of a lambda but not the obfuscated class itself. This pass traced the actual call chain
  from that lambda's real target (`fsz.java` case 2, `fsz.java:65-76`: `npy.f(int, "maestro_pw.Maestro",
  "WriteSetting")` → `nqo`) down through the real send path and identified all 5 classes:
  - **`nqx` = `pw_rpc.RpcPacket`** — see the `qjc`/`qja` entry above (Update, 2026-08-30) for the full
    field-by-field decode and the field-5/`payload` confirmation; not repeated here.
  - **`npy` = `pw_rpc.Client`**: holds the channel/service table (`npy.java:20-24`) and exposes
    `f(int channelId, String service, String method)` (`:130-132`) → `e(int, int, int)` (`:116-128`,
    hashes the service/method name strings via `mne.E(...)` and looks up/creates an `nqo`) and
    `a(int, nqm, BiFunction, mzq)` (`:34-80`, the method that actually builds and populates the `nqx`
    `RpcPacket`, per the `qjc`/`qja` entry's Update above, then hands the serialized bytes to
    `npwVar.a(...)` — the `Channel`'s own send method).
  - **`nqo` = `pw_rpc.MethodClient`**: its 4-argument constructor `nqo(npy, int, nqm, nqu)`
    (`nqo.java:94-99`) takes exactly `(Client, channelId, Method, defaultCallOptions)`, matching the
    real `MethodClient` API shape. `nqo.e(mzq)` (`:64-67`) is the confirmed unary-RPC-invoke method
    (`c(nql.UNARY)` guards against invoking a non-unary method this way, matching `pw_rpc`'s own
    "invoked as the wrong RPC type" runtime check, `nqo.java:42-48`'s exact error message wording is
    itself a close paraphrase of upstream Pigweed's own client-side error text) — this is the method
    `esk.java`'s already-documented `nqoVar.e(qjcVar)` call site (case 19) actually calls.
  - **`npw` = `pw_rpc.Channel`**: `npw(int i, npv npvVar)` (`npw.java:14-20`) throws
    `IllegalArgumentException` with the message `"The channel ID must be positive: %d is invalid"` if
    `i <= 0` — a direct, distinctive match to Pigweed's own `Channel` constructor validation. Field
    `a` = channel ID (feeds `nqx`/`RpcPacket` field 2), field `b` = the channel's output interface
    (`npv`, not traced further this pass — presumably Pigweed's `ChannelOutput`, the interface a
    transport like this app's RFCOMM/HDLC socket write would implement).
  - **`nqm` = `pw_rpc.Method` (descriptor)**: holds a service reference (`a`, type `nqs` — presumably
    `pw_rpc`'s `Service` descriptor, not independently traced this pass), a method-name string (`b`),
    and an `nql` (method-kind enum — `nql.UNARY`/`nql.SERVER_STREAMING` are both referenced from
    `nqo.java`, matching `pw_rpc`'s own `MethodType` enum). Its `a()` method (`nqm.java:20-22`) returns
    `mne.E(this.b)` — a string-hash of the method name — confirming `service_id`/`method_id` in `nqx`
    are Pigweed's standard 32-bit name-hash IDs, not sequential integers, exactly matching upstream
    `pw_rpc`'s actual ID-assignment scheme (`pw_rpc::internal::Hash()`).
- **What this does NOT establish**: `npv`/`nqs`/`nql`/`nqu`/`nqp`/`mne.E`'s own internals were referenced
  but not independently opened and read this pass (out of scope for Goal 2, which only asked to locate
  and decode the packet envelope itself); the exact numeric value Pigweed's `type` field takes for a
  `WriteSetting` request (`a.at(2)` → `0`, see the `qjc`/`qja` entry's caveat above) was not
  independently verified against a named `PacketType`-equivalent enum.
- **Open questions**: what `npv` (Channel output interface) is actually implemented by for this app's
  RFCOMM/HDLC transport — tracing that would be the natural next step to close the loop from "app code
  builds an `RpcPacket`" to "bytes actually observed on DLCI 0x02", but was not attempted this pass.
- **Update (2026-08-30, follow-up pass) — `npv`'s implementation traced end-to-end; the loop from
  "app code builds an `RpcPacket`" to "bytes on the wire" is now closed, and independently
  corroborates `PROTOCOL.md` §2.2a's wire-derived HDLC framing byte-for-byte, from the opposite
  (encode-side) evidence direction.** 🟢 FACT (code existence/structure and call-chain tracing; the
  framing-mechanism *match itself* is a direct structural comparison, not a semantic claim):
  - **`defpackage/npv.java`** is confirmed a single-method interface: `void a(byte[] bArr)`, matching
    `npw.a(byte[])`'s delegation (`npw.java:23-25`).
  - **All 6 real `npv` implementations** (`grep -rn "new npv" .` finds none tagged `implements npv`
    as a named class — all 6 are anonymous inner classes, JADX-merged under the label `fui`) live in
    `defpackage/frg.java`, `a()` method `case`s 7–12 (`frg.java:47-238`). Each constructs an `npw`
    (Channel) for one `(goq route target) × (goq.MAESTRO_A | goq.MAESTRO_B core)` pair — `CASE`,
    `LEFT_BT_CORE`, `RIGHT_BT_CORE` under `MAESTRO_A` (cases 7–9) and the same three under
    `MAESTRO_B` (cases 10–12) — i.e. **6 distinct pw_rpc `Channel`s, one per (route target, core)
    combination**, all using `goq` (already-documented `MaestroRouteTarget` enum). Every one of the 6
    anonymous `npv.a(byte[])` bodies is byte-identical in structure: a `switch` on a captured constant
    `int` (0–5, one per case) that calls **`((fut) b.a()).f(bArr, goq.<TARGET>)`** — i.e. all 6
    channels funnel through the *same* class, `fut`, differing only in which `goq` target constant
    they pass.
  - **`defpackage/fut.java`** (`class fut extends gbd implements fwv`) is confirmed as **the only
    class in this APK extending `gbd`** (`grep -rl "extends gbd" .` → `fut.java` alone) — i.e. `fut`
    is not one of several sibling RFCOMM-connection implementations, it is *the* concrete
    implementation of the already-documented `gbd`/`InternalRfcommConnection` abstract base for this
    app version. Its `d(int, int, byte[])` override (`fut.java`, `@Override // defpackage.gbd`)
    unconditionally throws `new IOException("Unsupported legacy communication style.")` — i.e. `gbd`
    itself supports (or once supported) more than one wire style, but `fut` only implements the
    HDLC-based one.
  - **`fut.f(byte[] bArr, goq goqVar)`** is a byte-for-byte code-level match to `PROTOCOL.md` §2.2a's
    already-wire-confirmed HDLC framing, evidenced line-by-line:
    1. Writes literal byte `126` (`0x7E`) — the opening HDLC flag, matching §2.2a exactly.
    2. Builds `j = ((a2 & 15) << 6) | ((a3 & 15) << 10)` — combining the local channel's own `goq`
       address (`a2`, read from `this.x`, the same `oql`/`Set<goq>` built in the constructor from
       `nmxVar2.a()`, i.e. the *other* end of the 6-channel table) and the target's `goq` address
       (`a3 = goqVar.a()`) — then LEB128-varint-encodes `j` (the `while (j2 >>>= 7) { i2++; }` counting
       loop, then the `while (j3 = j >>> 7; ...) { allocate.put(...) }` encode loop with the standard
       "continuation bit on all but the last byte" pattern) — a direct implementation of §2.2a's
       already-confirmed "HDLC Address field, LEB128-varint-encoded (1–3+ bytes)".
    3. Writes a single Control byte, value `3` (`0x03`) — the standard HDLC "UI" (Unnumbered
       Information) frame type, matching §2.2a's "single Control byte" and the `pbpctrl`-cited
       "wrapped in ... U-frames" description.
    4. Writes the payload bytes (the `RpcPacket`-serialized `bArr` argument) via
       `gvx.q(wrap, crc32, byteArrayOutputStream)`.
    5. Computes and writes a 4-byte little-endian CRC-32 (`bArr3[0]=(byte)crc32.getValue();
       bArr3[1..3]=...>>8/>>16/>>24`) over the unescaped Address+Control+Data — an exact match to
       §2.2a's "CRC-32 (IEEE 802.3/zlib polynomial, little-endian byte order)".
    6. Writes the closing flag byte `126` (`0x7E`).
    - **The escape/byte-stuffing step** (`defpackage/gvx.java:332-344`, static method `q(ByteBuffer,
      CRC32, OutputStream)`, called from all 3 write steps above) checks each byte: if it equals `126`
      (`0x7E`) it writes `gop.a`; if `125` (`0x7D`) it writes `gop.b`; otherwise the raw byte —
      accumulating every byte (pre-escape) into the running CRC. `defpackage/gop.java:6-7` defines
      `a = {125, 94}` (`{0x7D, 0x5E}`) and `b = {125, 93}` (`{0x7D, 0x5D}`) — i.e. `0x7E` is escaped
      as `0x7D 0x5E` and `0x7D` as `0x7D 0x5D`, the exact byte values §2.2a's own unescape formula
      (`X` transmitted as `0x7D (X XOR 0x20)`) predicts (`0x7E XOR 0x20 = 0x5E`,
      `0x7D XOR 0x20 = 0x5D`) — confirmed, not just consistent.
    - **The final socket write**: `fut.f` writes the fully-framed `ByteArrayOutputStream` to
      `this.u.j()` (`fut.java`, inherited `u` field from `gbd`, type `ffd`).
      `defpackage/ffd.java:374-375`'s `j()` method returns
      `((BluetoothSocket) this.a).getOutputStream()` — **the literal Android `BluetoothSocket`'s own
      output stream**, i.e. this is the actual, final RFCOMM socket write, with no further
      abstraction layers between it and the OS Bluetooth stack.
  - **Full confirmed send chain, code-traced end-to-end this pass**: UI/gesture trigger → `fye.a(qhs)`
    or equivalent (builds `qjc`) → `fyv.c`/`fyv.a(qjc)` → `esk` → `nqo.e(qjc)` (`MethodClient.invoke`)
    → `npy.a(...)` (`Client`, builds & serializes the `nqx`/`RpcPacket`, `payload`=serialized `qjc`)
    → `npw.a(bytes)` (`Channel`) → `npv.a(bytes)` (one of `frg.java`'s 6 anonymous implementations) →
    `fut.f(bytes, goq)` (HDLC-encode: flag + LEB128 address + control + payload + CRC-32 + flag, with
    `0x7D`-escaping) → `ffd.j()` = `BluetoothSocket.getOutputStream()`.
  - **What this does NOT establish**: this is a **code-level** confirmation that the app's *encoder*
    implements the same algorithm §2.2a already confirmed from *decoding* real captured bytes — it is
    strong, independent corroboration from the opposite evidence direction, comparable in kind to how
    this project already treats two-independent-path agreement elsewhere, but it does not itself
    constitute a new capture and changes nothing about §2.2a's already-🟢-FACT status (this update
    does not need to promote anything — the target was already FACT). It also does not independently
    confirm *which* of the 6 `(route target × core)` channels corresponds to the DLCI 0x02 socket
    already tied to SDP UUID `25e97ff7-24ce-4c4c-8951-f764a708f7b5` in ADR-018 — `fut` being the sole
    `gbd` subclass strongly suggests all 6 channels multiplex over that one socket (one RFCOMM
    connection, many logical `pw_rpc` channels distinguished by the Address field's `goq`-derived
    value), consistent with §2.2a's own note that "two distinct address values are observed... not a
    fixed/exhaustive set" and `DESKRESEARCH_FINDINGS.md` §6's `0x1e80`/`0x2680`/`0xe980` additional
    values — but this reading (one socket, N logical channels multiplexed via the Address field) was
    not independently checked against `gbb`/`gbc`/`fuu.java`'s own socket-selection wiring this pass
    (`fuu.java:37-60`, the factory that constructs `fut`, was opened but not traced further).
- **Open questions (remaining)**: whether `gbb`/`gbc` (the objects `fuu.java`'s factory passes into
  `fut`'s constructor) tie specifically to the one already-documented Pigweed-internal-socket UUID
  (ADR-018), or whether a different/second `gbd`-based construction path exists for some other
  socket — not traced this pass; what `npv`'s address-combination formula's `a2`/local-address side
  represents concretely (which `goq` value the app itself uses as its own end of the channel, i.e.
  is it always `goq.HOST` or does it vary) was read but not independently interpreted.
- **Update (2026-08-30, follow-up pass) — `gbb`/`gbc` confirmed NOT UUID-carrying; the actual
  UUID-aware connection step traced to a shared `gbn` singleton; a second, previously-unexamined
  `gbd`-construction path found along the way, with a significant unplanned finding for DLCI 0x08
  (see the extended `gbm` entry below for the full write-up — not repeated here in full to respect
  this doc's non-destructive-update/non-duplication convention).** 🟢 FACT (code
  existence/structure): `defpackage/gbb.java` is a plain wrapper around `Optional<BluetoothDevice>`
  (`a()` returns the device's MAC address, `b()` its reflection-based `isConnected()` state) — it
  carries **no** UUID or socket-selection information at all, ruling out the original guess that
  `gbb`/`gbc` themselves might encode which socket/UUID is in play. The actual UUID-driven step is
  one constructor argument further along: `fut`'s (and, newly found this pass, the base `gbd`'s own)
  `oql`/`this.a` constructor argument comes from `gbn.a()` (`defpackage/gbn.java:14-19`), a class with
  exactly **one** construction site in the whole decompiled tree (`defpackage/fqg.java:623`, i.e. a
  singleton in this app's dependency graph), whose `a()` method builds its connection-observable by
  filtering through `fzd.a` — the same UUID list already documented in the `gbm`/`fzd` entries below —
  via `gau`'s already-documented `case 3` normalizer. See the `gbm` entry below for what this pass
  found about the *other* branch's construction (`fwy.java` case 3) and its structural match to DLCI
  0x08's envelope shape.

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

- **Update (2026-08-30, follow-up pass) — field 9 vs. 10 vs. 13 resolved for the *write* direction;
  9/10 confirmed present-but-unhandled on the *read* direction.** 🟢 FACT (code existence/call-site
  tracing across the full decompiled tree, not a wire capture): every `qhr.a.k()` builder-construction
  call site in this APK version (`grep -rl "qhr\.a\.k()" .` → `fye.java`, `fyo.java`, `fyp.java`,
  `hnz.java`, `hgj.java`, plus one further site in `fxf.java` — 6 files, all individually read this
  pass) was enumerated, and **field 13 is the only one of the three `ANC_STATE`-typed oneof cases (9,
  10, 13) ever populated by a `WriteSetting` request anywhere in this app version's code.** No call
  site anywhere sets `qhr.b` to `9` or `10`.
  - **Field 13's write path — two distinct, independently-traceable callers, both routing through the
    same `fye.a(qhs)` method** (`defpackage/fye.java:17-39`: sets `qhrVar.b = 13;
    qhrVar.c = Integer.valueOf(qhsVar.f)`, wraps into `qjcVar.b = 4; qjcVar.c = qhrVar2`, sends via
    `fyv.c(qjc)` → the already-documented `WriteSetting` RPC send path):
    1. **In-app UI**: `com/google/android/apps/wearables/maestro/companion/ui/quickactions/
       QuickActionsFragment.java:33-98` — the "Quick actions" screen's ANC toggle-group
       `OnClickListener`. Reads the toggle group's selected index (`QuickActionsFragment.java:38`),
       maps it through a lookup table to a `qhs` value (`:52`, `qhs b2 = qhs.b(...)`), guards against
       `ANC_STATE_UNKNOWN` (`:53`), then calls `((fye) p.get()).a(b2)` (`:92`) — a direct,
       unambiguous in-app UI action (the on-screen Off/ANC/Transparency/Adaptive toggle group).
    2. **Physical earbud gesture**: `defpackage/gvi.java:19-33` (an `mbd` callback), constructed and
       invoked from `defpackage/gvj.java:105-110`. `gvj` implements `AudioManager
       .OnAudioFocusChangeListener` and reacts to `qhp` gesture-action-type events (`qhp.java`:
       `SINGLE_TAP`/`DOUBLE_TAP`/`TRIPLE_TAP`/`HOLD`/`SWIPE_FORWARD`/`SWIPE_BACKWARD`/`SWIPE_UP`/
       `SWIPE_DOWN`/`HOTWORD`/`LEFT_ON_HEAD`/etc.) via `switch (qhpVar.ordinal())` — **case 4
       corresponds to `qhp.HOLD`** (Java `enum.ordinal()` position, `HOLD` being the 5th-declared
       constant — not to be confused with `qhp`'s own proto wire value `r`, which is also 4 for
       `HOLD` by coincidence in this enum). On a `HOLD` gesture, `gvj.java:107` calls `new
       gvi(gvjVar)`, whose `s()` callback (`gvi.java:19-33`) reads the current ANC state and calls
       `fye.a(qhs.ANC_STATE_ACTIVE)` or `fye.a(qhs.ANC_STATE_AWARE)` — a binary Active↔Aware toggle —
       i.e. **a physical press-and-hold gesture on the earbud also writes field 13, through the exact
       same code path as the in-app toggle, not a separate field.**
  - **Read direction — `defpackage/fxb.java`, method `a(mzq)`** (internally logged/labeled
    `"handleAllegroSetting"`; takes a `qja`-typed response, matching `qja`'s already-documented
    `ReadSetting`/`SubscribeToSettingsChanges`-response role; constructed with the app's own
    device-repository (`gea`) and analytics logger (`ght`)): a `switch` over every `qhr.b` oneof case
    number from the incoming response, with named per-case handling for **2, 3, 4, 5, 7, 11, 12, 13,
    15, 16, 17, 18, 19, 21, 22, 23, 27, 28, 29** — and one shared `default` branch
    (`fxb.java:106-123`, log message `"Receive unhandled value case: %s"`) covering **cases 6, 8, 9,
    10, 14, 20, 24, 25, 26**. **Cases 9 and 10 fall into that same unhandled default**, exactly like
    6/8/14/20/24-26 — i.e. the response parser recognizes them as structurally valid oneof
    alternatives (consistent with `qgx.j`'s validity table already uniformly covering fields 9/10/13,
    per the original pass) but takes no action on their value in this app version. Field 13, by
    contrast, has a dedicated `case 13` (`fxb.java:278-311`) that decodes the `qhs` enum, forwards it
    to the device-repository (`geaVar2.I(str5, fxa.a.apply(qhsVar))`), and logs it to the app's
    analytics pipeline (`"Log ANC settings to Clearcut: %s"`) — i.e. field 13 is confirmed as both the
    write field **and** its own read/subscribe mirror.
  - **Conclusion (🟡 HYPOTHESIS, strong — code-level, not capture-correlated):** fields 9 and 10 are
    not decoder artifacts — they are real, distinctly-numbered oneof alternatives of the same `qhs`
    enum type as field 13 — but in this app version they are write-silent (nothing ever constructs a
    `qhr` with case 9 or 10) and read-inert (the response handler explicitly discards them via the
    same generic path as several confirmed-unused fields). This narrows, rather than replaces, this
    entry's original "why 🟡 HYPOTHESIS" framing above: field 13 is now code-confirmed as the sole
    active write+read-mirror field; why fields 9/10 exist at all in the schema (an earlier/newer
    protocol revision, a per-earbud Left/Right split later consolidated to one field, a
    debug/engineering-build-only path sharing this generated schema, etc.) remains genuinely open —
    no evidence either way was found this pass, and none is claimed.
  - **Hypothesis test (sharpened)**: same method as the original entry — a capture correlating an
    isolated ANC-mode change, now specifically **both** an in-app tap **and**, separately, a physical
    press-and-hold gesture — against DLCI 0x02 Sent frames. The refined prediction: **only field 13**
    should ever appear with a nonzero `ANC_STATE` value in a Sent frame from this app version; fields
    9/10 should never appear as the populated oneof case, since no code path writes them. A capture
    showing 9 or 10 populated would contradict this static analysis and should be flagged as either a
    different app version/client, or a write call site this search missed.
  - **Update (2026-08-30, Tier 0 re-decode task) — fields 4, 7, 12, and 29 (a distinct set from the
    9/10/13 question above) independently byte-confirmed against existing captures; field 13 itself
    not yet re-tested this pass (no isolated-ANC-tap-plus-gesture capture exists yet — this remains
    the still-open hypothesis test above).** 🟢 FACT (mechanical byte decode, re-extracted from the
    raw logs): `CAP-020` frames 1741/1935 decompose exactly to `qhr` field 4 (value `1`) and field 29
    (value `2`) respectively — see the `qjc`/`qja` entry's own 2026-08-30 update for the full byte
    trace. `CAP-021` frames 1895/3619/4315/4976 (`HOLD-001`–`HOLD-004`) all decompose to `qhr` field
    **7**, and frames 5237/5247/5255 (`HOLD-005`) to `qhr` field **12** — see the updated `qjo`/`qju`
    entry below for the full trace, including a previously-undocumented extra nesting level (`qik`→
    `qho`) inside field 7's own body. All four of these field numbers match this table's own
    write-call-site register exactly (field 4 = touch/head-gestures toggle per `fyo.java:124-144`,
    field 7 = `qju`, field 12 = `qht`, field 29 = head gestures) — a second, independent (wire-level)
    confirmation path for entries this pass had previously only established via static call-site
    tracing. **Promoted to `PROTOCOL.md` §4.5.3 as 🟢 FACT, 2026-08-30, maintainer sign-off
    (`DECISIONS.md` ADR-019)**: field 4 (full identity) and field 7/`qju` (full identity, plus the
    `qik`→`qho` nesting correction) in full; field 12/`qht` for its **field-number identity only** —
    the maintainer reviewed and explicitly declined to promote `qht`'s "ANC gesture loop" name as
    equivalent to `PROTOCOL.md` §4.5.3's separate "ANC-mode rotation checklist" HYPOTHESIS. Field 29
    was **not** included in this promotion (no self-describing code-side name exists for it, only the
    wire-level match) — it remains 🟡 HYPOTHESIS, unchanged.

- **Bonus finding — comprehensive `qhr` field-number → UI-action register (this pass), beyond the
  original 9/10/13 question.** Tracing every `qhr.a.k()` builder call site (write direction) and
  `fxb.java`'s full response `switch` (read direction) together resolved many more of the 38 fields'
  actual UI roles than this pass originally set out to find — included here since it fell directly out
  of the same investigation and is squarely still `qhr`-scoped, not a drift into the unrelated
  807-class sweep. 🟡 HYPOTHESIS (strong) throughout — code-level call-site/log-message evidence, not
  capture-correlated, per the same standard as the rest of this entry:

    | Field | Type | Write call site | Read case (`fxb.java`) / log message | Plausible role |
    |---|---|---|---|---|
    | 2 | BOOL | `fyo.java:169-188` (`l`) | case 2 (`:52-59`), no distinct log | not independently named |
    | 3 | BOOL | `fyo.java:212-232` (`n`) | case 3 (`:60-78`), `"Log OOBE Is Finished setting"` | **OOBE (out-of-box-experience) completion flag** |
    | 4 | BOOL | `fyo.java:124-144` (`h`) | case 4 (`:79-97`), `"Log Gestures Enable setting"` | **Head/touch gestures master enable toggle** |
    | 5 | BOOL | `fyo.java:102-122` (`f`) | case 5 (`:98-105`), no distinct log | not independently named |
    | 6 | BOOL | not found (no write site) | unhandled (default) | write-silent, read-inert (like 9/10) |
    | 7 | MESSAGE→`qju` | `fyo.java:300-374` (`t(gdx)`) | case 7 (`:124-219`), `"Log Gestures Customization for touch and hold setting, left: %s, right: %s"` | **touch-and-hold gesture customization, Left/Right** — see updated `qju` entry below |
    | 8 | BOOL | not found | unhandled (default) | write-silent, read-inert |
    | 9 | ENUM `qhs` | not found | unhandled (default) | write-silent, read-inert — see main finding above |
    | 10 | ENUM `qhs` | not found | unhandled (default) | write-silent, read-inert — see main finding above |
    | 11 | BOOL | `fyo.java:146-166` (`j`) | case 11 (`:220-227`), no distinct log | not independently named |
    | 12 | MESSAGE→`qht` | `hgj.java:216-331` (ANC gesture-loop preference screen) | case 12 (`:228-277`), `"Log ANC gesture loop to Clearcut"` | **ANC gesture-loop membership (On/Off/Transparency/Adaptive)** — see updated `qjg`/`qht` entry below |
    | 13 | ENUM `qhs` | `fye.java:17-39` (2 callers, see main finding above) | case 13 (`:278-311`), `"Log ANC settings to Clearcut"` | **ANC state (Off/Active/Aware/Adaptive) — confirmed** |
    | 14 | BOOL | `fyo.java:234-254` (`o`) | unhandled (default) | write-only; response ignored |
    | 15 | BOOL | `fyo.java:168-188`→`u` (`fyo.java:376-396`) | case 15 (`:312-319`), no distinct log | not independently named |
    | 16 | MESSAGE→`qjw` | `fyp.java:301-322` (`f`, "update user eq") | case 16 (`:320-323`), `"received user eq setting value"` | **live/current user EQ curve** — see updated `qjw` entry below |
    | 17 | INT32/BOOL-adjacent | `fxf.java:82-133` (case 16 of that dispatcher) | case 17 (`:324-327`), `"received last saved volume balance setting value"` | **volume balance** |
    | 18 | MESSAGE→`qjw` | `fyp.java:270-294` (`d`, "update last saved user eq"; also persists to local `SharedPreferences` key `key_user_custom_eq`) | case 18 (`:328-331`), `"received last saved user eq setting value"` | **last-saved/persisted user EQ curve** — see updated `qjw` entry below |
    | 19 | BOOL | `fyo.java:278-298` (`s`); also `fxf.java:113-133` (volume-balance-extremity side effect) | case 19 (`:332-340`), `"received mono setting value"` | **Mono audio** |
    | 21 | BOOL | `hey.java:165-190` (`HearingWellnessFragment` toggle) | case 21 (`:341-351`), no distinct log | HearingWellnessFragment-scoped toggle, not further named |
    | 22 | BOOL | `hnz.java:29-49` (`a`, `"Set Speech Detection"`) | case 22 (`:352-354`) | **Conversation Detection — matches `PROTOCOL.md` §4.5.1's existing field-22 HYPOTHESIS** |
    | 23 | MESSAGE→`qhq` (trivial marker) | not found | case 23 (`:355-357`), calls `geaVar.H(...)` | not independently named |
    | 27 | BOOL | `fyo.java:80-100` (`e`) | case 27 (`:358-361`), `"received case earcon setting value"` | **case-sound toggle (matches §4.5's "Case sound" grouping)** |
    | 28 | BOOL | `fyo.java:58-78` (`d`) | case 28 (`:362-365`), `"received bud return sound setting value"` | **"bud return" case-sound toggle (matches §4.5's other Case-sound entry)** |
    | 29 | ENUM (`qgx.n`/similar) | not found | case 29 (`:366-373`) | not independently named |

    Fields left out of this table (2, 5, 11, 15, 21, 29 excepted where partially covered above) had a
    write and/or read site but no distinguishing log message or UI-fragment context traced this pass;
    included in the table only where at least one side (write call site, read log message, or both)
    gave a nameable role. Fields 1, 6, 8, 14, 17 (write side), 20, 24, 25, 26, 30-38 were not traced to
    any call site this pass (either genuinely absent from the app's current write paths, like 6/8/9/10,
    or simply not searched for — this table is not claimed exhaustive of all 38 fields).

- **Open questions**: what fields 24/26/29's enum types actually are (validity-checker delegates to an
  unnamed obfuscated helper, unlike `qhs`); what the remaining nested `MESSAGE` fields' own semantics
  are beyond their raw shape (see their individual entries below, several now resolved per the table
  above); why fields 9/10 exist in the schema at all, given they are confirmed write-silent/read-inert
  in this app version (see the 2026-08-30 update above — this replaces the original phrasing of this
  question, which asked only "why 3 field numbers", now narrowed to "why 2 apparently-unused ones").
- **Update (2026-08-30, Tier 2 follow-up pass) — fields 24/26/29's delegate chain traced one level
  further; a dead end, not a resolution.** 🟢 FACT (code reading, `qgx.java` in full + `a.java`'s
  `aO`/`aI` methods): `qgx.n`/`qgx.r`/`qgx.s` (the three delegate instances this entry's fields
  24/26/29 route through, per the original pass) are `qgx`'s own indices 13, 17, and 18 respectively
  — and **all three call the exact same underlying method**, `a.aO(int)` (`a.java:487-489`:
  `return aI(i) != 0;`), which itself delegates to `a.aI(int)` (`a.java:431-439`: a 3-value remap,
  `{0→1, 1→2, 2→3, else→0}`). This is the same generic 3-valued validity/remap helper already seen
  elsewhere in this document's `qjn`/`qjt` write-side entry (`fyw.java`'s `qjg`-field boolean-to-tri-
  state conversion, e.g. `i21 = ((qjgVar.b & 1)==0||!qjgVar.c) ? 2 : 3`) — i.e. **`qgx.n`/`r`/`s` are
  not per-field-type enum validators; they are three call sites of one shared, generic "is this a
  valid 3-state ordinal" helper**, reused across unrelated proto enum types by R8 (matching the
  already-documented pattern for `a.at`/`a.as` elsewhere in this codebase). Tracing this delegate
  chain one level deeper does **not** reveal fields 24/26/29's actual backing enum type or semantic
  meaning — it confirms they share a *shape* (a 3-valued enum, structurally like a tri-state
  boolean) but nothing more. This is a genuine dead end via this specific path, not merely "not
  attempted" — recorded as such rather than left ambiguous, per this document's own zero-creativity
  standard. Fields 24/26/29 remain unnamed.

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

- **Update (2026-08-30, Tier 2 follow-up pass) — a structural reframing, not just more field names:
  `qhx`/`qjn`/`qjt` are very likely each an alternate, per-product-variant settings schema, not 3 of
  5 "categories" within one product's own settings.** 🟢 FACT (code existence/structure and call-graph
  tracing across the write-side classes): every write call site for `qhr`'s fields is in
  `defpackage/fyo.java`, a class that `implements fya` **directly**. Every write call site for `qjn`
  (this Group 2) and `qhx`'s field is in `defpackage/fyw.java`, which `extends fxz implements fya` — a
  **different, sibling** class. Every write call site for `qjt` (this Group 3) is in
  `defpackage/fyx.java`, which **also** `extends fxz`. `fyo`, `fyw`, and `fyx` are the **only three**
  classes in this APK that implement the `fya` interface (`grep -rln "implements fya\|extends fxz" .`
  → `fxz.java`, `fyo.java`, `fyw.java`, `fyx.java` — 4 files, one of which, `fxz`, is only ever a base
  class, never instantiated directly by any DI-provider site found this pass), and each is constructed
  from a **distinct** dependency-injection provider case: `frg.java:244` (`new fyo(...)`, feeding
  `qhr`'s writes) vs. `fub.java:52`/`:54` (`new fyw(...)`/`new fyx(...)`, feeding `qjn`'s/`qjt`'s
  writes respectively) — three separate construction sites, not one class picking a branch at
  runtime. **Crucially, `fyo` and `fyw`/`fyx` implement almost entirely disjoint subsets of `fya`'s
  methods**, each stubbing the other's methods as empty no-ops inherited from `fxz`'s or their own
  synthetic overrides (e.g. `fyo.c`/`.g`/`.i`/`.k`/`.p`/`.q`/`.v`/`.w` are all empty stubs in
  `fyo.java:402-432`, while `fyw` gives every one of those a real `qjn`/`qjg`-writing body). This is
  the standard shape of "one interface, N alternate implementations selected per hardware/product
  variant at DI-wiring time," not "one implementation handling 5 sub-categories of the same product's
  settings." **Reading this project's own already-confirmed context onto it**: `qhr` (Group 4,
  reached via `fyo`) is independently confirmed, by this project's own wire-capture evidence
  (`PROTOCOL.md` §4.1/§4.2/§4.5, all captured against the maintainer's actual Pixel Buds Pro 2 unit),
  to be the schema the Buds Pro 2 actually uses. `qhx`/`qjn`/`qjt` (reached via `fyw`/`fyx`) are
  therefore a strong 🟡 **HYPOTHESIS (structural, not wire-confirmed)**: alternate settings schemas
  for **other** Google earbud products sharing this same companion app and `WriteSetting` RPC method
  — plausibly an older/different Pixel Buds generation, or another "Presto"-family device (see the
  literal internal codename found below) — not the hardware this project targets. If correct, most of
  the field names recovered below describe a *different device's* settings, not gaps in the Buds Pro
  2's own feature set; per `PROJECT.md`'s non-goal ("no support for other Pixel Buds models unless the
  protocol is demonstrably identical"), this narrows rather than expands this project's own remaining
  work, but is flagged prominently since it changes how every finding below should be read. **Not
  independently confirmed**: no capture evidence (this project has none targeting a different earbud
  model) checks which of `qhx`/`qjn`/`qjt` a *different* physical device would actually select — this
  reading rests entirely on the DI/method-overlap structure, not a wire observation.

- **`qhx` (Group 1) resolved — OOBE (out-of-box-experience) mode toggle, both directions.** 🟡
  HYPOTHESIS (strong — code-level, both write and read sides self-describing): the only call sites
  referencing `qhx` outside its own oneof-schema declarations are `defpackage/fxz.java` (write) and
  `defpackage/gaa.java` (read). **Write** — `fxz.m(boolean z)` (`fxz.java:20-44`, the base class's own
  override, inherited by every `fya` implementor that doesn't override `m()` itself — `fyw`/`fyx` both
  inherit it unchanged): logs `"Change oobe mode state to %s"` (`fxz.java:23`), builds `qhx` field 1 =
  the boolean, wraps it as `qjc` field 1 (`qhx`'s own oneof case), and sends it via the standard
  `WriteSetting` path — **and additionally, in the same call, sends a second, separate write**
  (`qix` with `qixVar.c=1`, method id 8) alongside it, a detail not decoded further here (out of this
  pass's scope). **Read** — `defpackage/gaa.java`, method `b(int i, mxr mxrVar)` (a large notification
  dispatcher distinct from the already-documented `fxb.java`, which handles `qhr`/Group 4 specifically
  — `gaa.b` handles `i==10`, decoding a `qja` response and switching on its own oneof case): case 1
  (`qhx`) logs **`"Oobe mode is on"`** (`gaa.java:405`) when the boolean is true, and falls through to
  an `ACTION_TYPE_UNKNOWN` gesture dispatch otherwise (`gaa.java:407`) — i.e. OOBE mode being on
  appears to gate/suppress normal gesture-action dispatch, consistent with an out-of-box-experience
  flow wanting to intercept gestures. This class-level identification (OOBE mode) matches, by *label*
  and by *shared code pattern* (a single boolean field, no distinguishing suffix), several other
  "OOBE"-named fields found in `qjn`/`qjt` below (field 8, "oobe finished state") — these are
  plausibly related but are **distinct fields in distinct oneof groups**, not the same field seen
  twice; no evidence this pass ties them together beyond the shared word "oobe" in their respective
  log messages.

- **`qjn` (Group 2) — resolved field-by-field for 8 of its 12 fields, both write and read sides
  largely self-describing; the class's own internal name is literally "presto."** 🟡 HYPOTHESIS
  (strong — code-level; not capture-correlated, since no capture in this project's possession
  exercises a different-product Buds unit): `defpackage/gaa.java`'s read-side dispatcher (same method
  as `qhx` above) logs, for this oneof case specifically, `"Value field of presto setting not set"`
  (`gaa.java`, default branch of the Group-2 switch) — **"presto" is this app's own internal codename
  for whatever product `qjn` belongs to** (not this project's own prior "presto"/"MaestroSettingGroup2"
  alias, which was a project-invented placeholder — this is the app's own string). Write side
  (`defpackage/fyw.java`, all methods overriding the shared `fya` interface):

  | Field | Type | Write call site (`fyw.java`) | Read case (`gaa.java`) / log message | Plausible role |
  |---|---|---|---|---|
  | 1 | BOOL | `l()`, `:163-175`, `"Change OHD state to %s"` | case `i15==1`, no distinct log | **"OHD state"** — plausibly On-Head-Detection; note `qhr` field 2 (this app's *other*, Buds-Pro-2 schema) is independently wire-confirmed as In-ear detection (`PROTOCOL.md` §4.5.5) and is also reached via the *same shared interface method* `l()` in `fyo.java:169-188` — i.e. `l()` is this app's own "wear-detection toggle" slot across both schemas, under two different internal names (see `PROTOCOL.md` cross-reference note below) |
  | 4 | MESSAGE→`qjo` | `t(gdx)`, `:227-350`, `"Touch control has no value to set"` (guard message when empty) | case `i15==4`, builds a `gdx` and calls `geaVar2.ad(...)` | **Left/Right "touch control" gesture-action pair** — see the updated `qjo`/`qju` entry below; structurally and functionally parallel to `qju`/`qhr` field 7 (also reached via the same shared interface method `t(gdx)`, in `fyo.java`, for the Buds-Pro-2 schema) |
  | 5 | BOOL | `k()`, `:148-160`, `"Change noise detection state to %s"` | case `i15==5`, no distinct log | **"noise detection state"** |
  | 6 | BOOL | `p()`, `:193-205`, `"Change shared mode state to %s"` | case `i15==6`, no distinct log | **"shared mode state"** — plausibly an audio-sharing/multi-listener feature; not this project's own already-confirmed Multipoint (`PROTOCOL.md` §4.5.2, which is `qhr` field 11, a different oneof group entirely) |
  | 8 | BOOL | `n()`, `:178-190`, `"Change oobe finished state to %s"` | case `i15==8`, no distinct log | **"oobe finished state"** |
  | 9 | MESSAGE→`qjg` | `c()`/`g()`/`q()`/`y()` (4 separate one-flag-at-a-time setters, `:82-99`,`:116-133`,`:207-224`,`:370-380`) | case `i15==9` | **"Attention alert"** (4-boolean group) — see the updated `qjg`/`qht` entry below |
  | 10 | BOOL | `f()`, `:101-114`, `"Change diagnostics state to %s"` | case `i15==10`, no distinct log | **"diagnostics state"** |
  | 11 | BOOL | `h()`, `:135-145`, no distinct log either side | case `i15==11`, no distinct log | not independently named |
  | 12 | ENUM (`qic`, via `qgx.o`) | `w(gdc)`, `:352-368`, `"Change Eq Setting state to %s"` | case `i15==12`, routes through `geaVar3.S(str3, fms.D(i2))` | **"Eq Setting"** — an enum-valued EQ setting (plausibly a preset selector), structurally distinct from `qhr`'s own EQ representation (`qjw`, a 5×`float` custom-curve quintet, fields 16/18) — consistent with `qjn` belonging to a different, likely simpler product that only supports EQ presets, not a 5-band custom curve. `qic`'s own 3 constants (`qic.a`/`.b`/`.c` = 1/2/3) are themselves unnamed integers, not self-describing like `qhs`'s `ANC_STATE_*` — which specific preset/value each represents is not resolved. |

  Fields 2, 3, 7 were not traced to a call site this pass (no `fyw.java` method maps to them in the
  file as read); field 12's `qgx.o` delegate (index 14, `case 14: return qic.a(i2) != 0;`) is the same
  delegate `qjt` field 12 uses below (already noted in the original pass) — now additionally confirmed
  to route, on both sides, through the exact same `fms.D(qic)`/`gea.S(...)` conversion and setter,
  reinforcing (not merely coincidentally) that `qjn` field 12 and `qjt` field 12 represent the *same*
  underlying "Eq Setting" concept for their respective product/schema.

- **`qjt` (Group 3) — resolved for 6 of its 10 fields by direct cross-reference against `qjn`'s newly-
  named fields, via the same shared `fya` interface methods; `qjt`'s own write class (`fyx.java`)
  carries no distinct log messages of its own.** 🟡 HYPOTHESIS (strong for the *field-number*
  identification, since it's a direct code cross-reference; slightly weaker for the *semantic name*
  itself, since `qjt`'s own code path never logs it — the name is inherited from `qjn`'s sibling
  method, not independently stated): `defpackage/fyx.java` (`extends fxz implements fya`, the same
  sibling relationship as `fyw`) implements the *same* interface method names as `fyw`, writing to
  `qjt` instead of `qjn`:

  | Field | Type | Write call site (`fyx.java`) | Shared `fya` method | Name inherited from `qjn`'s same method |
  |---|---|---|---|---|
  | 1 | BOOL | `l()`, `:117-127` | `l(boolean)` | **"OHD state"** (matches `qjn` field 1) |
  | 5 | BOOL | `k()`, `:105-115` | `k(boolean)` | **"noise detection state"** (matches `qjn` field 5) |
  | 8 | BOOL | `n()`, `:129-139` | `n(boolean)` | **"oobe finished state"** (matches `qjn` field 8) |
  | 10 | BOOL | `f()`, `:69-79` | `f(boolean)` | **"diagnostics state"** (matches `qjn` field 10) |
  | 11 | BOOL | `h()`, `:81-91` | `h(boolean)` | not independently named (matches `qjn` field 11, also unnamed) |
  | 12 | ENUM (same `qgx.o`/`qic` as `qjn` field 12) | `w(gdc)`, `:153-168` | `w(gdc)` | **"Eq Setting"** (matches `qjn` field 12 exactly — same delegate) |
  | 13 | BOOL | `i()`, `:93-103` | `i(boolean)` | not independently named — `qjn` has no field using `i()` (`fyw` does not override it; inherited empty stub from `fxz`), so there is no sibling name to borrow |
  | 14 | INT32 | `v(int)`, `:141-151` | `v(int)` | not independently named — same situation as field 13, `qjn` has no field using `v()` |

  This cross-reference method (matching two sibling classes' *shared interface method names*, not
  their field numbers or log text directly) is a new technique introduced this pass — it works here
  specifically because `fyw`/`fyx` are structurally parallel siblings of the same base class (`fxz`)
  and interface (`fya`), and is a genuine confirmation (not a guess) for the 6 fields with a `qjn`-side
  log message to borrow, but is explicitly **not** claimed for fields 13/14, where no sibling name
  exists to cross-reference and no name is invented. `qjt`'s own fields 3 and 7 (present per the
  original pass's sparse-numbering list) were not traced to a call site this pass — `fyx.java` has no
  method setting `qjtVar.b` to 3 or 7.

- **`qjv` (Group 5) confirmed fully unused in this app version — both write-silent and read-inert,
  for the entire group, not just some fields within it.** 🟢 FACT (exhaustive whole-tree text search,
  not merely "no call site found in the files searched" as the original pass's phrasing put it):
  `grep -rn "qjv\b" .` across every `.java` file in `jadx-output/sources/` returns exactly 5 hits —
  `qjv.java` itself, `qjc.java`/`qja.java` (the two oneof-schema declarations, already documented),
  `qix.java:28` (an unrelated oneof's own info-string, which separately references the `qjv` class as
  one of *its own* nested types — a coincidental reuse of the class, not a `qjc`/`qja`-oneof write
  path), and `defpackage/fyv.java:68` — the **central** outbound-write dispatch/deduplication method
  (`fyv.c(qjc)`, the function every `fya` implementor's write path funnels through) has a defensive
  `case` for `qjc.b==5` (computing a dispatch-map key from `((qjv) qjcVar.c).b`), but this is dead
  code from a construction standpoint: **no site anywhere in the decompiled tree ever builds a `qjc`
  with case 5 set** (unlike cases 1/2/3/4, each of which has a real, traced builder call site above).
  On the read side, `gaa.java`'s response dispatcher (documented above) explicitly `return`s without
  any action for oneof case 5, the same as case 4 (`qhr`, correctly — that one's handled by the
  *separate* `fxb.java` class instead) — but unlike case 4, no other class anywhere handles case 5
  either. **Conclusion**: `qjv` (and by extension its own field 1/`qit` and field 3/`qjd` trivial
  marker types) is present in the schema but entirely inert in this app version — a group-level
  parallel to `qhr`'s individual fields 9/10, but total rather than partial.

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
- **Update (2026-08-30, follow-up pass) — fields 16 and 18 both resolved to a specific *role* (though
  not to "preview" vs. "commit-on-release" specifically).** 🟡 HYPOTHESIS (strong — code-level, not
  capture-correlated, so this does **not** by itself answer `PROTOCOL.md` §4.2's preview-vs-commit
  timing question): `defpackage/fyp.java` implements the EQ-write interface (`fyd`) with two distinct
  methods, each with its own explanatory log message, both confirmed independently on the read side
  by `defpackage/fxb.java`'s response handler:
  - **Field 16 = "user eq"** (current/live curve): `fyp.java:301-322` (method `f(qjw)`) logs
    `"update user eq: %s"` (`:302`) and sends `qhr` field 16. Read side: `fxb.java` `case 16`
    (`:320-323`) logs `"received user eq setting value"`. `f()` is called both from `e(gdy)`
    (`fyp.java:296-299`, itself called whenever the EQ curve object changes) and, via `c(int)`
    (`fyp.java:264-268`, `"setPreset: %d"`), whenever a built-in EQ preset is selected.
  - **Field 18 = "last saved user eq"** (persisted curve): `fyp.java:270-294` (method `d(gdy)`) logs
    `"update last saved user eq: %s"` (`:272`), sends `qhr` field 18, and — uniquely among the two —
    **also persists the same value locally**, independent of the device round-trip:
    `this.e.a.edit().putString("key_user_custom_eq", Base64.encodeToString(gdyVar.f(), 0)).apply()`
    (`fyp.java:293`, an Android `SharedPreferences` write). Read side: `fxb.java` `case 18`
    (`:328-331`) logs `"received last saved user eq setting value"`.
  - **Reading**: this establishes field 16 as "the EQ curve currently in effect" and field 18 as "the
    curve to remember/restore" (with a local `SharedPreferences` cache backing the latter specifically)
    — a live-value/persisted-value split, not necessarily the in-drag-preview/release-commit split
    `PROTOCOL.md` §4.2 hypothesized. The two readings are compatible (a "commit on release" write could
    plausibly update both the live value *and* the persisted one, explaining why `d()`/field 18 is the
    one with the extra `SharedPreferences` side effect) but this pass found no call site where `d()`
    and `f()` are invoked together or in a fixed sequence from one user gesture — `d(gdy)` and
    `e(gdy)`→`f(qjw)` are two separate, independently-callable interface methods (`fyd.d`/`fyd.e` per
    the interface each implements), and no caller of either was traced this pass (out of scope — would
    require finding `fyd`'s call sites in the EQ UI fragment, not attempted here). The
    preview-vs-commit timing question `PROTOCOL.md` §4.2 asks remains open; this only replaces "field
    16 and 18 are structurally identical, role unknown" with "field 16 and 18 have distinct, named
    roles (live vs. persisted), whose relationship to slider-drag-vs-release timing is still untraced."
- **Update (2026-08-30, Tier 0 re-decode task) — the "live"/"persisted" reading checked against
  `CAP-005`'s and `CAP-015`'s existing captured sequences; consistent with, and sharpens, the
  wire-observed pattern, without fully resolving the drag-vs-release timing question.** 🟡 HYPOTHESIS
  (strengthened by correlation, not independently capture-verified from scratch — this re-reads
  already-published decodes from `CAP-005-FINDINGS.md` §5a and `CAP-015-FINDINGS.md` §4, cross-checked
  against `fyp.java`'s code, not a fresh re-extraction from the raw logs):
  - **Preset selection (`fyp.c(int)` → `f(qjw)` = field 16 only, per `fyp.java:264-268`/`:301-322`)
    predicts presets should *only* ever produce field-16 writes, never field 18. `CAP-015`'s own
    6-preset sequence (frames 2111/2165/2227/2303/2351/2400, `CAP-015-FINDINGS.md` §4's table) matches
    this exactly — every preset tap in that capture is field 16, zero field-18 frames appear anywhere
    near a preset tap.** This is a clean, exact match between the code-derived call graph (presets
    route through `f()`/field-16 only) and the wire-observed pattern (presets are field-16-only in
    both captures that exercise them, `CAP-005` and `CAP-015`).
  - **Slider drags predict continuous field-16 writes (from `e(gdy)`→`f(qjw)`, called "whenever the
    curve object changes") plus a separate field-18 write from `d(gdy)` at some other trigger.**
    `CAP-015`'s 50 slider-related frames match this shape exactly: for every one of the 15 drag-cycles,
    a run of field-16 frames (one per intermediate value) is followed by exactly one field-18 frame,
    always carrying the identical final value to the immediately-preceding field-16 frame — consistent
    with `d(gdy)`/field-18 firing once per gesture, not continuously, and never introducing a new value
    of its own (matching `fyp.java`'s own description of `d()`'s side effect: persisting the *current*
    value locally, not computing a different one).
  - **Does not resolve which specific UI event calls `d(gdy)`.** `CAP-015-FINDINGS.md` §6 already
    found field 18 fires 0.05–1.9s after the last field-16 write in every cycle, with no video-visible
    `Save`-button tap in between, and revised its own hypothesis to "field 18 fires on slider-release"
    — this pass's code reading is compatible with that (a `SeekBar.OnStopTrackingTouch`-style listener
    plausibly calling both `e()`→`f()` during the drag and `d()` once at release), but no call site for
    `fyd.d`/`fyd.e` (the interface methods `fyp` implements) was traced this pass to confirm it — the
    same gap this document's own original `qjw` entry already flagged. **Net effect of this
    correlation**: the "field 16 = live, field 18 = persisted-locally" code-level reading and the
    "field 18 fires on release, not on Save" wire-level reading are mutually consistent and reinforce
    each other, but neither independently confirms the other's specific causal claim (what code calls
    `d()`, vs. what UI gesture the wire's timing implies) — both remain 🟡 HYPOTHESIS, now on two
    convergent evidence paths instead of one.

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
- **Update (2026-08-30, follow-up pass) — `qht` (the `qhr` field-12 alternative) resolved; `qjg`
  remains open.** 🟡 HYPOTHESIS (strong — code-level, not capture-correlated): `qht`'s 4 booleans
  (Java fields `c,d,e,f`) are set one-per-ANC-state in `defpackage/hgj.java:216-331`, the UI fragment
  behind `R.xml.anc_preference` (`hgj.java:53-54`) — a settings screen with 4 `CheckBoxPreference`s
  keyed `anc_preference_key_on`/`_adaptive`/`_off`/`_txp` (`hgj.java:115-129`). Each checkbox's
  on-change handler builds a `qht` with exactly one of its 4 fields set
  (`qhtVar.c`=On/`qhtVar.d`=Off/`qhtVar.e`=Transparency/`qhtVar.f`=Adaptive, `hgj.java:224-330`),
  wraps it as `qhr` field 12, and sends it as a `WriteSetting`. The read side confirms the same
  shape and adds an explicit name: `defpackage/fxb.java`'s response handler, `case 12`
  (`fxb.java:228-277`), unpacks all 4 booleans and logs `"Log ANC gesture loop to Clearcut: %s"`
  (`:267`) — i.e. **`qht`/`qhr` field 12 is the "ANC gesture loop" feature: which ANC states
  (On/Off/Transparency/Adaptive) are included when press-and-hold cycles through ANC modes** (the
  `hgj.java` UI screen for configuring *that* cycle's membership, as opposed to `fye`/`qhr` field 13,
  which is the actual state-*change* command the cycle — or the in-app toggle group — sends). This is
  a different, related feature from `ADR-013`'s "press-and-hold ×4 Left/Right × ANC/Assistant
  combinations" reading of DLCI 0x02 traffic — that capture-derived finding and this
  code-derived one have **not** been reconciled; they may describe the same underlying setting seen
  from two angles, or two genuinely different settings that happen to share a 4-flag shape. `qjg`
  (the `qjn` field-9 alternative, a *different* top-level oneof group from `qht`'s) was not resolved
  this pass — no write or read call site for it was found in the files searched.
- **Open questions**: which (if either) of `qjg`/`qht` is `ADR-013`'s press-and-hold finding — `qht`
  now has its own, better-evidenced "ANC gesture loop" identity (above), which may or may not be the
  same thing `ADR-013` observed on the wire; what `qjg` is remains fully open.
- **Update (2026-08-30, Tier 2 follow-up pass) — `qjg` resolved: "Attention alert," both directions.**
  🟡 HYPOTHESIS (strong — code-level, self-describing on the read side): `qjg` is the `qjn` (Group 2,
  see that entry's 2026-08-30 update above) field-9 alternative, **not** a `qhr` field — a correction
  to this entry's own original framing, which listed `qjg`/`qht` side-by-side as if both were
  `qhr`-reachable; only `qht` is (`qhr` field 12). **Write** — `defpackage/fyw.java` implements 4
  separate `fya` interface methods, each toggling exactly one of `qjg`'s 4 booleans and sending the
  result: `c(z)` sets field `c` (bit 1), `g(z)` sets field `e` (bit 4), `q(z)` sets field `f` (bit 8),
  `y()` sets field `c=false` unconditionally (bit 1, no parameter — an explicit "turn off" action
  distinct from `c(z)`'s general setter). **Read** — `defpackage/gaa.java`'s same Group-2 response
  dispatcher (see `qjn`'s entry above), case 8 (`i15==9`): unpacks all 4 booleans into a `gcs` object,
  calls `this.b.J(this.e, gcsVar5)` (a device-repository setter), then separately checks whether all 4
  fields now read a specific "all off" pattern (`a.aI(...)` tri-state remap, `==2` for each — see the
  qhr-fields-24/26/29 update above for what `a.aI` does) and, if so, logs **`"Turn off attention
  alert"`** and fires an app-internal event (`((fyc) this.c.a()).i(new fmp(16)).p()`). **This is the
  first unobfuscated, self-describing name for `qjg`**: a 4-flag "Attention alert" feature, distinct
  in both shape *and* now-confirmed identity from `qht`'s "ANC gesture loop" (also 4 booleans, but a
  different `qjn`-sibling schema's field, `qhr` field 12, with its own separate "Log ANC gesture loop
  to Clearcut" read-side name) — the two are **not** the same feature despite the shared 4-bool shape,
  resolving this entry's own "shape alone does not distinguish which (if either)" caveat for `qjg`
  specifically (it does still apply to any *other* untraced 4-bool message this project might
  encounter later). Not capture-correlated — `qjg` belongs to the `qjn`/"presto" schema, which per the
  `qjn` entry's structural finding is very likely a different product than this project's own Buds
  Pro 2, so no capture in this project's possession is expected to exercise it at all.

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
- **Update (2026-08-30, follow-up pass) — `qju` (the `qhr` field-7 alternative) resolved with a
  specific name; `qjo` remains open.** 🟡 HYPOTHESIS (strong — code-level, not capture-correlated):
  `defpackage/fxb.java`'s response handler, `case 7` (`fxb.java:124-219`), unpacks `qju`'s two nested
  `qik` messages (fields `c`/`d`, each itself wrapping a `qho` value via `fzw.a(qhoVar)`) and passes
  the two decoded values through a helper `b(int)` (`fxb.java:19-25`, mapping to an `fph` enum:
  `UNDEFINED`/`NONE`/`ANC`/`TALK_TO_ASSISTANT`) before logging **`"Log Gestures Customization for
  touch and hold setting, left: %s, right: %s"`** (`fxb.java:193`) — i.e. `qju`/`qhr` field 7 **is**
  the Left/Right press-and-hold gesture *action* customization (which of ANC/Assistant/None each
  earbud's press-and-hold triggers), matching `PROTOCOL.md` §4.5.4/`ADR-013`'s already-documented
  "press-and-hold ×4 Left/Right × ANC/Assistant combinations" finding by name, not just by shape —
  this is the first code-level confirmation of *which* field carries that setting. The write side
  (`defpackage/fyo.java:300-374`, method `t(gdx gdxVar)`) builds the matching `qju`/`qik`/`qho`
  structure from a `gdx` (Left/Right gesture-action pair) and sends it as `qhr` field 7. `qjo` (the
  `qjn` field-4 alternative, a *different* top-level oneof group) was not resolved this pass — no
  write or read call site for it was found in the files searched.
- **Open questions**: what `qjo`'s own two nested message types are — `qju`'s Left/Right
  press-and-hold-gesture-action reading is no longer speculative (see update above), but nothing
  established this pass carries over to `qjo`, a structurally similar but functionally untraced type.
- **Update (2026-08-30, Tier 2 follow-up pass) — `qjo` resolved: Left/Right "touch control"
  gesture-action pair, structurally and functionally parallel to `qju`, for the `qjn`/"presto" schema
  instead of `qhr`.** 🟡 HYPOTHESIS (strong — code-level, both write and read sides traced): `qjo` is
  the `qjn` (Group 2) field-4 alternative (see that entry's own 2026-08-30 update). **Write** —
  `defpackage/fyw.java`, method `t(gdx gdxVar)` (`:227-350`) — the **same interface method name and
  same `gdx` domain-object parameter type** as `fyo.java`'s already-documented `t(gdx)` for `qju`/`qhr`
  field 7. Logs `"Touch control has no value to set"` (`fyw.java:230`) as a guard when the incoming
  `gdx` has neither Left nor Right set, then builds a `qjk` (not `qik` — `qjo`'s own nested type is
  spelled differently from `qju`'s `qik`, confirmed by direct read of both files, not a typo) with two
  fields (`c`=Left, `d`=Right per the `(gdxVar.b & 1)`/`(gdxVar.b & 2)` bit checks matching `fyo`'s
  identical pattern for `qik`), each itself wrapping a `qho` value via the same `fzw.b(int)`/`fzw.a
  (qho)` helper pair `qju` uses, and wraps the result as `qjn` field 4. **Read** — `defpackage/gaa.java`
  (same Group-2 dispatcher as `qjn`'s other fields), case 4 (`i15==4`): unpacks `qjoVar.c`/`.d`
  (`qjk`'s own two `qho`-wrapped values, via `fzw.a`) into a `gde`/`gdx` pair — **the exact same domain
  types (`gde`, `gdx`) `fxb.java` builds for `qju`'s read side** — and calls `geaVar2.ad(str2, (gdx)
  k.q())`. **What this establishes**: `qjo` is `qjn`'s own Left/Right press-and-hold-style gesture-
  action container — the write-side log's literal wording, **"touch control"**, is the closest this
  pass found to a UI-facing name (contrast `qju`'s own read-side log, `"Log Gestures Customization for
  touch and hold setting, left: %s, right: %s"` — both describe the same broad feature area, touch/
  hold gesture-action assignment, under each schema's own wording). Per the `qjn` entry's structural
  finding, this is very likely the *other* product's equivalent of the Buds Pro 2's own press-and-hold
  feature (`qju`), not a second, additional feature on the Buds Pro 2 itself — not capture-correlated,
  for the same reason given there. `qjo`'s inner type is named `qjk` (not `qik`) — this document's
  original framing of `qjo`/`qju` as sharing an identical, unnamed nested-type shape is confirmed
  structurally (both wrap 2 `qho`-typed values) but the concrete class names differ per schema, now
  recorded rather than left implicit.
- **Update (2026-08-30, Tier 0 re-decode task) — byte-level capture confirmation of `qju`'s (not
  `qjo`'s — no capture exercises the `qjn`/"presto" schema) own nested structure, and a nesting detail
  finer than this document's original shape description.** 🟢 FACT (mechanical byte decode,
  re-extracted from `CAP-021-btsnoop_hci.log`, cross-checked against `CAP-021-FINDINGS.md` §3's own
  citation and found byte-identical): frames 1895/3619/4315/4976 (`HOLD-002`/`HOLD-004`/`HOLD-001`/
  `HOLD-003`) all decompose, after the standard `field5{field4{...}}` outer wrapper, to `qhr` field
  **7** (`qju`) — matching this entry's own write-site identification (`fyo.java:300-374`) exactly.
  **One level finer than `CAP-021-FINDINGS.md`'s own published decode** ("`field7(len6){ field1|
  field2(len4){ field4=varint(5|6) } }`" — a simplification that treats the inner `field4` as a
  direct varint): the actual bytes show `field4` is wiretype **2** (length-delimited), not a bare
  varint — e.g. frame 1895's `qju.field1` (Left) body `22 02 08 06` decomposes as tag `0x22` = field 4,
  wiretype 2, length 2, containing `08 06` = tag `0x08` = field 1, wiretype 0, value `6`. This matches
  this document's own already-established `qju` shape exactly: `qju.field1(Left)` → `qik` (a
  length-delimited submessage, not a scalar) → `qik.field4` → `qho` (itself length-delimited) →
  `qho.field1` = the raw integer (`5`=Active noise control / `6`=Digital assistant). All 4 `HOLD-001`–
  `HOLD-004` frames decode cleanly through this 3-level nesting (`qju`→`qik`→`qho`) with the predicted
  Left/Right selector (`field1`/`field2`) and value (`5`/`6`) matching `CAP-021-FINDINGS.md` §3's own
  table exactly. Separately, frames 5237/5247/5255 (`HOLD-005`) decompose to `qhr` field **12** (`qht`)
  with exactly 4 boolean sub-fields (tags `0x08`/`0x10`/`0x18`/`0x20`, one varint each) — matching
  `qht`'s already-documented 4×`BOOL` shape exactly, no further nesting (unlike `qju`, `qht`'s own
  fields are plain booleans, not `qik`/`qho`-wrapped). See `CAP-021-FINDINGS.md`'s 2026-08-30 addendum
  for the full byte trace and reproduction script. **Promoted to `PROTOCOL.md` §4.5.3 as 🟢 FACT,
  2026-08-30, maintainer sign-off (`DECISIONS.md` ADR-019)**: `qju`/field 7 in full, including the
  `qik`→`qho` nesting correction; `qht`/field 12 for its field-number identity only, not its "ANC
  gesture loop" name's equivalence to the separate rotation-checklist HYPOTHESIS.

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
- **Update (2026-08-30, follow-up pass) — `ghb.java` traced in full; confirms `fwv`/`npy`/`nqo` are
  shared, service-agnostic `pw_rpc` plumbing, not `WriteSetting`-specific.** Found while tracing
  `defpackage/fwy.java`'s other factory cases for context (`fwy` is confirmed a 20-case, R8-merged
  generic Dagger-style factory implementing `npi` — same pattern as `frg`/`gau`/`fsz`/`fuu`; case 6
  (`fwy.java:53-54`) is the one that constructs `ghb`: `new ghb((oql) this.c.a(), (ffd) this.d.a(),
  (fwv) this.e.a(), (Map) this.a.a(), (fvl) this.b.a())`). `ghb implements fuc` (`fuc.java:1-4`, a
  single-method interface `oqh a()`). Its two real methods, `b(pip)` and `c(Map, pip)`
  (`ghb.java:48-133`/`155-252`), are Kotlin `suspend` functions — JADX shows their coroutine
  state-machine bytecode rather than clean source, but the raw dump is legible enough to trace:
  - **`ghb.b(pip)`** (`ghb.java:87-120`, reading the raw bytecode) does, in order: `fwv r7 = r6.e;
    oql r7 = r7.a(); oqz r7 = r7.Z(); ... r7 = ple.A(r7, r0)` — i.e. it calls **`fwv.a()`** (the exact
    same single-method interface `fwv.java:1-4`, `oql a()`, that `fut` — the already-documented
    HDLC/pw_rpc RFCOMM connection, `class fut extends gbd implements fwv` — implements) and *awaits*
    the result before doing anything else. It then similarly awaits `fvl.a` (a second gate/lookup
    table), and finally: `Map r6 = r6.f; ... Object r6 = r6.get(r0); npy r6 = (npy) r6; ... int r7 =
    ((Number) r7).intValue(); ... nqo r6 = r6.f(r7, "maestro_pw.DynamicServerConfigService",
    "SetConfig"); return r6;` — **the exact same `npy.f(int channelId, String service, String
    method)` → `nqo` (`MethodClient`) call already fully documented in the `nqx`/`npy`/`nqo`/`npw`/
    `nqm` entry above for `WriteSetting`**, here invoked for `DynamicServerConfigService`/`SetConfig`
    instead. This directly confirms `fwv.a()` is a generic "wait until the pw_rpc channel/route for
    this `goq` target is ready" gate used by more than one RPC-calling class (not something specific
    to the `WriteSetting`/ANC-settings path), and that `npy`/`nqo` (`Client`/`MethodClient`) are
    shared plumbing serving every `maestro_pw.*` service in `fux`'s catalog, not infrastructure built
    specifically for settings writes. This generalizes, without contradicting, the earlier
    `nqx`/`npy`/`nqo`/`npw`/`nqm` entry's findings — the `WriteSetting` chain traced there is one
    instance of this shared mechanism, not a separate one.
  - **`ghb.c(Map, pip)`** (`ghb.java:155-252`) serializes an incoming `Map<Integer, Boolean>`-shaped
    config (filtered to keys `{1, 2, 3}`, `ghb.java:10`/`207-208`) into a length-delimited protobuf
    varint-field stream (`mxy`, a `CodedOutputStream`-style writer, `ghb.java:203-226`: for each of
    the 3 allowed keys present in the map, writes `field=<key>, value=<boolean>` via `r2.f(r5, r6)`),
    wraps the resulting bytes into a `qib` instance via `myp.n(qib.a, bytes, ..., myd.a)` (the
    standard protobuf-lite `parseFrom(bytes)`-equivalent static helper), then calls **`r10.e(r8)`**
    (`ghb.java:237`) — i.e. `nqo.e(mzq)`, the exact same unary-RPC-invoke method already documented on
    the `WriteSetting` path — passing the just-built `qib` as the request payload. **Caveat**: `qib`
    was previously documented (this doc's Tooling-note section) as one of two known-trivial 0-field
    marker types the `RawMessageInfo` decoder script was validated against — this call site's use of
    `qib` as a byte-carrying request wrapper is not necessarily a contradiction (constructing a
    message via `parseFrom(bytes)` doesn't require the target type to have any *named* fields; the
    raw bytes could simply become unrecognized/extension fields on an otherwise-empty message, or
    this could be a different, R8-merged-under-the-same-name `qib` than the one previously
    decoded) — not independently resolved this pass, flagged rather than guessed at.
- **Open questions**: what "dynamic server config" refers to here (feature-flag-style remote config
  pushed to the Buds themselves, vs. something purely client-side) — not yet clear from these two
  files alone. New from this pass: whether the `qib` `ghb.c` constructs from raw bytes is genuinely
  the same 0-field `qib` documented elsewhere in this doc, or a same-named-but-distinct R8-merged
  class — not resolved.

### `defpackage.gbu` — KPI event collector (out of core ANC/EQ/settings scope; documented for completeness)

- **Path**: `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/gbu.java:13-214`
- **Readable alias**: KpiEventCollector
- **Role**: 🟢 FACT (code existence/structure): constructed by `defpackage/fwy.java` case 4
  (`fwy.java:47-50`: `new gbu(ftmVar, ((gib) this.e).a(), executorService, ((npm) this.a).a(), ((gkr)
  this.b).a())`), found this pass while tracing `fwy`'s other cases for context, per the same task
  that traced `ghb` (see the `ghd` entry's Update above). `gbu implements fzy, gak`
  (`fzy.java:1-4`: single method `void b(int, mxr)`; `gak` has additional methods, only `a()`/`b()`
  traced). `gbu.b(int i, mxr mxrVar)` (`gbu.java:63-188`) is a dispatcher keyed on a small integer
  `i` (cases seen: 1, 4, 5) that parses incoming payloads as `qkr`/`qkq`-typed protobuf messages
  (`gbu.java:73-188`) — variable names, log strings (`"Received KPI: %s"`, `"Error parsing
  KpiEvent"`, `"Error uncompressing Kpi events"`), and the constructor's use of `fux.java`'s
  already-documented `case 6` (`a10a20.kpi.Kpi`/`KpiStream` — "KPI" = Key Performance Indicator) all
  point to this being a **device-reported usage/performance-metrics event collector**, not an
  ANC/EQ/settings feature. `gbu.g()` (`gbu.java:36-43`) sends a Group `137`/Code `2` request via
  `this.d.l(137, 2, f, j2)` — `this.d` is `ftm`-typed, a facade whose `l(int, int, byte[], long)`
  signature mirrors `gbd.d(int, int, byte[])`'s own Group/Code/Value write shape (plus a 4th
  rate-limiting/delay argument) — i.e. this feature also rides the same generic
  Group/Code-addressed RFCOMM transport as `gbd`'s base implementation, just through an `ftm`
  wrapper layer not otherwise traced this pass. Decoded/decompressed KPI events are forwarded to a
  `gbv` instance (`this.h`, not traced) rather than sent anywhere off-device in this class itself.
- **What this does NOT establish (superseded in part by the Update below)**: whether these KPI events
  are ever transmitted off-device once collected here — `AGENTS.md` §1's "Zero-GMS"/no-telemetry ban
  is, and remains, a rule for *this project's own* implementation, not a claim about what the official
  app does; the finding below documents the *official* app's behavior only, purely as informational
  background, per this doc's existing convention of describing reference-app behavior without implying
  it as a model to follow.
- **Update (2026-08-30, follow-up pass) — traced `gbv`/`gbt`/`gbs`/`ghq`/`gic` end-to-end: KPI events
  are assembled into a protobuf analytics-log entry and handed to a class whose own log tag is
  literally `"ClearcutEventLog"`, gated by a per-device consent check.** 🟢 FACT (code
  existence/structure — the `"ClearcutEventLog"` string is about as strong non-capture evidence for
  this claim as static analysis gets; this describes the *official* Google app only, see the caveat
  above):
  - **`defpackage/gbv.java`** (`final class gbv extends gbx`) is a small, generic batching/dedup
    helper — its 5 bridge methods (`a`–`e`, `gbv.java:6-44`) key `qkr` KPI events by destination
    (`qkm` enum, via `a()`), compute a time-delta (`b()`), merge/re-timestamp duplicates (`c()`), and
    filter (`d()`/`e()`) — i.e. `gbx` is a generic batch-and-flush base class, `gbv` is its
    `qkr`-specific instantiation. Neither `gbv` nor `gbx` themselves send anything anywhere; they hand
    off to whatever `gbw`-typed callback the base class's flush logic was given.
  - **`defpackage/gbt.java`** (`final class gbt implements gbw`, constructed fresh per flush as `new
    gbt(this)` from `gbu`) is that callback — its single method `a(Object obj)` (`gbt.java:16-224`,
    `obj` = one flushed `qkr` KPI event) is where the event actually gets turned into an
    analytics-log entry:
    1. Builds an `nhm` instance (`gbt.java:21-65`: fields `e`=destination enum, `f`=an int, `h`=an
       enum-minus-1 value, `g`=a timestamp delta) — **this is the first external reference found to
       `nhm`**, previously flagged in this doc's "807-class sweep, unidentified" table as "richest
       schema found in the entire sweep (76 fields, 0 oneofs); no external `new nhm(`/`nhm.class`
       reference found" — that open item is now resolved: `nhm` is a per-KPI-event detail record.
       When the event carries extra structured payload bytes (`(qkrVar.b & 16) != 0 &&
       qkrVar.g.d() > 0`, `gbt.java:63-65`), decoding is delegated to `gbs.a(myk, qkr)`
       (`defpackage/gbs.java:1758`, the last and, per a quick shape check, only substantial method in
       that 1765-line file — `gbs` is a single-purpose class, not an R8-merged multi-case dispatcher
       like `fwy`/`gau`/`frg`, unlike this pass's working assumption going in — it appears to be one
       large per-KPI-event-type payload decoder, not independently traced further this pass given its
       size and this task's bounded scope).
    2. Wraps the `nhm` into a `ner` instance (`gbt.java:67-86`: field `e`=the `nhm`, field
       `f`=`gbuVar.e` — an in-memory incrementing per-collector sequence counter) and logs it locally
       first: `((lsx) ((lsx) gbu.a.c()).R((char) 370)).s("KPI event: %s", nhmVar5);`.
    3. Builds a second, richer `ner` (`gbt.java:96-219`) carrying: `d`=a `neq` (a single string field,
       `String.valueOf(giaVar.a)` — an app-instance/session identifier, not a device MAC), `c`=a `nep`
       composed of 3 `neo` messages, each populated from a `gdd.c` string pulled off a `gdm` "device
       metadata" object (`gbt.java:119-165` — 3 identifying strings, not decoded further; plausibly
       device model/variant/serial-class fields given the surrounding `gcl`/device-registry context),
       `g`=a timestamp (`gcl.d()`), and two enum-derived ints `h`/`i` from `gic.b(gdh)` applied to
       `gcl.h()`/`gcl.i()` (`gic.b`, `gic.java:22-45`, a 13-case ordinal remapping — not independently
       named).
    4. **Gate**: `gic gicVar = gbuVar.c; String d = gbuVar.d.d(); if (gicVar.a(d)) { return; }`
       (`gbt.java:87-91`) — before sending, checks `gic.a(String)` (`gic.java:47-51`:
       `Optional e = this.a.e(str); return e.isPresent() && !((gcl) e.get()).B();`) keyed by a
       device-identifying string. **`gcl.B()`** (`gcl.java:47-55`) reads a single boolean-shaped field
       (`.A`) off a `gdw` sub-message nested inside a device descriptor (`gdb`), guarded by a
       presence-bit check (`(gdwVar.b & 256) == 0`) — i.e. `gic.a()` returns **true** (causing
       `gbt.a()` to return early, *not* sending the event) when the device is known **and** that one
       boolean field is **false** — structurally consistent with (not independently confirmed as)
       a per-device "diagnostics/usage-data sharing" consent flag stored in device metadata.
    5. **Sink**: `ghqVar.a(l.q(), new pzm(), 0);` (`gbt.java:220`) — `ghq` is `gicVar.c`
       (`gic`'s own constructor, `gic.java:16-19`: `this.c = new ghq(context, hvrVar);`).
       **`defpackage/ghq.java:9`: `public static final lud a = lud.m("ClearcutEventLog");`** — the
       class's own logger is tagged, verbatim, `"ClearcutEventLog"`. `ghq.a(mzq, iml, int)`
       (`ghq.java:16-23`) calls `hvrVar.h(mzqVar, imx.a(this.c, imlVar)).g(i).c().k(...)` — a
       build-event/set-code/enqueue-and-callback chain matching the real Clearcut client API shape.
       `defpackage/hvr.java:9`: `public final class hvr extends hvm` — a real (non-R8-merged) class
       hierarchy, consistent with wrapping an actual Clearcut logging library client (package name
       itself obfuscated, not independently confirmed as `com.google.android.libraries.clearcut.*` or
       similar beyond the log-tag string).
  - **Reading**: KPI events collected from the Buds over Bluetooth (per `gbu`'s already-documented
    Group `137`/Code `2` request/response cycle) are batched, converted into an `nhm`/`ner`/`nep`/
    `neo` analytics-log-entry protobuf structure carrying device-identifying metadata, and — unless a
    per-device consent-adjacent flag (`gcl.B()`) reads false — handed to a class whose own logger tag
    is literally "ClearcutEventLog", i.e. **the official app's Clearcut analytics pipeline**. This is
    the same general logging surface already seen elsewhere in this project's traces (`ght`'s many
    "Log ... to Clearcut" calls in the settings-write handlers, e.g. `fxb.java`/`QuickActionsFragment
    .java`) — `ghq`/`hvr` here is very plausibly the same or a closely related underlying mechanism,
    though `ght` (settings-event logging) and `ghq` (KPI-event logging, this entry) were not
    cross-checked against each other for whether they share the same `hvr` instance/sink this pass.
  - **What this still does NOT establish**: whether these Clearcut events actually leave the device on
    the network (Clearcut's own client-side batching/upload-scheduling logic was not traced — `hvr`/
    `hvm` were not opened), and the exact real-world meaning of `gcl.B()`'s flag, `neq`'s
    session-identifier string, or `nep`/`neo`'s 3 device-metadata strings — all read structurally,
    not semantically confirmed. **Reiterating the caveat above**: this entire finding describes the
    *official Google app's* behavior; it is not, and must not be read as, a suggestion for this
    project's own implementation — `AGENTS.md` §1's ban on any telemetry/analytics SDK in this
    project's own code is unaffected and unchanged by this finding.
- **Open questions (partly resolved below)**: what `ftm`/`gib`/`npm`/`gkr` (the remaining collaborator
  types `gbu` itself is built from) individually are; `gbs.java`'s own per-event-type payload decoding
  was not traced given its size and this task's bounded scope. Genuinely out of this project's
  ANC/EQ/settings-protocol scope unless a future task specifically needs it — flagged here as a plain
  listing per this pass's own instructions, not a relevance judgment.
- **Update (2026-08-30, follow-up pass) — `ght` vs. `ghq`'s `hvr` sink compared: same base
  class/method and the same device-consent gate confirmed; likely (not fully confirmed) different
  named Clearcut/Streamz log sources.** 🟢 FACT (code existence/structure — this describes the
  *official* app only; see the caveat above, unaffected and unchanged by this finding):
  - **Same class hierarchy, same sink method, not overridden**: `defpackage/ght.java:7`:
    `public class ght extends ghq` — confirmed a direct, single-level subclass, not a parallel
    reimplementation. `ght`'s own constructor (`ght.java:19-29`) takes its own `hvr hvrVar` argument
    and forwards it straight into `ghq`'s constructor (`super(context, hvrVar)`). `ght.java` (449
    lines, read in full this pass) defines **no** `a(mzq, iml, int)` override — every one of its ~15
    public logging methods (`b`/`c`/`d`/`e`/`f`/`g`/`i`/`j`/`k`/`l`/`m`/`n`/`o`/`p`/`q`/`r`/`s`,
    `ght.java:35-419`, each building an `fpp`/`fps`/`fpy`/`fqv`/`fra`-typed settings/usage-event
    protobuf and logging a `"Log ... to Clearcut: ..."` message — matching every already-documented
    "Log ... to Clearcut" call site traced in earlier passes) funnels through the single private
    dispatcher `h(String, fpn, fpm)` (`ght.java:116-132`), which asynchronously schedules
    `defpackage/ghs.java` (`final class ghs implements mbd`) as a callback. **`ghs.s(Object)`**
    (`ghs.java:20-...`) is where the actual send happens: after resolving device info and gating on
    `z = ((gcl) optional.get()).B()` (`ghs.java`, same variable name and same `gcl.B()` method as the
    KPI path's gate below), it calls **`ghtVar.a(fpnVar, new pzl(), fpmVar.k)`** — since `ght`
    defines no `a(...)` override, this resolves to the **inherited `ghq.a(mzq, iml, int)`** method,
    the exact same method `gbt.a()`'s KPI-event path calls (as `new pzm()` there vs. `new pzl()`
    here for the `iml`-typed 2nd argument — different log-source-tag objects, not traced further,
    but the *method* is identical).
  - **Same gating flag**: `ghs.java`'s `z = ((gcl) optional.get()).B()` uses the **exact same
    `gcl.B()` method** (`gcl.java:47-55`, the single boolean-shaped device-metadata field already
    documented in the `gbu` entry's KPI-path finding) as `gic.a(String)`'s gate on the KPI path
    (`gic.java:47-51`: `!((gcl) e.get()).B()`) — i.e. **both the settings-event and KPI-event
    Clearcut-logging paths are gated by the identical per-device flag**, strengthening (not just
    repeating) the earlier reading that `gcl.B()` is a shared, feature-wide
    diagnostics/analytics-consent flag rather than something KPI-specific. (`ght`'s gate additionally
    requires `!booleanValue`, a second `Pair`-derived flag not traced this pass — plausibly a
    dedup/already-sent check, not confirmed.)
  - **Likely different underlying `hvr` instances (named log sources), not fully confirmed**: unlike
    the transport-layer `gbn` singleton resolved in an earlier pass (exactly one `new gbn(` call site
    in the whole app), **`hvr` has multiple distinct construction sites with different 2nd-argument
    "log source" string tags**: `defpackage/ghz.java:27`: `new hvr(b, "A10A20_KPI",
    (String) f.get())` — explicitly tagged **`"A10A20_KPI"`**, matching `fux.java`'s already-documented
    `a10a20.kpi.Kpi`/`KpiStream` service naming, and thus a strong candidate for the specific `hvr`
    feeding `gic`'s `ghq` (`gib.java:33`: `new gia(b, intValue, gckVar, hvrVar, ...)`, `hvrVar` a
    passed-in parameter not traced to its ultimate origin this pass, but the "A10A20" naming
    correlation is suggestive); `defpackage/gwv.java:1626`: `new hvr(context,
    "STREAMZ_GNP_ANDROID", null)` — a third, differently-tagged construction, for an apparently
    unrelated "Streamz" metrics pipeline (see below); `defpackage/hvl.java:22`: `new hvr(this.a,
    this.b, this.d, this.c, null, null, this.e)` — a generic factory whose tag argument (`this.b`)
    is parameterized per-caller, not resolved to a specific string this pass. `ght`'s own `hvr`
    argument (`fuu.java:79`: `new ght(b5, intValue, hvrVar, freVar, gckVar, ftjVar, mbrVar, ...)`) is
    likewise a passed-in parameter, not traced to its ultimate tagged-construction site this pass —
    **whether it resolves to the same `"A10A20_KPI"`-tagged instance the KPI path likely uses, a
    fourth not-yet-found tag, or the generic `hvl.java` factory with some other tag, is not
    established**. Given the KPI-specific tag naming already found, and that `ght`'s events are
    semantically settings/usage events (not KPI events), the more likely reading is that they use
    *different* named log sources sharing the same `ghq`/`hvr` client class and API, not a literal
    single shared instance — but this is not confirmed, only the more plausible of the two readings.
  - **Bonus finding — `ght`/`ghs` also send to a *second*, separate pipeline, unconditionally**:
    `ghs.java`'s tail end (after the `gcl.B()`-gated `ghtVar.a(...)` call, and independent of it) builds
    an `mko`-typed message (`ghs.java`, fields including a fixed `mkoVar.c = 1017` code) and submits it
    via `ghtVar.g` (`ght`'s own `gwv gwvVar` field, `ght.java:12`) — `gwv` is the class whose
    `"STREAMZ_GNP_ANDROID"`-tagged `hvr` construction was found above (`gwv.java:1626`), i.e. this
    second call is very plausibly Google's internal "Streamz" metrics system, not Clearcut. Unlike the
    `ghq.a(...)` call, this second send is **not** gated by `gcl.B()`/`booleanValue` — only by
    `if (fpmVar.equals(fpm.UNKNOWN)) { return; }` — a different, apparently-always-on telemetry path
    running alongside the consent-gated Clearcut one. Not traced further this pass (out of this task's
    bounded scope, which was specifically the `ght`-vs-`ghq` comparison), but flagged since it's a
    directly-adjacent, previously-unnoticed second pipeline.
  - **Update (2026-08-30, follow-up pass) — this second pipeline traced one level further: no gating
    found in the code path actually exercised, but the trail runs into another giant R8-merged class
    before reaching a concrete sink, so "no gating" is confirmed only for the segment actually read,
    not end-to-end.** 🟢 FACT (code existence/structure only; describes the *official* app, unaffected
    caveat as above):
    1. `defpackage/mko.java` decodes (`python3 scripts/decode_rawmessageinfo.py mko.java`) to exactly
       3 fields — `c`=INT32 (the `1017` constant, plausibly an event/metric code), `d`=INT32 (from
       `fpm.k`, the settings-event-type enum's int value), `e`=STRING (a device-correlated label) — a
       small, generic "coded counter with a label" shape, consistent with a Streamz metric cell.
    2. `ghs.java:31` confirms `int i = 2;` is unchanged through the method — i.e. `new hvn(mkoVar4,
       str6, i)` (`ghs.java:203`) unambiguously invokes **`hvn`'s case 2**. `hvn` (`hvn.java:15`,
       `final /* synthetic */ class hvn implements mab`) is, itself, **yet another R8-merged
       multi-case dispatcher** (9 cases, 0–8 plus a default, `hvn.java:33-150`) — the same pattern
       already seen repeatedly (`fwy`/`gau`/`frg`/`fsz`). Case 2's body (`hvn.java:76-77`) is exactly
       one line: `return ((iqd) obj).a((mko) this.a, (String) this.b);` — **no gating check of any
       kind appears in this specific case**, confirming last pass's suspicion for the segment of the
       chain visible in `ghs`/`hvn` themselves.
    3. `defpackage/iqd.java` (7 lines) is a bare single-method interface: `mbo a(mko mkoVar, String
       str);` — i.e. the actual sink implementation is not visible at this call site at all; it comes
       from whatever `obj` resolves to (the result of `r1.submit(new epf(obj2, 14))`,
       `ghs.java:203` — `r1`/`obj2` being `gwvVar.c`/`gwvVar.a`), cast to `iqd`. Tracing the concrete
       `iqd` implementation would require opening `defpackage/epf.java`'s own case 14 — not done this
       pass (see bounded-scope note below).
    4. **Important correction to last pass's framing**: `defpackage/gwv.java` (1631 lines) is
       **confirmed, not just suspected, to be another R8-merged multi-purpose class** — `grep -rn "new
       gwv(" .` finds at least 5 structurally distinct constructor call sites across unrelated files
       (`bkr.java:10`: `new gwv((char[]) null)`; `bzq.java:14`: `new gwv(this)`; `dbu.java:23`:
       `new gwv((Context) this.a, (byte[]) null)`; `ddg.java:21`: `new gwv((byte[]) null, (byte[])
       null)`; `ddg.java:112`: `new gwv((Object) dkbVar, (Object) dkhVar, (Object) dddVar, (byte[])
       null)` — 5 incompatible signatures, an unambiguous R8 constructor-merge signature). The one
       constructor this pass read in full (`gwv(List list)`, `gwv.java:52-...`) initializes fields
       `a`/`b`/`c` as a plain `HashMap`/`HashMap`/`ArrayList` — **not** an `Executor`-shaped object,
       even though `ghs.java` calls `r1.submit(...)` on `gwvVar.c` (an `Executor`-style call) — i.e.
       the `gwv(List)` constructor this pass happened to read is **not** the one `ght`'s own `(gwv)
       this.j.a()`-resolved instance actually uses; a different, not-yet-located constructor overload
       must be the real one. The class also declares `public static gwv d;` (`gwv.java:48`), a
       classic singleton-holder field, which is at least consistent with (not proof of) one shared
       instance existing somewhere — but which of `gwv`'s many merged identities that static field
       belongs to was not determined. **Net effect**: the `"STREAMZ_GNP_ANDROID"`-tagged `hvr`
       construction found last pass at `gwv.java:1626-1629` is real code that exists somewhere in this
       file, but this pass could **not** confirm it belongs to the same merged-constructor identity
       that `ght`'s `gwvVar` field actually resolves to at runtime — this downgrades (without
       retracting) last pass's "very plausibly Google's Streamz metrics system" reading from
       "the mechanism is identified" to "a `\"STREAMZ_GNP_ANDROID\"`-tagged construction exists in the
       same giant class `ght` also draws from, not confirmed as the same code path."
    5. **Genuinely valuable side-finding, from `hvn`'s *other* cases** (not the one `ghs.java`
       invokes, found while reading the file to identify case 2): case 1 (`hvn.java:42-75`) is the
       actual logic of **`com.google.android.apps.wearables.maestro.companion.devicelogging
       .KpiLoggingWorker`** — a genuinely unobfuscated class name (survived R8, imported at
       `hvn.java:6`) implementing a `WorkManager`-style worker that **fetches raw diagnostic logs
       directly from the connected earbud** (`ftjVar.A(b)`/`ftjVar.g(b)`, `b`=device address),
       gated by the same `gcl.B()` consent flag already documented elsewhere in this entry — logging
       `"No consent for diagnostics"` (`hvn.java:67`/`72`) when consent/OOBE-completion isn't met.
       This is a real, separate on-device-log-retrieval feature, distinct from (but clearly related
       to) the KPI-event-streaming pipeline already documented above. Case 4 (`hvn.java:92-108`)
       references Google's real, public `com.google.android.libraries.performance.primes.transmitter
       .clearcut.ClearcutMetricSnapshotTransmitter` class (imported at `hvn.java:7`) — confirming the
       app uses Google's **Primes** performance-monitoring library, and that (in that one unrelated
       case, not the case `ghs.java`'s call goes through) Primes' own metric-snapshot transport is
       Clearcut-based. This is a useful nuance, not a contradiction: it suggests "Streamz"-labeled
       metric groups under Primes *can* still ultimately transmit via Clearcut in this codebase — but
       this was not confirmed for the specific case-2 path `ghs.java` actually uses, which remains
       unresolved per point 3 above.
    - **Bounded-scope stopping point (deliberate, not an oversight)**: this pass stopped at `iqd`
      (an unimplemented interface) and did not open `epf.java`'s case 14 or attempt to locate `gwv`'s
      correct constructor overload — both would be the natural next steps to fully resolve where this
      pipeline's bytes actually go, but doing so risked an open-ended trace through more R8-merged
      dispatcher classes without a clear stopping point, which this task's own guardrails asked to
      avoid. **Conclusion stands as a genuine, not-artificially-narrowed negative result for the
      segment actually traced**: no consent/feature-flag gating exists in `ghs`→`hvn`(case 2)
      themselves; whether gating exists further downstream, inside the not-yet-located `iqd`
      implementation, remains an open question, stated as such rather than assumed either way.
    - **Update (2026-08-30, follow-up pass) — `epf.java` case 14 opened; last pass's leading
      `gwv`-constructor lead checked and refuted; the correct constructor narrowed to 3 candidates,
      not fully resolved; the "no gating" finding holds one level further.** 🟢 FACT (code
      existence/structure only; describes the *official* app, same caveat as throughout this entry):
      - `defpackage/epf.java` (`final /* synthetic */ class epf implements Callable`, 179 lines, read
        in full) is itself confirmed as another R8-merged multi-case dispatcher (20 cases, same
        pattern as `hvn`/`fwy`/`gau`/`frg`/`fsz`). **Case 14** (`epf.java:133-134`, rendered as `case
        UrlRequest.Status.READING_RESPONSE /* 14 */:`) is a single line: `return (iqd)
        this.a.a();` — where `this.a` is whatever object was passed as `epf`'s own constructor
        argument (`ghs.java:203`'s `new epf(obj2, 14)`, `obj2 = gwvVar.a`). **No gating check of any
        kind appears in this case either** — extends last pass's "no gating in `ghs`→`hvn`" finding
        one level further, to `ghs`→`hvn`→`epf`(case 14) as a whole. The concrete `iqd` implementation
        is still one level further away: whatever `gwvVar.a.a()` (a generic `.a()` accessor call)
        returns.
      - **The prior pass's leading candidate — the `gwv(Context, byte[], byte[])` constructor at
        `gwv.java:1625-1630` (the one immediately adjacent to the `"STREAMZ_GNP_ANDROID"`-tagged `hvr`
        construction) — is refuted for this specific chain**: it sets `this.a = new
        CopyOnWriteArrayList();` (`gwv.java:1627`), and `CopyOnWriteArrayList` has no `.a()` method,
        so `epf` case 14's `this.a.a()` could not type-check against it. This doesn't mean that
        constructor overload is unused elsewhere in the app (it may genuinely back some other, unrelated
        `gwv` usage) — only that it cannot be the one `ght`'s `gwvVar` field resolves to, given what
        `epf`'s case 14 requires of `gwvVar.a`.
      - **All ~40 of `gwv`'s constructor overloads were read this pass** (`gwv.java:1299-1630`, plus
        the earlier-read `gwv(List)` at `gwv.java:52-...`) specifically checking each one's `this.a`
        assignment against the "must have a `.a()` method" requirement. `defpackage/nmx.java` (a bare
        single-method interface, `Object a();`) is the type this project's own traces have
        independently, repeatedly confirmed follows exactly this lazy-provider `.a()` accessor
        pattern (e.g. the `nph.b(...).a()` idiom used throughout `fyo`/`fyp`/`fut` and elsewhere) —
        **the only constructors whose `this.a` is assigned an `nmx`-typed argument** are: `gwv(String
        str, mbr mbrVar, nmx nmxVar)` (`gwv.java:1359-1363`, `this.a = nmxVar`), `gwv(nmx nmxVar, nmx
        nmxVar2, nmx nmxVar3)` (`gwv.java:1468-1475`, `this.a = nmxVar`), and `gwv(nmx nmxVar, nmx
        nmxVar2, nmx nmxVar3, byte[] bArr)` (`gwv.java:1593-1600`, `this.a = nmxVar3`) — **these 3 are
        the strongest remaining candidates**, though not confirmed as *the* one in play; a handful of
        other constructors assign `this.a` to an application-specific type (`ipc`, `jnq`, `jfl`,
        `jtt`, `bzq`, `oql`, etc.) whose own `.a()`-method-having-ness was not individually checked
        this pass, so they are not fully ruled out either, only judged less likely given no
        independent evidence (unlike `nmx`) that they follow this accessor convention.
      - **What would be needed to fully resolve this (not done this pass, next natural step)**: trace
        `ght`'s own `gwv gwvVar` constructor argument (`fuu.java:79`'s `(gwv) this.j.a()`) back to
        `fuu`'s *own* construction site, to find which `npo` is bound to `fuu`'s `j` field — the same
        method already used successfully for the `gbn`-singleton trace in an earlier pass. This pass
        did not attempt it (stopping here per this task's own explicit "close to the final pass on
        this side-thread" scoping) — recorded as the concrete, actionable next step rather than left
        vague.
      - **Gating conclusion (updated)**: extends cleanly — no gating found in `ghs`→`hvn`(case 2)→
        `epf`(case 14); whatever `iqd`'s real implementation is (reached via one of the 3 `nmx`
        candidates above, or an unconfirmed alternative) remains untraced, so gating still cannot be
        ruled out or confirmed beyond this point. This is stated as the final open question for this
        side-thread, not chased further, per this task's own scoping.

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

### Full-tree GATT/BLE reference sweep (2026-08-30, cross-validation follow-up)

- **Path**: exhaustive `grep -r` across
  `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/` (not a sample — the whole
  decompiled tree).
- **Readable alias**: n/a (a search result, not a class).
- **Role**: 🟢 FACT: this APK version's decompiled source contains **zero** references to
  `BluetoothGatt` and **zero** references to `BluetoothGattCallback`, anywhere. It also contains
  **zero** references to the Fast Pair GATT service UUID `0xFE2C` (`grep -rli "fe2c"`, no hits). A
  separate check for the two long-standing open GATT handle numbers from `CAP-014`/`PROTOCOL.md`
  §4.3's `0x0c0X`/`0x0f2X` cluster (`grep -ril "0x0c0c|0c0c|0x0f2a|0f2a"`) found exactly one file,
  `com/google/android/apps/wearables/maestro/companion/R.java`, and both hits there
  (`settingslib_neutral_variant96 = 0x7f060c0c`, `Widget_Material3Expressive_Toolbar_Surface =
  0x7f150c0c`) are unrelated Android resource-ID integer constants that merely contain the substring
  `0c0c` — not a GATT handle in any Fast-Pair-UUID-adjacent context. This is a genuine negative
  result, not a coincidental near-miss.
- **Relevant UUIDs found**: none (that is the finding).
- **Assessment**: corroborates, from the companion app's own source rather than from capture-based
  reasoning, this project's existing methodology choice (`CAP-004`/`CAP-014`) of using nRF Connect
  rather than the official app for all GATT-related captures — the companion app itself performs no
  direct `BluetoothGatt` connection management at all, consistent with GATT access (Fast Pair Account
  Key exchange, the Battery Service pairing, etc.) being delegated entirely to Google Play Services/
  Nearby out-of-process. It also rules out the companion APK's own source as the place where
  `PROTOCOL.md` §4.3's still-open `0x0c0X`/`0x0f2X` handle↔UUID mapping question could be resolved —
  that open question remains exactly as open as before, just with one plausible lead (checking the
  companion app's own source) now closed off.
- **Open questions**: none — this is a completed, reproducible, exhaustive search. The underlying
  `0x0c0X`/`0x0f2X` handle↔UUID mapping question itself (`PROTOCOL.md` §6) remains unresolved and is
  unaffected by this entry.

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
| `qhr` field 4 = "Use touch controls" master enable toggle (write site `fyo.java:124-144`, read site `fxb.java` case 4) | §4.5.3 top-level toggle opcode | 2026-08-30 | `CAP-020` frame 1741; `DECISIONS.md` ADR-019 |
| `qhr` field 7 = `qju` — Left/Right press-and-hold gesture-action customization (write site `fyo.java:300-374`, read site `fxb.java` case 7) | §4.5.3 press-and-hold action-selection opcode | 2026-08-30 | `CAP-021` frames 1895/3619/4315/4976; `DECISIONS.md` ADR-019 |
| `qhr` field 12 = `qht` — field-number identity only (write site `hgj.java:216-331`, read site `fxb.java` case 12); "ANC gesture loop"/"ANC-mode rotation checklist" equivalence explicitly not promoted | §4.5.3 ANC-mode rotation checklist opcode | 2026-08-30 | `CAP-021` frames 5237/5247/5255; `DECISIONS.md` ADR-019 |
| `qhr`'s oneof structure confirmed inside DLCI 0x02's `field5{field4{...}}` wrapper, for fields 4 and 29 sampled at the wire level | §2.2a 2026-08-30 update | 2026-08-30 | `CAP-020` frames 1741/1935; `DECISIONS.md` ADR-019 |
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
