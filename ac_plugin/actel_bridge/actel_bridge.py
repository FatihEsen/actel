import platform
import os
import sys
import time

try:
    import ac
    import acsys
    IS_IN_AC = True
except ImportError:
    IS_IN_AC = False

# Unconditionally add stdlib/stdlib64 to sys.path before loading ctypes
curr_dir = os.path.dirname(__file__)
if platform.architecture()[0] == "64bit":
    sysdir = os.path.join(curr_dir, 'stdlib64')
else:
    sysdir = os.path.join(curr_dir, 'stdlib')

if os.path.exists(sysdir) and sysdir not in sys.path:
    sys.path.insert(0, sysdir)
os.environ['PATH'] = os.environ.get('PATH', '') + ";."

try:
    import ctypes
except Exception as e:
    with open(os.path.join(curr_dir, "actel_load_err.log"), "w", encoding="utf-8") as f:
        f.write("Failed to import ctypes: " + str(e) + "\n")
    raise

kernel32 = ctypes.windll.kernel32
user32 = ctypes.windll.user32

# Win32 Function prototypes with 64-bit pointers
kernel32.OpenFileMappingW.restype = ctypes.c_void_p
kernel32.OpenFileMappingW.argtypes = [ctypes.c_uint32, ctypes.c_bool, ctypes.c_wchar_p]
kernel32.MapViewOfFile.restype = ctypes.c_void_p
kernel32.MapViewOfFile.argtypes = [ctypes.c_void_p, ctypes.c_uint32, ctypes.c_uint32, ctypes.c_uint32, ctypes.c_size_t]
kernel32.UnmapViewOfFile.restype = ctypes.c_bool
kernel32.UnmapViewOfFile.argtypes = [ctypes.c_void_p]
kernel32.CloseHandle.restype = ctypes.c_bool
kernel32.CloseHandle.argtypes = [ctypes.c_void_p]

user32.MapVirtualKeyA.restype = ctypes.c_uint32
user32.MapVirtualKeyA.argtypes = [ctypes.c_uint32, ctypes.c_uint32]
user32.keybd_event.restype = None
user32.keybd_event.argtypes = [ctypes.c_ubyte, ctypes.c_ubyte, ctypes.c_uint32, ctypes.c_void_p]

FILE_MAP_READ = 0x0004
KEYEVENTF_KEYUP = 0x0002
KEYEVENTF_EXTENDEDKEY = 0x0001

VK_MAP = {
    'ESC': 0x1B, 'ESCAPE': 0x1B,
    'CTRL': 0x11, 'LCTRL': 0x11, 'RCTRL': 0x11,
    'SHIFT': 0x10, 'LSHIFT': 0x10, 'RSHIFT': 0x10,
    'ALT': 0x12, 'LALT': 0x12, 'RALT': 0x12,
    'SPACE': 0x20, 'ENTER': 0x0D, 'TAB': 0x09,
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
    'LEFT': 0x25, 'UP': 0x26, 'RIGHT': 0x27, 'DOWN': 0x28,
    'PAGEUP': 0x21, 'PAGEDOWN': 0x22
}

