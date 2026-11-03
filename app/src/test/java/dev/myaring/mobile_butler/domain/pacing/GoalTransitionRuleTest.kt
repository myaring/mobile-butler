package dev.myaring.mobile_butler.domain.pacing

import dev.myaring.mobile_butler.domain.model.EndReason
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import org.junit.Assert.*
import org.junit.Test
import java.time.Clock
import java.time.Duration

class GoalTransitionRuleTest {

    private fun reduce(
        target: Goal,
        amount: Double,
        event: GoalEvent,
        clock: Clock = CLOCK,
    ) = GoalTransitionRule.reduce(
        target,
        amount,
        event,
        clock,
    )

    @Test
    fun recordUpdatesProgressWithoutCompleting() {
        val old = goal(dueDate = TODAY.plusDays(1))

        val result = reduce(
            old,
            10.0,
            GoalEvent.Reevaluate,
        )

        assertEquals(old, result.goal)
        assertEquals(
            setOf(GoalEffect.REFRESH_PROGRESS),
            result.effects,
        )
        assertFalse(result.changedFrom(old))
    }

    @Test
    fun recordReachingTargetCompletesAchieved() {
        val result = reduce(
            goal(dueDate = TODAY.plusDays(1)),
            300.0,
            GoalEvent.Reevaluate,
        )

        assertEquals(GoalStatus.COMPLETED, result.goal.status)
        assertEquals(EndReason.ACHIEVED, result.goal.endReason)
        assertEquals(TODAY, result.goal.endedAt)
        assertTrue(GoalEffect.SHOW_ACHIEVED in result.effects)
    }

    @Test
    fun manualFinishBeforeDueDateUsesToday() {
        val result = reduce(
            goal(dueDate = TODAY.plusDays(1)),
            20.0,
            GoalEvent.Finish(),
        )

        assertEquals(EndReason.MANUAL, result.goal.endReason)
        assertEquals(TODAY, result.goal.endedAt)
    }

    @Test
    fun overduePopupIsOneTimeOnly() {
        val old = goal(
            dueDate = TODAY.minusDays(1),
        )

        val first = reduce(
            old,
            20.0,
            GoalEvent.Reevaluate,
        )

        assertEquals(GoalStatus.ACTIVE, first.goal.status)
        assertTrue(first.goal.overdueNoticeShown)
        assertTrue(GoalEffect.SHOW_OVERDUE_ONCE in first.effects)

        val second = reduce(
            first.goal,
            20.0,
            GoalEvent.Reevaluate,
        )

        assertFalse(
            GoalEffect.SHOW_OVERDUE_ONCE in second.effects
        )
        assertFalse(second.changedFrom(first.goal))
    }

    @Test
    fun continueResetsNoticeAndChangesDueDate() {
        val overdue = goal(
            dueDate = TODAY.minusDays(1),
        ).copy(
            overdueNoticeShown = true,
        )

        val result = reduce(
            overdue,
            20.0,
            GoalEvent.Continue(TODAY.plusDays(4)),
        )

        assertEquals(GoalStatus.ACTIVE, result.goal.status)
        assertEquals(TODAY.plusDays(4), result.goal.dueDate)
        assertFalse(result.goal.overdueNoticeShown)
    }

    @Test
    fun continueRejectsTodayOrEarlier() {
        assertThrows(IllegalArgumentException::class.java) {
            reduce(
                goal(dueDate = TODAY.minusDays(1)),
                20.0,
                GoalEvent.Continue(TODAY),
            )
        }
    }

    @Test
    fun wrapUpOverdueUsesChosenDate() {
        val selected = TODAY.minusDays(1)

        val result = reduce(
            goal(dueDate = TODAY.minusDays(3)),
            10.0,
            GoalEvent.Finish(selected),
        )

        assertEquals(GoalStatus.COMPLETED, result.goal.status)
        assertEquals(EndReason.DEADLINE, result.goal.endReason)
        assertEquals(selected, result.goal.endedAt)
    }

    @Test
    fun dismissedOverduePopupDoesNotRepeatOnReevaluation() {
        val notified = goal(
            dueDate = TODAY.minusDays(1),
        ).copy(
            overdueNoticeShown = true,
        )

        val result = reduce(
            notified,
            10.0,
            GoalEvent.Reevaluate,
        )

        assertEquals(notified, result.goal)
        assertFalse(
            GoalEffect.SHOW_OVERDUE_ONCE in result.effects
        )

        val evaluation = GoalStatusRule.evaluate(
            notified,
            10.0,
            CLOCK,
        ) as GoalEvaluation.Active

        assertTrue(evaluation.overdue)
    }

    @Test
    fun directFinishOverdueDefaultsToToday() {
        val result = reduce(
            goal(dueDate = TODAY.minusDays(1)),
            10.0,
            GoalEvent.Finish(),
        )

        assertEquals(EndReason.DEADLINE, result.goal.endReason)
        assertEquals(TODAY, result.goal.endedAt)
    }

