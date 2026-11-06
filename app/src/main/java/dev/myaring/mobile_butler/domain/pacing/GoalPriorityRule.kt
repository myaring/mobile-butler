package dev.myaring.mobile_butler.domain.pacing

import dev.myaring.mobile_butler.domain.model.Goal

object GoalPriorityRule {
    const val TOP_GOALS_TODAY_LIMIT: Int = 3

    /**
     * Reorders a list of goals by moving the item at [fromIndex] to [toIndex].
     * Normalizes priorities sequentially (0, 1, 2, ...).
     */
    fun reorder(goals: List<Goal>, fromIndex: Int, toIndex: Int): List<Goal> {
        if (goals.size <= 1 || fromIndex == toIndex) {
            return normalize(goals)
        }

        if (fromIndex !in goals.indices || toIndex !in goals.indices) {
            return normalize(goals)
        }

        val mutable = goals.toMutableList()
        val item = mutable.removeAt(fromIndex)
        mutable.add(toIndex, item)

        return normalize(mutable)
    }

    /**
     * Moves a goal one position up.
     */
    fun moveUp(goals: List<Goal>, index: Int): List<Goal> {
        if (index <= 0 || index >= goals.size) return goals
        return reorder(goals, index, index - 1)
    }

    /**
     * Moves a goal one position down.
     */
    fun moveDown(goals: List<Goal>, index: Int): List<Goal> {
        if (index < 0 || index >= goals.size - 1) return goals
        return reorder(goals, index, index + 1)
    }

    /**
     * Re-normalizes priorities so they are strictly sequential from 0 to N-1.
     */
    fun normalize(goals: List<Goal>): List<Goal> {
        return goals.mapIndexed { index, goal ->
            if (goal.priority == index) goal else goal.copy(priority = index)
        }
    }

    /**
     * Checks whether a goal is displayed expanded in the Today view (top 3 priority goals).
     */
    fun isTopTodayGoal(priority: Int): Boolean {
        return priority in 0 until TOP_GOALS_TODAY_LIMIT
    }
}
