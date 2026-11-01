const assert = require('assert');
const networkHelper = require('../lib/networkHelper');

// Test synchronous / promise SVG generation
async function testQr() {
  const url = 'http://192.168.1.132:8080/download/actel.apk';
  const svg = await networkHelper.generateQrSvg(url);
  assert(typeof svg === 'string', 'SVG must be string');
  assert(svg.includes('<svg'), 'Must contain <svg');
  assert(svg.includes('</svg>'), 'Must contain </svg>');
  assert(svg.includes('path') || svg.includes('rect'), 'Must contain path or rect elements');

  // Verify adapter prioritization
  const ips = networkHelper.getLocalIps(8080);
  assert(Array.isArray(ips), 'ips must be array');
  if (ips.length > 1) {
    // If multiple, virtual adapters like WSL/VirtualBox/Hyper-V should not be first if physical Wi-Fi/Ethernet exists
    const first = ips[0].interface.toLowerCase();
    const hasPhysical = ips.some(i => /wi-fi|wifi|ethernet|lan/i.test(i.interface));
    if (hasPhysical) {
      assert(!/vethernet|virtual|wsl|hyper-v|loopback/i.test(first), 'First interface should not be virtual if physical exists');
    }
  }

  console.log('✓ QR validation and network prioritization tests passed');
}

testQr().catch(err => {
  console.error('Test failed:', err);
  process.exit(1);
});

