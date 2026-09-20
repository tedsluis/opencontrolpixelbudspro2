# 0040_FEATURE_RESULT_2026_09_19.md — On-demand Message Stream claiming, Battery Option B decoder, HFP-battery and EQ-read research

**Number:** 0040
**Category:** FEATURE
**Date:** 2026-09-19
**Title:** Claim the shared Message Stream channel (DLCI 0x04) on the user's own ANC/Find tap, unblock the Battery Option B decoder by ADR, and research why HFP battery shows nothing and whether the EQ can be read
**Status:** awaiting maintainer sign-off — §10 proposals 1–4 answered by the maintainer in prompt `0041` §1 (2026-09-20, re-confirmed in chat; charging flag accepted, `pw_rpc` promotions + read-path ADR approved (ADR-034), HFP removal decided after one confirming run, the `btsnoop` capture will be supplied) and implemented/handled in `ai-sessions/0041`; proposal 5 (BLE Fast Pair battery advertisement) is still open

> **Why "awaiting maintainer sign-off":** everything requested is done and verified by the full suite, but
> (a) nothing is hardware-verified, (b) §3's charging-flag reading and §5's `pw_rpc` findings are **proposals**
> the maintainer must accept before they become FACT/ADR (`AGENTS.md` §6), and (c) §7 lists what only a real
> Buds can confirm. Evidence is cited by timestamp and log line, never by filename (standing rule).

---

## 1. Decisions recorded, and how they were authorised

| ADR | What | Authorisation |
|---|---|---|
| **ADR-032** | DLCI 0x04 is a shared, on-demand channel: claimed by the user's own ANC / Find / Connect action, released after a short linger; its loss is not a session loss. | The maintainer's explicit choice of "Lezing 1" in chat, 2026-09-19 (`0040` prompt §1.1), including the reason for retracting the earlier "never connect automatically" instruction (that instruction assumed the *Bluetooth connection to the Buds* was dropped; only one RFCOMM *channel* is lost). Details marked "agent detail" in the ADR (linger 1.5 s, snapshot at Connect) are the agent's proposals inside that decision, open to veto. |
| **ADR-033** | The Battery Option B decoder (`Group 0x03 Code 0x03`) is unblocked for implementation — **percentage regime only**. | The maintainer's explicit instruction "een ADR vrijgeven voor de batterijdecoder op 0x04". The charging-flag reading is a separate **unaccepted proposal** inside the ADR (§3.2). |

No FACT was promoted in `PROTOCOL.md`. Both ADRs are registered in `id_registry.csv`.

## 2. The on-demand claim (ADR-032)

### 2.1 Evidence that shaped the design (three real-hardware rounds; timestamps UTC)

**Play services' permission allowed (20 sessions, debug export 07:52–08:02 local):**
- **20 of 20** sessions ended with a loss of DLCI 0x04; **none** of DLCI 0x02. Hold time 0.07–4.68 s, **median 4.49 s**; 14 of 20 fell within 4.2–4.7 s.
- In **16 of 20** the first attempt on 0x04 failed (42–541 ms) because Play services held it; the retry (same tap) succeeded.
- The ~4.5 s is Play services' recovery timer. Three recoveries are visible in the system log, at **2.7 s, 4.0 s and 5.0 s** after it lost its
  socket (06:01:56.749→06:01:59.452, 06:02:01.597→06:02:05.609, 06:02:07.670→06:02:12.702); the last one in detail: it loses its socket at 06:02:07.670
  (`FastPairControllerEventStreamListener: onDisconnect, try to recover event stream` → `maybeRecoverEventStream:
  schedule task`), re-opens at 06:02:12.702 (`connectSocket … uuid=df21fe2c-… from uid/pid=10205/…`), fails with
  `RFCOMM_CreateConnectionWithSecurity: already at opened state … dlci=4`, and the stack's failure path closes **our**
  port (`cleanup_rfc_slot … app_uid: 10338 … slot_id: 19` at 06:02:12.773) → our `Ready → Disconnected` at 06:02:12.790.
  So the recovery delay is **not fixed** (2.7–5.0 s observed): a total hold under ~2.5 s stayed clear of all three, but that margin is thin. Each of *our*
  claims likewise closes Play services' socket.
- While we hold the channel, commands work: ACKs (`ff 01 00 06 08 12 …`) and Notify frames return within tens of ms.

