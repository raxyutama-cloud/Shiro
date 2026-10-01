#!/usr/bin/env bash
# =============================================================================
# Setup Linux untuk Build Shiro (Tauri + Android)
# =============================================================================
# Jalankan di Ubuntu/Debian/Fedora/Arch
# =============================================================================

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() { echo -e "${BLUE}[INFO]${NC} $*"; }
success() { echo -e "${GREEN}[OK]${NC} $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*"; }

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Detect distro
if [[ -f /etc/os-release ]]; then
  . /etc/os-release
  DISTRO=$ID
  VERSION=$VERSION_ID
else
  error "Cannot detect Linux distribution"
  exit 1
fi

log "Detected: $PRETTY_NAME"
echo "======================================================"
echo "  Shiro Linux Setup (Tauri + Android)"
echo "======================================================"
echo

# 1. Install system dependencies
log "Installing system dependencies..."

case $DISTRO in
  ubuntu|debian|pop|mint|elementary|zorin)
    sudo apt update
    sudo apt install -y \
      libwebkit2gtk-4.1-dev \
      libayatana-appindicator3-dev \
      librsvg2-dev \
      patchelf \
      libssl-dev \
      curl \
      wget \
      file \
      desktop-file-utils \
      pkg-config \
      build-essential \
      python3 \
      git
    ;;
  fedora|rhel|centos|rocky|almalinux)
    sudo dnf install -y \
      webkit2gtk4.1-devel \
      libayatana-appindicator-gtk3-devel \
      librsvg2-devel \
      patchelf \
      openssl-devel \
      curl \
      wget \
      file \
      desktop-file-utils \
      pkgconfig \
      gcc \
      gcc-c++ \
      make \
      python3 \
      git
    ;;
  arch|manjaro|endeavouros|garuda)
    sudo pacman -S --needed --noconfirm \
      webkit2gtk-4.1 \
      libayatana-appindicator \
      librsvg \
      patchelf \
      openssl \
      curl \
      wget \
      file \
      desktop-file-utils \
      pkgconf \
      base-devel \
      python \
      git
    ;;
  opensuse*)
    sudo zypper install -y \
      webkit2gtk-4_1-devel \
      libayatana-appindicator3-devel \
      librsvg2-devel \
      patchelf \
      libopenssl-devel \
      curl \
      wget \
      file \
      desktop-file-utils \
      pkgconfig \
      gcc \
      gcc-c++ \
      make \
      python3 \
      git
    ;;
  *)
    warn "Unknown distro: $DISTRO. Please install manually:"
    echo "  webkit2gtk-4.1, libayatana-appindicator3, librsvg2, patchelf, openssl, build tools"
    ;;
esac

success "System dependencies installed"

# 2. Node.js
log "Checking Node.js..."
if ! command -v node &>/dev/null; then
  log "Installing Node.js via NodeSource..."
  curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
  sudo apt install -y nodejs 2>/dev/null || sudo dnf install -y nodejs 2>/dev/null || sudo pacman -S --noconfirm nodejs 2>/dev/null
else
  success "Node.js: $(node --version)"
fi

# 3. Rust
log "Checking Rust..."
if ! command -v cargo &>/dev/null; then
  log "Installing Rust..."
  curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y
  source "$HOME/.cargo/env"
else
  success "Rust: $(rustc --version)"
fi

# 4. Tauri CLI
log "Checking Tauri CLI..."
if ! cargo install --list | grep -q "tauri-cli"; then
  log "Installing Tauri CLI..."
  cargo install tauri-cli --version ^2
else
  success "Tauri CLI installed"
fi

# 5. Android SDK (optional)
log "Checking Android SDK..."
if [[ -z "${ANDROID_HOME:-}" ]]; then
  warn "ANDROID_HOME not set. For Android builds:"
  echo "  1. Download Android Studio: https://developer.android.com/studio"
  echo "  2. Or install command line tools only"
  echo "  3. Add to ~/.bashrc:"
  echo '     export ANDROID_HOME="$HOME/Android/Sdk"'
  echo '     export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin'
else
  success "ANDROID_HOME: $ANDROID_HOME"
fi

# 6. Sync & verify
log "Syncing Capacitor..."
cd "$PROJECT_ROOT"
npx cap sync 2>&1 | tail -3
success "Capacitor synced"

log "Verifying Tauri build (Debug)..."
cd "$PROJECT_ROOT/src-tauri"
cargo tauri build --debug 2>&1 | tail -5
success "Linux Tauri Debug build OK"

# 7. Desktop entry (optional)
log "Creating desktop entry..."
mkdir -p "$HOME/.local/share/applications"
cat > "$HOME/.local/share/applications/shiro.desktop" << EOF
[Desktop Entry]
Name=Shiro
Comment=Fast media downloader for 16+ platforms
Exec=$PROJECT_ROOT/src-tauri/target/release/shiro
Icon=$PROJECT_ROOT/src-tauri/icons/icon.png
Terminal=false
Type=Application
Categories=Network;FileTransfer;Utility;
StartupNotify=true
EOF
success "Desktop entry created: ~/.local/share/applications/shiro.desktop"

echo
echo "======================================================"
success "Linux Setup Complete!"
echo "======================================================"
echo
echo "Next steps:"
echo "  1. Build release:"
echo "     ./build-all.sh linux release"
echo "     # Output: dist/Shiro_4.4.2_linux.deb + .AppImage"
echo
echo "  2. Install DEB:"
echo "     sudo dpkg -i dist/Shiro_4.4.2_linux.deb"
echo "     # atau AppImage:"
echo "     chmod +x dist/Shiro_4.4.2_linux.AppImage"
echo "     ./dist/Shiro_4.4.2_linux.AppImage"
echo
echo "  3. Android build (if SDK installed):"
echo "     ./build-all.sh android release"
echo
warn "For AppImage distribution: no install needed, just run!"