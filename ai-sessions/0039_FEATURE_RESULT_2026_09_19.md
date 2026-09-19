# 0039_FEATURE_RESULT_2026_09_19.md — Connect flicker, empty EQ tab, and app-vs-OS connection state

**Number:** 0039
**Category:** FEATURE
**Date:** 2026-09-19
**Title:** Investigate and fix flaky Connect (flickers to Disconnected within 1-2s), an always-empty EQ tab, and inconsistent app-vs-OS Bluetooth connection state after `ai-sessions/0038`'s peer-disconnect-detection fix
**Status:** awaiting maintainer sign-off

> **Why "awaiting maintainer sign-off" and not "complete":** every app-side root cause found is fixed,
> tested and verified by the full build/test/lint suite — but (a) nothing here is hardware-verified
> (no Buds in this environment), and (b) the **largest** cause found is *outside* this app (Google Play
> services' Fast Pair holding the same RFCOMM channel this app needs) and cannot be removed by this app
> alone, so the remaining design decisions in §8 are the maintainer's, per `AGENTS.md` §6. No
> `PROTOCOL.md` FACT was promoted and no `DECISIONS.md` ADR was written.

**Standing rule honoured:** the supplied logs/screenshots were read in full and used as evidence, but
this file (like every committed file) refers to them only by what they show — timestamps, exact status
text, exact log lines — never by filename. `android/logs/` remains fully gitignored and untouched.
Timestamps below are **local time (UTC+2)** when quoting the app's own debug export or a screenshot,
and **UTC** when quoting logcat/system-log lines (the two differ by 2 h; "round 1" = 2026-09-18
evening, "round 2" = 2026-09-19 morning).

---

## 1. Verdict in one paragraph

The flicker is **not** a benign glitch mis-read as a disconnect by `ai-sessions/0038`'s new
`connectionLost` path. The Android Bluetooth stack's own log shows the RFCOMM channel *really was
closed* — by the stack — because a **second client tried to open a channel that was already open**.
Android allows one RFCOMM connection per (device, channel) across all apps, refuses the second with
`RFCOMM_CreateConnectionWithSecurity: already at opened state`, and — the nasty part — its failure path
then **closes the incumbent's port too**. On the maintainer's phone the contenders were (1) Google Play
services' Fast Pair event stream, which owns the Message Stream channel (RFCOMM channel 2 / DLCI 0x04)
and re-opens it whenever it drops, and (2) **this app's own leaked sockets**: `RfcommBudsTransport`
never closed the *surviving* channel's socket after a loss, so the next Connect collided with the app's
own zombie. `ai-sessions/0038`'s detection merely made this visible — before it, the UI would have stayed
on `Ready` for a dead link. Fixed on the app side: full teardown on any loss, no leaked sockets, no
stale/duplicate events, bounded in-tap retry, the real reason on screen, EQ controls reachable, and an
"Android says connected" hint. **What cannot be fixed on the app side:** Play services re-grabbing the
Message Stream channel — see §8.

## 2. Evidence — reconstructed timeline

Method: the app's own `BleLogger` export (state transitions, always-on per `AGENTS.md` §9) as the
primary timeline; app logcat (`BluetoothSocket` lines, with thread ids) to attribute each transition to a
code path; the system log's Bluetooth-stack lines to explain *why* — with one hard limit, stated up
front: **the system logs only contain Bluetooth-stack (`bluetooth`-tag) lines for roughly the last 1–3
minutes before each export**, so the stack-level proof exists for round 1's last three connect attempts
and the drop that followed (16:33:49–16:36:02 UTC), and for round 2 only *after* its last connect
(04:20:33 UTC on). Everything else is attributed by the app-level signature, and is labelled accordingly.

### 2.1 Numbers (computed from the two debug exports; script output, not eyeballed)

| | Round 1 (18:32:52–18:34:02) | Round 2 (06:12:40–06:20:15) |
|---|---|---|
| `connect()` attempts | 12 | 16 |
| attempts that failed (`Connecting → Failed(ConnectionLost)`) | 8 (each after 0.15–0.48 s) | 8 (each after 0.08–0.40 s) |
| attempts that reached `Ready` | 4 | 8 |
| `Ready → Disconnected` after | 3.1, 3.3, 2.9, 0.84 s | 5.6, 8.8, **253 s**, 2.3, 3.1, 2.2, 2.7, 1.6 s |
| `Failed → Disconnected` within 200 ms of the failure | 3 (42/48/44 ms) | 4 (50/17/46/7 ms) |
| duplicate `Disconnected → Disconnected` | 0 | 4 (9 ms, 8 ms, 1 ms, and one **4.9 s later**) |
| first attempt after a drop failed | 3 of 3 | 5 of 8 |

Those match the maintainer's report exactly: "Connecting… then Disconnected within ~1 s" (the
0.08–0.48 s failures), "Retry sometimes fails again immediately", "Connected, then Disconnected within ~2 s"
(the 0.84–3.3 s sessions), and the one long successful session (253 s, 06:13:07–06:17:20).

