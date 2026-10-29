// Dashboard (MoTeC / GT3 Digital Cluster) View Controller

class DashboardController {
  constructor() {
    this.shiftLeds = Array.from(document.querySelectorAll('#shiftLightsBar .led'));
    this.totalLeds = this.shiftLeds.length; // 16 LEDs

    // DOM Elements - Left Column
    this.elCarName = document.getElementById('dashCarName');
    this.elTrackName = document.getElementById('dashTrackName');
    this.dashTimingCard = document.getElementById('dashTimingCard');
    this.elLapPill = document.getElementById('dashLapPill');
    this.elSecPill = document.getElementById('dashSecPill');

    this.elCurrentLap = document.getElementById('dashCurrentLap');
    this.elDelta = document.getElementById('dashDelta');
    this.elBestLap = document.getElementById('dashBestLap');
    this.elLastLap = document.getElementById('dashLastLap');

    this.elStripTc = document.getElementById('stripTc');
    this.elStripAbs = document.getElementById('stripAbs');
    this.elStripBb = document.getElementById('stripBb');
    this.elStripDrs = document.getElementById('stripDrs');

    // Center Column - Speed & Turn Indicators
    this.elGear = document.getElementById('dashGear');
    this.elSpeed = document.getElementById('dashSpeed');
    this.elRpm = document.getElementById('dashRpm');
    this.rpmProgressBar = document.getElementById('rpmProgressBar');
    this.shiftLightsBar = document.getElementById('shiftLightsBar');

    // Cockpit Interactive Indicators (Turn, Hazard, Pit, Lights, Flasher)
    this.indTurnLeft = document.getElementById('indTurnLeft');
    this.indTurnRight = document.getElementById('indTurnRight');
    this.indHazard = document.getElementById('indHazard');
    this.indPit = document.getElementById('indPit');
    this.indLights = document.getElementById('indLights');
    this.indFlash = document.getElementById('indFlash');

    // Gear Corner Status Badges (ABS, TC, DRS, BB)
    this.indAbs = document.getElementById('indAbs');
    this.indTc = document.getElementById('indTc');
    this.indDrs = document.getElementById('indDrs');
    this.indBbVal = document.getElementById('indBbVal');

    // Live Delta & Opponent/Ghost Gap Card
    this.liveDeltaVal = document.getElementById('liveDeltaVal');
    this.liveDeltaTrend = document.getElementById('liveDeltaTrend');
    this.dashPosBadge = document.getElementById('dashPosBadge');
    this.deltaBarGain = document.getElementById('deltaBarGain');
    this.deltaBarLoss = document.getElementById('deltaBarLoss');
    this.gapAheadVal = document.getElementById('gapAheadVal');
    this.gapBehindVal = document.getElementById('gapBehindVal');
    this.dashGhostDelta = document.getElementById('dashGhostDelta');
    this.ghostArrowUp = document.getElementById('ghostArrowUp');
    this.ghostArrowDown = document.getElementById('ghostArrowDown');

    // Wire clicks to turn signals, hazard, pit limiter, lights, and flasher
    this.setupIndicatorClicks();

    // Right Column - Upper (Pedals & G-Force)
    this.pedalGas = document.getElementById('pedalGas');
    this.pedalBrake = document.getElementById('pedalBrake');
    this.pedalClutch = document.getElementById('pedalClutch');
    this.elPedalGasVal = document.getElementById('pedalGasVal');
    this.elPedalBrakeVal = document.getElementById('pedalBrakeVal');
    this.elPedalClutchVal = document.getElementById('pedalClutchVal');

    this.gforceCanvas = document.getElementById('gforceCanvas');
    this.elGforceVal = document.getElementById('gforceVal');
    if (this.gforceCanvas) {
      this.gforceCtx = this.gforceCanvas.getContext('2d');
    }

    // Right Column - Lower (Compact Tyres)
    this.ctTyres = {
      FL: { temp: document.getElementById('ctTempFL'), psi: document.getElementById('ctPsiFL'), wear: document.getElementById('ctWearFL') },
      FR: { temp: document.getElementById('ctTempFR'), psi: document.getElementById('ctPsiFR'), wear: document.getElementById('ctWearFR') },
      RL: { temp: document.getElementById('ctTempRL'), psi: document.getElementById('ctPsiRL'), wear: document.getElementById('ctWearRL') },
      RR: { temp: document.getElementById('ctTempRR'), psi: document.getElementById('ctPsiRR'), wear: document.getElementById('ctWearRR') }
    };
  }

