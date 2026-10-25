package com.actel.dashboard

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.actel.dashboard.data.model.ConnectionState
import com.actel.dashboard.ui.screens.ButtonBoxScreen
import com.actel.dashboard.ui.screens.DashboardScreen
import com.actel.dashboard.ui.screens.SettingsScreen
import com.actel.dashboard.ui.screens.TyresScreen
import com.actel.dashboard.ui.theme.ActelTheme
import com.actel.dashboard.ui.theme.BgCard
import com.actel.dashboard.ui.theme.BgDark
import com.actel.dashboard.ui.theme.BorderDark
import com.actel.dashboard.ui.theme.NeonCyan
import com.actel.dashboard.ui.theme.NeonGreen
import com.actel.dashboard.ui.theme.NeonRed
import com.actel.dashboard.ui.theme.NeonYellow
import com.actel.dashboard.ui.theme.TextDim
import com.actel.dashboard.ui.theme.TextPrimary
import com.actel.dashboard.ui.theme.TextSecondary
import com.actel.dashboard.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen awake during races
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ActelTheme {
                MainApp(
                    viewModel = viewModel,
                    onHapticFeedback = { duration, intensity -> performHaptic(duration, intensity) },
                    onStopHaptic = { stopHaptic() }
                )
            }
        }

        // Enable immersive full screen after content is mounted
        hideSystemUI()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        try {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } catch (_: Exception) {
            try {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
            } catch (_: Exception) {}
        }
    }

    private fun performHaptic(durationMs: Long = 40, intensity: Float = 1.0f) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator?.let { v ->
            if (v.hasVibrator()) {
                val clampedIntensity = intensity.coerceIn(0.1f, 1.0f)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val amp = (clampedIntensity * 255).toInt().coerceIn(1, 255)
                    try {
                        v.vibrate(VibrationEffect.createOneShot(durationMs, amp))
                    } catch (_: Exception) {
                        v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(durationMs)
                }
            }
        }
    }

    private fun stopHaptic() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
    }
}

enum class NavTab {
    DASHBOARD,
    BUTTON_BOX,
    SETTINGS
}

@Composable
fun MainApp(
    viewModel: MainViewModel,
    onHapticFeedback: (Long, Float) -> Unit,
    onStopHaptic: () -> Unit
) {
    var currentTab by remember { mutableStateOf(NavTab.DASHBOARD) }

    val telemetry by viewModel.telemetryState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val discoveredHost by viewModel.discoveredHost.collectAsState()
    val discoveredHostname by viewModel.discoveredHostname.collectAsState()
    val currentHost by viewModel.currentHost.collectAsState()
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()
    val hapticIntensity by viewModel.hapticIntensity.collectAsState()
    val surfaceHapticEnabled by viewModel.surfaceHapticEnabled.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()

    // Trigger car-specific shift up / redline continuous haptic vibration
    val currentRpm = telemetry?.physics?.rpms ?: 0
    val currentGear = telemetry?.physics?.gear ?: 1
    val staticInfo = telemetry?.staticInfo
    val limiterRpm = if (staticInfo != null && staticInfo.limiterRpm > 0) {
        staticInfo.limiterRpm
    } else {
        staticInfo?.maxRpm ?: 8500
    }

    // Son %5'lik devir dilimi: örneğin 8500 devirli araç için 8075 devir ve üzeri
    val redlineThreshold = (limiterRpm * 0.95f).toInt()
    val isRedline = currentGear >= 2 && currentRpm >= redlineThreshold

    // Kesintisiz Devir Kesici Titreşimi: Devir son %5'te kaldığı sürece motor kesici hissiyle titrer
    LaunchedEffect(isRedline, hapticEnabled, hapticIntensity) {
        if (isRedline && hapticEnabled) {
            try {
                while (true) {
                    onHapticFeedback(45L, hapticIntensity)
                    kotlinx.coroutines.delay(40L)
                }
            } finally {
                onStopHaptic()
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    // Çarpışmada titreşim uyarısı
    val isCollision = telemetry?.physics?.collision == true
    LaunchedEffect(isCollision) {
        if (isCollision && hapticEnabled) {
            onHapticFeedback(260L, 1.0f)
        }
    }

    // Bordür, kasis ve bozuk zemin (çimen/çakıl) titreşimi
    val surfaceRumble = telemetry?.physics?.surfaceRumble ?: 0f
    val speedKmh = telemetry?.physics?.speedKmh ?: 0
    var lastRumbleTime by remember { mutableStateOf(0L) }

    LaunchedEffect(surfaceRumble, speedKmh, isRedline, surfaceHapticEnabled, hapticEnabled) {
        if (surfaceHapticEnabled && hapticEnabled && !isRedline && speedKmh > 15 && surfaceRumble > 0.2f) {
            val now = System.currentTimeMillis()
            if (now - lastRumbleTime > 50L) {
                lastRumbleTime = now
                val rumbleDuration = (18L + (surfaceRumble * 18L)).toLong()
                val rumbleAmp = (surfaceRumble * hapticIntensity).coerceIn(0.2f, 1.0f)
                onHapticFeedback(rumbleDuration, rumbleAmp)
            }
        }
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                telemetry = telemetry,
                connectionState = connectionState,
                onTabSelect = { tab ->
                    if (hapticEnabled) onHapticFeedback(25L, hapticIntensity)
                    currentTab = tab
                },
                currentTab = currentTab
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavTab.DASHBOARD -> DashboardScreen(telemetry = telemetry)
                NavTab.BUTTON_BOX -> ButtonBoxScreen(
                    telemetry = telemetry,
                    onAction = { action, state ->
                        if (hapticEnabled && state != "up") onHapticFeedback(45L, hapticIntensity)
                        viewModel.triggerAction(action, state)

                        // Start veya yeniden başlat tuşuna basıldıktan 1sn sonra gösterge sayfası açılsın
                        if ((action == "starter" || action == "engine_start" || action == "restart_session") && state != "up") {
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(1000L)
                                currentTab = NavTab.DASHBOARD
                            }
                        }
                    }
                )
                NavTab.SETTINGS -> SettingsScreen(
                    connectionState = connectionState,
                    currentHost = currentHost,
                    discoveredHost = discoveredHost,
                    discoveredHostname = discoveredHostname,
                    hapticEnabled = hapticEnabled,
                    hapticIntensity = hapticIntensity,
                    surfaceHapticEnabled = surfaceHapticEnabled,
                    currentMode = currentMode,
                    onConnect = { host, port ->
                        if (hapticEnabled) onHapticFeedback(30L, hapticIntensity)
                        viewModel.connect(host, port)
                    },
                    onDisconnect = {
                        if (hapticEnabled) onHapticFeedback(30L, hapticIntensity)
                        viewModel.disconnect()
                    },
                    onModeChange = { mode ->
                        if (hapticEnabled) onHapticFeedback(25L, hapticIntensity)
                        viewModel.setMode(mode)
                    },
                    onToggleHaptic = { viewModel.toggleHaptic(it) },
                    onHapticIntensityChange = { viewModel.setHapticIntensity(it) },
                    onToggleSurfaceHaptic = { viewModel.toggleSurfaceHaptic(it) },
                    onPreviewHaptic = { onHapticFeedback(50L, hapticIntensity) }
                )
            }
        }
    }
}

