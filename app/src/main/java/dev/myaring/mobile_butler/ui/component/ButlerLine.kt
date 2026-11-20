package dev.myaring.mobile_butler.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.myaring.mobile_butler.ui.theme.MbRadius
import dev.myaring.mobile_butler.ui.theme.MbShapes
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MbSurfaceContainer
import dev.myaring.mobile_butler.ui.theme.MobilebutlerTheme

@Composable
fun ButlerLine(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.AccountCircle,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MbShapes.medium),
        color = MbSurfaceContainer,
        shape = MbShapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MbSpacing.md, vertical = MbSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "집사 아이콘",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(MbSpacing.sm))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview
@Composable
private fun ButlerLinePreview() {
    MobilebutlerTheme {
        ButlerLine(
            message = "오늘도 차근차근 목표를 향해 나아가 봅시다."
        )
    }
}
