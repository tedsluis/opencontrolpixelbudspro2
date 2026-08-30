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
| _(e.g. `v3.5.212-30500212/`)_ | _(e.g. `base.apk`)_ | _(`sha256sum <file>`)_ | _(from `pm dump <package>` or `aapt dump badging`)_ | _(same)_ | _(YYYY-MM-DD)_ | _(e.g. "Pixel 7a, maintainer's own device/account")_ | _(how obtained — e.g. "Play Store, maintainer's own Google account, installed on maintainer's own device")_ | _(e.g. `1.5.1`)_ | _(e.g. `2.9.3`)_ | _(e.g. `pbtk 2026.x`, `pipx list` output)_ |

No version has been pulled or analyzed yet — `TODO.md` Phase 2 (APK static analysis) is at the
tooling/process stage, not the analysis stage. Populate a row here, and create the matching
`reverse-engineering/apk/v<versionName>-<versionCode>/` folder locally, the first time an APK is
actually pulled — see `APK_REVERSE_ENGINEERING_PROCEDURE.md`.

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
