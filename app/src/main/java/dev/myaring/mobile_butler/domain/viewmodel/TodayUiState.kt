package dev.myaring.mobile_butler.domain.viewmodel

import dev.myaring.mobile_butler.domain.model.Goal
import java.time.LocalDate

data class DayCellUiModel(
    val date: LocalDate,
    val isToday: Boolean,
    val hasRecord: Boolean,
)

data class TodayGoalCardUiState(
    val goal: Goal,
    val dDayText: String,
    val todayProgress: Double,
    val todayQuota: Double,
    val totalProgress: Double,
    val targetValue: Double,
    val isOverdue: Boolean,
    val isTodayQuotaMet: Boolean,
    val recent14Days: List<DayCellUiModel>,
    val summary: String,
)

data class TodayUiState(
    val topGoals: List<TodayGoalCardUiState> = emptyList(),
    val remainingGoals: List<TodayGoalCardUiState> = emptyList(),
    val expandedGoalId: String? = null,
    val isMoreExpanded: Boolean = false,
    val butlerMessage: String = "오늘도 한 걸음씩 나아가 봅시다.",
)