  setupIndicatorClicks() {
    const send = (action) => {
      if (window.buttonBoxController && typeof window.buttonBoxController.sendAction === 'function') {
        window.buttonBoxController.sendAction(action);
      }
    };

    if (this.indTurnLeft) {
      this.indTurnLeft.addEventListener('click', () => {
        send('turn_left');
        const next = !this.indTurnLeft.classList.contains('active');
        this.indTurnLeft.classList.toggle('active', next);
        if (next && this.indTurnRight) this.indTurnRight.classList.remove('active');
      });
    }
    if (this.indTurnRight) {
      this.indTurnRight.addEventListener('click', () => {
        send('turn_right');
        const next = !this.indTurnRight.classList.contains('active');
        this.indTurnRight.classList.toggle('active', next);
        if (next && this.indTurnLeft) this.indTurnLeft.classList.remove('active');
      });
    }
    if (this.indHazard) {
      this.indHazard.addEventListener('click', () => {
        send('hazard');
        const next = !this.indHazard.classList.contains('active');
        this.indHazard.classList.toggle('active', next);
        if (this.indTurnLeft) this.indTurnLeft.classList.toggle('active', next);
        if (this.indTurnRight) this.indTurnRight.classList.toggle('active', next);
      });
    }
    if (this.indLights) {
      this.indLights.addEventListener('click', () => {
        send('headlights');
        this.indLights.classList.toggle('active');
      });
    }
    if (this.indFlash) {
      this.indFlash.addEventListener('click', () => {
        send('flasher');
        this.indFlash.classList.add('active');
        setTimeout(() => this.indFlash.classList.remove('active'), 450);
      });
    }
    if (this.indPit) {
      this.indPit.addEventListener('click', () => {
        send('pit_limiter');
        this.indPit.classList.toggle('active');
      });
    }
    if (this.indDrs) {
      this.indDrs.addEventListener('click', () => send('drs'));
    }
  }

