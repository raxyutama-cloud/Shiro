# 🛠️ Shiro — Build Guide (All Platforms)

> **Shiro v4.4.2** — Fast media downloader for 16+ platforms  
> Android · iOS · Windows · macOS · Linux

---

## 📋 Prasyarat Umum

| Tool | Versi Minimum | Catatan |
|------|---------------|---------|
| Node.js | 20+ | `npm ci` di root project |
| Rust | 1.77+ (stable) | `rustup default stable` |
| Git | - | Clone repo |

---

## 🤖 Android (Capacitor)

### Setup Satu Kali
```bash
# 1. Install Android Studio + SDK (API 34)
# 2. Set ANDROID_HOME di ~/.bashrc / ~/.zshrc
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin

# 3. Install Capacitor CLI
npm i -g @capacitor/cli
```

### Build Debug (Test Device)
```bash
npx cap sync android
cd android && ./gradlew assembleDebug --no-daemon
# Output: android/app/build/outputs/apk/debug/app-debug.apk
```

### Build Release (Play Store)
```bash
npx cap sync android
cd android && ./gradlew bundleRelease --no-daemon
# Output: android/app/build/outputs/bundle/release/app-release.aab
```

### Signing Release (Wajib untuk Play Store)
1. Generate keystore:
```bash
keytool -genkey -v -keystore shiro-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias shiro
```
2. Buat `android/keystore.properties`:
```properties
storeFile=../shiro-release-key.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=shiro
keyPassword=YOUR_KEY_PASSWORD
```
3. Edit `android/app/build.gradle` (sudah dikonfigurasi di project ini)

---

## 🍎 iOS (Capacitor) — **Hanya di macOS**

### Setup Satu Kali
```bash
# 1. Install Xcode dari App Store
# 2. Install Command Line Tools
xcode-select --install

# 3. Install CocoaPods
sudo gem install cocoapods

# 4. Setup Apple Developer Account (untuk device fisik / TestFlight)
```

### Build Debug (Simulator)
```bash
npx cap sync ios
cd ios/App && xcodebuild -workspace App.xcworkspace -scheme App -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 15' build
```

### Build Release (Archive untuk TestFlight / App Store)
```bash
npx cap sync ios
npx cap open ios
```
Di Xcode:
1. **Product → Archive**
2. Tunggu selesai → **Distribute App**
3. Pilih **App Store Connect** (TestFlight) atau **Export** (untuk .ipa)

### CI/CD (GitHub Actions)
Workflow sudah siap di `.github/workflows/build-release.yml` — push tag `v*` otomatis build IPA.

---

## 🪟 Windows (Tauri)

### Setup Satu Kali
```bash
# 1. Install Visual Studio 2022 Community + "Desktop development with C++"
# 2. Install WebView2 Runtime (sudah terpasang di Win 10/11 modern)
# 3. Install Rust
winget install Rustlang.Rust

# 4. Target MSVC (default di Windows)
rustup target add x86_64-pc-windows-msvc
```

### Build
```bash
cd src-tauri
cargo tauri build --target x86_64-pc-windows-msvc
# Output: src-tauri/target/x86_64-pc-windows-msvc/release/bundle/msi/Shiro_*.msi
```

### Code Signing (Opsional, untuk menghindari SmartScreen)
```bash
# Dapatkan sertifikat Code Signing (EV certificate recommended)
# Set env var:
$env:CSC_LINK="path/to/cert.p12"
$env:CSC_KEY_PASSWORD="password"
# Lalu build ulang
```

---

## 🍎 macOS (Tauri) — **Hanya di macOS**

### Setup Satu Kali
```bash
# 1. Xcode Command Line Tools
xcode-select --install

# 2. Rust + universal target
rustup target add universal-apple-darwin
# Atau untuk Apple Silicon saja:
rustup target add aarch64-apple-darwin
```

### Build Universal (Intel + Apple Silicon)
```bash
cd src-tauri
cargo tauri build --target universal-apple-darwin
# Output: src-tauri/target/universal-apple-darwin/release/bundle/dmg/Shiro_*.dmg
```

### Notarization (Wajib untuk distribusi di luar App Store)
```bash
# 1. Setup Apple Developer ID Application certificate
# 2. Buat App-Specific Password di appleid.apple.com
# 3. Set env:
export CSC_LINK="path/to/DeveloperID_application.p12"
export CSC_KEY_PASSWORD="p12_password"
export APPLE_ID="your@apple.com"
export APPLE_PASSWORD="app-specific-password"
export APPLE_TEAM_ID="XXXXXXXXXX"

# 4. Build (tauri-cli otomatis notarize jika env di-set)
cargo tauri build --target universal-apple-darwin
```

---

## 🐧 Linux (Tauri)

