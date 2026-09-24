# 0045_MAINTENANCE_RESULT_2026_09_24.md — Processing every finding of the 0044 audit into the project files and the app

**Number:** 0045
**Category:** MAINTENANCE
**Date:** 2026-09-24
**Title:** Validate and process every recommendation, finding, improvement and proposal of `ai-sessions/0044_AUDIT_RESULT_2026_09_23.md`, fix the Android app accordingly, with maintainer approval wherever `AGENTS.md` §6 / `PROJECT_RULES.md` require it
**Status:** complete

> All six phases done. Every 0044 finding was processed; none was left open (ledger §3.2). Nothing is committed yet: that waits for
> the maintainer's go-ahead in chat. The whole session ran in the main session; no subagent was used for any read or write.

---

## 0. Plain-language summary

1. **Every 0044 finding is handled.** Each one was checked again against the captures, the code or the official spec (0044 was not
   taken on trust), then fixed, or decided by you in chat and then fixed. None was rejected outright. Two were narrowed: one
   dependency 0044 called unused is actually needed (APP-12), and several of 0044's line numbers were wrong (noted in the ledger).
2. **The biggest correction: Case battery.** On 2026-09-22 we recorded as FACT that the Buds push the Case level without being asked.
   That was wrong where it matters. Every push right after a channel opens answers a `0e 04 00 00` request from Google Play services
   (13 of 13 opens), and all 8 of our app's silent attempts got nothing. With your approval the FACT was corrected, and ADR-039 makes
   the app send that request. This is the most likely reason the Case line never worked. It still needs a hardware re-test (§9).
3. **Four new decisions (your approvals, 2026-09-24):** ADR-039 (Case request), ADR-040 (HFP battery is not usable by an app; removed),
   ADR-041 (hand-written codec, no protobuf runtime), ADR-042 (Safe Mode: ANC/EQ/Find writes go only to firmware `release_5.203`
   plus Fast Pair model `da2db1`; anything else gets a read-only card). Eleven older ADRs got a dated Update, pointer or title note.
4. **App fixes:** ACK/NAK from the Buds are now understood and shown (a refused ANC change says so instead of pretending it worked). A
   half-received frame can no longer jam later frames. Button taps no longer die with the screen. The notification service lives in
   the app scope. `BLUETOOTH_SCAN` is gone. The dock line says "seem to be in the case" and marks early readings as provisional.
   "No paired Buds" has its own message.
5. **Tooling:** a GitHub Actions workflow now builds, tests and lints the app and fails on any `INTERNET` permission.
   `lint_docs.py` passes (exit 0) and skips `.venv`. Capture `.txt`/`.png`/`.jpg` files are now in Git LFS, without rewriting history.
6. **Coverage 0044 had not reached (Phase 5)** is done. It found and fixed: a wrong TODO line of mine (the app does *not* send
   "ring both"), stale KDoc, a stale HFP cadence paragraph, pw_hdlc address values with no correction pointer, a missing
   `pwrpc_decode.py` update, and `advanceUntilIdle()` still used 36 times in the repository test. One item needed you: the
   ADR-017 note in `REVERSE_ENGINEERING.md` (you chose "dated note only").
7. **Tests:** 1533 unit tests (data 1473, hardware 49, domain 11), 0 failures. Lint: 0 errors, plus the 2 older `:app` warnings.
   Seven mutation checks, all caught. **Nothing is hardware-verified.** Re-test instructions are in §9.

---

## 1. Reading done before acting (prompt §0)

`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md` (full), `PROTOCOL.md` (full, 3015 lines), `DECISIONS.md` (all 38
ADRs, full), `TODO.md` (full), `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`, the 0044 prompt and result (full), the 0042 and
0043 results (full), `id_registry.csv` (full). `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` and `CHANGELOG.md`
are read linearly in Phase 5 (they are also Phase-5 coverage items) and **before** any Phase-3 edit to those three files.

## 2. External sources fetched this session (2026-09-24)

