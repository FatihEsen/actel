const http = require('http');
const assert = require('assert');

// Connect to running server on 8080
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

        // Next check bridge status endpoint
        http.get('http://127.0.0.1:8080/api/setup/bridge-status', (bRes) => {
          assert.strictEqual(bRes.statusCode, 200);
          let bData = '';
          bRes.on('data', c => bData += c);
          bRes.on('end', () => {
            const bJson = JSON.parse(bData);
            assert('acFound' in bJson, 'bridge-status should have acFound');
            assert('bridgeInstalled' in bJson, 'bridge-status should have bridgeInstalled');
            console.log('✓ /api/setup/bridge-status endpoint verified:', bJson);
            process.exit(0);
          });
        });
      });
    });
  });
}).on('error', (err) => {
  console.error('Test error:', err);
  process.exit(1);
});
