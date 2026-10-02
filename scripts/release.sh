#!/usr/bin/env bash
# Builds and verifies a release of OpenControl for Pixel Buds Pro 2 — and publishes nothing (ai-sessions/0065, idea I-5; RELEASING.md has the full runbook).
#
#   scripts/release.sh <version> [commit]        e.g. scripts/release.sh 1.0.0      (commit defaults to HEAD)
#
# What it does, in order (it stops at the first failure):
#   1. The commit's android/app/build.gradle.kts must say versionName = "<version>" and versionCode = major*10000 + minor*100 + patch (a "-rc.N" version:
#      the code of the version before it, e.g. 1.0.0-rc.1 -> 9901). The commit must be on origin (so the tag you push points at a published commit).
#   2. A separate git worktree at that commit (always clean: the Info tab shows its hash without "-dirty"); your working copy is not touched.
#   3. ./gradlew --offline clean assembleRelease there. The signing values come from ~/.gradle/gradle.properties or the environment (OPENCONTROL_STORE_FILE,
#      OPENCONTROL_KEY_ALIAS, OPENCONTROL_STORE_PASSWORD, OPENCONTROL_KEY_PASSWORD); this script never reads, prints or asks for them.
#   4. Verifies: apksigner verify (v2 or v3 signature), the package's versionName/versionCode, no INTERNET permission.
#   5. Writes dist/<version>/: the APK as opencontrol-pixelbudspro2-<version>.apk, its .sha256, THIRD_PARTY_NOTICES.txt and release-notes.md (from
#      scripts/release_notes.template, with the checksums filled in).
#   6. Prints the tag and `gh release create` commands — it never runs them: publishing is the maintainer's act.
set -euo pipefail

