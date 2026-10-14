# 0091_FEATURE_PROMPT_2026_10_14.md — Build ADR-064: the noise-control mode from the app's own channel (DLCI 0x02, setting 13), replacing the hold, the re-claim and the Play-services card of ADR-061/062/063 — version 1.3.0

**Number:** 0091
**Category:** FEATURE
**Date:** 2026-10-14
**Title:** Build ADR-064 (accepted 2026-10-14, `ai-sessions/0090`): one `SubscribeToSettingsChanges` and one `ReadSetting 4:13` per MAESTRO session, so every
press-and-hold and every mode change the Buds make reaches the app on DLCI 0x02 — the channel no other client contends for — without a Message Stream claim.
The settings stream updates every setting the app already reads (the mode, the EQ, the switches). ADR-061's hold, ADR-062's re-claim with its "Noise control
with Google Play services" card and ADR-063's Play-services line are **replaced by one new ADR** and removed from the app; the setting disappears. The cause
line is rewritten. Switching the mode stays a short Message Stream claim per tap (ADR-032). Real capture bytes as fixtures, a green gate with mutation checks,
`CAP-076` rewritten as the hardware run of **1.3.0**, the documents, the release preparation (no tag, no publication). `WriteSetting 4:{13:n}` is **out of
scope**.

---

## 0. How to use this prompt

You are an expert software architect, Android/Kotlin engineer, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in
this repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Number check first.** `0091` is the next free number after `0090` (`ai-sessions/INDEX.md`, checked 2026-10-14). If another session took it, use the next
free number and rename this file and its RESULT (`AI_SESSION_LOG_PROCEDURE.md` §3).

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action, read **in full**, in this order: `AGENTS.md`,
`PROJECT_RULES.md`, `PROJECT.md`, **`ARCHITECTURE.md` (the whole file — `ai-sessions/0090` read it only in part, and this build touches §2.4, §3.1, §3.2, §5,
§5a, §6, §6.0b, §7, §8.1, §12)**, `PROTOCOL.md` (**§2.2a, §4.1 with its Update of 2026-10-14, §4.3 Option F, §4.5 (every setting the app reads), §6 in full**;
the rest at least by its headings and §8's last rows), `DECISIONS.md` (**every** ADR, ADR-001 … ADR-064 with every dated Update — in particular ADR-032 and its
Updates, ADR-034, ADR-036, ADR-042, ADR-044, ADR-045 … ADR-047, ADR-049, ADR-052 … ADR-055, ADR-057, ADR-059, **ADR-061, ADR-062 with its two Updates, ADR-063,
ADR-064**; the draft ADR-060 is not a decision), `TODO.md` (§1, §2, §4's items of 2026-10-14, §5's "Next" item, §6). Then: `AI_SESSION_LOG_PROCEDURE.md`
(§4b, §9), `ai-sessions/INDEX.md`, `RELEASING.md` (the Release checklist, "Repository rules", §4–§8, §11 item 4, §11a, §13), `APP_TESTPLAN.md` (§W, §X, §Y and
the Summary), `CHANGELOG.md` (`[1.2.2]`, `[1.2.1]`, Known issues), `README.md` (status, known issues), `id_registry.csv`, and:

- **`ai-sessions/0090_CROSSCHECK_PROMPT_2026_10_14.md` and `ai-sessions/0090_CROSSCHECK_RESULT_2026_10_14.md`** — the evidence this build rests on: the Summary,
  Phase A (E-02, E-01, E-03), the checkpoint answers (verbatim), **the continuation in chat** (the stored mode per wear state, 28 of 28; settings 8/9/10 never
  on any wire; the 12 changes that fall on a `DISC`);
- `DESKRESEARCH_FINDINGS.md`, entry 2026-10-14 (§1–§11: the census tables, the commands, the raw hex, the provenance note);
- **`REVERSE_ENGINEERING.md`, the `qhr` register with its Update of 2026-10-14** (how the official app subscribes: `fxg` → `fvo` case 2 → every `qja` packet →
  `fxb.a(qja)`; `fxb` case 13 → `gea.I`; the `ReadSetting 4:13` sent by `fwx` on runtime-info field 7) — the official app is the reference behaviour, the code
  is not copied (`AGENTS.md` §12);
