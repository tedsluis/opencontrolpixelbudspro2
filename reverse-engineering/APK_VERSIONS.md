# APK_VERSIONS.md

Index of every official Pixel Buds companion app APK version analyzed by this project. This is the
**only** git-tracked artifact under `reverse-engineering/` — the actual APK files and all decompiled/
extracted output live only on the maintainer's own machine, never in this repository (see
`.gitignore`, `PROJECT_RULES.md` §8 rule 20, and `PROJECT.md`'s non-goals). Register a new version
here **before** starting analysis on it, per `APK_REVERSE_ENGINEERING_PROCEDURE.md`.

## Directory layout (local only, not committed)

```
reverse-engineering/
  APK_VERSIONS.md              <- this file (git-tracked)
  apk/                          <- entirely gitignored, local-only
    v<versionName>-<versionCode>/
      <package>.apk             <- base APK (or base+split APKs, see "pm path" note below)
      <package>.config.apk      <- e.g. a density/language split, if `pm path` returned more than one file
      jadx-output/
      apktool-output/
      pbtk-output/
```

One subfolder per analyzed APK version, keyed by `versionName`+`versionCode` (e.g. `v3.5.212-30500212`).
If `pm path <package>` returns more than one file (a split APK install), pull and store all of them in
the same version subfolder — record each file's own SHA-256 in the table below, not just the base APK's.

## Analyzed versions

| Version dir | File(s) | SHA-256 | versionName | versionCode | Pull date | Source device | Provenance | JADX ver. | apktool ver. | pbtk ver. |
|---|---|---|---|---|---|---|---|---|---|---|
| `v1.0.955078536-10253511/` | `base.apk` | `b12d75d07f7743b83ed0d507a9d6b7b2abf61d8f1e7c3e63aadc442c097d6c86` | `1.0.955078536` | `10253511` | 2026-08-30 | Pixel 7a (`lynx`, serial `38021JEHN07835`), maintainer's own device | Play Store, maintainer's own Google account, installed on the maintainer's own device | `1.5.1` | `3.0.3` | `pbtk 1.1.3` (pipx) |
| `v1.0.955078536-10253511/` | `split_config.arm64_v8a.apk` | `ae63526672528d4fbed3086c2cd8280966bdd9ce9e764d2f00456f5b7698f3ba` | `1.0.955078536` | `10253511` | 2026-08-30 | Pixel 7a (`lynx`, serial `38021JEHN07835`), maintainer's own device | Play Store, maintainer's own Google account, installed on the maintainer's own device | `1.5.1` | `3.0.3` | `pbtk 1.1.3` (pipx) |
| `v1.0.955078536-10253511/` | `split_config.xxhdpi.apk` | `a759ef5bdf17aa431fa58f64c27ea5e2801b114b7436ca785834012126286c46` | `1.0.955078536` | `10253511` | 2026-08-30 | Pixel 7a (`lynx`, serial `38021JEHN07835`), maintainer's own device | Play Store, maintainer's own Google account, installed on the maintainer's own device | `1.5.1` | `3.0.3` | `pbtk 1.1.3` (pipx) |

minSdk=32, targetSdk=36 (from `dumpsys package`, recorded here for reference; not part of the
table's own columns).

The first APK version has now been pulled and stored locally under
`reverse-engineering/apk/v1.0.955078536-10253511/` (gitignored, not committed) — decompiling
(§3 of `APK_REVERSE_ENGINEERING_PROCEDURE.md`) has not started yet.

## Why provenance matters here

Recording *how* each APK was obtained strengthens the "legally obtained" basis `PROJECT_RULES.md`
§8 rule 20 requires for this project's whole reverse-engineering effort — e.g. "Play Store, pulled
from the maintainer's own device via `adb pull`, installed under the maintainer's own Google
account" is a materially stronger provenance record than no note at all, and is cheap to capture at
pull time versus reconstructing it later.

## Tool-version pinning

Decompiler output (line numbers, class layout, even smali register naming) can shift between JADX/
apktool versions. A file+line citation in `REVERSE_ENGINEERING.md` (e.g.
`jadx-output/sources/com/google/.../Xy2.java:142`) is only reproducible if the exact decompiler
version used to produce that output is also pinned — record it in this table's JADX/apktool/pbtk
version columns for every analyzed APK version, not just once globally, since tool versions
installed on the maintainer's workstation can change between analysis sessions (see
`WORKSTATION_PREPARATIONS.md`).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/reverse-engineering/APK_VERSIONS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/reverse-engineering/APK_VERSIONS
