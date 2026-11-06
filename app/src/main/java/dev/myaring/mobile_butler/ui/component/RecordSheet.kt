package dev.myaring.mobile_butler.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import dev.myaring.mobile_butler.ui.theme.MbOnSurface
import dev.myaring.mobile_butler.ui.theme.MbOnSurfaceVariant
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MbSurfaceContainer

/**
 * [B-8] 기록 시트 UI (design T-02, T-04b 대응).
 *
 * 완료 조건:
 * - 헤더: "목표 이름" 과 "오늘의 목표 M 중 N 했어요"
 * - 맨 위 노란 버튼: "남은 N 모두 했어요"
 * - "또는 한 만큼만" 영역: -/+ 1단위 정수 스테퍼와 "N 기록" 버튼
 * - 마감 지난 목표 시트: "오늘의 목표" 문구 없이 남은 분량 기준 기록
 * - 바깥 탭 또는 아래로 스와이프 시 저장 없이 닫힘
 *
 * @param title 목표 이름
 * @param unit 표시 단위 (예: "쪽", "문제", "km")
 * @param todayProgress 오늘 이미 기록한 양 (N)
 * @param todayQuota 오늘 할 몫 (M)
 * @param remainingTotal 전체 남은 분량 (마감 지난 목표용)
 * @param isOverdue 마감 지난 목표 여부
 * @param onSave 완료 또는 기록 버튼 클릭 시 전달되는 양 (정수)
 * @param onDismiss 시트를 닫을 때 호출되는 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordSheet(
    title: String,
    unit: String,
    todayProgress: Double,
    todayQuota: Double,
    remainingTotal: Double,
    isOverdue: Boolean,
    onSave: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    val remainingForToday = (todayQuota - todayProgress).coerceAtLeast(0.0).toInt()
    val remainingForOverdue = remainingTotal.coerceAtLeast(0.0).toInt()
    val completeAllAmount = if (isOverdue) remainingForOverdue else remainingForToday

    var stepperAmount by remember {
        mutableIntStateOf(if (completeAllAmount > 0) completeAllAmount.coerceAtMost(5) else 1)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MbSurfaceContainer,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MbSpacing.lg, vertical = MbSpacing.md)
        ) {
            // 1. 헤더: 목표 이름 및 진행 안내
            Text(
                text = "$title · 기록",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MbOnSurface
            )

            Spacer(modifier = Modifier.height(MbSpacing.xs))

            if (isOverdue) {
                Text(
                    text = "남은 ${remainingForOverdue}${unit} 중 진행한 만큼 기록합니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = MbOnSurfaceVariant
                )
            } else {
                Text(
                    text = "오늘의 목표 ${todayQuota.toLong()} 중 ${todayProgress.toLong()} 했어요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MbOnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(MbSpacing.lg))

            // 2. 한 번에 완료: 맨 위의 노란 버튼
            if (completeAllAmount > 0) {
                MbButton(
                    text = "남은 ${completeAllAmount}${unit} 모두 완료했어요",
                    onClick = { onSave(completeAllAmount) },
                    style = MbButtonStyle.Filled,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(MbSpacing.lg))
            }

            // 3. 일부만 기록: 스테퍼 및 기록 버튼
            Text(
                text = "또는 한 만큼만",
                style = MaterialTheme.typography.bodySmall,
                color = MbOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(MbSpacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (stepperAmount > 1) stepperAmount -= 1 },
                    enabled = stepperAmount > 1
                ) {
                    Text(
                        text = "−",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (stepperAmount > 1) MbOnSurface else MbOnSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.padding(horizontal = MbSpacing.md))

                Text(
                    text = "$stepperAmount$unit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MbOnSurface
                )

                Spacer(modifier = Modifier.padding(horizontal = MbSpacing.md))

                IconButton(onClick = { stepperAmount += 1 }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "1 $unit 추가",
                        tint = MbOnSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(MbSpacing.md))

            MbButton(
                text = "$stepperAmount$unit 기록",
                onClick = { onSave(stepperAmount) },
                style = MbButtonStyle.Outlined,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(MbSpacing.xl))
        }
    }
}
