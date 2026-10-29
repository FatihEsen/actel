# ACTel Development & Telemetry Guardrails

These rules must be strictly adhered to whenever modifying or debugging code in this repository.

## 1. Assetto Corsa Telemetry Invariants
- **Tyre Wear Units:** `physics.tyreWear` in Assetto Corsa shared memory is ALREADY a percentage (0.0 to 100.0). NEVER multiply it by 100.
- **Tyre Temperature (Sidekick Parity):** Never display raw `tyreCoreTemperature` alone for UI gauges. Always compute practical temperature:
  `tempPractical = 0.25 * ((tempI + tempM + tempO) / 3.0) + 0.75 * tempCore`.
- **Out-Lap & In-Pit Delta Suppression:** When the car is in the pit lane or on an out-lap (`completedLaps == 0`), suppress ghost delta (`-.--`) and set delta trend arrows to inactive until crossing the start/finish line.
- **Ghost Trend Filter:** Always throttle delta trend calculations to ~100ms and use the second-order difference formula:
  `trend = 2 * delta - prevDelta - prevDelta2`.
- **Dual Semantic Color Invariant:** Delta time color indicates absolute standing (- ahead / + behind); trend arrows indicate instantaneous delta progression (improving / worsening).

## 2. Bi-Directional Button Box & Control Invariants
- **Authoritative Game State:** Never rely solely on client-side optimistic click states for vehicle toggles (headlights, hazards, wipers, pit limiter).
- Button indicator lights must always reconcile with confirmed game telemetry (`graphics.lightsStage`, hazard state) to prevent desynchronization during session restarts or in-game toggles.

## 3. Tooling & Environment Guardrails
- **Binary Captures on Windows:** Never redirect binary streams (e.g. `adb exec-out screencap -p > file.png`) directly in PowerShell, as it corrupts the binary with UTF-16LE encoding. Always use `adb pull` or `cmd /c "... > file.png"`.

