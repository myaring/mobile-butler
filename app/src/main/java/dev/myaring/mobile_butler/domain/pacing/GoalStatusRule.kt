package dev.myaring.mobile_butler.domain.pacing

import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import java.time.Clock
import java.time.LocalDate

enum class GoalPhase {
    IN_PROGRESS,
    DUE_TODAY,
    OVERDUE,
}

sealed interface GoalEvaluation {
    data class Active(
        val achieved: Boolean,
        val overdue: Boolean,
        val phase: GoalPhase,
    ) : GoalEvaluation

    data object Completed : GoalEvaluation
}

object GoalStatusRule {
    fun evaluate(
        goal: Goal,
        recordedTotal: Double,
        clock: Clock,
    ): GoalEvaluation {
        require(recordedTotal.isFinite() && recordedTotal >= 0.0)

        if (goal.status == GoalStatus.COMPLETED) {
            return GoalEvaluation.Completed
        }

        @Suppress("DEPRECATION")
        require(goal.status != GoalStatus.ARCHIVED)

        val current = goal.startValue + recordedTotal

        require(current.isFinite())

        val today = LocalDate.now(clock)
        val dueDate = goal.dueDate

        val phase = when {
            dueDate == null || today.isBefore(dueDate) ->
                GoalPhase.IN_PROGRESS

            today.isEqual(dueDate) ->
                GoalPhase.DUE_TODAY

            else ->
                GoalPhase.OVERDUE
        }

        return GoalEvaluation.Active(
            achieved = current >= goal.targetValue,
            overdue = phase == GoalPhase.OVERDUE,
            phase = phase,
        )
    }
}