// Main Application Controller (WebSocket, Tabs, Modals, State)

class App {
  constructor() {
    this.ws = null;
    this.reconnectTimer = null;
    this.currentTab = 'dashboard';
    this.mode = 'AUTO'; // 'AUTO', 'LIVE_ONLY', 'DEMO_ONLY'

    // DOM Elements
    this.connBadge = document.getElementById('connectionBadge');
    this.carName = document.getElementById('carName');
    this.trackName = document.getElementById('trackName');
    this.sessionType = document.getElementById('sessionType');
    this.modeToggleBtn = document.getElementById('modeToggleBtn');
    this.modeText = document.getElementById('modeText');

    this.qrModal = document.getElementById('qrModal');
    this.qrModalBtn = document.getElementById('qrModalBtn');
    this.closeQrModal = document.getElementById('closeQrModal');
    this.ipUrlsList = document.getElementById('ipUrlsList');

    this.fullscreenBtn = document.getElementById('fullscreenBtn');

    this.cfgAcStatus = document.getElementById('cfgAcStatus');
    this.cfgModeSelect = document.getElementById('cfgModeSelect');
    this.cfgPressureUnit = document.getElementById('cfgPressureUnit');
    this.bindingsList = document.getElementById('bindingsList');
    this.htSpeed = document.getElementById('htSpeed');
    this.htRpm = document.getElementById('htRpm');
    this.htGear = document.getElementById('htGear');

    this.initTabs();
    this.initModals();
    this.initFullscreen();
    this.initSettings();
    this.initWebSocket();
    this.fetchStatus();
    this.fetchBindings();
  }

  // Tab switching (Supports nav tabs and header settings button)
  initTabs() {
    const tabs = document.querySelectorAll('.nav-tab, [data-tab]');
    const views = document.querySelectorAll('.tab-view');

    tabs.forEach(tab => {
      tab.addEventListener('click', () => {
        const target = tab.dataset.tab;
        if (!target) return;

        tabs.forEach(t => t.classList.remove('active'));
        views.forEach(v => v.classList.remove('active'));

        tab.classList.add('active');
        const activeView = document.getElementById(`view-${target}`);
        if (activeView) activeView.classList.add('active');

        this.currentTab = target;

        if (target === 'telemetry' && window.telemetryGraphController) {
          window.telemetryGraphController.handleResize();
        }
      });
    });

    const initialHash = window.location.hash.replace("#", "");
    if (initialHash) {
      const match = document.querySelector(`[data-tab="${initialHash}"]`);
      if (match) match.click();
    }
  }

  // Fullscreen support
  initFullscreen() {
    if (this.fullscreenBtn) {
      this.fullscreenBtn.addEventListener('click', () => {
        if (!document.fullscreenElement) {
          document.documentElement.requestFullscreen().catch(err => {
            console.warn('Fullscreen error:', err);
          });
        } else {
          document.exitFullscreen().catch(err => {
            console.warn('Exit fullscreen error:', err);
          });
        }
      });
    }
  }

  // Modal and QR Code
  initModals() {
    if (this.qrModalBtn) {
      this.qrModalBtn.addEventListener('click', () => {
        this.fetchStatus();
        this.qrModal.classList.remove('hidden');
      });
    }

    if (this.closeQrModal) {
      this.closeQrModal.addEventListener('click', () => {
        this.qrModal.classList.add('hidden');
      });
    }

    this.qrModal.addEventListener('click', (e) => {
      if (e.target === this.qrModal) {
        this.qrModal.classList.add('hidden');
      }
    });

    if (this.modeToggleBtn) {
      this.modeToggleBtn.addEventListener('click', () => {
        const nextMode = this.mode === 'AUTO' ? 'DEMO_ONLY' : (this.mode === 'DEMO_ONLY' ? 'LIVE_ONLY' : 'AUTO');
        this.setMode(nextMode);
      });
    }
  }