**Permission denied (1 session, 08:05:13–08:06:42 local = 06:05:13–06:06:42 UTC, 89.3 s):**
- Both channels opened at the first attempt (no collision); Play services made **zero** RFCOMM connects in the
  retained log window and logged nothing about Nearby.
- It ended with `RFC_PORT_EVENT_DISC` on **DLCI 2 and DLCI 4 within 1 ms** (06:06:42.601–.603) — the **Buds** closing both
  RFCOMM channels — while the audio link stayed up (A2DP active device still the Buds at 06:07:15; playback started
  06:07:16). The stack lines for the moments *before* 06:06:42.601 were not retained, so the cause is unknown (§7).

### 2.2 Design as built
- **Session = the MAESTRO channel (DLCI 0x02).** `Connect` opens only it; `Ready` means "MAESTRO is open".
  Loss of 0x02 is a session loss exactly as before (`Disconnected`, with the reason).
- **DLCI 0x04 is claimed per user action** (`BudsRepositoryImpl.withMessageStream`, under one mutex): ANC mode tap,
  ANC Refresh, Find ring/stop, and — agent detail — one short snapshot started by the Connect tap (ANC `Get` + the
  battery burst the Buds push on open). The action waits for the Buds' reply (ACK/Notify, 1 s; Refresh 2 s), and the
  channel is released **1.5 s** later so Play services can take it back and hold it stably. A second action inside the
  linger reuses the open channel and restarts the timer. A claim that finds the channel busy is retried inside
  `transport.openChannel` (the bounded retry from `0039`); a channel that dies under a command is re-claimed once.
- **Loss of 0x04 is not a session loss** (`BudsTransport.channelClosed`, distinct from `connectionLost`); a failed claim
  is reported per action (`BudsRepository.messageStreamError`) on the ANC/Find screens with the channel and the
  exception text, while the Connection card stays `Connected`.
- **Transport:** `BudsTransport` gained `openChannel`/`closeChannel`/`isChannelOpen`/`channelClosed`;
  `RfcommBudsTransport` keeps on-demand sockets apart from session sockets (no zombie: a dead on-demand socket is closed
  and removed, so a re-claim cannot collide with our own leftover — the `0039` root cause, now covered for this path too).
- **Two corrections found on the way:** (i) `AncFrameDecoder` accepts *every* Message Stream ACK (Group `0xFF`), so a
  Find My Buds ACK arrives as `AncFrame.Ack`, not `RingFrame.Ack` — the repository now recognises it by the echoed
  Group/Code (found by a test written for this design); (ii) the old `setAncMode` optimistic update ran *after* the
  Buds' Notify could already have arrived and would have overwritten it (e.g. after a NAK) — it now applies only when
  no Notify came in time, and a reply subscription is made **before** sending so a fast reply is never missed.

## 3. Battery Option B decoder (ADR-033)

### 3.1 What is implemented
`BatteryFrameDecoder` (`:data`), routed by `CodecRouter` to `BatteryStatus.left`/`right`:
`03 03 00 03 <b1> <b2> <b3>` → Left = `b1`, Right = `b2` **when the byte is `0..100`** (charging state stays `null`,
never fabricated); **any other value is not interpreted** and reads "Battery unavailable" — and *replaces* a previous
value, so a stale percentage never lingers; `b3` is not interpreted (Case stays unavailable). Real logs show the Buds
push three such frames within ~10 ms of DLCI 0x04 opening and again on every change, so even a very short claim yields
a reading. **Consequence to expect:** while an earbud sits charging in the case its byte is >100 (`e4`, `dd` in the
logs), so it reads "unavailable" until §3.2 is decided.

