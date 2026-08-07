# Prepare Fedora 44 workstation

Here we keep track of the tools needed for this project.

## packages

```bash
# Development tools group install
fedora ~/git/pixelbudspro2control [main L|✔] $ sudo dnf group install -y "Development Tools" "C Development Tools and Libraries" || true

# CLI-tools install
fedora ~/git/pixelbudspro2control [main L|✔] $ sudo dnf install -y \
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
fedora ~/git/pixelbudspro2control [main L|✔] $ sudo dnf install nodejs npm

# Install NPM latest
fedora ~/git/pixelbudspro2control [main L|✔] $ sudo npm install -g npm@latest
added 1 package in 4s
15 packages are looking for funding
  run npm fund for details

# Get NMP installation path
fedora ~/git/pixelbudspro2control [main L|✔] $ npm config get prefix 
/usr/local
# Fix permissions to install NPM Gobal without sudo
fedora ~/git/pixelbudspro2control [main L|✔] $ sudo chown -R $(whoami):$(whoami) /usr/local/lib/node_modules /usr/local/bin /usr/local/share

# Permit Global installs
fedora ~/git/pixelbudspro2control [main L|✔] $ npm config set allow-scripts=@anthropic-ai/claude-code,@github/keytar,node-pty --location=user
```

## Install Claude-code

```bash
# Install Claude Code
fedora ~/git/pixelbudspro2control [main L|✔] $ npm install -g @anthropic-ai/claude-code

added 5 packages, removed 36 packages, and changed 4 packages in 11s
npm warn install-scripts 3 packages had install scripts blocked because they are not covered by allowScripts:
npm warn install-scripts   @anthropic-ai/claude-code@2.1.224 (postinstall: node install.cjs)
npm warn install-scripts   @github/keytar@7.10.6 (install: node script/install.js || npm run build)
npm warn install-scripts   node-pty@1.0.0 (install: node-gyp rebuild; postinstall: node scripts/post-install.js)
npm warn install-scripts
changed 9 packages in 12s

# Check Claude Code
fedora ~/git/pixelbudspro2control [main L|✔] $ hash -r
fedora ~/git/pixelbudspro2control [main L|✔] $ claude --version
2.1.224 (Claude Code)
```

## Install Antigravity

```bash
# Add Antigravity repo
fedora ~/git/pixelbudspro2control [main L|✔] sudo tee /etc/yum.repos.d/antigravity.repo << EOL
[antigravity-rpm]
name=Antigravity RPM Repository
baseurl=https://us-central1-yum.pkg.dev/projects/antigravity-auto-updater-dev/antigravity-rpm
enabled=1
gpgcheck=0
EOL

# Update the package cache
fedora ~/git/pixelbudspro2control [main L|✔] sudo dnf makecache

# Install the package
fedora ~/git/pixelbudspro2control [main L|✔] sudo dnf install antigravity -y

antigravity --version
1.107.0
15487b3041e65228cae24980a3f796c905ef582c
x64
```

## Install Java 21 OpenJDK

```bash
fedora ~/git/pixelbudspro2control [main L|✔] $ sudo dnf install -y java-21-openjdk java-21-openjdk-devel
fedora ~/git/pixelbudspro2control [main L|✔] $ sudo alternatives --config java || true
fedora ~/git/pixelbudspro2control [main L|✔] $ echo 'export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))' >> "$HOME/.bashrc"
fedora ~/git/pixelbudspro2control [main L|✔] $ java --version
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
fedora ~/git/pixelbudspro2control [main L|✔] $ git config --global init.defaultBranch main
fedora ~/git/pixelbudspro2control [main L|✔] $ git config --global pull.rebase false
fedora ~/git/pixelbudspro2control [main L|✔] $ git config --global core.editor "code --wait
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
fedora ~/git/pixelbudspro2control [main L|✔] $ for ext in "${VSCODE_EXTENSIONS[@]}"; do
  code-insiders --install-extension "$ext" || warn "Can not install extention $ext"
done
```

## Install Wireshark

```bash
fedora ~/git/pixelbudspro2control [main L|✔] $ sudo dnf install -y wireshark wireshark-cli
fedora ~/git/pixelbudspro2control [main L|✔] $ sudo groupadd -f wireshark
fedora ~/git/pixelbudspro2control [main L|✔] $ sudo usermod -aG wireshark,dialout "$USER"
```

## Install Android SDK / adb / logcat

```bash
# Android tools
fedora ~/git/pixelbudspro2control [main L|✔] $ sudo dnf install -y android-tools

# Android Studion
fedora ~/git/pixelbudspro2control [main L|✔] $ flatpak install -y flathub com.google.AndroidStudio 
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

## Reverse engineering tools: JADX, apktool

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