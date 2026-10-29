const fs = require('fs');
const path = require('path');
const acPathDetector = require('./acPathDetector');

class GhostTracker {
  constructor() {
    this.currentCar = '';
    this.currentTrack = '';
    this.currentConfig = '';
    this.ghostData = null; // { posList: Float64Array, timeList: Int32Array, bestLapMs: number }
    this.lastDelta = 0;
    this.lastDeltaCheckTime = 0;
    this.deltaTrend = 'neutral';
    this.lastTrendTime = 0;
  }

  getSidekickPbDir() {
    const install = acPathDetector.getInstallDir() || 'D:\\Saved Games\\Steam\\steamapps\\common\\assettocorsa';
    return path.join(install, 'apps', 'python', 'Sidekick', 'personal_best');
  }

  getActelPbDir() {
    const install = acPathDetector.getInstallDir() || 'D:\\Saved Games\\Steam\\steamapps\\common\\assettocorsa';
    return path.join(install, 'apps', 'python', 'actel_bridge', 'personal_best');
  }

  parsePosList(buf) {
    const floats = [];
    for (let i = 0; i < buf.length - 8; i++) {
      if (buf[i] === 0x47) { // BINFLOAT in pickle
        floats.push(buf.readDoubleBE(i + 1));
        i += 8;
      }
    }
    return floats;
  }

  parseTimeList(buf) {
    const times = [];
    let i = 0;
    while (i < buf.length) {
      const op = buf[i];
      if (op === 0x4d) { // BININT2 (unsigned 16-bit)
        times.push(buf.readUInt16LE(i + 1));
        i += 3;
      } else if (op === 0x4a) { // BININT (signed 32-bit)
        times.push(buf.readInt32LE(i + 1));
        i += 5;
      } else if (op === 0x4b) { // BININT1 (unsigned 8-bit)
        times.push(buf[i + 1]);
        i += 2;
      } else {
        i++;
      }
    }
    return times;
  }

  loadGhost(carModel, trackName, trackConfig = '') {
    if (!carModel || !trackName) return null;

    if (this.currentCar === carModel && this.currentTrack === trackName && this.currentConfig === trackConfig && this.ghostData) {
      return this.ghostData;
    }

    this.currentCar = carModel;
    this.currentTrack = trackName;
    this.currentConfig = trackConfig || '';
    this.ghostData = null;
    this.lastDelta = 0;
    this.deltaTrend = 'neutral';

    const searchDirs = [this.getSidekickPbDir(), this.getActelPbDir()];

    // Generate potential candidate file prefixes
    const cClean = carModel.toLowerCase().trim();
    const tClean = trackName.toLowerCase().trim();
    const cfgClean = (trackConfig || '').toLowerCase().trim();

    const prefixes = [
      `${carModel}_${trackName}${trackConfig}`,
      `${cClean}_${tClean}${cfgClean}`,
      `${carModel}_${trackName}_${trackConfig}`,
      `${cClean}_${tClean}_${cfgClean}`,
      `${carModel}_${trackName}`,
      `${cClean}_${tClean}`
    ];

    for (const dir of searchDirs) {
      if (!fs.existsSync(dir)) continue;

      for (const prefix of prefixes) {
        const posFile = path.join(dir, `${prefix}_pbposlist.ini`);
        const timeFile = path.join(dir, `${prefix}_pbtimelist.ini`);
        const lapFile = path.join(dir, `${prefix}_pb.ini`);

        if (fs.existsSync(posFile) && fs.existsSync(timeFile)) {
          try {
            const posBuf = fs.readFileSync(posFile);
            const timeBuf = fs.readFileSync(timeFile);

            const posList = this.parsePosList(posBuf);
            const timeList = this.parseTimeList(timeBuf);

            if (posList.length > 10 && timeList.length > 10) {
              let bestLapMs = 0;
              if (fs.existsSync(lapFile)) {
                try {
                  const rawLap = fs.readFileSync(lapFile);
                  // Check if pickle
                  if (rawLap[0] === 0x80) {
                    let j = 0;
                    while (j < rawLap.length) {
                      if (rawLap[j] === 0x4a) { bestLapMs = rawLap.readInt32LE(j + 1); break; }
                      if (rawLap[j] === 0x4d) { bestLapMs = rawLap.readUInt16LE(j + 1); break; }
                      j++;
                    }
                  } else {
                    bestLapMs = parseInt(rawLap.toString().trim(), 10) || 0;
                  }
                } catch (_) {}
              }
              if (!bestLapMs && timeList.length > 0) {
                bestLapMs = timeList[timeList.length - 1];
              }

              this.ghostData = {
                posList,
                timeList,
                bestLapMs,
                count: Math.min(posList.length, timeList.length)
              };

              console.log(`[GhostTracker] Loaded ghost for ${carModel} on ${trackName}: ${posList.length} samples, best: ${bestLapMs}ms`);
              return this.ghostData;
            }
          } catch (err) {
            console.warn('[GhostTracker] Error loading ghost files:', err.message);
          }
        }
      }
    }

    return null;
  }

  computeDelta(lapTimeMs, splinePos) {
    if (!this.ghostData || splinePos <= 0.003 || splinePos >= 0.998) {
      return null;
    }

    const { posList, timeList, count } = this.ghostData;
    let low = 0;
    let high = count - 1;
    let idx = 0;

    while (low <= high) {
      const mid = (low + high) >> 1;
      if (posList[mid] <= splinePos) {
        idx = mid;
        low = mid + 1;
      } else {
        high = mid - 1;
      }
    }

    const ghostTime = timeList[idx];
    if (typeof ghostTime !== 'number') return null;

    // Delta = Current lap elapsed time - Ghost elapsed time
    // Positive (+) => User took more time than ghost => User is slower / Ghost ahead
    // Negative (-) => User took less time than ghost => User is faster / User ahead
    const deltaSec = (lapTimeMs - ghostTime) / 1000.0;
    const now = Date.now();

    // Trend calculation
    if (now - this.lastDeltaCheckTime >= 120) {
      const diff = deltaSec - this.lastDelta;
      if (diff < -0.010) {
        // Delta decreasing => User gaining time / closing gap / pulling away
        this.deltaTrend = 'improving';
        this.lastTrendTime = now;
      } else if (diff > 0.010) {
        // Delta increasing => User losing time / ghost pulling away / ghost catching up
        this.deltaTrend = 'worsening';
        this.lastTrendTime = now;
      } else if (now - this.lastTrendTime > 400) {
        this.deltaTrend = 'neutral';
      }

      this.lastDelta = deltaSec;
      this.lastDeltaCheckTime = now;
    }

    return {
      delta: Number(deltaSec.toFixed(3)),
      trend: this.deltaTrend,
      bestLapMs: this.ghostData.bestLapMs
    };
  }

  reset() {
    this.lastDelta = 0;
    this.lastDeltaCheckTime = 0;
    this.deltaTrend = 'neutral';
    this.lastTrendTime = 0;
  }
}

module.exports = new GhostTracker();

