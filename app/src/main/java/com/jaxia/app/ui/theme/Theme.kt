package com.jaxia.app.ui.theme

import android.os.Build
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

/**
 * JAXIA ships light-only, on purpose.
 *
 * [DarkColorScheme] below is complete and correct, but the screens do not
 * consume it: they hardcode `Color.White` surfaces and literal `Color(0xFF…)`
 * text in 174 places against 3 uses of `MaterialTheme.colorScheme`. Following
 * the system setting therefore produced near-white text (`onSurface =
 * 0xFFEDE5E0`) on hardcoded white cards — unreadable (AUDITORIA.md A-07).
 *
 * Forcing light is the correct call until the screens are migrated to
 * `MaterialTheme.colorScheme`. Set [darkTheme] explicitly only once that
 * migration is done; [DarkColorScheme] is kept ready for it.
 */
@Composable
fun JaxiaTheme(
  darkTheme: Boolean = false,
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

