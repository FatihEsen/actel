const koffi = require('koffi');
const carInfoReader = require('./carInfoReader');
const ghostTracker = require('./ghostTracker');

const kernel32 = koffi.load('kernel32.dll');

const OpenFileMappingW = kernel32.func('OpenFileMappingW', 'void*', ['uint32', 'bool', 'str16']);
const MapViewOfFile = kernel32.func('MapViewOfFile', 'void*', ['void*', 'uint32', 'uint32', 'uint32', 'size_t']);
const UnmapViewOfFile = kernel32.func('UnmapViewOfFile', 'bool', ['void*']);
const CloseHandle = kernel32.func('CloseHandle', 'bool', ['void*']);

const FILE_MAP_READ = 0x0004;

// Definining struct types for Assetto Corsa Shared Memory
// SPageFilePhysics
const SPageFilePhysics = koffi.struct('SPageFilePhysics', {
  packetId: 'int32',
  gas: 'float',
  brake: 'float',
  fuel: 'float',
  gear: 'int32',
  rpms: 'int32',
  steerAngle: 'float',
  speedKmh: 'float',
  velocity: koffi.array('float', 3),
  accG: koffi.array('float', 3),
  wheelSlip: koffi.array('float', 4),
  wheelLoad: koffi.array('float', 4),
  wheelsPressure: koffi.array('float', 4),
  wheelAngularSpeed: koffi.array('float', 4),
  tyreWear: koffi.array('float', 4),
  tyreDirtyLevel: koffi.array('float', 4),
  tyreCoreTemperature: koffi.array('float', 4),
  camberRAD: koffi.array('float', 4),
  suspensionTravel: koffi.array('float', 4),
  drs: 'float',
  tc: 'float',
  heading: 'float',
  pitch: 'float',
  roll: 'float',
  cgHeight: 'float',
  carDamage: koffi.array('float', 5),
  numberOfTyresOut: 'int32',
  pitLimiterOn: 'int32',
  abs: 'float',
  kersCharge: 'float',
  kersInput: 'float',
  autoShifterOn: 'int32',
  rideHeight: koffi.array('float', 2),
  turboBoost: 'float',
  ballast: 'float',
  airDensity: 'float',
  airTemp: 'float',
  roadTemp: 'float',
  localAngularVel: koffi.array('float', 3),
  finalFF: 'float',
  performanceMeter: 'float',
  engineBrake: 'int32',
  ersRecoveryLevel: 'int32',
  ersPowerLevel: 'int32',
  ersHeatCharging: 'int32',
  ersIsCharging: 'int32',
  kersCurrentKJ: 'float',
  drsAvailable: 'int32',
  drsEnabled: 'int32',
  brakeTemp: koffi.array('float', 4),
  clutch: 'float',
  tyreTempI: koffi.array('float', 4),
  tyreTempM: koffi.array('float', 4),
  tyreTempO: koffi.array('float', 4),
  isAIControlled: 'int32',
  tyreContactPoint: koffi.array('float', 12),
  tyreContactNormal: koffi.array('float', 12),
  tyreContactHeading: koffi.array('float', 12),
  brakeBias: 'float',
  localVelocity: koffi.array('float', 3)
});

// SPageFileGraphic
const SPageFileGraphic = koffi.struct('SPageFileGraphic', {
  packetId: 'int32',
  status: 'int32',
  session: 'int32',
  currentTime: koffi.array('char16', 15),
  lastTime: koffi.array('char16', 15),
  bestTime: koffi.array('char16', 15),
  split: koffi.array('char16', 15),
  completedLaps: 'int32',
  position: 'int32',
  iCurrentTime: 'int32',
  iLastTime: 'int32',
  iBestTime: 'int32',
  sessionTimeLeft: 'float',
  distanceTraveled: 'float',
  isInPit: 'int32',
  currentSectorIndex: 'int32',
  lastSectorTime: 'int32',
  numberOfLaps: 'int32',
  tyreCompound: koffi.array('char16', 33),
  replayTimeMultiplier: 'float',
  normalizedCarPosition: 'float',
  carCoordinates: koffi.array('float', 3),
  penaltyTime: 'float',
  flag: 'int32',
  idealLineOn: 'int32',
  isInPitLane: 'int32',
  surfaceGrip: 'float',
  mandatoryPitDone: 'int32',
  windSpeed: 'float',
  windDirection: 'float'
});

