#!/usr/bin/env bash
# =============================================================================
# Setup Windows untuk Build Shiro (Tauri + Android)
# =============================================================================
# Jalankan di Git Bash / WSL / PowerShell (bash)
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

echo "======================================================"
echo "  Shiro Windows Setup (Tauri + Android)"
echo "======================================================"
echo

# Detect shell
if [[ -n "${MSYSTEM:-}" ]] || [[ -n "${WSL_DISTRO_NAME:-}" ]]; then
  log "Detected: ${MSYSTEM:-WSL}"
else
  warn "Recommended: Run in Git Bash or WSL"
fi

# 1. Chocolatey (package manager)
log "Checking Chocolatey..."
if ! command -v choco &>/dev/null; then
  warn "Chocolatey not found. Install from https://chocolatey.org/install"
  warn "Then re-run this script in Admin PowerShell:"
  echo '  Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString("https://community.chocolatey.org/install.ps1"))'
else
  success "Chocolatey: $(choco --version)"
fi

# 2. Node.js
log "Checking Node.js..."
if ! command -v node &>/dev/null; then
  log "Installing Node.js via Chocolatey..."
  choco install nodejs-lts -y --no-progress
  refreshenv
else
  success "Node.js: $(node --version)"
fi

# 3. Rust
log "Checking Rust..."
if ! command -v cargo &>/dev/null; then
  log "Installing Rust via Chocolatey..."
  choco install rust -y --no-progress
  refreshenv
else
  success "Rust: $(rustc --version)"
fi

# 4. Rust MSVC target
log "Adding MSVC target..."
rustup target add x86_64-pc-windows-msvc
success "Target x86_64-pc-windows-msvc added"

# 5. Tauri CLI
log "Checking Tauri CLI..."
if ! cargo install --list | grep -q "tauri-cli"; then
  log "Installing Tauri CLI..."
  cargo install tauri-cli --version ^2
else
  success "Tauri CLI installed"
fi

# 6. Visual Studio Build Tools (required for Tauri)
log "Checking Visual Studio Build Tools..."
if ! command -v cl.exe &>/dev/null; then
  warn "Visual Studio Build Tools not found."
  log "Install via Chocolatey (Admin):"
  echo "  choco install visualstudio2022buildtools -y --no-progress"
  echo "  # Pilih workload: 'Desktop development with C++'"
  echo "  # Atau install Visual Studio Community dari installer"
else
  success "MSVC compiler found"
fi

# 7. WebView2 Runtime
log "Checking WebView2 Runtime..."
if ! reg query "HKLM\SOFTWARE\WOW6432Node\Microsoft\EdgeUpdate\Clients\{F3017226-FE2A-4295-8BDF-00C3A9A7E4C5}" &>/dev/null; then
  log "Installing WebView2 Runtime..."
  choco install microsoft-edge-webview2-runtime -y --no-progress
else
  success "WebView2 Runtime installed"
fi

# 8. Android Studio / SDK (optional, for Android builds)
log "Checking Android SDK..."
if [[ -z "${ANDROID_HOME:-}" ]]; then
  warn "ANDROID_HOME not set. For Android builds:"
  echo "  1. Install Android Studio: choco install androidstudio -y"
  echo "  2. Set ANDROID_HOME in ~/.bashrc:"
  echo '     export ANDROID_HOME="$HOME/AppData/Local/Android/Sdk"'
  echo '     export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin'
else
  success "ANDROID_HOME: $ANDROID_HOME"
fi

# 9. Capacitor CLI
log "Checking Capacitor CLI..."
if ! command -v npx &>/dev/null; then
  error "npx not found (Node.js issue)"
else
  success "npx available"
fi

# 10. Sync & verify Tauri build
log "Syncing Capacitor..."
cd "$PROJECT_ROOT"
npx cap sync 2>&1 | tail -3
success "Capacitor synced"

log "Verifying Tauri build (Debug)..."
cd "$PROJECT_ROOT/src-tauri"
cargo tauri build --target x86_64-pc-windows-msvc --debug 2>&1 | tail -5
success "Windows Tauri Debug build OK"

echo
echo "======================================================"
success "Windows Setup Complete!"
echo "======================================================"
echo
echo "Next steps:"
echo "  1. Build release MSI:"
echo "     ./build-all.sh windows release"
echo
echo "  2. For Code Signing (optional, avoid SmartScreen):"
echo "     \$env:CSC_LINK=\"path/to/cert.p12\""
echo "     \$env:CSC_KEY_PASSWORD=\"password\""
echo "     ./build-all.sh windows release"
echo
echo "  3. Android build (if SDK installed):"
echo "     ./build-all.sh android release"
echo
warn "Certificates: simpan di password manager!"