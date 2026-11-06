package dev.myaring.mobile_butler.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import dev.myaring.mobile_butler.R

private val MbFontFamily = FontFamily(
    Font(
        R.font.noto_sans_kr_regular,
        FontWeight.Normal
    ),
    Font(
        R.font.noto_sans_kr_medium,
        FontWeight.Medium
    ),
    Font(
        R.font.noto_sans_kr_bold,
        FontWeight.Bold
    )
)

private val BaseTypography = Typography()

val Typography = Typography(
    displayLarge = BaseTypography.displayLarge.copy(
        fontFamily = MbFontFamily
    ),
    displayMedium = BaseTypography.displayMedium.copy(
        fontFamily = MbFontFamily
    ),
    displaySmall = BaseTypography.displaySmall.copy(
        fontFamily = MbFontFamily
    ),

    headlineLarge = BaseTypography.headlineLarge.copy(
        fontFamily = MbFontFamily
    ),
    headlineMedium = BaseTypography.headlineMedium.copy(
        fontFamily = MbFontFamily
    ),
    headlineSmall = BaseTypography.headlineSmall.copy(
        fontFamily = MbFontFamily
    ),

    titleLarge = BaseTypography.titleLarge.copy(
        fontFamily = MbFontFamily,
        fontWeight = FontWeight.Bold
    ),
    titleMedium = BaseTypography.titleMedium.copy(
        fontFamily = MbFontFamily
    ),
    titleSmall = BaseTypography.titleSmall.copy(
        fontFamily = MbFontFamily
    ),

    bodyLarge = BaseTypography.bodyLarge.copy(
        fontFamily = MbFontFamily
    ),
    bodyMedium = BaseTypography.bodyMedium.copy(
        fontFamily = MbFontFamily
    ),
    bodySmall = BaseTypography.bodySmall.copy(
        fontFamily = MbFontFamily
    ),

    labelLarge = BaseTypography.labelLarge.copy(
        fontFamily = MbFontFamily
    ),
    labelMedium = BaseTypography.labelMedium.copy(
        fontFamily = MbFontFamily
    ),
    labelSmall = BaseTypography.labelSmall.copy(
        fontFamily = MbFontFamily
    )
)