- `captures/CAP-051-2026-09-14_21-42-55_21-44-09-Group_AM/CAP-051-FINDINGS.md` §2–§3 (the filmed holds and their stream packets),
  `captures/CAP-038-2026-09-06_06-49-50_06-53-57-Group_AE/CAP-038-FINDINGS.md` §3/§5 (the stream with no Message Stream open),
  `captures/CAP-056-2026-09-28_17-30-53_17-35-58-Group_AR/CAP-056-FINDINGS.md` §4 (the wear changes),
  `captures/CAP-075-2026-10-12_17-22-31_2026-10-13_06-34-32-Group_BK/CAP-075-FINDINGS.md` §1, §4, §5, §9 (the contention this build removes from the
  *showing* of the mode; Play's timers), `captures/CAP-073-2026-10-10_08-23-35_08-33-24-Group_BI/CAP-073-FINDINGS.md` §4 (the user without Play — every
  hold shown; must not regress), `captures/CAP-076-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BL/CAP-076-EVENT-NOTES.md` (the 1.2.2 skeleton this session rewrites);
- `ai-sessions/0087_FEATURE_RESULT_2026_10_12.md` and `ai-sessions/0089_FEATURE_RESULT_2026_10_13.md` — how the hold, the re-claim, the card and the cause line
  were built (what this session removes or rewrites; their mutation checks as the pattern);
- **Every Kotlin source this build touches, in full, before changing it** — at least: `BudsRepositoryImpl.kt` (the Connect sequence — `ReadSetting 4:16`,
  `SubscribeRuntimeInfo`, `GetHardwareInfo` ≈ `:1163–1626`; `setAncTabShown` ≈ `:421`, `reconcileHold`, `endHold`, `onHeldClaimTaken`, `startReclaimChain`,
  `cancelReclaim`, `reclaimAncChannel` ≈ `:1130`, `causeOfNotify` ≈ `:366`, `emitAncMode`, `refreshAncMode`, the `transport.channelClosed` collector, the
  constants ≈ `:1776–1796` — `MESSAGE_STREAM_LINGER_MS = 1_500L`, `HOLD_RECLAIM_STEP_MS`, `HOLD_RECLAIM_CAP`, `MAESTRO_ANNOUNCE_WAIT_MS = 3_000L`),
  `SessionReopener.kt` (ADR-044 — every re-open must subscribe again), `codec/Maestro.kt` (`READABLE_FIELDS` ≈ `:55`, `readSettingRequest`,
  `subscribeRuntimeInfoRequest` ≈ `:90`, `getHardwareInfoRequest`, `MaestroChannel`), `codec/CodecRouter.kt` (`RoutedFrame`, the DLCI 0x02 route: how
  `RESPONSE` and `SERVER_STREAM` packets are told apart today — `RuntimeInfoDecoder`), `codec/SettingFrame.kt` (`SettingsCodec`: the field decoders the stream
  will feed), `codec/PwRpc.kt`, `codec/AncFrame.kt` (the mode bitmask; `AncAvailability` in `:domain`'s `DeviceInfo.kt`), `BudsRepository.kt` (`:domain`:
  `setAncTabShown`, `reclaimAncChannel`, `ancChannelTaken`, `ancModeCause`), `AncModeCause.kt`, `HoldReclaimMode.kt`, `settings/HoldReclaimSettingsStore.kt`,
  `app/PlayServicesProbe.kt`, `app/AncTileService.kt` (how the tile learns and sets the mode), `app/MainActivity.kt` and `OpenControlNavHost.kt` (the wiring of
  the hold, the setting, the Play line), `ui/AncScreen.kt` (the (i) lines, the hint), `ui/SettingsMenu.kt` (the card ≈ `:213–260`), `ui/ConnectionBanner.kt`,
  `app/src/main/AndroidManifest.xml` (the `<queries>` entry of ADR-063), `android/app/build.gradle.kts`; the tests: `BudsRepositoryImplTest.kt`,
  `SessionReopenerTest.kt`, `CodecRouterTest.kt` (incl. the fuzz test), `SettingsFixtures.kt`, `Cap074Fixtures`, `FakeBudsTransport`, `AncScreenTest.kt`,
  `SettingsMenuTest.kt`, `HoldReclaimSettingsStoreTest.kt`.

