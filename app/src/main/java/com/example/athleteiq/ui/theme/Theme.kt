package com.example.athleteiq.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF176B52), onPrimary = Color.White,
    primaryContainer = Color(0xFFD8F6A4), onPrimaryContainer = Color(0xFF152C24),
    secondary = Color(0xFF526658), secondaryContainer = Color(0xFFE7EFDF),
    background = Color(0xFFF4F6F2), onBackground = Color(0xFF172D29),
    surface = Color.White, onSurface = Color(0xFF172D29),
    surfaceVariant = Color(0xFFE9EEE7), onSurfaceVariant = Color(0xFF56665F),
    outline = Color(0xFF7A8C83), outlineVariant = Color(0xFFDCE4DA)
)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB9E985), onPrimary = Color(0xFF173527),
    primaryContainer = Color(0xFF294F3D), onPrimaryContainer = Color(0xFFD8F6A4),
    secondary = Color(0xFFB8CBBE), secondaryContainer = Color(0xFF30473B),
    background = Color(0xFF101E1A), onBackground = Color(0xFFE8F0E8),
    surface = Color(0xFF1A2A24), onSurface = Color(0xFFE8F0E8),
    surfaceVariant = Color(0xFF283C32), onSurfaceVariant = Color(0xFFB6C8BC),
    outline = Color(0xFF899E90), outlineVariant = Color(0xFF344C3E)
)

@Composable
fun AthleteIQTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(18.dp), large = RoundedCornerShape(24.dp)),
        content = content
    )
}
