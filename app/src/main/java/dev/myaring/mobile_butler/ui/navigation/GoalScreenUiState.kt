package dev.myaring.mobile_butler.ui.navigation

import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus

/**
 * [C-5] 목표 화면 전체 상태.
 */
data class GoalScreenUiState(
    val selectedTab: EGoalTab = EGoalTab.LIST,
    val goals: List<Goal> = emptyList(),
    val selectedGoalId: String? = null,
) {
    val activeGoals: List<Goal>
        get() = goals.filter { it.status == GoalStatus.ACTIVE }

    val archivedGoals: List<Goal>
        get() = goals.filter { it.status == GoalStatus.COMPLETED }

    val activeGoalsCount: Int
        get() = activeGoals.size

    val subTabOrdinal: Int
        get() = when (selectedTab) {
            EGoalTab.LIST -> 0
            EGoalTab.ARCHIVE -> 1
            else -> -1
        }
}
