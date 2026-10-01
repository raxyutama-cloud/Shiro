#!/usr/bin/env bash
# =============================================================================
# Generate Android Release Keystore untuk Shiro
# =============================================================================
# Output: shiro-release-key.jks (di root project)
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
KEYSTORE_PATH="$PROJECT_ROOT/shiro-release-key.jks"
ALIAS="shiro"
VALIDITY=10000  # ~27 tahun
KEYSIZE=2048
STORETYPE="PKCS12"

echo "======================================================"
echo "  Shiro Android Keystore Generator"
echo "======================================================"
echo

if [[ -f "$KEYSTORE_PATH" ]]; then
  warn "Keystore sudah ada: $KEYSTORE_PATH"
  read -p "Timpa? (y/N): " -n 1 -r
  echo
  if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    log "Dibatalkan."
    exit 0
  fi
fi

# Input password (hidden)
read -s -p "Keystore password (min 6 char): " STORE_PASS
echo
read -s -p "Konfirmasi password: " STORE_PASS_CONFIRM
echo
if [[ "$STORE_PASS" != "$STORE_PASS_CONFIRM" ]]; then
  error "Password tidak cocok!"
  exit 1
fi
if [[ ${#STORE_PASS} -lt 6 ]]; then
  error "Password minimal 6 karakter!"
  exit 1
fi

# Key password (bisa sama)
read -s -p "Key password (Enter = sama dengan keystore): " KEY_PASS
echo
if [[ -z "$KEY_PASS" ]]; then
  KEY_PASS="$STORE_PASS"
fi

# Info sertifikat
echo
log "Info sertifikat (kosongkan untuk default):"
read -p "Nama (CN) [Raxy Utama]: " CN
CN=${CN:-"Raxy Utama"}

read -p "Unit Organisasi (OU) [Mobile Dev]: " OU
OU=${OU:-"Mobile Dev"}

read -p "Organisasi (O) [Shiro]: " O
O=${O:-"Shiro"}

read -p "Kota (L) [Jakarta]: " L
L=${L:-"Jakarta"}

read -p "Provinsi (ST) [DKI Jakarta]: " ST
ST=${ST:-"DKI Jakarta"}

read -p "Kode Negara (C) [ID]: " C
C=${C:-"ID"}

DNAME="CN=$CN, OU=$OU, O=$O, L=$L, ST=$ST, C=$C"

echo
log "Membuat keystore..."
log "  File: $KEYSTORE_PATH"
log "  Alias: $ALIAS"
log "  DN: $DNAME"
log "  Validitas: $VALIDITY hari"
log "  Key size: $KEYSIZE bit"
echo

keytool -genkeypair \
  -keystore "$KEYSTORE_PATH" \
  -storepass "$STORE_PASS" \
  -keypass "$KEY_PASS" \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize "$KEYSIZE" \
  -storetype "$STORETYPE" \
  -validity "$VALIDITY" \
  -dname "$DNAME" \
  -v

success "Keystore dibuat: $KEYSTORE_PATH"

echo
echo "======================================================"
echo "  LANGKAH SELANJUTNYA (WAJIB):"
echo "======================================================"
echo
echo "1. Buat file android/keystore.properties:"
echo "   cp android/keystore.properties.example android/keystore.properties"
echo
echo "2. Edit android/keystore.properties dengan password Anda:"
cat << EOF
   storeFile=../shiro-release-key.jks
   storePassword=$STORE_PASS
   keyAlias=shiro
   keyPassword=$KEY_PASS
EOF
echo
echo "3. Tambahkan ke .gitignore (sudah ada default):"
echo "   shiro-release-key.jks"
echo "   android/keystore.properties"
echo
echo "4. Build release:"
echo "   ./build-all.sh android release"
echo
warn "SIMPAN PASSWORD & FILE KEYSTORE DI TEMPAT AMAN!"
warn "Tanpa ini, tidak bisa update app di Play Store."
echo

# Generate base64 untuk GitHub Actions secret
if command -v base64 &> /dev/null; then
  echo "--- GitHub Actions Secret (ANDROID_KEYSTORE) ---"
  base64 -w 0 "$KEYSTORE_PATH"
  echo
  echo "--- Simpan output di atas sebagai secret ANDROID_KEYSTORE ---"
  echo "--- ANDROID_STORE_PASSWORD: $STORE_PASS ---"
  echo "--- ANDROID_KEY_PASSWORD: $KEY_PASS ---"
  echo "--- ANDROID_KEY_ALIAS: shiro ---"
fi