### 3.2 PROPOSAL awaiting sign-off (not implemented): bit 7 is the charging flag
`PROTOCOL.md` §4.3 Option A documents the official Fast Pair battery byte as `0bSVVVVVVV` (S = charging, V = 0–100 %,
`0x7F` = unknown). `CAP-009-FINDINGS.md`'s "anomaly" fits it exactly: after the Left earbud entered the case, `b1` went
**221 → 222 → 223 → 224 → 225 → 226 → 228** (frames 26852…28563) while Option E (independent DLCI 0x08 message,
FACT per ADR-014) reported that earbud climbing **93 → 94 → 95 → 96 → 97 → 98 → 100**. `b1 − 128` reproduces that sequence
step for step, **including the skipped 99** (98 → 100). The third byte `0xff` would be S = 1, V = `0x7F` = "unknown".
The maintainer's own 2026-09-19 frames are consistent with it (`e4` = 100 %, `dd` = 93 % while charging; `64`, `60` = 100 %, 96 % not
charging; `e4 64` = one earbud in the case) — the on-screen values for those moments were not recorded, so this is consistency, not a check. If accepted, the decoder can show a charging state and a level in the
charging regime, and ADR-031's "field-switch anomaly" becomes a decoded flag. One reviewable ADR update.

## 4. Research: why the HFP battery shows nothing
Full write-up with evidence levels: `DESKRESEARCH_FINDINGS.md`, entry "2026-09-19 (second entry)". Summary:
- **FACT (direct):** every screenshot of a `Connected` app in three sessions shows all four battery rows "Battery unavailable".
- **The wire facts stay** (ADR-015/023): the Buds do send `AT+BIEV` over HFP. The problem is *delivery to an app*.
- **🟡 HYPOTHESIS (strong):** `HfpBatteryReader` listens for `ACTION_VENDOR_SPECIFIC_HEADSET_EVENT` with a bare filter (no
  company-ID category — and its own `TODO(verify)` predicted this), and standard `AT+BIEV` is not a vendor-specific command
  (AOSP maps only specific vendor AT commands to company IDs and handles the battery indicator internally; read through a
  fetch tool that returned a partial summary, so *not* a line-by-line reading). The OS's own battery record is reachable
  only via `@hide`/`@SystemApi` calls, which `AGENTS.md` §3 bans.
- **Conclusion:** Option C's implementation path most likely cannot work on Android 14+. Battery now comes from DLCI 0x04
  (§3); BLE advertisement (ADR-006) is the next contention-free source. Confirm with
  `adb shell dumpsys bluetooth_manager | grep -i -B3 -A3 battery`. Whether to remove/relabel HFP is a maintainer decision.

## 5. Research: can the current EQ be read? — **Yes, very probably**
Full write-up with commands and raw hex: `DESKRESEARCH_FINDINGS.md`, entry "2026-09-19". Headlines:
1. **DLCI 0x02 is pw_rpc, byte-exact.** The constant prefix of an EQ write (`CAP-015` frame 2165) parses as a Pigweed
   `RpcPacket` with `service_id = 0x7ede71ea = hash("maestro_pw.Maestro")` and `method_id = 0x9e8c9a1d = hash("WriteSetting")`
   (pw_rpc 65599 name hash; both 32-bit IDs match exactly). This is the wire confirmation `ADR-018`/`PROTOCOL.md` §2.2a said was missing.
2. **The official app reads settings on every connect.** `CAP-036` (a plain reconnect) has 64 `ReadSetting` packets: request
   `4:N`, response `4:{N: value}`, for ~30 setting numbers. **`4:16` (live EQ) and `4:18` (last saved EQ) return the 5-float
   quintet** in the same shape as the write body, e.g. `4:{16:{1:0.10 2:0.00 3:0.30 4:0.20 5:0.20}}`.
3. **The read value is the true current EQ:** `CAP-015`'s connect-time read (`[4.5, −4.9, 4.5, 3.8, 4.1]`, frames 1978/1985) equals
   the four bands that stay untouched during its later single-band drags; `SubscribeToSettingsChanges` (server stream) mirrors every
   change, and field 18 appears ~1 s *after* a drag ends while field 16 streams *during* it — strengthening (not promoting) the open
   "16 = live, 18 = persisted" reading (`ADR-020`).
4. **Open before any implementation:** what a fresh client must send first, which `channel_id`/HDLC address to use (19 vs 21),
   request/response matching, and confirmation against the on-screen EQ. **Needs (a) sign-off on the FACT proposals and (b) an ADR
   unblocking a *read* path on DLCI 0x02** (ADR-013 unblocked only the write wrapper) — not implemented this session.

