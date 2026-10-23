const fs = require('fs');
const path = require('path');

// Assetto Corsa Content Paths
const AC_CONTENT_ROOT = 'D:\\Saved Games\\SteamLibrary\\steamapps\\common\\assettocorsa\\content';
const CARS_DIR = path.join(AC_CONTENT_ROOT, 'cars');
const TRACKS_DIR = path.join(AC_CONTENT_ROOT, 'tracks');

const carCache = new Map();
const trackCache = new Map();

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

  const carDir = path.join(CARS_DIR, carModel);

  // 1. Try reading ui/ui_car.json
  const uiJsonPath = path.join(carDir, 'ui', 'ui_car.json');
  if (fs.existsSync(uiJsonPath)) {
    try {
      const raw = fs.readFileSync(uiJsonPath, 'utf8');
      const data = safeJsonParse(raw);
      if (data && data.name) carName = data.name;
      if (data && data.brand) brand = data.brand;

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

  // 2. Try reading data/engine.ini (unpacked or mod car)
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

  const info = {
    carName,
    brand,
    limiterRpm,
    shiftRpm,
    idleRpm
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
  const trackDir = path.join(TRACKS_DIR, track);

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
