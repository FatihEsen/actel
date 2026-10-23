@echo off
title Assetto Corsa Telemetry ^& Controller Server
cd /d "%~dp0"
echo =======================================================
echo    ASSETTO CORSA TELEMETRY ^& BUTTON BOX WEB SERVER
echo =======================================================
echo.
echo Sunucu baslatiliyor...

REM Port 8080 uzerindeki eski asili kalan islemleri temizle
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    taskkill /f /pid %%a >nul 2>&1
)

node server.js
pause
