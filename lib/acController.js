const koffi = require('koffi');

const kernel32 = koffi.load('kernel32.dll');
const user32 = koffi.load('user32.dll');

// Windows Win32 API functions
const CreateFileMappingW = kernel32.func('CreateFileMappingW', 'void*', ['void*', 'void*', 'uint32', 'uint32', 'uint32', 'str16']);
const MapViewOfFile = kernel32.func('MapViewOfFile', 'void*', ['void*', 'uint32', 'uint32', 'uint32', 'size_t']);
const UnmapViewOfFile = kernel32.func('UnmapViewOfFile', 'bool', ['void*']);
const CloseHandle = kernel32.func('CloseHandle', 'bool', ['void*']);

const keybd_event = user32.func('keybd_event', 'void', ['uint8', 'uint8', 'uint32', 'uintptr']);
const MapVirtualKeyA = user32.func('MapVirtualKeyA', 'uint32', ['uint32', 'uint32']);
const FindWindowA = user32.func('FindWindowA', 'void*', ['string', 'string']);
const GetForegroundWindow = user32.func('GetForegroundWindow', 'void*', []);
const SetForegroundWindow = user32.func('SetForegroundWindow', 'bool', ['void*']);

const PAGE_READWRITE = 0x04;
const FILE_MAP_ALL_ACCESS = 0x001F;
const INVALID_HANDLE_VALUE = -1n;

const KEYEVENTF_KEYUP = 0x0002;
const KEYEVENTF_EXTENDEDKEY = 0x0001;

const VK_MAP = {
  'ESC': 0x1B, 'ESCAPE': 0x1B,
  'CTRL': 0x11, 'LCTRL': 0x11, 'RCTRL': 0x11,
  'SHIFT': 0x10, 'LSHIFT': 0x10, 'RSHIFT': 0x10,
  'ALT': 0x12, 'LALT': 0x12, 'RALT': 0x12,
  'SPACE': 0x20, 'ENTER': 0x0D, 'RETURN': 0x0D, 'TAB': 0x09, 'BACKSPACE': 0x08,

  'F1': 0x70, 'F2': 0x71, 'F3': 0x72, 'F4': 0x73, 'F5': 0x74, 'F6': 0x75,
  'F7': 0x76, 'F8': 0x77, 'F9': 0x78, 'F10': 0x79, 'F11': 0x7A, 'F12': 0x7B,

  '0': 0x30, '1': 0x31, '2': 0x32, '3': 0x33, '4': 0x34,
  '5': 0x35, '6': 0x36, '7': 0x37, '8': 0x38, '9': 0x39,

  'NUMPAD0': 0x60, 'NUMPAD1': 0x61, 'NUMPAD2': 0x62, 'NUMPAD3': 0x63, 'NUMPAD4': 0x64,
  'NUMPAD5': 0x65, 'NUMPAD6': 0x66, 'NUMPAD7': 0x67, 'NUMPAD8': 0x68, 'NUMPAD9': 0x69,

  'A': 0x41, 'B': 0x42, 'C': 0x43, 'D': 0x44, 'E': 0x45, 'F': 0x46,
  'G': 0x47, 'H': 0x48, 'I': 0x49, 'J': 0x4A, 'K': 0x4B, 'L': 0x4C,
  'M': 0x4D, 'N': 0x4E, 'O': 0x4F, 'P': 0x50, 'Q': 0x51, 'R': 0x52,
  'S': 0x53, 'T': 0x54, 'U': 0x55, 'V': 0x56, 'W': 0x57, 'X': 0x58,
  'Y': 0x59, 'Z': 0x5A,

  'LEFT': 0x25, 'UP': 0x26, 'RIGHT': 0x27, 'DOWN': 0x28
};

