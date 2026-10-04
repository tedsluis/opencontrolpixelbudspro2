# APK_REVERSE_ENGINEERING_PROCEDURE.md — Pixel Buds APK Analysis Guide

**Purpose:** step-by-step procedure to pull, store, decompile, and analyze the official Pixel Buds
companion app APK, in order to fill in the confidence-rated placeholders in `REVERSE_ENGINEERING.md`
and, once wire-correlated, `PROTOCOL.md`. This document is the *how* — `REVERSE_ENGINEERING.md` is
the *what we found*, and `reverse-engineering/APK_VERSIONS.md` is the *which APK version(s) we've
looked at*.

This procedure follows `AGENTS.md` §6/`DECISIONS.md` ADR-017's AI-assistance boundary throughout:
an AI session may run the mechanical steps below (pulling, hashing, decompiling, running `pbtk`,
keyword/string searching, explaining already-surfaced code or disassembly — including native `.so`
disassembly output, per ADR-017 §4), but never decides which candidate is relevant to the protocol
and never decides that something becomes a recorded HYPOTHESIS in `REVERSE_ENGINEERING.md`. Both of
those remain the maintainer's calls at every step marked **[Maintainer decision]** below.

---

## 1. Prerequisites

### 1.1 On your computer
- [x] **JADX**, **apktool**, and **pbtk** installed (`WORKSTATION_PREPARATIONS.md`'s "Reverse
      engineering tools: JADX, apktool, pbtk" section) — pbtk's own dependencies (Python ≥ 3.10,
      PySide6, python-protobuf, and `jad`/`dex2jar` for some extractor scripts) confirmed there too.
      Verified 2026-08-30: `apktool --version` → `3.0.3`, `jadx --version` → `1.5.1`, `pipx list` →
      `pbtk 1.1.3` with `pbtk-jar-extract`/`pbtk-from-binary` on `PATH`.
- [x] **Android platform-tools** (`adb`) installed and on your `PATH` (already required by
      `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §1.1). Verified 2026-08-30: `adb --version` →
      `1.0.41` / `37.0.0-android-tools`.
- [x] `sha256sum` (standard on Fedora). Verified 2026-08-30: GNU coreutils 9.10.

### 1.2 On the phone
- [x] The official Pixel Buds companion app installed under the maintainer's own Google account,
      on the maintainer's own device — this is the provenance basis `PROJECT_RULES.md` §8 rule 20
      requires; see `reverse-engineering/APK_VERSIONS.md`'s provenance column. Confirmed by
      maintainer 2026-08-30: Play Store, maintainer's own Google account, installed on the
      maintainer's own Pixel 7a.
- [x] USB debugging enabled (same as `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §1.2). Confirmed 2026-08-30:
      `adb devices -l` shows the Pixel 7a (`38021JEHN07835`, model `lynx`) authorized, and
      `adb pull` succeeded against it.

### 1.3 Reading this session should already have done
- [x] `AGENTS.md` §4/§6 (proto-schema extraction rules, sign-off requirement).
- [x] `DECISIONS.md` ADR-017 (the current AI-assistance boundary — supersedes the older ADR-003).
- [x] `REVERSE_ENGINEERING.md`'s own template and status legend.

---

## 2. Pulling and Storing a New APK Version

1. Find the package name and installed version:
   ```bash
   adb shell dumpsys package com.google.android.apps.wearables.maestro.companion | grep -E "versionName|versionCode"
   ```
2. Find the APK path(s) — **a companion app this size may be split** (base + density/language/ABI
   splits), so `pm path` can legitimately return more than one line. Pull **all** of them:
   ```bash
   adb shell pm path com.google.android.apps.wearables.maestro.companion
   # package:/data/app/.../base.apk
   # package:/data/app/.../split_config.xxhdpi.apk   <- pull this one too, if present
   mkdir -p "reverse-engineering/apk/v<versionName>-<versionCode>/"
   adb pull /data/app/.../base.apk "reverse-engineering/apk/v<versionName>-<versionCode>/"
   adb pull /data/app/.../split_config.xxhdpi.apk "reverse-engineering/apk/v<versionName>-<versionCode>/"  # repeat per split
   ```
3. Hash every pulled file and record the result:
   ```bash
   sha256sum reverse-engineering/apk/v<versionName>-<versionCode>/*.apk
   ```
4. **[Maintainer decision — recording provenance is mechanical, but confirm the row is accurate]**
   Add a row to `reverse-engineering/APK_VERSIONS.md`'s table: version dir, file(s), SHA-256 per
   file, versionName/versionCode, pull date, source device, and provenance (how it was obtained —
   e.g. "Play Store, maintainer's own Google account, installed on the maintainer's own device").
