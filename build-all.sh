#!/usr/bin/env bash
# =============================================================================
# Shiro - Build Script for All Platforms (Android, iOS, Windows, macOS, Linux)
# =============================================================================
# Usage: ./build-all.sh [platform] [mode]
#   platform: android | ios | windows | macos | linux | all (default: all)
#   mode: debug | release (default: release)
# Example: ./build-all.sh android release
# =============================================================================

set -euo pipefail

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PLATFORM="${1:-all}"
MODE="${2:-release}"
VERSION=$(cat "$PROJECT_ROOT/src-tauri/tauri.conf.json" | grep '"version"' | head -1 | sed -E 's/.*"version": "([^"]+)".*/\1/')

log() { echo -e "${BLUE}[INFO]${NC} $*"; }
success() { echo -e "${GREEN}[OK]${NC} $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*"; }

check_command() {
  if ! command -v "$1" &> /dev/null; then
    error "$1 tidak ditemukan. Install dulu."
    return 1
  fi
  return 0
}

# =============================================================================
# BUILD ANDROID
# =============================================================================
build_android() {
  log "Building Android ($MODE)..."
  cd "$PROJECT_ROOT"

  # Sync Capacitor
  log "Syncing Capacitor..."
  npx cap sync android

  cd android

  if [[ "$MODE" == "release" ]]; then
    log "Building Release AAB..."
    ./gradlew bundleRelease --no-daemon
    OUTPUT="app/build/outputs/bundle/release/app-release.aab"
    if [[ -f "$OUTPUT" ]]; then
      cp "$OUTPUT" "$PROJECT_ROOT/dist/Shiro_${VERSION}_android.aab"
      success "AAB: $PROJECT_ROOT/dist/Shiro_${VERSION}_android.aab"
    else
      error "AAB tidak ditemukan"
      return 1
    fi
  else
    log "Building Debug APK..."
    ./gradlew assembleDebug --no-daemon
    OUTPUT="app/build/outputs/apk/debug/app-debug.apk"
    if [[ -f "$OUTPUT" ]]; then
      cp "$OUTPUT" "$PROJECT_ROOT/dist/Shiro_${VERSION}_android_debug.apk"
      success "APK: $PROJECT_ROOT/dist/Shiro_${VERSION}_android_debug.apk"
    else
      error "APK tidak ditemukan"
      return 1
    fi
  fi
}

# =============================================================================
# BUILD IOS (macOS only)
# =============================================================================
build_ios() {
  if [[ "$(uname)" != "Darwin" ]]; then
    error "iOS build hanya bisa di macOS"
    return 1
  fi

  log "Building iOS ($MODE)..."
  cd "$PROJECT_ROOT"

  # Sync Capacitor
  log "Syncing Capacitor..."
  npx cap sync ios

  if [[ "$MODE" == "release" ]]; then
    log "Opening Xcode for Archive..."
    npx cap open ios
    warn "Di Xcode: Product → Archive → Distribute App"
    warn "Output: .ipa di ~/Library/Developer/Xcode/Archives/"
  else
    log "Building Debug untuk Simulator..."
    xcodebuild -workspace ios/App/App.xcworkspace \
      -scheme App \
      -configuration Debug \
      -destination 'platform=iOS Simulator,name=iPhone 15' \
      -derivedDataPath build/ios
  fi
}

# =============================================================================
# BUILD WINDOWS (Tauri)
# =============================================================================
build_windows() {
  if [[ "$(uname)" != *"MINGW"* ]] && [[ "$(uname)" != *"MSYS"* ]] && [[ "$(uname)" != *"CYGWIN"* ]]; then
    warn "Windows build sebaiknya dijalankan di Windows (Git Bash / PowerShell)"
  fi

  log "Building Windows ($MODE)..."
  cd "$PROJECT_ROOT/src-tauri"

  check_command cargo || return 1
  check_command rustup || return 1

  if [[ "$MODE" == "release" ]]; then
    cargo tauri build --target x86_64-pc-windows-msvc
    OUTPUT="target/x86_64-pc-windows-msvc/release/bundle/msi"
  else
    cargo tauri build --target x86_64-pc-windows-msvc --debug
    OUTPUT="target/x86_64-pc-windows-msvc/debug/bundle/msi"
  fi

  if [[ -d "$OUTPUT" ]]; then
    mkdir -p "$PROJECT_ROOT/dist"
    cp "$OUTPUT"/Shiro_*.msi "$PROJECT_ROOT/dist/Shiro_${VERSION}_windows.msi" 2>/dev/null || true
    success "MSI: $PROJECT_ROOT/dist/Shiro_${VERSION}_windows.msi"
  else
    error "MSI tidak ditemukan di $OUTPUT"
    return 1
  fi
}

