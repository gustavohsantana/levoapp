package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = LevoBluePrimaryDark,
    onPrimary = Slate950,
    secondary = LevoAmberSecondary,
    tertiary = LevoEmeraldTertiary,
    background = Color(0xFF090D16),
    surface = Slate900,
    surfaceVariant = Slate800,
    surfaceContainer = Slate900,
    surfaceContainerHigh = Slate800,
    outline = SurfaceBorderDark,
    outlineVariant = Slate700,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LevoBluePrimary,
    onPrimary = Color.White,
    secondary = LevoAmberSecondary,
    tertiary = LevoEmeraldTertiary,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFF1F5F9),
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFCBD5E1),
    onBackground = Slate900,
    onSurface = Slate900,
    onSurfaceVariant = Slate500,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamicColor by default to guarantee crisp POS restaurant theme contrast
  dynamicColor: Boolean = false,
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
