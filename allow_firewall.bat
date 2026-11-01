@echo off
:: ============================================================
:: ACTel Windows Firewall Exception Helper
:: Automatically requests Administrator elevation and adds inbound rules
:: ============================================================
NET SESSION >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo [ACTel] Yonetici haklari isteniyor...
    powershell -Command "Start-Process cmd -ArgumentList '/c \"%~fn0\"' -Verb runAs"
    exit /b
)

echo ============================================================
echo   ACTel Telemetri ve Gosterge - Guvenlik Duvari Yapilandirma
echo ============================================================
echo.
echo [1/2] Port 8080 (TCP - Web Dashboard ve WebSocket) kurali ekleniyor...
netsh advfirewall firewall delete rule name="ACTel Telemetry Dashboard" protocol=TCP localport=8080 >nul 2>&1
netsh advfirewall firewall add rule name="ACTel Telemetry Dashboard" dir=in action=allow protocol=TCP localport=8080 profile=any >nul
echo   -> Port 8080 TCP basariyla acildi.

echo.
echo [2/2] Port 8088 (UDP - Android Otomatik Kesif) kurali ekleniyor...
netsh advfirewall firewall delete rule name="ACTel UDP Discovery" protocol=UDP localport=8088 >nul 2>&1
netsh advfirewall firewall add rule name="ACTel UDP Discovery" dir=in action=allow protocol=UDP localport=8088 profile=any >nul
echo   -> Port 8088 UDP basariyla acildi.

echo.
echo ============================================================
echo   TEBRIKLER! Guvenlik duvari kurallari basariyla olusturuldu.
echo   Telefonunuz artik yerel ag uzerinden baglanabilir.
echo ============================================================
echo.
pause

