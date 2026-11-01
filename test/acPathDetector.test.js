const assert = require('assert');
const acPathDetector = require('../lib/acPathDetector');

// 1. getBridgeStatus method
assert(typeof acPathDetector.getBridgeStatus === 'function', 'getBridgeStatus should be a function');
const status = acPathDetector.getBridgeStatus();
assert(typeof status === 'object', 'getBridgeStatus should return an object');
assert('acFound' in status, 'status should have acFound');
assert('installDir' in status, 'status should have installDir');
assert('docsDir' in status, 'status should have docsDir');
assert('bridgeInstalled' in status, 'status should have bridgeInstalled');
assert('syncInstalled' in status, 'status should have syncInstalled');

// 2. detectSteamLibraryFolders method
assert(typeof acPathDetector.detectSteamLibraryFolders === 'function', 'detectSteamLibraryFolders should be a function');
const libraries = acPathDetector.detectSteamLibraryFolders();
assert(Array.isArray(libraries), 'detectSteamLibraryFolders should return an array');

// 3. installBridge method exists
assert(typeof acPathDetector.installBridge === 'function', 'installBridge should be a function');

console.log('✓ acPathDetector tests passed. Status:', status);
