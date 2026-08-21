#!/usr/bin/env bash
set -e

echo "==================================================="
echo "  BeiChat v1.0.3 - Build APK Otomatis (Linux/macOS)"
echo "==================================================="

if [ ! -f .env ]; then
    echo "APPLICATION_ID=com.aistudio.bitchat.qvxwzp" > .env
fi
if [ ! -f .env.example ]; then
    echo "APPLICATION_ID=com.aistudio.bitchat.qvxwzp" > .env.example
fi

echo "[1/2] Mengompilasi project menjadi file APK..."
gradle :app:assembleDebug

echo "==================================================="
echo "[SUKSES] APK Berhasil dibuat!"
echo "Lokasi APK: app/build/outputs/apk/debug/app-debug.apk"
echo "==================================================="
