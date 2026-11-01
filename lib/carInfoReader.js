const fs = require('fs');
const path = require('path');
const acPathDetector = require('./acPathDetector');

function getCarsDir() {
  return acPathDetector.getContentCarsDir() || path.join('D:\\Saved Games\\Steam\\steamapps\\common\\assettocorsa\\content', 'cars');
}

function getTracksDir() {
  return acPathDetector.getContentTracksDir() || path.join('D:\\Saved Games\\Steam\\steamapps\\common\\assettocorsa\\content', 'tracks');
}

const carCache = new Map();
const trackCache = new Map();

function getAcdKey(name) {
  const sz = name.length;
  let k1 = 0;
  for (let i = 0; i < sz; i++) k1 += name.charCodeAt(i);

  let k2 = 0;
  for (let i = 0; i < sz - 1; i++) {
    k2 = (Math.imul(k2, name.charCodeAt(i)) - name.charCodeAt(i + 1)) | 0;
  }

  let k3 = 0;
  for (let i = 1; i < sz - 3; i += 4) {
    let tmp = name.charCodeAt(i);
    let tmp2 = (name.charCodeAt(i + 1) + 0x1b) | 0;
    let div = Math.trunc(Math.imul(k3, tmp) / tmp2);
    let tmp3 = name.charCodeAt(i - 1);
    k3 = (div - 0x1b - tmp3) | 0;
  }

  let k4 = 0x1683;
  for (let i = 1; i < sz; i++) k4 = (k4 - name.charCodeAt(i)) | 0;

  let k5 = 0x42;
  for (let i = 1; i < sz - 4; i += 4) {
    let tmp = Math.imul(name.charCodeAt(i) + 0xf, k5) | 0;
    let tmp2 = (Math.imul(name.charCodeAt(i - 1) + 0xf, tmp) + 0x16) | 0;
    k5 = tmp2;
  }

  let k6 = 0x65;
  for (let i = 0; i < sz - 2; i += 2) k6 = (k6 - name.charCodeAt(i)) | 0;

  let k7 = 0xab;
  for (let i = 0; i < sz - 2; i += 2) k7 = (k7 % name.charCodeAt(i)) | 0;
  k7 = (k7 + 2) | 0;

  let k8 = 0xab;
  for (let i = 0; i < sz - 1; i++) {
    let div = Math.trunc(k8 / name.charCodeAt(i));
    k8 = (div + name.charCodeAt(i + 1)) | 0;
  }

  const keys = [k1, k2, k3, k4, k5, k6, k7, k8].map(k => (k & 0xff));
  return keys.join('-');
}

function readAcdEntries(carDir, carModel) {
  const acdPath = path.join(carDir, 'data.acd');
  if (!fs.existsSync(acdPath)) return null;

  try {
    const buf = fs.readFileSync(acdPath);
    let pos = buf.readInt32LE(0) < 0 ? 8 : 0;
    const keyBytes = Buffer.from(getAcdKey(carModel), 'ascii');
    const result = new Map();

    while (pos < buf.length) {
      const nameLen = buf.readInt32LE(pos);
      pos += 4;
      const name = buf.subarray(pos, pos + nameLen).toString('ascii');
      pos += nameLen;
      const size = buf.readInt32LE(pos);
      pos += 4;

      if (['electronics.ini', 'turbo.ini', 'engine.ini', 'drivetrain.ini', 'ers.ini'].includes(name) || name.endsWith('.lut')) {
        const dec = Buffer.alloc(size);
        for (let x = 0; x < size; x++) {
          const b = buf.readInt32LE(pos + x * 4);
          dec[x] = (b - keyBytes[x % keyBytes.length]) & 0xff;
        }
        result.set(name, dec.toString('ascii'));
      }
      pos += size * 4;
    }
    return result;
  } catch (e) {
    return null;
  }
}

