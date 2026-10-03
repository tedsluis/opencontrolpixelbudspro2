# RELEASING.md — How to publish a release of OpenControl for Pixel Buds Pro 2

The app is distributed manually, as a signed APK on this repository's GitHub Releases page — no app store, and no update check in the app (`DECISIONS.md`
ADR-004). This runbook was written in `ai-sessions/0065` (2026-10-02); every step is the maintainer's own act. An AI session may prepare files and print
commands, but it never runs a publishing step (tag push, release, upload, repository setting) without an explicit "yes" in chat for that step, and it never
sees the keystore or its passwords.

**Why the key matters** (developer.android.com, "Sign your app", fetched 2026-10-02): *"When the system is installing an update to an app, it compares the
certificate(s) in the new version with those in the existing version. The system allows the update if the certificates match."* and *"If you lose your
app's signing key, you lose the ability to update your app."* So: one key for the life of the app, backed up, never in this repository.

## Release flow — the normal path and what to do when something goes wrong

```mermaid
flowchart TD
    A([Start a release]) --> K{Signing key and the four<br/>values set up?}
    K -- no --> K1["§1 create key + backup<br/>§2 gradle.properties<br/>(one password for PKCS12)"] --> K
    K -- yes --> P["§4 version, CHANGELOG, README<br/>commit + push"]
    P --> CI{CI green?}
    CI -- no --> CIF[Fix, commit, push] --> CI
    CI -- yes --> B["§5 scripts/release.sh &lt;version&gt;"]
    B --> BOK{Script OK?}
    BOK -- "missing signing values" --> K1
    BOK -- "Get Key failed / bad padding" --> KP["Key password ≠ store password,<br/>or .properties escaping (§2)"] --> B
    BOK -- "versionName / versionCode wrong" --> P
    BOK -- "commit not on origin" --> PU[git push] --> B
    BOK -- "signature / version / INTERNET check fails" --> STOP1[["Stop: find the cause<br/>in the build, fix via §4"]] --> P
    BOK -- yes --> FP{"Certificate SHA-256 =<br/>your noted fingerprint?"}
    FP -- no --> STOP2[["Stop: wrong keystore.<br/>Never publish with another key"]] --> K
    FP -- yes --> KEEP["Copy dist/&lt;version&gt; aside;<br/>do not rebuild this version"]
    KEEP --> I["§6 adb install --user &lt;id&gt;"]
    I --> IOK{Installed?}
    IOK -- "UPDATE_INCOMPATIBLE" --> UN["adb uninstall (debug build,<br/>all users, data lost)"] --> I
    IOK -- yes --> C["Hardware capture CAP-NNN on this APK<br/>(Info tab: version, hash, no -dirty)"]
    C --> AN[Analysis session: FINDINGS]
    AN --> R{Result?}
    R -- "heavy issue: crash, wrong write,<br/>Safe Mode, lost session, DoD failed" --> FIX["Fix in a normal session<br/>(tests, ADR if needed); commit + push"]
    FIX --> REV{Already published?}
    REV -- "no" --> SAME["Same version again<br/>(nothing public yet)"] --> B
    REV -- "yes" --> NEXT["Next version, higher versionCode<br/>(e.g. 1.0.1) — §4"] --> P
    R -- "Play-services claim in the HCI log" --> DOD["App not at fault: DoD 1–3 not proven<br/>by this run — investigate or re-run"] --> C
    R -- "minor issues" --> KI[Known issues in release-notes.md] --> CM
    R -- "OK" --> CM["Commit capture + analysis on main<br/>(tick DoD only with the evidence)"]
    CM --> T["§7 git tag -s v&lt;version&gt; &lt;build commit&gt;<br/>git push origin v&lt;version&gt;"]
    T --> D["gh release create --verify-tag --draft<br/>with the files from the kept copy"]
    D --> DC{Draft correct?}
    DC -- "text wrong" --> DE[gh release edit / fix notes] --> DC
    DC -- "wrong file attached" --> DF["gh release upload --clobber<br/>the tested files"] --> DC
    DC -- yes --> PUB[gh release edit --draft=false]
    PUB --> V{"§8 phone: download,<br/>SHA-256 matches?"}
    V -- no --> STOP3[["Stop: replace the asset with the<br/>tested file, check again"]] --> V
    V -- yes --> DONE(["Done: CHANGELOG date + README<br/>on main"])
    DONE -. "problem found after publishing" .-> NEXT
```

**Rules behind the flow:**

- **Until a release is published,** nothing is public but a pushed tag: a fix may be rebuilt as the **same version** (the builds differ by commit hash and
  SHA-256; an equal versionCode installs over the tested one). Push the tag only after the analysis is OK, so this case stays rare.
