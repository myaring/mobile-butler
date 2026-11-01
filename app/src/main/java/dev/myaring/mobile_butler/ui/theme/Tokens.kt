package dev.myaring.mobile_butler.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object MbSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
}

object MbRadius {
    val xs = 4.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 28.dp
    val full = RoundedCornerShape(50)
}

val MbShapes = Shapes(
    extraSmall = RoundedCornerShape(MbRadius.xs),
    small = RoundedCornerShape(MbRadius.sm),
    medium = RoundedCornerShape(MbRadius.md),
    large = RoundedCornerShape(MbRadius.lg),
    extraLarge = RoundedCornerShape(MbRadius.lg)
)