    @Test
    fun overdueGoalCanStillAchieveTarget() {
        val result = reduce(
            goal(dueDate = TODAY.minusDays(1)),
            300.0,
            GoalEvent.Reevaluate,
        )

        assertEquals(EndReason.ACHIEVED, result.goal.endReason)
        assertFalse(
            GoalEffect.SHOW_OVERDUE_ONCE in result.effects
        )
    }

    @Test
    fun overdueGoalStaysActiveAfterPartialEntry() {
        val notified = goal(
            dueDate = TODAY.minusDays(1),
        ).copy(
            overdueNoticeShown = true,
        )

        val result = reduce(
            notified,
            30.0,
            GoalEvent.Reevaluate,
        )

        assertEquals(GoalStatus.ACTIVE, result.goal.status)
    }

    @Test
    fun editingTargetRequiresConfirmationBeforeCompletion() {
        val old = goal(
            target = 300.0,
            dueDate = TODAY.plusDays(2),
        )

        val candidate = old.copy(
            targetValue = 100.0,
        )

        val ask = reduce(
            old,
            150.0,
            GoalEvent.EditGoal(candidate),
        )

        assertEquals(old, ask.goal)
        assertTrue(
            GoalEffect.CONFIRM_AUTO_COMPLETION in ask.effects
        )

        val accepted = reduce(
            old,
            150.0,
            GoalEvent.EditGoal(
                candidate,
                confirmedAutoCompletion = true,
            ),
        )

        assertEquals(100.0, accepted.goal.targetValue, 0.0)
        assertEquals(GoalStatus.COMPLETED, accepted.goal.status)
        assertEquals(EndReason.ACHIEVED, accepted.goal.endReason)
    }

    @Test
    fun completedGoalIsNotAutomaticallyReopened() {
        val completed = goal().copy(
            status = GoalStatus.COMPLETED,
            endReason = EndReason.ACHIEVED,
            endedAt = TODAY,
        )

        val result = reduce(
            completed,
            0.0,
            GoalEvent.Reevaluate,
        )

        assertEquals(completed, result.goal)

        assertEquals(
            GoalEvaluation.Completed,
            GoalStatusRule.evaluate(
                completed,
                0.0,
                CLOCK,
            ),
        )
    }

    @Test
    fun undoClearsCompletionMetadata() {
        val completed = goal().copy(
            status = GoalStatus.COMPLETED,
            endReason = EndReason.ACHIEVED,
            endedAt = TODAY,
        )

        val result = reduce(
            completed,
            295.0,
            GoalEvent.UndoCompletion,
        )

        assertEquals(GoalStatus.ACTIVE, result.goal.status)
        assertNull(result.goal.endReason)
        assertNull(result.goal.endedAt)
    }

    @Test
    fun undoAchievedWithoutCompensationIsRejected() {
        val completed = goal().copy(
            status = GoalStatus.COMPLETED,
            endReason = EndReason.ACHIEVED,
            endedAt = TODAY,
        )

        assertThrows(IllegalStateException::class.java) {
            reduce(
                completed,
                300.0,
                GoalEvent.UndoCompletion,
            )
        }
    }

    @Test
    fun undoRejectsAlreadyActiveGoal() {
        assertThrows(IllegalStateException::class.java) {
            reduce(
                goal(),
                0.0,
                GoalEvent.UndoCompletion,
            )
        }
    }

    @Test
    fun deadlineDayIsNotOverdueAndUsesOneDayQuota() {
        val evaluation = GoalStatusRule.evaluate(
            goal(dueDate = TODAY),
            20.0,
            CLOCK,
        ) as GoalEvaluation.Active

        assertEquals(GoalPhase.DUE_TODAY, evaluation.phase)
        assertFalse(evaluation.overdue)

        val quota = PacingCalculator.calculate(
            goal(dueDate = TODAY),
            emptyList(),
            CLOCK,
        ) as PacingResult.Daily

        assertEquals(300.0, quota.quota, 0.0)
        assertEquals(1L, quota.daysRemaining)
    }

    @Test
    fun clockDateChangeAloneTriggersOverdueNotice() {
        val old = goal(dueDate = TODAY)

        val tomorrow = Clock.offset(
            CLOCK,
            Duration.ofDays(1),
        )

        val result = reduce(
            old,
            0.0,
            GoalEvent.Reevaluate,
            tomorrow,
        )

        assertTrue(result.goal.overdueNoticeShown)
        assertTrue(
            GoalEffect.SHOW_OVERDUE_ONCE in result.effects
        )
    }

    @Test
    fun modifyingDeadlineKeepsRecordedProgress() {
        val old = goal(
            dueDate = TODAY.minusDays(1),
        )

        val next = reduce(
            old,
            50.0,
            GoalEvent.Continue(TODAY.plusDays(4)),
        )

        val newPacing = PacingCalculator.calculate(
            next.goal,
            listOf(
                entry(
                    TODAY.minusDays(1),
                    amount = 50.0,
                )
            ),
            CLOCK,
        ) as PacingResult.Daily

        assertEquals(250.0, newPacing.remainingAmount, 0.0)
        assertEquals(50.0, newPacing.quota, 0.0)
    }
}