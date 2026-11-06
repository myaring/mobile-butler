package dev.myaring.mobile_butler.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import dev.myaring.mobile_butler.ui.theme.MbOnSurface
import dev.myaring.mobile_butler.ui.theme.MbOnSurfaceVariant
import dev.myaring.mobile_butler.ui.theme.MbPrimary
import dev.myaring.mobile_butler.ui.theme.MbShapes
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MbSurfaceContainer

/**
 * [B-5] GoalCard 펼친 상태 (누적형 목표).
 *
 * 레이아웃 순서:
 * 1. 제목 줄 (제목, D-day, 자세히 버튼)
 * 2. 오늘의 목표 + 진행 바
 * 3. 오늘 기록 버튼 (몫을 채우면 테두리 '기록 수정' 버튼으로 전환)
 * 4. [오늘 묶음과 전체 묶음 간격 분리]
 * 5. 전체 진행 + 진행 바
 * 6. 최근 14일 줄 (14칸 스트립)
 */
@Composable
fun GoalCardExpanded(
    title: String,
    dDayText: String,
    unit: String,
    todayProgress: Double,
    todayQuota: Double,
    totalProgress: Double,
    targetValue: Double,
    recent14Days: List<DayCellUiModel>,
    onRecordClick: () -> Unit,
    onDetailClick: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isTodayQuotaMet = todayQuota > 0.0 && todayProgress >= todayQuota
    val todayRatio = if (todayQuota > 0.0) {
        (todayProgress / todayQuota).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }
    val totalRatio = if (targetValue > 0.0) {
        (totalProgress / targetValue).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }
    val totalPercentage = (totalRatio * 100).toInt()
    val recordedDaysCount = recent14Days.count { it.hasRecord }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        color = MbSurfaceContainer,
        shape = MbShapes.medium
    ) {
        Column(modifier = Modifier.padding(MbSpacing.lg)) {
            // 1. 제목 줄 (제목, D-day, 자세히 버튼)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MbSpacing.sm)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MbOnSurface
                    )
                    Text(
                        text = dDayText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MbPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(onClick = onDetailClick) {
                    Text(
                        text = "자세히",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MbOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(MbSpacing.sm))

            // 2 & 3. 오늘 묶음: 오늘의 목표 + 진행 바 + 오늘 기록 버튼
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "오늘의 목표",
                        style = MaterialTheme.typography.bodySmall,
                        color = MbOnSurfaceVariant
                    )
                    Text(
                        text = "${todayProgress.toLong()} / ${todayQuota.toLong()}$unit",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MbOnSurface
                    )
                }

                Spacer(modifier = Modifier.height(MbSpacing.xs))

                MbProgressBar(
                    progress = todayRatio,
                    contentDescription = "오늘 목표 진행률 ${(todayRatio * 100).toInt()}%"
                )

                Spacer(modifier = Modifier.height(MbSpacing.md))

                // 오늘 몫을 다 채우면 노란 버튼이 '오늘 기록'에서 테두리 '기록 수정'으로 전환
                if (isTodayQuotaMet) {
                    MbButton(
                        text = "기록 수정",
                        onClick = onRecordClick,
                        style = MbButtonStyle.Outlined,
                        icon = Icons.Default.Edit,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    MbButton(
                        text = "오늘 기록",
                        onClick = onRecordClick,
                        style = MbButtonStyle.Filled,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 4. 오늘 묶음과 전체 묶음 간격 (명확한 구분)
            Spacer(modifier = Modifier.height(MbSpacing.lg))

            // 5. 전체 진행 + 진행 바
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "전체 진행",
                        style = MaterialTheme.typography.bodySmall,
                        color = MbOnSurfaceVariant
                    )
                    Text(
                        text = "${totalProgress.toLong()} / ${targetValue.toLong()}$unit · ${totalPercentage}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MbOnSurface
                    )
                }

                Spacer(modifier = Modifier.height(MbSpacing.xs))

                MbProgressBar(
                    progress = totalRatio,
                    contentDescription = "전체 목표 진행률 $totalPercentage%"
                )
            }

            Spacer(modifier = Modifier.height(MbSpacing.md))

            // 6. 최근 14일 줄
            RecentRecordStrip(
                recordedDays = recordedDaysCount,
                days = recent14Days
            )
        }
    }
}
