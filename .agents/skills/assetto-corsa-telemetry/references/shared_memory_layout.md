# Assetto Corsa Shared Memory C Struct Reference

This document provides ctypes struct definitions and byte offsets for Assetto Corsa's 3 memory-mapped files.

## 1. Physics (`acpmf_physics`)

```c
struct SPageFilePhysics {
    int packetId;
    float gas;
    float brake;
    float fuel;
    int gear;
    int rpms;
    float steerAngle;
    float speedKmh;
    float velocity[3];
    float accG[3];
    float wheelSlip[4];
    float wheelLoad[4];
    float wheelsPressure[4];       // PSI
    float wheelAngularSpeed[4];
    float tyreWear[4];             // Percentage: 0.0 - 100.0 (DO NOT multiply by 100)
    float tyreDirtyLevel[4];
    float tyreCoreTemperature[4];  // Carcass core temp in Celsius
    float camberRAD[4];
    float suspensionTravel[4];
    float drs;
    float tc;
    float heading;
    float pitch;
    float roll;
    float cgHeight;
    float carDamage[5];
    int numberOfTyresOut;
    int pitLimiterOn;
    float abs;
    float kersCharge;              // 0.0 - 1.0 (Multiply by 100 for %)
    float kersInput;               // 0.0 - 1.0 (Deployment)
    int autoShifterOn;
    float rideHeight[2];
    float turboBoost;              // Bar
    float ballast;
    float airDensity;
    float airTemp;
    float roadTemp;
    float localAngularVel[3];
    float finalFF;
    float performanceMeter;
    int engineBrake;
    int ersRecoveryLevel;
    int ersPowerLevel;
    int ersHeatCharging;
    int ersIsCharging;
    float kersCurrentKJ;
    int drsAvailable;              // 0 or 1
    int drsEnabled;                // 0 or 1
    float brakeTemp[4];            // Celsius
    float clutch;                  // 0.0 - 1.0
    float tyreTempI[4];            // Inner tread temp
    float tyreTempM[4];            // Middle tread temp
    float tyreTempO[4];            // Outer tread temp
    int isAIControlled;
    float tyreContactPoint[4][3];
    float tyreContactNormal[4][3];
    float tyreContactHeading[4][3];
    float brakeBias;               // Front bias percentage (e.g. 0.69 -> 69.0%)
    float localVelocity[3];
};
```

## 2. Graphics (`acpmf_graphics`)

```c
struct SPageFileGraphic {
    int packetId;
    int status;                    // 0: OFF, 1: REPLAY, 2: LIVE, 3: PAUSE
    int session;                   // 0: PRACTICE, 1: QUALIFY, 2: RACE, 3: HOTLAP, 4: TIME_ATTACK, 5: DRIFT, 6: DRAG
    wchar_t currentTime[15];       // Formatted current lap time
    wchar_t lastTime[15];          // Formatted last lap time
    wchar_t bestTime[15];          // Formatted best lap time
    wchar_t split[15];
    int completedLaps;
    int position;
    int iCurrentTime;              // Milliseconds
    int iLastTime;                 // Milliseconds
    int iBestTime;                 // Milliseconds
    float sessionTimeLeft;
    float distanceTraveled;
    int isInPit;                   // In pit box
    int currentSectorIndex;
    int lastSectorTime;
    int numberOfLaps;
    wchar_t tyreCompound[33];
    float replayTimeMultiplier;
    float normalizedCarPosition;   // 0.0 to 1.0 along track spline
    int activeCars;
    float carCoordinates[60][3];
    int carID[60];
    int playerCarID;
    float penaltyTime;
    int flag;
    int penalty;
    int idealLineRPM;
    int isInPitLane;               // Inside pit lane speed limit area
    float surfaceGrip;
    int mandatoryPitDone;
    float windSpeed;
    float windDirection;
    int isSetupMenuVisible;
    int mainDisplayIndex;
    int secondaryDisplayIndex;
    int TC;
    int TCCut;
    int EngineMap;
    int ABS;
    int fuelXLap;
    int rainLights;
    int flasherLights;
    int lightsStage;               // 0: OFF, 1: LOW, 2: HIGH
    float exhaustTemperature;
    int wiperLV;
    int DriverStintTotalTimeLeft;
    int DriverStintTimeLeft;
    int rainTyres;
};
```

## 3. Static (`acpmf_static`)

```c
struct SPageFileStatic {
    wchar_t smVersion[15];
    wchar_t acVersion[15];
    int numberOfSessions;
    int numCars;
    wchar_t carModel[33];
    wchar_t track[33];
    wchar_t playerName[33];
    wchar_t playerSurname[33];
    wchar_t playerNick[33];
    int sectorCount;
    float maxTorque;
    float maxPower;
    int maxRpm;
    float maxFuel;
    float suspensionMaxTravel[4];
    float tyreRadius[4];
    float maxTurboBoost;
    float deprecated_1;
    float deprecated_2;
    int penaltiesEnabled;
    float aidFuelRate;
    float aidTireRate;
    float aidMechanicalDamage;
    int aidAllowTyreBlankets;
    float aidStability;
    int aidAutoClutch;
    int aidAutoBlip;
    int hasDRS;                    // Car equipped with DRS
    int hasERS;                    // Car equipped with ERS
    int hasKERS;                   // Car equipped with KERS
    float kersMaxJ;
    int engineBrakeSettingsCount;
    int ersPowerControllerCount;
    float trackSPlineLength;
    wchar_t trackConfiguration[33];
    float ersMaxJ;
    int isTimedRace;
    int hasExtraA;
    int hasExtraB;
    int isOnline;
};
```

