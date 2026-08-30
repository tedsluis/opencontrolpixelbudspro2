# Prepare Fedora 44 workstation

Here we keep track of the tools needed for this project.

## packages

```bash
# Development tools group install
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ sudo dnf group install -y "Development Tools" "C Development Tools and Libraries" || true

# CLI-tools install
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ sudo dnf install -y \
  git git-lfs gh \
  curl wget \
  jq ripgrep fd-find tree htop tmux \
  unzip zip p7zip \
  gnome-terminal \
  make cmake gcc gcc-c++ \
  dnf-plugins-core
```

## NPM and Node

```bash
# Install packages
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ sudo dnf install nodejs npm

# Install NPM latest
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ sudo npm install -g npm@latest
added 1 package in 4s
15 packages are looking for funding
  run npm fund for details

# Get NMP installation path
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ npm config get prefix 
/usr/local
# Fix permissions to install NPM Gobal without sudo
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ sudo chown -R $(whoami):$(whoami) /usr/local/lib/node_modules /usr/local/bin /usr/local/share

# Permit Global installs
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ npm config set allow-scripts=@anthropic-ai/claude-code,@github/keytar,node-pty --location=user
```

## Install Claude-code

```bash
# Install Claude Code
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ npm install -g @anthropic-ai/claude-code

added 5 packages, removed 36 packages, and changed 4 packages in 11s
npm warn install-scripts 3 packages had install scripts blocked because they are not covered by allowScripts:
npm warn install-scripts   @anthropic-ai/claude-code@2.1.224 (postinstall: node install.cjs)
npm warn install-scripts   @github/keytar@7.10.6 (install: node script/install.js || npm run build)
npm warn install-scripts   node-pty@1.0.0 (install: node-gyp rebuild; postinstall: node scripts/post-install.js)
npm warn install-scripts
changed 9 packages in 12s

# Check Claude Code
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ hash -r
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ claude --version
2.1.224 (Claude Code)
```

## Install Antigravity

```bash
# Add Antigravity repo
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] sudo tee /etc/yum.repos.d/antigravity.repo << EOL
[antigravity-rpm]
name=Antigravity RPM Repository
baseurl=https://us-central1-yum.pkg.dev/projects/antigravity-auto-updater-dev/antigravity-rpm
enabled=1
gpgcheck=0
EOL

# Update the package cache
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] sudo dnf makecache

# Install the package
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] sudo dnf install antigravity -y

antigravity --version
1.107.0
15487b3041e65228cae24980a3f796c905ef582c
x64
```

## Install Java 21 OpenJDK

```bash
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ sudo dnf install -y java-21-openjdk java-21-openjdk-devel
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ sudo alternatives --config java || true
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ echo 'export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))' >> "$HOME/.bashrc"
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ java --version
openjdk 21.0.4 2024-07-16 LTS
OpenJDK Runtime Environment Temurin-21.0.4+7 (build 21.0.4+7-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.4+7 (build 21.0.4+7-LTS, mixed mode, sharing)

```

## Python

```bash
$ python --version
Python 3.14.6
```

## Git settings

```bash
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ git config --global init.defaultBranch main
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ git config --global pull.rebase false
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ git config --global core.editor "code --wait"
```

## Install VSCode extentions

```bash
VSCODE_EXTENSIONS=(
  redhat.vscode-yaml
  esbenp.prettier-vscode
  DavidAnson.vscode-markdownlint
  ms-vscode.cpptools
  vscjava.vscode-java-pack
  fwcd.kotlin
  eamodio.gitlens
  ms-python.python
  ms-python.vscode-pylance
  redhat.vscode-xml
)
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ for ext in "${VSCODE_EXTENSIONS[@]}"; do
  code-insiders --install-extension "$ext" || warn "Can not install extention $ext"
done
```

## Install Wireshark

```bash
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ sudo dnf install -y wireshark wireshark-cli
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ sudo groupadd -f wireshark
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ sudo usermod -aG wireshark,dialout "$USER"
```

## Install Android SDK / adb / logcat

```bash
# Android tools
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ sudo dnf install -y android-tools

# Android Studion
fedora ~/git/opencontrolpixelbudspro2 [main L|✔] $ flatpak install -y flathub com.google.AndroidStudio 
```

## Install Kotlin using SDKMAN

```bash
if [[ ! -d "$HOME/.sdkman" ]]; then
  curl -s "https://get.sdkman.io" | bash
fi
set +u
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install kotlin || warn "Kotlin install using sdkman was skipped or failed."
set -u
```

## Cross-validation between AI models (maintainer strategy)

This is a **maintainer-executed** workflow, not something a single AI agent
session can do to itself — it requires orchestrating two independent model
sessions/tools and comparing their output, which is why it lives here rather
than as a directive in `AGENTS.md` (moved from that document's former §14,
2026-08-15). Two installed tools are available for this: Claude Code and
Antigravity (see above).

When a protocol hypothesis is significant enough to implement against:

1. Have one model/session summarize the hypothesis and its evidence.
2. Give the same evidence (without the conclusion) to a second model, or to a
   fresh session of the same model — e.g. Claude Code vs. Antigravity, or two
   independent Claude Code sessions with cleared context.
3. Compare the two independent interpretations yourself.
4. On agreement: raise confidence, but still mark the finding as HYPOTHESIS
   until a physical experiment (logged in the relevant `CAP-NNN-FINDINGS.md`)
   confirms it — agreement between two AI readings of the same bytes is not
   itself proof, since both may share the same training-data blind spot.
5. On disagreement: do not have either model "resolve" it automatically —
   treat the discrepancy itself as a signal that more evidence (a distinguishing
   capture/experiment) is needed before promoting anything.