// SPageFileStatic
const SPageFileStatic = koffi.struct('SPageFileStatic', {
  smVersion: koffi.array('char16', 15),
  acVersion: koffi.array('char16', 15),
  numberOfSessions: 'int32',
  numCars: 'int32',
  carModel: koffi.array('char16', 33),
  track: koffi.array('char16', 33),
  playerName: koffi.array('char16', 33),
  playerSurname: koffi.array('char16', 33),
  playerNick: koffi.array('char16', 33),
  sectorCount: 'int32',
  maxTorque: 'float',
  maxPower: 'float',
  maxRpm: 'int32',
  maxFuel: 'float',
  suspensionMaxTravel: koffi.array('float', 4),
  tyreRadius: koffi.array('float', 4),
  maxTurboBoost: 'float',
  deprecated_1: 'float',
  deprecated_2: 'float',
  penaltiesEnabled: 'int32',
  aidFuelRate: 'float',
  aidTireRate: 'float',
  aidMechanicalDamage: 'float',
  aidAllowTyreBlankets: 'int32',
  aidStability: 'float',
  aidAutoClutch: 'int32',
  aidAutoBlip: 'int32',
  hasDRS: 'int32',
  hasERS: 'int32',
  hasKERS: 'int32',
  kersMaxJ: 'float',
  engineBrakeSettingsCount: 'int32',
  ersPowerControllerCount: 'int32',
  trackSPlineLength: 'float',
  trackConfiguration: koffi.array('char16', 33),
  ersMaxJ: 'float',
  isTimedRace: 'int32',
  hasExtraLap: 'int32',
  carSkin: koffi.array('char16', 33),
  reversedGridPositions: 'int32',
  pitWindowStart: 'int32',
  pitWindowEnd: 'int32',
  isOnline: 'int32'
});

function decodeWcharArray(arr) {
  if (!arr) return '';
  let str = '';
  for (let i = 0; i < arr.length; i++) {
    const code = typeof arr[i] === 'number' ? arr[i] : (arr[i] ? arr[i].charCodeAt(0) : 0);
    if (code === 0) break;
    str += String.fromCharCode(code);
  }
  return str.trim();
}

class ACReader {
  constructor() {
    this.hPhysics = null;
    this.hGraphic = null;
    this.hStatic = null;

    this.ptrPhysics = null;
    this.ptrGraphic = null;
    this.ptrStatic = null;

    this.isConnected = false;
    this.lastPacketId = -1;
    this.prevSuspension = [0, 0, 0, 0];
    this.prevAccGZ = 0;
    this.prevDamage = 0;
    this.prevAccG = [0, 0, 0];
    this.lastCollisionTime = 0;
    this.lastDelta = 0;
    this.lastDeltaCheckTime = 0;
    this.deltaTrend = 'neutral';
    this.isLapCut = false;
    this.lastLapNumber = -1;
    this.lastGraphicStatus = 0;
    this.isOutLap = true;
    this.carWasInPit = true;
  }

  isLive() {
    return this.isConnected && this.lastGraphicStatus === 2;
  }

  connect() {
    if (this.isConnected) return true;

    try {
      this.hPhysics = OpenFileMappingW(FILE_MAP_READ, false, 'Local\\acpmf_physics') || OpenFileMappingW(FILE_MAP_READ, false, 'acpmf_physics');
      this.hGraphic = OpenFileMappingW(FILE_MAP_READ, false, 'Local\\acpmf_graphics') || OpenFileMappingW(FILE_MAP_READ, false, 'acpmf_graphics');
      this.hStatic = OpenFileMappingW(FILE_MAP_READ, false, 'Local\\acpmf_static') || OpenFileMappingW(FILE_MAP_READ, false, 'acpmf_static');

      if (!this.hPhysics || !this.hGraphic || !this.hStatic) {
        this.disconnect();
        return false;
      }

      this.ptrPhysics = MapViewOfFile(this.hPhysics, FILE_MAP_READ, 0, 0, 0);
      this.ptrGraphic = MapViewOfFile(this.hGraphic, FILE_MAP_READ, 0, 0, 0);
      this.ptrStatic = MapViewOfFile(this.hStatic, FILE_MAP_READ, 0, 0, 0);

      if (!this.ptrPhysics || !this.ptrGraphic || !this.ptrStatic) {
        this.disconnect();
        return false;
      }

      this.isConnected = true;
      return true;
    } catch (err) {
      this.disconnect();
      return false;
    }
  }

