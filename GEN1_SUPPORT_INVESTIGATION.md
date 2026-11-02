# Original Pixel Buds Pro (Gen 1) support in OpenControl — investigation report

Date: 2026-11-01. Investigation only. No repository was modified and nothing was sent to any earbuds.

| Repository | Local path | Commit inspected | Licence |
|---|---|---|---|
| OpenControl (`tedsluis/opencontrolpixelbudspro2`) | `opencontrolpixelbudspro2/` | `29ca74d` (2026-10-24, release 1.4.0 + docs) | AGPL-3.0-or-later |
| pbpctrl (`qzed/pbpctrl`) | `pbpctrl/` | `2620367` (2026-01-04) | MIT OR Apache-2.0 |
| PixelBudsMacOS (`KashaMalaga/PixelBudsMacOS`) | `PixelBudsMacOS/` | `0ffd4f9` (2026-06-03) | MIT |

Paths below are relative to a folder holding the three clones side by side (`opencontrolpixelbudspro2/`, `pbpctrl/`, `PixelBudsMacOS/`). `OC/` stands for
`opencontrolpixelbudspro2/android/<module>/src/main/kotlin/io/github/tedsluis/opencontrolpixelbuds/`.

Helper tool written for this report: `scripts/decode_maestro_frames.py`. It un-escapes HDLC, checks CRC-32, decodes the address
and the pw_rpc packet. Run it with `python3 -I scripts/decode_maestro_frames.py PixelBudsMacOS/Reference/captured-frames-2026-05-16.txt`.

> **Status (2026-11-02, `CAP-081`, `ai-sessions/0102`).** This report was written on 2026-11-01, before any Gen 1 capture of the project's own.
> `CAP-081` (Group BQ) has since recorded the official app writing every Gen 1 setting and this app doing the same, and it supersedes these statements
> of the body:
> - the Gen 1 identification (§1 item 4, §6.3): device type **4**, Model ID **`6edaf7`** — `CAP-081-FINDINGS.md` §1;
> - "G1-HW: nothing is in this state yet" and §8.3: every write of §7 M5 has now run on the hardware, answered OK (`CAP-081-FINDINGS.md` §4);
> - "writable = ∅ at first" (§6.2, M3): the pull request enables the writes `CAP-081` shows, the mode and multipoint marked `TODO(verify)` because the
>   official app uses signed Message Stream commands for them (`PROTOCOL.md` §4.6);
> - press-and-hold "single-bud or both-bud form per the capture" (§6.5): one bud per write, as the official app.
>
> Nothing in this report or in `CAP-081` is accepted by the maintainer yet (`AGENTS.md` §6).

### Evidence labels used in this report

