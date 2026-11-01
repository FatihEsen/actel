package com.actel.dashboard.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.actel.dashboard.R

// Map all font weights directly to the vector typeface to avoid synthetic stroke distortion
val DigitalNumbersFontFamily = FontFamily(
    Font(R.font.digital_numbers, FontWeight.Normal),
    Font(R.font.digital_numbers, FontWeight.Medium),
    Font(R.font.digital_numbers, FontWeight.SemiBold),
    Font(R.font.digital_numbers, FontWeight.Bold),
    Font(R.font.digital_numbers, FontWeight.ExtraBold),
    Font(R.font.digital_numbers, FontWeight.Black)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Black,
        fontSize = 56.sp,
        letterSpacing = (-1).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    )
)
