package dev.myaring.mobile_butler.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val MbDarkColorScheme = darkColorScheme(
    primary = MbPrimary,
    onPrimary = MbOnPrimary,
    inversePrimary = MbInversePrimary,

    primaryContainer = MbSurfaceContainer,
    onPrimaryContainer = MbOnSurface,

    secondaryContainer = MbSecondaryContainer,
    onSecondaryContainer = MbOnSurface,

    secondary = MbOnSurfaceVariant,
    onSecondary = MbBackground,

    tertiary = MbRecordDone,
    onTertiary = MbOnTertiary,

    background = MbBackground,
    onBackground = MbOnBackground,

    surface = MbBackground,
    onSurface = MbOnSurface,
    inverseSurface = MbOnSurface,
    inverseOnSurface = MbSurfaceContainer,

    surfaceVariant = MbSurfaceVariant,
    onSurfaceVariant = MbOnSurfaceVariant,

    surfaceContainer = MbSurfaceContainer,
    surfaceContainerLow = MbBackground,
    surfaceContainerHigh = MbSurfaceVariant,
    surfaceContainerHighest = MbSurfaceVariant,

    surfaceBright = MbSurfaceVariant,
    surfaceDim = MbBackground,

    outline = MbOutline,
    outlineVariant = MbOutlineVariant,

    error = MbError,
    onError = MbOnError
)

@Composable
fun MobilebutlerTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamicColor) {
        dynamicDarkColorScheme(LocalContext.current)
    } else {
        MbDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = MbShapes,
        content = content
    )
}
