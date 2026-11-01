const http = require('http');
const assert = require('assert');

// Verify that server endpoints compile and work
const serverProcess = require('../server.js');

// Give server 500ms to bind
setTimeout(() => {
  http.get('http://127.0.0.1:8080/api/setup/network', (res) => {
    assert.strictEqual(res.statusCode, 200);
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => {
      const json = JSON.parse(data);
      assert('localIps' in json, 'should have localIps');
      assert('apk' in json, 'should have apk status');
      assert('mobileConnected' in json, 'should have mobileConnected');
      console.log('✓ /api/setup/network endpoint verified:', json);

      // Next check QR endpoint
      http.get('http://127.0.0.1:8080/api/setup/qr', (qrRes) => {
        assert.strictEqual(qrRes.statusCode, 200);
        assert(qrRes.headers['content-type'].includes('image/svg+xml'), 'content-type should be image/svg+xml');
        let svg = '';
        qrRes.on('data', c => svg += c);
        qrRes.on('end', () => {
          assert(svg.includes('<svg'), 'QR response should be SVG');
          console.log('✓ /api/setup/qr endpoint verified');
          process.exit(0);
        });
      });
    });
  }).on('error', (err) => {
    console.error('Test error:', err);
    process.exit(1);
  });
}, 800);