Reproduction: `scripts/pwrpc_decode.py <capture>`. The method census used this scan (every `*btsnoop*.log`, service-id field +
method-id tag, HDLC-unescaped; counts are *packets*, requests and responses together):
```python
import glob,collections
def h(s):
    b=s.encode();x=len(b);c=65599
    for ch in b: x=(x+c*ch)&0xFFFFFFFF; c=(c*65599)&0xFFFFFFFF
    return x
names={h(n):n for n in ("WriteSetting","ReadSetting","SubscribeToSettingsChanges","GetSoftwareInfo")}
needle=bytes.fromhex("1dea71de7d5e25")  # service_id field (0x7e escaped as 7d 5e) + method_id tag
tally=collections.Counter()
for f in sorted(glob.glob('captures/*/*btsnoo*.log')):
    d=open(f,'rb').read(); i=0
    while (i:=d.find(needle,i))>=0:
        j=i+len(needle); m=bytearray()
        while len(m)<4:
            if d[j]==0x7d: m.append(d[j+1]^0x20); j+=2
            else: m.append(d[j]); j+=1
        tally[names.get(int.from_bytes(m,'little'),'other')]+=1; i+=1
```
Result: `ReadSetting` 7416, `SubscribeToSettingsChanges` 457, `WriteSetting` 344, `GetSoftwareInfo` 151, three unnamed method IDs (`0x28eca5e3`, `0xe61e8290`, `0x673bed4e`).

## 6. Verification
```
cd android && ./gradlew assembleDebug testDebugUnitTest test lint
BUILD SUCCESSFUL
```
**1281 tests, 0 failures** (`:data` 1244, up 19 from 1225; `:hardware` 37, up 9 from 28). Lint: 0 errors; `:app` 32 pre-existing warnings unchanged; `:ui` 0.
New tests (pure JVM, scripted fakes): transport on-demand channels (open, idempotent open, deliberate close reports nothing, an
on-demand channel dying is **not** a connection loss, re-claim right after being taken away, session loss closes on-demand channels
too, busy channel leaves the session alone, failed write closes only that channel); repository (claim → send → release after linger,
reuse inside the linger with the first timer cancelled, busy channel reports the reason and sends nothing, on-demand loss keeps the
session `Ready` and the next tap re-claims, tap while not `Ready` never touches the channel, the Buds' Notify wins over the optimistic
update, a Find tap ends at the ACK not after the full wait, disconnect clears the error, battery frames fill Left/Right, a
charging-regime frame replaces a known percentage with "unavailable"); battery decoder (CAP-009 frame 1044, real 2026-09-19 frames,
0/100/101/`0x7f`/`0x80`/`0xff` boundaries, the un-interpreted third byte, structural rejects, fuzz over 7 000 inputs). Two existing tests
that used the battery frame as their "unidentified" example were changed to a real SASS frame (`07 34`) — that gate is exactly what
ADR-033 lifted. **Not unit-tested (no Compose/instrumented infrastructure; unchanged from earlier sessions):** the UI wording, `OsConnectionObserver`,
and `connect()`'s snapshot launch (needs a real `BluetoothDevice`).

## 7. What is NOT verified, and known risks
1. **Nothing here is hardware-verified.**
2. **Does Find My Buds keep ringing after the Message Stream socket is released?** Unknown (🟡 HYPOTHESIS that it does). If ringing stops within ~1.5 s
   of the tap, the Find flow needs a longer hold (one constant).
3. **2 of 20 observed hold windows were < 1 s** (0.07 s, 0.64 s): an action can occasionally fail; it then reports the error (and is retried once).
4. **Each claim closes Play services' socket** for its duration (stack behaviour we cannot avoid); Android's own ANC controls may hiccup. Play services then
   recovers on a 2.7–5.0 s timer — a total hold of ~1.6–2.5 s (claim + reply + 1.5 s linger) is *usually* gone before that, so it can reconnect successfully and
   hold the channel, but the shortest observed recovery (2.7 s) leaves little margin; if a claim is occasionally cut short, the linger is the constant to shorten.
5. **The deny-mode drop at 06:06:42 is not explained** and is now a *session* loss (both RFCOMM channels closed by the Buds). Needs a `btsnoop`
   capture (see §9).
6. **The Connect-time snapshot is an agent detail** in ADR-032: without it battery and ANC would stay unknown until the first ANC/Find tap.

