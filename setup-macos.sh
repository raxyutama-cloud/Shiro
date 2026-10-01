#!/usr/bin/env bash
# =============================================================================
# Setup macOS untuk Build & Deploy Shiro (iOS + macOS)
# =============================================================================
# Jalankan di Mac: ./setup-macos.sh
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
echo "  Shiro macOS Setup (iOS + macOS Tauri)"
echo "======================================================"
echo

# 1. Xcode Command Line Tools
log "Checking Xcode Command Line Tools..."
if ! xcode-select -p &>/dev/null; then
  log "Installing Xcode Command Line Tools..."
  xcode-select --install
  warn "Please complete the installation in the popup, then re-run this script."
  exit 1
else
  success "Xcode Command Line Tools: $(xcode-select -p)"
fi

# 2. Xcode
log "Checking Xcode..."
if ! command -v xcodebuild &>/dev/null; then
  error "Xcode not found. Install from App Store first."
  exit 1
else
  XCODE_VER=$(xcodebuild -version | head -1)
  success "Xcode: $XCODE_VER"
fi

# 3. CocoaPods
log "Checking CocoaPods..."
if ! command -v pod &>/dev/null; then
  log "Installing CocoaPods..."
  sudo gem install cocoapods
else
  success "CocoaPods: $(pod --version)"
fi

# 4. Rust
log "Checking Rust..."
if ! command -v cargo &>/dev/null; then
  log "Installing Rust..."
  curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y
  source "$HOME/.cargo/env"
else
  success "Rust: $(rustc --version)"
fi

# 5. Rust targets
log "Adding Rust targets..."
rustup target add universal-apple-darwin 2>/dev/null || rustup target add aarch64-apple-darwin
rustup target add x86_64-apple-darwin
success "Targets added: universal-apple-darwin, aarch64-apple-darwin, x86_64-apple-darwin"

# 6. Tauri CLI
log "Checking Tauri CLI..."
if ! cargo install --list | grep -q "tauri-cli"; then
  log "Installing Tauri CLI..."
  cargo install tauri-cli --version ^2
else
  success "Tauri CLI installed"
fi

# 7. Fastlane
log "Checking Fastlane..."
if ! command -v fastlane &>/dev/null; then
  log "Installing Fastlane via bundler..."
  cd "$PROJECT_ROOT"
  bundle install
else
  success "Fastlane: $(fastlane --version)"
fi

# 8. Xcode project dependencies
log "Installing CocoaPods dependencies..."
cd "$PROJECT_ROOT/ios/App"
pod install --repo-update
success "Pods installed"

# 9. Capacitor sync
log "Syncing Capacitor..."
cd "$PROJECT_ROOT"
npx cap sync ios
success "Capacitor synced"

# 10. Verify iOS build works
log "Verifying iOS build (Debug, Simulator)..."
cd "$PROJECT_ROOT/ios/App"
xcodebuild -workspace App.xcworkspace \
  -scheme App \
  -configuration Debug \
  -destination 'platform=iOS Simulator,name=iPhone 15' \
  -quiet \
  build 2>&1 | tail -5
success "iOS Debug build OK"

# 11. Verify macOS Tauri build works
log "Verifying macOS Tauri build (Debug)..."
cd "$PROJECT_ROOT/src-tauri"
cargo tauri build --target universal-apple-darwin --debug 2>&1 | tail -5
success "macOS Tauri Debug build OK"

# 12. Generate app icons if needed
log "Checking app icons..."
if [[ ! -f "$PROJECT_ROOT/src-tauri/icons/icon.icns" ]]; then
  warn "icon.icns not found. Generate from 1024x1024 PNG:"
  echo "  sips -s format icns icon.png --out src-tauri/icons/icon.icns"
fi

echo
echo "======================================================"
success "macOS Setup Complete!"
echo "======================================================"
echo
echo "Next steps:"
echo "  1. Setup Apple Developer certificates (match):"
echo "     cd $PROJECT_ROOT"
echo "     bundle exec fastlane ios setup_match"
echo
echo "  2. Build for TestFlight:"
echo "     bundle exec fastlane ios beta"
echo
echo "  3. Build macOS DMG (release):"
echo "     ./build-all.sh macos release"
echo
echo "  4. For notarization, set these env vars:"
echo "     export CSC_LINK=\"path/to/DeveloperID_application.p12\""
echo "     export CSC_KEY_PASSWORD=\"p12_password\""
echo "     export APPLE_ID=\"your@apple.com\""
echo "     export APPLE_PASSWORD=\"app-specific-password\""
echo "     export APPLE_TEAM_ID=\"XXXXXXXXXX\""
echo
warn "Certificates & keys: simpan di password manager / 1Password!"