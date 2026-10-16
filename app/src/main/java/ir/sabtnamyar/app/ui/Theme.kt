package ir.sabtnamyar.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7FC8F8),
    onPrimary = Color(0xFF00344F),
    primaryContainer = Color(0xFF14506F),
    onPrimaryContainer = Color(0xFFCDE7FF),
    secondary = Color(0xFF8BD5A0),
    onSecondary = Color(0xFF00391C),
    tertiary = Color(0xFFF5C77E),
    background = Color(0xFF0B1220),
    onBackground = Color(0xFFE4E8F1),
    surface = Color(0xFF121B2E),
    onSurface = Color(0xFFE4E8F1),
    surfaceVariant = Color(0xFF1C2740),
    onSurfaceVariant = Color(0xFFC0C8DA),
    outline = Color(0xFF3A4869),
    error = Color(0xFFFF8A80)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0E6BA8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDE7FF),
    onPrimaryContainer = Color(0xFF00344F),
    secondary = Color(0xFF2E7D46),
    onSecondary = Color.White,
    tertiary = Color(0xFF8A5A00),
    background = Color(0xFFF6F8FC),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFE6ECF5),
    onSurfaceVariant = Color(0xFF44506B),
    outline = Color(0xFFC3CDE0),
    error = Color(0xFFB3261E)
)

@Composable
fun SabtNamYarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
