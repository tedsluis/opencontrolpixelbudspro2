# REVERSE_ENGINEERING.md

Findings from static analysis of the official Pixel Buds companion app APK
(`com.google.android.apps.wearables.maestro.companion`, see
`SCREENSHOTS_PIXEL_BUDS_APP.md`). Every entry references a concrete file/path
in `reverse-engineering/apk/jadx-output/` or `apktool-output/`.

> **Status:** no analysis session has been logged yet — this document currently
> defines the structure and workflow. Populate the sections below as findings
> come in, one class/finding at a time, following `PROJECT_RULES.md` §1
> (FACT / HYPOTHESIS / ASSUMPTION) and §3.

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

| Field | Value |
|---|---|
| Package name | `com.google.android.apps.wearables.maestro.companion` |
| App version | _(fill in — Play Store listing or `aapt dump badging`)_ |
| Version code | _(fill in)_ |
| Download/acquisition date | _(fill in)_ |
| APK SHA-256 | _(fill in — `sha256sum pixelbuds.apk`)_ |
| Min/target/compile SDK | _(fill in — from `AndroidManifest.xml`)_ |
| Obfuscation present? | _(yes/no — R8/ProGuard indicators: short/renamed classes, `-keep` residue in strings, etc.)_ |
| Native libraries present? | _(yes/no — which `.so` files under `lib/`, see §Native libraries below)_ |
| Firmware/library versions referenced in-app | _(cross-reference against `PROTOCOL.md` §0.1 firmware baseline, `release_5.203`)_ |

## Method

1. `apktool d pixelbuds.apk -o apktool-output/` — for resources, manifest,
   smali.
2. `jadx -d jadx-output/ pixelbuds.apk` — for readable (decompiled)
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

> Template per class — copy for each new finding.

### `<package.ClassName>` (or obfuscated name, e.g. `a.b.c.Xy2`)

- **Path**: `reverse-engineering/apk/jadx-output/.../ClassName.java`
- **Readable alias**: _(e.g. "GattCallbackImpl")_
- **Role**: _(e.g. "🟢 FACT: implements `BluetoothGattCallback`, receives
  `onCharacteristicChanged` for battery updates" or "🟡 HYPOTHESIS: appears to
  serialize `AncCommand` based on field names, not yet confirmed against a
  capture")_
- **Relevant UUIDs found**: _(list, cross-reference §UUID register)_
- **Relevant message groups/codes found** (if Fast Pair Message Stream-related,
  cross-reference `PROTOCOL.md` §2.1): _(list)_
- **Relevant methods**:
  - `methodName(...)` — _(what this method does, and why you think so)_
- **Open questions**: _(what is still unclear)_

---

## UUID register

All UUIDs found in the APK, with status. Once a UUID's function is confirmed
via a capture, update its status here **and** promote it into `PROTOCOL.md`.

| UUID | Found in (file:line) | Suspected function | Status |
|---|---|---|---|
| _(e.g. `0000xxxx-...`)_ | _(path)_ | _(e.g. "battery service")_ | 🟡 HYPOTHESIS / 🟢 FACT (confirmed by capture ID X) |

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

> Per `AGENTS.md` §6, the AI assistant does not attempt to reverse engineer
> native binaries itself — schemas/opcodes extracted from them (e.g. via
> `pbtk`) are treated as given inputs once the maintainer has extracted them.
> This table tracks *what exists*, not a request for the AI to decompile it.

## Call graph notes

Build this up incrementally — start at the BLE/RFCOMM connect entry point and
work outward.

```
<entry point>
  -> ...
```

## Correlation status with PROTOCOL.md

Track which findings here have been cross-checked against a capture and
promoted into the protocol documentation, to avoid the same finding being
"rediscovered" independently in both documents.

| Finding (this doc) | Promoted to `PROTOCOL.md` section | Date | Capture/Finding ID |
|---|---|---|---|
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