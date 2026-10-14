# 0091_FEATURE_RESULT_2026_10_14.md — Build ADR-064 as version 1.3.0: the settings stream on DLCI 0x02 replaces the hold, the re-claim and the Play-services card

**Number:** 0091
**Category:** FEATURE
**Date:** 2026-10-14
**Prompt:** `ai-sessions/0091_FEATURE_PROMPT_2026_10_14.md`
**Status:** complete (not committed — awaiting the maintainer's yes in chat)

## Progress

- Phase 0: branch `feature/settings-stream` created from `main` at `c1d898b` (Merge PR #55); `git fetch` showed no commits in `HEAD..origin/main`;
  `git status --short` showed only `?? android/domain/bin/` (build output, never staged). Gate before any change (`cd android && ./gradlew assembleDebug testDebugUnitTest test lint --max-workers=2`): BUILD SUCCESSFUL, exit 0; JUnit XML test counts (debug + release variants summed): `:data` 3344, `:hardware` 128, `:domain` 42, `:ui` 204, `:app` 0 — i.e. per variant `:data` 1672, `:hardware` 64, `:domain` 42 (JVM, one variant), `:ui` 102, the same as `ai-sessions/0089`'s final gate. `python3 scripts/lint_docs.py`: only this new RESULT's missing footer (ensure_footers in Phase F). Reading of the mandatory documents: done (see "Files read").
- Phase A: done — evidence re-derived (section A.1), design (A.2–A.6), drafts and questions (A.7–A.8).
- Phase B: done — eleven questions, all answered with the recommended option (section B).
- Phase C: done (section C).
- Phase D: done — the gate green, M1–M5 caught, compliance checked (section D).
- Phase E: done (section E).
- Phase F: done (section F); ensure_footers and lint_docs run (see the chat summary).
- Phase G: the summary and the commit/push question in chat (section G).

## A. Evidence and design (Phase A)

### A.1 Evidence re-derived from the captures

Every frame below was listed with `tshark -r <log> -Y 'frame.number==N' -T fields -E separator='|' -e frame.number -e frame.time -e frame.p2p_dir
-e bthci_acl.chandle -e btrfcomm.dlci -e btrfcomm.frame_type -e data.data` (exit 0) after scoping to the Buds' connection handle (the Connection Complete for
the Buds' address: `CAP-015` `0x0004`, `CAP-021` `0x0002`, `CAP-027` `0x0002`, `CAP-038` `0x0001` + `0x0004`, `CAP-045` `0x0002`, `CAP-051` `0x0002`,
`CAP-056` `0x0005`, `CAP-069` `0x0001` + `0x0002`), and decoded with `python3 scripts/pwrpc_decode.py <log>`; multi-frame RFCOMM payloads were split on `7e`
and every HDLC frame's CRC-32 verified. The logs are real files (no LFS pointers). Positive control: `pwrpc_decode.py` lists the subscribe REQUEST once per
MAESTRO open in the official-app captures (`CAP-051` 1177 on channel 21, 2229 on channel 19; `CAP-056` 1451) and 0 times in every OpenControl log
(`ai-sessions/0090` E-02).

| What | Log, frame (time) | RFCOMM payload (hex, flags included) |
|---|---|---|
| `SubscribeToSettingsChanges` REQUEST, channel 21 | `CAP-051` 1177 (21:43:13.004) | `7e004b0310151dea71de7d5e25f5ad2128019754b07e` |
| the same, channel 19 | `CAP-056` 1451 (17:31:15.770); `CAP-051` 2229 | `7e003b0310131dea71de7d5e25f5ad212807ee8eb27e` |
| `ReadSetting 4:13` REQUEST, channel 21 | `CAP-051` 1178; `CAP-045` 2021 | `7e004b0310151dea71de7d5e2551aed0ae2a02200d9e82abac7e` |
| the same, channel 19 | `CAP-056` 1450; `CAP-021` 1069; `CAP-027` 910; `CAP-051` 2231 | `7e003b0310131dea71de7d5e2551aed0ae2a02200d24c239477e` |
| RESPONSE `4:{13:4}` ch 21 | `CAP-051` 1225 (2nd HDLC frame of 5) | `7e00a5032a0422026804080110151dea71de7d5e2551aed0ae067d5e749e7e` |
| RESPONSE `4:{13:2}` ch 19 | `CAP-056` 1468 | `7e80a3032a0422026802080110131dea71de7d5e2551aed0ae57085e2d7e` |
| RESPONSE `4:{13:3}` ch 19 | `CAP-027` 923 (4th of 5) | `7e80a3032a0422026803080110131dea71de7d5e2551aed0aebfd3a5947e` |
| RESPONSE `4:{13:1}` ch 19 | `CAP-021` 1089 (2nd of 4) | `7e80a3032a0422026801080110131dea71de7d5e2551aed0ae2e62233c7e` |
| RESPONSE `4:{13:0}` ch 21 | `CAP-045` 2033 (1st of 3) | `7e00a5032a0422026800080110151dea71de7d5e2551aed0ae651b08147e` |
| RESPONSE `4:{13:0}` ch 19 | `CAP-051` 2247 (2nd of 2) | `7e80a3032a0422026800080110131dea71de7d5e2551aed0aec6b9d8857e` |
| SERVER_STREAM `4:{13:2}` ch 21 | `CAP-051` 1717 (21:43:43.479) | `7e00a5032a0422026802080710151dea71de7d5e25f5ad2128cbb358d57e` |
| SERVER_STREAM `4:{13:4}` ch 21 | `CAP-051` 1755 (21:43:56.004) | `7e00a5032a0422026804080710151dea71de7d5e25f5ad21283967a2f77e` |
| SERVER_STREAM `4:{13:3}` ch 21 | `CAP-051` 1802 (21:44:05.150), 1534 | `7e00a5032a0422026803080710151dea71de7d5e25f5ad21282368a36c7e` |
| SERVER_STREAM `4:{13:3}` ch 19 | `CAP-038` 2170 (06:50:50.589; DLCI 0x03, no Message Stream open) | `7e80a3032a0422026803080710131dea71de7d5e25f5ad212880ca73fd7e` |
| SERVER_STREAM `4:{13:2}` ch 19 | `CAP-038` 2205; `CAP-056` 2324, 4028 (1st frame) | `7e80a3032a0422026802080710131dea71de7d5e25f5ad2128681188447e` |
| SERVER_STREAM `4:{13:0}` ch 21 | `CAP-045` 2277; `CAP-038` 2479 | `7e00a5032a0422026800080710151dea71de7d5e25f5ad21285a02de7d5d7e` |
| SERVER_STREAM `4:{13:1}` ch 19 | `CAP-021` 1842 | `7e80a3032a0422026801080710131dea71de7d5e25f5ad2128117bf5557e` |
| SERVER_STREAM `4:{13:4}` ch 19 | `CAP-056` 2259, 4025 | `7e80a3032a0422026804080710131dea71de7d5e25f5ad21289ac572667e` |
| SERVER_STREAM `4:{12:{1:0 2:1 3:1 4:1}}` ch 19 | `CAP-056` 1696 | `7e80a3032a0c220a62080800100118012001080710131dea71de7d5e25f5ad21281fbcad167e` |
| SERVER_STREAM `4:{2:1}` ch 19 | `CAP-056` 2175 | `7e80a3032a0422021001080710131dea71de7d5e25f5ad2128475780f97e` |
| SERVER_STREAM `4:{29:1}` ch 21 (mirror of the write 2492; RESPONSE 2506) | `CAP-069` 2502 | `7e00a5032a052203e80101080710151dea71de7d5e25f5ad2128cff7296f7e` |
| SERVER_STREAM field 16, all-zero quintet, ch 19 | `CAP-015` **2116** (the prompt wrote 2117; 2117 is the WriteSetting ACK) | `7e80a3032a1e221c8201190d0000000015000000001d0000000025000000002d00000000080710131dea71de7d5e25f5ad2128b1cbb10b7e` |

Timing (`CAP-051`, channel 21): Notify 1707 (`0813000401e8e808`, 21:43:43.336) → stream 1717 (+144 ms, 13:2); 1754 (`…e8e840`) → 1755 (+2 ms, 13:4);
1801 (`…e8e880`) → 1802 (+48 ms, 13:3). Own tap: 1527 Set (`0812001401e8e880…`, 21:43:28.107) → 1529 ACK `ff010006081201e8e880` (28.219) → 1530 Notify
`0813000401e8e880` (28.299) → 1533 Notify (28.375) → 1534 stream 13:3 (28.376) — 269 ms from the Set. `CAP-056` wear sequence: 2257 Notify `…e8e840`
(17:33:46.716) → 2259 stream 13:4 (+11 ms); 2322 `…e8e808` → 2324 13:2 (+1 ms); 3983 `…40` (17:35:31.366) and 4001 `…08` (31.534) ↔ 4025 13:4 (31.569) and
4028 13:2 (31.589) — the stream after the second Notify, 55 ms after it. `DISC` case: `CAP-051` 1993 Notify `0813000401e8e880` (21:45:45.603) → 1996 the
Buds' `DISC` DLCI 0x02 (45.608) → 1998 `DISC` DLCI 0x04 (45.612) → 2229 the re-open's subscribe on channel 19 (21:45:50.057) → 2231 `ReadSetting 4:13` → 2247
answer 13:0 (50.260). Note (no finding): in `CAP-038` the stream packet 2479 is on channel 21 while 2170/2205 are on channel 19 within the same connection.

### A.2 Design — the wire

- `Maestro.subscribeToSettingsChangesRequest(channelId)`: `RpcPacket(REQUEST, channelId, SERVICE_ID, METHOD_SUBSCRIBE_TO_SETTINGS_CHANGES, empty payload)`,
  sealed with the announced channel's ADR-034 request address — asserted byte-identical to `CAP-051` 1177 (ch 21) and `CAP-056` 1451 (ch 19).
- `Maestro.READABLE_FIELDS` gains 13; `Maestro.readSettingRequest(channelId, 13)` is then byte-identical to `CAP-051` 1178 / `CAP-056` 1450.
- Connect sequence (`launchInitialEqRead`, the same function for Connect and every ADR-044 re-open since both run `openSession`): `readEq` → `readSettings`
  (12 fields) → `subscribeRuntimeInfo` → **`subscribeToSettingsChanges`** → **`readAncSetting`** (`ReadSetting 4:13`, waits ≤ `SETTING_READ_TIMEOUT_MS`
  2 000 ms, never retried) → `readHardwareInfo` (checkpoint question: GetHardwareInfo last, as proposed, or before the two new requests). A failed send ends the
  sequence (the session is going away; the re-open subscribes again). Nothing else new on the wire; nothing written in reaction to a stream packet.

### A.3 Design — routing and decoding

- `SettingsCodec.FIELD_ANC_STATE = 13`; `SettingValue.AncState(mode: AncMode?)`: 1 → OFF, 2 → ACTIVE, 3 → TRANSPARENT, 4 → ADAPTIVE (`PROTOCOL.md` §4.1
  Update of 2026-10-14), 0 → `null` = no mode settable; any other value → the decoder returns `null` (not interpreted). Never encodable (no write path).
- `RoutedFrame.Setting` gains `stream: Boolean` (`SERVER_STREAM` = true, `RESPONSE` = false), so the repository can tell a push from a read answer.
- `routeMaestro`: a `SERVER_STREAM` of `SubscribeToSettingsChanges` whose payload is `4:{N:…}` with N outside `READABLE_FIELDS` (18 among them) → the new
  `RoutedFrame.UnreadSetting(field)` → one always-on, payload-free log line ("Settings stream: field N not shown (not read by this app)") — not an
  `UnidentifiedFrame`. A stream packet that does not parse as `4:{N:…}` stays an `UnidentifiedFrame` (Debug UI). A field-16 stream packet is an EQ frame
  (`CAP-015` 2116) and updates the equalizer like a read answer.
- Fuzz: `OversizedLengthTest`'s structured fuzz and `CodecRouterTest` gain server-stream packets of the subscribe method (random, truncated, oversized
  lengths, unknown fields, field 13 with the values 0–255), asserting no throw and only `Setting` / `UnreadSetting` / `UnidentifiedFrame` / `MalformedFrame`.