# =============================================================================
# BUILD MACOS (Tauri)
# =============================================================================
build_macos() {
  if [[ "$(uname)" != "Darwin" ]]; then
    error "macOS build hanya bisa di macOS"
    return 1
  fi

  log "Building macOS ($MODE)..."
  cd "$PROJECT_ROOT/src-tauri"

  check_command cargo || return 1

  if [[ "$MODE" == "release" ]]; then
    cargo tauri build --target universal-apple-darwin
    OUTPUT="target/universal-apple-darwin/release/bundle/dmg"
  else
    cargo tauri build --target universal-apple-darwin --debug
    OUTPUT="target/universal-apple-darwin/debug/bundle/dmg"
  fi

  if [[ -d "$OUTPUT" ]]; then
    mkdir -p "$PROJECT_ROOT/dist"
    cp "$OUTPUT"/Shiro_*.dmg "$PROJECT_ROOT/dist/Shiro_${VERSION}_macos.dmg" 2>/dev/null || true
    success "DMG: $PROJECT_ROOT/dist/Shiro_${VERSION}_macos.dmg"
  else
    error "DMG tidak ditemukan di $OUTPUT"
    return 1
  fi
}

# =============================================================================
# BUILD LINUX (Tauri)
# =============================================================================
build_linux() {
  if [[ "$(uname)" != "Linux" ]]; then
    warn "Linux build sebaiknya dijalankan di Linux"
  fi

  log "Building Linux ($MODE)..."
  cd "$PROJECT_ROOT/src-tauri"

  check_command cargo || return 1

  if [[ "$MODE" == "release" ]]; then
    cargo tauri build
    DEB="target/release/bundle/deb"
    APPIMAGE="target/release/bundle/appimage"
  else
    cargo tauri build --debug
    DEB="target/debug/bundle/deb"
    APPIMAGE="target/debug/bundle/appimage"
  fi

  mkdir -p "$PROJECT_ROOT/dist"
  if [[ -d "$DEB" ]]; then
    cp "$DEB"/shiro_*.deb "$PROJECT_ROOT/dist/Shiro_${VERSION}_linux.deb" 2>/dev/null || true
    success "DEB: $PROJECT_ROOT/dist/Shiro_${VERSION}_linux.deb"
  fi
  if [[ -d "$APPIMAGE" ]]; then
    cp "$APPIMAGE"/Shiro_*.AppImage "$PROJECT_ROOT/dist/Shiro_${VERSION}_linux.AppImage" 2>/dev/null || true
    success "AppImage: $PROJECT_ROOT/dist/Shiro_${VERSION}_linux.AppImage"
  fi
}

# =============================================================================
# MAIN
# =============================================================================
main() {
  mkdir -p "$PROJECT_ROOT/dist"

  log "Shiro v$VERSION - Build $MODE untuk $PLATFORM"
  log "Project: $PROJECT_ROOT"

  case "$PLATFORM" in
    android)
      build_android
      ;;
    ios)
      build_ios
      ;;
    windows)
      build_windows
      ;;
    macos)
      build_macos
      ;;
    linux)
      build_linux
      ;;
    all)
      # Build desktop platforms based on current OS
      case "$(uname)" in
        Darwin)
          build_macos
          build_ios
          ;;
        Linux)
          build_linux
          build_android
          ;;
        MINGW*|MSYS*|CYGWIN*)
          build_windows
          ;;
      esac
      ;;
    *)
      error "Platform tidak dikenal: $PLATFORM"
      echo "Gunakan: android | ios | windows | macos | linux | all"
      exit 1
      ;;
  esac

  success "Build selesai! Output di: $PROJECT_ROOT/dist/"
  ls -la "$PROJECT_ROOT/dist/"
}

main "$@"