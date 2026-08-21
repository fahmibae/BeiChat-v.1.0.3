#!/usr/bin/env bash
set -e

# Pindah ke direktori root project
cd "$(dirname "$0")"

echo "==================================================="
echo "  BeiChat v1.0.3 - Build APK Otomatis (Linux/macOS)"
echo "==================================================="

if [ ! -f .env ]; then
    echo "APPLICATION_ID=com.aistudio.bitchat.qvxwzp" > .env
fi
if [ ! -f .env.example ]; then
    echo "APPLICATION_ID=com.aistudio.bitchat.qvxwzp" > .env.example
fi

echo "[1/2] Memeriksa Gradle dan mengompilasi project..."

if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    ./gradlew :app:assembleDebug
elif command -v gradle &> /dev/null; then
    gradle :app:assembleDebug
else
    echo "==================================================="
    echo "[PERINGATAN] Gradle belum terpasang di Linux Anda."
    echo "Silakan install Gradle dengan menjalankan perintah berikut di terminal:"
    echo ""
    echo "  sudo apt update && sudo apt install -y gradle openjdk-17-jdk"
    echo ""
    echo "Setelah selesai diinstall, jalankan ulang:"
    echo "  bash build_apk.sh"
    echo "==================================================="
    exit 1
fi

echo "==================================================="
echo "[SUKSES] APK Berhasil dibuat!"
echo "Lokasi APK: app/build/outputs/apk/debug/app-debug.apk"
echo "==================================================="

