-- ACTel Sync: Realtime electrical, headlight & indicator synchronization for Assetto Corsa & CSP
local ffi = nil
pcall(function() ffi = require('ffi') end)

if ffi then
  pcall(function()
    ffi.cdef[[
      void* OpenFileMappingA(unsigned int dwDesiredAccess, int bInheritHandle, const char* lpName);
      void* MapViewOfFile(void* hFileMappingObject, unsigned int dwDesiredAccess, unsigned int dwFileOffsetHigh, unsigned int dwFileOffsetLow, size_t dwNumberOfBytesToMap);
      int UnmapViewOfFile(void* lpBaseAddress);
      int CloseHandle(void* hObject);
    ]]
  end)
end

local kernel32 = ffi and ffi.C or nil
local hMap = nil
local pBuf = nil
local FILE_MAP_ALL_ACCESS = 0xF001F

local function initSharedMem()
  if pBuf ~= nil or not kernel32 then return true end
  pcall(function()
    hMap = kernel32.OpenFileMappingA(FILE_MAP_ALL_ACCESS, 0, "Local\\actel_cmd")
    if hMap == nil then
      hMap = kernel32.OpenFileMappingA(FILE_MAP_ALL_ACCESS, 0, "actel_cmd")
    end
    if hMap ~= nil then
      pBuf = kernel32.MapViewOfFile(hMap, FILE_MAP_ALL_ACCESS, 0, 0, 1024)
    end
  end)
  return pBuf ~= nil
end

local ws = nil
local lastWsTry = 0
local lastH = nil
local lastHaz = nil
local lastTL = nil
local lastTR = nil

local function getWs()
  if ws ~= nil then return ws end
  local now = os.clock()
  if now - lastWsTry < 3.0 then return nil end
  lastWsTry = now
  if web and web.socket then
    pcall(function()
      ws = web.socket('ws://127.0.0.1:8080/ws', function(data) end, { reconnect = true })
    end)
  end
  return ws
end

function script.update(dt)
  local car = ac.getCar(0)
  if not car then return end

  -- 1. Shared Memory Direct Fast Path (Offset 128..140)
  if not pBuf and kernel32 then
    initSharedMem()
  end

  if pBuf ~= nil and ffi then
    pcall(function()
      local ptr = ffi.cast("uint8_t*", pBuf)
      ptr[128] = car.headlightsActive and 1 or 0
      ptr[129] = car.hazardLights and 1 or 0
      ptr[130] = car.turningLeftLights and 1 or 0
      ptr[131] = car.turningRightLights and 1 or 0
      ptr[132] = car.turningLightsActivePhase and 1 or 0
      ptr[133] = car.highBeams and 1 or 0
      ptr[134] = car.lowBeams and 1 or 0
      ptr[135] = car.flashingLightsActive and 1 or 0

      local hbPtr = ffi.cast("uint32_t*", ptr + 136)
      hbPtr[0] = hbPtr[0] + 1
    end)
  end

  -- 2. WebSocket Direct Event Path on State Change
  local h = car.headlightsActive
  local haz = car.hazardLights
  local tl = car.turningLeftLights
  local tr = car.turningRightLights

  if h ~= lastH or haz ~= lastHaz or tl ~= lastTL or tr ~= lastTR then
    lastH = h
    lastHaz = haz
    lastTL = tl
    lastTR = tr

    local client = getWs()
    if client then
      pcall(function()
        local msg = string.format('{"type":"csp_indicators","headlights":%s,"hazard":%s,"turnLeft":%s,"turnRight":%s}',
          tostring(h), tostring(haz), tostring(tl), tostring(tr))
        client(msg)
      end)
    end
  end
end