const SCAN_MAP = {
  'ESC': 0x01, 'ESCAPE': 0x01,
  '1': 0x02, '2': 0x03, '3': 0x04, '4': 0x05, '5': 0x06, '6': 0x07, '7': 0x08, '8': 0x09, '9': 0x0A, '0': 0x0B,

  'Q': 0x10, 'W': 0x11, 'E': 0x12, 'R': 0x13, 'T': 0x14, 'Y': 0x15, 'U': 0x16, 'I': 0x17, 'O': 0x18, 'P': 0x19,
  'A': 0x1E, 'S': 0x1F, 'D': 0x20, 'F': 0x21, 'G': 0x22, 'H': 0x23, 'J': 0x24, 'K': 0x25, 'L': 0x26,
  'Z': 0x2C, 'X': 0x2D, 'C': 0x2E, 'V': 0x2F, 'B': 0x30, 'N': 0x31, 'M': 0x32,

  'SPACE': 0x39, 'ENTER': 0x1C, 'RETURN': 0x1C, 'TAB': 0x0F, 'BACKSPACE': 0x0E,
  'CTRL': 0x1D, 'LCTRL': 0x1D, 'RCTRL': 0x1D,
  'SHIFT': 0x2A, 'LSHIFT': 0x2A, 'RSHIFT': 0x36,
  'ALT': 0x38, 'LALT': 0x38, 'RALT': 0x38,

  'F1': 0x3B, 'F2': 0x3C, 'F3': 0x3D, 'F4': 0x3E, 'F5': 0x3F, 'F6': 0x40,

  'NUMPAD0': 0x52, 'NUMPAD1': 0x4F, 'NUMPAD2': 0x50, 'NUMPAD3': 0x51, 'NUMPAD4': 0x4B,
  'NUMPAD5': 0x4C, 'NUMPAD6': 0x4D, 'NUMPAD7': 0x47, 'NUMPAD8': 0x48, 'NUMPAD9': 0x49,

  'LEFT': 0x4B, 'UP': 0x48, 'RIGHT': 0x4D, 'DOWN': 0x50
};

const DEFAULT_ACTION_BINDS = {
  'turn_left': ['ALT', 'LEFT'],
  'turn_right': ['ALT', 'RIGHT'],
  'hazard': ['ALT', 'DOWN'],
  'headlights': ['L'],
  'flasher': ['F'],
  'wipers': ['ALT', 'W'],
  'horn': ['H'],
  'tc_up': ['CTRL', 'T'],
  'tc_down': ['CTRL', 'SHIFT', 'T'],
  'abs_up': ['CTRL', 'A'],
  'abs_down': ['CTRL', 'SHIFT', 'A'],
  'bb_up': ['CTRL', 'B'],
  'bb_down': ['CTRL', 'SHIFT', 'B'],
  'drs': ['D'],
  'pit_limiter': ['CTRL', 'L'],
  'look_left': ['Q'],
  'look_right': ['E'],
  'look_back': ['SPACE'],
  'camera_change': ['C'],
  'starter': ['I'],
  'engine_start': ['I'],
  'pause': ['ESC'],
  'teleport_pits': ['CTRL', 'P'],
  'vr_recenter': ['CTRL', 'SPACE'],
  'restart_session': ['CTRL', 'O']
};

class ACController {
  constructor() {
    this.keyBindings = { ...DEFAULT_ACTION_BINDS };
    this.packetId = 1;

    this.hMap = null;
    this.ptrMap = null;
    this.initSharedMemory();
  }

  initSharedMemory() {
    try {
      this.hMap = CreateFileMappingW(INVALID_HANDLE_VALUE, null, PAGE_READWRITE, 0, 1024, 'Local\\actel_cmd');
      if (this.hMap) {
        this.ptrMap = MapViewOfFile(this.hMap, FILE_MAP_ALL_ACCESS, 0, 0, 1024);
        console.log('[ACController] Command Shared Memory connected at Local\\actel_cmd');
      }
    } catch (e) {
      console.error('[ACController] Error creating shared memory command buffer:', e);
    }
  }

  writeSharedMemoryCommand(cmdString) {
    if (!this.ptrMap) {
      this.initSharedMemory();
      if (!this.ptrMap) return;
    }

    try {
      this.packetId++;
      const buf = Buffer.alloc(1024);
      buf.writeUInt32LE(this.packetId, 0); // Offset 0: Packet ID (uint32)
      buf.write(cmdString, 4, 60, 'utf8'); // Offset 4: Command String

      koffi.encode(this.ptrMap, 'uint8', buf, 1024);
    } catch (err) {
      console.warn('[ACController] Write to shared memory error:', err);
    }
  }