| Label | Meaning |
|---|---|
| **IMPL** | Implemented in OpenControl today. |
| **SRC** | Shown in the source code of pbpctrl or PixelBudsMacOS. Not observed by us. |
| **G1-CAP** | Seen in real bytes captured from a Gen 1 on `release_5.11_signed`. These come from a third party (PixelBudsMacOS [Reference/captured-frames-2026-05-16.txt](https://github.com/KashaMalaga/PixelBudsMacOS/blob/0ffd4f9/Reference/captured-frames-2026-05-16.txt)); we decoded them and the CRC-32 is valid. |
| **G1-HW** | Verified on the user's own Gen 1 hardware. **Nothing is in this state yet.** |
| **UNVERIFIED** | No evidence either way. |

OpenControl uses its own scale (🟢 FACT / 🟡 HYPOTHESIS / ⚪ ASSUMPTION, `PROJECT_RULES.md` §1). Agents may not promote anything to 🟢 or
write an ADR without the maintainer (`AGENTS.md` §6, §15). Every Gen 1 statement here would enter `PROTOCOL.md` as 🟡 at most until
the maintainer approves.

---

## 1. Executive summary and feasibility

**Gen 1 support is feasible. Most of the protocol stack can be reused unchanged. Adding `release_5.11_signed` to the allowlist alone
would be unsafe and would also not work.**

### What already matches Gen 1

- **Same transport and same protocol: RFCOMM → pw_hdlc (CRC-32) → pw_rpc → `maestro_pw.Maestro`.**
  - pbpctrl was written for the Gen 1 in 2022 (SRC).
  - The third-party Gen 1 capture shows the exact frame shapes OpenControl already parses (G1-CAP):
    - the unsolicited `GetSoftwareInfo` "hello" with `call_id 0xFFFFFFFF` on channel 19;
    - the same response addresses `80 a3` / `00 a5` as on the Pro 2;
    - the same service hash `0x7ede71ea`;
    - a settings-stream push `4:{13:3}`.
- **The settings schema is the same message family.**
  - The fields the Pro 2 app uses (2, 4, 7, 11, 12, 13, 15, 16, 17, 19, 22) all exist in pbpctrl's Gen 1-era proto (2022–2023).
  - The value encodings agree: EQ 5×float32 within ±6, balance zig-zag, press-and-hold nesting.

### What will not work as-is

These are the exact code paths:

1. **The write gate checks the Pro 2 only.**
   - `OC/data/SafeModeGate.kt:36` allows only `release_5.203`.
   - `:50` refuses any Fast Pair Model ID other than `da2db1`.
2. **ANC writes need a Pro 2-shaped mode list.**
   - `writeAncMode` (`OC/data/BudsRepositoryImpl.kt:975`) refuses unless field 12 was read and decoded.
   - `SettingsCodec.decodeAncModes` (`OC/data/codec/SettingFrame.kt:270-272`) accepts only exactly four booleans. Field 4 is "Adaptive".
   - Field 4 was added to pbpctrl in Dec 2025 for the Pro 2, and the Gen 1 is not known to have Adaptive. A 3-field Gen 1 answer
     would leave the list unread, and every ANC write would fail with `AncModeListNotRead`.
3. **The UI and the read sweep assume the Pro 2 feature set.**
   - Adaptive is always offered: `AncMode`, the Quick Settings tile cycle `AncMode.kt:51`, `AncScreen`.
   - Head gestures (29) and the case sounds (27/28) are always read and shown.
   - These are Pro 2 features. Gen 1 behaviour for them is UNVERIFIED.
4. **The Gen 1 cannot be identified.**
   - The only model check is the Pro 2's Model ID.
   - No Gen 1 Model ID, device-type value or other identifier exists in any of the three repos.
5. **Project scope.** `PROJECT.md:64-65` makes other models a non-goal "unless the protocol is demonstrably identical — this must be
   separately verified, never assumed". Gen 1 support therefore needs maintainer-approved ADRs before any code (see §6.1).

### Recommendation

Add a **device-profile layer**: a model, the firmware verified for that model, and per-capability read/write support. Identify the
Gen 1 from **two independent signals**: the firmware string in the hello, and the `GetHardwareInfo` field 1 device type. The Pro 2
reports type 6. The official app maps types 4/5 to "ALLEGRO", which very likely means the Gen 1 (unverified).

Enable Gen 1 writes **one capability at a time**. Each one needs a capture of the official app performing that write on a Gen 1 at
`release_5.11_signed`, plus a hardware run. Gen 1 then starts identified but read-only, and Safe Mode stays the default for anything
unrecognised.

### Effort estimate (code only, excluding the hardware and capture sessions)

| Work | Size | Risk |
|---|---|---|
| Profile and gate refactor | ~1–2 days | low |
| Codec and UI capability gating | ~2–3 days | low |
| Per-capability Gen 1 enablement | small per item | dominated by hardware verification |

---

## 2. Architecture of the three projects

### 2.1 OpenControl (Android, Kotlin, AGPL-3.0)

Clean architecture in five Gradle modules (`android/settings.gradle.kts`): `:app`, `:ui`, `:domain`, `:data`, `:hardware`. There is no
ViewModel (ADR-048), Hilt is used for DI, and there is **no protobuf runtime**. The wire codec is hand-written (ADR-041).

| Concern | Where | Notes |
|---|---|---|
| Device discovery | `OC/hardware/BudsCompanionPairing.kt:168,378` (CDM name pattern `(?i).*Pixel\s*Buds.*`), `OC/hardware/PairingLogic.kt:77-91` (`chooseBondedOrAsk`) | Name-based. A Gen 1 **will** appear and be selectable. There is no model check at pairing time. |
| RFCOMM | `OC/hardware/RfcommBudsTransport.kt` (`connectWith` :162, `openChannel` :337), `OC/hardware/BudsSdpUuids.kt:43-44` | Sockets are opened **by SDP UUID**: Maestro `25e97ff7-…f7b5`, GFPS `df21fe2c-…7927c`. "DLCI 0x02/0x04" are labels, not hard-coded channel numbers. Since 1.4.0 only Maestro is a session channel; GFPS is opened on demand for Find. |
| Channel demux | `OC/data/codec/CodecRouter.kt:26-33` (`Dlci`), `:118` (`CodecRouter`) | One splitter per channel. |
| HDLC | `OC/data/codec/Hdlc.kt` | pw_hdlc U-frames, control `0x03`, CRC-32 LE. Same as pbpctrl `libmaestro/src/hdlc/`. |
| pw_rpc | `OC/data/codec/PwRpc.kt` (`CALL_ID_UNSOLICITED = 0xFFFFFFFF` at :70), `OC/data/codec/Maestro.kt` | Method ids are 65599 name hashes. |
| Channel selection | `routeMaestro` `CodecRouter.kt:292-300` → `RoutedFrame.MaestroHello` → `BudsRepositoryImpl.kt:759` → `awaitMaestroChannel` :1154 | **Passive.** The app waits up to 3 s (`MAESTRO_ANNOUNCE_WAIT_MS`, :1855) for the buds' unsolicited `GetSoftwareInfo` RESPONSE (`call_id 0xFFFFFFFF`) and uses its `channel_id`. Only channels 19/21/24/26 have known request addresses (`Maestro.kt:131-140`). Any other channel means nothing is sent (ADR-034). |
| Firmware | `OC/data/codec/SoftwareInfo.kt:37` | Field 2 of each entry of payload field 4 is the firmware string. |
| Settings codec | `OC/data/codec/SettingFrame.kt` (`SettingsCodec`) | Allowlists: `Maestro.READABLE_FIELDS` (`Maestro.kt`), `SettingsCodec.WRITABLE_FLAG_FIELDS` (:122). Typed encoders for 7, 12, 13, 17, 29. |
| EQ codec | `OC/data/codec/EqFrameEncoder.kt`, `EqFrameDecoder.kt` | Field 16 (live) is written, `persist=false`, `BudsRepositoryImpl.kt:1072`. Field 18 is read only. |
| Model ID | `OC/data/codec/ModelIdFrame.kt:36` (`PIXEL_BUDS_PRO_2_MODEL_ID = "da2db1"`) | Arrives on the GFPS Message Stream (group 3, code 1) when it opens. Since ADR-068 it is only seen when Find opens that channel. |
| Write gate | `OC/data/SafeModeGate.kt:32-67`, `BudsRepositoryImpl.writeGate` :1440, `refreshObservedSafeMode` :1458 | See §2.1.1. |
| Read/write orchestration | `BudsRepositoryImpl`: `openSession` :849, `writeAncMode` :975, `setEqGains` :1056, generic setting write ~:1270-1300, `readSettings` :1716, `SETTING_READ_ORDER` :1861 | Writes are serialised with `eqMutex` and wait for `RESPONSE`/status. There is a write quarantine after timeouts. |
| UI | `OC/ui/AncScreen.kt`, `ControlsScreen.kt`, `EqScreen.kt`, `SettingsMenu.kt`, `ConnectionScreen.kt:307` (`SafeModeCard`), `OC/app/AncTileService.kt` | Shows every Pro 2 control. There is no capability model. |

#### 2.1.1 Why an unsupported firmware enters Safe Mode

`SafeModeGate.evaluate(firmware, modelIdHex, requireModelId)` (`SafeModeGate.kt:43-55`) refuses a write when any of these is true:

- no firmware was announced;
- any announced firmware is not in `VERIFIED_FIRMWARE`;
- a Model ID other than `da2db1` was seen;
- a Message Stream command was sent without a Model ID on that claim.

`writeGate` is called before **every** write:

| Write | Call site |
|---|---|
| ANC | :987 |
| EQ | :1071 |
| Settings | :1285 |
| Find / Ring | :1421, `requireModelIdOfClaim = true` |

`refreshObservedSafeMode` shows the Safe Mode card as soon as a known firmware contradicts the allowlist. Reads are never gated
(ADR-042 item 3).

On a Gen 1, OpenControl 1.4.0 would therefore (based on the G1-CAP hello, not tested):

- see `release_5.11_signed`;
- show the Safe Mode card;
- refuse every write;
- keep reading.

**OpenControl 1.4.0 is safe to point at a Gen 1 as-is.** That makes it a useful read-only observation tool (Milestone 1).

The process for adding a firmware is `RELEASING.md:313-328` §12 "New Buds firmware":

1. Record the version.
2. Capture the official app's connect burst.
3. Diff it against `release_5.203` with `scripts/pwrpc_decode.py`.
4. Replay the write fixtures with the official app.
5. Only if nothing differs, extend the constant. Otherwise update `PROTOCOL.md`, add an ADR and get maintainer approval.

That runbook is written for a *new firmware on the same model*. A new model needs more (see §6).

#### 2.1.2 Pro 2-specific components

| Component | Where |
|---|---|
| Firmware constant | `SafeModeGate.VERIFIED_FIRMWARE` |
| Model ID constant | `DeviceInformation.PIXEL_BUDS_PRO_2_MODEL_ID` |
| Adaptive mode | `AncMode.ADAPTIVE` and the tile cycle |
| Four-field mode list | `AncModeList` (`OC/domain/BudsSettings.kt`) and `decodeAncModes` |
| Pro 2-only reads | fields 27/28/29 in `READABLE_FIELDS`/`SETTING_READ_ORDER` |
| Head-gesture encoding | values 1/2 (ADR-052) |
| Single-bud press-and-hold writes | one bud per write, copied from the official Pro 2 app (`pressAndHoldRequest`) |
| Channel/address table | 4 observed pairs (`MaestroChannel.KNOWN`) |
| Branding | app name, `PROJECT.md` single-model scope, `ARCHITECTURE.md` §15 |
| Test fixtures | all from Pro 2 captures (`android/data/src/test/...`) |

### 2.2 pbpctrl (Linux, Rust, MIT/Apache-2.0)

Crates: `libmaestro` (HDLC, pw_rpc client, protocol, services), `libgfps` (Fast Pair Message Stream), `cli` (`pbpctrl`).

**Models supported.** The original Pixel Buds Pro is the primary target. The README says "Pixel Buds Pro", and the 2022–2023 history
predates the Pro 2, which launched Sep 2024. Pro 2 support is a **one-line** class-of-device addition (`ba253a7`,
`cli/src/bt.rs:11-12`): `PIXEL_BUDS_CLASS = 0x240404`, `PIXEL_BUDS2_CLASS = 0x244404`.

**Discovery.** `find_maestro_device` (`cli/src/bt.rs:15-35`) accepts the first BlueZ device that has either class of device and
advertises the Maestro UUID. **The two models are not told apart anywhere**, and no capability differences are modelled.

**Channel resolution.** `resolve_channel` (`libmaestro/src/protocol/utils.rs:11-54`) sends `GetSoftwareInfo` with
`call_id 0xffffffff` on channels 18, 19, 21, 23, 24, 26 at once and keeps the first one that answers. Commit `7e75204` (2023-10-14)
says that **on Gen 1 firmware 5.9 the answer arrives with call id 0xffffffff**. That is consistent with the unsolicited hello
OpenControl relies on (SRC).

**Addressing.** `libmaestro/src/protocol/addr.rs:37-41` defines the address as `(src&0xf)<<6 | (dst&0xf)<<10`. The channel table is
at :85-99. These reproduce OpenControl's observed addresses: channel 19 → 3712 = `00 3b`.

**Proto.** `libmaestro/proto/maestro_pw.proto` covers:

- services `Maestro`, `Multipoint`, `EartipFitTest`, `JitterBuffer`, `Dosimeter`;
- `SettingValue` fields 1–19, 21, 22.

Gen 1-era notes in that file (dated with git blame):

| Note | Date |
|---|---|
| Reading settings 8/9/10 and 18 returns status 2 | 2022-12-23 |
| `ANC_STATE` 1 Off, 2 Active, 3 Aware | 2022-12-23 |
| `AncrGestureLoop` fields 1–3 | 2022-12-23/27 |
| Fields 19, 21, 22 | 2023-10-15 |
| **Adaptive (ANC 4, loop field 4)** | **2025-12-08, Pro 2 era (`003a3fc`)** |

**Writes.**

- `MaestroService::write_setting` (`libmaestro/src/service/impls/maestro.rs`) and the CLI (`cli/src/main.rs`) write every setting,
  with no firmware or model check.
- The CLI refuses an ANC loop with fewer than two modes (`AncrGestureLoop::is_valid`).
- EQ values are clamped to ±6.
- Press-and-hold writes **both buds in one message** (`settings.rs:158-179`).
- prost (proto3) **omits false booleans**, so pbpctrl's loop writes on Gen 1 carry only the true fields.

Whether these writes work on Gen 1 is SRC. pbpctrl was developed on Gen 1 and is packaged for Arch and Debian, but this investigation
has not verified it. Hardware differences are not documented beyond the class of device.

### 2.3 PixelBudsMacOS (Swift, MIT)

Layers (`ARCHITECTURE.md`):

- `Maestro`: pure Swift HDLC, pw_rpc and the generated protos (`Sources/Maestro/`);
- `MaestroIOBluetooth`: RFCOMM;
- `PixelBudsBar`: the app.

The proto is pbpctrl's, plus three **speculative** read ids 19/21/22 (`Sources/Maestro/Protos/maestro_pw.proto:102-109`, marked as
such by the author).

**Identification code (traced, not taken from the README):**

- `MaestroChannelOpener.findFirstPairedBuds` (`Sources/MaestroIOBluetooth/MaestroChannelOpener.swift:5-12`): the class of device is
  `0x240404` or `0x244404` **and** the name contains "pixel buds".
- `BudsConnection.Model.from` (`Sources/PixelBudsBar/BudsConnection.swift:35-62`):
  - `0x240404` → Gen 2 if the name contains "Pixel Buds Pro 2", otherwise Gen 1;
  - **any other class of device (including `0x244404`) → `.unknown`.**
  - The comment claims both models report `0x240404`. That contradicts OpenControl's Pro 2 captures (`0x244404`,
    `PROTOCOL.md:143-146`) and the app's own diagnostic `BudsSpike/DeviceFinder.swift:9-24`, which maps `0x244404` to the Pro 2.
  - **Name-based detection is unreliable.** Users can rename devices, and the app's own code is inconsistent here.