- **After publishing,** never move or delete a pushed tag and never reuse a version: a fix is the next version with a higher versionCode (§4).
- **The published files are the tested files** — from the copy kept in §5, never a rebuild.
- **The Definition of done** (`PROJECT.md`) is ticked only with the capture's evidence; a Play-services claim in the HCI log means "not proven", not "app broken".
- Every arrow that publishes (tag push, release create/edit/upload) is the maintainer's own act (see the top of this document).

(GitHub renders the diagram; the docs site shows its source text.)

## 1. Once: create the signing key

Run these yourself (keytool asks for the passwords and your name — the name ends up in the public certificate):

```bash
mkdir -p ~/keys && chmod 700 ~/keys
keytool -genkeypair -v -storetype PKCS12 \
  -keystore ~/keys/opencontrol-release.jks \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -alias opencontrol
keytool -list -v -keystore ~/keys/opencontrol-release.jks -alias opencontrol | grep -i "SHA256:"
```

**One password, not two:** a PKCS12 keystore protects the store and the key with the **same** password — keytool asks only once and uses it for both
(it ignores a separate `-keypass` for PKCS12). Write the same value into both `OPENCONTROL_STORE_PASSWORD` and `OPENCONTROL_KEY_PASSWORD` (§2).

Write the `SHA256:` fingerprint down (e.g. in your password manager). Every release prints the certificate's SHA-256; it must be this value (keytool
prints it in upper case with colons, `apksigner` in lower case without — the same hex digits).

**Backup, right away:** copy `opencontrol-release.jks` to two offline media (e.g. two USB sticks kept apart), encrypted — for example
`gpg --symmetric --cipher-algo AES256 ~/keys/opencontrol-release.jks` and copy the `.gpg` file. Keep both passwords in a password manager. A lost key or
password means no update can ever be installed over the released app (users would have to uninstall, losing their settings).

## 2. Once: tell Gradle where the key is

In `~/.gradle/gradle.properties` (outside the repository; type the passwords in an editor, not on a command line), then `chmod 600 ~/.gradle/gradle.properties`:

```properties
OPENCONTROL_STORE_FILE=/home/<you>/keys/opencontrol-release.jks
OPENCONTROL_KEY_ALIAS=opencontrol
OPENCONTROL_STORE_PASSWORD=...
OPENCONTROL_KEY_PASSWORD=...
```

