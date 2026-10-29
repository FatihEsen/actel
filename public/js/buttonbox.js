// Virtual Button Box Controller & Tactile Audio/Haptics
// Supports 5x6 grid, momentary hold buttons (KERS, DRS), and instant haptic/sound response

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

  triggerHaptic(duration = 35) {
    if (navigator.vibrate) {
      try {
        navigator.vibrate(duration);
      } catch (_) {}
    }
  }

  initButtons() {
    this.buttons.forEach(btn => {
      const action = btn.dataset.action;
      if (!action) return;

      const isHold = btn.dataset.hold === 'true';

      if (isHold) {
        let isDown = false;

        const onDown = (e) => {
          if (e) e.preventDefault();
          if (isDown) return;
          isDown = true;
          btn.classList.add('pressed');
          this.playClickSound();
          this.triggerHaptic(45);
          this.sendAction(action, 'down');
        };

        const onUp = (e) => {
          if (e) e.preventDefault();
          if (!isDown) return;
          isDown = false;
          btn.classList.remove('pressed');
          this.sendAction(action, 'up');
        };

        btn.addEventListener('pointerdown', onDown);
        btn.addEventListener('pointerup', onUp);
        btn.addEventListener('pointercancel', onUp);
        btn.addEventListener('mouseleave', onUp);
      } else {
        const trigger = (e) => {
          if (e) e.preventDefault();
          btn.classList.add('pressed');
          this.playClickSound();
          this.triggerHaptic(35);

          this.sendAction(action, null);

// Auto-switch to dashboard removed per user preference

          setTimeout(() => {
            btn.classList.remove('pressed');
          }, 160);
        };

        btn.addEventListener('click', trigger);
      }
    });
  }

  sendAction(action, state = null) {
    const payload = {
      type: 'control',
      action: action
    };
    if (state) payload.state = state;

    // Send via WebSocket if open
    if (window.app && window.app.ws && window.app.ws.readyState === WebSocket.OPEN) {
      window.app.ws.send(JSON.stringify(payload));
      return;
    }

    // Fallback to REST API only when WebSocket is not open
    fetch('/api/control', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    }).catch(err => console.error('Control error:', err));
  }
}

window.buttonBoxController = new ButtonBoxController();