SCAN_MAP = {
    'ESC': 0x01, 'ESCAPE': 0x01,
    '1': 0x02, '2': 0x03, '3': 0x04, '4': 0x05, '5': 0x06, '6': 0x07, '7': 0x08, '8': 0x09, '9': 0x0A, '0': 0x0B,
    'NUMPAD0': 0x52, 'NUMPAD1': 0x4F, 'NUMPAD2': 0x50, 'NUMPAD3': 0x51, 'NUMPAD4': 0x4B,
    'NUMPAD5': 0x4C, 'NUMPAD6': 0x4D, 'NUMPAD7': 0x47, 'NUMPAD8': 0x48, 'NUMPAD9': 0x49,
    'Q': 0x10, 'W': 0x11, 'E': 0x12, 'R': 0x13, 'T': 0x14, 'Y': 0x15, 'U': 0x16, 'I': 0x17, 'O': 0x18, 'P': 0x19,
    'A': 0x1E, 'S': 0x1F, 'D': 0x20, 'F': 0x21, 'G': 0x22, 'H': 0x23, 'J': 0x24, 'K': 0x25, 'L': 0x26,
    'Z': 0x2C, 'X': 0x2D, 'C': 0x2E, 'V': 0x2F, 'B': 0x30, 'N': 0x31, 'M': 0x32,
    'SPACE': 0x39, 'ENTER': 0x1C, 'TAB': 0x0F,
    'CTRL': 0x1D, 'SHIFT': 0x2A, 'ALT': 0x38,
    'F1': 0x3B, 'F2': 0x3C, 'F3': 0x3D, 'F4': 0x3E, 'F5': 0x3F, 'F6': 0x40,
    'LEFT': 0x4B, 'UP': 0x48, 'RIGHT': 0x4D, 'DOWN': 0x50,
    'PAGEUP': 0x49, 'PAGEDOWN': 0x51
}

ACTION_KEYS = {
    'headlights': ['L'],
    'flasher': ['G'],
    'extra_a': ['ALT', 'NUMPAD7'],
    'extra_b': ['ALT', 'NUMPAD8'],
    'wipers': ['ALT', 'W'],
    'horn': ['H'],
    'turn_left': ['ALT', 'LEFT'],
    'turn_right': ['ALT', 'RIGHT'],
    'hazard': ['ALT', 'DOWN'],
    'tc_up': ['CTRL', 'T'],
    'tc_down': ['CTRL', 'SHIFT', 'T'],
    'abs_up': ['CTRL', 'A'],
    'abs_down': ['CTRL', 'SHIFT', 'A'],
    'bb_up': ['CTRL', 'B'],
    'bb_down': ['CTRL', 'SHIFT', 'B'],
    'turbo_up': ['CTRL', 'PAGEUP'],
    'turbo_down': ['CTRL', 'PAGEDOWN'],
    'eb_up': ['CTRL', 'E'],
    'eb_down': ['CTRL', 'SHIFT', 'E'],
    'drs': ['D'],
    'kers': ['K'],
    'pit_limiter': ['ALT', 'L'],
    'teleport_pits': ['CTRL', 'P'],
    'vr_recenter': ['CTRL', 'SPACE'],
    'restart_session': ['CTRL', 'O'],
    'pause': ['ESC'],
    'camera_change': ['F1'],
    'starter': ['CTRL', 'S'],
    'engine_start': ['CTRL', 'S'],
    'look_left': ['Q'],
    'look_right': ['E'],
    'look_back': ['W']
}

hMap = None
pBuf = None
last_packet_id = 0
command_count = 0
active_releases = []

log_file_path = os.path.join(curr_dir, 'actel_debug.log')

def log_debug(msg):
    full_msg = "[{0}] {1}".format(time.strftime("%H:%M:%S"), msg)
    if IS_IN_AC:
        try:
            ac.log("ACTel: " + msg)
        except Exception:
            pass
    try:
        with open(log_file_path, "a", encoding="utf-8") as f:
            f.write(full_msg + "\n")
    except Exception:
        pass

def init_shared_memory():
    global hMap, pBuf
    if pBuf:
        return True
    try:
        hMap = kernel32.OpenFileMappingW(FILE_MAP_READ, False, "Local\\actel_cmd")
        if not hMap:
            hMap = kernel32.OpenFileMappingW(FILE_MAP_READ, False, "actel_cmd")
        if hMap:
            pBuf = kernel32.MapViewOfFile(hMap, FILE_MAP_READ, 0, 0, 1024)
            if pBuf:
                log_debug("Shared Memory Command Buffer Connected!")
                return True
    except Exception as e:
        log_debug("Error connecting to shared memory: " + str(e))
    return False

