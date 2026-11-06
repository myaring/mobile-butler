package dev.myaring.mobile_butler.domain.repository

import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.EndReason
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface GoalRepository {

    /**
     * Observe all goals.
     *
     * Ordering:
     * 1. priority ASC
     * 2. id ASC
     *
     * Includes both ACTIVE and COMPLETED goals.
     */
    fun observeGoals(): Flow<List<Goal>>

    /**
     * Observe one goal by id.
     */
    fun observeGoal(id: String): Flow<Goal?>

    /**
     * Returns null if the goal does not exist.
     */
    suspend fun getGoal(id: String): Goal?

    /**
     * Insert or update a goal.
     *
     * Goal model invariants must be preserved.
     */
    suspend fun saveGoal(goal: Goal)

    /**
     * Update the deadline of an active goal.
     *
     * Also resets overdueNoticeShown.
     */
    suspend fun updateDueDate(
        id: String,
        dueDate: LocalDate,
    )

    /**
     * Update goal priorities atomically.
     *
     * Unspecified goals retain their existing priority.
     */
    suspend fun updatePriorities(
        priorities: Map<String, Int>,
    )

    /**
     * Complete an active goal.
     *
     * status becomes COMPLETED.
     * endedAt and endReason are stored together.
     */
    suspend fun completeGoal(
        id: String,
        endReason: EndReason,
        endedAt: LocalDate,
    )

    /**
     * Restore a completed goal to ACTIVE.
     *
     * endedAt and endReason become null.
     *
     * Undo availability is controlled by the
     * application/domain transition rules (#45).
     */
    suspend fun reopenGoal(id: String)

    /**
     * Persist whether the first overdue
     * notification popup has been shown.
     */
    suspend fun setOverdueNoticeShown(
        id: String,
        shown: Boolean,
    )
}
