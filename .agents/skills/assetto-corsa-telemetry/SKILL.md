---
name: assetto-corsa-telemetry
description: >-
  Expert architectural knowledge and best practices for Assetto Corsa telemetry reading,
  bi-directional control injection, Sidekick parity algorithms (practical tyre temperatures,
  live delta spline interpolation, out-lap suppression), shared memory structures, and building
  low-latency sim racing dashboards (Web & Android). Use whenever developing, debugging, or
  extending Assetto Corsa telemetry, companion apps, button boxes, or HUDs.
---

# Assetto Corsa Telemetry & Dashboard Development Skill

This skill documents proven architectures, mathematical models, shared memory layouts, and implementation patterns learned from developing high-performance telemetry and control systems for Assetto Corsa (ACTel).

---

## 1. System Architecture

A complete, production-grade Assetto Corsa companion system requires three core tiers:

```
+--------------------------------------------------------------------------+
|                            ASSETTO CORSA                                 |
|  +--------------------------------+  +--------------------------------+  |
|  |     Shared Memory Mappings     |  |   Embedded Python Plugin App   |  |
|  |  (Physics, Graphics, Static)   |  |      ("actel_bridge")          |  |
|  +---------------+----------------+  +---------------+----------------+  |
+------------------|-----------------------------------|-------------------+
                   | Read-only (50-100Hz)              | IPC: Local\actel_cmd
                   v                                   v
+--------------------------------------------------------------------------+
|                          NODE.JS HOST SERVER                             |
|  * acReader.js        : Reads Windows Shared Memory (ctypes / C++ addon) |
|  * acController.js    : Writes commands to bridge IPC or Windows SendInput|
|  * ghostTracker.js    : Spline delta calculation & trend filtering       |
|  * carInfoReader.js   : Reads car/track ini specs for rev limits & boost |
|  * UDP Discovery      : Broadcasts server IP on port 8088                |
|  * WebSocket Server   : 50Hz low-latency JSON telemetry stream           |
+----------------------------------+---------------------------------------+
                                   | WebSocket (50Hz) + HTTP REST
                   +---------------+---------------+
                   v                               v
+-------------------------------------+  +---------------------------------+
|          WEB COCKPIT APP            |  |      ANDROID MOBILE APP         |
|  * HTML5 / CSS Grid Cockpit View    |  |  * Jetpack Compose + Canvas     |
|  * Digital SVG delta & tyre gauges  |  |  * Auto UDP Server Discovery    |
|  * Integrated Button Box Controller |  |  * Swipeable Dashboard/Controls |
+-------------------------------------+  +---------------------------------+
```

---

## 2. Assetto Corsa Shared Memory (`acpmf_*`)

Assetto Corsa exposes telemetry via three named memory-mapped files on Windows:

| Shared Memory Name | Update Rate | Purpose | Key Fields |
| :--- | :--- | :--- | :--- |
| `acpmf_physics` | ~100-333 Hz | Real-time vehicle physics | `speedKmh`, `rpms`, `gear`, `gas`, `brake`, `clutch`, `steerAngle`, `tyreCoreTemperature`, `tyreTempI/M/O`, `wheelsPressure`, `tyreWear`, `brakeTemp`, `turboBoost`, `kersCharge`, `kersInput`, `drsAvailable`, `drsEnabled`, `accG` |
| `acpmf_graphics` | Video FPS (~60Hz) | Session, timing & visual state | `status`, `session`, `currentTime`, `lastTime`, `bestTime`, `split`, `completedLaps`, `numberOfLaps`, `normalizedCarPosition`, `penaltyTime`, `flag`, `idealLineRPM`, `lightsStage` |
| `acpmf_static` | Session Init / Static | Vehicle & track static metadata | `carModel`, `track`, `playerName`, `playerSurname`, `maxTorque`, `maxPower`, `maxRpm`, `hasDRS`, `hasERS`, `hasKERS`, `isOnline` |

> [!IMPORTANT]
> **Memory Mapping Tips:**
> - String buffers in AC shared memory are UTF-16LE (`wchar_t`) fixed-length arrays. Always read with `16-bit` width and strip trailing null characters (`\0`).
> - `gear` value mapping: `0` = Reverse, `1` = Neutral, `2` = 1st Gear, `3` = 2nd Gear, etc. Display value is `gear - 1` (with `R` for 0, `N` for 1).

---

## 3. Sidekick Parity: Accurate Delta & Ghost Tracking

Achieving 1:1 mathematical parity with the industry standard **Sidekick** app requires solving three critical challenges:

### 3.1. Out-Lap & Pit Exit Suppression
When leaving the pit lane or starting a session (Out-Lap / Lap 0), lap times and spline position deltas produce erratic numbers (e.g., `+19.07s`).
- **Rule:** Suppress all ghost delta calculation while `carWasInPit` is true or `completedLaps == 0` until the car crosses the start/finish line on a timed flying lap.
- **Display:** Show `-.--` and render both trend arrows in an inactive/dim outline state.

### 3.2. Spline Position & Linear Interpolation
Tracking delta against personal best (PB) or session ghost requires sampling car progress (`normalizedCarPosition`, $0.0 \to 1.0$) and corresponding elapsed time.

Given recorded arrays `posList` and `timeList` (from the PB lap):
1. Perform a **binary search** to find the closest sample interval where $\text{posList}[i] \le \text{currentSpline} < \text{posList}[i+1]$.
2. Calculate the exact linear interpolation:
   $$c = \frac{\text{timeList}[i+1] - \text{timeList}[i]}{\text{posList}[i+1] - \text{posList}[i]}$$
   $$\text{targetTime} = \text{timeList}[i] + c \times (\text{currentSpline} - \text{posList}[i])$$
   $$\text{delta} = \text{currentTime} - \text{targetTime}$$