function parseIniSection(content, sectionName) {
  const lines = content.split(/\r?\n/);
  let inSection = false;
  const result = {};

  for (let line of lines) {
    line = line.trim();
    if (!line || line.startsWith(';') || line.startsWith('#')) continue;

    if (line.startsWith('[') && line.endsWith(']')) {
      const currentSection = line.substring(1, line.length - 1).trim();
      inSection = (currentSection.toUpperCase() === sectionName.toUpperCase());
      continue;
    }

    if (inSection) {
      const eqIdx = line.indexOf('=');
      if (eqIdx !== -1) {
        const key = line.substring(0, eqIdx).trim().toUpperCase();
        let val = line.substring(eqIdx + 1).trim();
        // Remove trailing comments
        const commentIdx = val.search(/[;#]/);
        if (commentIdx !== -1) {
          val = val.substring(0, commentIdx).trim();
        }
        result[key] = val;
      }
    }
  }

  return result;
}

function safeJsonParse(raw) {
  try {
    return JSON.parse(raw);
  } catch (e1) {
    try {
      // AC files often have raw newlines or control chars inside string descriptions
      const clean = raw.replace(/[\u0000-\u001F\u007F-\u009F]/g, (c) => (c === '\r' || c === '\n' || c === '\t') ? ' ' : '');
      return JSON.parse(clean);
    } catch (e2) {
      // Regex fallbacks
      const nameMatch = raw.match(/"name"\s*:\s*"([^"]+)"/);
      const brandMatch = raw.match(/"brand"\s*:\s*"([^"]+)"/);
      return {
        name: nameMatch ? nameMatch[1] : null,
        brand: brandMatch ? brandMatch[1] : null
      };
    }
  }
}

