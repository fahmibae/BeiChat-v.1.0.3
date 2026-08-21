@echo off
echo ===================================================
echo   BeiChat v1.0.3 - Build APK Otomatis (Tanpa Android Studio)
echo ===================================================

if not exist .env (
    echo APPLICATION_ID=com.aistudio.bitchat.qvxwzp > .env
)
if not exist .env.example (
    echo APPLICATION_ID=com.aistudio.bitchat.qvxwzp > .env.example
)

echo [1/2] Mengompilasi project menjadi file APK...
call gradle :app:assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo ===================================================
    echo [SUKSES] APK Berhasil dibuat!
    echo Lokasi APK: app\build\outputs\apk\debug\app-debug.apk
    echo ===================================================
    pause
) else (
    echo ===================================================
    echo [GAGAL] Build gagal. Pastikan Java JDK dan Gradle sudah terpasang.
    echo ===================================================
    pause
)
