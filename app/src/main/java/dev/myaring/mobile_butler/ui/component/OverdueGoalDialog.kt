package dev.myaring.mobile_butler.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * [C-13] 마감 지난 목표의 첫 팝업 다이얼로그.
 * 실패나 미달 표현 없이 지금까지 진행한 분량과 남은 분량을 긍정적으로 안내합니다.
 */
@Composable
fun OverdueGoalDialog(
    goal: Goal,
    recordedTotal: Double,
    onContinue: () -> Unit,
    onFinish: () -> Unit,
    onDismissRequest: () -> Unit,
    today: LocalDate = LocalDate.now(),
) {
    val daysPassed = (ChronoUnit.DAYS.between(goal.startDate, today) + 1L).coerceAtLeast(1L)
    val remaining = (goal.targetValue - (goal.startValue + recordedTotal)).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "${goal.title} 일정 안내",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "${daysPassed}일 동안 ${recordedTotal.toLong()}${goal.unit} 진행하셨습니다.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(MbSpacing.xs))
                Text(
                    text = "남은 ${remaining.toLong()}${goal.unit}을 이어서 진행하시겠습니까?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            MbButton(
                text = "이어서 하기",
                onClick = onContinue,
                style = MbButtonStyle.Filled
            )
        },
        dismissButton = {
            TextButton(onClick = onFinish) {
                Text("여기서 마무리")
            }
        }
    )
}