- The only feature gated by generation is `supportsAdaptiveAnc` (`BudsConnection.swift:40-42`, used at
  `BudsPopoverView.swift:167` and `SettingsView.swift:218`). Gen 1 shows Off/Active/Aware.

**Channel resolution — do not port.** `ChannelResolver.resolve` (`Sources/Maestro/PwRPC/ChannelResolver.swift:12-65`) probes
`[19, 24, 21, 26, 18, 23]` one at a time. When both buds answer, `isWriteCapable` (:91-120) **reads `gesture_enable` and writes it
back** to choose a channel. That is a write during connection setup, before any firmware check. It conflicts with this task's
constraints and with OpenControl's ADR-034 and ADR-042.

**Settings (`Sources/Maestro/Services/MaestroService.swift:121-258`).** Read/write wrappers exist for:

- ANC 13, multipoint 11, gestures 4, OHD 2, volume EQ 15, gesture control 7, loop 12, EQ 16, balance 17, speech 22, mono 19,
  exposure 21;
- OTTS 14, read only. `BudsViewModel.swift:413-417`: **writing OTTS (14) makes the buds drop RFCOMM.** The generation is not stated,
  but the author's device is a Gen 1 ([Reference/captured-frames-2026-05-16.txt](https://github.com/KashaMalaga/PixelBudsMacOS/blob/0ffd4f9/Reference/captured-frames-2026-05-16.txt), line 1).

**Field learnings worth keeping** (`ARCHITECTURE.md`, `AGENTS.md`):

- status 9 = not worn or in the case;
- status 12 = setting not on this firmware;
- status 2 = not readable;
- **only one `SubscribeRuntimeInfo` per channel** — OpenControl `PROTOCOL.md` §4.5 matches this on the Pro 2;
- a multipoint "half-dead" link needs a unary heartbeat;
- `writeSync` hangs. That one is macOS-specific.

**Gen 1 evidence in the repo.**

