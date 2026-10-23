// Tyre and Vehicle Diagnostics View Controller

class TyresController {
  constructor() {
    this.wheels = ['fl', 'fr', 'rl', 'rr'];

    this.gCanvas = document.getElementById('gForceCanvas');
    this.gCtx = this.gCanvas ? this.gCanvas.getContext('2d') : null;
    this.latGVal = document.getElementById('latGVal');
    this.lonGVal = document.getElementById('lonGVal');
    this.surfaceGripVal = document.getElementById('surfaceGripVal');

    this.pressureUnit = 'PSI'; // or 'BAR'
  }

  // Converts temperature to heat color (Cold blue -> Optimal green -> Hot yellow -> Overheat red)
  getTempColor(tempC) {
    if (tempC < 65) return '#3b82f6'; // Cold (Blue)
    if (tempC < 75) return '#06b6d4'; // Warming (Cyan)
    if (tempC <= 95) return '#10b981'; // Optimal (Green)
    if (tempC <= 105) return '#f59e0b'; // Hot (Yellow/Orange)
    return '#ef4444'; // Overheating (Red)
  }

  getBrakeTempColor(tempC) {
    if (tempC < 300) return '#94a3b8'; // Cold slate
    if (tempC < 550) return '#f97316'; // Normal warm orange
    if (tempC < 750) return '#ef4444'; // Hot red
    return '#ff0055'; // Glowing extreme red
  }

  update(telemetry) {
    if (!telemetry || !telemetry.physics) return;

    const phys = telemetry.physics;
    const tyres = phys.tyres || {};
    const graph = telemetry.graphic || {};

    // 1. Update 4 Tyres Matrix
    for (let i = 0; i < 4; i++) {
      const code = this.wheels[i];
      const press = tyres.pressure ? tyres.pressure[i] : 27.5;
      const core = tyres.tempCore ? tyres.tempCore[i] : 80;
      const ti = tyres.tempI ? tyres.tempI[i] : core + 2;
      const tm = tyres.tempM ? tyres.tempM[i] : core;
      const to = tyres.tempO ? tyres.tempO[i] : core - 2;
      const wear = tyres.wear ? tyres.wear[i] : 98;
      const brake = tyres.brakeTemp ? tyres.brakeTemp[i] : 400;

      const elCore = document.getElementById(`${code}TempCore`);
      const elPress = document.getElementById(`${code}Press`);
      const elWear = document.getElementById(`${code}Wear`);
      const elBrake = document.getElementById(`${code}Brake`);
      const elVisual = document.getElementById(`${code}Visual`);
      const elI = document.getElementById(`${code}TempI`);
      const elM = document.getElementById(`${code}TempM`);
      const elO = document.getElementById(`${code}TempO`);

      if (elCore) elCore.textContent = `${core}°C`;
      if (elI) elI.textContent = `${ti}°`;
      if (elM) elM.textContent = `${tm}°`;
      if (elO) elO.textContent = `${to}°`;

      if (elPress) {
        if (this.pressureUnit === 'BAR') {
          const barVal = (press * 0.0689476).toFixed(2);
          elPress.textContent = `${barVal} BAR`;
        } else {
          elPress.textContent = `${press.toFixed(1)} PSI`;
        }
      }

      if (elWear) elWear.textContent = `${wear.toFixed(1)}%`;

      if (elBrake) {
        elBrake.textContent = `${brake}°C`;
        elBrake.style.color = this.getBrakeTempColor(brake);
      }

      if (elVisual) {
        const color = this.getTempColor(core);
        elVisual.style.borderColor = color;
        elVisual.style.background = `radial-gradient(circle, ${color}33 0%, #0d121c 100%)`;
      }
    }

    // 2. G-Force Canvas Rendering
    const latG = phys.accG ? phys.accG.lateral : 0;
    const lonG = phys.accG ? phys.accG.longitudinal : 0;

    if (this.latGVal) this.latGVal.textContent = `${latG >= 0 ? '+' : ''}${latG.toFixed(2)} G`;
    if (this.lonGVal) this.lonGVal.textContent = `${lonG >= 0 ? '+' : ''}${lonG.toFixed(2)} G`;

    if (this.surfaceGripVal && graph.surfaceGrip) {
      this.surfaceGripVal.textContent = `${graph.surfaceGrip}%`;
    }

    this.drawGForceMeter(latG, lonG);
  }

  drawGForceMeter(latG, lonG) {
    if (!this.gCtx) return;
    const ctx = this.gCtx;
    const w = this.gCanvas.width;
    const h = this.gCanvas.height;
    const cx = w / 2;
    const cy = h / 2;
    const maxRadius = cx - 12;

    ctx.clearRect(0, 0, w, h);

    // Draw concentric circles (0.5G, 1.0G, 1.5G, 2.0G)
    const rings = [0.25, 0.5, 0.75, 1.0];
    rings.forEach((rRatio, idx) => {
      ctx.beginPath();
      ctx.arc(cx, cy, maxRadius * rRatio, 0, Math.PI * 2);
      ctx.strokeStyle = idx === 3 ? '#2a384f' : '#172030';
      ctx.lineWidth = 1.5;
      ctx.stroke();
    });

    // Crosshairs
    ctx.beginPath();
    ctx.moveTo(cx, 10);
    ctx.lineTo(cx, h - 10);
    ctx.moveTo(10, cy);
    ctx.lineTo(w - 10, cy);
    ctx.strokeStyle = '#1a2436';
    ctx.lineWidth = 1;
    ctx.stroke();

    // Calculate ball position (assuming max 2.5G range)
    const scale = maxRadius / 2.5;
    const ballX = Math.max(12, Math.min(w - 12, cx + (latG * scale)));
    const ballY = Math.max(12, Math.min(h - 12, cy - (lonG * scale)));

    // Ball glow trail
    const grad = ctx.createRadialGradient(ballX, ballY, 2, ballX, ballY, 14);
    grad.addColorStop(0, '#00f0ff');
    grad.addColorStop(0.5, 'rgba(0, 240, 255, 0.4)');
    grad.addColorStop(1, 'rgba(0, 240, 255, 0)');

    ctx.fillStyle = grad;
    ctx.beginPath();
    ctx.arc(ballX, ballY, 14, 0, Math.PI * 2);
    ctx.fill();

    // Center dot
    ctx.fillStyle = '#ffffff';
    ctx.beginPath();
    ctx.arc(ballX, ballY, 4, 0, Math.PI * 2);
    ctx.fill();
  }
}

window.tyresController = new TyresController();