def send_key_down(key_name):
    k = key_name.upper()
    vk = VK_MAP.get(k, 0)
    scan = SCAN_MAP.get(k, user32.MapVirtualKeyA(vk, 0) if vk else 0)
    flags = 0
    if k in ['LEFT', 'UP', 'RIGHT', 'DOWN']:
        flags |= KEYEVENTF_EXTENDEDKEY
    user32.keybd_event(vk, scan, flags, 0)

def send_key_up(key_name):
    k = key_name.upper()
    vk = VK_MAP.get(k, 0)
    scan = SCAN_MAP.get(k, user32.MapVirtualKeyA(vk, 0) if vk else 0)
    flags = KEYEVENTF_KEYUP
    if k in ['LEFT', 'UP', 'RIGHT', 'DOWN']:
        flags |= KEYEVENTF_EXTENDEDKEY
    user32.keybd_event(vk, scan, flags, 0)

def trigger_keys_frame(keys, hold_ms = 100):
    global active_releases
    for k in keys:
        send_key_down(k)
    now_ms = time.time() * 1000
    active_releases.append((now_ms + hold_ms, keys))

app_window = 0
status_lbl = 0
last_recon_try = 0

def acMain(ac_version):
    global app_window, status_lbl
    log_debug("acMain called, initializing ACTel Bridge...")
    try:
        app_window = ac.newApp("ACTel Bridge")
        ac.setSize(app_window, 240, 60)
        ac.setTitle(app_window, "ACTel Bridge")
        status_lbl = ac.addLabel(app_window, "ACTel: Searching...")
        ac.setPosition(status_lbl, 10, 24)
        init_shared_memory()
        if pBuf:
            ac.setText(status_lbl, "ACTel: READY (Waiting)")
    except Exception as e:
        log_debug("acMain init error: " + str(e))

    return "ACTel Bridge"

def acUpdate(deltaT):
    global hMap, pBuf, last_packet_id, command_count, active_releases, status_lbl, last_recon_try

    now = time.time()

    # Reconnect if not connected
    if not pBuf:
        if now - last_recon_try > 2.0:
            last_recon_try = now
            if init_shared_memory():
                if status_lbl:
                    ac.setText(status_lbl, "ACTel: READY (Connected)")
        return

    # Check incoming command
    try:
        packet_id = ctypes.c_uint32.from_address(pBuf).value
        if packet_id > 0 and packet_id != last_packet_id:
            last_packet_id = packet_id
            command_count += 1
            cmd_raw = (ctypes.c_char * 64).from_address(pBuf + 4).value
            cmd = cmd_raw.decode('utf-8', 'ignore').strip()

            log_debug("Received Command: " + cmd + " (#" + str(command_count) + ")")
            if status_lbl:
                ac.setText(status_lbl, "ACTel: " + cmd + " (#" + str(command_count) + ")")

            keys = None
            if cmd in ACTION_KEYS:
                keys = ACTION_KEYS[cmd]
            elif cmd.startswith('combo:'):
                keys = cmd[6:].split('+')
            elif cmd.startswith('key:'):
                keys = [cmd[4:]]

            if keys:
                duration = 100
                if cmd in ['look_left', 'look_right', 'look_back']:
                    duration = 1200
                elif cmd == 'horn':
                    duration = 350
                elif cmd in ['starter', 'engine_start']:
                    duration = 900
                trigger_keys_frame(keys, duration)
    except Exception as e:
        log_debug("acUpdate command reading error: " + str(e))

    # Delayed key releases
    if active_releases:
        now_ms = now * 1000
        remaining = []
        for rel_time, keys in active_releases:
            if now_ms >= rel_time:
                for k in reversed(keys):
                    send_key_up(k)
            else:
                remaining.append((rel_time, keys))
        active_releases = remaining

def acShutdown():
    global hMap, pBuf
    log_debug("acShutdown called")
    if pBuf:
        kernel32.UnmapViewOfFile(pBuf)
        pBuf = None
    if hMap:
        kernel32.CloseHandle(hMap)
        hMap = None