### A.4 Design — state and cause

- A stream `4:{13:n}` (n = 1…4): the mode is shown with the packet's receive time. Cause: equal to the mode of the app's own last `Set` within
  `ANC_SET_WINDOW_MS` → `SET_BY_APP` (the stream can come after the ACK ended the Set's wait, `CAP-051` 1534 at +269 ms, or between two Notifies, `CAP-056`
  4028); otherwise, if it differs from the shown mode → `CHANGED_BY_BUDS` ("Changed by the Buds (HH:MM:SS)"); the same mode again → the cause stays, the time
  moves. The repository keeps `(lastSetMode, lastSetAt)` from `ancSetOnClaim` besides today's `setPendingMode`.
- `ANC_SET_WINDOW_MS = 2 000` (new constant, `ARCHITECTURE.md` §3.2): both observed orders fit (269 ms, 55 ms) with margin; a hold to the mode just set
  changes nothing, so a wrong `SET_BY_APP` inside the window has no visible effect.
- A stream `4:{13:0}` or a RESPONSE `4:{13:0}`: never shown as a mode; `AncAvailability` → `NOT_ALLOWED` (checkpoint question A.8-Q5: whether the stream and
  the read also feed `AncAvailability` — recommended yes: 0 ↔ Settable `00`, ADR-049; 1–4 → `ALLOWED`, with the receive time).
