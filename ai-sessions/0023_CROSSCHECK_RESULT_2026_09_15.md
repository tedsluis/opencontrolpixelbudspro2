# 0023_CROSSCHECK_PROMPT_2026_09_15.md — Exhaustive APK cross-validation of CAP-047/CAP-050/CAP-051

**Number:** 0023
**Category:** CROSSCHECK
**Date:** 2026-09-15
**Title:** Deep, non-sampled cross-validation of `CAP-047` (Group AL), `CAP-050` (Group AG repeat), and `CAP-051` (Group AM) against the decompiled companion APK
**Status:** partial — resumed

> **Maintainer sign-off obtained 2026-09-16** (same chat session, continued from this prompt): the
> five headline proposals in this file's own "Summary for the maintainer" section below were
> reviewed and explicitly accepted for recording at 🟡 HYPOTHESIS level (not promoted to FACT, no
> `DECISIONS.md` ADR involved, per `AGENTS.md` §6/§15). This is reflected in place in
> `PROTOCOL.md` §6/§8, `REVERSE_ENGINEERING.md`'s `qhr`/GSND/`frb`-`gjv` entries, `CAP-051-FINDINGS.md`
> §3a, and `DESKRESEARCH_FINDINGS.md`'s 2026-09-15 entry — each carries its own "Maintainer sign-off
> obtained 2026-09-16" note. This file's own `Status` remains `partial — resumed`, not `complete`:
> the sign-off covers what this pass *found*, not the phase items this pass left genuinely
> untried (see "What genuinely remains" below, unaffected by this sign-off).

## Phase status table