Say in the RESULT what was read in full and what in part.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5).** Create **ai-sessions/0091_FEATURE_RESULT_2026_10_14.md** at the very start with
`**Status:** partial — resumed` and a **Progress** block; update it at the end of every phase and after every substantial step (each file changed, each test
class green, each gate run). A resumed session reads this prompt, then the Progress block, re-reads the files the unfinished step touched, and continues; it
never redoes a recorded step and never assumes an unrecorded one.

**Memory and the machine.** 15 GB laptop. Never two Gradle builds at once; run mutation checks one at a time with `--max-workers=2` and check for leftover
mutations after a crash. In `BudsRepositoryImplTest` use `runCurrent()`/a settle helper for `backgroundScope` collectors (`advanceUntilIdle()` does not run
them). `android/domain/bin/` is build output: never stage it.

## 1. What the maintainer asked for (chat, 2026-10-14, after `ai-sessions/0090` and PR #55)

> "De nieuwe versie wordt meteen build 1.3.0 en `CAP-076` moet herschreven worden voor het testen van 1.3.0. ADR-061 (de hold op het tabblad) en ADR-062/063 (de
> re-claim, "Take it back again", de kaart met de Play-services-detectie) moeten vervangen worden door een nieuwe ADR. De instelling vervalt in een nieuwe
> versie. De stream werkt voortaan niet alleen veld 13 bij, maar alle velden die de app al leest (EQ, de schakelaars). Die komen ook langs. Wat mij betreft worden
> meteen alle velden bijgewerkt. De oorzaakregel wordt ook herschreven. "Changed by the Buds" kan nu uit elke push komen zonder eigen Set. Een tekst als "bij het
> in- of uitdoen van een bud" blijft 🟡 en hoort dus voorzichtig geformuleerd."

And the frame the maintainer set: ADR-064 is read-only. It removes the fight over **showing** the mode and the holds. **Switching** stays `08 12` on DLCI 0x04
through a short claim per tap (ADR-032) — not a fight, a short collision per tap at most. Only the E-01 experiment (`WriteSetting 4:{13:n}`) could remove that
too; it is a separate experiment, not this build.

## 2. The goal, with measurable acceptance

In the GrapheneOS user **with** Google Play services, the noise-control tab open, no tap:

1. **0 `SABM` from OpenControl on DLCI 0x04** for the whole observation (the only claims are per tap: one `SABM`, the `Get`/`Set`, released within
   `MESSAGE_STREAM_LINGER_MS`);
2. **every press-and-hold on the screen within about 1 s** (the stream packet arrives 1–150 ms after the Buds' Notify, `PROTOCOL.md` §4.1 2026-10-14);
3. **a hold that falls on a Buds-side `DISC` of MAESTRO** (12 of 77 in the captures) **is shown after the re-open's `ReadSetting 4:13`** (ADR-044's re-open, then
   the read) — with the cause "changed since the app last saw it";
4. the same three with the tab **closed** (the mode on the tile and in the notification follow — ADR-057's "current" rule);
5. the user **without** Play services sees no regression against `CAP-073`/`CAP-075` set 1 (41 of 41 holds shown within a second).

The same acceptance, as counts, is the hardware run's verdict (§2 of the skeleton, item 7 below).

## 3. Decisions already taken (the maintainer, chat 2026-10-14) — confirm once at the checkpoint, then do not reopen

The approvals below were given in chat; `AGENTS.md` §6 and this project's practice (an approval quoted only in a prompt file is not an approval in the
executing session) mean the session states them back **once** at its checkpoint (Phase B) and asks for the ADR text's approval there; it does not re-argue them.

1. **Version 1.3.0** (versionCode 10300, `android/app/build.gradle.kts`); `CHANGELOG.md` `[1.3.0]` (no date); `scripts/release_notes.template`; `README.md`
   and `PROJECT.md`'s status lines.
2. **One new ADR (the next free number in `id_registry.csv` — ADR-064 is the highest today; the prompt calls it "the new ADR") supersedes ADR-061 (the hold), ADR-062 with its Updates (the re-claim, the
   "Take it back again" action, the "Noise control with Google Play services" card) and ADR-063 (the Play-services line and its `<queries>` entry).** The
   setting is removed from the app: `HoldReclaimMode`, `HoldReclaimSettingsStore` (its DataStore key `hold_reclaim_mode` is read no more; a stored value is
   ignored, never migrated into anything), `ChannelTaken`, `ancChannelTaken`, `reclaimAncChannel`, `setAncTabShown` and the hold, the chain, the card, the hint
   line, `PlayServicesProbe`, the `<queries>` element and the manifest comment. ADR-032's claim model (claim per tap, linger, release) is **unchanged** and is
   again the only Message Stream behaviour. `AGENTS.md` §1's note on ADR-063 gets a dated pointer ("superseded by the new ADR; the package query is gone") —
   project law, so it needs the maintainer's yes at the checkpoint. ADR-061/062/063's texts are not rewritten: their Status lines get "Superseded by the new ADR".