  update(telemetry) {
    if (!telemetry || !telemetry.physics) return;

    const phys = telemetry.physics;
    const graph = telemetry.graphic || {};
    const stat = telemetry.staticInfo || {};

    const maxRpm = stat.limiterRpm || stat.maxRpm || 8500;
    const currentRpm = phys.rpms || 0;

    // 1. Shift Lights & RPM Bar
    if (this.elRpm) this.elRpm.textContent = currentRpm.toLocaleString();

    const rpmPercent = Math.max(0, Math.min(1, currentRpm / maxRpm));
    if (this.rpmProgressBar) {
      this.rpmProgressBar.style.width = `${(rpmPercent * 100).toFixed(1)}%`;
      if (rpmPercent > 0.92) {
        this.rpmProgressBar.style.background = '#ff0044';
      } else if (rpmPercent > 0.78) {
        this.rpmProgressBar.style.background = '#ffcc00';
      } else {
        this.rpmProgressBar.style.background = '#00f0ff';
      }
    }

    // Shift light calculation: 1 dot steadily lit at idle, proportional rise to redline
    const idleThreshold = 0.12;
    const shiftRedline = 0.96;

    if (this.shiftLightsBar) {
      if (currentRpm <= 50) {
        this.shiftLeds.forEach(led => led.classList.remove('lit'));
        this.shiftLightsBar.classList.remove('flash-all');
      } else if (rpmPercent >= shiftRedline) {
        this.shiftLeds.forEach(led => led.classList.add('lit'));
        this.shiftLightsBar.classList.add('flash-all');
      } else {
        this.shiftLightsBar.classList.remove('flash-all');
        let ledsToLight = 1;
        if (rpmPercent > idleThreshold) {
          const fraction = Math.min(1, Math.max(0, (rpmPercent - idleThreshold) / (shiftRedline - idleThreshold)));
          ledsToLight = 1 + Math.round(fraction * 15);
        }

        this.shiftLeds.forEach((led, idx) => {
          if (idx < ledsToLight) led.classList.add('lit');
          else led.classList.remove('lit');
        });
      }
    }

    // 2. Car & Track Name (Above Timer)
    if (this.elCarName) {
      this.elCarName.textContent = stat.carName || stat.carModel || '---';
    }
    if (this.elTrackName) {
      this.elTrackName.textContent = stat.trackName || stat.track || '---';
    }

    // 3. Timing, Delta & Sectors
    if (this.elLapPill) this.elLapPill.textContent = `TUR ${graph.completedLaps || 0}`;
    if (this.elSecPill) this.elSecPill.textContent = `SEKTÖR S${graph.currentSectorIndex !== undefined ? graph.currentSectorIndex : 1}`;

    if (this.dashTimingCard) {
      this.dashTimingCard.classList.toggle('lap-cut', !!telemetry.isLapCut);
    }
    if (this.elCurrentLap) {
      this.elCurrentLap.textContent = graph.currentTime || '0:00.000';
      this.elCurrentLap.classList.toggle('cut-lap', !!telemetry.isLapCut);
    }
    if (this.elLastLap && graph.lastTime) this.elLastLap.textContent = graph.lastTime;
    if (this.elBestLap && graph.bestTime) this.elBestLap.textContent = graph.bestTime;

    if (this.elDelta && graph.split) {
      this.elDelta.textContent = graph.split;
      const numDelta = parseFloat(graph.split);
      this.elDelta.className = 'timing-val mono ' + (numDelta < 0 ? 'delta-good' : (numDelta > 0 ? 'delta-bad' : 'delta-neutral'));
    }

    // 4. Setup Strip (TC, ABS, BB, DRS) - Legacy / fallback support if present
    if (this.elStripTc) this.elStripTc.textContent = Math.round(phys.tc || 0);
    if (this.elStripAbs) this.elStripAbs.textContent = Math.round(phys.abs || 0);
    if (this.elStripBb) this.elStripBb.textContent = `${((phys.brakeBias || 0.58) * 100).toFixed(1)}%`;
    if (this.elStripDrs) {
      const drsAvailable = phys.drsAvailable;
      const drsOn = phys.drs || phys.drsEnabled;
      const batteryVal = (phys.kersCharge !== undefined && phys.kersCharge !== null) ? Math.round(phys.kersCharge) : 100;
      this.elStripDrs.textContent = `${batteryVal}%`;
      if (drsAvailable || drsOn) {
        this.elStripDrs.style.color = '#00ff88';
      } else {
        this.elStripDrs.style.color = '#ff3344';
      }
    }

    // 5. Cockpit Interactive Indicators (Turn Signals, Hazard, Headlights, Flasher)
    const ind = telemetry.indicators || {};
    const isLeftActive = !!(ind.turnLeft || ind.hazard);
    const isRightActive = !!(ind.turnRight || ind.hazard);
    if (this.indTurnLeft) {
      this.indTurnLeft.classList.toggle('active', isLeftActive);
    }
    if (this.indTurnRight) {
      this.indTurnRight.classList.toggle('active', isRightActive);
    }
    if (this.indHazard) {
      this.indHazard.classList.toggle('active', !!ind.hazard);
    }
    if (this.indLights) {
      this.indLights.classList.toggle('active', !!ind.headlights);
    }
    if (this.indFlash) {
      this.indFlash.classList.toggle('active', !!ind.flasher);
    }

    // 6. Cockpit Status Badges (ABS, TC, DRS, PIT, BB)
    if (this.indAbs) {
      // Solid steady active flag during braking wheel slip
      this.indAbs.classList.toggle('active', !!phys.absActive);
    }
    if (this.indPit) {
      const isPitActive = Boolean(phys.pitLimiterOn || graph.isInPitLane || graph.isInPit || ind.pitLimiter);
      this.indPit.classList.toggle('active', isPitActive);
    }
    if (this.indTc) {
      // Solid steady active flag during throttle wheelspin
      this.indTc.classList.toggle('active', !!phys.tcActive);
    }
    if (this.indDrs) {
      const drsAvail = !!phys.drsAvailable;
      const drsActive = !!(phys.drs || phys.drsEnabled);
      this.indDrs.classList.toggle('available', drsAvail);
      this.indDrs.classList.toggle('active', drsActive);
    }
    if (this.indBbVal) {
      const bbVal = phys.brakeBias !== undefined ? (phys.brakeBias * 100).toFixed(1) : '58.0';
      this.indBbVal.textContent = `${bbVal}%`;
    }

    // 7. Live Delta & Ghost/Opponent Gap Display
    let deltaSec = 0;
    let hasValidDelta = false;

    if (typeof phys.performanceMeter === 'number' && Math.abs(phys.performanceMeter) > 0.0001) {
      deltaSec = phys.performanceMeter;
      hasValidDelta = true;
    } else if (graph.split && typeof graph.split === 'string' && graph.split.trim() !== '' && graph.split !== '0.000') {
      const parsed = parseFloat(graph.split);
      if (!isNaN(parsed) && Math.abs(parsed) > 0.0001) {
        deltaSec = parsed;
        hasValidDelta = true;
      }
    }

    if (this.liveDeltaVal) {
      if (!hasValidDelta) {
        this.liveDeltaVal.textContent = '+0.00';
        this.liveDeltaVal.className = 'live-delta-num mono delta-neutral';
        if (this.liveDeltaTrend) {
          this.liveDeltaTrend.textContent = '▲';
          this.liveDeltaTrend.className = 'delta-arrow-icon delta-neutral';
        }
      } else {
        const sign = deltaSec > 0 ? '+' : (deltaSec < 0 ? '-' : '');
        const absVal = Math.abs(deltaSec).toFixed(2);
        this.liveDeltaVal.textContent = `${sign}${absVal}`;
        const isGood = deltaSec < -0.01;
        const isBad = deltaSec > 0.01;
        this.liveDeltaVal.className = 'live-delta-num mono ' + (isGood ? 'text-green' : (isBad ? 'text-red' : 'delta-neutral'));
        if (this.liveDeltaTrend) {
          this.liveDeltaTrend.textContent = isBad ? '▼' : '▲';
          this.liveDeltaTrend.className = 'delta-arrow-icon ' + (isGood ? 'text-green' : (isBad ? 'text-red' : 'delta-neutral'));
        }
      }
    }

    // Center-origin dynamic delta bar (+/- 3.00s max scale)
    const maxDeltaSpan = 3.0;
    if (this.deltaBarGain && this.deltaBarLoss) {
      if (hasValidDelta && deltaSec < 0) {
        // Gaining time -> green extends left from center
        const gainPct = Math.min(100, (Math.abs(deltaSec) / maxDeltaSpan) * 100);
        this.deltaBarGain.style.width = `${gainPct.toFixed(1)}%`;
        this.deltaBarLoss.style.width = '0%';
      } else if (hasValidDelta && deltaSec > 0) {
        // Losing time -> red extends right from center
        const lossPct = Math.min(100, (deltaSec / maxDeltaSpan) * 100);
        this.deltaBarGain.style.width = '0%';
        this.deltaBarLoss.style.width = `${lossPct.toFixed(1)}%`;
      } else {
        this.deltaBarGain.style.width = '0%';
        this.deltaBarLoss.style.width = '0%';
      }
    }

    // Race Position Badge
    if (this.dashPosBadge) {
      const pos = graph.position || 1;
      const total = graph.totalCars || 16;
      this.dashPosBadge.textContent = `P${pos} / ${total}`;
    }

    // Gaps to Car Ahead (or Ghost) and Car Behind
    // Ghost Delta & Arrows
    const ghostVal = (telemetry.ghostDelta !== undefined && telemetry.ghostDelta !== null && telemetry.ghostDelta !== '') ? telemetry.ghostDelta : '-.--';
    if (this.dashGhostDelta) {
      this.dashGhostDelta.textContent = ghostVal;
      const isAhead = ghostVal !== '-.--' && ghostVal.startsWith('-');
      const isNeutral = ghostVal === '-.--' || ghostVal === '+0.00s' || ghostVal === '0.00s';
      this.dashGhostDelta.className = 'ghost-time mono ' + (isAhead ? 'text-green' : (isNeutral ? 'text-neutral' : 'text-red'));
    }
    if (this.ghostArrowUp) {
      this.ghostArrowUp.classList.toggle('active', telemetry.deltaTrend === 'improving');
    }
    if (this.ghostArrowDown) {
      this.ghostArrowDown.classList.toggle('active', telemetry.deltaTrend === 'worsening');
    }
    if (this.gapAheadVal) {
      this.gapAheadVal.textContent = ghostVal;
    }

    // 8. Speed & Gear
    if (this.elSpeed) this.elSpeed.textContent = phys.speedKmh || 0;
    if (this.elGear) {
      this.elGear.textContent = phys.gearStr || 'N';
      if (phys.gearStr === 'R') this.elGear.style.color = '#ff0044';
      else if (phys.gearStr === 'N') this.elGear.style.color = '#ffcc00';
      else this.elGear.style.color = '#00f0ff';
    }

    // 6. Pedals (CLU, BRA, THR)
    const clutchPct = Math.round((phys.clutch || 0) * 100);
    const brakePct = Math.round((phys.brake || 0) * 100);
    const gasPct = Math.round((phys.gas || 0) * 100);

    if (this.pedalClutch) this.pedalClutch.style.height = `${clutchPct}%`;
    if (this.pedalBrake) this.pedalBrake.style.height = `${brakePct}%`;
    if (this.pedalGas) this.pedalGas.style.height = `${gasPct}%`;

    if (this.elPedalClutchVal) this.elPedalClutchVal.textContent = `${clutchPct}%`;
    if (this.elPedalBrakeVal) this.elPedalBrakeVal.textContent = `${brakePct}%`;
    if (this.elPedalGasVal) this.elPedalGasVal.textContent = `${gasPct}%`;

    // 7. G-Force Meter
    const latG = (phys.accG && phys.accG.lateral) ? phys.accG.lateral : 0;
    const lonG = (phys.accG && phys.accG.longitudinal) ? phys.accG.longitudinal : 0;

    if (this.elGforceVal) {
      this.elGforceVal.textContent = `LAT: ${latG.toFixed(2)}G`;
    }

    if (this.gforceCtx && this.gforceCanvas) {
      this.drawGForce(latG, lonG);
    }

    // 8. Compact Tyres (FL: 0, FR: 1, RL: 2, RR: 3)
    const tyres = phys.tyres || {};
    const pressures = tyres.pressure || [0, 0, 0, 0];
    const temps = tyres.tempPractical || tyres.tempCore || [0, 0, 0, 0];
    const wears = tyres.wear || [100, 100, 100, 100];

    const keys = ['FL', 'FR', 'RL', 'RR'];
    keys.forEach((k, idx) => {
      const cell = this.ctTyres[k];
      if (cell) {
        const p = pressures[idx] || 0;
        const t = temps[idx] || 0;
        const rawW = wears[idx] !== undefined ? wears[idx] : 100;
        const w = rawW > 100 ? rawW / 100 : rawW;

        if (cell.psi) cell.psi.textContent = p.toFixed(1);
        if (cell.temp) {
          cell.temp.textContent = `${Math.round(t)}°C`;
          cell.temp.style.color = this.getTyreTempColor(t);
        }
        if (cell.wear) {
          cell.wear.textContent = `${Math.round(w)}%`;
          cell.wear.style.color = w > 75 ? '#00ff88' : (w > 50 ? '#ffcc00' : '#ff0044');
        }
      }
    });
  }