5. Remember: **nothing under `reverse-engineering/apk/` is ever committed** — it's fully covered by
   `.gitignore` (the APK itself, `jadx-output/`, `apktool-output/`, `pbtk-output/`). Only the
   `reverse-engineering/APK_VERSIONS.md` row you just added is git-tracked.

### 2.1 Diff / re-check pass against the previous version

A new companion-app release doesn't mean starting analysis from zero. Before decompiling from
scratch:

1. If a previous version's `apktool-output/` still exists locally, diff the two versions' DEX class
   lists (e.g. `diff <(unzip -l v<old>/base.apk | grep '\.dex$') <(unzip -l v<new>/base.apk | grep '\.dex$')`,
   or compare `apktool`'s own `smali/` directory trees after decompiling both) to see whether the
   set of classes changed at all before re-running a full keyword search.
2. Re-check every class/finding already recorded in `REVERSE_ENGINEERING.md`'s "Identified relevant
   classes" section and "Correlation status with `PROTOCOL.md`" table against the new version — note
   whether each one still exists, moved, or was removed/renamed, rather than assuming prior findings
   still apply unchanged.
3. **[Maintainer decision]** Whether a shifted/renamed class still represents the same behavior (and
   whether any existing `PROTOCOL.md` FACT needs re-verification against the new version) is a
   relevance judgment — flag candidates, don't silently carry forward or silently invalidate a prior
   finding.

---

## 3. Decompiling and Schema Extraction

```bash
cd "reverse-engineering/apk/v<versionName>-<versionCode>/"

# Readable Kotlin/Java (primary source for keyword search and citations)
jadx -d jadx-output/ base.apk

# Resources, manifest, smali (fallback when JADX misdecompiles something)
apktool d base.apk -o apktool-output/

# Schema recovery — the method that works on this APK (corrected 2026-10-03, ai-sessions/0069, A68-RE-04 / §6.15):
# the messages are protobuf-lite classes whose layout is a "RawMessageInfo" string in the decompiled Java; this
# script decodes that string into field numbers and types.
python3 scripts/decode_rawmessageinfo.py <path to the decompiled class>.java
# Batch and helper tools (each with its own SPEC.md / README.md): reverse-engineering/tools/schema_batch_extractor,
# structural_index, lambda_dispatcher_resolver, limited_dataflow, uuid_ble_context.
```

**`pbtk` does not apply to this APK** (`DECISIONS.md` ADR-041): `pbtk-jar-extract` finds no schema in it, and there is no native
`libmaestro.so`/`libgfps.so` to run `pbtk-from-binary` on — the Maestro logic is Kotlin/Java (`AGENTS.md` §0, correction of 2026-08-30). The two
`pbtk` commands this section used to list are kept out of the procedure; try them again only on an APK version that ships such a library.

**When JADX skips a method** ("Method dump skipped", an empty body, or a `/* JADX WARN */` block), grep the smali before recording "not found":
`grep -rn "<string or field>" apktool-output/smali*/` — the head-gestures write site was found this way after two passes had recorded it as
missing (`REVERSE_ENGINEERING.md` row 29, `ai-sessions/0068` `A68-RE-01`).

Record the exact **tool versions used** (`jadx --version`, `apktool --version`, the commit of `scripts/decode_rawmessageinfo.py`) in `reverse-engineering/APK_VERSIONS.md`'s row for this version — decompiler output (line
numbers, class layout) can shift between versions, so a file+line citation is only reproducible if
the decompiler version is pinned too.

---

## 4. Keyword Search and Search-Efficiency Techniques

This is the mechanical step ADR-017 permits an AI session to run directly. Search results are
**candidates**, not findings — every candidate goes to the maintainer for the relevance call before
anything is recorded in `REVERSE_ENGINEERING.md`.

1. **Start from `AndroidManifest.xml`** (`apktool-output/AndroidManifest.xml`, not the obfuscated
   binary form) — find services/receivers registered against Bluetooth-related intents/actions
   first. This anchors which classes are worth reading closely before any blind keyword search.
2. **Search for string literals, not just class/method names** — log tags, notification channel
   names, broadcast action strings, and SharedPreferences keys survive ProGuard/R8 obfuscation even
   when class names don't (obfuscation renames identifiers, not string constants).
3. **Check `qzed/pbpctrl`'s public documentation first**, as a research accelerant — protocol
   *knowledge* only, per `AGENTS.md` §12/`README.md`'s attribution boundary (never copy code; every
   candidate this surfaces still needs independent re-verification against this project's own
   APK/captures, not taken on `pbpctrl`'s word).
4. Search `jadx-output/` for the keyword list in `REVERSE_ENGINEERING.md` §Method (BLE/GATT classes,
   RFCOMM/Fast-Pair classes, HID classes, package-name fragments, `.proto`-generated-class markers).
