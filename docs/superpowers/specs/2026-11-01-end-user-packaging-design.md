# ACTel End-User Packaging & Onboarding Wizard Design

## 1. Intent & Purpose
ACTel has reached full feature parity with professional Assetto Corsa dashboards (Sidekick practical tyre temperatures, live spline delta tracking, bi-directional control, Android Jetpack Compose UI). However, running ACTel currently requires developer tools (Node.js, terminal commands, Android Studio / ADB).

The goal of this project is to package ACTel into a frictionless, "plug-and-play" experience for end-user sim racers with zero developer prerequisites on PC and Android.

---

## 2. Goals, Non-Goals & Anti-Goals

### Goals
- **Single-Click PC Execution:** Provide a single standalone `ACTel.exe` that runs without requiring Node.js to be pre-installed on the user's PC.
- **Dedicated Clean Desktop Window:** Launch the dashboard in a frameless, native-feeling app window (using Windows' built-in Edge App mode: `--app=http://localhost:8080`) without browser address bars or cluttered tabs.
- **Background & Tray Persistence:** Allow the server to continue running seamlessly in the background and system tray so racers don't lose telemetry or disconnect if the window is closed.
- **Self-Hosted Android Onboarding:** Host the ready-to-use Android APK directly from the PC server (`/download/actel.apk`) and display a dynamic QR code on the setup page for instant phone installation and Wi-Fi pairing.
- **One-Click Assetto Corsa Bridge Installer:** Auto-detect the user's Steam Assetto Corsa directory (via Windows Registry and `libraryfolders.vdf`) and install the `actel_bridge` Python app with a single click, with manual folder fallback.
- **Network & Firewall Diagnostic:** Display the local LAN IP, port status, and a quick health indicator so users immediately know if Windows Firewall is blocking mobile connections.

### Non-Goals
- Publishing to the Google Play Store (direct APK distribution is sufficient and avoids sim racing licensing/review delays).
- Cross-platform PC support (Assetto Corsa is exclusively Windows/DirectX; Linux/macOS PC builds are out of scope).
- Complete rewrite into a native C++ or C# app (Node.js + koffi is already high performance and rock-solid).

### Anti-Goals
- Heavy Electron bundles (>150MB download, 200MB+ background RAM) that compete with sim racing frame rates.
- Complex multi-step manual file copying for the Assetto Corsa Python app.

---

## 3. Approaches Considered & Selected Approach

### Approach 1 (Selected): Portable ZIP Bundle + Lightweight EXE & App Mode Window
- **Distribution Format:** `ACTel-v1.0.0-windows-x64.zip` portable archive containing:
  - `ACTel.exe` (Standalone launcher running Node server + Edge App window + System Tray)
  - `public/` (Dashboard assets, fonts, icons, sound effects)
  - `download/actel.apk` (Pre-compiled Android release APK, ready to serve)
  - `actel_bridge/` (Assetto Corsa Python app for one-click bridge installation)
- **Runtime Execution:** When `ACTel.exe` is double-clicked:
  1. Port 8080 cleanup: Checks for and terminates any orphaned stale ACTel processes on port 8080.
  2. Starts the background telemetry & WebSocket server.
  3. Launches Windows Edge in native app mode: `msedge.exe --app=http://localhost:8080 --window-size=1280,820`.
  4. Minimizes to the Windows System Tray on close to prevent accidental game disconnects.
- **Benefits:** SimHub / Content Manager style portability. Instant updates, zero virtual filesystem extraction issues with native DLLs (`koffi`), and clean modular layout.

### Approach 2: Monolithic Single-File Executable (~75MB)
- **How it works:** Compresses Node runtime, web assets, and the 30MB Android APK into one massive binary.
- **Trade-offs:** Every Android APK update forces a re-packaging of the PC executable. Virtual filesystem extraction of `koffi.node` introduces runtime extraction latency. Rejected.

### Approach 3: Full Electron Application
- **How it works:** Bundles Chromium and Node.js into a unified setup installer.
- **Trade-offs:** 150MB+ download, 200MB+ RAM usage. Rejected due to heavy resource consumption during high-load racing sessions.

---

## 4. System Architecture & Detailed Components

```
+-------------------------------------------------------------+
|                          ACTel.exe                          |
|  +-------------------------+   +-------------------------+  |
|  |  Portable Node Server   |   |  Edge App Window        |  |
|  |  - Express / WebSocket  |   |  (msedge.exe --app=...) |  |
|  |  - Shared Memory Reader |   |  - Dashboard UI         |  |
|  |  - Bridge Auto-Install  |   |  - Onboarding Wizard    |  |
|  |  - APK Download Host    |   |  - Live Settings        |  |
|  +-------------------------+   +-------------------------+  |
|               |                             |               |
|               +------------+   +------------+               |
|                            |   |                            |
|                     +-----------------+                     |
|                     |   System Tray   |                     |
|                     +-----------------+                     |
+-------------------------------------------------------------+
        |                                       |
        | Local Wi-Fi (HTTP & WS)               | File Copy
        v                                       v
+------------------+             +-------------------------------+
| Android Phone    |             | Assetto Corsa Root            |
| (QR Scan & APK)  |             | apps/python/actel_bridge      |
+------------------+             +-------------------------------+
```

### 4.1 PC Packaging Architecture (`ACTel.exe`)
1. **Packaging Strategy:**
   - Use `pkg` or standalone Node 20 portable runner configured to load native `koffi` binary (`koffi.node`).
   - Include `public/`, `lib/`, and `actel_bridge/` in the asset bundle or alongside in a clean directory.
   - Launcher script:
     - Check if port 8080 is available; if not, gracefully notify or bind.
     - Launch Edge in app mode: `msedge.exe --app=http://localhost:8080 --window-size=1280,820`.
     - Maintain tray icon for background operation, with menu options: "Show Dashboard", "Open Settings", "Exit ACTel".

### 4.2 In-App Setup & Settings Wizard (Web & Desktop)
Add a dedicated "Setup & Devices" (Kurulum & Cihazlar) modal/view to the existing settings overlay:
1. **Mobile Setup Tab:**
   - Renders a clean QR code pointing to `http://<LAN_IP>:8080/download/actel.apk`.
   - Step 1: Scan QR code with phone camera to download APK.
   - Step 2: Open app on phone. App automatically connects to the PC via existing UDP broadcast (8088) or manual IP.
   - **Reactive Handshake & Firewall Status:** Avoids false-positive loopback checks. The PC UI listens for the phone's first WebSocket ping or APK download request; once received, the status badge reactively turns from "Bekleniyor..." (Waiting) to a glowing green "Telefon Bağlandı!" (Phone Connected). A "Güvenlik Duvarı İzni Ekle" batch script option is also provided if connection fails.
2. **Assetto Corsa Bridge Tab:**
   - Detects AC installation status (`Installed` or `Not Found`).
   - AC Path Auto-Discovery:
     - Query Registry: `HKCU\Software\Valve\Steam\SteamPath` or `HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Steam App 244210`.
     - Parse `steamapps/libraryfolders.vdf` to find secondary SSD libraries containing `assettocorsa`.
   - "Install Bridge" button:
     - Copies `actel_bridge/` Python files to `<AC_PATH>/apps/python/actel_bridge/`.
     - Validates destination files and updates status badge to "Ready in Assetto Corsa".
   - "Browse / Manual Path" option if user has a non-standard or custom AC directory.
3. **Port Conflict & Process Management:**
   - Port 8080 is treated as authoritative to guarantee zero breaking changes for CSP Lua (`actel_sync.lua`) and Android UDP discovery.
   - The launcher cleanly checks for and reclaims port 8080 from any previous hung ACTel process before launching.

### 4.3 Android Release APK Generation
- Configure `android/app/build.gradle.kts` to assemble a clean release APK (or signed debug APK ready for sideloading).
- Provide build script / npm script (`npm run build:release`) that places the output APK into `public/download/actel.apk`.

---

## 5. What is Left to the Implementer
- Selection of specific tray icon library / helper (e.g. lightweight Windows PowerShell/VBS wrapper vs `systray2` vs standalone runner).
- UI layout styling of the Setup Wizard matching the dark carbon-fiber / neon sim-racing design language.
- Packaging script nuances for `koffi.node` dynamic library extraction if `pkg` virtual filesystem requires external binary linking.