  disconnect() {
    if (this.ptrPhysics) { UnmapViewOfFile(this.ptrPhysics); this.ptrPhysics = null; }
    if (this.ptrGraphic) { UnmapViewOfFile(this.ptrGraphic); this.ptrGraphic = null; }
    if (this.ptrStatic) { UnmapViewOfFile(this.ptrStatic); this.ptrStatic = null; }

    if (this.hPhysics) { CloseHandle(this.hPhysics); this.hPhysics = null; }
    if (this.hGraphic) { CloseHandle(this.hGraphic); this.hGraphic = null; }
    if (this.hStatic) { CloseHandle(this.hStatic); this.hStatic = null; }

    this.prevSuspension = [0, 0, 0, 0];
    this.prevAccGZ = 0;
    this.prevDamage = 0;
    this.prevAccG = [0, 0, 0];
    this.lastCollisionTime = 0;
    this.lastDelta = 0;
    this.lastDeltaCheckTime = 0;
    this.deltaTrend = 'neutral';
    this.isLapCut = false;
    this.lastLapNumber = -1;
    this.lastGraphicStatus = 0;
    this.isOutLap = true;
    this.carWasInPit = true;
    try {
      const acController = require('./acController');
      acController.resetIndicators();
    } catch (_) {}

    this.isConnected = false;
  }