### 2.2 Which code path caused each transition (thread attribution, app logcat)

`connect()`/`disconnect()` run on the main thread (`rememberCoroutineScope`); `BudsRepositoryImpl`'s
collectors run on `Dispatchers.Default` workers. In the app logcat every `Connecting → Failed(...)` is on
the main thread (thread 5416 in round 1, 14378 in round 2) = **`BudsRepositoryImpl.connect()`'s
`BudsResult.Failure` branch**. Every `Ready → Disconnected` and every `Failed → Disconnected` is on a
non-main worker (5477/5481 in round 1; 14402/14406/14407 in round 2) = **the
`transport.connectionLost` collector added in `ai-sessions/0038`** — never `disconnect()`'s explicit call
(no session in either round shows a main-thread `→ Disconnected`).

### 2.3 Socket-level signature of one failing attempt (app logcat, UTC)

```
04:20:04.291 [main] Disconnected -> Connecting
04:20:04.296        socket connect() started                 (channel 1, MAESTRO)
04:20:04.429        socket CONNECTED 1                       (133 ms)
04:20:04.441        socket connect() started                 (channel 2, Message Stream)
04:20:04.500        close() channel=1  fd:open               <- our own cleanup of the one that DID connect
04:20:04.503 [main] Connecting -> Failed(ConnectionLost)     <- channel 2 failed after 59 ms
04:20:07.841 [main] Failed -> Connecting                     <- user taps Retry
04:20:07.926        socket CONNECTED 1
04:20:07.987        socket CONNECTED 2
04:20:07.994 [main] Discovering -> Ready
04:20:09.629 [worker] Ready -> Disconnected                  <- 1.6 s later
04:20:15.038        socket connect() started (channel 1)
04:20:15.113 [main] Connecting -> Failed(ConnectionLost)     <- channel 1 failed after 75 ms
04:20:15.120 [worker] Failed -> Disconnected                 <- 7 ms later: NOT the user
```

