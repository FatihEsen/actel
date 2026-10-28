// Realistic Mock Telemetry Generator for Assetto Corsa Dashboard & Testing

class MockTelemetry {
  constructor() {
    this.time = 0;
    this.gear = 3;
    this.rpm = 4500;
    this.speed = 120;
    this.maxRpm = 8500;
    this.fuel = 45.8;
    this.lap = 3;
    this.lapTimeMs = 45200; // ms in current lap
    this.bestLapMs = 107350; // 1:47.350
    this.lastLapMs = 108120; // 1:48.120
    this.sector = 1;
    this.delta = -0.32; // -0.32s ahead of best lap
    
    // Tyre status (FL, FR, RL, RR)
    this.tyreTemps = [82, 85, 80, 83];
    this.tyrePressures = [27.4, 27.8, 26.9, 27.2];
    this.tyreWear = [96.4, 95.8, 97.1, 96.9];
    this.brakeTemps = [410, 435, 380, 395];

    // Car settings
    this.tc = 4;
    this.abs = 3;
    this.brakeBias = 0.58;
    this.drs = false;
    this.pitLimiter = false;
    this.turboBoost = 1.45;
  }

  update(dt = 0.016) {
    this.time += dt;
    this.lapTimeMs += Math.round(dt * 1000);

    // Track cycle (simulate a 75-second lap sequence)
    const cycle = (this.time % 75);

    let gas = 0;
    let brake = 0;
    let steer = 0;
    let latG = 0;
    let lonG = 0;

    // Simulate different track phases (Straights, heavy braking, corners, chicanes)
    if (cycle < 18) {
      // Main Straight: Full throttle, gear upshifts
      gas = 1.0;
      brake = 0.0;
      steer = Math.sin(this.time * 2) * 0.02;
      
      const speedProgress = cycle / 18;
      this.speed = 140 + speedProgress * 135; // 140 -> 275 km/h
      
      // Calculate gear based on speed
      if (this.speed < 170) { this.gear = 3; this.rpm = 5500 + ((this.speed - 140) / 30) * 2800; }
      else if (this.speed < 215) { this.gear = 4; this.rpm = 5600 + ((this.speed - 170) / 45) * 2700; }
      else if (this.speed < 255) { this.gear = 5; this.rpm = 5800 + ((this.speed - 215) / 40) * 2500; }
      else { this.gear = 6; this.rpm = 6000 + ((this.speed - 255) / 35) * 2300; }

      this.drs = cycle > 5 && cycle < 16;
      lonG = 0.65;
      latG = steer * 4;
    } else if (cycle < 23) {
      // Heavy Braking into Chicane / Turn 1
      const brakeProgress = (cycle - 18) / 5;
      gas = 0.0;
      brake = Math.max(0.2, 1.0 - brakeProgress * 0.7);
      this.speed = Math.max(70, 275 - brakeProgress * 205);
      this.gear = brakeProgress > 0.6 ? 2 : (brakeProgress > 0.3 ? 3 : 4);
      this.rpm = 4000 + (1 - brakeProgress) * 3500;
      this.drs = false;
      lonG = -2.4 * brake;
      latG = 0.1;
      
      // Brakes heat up
      this.brakeTemps[0] = Math.min(680, this.brakeTemps[0] + 4);
      this.brakeTemps[1] = Math.min(690, this.brakeTemps[1] + 4);
    } else if (cycle < 36) {
      // Cornering / Apex / Exit
      const cornerProgress = (cycle - 23) / 13;
      steer = Math.sin(cornerProgress * Math.PI) * 0.45;
      latG = steer * 4.2;
      
      if (cornerProgress < 0.4) {
        gas = 0.2 + cornerProgress * 0.8;
        brake = 0.0;
        this.speed = 70 + cornerProgress * 40;
        this.gear = 2;
        this.rpm = 4800 + cornerProgress * 3200;
      } else {
        gas = 0.95;
        this.speed = 110 + (cornerProgress - 0.4) * 90;
        this.gear = this.speed > 160 ? 4 : 3;
        this.rpm = 5200 + ((this.speed % 50) / 50) * 2800;
      }
      lonG = gas * 0.7;
    } else if (cycle < 52) {
      // Medium High Speed Sweeper
      const sweeperProg = (cycle - 36) / 16;
      steer = -Math.sin(sweeperProg * Math.PI) * 0.28;
      gas = 0.85;
      this.speed = 175 + Math.sin(sweeperProg * Math.PI) * 35;
      this.gear = 4;
      this.rpm = 6200 + Math.sin(sweeperProg * Math.PI) * 1500;
      latG = steer * 4.8;
      lonG = 0.3;
    } else {
      // Final sector back to straight
      gas = 1.0;
      this.speed = 210 + ((cycle - 52) / 23) * 55;
      this.gear = 5;
      this.rpm = 6100 + ((cycle - 52) / 23) * 2100;
      steer = 0.0;
      latG = 0.05;
      lonG = 0.5;
    }

    // Fuel consumption
    this.fuel = Math.max(0.5, this.fuel - (dt * 0.035 * gas));

    // Sector calculation
    if (cycle < 25) this.sector = 1;
    else if (cycle < 55) this.sector = 2;
    else this.sector = 3;

    // Delta fluctuation
    this.delta = Number((-0.45 + Math.sin(this.time * 0.4) * 0.25).toFixed(3));

    // Cool down brakes when not braking
    if (brake === 0) {
      for (let i = 0; i < 4; i++) {
        this.brakeTemps[i] = Math.max(350, this.brakeTemps[i] - 0.4);
      }
    }

    // Tyre temperatures dynamic shift
    this.tyreTemps[0] = Math.round(82 + Math.abs(latG) * 3.5 + Math.abs(lonG) * 2);
    this.tyreTemps[1] = Math.round(84 + Math.abs(latG) * 3.8 + Math.abs(lonG) * 2);
    this.tyreTemps[2] = Math.round(80 + Math.abs(lonG) * 3);
    this.tyreTemps[3] = Math.round(82 + Math.abs(lonG) * 3.2);

    const formatMs = (ms) => {
      const min = Math.floor(ms / 60000);
      const sec = Math.floor((ms % 60000) / 1000);
      const millis = ms % 1000;
      return `${min}:${sec.toString().padStart(2, '0')}.${millis.toString().padStart(3, '0').slice(0, 3)}`;
    };

    return {
      connected: true,
      source: 'MOCK_SIMULATOR',
      timestamp: Date.now(),
      physics: {
        packetId: Math.floor(this.time * 60),
        gas: Number(gas.toFixed(2)),
        brake: Number(brake.toFixed(2)),
        clutch: 0,
        fuel: Number(this.fuel.toFixed(2)),
        gear: this.gear + 1,
        gearStr: this.gear.toString(),
        rpms: Math.round(Math.min(this.maxRpm, Math.max(1000, this.rpm))),
        steerAngle: Number((steer * 450).toFixed(1)),
        speedKmh: Math.round(this.speed),
        accG: {
          lateral: Number(latG.toFixed(2)),
          longitudinal: Number(lonG.toFixed(2)),
          vertical: 0.98
        },
        tyres: {
          pressure: this.tyrePressures,
          tempCore: this.tyreTemps,
          tempI: this.tyreTemps.map(t => t + 3),
          tempM: this.tyreTemps.map(t => t),
          tempO: this.tyreTemps.map(t => t - 2),
          wear: this.tyreWear,
          brakeTemp: this.brakeTemps.map(Math.round)
        },
        drs: this.drs,
        drsAvailable: cycle < 18,
        drsEnabled: this.drs,
        tc: this.tc,
        tcActive: gas > 0.88 && this.gear <= 2,
        abs: this.abs,
        absActive: brake > 0.75,
        performanceMeter: Number(this.delta.toFixed(3)),
        pitLimiterOn: this.pitLimiter,
        turboBoost: this.turboBoost,
        airTemp: 24,
        roadTemp: 32,
        brakeBias: this.brakeBias,
        engineBrake: 2,
        kersCharge: Number((Math.max(45, Math.min(98, 92 - (cycle < 18 ? cycle * 2.5 : 0) + (brake > 0 ? (cycle - 18) * 8 : 0)))).toFixed(1)),
        finalFF: 0.25,
        surfaceRumble: (cycle >= 20.5 && cycle <= 22.0) || (cycle >= 46.0 && cycle <= 47.5) ? 0.75 : 0.0,
        collision: cycle >= 18.05 && cycle <= 18.20,
        numberOfTyresOut: 0
      },
      gapAhead: '+' + (Math.abs(this.delta) + 1.24).toFixed(2) + 's',
      gapBehind: '-' + (Math.abs(this.delta) + 0.85).toFixed(2) + 's',
      totalCars: 16,
      graphic: {
        packetId: Math.floor(this.time * 60),
        status: 2, // Live
        session: 3, // Race
        currentTime: formatMs(this.lapTimeMs % 108000),
        lastTime: formatMs(this.lastLapMs),
        bestTime: formatMs(this.bestLapMs),
        split: (this.delta > 0 ? '+' : '') + this.delta.toFixed(3),
        completedLaps: this.lap,
        position: 2,
        iCurrentTime: this.lapTimeMs % 108000,
        iLastTime: this.lastLapMs,
        iBestTime: this.bestLapMs,
        sessionTimeLeft: 940,
        distanceTraveled: Number((this.time * 0.05).toFixed(1)),
        isInPit: false,
        isInPitLane: false,
        currentSectorIndex: this.sector,
        numberOfLaps: 15,
        tyreCompound: 'Soft Slick (DHE)',
        surfaceGrip: 99.2,
        flag: 0,
        penaltyTime: 0
      },
      staticInfo: {
        smVersion: '1.7',
        acVersion: '1.16.4',
        carModel: 'Ferrari 488 GT3 Evo',
        carName: 'Ferrari 488 GT3 Evo',
        brand: 'Ferrari',
        track: 'Autodromo Nazionale Monza',
        trackName: 'Autodromo Nazionale Monza',
        trackConfiguration: 'Grand Prix',
        playerName: 'Sim Racer',
        sectorCount: 3,
        maxRpm: this.maxRpm,
        limiterRpm: this.maxRpm,
        shiftRpm: 8100,
        idleRpm: 850,
        maxFuel: 110,
        hasDRS: true,
        hasERS: true,
        hasKERS: false,
        isOnline: false
      }
    };
  }
}

module.exports = new MockTelemetry();
