package dev.myaring.mobile_butler.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.ui.theme.MbRadius
import dev.myaring.mobile_butler.ui.theme.MbShapes
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MbSurfaceContainer

/**
 * [C-13] 마감 지난 목표 카드에 표시되는 상시 안내 배너.
 * 실패/미달 표현 없이 현재까지 기록한 양을 보여주며 마감일 연장(이어서) 및 마무리를 지원합니다.
 */
@Composable
fun OverdueGoalBanner(
    goal: Goal,
    recordedTotal: Double,
    onContinue: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remaining = (goal.targetValue - (goal.startValue + recordedTotal)).coerceAtLeast(0.0)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MbSurfaceContainer,
        shape = MbShapes.medium
    ) {
        Column(modifier = Modifier.padding(MbSpacing.md)) {
            Text(
                text = "마감 기한이 지난 목표입니다.",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(MbSpacing.xs))
            Text(
                text = "현재까지 ${recordedTotal.toLong()}${goal.unit} 완료 (남은 분량: ${remaining.toLong()}${goal.unit})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(MbSpacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onFinish) {
                    Text("여기서 마무리")
                }
                Spacer(modifier = Modifier.padding(horizontal = MbSpacing.xs))
                MbButton(
                    text = "이어서",
                    onClick = onContinue,
                    style = MbButtonStyle.Filled
                )
            }
        }
    }
}