- The RESPONSE to `ReadSetting 4:13` (Connect and re-open): a mode that differs from the last shown mode → `CHANGED_SINCE_LAST_READ` ("Changed since the app
  last saw it (read at HH:MM:SS)"); the first mode ever shown, or the same mode → `READ`. Checkpoint question Q4: whether a new Connect's first read that
  differs from the previous session's mode is `CHANGED_SINCE_LAST_READ` (one rule, recommended) or stays `READ` (0089's "a session's first read stays a
  reading", which `_ancModeCause = null` at Connect implements today). Collisions: the last value received wins, whatever its channel (Notify or stream).
- Other fields from the stream: applied through `applySetting` with the receive time. `changedByApp` stays `true` only when the current reading has the same
  value and was the app's own write (the mirror `CAP-069` 2502 of the app's write); otherwise `false` ("read HH:MM:SS"). Field 16 → `updateEqProfile`.
- Tile and notification: `currentAncMode()` on the repository flow — they follow the stream without a claim; the tile's own claim is only its Set.
- Logging (always on, payload-free): "Settings stream subscribed (channel N)", "Noise-control mode read (channel N)" / "… : no mode settable", "Noise-control
  mode from the settings stream (<cause>)", "Settings stream: field N not shown (not read by this app)"; failures via `eqErrorLogText`.