`OPENCONTROL_KEY_PASSWORD` is the **same** as `OPENCONTROL_STORE_PASSWORD` (PKCS12, §1). A different value fails at `packageRelease` with *"Failed to read
key opencontrol from store …: Get Key failed: Given final block not properly padded"* (seen 2026-10-02) — the store opened, the key did not. Mind the
`.properties` format too: a backslash is an escape character (write `\\` for one `\`), trailing spaces belong to the value, and non-ASCII characters may be
read differently; if a name appears twice, the last line wins. To check without showing a password, `keytool -list -v -keystore … -alias opencontrol` proves the
store password (it prompts).

Environment variables with the same names work too. Without all four, `assembleRelease` stops with *"Release signing is not configured: missing …"* — it never
builds an unsigned APK or one signed with the debug key (`android/app/build.gradle.kts`). Debug builds, tests and lint need none of this.

## 3. Once: signed tags (optional but approved, `ai-sessions/0065` I-3)

With an SSH key you already use for GitHub:

```bash
git config --global gpg.format ssh
git config --global user.signingkey ~/.ssh/id_ed25519.pub
```

and add the same public key on github.com → Settings → SSH and GPG keys as a **Signing key**, so GitHub shows the tag as "Verified".

## 4. Each release: prepare the commit

1. **Version** in `android/app/build.gradle.kts`: `versionName` = the version, `versionCode` = major × 10000 + minor × 100 + patch (1.0.0 → 10000,
   1.0.1 → 10001, 1.1.0 → 10100; a release candidate before it, `1.0.0-rc.1` → 9901). The code must always grow — Android refuses a lower one over a higher one.
2. **`CHANGELOG.md`**: the version's block (marked "not yet released"; the date is added on `main` after publishing, §8). **`README.md`**: the status line.
3. Commit, push, and wait for CI (`.github/workflows/android.yml`) to be green.

## 5. Each release: build and verify

```bash
scripts/release.sh 1.0.0
```

It checks the version scheme and that the commit is on `origin`, builds in a separate clean worktree (so the Info tab shows the commit's hash without
"-dirty"), verifies the signature (`apksigner verify --print-certs`), the version and that there is no `INTERNET` permission, and writes `dist/1.0.0/`: the APK
(`opencontrol-pixelbudspro2-1.0.0.apk`), its `.sha256`, `THIRD_PARTY_NOTICES.txt` (the bundled libraries and their licences, `scripts/third_party_notices.py`)
and `release-notes.md` (from `scripts/release_notes.template`, checksums filled in). `dist/` is gitignored. It prints the next commands and runs none.

Check: the printed certificate SHA-256 equals your noted fingerprint; read `release-notes.md`. **Then keep this build:** copy it aside
(`cp -a dist/1.0.0 ~/opencontrol-1.0.0-tested`) and do **not** run `scripts/release.sh` for this version again — it empties `dist/<version>/`, and a new build
can have a different SHA-256: you would publish an APK you did not test.

## 6. Each release: test this exact APK

Install `dist/<version>/opencontrol-pixelbudspro2-<version>.apk` on the phone and run the planned hardware capture on it (for 1.0.0: `CAP-067`, in a
GrapheneOS secondary user without sandboxed Google Play — the route is in `ai-sessions/0066` RESULT §C.1; that run is also the evidence for `PROJECT.md`'s
Definition of done). On the Info tab: version and hash (no "-dirty"). A debug build installed before must be uninstalled first (different key).

With adb, into one user only (`adb shell pm list users` gives its id):

```bash
adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds | grep -E "versionName|User [0-9]+:.*installed=true"   # installed anywhere?
adb uninstall io.github.tedsluis.opencontrolpixelbuds    # only if a debug build is installed: removes it from ALL users, with its data
adb install --user <id> dist/1.0.0/opencontrol-pixelbudspro2-1.0.0.apk
```

`INSTALL_FAILED_UPDATE_INCOMPATIBLE` means another signing key's build is still installed in some user. Commits made on `main` while you test (documentation,
the capture and its analysis) do not touch the tested APK: the release is tied to the **build commit** (the hash on the Info tab), not to `main`.

## 7. Each release: publish (outward-facing — no way back once public)

The tag goes on the **build commit** printed by `scripts/release.sh` (the hash on the Info tab), even when `main` has moved on since — never on a later
commit, and never on `HEAD` by habit.

```bash
git tag -s v1.0.0 <commit> -m "OpenControl for Pixel Buds Pro 2 1.0.0"   # -a instead of -s without a signing key
git push origin v1.0.0
gh release create v1.0.0 --verify-tag --draft --title "OpenControl for Pixel Buds Pro 2 1.0.0" \
    --notes-file dist/1.0.0/release-notes.md \
    dist/1.0.0/opencontrol-pixelbudspro2-1.0.0.apk dist/1.0.0/opencontrol-pixelbudspro2-1.0.0.apk.sha256 dist/1.0.0/THIRD_PARTY_NOTICES.txt
```

Add `--prerelease` for a release candidate (`scripts/release.sh` does this for `-rc.N`). Open the draft on github.com, check the text and the three files,
then `gh release edit v1.0.0 --draft=false`.

## 8. After publishing

- On the phone: download the APK from the release page, `sha256sum` (or a checksum app) against the notes, install over the tested one — an update keeps
  the settings.
- `CHANGELOG.md`: the date in the block; a new empty `[Unreleased]`.

## 9. Repository settings (once; approved as commands, `ai-sessions/0065` I-10/I-11)

```bash
# SECURITY.md points to private vulnerability reporting; it is off (checked 2026-10-02: {"enabled":false}):
gh api -X PUT repos/tedsluis/opencontrolpixelbudspro2/private-vulnerability-reporting
# The About box is set; one topic is misspelt ("graphenos"):
gh repo edit tedsluis/opencontrolpixelbudspro2 --remove-topic graphenos --add-topic grapheneos --add-topic android-app
```

## 10. README media

A new screen recording becomes GitHub-ready media with `scripts/readme_media.sh <recording.mp4> images/<name>` (ffmpeg only): an H.264 MP4 without
audio or metadata, checked against GitHub's 10 MB video limit, a short GIF preview and a poster PNG. Look at every frame before committing — the script
does not blur anything.

## Not used (decided `ai-sessions/0065`)

- **Signing in CI:** the key would leave this machine as a repository secret; releases are built and signed locally (checkpoint answer (d) "None now").
- **R8/minify:** the release is the same unminified code the tests and hardware runs checked (`ai-sessions/0066` RESULT §C.5).
- **Key rotation** (`apksigner rotate`, APK Signature Scheme v3) exists for a compromised or weak key; not needed while the key is safe.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/RELEASING.md - https://tedsluis.github.io/opencontrolpixelbudspro2/RELEASING
