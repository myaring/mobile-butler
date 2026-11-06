package dev.myaring.mobile_butler.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.myaring.mobile_butler.ui.theme.MbOnBackground
import dev.myaring.mobile_butler.ui.theme.MbOnSurfaceVariant
import dev.myaring.mobile_butler.ui.theme.MbRadius
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MbSurfaceContainer
import dev.myaring.mobile_butler.ui.theme.MbSurfaceVariant

/**
 * 순서 변경 손잡이 아이콘 (가로선 3줄).
 */
@Composable
fun DragHandleIcon(
    modifier: Modifier = Modifier,
    tint: Color = MbOnSurfaceVariant,
) {
    Canvas(
        modifier = modifier
            .size(width = 18.dp, height = 12.dp)
            .semantics { contentDescription = "순서 변경 손잡이" }
    ) {
        val strokeWidth = 2.dp.toPx()
        val cornerRadius = CornerRadius(strokeWidth / 2, strokeWidth / 2)
        drawRoundRect(
            color = tint,
            topLeft = Offset(0f, 0f),
            size = Size(size.width, strokeWidth),
            cornerRadius = cornerRadius
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(0f, 5.dp.toPx()),
            size = Size(size.width, strokeWidth),
            cornerRadius = cornerRadius
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(0f, 10.dp.toPx()),
            size = Size(size.width, strokeWidth),
            cornerRadius = cornerRadius
        )
    }
}

/**
 * [B-7] 목표 목록의 개별 목표 아이템 컴포넌트.
 */
@Composable
fun GoalListItem(
    title: String,
    typeLabel: String,
    summary: String,
    modifier: Modifier = Modifier,
    progress: Float = 0f,
    showProgress: Boolean = true,
    onClick: () -> Unit = {},
    dragHandleModifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics {
                this.contentDescription = "$title, $typeLabel, $summary"
            },
        shape = RoundedCornerShape(MbRadius.md),
        colors = CardDefaults.cardColors(
            containerColor = MbSurfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MbSpacing.md, vertical = MbSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = dragHandleModifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                DragHandleIcon(tint = MbOnSurfaceVariant)
            }

            Spacer(modifier = Modifier.width(MbSpacing.sm))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MbSpacing.xs)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MbSpacing.sm)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MbOnBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Surface(
                        shape = RoundedCornerShape(MbRadius.xs),
                        color = MbSurfaceVariant
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MbOnSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MbOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (showProgress) {
                    Spacer(modifier = Modifier.height(2.dp))
                    MbProgressBar(
                        progress = progress,
                        contentDescription = "$title 진행률 ${(progress.coerceIn(0f, 1f) * 100).toInt()}%",
                        height = 6.dp
                    )
                }
            }

            Spacer(modifier = Modifier.width(MbSpacing.sm))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "상세 보기",
                tint = MbOnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