  readTelemetry() {
    if (!this.isConnected) {
      if (!this.connect()) {
        return null;
      }
    }

    try {
      const physics = koffi.decode(this.ptrPhysics, SPageFilePhysics);
      const graphic = koffi.decode(this.ptrGraphic, SPageFileGraphic);
      const staticInfo = koffi.decode(this.ptrStatic, SPageFileStatic);

      // Check if AC closed (packetId unchanged for a long time or all 0s)
      if (!physics || (physics.packetId === 0 && graphic.packetId === 0 && graphic.status === 0)) {
        if (this.lastGraphicStatus !== 0) {
          this.lastGraphicStatus = 0;
          try { require('./acController').resetIndicators(); } catch (_) {}
        }
        return null;
      }

      // Check status transitions for clean reset / sync
      if (graphic.status !== this.lastGraphicStatus) {
        if (graphic.status === 2 || graphic.status === 0) {
          try { require('./acController').resetIndicators(); } catch (_) {}
        }
        this.lastGraphicStatus = graphic.status;
      }

      // Sync physical pit limiter state directly
      try {
        require('./acController').setPitLimiter(physics.pitLimiterOn > 0);
      } catch (_) {}

      // Convert gear to string: 0 = R, 1 = N, 2 = 1, etc.
      let gearStr = 'N';
      if (physics.gear === 0) gearStr = 'R';
      else if (physics.gear === 1) gearStr = 'N';
      else gearStr = (physics.gear - 1).toString();

      // Calculate kerb / kasis / surface rumble
      let surfaceRumble = 0.0;
      if (physics.speedKmh > 15) {
        const dSusp0 = Math.abs(physics.suspensionTravel[0] - this.prevSuspension[0]);
        const dSusp1 = Math.abs(physics.suspensionTravel[1] - this.prevSuspension[1]);
        const dSusp2 = Math.abs(physics.suspensionTravel[2] - this.prevSuspension[2]);
        const dSusp3 = Math.abs(physics.suspensionTravel[3] - this.prevSuspension[3]);
        const maxSuspDelta = Math.max(dSusp0, dSusp1, dSusp2, dSusp3);

        const dVertG = Math.abs(physics.accG[2] - this.prevAccGZ);

        let bump = 0;
        // Sharp suspension oscillation (kasis, kerb sawtooth)
        if (maxSuspDelta > 0.003) {
          bump = Math.min(1.0, (maxSuspDelta - 0.003) * 70);
        }
        // Vertical G impact
        if (dVertG > 0.35) {
          bump = Math.max(bump, Math.min(1.0, (dVertG - 0.35) * 1.6));
        }
        // Off-track grass / gravel surface
        if (physics.numberOfTyresOut > 0) {
          const grass = Math.min(1.0, (physics.numberOfTyresOut * 0.25) * (physics.speedKmh / 70));
          bump = Math.max(bump, grass);
        }

        surfaceRumble = Number(bump.toFixed(2));
      }

      // Collision / Impact Detection
      let totalDamage = 0;
      if (physics.carDamage) {
        totalDamage = (physics.carDamage[0] || 0) + (physics.carDamage[1] || 0) +
                      (physics.carDamage[2] || 0) + (physics.carDamage[3] || 0) +
                      (physics.carDamage[4] || 0);
      }
      const deltaDamage = Math.max(0, totalDamage - this.prevDamage);

      const dAccX = Math.abs(physics.accG[0] - this.prevAccG[0]);
      const dAccY = Math.abs(physics.accG[1] - this.prevAccG[1]);
      const dAccZ = Math.abs(physics.accG[2] - this.prevAccG[2]);
      const maxGSpike = Math.max(dAccX, dAccY, dAccZ);

      const now = Date.now();
      let collision = false;
      if (deltaDamage > 0.08 || (physics.speedKmh > 12 && maxGSpike > 3.2)) {
        if (now - this.lastCollisionTime > 600) {
          collision = true;
          this.lastCollisionTime = now;
        }
      }
      this.prevDamage = totalDamage;
      this.prevAccG = [physics.accG[0], physics.accG[1], physics.accG[2]];

      this.prevSuspension = [
        physics.suspensionTravel[0],
        physics.suspensionTravel[1],
        physics.suspensionTravel[2],
        physics.suspensionTravel[3]
      ];
      this.prevAccGZ = physics.accG[2];

      // Lap cut detection (turns red on track cut until next lap)
      const currentLap = graphic.completedLaps;
      const currentTime = graphic.iCurrentTime || 0;
      const isInPit = Boolean(graphic.isInPit || graphic.isInPitLane);
      const speedKmh = physics.speedKmh || 0;
      const session = graphic.session;

      if (this.lastLapNumber !== currentLap || currentTime < 1000) {
        if (this.lastLapNumber !== -1 && currentLap > this.lastLapNumber) {
          // Completed at least one lap, out-lap is definitely over
          this.isOutLap = false;
          this.carWasInPit = false;
        }
        this.lastLapNumber = currentLap;
        this.isLapCut = false;
      }
      if ((physics.numberOfTyresOut >= 3 || (graphic.penaltyTime && graphic.penaltyTime > 0)) && !isInPit) {
        this.isLapCut = true;
      }

      // Track out-lap status (Sidekick logic)
      if (isInPit) {
        this.isOutLap = true;
        this.carWasInPit = true;
      }

      // If out-lap car crosses the start/finish line to begin hot lap:
      // iCurrentTime resets to < 1500ms while moving on track (> 25 km/h) outside of pit
      if (this.isOutLap && !isInPit && currentTime > 0 && currentTime < 1500 && speedKmh > 25) {
        this.isOutLap = false;
        this.carWasInPit = false;
      }

      // In race session (session === 2), race starts from grid so out-lap is false once green
      if (session === 2 && !isInPit && currentTime > 1000) {
        this.isOutLap = false;
        this.carWasInPit = false;
      }

      // Ghost Delta & Delta Trend (PerformanceMeter or GhostTracker for Personal Best)
      let rawDelta = null;
      let activeTrend = 'neutral';
      let formattedGhostDelta = '';

      // Only calculate delta if NOT on out-lap, NOT in pit, and after the first 3.5 seconds of the lap
      if (!this.isOutLap && !isInPit && currentTime >= 3500) {
        const splinePos = typeof graphic.normalizedCarPosition === 'number' ? graphic.normalizedCarPosition : 0;
        const ghostRes = ghostTracker.computeDelta(currentTime, splinePos);

        if (ghostRes) {
          // ALWAYS prioritize ghostRes when PB data is available!
          rawDelta = ghostRes.delta;
          activeTrend = ghostRes.trend;
        } else if (typeof physics.performanceMeter === 'number' && !isNaN(physics.performanceMeter) && physics.performanceMeter !== 0) {
          rawDelta = physics.performanceMeter;
          if (now - this.lastDeltaCheckTime > 150) {
            const deltaDiff = rawDelta - this.lastDelta;
            if (deltaDiff < -0.012) {
              this.deltaTrend = 'improving';
            } else if (deltaDiff > 0.012) {
              this.deltaTrend = 'worsening';
            } else if (now - this.lastDeltaCheckTime > 800) {
              this.deltaTrend = 'neutral';
            }
            this.lastDelta = rawDelta;
            this.lastDeltaCheckTime = now;
          }
          activeTrend = this.deltaTrend;
        }

        if (rawDelta !== null) {
          formattedGhostDelta = (rawDelta >= 0 ? '+' : '-') + Math.abs(rawDelta).toFixed(2) + 's';
        }
      } else {
        ghostTracker.reset();
      }
      

      return {
        connected: true,
        source: 'AC_LIVE',
        timestamp: Date.now(),
        ghostDelta: formattedGhostDelta,
        delta: rawDelta !== null ? Number(rawDelta.toFixed(3)) : null,
        deltaTrend: activeTrend,
        isLapCut: this.isLapCut,
        physics: {
          packetId: physics.packetId,
          gas: Math.max(0, Math.min(1, physics.gas)),
          brake: Math.max(0, Math.min(1, physics.brake)),
          clutch: Math.max(0, Math.min(1, physics.clutch)),
          fuel: Number(physics.fuel.toFixed(2)),
          gear: physics.gear,
          gearStr: gearStr,
          rpms: Math.round(physics.rpms),
          steerAngle: Number(physics.steerAngle.toFixed(2)),
          speedKmh: Math.round(physics.speedKmh),
          accG: {
            lateral: Number(physics.accG[0].toFixed(2)),
            longitudinal: Number(physics.accG[1].toFixed(2)),
            vertical: Number(physics.accG[2].toFixed(2))
          },
          tyres: {
            pressure: [
              Number(physics.wheelsPressure[0].toFixed(1)),
              Number(physics.wheelsPressure[1].toFixed(1)),
              Number(physics.wheelsPressure[2].toFixed(1)),
              Number(physics.wheelsPressure[3].toFixed(1))
            ],
            tempCore: [
              Math.round(physics.tyreCoreTemperature[0]),
              Math.round(physics.tyreCoreTemperature[1]),
              Math.round(physics.tyreCoreTemperature[2]),
              Math.round(physics.tyreCoreTemperature[3])
            ],
            tempI: [
              Math.round(physics.tyreTempI[0]),
              Math.round(physics.tyreTempI[1]),
              Math.round(physics.tyreTempI[2]),
              Math.round(physics.tyreTempI[3])
            ],
            tempM: [
              Math.round(physics.tyreTempM[0]),
              Math.round(physics.tyreTempM[1]),
              Math.round(physics.tyreTempM[2]),
              Math.round(physics.tyreTempM[3])
            ],
            tempO: [
              Math.round(physics.tyreTempO[0]),
              Math.round(physics.tyreTempO[1]),
              Math.round(physics.tyreTempO[2]),
              Math.round(physics.tyreTempO[3])
            ],
            wear: [
              Number((physics.tyreWear[0] * 100).toFixed(1)),
              Number((physics.tyreWear[1] * 100).toFixed(1)),
              Number((physics.tyreWear[2] * 100).toFixed(1)),
              Number((physics.tyreWear[3] * 100).toFixed(1))
            ],
            brakeTemp: [
              Math.round(physics.brakeTemp[0]),
              Math.round(physics.brakeTemp[1]),
              Math.round(physics.brakeTemp[2]),
              Math.round(physics.brakeTemp[3])
            ]
          },
          drs: physics.drs > 0,
          drsAvailable: physics.drsAvailable > 0,
          drsEnabled: physics.drsEnabled > 0,
          tc: Number(physics.tc.toFixed(2)),
          tcActive: Boolean(physics.gas > 0.15 && (Math.max(physics.wheelSlip[2], physics.wheelSlip[3]) > 0.35 || Math.max(physics.wheelSlip[0], physics.wheelSlip[1], physics.wheelSlip[2], physics.wheelSlip[3]) > 0.45) && physics.tc > 0),
          abs: Number(physics.abs.toFixed(2)),
          absActive: Boolean(physics.speedKmh > 4 && physics.brake > 0.12 && Math.max(physics.wheelSlip[0], physics.wheelSlip[1], physics.wheelSlip[2], physics.wheelSlip[3]) > 0.35 && physics.abs > 0),
          performanceMeter: Number((physics.performanceMeter || 0).toFixed(3)),
          pitLimiterOn: physics.pitLimiterOn > 0,
          turboBoost: Number(physics.turboBoost.toFixed(2)),
          airTemp: Math.round(physics.airTemp),
          roadTemp: Math.round(physics.roadTemp),
          brakeBias: Number(physics.brakeBias.toFixed(3)),
          engineBrake: physics.engineBrake,
          kersCharge: Number((physics.kersCharge * 100).toFixed(1)),
          finalFF: Number(physics.finalFF.toFixed(2)),
          surfaceRumble: surfaceRumble,
          collision: collision,
          numberOfTyresOut: physics.numberOfTyresOut
        },
        graphic: {
          packetId: graphic.packetId,
          status: graphic.status, // 0: off, 1: replay, 2: live, 3: pause
          session: graphic.session, // 1: Prac, 2: Qual, 3: Race, 4: Hotlap...
          currentTime: decodeWcharArray(graphic.currentTime),
          lastTime: decodeWcharArray(graphic.lastTime),
          bestTime: decodeWcharArray(graphic.bestTime),
          split: decodeWcharArray(graphic.split),
          completedLaps: graphic.completedLaps,
          position: graphic.position,
          iCurrentTime: graphic.iCurrentTime,
          iLastTime: graphic.iLastTime,
          iBestTime: graphic.iBestTime,
          sessionTimeLeft: Math.max(0, Math.round(graphic.sessionTimeLeft)),
          distanceTraveled: Number(graphic.distanceTraveled.toFixed(1)),
          isInPit: graphic.isInPit > 0,
          isInPitLane: graphic.isInPitLane > 0,
          currentSectorIndex: graphic.currentSectorIndex,
          numberOfLaps: graphic.numberOfLaps,
          tyreCompound: decodeWcharArray(graphic.tyreCompound),
          surfaceGrip: Number((graphic.surfaceGrip * 100).toFixed(1)),
          flag: graphic.flag,
          penaltyTime: Number(graphic.penaltyTime.toFixed(1)),
          normalizedCarPosition: Number((graphic.normalizedCarPosition || 0).toFixed(4))
        },
        staticInfo: (() => {
          const carModelStr = decodeWcharArray(staticInfo.carModel);
          if (carModelStr !== this.currentCarModel) { this.currentCarModel = carModelStr; this.carHasTurbo = false; }
          if (staticInfo.maxTurboBoost > 0.05 || physics.turboBoost > 0.05) { this.carHasTurbo = true; }
          const trackStr = decodeWcharArray(staticInfo.track);
          const trackConfigStr = decodeWcharArray(staticInfo.trackConfiguration);
          if (carModelStr && trackStr) {
            ghostTracker.loadGhost(carModelStr, trackStr, trackConfigStr);
          }

          const carDetails = carInfoReader.getCarInfo(carModelStr, staticInfo.maxRpm);
          const trackDetails = carInfoReader.getTrackInfo(trackStr, trackConfigStr);

          const resolvedLimiter = carDetails.limiterRpm || staticInfo.maxRpm || 8500;
          const resolvedShift = carDetails.shiftRpm || Math.round(resolvedLimiter * 0.96);

          return {
            smVersion: decodeWcharArray(staticInfo.smVersion),
            acVersion: decodeWcharArray(staticInfo.acVersion),
            carModel: carModelStr,
            carName: carDetails.carName || carModelStr,
            brand: carDetails.brand || '',
            track: trackStr,
            trackName: trackDetails.trackName || trackStr,
            trackConfiguration: trackConfigStr,
            playerName: decodeWcharArray(staticInfo.playerName),
            sectorCount: staticInfo.sectorCount,
            maxRpm: resolvedLimiter,
            limiterRpm: resolvedLimiter,
            shiftRpm: resolvedShift,
            idleRpm: carDetails.idleRpm || 800,
            maxFuel: Math.round(staticInfo.maxFuel) || 100,
            hasDRS: staticInfo.hasDRS > 0,
            hasERS: carDetails.hasERS || staticInfo.hasERS > 0 || (physics.kersCharge > 0.5),
            hasKERS: staticInfo.hasKERS > 0,
            hasTurbo: carDetails.hasTurbo || this.carHasTurbo || (staticInfo.maxTurboBoost > 0.05) || (physics.turboBoost > 0.02),
            maxTurboBoost: staticInfo.maxTurboBoost > 0 ? Number(staticInfo.maxTurboBoost.toFixed(2)) : 2.5,
            isOnline: staticInfo.isOnline > 0
          };
        })()
      };
    } catch (e) {
      this.disconnect();
      return null;
    }
  }
}

module.exports = new ACReader();
