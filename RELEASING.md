# RELEASING.md — How to publish a release of OpenControl for Pixel Buds Pro 2

The app is distributed manually, as a signed APK on this repository's GitHub Releases page — no app store, and no update check in the app (`DECISIONS.md`
ADR-004). This runbook was written in `ai-sessions/0065` (2026-10-02); every step is the maintainer's own act. An AI session may prepare files and print
commands, but it never runs a publishing step (tag push, release, upload, repository setting) without an explicit "yes" in chat for that step, and it never
sees the keystore or its passwords.

**Why the key matters** (developer.android.com, "Sign your app", fetched 2026-10-02): *"When the system is installing an update to an app, it compares the
certificate(s) in the new version with those in the existing version. The system allows the update if the certificates match."* and *"If you lose your
app's signing key, you lose the ability to update your app."* So: one key for the life of the app, backed up, never in this repository.

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
2. **`CHANGELOG.md`**: the version's block, dated the day you publish. **`README.md`**: the status line.
3. Commit, push, and wait for CI (`.github/workflows/android.yml`) to be green.

## 5. Each release: build and verify

```bash
scripts/release.sh 1.0.0
```

It checks the version scheme and that the commit is on `origin`, builds in a separate clean worktree (so the Info tab shows the commit's hash without
"-dirty"), verifies the signature (`apksigner verify --print-certs`), the version and that there is no `INTERNET` permission, and writes `dist/1.0.0/`: the APK
(`opencontrol-pixelbudspro2-1.0.0.apk`), its `.sha256`, `THIRD_PARTY_NOTICES.txt` (the bundled libraries and their licences, `scripts/third_party_notices.py`)
and `release-notes.md` (from `scripts/release_notes.template`, checksums filled in). `dist/` is gitignored. It prints the next commands and runs none.

Check: the printed certificate SHA-256 equals your noted fingerprint; read `release-notes.md`.

## 6. Each release: test this exact APK

Install `dist/<version>/opencontrol-pixelbudspro2-<version>.apk` on the phone and run the planned hardware capture on it (for 1.0.0: `CAP-067`, in a
GrapheneOS secondary user without sandboxed Google Play — the route is in `ai-sessions/0066` RESULT §C.1; that run is also the evidence for `PROJECT.md`'s
Definition of done). On the Info tab: version and hash (no "-dirty"). A debug build installed before must be uninstalled first (different key).

## 7. Each release: publish (outward-facing — no way back once public)

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
