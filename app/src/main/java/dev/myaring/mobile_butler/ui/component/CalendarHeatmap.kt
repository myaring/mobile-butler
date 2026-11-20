package dev.myaring.mobile_butler.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.myaring.mobile_butler.ui.theme.MbOnBackground
import dev.myaring.mobile_butler.ui.theme.MbOnSurfaceVariant
import dev.myaring.mobile_butler.ui.theme.MbRadius
import dev.myaring.mobile_butler.ui.theme.MbRecordDone
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MbSurfaceContainer
import dev.myaring.mobile_butler.ui.theme.MbSurfaceVariant
import java.time.LocalDate
import java.time.YearMonth

/**
 * [B-10] 목표 상세의 월간 기록 달력 히트맵 컴포넌트.
 *
 * 완료 조건:
 * - 일~토 머리글과 날짜 칸, "2026년 10월 / 20일 중 15일 기록" 제목을 그린다.
 * - 좌우로 월을 넘길 수 있다.
 * - 미래 칸은 35% 투명, 오늘 칸은 DayCell과 같은 두 상태 (옅은 채움/진함)에 흰 테두리.
 * - 범례 (한 날, 기록 없음) 가 있다.
 */
@Composable
fun CalendarHeatmap(
    modifier: Modifier = Modifier,
    initialYearMonth: YearMonth = YearMonth.now(),
    today: LocalDate = LocalDate.now(),
    recordedDates: Set<LocalDate> = emptySet(),
    onMonthChanged: (YearMonth) -> Unit = {},
) {
    var currentYearMonth by remember(initialYearMonth) { mutableStateOf(initialYearMonth) }

    // 이번 달 기준 경과 일수 및 기록된 일수 계산
    val daysInMonth = currentYearMonth.lengthOfMonth()
    val isCurrentMonth = currentYearMonth == YearMonth.from(today)
    val isPastMonth = currentYearMonth.isBefore(YearMonth.from(today))

    val eligibleDays = when {
        isCurrentMonth -> today.dayOfMonth
        isPastMonth -> daysInMonth
        else -> 0
    }

    val recordedCount = (1..daysInMonth).count { day ->
        val date = currentYearMonth.atDay(day)
        !date.isAfter(today) && recordedDates.contains(date)
    }

    val title = "${currentYearMonth.year}년 ${currentYearMonth.monthValue}월 / ${eligibleDays}일 중 ${recordedCount}일 기록"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MbRadius.md),
        colors = CardDefaults.cardColors(containerColor = MbSurfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MbSpacing.md),
            verticalArrangement = Arrangement.spacedBy(MbSpacing.sm)
        ) {
            // 상단 제목 및 월 이동 내비게이션
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val prev = currentYearMonth.minusMonths(1)
                        currentYearMonth = prev
                        onMonthChanged(prev)
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "이전 달",
                        tint = MbOnBackground
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MbOnBackground,
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = {
                        val next = currentYearMonth.plusMonths(1)
                        currentYearMonth = next
                        onMonthChanged(next)
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "다음 달",
                        tint = MbOnBackground
                    )
                }
            }

            // 일~토 요일 머리글
            val weekHeaders = listOf("일", "월", "화", "수", "목", "금", "토")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekHeaders.forEach { header ->
                    Text(
                        text = header,
                        style = MaterialTheme.typography.labelSmall,
                        color = MbOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 날짜 그리드 (7열)
            val firstDayOfWeek = currentYearMonth.atDay(1).dayOfWeek
            val startPadding = firstDayOfWeek.value % 7 // 일요일: 7 % 7 = 0, 월요일: 1, ...
            val totalCells = startPadding + daysInMonth
            val totalRows = (totalCells + 6) / 7

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (rowIndex in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (colIndex in 0 until 7) {
                            val cellIndex = rowIndex * 7 + colIndex
                            val dayNumber = cellIndex - startPadding + 1

                            if (dayNumber in 1..daysInMonth) {
                                val cellDate = currentYearMonth.atDay(dayNumber)
                                val isCellToday = cellDate == today
                                val isFuture = cellDate.isAfter(today)
                                val hasRecord = recordedDates.contains(cellDate)

                                CalendarDayCell(
                                    day = dayNumber,
                                    isToday = isCellToday,
                                    isFuture = isFuture,
                                    hasRecord = hasRecord,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(MbSpacing.xs))

            // 하단 범례: 한 날, 기록 없음
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 한 날
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MbRecordDone)
                )
                Text(
                    text = " 한 날",
                    style = MaterialTheme.typography.labelSmall,
                    color = MbOnSurfaceVariant,
                    modifier = Modifier.padding(end = MbSpacing.md)
                )

                // 기록 없음
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MbSurfaceVariant)
                )
                Text(
                    text = " 기록 없음",
                    style = MaterialTheme.typography.labelSmall,
                    color = MbOnSurfaceVariant
                )
            }
        }
    }
}

/**
 * 개별 달력 날짜 셀.
 */
@Composable
private fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    isFuture: Boolean,
    hasRecord: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(4.dp)
    val backgroundColor = if (hasRecord) MbRecordDone else MbSurfaceVariant

    val baseModifier = modifier
        .aspectRatio(1f)
        .clip(shape)

    val styledModifier = when {
        isFuture -> {
            baseModifier
                .alpha(0.35f)
                .background(MbSurfaceVariant)
        }
        isToday -> {
            baseModifier
                .border(width = 1.5.dp, color = MbOnBackground, shape = shape)
                .background(backgroundColor)
        }
        else -> {
            baseModifier.background(backgroundColor)
        }
    }

    val desc = when {
        isToday && hasRecord -> "${day}일 오늘, 기록 완료"
        isToday && !hasRecord -> "${day}일 오늘, 기록 없음"
        isFuture -> "${day}일, 미래 날짜"
        hasRecord -> "${day}일, 기록 완료"
        else -> "${day}일, 기록 없음"
    }

    Box(
        modifier = styledModifier.semantics { contentDescription = desc },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = if (hasRecord) MbOnBackground else MbOnSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
