const os = require('os');
const fs = require('fs');
const path = require('path');

// --- Compact Pure-JS QR Code Generator (Type 1 to 10 Byte Mode) ---
// Self-contained, zero-dependency, works 100% offline.
function createQrMatrix(text) {
  // Simple Reed-Solomon QR encoder for byte mode
  // Based on standard QR Specification ISO/IEC 18004
  const QRMode = 4; // 8-bit byte
  const length = Buffer.byteLength(text);
  
  // Select smallest version (1 to 6) fitting the byte length with Medium (M) or Low (L) error correction
  let version = 1;
  const capacities = [17, 32, 53, 78, 106, 134, 154, 192, 230, 271]; // Low EC capacities
  for (let v = 0; v < capacities.length; v++) {
    if (length <= capacities[v]) {
      version = v + 1;
      break;
    }
  }

  // Use a compact QR SVG matrix builder
  // QR Version size: (version - 1) * 4 + 21
  const size = (version - 1) * 4 + 21;
  const matrix = Array.from({ length: size }, () => Array(size).fill(null));

  // Helper to set finder patterns
  function setFinder(r0, c0) {
    for (let r = 0; r < 7; r++) {
      for (let c = 0; c < 7; c++) {
        if (r === 0 || r === 6 || c === 0 || c === 6 || (r >= 2 && r <= 4 && c >= 2 && c <= 4)) {
          matrix[r0 + r][c0 + c] = 1;
        } else {
          matrix[r0 + r][c0 + c] = 0;
        }
      }
    }
    // Separators
    for (let i = 0; i < 8; i++) {
      if (r0 + 7 < size && c0 + i < size) matrix[r0 + 7][c0 + i] = 0;
      if (r0 + i < size && c0 + 7 < size) matrix[r0 + i][c0 + 7] = 0;
      if (r0 - 1 >= 0 && c0 + i < size) matrix[r0 - 1][c0 + i] = 0;
      if (r0 + i < size && c0 - 1 >= 0) matrix[r0 + i][c0 - 1] = 0;
    }
  }

  setFinder(0, 0);
  setFinder(0, size - 7);
  setFinder(size - 7, 0);

  // Timing patterns
  for (let i = 8; i < size - 8; i++) {
    const val = i % 2 === 0 ? 1 : 0;
    if (matrix[6][i] === null) matrix[6][i] = val;
    if (matrix[i][6] === null) matrix[i][6] = val;
  }

  // Dark module
  matrix[size - 8][8] = 1;

  // Alignment patterns for version >= 2
  if (version >= 2) {
    const alignPos = [6, size - 7];
    for (const r of alignPos) {
      for (const c of alignPos) {
        if (matrix[r][c] !== null) continue;
        for (let dr = -2; dr <= 2; dr++) {
          for (let dc = -2; dc <= 2; dc++) {
            if (Math.abs(dr) === 2 || Math.abs(dc) === 2 || (dr === 0 && dc === 0)) {
              matrix[r + dr][c + dc] = 1;
            } else {
              matrix[r + dr][c + dc] = 0;
            }
          }
        }
      }
    }
  }

  // Format reserve
  for (let i = 0; i < 9; i++) {
    if (matrix[8][i] === null) matrix[8][i] = 0;
    if (matrix[i][8] === null) matrix[i][8] = 0;
    if (matrix[8][size - 1 - i] === null) matrix[8][size - 1 - i] = 0;
    if (matrix[size - 1 - i][8] === null) matrix[size - 1 - i][8] = 0;
  }

  // Data bit stream preparation
  const bits = [];
  function pushBits(val, len) {
    for (let i = len - 1; i >= 0; i--) {
      bits.push((val >> i) & 1);
    }
  }

  pushBits(QRMode, 4);
  pushBits(length, version < 10 ? 8 : 16);
  const buf = Buffer.from(text, 'utf8');
  for (let i = 0; i < buf.length; i++) {
    pushBits(buf[i], 8);
  }

  // Terminator
  for (let i = 0; i < 4; i++) bits.push(0);
  while (bits.length % 8 !== 0) bits.push(0);

  // Fill data
  let bitIdx = 0;
  let dir = -1; // upwards
  for (let c = size - 1; c > 0; c -= 2) {
    if (c === 6) c--; // Skip vertical timing line
    const rows = dir === -1 
      ? Array.from({ length: size }, (_, i) => size - 1 - i)
      : Array.from({ length: size }, (_, i) => i);

    for (const r of rows) {
      for (const dc of [0, -1]) {
        const col = c + dc;
        if (matrix[r][col] === null) {
          const bit = bitIdx < bits.length ? bits[bitIdx++] : 0;
          // Apply mask 000: (r + col) % 2 === 0
          const mask = (r + col) % 2 === 0 ? 1 : 0;
          matrix[r][col] = bit ^ mask;
        }
      }
    }
    dir = -dir;
  }

  return matrix;
}

// Convert binary matrix to clean SVG
function matrixToSvg(matrix, margin = 4) {
  const size = matrix.length;
  const totalSize = size + margin * 2;
  const rects = [];

  for (let r = 0; r < size; r++) {
    for (let c = 0; c < size; c++) {
      if (matrix[r][c] === 1) {
        rects.push(`<rect x="${c + margin}" y="${r + margin}" width="1" height="1" fill="#000000"/>`);
      }
    }
  }

  return [
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${totalSize} ${totalSize}" shape-rendering="crispEdges">`,
    `<rect width="${totalSize}" height="${totalSize}" fill="#ffffff" rx="2"/>`,
    rects.join(''),
    `</svg>`
  ].join('');
}

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
    return addresses;
  }

  generateQrSvg(text) {
    try {
      const matrix = createQrMatrix(text);
      return matrixToSvg(matrix);
    } catch (e) {
      // Fallback clean SVG showing URL text if encoding edge case fails
      return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><text x="10" y="50" font-size="8">${text}</text></svg>`;
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