### A.5 Design — the removal (§3 item 2; executed file by file after the checkpoint's approval of this list)

Deleted files: `android/domain/src/main/kotlin/io/github/tedsluis/opencontrolpixelbuds/domain/HoldReclaimMode.kt` (with `ChannelTaken`,
`PlayServicesPresence`); `android/data/src/main/kotlin/io/github/tedsluis/opencontrolpixelbuds/data/settings/HoldReclaimSettingsStore.kt` and
`android/data/src/test/kotlin/io/github/tedsluis/opencontrolpixelbuds/data/settings/HoldReclaimSettingsStoreTest.kt`;
`android/app/src/main/kotlin/io/github/tedsluis/opencontrolpixelbuds/app/PlayServicesProbe.kt`; `android/data/src/test/kotlin/io/github/tedsluis/opencontrolpixelbuds/data/Cap074Fixtures.kt`
if no remaining test uses it. Edited: `BudsRepositoryImpl` (the ctor param, the hold, the chain, `setAncTabShown`, `ancChannelTaken`, `reclaimAncChannel`,
`HOLD_RECLAIM_*`; `withMessageStream` releases after `MESSAGE_STREAM_LINGER_MS` again), `BudsRepository` (three members), `RepositoryModule`, `MainActivity`
(store, probe, three callbacks), `AndroidManifest.xml` (`<queries>` + comment), `OpenControlNavHost`, `AncScreen` (the line, the button, the hint text),
`SettingsMenu` (the card, the Play line), the tests of 0084/0087/0089 (deleted or rewritten, not skipped), `TabRestoreTest` (the ADR-061 test),
`Cap075Fixtures.kt` (the Notify frames stay for the cause tests). The key `hold_reclaim_mode` is no longer read (one test: a preferences file with that key
and `dark_mode` — dark mode still reads, the key is untouched).

### A.6 Design — tests and mutations

Codec: the subscription's bytes on both channels; every fixture of A.1 decodes (type, field, value); value 0; a value 5 is not interpreted; `CAP-015` 2116 is
an EQ frame; `CAP-056` 1696 / 2175 / `CAP-069` 2502 are `Setting(stream = true)`; a stream `4:{18:…}` (synthetic, labelled) is `UnreadSetting(18)`; fuzz.
Repository (`FakeBudsTransport`, `runCurrent()`/`settle()`): Connect sends subscribe + `4:13` after `SubscribeRuntimeInfo`; the re-open sends both again
(the M1 test: a stub `android.bluetooth.BluetoothDevice` from a same-package test helper — the stub's constructor is package-private in the SDK stub; the test
then runs `connect()` → loss → ADR-044 re-open end to end; if the stub cannot be loaded the fallback is an `internal` re-open entry point); a push updates
the mode with its time; value 0; double pushes; Notify-then-stream and stream-then-Notify around an own Set (`SET_BY_APP` both); a push with no Set
("Changed by the Buds"); loss during the subscription (no retry); the `CAP-056` wear sequence (4 → 2 → 4 → 2); an EQ and a switch stream packet update
their values with the time; an unread field logged and ignored; the removal tests. UI: the ANC (i) texts, no card, no hint line, no Play line, the tile.
Mutations, one at a time with `--max-workers=2`: M1 the subscription skipped after a re-open; M2 value 0 shown as a mode; M3 a stream value equal to the own
Set classed "Changed by the Buds"; M4 the stream routed to `UnidentifiedFrame`; M5 an EQ stream packet ignored.

### A.7 Drafts for the checkpoint (the exact texts are in the questions' previews)

The new ADR is **ADR-065** (ADR-064 is the highest). It supersedes ADR-061, ADR-062 (with its Updates) and ADR-063 and amends ADR-064 items 2 and 6; its
text, the `AGENTS.md` §1 pointer, the ANC (i) text, the two cause lines, the `CHANGELOG.md` `[1.3.0]` block, the README/PROJECT status lines and the
release-notes feature line are the previews of Phase B.