### 3.3. 2nd-Order Delta Trend Filter
Raw frame-to-frame delta fluctuates wildly due to bumps and track camber. Sidekick applies a second-order difference throttled to **100 ms**:

$$\Delta_{\text{trend}} = 2 \times \text{delta} - \text{prevDelta} - \text{prevDelta2}$$

- If $\Delta_{\text{trend}} < 0$: Gaining time / improving $\to$ **Green Up Arrow** ($\Uparrow$).
- If $\Delta_{\text{trend}} > 0$: Losing time / worsening $\to$ **Red Down Arrow** ($\Downarrow$).
- If $|\Delta_{\text{trend}}| \approx 0$: Delta stable $\to$ Inactive arrows.

### 3.4. Dual Semantic Color System
- **Delta Value Color:** Compares *absolute standing*:
  - Negative delta (e.g. `-0.45s` - ahead of ghost): **Green**.
  - Positive delta (e.g. `+1.20s` - behind ghost): **Red**.
- **Arrow Indicator Color:** Compares *instantaneous pace*:
  - A driver can be behind ghost (`+1.20s`, red text) but gaining time through a corner (**Green Up Arrow**).
  - A driver can be ahead (`-0.45s`, green text) but losing time on the straight (**Red Down Arrow**).

---

## 4. Sidekick Parity: Tyre Physics & Display Standards

### 4.1. Practical Temperature vs. Core Temperature
Assetto Corsa provides both `tyreCoreTemperature` (inner carcass) and surface tread temperatures (`tyreTempI`, `tyreTempM`, `tyreTempO`).
- **The Issue:** Displaying only core temperature makes the gauge sluggish; it ignores immediate tire scrub and lockup heating. Displaying only surface temperature makes the numbers fluctuate erratically.
- **Sidekick Formula (Practical Temperature):**
  $$\text{tempPractical} = 0.25 \times \left(\frac{T_{\text{Inner}} + T_{\text{Middle}} + T_{\text{Outer}}}{3}\right) + 0.75 \times T_{\text{Core}}$$
  This achieves the exact responsive, balanced feel sim racers expect.

### 4.2. Tyre Wear Scale
- `physics.tyreWear` in AC shared memory is **already a percentage** ($0.0 \to 100.0$, where $100.0$ is a brand new tire and $98.5$ is $1.5\%$ worn).
- **CRITICAL BUG TO AVOID:** Never multiply `tyreWear` by 100. Multiplying produces `9850%` or `10000%`.

### 4.3. Tyre Pressure
- `physics.wheelsPressure` provides values in **PSI**.
- Sidekick rounds to integer PSI (`27 PSI`). Dashboards can display 1 decimal place (`26.8 PSI`) for precision tuning.

---

## 5. Bi-Directional Game Control & State Synchronization

Assetto Corsa lacks external control APIs. Two-way control requires:

### 5.1. Embedded Python App Bridge
Deploy a lightweight Python app in `assettocorsa/apps/python/actel_bridge/`:
- Interacts with AC's internal Python module (`import ac`).
- Listens to commands over a Windows named shared memory or local UDP/socket.
- Calls native AC functions: `ac.setLightState()`, `ac.setHazardLightState()`, etc.

### 5.2. Windows `SendInput` Fallback
For functions without Python APIs (e.g., pit limiter, starter, ABS/TC adjustments):
- Send Windows Virtual Key codes (`VK_F`, `VK_H`, etc.) using `user32.dll!SendInput`.
- Pass `KEYEVENTF_EXTENDEDKEY` where applicable.

### 5.3. State Reconciliation
To prevent button boxes from becoming desynchronized with the sim:
- Always read actual state back from `acpmf_graphics.lightsStage` and telemetry flags.
- Update UI button indicators based on **confirmed game telemetry**, not optimistic click state.

---

## 6. Vehicle Feature Dynamic Adaptability

Cars in AC differ widely (GT3 vs. Hybrid LMP1 vs. Vintage Road Cars). A telemetry dashboard must dynamically adapt its UI:

1. **Turbo Boost Gauge:**
   - Detect via `static.hasTurbo` or `physics.turboBoost > 0`.
   - Keep the gauge visible when stationary if the car model is equipped with a turbocharger (do not hide just because RPM/boost is at idle).
2. **KERS / ERS System:**
   - Detect via `static.hasERS` or `static.hasKERS`.
   - Render dual indicators: **Charge / Battery Level** (`physics.kersCharge`) and **Deployment Input** (`physics.kersInput`).
3. **DRS (Drag Reduction System):**
   - Detect via `static.hasDRS`.
   - States: Available (`physics.drsAvailable == 1`, yellow/blue icon) vs Active (`physics.drsEnabled == 1`, green highlighted icon).

---

## 7. Cross-Platform UI & Connectivity Patterns

### 7.1. Zero-Config LAN UDP Discovery
Mobile companion apps should not require manual IP entry.
- Server broadcasts a UDP beacon on LAN (e.g., port `8088`) every 2 seconds:
  `{"app": "ACTel", "wsPort": 8080, "httpPort": 8080}`
- Mobile app listens on UDP `8088`, discovers server IP automatically, and initiates WebSocket handshake.

### 7.2. High-Performance Mobile Dashboard (Android Jetpack Compose)
- Use hardware-accelerated `Canvas` drawing for RPM dots and needle gauges to sustain smooth 60fps rendering.
- Use 7-segment digital monospace fonts for speed, gear, and delta numbers to prevent jitter.
- Implement `HorizontalPager` with touch swipe gestures for seamless navigation between Live Cockpit, Button Box, and Settings.