## 8. Documentation updated
`DECISIONS.md` (ADR-032, ADR-033), `id_registry.csv`, `ARCHITECTURE.md` (§2.1, §3.1, §5a, §6.0b), `TODO.md`, `CHANGELOG.md`,
`DESKRESEARCH_FINDINGS.md` (two entries), `scripts/pwrpc_decode.py`, `ai-sessions/INDEX.md`, this file and its prompt; `0039`'s status
closed per `AI_SESSION_LOG_PROCEDURE.md` §4a. `PROTOCOL.md` **unchanged** — nothing was promoted.

## 9. Re-test instructions for the maintainer
Turn **Debug mode** on and use **Export debug log** (and export the *system* log **within a minute** of any surprising drop — Bluetooth-stack
lines are kept only ~1–3 minutes).
1. **Permission allowed.** Connect → the Connection card stays `Connected` (no 5-second flicker: only 0x02 is held). Within ~2 s of Connect the ANC
   screen shows a mode and the battery card shows Left/Right. *Refuted if* it still drops back every ~5 s.
2. **ANC tap.** Tap a mode → it changes (audibly); the log shows `RFCOMM channel 0x04 connected` then, ~2.5 s later, `RFCOMM on-demand channel 0x04 released`.
   Two taps within ~1 s → **one** `connected` line. Android's own ANC control should keep working afterwards.
3. **Find.** Ring Left → the bud rings; **note whether it keeps ringing after ~2 s** (§7.2); Stop stops it.
4. **Busy channel.** If a tap ever fails, the ANC/Find screen must show *"Couldn't open the Message Stream channel…"* with the exception text, while the
   Connection card stays `Connected`.
5. **Battery.** Earbuds in your ears: percentages for Left/Right. An earbud in the case → "Battery unavailable" (expected until §3.2 is accepted). Please
   note the case-charging values you see on screen vs. Android's own battery display, to help §3.2.
6. **Permission denied.** Same steps; behaviour should be identical. If the session drops, export both logs at once and start a capture (§10.4).
7. **HFP (optional).** `adb shell dumpsys bluetooth_manager | grep -i -B3 -A3 battery` with the Buds connected; paste the Buds' lines.

## 10. Proposals awaiting maintainer sign-off (nothing below is done)
1. **Accept ADR-033's charging-flag proposal** (§3.2) — updates ADR-033/031 and `PROTOCOL.md` §4.3 Option B; enables charging state and levels in the case.
2. **Promote `pw_rpc` identification and the `ReadSetting` reading to FACT** (`DESKRESEARCH_FINDINGS.md` proposals a/b) and **write an ADR unblocking a
   read path on DLCI 0x02**, then a debug-only `ReadSetting 4:16` experiment — the way to make the EQ tab show the *real* current EQ.
3. **Decide the HFP row's fate** (keep, remove, or re-label Option C as "wire-confirmed, not app-consumable").
4. **A `btsnoop` capture** of the deny-mode drop, in the project's convention (`captures/CAP-NNN-…/`, next free number from `id_registry.csv`, EVENT-NOTES +
   FINDINGS, real MAC allowed by ADR-010, raw `btsnoop_hci.log` not the bugreport route): HCI snoop on *before* toggling Bluetooth; screen + case video with a
   visible clock; three runs with the permission off — idle, periodic commands, a second host using the Buds. Also check the existing official-app captures for
   whether the periodic `07 34` SASS message is ever answered (cheap, no new capture).
5. **Implement the BLE Fast Pair battery advertisement** (ADR-006's bounded exception) as the channel-independent battery source.

## Final summary
Lezing 1 is implemented as ADR-032: the app now holds only the MAESTRO channel as its session, and claims the Message Stream for the duration of a tap
(claim → act → wait for the Buds' reply → release after 1.5 s), which sidesteps Play services' ~5 s recovery timer instead of fighting it and keeps
the connection from flickering. The battery decoder is unblocked (ADR-033) and live for the percentage regime, with the charging-flag reading — well supported by
CAP-009 — waiting for your sign-off. The HFP research says the receiver most likely can never get `AT+BIEV` on modern Android. The EQ research found something
bigger than asked: DLCI 0x02 is verifiably pw_rpc, and the official app reads every setting — including the current EQ — with `ReadSetting` on each connect,
so an EQ read is very likely possible once you approve the FACT promotion and a read-path ADR. 1281 tests pass; nothing is hardware-verified.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0040_FEATURE_RESULT_2026_09_19.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0040_FEATURE_RESULT_2026_09_19