### A.8 Checkpoint questions

Q1 ADR-065 text; Q2 `AGENTS.md` §1 pointer; Q3 the ANC (i) text; Q4 a new Connect's first differing read; Q5 the stream feeding `AncAvailability`; Q6 the
position of `GetHardwareInfo`; Q7 the Connect-time snapshot claim on DLCI 0x04 (keep — it brings the battery burst and the Settable byte; §2's "0 `SABM`"
count then starts after the Connect's snapshot); Q8 the deletion list (A.5); Q9 the optional `ReadSetting 4:8/9/10` in `CAP-076` (not part, as the prompt
says); Q10 the cause lines; Q11 the `CHANGELOG.md`/README/release-notes texts.

## B. Checkpoint (Phase B) — the maintainer's answers in chat, 2026-10-14, verbatim

Stated back once: §3's four decisions (1.3.0 / 10300; one new ADR = ADR-065 superseding ADR-061/062/063 and removing the setting, the hold, the chain, the
card, the hint, the Play line, `PlayServicesProbe`, `<queries>`; the stream feeding every read setting; the rewritten cause line and the hedged (i) text).

| # | Question (`AskUserQuestion`) | Answer |
|---|---|---|
| Q1 | ADR-065 text | "Accept as drafted (Recommended)" — the text of the preview (written into `DECISIONS.md` in Phase F unchanged) |
| Q2 | `AGENTS.md` §1 pointer | "Yes, add the pointer (Recommended)" — "- **Pointer (2026-10-14, maintainer-approved in chat, `DECISIONS.md` ADR-065, `ai-sessions/0091`):** superseded — the package query is gone; since 1.3.0 the app no longer asks Android whether `com.google.android.gms` is installed (no `<queries>` entry in the manifest)." |
| Q3 | ANC (i) text | "The prompt's text (Recommended)" — "The Buds report every change of the mode on the app's own channel. A press-and-hold on a bud is one cause; on film the Buds also switched the mode by themselves when a bud was put in or taken out — that part is a hypothesis." |
| Q4 | a new Connect's first differing read | "Changed since the app last saw it (Recommended)" — one rule for every read answer; `_ancModeCause` no longer reset at Connect; the kept mode of the ended session is the reference; the first mode ever shown this run stays READ |
| Q5 | the stream feeding `AncAvailability` | "Yes, feed it (Recommended)" — 0 → NOT_ALLOWED, 1–4 → ALLOWED, with the receive time; the Notify's Settable byte as today (last report wins) |
| Q6 | order | "Before GetHardwareInfo (Recommended)" — readEq → readSettings → SubscribeRuntimeInfo → SubscribeToSettingsChanges → ReadSetting 4:13 → GetHardwareInfo |
| Q7 | the Connect-time snapshot claim | "Keep it (Recommended)" — one `SABM` on DLCI 0x04 per Connect/re-open is expected; `CAP-076`'s count starts after it |
| Q8 | the deletion list | "Approve the list (Recommended)" — the four files of A.5, plus Cap074Fixtures.kt only if unused |
| Q9 | `ReadSetting 4:8/9/10` | "Not part of this run (Recommended)" |
| Q10 | the cause lines | "Rewritten as proposed (Recommended)" — "Changed by the Buds at HH:MM:SS: the Buds reported a new mode — a press-and-hold on a bud, or a change they made themselves." / "Changed since the app last saw it (read at HH:MM:SS): a press-and-hold on a bud or the Buds' own change, made while this app was not connected." |
| Q11 | `CHANGELOG.md` `[1.3.0]`, README/PROJECT lines, release-notes feature line | "Accept as drafted (Recommended)" — the texts of the preview |

## C. Build (Phase C) — as built

**Plain-language summary.** Since this build the app asks the Buds, at every Connect and every automatic re-open, to report every change of their settings on
the app's own channel, and reads the noise-control mode once right after. A press-and-hold on a bud therefore shows in the app within a second on any tab, with
or without Google Play services on the phone, and every setting the app shows follows a change made elsewhere. The hold of the shared channel, the re-claim
chain, the "Noise control with Google Play services" setting with its card, "Take it back again" and the Play-services line are gone; the shared channel is
claimed only for a tap, as before 1.2.0.

