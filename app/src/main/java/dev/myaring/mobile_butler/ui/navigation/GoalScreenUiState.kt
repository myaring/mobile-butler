package dev.myaring.mobile_butler.ui.navigation

import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import dev.myaring.mobile_butler.domain.model.ProgressEntry

/**
 * [C-8] 목표 화면 UI 상태.
 */
data class GoalScreenUiState(
    val selectedTab: EGoalTab = EGoalTab.LIST,
    val goals: List<Goal> = emptyList(),
    val selectedGoalId: String? = null,
    val entries: Map<String, List<ProgressEntry>> = emptyMap(),
) {
    val activeGoals: List<Goal>
        get() = goals.filter { it.status == GoalStatus.ACTIVE }

    val archivedGoals: List<Goal>
        get() = goals.filter { it.status == GoalStatus.COMPLETED }

    val activeGoalsCount: Int
        get() = activeGoals.size

    val selectedGoal: Goal?
        get() = goals.find { it.id == selectedGoalId } ?: goals.firstOrNull()

    val selectedGoalEntries: List<ProgressEntry>
        get() = selectedGoal?.let { entries[it.id] } ?: emptyList()

    val subTabOrdinal: Int
        get() = when (selectedTab) {
            EGoalTab.LIST -> 0
            EGoalTab.ARCHIVE -> 1
            else -> -1
        }
}
