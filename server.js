const express = require('express');
const http = require('http');
const path = require('path');
const os = require('os');
const dgram = require('dgram');
const { WebSocketServer } = require('ws');

const acReader = require('./lib/acReader');
const mockTelemetry = require('./lib/mockTelemetry');
const acController = require('./lib/acController');

const app = express();
const server = http.createServer(app);
const wss = new WebSocketServer({ server });

const PORT = process.env.PORT || 8080;

app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// Operating mode: 'AUTO' (uses AC if available, otherwise demo), 'LIVE_ONLY', 'DEMO_ONLY'
let telemetryMode = 'AUTO';
let lastAcPacketReceived = 0;

// Helper to get local network IP addresses
function getLocalIpAddresses() {
  const interfaces = os.networkInterfaces();
  const addresses = [];
  for (const name of Object.keys(interfaces)) {
    for (const iface of interfaces[name]) {
      if (iface.family === 'IPv4' && !iface.internal) {
        addresses.push({
          interface: name,
          ip: iface.address,
          url: `http://${iface.address}:${PORT}`
        });
      }
    }
  }
  return addresses;
}

// REST Endpoints
app.get('/api/status', (req, res) => {
  const isAcActive = acReader.isConnected;
  res.json({
    acConnected: isAcActive,
    mode: telemetryMode,
    clientCount: wss.clients.size,
    localIps: getLocalIpAddresses(),
    port: PORT
  });
});

app.post('/api/mode', (req, res) => {
  const { mode } = req.body;
  if (['AUTO', 'LIVE_ONLY', 'DEMO_ONLY'].includes(mode)) {
    telemetryMode = mode;
    return res.json({ success: true, mode: telemetryMode });
  }
  res.status(400).json({ error: 'Invalid mode' });
});

app.post('/api/control', async (req, res) => {
  const { action, key, keys, state } = req.body;
  console.log('[HTTP] /api/control received:', req.body);

  try {
    let result;
    if (action) {
      result = await acController.triggerAction(action, state);
    } else if (key) {
      result = { success: await acController.tapKey(key), key };
    } else if (keys && Array.isArray(keys)) {
      result = { success: await acController.sendCombo(keys), keys };
    } else {
      return res.status(400).json({ error: 'Missing action, key, or keys' });
    }

    res.json(result);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/bindings', (req, res) => {
  res.json(acController.getBindings());
});

app.post('/api/bindings', (req, res) => {
  const { action, keys } = req.body;
  if (!action || !Array.isArray(keys)) {
    return res.status(400).json({ error: 'Invalid payload' });
  }
  const result = acController.updateBinding(action, keys);
  res.json(result);
});

// WebSocket Connection Handling
wss.on('connection', (ws, req) => {
  const clientIp = req.socket.remoteAddress;
  console.log(`[WS] Client connected from ${clientIp} (Total: ${wss.clients.size})`);

  ws.on('message', async (message) => {
    try {
      const text = message.toString();
      const data = JSON.parse(text);
      if (data.type === 'control') {
        console.log(`[WS] Control action received from ${clientIp}:`, data.action, data.state || 'tap');
        const result = await acController.triggerAction(data.action, data.state);
        ws.send(JSON.stringify({ type: 'control_ack', ...result }));
      } else if (data.type === 'csp_indicators') {
        acController.updateFromCsp(data);
      } else if (data.type === 'set_mode') {
        if (['AUTO', 'LIVE_ONLY', 'DEMO_ONLY'].includes(data.mode)) {
          telemetryMode = data.mode;
        }
      }
    } catch (e) {
      console.error('[WS] Error processing message:', e);
    }
  });

  ws.on('close', () => {
    console.log(`[WS] Client disconnected (Total: ${wss.clients.size})`);
  });
});

// 50Hz (20ms interval) Telemetry Broadcast Loop
const BROADCAST_INTERVAL_MS = 20;
let lastTickTime = Date.now();

setInterval(() => {
  if (wss.clients.size === 0) return;

  const now = Date.now();
  const dt = (now - lastTickTime) / 1000;
  lastTickTime = now;

  let telemetryData = null;

  if (telemetryMode === 'LIVE_ONLY') {
    telemetryData = acReader.readTelemetry();
  } else if (telemetryMode === 'DEMO_ONLY') {
    telemetryData = mockTelemetry.update(dt);
  } else {
    // AUTO MODE: Try live AC first
    telemetryData = acReader.readTelemetry();
    if (!telemetryData) {
      telemetryData = mockTelemetry.update(dt);
    }
  }

  if (telemetryData) {
    telemetryData.indicators = acController.getIndicators();

    const payload = JSON.stringify({
      type: 'telemetry',
      data: telemetryData
    });

    for (const client of wss.clients) {
      if (client.readyState === 1) { // OPEN
        client.send(payload);
      }
    }
  }
}, BROADCAST_INTERVAL_MS);

server.listen(PORT, '0.0.0.0', () => {
  console.log(`=======================================================`);
  console.log(`🏎️  ASSETTO CORSA TELEMETRY & CONTROLLER WEB SERVER`);
  console.log(`=======================================================`);
  console.log(`📡 Local Web URL:   http://localhost:${PORT}`);
  
  const localIps = getLocalIpAddresses();
  if (localIps.length > 0) {
    console.log(`📱 Mobile/LAN URLs:`);
    localIps.forEach(net => {
      console.log(`   - ${net.interface}: ${net.url}`);
    });
  }
  console.log(`⚡ WebSocket Stream: 50Hz High-Speed Active`);
  console.log(`🎮 Mode: ${telemetryMode}`);
  console.log(`📡 UDP Discovery: Broadcasting on port 8088`);
  console.log(`=======================================================`);
});

// UDP Auto-Discovery Beacon for Android App (Broadcasts every 2 seconds on UDP 8088)
const UDP_DISCOVERY_PORT = 8088;
const udpSocket = dgram.createSocket({ type: 'udp4', reuseAddr: true });

function broadcastDiscoveryBeacon() {
  const beacon = JSON.stringify({
    service: 'actel',
    version: '1.0',
    port: PORT,
    hostname: os.hostname(),
    mode: telemetryMode
  });
  const message = Buffer.from(beacon);

  udpSocket.send(message, 0, message.length, UDP_DISCOVERY_PORT, '255.255.255.255', (err) => {
    if (err && err.code !== 'ENETUNREACH') {
      // ignore normal broadcast warnings
    }
  });
}

udpSocket.bind(() => {
  try {
    udpSocket.setBroadcast(true);
    setInterval(broadcastDiscoveryBeacon, 2000);
  } catch (e) {
    console.log('[UDP] Broadcast configuration note:', e.message);
  }
});
