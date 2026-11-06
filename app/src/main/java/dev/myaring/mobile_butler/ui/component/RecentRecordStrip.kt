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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.myaring.mobile_butler.domain.viewmodel.DayCellUiModel
import dev.myaring.mobile_butler.ui.theme.MbOnSurface
import dev.myaring.mobile_butler.ui.theme.MbOnSurfaceVariant
import dev.myaring.mobile_butler.ui.theme.MbRecordDone
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MbSurfaceVariant

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
