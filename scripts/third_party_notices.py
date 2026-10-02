#!/usr/bin/env python3
"""Writes the third-party notices for the release APK (ai-sessions/0065, idea I-12, approved by the maintainer in chat 2026-10-02).

The APK bundles open-source libraries (AndroidX, Jetpack Compose, Kotlin, kotlinx.coroutines, Dagger/Hilt, ...). Most are Apache-2.0, whose section 4
asks a redistributor to "give any other recipients of the Work or Derivative Works a copy of this License" (4a) and to include the attribution notices
of a library's NOTICE file (4d). This script lists every library of the release runtime classpath with the licence its Maven POM declares, adds the
NOTICE files found inside the APK, and appends the full text of every licence that has a file under `LICENSES/` (e.g. `LICENSES/Apache-2.0.txt`).

The dependency list comes from Gradle (`--offline`), the licences from the POMs in the local Gradle cache (`~/.gradle/caches/modules-2/files-2.1`),
following `<parent>` POMs when a POM declares no licence. Gradle does not always keep a POM there (it may hold only the `.aar`/`.jar`); with `--fetch`
the script downloads just those missing POMs — from Google's Maven repository, else Maven Central — into `~/.cache/opencontrol-poms/` (a developer
tool on the maintainer's machine; the app itself never touches the network). A library whose licence still cannot be found is listed as "licence not
found in the POM" — never guessed — and the script exits 1 so the release script stops.

    python3 scripts/third_party_notices.py [--fetch] <release.apk> <output.txt>
"""
import re
import subprocess
import urllib.request
import sys
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent
CACHE = Path.home() / ".gradle/caches/modules-2/files-2.1"
# Licence names as POMs spell them -> the file under LICENSES/ with the full text.
LICENCE_FILES = {
    "apache-2.0": "Apache-2.0.txt",
    "the apache software license, version 2.0": "Apache-2.0.txt",
    "the apache license, version 2.0": "Apache-2.0.txt",
    "apache license, version 2.0": "Apache-2.0.txt",
    "apache license 2.0": "Apache-2.0.txt",
    "apache 2.0": "Apache-2.0.txt",
}
COORD = re.compile(r"[+\\]--- ([\w.\-]+):([\w.\-]+):(\S+)(?: -> (\S+))?")


def runtime_libraries() -> list[tuple[str, str, str]]:
    out = subprocess.run(
        ["./gradlew", "--offline", "-q", ":app:dependencies", "--configuration", "releaseRuntimeClasspath"],
        cwd=REPO / "android", capture_output=True, text=True, check=True,
    ).stdout
    libs = set()
    for line in out.splitlines():
        if line.rstrip().endswith("(c)") or line.rstrip().endswith("(n)"):
            continue  # a constraint or an unresolved node, not a library on the classpath
        m = COORD.search(line)
        if m:
            group, artifact, version, resolved = m.groups()
            libs.add((group, artifact, resolved or version))
    return sorted(libs)


FETCHED = Path.home() / ".cache/opencontrol-poms"
REPOSITORIES = ("https://dl.google.com/android/maven2", "https://repo1.maven.org/maven2")
FETCH = False


def pom_path(group: str, artifact: str, version: str) -> Path | None:
    found = sorted((CACHE / group / artifact / version).glob(f"*/{artifact}-{version}.pom"))
    if found:
        return found[0]
    local = FETCHED / group / f"{artifact}-{version}.pom"
    if local.exists():
        return local
    if not FETCH:
        return None
    for repo in REPOSITORIES:
        url = f"{repo}/{group.replace('.', '/')}/{artifact}/{version}/{artifact}-{version}.pom"
        try:
            with urllib.request.urlopen(url, timeout=30) as response:
                data = response.read()
        except OSError:
            continue
        local.parent.mkdir(parents=True, exist_ok=True)
        local.write_bytes(data)
        return local
    return None


def pom_info(group: str, artifact: str, version: str, depth: int = 0) -> tuple[str | None, list[tuple[str, str]]]:
    path = pom_path(group, artifact, version)
    if path is None or depth > 5:
        return None, []
    root = ET.parse(path).getroot()
    for element in root.iter():  # some POMs (e.g. javax.inject:1) carry no Maven namespace: read every one without it
        element.tag = element.tag.split("}")[-1]
    name = root.findtext("name", default=None)
    licences = [
        ((lic.findtext("name", default="") or "").strip(), (lic.findtext("url", default="") or "").strip())
        for lic in root.findall("licenses/license")
    ]
    if not licences:
        parent = root.find("parent")
        if parent is not None:
            p = [parent.findtext(k, default="") for k in ("groupId", "artifactId", "version")]
            pname, licences = pom_info(*p, depth=depth + 1)
            name = name or pname
    return name, licences


def main() -> int:
    global FETCH
    args = sys.argv[1:]
    if args[:1] == ["--fetch"]:
        FETCH, args = True, args[1:]
    if len(args) != 2:
        print(__doc__.strip().splitlines()[-1].strip(), file=sys.stderr)
        return 2
    apk, output = Path(args[0]), Path(args[1])
    lines = [
        "Third-party notices — OpenControl for Pixel Buds Pro 2",
        "",
        "This app is licensed under the GNU Affero General Public License v3.0 or later (see LICENSE in the source repository and Settings → Info in the app).",
        "It includes the following open-source libraries, each under its own licence as declared in its Maven POM:",
        "",
    ]
    texts, missing = set(), []
    for group, artifact, version in runtime_libraries():
        name, licences = pom_info(group, artifact, version)
        lines.append(f"- {group}:{artifact}:{version}" + (f" ({name})" if name else ""))
        if not licences:
            missing.append(f"{group}:{artifact}:{version}")
            lines.append("    licence not found in the POM")
        for lic_name, lic_url in licences:
            lines.append(f"    {lic_name}" + (f" — {lic_url}" if lic_url else ""))
            if lic_name.lower() in LICENCE_FILES:
                texts.add(LICENCE_FILES[lic_name.lower()])
    with zipfile.ZipFile(apk) as z:
        for entry in sorted(n for n in z.namelist() if re.search(r"(^|/)NOTICE(\.\w+)?$", n, re.I)):
            lines += ["", f"===== {entry} (from the APK) =====", "", z.read(entry).decode("utf-8", "replace").rstrip()]
    for text in sorted(texts):
        lines += ["", f"===== LICENSES/{text} =====", "", (REPO / "LICENSES" / text).read_text().rstrip()]
    output.write_text("\n".join(lines) + "\n")
    print(f"{output}: {sum(1 for l in lines if l.startswith('- '))} libraries, licence texts {sorted(texts)}")
    if missing:
        print("licence not found for: " + ", ".join(missing), file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