3. **The settings stream updates every setting the app reads** — not only field 13: the EQ (16, and 18 if the app shows it), the switches and values of
   `Maestro.READABLE_FIELDS` (2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29) — through the **same decoders** as the `ReadSetting` answers (`SettingsCodec`), with
   the receive time; a field the app does not read is logged payload-free and not interpreted. ADR-064 item 2 ("only field 13 is decoded") is amended by
   the new ADR accordingly — say so in the ADR text. Nothing is written in reaction to a stream packet.
4. **The cause line is rewritten.** Today's `AncModeCause` (`READ`, `SET_BY_APP`, `CHANGED_BY_BUDS`, `CHANGED_SINCE_LAST_READ`) was built around the Message
   Stream. Now: a stream value that differs from the mode last shown, with no own `Set` in the last `SET_WINDOW`, is **"Changed by the Buds (HH:MM:SS)"** — the
   cause is the push itself, whatever triggered it (a press-and-hold, a wear change, another client's Set); the (i) says what the Buds are known to do, in
   hedged words: *"The Buds report every change of the mode on the app's own channel. A press-and-hold on a bud is one cause; on film the Buds also switched
   the mode by themselves when a bud was put in or taken out — that part is a hypothesis."* (🟡, `PROTOCOL.md` §6 item of 2026-10-14; never stated as a fact in
   the UI). A value read by the re-open's `ReadSetting 4:13` that differs from the last shown mode keeps the "changed since the app last saw it" cause.
   Exact texts are checkpoint questions.

## 4. Design rules for the build (binding)

- **Requests.** One `SubscribeToSettingsChanges` REQUEST per MAESTRO open — at Connect **and after every ADR-044 re-open** (`SessionReopener`), after
  `SubscribeRuntimeInfo`; then one `ReadSetting 4:13` at every Connect and every re-open (add 13 to `READABLE_FIELDS`; the Connect sweep already reads the
  other fields). Bytes: the subscription byte-identical to `CAP-051` 1177 (channel 21) / `CAP-056` 1451 (channel 19), i.e. the `SubscribeRuntimeInfo` request's
  shape with method `0x2821adf5` = `h65599("SubscribeToSettingsChanges")`; the read as every other `ReadSetting`. On the announced channel with its ADR-034
  address; no retry, no polling, nothing else new on the wire. 🟡 the Buds push the stream only to a subscriber (0 stream packets in 33 unsubscribed logs) —
  the build assumes the subscription is needed after every re-open, and the hardware run checks it.
- **Routing.** The stream's packets are `SERVER_STREAM` (`RpcPacket.type` 7) with method `SubscribeToSettingsChanges`; a `ReadSetting` answer is `RESPONSE`
  (type 1). `CodecRouter` routes them to the same settings decoders; a stream packet for a field outside `READABLE_FIELDS` becomes a payload-free log line, not
  an `UnidentifiedFrame` (it is identified — its field is just not shown). The fuzz test covers server-stream packets (random, truncated, oversized, unknown
  fields, field 13 with values 0–255).