| # | URL | What it says (quoted/paraphrased from the fetched page) | Used for |
|---|---|---|---|
| E1 | developers.google.com/nearby/fast-pair/specifications/extensions/mac | A MAC-protected message ends with an 8-byte message nonce (octets n+1…n+8) and an 8-byte MAC (n+9…n+16); MAC = HMAC-SHA256 with K = account key ‖ 48 zero bytes over (session nonce ‖ message nonce ‖ message); the **session nonce** is a Device information event, code **`0x0A`**, 8 bytes, "should be generated and sent to the Seeker when Message Stream connects"; on a wrong MAC "the Provider shall send a NAK with the error reason, 0x3". | PR-MAC |
| E2 | …/extensions/hearablecontrols | Set ANC state `0x12`: every byte marked MAC **Y**, ACK **Y**; bytes 8–23 are labelled **"Reserved bytes"**, present iff the length is `0x14`. Notify `0x13`: version byte "`0x02` for this version", then UI toggles, Settable toggles, Current state; on "Settable": "Any or all of the UI toggle bits above may also be set here, to indicate which are currently enabled." | PR-MAC (narrowed), D-3/P9 |
| E3 | …/extensions/deviceaction | Ring `04 01`: `0x00` all stop, `0x01` ring right, `0x02` ring left, **`0x03` ring both left and right**; optional 2nd byte = timeout in seconds; no ACK within 1 s (or a NAK) ⇒ the Seeker assumes the action is unsupported. | PR-RING |
| E4 | …/extensions/deviceinformation | Codes `0x01` Model ID … `0x03` Battery updated (same bytes as the advertisement's s+2..s+4) , `0x04` Remaining battery time (minutes), `0x09` Firmware version, `0x0B` Current FHN ephemeral identifier. **No `0x0A` row on this page** (it is defined on the MAC page, E1). | PR-MAC, SYN-1 |
| E5 | …/extensions/batterynotification | No trigger, cadence or display-duration statement (no 8 s / 20 s); "the Provider should not include raw battery data in the advertisement all the time"; battery "can be sent via Message Stream when connected". | SP4, GOV-6, PR-11 |
| E6 | source.android.com/docs/core/interaction/sensors/head-tracker-hid-protocol | Head-tracker HID sensor: usage page `0x20`, usage `0xE1`, description string `#AndroidHeadTracker#<version>`; used by Android's spatial-audio/head-tracking; v2.0 (Android 15+) adds a transport property (ACL/ISO). | PR-HID |
| E7 | …/extensions/acknowledgement | ACK `FF 01` = echoed group, code (+ optional state); NAK `FF 02` = reason first, then echoed group, code; reasons `0x00` not supported, `0x01` busy, `0x02` not allowed in current state, `0x03` incorrect MAC, `0x04` redundant device action | APP-3 (`MessageStreamReply.kt`) |
| E8 | developer.android.com/develop/background-work/background-tasks/broadcasts | "Some system broadcasts come from highly privileged apps, such as Bluetooth and telephony, that are part of the Android framework but don't run under the system's unique process ID (UID)." A `RECEIVER_NOT_EXPORTED` receiver "is able to receive some system broadcasts … but not broadcasts from the highly privileged apps." | Phase 5 external check: supports `ai-sessions/0042`'s switch to `RECEIVER_EXPORTED` (`ARCHITECTURE.md` note) |
| E9 | Bluetooth RFCOMM with TS 07.10 (Part F:1), rfc.nop.hu/bluetooth/rfcomm.pdf and the dcs.ed.ac.uk copy | §5.4: the DLCI comes from the server channel plus the session's direction bit, "thereafter used for all packets in both directions between the endpoints"; §2.3.2: "each multiplexer session is using its own L2CAP channel ID", multiple sessions for endpoints in *different* devices | Phase 5 external check: backs the protocol half of "one RFCOMM connection per (device, channel)"; the "stack closes the incumbent too" half stays Android behaviour seen in logs (`ARCHITECTURE.md` §6.0b note) |

## 3. Per-item ledger

Verdict classes (prompt §4 Phase 1 step 3): **A** mechanical/documentation, **B** code fix under an existing rule/ADR, **C** needs
maintainer approval, **D** rejected. "Status" is updated per phase.

### 3.1 Capture re-derivations (commands and raw results)

**CAP-1 / D-38 (S1) — re-derived, confirmed and strengthened.** Script `dlci8_opens.py` (session scratchpad; it runs
`tshark -r <log> -Y "btrfcomm.dlci==8" -T fields -e frame.number -e frame.time_relative -e frame.p2p_dir -e btrfcomm.frame_type -e data.data`,
reassembles each direction's `[Group][Code][Len:2BE][Value]` stream per open, and reports the first phone-side `0e 04` and the Buds'
`0e 01` pushes per `SABM`). Result, every DLCI 0x08 open in `CAP-060` (19) and `CAP-059` (3):

| Capture | SABM frame (t) | Opened by | Phone `0e 04` (after SABM) | First `0e 01` (after SABM) | Close |
|---|---|---|---|---|---|
| CAP-060 | 1223 (38.689 s) | Play services | 1284 (+0.431) | 1307 (+0.595; bundled after `0e 02`) | 1822 phone +12.09 s |
| CAP-060 | 1864 (51.574) | **app** (attempt 2) | — | — | 1870 **Buds** +0.01 s |
| CAP-060 | 1928 (52.942) | Play services | 1979 (+0.263) | 1993 (+0.286) | 2216 phone |
| CAP-060 | 2245 (78.295) | Play services | 2279 (+0.586, `04 15 00 00 0e 04 00 00`) | 2310 (+0.964) | 2498 phone |
| CAP-060 | 2520 (86.641) | **app** (attempt 2) | — | — | 2525 **Buds** +0.13 s |
| CAP-060 | 2584 (88.729) | Play services | 2592 (+0.199, bundled) | 2628 (+0.755) | 3124 phone |
| CAP-060 | 3770 (213.085) | **app** | — | — (0 frames) | 3783 phone +2.78 s (app release) |
| CAP-060 | 4221 (277.014) | **app** | — | — (0 frames) | 4246 phone +2.47 s (app release) |
| CAP-060 | 4460 (294.878) | **app** | — | — (0 frames) | 4698 phone +1.85 s |
| CAP-060 | 4792 (299.231) | Play services | 4830 (+0.591) | 4856 (+0.771) | 4948 phone |
| CAP-060 | 4975 (310.300) | Play services | 5015 (+0.542) | 5041 (+0.742) | 5084 phone |
| CAP-060 | 5139 (318.615) | Play services | 5149 (+0.238, bundled) | 5168 (+0.623) | 5226 phone |
| CAP-060 | 5262 (328.897) | Play services | — | — (0 frames) | ACL Disconnection Complete frame 5277 (329.21 s) |
| CAP-060 | 5449 (335.540) | **app** | — | — (0 frames) | 5749 phone +2.15 s |
| CAP-060 | 5818 (342.729) | Play services | 5828 (+0.179, bundled) | 5856 (+0.610) | 5968 phone |
| CAP-060 | 5989 (348.652) | **app** (attempt 2) | — | — | 5996 **Buds** +0.13 s |
| CAP-060 | 6058 (350.949) | Play services | 6068 (+0.170, bundled) | 6091 (+0.460) | — |
| CAP-060 | 6419 (383.865) | **app** | — | — (0 frames) | 6728 phone +2.04 s |
| CAP-060 | 6789 (388.070) | Play services | 6833 (+0.069) | 6859 (+0.097) | — |
| CAP-059 | 1135 (43.151) | Play services | 1176 (+0.075) | 1211 (+0.165) | 3011 phone |
| CAP-059 | 3845 (199.175) | Play services | 3888 (+0.238) | 3915 (+0.268) | 4609 phone |
| CAP-059 | 4662 (249.667) | Play services | 4674 (+0.103) | 4693 (+0.170) | 4947 phone |

Attribution of the opens to the app is by the app's own log (`CAP-060-debug-export.log` lines 35–38, 61–64, 218–221, 300–306,
328–334, 356–363, 371–374, 403–410: "RFCOMM channel 0x08 connected (attempt n/3)" at the same wall-clock second as the `SABM`/`UA`,
then "Case battery not read: Timeout"). Findings: (a) **13 of 13** Play-services opens that carried payload have a phone-side
`0e 04` before the first `0e 01`, and the first push follows it by 23 ms–0.38 s; (b) **8 of 8** app claims (no `0e 04`, receive-only
per ADR-035) received **no** `0e 01` — 5 held the channel open 1.85–2.78 s with zero data, 3 were closed by a **Buds-side** `DISC`
10–130 ms after the `UA` (each time right after the app's first attempt had failed and the stack closed Play services' port: phone
`DISC` 1822, 2498, 5968); (c) pushes with no `0e 04` do exist, but only 10 s–2 min into a long-held Play-services channel (1653,
2196, 2760, 2928, 3065, 3099, 4938, 5080, 5196). The `CAP-060` build was `ai-sessions/0042`'s (no ADR-038 retry). The current code's
retry (`BudsRepositoryImpl.kt:583-590`) fires only on `ChannelLost`, not on a timeout with the channel open. Verdict: **C** (P1, P2).