  getTyreTempColor(temp) {
    if (temp < 65) return '#00a2ff'; // Cold
    if (temp < 75) return '#00f0ff'; // Warm
    if (temp < 98) return '#00ff88'; // Optimal
    return '#ff0044'; // Hot
  }

  drawGForce(latG, lonG) {
    const ctx = this.gforceCtx;
    const w = this.gforceCanvas.width;
    const h = this.gforceCanvas.height;
    const cx = w / 2;
    const cy = h / 2;
    const r = Math.min(cx, cy) - 4;

    ctx.clearRect(0, 0, w, h);

    // Target Circles
    ctx.strokeStyle = '#263042';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.arc(cx, cy, r, 0, Math.PI * 2);
    ctx.stroke();

    ctx.strokeStyle = '#1a2230';
    ctx.beginPath();
    ctx.arc(cx, cy, r * 0.5, 0, Math.PI * 2);
    ctx.stroke();

    // Crosshairs
    ctx.strokeStyle = '#263042';
    ctx.beginPath();
    ctx.moveTo(cx, cy - r);
    ctx.lineTo(cx, cy + r);
    ctx.moveTo(cx - r, cy);
    ctx.lineTo(cx + r, cy);
    ctx.stroke();

    // Dot
    const maxG = 2.5;
    const clampLat = Math.max(-1, Math.min(1, latG / maxG));
    const clampLon = Math.max(-1, Math.min(1, -lonG / maxG));

    const dotX = cx + clampLat * r;
    const dotY = cy + clampLon * r;

    // Glowing dot
    ctx.fillStyle = 'rgba(0, 240, 255, 0.35)';
    ctx.beginPath();
    ctx.arc(dotX, dotY, 6, 0, Math.PI * 2);
    ctx.fill();

    ctx.fillStyle = '#00f0ff';
    ctx.beginPath();
    ctx.arc(dotX, dotY, 3.5, 0, Math.PI * 2);
    ctx.fill();
  }
}

window.dashboardController = new DashboardController();