- [Reference/captured-frames-2026-05-16.txt](https://github.com/KashaMalaga/PixelBudsMacOS/blob/0ffd4f9/Reference/captured-frames-2026-05-16.txt) holds 6 frames from the author's Gen 1 on `release_5.11_signed` (decoded in §5.2).
- `Tests/MaestroTests/CapturedFramesTests.swift` uses them. Its header gives 2026-05-21; the file says 2026-05-16.
- The [screenshots/popover-gen1.png](https://github.com/KashaMalaga/PixelBudsMacOS/blob/0ffd4f9/screenshots/popover-gen1.png) screenshot shows reads only.
- **No Gen 1 write transcript or log is recorded.** Gen 1 writes are SRC only.

---

## 3. Comparison matrix

| Area | OpenControl (IMPL) | pbpctrl (SRC) | PixelBudsMacOS (SRC) |
|---|---|---|---|
| Original Pro identification | None. The CDM name filter accepts it, then the write gate refuses it (unknown firmware). | CoD `0x240404` + Maestro UUID. Does not tell the models apart. | CoD `0x240404` + name without "Pro 2" (`BudsConnection.swift:52-61`). |
| Pro 2 identification | Firmware `release_5.203` + Model ID `da2db1` (write gate only). | CoD `0x244404`. | Name contains "Pixel Buds Pro 2" (and only with CoD `0x240404`, which contradicts the Pro 2 captures). |
| RFCOMM transport | Android `createRfcommSocketToServiceRecord` by UUID. One unit per connection, GFPS on demand. | BlueZ profile registration (`bt.rs:37-90`). | IOBluetooth by SDP UUID, 5 s write timeout, SDP refresh. |
| Maestro / HDLC | Hand-written. CRC-32. Address from a 4-entry observed table. | `libmaestro/src/hdlc`. CRC-32. Address computed from peers. | `Sources/Maestro/HDLC`. CRC-32 (its [Reference/README.md](https://github.com/KashaMalaga/PixelBudsMacOS/blob/0ffd4f9/Reference/README.md) wrongly says CRC-16). |
| Pigweed RPC | Hand-written `PwRpc.kt`. Passive channel from the hello. | Full client. Parallel probe with `call_id 0xffffffff`. | Actor client. Sequential probe **with a write** (`isWriteCapable`). |
| Protobuf definitions | None compiled (ADR-041). Schemas documented in `REVERSE_ENGINEERING.md` (APK names `qhr`, `qht`, `qju`…). | `maestro_pw.proto` (fields 1–19, 21, 22). | pbpctrl's proto + speculative read ids 19/21/22. |
| ANC read/write | Read field 13 + settings stream. Write `4:{13:n}` n=1..4, gated on the field-12 list (ADR-067). Adaptive always offered. | Read/write field 13 values 1–4. Cycle through loop field 12. | Read/write field 13. Adaptive hidden on Gen 1. |
| EQ read/write | Read 16 (and 18). Write 16, 5×float ±6. | Read/write 16, ±6 clamp. Reading 18 → status 2 (Gen 1 note). | Read/write 16, presets, ±6. |
| Touch controls | 4 (on/off), 7 (hold per bud, one bud per write), 12 (loop, 4 bools always sent). | 4, 7 (both buds per write), 12 (false fields omitted). | 4, 7 (both buds), 12. |
| Multipoint | Field 11 on/off. | Field 11. `Multipoint` service quiet-mode stream (read). | Field 11. Heartbeat for half-dead links. |
| Firmware validation | **Yes.** Allowlist + Model ID, Safe Mode. | None (displays only). | None (displays only). |

### 3a. Reusable, missing, incompatible

**Reusable (behaviour, re-implemented — see §9):**

- OpenControl's own HDLC, pw_rpc, `SoftwareInfo`, EQ, balance, flag and press-and-hold encoders. The wire shapes match pbpctrl's
  Gen 1 proto.
- pbpctrl's address formula, to confirm Gen 1 channel/address pairs.
- pbpctrl's Gen 1 field notes: status 2 on 8/9/10/18; ANC values 1–3.
- MacOS's status-code handling and its "never write 14" finding.

**Missing in OpenControl:**

- a model or capability concept;
- Gen 1 identification;
- a 3-mode ANC list;
- Gen 1 firmware and model constants;
- Gen 1 fixtures.

**Incompatible assumptions:**

- `decodeAncModes` requires exactly 4 fields.
- Adaptive is always offered.
- Reads of 27/28/29 are unconditional.
- MacOS's write-during-probe.
- MacOS's class-of-device claim.
- pbpctrl's omission of false booleans in field 12. Do not copy it; the official encoding is unknown for Gen 1.

---

## 4. Key source references

| Topic | File:line |
|---|---|
| Write gate | `OC/data/SafeModeGate.kt:36` (allowlist), `:43-55` (`evaluate`), `:61-66` (`observed`) |
| Gate callers | `OC/data/BudsRepositoryImpl.kt:987` (ANC), `:1071` (EQ), `:1285` (settings), `:1421` (Find), `:1440-1456` (`writeGate`) |
| Hello → channel → firmware | `OC/data/codec/CodecRouter.kt:292-300`, `OC/data/BudsRepositoryImpl.kt:759-767`, `:1154-1160` |
| Channel/address table | `OC/data/codec/Maestro.kt:131-140` |
| Readable fields | `OC/data/codec/Maestro.kt` (`READABLE_FIELDS`) and `BudsRepositoryImpl.kt:1861-1874` (`SETTING_READ_ORDER`) |
| Settings encoders/decoder | `OC/data/codec/SettingFrame.kt:85-113` (fields), `:122` (writable flags), `:196` (`ancStateRequest`), `:219` (`decode`), `:270-272` (4-field list check) |
| ANC write precondition | `OC/data/BudsRepositoryImpl.kt:975-995` |
| Model ID | `OC/data/codec/ModelIdFrame.kt:31-37` |
| Firmware parse | `OC/data/codec/SoftwareInfo.kt:37-49` |
| ANC domain / tile | `OC/domain/AncMode.kt:28-60`, `OC/app/AncTileService.kt` |
| Pairing name filter | `OC/hardware/BudsCompanionPairing.kt:168,378`, `OC/hardware/PairingLogic.kt:77-91` |
| Policy | `opencontrolpixelbudspro2/DECISIONS.md:2146-2168` (ADR-042), `RELEASING.md:313-328`, `PROJECT.md:64-67`, `AGENTS.md` §6/§12/§15 |
| Device-type mapping in the official app | `opencontrolpixelbudspro2/REVERSE_ENGINEERING.md:1709-1714` |
| pbpctrl | `cli/src/bt.rs:11-35`, `libmaestro/src/protocol/utils.rs:11-73`, `addr.rs:37-115`, `service/settings.rs` (all), `proto/maestro_pw.proto` |
| MacOS | `BudsConnection.swift:35-62`, `ChannelResolver.swift:12-120`, `MaestroService.swift:121-258`, `BudsViewModel.swift:413-417`, [Reference/captured-frames-2026-05-16.txt](https://github.com/KashaMalaga/PixelBudsMacOS/blob/0ffd4f9/Reference/captured-frames-2026-05-16.txt) |

### Device type (key for identification)

`REVERSE_ENGINEERING.md:1709-1714` is a 🟢 code finding in OpenControl's docs. The official app chooses its settings schema by
`GetHardwareInfo` field 1:

| Device type | Code name | Schema |
|---|---|---|
| 1 | PRESTO | `qjn` |
| 2 | STRETTO | `qjt` |
| 4 or 5 | ALLEGRO | `qhr` |
| 6 | LARGO — the Pixel Buds Pro 2 | `qhr` |
| 9 | AG12AG22 | `qhr` |

pbpctrl's Gen 1 settings enum is called `AllegroSettingType`. That makes **Gen 1 = ALLEGRO (type 4 or 5), using the same `qhr` schema
as the Pro 2** a strong 🟡 HYPOTHESIS, but it is not verified. pbpctrl names this field `HardwareInfo.unknown1` and never interprets
it.

---

## 5. Gen 1 vs Gen 2 protocol differences

### 5.1 Table

| Aspect | Pro 2 (OpenControl evidence) | Gen 1 | Gen 1 evidence |
|---|---|---|---|
| Class of Device | `0x244404` (🟢 captures) | `0x240404` | SRC (pbpctrl, MacOS). Hint only; Android apps don't need it. |
| Default name | "Pixel Buds Pro 2" | "Pixel Buds Pro" | Hint only; can be renamed. |
| Fast Pair Model ID | `da2db1` | **unknown** | UNVERIFIED. Must be captured (group 3 / code 1 on GFPS open). |
| `GetHardwareInfo` field 1 | 6 | 4 or 5? | UNVERIFIED (inference). |
| Firmware string | `release_5.203` | `release_5.11_signed` | **G1-CAP** (frame 1). The user's "5.11_signed" is very likely this exact string, but must be checked on the user's unit. |
| Hello entries | entries 1 (case), 2, 3 | Only entries 2 and 3 in the one Gen 1 frame | G1-CAP. Maybe because the case wasn't reachable; `SoftwareInfo.entries` copes either way. |
| Maestro UUID / GFPS UUID | same | same | SRC (pbpctrl requires the Maestro UUID for both classes of device; `libgfps` UUID). |
| HDLC framing, CRC-32 | same | same | G1-CAP (CRC valid on all 6 frames). |
| Address scheme / response bytes | ch 19 `80 a3`, ch 21 `00 a5` | identical | G1-CAP (frames 1–6). |
| Unsolicited hello, `call_id 0xFFFFFFFF` | 🟢 43/43 captures | present | G1-CAP + SRC (`7e75204`). **Not yet proven unsolicited on Gen 1**: the macOS tools probe first. |
| `maestro_pw.Maestro` hash `0x7ede71ea` | same | same | G1-CAP. |
| Settings stream push `4:{13:n}` | same | `4:{13:3}` seen | G1-CAP (frame 5, call id 9). |
| KPI stream `a10a20.kpi.Kpi` `0xbf6c9399` | seen | seen (call id 0x21) | G1-CAP. OpenControl will route it as unidentified, which is harmless. |
| ANC values (field 13) | 1 Off, 2 NC, 3 Transparency, 4 Adaptive | 1–3; **4 not expected** | SRC (pbpctrl 2022 enum; MacOS hides Adaptive). |
| ANC loop (field 12) | always 4 bools | 3 fields expected; presence of field 4 and of false values unknown | SRC (2022 proto had 3 fields). |
| EQ (16) | 5×float ±6 | same shape | SRC. |
| Last-saved EQ (18) | readable | read → status 2 | SRC (pbpctrl note). Don't read it on Gen 1, or tolerate the error. |
| Balance (17) | zig-zag ±100 | same wire (pbpctrl raw 0..200 = −zigzag) | SRC + `PROTOCOL.md:2097-2098`. |
| Hold action (7) | `7{1\|2:{4:{1:a}}}`, one bud per write | same nesting; pbpctrl writes both buds | SRC. Whether a single-bud write is accepted is UNVERIFIED. |
| 11, 15, 19, 22, 2, 4 | flags | exist | SRC (pbpctrl; 19/22 added Oct 2023 on Gen 1). |
| 27/28 case sounds, 29 head gestures | exist | **unknown, probably absent** | UNVERIFIED. |
| 8/9/10 | — | read → status 2 | SRC. |
| 14 OTTS write | not written | **drops the RFCOMM link** | SRC (MacOS, Gen 1 author). Never write. |
| Write when not worn | status 9 | status 9 | SRC (MacOS). |

### 5.2 Decoded Gen 1 frames (G1-CAP)

Source: [PixelBudsMacOS Reference/captured-frames-2026-05-16.txt](https://github.com/KashaMalaga/PixelBudsMacOS/blob/0ffd4f9/Reference/captured-frames-2026-05-16.txt), re-decoded with `scripts/decode_maestro_frames.py`. The CRC-32 is OK
on all six frames.

**Frame 1: the hello.**

```
addr 80 a3 = LeftBtCore → MaestroA (channel 19), ctrl 0x03
RpcPacket{ type=RESPONSE(1), channel_id=19, service=0x7ede71ea (maestro_pw.Maestro),
           method=0x7199fa44 (GetSoftwareInfo), call_id=0xFFFFFFFF,
           payload{ 2: <varint>,
                    4: { 2:{1:"575", 2:"release_5.11_signed"}, 3:{1:"575", 2:"release_5.11_signed"} },
                    5: <fixed64>, 6: 0 } }
```

This is exactly the shape `routeMaestro` turns into `MaestroHello(19, ["release_5.11_signed"], …)`. On the Pro 2, field 6 is the
"not-primary" flag (`PROTOCOL.md` 2026-10-06 note); here it is 0.

**Frame 5: a settings push.**

```
addr 00 a5 = RightBtCore → MaestroA (channel 21)
SERVER_STREAM, service maestro_pw.Maestro, method 0x2821adf5 (SubscribeToSettingsChanges),
call_id 9, payload 22 02 68 03 = 4:{13:3}  → ANC = Transparency/Aware
```

**Frames 2–4 and 6:** `SERVER_STREAM` packets from service `0xbf6c9399`, method `0x92476025` (`a10a20.kpi.Kpi/KpiStream`), call id
0x21, on channel 21.

The hello came on channel 19 and the streams on channel 21. That is consistent with both buds being reachable. OpenControl would use
whichever channel the hello announces, and both 19 and 21 are in its table.

---

## 6. Exact changes required in OpenControl

### 6.1 Governance — must come before code

Each item is a **draft ADR for the maintainer**. An agent may draft it but must not accept it (`AGENTS.md` §6).

- **D-1: scope.** Supersede the `PROJECT.md:64-65` non-goal and `ARCHITECTURE.md` §15 for exactly one extra model, the Pixel Buds
  Pro (Gen 1). This also covers naming: the app is "OpenControl for Pixel Buds Pro 2" (ADR-051). Renaming it, or adding a
  compatibility line, is the maintainer's call.
- **D-2: device profiles.** The model / firmware / capability structure in §6.2. ADR-042 is amended so that gating is per model, per
  firmware and per capability.
- **D-3: Gen 1 identification.** The signals and how conflicts are resolved (§6.3).
- **D-4…D-n: one ADR per Gen 1 write capability**, following the per-feature rule in `AGENTS.md` §6. Each is accepted only after its
  official-app capture and its hardware run (§7, M5).
- `PROTOCOL.md`: a new "Gen 1 (Pixel Buds Pro)" subsection. Entries start 🟡 or ⚪ with the frame evidence from §5. The maintainer
  promotes them.

### 6.2 Domain: device profile — new types in `:domain`

```kotlin
enum class BudsModel { PIXEL_BUDS_PRO_2, PIXEL_BUDS_PRO_GEN1, UNKNOWN }

enum class Capability {
    ANC_MODE, ANC_ADAPTIVE, ANC_HOLD_LIST, EQ, TOUCH_CONTROLS, PRESS_AND_HOLD, MULTIPOINT,
    IN_EAR_DETECTION, VOLUME_EQ, BALANCE, MONO, CONVERSATION_DETECTION, CASE_SOUNDS, HEAD_GESTURES, FIND,
}

/** What this app knows a model has (read) and has verified writing (write) on one firmware. */
data class FirmwareProfile(val firmware: String, val writable: Set<Capability>)

data class DeviceProfile(
    val model: BudsModel,
    val readable: Set<Capability>,          // what to read / show at all
    val ancModes: Set<AncMode>,             // Gen 1: OFF, ACTIVE, TRANSPARENT
    val readableFields: List<Int>,          // the per-model ReadSetting sweep
    val firmware: Map<String, FirmwareProfile>,
)
```

Registry in `:data`, e.g. `data/DeviceProfiles.kt`:

- **`PIXEL_BUDS_PRO_2`**:
  - today's behaviour, written as data;
  - `release_5.203 → writable = all current capabilities`;
  - `readableFields = SETTING_READ_ORDER`.
- **`PIXEL_BUDS_PRO_GEN1`**:
  - `readable` is limited to Gen 1 evidence: no `ANC_ADAPTIVE`, `CASE_SOUNDS` or `HEAD_GESTURES`;
  - `readableFields = [2, 4, 7, 11, 12, 13, 15, 17, 19, 22]`;
  - `release_5.11_signed → writable = ∅` at first. This is the key safety property: Gen 1 is *identified* but still read-only.
  - Capabilities are added one at a time in M5.

### 6.3 Identification — new pure `DeviceIdentifier` in `:data`, unit-tested

Inputs, all already received or sent by the app today:

1. **Hello firmware strings** (`MaestroHello.firmware`).
2. **`GetHardwareInfo` field 1** (device type). The app already sends one `GetHardwareInfo` per Connect (ADR-058,
   `Maestro.getHardwareInfoRequest`). Today `OC/data/codec/HardwareInfo.kt` reads only field 7. Extend it to read field 1 as well.
3. **Fast Pair Model ID**, if seen on this connection.
4. Class of device and name: diagnostic only, never decisive.

Rules:

- Each firmware string in the registry belongs to exactly one model.
- **Pro 2:** firmware ∈ Pro 2 set, device type ∈ {6, absent}, Model ID ∈ {`da2db1`, absent}.
  - This is today's rule plus a device-type cross-check, so Pro 2 behaviour is preserved.
  - Whether "absent" stays allowed is for the maintainer to decide in D-3.
- **Gen 1:** firmware ∈ Gen 1 set **and** device type = the value captured from Gen 1 (expected 4 or 5).
  - For `FIND`, also Model ID = the captured Gen 1 Model ID.
  - Both signals are required because Gen 1 is new and its firmware namespace is not proven disjoint from the Pro 2's.
- Any contradiction or missing required signal → `UNKNOWN` → Safe Mode (today's card, extended to show the model and device type).

### 6.4 Gate — `SafeModeGate.kt`

Change `evaluate(firmware, modelIdHex, requireModelId)` to `evaluate(identity, capability, requireModelId)`. The result is Allowed only
when all of these hold:

- the model is known;
- every announced firmware has a `FirmwareProfile` for that model;
- the capability is in `writable`.

Keep `VERIFIED_FIRMWARE` as a derived view for the Pro 2, so the existing tests and docs still make sense.

Call-site changes in `BudsRepositoryImpl`:

- `writeGate(requireModelIdOfClaim)` becomes `writeGate(capability, requireModelIdOfClaim)` at :987 (`ANC_MODE`), :1071 (`EQ`),
  :1285 (the capability mapped from the setting field) and :1421 (`FIND`).
- `writeGate` must also wait, bounded like today's firmware wait, for the device type when the model needs it.

### 6.5 Codec and repository

| Change | Where | Gen 1 | Pro 2 regression |
|---|---|---|---|
| Profile-aware `decodeAncModes` | `SettingFrame.kt:270` | Accept the Gen 1 shape **as captured**: 3 fields, or omitted falses if the capture shows that. `adaptive = false`, unsupported. | Pro 2 keeps the exact 4-field rule. |
| `ancModeListRequest` encoding | `SettingFrame.kt` | Encode exactly the official Gen 1 app's bytes (fixture). Do not assume 4 fields. | unchanged |
| `ancStateRequest` | `SettingFrame.kt:196` | Refuse `ADAPTIVE` if it is not in `profile.ancModes`. | unchanged |
| Read sweep | `BudsRepositoryImpl.kt:1861` | Use `profile.readableFields`. Gen 1 skips 27/28/29 and never reads 18. | same list as today |
| Readable/writable allowlists | `Maestro.READABLE_FIELDS`, `WRITABLE_FLAG_FIELDS` | Make them per profile. The global sets stay the upper bound. | unchanged |
| Press-and-hold | `pressAndHoldRequest` | Single-bud or both-bud form per the Gen 1 official-app capture. | unchanged |
| Channel table | `Maestro.kt:131-140` | Add only channel/address pairs **observed on Gen 1** (expected to be the same 19/21/24/26). | unchanged |
| Never-write list | `SettingsCodec` | Assert at the type level that 1, 3, 5, 6, 8, 9, 10, 14 can never be written, for every profile. | — |

### 6.6 UI

- `AncScreen`, the Quick Settings tile (`AncMode.nextInTileCycle`, `AncTileService`) and the Connection tab show only
  `profile.ancModes`. The Gen 1 cycle is NC → Transparency → Off.
- `ControlsScreen` and `SettingsMenu` hide controls whose capability is not in `profile.readable`, and disable controls not in
  `writable`, with a reason line.
- The Info tab shows the model, the device type and the firmware.
- The Safe Mode card text (`ConnectionScreen.kt:322`) distinguishes "unknown model" from "known model, unverified firmware" and
  "known model, this control not yet verified".

### 6.7 Tests (follow `AGENTS.md` §11: fixtures must be real bytes from a registered capture)

- `DeviceIdentifierTest`: every combination of the four signals, including conflicts. Gen 1 with an unknown device type → `UNKNOWN`.
- `SafeModeGateTest`: per-capability matrix. **Every existing Pro 2 case must pass unchanged.**
- Codec tests with Gen 1 fixtures from the project's own Gen 1 capture (M2).
  - The MacOS frames may be used only as clearly labelled supplementary structural tests. They have no btsnoop log or frame number
    in a registered capture, and MIT attribution is required (§9).
- Fuzz tests: extend the existing `CodecRouter` fuzzing to Gen 1-shaped field-12 payloads.

**No new dependencies are needed.**

---

## 7. Implementation sequence

Every milestone ends with a stopping point for maintainer review.

### M0 — Governance (no code)

- Draft D-1, D-2 and D-3.
- Draft the `PROTOCOL.md` Gen 1 subsection, labelled 🟡/⚪.
- Get maintainer decisions.
- Exit: D-1 and D-2 accepted.

### M1 — Read-only observation of the user's Gen 1 (no code changes, no writes)

1. Install the **unchanged** OpenControl 1.4.0 and connect to the Gen 1. Expected: Safe Mode card, firmware shown. Export the debug
   log.
   - It confirms: the firmware string; that the hello arrives *without a request* (OpenControl never sends `GetSoftwareInfo`); the
     channel id; the read sweep's statuses for each field, including 27/28/29; field 12's raw shape; the `GetHardwareInfo` answer.
   - These are reads only. Reading 27/28/29 on Gen 1 is an unverified *read*. The expected worst case is an error status. Accept this
     risk explicitly, or skip step 1 and rely on step 2.
2. HCI-snoop capture of the **official Pixel Buds app** connecting to the Gen 1 (GrapheneOS: sandboxed Play in a separate user or
   profile, or another phone). Register it as `CAP-NNN`.
   - It shows the connect burst, the Model ID frame on GFPS, and `GetHardwareInfo` field 1.
3. Optional cross-check on Linux: `pbpctrl show software|hardware|runtime` and `pbpctrl get <setting>` (reads only).

Exit: the Gen 1 firmware string, device type, Model ID and channel(s) are recorded in a `CAP-NNN-FINDINGS.md`.

### M2 — Official-app write captures on Gen 1

Capture, filmed, with HCI snoop, the official app performing each write:

- ANC → each of Off/NC/Transparency;
- the hold-loop list;
- EQ (one band change + a preset);
- touch controls on/off;
- press-and-hold per bud;
- multipoint;
- in-ear detection;
- volume EQ;
- balance;
- mono;
- conversation detection.

These bytes become the Gen 1 fixtures. Writes by Google's own app are the only "verified" source of write bytes. The app will not
send anything that differs from them.

### M3 — Profiles, identification and the gate refactor

- Implement §6.2–6.4 with Gen 1 `writable = ∅`.
- Run all Pro 2 tests.
- Pro 2 hardware regression run (§8.2).
- Gen 1 hardware run: Gen 1 identified, read-only, the card says so.
- Exit: Pro 2 unchanged on hardware; Gen 1 identified with zero writes sent (verified in the debug log or snoop).

### M4 — Gen 1 reads and UI capability gating

- Implement §6.5 (decoders, read sweep) and §6.6.
- Exit: every Gen 1 read value matches what the official app shows, on hardware.

### M5 — Enable Gen 1 writes one capability at a time

Order:

1. ANC mode
2. EQ
3. touch on/off
4. press-and-hold
5. hold-loop
6. multipoint
7. in-ear, volume EQ, balance, mono, conversation detection
8. Find (needs the Gen 1 Model ID)

For each capability:

- the encoder output equals the M2 fixture byte for byte (unit test);
- its ADR is accepted;
- the capability is added to `release_5.11_signed.writable`;
- a hardware run (§8.1) verifies it.

One capability per pull request.

### M6 — Release

- Docs: `README.md` "What it does" per model, the Safety section, `ARCHITECTURE.md` §5a with a per-model verified column,
  `RELEASING.md` §12 (per-model runbook), `CHANGELOG.md`.

---

## 8. Validation plan

### 8.1 Gen 1 on `release_5.11_signed`

Record a debug-log export and, where marked, an HCI snoop for every step. A step passes only with evidence (frame number or log line)
written into a `CAP-NNN-FINDINGS.md`.

| ID | Priority | Test | Pass criteria | Evidence |
|---|---|---|---|---|
| G1-ID-01 | 1 | Pair through CDM. Connect. | Model shown as "Pixel Buds Pro (Gen 1)". Device type and firmware shown. | Log: hello channel, firmware, `GetHardwareInfo` field 1. |
| G1-ID-02 | 1 | Same, with the buds renamed to "Pixel Buds Pro 2". | Still Gen 1 (the name is ignored). | Log. |
| G1-ID-03 | 1 | Unit test: Gen 1 firmware + Pro 2 device type. | `UNKNOWN`, Safe Mode. | Test. |
| G1-CON-01 | 2 | Connect / disconnect ×10, Bluetooth off/on, buds in/out of the case. | No crash. State follows Android. Hello received each time within 3 s. | Log. |
| G1-CON-02 | 2 | Multipoint: the phone and a second device both connected; move audio focus. | Session recovers (existing `SessionReopener`). No stale "connected". | Log + snoop. |
| G1-CON-03 | 2 | GrapheneOS with and without sandboxed Play (GFPS contention, ADR-067/068). | Maestro session unaffected. | Log. |
| G1-INFO-01 | 3 | Info tab: firmware per entry, serials (field 7). | Matches the official app. | Screenshot + log. |
| G1-BAT-01 | 3 | Battery L/R/Case, charging, in-case placement from `SubscribeRuntimeInfo`. | Matches the official app ±1 %. No second runtime subscription. | Log. |
| G1-ANC-R-01 | 4 | Read ANC on connect. Change it with a touch gesture on the bud. | UI follows the settings-stream `4:{13:n}`. Adaptive never offered. | Log. |
| G1-ANC-W-01 | 5 | (After M5 ANC.) Tap each of Off/NC/Transparency, worn. | `RESPONSE` OK. Stream echo `13=n`. Audible change. Official app agrees. | Snoop + film. |
| G1-ANC-W-02 | 5 | Same with both buds in the case / not worn. | Status 9 surfaced as "not worn". No retry storm. UI reverts. | Log. |
| G1-ANC-W-03 | 5 | Tile cycle. | NC → Transparency → Off only. | Film. |
| G1-EQ-01 | 6 | Read EQ. Set one band. Apply a preset. Reconnect. | Bytes equal the M2 fixture. Value persists across reconnect. Field 18 not read. | Snoop. |
| G1-TC-01 | 7 | Touch on/off; press-and-hold L and R; hold-loop with 2 and 3 modes. | Bytes equal the fixtures. The physical gesture behaves as set. A loop with fewer than 2 modes is refused before sending. | Snoop + film. |
| G1-MP-01 | 8 | Multipoint off → on with two hosts. | `RESPONSE` OK; behaviour changes. | Snoop. |
| G1-OTH-01 | 8 | In-ear detection, volume EQ, balance ±100/0, mono, conversation detection. | Each: bytes equal the fixture, `RESPONSE` OK, and the effect is observed. | Snoop. |
| G1-NEG-01 | all | Grep the snoop for `WriteSetting` with fields 1, 3, 5, 6, 8–10, 14, 18, 27–29. | **Zero.** | Snoop (`scripts/pwrpc_decode.py`). |
| G1-NEG-02 | all | A firmware other than `release_5.11_signed` (simulated in a unit test). | Safe Mode. Zero writes. | Test. |

### 8.2 Pro 2 regression on `release_5.203`

- All existing unit tests unchanged and green, including `SafeModeGateTest`, `SettingsCodecTest`, `BudsRepositoryImplTest` and the
  fuzz tests.
- Re-run `APP_TESTPLAN.md` with a CAP run equivalent to `CAP-079`. Check:
  - identification shows Pro 2 with device type 6;
  - every write still allowed and byte-identical to the fixtures;
  - Adaptive present;
  - fields 27/28/29 still read and written.
- A Pro 2 with a simulated unknown firmware still goes to Safe Mode.

### 8.3 Status at the time of writing

| Item | IMPL | SRC | G1-CAP | G1-HW |
|---|---|---|---|---|
| HDLC / CRC-32 / address scheme | ✅ | ✅ | ✅ | ☐ |
| Unsolicited hello, firmware string | ✅ (Pro 2) | ✅ | ✅ (unsolicited not proven) | ☐ |
| Gen 1 identification | ❌ | partial (CoD/name) | — | ☐ |
| Battery / runtime info | ✅ (Pro 2) | ✅ | — | ☐ |
| ANC read | ✅ (Pro 2) | ✅ | ✅ (stream push 13=3) | ☐ |
| ANC write (1–3) | ✅ (Pro 2) | ✅ | — | ☐ |
| EQ read/write | ✅ (Pro 2) | ✅ | — | ☐ |
| Touch / hold / loop | ✅ (Pro 2) | ✅ | — | ☐ |
| Multipoint | ✅ (Pro 2) | ✅ | — | ☐ |
| Head gestures / case sounds on Gen 1 | — | — | — | ☐ (expected absent) |
| Gen 1 Model ID / device type | — | — | — | ☐ |

**Nothing is verified on the user's Gen 1 yet.**

---

## 9. Licensing

- **OpenControl is AGPL-3.0-or-later.** Every Kotlin file needs the AGPL header (`AGENTS.md` §12). Project policy is stricter than
  the licences require: **no code may be copied** from pbpctrl, the official APK or any other source. Only behaviour (wire formats,
  field numbers) is re-implemented, with attribution in docs and comments (`AGENTS.md` §12, `PROJECT_RULES.md` §8 rule 20,
  ADR-002).
- **pbpctrl (MIT OR Apache-2.0).** Both licences are compatible with AGPL-3.0. Copying is legally possible with the notices kept,
  but the project forbids it. Use pbpctrl as a reference: cite file and line for field numbers, the address formula and the Gen 1
  status-2 notes. Per `DESKRESEARCH_FINDINGS.md:1090-1098`, pbpctrl's names count as "APK names", not as independent evidence.
- **PixelBudsMacOS (MIT, © 2026 Jesús Ramírez Ramos).** The same reasoning applies.
  - Its `Reference/` files are pbpctrl copies (Apache/MIT).
  - The six Gen 1 frames are device output, but they come from the MIT repo's text file. If they are used as supplementary test
    bytes, keep the MIT notice and the attribution in the test file, and label them as external. Primary fixtures must come from the
    project's own registered capture.
- Official APK: only behaviour, never code (already project law).
- **No new dependencies are proposed.**

---

## 10. Risks, unknowns and open research

| # | Risk / unknown | Impact | Mitigation / research |
|---|---|---|---|
| R1 | Gen 1 Fast Pair Model ID unknown | Find cannot be gated for Gen 1 | M1 capture of GFPS open. Keep Find disabled until it is known. |
| R2 | Gen 1 device type unknown (4 or 5 assumed) | Identification | M1: read `GetHardwareInfo` field 1. Don't hard-code 4/5 before then. |
| R3 | Hello may not be unsolicited on Gen 1 | The app never learns the channel. Every write fails safely with `MaestroChannelUnknown`, but nothing works. | M1 step 1 shows it directly. If absent, a request-based fallback (pbpctrl-style probe, **reads only**) needs its own ADR. Never adopt MacOS's write-probe. |
| R4 | Field 12 shape on Gen 1 (3 fields? false values omitted?) | ANC writes blocked, or the wrong encoding | M1 read + M2 official write capture. Encode exactly like the official app. |
| R5 | Single-bud press-and-hold write on Gen 1 | A wrong side, or a reset of the other side | M2 capture decides the form. |
| R6 | Writing 14 (OTTS) drops the link; other unknown "dangerous" fields (1 auto-OTA, 3/6 OOBE, 5 diagnostics) | Lost link or a changed bud state | Type-level never-write list (§6.5) + G1-NEG-01. |
| R7 | Firmware namespace overlap between models | Wrong profile | Both signals required for Gen 1 (§6.3). |
| R8 | Gen 1 firmware updates after 5.11 | Safe Mode again (by design) | Per-model `RELEASING.md` runbook. |
| R9 | The official app on GrapheneOS needs Play Services | M1/M2 captures are harder | Sandboxed Play in a secondary profile, or another Android phone. Captures can be made on any phone. |
| R10 | Factory-reset recovery for Gen 1 | Recovery if a write misbehaves | The Pro 2 procedure is in the README. Confirm Google's Gen 1 procedure before M5 and document it. |
| R11 | Multipoint half-dead links (seen by MacOS) | Stale UI | Already handled by OpenControl's reopener on the Pro 2. Verify on Gen 1 (G1-CON-02). |
| R12 | Project scope and name (single-model app "for Pixel Buds Pro 2") | The work may not be accepted | M0 governance first. Alternative: a maintained fork. |
| R13 | MacOS CoD/name logic contradicts the Pro 2 captures | Misleading reference | Don't port it. Use device type + firmware. |
| R14 | The third-party frames may not represent the user's unit (date mismatch 05-16 vs 05-21; probe traffic unknown) | Over-confidence | Treat them as G1-CAP hints only. M1 is authoritative. |

### Outstanding research checklist (all read-only)

1. Gen 1 `GetHardwareInfo` full answer (field 1, plus fields 2/5/6).
2. Gen 1 Model ID (GFPS group 3 / code 1).
3. Whether the hello arrives without any request; which channel(s).
4. Field-by-field `ReadSetting` status on Gen 1 for 1–29.
5. The official app's Gen 1 connect burst and every write's bytes (M2).
6. Whether the Gen 1 official app exposes conversation detection, volume EQ and mono on `release_5.11_signed`. This decides `readable`.

---

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/GEN1_SUPPORT_INVESTIGATION.md - https://tedsluis.github.io/opencontrolpixelbudspro2/GEN1_SUPPORT_INVESTIGATION