function getCarInfo(carModel, fallbackMaxRpm = 8500) {
  if (!carModel) {
    return {
      carName: 'Bilinmeyen Araç',
      brand: '',
      limiterRpm: fallbackMaxRpm,
      shiftRpm: Math.round(fallbackMaxRpm * 0.96),
      idleRpm: 800
    };
  }

  if (carCache.has(carModel)) {
    return carCache.get(carModel);
  }

  let carName = carModel;
  let brand = '';
  let limiterRpm = 0;
  let idleRpm = 800;
  let curveMaxRpm = 0;
  let hasTurbo = false;
  let hasERS = false;

  const carDir = path.join(getCarsDir(), carModel);

  // 1. Try reading ui/ui_car.json
  const uiJsonPath = path.join(carDir, 'ui', 'ui_car.json');
  if (fs.existsSync(uiJsonPath)) {
    try {
      const raw = fs.readFileSync(uiJsonPath, 'utf8');
      const data = safeJsonParse(raw);
      if (data && data.name) carName = data.name;
      if (data && data.brand) brand = data.brand;

      const jsonStr = (raw || '').toLowerCase();
      if (jsonStr.includes('turbo') || jsonStr.includes('vr38') || jsonStr.includes('biturbo') || jsonStr.includes('twin-turbo')) {
        hasTurbo = true;
      }
      if (/\b(hybrid|kers|ers|mgu-k|mgu-h|mgu)\b/i.test(jsonStr)) {
        hasERS = true;
      }

      if (data && Array.isArray(data.powerCurve) && data.powerCurve.length > 0) {
        const lastEntry = data.powerCurve[data.powerCurve.length - 1];
        if (lastEntry && lastEntry[0]) {
          curveMaxRpm = parseInt(lastEntry[0], 10) || 0;
        }
      }
    } catch (e) {
      // ignore
    }
  }

  // 2. Try checking data/turbo.ini, ers.ini, kers.ini
  const turboIniPath = path.join(carDir, 'data', 'turbo.ini');
  if (fs.existsSync(turboIniPath)) {
    hasTurbo = true;
  }
  const ersIniPath = path.join(carDir, 'data', 'ers.ini');
  const kersIniPath = path.join(carDir, 'data', 'kers.ini');
  if (fs.existsSync(ersIniPath) || fs.existsSync(kersIniPath)) {
    hasERS = true;
  }

  // 3. Try reading data/engine.ini (unpacked or mod car)
  const engineIniPath = path.join(carDir, 'data', 'engine.ini');
  if (fs.existsSync(engineIniPath)) {
    try {
      const content = fs.readFileSync(engineIniPath, 'utf8');
      const engineData = parseIniSection(content, 'ENGINE_DATA');
      if (engineData.LIMITER) {
        const parsedLimiter = parseInt(engineData.LIMITER, 10);
        if (parsedLimiter > 0) limiterRpm = parsedLimiter;
      }
      if (engineData.MINIMUM) {
        const parsedIdle = parseInt(engineData.MINIMUM, 10);
        if (parsedIdle > 0) idleRpm = parsedIdle;
      }
    } catch (e) {
      // ignore
    }
  }

  // 3. Try reading data/car.ini for SCREEN_NAME
  const carIniPath = path.join(carDir, 'data', 'car.ini');
  if (fs.existsSync(carIniPath)) {
    try {
      const content = fs.readFileSync(carIniPath, 'utf8');
      const infoData = parseIniSection(content, 'INFO');
      if (infoData.SCREEN_NAME) {
        carName = infoData.SCREEN_NAME;
      }
    } catch (e) {
      // ignore
    }
  }

  // Final resolution of limiter and shift point
  if (!limiterRpm) {
    if (fallbackMaxRpm > 0 && fallbackMaxRpm !== 8500) {
      limiterRpm = fallbackMaxRpm;
    } else if (curveMaxRpm > 0) {
      limiterRpm = curveMaxRpm;
    } else {
      limiterRpm = fallbackMaxRpm || 8500;
    }
  }

  // Ideal shift point is ~96.5% of limiter or limiter - 220 RPM
  const shiftRpm = Math.min(
    Math.round(limiterRpm * 0.965),
    Math.max(limiterRpm - 250, Math.round(limiterRpm * 0.94))
  );

    const lower = carModel.toLowerCase();
  if (lower.includes('turbo') || lower.includes('919') || lower.includes('r18') || lower.includes('488') || lower.includes('gtr') || lower.includes('gt-r') || lower.includes('nismo') || lower.includes('ts040') || lower.includes('toyota') || lower.includes('sf70') || lower.includes('sf15') || lower.includes('f1') || lower.includes('lmp1')) {
    hasTurbo = true;
  }
  if (lower.includes('919') || lower.includes('hybrid') || lower.includes('r18') || lower.includes('p1') || lower.includes('laferrari') || lower.includes('ts040') || lower.includes('toyota') || lower.includes('sf70') || lower.includes('sf15') || lower.includes('f1') || lower.includes('lmp1')) {
    hasERS = true;
  }
  const hasDRS = (lower.includes('f1') || lower.includes('sf70') || lower.includes('sf15') || lower.includes('p1') || lower.includes('drs') || lower.includes('dtm'));

  // Default baseline checks
  let hasABS = !(lower.includes('919') || lower.includes('ts040') || lower.includes('r18') || lower.includes('sf70') || lower.includes('sf15') || lower.includes('f1') || lower.includes('f40') || lower.includes('cobra') || lower.includes('lotus_25') || lower.includes('lotus_49') || lower.includes('rsr') || lower.includes('gte') || lower.includes('c7r'));
  let hasTC = !(lower.includes('lotus_25') || lower.includes('lotus_49') || lower.includes('cobra') || lower.includes('f40') || lower.includes('98t') || lower.includes('miura') || lower.includes('250_gto') || lower.includes('yellowbird') || lower.includes('shelby') || lower.includes('alfa_33') || lower.includes('787b') || lower.includes('c9'));
  let tcStages = hasTC ? 1 : 0;
  let absStages = hasABS ? 1 : 0;

  // Exact resolution from data.acd or unpacked data folder
  const acdEntries = readAcdEntries(carDir, carModel);
  if (acdEntries) {
    if (acdEntries.has('turbo.ini')) hasTurbo = true;
    if (acdEntries.has('ers.ini') || acdEntries.has('kers.ini')) hasERS = true;

    // Check traction control LUT
    for (const [k, text] of acdEntries.entries()) {
      if (k.toLowerCase().includes('traction') && k.endsWith('.lut')) {
        const count = text.split(/\r?\n/).filter(l => l.includes('|')).length;
        if (count > 0) {
          tcStages = count;
          hasTC = true;
        }
      }
      if (k.toLowerCase().includes('abs') && k.endsWith('.lut')) {
        const count = text.split(/\r?\n/).filter(l => l.includes('|')).length;
        if (count > 0) {
          absStages = count;
          hasABS = true;
        }
      }
    }

    const elec = acdEntries.get('electronics.ini');
    if (elec) {
      if (elec.includes('TRACTION_CONTROL')) {
        const tcPres = elec.match(/PRESENT\s*=\s*([0-9]+)/i);
        if (tcPres && parseInt(tcPres[1], 10) === 0) {
          hasTC = false;
          tcStages = 0;
        } else if (tcStages === 0) {
          hasTC = true;
          tcStages = 1;
        }
      }
      if (elec.includes('ABS')) {
        const absPres = elec.match(/PRESENT\s*=\s*([0-9]+)/i);
        if (absPres && parseInt(absPres[1], 10) === 0) {
          hasABS = false;
          absStages = 0;
        } else if (absStages === 0) {
          hasABS = true;
          absStages = 1;
        }
      }
    }
  }

  // Known fallback heuristics for unpacked/modded special cases
  if (lower.includes('bmw_z4_gt3')) {
    tcStages = 7;
    absStages = 7;
  } else if (lower.includes('gtr') || lower.includes('gt-r') || lower.includes('nismo')) {
    tcStages = 7;
    absStages = 8;
    hasTC = true;
    hasABS = true;
  } else if (lower.includes('r8_lms') || lower.includes('650s_gt3')) {
    tcStages = 8;
    absStages = 8;
  } else if (lower.includes('ts040') || lower.includes('919') || lower.includes('r18')) {
    tcStages = 7;
    absStages = 0;
    hasABS = false;
  } else if (lower.includes('gte') || lower.includes('rsr') || lower.includes('c7r')) {
    tcStages = 9;
    absStages = 0;
    hasABS = false;
  } else if (lower.includes('sf70') || lower.includes('sf15') || lower.includes('f1')) {
    tcStages = 0;
    absStages = 0;
    hasTC = false;
    hasABS = false;
  } else if (lower.includes('cayman_gt4')) {
    tcStages = 1;
    absStages = 1;
  } else if (lower.includes('laferrari') || lower.includes('p1') || lower.includes('918')) {
    tcStages = 3;
    absStages = 1;
  }

  const info = {
    carName,
    brand,
    limiterRpm,
    shiftRpm,
    idleRpm,
    hasTurbo,
    hasERS,
    hasDRS,
    hasABS,
    hasTC,
    tcStages,
    absStages
  };

  carCache.set(carModel, info);
  console.log(`[CarInfoReader] Resolved car '${carModel}': Name='${carName}', Limiter=${limiterRpm} RPM, Shift=${shiftRpm} RPM, Idle=${idleRpm} RPM`);
  return info;
}

