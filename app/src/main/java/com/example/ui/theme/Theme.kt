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
    primary = TerracottaLight,
    onPrimary = TerracottaDark,
    primaryContainer = TerracottaDark,
    onPrimaryContainer = TerracottaLight,
    secondary = SageContainer,
    onSecondary = OnSageContainer,
    background = Color(0xFF191614),
    surface = Color(0xFF221E1C),
    onBackground = Color(0xFFEDE5E0),
    onSurface = Color(0xFFEDE5E0)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = OnTerracottaContainer,
    secondary = SageSecondary,
    onSecondary = Color.White,
    secondaryContainer = SageContainer,
    onSecondaryContainer = OnSageContainer,
    tertiary = WarmHoney,
    onTertiary = Color.White,
    tertiaryContainer = WarmHoneyContainer,
    background = LinenBackground,
    onBackground = EspressoTextPrimary,
    surface = SurfacePure,
    onSurface = EspressoTextPrimary,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = EspressoTextSecondary,
    outline = CardBorder
  )

@Composable
fun LookIATheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
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

