package dev.myaring.mobile_butler.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.viewmodel.DayCellUiModel
import dev.myaring.mobile_butler.domain.viewmodel.ITopBarViewModel
import dev.myaring.mobile_butler.domain.viewmodel.TodayGoalCardUiState
import dev.myaring.mobile_butler.domain.viewmodel.TodayScreenViewModel
import dev.myaring.mobile_butler.domain.viewmodel.TodayUiState
import dev.myaring.mobile_butler.ui.component.ButlerLine
import dev.myaring.mobile_butler.ui.component.GoalCardCollapsed
import dev.myaring.mobile_butler.ui.component.GoalCardExpanded
import dev.myaring.mobile_butler.ui.component.RecordSheet
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MobilebutlerTheme
import java.time.LocalDate

public val todayScreenImpl = Screen(
    eScreen = EScreen.TODAY,
    screenDrawerDelegateeOrNull = ::TodayScreenRouter,
)

@Composable
private fun TodayScreenRouter(
    onNotifyTopBar: (ITopBarViewModel) -> Unit,
    modifier: Modifier
) {
    val viewModel: TodayScreenViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    onNotifyTopBar(viewModel)

    TodayScreenContent(
        uiState = uiState,
        onCardClick = viewModel::toggleCardExpansion,
        onRecordClick = { /* C-3 기록 플로우 연동 지점 */ },
        onDetailClick = { /* C-7 목표 상세 연동 지점 */ },
        onMoreToggle = viewModel::toggleMoreExpansion,
        modifier = modifier
    )
}

/**
 * [C-1] 오늘 화면 조립 컴포넌트.
 *
 * 위에서 아래로 순서:
 * 1. 집사 한마디 (ButlerLine)
 * 2. 펼친 카드 1개 (GoalCardExpanded)
 * 3. 접힌 카드들 (GoalCardCollapsed)
 * 4. 목표 N개 더보기 텍스트 버튼
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TodayScreenContent(
    uiState: TodayUiState,
    onCardClick: (String) -> Unit,
    onRecordClick: (Goal) -> Unit,
    onDetailClick: (String) -> Unit,
    onMoreToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var recordingGoal by remember { mutableStateOf<TodayGoalCardUiState?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = MbSpacing.md, vertical = MbSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(MbSpacing.md)
    ) {
        // 1. 집사 한마디
        ButlerLine(message = uiState.butlerMessage)

        // 2. 상위 3개 목표 카드 (펼친 카드 1개 + 접힌 카드들)
        uiState.topGoals.forEach { cardState ->
            val isExpanded = cardState.goal.id == uiState.expandedGoalId
            if (isExpanded) {
                GoalCardExpanded(
                    title = cardState.goal.title,
                    dDayText = cardState.dDayText,
                    unit = cardState.goal.unit,
                    todayProgress = cardState.todayProgress,
                    todayQuota = cardState.todayQuota,
                    totalProgress = cardState.totalProgress,
                    targetValue = cardState.targetValue,
                    recent14Days = cardState.recent14Days,
                    onRecordClick = { recordingGoal = cardState },
                    onDetailClick = { onDetailClick(cardState.goal.id) },
                    onCardClick = { onCardClick(cardState.goal.id) }
                )
            } else {
                GoalCardCollapsed(
                    title = cardState.goal.title,
                    summary = cardState.summary,
                    onCardClick = { onCardClick(cardState.goal.id) },
                    onRecordClick = {
                        // 접힌 카드의 테두리 버튼은 카드를 펼치며 기록 시트를 엶
                        onCardClick(cardState.goal.id)
                        recordingGoal = cardState
                    }
                )
            }
        }

        // 3. 목표 N개 더보기 영역 (나머지 목표)
        if (uiState.remainingGoals.isNotEmpty()) {
            if (uiState.isMoreExpanded) {
                uiState.remainingGoals.forEach { cardState ->
                    GoalCardCollapsed(
                        title = cardState.goal.title,
                        summary = cardState.summary,
                        onCardClick = { onCardClick(cardState.goal.id) },
                        onRecordClick = {
                            onCardClick(cardState.goal.id)
                            recordingGoal = cardState
                        }
                    )
                }
                TextButton(
                    onClick = onMoreToggle,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "목표 접기",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                TextButton(
                    onClick = onMoreToggle,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "목표 ${uiState.remainingGoals.size}개 더보기",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // 기록 시트 (T-02)
    recordingGoal?.let { target ->
        RecordSheet(
            title = target.goal.title,
            unit = target.goal.unit,
            todayProgress = target.todayProgress,
            todayQuota = target.todayQuota,
            remainingTotal = (target.targetValue - target.totalProgress).coerceAtLeast(0.0),
            isOverdue = target.isOverdue,
            onSave = { amount ->
                onRecordClick(target.goal)
                recordingGoal = null
            },
            onDismiss = { recordingGoal = null }
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
private fun TodayScreenRouterPreview() {
    val sampleDate = LocalDate.of(2026, 11, 6)
    val sample14Days = (0 until 14).map { i ->
        DayCellUiModel(
            date = sampleDate.minusDays((13 - i).toLong()),
            isToday = (i == 13),
            hasRecord = (i % 2 == 0)
        )
    }

    val sampleGoal1 = Goal(
        id = "goal_1",
        title = "영어 원서 읽기",
        targetValue = 734.0,
        unit = "쪽",
        startDate = sampleDate.minusDays(10),
        dueDate = sampleDate.plusDays(26),
        priority = 0
    )

    val sampleGoal2 = Goal(
        id = "goal_2",
        title = "매일 1만 보 걷기",
        targetValue = 100000.0,
        unit = "보",
        startDate = sampleDate.minusDays(5),
        dueDate = sampleDate.plusDays(18),
        priority = 1
    )

    val previewState = TodayUiState(
        topGoals = listOf(
            TodayGoalCardUiState(
                goal = sampleGoal1,
                dDayText = "D-26",
                todayProgress = 10.0,
                todayQuota = 20.0,
                totalProgress = 220.0,
                targetValue = 734.0,
                isOverdue = false,
                isTodayQuotaMet = false,
                recent14Days = sample14Days,
                summary = "오늘의 목표 20쪽 · D-26"
            ),
            TodayGoalCardUiState(
                goal = sampleGoal2,
                dDayText = "D-18",
                todayProgress = 5000.0,
                todayQuota = 10000.0,
                totalProgress = 45000.0,
                targetValue = 100000.0,
                isOverdue = false,
                isTodayQuotaMet = false,
                recent14Days = sample14Days,
                summary = "오늘의 목표 1만 보 · D-18"
            )
        ),
        expandedGoalId = "goal_1",
        butlerMessage = "2일 연속으로 읽고 계십니다."
    )

    MobilebutlerTheme {
        TodayScreenContent(
            uiState = previewState,
            onCardClick = {},
            onRecordClick = {},
            onDetailClick = {},
            onMoreToggle = {},
            modifier = Modifier
        )
    }
}