**Wire.** `Maestro.subscribeToSettingsChangesRequest(channelId)` (empty payload, method `0x2821adf5`) and `Maestro.readSettingRequest(channelId, 13)`
(`READABLE_FIELDS` += 13) — byte-identical to `CAP-051` 1177/1178 (channel 21) and `CAP-056` 1451/1450 (channel 19), asserted in `SettingsStreamCodecTest`.
`launchInitialEqRead()` (Connect and re-open, through `openSession`): `readEq` → `readSettings` → `subscribeRuntimeInfo` → `subscribeToSettingsChanges` →
`readAncSetting` → `readHardwareInfo` (Q6). Each step fails on its own, never retried (the ADR-065 "as built" note). Nothing else new on the wire; nothing
written in reaction to a stream packet (`git diff` shows exactly two new `transport.send` call sites, both DLCI 0x02 requests).

**Codec.** `SettingValue.AncState(mode: AncMode?)` (field 13: 0 → `null`, 1–4 → the mode, else not interpreted, never encodable); `RoutedFrame.Setting`
carries `stream: Boolean`; `RoutedFrame.UnreadSetting(field)` for a `SERVER_STREAM` `4:{N: …}` with N outside `READABLE_FIELDS` (`SettingsCodec.fieldOf`);
a stream packet that does not parse stays an `UnidentifiedFrame`; field 16 from the stream is an EQ frame (as before), field 18 an EQ frame with `persist`.

**State and cause** (`BudsRepositoryImpl`). `applyAncState(state, stream)`: the availability from the value (0 → `NOT_ALLOWED`, 1–4 → `ALLOWED`, Q5), the mode
with the packet's time; `causeOfStream` (own `Set` pending or within `ANC_SET_WINDOW_MS` = 2 000 ms → `SET_BY_APP`; differing → `CHANGED_BY_BUDS`; first ever →
`READ`; same → unchanged), `causeOfReadAnswer` (differing → `CHANGED_SINCE_LAST_READ`, else `READ`) used by both the `4:13` RESPONSE and the `Get`'s `Notify`;
`_ancModeCause` no longer reset at Connect (Q4); `lastSetMode`/`lastSetAt` recorded in `ancSetOnClaim`. Other fields: `applySetting` with the receive time,
`keepsOwnMark` keeps `changedByApp` for the mirror of the app's own write. `currentAncMode()` (tile, notification) follows the same flow.

**Removal** (Q8 list, executed file by file after a fresh listing): deleted `HoldReclaimMode.kt` (with `ChannelTaken`, `PlayServicesPresence`),
`HoldReclaimSettingsStore.kt` + its test, `PlayServicesProbe.kt`, `Cap074Fixtures.kt` (unused after the hold tests went); edited `BudsRepositoryImpl` (the
ctor param, the hold, the chain, `setAncTabShown`, `ancChannelTaken`, `reclaimAncChannel`, `HOLD_RECLAIM_*`; `withMessageStream` releases after the linger
again), `BudsRepository`, `RepositoryModule`, `MainActivity`, `AndroidManifest.xml` (`<queries>` + comment gone), `OpenControlNavHost`, `AncScreen`
(`ANC_STREAM_HINT_TEXT`, the two cause lines — Q3/Q10), `SettingsMenu` (the card and the Play line), `DarkModeSettingsStore` (a `DataStore` constructor for the
JVM test; `hold_reclaim_mode` never read). Always-on, payload-free log lines: "Settings stream subscribed (channel N)", "Noise-control mode read (channel N)",
"Noise-control mode from the settings stream (<cause>)" / "… from the mode read (…)", "… : no mode settable", "Settings stream: field N not shown (not read by
this app)", and the failures with `eqErrorLogText`.

**Tests.** New: `SettingsStreamFixtures.kt` (the real frames of §A.1 with file and frame), `SettingsStreamCodecTest` (the requests on both channels; every
RESPONSE and stream packet; value 0; the other fields; field 13 with the values 0–255; unread fields 8/10/20/31 → `UnreadSetting`, the same as a RESPONSE →
`RpcResult`; field 18; a fuzz over mutated/truncated/oversized/random-field stream packets), `DarkModeSettingsStoreTest` (a 1.2.2 file with
`hold_reclaim_mode` — read, written, the key untouched), `android/bluetooth/StubBluetoothDevice.kt` (the SDK stub's package-private constructor, so
`connect()` and the ADR-044 re-open run end to end against the fake transport), the ADR-064/065 section of `BudsRepositoryImplTest` (the Connect sequence's
bytes and order; the re-open subscribing again — M1; a lost session at the subscription not retried; a push as changed by the Buds with its time, nothing sent,
no claim; value 0 — M2; a differing read of 13 and the `CAP-051` 2247 case; the own Set in `CAP-051`'s order, before the ACK and after a cut-off answer — M3;
the `CAP-056` wear sequence; the other fields — M5, with the own-write mirror; an unread field — M4; nothing held or re-claimed). Rewritten: the 0089 cause
tests (a session's first differing read is now "changed since", the Connect keeps the cause), `AncScreenTest` (the (i) text, the two cause lines, no card/hint/
button/Play line), `SettingsMenuTest` (no card, no Play line), `TabRestoreTest` (the ADR-061 test removed), the Connect-sequence counts (15 requests),
`PwRpcTest`/`SettingsCodecTest` (13 readable, the stream flag), `Settings036` gains `CAP-036` 1517/1519 (`ReadSetting 4:13` → `4:{13:0}`) so the official
sweep's answer set is complete.