### Setup (Ubuntu/Debian)
```bash
sudo apt update && sudo apt install -y \
  libwebkit2gtk-4.1-dev \
  libayatana-appindicator3-dev \
  librsvg2-dev \
  patchelf \
  libssl-dev \
  cargo
```

### Build
```bash
cd src-tauri
cargo tauri build
# Output:
#   DEB:  src-tauri/target/release/bundle/deb/Shiro_*.deb
#   AppImage: src-tauri/target/release/bundle/appimage/Shiro_*.AppImage
```

### Distro Lain
| Distro | Package Manager |
|--------|-----------------|
| Fedora | `dnf install webkit2gtk4.1-devel libayatana-appindicator-gtk3-devel librsvg2-devel patchelf openssl-devel` |
| Arch | `pacman -S webkit2gtk-4.1 libayatana-appindicator librsvg patchelf openssl` |

---

## 🚀 One-Command Build (Script Otomatis)

```bash
# Di root project
chmod +x build-all.sh

# Build semua platform yang didukung OS saat ini
./build-all.sh all release

# Atau platform spesifik
./build-all.sh android release
./build-all.sh windows release
./build-all.sh macos release
./build-all.sh linux release
./build-all.sh ios release
```

Output ada di folder `dist/`:
```
dist/
├── Shiro_4.4.2_android.aab
├── Shiro_4.4.2_windows.msi
├── Shiro_4.4.2_macos.dmg
├── Shiro_4.4.2_linux.deb
├── Shiro_4.4.2_linux.AppImage
└── Shiro_4.4.2_ios.ipa (di macOS)
```

---

## ☁️ GitHub Actions CI/CD (Otomatis)

Sudah dikonfigurasi di `.github/workflows/build-release.yml`.

### Cara Pakai
```bash
# 1. Commit semua perubahan
git add .
git commit -m "chore: release v4.4.2"

# 2. Tag versi
git tag v4.4.2
git push origin v4.4.2

# 3. GitHub Actions akan:
#    - Build Android (AAB)
#    - Build iOS (IPA unsigned)
#    - Build Windows (MSI)
#    - Build macOS (DMG Universal)
#    - Build Linux (DEB + AppImage)
#    - Create GitHub Release dengan semua artifact
```

### Secrets yang Dibutuhkan (Settings → Secrets → Actions)
| Secret | Untuk | Contoh |
|--------|-------|--------|
| `ANDROID_KEYSTORE` | Android signing | Base64 encoded `.jks` |
| `ANDROID_STORE_PASSWORD` | Android | `********` |
| `ANDROID_KEY_PASSWORD` | Android | `********` |
| `APPLE_CERTIFICATE` | macOS/iOS signing | Base64 `.p12` |
| `APPLE_CERT_PASSWORD` | macOS/iOS | `********` |
| `APPLE_ID` | Notarization | `you@email.com` |
| `APPLE_PASSWORD` | Notarization | App-specific password |
| `APPLE_TEAM_ID` | Notarization | `XXXXXXXXXX` |
| `WINDOWS_CERT` | Windows signing | Base64 `.pfx` |
| `WINDOWS_CERT_PASSWORD` | Windows | `********` |

> **Tip:** Untuk iOS/macOS signing di CI, gunakan `fastlane match` atau `xcodes` untuk manajemen sertifikat.

---

## 📦 Versioning

Update versi di 3 tempat sebelum release:
```json
// src-tauri/tauri.conf.json
"version": "4.4.2"

// capacitor.config.json (Android/iOS)
"appName": "Shiro"  // version di android/app/build.gradle & Xcode

// package.json (opsional)
"version": "4.4.2"
```

---

## ✅ Checklist Pre-Release

- [ ] Versi di `tauri.conf.json`, `build.gradle`, Xcode project sama
- [ ] Ikon & splash screen semua ukuran tersedia
- [ ] Permissions Android/iOS lengkap
- [ ] Tauri allowlist minimal (sudah dikonfigurasi)
- [ ] Test di device fisik (Android & iOS)
- [ ] Test install MSI/DMG/DEB/AppImage
- [ ] Changelog diperbarui
- [ ] Tag `vX.Y.Z` di-push

---

## 🔗 Referensi Cepat

| Platform | Doc Resmi |
|----------|-----------|
| Capacitor Android | https://capacitorjs.com/docs/android |
| Capacitor iOS | https://capacitorjs.com/docs/ios |
| Tauri Windows | https://tauri.app/v2/guides/distribution/windows/ |
| Tauri macOS | https://tauri.app/v2/guides/distribution/macos/ |
| Tauri Linux | https://tauri.app/v2/guides/distribution/linux/ |
| GitHub Actions | https://docs.github.com/actions |

---

**Butuh bantuan?** Buka issue di GitHub atau cek log build di Actions tab.