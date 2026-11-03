const os = require('os');
const fs = require('fs');
const path = require('path');
const QRCode = require('qrcode');

class NetworkHelper {
  constructor() {
    this.mobileConnected = false;
    this.lastMobileHeartbeat = 0;
  }

  getLocalIps(port = 8080) {
    const interfaces = os.networkInterfaces();
    const addresses = [];

    for (const name of Object.keys(interfaces)) {
      for (const iface of interfaces[name]) {
        if (iface.family === 'IPv4' && !iface.internal) {
          addresses.push({
            interface: name,
            ip: iface.address,
            url: `http://${iface.address}:${port}`
          });
        }
      }
    }

    // Sort: prioritize physical adapters (Wi-Fi, Ethernet, LAN) over virtual adapters (vEthernet, WSL, VirtualBox)
    addresses.sort((a, b) => {
      const aVirtual = /vethernet|virtual|wsl|hyper-v|loopback|vmnet/i.test(a.interface);
      const bVirtual = /vethernet|virtual|wsl|hyper-v|loopback|vmnet/i.test(b.interface);
      if (aVirtual && !bVirtual) return 1;
      if (!aVirtual && bVirtual) return -1;

      const aPhysical = /wi-fi|wifi|wireless|ethernet|yerel/i.test(a.interface);
      const bPhysical = /wi-fi|wifi|wireless|ethernet|yerel/i.test(b.interface);
      if (aPhysical && !bPhysical) return -1;
      if (!aPhysical && bPhysical) return 1;

      return 0;
    });

    return addresses;
  }

  async generateQrSvg(text) {
    try {
      const svg = await QRCode.toString(text, {
        type: 'svg',
        width: 200,
        margin: 1,
        color: {
          dark: '#000000',
          light: '#ffffff'
        }
      });
      // Ensure SVG has width and height attributes if omitted by generator
      if (!svg.includes('width=')) {
        return svg.replace('<svg ', '<svg width="200" height="200" ');
      }
      return svg;
    } catch (e) {
      // Clean fallback SVG if encoding edge case fails
      return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100" width="200" height="200"><text x="10" y="50" font-size="8">${text}</text></svg>`;
    }
  }

  getApkStatus() {
    const candidates = [
      path.join(__dirname, '..', 'public', 'download', 'actel.apk'),
      path.join(__dirname, '..', 'download', 'actel.apk'),
      path.join(__dirname, '..', 'android', 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk')
    ];

    for (const p of candidates) {
      if (fs.existsSync(p)) {
        const stats = fs.statSync(p);
        return {
          exists: true,
          sizeMb: Number((stats.size / (1024 * 1024)).toFixed(2)),
          path: p
        };
      }
    }

    return {
      exists: false,
      sizeMb: 0,
      path: candidates[0]
    };
  }

  setMobileConnected(connected = true) {
    this.mobileConnected = connected;
    if (connected) {
      this.lastMobileHeartbeat = Date.now();
    }
  }

  isMobileConnected() {
    if (!this.mobileConnected) return false;
    // Expire mobile connection after 30 seconds without heartbeat
    if (Date.now() - this.lastMobileHeartbeat > 30000) {
      this.mobileConnected = false;
    }
    return this.mobileConnected;
  }
}

module.exports = new NetworkHelper();