  getKeyCodes(keyName) {
    const k = keyName.toUpperCase();
    const vk = VK_MAP[k] || 0;
    const scan = SCAN_MAP[k] || (vk ? MapVirtualKeyA(vk, 0) : 0);
    return { vk, scan };
  }

  ensureAcFocus() {
    try {
      const hAc = FindWindowA(null, 'Assetto Corsa');
      if (hAc) {
        const hFg = GetForegroundWindow();
        if (hFg !== hAc) {
          SetForegroundWindow(hAc);
        }
      }
    } catch (e) {
      // Ignore focus errors
    }
  }

  sendKeyDown(keyName) {
    const { vk, scan } = this.getKeyCodes(keyName);
    if (!vk && !scan) return false;
    let flags = 0;
    if (['LEFT', 'UP', 'RIGHT', 'DOWN'].includes(keyName.toUpperCase())) {
      flags |= KEYEVENTF_EXTENDEDKEY;
    }
    keybd_event(vk, scan, flags, 0);
    return true;
  }

  sendKeyUp(keyName) {
    const { vk, scan } = this.getKeyCodes(keyName);
    if (!vk && !scan) return false;
    let flags = KEYEVENTF_KEYUP;
    if (['LEFT', 'UP', 'RIGHT', 'DOWN'].includes(keyName.toUpperCase())) {
      flags |= KEYEVENTF_EXTENDEDKEY;
    }
    keybd_event(vk, scan, flags, 0);
    return true;
  }

  async tapKey(keyName, holdMs = 90) {
    this.sendKeyDown(keyName);
    await new Promise(r => setTimeout(r, holdMs));
    this.sendKeyUp(keyName);
    return true;
  }

  async sendCombo(keys, holdMs = 90) {
    for (const k of keys) {
      this.sendKeyDown(k);
      await new Promise(r => setTimeout(r, 20));
    }
    await new Promise(r => setTimeout(r, holdMs));
    for (const k of [...keys].reverse()) {
      this.sendKeyUp(k);
      await new Promise(r => setTimeout(r, 20));
    }
    return true;
  }

  async triggerAction(actionName) {
    console.log(`[ACController] Triggering action: ${actionName}`);

    // 1. Send via Shared Memory buffer for In-Game Python app
    this.writeSharedMemoryCommand(actionName);

    // 2. Ensure Assetto Corsa has window focus if running
    this.ensureAcFocus();

    // 3. Headlight flasher: send both F and quick dual-flash on L
    if (actionName === 'flasher') {
      this.tapKey('F', 80);
      await this.tapKey('L', 70);
      await new Promise(r => setTimeout(r, 70));
      await this.tapKey('L', 70);
      return { success: true, action: actionName };
    }

    // 4. Horn: hold for 350ms so horn sound is clearly audible
    if (actionName === 'horn') {
      await this.tapKey('H', 350);
      return { success: true, action: actionName };
    }

    // 5. Engine Starter / CSP Start Session:
    // If in pause/pit menu, starts the session (Ctrl+S / ESC). Also cranks engine starter (I).
    if (actionName === 'starter' || actionName === 'engine_start') {
      // Trigger CSP Start Session (Ctrl+S)
      this.sendCombo(['CTRL', 'S'], 60);

      // Tap ESC to unpause if in standard pause menu
      this.tapKey('ESC', 50);

      // Crank starter for 900ms
      await this.tapKey('I', 900);
      return { success: true, action: actionName };
    }

    // 5. Look left / right / back: hold for 1200ms so driver can see surroundings
    if (['look_left', 'look_right', 'look_back'].includes(actionName)) {
      const combo = this.keyBindings[actionName] || [actionName];
      const k = combo[0];
      await this.tapKey(k, 1200);
      return { success: true, action: actionName };
    }

    // 6. Send direct key combination
    const combo = this.keyBindings[actionName];
    if (combo && Array.isArray(combo)) {
      await this.sendCombo(combo, 100);
    } else {
      await this.tapKey(actionName, 100);
    }

    return { success: true, action: actionName };
  }

  updateBinding(actionName, keysArray) {
    this.keyBindings[actionName] = keysArray;
    return { success: true, bindings: this.keyBindings };
  }

  getBindings() {
    return this.keyBindings;
  }
}

module.exports = new ACController();
