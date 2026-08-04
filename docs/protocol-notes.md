# **Protocol Notes: Pixel Buds Pro 2 (libmaestro / libgfps)**

**Status:** Living document — single source of truth for reverse-engineered protocol

knowledge, kept separate from implementation code so it can be versioned and corrected

independently. Every entry below must carry a **confidence level** (§2.1) and, where

possible, a reference to how it was verified (§6).

## **0\. Document Metadata**

| Field | Value |
| :---- | :---- |
| Last verified against firmware | release\_5.203 |
| Primary source | qzed/pbpctrl (Linux/Rust), commit/tag: *pin this* |
| Secondary sources | Official App Screenshots (2026-07-30), community discussions, pbtk-extracted schemas |
| Maintainer verification method | Android btsnoop\_hci.log comparison (§6) |
| Last updated | 2026-07-30 |

**Rule:** any change to this document that comes from your own testing (not from pbpctrl) must be marked \[VERIFIED-LOCAL\] with a date, so it's clear which knowledge came from the upstream project versus your own empirical confirmation.

## **1\. Overview**

This document is the single source of truth for the reverse-engineered libmaestro and libgfps protocols used by the Google Pixel Buds Pro 2\. Knowledge here is derived primarily from the qzed/pbpctrl project (Linux/Rust), community discussion, and our own empirical verification via Android's Bluetooth HCI snoop log — adapted for the Android Kotlin implementation described in ARCHITECTURE.md.

This document intentionally does **not** contain Android/Kotlin implementation details (those live in ARCHITECTURE.md §5) — it contains only protocol facts: what bytes mean, not how we structure the code that handles them.

## **2\. RFCOMM Envelope (Framing Structure)**

All communication over the BluetoothSocket (RFCOMM/SPP) is encapsulated in a proprietary framing envelope. The raw byte stream must be parsed into discrete packets before the payload can be handed to the Protobuf deserializer.

### **General Frame Layout**

\+------------+-----------------+------------------+---------------------+------------------+  
| Magic (?B) | Payload Length  | Channel / Msg ID | Protobuf Payload    | Checksum (opt.)  |  
\+------------+-----------------+------------------+---------------------+------------------+

| Field | Size | Notes | Confidence |
| :---- | :---- | :---- | :---- |
| Magic Bytes | TBD (commonly 1B, e.g. 0x5A) | Marks start-of-frame; needed to resync a buffered stream after a partial/corrupt read | 🟡 Medium — confirm exact value(s) against pbpctrl source before implementing FrameDecoder |
| Payload Length | 2 bytes (16-bit) | Size of the protobuf payload *only* — confirm whether it includes the Channel/Msg ID byte(s) or not | 🟡 Medium |
| Channel / Message ID | TBD size | Selects which .proto message handler decodes the payload | 🟡 Medium |
| Protobuf Payload | variable | Serialized libmaestro protobuf message | 🟢 High (protobuf itself is self-describing once you have the right .proto) |
| Checksum/CRC | optional, TBD | Integrity check; algorithm (CRC16? XOR? none for some channels?) not yet confirmed | 🔴 Low — must verify empirically |

**Handling rule (unchanged from AGENTS.md/ARCHITECTURE.md):** any checksum mismatch, or any frame that fails to parse against the magic/length invariants, is dropped silently and surfaced internally as BudsError.MalformedFrame — never a crash, never a best-effort guess at the payload.

### **2.1 Confidence Level Legend**

Use this legend consistently across this document:

* 🟢 **High** — directly confirmed in pbpctrl source code, or \[VERIFIED-LOCAL\] against a real device with an HCI snoop capture.  
* 🟡 **Medium** — inferred from pbpctrl documentation/behavior but not yet confirmed against raw bytes ourselves, or based on a related/older Pixel Buds generation.  
* 🔴 **Low** — community speculation, undocumented, or extrapolated from a different device family. Treat as a hypothesis to test, not a fact to implement against blindly.

## **3\. Protobuf (.proto) Definitions**

The device communicates using serialized Protocol Buffers. Schemas are typically extracted from the official companion app APK using tools like pbtk.

### **3.1 Known Schema Files**

| File | Purpose | Confidence |
| :---- | :---- | :---- |
| maestro\_pw.proto | Core control messages, routing, generic request/response envelope | 🟢 High |
| anc\_settings.proto | ANC / Transparency / Adaptive mode enum | 🟢 High |
| eq\_settings.proto | 5-band equalizer definitions, presets | 🟢 High |
| hardware\_status.proto | Battery / hardware telemetry query-response | 🟡 Medium |

## **4\. Feature Status & Commands**

### **4.1 Confirmed Working (per upstream pbpctrl & UI captures)**

| Feature | Detail | Confidence |
| :---- | :---- | :---- |
| ANC — Off |  | 🟢 High |
| ANC — Active (Noise Cancelling) |  | 🟢 High |
| ANC — Aware (Transparency) |  | 🟢 High |
| ANC — Adaptive | Confirmed present in firmware release\_5.203 | 🟢 High |
| EQ — 5-Band Custom | Bands: Low Bass, Bass, Mid, Treble, Upper Treble | 🟢 High |
| EQ — Presets | Default, Heavy Bass, Light Bass, Balanced, Vocal Boost, Clarity, Last Saved | 🟢 High |

**Command opcode table** *(Pending extraction from btsnoop\_hci.log)*:

| Command | Channel/Msg ID | Protobuf message | Direction | Confidence |
| :---- | :---- | :---- | :---- | :---- |
| Set ANC mode | TBD | AncCommand (name TBD) | App → Buds | 🔴 Low |
| ANC state notification | TBD | TBD | Buds → App | 🔴 Low |
| Set EQ band values | TBD | TBD | App → Buds | 🔴 Low |

### **4.2 Toggles & Secondary Features (Backlog)**

Based on official UI analysis (firmware release\_5.203), the following features exist and require protobuf mapping:

* Conversation Detection (Gespreksdetectie)  
* Multipoint Bluetooth  
* Touch & Hold customization (per bud: ANC cycle or Digital Assistant)  
* In-ear detection (In-eardetectie)  
* Volume EQ (Volume-equalizer)  
* Volume Balance (L/R Balance slider)  
* Case Sounds (Oordopjes terugplaatsen, Andere meldingen)  
* Head Tracking (Hoofdbewegingen gebruiken)

### **4.3 Experimental / Hardware-Dependent (Battery)**

**Detailed battery metrics (Left / Right / Case):** Official UI confirms the device sends independent telemetry for Left, Right, and Case. Android native ACTION\_BATTERY\_LEVEL\_CHANGED often merges this into one value. Therefore, libmaestro HardwareStatus query is the **primary** target for accurate battery data.

1. **libmaestro HardwareStatus query** — poll a specific protobuf request over the RFCOMM channel. Confidence: 🟡 Medium (UI proves data exists, opcode pending).  
2. **HFP AT commands** (AT+IPHONEACCEV) — Fallback if protobuf fails. Confidence: 🟡 Medium.  
3. **BLE Battery Service (0x180F)** — Standard GATT characteristic. Confidence: 🔴 Low.

## **5\. Firmware / Version Compatibility Matrix**

| Firmware version | Known protocol differences | Source |
| :---- | :---- | :---- |
| release\_5.203 | ADAPTIVE ANC mode present; 5-band EQ; L/R/Case independent battery | \[VERIFIED-LOCAL\] (Screenshot UI Analysis, 2026-07-30) |

## **6\. Verification Methodology (HCI Snoop Log)**

*(See previous revisions for Wireshark procedure)*
