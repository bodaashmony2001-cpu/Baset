package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = DarkGreenPrimary,
    secondary = DarkGreenSecondary,
    tertiary = DarkGreenAccent,
    background = DarkGreenBackground,
    surface = DarkGreenSurface,
    onPrimary = Color(0xFF030A07),
    onSecondary = Color(0xFF030A07),
    onBackground = DarkGreenText,
    onSurface = DarkGreenText
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MansouriaGreenDark,
    secondary = MansouriaGreenLight,
    tertiary = MansouriaGoldAccent,
    background = MansouriaSandBackground,
    surface = MansouriaCreamCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = MansouriaTextPrimary,
    onSurface = MansouriaTextPrimary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is checked
  dynamicColor: Boolean = false, // Set to false to force our beautiful custom Egyptian sunset oasis branding!
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