| Phase | Status | Summary |
|---|---|---|
| 0 | done | Mandatory reading order completed (`AGENTS.md`, `PROJECT.md`, `PROJECT_RULES.md`, `DECISIONS.md` full through ADR-029, `ARCHITECTURE.md` full, `PROTOCOL.md` §2/§4.1/§6/changelog, `TODO.md` full, `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`, `CAP-047`/`CAP-050`/`CAP-051` FINDINGS+EVENT-NOTES in full, `REVERSE_ENGINEERING.md`'s `qhr` entry in full plus a full section-header map). APK version confirmed on disk: `reverse-engineering/apk/v1.0.955078536-10253511/` — matches `CAP-047`'s own stated companion-app version; no drift indication found anywhere in `PROTOCOL.md`'s changelog for `CAP-050`/`CAP-051`'s timeframe. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` and `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 were **not** read in full this session (out of the mandatory list nominally, in practice referenced only via the three captures' own FINDINGS files) — flagged honestly, not silently skipped from this record. |
| 1 | done | `qhr` field 13's silence traced end-to-end through `fye.a()`→`fyv.c()`→`aie` (smali-resolved, JADX-undecompilable `BiFunction`)→`fyv.a()`→`esk` case 19→`nqo.e()` (a real, unconditional pw_rpc send once reached — not a dead/cold-observable path). **New candidate found**: both known callers (`QuickActionsFragment` in-app tap, `gvj`/`gvi` physical-gesture path) are gated by the *same* `ftj.p(deviceId)`→`Optional<fye>` per-device lookup (Dagger-subcomponent-backed) — if empty, both paths silently skip `fye.a()` (the in-app path logs; the gesture path does not). Not confirmed empty/non-empty for `CAP-051`'s session — tracing the Dagger subcomponent's own population condition is flagged as the next step, not completed. Also **refuted** the "wrong screen" hypothesis: `main_fragment_contents.xml` confirms `QuickActionsFragment` is embedded directly in the same screen `CAP-051` used. Written up in `CAP-051-FINDINGS.md` §3a and `REVERSE_ENGINEERING.md`'s `qhr` entry; proposed to `PROTOCOL.md` §6. |
| 2 | done | `CAP-050`'s `PRIV-001` codes checked against every remaining official Fast Pair spec extension page (`WebFetch` — Message Stream, Device Action, Change Capability, Personalized Name, Retroactive Account Key, MAC — all clean negatives); a hex-literal-pair APK search (zero matches, cleaner signal than the noisy decimal-pair search also tried); the `qhr`-field-21-numeric-coincidence check (already has a known, unrelated write site); `MaestroDeviceSettingsProviderService`'s dispatcher re-read in full (confirmed exactly 6 case IDs, none DLCI-0x08-shaped); the native `.so` list re-checked directly (still only the same 2 files). All reinforce the existing negative; no new match found. |
| 3 | partial | "GSND" naming lead re-searched: resource-XML APK sweep (new location, still zero matches) plus a public `WebSearch` — found `"GSOUND_BT_CONTROL"`/`"GSOUND_BT_AUDIO"` independently documented on an unrelated Sony headphone, a genuinely new cross-vendor lead (not a resolution). The per-earbud charging-icon asymmetry was checked against `HeadsetPiece` (confirmed a raw GMS pass-through, unresolvable from this app's code) and the ADR-016 disconnect tension against `gck`/`gcl`/`eht` (no dock-debounce/suppression logic found, clean negative). **Not done this session**: the DLCI 0x0a burst vs. the full `maestro_pw.*` RPC catalog re-check (item 2) — deferred, not attempted. |
| 4 | partial | `gjv.p()`'s caller **found** — a genuinely different strategy (smali cross-reference on the abstract supertype's call descriptor `Lgiz;->p(`, rather than a search for `giz`-typed fields) locates the sole call site: an `OtaApplyWorker` completion-callback lambda, an inline chain that the field-based search in both prior passes could not have found by construction. This makes `gjv.p()` an OTA-apply-completion trigger, not the generic connect-lifecycle trigger it was previously framed as — narrows, rather than confirms, the original hypothesis. `MaestroEndpointService`'s Dagger-multibinding assembly site was attempted with a different strategy (generic-signature/`Lofd;`-reference smali search) — found several new candidate files (`fsg`/`gaf`/`oex`/`ofh`/`ofi`/`ofj`) not previously catalogued, but none confirmed as the actual assembly site before time ran out on this angle; still open, genuinely attempted a third distinct way, not merely re-asserted. **Not done this session**: full manifest re-review; the broader resource/string-table sweep beyond GSND. |
| 5 | done | Cross-checked every finding above against `PROTOCOL.md`/`DECISIONS.md`'s existing text — no contradiction of any existing 🟢 FACT found; every finding is additive or a reinforced negative, and is written up as such (not silently resolved) in the actual document updates below. |
| 6 | done | External validation performed inline with Phases 2/3 (`WebFetch` for the Fast Pair spec sweep, `WebSearch` for the GSND/GSOUND lead) rather than as a separate pass; `DESKRESEARCH_FINDINGS.md` given a new dated entry recording both. |
| 7 | done | Updated in place: `CAP-051-FINDINGS.md` (new §3a + open-questions cross-references), `PROTOCOL.md` §6 (3 dated updates: `qhr` field 13, `PRIV-001`, GSND/`gjv.p()`) and §8 (new changelog row), `REVERSE_ENGINEERING.md` (`qhr`/`fye` entry, GSND entry, `MaestroDeviceSettingsProviderService` entry, `ijk`/`HeadsetPiece` entry, `frb`/`fuh`/`glk`/`gjv` entry, Native libraries section), `TODO.md` (`gjv.p()` item closed), `DESKRESEARCH_FINDINGS.md` (new entry). All new correlations are labeled as proposals awaiting maintainer sign-off, per `AGENTS.md` §6 — nothing was promoted to 🟢 FACT and no `DECISIONS.md` ADR was written. |
| 8 | done | Wrap-up summary below. `ai-sessions/INDEX.md` row updated to `awaiting maintainer sign-off`. `scripts/lint_docs.py` run (see note below). |

## What genuinely remains for a future resumption of this prompt

This pass covered the highest-value angles for each capture's open items in real depth (particularly Phase 1's `qhr`/`fye` trace and Phase 4's `gjv.p()` resolution, both genuinely new), but did **not** achieve literal, mechanical coverage of every sub-bullet the original prompt listed. Specifically still open, honestly not attempted this pass:

- Phase 3 item 2: DLCI 0x0a's burst content vs. the *full* `maestro_pw.*` RPC catalog (only the already-catalogued 8 services were considered, no fresh re-enumeration).
- Phase 4 item 1: `MaestroEndpointService`'s Dagger-multibinding assembly site — new candidate files found (`fsg`/`gaf`/`oex`/`ofh`/`ofi`/`ofj`), none individually confirmed or ruled out.
- Phase 4 item 3: full `AndroidManifest.xml` re-review for uncatalogued exported components.
- Phase 4 item 4: a resource/string-table sweep beyond the GSND-specific search.
- `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`/`CAPTURE_BLUETOOTH_HCI_SNOOP.md` full re-reads (Phase 0's own ask) — referenced only indirectly via the capture FINDINGS files, not read start-to-finish.
- Tracing `ftf.g(str)`/`D(str)`'s own Dagger-subcomponent construction (Phase 1's own natural next step) — would determine whether the `Optional<fye>` gate hypothesis is actually confirmable from static analysis alone.

A future session resuming this prompt should start with these six items rather than re-doing Phases 0-2/5-7, which are genuinely complete for this pass.

## Summary for the maintainer

**Headline results, each accepted at 🟡 HYPOTHESIS (maintainer sign-off obtained 2026-09-16) — nothing promoted to FACT, no `DECISIONS.md` ADR written:**

1. **`CAP-051`'s `qhr` field-13 silence** now has a single, concrete, code-traced candidate explanation — a shared `Optional<fye>` per-device gate (`ftj.p(deviceId)`) — that would explain both the in-app-tap and physical-gesture silence uniformly, rather than two separate unexplained gaps. Not confirmed as the actual cause for this session; the "wrong screen" alternative is refuted, not confirmed.
2. **`CAP-050`'s `PRIV-001` codes**: 5 of 7 resolve to reinforced negatives after a structurally broader search (official spec pages, hex-literal search, case-ID re-read, native-lib re-check); 2 (`04 04`/`04 15`) remain genuinely inconclusive, unchanged.
3. **The "GSND" naming lead** gained a genuinely new, externally-verifiable data point: `"GSOUND_BT_CONTROL"`/`"GSOUND_BT_AUDIO"` are independently documented on an unrelated Sony headphone — pointing toward a cross-vendor accessory-naming convention, not a Google-specific term. Does not resolve DLCI 0x08's identity.
4. **`gjv.p()`'s long-unresolved caller was found** — a genuine resolution of an item two prior sessions had given up on, via a smali-level strategy neither prior pass tried. It turns out to be an OTA-firmware-update-apply-completion callback, which *narrows away from*, rather than confirms, the "connect-lifecycle trigger" reading this project had been assuming.
5. **Per-earbud charging-icon asymmetry (`CAP-047`) and the ADR-016 disconnect tension**: both checked against the relevant app-side domain code and found unresolvable from this companion app's own decompiled source (clean negatives, consistent with the existing GMS-boundary understanding).

**Everything above is written up in place** in `CAP-051-FINDINGS.md`, `PROTOCOL.md` §6/§8, `REVERSE_ENGINEERING.md` (multiple entries), `TODO.md`, and `DESKRESEARCH_FINDINGS.md` — see each file's own dated 2026-09-15 update for full command+hex/code evidence. Nothing is uncommitted beyond these documentation edits (no code, no new capture).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0023_CROSSCHECK_RESULT_2026_09_15.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0023_CROSSCHECK_RESULT_2026_09_15