Two things stand out: the failing socket is *never* closed by the app (no `close()` line for it — the
finalizer daemon later logs `A resource failed to call close` / `Uncaught exception thrown by finalizer:
IOException: socket not created` for channel 1 and channel 2), and in round 1 the JVM finalizer
closes **five still-open leaked sockets within 6 ms** (`close() channel=1/2 … fd:open` ×5 at
16:33:36.640–.646 UTC, each preceded by CloseGuard's `A resource failed to call close.`) — sockets from
earlier sessions that this app had never closed itself.

### 2.4 The stack-level proof (system log, round 1, UTC; uid 10338 = this app, 10205 = `com.google.android.gms`, 10302 = `com.google.android.googlequicksearchbox`)

**Attempt A — Connect tapped 16:33:55.821 UTC (18:33:55.821 in the debug export) → fails on channel 1 (MAESTRO, DLCI 2):**
```
16:33:56.004 E bt_port_api: RFCOMM_CreateConnectionWithSecurity: already at opened state 2, RFC_state=4,
             MCB_state=5, ..., scn=1, is_server=false, mtu=990, uuid=0x1101, dlci=2, ..., port=23
16:33:56.004 E bluetooth: bta_jv_rfcomm_connect: RFCOMM_CreateConnection failed
16:33:56.004 W bt_btif_sock_rfcomm: on_cl_rfc_init: INIT unsuccessful ... Cleaning up slot_id 42
16:33:56.004 I bluetooth: rfc_port_sm_opened: RFC_PORT_EVENT_CLOSE ... port_handle:23 dlci:2 scn:1
16:33:56.049 I bt_btif_sock_rfcomm: cleanup_rfc_slot: ... scn: 1, app_uid: 10338, slot_id: 38, socket_id: 3897229818950191090
16:33:56.051 [app] Failed -> Disconnected                    <- our own leaked slot 38's reader reporting the loss
```
The incumbent on DLCI 2 was **slot 38 of our own uid** — a socket from an earlier session that the app
never closed. The failed attempt then closed it (`RFC_PORT_EVENT_CLOSE` on the *existing* port_handle),
its reader saw an `IOException`, `connectionLost` fired, and 2 ms later the state machine dropped a fresh
`Failed` back to `Disconnected` — the "Failed → Disconnected 7–50 ms later" signature of §2.1.

**Attempt B — 16:33:57.077 UTC → channel 1 connects, channel 2 (Message Stream, DLCI 4) collides with Play services:**
```
16:33:57.513 E bt_port_api: RFCOMM_CreateConnectionWithSecurity: already at opened state 2, ..., scn=2, dlci=4, port=25
16:33:57.531 I bt_btif_sock_rfcomm: cleanup_rfc_slot: ... scn: 2, app_uid: 10205, slot_id: 41       <- Play services' socket, closed by OUR failed attempt
16:33:57.538 I NearbyDiscovery: FastPair: EventStream disconnected
16:33:57.538 I NearbyDiscovery: FastPairControllerEventStreamListener: onDisconnect, try to recover event stream.
```
Here the roles reverse: the incumbent on DLCI 4 was **Google Play services** (uid 10205), and *our*
failed attempt killed *its* socket. Play services logs that it will "try to recover event stream".

**Attempt C — 16:34:01.558 UTC → both channels connect (`Ready` at 16:34:01.946), then Play services' recovery kills us:**
```
16:34:01.938 I bt_btif_sock_rfcomm: on_cli_rfc_connect: connected ... scn: 2, app_uid: 10338, id: 46
16:34:02.582 I NearbyDiscovery: FastPairController: connectEventStreamForRfcomm: connect RFCOMM ...
16:34:02.590 I BluetoothSocketManagerBinder: connectSocket: ... uuid=df21fe2c-2515-4fdb-8886-f12c4d67927c, ... from uid/pid=10205/25981
16:34:02.740 E bt_port_api: RFCOMM_CreateConnectionWithSecurity: already at opened state 2, ..., scn=2, dlci=4, port=28
16:34:02.741 I bt_btif_sock_rfcomm: cleanup_rfc_slot: ... scn: 2, app_uid: 10205, slot_id: 47
16:34:02.746 W NearbyDiscovery: RfcommEventStreamMedium: ... Failed to read from socket. Retry times remaining 5
16:34:02.782 I bt_btif_sock_rfcomm: cleanup_rfc_slot: ... scn: 2, app_uid: 10338, slot_id: 46          <- OUR socket, closed by Play services' failed attempt
16:34:02.790 [app] Ready -> Disconnected                     <- 8 ms later
```
`uuid=df21fe2c-…` is exactly the Message Stream UUID this app also connects to
(`BudsSdpUuids.FAST_PAIR_MESSAGE_STREAM`). Play services decided to (re)connect its event stream 0.64 s
after we reached `Ready`, collided with us, lost the collision, and — through the stack's failure path —
closed our port. **That is the 0.84 s "Connected → Disconnected" flicker, in full.**

### 2.5 What the evidence rules out

- **"A benign per-channel glitch mis-read as a link loss" (this task's primary suspect): refuted.** The
  channel was genuinely closed by the stack (`rfc_port_sm_opened: RFC_PORT_EVENT_CLOSE` on our port,
  16:34:02.782). Relaxing what counts as a loss (e.g. "only the first read") would have *hidden* a real
  closure and left the UI on `Ready` with a dead channel. The OS-level link stayed up throughout
  (`BTAudioSessionAidl`/A2DP lines continue every ~200 ms across all three attempts) — which is why
  Android's settings showed "Connected" while the app flickered.
- **"The MAESTRO socket never opened" (EQ, §4): refuted** — see §4.
- **The Play Services "needs to show a screen" popup as *the* cause: refuted; as a symptom of the same
  activity: yes** — see §6.

### 2.6 What the evidence does *not* show (HYPOTHESES, with the experiment that would settle each)

1. **🟡 HYPOTHESIS — the same mechanism explains every flicker in both rounds**, including those outside
   the stack-log window (all of round 2, round 1's first nine attempts). Consistent with the app-level
   signature (both sockets connect, drop 0.84–8.8 s later, duplicates/stale events, finalizer-closed leaked
   sockets) but not directly proven for those attempts. *Experiment:* re-test with the new debug log
   (§9) — the new `RFCOMM channel 0x… lost (…)` / `connect failed after N ms` lines name the channel and
   the exception text for every drop — and export the system log *immediately* after a flicker (within a
   minute) so the stack lines are still in it.
2. **🟡 HYPOTHESIS — why round 2's 253-second session survived.** No stack lines cover it. Plausibly
   Play services' event-stream retries were exhausted or idle at that moment (it logs "Retry times
   remaining 5"); unverified. *Experiment:* same as above, or repeat the session with Play services'
   Fast Pair switched off (§9, test 5).
3. **🟡 HYPOTHESIS — the asymmetric case (§3 of the prompt: OS showed "not connected", an app-driven
   Connect made the OS show "connected" although the app reported failure).** Mechanism, unverified: a
   `BluetoothSocket.connect()` to a bonded-but-not-connected device makes the stack bring up the ACL link;
   the Buds then (re)connect their audio profiles on their own, independent of whether this app's RFCOMM
   attempt succeeds. No log window covers such a moment. *Experiment:* §9, test 4.

## 3. Root causes found and fixed (app side)

Every item is a defect in this app's own code, evidenced above, fixed, and regression-tested (§7).

| # | Root cause | Evidence | Fix |
|---|---|---|---|
| 1 | **Zombie sockets after a loss.** `connectionLost` only reset the state machine; the *surviving* channel's socket stayed open. The next Connect collided with our own leftover (`already at opened state`, slot 38, uid 10338) and the failed attempt then killed it, producing a second, stale loss. | §2.4 attempt A; five finalizer-closed `fd:open` sockets (§2.3) | On any loss the transport closes **every** socket of that connection *before* emitting (`reportLoss`). |
| 2 | **Failed `connect()` leaked the socket that failed** (only the ones that had succeeded were closed). | Finalizer `A resource failed to call close` for channels 1 and 2 (§2.3) | `openChannel` closes the failing socket in every failure path. |
| 3 | **Stale / duplicate loss events.** Two readers each reported the same loss; a loss from a previous session could land on the next attempt. | 4 duplicate `Disconnected → Disconnected`, 7 `Failed → Disconnected` < 200 ms (§2.1) | Loss is reported **at most once per connection**, only for the *current* connection; `ConnectionStateMachine.onDisconnected()` is idempotent; `BudsRepositoryImpl` acts only on a loss while `Discovering`/`Ready`. |
| 4 | **EOF and write failures were not reported.** `read() < 0` was treated as "normal, not an error" and a failed `send()` silently flipped `connected` — the UI stayed `Ready` on a dead channel (the same class of bug `0038` fixed for `IOException` reads). | Code read (`RfcommBudsTransport` before this session) | Both now go through the same `reportLoss` path; a failed `send()` returns `BudsError.ChannelLost`. |
| 5 | **The reason was thrown away.** `catch (e: IOException) { … BudsError.ConnectionLost }` discarded the exception; every failure read as "Connection lost." | The app log contains no connect-failure reason at all | New `BudsError.ChannelUnavailable(channelId, detail)` / `ChannelLost(channelId, detail)`; exception class + message (Bluetooth addresses redacted) go to the always-on log and the screen. |
| 6 | **No retry inside one tap for a fast collision**, so the user had to tap Retry repeatedly. | 8 of 12 / 8 of 16 attempts failed in < 0.5 s; the *next* tap usually succeeded (the failed attempt itself frees the port) | Up to 3 attempts per channel within one `connect()`, 400 ms apart, only for *fast* (< 2 s) failures. Slow failures (unreachable peer) are never retried. Not a background loop (`ARCHITECTURE.md` §6) — it ends with the tap that started it. |
| 7 | **Double-tap could run two `connect()`s** and `connect()` while `Ready` would tear the live link down. | Code read | `BudsRepositoryImpl.connect()` is mutex-guarded and a no-op success when already `Ready`. |
| 8 | **Command screens looked usable while disconnected** (screenshot: "Connection: Disconnected" above four enabled ANC buttons). | Screenshot at 18:36 local | ANC/EQ/Find controls are disabled and show a "Not connected…" banner unless `Ready`. |

**Not a defect, but the reason the above matter:** `ai-sessions/0038`'s detection is *correct in
principle* (a channel really was lost). Its flaw was only that detection was wired without teardown,
de-duplication or a reason.

**Per-channel failure tolerance — evaluated, deliberately not adopted here.** The evidence actually
strengthens the case for it (the contested channel is only DLCI 0x04; MAESTRO/DLCI 0x02 was never
contested by another app), but `ConnectionState` has no "degraded" state and a `Ready` that silently
lacks a channel is the very lie the all-or-nothing rule prevents. Adopting it is a design change → §8.

## 4. Why the EQ tab showed nothing (task 4)

The screenshot shows the `gains == null` branch: *"EQ state unknown — change a setting or wait for the
Buds to report one."* — not a blank screen. **Not** a MAESTRO-socket failure: in the successful session
the debug export shows the MAESTRO channel open and delivering its connect-time frame (`DLCI 0x02: 7e …`,
123 bytes, 06:13:07.643) and then **no further DLCI 0x02 traffic for the remaining 4 minutes** — the Buds
never volunteered an EQ frame, exactly as `ARCHITECTURE.md` §3.1's reconciliation table predicted ("no
confirmed connect-time push exists yet for EQ").

The real defect was a **dead end**: with `gains == null` the screen replaced *all* controls with a message
asking the user to "change a setting", which was impossible because the controls were hidden. Since the Buds
never report EQ on connect, the tab could never leave that state. **Fixed:** the sliders and presets are
always shown (disabled when not connected); while the value is unknown a banner says so plainly and
states that the sliders start from flat (0.0) and **do not** show the Buds' current setting. A preset is a
complete quintet and a slider move sends all five bands, so neither needs a known starting value — no
new protocol claim, no invented "Get EQ" query (`ARCHITECTURE.md` §3.1's constraint is kept).

## 5. Why the app could not see an already-connected device (task 5)

- **Bonded vs connected — not conflated in code.** `BudsCompanionPairing.bondedDevice()` correctly means
  "bonded" (`adapter.bondedDevices` filtered by name); nothing treats it as "connected". The mismatch was a
  *missing signal*: the app only knew about its **own** RFCOMM sockets, so it said "Disconnected" while
  Android's settings said "Connected" (audio/HFP profiles up). Fixed with an informational
  `OsConnectionObserver` (public APIs only: A2DP/HEADSET profile proxies + their connection-state
  broadcasts — `BluetoothDevice.isConnected()` is `@hide` and banned) and a hint under "Disconnected" /
  the failure message. It never triggers a connect (`ARCHITECTURE.md` §6). *Not hardware-verified.*
- **Reusing a live link — already the case.** `createRfcommSocketToServiceRecord()` + `connect()` rides the
  existing ACL: the round-1 log shows L2CAP `CONNECT_REQ` for SDP and RFCOMM straight onto the live link
  ("Requested role is already in effect", no HCI create-connection). So an already-connected Buds never
  needed a special path; what failed was the RFCOMM *channel* being taken (§2.4), not the link.
- **Failure-but-OS-connected (asymmetric case):** 🟡 HYPOTHESIS only, §2.6-3.

## 6. The Google Play services popup (task 6)

- **What it is (FACT, from the system log):** the notification is posted by `app.grapheneos.gmscompat`
  (GrapheneOS's Play-services compatibility layer) on its `bg_activity_start` channel — the "Google Play
  Services needs to show a screen" prompt for a background activity start (here: Fast Pair's "Pixel Buds
  Pro 2 appears on devices signed in to…" surface). It is **not** invoked by, requested by or dependent on
  this app; `AGENTS.md` §1 is untouched and no Play-services-related code was written.
- **Timing (FACT):** the pairing tests of the 2026-09-18 afternoon (15:03:55 and 15:05:03 UTC, matching the
  17:03/17:06 local screenshots that show it next to this app's own CDM confirmation dialog) predate any
  `connect()` code (`ai-sessions/0037`), so they cannot have caused connect instability. In round 2 a
  `bg_activity_start` notification was posted at 04:12:36.5 UTC — 3.7 s before the first Connect of the
  morning and ~10 s before its first drop. In round 1 the visible notifications were stale (posted
  ~15:0x UTC, hours earlier).
- **Conclusion:** the popup is a *symptom of Play services actively engaging the Buds at the same moments*,
  not a cause. The actual contributing factor is Play services' **RFCOMM event stream** (§2.4), which
  collided with this app on the Message Stream channel — "two independent clients racing on the same
  device", exactly the scenario the prompt anticipated. The fix that lives entirely on this app's side is
  §3 (own hygiene, bounded retry, honest reporting). **Nothing touches, suppresses or works around Play
  services or Fast Pair UI.** The Google app (uid 10302) was also seen holding RFCOMM channels 4/5
  (DLCI 8/10), released at 16:33:55.28 UTC — irrelevant to this app's channels, noted for completeness.

## 7. Verification

```
cd android && ./gradlew assembleDebug testDebugUnitTest test lint
BUILD SUCCESSFUL
```
- **1253 tests, 0 failures** (`:data` 1225, up 5 from 1220; `:hardware` 28, up 14 from 14). Lint: 0 errors;
  `:app` 32 pre-existing unrelated warnings unchanged; `:ui` 0. No lint suppressions added.
- **New regression tests** (all pure JVM, no real Bluetooth — `RfcommSocket` was extracted from
  `BluetoothSocket` precisely so the transport logic can be driven by scripted fakes; the fake models the one
  stack behaviour §2.4 proves, "a second connection to an open channel fails"):
  `RfcommBudsTransportTest` (11) — retry then success; exhausted retry → `ChannelUnavailable` naming the
  channel with the address redacted and *every* socket closed; slow failure not retried; one channel dying
  closes all channels and reports **exactly one** loss; EOF is a loss; explicit `disconnect()` never reports
  a loss; late error from a replaced connection is ignored; **reconnect after a loss succeeds because no
  zombie collides (the round-1 failure)**; failed write → loss + `ChannelLost`; send with no connection.
  `BudsRepositoryImplTest` (+5) — loss records channel and reason; loss while `Connecting`/`Failed` is
  ignored; explicit disconnect clears the reason; connect-while-`Ready` is a no-op. `ConnectionStateMachineTest`
  (+3) — idempotent `onDisconnected`, `Failed → Disconnected` still works, address redaction in
  `BleLogger.describe`.
- **Mutation-checked** (two mutants, each reverted): removing the teardown-on-loss makes 2 transport tests
  fail; removing the state guard in the repository's loss handler makes 2 repository tests fail.
  One honest limit: the `current === connection` stale-report guard in `reportLoss` is defence-in-depth for
  a narrow race (a reader already past its `isActive` check) and cannot be forced from a black-box test, so
  that one test asserts the observable outcome, not which of the two guards fired.
- **Not unit-tested, by the codebase's existing pattern (no Compose/instrumented tests yet):** the Compose
  changes (banner, disabled controls, EQ layout, hint text), `OsConnectionObserver` (needs a real profile
  proxy) and `BudsRepositoryImpl.connect()`'s success/failure mapping (needs a real `BluetoothDevice`, which
  cannot be constructed in a JVM test; the mapping is three lines and each half is covered by the transport
  and state-machine tests).

## 8. Flows audit (task 7) and what needs a maintainer decision

| Flow | Before | Now |
|---|---|---|
| (a) first connect after pairing | present; hit the collision | present; fixed by §3 |
| (b) retry after an explicit failure | present ("Retry") but re-collided with the app's own zombie | one tap now includes the bounded retry; failure shows *which channel* and the exception |
| (c) reconnect after a peer-initiated disconnect | detection present (`0038`) but no teardown, duplicates, no reason | detection + teardown + reason on screen; **reconnect itself is still user-initiated only** (`ARCHITECTURE.md` §6) |
| (d) launch/resume, bonded, not connected | present (bond re-check on resume; "Disconnected" + Connect) | unchanged, plus the "doesn't appear connected to this phone" hint |
| (e) launch/resume, bonded **and** OS-connected | **missing** (UI contradicted Android) | informational hint; **no auto-connect** |

**PROPOSALS — awaiting maintainer decision (nothing below is implemented or recorded as an ADR):**

1. **Per-channel tolerance / a "degraded" connection state.** Evidence in §2/§3: Play services contends only
   for the Message Stream (DLCI 0x04); MAESTRO (DLCI 0x02, EQ) was never contested. A `Ready` that says
   "EQ available, ANC/Find My Buds unavailable — channel in use by another app" would keep half the app usable
   during a contention. Needs a `ConnectionState`/`ARCHITECTURE.md` §2.1 change → its own ADR.
2. **Auto-reconnect after a loss, and/or auto-connect on launch when Android reports the Buds connected.**
   Contradicts `ARCHITECTURE.md` §6 "user-initiated reconnection only", and — importantly — **would fight
   Play services**: every reconnect kills its socket and every recovery of its socket kills ours (§2.4,
   attempts B and C). Do not adopt without deciding how to avoid that ping-pong.
3. **Lazily opening the Message Stream channel** (only when ANC / Find My Buds is used) to shrink the
   contention window. Needs a decision on connection-model semantics.
4. **A one-line note in `PROTOCOL.md`?** Deliberately *not* done: the stack behaviour in §2.4 is
   Android-stack/Play-services behaviour, not Pixel Buds protocol knowledge, so it does not belong in
   `PROTOCOL.md`; it is recorded here and in `ARCHITECTURE.md` §6.0b.

**Remaining limitation, stated plainly:** these fixes remove every app-side cause found, but they cannot
stop Play services from re-opening the Message Stream channel. While it keeps doing that, the flicker
can still occur on this phone — now with an explanation on screen, no zombie sockets, and one-tap
recovery — until proposal 1/3 or Play services' Fast Pair activity changes. §9 test 5 separates the two.

## 9. Re-test instructions for the maintainer

Install the new debug APK. Turn **Debug mode** on (Debug tab) before starting so the log has full detail,
and use **Export debug log** after each scenario. Each test confirms or refutes one fix on its own.

1. **Zombie sockets / retry (fixes 1, 2, 6).** Tap Connect, wait for `Connected`, tap Disconnect, tap
   Connect again — repeat 5×. *Pass:* no attempt ends `Failed` twice in a row; the log shows no
   `already at opened`-style repeat failures and no `Failed → Disconnected` within 200 ms of a failure.
   *Refuted if:* attempts still fail back-to-back with our own channel as the collision (then the log's new
   `connect failed after N ms (attempt k/3): <exception>` lines say which channel and why).
2. **Reason on screen (fix 5).** Provoke a drop (walk out of range / put a bud in the case). *Pass:* under
   "Disconnected" you see *"The … channel was closed. Another app may have taken it over…"* plus a small
   technical line; a failed attempt shows *"Couldn't open the … channel…"* with the exception text. *Refuted
   if:* a bare "Disconnected" or "Connection lost." appears with no explanation.
3. **No stale/duplicate events (fix 3).** In the exported log, after any failure there must be **no**
   `Failed -> Disconnected` and **no** `Disconnected -> Disconnected` line.
4. **OS-connected hint (task 5).** (a) Buds connected in Android settings, app freshly opened → the card
   should say *"Android shows your Buds as connected to this phone. This app isn't controlling them yet —
   tap Connect."* (b) Disconnect the Buds in Android settings → *"…don't appear to be connected…"*.
   (c) With (b), tap Connect and note whether Android then shows "Connected" while the app reports
   failure — this settles the §2.6-3 HYPOTHESIS; please record it either way.
5. **Separate this app from Play services (the decisive experiment).** Repeat test 1's flicker scenario
   once with Play services' **Fast Pair** setting switched off (Settings → Google → Devices & sharing;
   exact menu names vary by Play-services version) and once with it on. If the flicker disappears with it
   off, §2.4/§8 is confirmed for the whole of both rounds, not just round 1's window. *This is a
   maintainer-side experiment only — no app code changes or depends on it (`AGENTS.md` §1).*
6. **EQ tab.** Connect, open EQ. *Pass:* banner "The Buds haven't reported their current EQ…", five sliders
   and the presets visible and enabled; tapping a preset changes the sound and, if the Buds echo it, the
   banner disappears. Disconnected → controls greyed, "Not connected" banner.
7. **ANC / Find My Buds / general.** Connected: ANC modes, Refresh, Ring Left/Right, Stop all work as in the
   previous session's successful run. Disconnected: those controls are disabled with the banner.
8. **Long session.** Stay connected 10+ minutes with the phone idle-locked; report whether it drops, and
   export the log within a minute if it does.

## 10. Documentation updated

- `TODO.md` — Known technical debt entry for this session; open items for the §8 proposals and the small
  deferred items below.
- `CHANGELOG.md` — `### Fixed` entry in the `0034`–`0038` style.
- `ARCHITECTURE.md` — §7 error hierarchy (two new cases), new §6.0b (connection-lifecycle rules and the
  Android RFCOMM one-connection-per-channel behaviour), §2.1 `BudsTransport` row. No decision changed; the
  bounded in-tap retry and teardown are implementation detail inside §6's "user-initiated only" rule.
- `ai-sessions/INDEX.md` — the `0039` row.
- `DECISIONS.md` / `PROTOCOL.md` — **unchanged** (no FACT promoted, no ADR written, per `AGENTS.md` §6).

**Small items found and deliberately left (recorded in `TODO.md`):** `connect()` with no bonded device
returns `PermissionDenied` ("Bluetooth permission is required" — misleading for "no device paired");
`bondedDevice()` matches by the substring "Pixel Buds" in the device *name*, so a user-renamed device would
not be found; every failed attempt starts and stops the foreground service within ~160 ms (visible as a
notification flash in the system log) — `ARCHITECTURE.md` §6.0a's own rule, harmless but noisy.

## Final summary

The flicker was real channel closures caused by **two clients fighting over one RFCOMM channel**: Google
Play services' Fast Pair event stream, and — the part that was this app's own fault — the app's own
leaked sockets. Eight app-side defects fixed and regression-tested (1253 tests, 0 failures, lint clean,
two mutants caught); the EQ tab was a UX dead end, not a socket failure; the "Disconnected vs Android says
Connected" mismatch was a missing signal, now surfaced. The Play-services contention itself cannot be
removed from this side; §8 lists the design decisions that would, and §9 test 5 is the experiment that
proves it. Nothing is hardware-verified.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0039_FEATURE_RESULT_2026_09_19.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0039_FEATURE_RESULT_2026_09_19
