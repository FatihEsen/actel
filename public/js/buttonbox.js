// Virtual Button Box Controller & Tactile Audio/Haptics

class ButtonBoxController {
  constructor() {
    this.soundEnabled = true;
    this.audioCtx = null;
    this.buttons = document.querySelectorAll('.bb-btn');

    this.initButtons();
    this.initAudioToggle();
  }

  initAudioToggle() {
    const soundBtn = document.getElementById('soundToggleBtn');
    if (soundBtn) {
      soundBtn.addEventListener('click', () => {
        this.soundEnabled = !this.soundEnabled;
        soundBtn.classList.toggle('active', this.soundEnabled);
      });
    }
  }

  playClickSound() {
    if (!this.soundEnabled) return;
    try {
      if (!this.audioCtx) {
        const AudioContext = window.AudioContext || window.webkitAudioContext;
        this.audioCtx = new AudioContext();
      }

      if (this.audioCtx.state === 'suspended') {
        this.audioCtx.resume();
      }

      const osc = this.audioCtx.createOscillator();
      const gain = this.audioCtx.createGain();

      osc.type = 'triangle';
      osc.frequency.setValueAtTime(440, this.audioCtx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(120, this.audioCtx.currentTime + 0.04);

      gain.gain.setValueAtTime(0.3, this.audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, this.audioCtx.currentTime + 0.04);

      osc.connect(gain);
      gain.connect(this.audioCtx.destination);

      osc.start();
      osc.stop(this.audioCtx.currentTime + 0.05);
    } catch (e) {
      // Audio not supported or blocked
    }
  }

  triggerHaptic() {
    if (navigator.vibrate) {
      navigator.vibrate(35); // 35ms crisp vibration
    }
  }

  initButtons() {
    this.buttons.forEach(btn => {
      const action = btn.dataset.action;
      if (!action) return;

      const trigger = (e) => {
        if (e) e.preventDefault();
        btn.classList.add('pressed');
        this.playClickSound();
        this.triggerHaptic();

        this.sendAction(action);

        setTimeout(() => {
          btn.classList.remove('pressed');
        }, 160);
      };

      btn.addEventListener('click', trigger);
      btn.addEventListener('touchstart', trigger, { passive: false });
    });
  }

  sendAction(action) {
    // Send via WebSocket if open
    if (window.app && window.app.ws && window.app.ws.readyState === WebSocket.OPEN) {
      window.app.ws.send(JSON.stringify({
        type: 'control',
        action: action
      }));
    }

    // Also send via REST API for maximum reliability
    fetch('/api/control', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action })
    }).catch(err => console.error('Control error:', err));
  }
}

window.buttonBoxController = new ButtonBoxController();
