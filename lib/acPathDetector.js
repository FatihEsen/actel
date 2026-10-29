const fs = require('fs');
const path = require('path');
const os = require('os');

class ACPathDetector {
  constructor() {
    this.acDocsDir = null;
    this.acInstallDir = null;
    this.detectPaths();
  }

  detectPaths() {
    // 1. Detect Documents/Assetto Corsa directory
    const docCandidates = [
      'D:/Documents/Assetto Corsa',
      'D:\\Documents\\Assetto Corsa',
      path.join(os.homedir(), 'Documents', 'Assetto Corsa'),
      path.join(os.homedir(), 'OneDrive', 'Documents', 'Assetto Corsa'),
      'C:/Documents/Assetto Corsa'
    ];

    for (const d of docCandidates) {
      if (fs.existsSync(d) && fs.existsSync(path.join(d, 'cfg'))) {
        this.acDocsDir = d;
        break;
      }
    }

    // 2. Detect Assetto Corsa Game Installation directory
    const installCandidates = [
      'D:/Saved Games/Steam/steamapps/common/assettocorsa',
      'D:\\Saved Games\\Steam\\steamapps\\common\\assettocorsa',
      'D:/Saved Games/SteamLibrary/steamapps/common/assettocorsa',
      'C:/Program Files (x86)/Steam/steamapps/common/assettocorsa',
      'C:/Program Files/Steam/steamapps/common/assettocorsa',
      'D:/Steam/steamapps/common/assettocorsa',
      'D:/SteamLibrary/steamapps/common/assettocorsa',
      'E:/Steam/steamapps/common/assettocorsa',
      'E:/SteamLibrary/steamapps/common/assettocorsa'
    ];

    for (const i of installCandidates) {
      if (fs.existsSync(i) && fs.existsSync(path.join(i, 'content', 'cars'))) {
        this.acInstallDir = i;
        break;
      }
    }
  }

  getDocsDir() {
    if (!this.acDocsDir) this.detectPaths();
    return this.acDocsDir;
  }

  getInstallDir() {
    if (!this.acInstallDir) this.detectPaths();
    return this.acInstallDir;
  }

  getContentCarsDir() {
    const install = this.getInstallDir();
    return install ? path.join(install, 'content', 'cars') : null;
  }

  getContentTracksDir() {
    const install = this.getInstallDir();
    return install ? path.join(install, 'content', 'tracks') : null;
  }

