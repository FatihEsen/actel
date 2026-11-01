const assert = require('assert');
const networkHelper = require('../lib/networkHelper');

// 1. IP retrieval
const ips = networkHelper.getLocalIps(8080);
assert(Array.isArray(ips), 'getLocalIps should return an array');

// 2. Offline QR SVG generation
const svg = networkHelper.generateQrSvg('http://192.168.1.50:8080');
assert(typeof svg === 'string', 'generateQrSvg should return a string');
assert(svg.includes('<svg'), 'QR output should be valid SVG');
assert(svg.includes('</svg>'), 'QR output should close SVG');

// 3. Mobile handshake state
assert.strictEqual(networkHelper.isMobileConnected(), false);
networkHelper.setMobileConnected(true);
assert.strictEqual(networkHelper.isMobileConnected(), true);

// 4. APK status check
const apkStatus = networkHelper.getApkStatus();
assert(typeof apkStatus === 'object', 'getApkStatus should return an object');
assert('exists' in apkStatus, 'apkStatus should have exists flag');

console.log('✓ networkHelper tests passed');
