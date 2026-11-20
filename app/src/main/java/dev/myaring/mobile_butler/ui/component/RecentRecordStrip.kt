package dev.myaring.mobile_butler.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.myaring.mobile_butler.domain.pacing.RecordState
import dev.myaring.mobile_butler.domain.pacing.TodayCellState
import dev.myaring.mobile_butler.ui.theme.MbOnSurface
import dev.myaring.mobile_butler.ui.theme.MbOnSurfaceVariant
import dev.myaring.mobile_butler.ui.theme.MbRecordDone
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MbSurfaceVariant
import java.time.LocalDate

/**
 * 14칸 스트립에서 각 날짜 칸의 상태 데이터.
 */
data class DayCellUiModel(
    val date: LocalDate,
    val isToday: Boolean,
    val hasRecord: Boolean,
)

/**
 * [B-1] 14일 기록 줄의 개별 날짜 칸 컴포넌트.
 *
 * 상태:
 * - Done: 진함 (MbRecordDone)
 * - Empty: 옅은 색 채움 (MbSurfaceVariant)
 * - TodayPending: 오늘 기록 전 (옅게 채움 + 흰 테두리)
 * - TodayDone: 오늘 기록 후 (진함 + 흰 테두리)
 */
@Composable
fun DayCell(
    model: DayCellUiModel,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(4.dp)
    val backgroundColor = if (model.hasRecord) MbRecordDone else MbSurfaceVariant
    val description = when {
        model.isToday && model.hasRecord -> "오늘, 기록 완료"
        model.isToday && !model.hasRecord -> "오늘, 아직 기록 없음"
        model.hasRecord -> "${model.date.monthValue}월 ${model.date.dayOfMonth}일, 기록 완료"
        else -> "${model.date.monthValue}월 ${model.date.dayOfMonth}일, 기록 없음"
    }

    val cellModifier = if (model.isToday) {
        modifier
            .size(18.dp)
            .border(width = 1.5.dp, color = MbOnSurface, shape = shape)
            .clip(shape)
            .background(backgroundColor)
            .semantics { contentDescription = description }
    } else {
        modifier
            .size(18.dp)
            .clip(shape)
            .background(backgroundColor)
            .semantics { contentDescription = description }
    }

    Box(modifier = cellModifier)
}

/**
 * [B-1] 최근 14일의 기록 유무를 14개 칸으로 보여주는 스트립 컴포넌트.
 *
 * @param recordedDays 14일 중 기록이 존재하는 날의 수
 * @param days 14개의 날짜 셀 데이터 목록 (과거 -> 오늘 순)
 */
@Composable
fun RecentRecordStrip(
    recordedDays: Int,
    days: List<DayCellUiModel>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "최근 14일",
                style = MaterialTheme.typography.bodySmall,
                color = MbOnSurfaceVariant
            )
            Text(
                text = "14일 중 ${recordedDays}일 기록",
                style = MaterialTheme.typography.bodySmall,
                color = MbOnSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(MbSpacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            days.forEach { dayModel ->
                DayCell(model = dayModel)
            }
        }
    }
}
