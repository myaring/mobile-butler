package dev.myaring.mobile_butler.domain.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import dev.myaring.mobile_butler.domain.model.ProgressEntry
import dev.myaring.mobile_butler.domain.pacing.PacingCalculator
import dev.myaring.mobile_butler.domain.pacing.PacingResult
import dev.myaring.mobile_butler.domain.pacing.ProgressCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class TodayScreenViewModel(
    private var clock: Clock = Clock.systemDefaultZone()
) : ViewModel(), ITopBarViewModel {

    private val _title = mutableStateOf("오늘")
    override val title: State<String> = _title

    private val _showBackButton = mutableStateOf(false)
    override val showBackButton: State<Boolean> = _showBackButton

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private var currentGoals: List<Goal> = emptyList()
    private var currentEntries: Map<String, List<ProgressEntry>> = emptyMap()

    override fun onBackTab() {}

    fun setClock(newClock: Clock) {
        clock = newClock
        recalculate()
    }

    fun updateGoals(
        goals: List<Goal>,
        entries: Map<String, List<ProgressEntry>> = emptyMap(),
    ) {
        currentGoals = goals.filter { it.status == GoalStatus.ACTIVE }
            .sortedBy { it.priority }
        currentEntries = entries
        recalculate()
    }

    fun toggleCardExpansion(goalId: String) {
        _uiState.update { state ->
            val nextExpandedId = if (state.expandedGoalId == goalId) null else goalId
            state.copy(expandedGoalId = nextExpandedId)
        }
    }

    fun toggleMoreExpansion() {
        _uiState.update { state ->
            state.copy(isMoreExpanded = !state.isMoreExpanded)
        }
    }

    private fun recalculate() {
        val today = LocalDate.now(clock)
        val sortedGoals = currentGoals.sortedBy { it.priority }

        val cardStates = sortedGoals.map { goal ->
            val goalEntries = currentEntries[goal.id].orEmpty()
            val progress = ProgressCalculator.calculate(goal, goalEntries)
            val totalProgress = progress.recordedAmount
            val todayProgress = goalEntries.filter { it.recordedDate == today }.sumOf { it.amount }

            val pacingResult = PacingCalculator.calculate(goal, goalEntries, clock)
            val todayQuota = when (pacingResult) {
                is PacingResult.Daily -> pacingResult.quota
                else -> 0.0
            }

            val isOverdue = pacingResult is PacingResult.Overdue
            val isTodayQuotaMet = todayQuota > 0.0 && todayProgress >= todayQuota

            val dDayText = when {
                goal.dueDate == null -> ""
                goal.dueDate == today -> "D-Day"
                goal.dueDate.isBefore(today) -> "마감 지남"
                else -> "D-${ChronoUnit.DAYS.between(today, goal.dueDate)}"
            }

            val recent14Days = (0 until 14).map { i ->
                val date = today.minusDays((13 - i).toLong())
                DayCellUiModel(
                    date = date,
                    isToday = (date == today),
                    hasRecord = goalEntries.any { it.recordedDate == date }
                )
            }

            val summary = "오늘의 목표 ${todayQuota.toLong()}${goal.unit} · $dDayText"

            TodayGoalCardUiState(
                goal = goal,
                dDayText = dDayText,
                todayProgress = todayProgress,
                todayQuota = todayQuota,
                totalProgress = totalProgress,
                targetValue = goal.targetValue,
                isOverdue = isOverdue,
                isTodayQuotaMet = isTodayQuotaMet,
                recent14Days = recent14Days,
                summary = summary
            )
        }

        val topGoals = cardStates.take(3)
        val remainingGoals = cardStates.drop(3)

        _uiState.update { state ->
            val initialExpandedId = state.expandedGoalId
                ?: topGoals.firstOrNull()?.goal?.id

            val butlerMsg = if (topGoals.isEmpty()) {
                "오늘 할 목표가 없습니다. 새로운 목표를 등록해 보세요."
            } else {
                "오늘도 한 걸음씩 나아가 봅시다."
            }

            state.copy(
                topGoals = topGoals,
                remainingGoals = remainingGoals,
                expandedGoalId = initialExpandedId,
                butlerMessage = butlerMsg
            )
        }
    }
}