function getTrackInfo(track, trackConfiguration = '') {
  if (!track) return { trackName: 'Bilinmeyen Pist' };

  const cacheKey = `${track}_${trackConfiguration || 'default'}`;
  if (trackCache.has(cacheKey)) {
    return trackCache.get(cacheKey);
  }

  let trackName = track;
  const trackDir = path.join(getTracksDir(), track);

  // If trackConfiguration specified, check config subfolder first
  if (trackConfiguration) {
    const configJson = path.join(trackDir, 'ui', trackConfiguration, 'ui_track.json');
    if (fs.existsSync(configJson)) {
      try {
        const raw = fs.readFileSync(configJson, 'utf8');
        const data = safeJsonParse(raw);
        if (data && data.name) trackName = data.name;
      } catch (e) {}
    }
  }

  if (trackName === track) {
    const mainJson = path.join(trackDir, 'ui', 'ui_track.json');
    if (fs.existsSync(mainJson)) {
      try {
        const raw = fs.readFileSync(mainJson, 'utf8');
        const data = safeJsonParse(raw);
        if (data && data.name) trackName = data.name;
      } catch (e) {}
    }
  }

  const info = { trackName };
  trackCache.set(cacheKey, info);
  console.log(`[CarInfoReader] Resolved track '${track}' (${trackConfiguration}): '${trackName}'`);
  return info;
}

module.exports = {
  getCarInfo,
  getTrackInfo
};