**PR-MAC — re-derived, confirmed with one narrowing.** `tshark -r CAP-059-btsnoop_hci.log -Y "btrfcomm.dlci==4 && (btrfcomm.frame_type==0x2f || btrfcomm.len>0)"`:
all **19** DLCI 0x04 opens in `CAP-059` are followed by a Buds frame `03 0a 00 08 <8 bytes>`, 19 distinct values (e.g. frame 1049
`03 0a 00 08 04 d6 2f e3 c2 2d 30 3b …`, frame 1509 `… 4c b5 93 ed 88 3f 5a ef …`); also `CAP-006` 727/2102 and `CAP-001` 1004/1826.
Official-app `Set` frames carry 16 differing, random-looking tail bytes: `CAP-001` 2039 `08 12 00 14 01 e8 e8 40 cf 36 0a eb 18 1f 81 07
64 0a 73 7b 15 b2 73 84`, 2132, 2159, 2193; `CAP-006` 1393, 1627, 1731, 1862. The app sends 16 zero bytes (`AncFrame.kt` `reserved =
ByteArray(16)`) and the Buds ACK and apply it: `CAP-059` 2768 (`08 12 00 14 01 e8 e8 08 00…00`) → 2779 `ff 01 00 06 08 12 01 e8 e8 08` →
2782 Notify `08 13 00 04 01 e8 e8 08`. **New detail:** in that exchange the app's `Set` (2768, t=134.466) went out **66 ms before** the
Buds' session nonce for that open (2771, t=134.532) — the zero tail cannot have been a MAC over that nonce in any case. **Narrowing:**
the hearablecontrols page itself labels bytes 8–23 "Reserved" (E2); only the MAC page (E1) defines the 8+8 layout. So "Device
Information `0x0A` = session nonce" is spec text + 19/19 wire (FACT-grade), whereas "bytes 8–23 of `08 12` = message nonce + MAC" is an
inference combining E1's generic layout with E2's "MAC: Y" column and the wire shape — proposed as 🟡 HYPOTHESIS (strong), not FACT.
Verdict: **C** (P3).

**PR-52 — re-derived, confirmed.** `tshark -r CAP-037-btsnoop_hci.log -Y "bthci_evt.code==0x03 || bthci_evt.code==0x05" …`:
chandle `0x0006`: Connection Complete frame 3866 (81.696 s), Disconnection Complete 4681 (96.929 s, `0x16`), **Connection Complete
8731 (252.778 s)**, Disconnection Complete 9506 (274.508 s). The reopening's `SABM`s (chandle `0x0006`, 250–275 s): 8827 `0x00`
(253.932), 8838 `0x0c` (254.112), 9041 `0x04` (255.884), **9155 `0x02` (257.318)**, 9216 `0x0a` (258.041), 9273 `0x08` (258.610). So it is a
fresh ACL connection with a reused handle, and DLCI 0x02 opened **third** — a real counter-example to "opens last on a fresh
reconnect" (§5.2: 6/6 becomes 6/7). Verdict: **C** (P8).

**PR-BURST — resolved beyond 0044's expectation.** `python3 scripts/pwrpc_decode.py captures/CAP-036-*/CAP-036-btsnoop_hci.log`, frames
1404–1570, plus the 65599 hashes of the method names from the APK's `fux.java` catalog (`h65599("GetHardwareInfo") = 0x28eca5e3`,
`SubscribeRuntimeInfo = 0xe61e8290`, `SetWallclock = 0x673bed4e`, `GetSoftwareInfo = 0x7199fa44`, `ReadSetting = 0xaed0ae51`,
`SubscribeToSettingsChanges = 0x2821adf5`): the connect-time burst is — 1405 the unsolicited `GetSoftwareInfo` RESPONSE (`call_id
0xFFFFFFFF`, serial + `release_5.203` ×3); 1407 `SubscribeToSettingsChanges`; 1410→1421 `SubscribeRuntimeInfo` (SERVER_STREAM,
`6:{1:{1:100 2:1} 2:{1:100 2:2} 3:{1:100 2:2}}` — the "battery-triple-like" periodic DLCI 0x02 push of `PROTOCOL.md` §6); **1415→1423
`GetHardwareInfo` RESPONSE carrying the three 14-character component serials** (`57071WRBEC0251`, `57081WRBDR2309`, `57071WRBDL3147`);
1430 `SetWallclock`; 1412…1570 a `ReadSetting` sweep of `qhr` fields 1–32; plus requests to unnamed services `0x73d5d805`,
`0xaf3a7737`, `0x755ffe65` (answers the serial `1779298694`), `0x1c256c5d`. This answers `PROTOCOL.md` §6's "which `maestro_pw` RPCs make
up the burst" and the frame-1423 question without the planned `CAP-057`. Verdict: **C** (new FACT proposal P10) + **A** (teach
`pwrpc_decode.py` the extra method names).

**PR-RING, PR-HID, SP1–SP6** — re-checked against E1–E6 (table §2). PR-RING confirmed (**C**, P6). PR-HID confirmed: E6's description
string equals `CAP-016`'s decoded HID Feature Report 2 (`PROTOCOL.md` §6 item, `CAP-016-FINDINGS.md` §10) — the surface being
OS-consumed spatial-audio head tracking is 🟡 HYPOTHESIS (strong) (**C**, P7). SP4 confirmed (E5: no timing/trigger sentence).

### 3.2 Ledger