@Composable
fun TopAppBar(
    telemetry: com.actel.dashboard.data.model.TelemetryData?,
    connectionState: ConnectionState,
    currentTab: NavTab,
    onTabSelect: (NavTab) -> Unit
) {
    val staticInfo = telemetry?.staticInfo

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(BgCard)
            .border(0.5.dp, BorderDark)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand & Connection Badge & Quick Telemetry
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🏎️ ACTEL",
                color = NeonCyan,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            ConnectionBadge(connectionState = connectionState)

            val speed = telemetry?.physics?.speedKmh ?: 0
            val rpm = telemetry?.physics?.rpms ?: 0
            val gear = telemetry?.physics?.gearStr ?: "N"

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "|",
                    color = TextDim,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "$speed KM/H",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "|",
                    color = TextDim,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "$rpm RPM",
                    color = NeonYellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "|",
                    color = TextDim,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = gear,
                    color = if (gear == "R") NeonRed else if (gear == "N") NeonYellow else NeonGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Navigation Tabs
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTabButton("🏁 GÖSTERGE", currentTab == NavTab.DASHBOARD) { onTabSelect(NavTab.DASHBOARD) }
            NavTabButton("🎛️ BUTTON BOX", currentTab == NavTab.BUTTON_BOX) { onTabSelect(NavTab.BUTTON_BOX) }
            NavTabButton("⚙️", currentTab == NavTab.SETTINGS) { onTabSelect(NavTab.SETTINGS) }
        }
    }
}

@Composable
fun ConnectionBadge(connectionState: ConnectionState) {
    val (color, text) = when (connectionState) {
        ConnectionState.CONNECTED -> NeonGreen to "CANLI (50Hz)"
        ConnectionState.CONNECTING -> NeonYellow to "BAĞLANILIYOR..."
        ConnectionState.SEARCHING -> NeonCyan to "ARANIYOR..."
        ConnectionState.DISCONNECTED -> NeonRed to "BAĞLANTI YOK"
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun NavTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color.Transparent
    val borderColor = if (isSelected) NeonCyan else Color.Transparent
    val textColor = if (isSelected) NeonCyan else TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun InfoChip(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label: ", color = TextDim, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(
            text = value.take(22),
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