  setMode(newMode) {
    this.mode = newMode;
    fetch('/api/mode', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ mode: newMode })
    })
    .then(r => r.json())
    .then(() => {
      this.updateModeUI();
    })
    .catch(err => console.error(err));
  }

  updateModeUI(source) {
    if (this.cfgModeSelect) {
      this.cfgModeSelect.value = this.mode;
    }

    if (this.modeText && this.modeToggleBtn) {
      if (source === 'AC_LIVE' || this.mode === 'LIVE_ONLY') {
        this.modeText.textContent = 'CANLI AC';
        this.modeToggleBtn.className = 'action-btn mode-btn live';
      } else {
        this.modeText.textContent = 'DEMO MODU';
        this.modeToggleBtn.className = 'action-btn mode-btn';
      }
    }
  }

  // Settings view
  initSettings() {
    if (this.cfgModeSelect) {
      this.cfgModeSelect.addEventListener('change', (e) => {
        this.setMode(e.target.value);
      });
    }

    if (this.cfgPressureUnit) {
      this.cfgPressureUnit.addEventListener('change', (e) => {
        if (window.tyresController) {
          window.tyresController.pressureUnit = e.target.value;
        }
      });
    }
  }

  fetchStatus() {
    fetch('/api/status')
      .then(r => r.json())
      .then(data => {
        this.mode = data.mode;
        this.updateModeUI(data.acConnected ? 'AC_LIVE' : 'DEMO');

        if (this.cfgAcStatus) {
          this.cfgAcStatus.textContent = data.acConnected ? 'BAĞLI (ÇALIŞIYOR)' : 'KAPALI (BEKLENİYOR)';
          this.cfgAcStatus.className = 'badge ' + (data.acConnected ? 'status-badge connected' : 'status-badge disconnected');
        }

        // Render IP links and QR code
        if (this.ipUrlsList && data.localIps) {
          this.ipUrlsList.innerHTML = '';
          data.localIps.forEach(net => {
            const row = document.createElement('div');
            row.className = 'ip-item';
            row.innerHTML = `
              <span class="pill-label">${net.interface}:</span>
              <a href="${net.url}" target="_blank" class="ip-link">${net.url}</a>
            `;
            this.ipUrlsList.appendChild(row);
          });

          if (data.localIps.length > 0) {
            this.renderQrCode(data.localIps[0].url);
          }
        }
      })
      .catch(e => console.warn('Fetch status failed:', e));
  }

  fetchBindings() {
    fetch('/api/bindings')
      .then(r => r.json())
      .then(binds => {
        if (!this.bindingsList) return;
        this.bindingsList.innerHTML = '';
        for (const [action, keys] of Object.entries(binds)) {
          const item = document.createElement('div');
          item.className = 'binding-item';
          item.innerHTML = `
            <span class="binding-name">${action.toUpperCase().replace('_', ' ')}</span>
            <span class="binding-keys">${keys.join(' + ')}</span>
          `;
          this.bindingsList.appendChild(item);
        }
      })
      .catch(e => console.warn('Fetch bindings error:', e));
  }

  renderQrCode(text) {
    const holder = document.getElementById('qrSvgHolder');
    if (!holder) return;
    // Generate QR using Google Charts / QuickChart API or SVG
    holder.innerHTML = `<img src="https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${encodeURIComponent(text)}" width="180" height="180" alt="QR Code" style="display:block; border-radius: 6px;" />`;
  }

  // WebSocket Connection with high-speed receiver
  initWebSocket() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws`;

    this.connBadge.className = 'status-badge connecting';
    this.connBadge.querySelector('.status-text').textContent = 'BAĞLANILIYOR';

    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      this.connBadge.className = 'status-badge connected';
      this.connBadge.querySelector('.status-text').textContent = 'CANLI AKIŞ';
      if (this.reconnectTimer) {
        clearInterval(this.reconnectTimer);
        this.reconnectTimer = null;
      }
    };

    this.ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data);
        if (msg.type === 'telemetry') {
          this.handleTelemetry(msg.data);
        }
      } catch (err) {
        console.error('Error handling WS packet:', err);
      }
    };

    this.ws.onclose = () => {
      this.connBadge.className = 'status-badge disconnected';
      this.connBadge.querySelector('.status-text').textContent = 'KOPUK';
      this.scheduleReconnect();
    };

    this.ws.onerror = (err) => {
      this.ws.close();
    };
  }

  scheduleReconnect() {
    if (!this.reconnectTimer) {
      this.reconnectTimer = setInterval(() => {
        this.initWebSocket();
      }, 2000);
    }
  }

  // Process telemetry packet
  handleTelemetry(data) {
    if (!data) return;

    // Top Header info
    if (data.staticInfo) {
      if (this.carName && data.staticInfo.carModel && this.carName.textContent !== data.staticInfo.carModel) {
        this.carName.textContent = data.staticInfo.carModel;
      }
      if (this.trackName && data.staticInfo.track && this.trackName.textContent !== data.staticInfo.track) {
        this.trackName.textContent = data.staticInfo.track;
      }
    }

    if (data.graphic && this.sessionType) {
      const sessionMap = { 0: 'SERBEST', 1: 'ANTRENMAN', 2: 'SIRALAMA', 3: 'YARIŞ', 4: 'HOTLAP', 5: 'TIME ATTACK', 6: 'DRIFT' };
      const sName = sessionMap[data.graphic.session] || 'YARIŞ';
      if (this.sessionType.textContent !== sName) {
        this.sessionType.textContent = sName;
      }
    }

    this.updateModeUI(data.source);

    // Top Header Quick Telemetry
    if (data.physics) {
      if (this.htSpeed) this.htSpeed.textContent = `${data.physics.speedKmh || 0} KM/H`;
      if (this.htRpm) this.htRpm.textContent = `${data.physics.rpms || 0} RPM`;
      if (this.htGear) {
        const g = data.physics.gearStr || 'N';
        this.htGear.textContent = g;
        this.htGear.style.color = (g === 'R' ? '#ff3b30' : (g === 'N' ? '#ffd600' : '#00e676'));
      }
    }

    // Çarpışma / Hasar Titreşimi ve Görsel Efekt
    if (data.physics && data.physics.collision) {
      if (navigator.vibrate) {
        try {
          navigator.vibrate([160, 50, 240, 50, 360]); // Güçlü darbe titreşim ritmi
        } catch (_) {}
      }

      // Bağlı gamepad varsa titreşim motorunu çalıştır
      if (typeof navigator.getGamepads === 'function') {
        try {
          const gamepads = navigator.getGamepads();
          for (const gp of gamepads) {
            if (gp && gp.vibrationActuator) {
              gp.vibrationActuator.playEffect('dual-rumble', {
                startDelay: 0,
                duration: 400,
                weakMagnitude: 1.0,
                strongMagnitude: 1.0
              }).catch(() => {});
            }
          }
        } catch (_) {}
      }

      // Görsel sarsıntı / kırmızı parlama efekti
      document.body.classList.remove('collision-shake');
      void document.body.offsetWidth;
      document.body.classList.add('collision-shake');
    }

    // Update child components
    if (window.dashboardController) {
      window.dashboardController.update(data);
    }

    if (window.tyresController) {
      window.tyresController.update(data);
    }

    if (window.telemetryGraphController) {
      window.telemetryGraphController.pushData(data);
    }
  }
}

document.addEventListener('DOMContentLoaded', () => {
  window.app = new App();
});