| ID | Sev | Re-verification (evidence) | Verdict | Class | Phase | Files | Status |
|---|---|---|---|---|---|---|---|
| GOV-1 | S2 | "clean-room" present at `PROJECT.md:78`, `ARCHITECTURE.md:384`, `DECISIONS.md:1290,1334`, `TODO.md:597`, `CHANGELOG.md:587` (0044's `PROJECT.md:303-308` line numbers are wrong; `PROJECT.md` has 113 lines) | confirmed, locations corrected | A (PROJECT/ARCH/TODO) + C (ADR-025 Update) | 3 | — | done |
| GOV-2 | S3 | `PROJECT.md:31-39` all unchecked | confirmed | A | 3 | | done |
| GOV-3 | S4 | `PROJECT.md:106` "read and change battery status" | confirmed | A | 3 | | done |
| GOV-4 | S3 | `.gitattributes` has only log/mp4/zip/pcapng; `git check-attr filter` = unspecified for the CAP-059/060 `.txt`/`.png`; `git ls-files -s` shows mode 100755 for 5 `.txt`/`.png` and `CAP-060-recording.mp4` | confirmed | A (+ C for history rewrite) | 3 | | done |
| GOV-5 | S3 | `AGENTS.md` §5 still lists HFP; no ADR for the removal | confirmed | C (P4) | 2 | | done |
| GOV-6 | S3 | E5: no timing on the spec page | confirmed | C (ADR-006 Update; `AGENTS.md` §7 edit) | 2 | | done |
| GOV-7 | S4 | `PROJECT_RULES.md:53` cites the retired report | confirmed | A | 3 | | done |
| GOV-8 | S4 | `CONTRIBUTING.md:62` lists three labels (0044 said :103-104) | confirmed, location corrected | A | 3 | | done |
| GOV-9 | S3 | no `.proto`, no protobuf plugin; hand codec | confirmed | C (P5) | 2 | | done |
| GOV-10 | S2 | `.github/workflows/` = lint-docs, sidebar, sitemap only | confirmed | A | 3 | | done |
| GOV-11 | S3 | `python3 scripts/lint_docs.py` → exit 1, 202 lines (3 from `.venv`), incl. `ANC-005` ×11 | confirmed | A | 3 | | done |
| SP1–SP6 | — | E1–E6 | confirmed (SP1: bytes 8–23 are "Reserved" on E2) | — | 1 | | done |
| PR-MAC | S2 | §3.1 | confirmed, narrowed | C (P3) | 2 | | done |
| PR-RING | S2 | E3 | confirmed | C (P6) | 2 | | done |
| PR-HID | S3 | E6 + `CAP-016` §10 | confirmed | C (P7) | 2 | | done |
| PR-52 | S2 | §3.1 | confirmed | C (P8) | 2 | | done |
| PR-BURST | S3 | §3.1 | resolved | C (P10) + A | 2 | | done |
| PR-ADV | S3 | `PROTOCOL.md` Option D (`:1040-1051`) FACT vs 0042 §9(d) LE view | confirmed | A (dated 🔴 note, no status change) | 3 | | done |
| PR-1 | S3 | `PROTOCOL.md:151-152` | confirmed | A | 3 | | done |
| PR-2 | S4 | `:162-163` | confirmed | A | 3 | | done |
| PR-3 | S3 | `:218`, §6 address item, `ARCHITECTURE.md:324-332` | confirmed | A | 3 | | done |
| PR-4 | S3 | `:282-293`, `:372` | confirmed | A | 3 | | done |
| PR-5 | S3 | `:481-488` | confirmed | A | 3 | | done |
| PR-6 | S3 | `:600-607` | confirmed | A | 3 | | done |
| PR-7 | S3 | `:707-711`, `:1255-1256`, §4.5 preamble | confirmed | A | 3 | | done |
| PR-8 | S4 | `:735` | confirmed | A | 3 | | done |
| PR-9 | S3 | `:748-760`, `:1153-1162` | confirmed | A | 3 | | done |
| PR-10 | S3 | `:63` | confirmed | A | 3 | | done |
| PR-11 | S3 | `:797-799`; E5 | confirmed | A | 3 | | done |
| PR-12 | S3 | `:1508-1514`, `:1718-1727` | confirmed | A | 3 | | done |
| PR-13 | S3 | §6 items listed | confirmed | A | 3 | | done |
| PR-14 | S3 | `:3012` row; no 0040/0041/0043 rows | confirmed | A | 3 | | done |
| PR-15 | S4 | `:2502`, `:2703`, `:1231`, `:2993` | confirmed | A (+ date via git) | 3 | | done |
| PR-16 | S4 | `:2973` | confirmed | A | 3 | | done |
| AR-1 | S2 | `UnsupportedFirmware` produced nowhere (`grep`: only `BudsError.kt:54`, `ConnectionScreen.kt:305`) | confirmed | C (APP-1 design) | 2 | | done (APP-1 / ADR-042) |
| AR-2 | S3 | §1 diagram/§3/§6/§13 describe non-existent classes | confirmed | A | 3/4 | | done |
| AR-3 | S3 | §2 plain vs §9/§15 "encrypted" | confirmed | A (align with `AGENTS.md` §10 "encrypted where applicable") | 3 | | done |
| AR-4 | S3 | §4 | confirmed | A | 3 | | done |
| AR-5 | S3 | §5a/§15 predate ADR-036 | confirmed | A | 3 | | done |
| AR-6 | S3 | §6.0b | confirmed | A | 3 | | done |
| AR-7 | S3 | `:383` | confirmed | A | 3 | | done |
| AR-8 | S4 | "two transports" lists three; `Instant` vs `timestampMillis` | confirmed | A | 3 | | done |
| AR-9 | S3 | `ConnectionStateMachine` doc vs `BudsRepositoryImpl.kt:375-380` | confirmed | A (+ B stale TODO) | 3/4 | | done |
| AR-10 | S3 | = PR-HID | confirmed | C (P7) | 2 | | done |
| CAP-1 / D-38 | S1 | §3.1 | confirmed, strengthened | C (P1, P2) | 2 | | done |
| D-1 | S3 | ADR-037 cites an `AGENTS.md` §0 note that does not exist (`git log -- AGENTS.md`: no 0043 commit) | confirmed | C (ADR-037 Update) | 2 | | done |
| D-2 | S4 | ADR-037 "§8 preamble" | confirmed | C (same Update) | 2 | | done |
| D-3 | S3 | E2: "Settable toggles … which are currently enabled" | confirmed | C (P9) | 2 | | done |
| D-4 | S4 | registry rows stale (ADR-009/024/033, CAP-046) | confirmed | A (registry) + C (ADR title annotation) | 3 | | done |
| D-5 | S4 | ADR-015 "2026-08-2x" | confirmed | C (ADR edit, date from git) | 2 | | done |
| D-6 | S3 | ADR-012/036 depend on §8.1 | confirmed | A | 3 | | done |
| D-7 | S3 | = PR-RING | confirmed | C (P6) | 2 | | done |
| D-8 | S4 | status column mixes dates; no `CAP-058` folder | confirmed | A | 3 | | done |
| T-1 | S4 | `TODO.md:4,796` "rule 13" | confirmed | A | 3 | | done |
| T-2 | S3 | every listed spot re-read | confirmed | A | 3 | | done |
| C-1 | S3 | `CHANGELOG.md` gaps/order | confirmed (full read in Phase 5) | A | 3 | | done |
| S-1 | S4 | `0007` PROMPT dated 09_08, RESULT 09_11 | confirmed | A (INDEX note) | 3 | | done |
| S-2 | S4 | `0011` RESULT "Status: done" | confirmed | A | 3 | | done |
| S-3 | S4 | `0016` RESULT "awaiting maintainer sign-off" vs INDEX "complete" | confirmed | A | 3 | | done |
| S-4 | S3 | `0023`/`0030` "partial — resumed" | confirmed | A (annotate) | 3 | | done |
| S-5 | S3 | `0042`/`0033`/`0040` statuses | confirmed | A (per §4a, citing) | 3 | | done |
| README-1 | S2 | `README.md` status frozen at 2026-09-18; Safe-Mode claim | confirmed | A (+ depends on APP-1) | 3 | | done |
| CAP-2 | S3 | code `0x0a` in `CAP-002`/`004`/`036` findings | confirmed | A after P3 | 3 | | done |
| CAP-3 | S4 | lint dead refs in capture docs | confirmed | A | 3 | | done |
| CAP-4 | S3 | index rows / traceability | to verify in Phase 5 | A | 5 | | done (Phase 5: traceability checked, §7) |
| 0042-1…3 | S3/S4 | re-read | confirmed | A (notes only; history not rewritten) | 3 | | done |
| 0043-1…6 | S1–S4 | re-read; 0043-1 matches CAP-1 | confirmed | A (+ statuses) | 3 | | done |
| APP-1 | S2 | no firmware/Model-ID gate; `PairingLogic.chooseBonded` name fallback | confirmed | C (gate design) | 2 | | done |
| APP-2 | S2 | `CodecRouter.kt` splitters never reset; no length guard; unbounded HDLC buffer | confirmed | B | 4 | | done |
| APP-3 | S2 | `BudsRepositoryImpl.kt:408-417`; `AncFrameDecoder` accepts only `ff 01` | confirmed | B | 4 | | done |
| APP-4 | S2 | `MainActivity` `rememberCoroutineScope()` for every action | confirmed | B | 4 | | done |
| APP-5 | S2 | dock state from every Notify (`BudsRepositoryImpl.kt:302`) | confirmed | B (+ wording P9) | 4 | | done |
| APP-6 | S2 | FGS driven by lifecycle-bound `LaunchedEffect`; `updateStatus` unused | confirmed | B | 4 | | done |
| APP-7 | S3 | collector on `Dispatchers.Default` (`RepositoryModule`) resets `eqProfile` asynchronously; proving test in Phase 4 | plausible (multi-threaded scope) | B | 4 | | done |
| APP-8 | S3 | `connect()` returns `PermissionDenied` when no bonded device | confirmed | B | 4 | | done |
| APP-9 | S3 | retry only on `ChannelLost`; DLCI 0x04 release not cancelled/forced before 0x08 claim | confirmed | C (P2) + B | 2/4 | | done |
| APP-10 | S4 | `FakeBudsTransport` in `hardware/src/main` | confirmed | B | 4 | | done |
| APP-11 | S4 | stale KDoc spots | confirmed | B | 4 | | done |
| APP-12 | S4 | `lifecycle-viewmodel-compose` unused; `composeCompiler`, `kotest-runner-junit5` unused catalog entries | confirmed | B | 4 | | done, narrowed (`lifecycle-viewmodel-compose` is used — kept; §6) |
| APP-13 | S4 | `BLUETOOTH_SCAN` declared, no scan | confirmed | C | 2 | | done |
| SYN-1…3 | — | synthesis | carried into TODO/TESTPLAN (scope question for SYN-2) | A + C | 3 | | done (TESTPLAN §4b, `TODO.md`; SYN-2 approved) |
| P1–P9 | — | proposals | put to the maintainer | C | 2 | | done (answers in §4) |

## 4. Phase 2 — maintainer checkpoint (asked and answered in this chat, 2026-09-24, `AskUserQuestion`, four rounds)

Every question carried a summary, options with pros/cons, one "(Recommended)" option and, where text would be written, the draft.
The maintainer chose the recommended option in every case:

| Item | Question (short) | Maintainer's answer (2026-09-24, this chat) | What it authorises |
|---|---|---|---|
| P1 | Correct Option E's 2026-09-22 FACT | "Gedateerde correctie (Recommended)" | dated correction in `PROTOCOL.md` §4.3 Option E (text as previewed), `CAP-060-FINDINGS.md` §2 rewritten in place |
| P2 | ADR: send `0e 04` on the DLCI 0x08 claim | "ADR-039 + bouwen (Recommended)" | ADR-039 (supersedes ADR-035 item 2, amends ADR-038's premise; release DLCI 0x04 first), code |
| P3 | Session nonce / MAC | "a FACT, b+c 🟡 (Recommended)" | 🟢 `0x0A` = session nonce; 🟡 bytes 8–23 = nonce + MAC; 🟡 MAC not enforced by `release_5.203` + risk note + 🔴 test |
| P10 | Connect-time burst RPCs (new) | "FACT + CAP-057 vervalt (Recommended)" | 🟢 FACT in `PROTOCOL.md` §6/§2.2a; `CAP-057` withdrawn |
| P6 | Spec `0x03` ring both | "ADR-027 Update + test-TODO (Recommended)" | ADR-027 Update (text as previewed); FIND-004 test tracked; no send |
| P7 | HID question | "Sluiten als 🟡 OS-consumed (Recommended)" | close `ARCHITECTURE.md` §15 item at 🟡 |
| P8 | `PROTOCOL.md` §5.2 | "Gedateerde correctie, blijft 🟡 (Recommended)" | dated correction, 6 of 7, stays 🟡 |
| P9 | Dock byte semantics / UI | "Update + nieuwe tekst (Recommended)" | ADR-024 Update (spec meaning), UI wording, provisional ≤ 2 s rule (APP-5) |
| P4 | HFP-removal ADR + `AGENTS.md` §5 | "ADR + AGENTS.md §5 aanpassen (Recommended)" | ADR-040; the maintainer's instruction to apply the approved sentence to `AGENTS.md` §5 |
| P5 | Hand-written codec ADR | "ADR + §14 + AGENTS §4 (Recommended)" | ADR-041; `ARCHITECTURE.md` §14; dated note in `AGENTS.md` §4 |
| ADR bundle | GOV-1 ADR-025 Update, GOV-6 ADR-006 Update + `AGENTS.md` §7, D-1/D-2 ADR-037 Update, D-5 ADR-015 date, D-4 ADR-009 title note | "Alles, incl. AGENTS §7 (Recommended)" | all five as dated, non-destructive edits |
| GOV-4 | LFS | "Alleen vooruit (Recommended)" | `.gitattributes` + `chmod -x` + re-add existing blobs as LFS pointers; **no** history rewrite |
| APP-1 | Safe Mode design | "Firmware + Model ID gate (Recommended)" | write gate on firmware ∈ {`release_5.203`} and Model ID `da 2d b1`, Safe Mode card, `UnsupportedFirmware`; recorded as ADR-042 (rule 8: an architecture choice is recorded before it is implemented) |
| APP-13 | `BLUETOOTH_SCAN` | "Verwijderen tot Option A (Recommended)" | remove from the manifest until ADR-006's scan is built |
| SYN-2 | Spatial audio / LE Audio | "Ja, als 🔴 kandidaten (Recommended)" | two new 🔴 Test-IDs + `TODO.md` line; no scope change |

ADR numbers were taken as the next free numbers after ADR-038 (checked against `DECISIONS.md` and `id_registry.csv`): 039 (`0e 04`),
040 (HFP), 041 (codec), 042 (Safe Mode). No item was declined, so nothing is "rejected by the maintainer".

## 5. Phase 3 — documentation, protocol and decision fixes (progress record)

Applied (all approved items written with a process note citing this chat; every (A) item mechanical):

- `DECISIONS.md`: ADR-006, 024, 025, 027, 037, 038 dated Updates; ADR-009 title suffix; ADR-015 date resolved from commit `76c482e`;
  pointer Updates on ADR-015/023/031 (→ ADR-040) and ADR-035 (→ ADR-039); new **ADR-039, ADR-040, ADR-041, ADR-042**.
- `id_registry.csv`: ADR-039…042 registered; stale rows refreshed (ADR-009/024/025/027/033/035/038, CAP-046); status column
  normalised to `analyzed`/`planned`/`withdrawn` (D-8); `CAP-057` → withdrawn (P10); `ANC-005` registered as `declined` (GOV-11);
  new Test-IDs `SPATIAL-001`, `LEAUDIO-001` (SYN-2).
- `PROTOCOL.md`: Option E correction (P1); §0.1 session nonce FACT and §4.1 nonce/MAC HYPOTHESES + risk (P3); §6 burst FACT with three
  items checked off (P10); §5.2 correction and retitle (P8); §6 HID context (P7); PR-1…PR-16 and PR-ADV as dated pointers/notes; §8
  changelog row fixed and rows for 2026-09-19/20, 2026-09-22 and 2026-09-24 added.
- `AGENTS.md` (maintainer's instruction in this chat): §4 note (ADR-041), §5 note (ADR-040), §7 time-box wording (ADR-006 Update).
- `PROJECT.md` (GOV-1/2/3), `PROJECT_RULES.md` (GOV-7), `CONTRIBUTING.md` (GOV-8), `TODO.md` (T-1, T-2 stale items).
- `ARCHITECTURE.md`: AR-1…AR-10 and the as-built changes of Phase 4 (§1, diagram, §2, §2.1, §3 rewritten, §3.1, §4 rewritten, §5,
  §5a re-derived through ADR-042, §6.0a/§6.0b, §7, §8/§8.1, §9, §13, §14, §15).
- `README.md` (README-1): status block, disclaimer (Safe Mode now implemented), current state, permissions (no `BLUETOOTH_SCAN`).
- `CAP-060-FINDINGS.md` §2 rewritten in place (P1, rule 9a).
- `.gitattributes` + `chmod -x` + re-add of the 16 `.txt`/`.png` capture files as LFS pointers, checksums identical before/after, no
  history rewrite (GOV-4).
- `scripts/lint_docs.py` (GOV-11): `.venv`/build dirs excluded; generic suffix mentions, ephemeral frame-grab names, elided `...` paths
  and tool-relative `SPEC.md`/`BACKLOG.md` handled; deliberate "renamed from/deleted" mentions allowlisted; Test-ID prefixes
  `GSND|SDP|PRIV|SPATIAL|LEAUDIO` added; dead references in historical `ai-sessions/` logs reported as information, not failures
  (they are not rewritten, `AI_SESSION_LOG_PROCEDURE.md` §4a).
- `.github/workflows/android.yml` (GOV-10): build + unit tests + lint + "no `INTERNET`" in every manifest and in the merged manifest;
  actions pinned to commit SHAs.

## 6. Phase 4 — the app (progress record)

| Item | Change | Regression test(s) |
|---|---|---|
| APP-2 | `CodecRouter.reset(channel)`/`resetAll()` (synchronised), called on connect, disconnect, loss, every claim open/close and every `channelClosed`; max frame guards (1024 data bytes Message Stream, 4096 HDLC) | `CodecRouterTest`: liveness property over 150 random prefixes ("valid frame decodes after garbage + reset"), poisoned-without-reset, oversize length, unbounded HDLC run; repository: "a frame cut off by a closed on-demand channel cannot misalign the next claim" |
| APP-3 | `MessageStreamReply` (ACK/NAK, spec reason codes) is its own routed type; `setAncMode` waits for ACK/NAK/`Notify`: NAK → `CommandRejected`, none → `Timeout`, old mode kept; Ring commands likewise (spec: no ACK in 1 s = unsupported) | NAK, timeout, Ring NAK tests; ACK fixtures moved to the reply decoder (CAP-001 2041, CAP-025 2044/2048) |
| APP-4 | all user actions in the injected application scope; `withMessageStream`/`readCaseBattery` release in `finally`; a cancelled `connect()` disconnects and returns to `Disconnected` | "a tap cancelled while it waits still releases the Message Stream" |
| APP-5 | `dockStateProvisional` (≤ 2 s after the claim opened; last `Notify` wins during the linger); UI wording per P9 | provisional/not-provisional test with a virtual clock |
| APP-6 | foreground service driven from `OpenControlApplication` in the application scope; dead `updateStatus` removed | (Android framework — hardware re-test) |
| APP-7 | EQ reset moved into `connect()` before `Ready`; the asynchronous `Ready` collector (on `Dispatchers.Default`) deleted | "a new connect resets the EQ…", "a Ready transition never wipes an EQ value read for this connection" |
| APP-8 | `BudsError.NotPaired` | connect test |
| APP-9 / ADR-039 | the Case claim ends the Message Stream linger first, sends exactly one `0e 04 00 00`, keeps ADR-038's retry | refreshBattery test asserts close-0x04-then-open-0x08 and exactly `0e040000` |
| APP-10 | `FakeBudsTransport` → `:hardware` test fixtures (+ `onOpenChannel` hook) | build |
| APP-11 | stale KDoc: `BatteryStatus`, `BudsRepository`, `ConnectionStateMachine`, `BudsSdpUuids`, `OsConnectionObserver`, `LinkEvaluation`, manifest header, `AncFrame` | — |
| APP-12 | `composeCompiler` and `kotest-runner-junit5` catalog entries removed. **Narrowed:** `lifecycle-viewmodel-compose` is **not** dead — removing it made `navigation-compose` resolve `lifecycle-viewmodel-compose` 2.6.2 instead of 2.8.6 (the offline build failed on exactly that); kept with a comment | build |
| APP-13 | `BLUETOOTH_SCAN` removed from both manifests (approved) | build/lint |
| APP-1 / ADR-042 | `SafeModeGate` (pure) + `writeGate` (bounded waits for the firmware announcement and the claim's Model ID) on ANC Set, Ring/Stop, EQ write; `ModelIdFrameDecoder`; `safeMode` flow; Safe Mode card | gate unit tests; repository: unverified firmware refuses all writes and sends nothing, reads still work, wrong Model ID, no Model ID, no announcement |

Mutation checks (each applied, the suite run, then restored — all caught): (1) splitter reset on `channelClosed` removed → 1 failure;
(2) no-answer treated as success → 1; (3) Safe-Mode gate disabled → 4; (4) NAK treated as success → 1; (5) Message Stream length guard
disabled → 1; (6) `0e 04` not sent → 1.

Build gate (`cd android && ./gradlew --offline assembleDebug testDebugUnitTest test lint`): BUILD SUCCESSFUL. Unit tests (debug variant):
`:data` 1473, `:hardware` 49, `:domain` 11 = **1533**, 0 failures. Lint: 0 errors; `:app` 2 warnings, both pre-existing
(`DataExtractionRules`, `MissingApplicationIcon`, recorded in `ai-sessions/0042`); no new suppression.

Additional Phase 5/6 fix to the app (same gate): `PwRpc.methodName` and `Maestro` name the three connect-burst methods
(`GetHardwareInfo`, `SubscribeRuntimeInfo`, `SetWallclock`) for the debug log only; nothing sends them. The hash test asserts them.
**M7** (Phase 5, for 0043's unreported task-24 check): ADR-038's retry reduced to one attempt → 2 failures in `BudsRepositoryImplTest`,
restored (byte-identical, `cmp`). Final gate after all Phase 5 edits: BUILD SUCCESSFUL, `:data` 1473 / `:hardware` 49 / `:domain` 11
= **1533**, 0 failures; lint `:data`/`:hardware`/`:ui` "No issues found", `:app` 0 errors + the same 2 warnings; no new suppression.

## 7. Phase 5 — closing 0044's coverage gaps (§11 of 0044)

| 0044 "Remaining" item | What was done | What it found (class) → action |
|---|---|---|
| `CHANGELOG.md` 330–613 | read in full | C-1: 0001–0032 backfilled (one line per session), Added reordered by date, 0045 entry; the 0043 entry and a 2026-09-07 "clean-room" line annotated (A) |
| rest of `README.md` | read in full | status/state/permissions refreshed in Phase 3 (A) |
| `CAPTURE_…`, `TESTPLAN_…` linear + §13.7 traceability | read in full; every Test-ID CAP-059/060's groups exercise appears in their EVENT-NOTES | Group AS withdrawn, CAP-058 folder skeleton, prefixes, BATT/FIND/OBS rows, §4b (A) |
| `REVERSE_ENGINEERING.md`, `DESKRESEARCH_FINDINGS.md` + ADR-017 boundary | scripted section scan (every section with a 🟡 but no maintainer-approval wording), then the flagged sections read | stale "no analysis session logged yet" status (A, fixed); pw_hdlc address pointer (A); HFP-removal pointer in DESKRESEARCH (A); **17 "2026-08-30 follow-up pass" 🟡 entries with no recorded maintainer relevance decision (C → asked in chat 2026-09-24: "Dated note only (Recommended)" → note added)** |
| the other 57 capture folders | scripted pass over all 60: files vs registry status, status lines, undefined Test-IDs, confidence labels in every FINDINGS; targeted greps for claims later corrected (session nonce, Case push, pw_hdlc addresses, HFP cadence) | CAP-002/004/036 session-nonce identity (CAP-2, A); CAP-060 EVENT-NOTES correction note (A); CAP-009 "2026-08-2x" → 2026-08-23 from commit `76c482e` (A); CAP-031/032 had no Status line (A); pointers in CAP-001/005/011 (A). Not a line-by-line read of all 57: disclosed here. |
| 0042 prompt; 0043 prompt phases E–H | read in full, task by task against the RESULTs | 0042: every task has a RESULT section; no new finding beyond 0044's 0042-1…3. 0043: task 22's as-built description missing (now `ARCHITECTURE.md`, Phase 3); task 23 had no severities and its "nothing fundamentally wrong" is contradicted by APP-2…6; task 24's mutation check not reported (**M7**, caught); §8 "0 lint issues" ignored the 2 `:app` warnings. Recorded in 0043's Status annotation (A). |
| content pass 0001–0041 | scripted: pairs, category, date, Status vs INDEX for all 44; every RESULT with open-proposal wording traced to its sign-off round (0026/0028/0029/0031, 0041/0042) | statuses fixed per §4a (§5 list below); still open: 0033's settings-*write* ADR proposal, 0040's proposal 5 (annotated, in `TODO.md`) |
| remaining Kotlin + all 17 test files | read: `PwRpc`, `Varint`, `Ring*`, `BatteryFrame`, `EqFrame`, `SessionDiagnostics`, `DebugSettingsStore`, all `ui/*`; tests skimmed in full for names/fixtures/timing | stale KDoc in `RingFrame`, `FindMyBudsScreen`, `BatteryFrame`, `DebugSettingsStore`; `LOGS-001` → `CAP-059` in 10 comments (B, fixed); 36 `advanceUntilIdle()` in `BudsRepositoryImplTest` → `settle()` (B, 57/57 pass) |
| external: RFCOMM one-per-DLCI, `RECEIVER_EXPORTED` | E8, E9 | both claims hold as far as official texts go; notes added to `ARCHITECTURE.md` §6.0b and the `OsConnectionObserver` paragraph (A) |

Session-history hygiene (§4a, citing this session): `0011` done → complete; `0016` → complete (its proposal became Group AT, approved
2026-09-18 in `0031`); `0030` → complete (finished by `0031` Phase 1); `0042` → complete (approvals quoted in its own header; re-test
answered in §8 below); `0044` → complete; `0023` stays partial with a reason; `0033`, `0040` annotated (still awaiting sign-off, reason
given); `0043` annotated with 0043-1…6; `INDEX.md` rows 0007 (date note), 0030, 0042, 0044 updated.

## 8. `ai-sessions/0042` §12 re-test items (a)–(j), answered from `CAP-060` (0043-3)

| Item | Verdict | Evidence |
|---|---|---|
| (a) mirror | 🟢 confirmed | 13 `Android link:` lines in `CAP-060-debug-export.log`; card followed Android at 17:59:02 / 17:59:42 (`CAP-060-EVENT-NOTES.md`) |
| (b) pairing | 🟢 confirmed for one picker run | `bond state BONDED` 17:54:42.052 (export line 15), 0 "bond timed out" lines; the double-tap variant was not tried |
| (c) session-end lines | 🟢 confirmed | 1 "Session ended by the user's Disconnect tap" line, 6 "Session lost: channel …" lines |
| (d) UNKNOWN wording | ⚪ not assessable | the export logs no UI text; the film does not show the card at that moment |
| (e) drop hunt | 🔴 not run | no isolated idle / periodic / second-host runs; still in `TODO.md` |
| (f) EQ audibility | 🔴 not assessable | no narration of a heard change |
| (g) HFP | — | removed, nothing to test (ADR-040) |
| (h) Case battery | 🟢 the "Timeout every time" branch | 8/8 `Case battery not read: Timeout`; the per-open re-derivation (§3.1) shows why, and 0042 §12(h)'s own next step (an ADR to send `0e 04`) is ADR-039 |
| (i) extras | partly | Find: "Ringing: Right earbud" on film `t=139s` 🟢; the firmware line and dock flip are not visible in the logs or on film ⚪ |
| (j) ANC tile | 🔴 not tested | the tile was never added to Quick Settings (`CAP-060-FINDINGS.md` §4) |

## 9. Re-test instructions (nothing in this session is hardware-verified)

Use the 0042 §12 set-up: **Debug mode** on; HCI snoop ON, then Bluetooth off/on **on film**; phone clock with seconds in shot; narrate
what you hear; write down the build (this commit), the time of every tap, and Play services' *Nearby devices* permission state.

- **(A) Case battery with the request (ADR-039), the important one.** Connect with the buds in the case, lid open. Then take both out
  and tap **Refresh battery**. *Expected:* a Case percentage within ~2 s each time, no "Case battery not read: Timeout". **HCI bracket for
  each claim** (pre-filter by the Buds' address, `AGENTS.md` §13), in this order: phone `DISC` on DLCI 0x04, if our Message Stream was still
  lingering; phone `SABM` on DLCI 0x08, then `UA`; one phone UIH carrying exactly `0e 04 00 00`; then a Buds UIH `0e 01 …` within ~1 s;
  phone `DISC` on DLCI 0x08 about 1 s after the push (`CASE_LINGER_MS`). *Refuted if* `0e 04 00 00` goes out and no `0e 01` follows within 1 s on an open that
  Play services does not hold, or if the app sends **anything else** on DLCI 0x08. *Also informative:* whether the `DISC` comes from the
  Buds (`ChannelLost`, the one retry should follow) or from the phone.
- **(B) ANC answers.** Change ANC 4 times. *Expected:* the mode changes and no error. The export shows no "NAK" line. If the Buds refuse
  (e.g. reason `0x03` incorrect MAC, `PROTOCOL.md` §4.1), the ANC screen says "The Buds refused the command (…)" and the old mode stays.
  *Refuted if* the screen shows the new mode although the HCI log has an `ff 02` for that `08 12`, or it reports "Timeout" although an
  `ff 01 … 08 12` came back.
- **(C) Safe Mode (ADR-042).** On your Buds (`release_5.203`): **no** "Safe Mode — read-only" card, and ANC/EQ/Find work. *Refuted if* the
  card appears on this firmware, or a write goes out before the firmware line has appeared.
- **(D) Dock line.** Buds in the case, then connect or tap Refresh: "Both earbuds seem to be in the case …", with "provisional" only for a
  value read in the first ~2 s after a claim. Take one bud out, tap Refresh: the line changes. *Refuted if* "provisional" sticks
  or never appears right after an open.
- **(E) Not paired.** Forget the Buds in Android, open the app, tap Connect: "No paired Pixel Buds found …" (not a permission message).
- **(F) Taps survive the screen.** Tap Refresh battery and switch to another app right away; come back after 5 s. *Expected:* the Case result
  (or a specific error) is there, and DLCI 0x08 was released (HCI `DISC`).
- **(G) Notification.** Connect: one "Connected — ANC: …" notification that follows the mode; Disconnect removes it. A connect that fails
  fast may still flash it (`TODO.md` debt).
- **(H) Carried over, still open:** ANC tile on the panel (0042 (j)); EQ audibility (0042 (f)); Find keeps ringing after release; ring
  "both" is **not** built (`FIND-004` is a capture test of the official behaviour only); drop hunt (0042 (e)).

## 10. Status and what is left for the maintainer

Done: Phases 1–6. Waiting for you: the go-ahead to commit and push (prompt §6). Open by design (in `TODO.md`): the hardware re-tests
above, ADR-036's read-only settings UI, and the undecided items (auto-connect, BLE battery advertisement, settings writes).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0045_MAINTENANCE_RESULT_2026_09_24.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0045_MAINTENANCE_RESULT_2026_09_24