if [[ $# -lt 1 || $# -gt 2 ]]; then
    echo "usage: $0 <version> [commit]" >&2
    exit 2
fi
version="$1"
commit="$(git rev-parse --verify "${2:-HEAD}^{commit}")"
tag="v$version"
repo="$(git rev-parse --show-toplevel)"
dist="$repo/dist/$version"
apk_name="opencontrol-pixelbudspro2-$version.apk"
fail() { echo "FAIL: $*" >&2; exit 1; }

[[ "$version" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)(-rc\.([0-9]+))?$ ]] || fail "version '$version' is not X.Y.Z or X.Y.Z-rc.N"
major="${BASH_REMATCH[1]}" minor="${BASH_REMATCH[2]}" patch="${BASH_REMATCH[3]}" rc="${BASH_REMATCH[5]:-}"
code=$((major * 10000 + minor * 100 + patch))
[[ -n "$rc" ]] && code=$((code - 100 + rc)) # 1.0.0-rc.1 -> 9901 (below 1.0.0's 10000)

# 1. The commit's version and its presence on origin.
gradle_file="$(git show "$commit:android/app/build.gradle.kts")"
grep -q "versionName = \"$version\"" <<<"$gradle_file" || fail "versionName in $commit is not \"$version\""
grep -q "versionCode = $code\b" <<<"$gradle_file" || fail "versionCode in $commit is not $code (the scheme for $version)"
git fetch --quiet origin
git branch -r --contains "$commit" | grep -q . || fail "commit $commit is not on origin yet — push it first"
if git rev-parse -q --verify "refs/tags/$tag" >/dev/null; then
    [[ "$(git rev-parse "$tag^{commit}")" == "$commit" ]] || fail "tag $tag exists and points elsewhere"
fi

# Tools from the Android SDK (ANDROID_HOME, else android/local.properties' sdk.dir, else ~/Android/Sdk).
sdk="${ANDROID_HOME:-$(sed -n 's/^sdk.dir=//p' "$repo/android/local.properties" 2>/dev/null || true)}"
sdk="${sdk:-$HOME/Android/Sdk}"
build_tools="$(ls -d "$sdk"/build-tools/* 2>/dev/null | sort -V | tail -1)"
[[ -x "$build_tools/apksigner" && -x "$build_tools/aapt2" ]] || fail "apksigner/aapt2 not found under $sdk/build-tools"

# 2-3. Build in a separate, clean worktree.
work="$(mktemp -d "${TMPDIR:-/tmp}/ocp-release-XXXXXX")"
cleanup() { git -C "$repo" worktree remove --force "$work/tree" >/dev/null 2>&1 || true; rm -rf "$work"; }
trap cleanup EXIT
# GIT_LFS_SKIP_SMUDGE: the build needs no capture file; without it the checkout would download every LFS capture log (found in the 0065 test).
GIT_LFS_SKIP_SMUDGE=1 git worktree add --quiet --detach "$work/tree" "$commit"
[[ -z "$(git -C "$work/tree" status --porcelain --untracked-files=no)" ]] || fail "the worktree is not clean"
cp "$repo/android/local.properties" "$work/tree/android/" 2>/dev/null || true # the SDK path only (gitignored, holds no secret)
(cd "$work/tree/android" && ./gradlew --offline --quiet clean assembleRelease)
apk="$work/tree/android/app/build/outputs/apk/release/app-release.apk"
[[ -f "$apk" ]] || fail "no signed release APK was built"

# 4. Verify.
certs="$("$build_tools/apksigner" verify --verbose --print-certs "$apk")" || fail "apksigner verify failed"
grep -Eq "Verified using v(2|3) scheme.*: true" <<<"$certs" || fail "no v2/v3 signature"
[[ "$(grep -c '^Signer #[0-9]* certificate DN' <<<"$certs")" == 1 ]] || fail "expected exactly one signer"
cert_sha256="$(sed -n 's/^Signer #1 certificate SHA-256 digest: //p' <<<"$certs")"
badging="$("$build_tools/aapt2" dump badging "$apk")"
grep -q "versionCode='$code' versionName='$version'" <<<"$badging" || fail "the APK's version is not $version ($code)"
grep -q "uses-permission: name='android.permission.BLUETOOTH_CONNECT'" <<<"$badging" || fail "positive control failed: BLUETOOTH_CONNECT not listed"
if grep -q "android.permission.INTERNET" <<<"$badging"; then fail "the APK requests INTERNET (AGENTS.md §1)"; fi

# 5. dist/<version>/.
rm -rf "$dist" && mkdir -p "$dist"
cp "$apk" "$dist/$apk_name"
(cd "$dist" && sha256sum "$apk_name" > "$apk_name.sha256")
apk_sha256="$(cut -d' ' -f1 "$dist/$apk_name.sha256")"
python3 "$repo/scripts/third_party_notices.py" --fetch "$apk" "$dist/THIRD_PARTY_NOTICES.txt"
sed -e "s/{APK}/$apk_name/g" -e "s/{APK_SHA256}/$apk_sha256/g" -e "s/{CERT_SHA256}/$cert_sha256/g" -e "s/{TAG}/$tag/g" \
    "$repo/scripts/release_notes.template" > "$dist/release-notes.md"

# 6. The commands — printed, never run.
prerelease=""
[[ -n "$rc" ]] && prerelease=" --prerelease"
cat <<EOF

Built and verified: $dist/$apk_name
  versionName $version, versionCode $code, commit $(git rev-parse --short "$commit")
  APK SHA-256:         $apk_sha256
  certificate SHA-256: $cert_sha256   <- must equal the fingerprint you noted when you created the key

Next (RELEASING.md) — test this exact APK on the phone first, then, only when you decide to publish:
  git tag -s $tag $commit -m "OpenControl for Pixel Buds Pro 2 $version"
  git push origin $tag
  gh release create $tag --verify-tag --draft$prerelease --title "OpenControl for Pixel Buds Pro 2 $version" \\
      --notes-file "$dist/release-notes.md" "$dist/$apk_name" "$dist/$apk_name.sha256" "$dist/THIRD_PARTY_NOTICES.txt"
  # check the draft on github.com, then:
  gh release edit $tag --draft=false
EOF
