package dev.myaring.mobile_butler.domain.pacing

import dev.myaring.mobile_butler.domain.model.EndReason
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import java.time.Clock
import java.time.LocalDate

sealed interface GoalEvent {
    data object Reevaluate : GoalEvent

    data class EditGoal(
        val candidate: Goal,
        val confirmedAutoCompletion: Boolean = false,
    ) : GoalEvent

    data class Continue(
        val newDueDate: LocalDate,
    ) : GoalEvent

    data class Finish(
        val chosenDate: LocalDate? = null,
    ) : GoalEvent

    data object UndoCompletion : GoalEvent
}

enum class GoalEffect {
    SHOW_ACHIEVED,
    SHOW_OVERDUE_ONCE,
    CONFIRM_AUTO_COMPLETION,
    SHOW_UNDO,
    REFRESH_PROGRESS,
}

data class GoalTransition(
    val goal: Goal,
    val effects: Set<GoalEffect> = emptySet(),
) {
    fun changedFrom(previous: Goal): Boolean =
        goal != previous
}

object GoalTransitionRule {
    fun reduce(
        goal: Goal,
        recordedTotal: Double,
        event: GoalEvent,
        clock: Clock,
    ): GoalTransition {
        require(
            recordedTotal.isFinite() &&
                    recordedTotal >= 0.0
        )

        val today = LocalDate.now(clock)

        return when (event) {
            GoalEvent.Reevaluate ->
                reevaluate(
                    goal,
                    recordedTotal,
                    today,
                    clock,
                )

            is GoalEvent.EditGoal ->
                editGoal(
                    goal,
                    recordedTotal,
                    event,
                    today,
                    clock,
                )

            is GoalEvent.Continue ->
                continueGoal(
                    goal,
                    event.newDueDate,
                    today,
                )

            is GoalEvent.Finish ->
                finishGoal(
                    goal,
                    recordedTotal,
                    event.chosenDate,
                    today,
                )

            GoalEvent.UndoCompletion ->
                undo(
                    goal,
                    recordedTotal,
                )
        }
    }

    private fun reevaluate(
        goal: Goal,
        recordedTotal: Double,
        today: LocalDate,
        clock: Clock,
    ): GoalTransition {
        val evaluation = GoalStatusRule.evaluate(
            goal,
            recordedTotal,
            clock,
        )

        if (evaluation == GoalEvaluation.Completed) {
            return GoalTransition(goal)
        }

        evaluation as GoalEvaluation.Active

        if (evaluation.achieved) {
            return GoalTransition(
                goal = goal.copy(
                    status = GoalStatus.COMPLETED,
                    endReason = EndReason.ACHIEVED,
                    endedAt = today,
                ),
                effects = setOf(
                    GoalEffect.SHOW_ACHIEVED,
                    GoalEffect.SHOW_UNDO,
                ),
            )
        }

        if (
            evaluation.overdue &&
            !goal.overdueNoticeShown
        ) {
            return GoalTransition(
                goal = goal.copy(
                    overdueNoticeShown = true,
                ),
                effects = setOf(
                    GoalEffect.SHOW_OVERDUE_ONCE,
                ),
            )
        }

        return GoalTransition(
            goal,
            setOf(GoalEffect.REFRESH_PROGRESS),
        )
    }

    private fun editGoal(
        previous: Goal,
        recordedTotal: Double,
        event: GoalEvent.EditGoal,
        today: LocalDate,
        clock: Clock,
    ): GoalTransition {
        check(previous.status == GoalStatus.ACTIVE)

        require(event.candidate.id == previous.id)
        require(event.candidate.status == GoalStatus.ACTIVE)

        val oldValue = previous.startValue + recordedTotal
        val newValue = event.candidate.startValue + recordedTotal

        require(oldValue.isFinite() && newValue.isFinite())

        val oldReached = oldValue >= previous.targetValue
        val newReached = newValue >= event.candidate.targetValue

        if (
            !oldReached &&
            newReached &&
            !event.confirmedAutoCompletion
        ) {
            return GoalTransition(
                previous,
                setOf(GoalEffect.CONFIRM_AUTO_COMPLETION),
            )
        }

        val resetNotice =
            event.candidate.dueDate != previous.dueDate &&
                    event.candidate.dueDate?.isBefore(today) == false

        val newGoal = event.candidate.copy(
            overdueNoticeShown = if (resetNotice) {
                false
            } else {
                previous.overdueNoticeShown
            },
        )

        return reevaluate(
            newGoal,
            recordedTotal,
            today,
            clock,
        )
    }

    private fun continueGoal(
        goal: Goal,
        newDueDate: LocalDate,
        today: LocalDate,
    ): GoalTransition {
        check(goal.status == GoalStatus.ACTIVE)

        check(goal.dueDate?.isBefore(today) == true) {
            "Continue is only for overdue goals."
        }

        require(newDueDate.isAfter(today)) {
            "A continued goal must have a due date after today."
        }

        return GoalTransition(
            goal = goal.copy(
                dueDate = newDueDate,
                overdueNoticeShown = false,
            ),
            effects = setOf(
                GoalEffect.REFRESH_PROGRESS,
            ),
        )
    }

    private fun finishGoal(
        goal: Goal,
        recordedTotal: Double,
        chosenDate: LocalDate?,
        today: LocalDate,
    ): GoalTransition {
        check(goal.status == GoalStatus.ACTIVE)

        val current = goal.startValue + recordedTotal

        require(current.isFinite())

        val achieved = current >= goal.targetValue
        val overdue = goal.dueDate?.isBefore(today) == true

        val reason = when {
            achieved -> EndReason.ACHIEVED
            overdue -> EndReason.DEADLINE
            else -> EndReason.MANUAL
        }

        val endedAt = if (reason == EndReason.DEADLINE) {
            chosenDate ?: today
        } else {
            today
        }

        require(
            !endedAt.isBefore(goal.startDate) &&
                    !endedAt.isAfter(today)
        )

        val effects = if (reason == EndReason.ACHIEVED) {
            setOf(
                GoalEffect.SHOW_ACHIEVED,
                GoalEffect.SHOW_UNDO,
            )
        } else {
            setOf(
                GoalEffect.SHOW_UNDO,
            )
        }

        return GoalTransition(
            goal = goal.copy(
                status = GoalStatus.COMPLETED,
                endReason = reason,
                endedAt = endedAt,
            ),
            effects = effects,
        )
    }

    private fun undo(
        goal: Goal,
        recordedTotal: Double,
    ): GoalTransition {
        check(goal.status == GoalStatus.COMPLETED)

        if (goal.endReason == EndReason.ACHIEVED) {
            val current = goal.startValue + recordedTotal

            require(current.isFinite())

            check(current < goal.targetValue) {
                "Undo ACHIEVED requires a compensating entry or target edit."
            }
        }

        return GoalTransition(
            goal = goal.copy(
                status = GoalStatus.ACTIVE,
                endReason = null,
                endedAt = null,
            ),
            effects = setOf(
                GoalEffect.REFRESH_PROGRESS,
            ),
        )
    }
}