## D. Gate, mutations, compliance (Phase D)

- `cd android && ./gradlew assembleDebug testDebugUnitTest test lint --max-workers=2`: **BUILD SUCCESSFUL in 2m 29s, exit 0**. JUnit XML (debug + release
  summed): `:data` 3344 (1672 per variant: the hold/re-claim tests and `HoldReclaimSettingsStoreTest` out, the stream tests in), `:hardware` 128, `:domain` 42,
  `:ui` 184 (92 per variant; 102 before — the ADR-061/062/063 screen tests out, two in), `:app` 0; 0 failures. Lint: 7 warnings (6 `:app`, 1 `:ui`), no error.
- Mutation checks, one at a time (`--max-workers=2`, the script in the scratchpad; the sources restored and verified byte-identical afterwards):
  M1 (the subscription sent once per process, not per session) → 2 tests fail (the re-open test, the lost-session test); M2 (0 decoded as Off) → 4 fail;
  M3 (`causeOfStream` without its `SET_BY_APP` branches) → 1 fails (the stream before the ACK / after a cut-off answer); M4 (no `UnreadSetting`) → 2 fail;
  M5 (an EQ frame only from a RESPONSE) → 6 fail. All five caught.
- Compliance: the manifest's permissions unchanged (`BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`,
  `FOREGROUND_SERVICE_CONNECTED_DEVICE`; the merged manifest adds only AndroidX Core's private receiver permission), **no `<queries>`**, no `INTERNET`;
  `git diff HEAD -- '*.kts' gradle/libs.versions.toml` empty (no dependency change), `./gradlew :app:dependencies --configuration releaseRuntimeClasspath`
  run (892 lines, no gms/firebase/sentry/crashlytics/analytics/okhttp/retrofit); the AGPL header on every new and changed Kotlin file; no hidden API, no
  reflection; no MAC or serial in any new log line (all payload-free; the unread-field test asserts the payload bytes are not in the log); the only new wire
  bytes are the two requests.

## E. `CAP-076` (Phase E)