  /**
   * Automatically validates and configures controls.ini with standard AC defaults.
   * Ensures zero-friction setup for new users (wheel, gamepad, keyboard).
   */
  ensureDefaultControls() {
    const docs = this.getDocsDir();
    if (!docs) {
      console.log('[ACSetup] Assetto Corsa Belgeler klasörü bulunamadı, kontroller atlandı.');
      return false;
    }

    const controlsPath = path.join(docs, 'cfg', 'controls.ini');
    if (!fs.existsSync(controlsPath)) {
      console.log('[ACSetup] controls.ini bulunamadı:', controlsPath);
      return false;
    }

    try {
      let content = fs.readFileSync(controlsPath, 'utf8');
      let modified = false;

      // 1. COMBINE_WITH_KEYBOARD_CONTROL: Must be 1 so wheel/gamepad users can use keyboard inputs simultaneously
      if (content.includes('COMBINE_WITH_KEYBOARD_CONTROL=0')) {
        content = content.replace(/COMBINE_WITH_KEYBOARD_CONTROL=0/g, 'COMBINE_WITH_KEYBOARD_CONTROL=1');
        modified = true;
        console.log('[ACSetup] Klavye + Direksiyon/Gamepad eşzamanlı girdi (COMBINE_WITH_KEYBOARD_CONTROL=1) aktif edildi.');
      }

      // Robust INI section key setter that avoids regex escaping problems
      const ensureKey = (sectionName, keyVal, modVal = null) => {
        const secHeader = `[${sectionName}]`;
        const secIdx = content.indexOf(secHeader);
        if (secIdx === -1) {
          let newSec = `\r\n\r\n${secHeader}\r\nJOY=-1\r\nBUTTON=-1\r\nXBOXBUTTON=-1\r\nKEY=${keyVal}\r\n`;
          if (modVal !== null) {
            newSec += `KEY_MODIFICATOR=${modVal}\r\n`;
          }
          content = content.trimEnd() + newSec;
          modified = true;
          console.log(`[ACSetup] [${sectionName}] bölümü eklendi ve kısayol bağlandı: ${keyVal}`);
          return;
        }

        const nextSecIdx = content.indexOf('\n[', secIdx + secHeader.length);
        const endIdx = nextSecIdx === -1 ? content.length : nextSecIdx;
        let secBody = content.substring(secIdx, endIdx);

        const keyMatch = secBody.match(/KEY=([^\r\n]*)/);
        const currentKey = keyMatch ? keyMatch[1].trim() : '-1';

        if (!keyMatch || currentKey === '-1' || currentKey === '') {
          if (keyMatch) {
            secBody = secBody.replace(/KEY=[^\r\n]*/, `KEY=${keyVal}`);
          } else {
            secBody += `\r\nKEY=${keyVal}`;
          }

          if (modVal !== null) {
            if (secBody.includes('KEY_MODIFICATOR=')) {
              secBody = secBody.replace(/KEY_MODIFICATOR=[^\r\n]*/, `KEY_MODIFICATOR=${modVal}`);
            } else {
              secBody += `\r\nKEY_MODIFICATOR=${modVal}`;
            }
          }

          content = content.substring(0, secIdx) + secBody + content.substring(endIdx);
          modified = true;
          console.log(`[ACSetup] [${sectionName}] için varsayılan kısayol bağlandı: ${keyVal}`);
        }
      };

      // 1. KERS & DRS
      ensureKey('KERS', '0x4B ; K');
      ensureKey('DRS', '0x44 ; D');

      // 2. Brake Bias (Fren Dağılımı)
      ensureKey('BALANCEUP', '0x42 ; B', '17 ; Control+B');
      ensureKey('BALANCEDN', '0x42 ; B', '17, 16 ; Control+Shift+B');

      // 3. Turbo Boost
      ensureKey('TURBOUP', '0x21 ; Page Up', '17 ; Control+Page Up');
      ensureKey('TURBODN', '0x22 ; Page Down', '17 ; Control+Page Down');

      // 4. Engine Brake (Motor Freni)
      ensureKey('ENGINE_BRAKE_UP', '0x45 ; E', '17 ; Control+E');
      ensureKey('ENGINE_BRAKE_DN', '0x45 ; E', '17, 16 ; Control+Shift+E');

      // 5. TC & ABS
      ensureKey('TRACTION_CONTROL', '0x54 ; T');
      ensureKey('ABS', '0x41 ; A');

      // 6. Aydınlatma & Sinyal & Selektör & Korna
      ensureKey('ACTION_HEADLIGHTS', '0x4C ; L');
      ensureKey('ACTION_HEADLIGHTS_FLASH', '0x47 ; G');
      ensureKey('ACTION_HORN', '0x48 ; H');
      ensureKey('__EXT_TURNSIGNAL_LEFT', '0x25 ; Left Arrow', '18 ; Alt+Left Arrow');
      ensureKey('__EXT_TURNSIGNAL_RIGHT', '0x27 ; Right Arrow', '18 ; Alt+Right Arrow');
      ensureKey('__EXT_HAZARDS', '0x28 ; Down Arrow', '18 ; Alt+Down Arrow');
      ensureKey('__EXT_LIGHT_A', '0x67 ; Numpad 7', '18 ; Alt+Numpad 7');
      ensureKey('__EXT_LIGHT_B', '0x68 ; Numpad 8', '18 ; Alt+Numpad 8');

      // 7. Silecek (Wipers)
      ensureKey('__EXT_WIPERS_MORE', '0x62 ; Numpad 2', '18 ; Alt+Numpad 2');
      ensureKey('__EXT_WIPERS_LESS', '0x61 ; Numpad 1', '18 ; Alt+Numpad 1');
      ensureKey('__EXT_WIPERS_OFF', '0x60 ; Numpad 0', '18 ; Alt+Numpad 0');
      ensureKey('__EXT_WIPERS', '0x57 ; W', '18 ; Alt+W');

      // 8. Pit & Seans & Başlatma
      ensureKey('__EXT_PIT_LIMITER', '0x4C ; L', '18 ; Alt+L');
      ensureKey('__CM_START_SESSION', '0x53 ; S', '17 ; Control+S');
      ensureKey('__CM_TO_PITS', '0x50 ; P', '17 ; Control+P');
      ensureKey('RESET_RACE', '0x4F ; O', '17 ; Control+O');
      ensureKey('__CM_RESET_SESSION', '0x4F ; O', '17 ; Control+O');
      ensureKey('STARTER', '0x49 ; I');

      // 9. Kamera & Bakış
      ensureKey('ACTION_CHANGE_CAMERA', '0x43 ; C');
      ensureKey('GLANCELEFT', '0x51 ; Q');
      ensureKey('GLANCERIGHT', '0x45 ; E');
      ensureKey('GLANCEBACK', '0x57 ; W');
      ensureKey('__EXT_LOOK_LEFT', '0x51 ; Q');
      ensureKey('__EXT_LOOK_RIGHT', '0x45 ; E');
      ensureKey('__EXT_LOOK_BACK', '0x57 ; W');

      if (modified) {
        fs.writeFileSync(controlsPath, content, 'utf8');
        console.log('[ACSetup] Assetto Corsa kontrolleri varsayılan ayarlara göre doğrulandı ve kaydedildi.');
      }

      return true;
    } catch (err) {
      console.warn('[ACSetup] controls.ini yapılandırma hatası:', err.message);
      return false;
    }
  }

