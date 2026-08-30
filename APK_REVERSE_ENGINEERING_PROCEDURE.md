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
- [ ] **JADX**, **apktool**, and **pbtk** installed (`WORKSTATION_PREPARATIONS.md`'s "Reverse
      engineering tools: JADX, apktool, pbtk" section) — pbtk's own dependencies (Python ≥ 3.10,
      PySide6, python-protobuf, and `jad`/`dex2jar` for some extractor scripts) confirmed there too.
- [ ] **Android platform-tools** (`adb`) installed and on your `PATH` (already required by
      `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §1.1).
- [ ] `sha256sum` (standard on Fedora).

### 1.2 On the phone
- [ ] The official Pixel Buds companion app installed under the maintainer's own Google account,
      on the maintainer's own device — this is the provenance basis `PROJECT_RULES.md` §8 rule 20
      requires; see `reverse-engineering/APK_VERSIONS.md`'s provenance column.
- [ ] USB debugging enabled (same as `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §1.2).

### 1.3 Reading this session should already have done
- [ ] `AGENTS.md` §4/§6 (proto-schema extraction rules, sign-off requirement).
- [ ] `DECISIONS.md` ADR-017 (the current AI-assistance boundary — supersedes the older ADR-003).
- [ ] `REVERSE_ENGINEERING.md`'s own template and status legend.

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

# .proto schema extraction (see WORKSTATION_PREPARATIONS.md for the real, confirmed pbtk scope —
# it is NOT limited to Java/DEX; pbtk-from-binary targets native .so files too, though whether it
# actually succeeds against libmaestro/libgfps's specific binaries is unconfirmed until tried)
pbtk-jar-extract base.apk pbtk-output/
pbtk-from-binary apktool-output/lib/<abi>/libmaestro.so pbtk-output/   # once §4's native .so pass locates it
```

Record the exact **tool versions used** (`jadx --version`, `apktool --version`, pbtk's `pipx list`
output) in `reverse-engineering/APK_VERSIONS.md`'s row for this version — decompiler output (line
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
  compatibility. Use `pbtk`'s actual extraction output (§3), not a guessed schema.
- **JADX can misdecompile obfuscated/optimized constructs** — when a decompiled method looks
  suspicious or incomplete, cross-check against the `apktool` smali output before trusting it.
- **Reflection-based code stays invisible to static analysis** — if a call site is never found
  despite a class clearly needing one, that's a sign dynamic analysis (Frida) may be needed; log any
  such experiment in the relevant capture's `CAP-NNN-FINDINGS.md` first, per `PROJECT_RULES.md` §4.
- **Native `.so` disassembly is in scope for AI mechanical assistance** (`DECISIONS.md` ADR-017 §4),
  on the same terms as DEX/Java work: search, list, and explain disassembly output; never decide
  relevance or promote a finding. Whether `pbtk-from-binary` actually succeeds against
  `libmaestro`/`libgfps`'s specific binaries (vs. a stripped protobuf-lite descriptor pool) is
  unconfirmed until tried — see `WORKSTATION_PREPARATIONS.md`.
- **A class or method existing in the APK does not prove it's exercised by any specific action** in
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` — treat every static finding as 🟡 HYPOTHESIS until a capture
  shows the corresponding traffic (`REVERSE_ENGINEERING.md`'s own "Known limitations" section).
- **Nothing under `reverse-engineering/apk/` is ever committed** — not the APK, not `jadx-output/`,
  not `apktool-output/`, not `pbtk-output/`. Only `reverse-engineering/APK_VERSIONS.md` and this
  procedure document are git-tracked. Double-check `git status` before committing anything from a
  reverse-engineering session.
- **Never independently promote a finding to 🟢 FACT, and never commit a new/superseding
  `DECISIONS.md` ADR** — `AGENTS.md` §6/§15, unaffected by ADR-017's mechanical-assistance boundary.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/APK_REVERSE_ENGINEERING_PROCEDURE.md - https://tedsluis.github.io/opencontrolpixelbudspro2/APK_REVERSE_ENGINEERING_PROCEDURE
