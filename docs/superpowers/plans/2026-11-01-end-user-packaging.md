# End-User Packaging & Onboarding Wizard Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Package ACTel into a portable, zero-configuration Windows ZIP distribution with a native desktop launcher (`ACTel.exe`), Edge App mode window, System Tray management, offline QR code mobile onboarding, and a one-click Assetto Corsa bridge installer.

**Architecture:** A lightweight native C# launcher (`ACTel.exe`) compiled via built-in Windows `csc.exe` cleans stale port 8080 processes, runs the Node.js telemetry server silently in the background, creates a System Tray icon, and opens the dashboard in frameless Edge App mode. An onboard "Kurulum & Cihazlar" wizard serves the Android APK with offline QR code generation, reactively detects phone connection, and auto-detects Steam/AC paths to install python/lua plugins with a single click. A packaging script bundles everything into a portable distribution ZIP.

**Tech Stack:** Node.js (CommonJS, Express, WebSocket, dgram), C# (.NET Framework 4.0 via built-in `csc.exe`), Windows Edge App Mode, Vanilla JS/CSS (responsive cockpit), Kotlin / Jetpack Compose (Android).

**Spec:** [docs/superpowers/specs/2026-11-01-end-user-packaging-design.md](file:///d:/Documents/agy/actel/docs/superpowers/specs/2026-11-01-end-user-packaging-design.md)

---

## Global Constraints
- **Offline Self-Sufficiency:** Zero runtime dependence on external CDNs or online APIs (e.g. `api.qrserver.com`); QR code generation and asset hosting must run 100% locally.
- **Port 8080 Invariant:** Port 8080 remains authoritative across all Assetto Corsa Lua (`actel_sync.lua`) and Android UDP discovery connections; conflicts are resolved by cleaning stale ACTel processes.
- **Windows Binary Redirection:** Adhere to `AGENTS.md` - never redirect binary streams in PowerShell; use node scripts or direct file operations.
- **Sidekick Parity Invariants:** Preserve existing tyre practical temperature, delta calculation, and out-lap suppression invariants in all telemetry code.

---

## Review Focus
1. **Offline Sim Rig:** User's gaming PC has no internet access during LAN racing. Expected: QR code generates locally via SVG, APK downloads over local Wi-Fi, and dashboard renders completely offline.
2. **Custom / Secondary AC Library:** Assetto Corsa is installed on a secondary drive not in default paths. Expected: Path detector reads `libraryfolders.vdf` or allows manual directory browsing without crashing.
3. **Port 8080 Conflict / Zombie Process:** A previous Node.js server crashed or was orphaned. Expected: Launcher identifies and kills the stale process holding 8080 before starting.
4. **Firewall Blocking Mobile:** Inbound connections to 8080 are blocked by Windows Firewall. Expected: Reactive handshake remains in "Waiting..." and UI provides a one-click batch script to add the firewall rule.
5. **Missing APK File:** Server is started before Android APK is built. Expected: UI displays a clear "APK Not Built" warning with rebuild instructions instead of serving a dead 404 download.

---

### Task 1: Offline QR Code Generator & Network Diagnostic Helper

**Files:**
- Create: `lib/networkHelper.js`
- Create: `test/networkHelper.test.js`
- Modify: `server.js`

**Interfaces:**
- Consumes: Node.js `os`, `fs`, `path`.
- Produces: 
  - `networkHelper.getLocalIps(): Array<{interface: string, ip: string, url: string}>`
  - `networkHelper.generateQrSvg(text: string): string` (Local pure-JS QR matrix to SVG generator)
  - `networkHelper.getApkStatus(): { exists: boolean, sizeMb: number, path: string }`
  - `networkHelper.setMobileConnected(connected: boolean): void`
  - `networkHelper.isMobileConnected(): boolean`

- [ ] **Step 1: Write unit tests for `networkHelper`**

```javascript
// test/networkHelper.test.js
const assert = require('assert');
const networkHelper = require('../lib/networkHelper');

// 1. IP retrieval
const ips = networkHelper.getLocalIps(8080);
assert(Array.isArray(ips), 'getLocalIps should return an array');

// 2. Offline QR SVG generation
const svg = networkHelper.generateQrSvg('http://192.168.1.50:8080');
assert(typeof svg === 'string', 'generateQrSvg should return a string');
assert(svg.includes('<svg'), 'QR output should be valid SVG');
assert(svg.includes('</svg>'), 'QR output should close SVG');

// 3. Mobile handshake state
assert.strictEqual(networkHelper.isMobileConnected(), false);
networkHelper.setMobileConnected(true);
assert.strictEqual(networkHelper.isMobileConnected(), true);

console.log('✓ networkHelper tests passed');
```

- [ ] **Step 2: Run test to verify it fails**

Run: `node test/networkHelper.test.js`
Expected: FAIL (Cannot find module '../lib/networkHelper')

- [ ] **Step 3: Implement `lib/networkHelper.js`**

Implement pure-JS QR code matrix generator (compact QR code algorithm producing `<svg>` without external npm dependencies) and network inspection methods.

- [ ] **Step 4: Integrate `networkHelper` and APK download route in `server.js`**

Add endpoints:
- `GET /api/setup/network`: returns `{ localIps, apk: getApkStatus(), mobileConnected: isMobileConnected() }`
- `GET /api/setup/qr`: returns SVG QR code for the primary LAN download URL
- `GET /download/actel.apk`: serves `public/download/actel.apk` with `Content-Disposition: attachment; filename=actel.apk`
- WebSocket handshake: When any WS client connects with query `?client=mobile` or hits `/download/actel.apk`, mark `networkHelper.setMobileConnected(true)` and broadcast `{ type: 'mobile_connected' }`.

- [ ] **Step 5: Run tests and verify server endpoints**

Run: `node test/networkHelper.test.js`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add lib/networkHelper.js test/networkHelper.test.js server.js
git commit -m "feat(setup): add offline QR generator, network helper, and APK download route"
```

---

### Task 2: Assetto Corsa Steam Registry & Bridge Auto-Installer

**Files:**
- Modify: `lib/acPathDetector.js`
- Create: `test/acPathDetector.test.js`
- Modify: `server.js`

**Interfaces:**
- Consumes: `lib/acPathDetector.js`, Windows Registry / VDF paths.
- Produces:
  - `acPathDetector.detectSteamLibraryFolders(): string[]`
  - `acPathDetector.getBridgeStatus(): { acFound: boolean, installDir: string, docsDir: string, bridgeInstalled: boolean, syncInstalled: boolean }`
  - `acPathDetector.installBridge(targetDir?: string): { success: boolean, message: string }`
  - `POST /api/setup/install-bridge`: Express endpoint triggering installation.
  - `GET /api/setup/bridge-status`: Express endpoint returning current bridge status.

- [ ] **Step 1: Write unit tests for `acPathDetector` extensions**

```javascript
// test/acPathDetector.test.js
const assert = require('assert');
const acPathDetector = require('../lib/acPathDetector');

const status = acPathDetector.getBridgeStatus();
assert(typeof status === 'object', 'getBridgeStatus should return an object');
assert('acFound' in status, 'status should have acFound');
assert('bridgeInstalled' in status, 'status should have bridgeInstalled');
assert('syncInstalled' in status, 'status should have syncInstalled');

console.log('✓ acPathDetector tests passed. Status:', status);
```

- [ ] **Step 2: Run test to verify it fails**

Run: `node test/acPathDetector.test.js`
Expected: FAIL (`acPathDetector.getBridgeStatus is not a function`)

- [ ] **Step 3: Enhance `lib/acPathDetector.js`**

Implement:
1. `detectSteamLibraryFolders()`: Checks `HKCU\Software\Valve\Steam\SteamPath`, parses `libraryfolders.vdf` for paths containing `assettocorsa`.
2. `getBridgeStatus()`: Checks presence of `apps/python/actel_bridge/actel_bridge.py` and `apps/lua/actel_sync/manifest.ini`.
3. `installBridge(targetDir)`: Copies `ac_plugin/actel_bridge` and `ac_plugin/actel_sync` to target directory and enables `ACTEL_BRIDGE` in `python.ini`.

- [ ] **Step 4: Expose Bridge Endpoints in `server.js`**

Add `GET /api/setup/bridge-status` and `POST /api/setup/install-bridge` (with optional `{ customPath }` body).

- [ ] **Step 5: Run tests and verify**

Run: `node test/acPathDetector.test.js`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add lib/acPathDetector.js test/acPathDetector.test.js server.js
git commit -m "feat(setup): enhance AC path detection with Steam VDF and add bridge install API"
```

---

### Task 3: Setup & Devices UI Wizard in Web Dashboard

**Files:**
- Modify: `public/index.html`
- Modify: `public/js/app.js`
- Modify: `public/css/style.css`

**Interfaces:**
- Consumes: `/api/setup/network`, `/api/setup/qr`, `/api/setup/bridge-status`, `/api/setup/install-bridge`, `/download/actel.apk`.
- Produces: Interactive "Kurulum & Cihazlar" wizard in the settings view and QR connection modal.

- [ ] **Step 1: Add Setup Wizard markup to `public/index.html`**

In `#view-settings`:
- Add "Cihaz ve Eklenti Kurulumu" section with 3 interactive cards:
  1. **Mobil Uygulama (Android):** Live offline QR code image, "APK İndir (.apk)" direct download button, and a reactive connection badge:
     `<span id="mobileStatusBadge" class="status-badge waiting">Telefon Bekleniyor...</span>`.
  2. **Assetto Corsa Eklenti Köprüsü (Bridge):** Shows detected AC path, status badges ("Python Bridge: Yüklü/Eksik", "CSP Sync: Yüklü/Eksik"), "Tek Tıkla Oyuna Kur" button (`#installBridgeBtn`), and a folder path override input.
  3. **Windows Güvenlik Duvarı:** IP / Port diagnostic and "Güvenlik Duvarı İzin Betiği İndir" helper.

- [ ] **Step 2: Add styling for setup cards in `public/css/style.css`**

Add modern dark-carbon styles matching the existing cockpit theme, including glowing green/amber status pills and animated progress states for the installation buttons.

- [ ] **Step 3: Implement client logic in `public/js/app.js`**

Implement:
- `loadSetupStatus()`: Calls `/api/setup/network` and `/api/setup/bridge-status`.
- Renders local SVG QR code from `/api/setup/qr`.
- Binds `#installBridgeBtn`: Sends POST to `/api/setup/install-bridge`, shows loading spinner, and displays toast notification on success.
- Handles WebSocket `mobile_connected` event: Updates `#mobileStatusBadge` to glowing green "Telefon Bağlandı!".

- [ ] **Step 4: Verify UI via headless Edge screenshot**

Run: `msedge --headless --screenshot=setup_wizard_check.png --window-size=1280,820 http://localhost:8080`
Verify layout and elements.

- [ ] **Step 5: Commit**

```bash
git add public/index.html public/js/app.js public/css/style.css
git commit -m "feat(ui): add setup and devices wizard to settings with reactive mobile handshake"
```

---

### Task 4: Android Release APK Build Script & Bundling

**Files:**
- Modify: `package.json`
- Create: `scripts/build-apk.js`

**Interfaces:**
- Consumes: `android/gradlew.bat assembleRelease` (or `assembleDebug`).
- Produces: `public/download/actel.apk`.

- [ ] **Step 1: Create `scripts/build-apk.js`**

Write Node.js build runner:
1. Runs `gradlew.bat assembleDebug` (or `assembleRelease`).
2. Locates the output APK in `android/app/build/outputs/apk/...`.
3. Ensures `public/download/` directory exists.
4. Copies and renames output to `public/download/actel.apk`.
5. Logs APK file size and confirms readiness.

- [ ] **Step 2: Add `npm run build:apk-bundle` to `package.json`**

- [ ] **Step 3: Run APK build and verify output**

Run: `npm run build:apk-bundle`
Expected: `public/download/actel.apk` created and valid.

- [ ] **Step 4: Commit**

```bash
git add package.json scripts/build-apk.js
git commit -m "feat(mobile): add automated APK build and public bundle script"
```

---

### Task 5: Native Windows Desktop Launcher (`ACTel.exe`)

**Files:**
- Create: `src/launcher/ACTelLauncher.cs`
- Create: `scripts/build-launcher.js`
- Create: `allow_firewall.bat`

**Interfaces:**
- Consumes: Windows .NET Framework 4.0 `csc.exe`.
- Produces: `dist/ACTel.exe`.

- [ ] **Step 1: Write `src/launcher/ACTelLauncher.cs`**

C# Windows Forms application that:
1. Resolves port 8080 conflicts: Finds any PID listening on port 8080 and gracefully terminates it if it is a stale node/actel instance.
2. Starts background Node.js server: Spawns `node.exe server.js` with `CreateNoWindow = true` and `UseShellExecute = false`.
3. System Tray: Adds a `NotifyIcon` with icon and context menu:
   - "Dashboard'ı Aç" -> launches or focuses Edge App window.
   - "Eklenti & Cihaz Kurulumu" -> opens `http://localhost:8080#setup`.
   - "Çıkış" -> kills child node process and exits.
4. Edge App Window: Spawns `msedge.exe --app=http://localhost:8080 --window-size=1280,820`.
5. Process Life-cycle: Hooks `Application.ApplicationExit` and `ProcessExit` to terminate child processes cleanly.

- [ ] **Step 2: Create `scripts/build-launcher.js`**

Compiles `src/launcher/ACTelLauncher.cs` using `C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe /target:winexe /out:dist\ACTel.exe`.

- [ ] **Step 3: Create `allow_firewall.bat`**

Self-elevating batch script that adds Windows Firewall rules for inbound TCP 8080 and UDP 8088 (`netsh advfirewall firewall add rule name="ACTel Telemetry" dir=in action=allow protocol=TCP localport=8080`).

- [ ] **Step 4: Compile and test launcher**

Run: `node scripts/build-launcher.js`
Expected: `dist/ACTel.exe` created successfully.

- [ ] **Step 5: Commit**

```bash
git add src/launcher/ACTelLauncher.cs scripts/build-launcher.js allow_firewall.bat
git commit -m "feat(launcher): add native Windows tray launcher and firewall helper"
```

---

### Task 6: Portable Distribution Packaging & End-to-End Verification

**Files:**
- Create: `scripts/package-portable.js`
- Modify: `package.json`

**Interfaces:**
- Consumes: `dist/ACTel.exe`, `public/`, `lib/`, `ac_plugin/`, `download/actel.apk`, `server.js`, `package.json`, `node_modules/`.
- Produces: `dist/ACTel-v1.0.0-windows-x64.zip`.

- [ ] **Step 1: Create `scripts/package-portable.js`**

Packaging script:
1. Runs `npm run build:apk-bundle` (ensures `public/download/actel.apk` is up to date).
2. Runs `node scripts/build-launcher.js` (ensures `ACTel.exe` is compiled).
3. Creates a staging folder `dist/ACTel-v1.0.0-windows-x64/`:
   - `ACTel.exe`
   - `allow_firewall.bat`
   - `server.js`
   - `lib/`
   - `public/` (including `download/actel.apk`)
   - `ac_plugin/`
   - `node_modules/` (production dependencies: `express`, `ws`, `koffi`)
   - `package.json`
4. Creates ZIP archive `dist/ACTel-v1.0.0-windows-x64.zip` using PowerShell `Compress-Archive` or archiver.
5. Verifies ZIP integrity and size.

- [ ] **Step 2: Add `npm run package` script to `package.json`**

- [ ] **Step 3: Run packaging script and verify output**

Run: `npm run package`
Expected: `dist/ACTel-v1.0.0-windows-x64.zip` generated.

- [ ] **Step 4: End-to-End Test in a clean temporary directory**

1. Extract `dist/ACTel-v1.0.0-windows-x64.zip` to a temp directory (`C:\Temp\ACTel-Test`).
2. Verify all required files exist: `ACTel.exe`, `public/download/actel.apk`, `lib/acReader.js`, `ac_plugin/`.
3. Verify `/api/setup/network` and `/api/setup/bridge-status` respond with correct statuses.

- [ ] **Step 5: Commit and merge readiness**

```bash
git add scripts/package-portable.js package.json
git commit -m "feat(distribution): add portable ZIP packaging script and npm package command"
```