  /**
   * Ensures actel_bridge Python app is installed in the game directory and enabled.
   */
  ensurePluginInstalled() {
    const install = this.getInstallDir();
    const docs = this.getDocsDir();
    const localPluginDir = path.join(__dirname, '..', 'ac_plugin', 'actel_bridge');

    if (!fs.existsSync(localPluginDir)) return false;

    // 1. Copy to game directory if install found
    if (install) {
      const targetDir = path.join(install, 'apps', 'python', 'actel_bridge');
      try {
        fs.mkdirSync(targetDir, { recursive: true });
        const files = ['actel_bridge.py'];
        for (const f of files) {
          const src = path.join(localPluginDir, f);
          const dst = path.join(targetDir, f);
          if (fs.existsSync(src)) {
            fs.copyFileSync(src, dst);
          }
        }
        // Copy stdlib / stdlib64 if present
        for (const libDir of ['stdlib', 'stdlib64']) {
          const srcLib = path.join(localPluginDir, libDir);
          const dstLib = path.join(targetDir, libDir);
          if (fs.existsSync(srcLib)) {
            fs.mkdirSync(dstLib, { recursive: true });
            const pyds = fs.readdirSync(srcLib);
            for (const p of pyds) {
              fs.copyFileSync(path.join(srcLib, p), path.join(dstLib, p));
            }
          }
        }

        // Copy actel_sync Lua app for CSP real-time light sync
        const localLuaDir = path.join(__dirname, '..', 'ac_plugin', 'actel_sync');
        if (fs.existsSync(localLuaDir)) {
          const targetLuaDir = path.join(install, 'apps', 'lua', 'actel_sync');
          fs.mkdirSync(targetLuaDir, { recursive: true });
          for (const lf of ['manifest.ini', 'actel_sync.lua']) {
            const srcF = path.join(localLuaDir, lf);
            const dstF = path.join(targetLuaDir, lf);
            if (fs.existsSync(srcF)) fs.copyFileSync(srcF, dstF);
          }
        }
      } catch (err) {
        console.warn('[ACSetup] Eklenti kurulumu uyarısı:', err.message);
      }
    }

    // 2. Ensure python.ini enables ACTEL_BRIDGE
    if (docs) {
      const pyIniPath = path.join(docs, 'cfg', 'python.ini');
      try {
        let content = fs.existsSync(pyIniPath) ? fs.readFileSync(pyIniPath, 'utf8') : '';
        if (!content.includes('[ACTEL_BRIDGE]')) {
          content += '\r\n[ACTEL_BRIDGE]\r\nACTIVE=1\r\n';
          fs.writeFileSync(pyIniPath, content, 'utf8');
          console.log('[ACSetup] python.ini dosyasında ACTEL_BRIDGE aktif edildi.');
        }
      } catch (e) {
        console.warn('[ACSetup] python.ini yazma uyarısı:', e.message);
      }
    }

    return true;
  }
}

module.exports = new ACPathDetector();