## Reverse engineering tools: JADX, apktool, pbtk

```bash
sudo dnf install -y apktool || warn "apktool not in repo, install manual: https://apktool.org/docs/install"

JADX_VERSION="1.5.1"
JADX_DIR="$HOME/tools/jadx"
if [[ ! -d "$JADX_DIR" ]]; then
  log "JADX $JADX_VERSION download and install in $JADX_DIR..."
  mkdir -p "$JADX_DIR"
  TMPZIP=$(mktemp)
  curl -L -o "$TMPZIP" "https://github.com/skylot/jadx/releases/download/v${JADX_VERSION}/jadx-${JADX_VERSION}.zip"
  unzip -q -o "$TMPZIP" -d "$JADX_DIR"
  rm -f "$TMPZIP"
  echo "export PATH=\"\$PATH:$JADX_DIR/bin\"" >> "$HOME/.bashrc"
else
  log "JADX al installed in $JADX_DIR, skipped."
fi
```

### pbtk (Protobuf toolkit — `.proto` schema extraction)

Confirmed against pbtk's own README (`github.com/marin-m/pbtk`, checked 2026-08-30) rather than
assumed. **Real scope, corrected from an earlier working assumption:** pbtk ships two separate
extractors relevant here, not one — `pbtk-jar-extract` for Java-runtime protobuf (base/Lite/Nano/
Micro/J2ME, i.e. DEX/APK-embedded classes), **and** `pbtk-from-binary` for "binaries containing
embedded reflection metadata (typically C++, sometimes Java and most other bindings)," which the
upstream README states "still works well" as of 2026. So native `.so` extraction is **not**
ruled out the way this project first assumed — whether it actually works against
`libmaestro`/`libgfps`'s specific `.so` files depends on whether those binaries retain their
protobuf descriptor pool (full protobuf runtimes typically do; protobuf-lite builds typically strip
it to save size) — this is an ⚪ ASSUMPTION until tried against a real extracted `.so`, not a known
fact either way. Per `DECISIONS.md` ADR-017, running either extractor and explaining its output is
in scope for AI mechanical assistance; deciding what a resulting `.proto`/candidate struct *means*
for the wire protocol stays the maintainer's call.

**Real dependencies** (per the upstream README, not assumed): Python ≥ 3.10, PySide 6, Python
Protobuf 3, and a handful of external executables (`chromium`, `jad`, `dex2jar`) used by some
extractor scripts. Debian/Ubuntu's own documented apt line is `python3-pip git openjdk-8-jre
python3-qtpy-pyside6`; Fedora has no native `pbtk` package (no snap by default, and the upstream AUR
package is Arch-specific), so the upstream-recommended `pipx`/`uv` path — which pulls PySide6 and
python-protobuf into an isolated environment itself — is used instead:

```bash
# Runtime prerequisites pbtk itself doesn't vendor (Java 21 already installed above satisfies
# pbtk's JRE dependency; jad/dex2jar aren't in Fedora's repos — install manually only if a
# specific extractor script reports them missing, per pbtk's own runtime warning).
sudo dnf install -y python3-pip pipx || warn "python3-pip/pipx install failed, check repo availability"
pipx ensurepath

# Install pbtk itself (pulls PySide6 + python-protobuf into its own isolated pipx venv)
pipx install pbtk
hash -r

# Verify — launches the GUI, or use the two extractor scripts directly (no GUI required):
pbtk --version 2>/dev/null || pbtk-jar-extract -h
pbtk-from-binary -h
```

Extracted `.proto` output lands in `~/.pbtk/protos/<APK name>/` by default (per pbtk's own
documented local-storage convention) — copy the relevant files into this project's
`reverse-engineering/apk/v<versionName>-<versionCode>/pbtk-output/` (see `reverse-engineering/APK_VERSIONS.md`, not
committed to git — see `.gitignore`) rather than leaving them only in pbtk's own home-directory
cache, so a given APK version's extraction output stays associated with that version.

## Disaster Recovery

See `README.md`'s bricking disclaimer — this project sends undocumented
commands to real hardware, and a malformed or unexpected one could leave the
Buds/case in a bad state (e.g. unresponsive to normal pairing, stuck in an
unexpected mode). Know this procedure **before** running any experimental
write command against real hardware, not after something goes wrong.

**Hardware factory reset (confirmed, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
`CASE-007`):**

1. Place both buds in the case.
2. Open the case lid, and keep the case connected to power (plugged in).
3. Press and hold the case button for **30 seconds**.
4. This performs a **full factory reset** — confirmed via official support
   documentation and reproduced on-the-wire in `CAP-001`/`CAP-002`
   (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group P #16). It also resets the Find My
   Device link on the Pro 2.

**What this does and doesn't recover from:**

- Recovers from: a bad pairing/bonding state, a stuck connection, the Buds
  not responding to the phone in a way normal reconnect doesn't fix — this
  is the standard, Google-documented "starting over" mechanism.
- Does **not** guarantee recovery from a genuinely corrupted firmware state
  or a malformed command that put the hardware into an undocumented failure
  mode — there is no lower-level recovery mechanism known to this project
  (no documented DFU/recovery-mode procedure). If a 30-second reset doesn't
  restore normal behavior, treat the hardware as potentially bricked and stop
  sending further experimental commands to it.
- This is destructive: it clears pairing state and Find My Device linkage.
  Don't use it casually — it's the last resort referenced by `README.md`'s
  disclaimer and `ARCHITECTURE.md` §8.1's Safe Mode design, not a routine
  troubleshooting step.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/WORKSTATION_PREPARATIONS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/WORKSTATION_PREPARATIONS
