// Live Telemetry Waveform Graphing Engine (HTML5 Canvas 60 FPS)

class TelemetryGraphController {
  constructor() {
    this.canvas = document.getElementById('telemetryCanvas');
    this.ctx = this.canvas ? this.canvas.getContext('2d') : null;
    this.history = [];
    this.maxPoints = 300; // ~6-10 seconds buffer at 50Hz

    this.clearBtn = document.getElementById('clearGraphBtn');
    if (this.clearBtn) {
      this.clearBtn.addEventListener('click', () => {
        this.history = [];
      });
    }

    this.handleResize();
    window.addEventListener('resize', () => this.handleResize());
  }

  handleResize() {
    if (!this.canvas) return;
    const parent = this.canvas.parentElement;
    if (parent) {
      this.canvas.width = parent.clientWidth;
      this.canvas.height = parent.clientHeight;
    }
  }

  pushData(telemetry) {
    if (!telemetry || !telemetry.physics) return;
    const phys = telemetry.physics;

    this.history.push({
      gas: phys.gas || 0,
      brake: phys.brake || 0,
      speedNorm: Math.min(1, (phys.speedKmh || 0) / 320), // 0-320 km/h normalized
      steerNorm: Math.max(-1, Math.min(1, (phys.steerAngle || 0) / 450)) // -450 to +450 normalized
    });

    if (this.history.length > this.maxPoints) {
      this.history.shift();
    }

    this.render();
  }

  render() {
    if (!this.ctx || !this.canvas || this.history.length < 2) return;
    const ctx = this.ctx;
    const w = this.canvas.width;
    const h = this.canvas.height;

    ctx.clearRect(0, 0, w, h);

    // Draw Grid Lines
    ctx.strokeStyle = '#141c2b';
    ctx.lineWidth = 1;
    for (let y = 0.25; y < 1; y += 0.25) {
      ctx.beginPath();
      ctx.moveTo(0, h * y);
      ctx.lineTo(w, h * y);
      ctx.stroke();
    }

    const step = w / (this.maxPoints - 1);
    const startIndex = Math.max(0, this.maxPoints - this.history.length);

    // Draw Channels:
    // 1. Gas (Green)
    this.drawChannel(ctx, (p) => 1 - p.gas, '#00ff88', 2, startIndex, step, h);

    // 2. Brake (Red)
    this.drawChannel(ctx, (p) => 1 - p.brake, '#ff0044', 2.5, startIndex, step, h);

    // 3. Speed (Cyan)
    this.drawChannel(ctx, (p) => 1 - p.speedNorm, '#00f0ff', 1.8, startIndex, step, h);

    // 4. Steer (Yellow, centered around middle)
    this.drawChannel(ctx, (p) => 0.5 - (p.steerNorm * 0.4), '#ffcc00', 1.5, startIndex, step, h);
  }

  drawChannel(ctx, valGetter, color, lineWidth, startIdx, step, h) {
    ctx.strokeStyle = color;
    ctx.lineWidth = lineWidth;
    ctx.beginPath();

    for (let i = 0; i < this.history.length; i++) {
      const x = (startIdx + i) * step;
      const normalizedY = valGetter(this.history[i]);
      const y = Math.max(5, Math.min(h - 5, normalizedY * h));

      if (i === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    }

    ctx.stroke();
  }
}

window.telemetryGraphController = new TelemetryGraphController();