The skeleton captures/CAP-076-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BL/CAP-076-EVENT-NOTES.md rewritten for 1.3.0 (both users, the extended tier, screen
recordings with sound and a camera on the ears; BL-1 … BL-25 with the expected bytes of the two requests, the `4:{13:n}` answers and stream packets per channel
and value, the other fields' packets, the own tap's order, the `tshark` lines; the counts for the verdict in the analysis checklist); Group BL and the index
row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`; the registry row (status planned); `APP_TESTPLAN.md` §Y (Y1 … Y22) and its header note; the Summary row.

## F. Documents (Phase F)

`DECISIONS.md`: ADR-065 (the approved text, plus an "as built" note on item 1's sequence convention), the Status lines of ADR-061/062/063 ("Superseded by
ADR-065"; texts kept), pointer Updates on ADR-032 (the hold exception gone) and ADR-064 (built; items 2 and 6 amended). `id_registry.csv`: ADR-065 added,
ADR-061/062/063 → superseded, ADR-064 and `CAP-076` updated. `AGENTS.md` §1: the approved pointer. `ARCHITECTURE.md`: §2 (the store line), §2.4 (the (i) and
cause texts; the 1.2.x card as history), §3.1 (the stream rule), §3.2 (`ANC_SET_WINDOW_MS`; `HOLD_RECLAIM_*` gone; the subscription row), §5a (the cause row),
§6.0b (the hold/re-claim paragraphs as history with a pointer; the 1.3.0 paragraph), §7 (`ChannelTaken` as history), §9 (no package visibility). `PROTOCOL.md`:
unchanged (its §4.1 Update of 2026-10-14 is still right). `TODO.md`: §1 (`CAP-076` = the 1.3.0 run), §2 (the hold branches moot), §3 (the row), §4 (item 4
built), §5 ("Next" done; the Play-user item reduced to the per-tap collision; E-01 stays). `PROJECT.md`, `README.md` (the 1.3.0 paragraph, Current state),
`CHANGELOG.md` `[1.3.0]`, `scripts/release_notes.template`, `android/app/build.gradle.kts` 1.3.0 / 10300, `ai-sessions/INDEX.md`. Then `ensure_footers.py`
and `lint_docs.py` (below).

## G. Files read, deferred documentation, commits

**Files read in full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `DECISIONS.md` (ADR-001 … ADR-064 with every Update), `TODO.md`,
`AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`, `RELEASING.md`, the prompt, `ai-sessions/0090_CROSSCHECK_PROMPT`/`RESULT`, the `0087` and `0089`
RESULTs, `DESKRESEARCH_FINDINGS.md` entry 2026-10-14, `REVERSE_ENGINEERING.md`'s `qhr` Update of 2026-10-14, `CAP-051-FINDINGS.md` §2–§3, `CAP-038-FINDINGS.md`
§3/§5, `CAP-056-FINDINGS.md` §4, `CAP-075-FINDINGS.md` §1/§4/§5/§9, `CAP-073-FINDINGS.md` §4, `CAP-076-EVENT-NOTES.md` (the 1.2.2 skeleton), `APP_TESTPLAN.md`
§W/§X/§Y/Summary, `CHANGELOG.md`'s head and `[1.2.2]`/`[1.2.1]`/`[1.2.0]`, README's Status and Current state, `scripts/release_notes.template`,
`id_registry.csv`'s tail; Kotlin in full: `BudsRepositoryImpl.kt`, `SessionReopener.kt`, `codec/Maestro.kt`, `CodecRouter.kt`, `SettingFrame.kt`, `PwRpc.kt`,
`AncFrame.kt`, `RuntimeInfo.kt`, `EqFrame.kt`, `EqFrameDecoder.kt`, `HoldReclaimSettingsStore.kt`, `DarkModeSettingsStore.kt`, `SettingsDataStore.kt`,
`BudsRepository.kt`, `AncModeCause.kt`, `HoldReclaimMode.kt`, `DeviceInfo.kt`, `AncMode.kt`, `AncTile.kt`, `BudsSettings.kt`, `ValueCurrency.kt`,
`MainActivity.kt`, `AncTileService.kt`, `PlayServicesProbe.kt`, `RepositoryModule.kt`, `AndroidManifest.xml`, `app/build.gradle.kts`, `data/build.gradle.kts`,
`OpenControlNavHost.kt`, `AncScreen.kt`, `ConnectionBanner.kt`, `SettingsMenu.kt`, `BudsTransport.kt`, `FakeBudsTransport.kt`, `Cap074Fixtures.kt`,
`Cap075Fixtures.kt`, `Cap061Fixtures.kt`, `HoldReclaimSettingsStoreTest.kt`, `SessionReopenerTest.kt`, `CodecRouterTest.kt`, `SettingsFixtures.kt`,
`SettingsCodecTest.kt`, `OversizedLengthTest.kt`, `AncScreenTest.kt`, `SettingsMenuTest.kt`, `TabRestoreTest.kt`. **In part:** `PROTOCOL.md` (§2.2a, §4.1 with
its 2026-10-14 Update, §4.3 Option F, §4.5, §6, §8's last rows), `BudsRepositoryImplTest.kt` (lines 1–720, 1008–1060, 1180–1345, 1750–1835, 2470–2800,
2975–3634; the rest by its index of test names), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the rows of `ANC-001`, `HOLD-005`, `FW-003`, `TOUCH-007`, `BATT-004`,
`PAIR-003`), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BL, the `CAP-076` row, the headings), `APP_TESTPLAN.md`'s header, `Details.kt` (the `CardTitle` API),
`PwRpcTest.kt` (the method ids and the readable-fields test), `OpenControlApplication.kt` (`currentAncMode()`), `Cap045`/`Cap065`/`Cap072`
fixture names, `Varint.kt`, `CaseBatteryFrame.kt` (`Proto.fields`).

**Deferred documentation:** none beyond `TODO.md`'s open items (the maintainer's next steps below; `CAP-076`'s analysis will add an ADR-065 Update with the
numbers and may settle `PROTOCOL.md` §6's wear-change item). The capture logs' `tshark` positive control for "0 `SABM` from the app" belongs to that analysis.

**Commits:** none yet — the maintainer's yes is asked first (Phase G). Proposed, per concern: `feat`: the subscription, the read of 13 and the stream in the
codec and the repository; `refactor`: the hold/re-claim/Play removal (code, manifest, UI); `test`: the fixtures and tests; `docs`: ADR-065 and the documents,
`CAP-076`, 1.3.0.

**The maintainer's next steps (not executed by this session):** push `feature/settings-stream` and open the PR with the Release checklist; CI green; B1
`scripts/release.sh 1.3.0`, B2, B3; C1–C2 the `CAP-076` run in both users (the skeleton); a CAPTURE session for C3/C4 and the verdict; only then the
release (never by this session: no tag, no `dist/`, no merge).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0091_FEATURE_RESULT_2026_10_14.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0091_FEATURE_RESULT_2026_10_14