- **Value 0** of setting 13 = "no mode settable" — the same reading as Settable `00` (ADR-049; today's `AncAvailability`); never shown as a mode.
- **Colliding reports.** The last value received wins, whatever its channel. Around an own `Set` the order is Set → ACK → Notify → stream (`CAP-051` 1527 →
  1529 → 1530/1533 → 1534, 268 ms end to end), but the stream can also come **before** the ACK or between two Notifies (`CAP-056` 3983/4001 ↔ 4025/4028): a
  stream value equal to the mode just set is the app's own change (cause `SET_BY_APP`), not "changed by the Buds"; use the existing `SET_WINDOW` idea, with
  a repository test for both orders.
- **The tile (`AncTileService`)** reads the mode from the repository's flow as today (so from the stream) and uses the short claim only for its `Set`.
- **The Safe-Mode gate** (ADR-042) is unchanged: the subscription and the read are reads, not gated; writes stay gated.
- **No background work:** the stream lives and dies with the MAESTRO session; nothing is scheduled; no service changes; no new permission or dependency.
- **Timing constants** go into `ARCHITECTURE.md` §3.2 if any is added (e.g. a wait for the first read; prefer the existing settings-read bound).

## 5. Fixtures (real bytes, file and frame in a comment — `AGENTS.md` §11; the raw hex is in `DESKRESEARCH_FINDINGS.md` 2026-10-14 and the RESULT of 0090)

- the subscribe REQUEST: `CAP-051` 1177 (channel 21, `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 f5 ad 21 28 01 97 54 b0 7e`), `CAP-056` 1451 (channel 19);
- stream packets with field 13: `CAP-051` 1717 (`13:2`), 1755 (`13:4`, `7e 00 a5 03 2a 04 22 02 68 04 08 07 10 15 1d ea 71 de 7d 5e 25 f5 ad 21 28 39 67 a2 f7 7e`),
  1802 (`13:3`); `CAP-038` 2170 (`13:3`, channel 19, with no Message Stream open); `CAP-045` 2277 (`13:0`); `CAP-021` 1842 (`13:1`);
- stream packets with other fields: `CAP-056` 1696 (`12:{…}`), 2175 (`2:1`), `CAP-069` 2492's mirror (`29:1`), `CAP-015` 2117 (field 16) — take them from
  `python3 scripts/pwrpc_decode.py <log>` and quote the raw frame;
- `ReadSetting 4:13` request/answer pairs with every value: `CAP-021` 1069 → 1089 (`13:1`), `CAP-056` 1450 → 1468 (`13:2`), `CAP-027` 910 → 923 (`13:3`),
  `CAP-051` 1178 → 1225 (`13:4`), `CAP-045` 2021 → 2033 (`13:0`);
- the Notify ↔ stream pairs for the cause tests: `CAP-051` 1707/1717, 1754/1755, 1801/1802 (holds), 1527/1529/1530/1534 (the app's Set);
- a `DISC` in the middle of a change: `CAP-051` 1993 (Notify) → 1996 (`DISC` DLCI 0x02) → 2229 (re-open's subscribe) → 2247 (`ReadSetting 4:13` = `13:0`).

A hand-built array is allowed only as a clearly labelled structural test beside these.

## 6. Tests (unit, `:data`/`:domain`/`:ui`/`:app` where the module has a set-up)

- codec: encode of the subscription (byte-identical to the fixtures on both channels), decode of every fixture above, the fuzz extension;
- repository (`FakeBudsTransport`, `runCurrent()`): the subscription and the read at Connect and after a re-open; a push updates the mode with its time; value 0;
  double pushes; Notify-then-stream and stream-then-Notify around an own Set (cause `SET_BY_APP` both times); a push with no Set (cause "Changed by the Buds");
  a loss during the subscription (no retry, the re-open subscribes again); a wear-change sequence from `CAP-056` (the mode jumps to the stored value and back);
  a stream packet for the EQ and for a switch updates that value with its time; a stream packet for an unread field is logged and ignored;
- the removal: no `setAncTabShown`/re-claim code paths left (the tests of `ai-sessions/0084`/`0087`/`0089` deleted or rewritten, not skipped); a stored
  `hold_reclaim_mode` value is ignored (one test);
- UI: the ANC (i) texts, no card, no hint line, no Play line; the tile's mode follows the repository flow;
- mutation checks (one at a time): M1 the subscription not sent after a re-open → the re-open test fails; M2 value 0 shown as a mode → fails; M3 a stream value
  equal to the own Set classed "Changed by the Buds" → fails; M4 the stream routed to `UnidentifiedFrame` → fails; M5 the EQ stream packet ignored → fails.

## 7. The hardware run: `CAP-076` rewritten for 1.3.0 (Group BL, the registry row stays *planned*; `APP_TESTPLAN.md` §Y rewritten)

A registered capture in **both** GrapheneOS users, extended tier (`RELEASING.md` §11a), the screen recording on, observations said aloud:

- holds with the noise-control tab **closed** and **open** (≥ 10 each per user), the ears on film;
- a wear-change re-home (one bud out with both worn → the Buds' `DISC` → the re-open → the read; the mode before and after);
- Bluetooth off and on (the re-open subscribes again);
- a tap on the tab and a tap on the tile **with Play services present** (one claim each, released within `MESSAGE_STREAM_LINGER_MS`);
- the EQ or a switch changed in the official app on another phone, if available, else skipped and said so (the stream mirrors it);
- counts for the verdict: OpenControl `SABM`s on DLCI 0x04 against taps; holds shown against holds made; stream packets against Notifies (the latter only
  where another client's Notify is visible); the first read's value after every re-open.

The skeleton carries the expected bytes (the subscribe request per channel, the read, a stream packet) and the `tshark`/`pwrpc_decode.py` lines that count
them. The optional read-only `ReadSetting 4:8/9/10` of `TODO.md` §4 is **not** part of this run unless the maintainer adds it at the checkpoint.

## 8. Tasks

### Phase 0 — set-up

1. Create the RESULT (§0). Record `git log -1`, `git status --short`, `git fetch`, `git log --oneline HEAD..origin/main`. Branch from an up-to-date `main`
   (PR #55 must be merged first — if `main` lacks ADR-064, stop and say so): `git switch main && git pull --ff-only && git switch -c feature/settings-stream`
   (say the name used). `main` takes no direct push.
2. The gate once **before** any change (`cd android && ./gradlew assembleDebug testDebugUnitTest test lint --max-workers=2`; `python3 scripts/lint_docs.py`) and
   record the counts.

### Phase A — evidence and design check (no code change)

3. Re-derive from the captures the fixture bytes of §5 (`python3 scripts/pwrpc_decode.py`, `tshark -r <log> -Y 'frame.number==N' -T fields -e data.data`),
   and from `CAP-051` the `DISC` case (1993 → 2247). Read the code paths of §0; write the design into the RESULT: the Connect/re-open sequence, the routing,
   the state (what replaces `ancModeCause`'s inputs), the removal list, the log lines (always-on, payload-free), the tests, the constants.
4. The new ADR's draft text (supersedes 061/062/063; amends ADR-064 item 2), the `AGENTS.md` §1 pointer, the UI texts, the `CHANGELOG.md` entry, the `CAP-076`
   rewrite outline, and the questions of Phase B.

### Phase B — checkpoint (stop and ask, in this chat, via `AskUserQuestion`)

5. One question per decision, options with pros and cons, "(Recommended)" on one, the exact text in the preview: the new ADR's text; the `AGENTS.md` §1 pointer;
   the cause line and (i) texts; the `CHANGELOG.md`/README/release-notes texts; the `CAP-076` scope (and whether `ReadSetting 4:8/9/10` joins it); anything
   Phase A found that §3/§4 did not foresee. Record the answers verbatim. Build nothing that was not chosen.

### Phase C — the build

6. `:data` (the subscription, the read, the routing, the decoders fed from the stream, the cause, the removal), `:domain` (the cause enum, the removed types),
   `:ui` (the texts, the removed card and lines), `:app` (the wiring, the tile, the manifest; `PlayServicesProbe` removed); the tests of §6 first or alongside;
   `ARCHITECTURE.md` in the same commits.

### Phase D — gate, mutations, compliance

7. The full gate green; the mutation checks M1–M5 one at a time.
8. Compliance: no new permission (the `<queries>` element **removed**), no `INTERNET`, no dependency (`./gradlew :app:dependencies --configuration
   releaseRuntimeClasspath` unchanged), every new or changed Kotlin file with the AGPL header, no hidden API, no MAC or serial logged at INFO or above, the
   always-on log lines payload-free; the only new wire bytes are the two requests of ADR-064/065; nothing is written to the Buds that was not written before.

### Phase E — the hardware-run skeleton

9. `CAP-076` rewritten (EVENT-NOTES, the planned registry row's description, the Capture Index row, Group BL in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`),
   `APP_TESTPLAN.md` §Y rewritten for 1.3.0, the counts of §7 as the verdict rule.

### Phase F — documentation and release preparation

10. `DECISIONS.md` (the new ADR as approved; the Status lines of 061/062/063; a pointer Update on ADR-064 item 2; a pointer on ADR-032 "the hold exception is
    gone"), `id_registry.csv`, `AGENTS.md` §1 (as approved), `ARCHITECTURE.md` (§2.4, §3.1, §3.2, §5a, §6.0b — the hold and re-claim paragraphs become history
    with a pointer; §7, §8.1 unchanged), `PROTOCOL.md` only if a text is wrong (a dated note, approved), `TODO.md` (§1 `CAP-076`, §2, §4's items, §5's "Next"
    done; the E-01 experiment stays), `PROJECT.md`, `README.md`, `CHANGELOG.md` `[1.3.0]` (Known issues: the per-tap collision with Play services remains, about
    5 s; the 12-of-77 `DISC` case is covered by the re-open's read), `scripts/release_notes.template`, `android/app/build.gradle.kts` (1.3.0 / 10300).
11. `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py` (exit 0).

### Phase G — finish

12. The RESULT: a plain-language summary first (what the app does differently, what the user no longer sees, what the maintainer does next); the design; the
    tests and mutation results; the files read in full and in part; "Deferred documentation" (each in `TODO.md`) and "Commits".
13. **The maintainer's next steps as instructions, never executed:** push and open the pull request (with the Release checklist); both CI workflows green;
    B1 `scripts/release.sh 1.3.0` on the branch tip; B2; B3; C1–C2 the `CAP-076` run; then a CAPTURE session for its analysis and the verdict.
14. Show a short summary and **ask whether to commit and push**. Only after a yes: commits per concern (`feat`, `refactor`, `test`, `docs`), each with its *why*
    and the session's attribution line; `git fetch` and rebase; push without force; no file from a build directory, `dist/`, `.vscode/`, `__pycache__` or
    `android/domain/bin/`. Opening the pull request is a separate question; never merge it.

## 9. Guardrails (binding, in addition to `AGENTS.md`)

- **Scope.** Only the items above. **`WriteSetting 4:{13:n}` is out of scope** — no code path, no debug action, no test that sends it (`RELEASING.md` §11 item 4
  by analogy: a new write needs its `PROTOCOL.md` entry, its ADR and its own capture first; `TODO.md` §4 keeps the experiment). If it succeeds later, it is the
  next feature: switching over DLCI 0x02, DLCI 0x04 for Ring only. No background claim, no periodic request, no reconnect loop (ADR-032, ADR-044).
- **Approvals only in this chat** (`AGENTS.md` §6): no 🟢 FACT promotion, no ADR, no ADR Update, no project-law edit without the maintainer's answer in this
  chat — the decisions of §3 included (state them once, ask once). ADR-060 stays a draft. Accepted ADRs are not rewritten; the new ADR supersedes what it names.
- **Evidence.** Fixtures are real bytes with file and frame (`AGENTS.md` §11). Every claim in the RESULT and the docs cites a frame, a log line or `file:line`.
  A negative needs its command, its exit status and a positive control. "The Buds switch the mode on a wear change" is 🟡 in every text.
- **Reading.** `ARCHITECTURE.md` in full is not optional for this build (`TODO.md` §6, the reading owed by `ai-sessions/0090`).
- **Privacy.** No serial unredacted and no MAC in any committed text.
- **Release.** No `scripts/release.sh`, nothing in `dist/`, no tag, no GitHub release, no merge.
- **Files.** Before moving or deleting any file, diff a fresh listing; never wildcard deletes; ask before any delete (the removal of §3 item 2 is approved as a
  list at the checkpoint, then executed file by file).
- **Subagents** may only read and report; every write is done and verified in the main session.

## 10. Deliverables

- The app changes (the subscription and the read at Connect and re-open, the stream feeding every read setting, the rewritten cause, the removed hold /
  re-claim / card / Play line) with their tests, green gate and mutation results; version 1.3.0.
- `DECISIONS.md` (the new ADR as approved; the pointers), `id_registry.csv`, `AGENTS.md` §1 (as approved), `ARCHITECTURE.md`.
- `CAP-076` rewritten (EVENT-NOTES, registry and index rows, Group BL), `APP_TESTPLAN.md` §Y.
- `TODO.md`, `PROJECT.md`, `README.md`, `CHANGELOG.md`, `scripts/release_notes.template`.
- ai-sessions/0091_FEATURE_RESULT_2026_10_14.md and the `ai-sessions/INDEX.md` row.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0091_FEATURE_PROMPT_2026_10_14.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0091_FEATURE_PROMPT_2026_10_14