5. **Apply the exclusion list below during this step itself** — don't collect candidates from these
   areas and filter them out later; skip past them at search time.
6. Present candidates to the maintainer: file path, line number, the matched keyword/string, and a
   one-line note on why it looked relevant. **[Maintainer decision]** which candidates get written up
   in `REVERSE_ENGINEERING.md`, and at what confidence tier.

### 4a. Resolving an R8-merged lambda dispatcher (added 2026-09-16, `ai-sessions/0024`; adoption confirmed 2026-09-16, `ai-sessions/0025`)

If step 4's keyword search (or any other reading) surfaces a class matching the shape in
`reverse-engineering/tools/lambda_dispatcher_resolver/SPEC.md` §3 (a `synthetic final` class, one
interface with one abstract method, an `int` discriminator field assigned in every constructor and
switched on immediately in the method body) — run `lambda_dispatcher_resolver list` first if the
class isn't already a known instance of this pattern, then `resolve-all --class <name>` rather than
hand-reading the smali `packed-switch`/`if`-chain. Read **every** returned case (per `AGENTS.md`
§13.6 — an unresolved case is not the same as a checked-and-irrelevant one), cross-reference each
against `REVERSE_ENGINEERING.md`'s existing catalogue, and record only genuinely Bluetooth-relevant
findings there — most cases of a heavily-reused dispatcher (see `ai-sessions/0024`'s own `aie`/`esk`
pass, which found only 1 of 20 and 3 of 21 cases respectively were Bluetooth-relevant) will be
unrelated app-wide code, and that is a legitimate, recordable checked-negative result, not a reason
to stop reading partway through.

