package dev.myaring.mobile_butler.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.myaring.mobile_butler.domain.model.EndReason
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import dev.myaring.mobile_butler.domain.model.ProgressEntry
import dev.myaring.mobile_butler.domain.viewmodel.GoalScreenViewModel
import dev.myaring.mobile_butler.ui.component.CalendarHeatmap
import dev.myaring.mobile_butler.ui.component.MbButton
import dev.myaring.mobile_butler.ui.component.MbButtonStyle
import dev.myaring.mobile_butler.ui.component.MbProgressBar
import dev.myaring.mobile_butler.ui.component.RecordSheet
import dev.myaring.mobile_butler.ui.theme.MbBackground
import dev.myaring.mobile_butler.ui.theme.MbOnBackground
import dev.myaring.mobile_butler.ui.theme.MbOnSurface
import dev.myaring.mobile_butler.ui.theme.MbOnSurfaceVariant
import dev.myaring.mobile_butler.ui.theme.MbRadius
import dev.myaring.mobile_butler.ui.theme.MbRecordDone
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MbSurfaceContainer
import dev.myaring.mobile_butler.ui.theme.MbSurfaceVariant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * [C-7] 목표 상세 화면 컴포넌트.
 *
 * 완료 조건:
 * - 요약: 전체 진행, 퍼센트, D-day, 마감일, 오늘 할 몫, 오늘 기록 버튼
 * - 달력 히트맵 (#65) 과 "n일 중 m일 기록" 이 보인다.
 * - 기록 이력은 같은 날 여러 건을 각각 한 줄로 보여준다.
 * - 앱 바 메뉴에 수정, 마감일 연장, 끝내기 (MANUAL)가 있다.
 * - 완료된 목표의 상세는 읽기 전용이라 오늘 기록 버튼과 이 메뉴를 숨기고, 끝낸 날/이유 (예: "조기 종료") 를 보여준다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailTab(
    viewModel: GoalScreenViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val goal = uiState.selectedGoal

    if (goal == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "선택된 목표가 없습니다.",
                color = MbOnSurfaceVariant
            )
        }
        return
    }

    val entries = uiState.selectedGoalEntries
    val isCompleted = goal.status == GoalStatus.COMPLETED

    var showMenu by remember { mutableStateOf(false) }
    var showRecordSheet by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showExtendDialog by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val recordedTotal = entries.sumOf { it.amount }
    val progressRatio = (recordedTotal / goal.targetValue).toFloat().coerceIn(0f, 1f)
    val percent = (progressRatio * 100).toInt()

    val dDayText = when {
        goal.dueDate == null -> "기한 없음"
        goal.dueDate.isBefore(today) -> "마감 지남"
        goal.dueDate.isEqual(today) -> "D-Day"
        else -> "D-${ChronoUnit.DAYS.between(today, goal.dueDate)}"
    }

    val remainingDays = if (goal.dueDate != null && !goal.dueDate.isBefore(today)) {
        ChronoUnit.DAYS.between(today, goal.dueDate) + 1
    } else {
        1L
    }
    val remainingValue = (goal.targetValue - recordedTotal).coerceAtLeast(0.0)
    val todayQuota = (remainingValue / remainingDays).coerceAtLeast(1.0).toInt()

    val recordedDates = entries.map {
        it.recordedAt.atZone(ZoneId.systemDefault()).toLocalDate()
    }.toSet()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(MbSpacing.md),
        verticalArrangement = Arrangement.spacedBy(MbSpacing.lg)
    ) {
        // 1. 요약 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(MbRadius.md),
            colors = CardDefaults.cardColors(containerColor = MbSurfaceContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MbSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(MbSpacing.md)
            ) {
                // 상단 줄: 목표 이름 및 메뉴 / 종료 뱃지
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MbOnBackground
                        )

                        if (isCompleted) {
                            Spacer(modifier = Modifier.height(MbSpacing.xs))
                            val endReasonLabel = when (goal.endReason) {
                                EndReason.MANUAL -> "조기 종료"
                                EndReason.ACHIEVED -> "목표 달성"
                                EndReason.DEADLINE -> "마감 기한 종료"
                                null -> "종료"
                            }
                            Text(
                                text = "끝낸 날: ${goal.endedAt} · $endReasonLabel",
                                style = MaterialTheme.typography.labelMedium,
                                color = MbOnSurfaceVariant
                            )
                        }
                    }

                    // 완료된 목표에서는 메뉴 숨김
                    if (!isCompleted) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "더보기 메뉴",
                                    tint = MbOnBackground
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("수정") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.onChangeTabToCreation()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("마감일 연장") },
                                    onClick = {
                                        showMenu = false
                                        showExtendDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("끝내기") },
                                    onClick = {
                                        showMenu = false
                                        showCompleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }

                // 진행률 바 및 텍스트
                Column(verticalArrangement = Arrangement.spacedBy(MbSpacing.xs)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "전체 진행 ${recordedTotal.toLong()}${goal.unit} / ${goal.targetValue.toLong()}${goal.unit}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MbOnSurface
                        )
                        Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MbOnBackground
                        )
                    }

                    MbProgressBar(
                        progress = progressRatio,
                        contentDescription = "전체 진행률 $percent%",
                        height = 8.dp
                    )
                }

                // D-day, 마감일, 오늘 할 몫 지표 그리드
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricChip(label = "D-day", value = dDayText)
                    MetricChip(label = "마감일", value = goal.dueDate?.toString() ?: "기한 없음")
                    if (!isCompleted) {
                        MetricChip(label = "오늘 할 몫", value = "$todayQuota${goal.unit}")
                    }
                }

                // 완료된 목표에서는 '오늘 기록' 버튼 숨김
                if (!isCompleted) {
                    MbButton(
                        text = "오늘 기록",
                        onClick = { showRecordSheet = true },
                        style = MbButtonStyle.Filled,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 2. 달력 히트맵
        CalendarHeatmap(
            today = today,
            recordedDates = recordedDates
        )

        // 3. 기록 이력 (같은 날 여러 건을 각각 한 줄로 표시)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(MbRadius.md),
            colors = CardDefaults.cardColors(containerColor = MbSurfaceContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MbSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(MbSpacing.sm)
            ) {
                Text(
                    text = "기록 이력 (${entries.size}건)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MbOnBackground
                )

                if (entries.isEmpty()) {
                    Text(
                        text = "아직 기록된 이력이 없습니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MbOnSurfaceVariant,
                        modifier = Modifier.padding(vertical = MbSpacing.sm)
                    )
                } else {
                    entries.sortedByDescending { it.recordedAt }.forEach { entry ->
                        val entryDate = entry.recordedAt.atZone(ZoneId.systemDefault()).toLocalDate()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(MbRadius.xs))
                                .background(MbSurfaceVariant)
                                .padding(horizontal = MbSpacing.md, vertical = MbSpacing.sm),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${entryDate.monthValue}월 ${entryDate.dayOfMonth}일",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MbOnSurface
                            )
                            Text(
                                text = "${entry.amount.toLong()}${goal.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MbRecordDone
                            )
                        }
                    }
                }
            }
        }
    }

    // 기록 시트
    if (showRecordSheet && !isCompleted) {
        RecordSheet(
            title = goal.title,
            unit = goal.unit,
            todayProgress = 0.0,
            todayQuota = todayQuota.toDouble(),
            remainingTotal = remainingValue,
            isOverdue = goal.dueDate != null && goal.dueDate.isBefore(today),
            onSave = { amount ->
                viewModel.recordProgress(goal.id, amount.toDouble())
                showRecordSheet = false
            },
            onDismiss = { showRecordSheet = false }
        )
    }

    // 끝내기 확인 팝업
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("여기서 끝내시겠습니까?") },
            text = { Text("목표를 조기 종료하고 보관함으로 이동합니다. 지금까지의 기록은 안전하게 보존됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.completeGoalManually(goal.id)
                        showCompleteDialog = false
                    }
                ) {
                    Text("끝내기")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // 마감일 연장 팝업
    if (showExtendDialog) {
        val currentDue = goal.dueDate ?: today
        val extendedDue = currentDue.plusDays(7)
        AlertDialog(
            onDismissRequest = { showExtendDialog = false },
            title = { Text("마감일 연장") },
            text = { Text("마감일을 7일 연장하여 ${extendedDue}까지 진행하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.extendDueDate(goal.id, extendedDue)
                        showExtendDialog = false
                    }
                ) {
                    Text("연장하기")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExtendDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MbOnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MbOnBackground
        )
    }
}
