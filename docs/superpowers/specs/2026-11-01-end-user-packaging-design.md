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

### Approach 1 (Selected): Lightweight Portable EXE + Windows App Mode & Tray Shell
- **How it works:** Package `server.js` and native modules (`koffi`) using a portable binary packager (`pkg` or standalone Node runtime wrapper). When launched, it starts the background server, launches `msedge.exe --app=http://localhost:8080 --window-size=1280,820`, and creates a System Tray icon.
- **Trade-offs:** Minimal disk footprint (~35-45 MB), ultra-low memory (~40-60 MB RAM), zero impact on in-game FPS. Relies on the Edge runtime already built into Windows 10/11.
- **Decision:** Chosen because sim racers demand minimal background overhead and zero installation friction.

### Approach 2: Full Electron Application
- **How it works:** Bundles Chromium and Node.js into a unified setup installer.
- **Trade-offs:** 150MB+ download, 200MB+ RAM usage. Rejected due to heavy resource consumption during high-load racing sessions.

### Approach 3: Headless CLI Service + Browser Tab
- **How it works:** A bare console window running the server and opening a default browser tab.
- **Trade-offs:** Doesn't feel like a professional standalone application, easy to accidentally close in a race. Rejected.

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
2. **Assetto Corsa Bridge Tab:**
   - Detects AC installation status (`Installed` or `Not Found`).
   - AC Path Auto-Discovery:
     - Query Registry: `HKCU\Software\Valve\Steam\SteamPath` or `HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Steam App 244210`.
     - Parse `steamapps/libraryfolders.vdf` to find secondary SSD libraries containing `assettocorsa`.
   - "Install Bridge" button:
     - Copies `actel_bridge/` Python files to `<AC_PATH>/apps/python/actel_bridge/`.
     - Validates destination files and updates status badge to "Ready in Assetto Corsa".
   - "Browse / Manual Path" option if user has a non-standard or pirate/custom AC directory.
3. **Network & Firewall Status:**
   - Shows detected local IPv4 address (e.g. `192.168.1.45`).
   - Displays test button to verify mobile reachable port.

### 4.3 Android Release APK Generation
- Configure `android/app/build.gradle.kts` to assemble a clean release APK (or signed debug APK ready for sideloading).
- Provide build script / npm script (`npm run build:release`) that places the output APK into `public/download/actel.apk`.

---

## 5. What is Left to the Implementer
- Selection of specific tray icon library / helper (e.g. lightweight Windows PowerShell/VBS wrapper vs `systray2` vs standalone runner).
- UI layout styling of the Setup Wizard matching the dark carbon-fiber / neon sim-racing design language.
- Packaging script nuances for `koffi.node` dynamic library extraction if `pkg` virtual filesystem requires external binary linking.