**Adoption note (2026-09-16, `ai-sessions/0025`):** this step's own text was applied directly by
`ai-sessions/0024` as normative procedure (never carried a "proposal"/"awaiting sign-off" tag in
this file's own text, unlike `reverse-engineering/tools/BACKLOG.md`'s separate priority-order
blockquote) — this note records that `ai-sessions/0025` re-read and exercised it for real
(`resolve-all --class gag`, per that session's own Phase 1/Phase 3 write-up) with no need for
substantive change, per `AI_SESSION_LOG_PROCEDURE.md` §4a's citation requirement for this class of
workflow-process decision.

### 4.1 Exclusion list — noise to skip past, not to investigate

A fully decompiled app exposes far more surface area than any single capture. The following are
explicitly **out of scope** (`PROJECT.md` non-goals, `DECISIONS.md` ADR-008) — do not spend search
or read time on them even if they surface incidentally:

- `AccountLinking` (any package/class matching this name)
- `OwnershipTransfer` (any package/class matching this name)
- `AccessoryNonOwner` (the Accessory Non-Owner Service — see ADR-008)
- Any Firebase-, Analytics-, or Crashlytics-named package or class (`com.google.firebase.*`,
  `*Analytics*`, `*Crashlytics*`, or equivalent — see `AGENTS.md` §1's Zero-GMS rule)

If one of these surfaces incidentally while searching for something else, note it as
"out-of-scope, skipped" (mirroring how `DECISIONS.md` ADR-008 already treats incidental
Account-Linking/Non-Owner traffic in captures) and move on — do not follow the call graph into it.

---

## 5. Analysis Approach

1. Write up each maintainer-approved candidate in `REVERSE_ENGINEERING.md`'s "Identified relevant
   classes" section, using its template — **every finding cites the exact decompiled file and line
   number** (e.g. `jadx-output/sources/com/google/.../Xy2.java:142`), not only a class name.
   **Tracing a wire-confirmed field number back to its APK write/read call sites** follows a named
   precedent, `DECISIONS.md` ADR-019 — either backward (from an already-decoded response-handler log
   message to the write call site that populates the same field) or forward (from a named UI
   fragment/preference key to the write call site), whichever direction actually has evidence
   available; both directions are equally valid, per ADR-019's own two applications.
2. Label every finding FACT / HYPOTHESIS / ASSUMPTION / OPEN QUESTION per `PROJECT_RULES.md` §1 —
   static analysis alone is never 🟢 FACT for a *protocol* claim (only for "this code exists and
   looks like X"); a protocol-behavior claim needs capture correlation first.
3. For every HYPOTHESIS recorded, add a short note on how it could be confirmed or refuted against a
   capture — which action/Test-ID (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`) would need to be captured —
   per `REVERSE_ENGINEERING.md`'s template (see `PROJECT_RULES.md` §4's hypothesis-test discipline,
   already used for captures).
4. Once a finding is cross-checked against a real capture, promote it **directly** into `PROTOCOL.md`
   — there is no intermediate working-notes buffer (`PROTOCOL_NOTES.md` was retired 2026-08-15, see
   `PROJECT_RULES.md` §2 rule 5). Update `REVERSE_ENGINEERING.md`'s "Correlation status with
   `PROTOCOL.md`" table so the same finding is never independently "rediscovered" in both documents.
5. **[Maintainer decision, per `AGENTS.md` §6]** Promoting anything to 🟢 FACT in `PROTOCOL.md`, or
   writing/superseding a `DECISIONS.md` ADR, still requires explicit maintainer sign-off — an AI
   session may propose and draft, never commit either as settled.

---

## 6. Notes & Gotchas

- **Never hand-reconstruct a `.proto` schema from getter/setter names alone** — field *names*
  recovered from JADX are not proof of the actual wire field *numbers*, which determine binary
  compatibility. Use the schema decoder's output (§3, `scripts/decode_rawmessageinfo.py`), not a guessed schema.
- **JADX can misdecompile obfuscated/optimized constructs** — when a decompiled method looks
  suspicious or incomplete, cross-check against the `apktool` smali output before trusting it.
- **Reflection-based code stays invisible to static analysis** — if a call site is never found
  despite a class clearly needing one, that's a sign dynamic analysis (Frida) may be needed; log any
  such experiment in the relevant capture's `CAP-NNN-FINDINGS.md` first, per `PROJECT_RULES.md` §4.
- **Native `.so` disassembly is in scope for AI mechanical assistance** (`DECISIONS.md` ADR-017 §4),
  on the same terms as DEX/Java work: search, list, and explain disassembly output; never decide
  relevance or promote a finding. This APK version has no `libmaestro`/`libgfps` native library at all (§3), so there is nothing
  to disassemble for the Maestro logic today.
- **If a caller/reference search comes back empty, retry with the structurally opposite search
  strategy before concluding it's a dead end** (added 2026-09-16, `ai-sessions/0023`/`ai-sessions/0024`).
  `ai-sessions/0023`'s `gjv.p()` caller trace failed twice searching for classes holding a
  `giz`-typed *field*, then succeeded immediately on a differently-shaped search (a smali
  invoke-descriptor grep, `Lgiz;->p(`, for an inline-chained call that was never stored in a field —
  structurally invisible to the first strategy by construction, not by bad luck). Two different
  search strategies for the same question is cheap; a third session re-deriving the same failed
  strategy from scratch is not. **Adoption note (2026-09-16, `ai-sessions/0025`):** confirmed
  practiced, not just documented — this same session found `qhr` field 6's own caller
  (`REVERSE_ENGINEERING.md`'s `qhr` entry's 2026-09-16 update) using exactly this technique
  (`fyo.m`'s own zero callers → retrying against the abstract interface type `fya` instead of the
  concrete class → finding the real caller immediately), an independent second confirmation beyond
  `gjv.p()` that this discipline generalizes.
- **A class or method existing in the APK does not prove it's exercised by any specific action** in
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` — treat every static finding as 🟡 HYPOTHESIS until a capture
  shows the corresponding traffic (`REVERSE_ENGINEERING.md`'s own "Known limitations" section).
- **Nothing under `reverse-engineering/apk/` is ever committed** — not the APK, not `jadx-output/`,
  not `apktool-output/`, not `pbtk-output/`. Only `reverse-engineering/APK_VERSIONS.md` and this
  procedure document are git-tracked. Double-check `git status` before committing anything from a
  reverse-engineering session.
- **Never independently promote a finding to 🟢 FACT, and never commit a new/superseding
  `DECISIONS.md` ADR** — `AGENTS.md` §6/§15, unaffected by ADR-017's mechanical-assistance boundary.

## 7. Checklist for a new APK version or a new Buds firmware (added 2026-10-03, `ai-sessions/0069`)

1. Pull and store the APK (§2); add its row to `reverse-engineering/APK_VERSIONS.md` with the tool versions.
2. Decompile (§3). Re-run the schema decoder on the settings message (`qhr` in `v1.0.955078536`; the class name changes with every build —
   find it by its field count and by the string literals of its callers, `REVERSE_ENGINEERING.md`).
3. Diff against the previous version (§2.1): the settings fields (numbers and types), the pw_rpc service and method name literals, the Fast Pair
   model-ID handling.
4. For every field the OpenControl app **writes** (`DECISIONS.md` ADR-045/046/047, EQ ADR-034): same number, same type, same value range? A
   difference is a protocol change — `PROTOCOL.md` and an ADR before any code.
5. For a new **firmware**: the wire side of this check is `RELEASING.md`, "New Buds firmware" (capture the official app's connect burst and
   compare it with `release_5.203`).
6. Record what was checked and what was not in `REVERSE_ENGINEERING.md`, with file and line for each claim.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/APK_REVERSE_ENGINEERING_PROCEDURE.md - https://tedsluis.github.io/opencontrolpixelbudspro2/APK_REVERSE_ENGINEERING_PROCEDURE
