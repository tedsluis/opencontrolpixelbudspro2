# 0038_MAINTENANCE_RESULT_2026_09_18.md — Audit and close remaining unimplemented/too-basic v1 gaps

**Number:** 0038
**Category:** MAINTENANCE
**Date:** 2026-09-18
**Title:** Survey the v1 app for parts that are unimplemented, incomplete, or too basic and fix what can be fixed without new maintainer decisions
**Status:** complete

---

## 1. Survey method

Read `TODO.md`'s open Phase 4/5 checklist, grepped the Kotlin source for `TODO`/`FIXME`/placeholder
markers, then cross-referenced `BudsRepository`'s full public method/`Flow` list against every call
site in `:app`/`:ui` to find methods that exist and are unit-tested but are never actually reachable
from the UI. Also read every Compose screen fully (not just for compile-correctness) looking for
callback wiring that does something different from what it looks like it does.

## 2. Findings and fixes

1. **`BudsForegroundService` never started/stopped.** A fully-built, compiling class
   (`ai-sessions/0033`) that nothing in `:app` ever called `startForegroundService`/`stopService` on.
   `ARCHITECTURE.md` §6.0a already specified exactly when/how in detail. Fixed: `MainActivity` now
   observes `connectionState`/`ancMode` in a `LaunchedEffect` and starts the service for any
   non-`Disconnected`/non-`Failed` state (notification text: `Connecting…` / `Discovering services…` /
   `Connected — ANC: <mode>` once `Ready`), stopping it back at `Disconnected`/`Failed`.
2. **No "Export debug log" affordance.** `BleLogger.exportLog()`'s ring buffer (`ai-sessions/0033`)
   was built and populated but had no UI path to actually read it. Fixed: a button on the Debug screen
   hands the snapshot to the system share sheet (`Intent.ACTION_SEND`, `text/plain`) — local-only per
   AGENTS.md §9, the destination is the user's own choice, this app never transmits it itself.
3. **No peer-disconnect detection — the most significant find.** `BudsTransport.connected` was a
   plain, unobserved `Boolean`. If the peer dropped the link for any reason other than the user
   tapping "Disconnect" (out of range, buds powered off, an OS-triggered socket teardown),
   `RfcommBudsTransport`'s reader coroutine caught the resulting `IOException` and flipped its own
   `connected` flag privately — nothing ever told `ConnectionStateMachine`, so the UI would have kept
   showing `Ready` for a connection that had actually already died, with every subsequent command
   silently failing against a dead socket. `ConnectionStateMachine.onDisconnected()`'s own doc comment
   already described this exact case ("an `IOException` from the socket ... always moves to
   Disconnected — a normal, expected transition") — it had simply never been wired end-to-end. Fixed:
   a new `BudsTransport.connectionLost: Flow<Unit>`, emitted from the reader coroutine only when its
   own `isActive` is still `true` at the moment of the `IOException` — this is what distinguishes a
   genuine peer/range-loss drop from the `IOException` this app's own `disconnect()` deliberately
   causes by closing the socket out from under an in-flight blocking `read()` call (which would
   otherwise have double-reported every ordinary user-initiated disconnect as a "connection lost"
   event). `BudsRepositoryImpl` now collects this flow and calls
   `connectionStateMachine.onDisconnected()`.
4. **Two implemented-but-unreachable repository methods, and one over-eager slider.**
   `BudsRepository.refreshAncMode()` (built and unit-tested since `ai-sessions/0033`) had no UI
   affordance anywhere — `AncScreen` gained a "Refresh" button. Separately, `EqScreen`'s sliders called
   `onGainsChanged` (a real DLCI 0x02 wire write via `BudsRepositoryImpl.setEqGains`) from `Slider`'s
   continuous `onValueChange` callback rather than the once-per-drag `onValueChangeFinished` — every
   pixel of drag movement would have sent its own RFCOMM frame. Fixed with a local,
   `remember(value)`-keyed slider value that tracks the drag smoothly and only calls `onGainsChanged`
   once the drag completes.

## 3. What was deliberately left alone

- **Battery Option B and the other DLCI 0x02 settings** (`TODO.md`'s existing PROPOSAL entry,
  `ARCHITECTURE.md` §5a): both remain correctly gated behind a `DECISIONS.md` ADR that does not yet
  exist. AGENTS.md §6 forbids an AI agent from independently promoting a finding to FACT or authoring
  that ADR — this stays exactly as gated, awaiting maintainer review of the existing PROPOSAL.
- **Secondary GATT client**: explicitly out of scope — no v1 feature needs it (`TODO.md`'s own note).
- **Visual/UX polish** (raw `::class.simpleName` connection-state text, plain `Text`/`Button` layouts
  across all five screens): read in full during this audit and judged as intentionally minimal per
  each screen's own doc comment, not a functional gap — the maintainer's question was about
  unimplemented/incomplete functionality, and these screens are functionally complete for v1's scope.

## 4. Verification

```
cd android && ./gradlew assembleDebug testDebugUnitTest test lint
BUILD SUCCESSFUL
```

1234 tests, 0 failures (1220 in `:data`, up by 1 — a new test drives `FakeBudsTransport.emitConnectionLost()`
and asserts `ConnectionStateMachine` reaches `Disconnected`; 14 in `:hardware`), 0 lint errors, 32
pre-existing unrelated `:app` warnings unchanged. The `BudsForegroundService`/export-log/ANC-refresh/
EQ-slider UI wiring has no new unit tests — consistent with this codebase's existing pattern of no
Compose UI/interaction tests anywhere yet, not a gap newly introduced by this session.

## 5. Documentation

- `TODO.md`: the two `ai-sessions/0033`-added items (foreground service, export log) checked off; a
  new "Known technical debt" entry for the peer-disconnect-detection gap and the
  refreshAncMode/EQ-slider findings.
- `CHANGELOG.md`: new `### Fixed` entry.
- `ai-sessions/INDEX.md`: `0038` row added.
- `ARCHITECTURE.md`/`DECISIONS.md` needed **no change** — every fix implemented an already-documented
  design (§6.0a's foreground-service lifecycle, §6's IOException-always-means-Disconnected rule) or
  closed a UI-wiring gap for an already-approved, already-implemented repository method; nothing here
  newly settles a protocol or architecture question.

## Final summary

**Four real gaps found and fixed**, none from a bug report — this was a self-directed audit. The most
consequential is the peer-disconnect detection: without it, a Buds-side disconnect (walking out of
range, powering the case shut) would have left the app's UI silently stuck showing `Ready` with every
further command failing invisibly, which is a worse failure mode than any of `ai-sessions/0034`-`0037`'s
bugs since it gives no feedback at all. The foreground service and export-log fixes complete features
that were already fully built at the data/hardware layer but never reachable. The ANC-refresh button
and EQ-slider fix close a smaller "built but unreachable" gap and a real protocol-abuse risk
respectively. Everything gated behind an outstanding `DECISIONS.md` ADR decision was left exactly as
gated, per AGENTS.md §6.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0038_MAINTENANCE_RESULT_2026_09_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0038_MAINTENANCE_RESULT_2026_09_18
